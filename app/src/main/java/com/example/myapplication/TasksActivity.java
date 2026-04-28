package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TasksActivity extends AppCompatActivity {

    private RecyclerView rvTasks;
    private Spinner spStatusFilter, spCategoryFilter;
    private Button btnManageCategories;
    private FloatingActionButton fabAddTask;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;
    private ArrayList<Category> categories;
    private String currentUserId = "";
    private static final String ALL_CATEGORIES = "All Categories";
    private final List<String> statusOptions = Arrays.asList(
            "All",
            Task.STATUS_IN_PROGRESS,
            Task.STATUS_COMPLETED,
            "Archived"
    );

    private final ActivityResultLauncher<Intent> addTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task task = (Task) result.getData().getSerializableExtra(AddTaskActivity.EXTRA_TASK);
                    if (task != null) {
                        allTasks.add(task);
                        refreshFilter();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> editTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task task = (Task) result.getData().getSerializableExtra(AddTaskActivity.EXTRA_TASK);
                    int index = result.getData().getIntExtra(AddTaskActivity.EXTRA_TASK_INDEX, -1);
                    if (task != null && index >= 0 && index < allTasks.size()) {
                        allTasks.set(index, task);
                        refreshFilter();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> categoriesLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<Category> updatedCategories = readCategoriesResult(result.getData());
                    if (updatedCategories != null) {
                        categories = updatedCategories;
                        normalizeTaskCategories();
                        setupCategoryFilter();
                        refreshFilter();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks);

        rvTasks = findViewById(R.id.rv_tasks);
        spStatusFilter = findViewById(R.id.sp_status_filter);
        spCategoryFilter = findViewById(R.id.sp_category_filter);
        btnManageCategories = findViewById(R.id.btn_manage_categories);
        fabAddTask = findViewById(R.id.fab_add_task);

        currentUserId = getIntent().getStringExtra("email");
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            currentUserId = "local-user";
        }

        categories = Category.defaultCategories(currentUserId);
        allTasks = createSampleTasks(currentUserId);
        normalizeTaskCategories();

        taskAdapter = new TaskAdapter(new ArrayList<>(allTasks));
        taskAdapter.setOnTaskStatusChangedListener(this::refreshFilter);
        taskAdapter.setOnTaskDeleteListener(this::confirmDeleteTask);
        taskAdapter.setOnTaskArchiveChangedListener(this::toggleArchivedTask);
        taskAdapter.setOnTaskClickListener((task, position) -> {
            int realIndex = allTasks.indexOf(task);
            if (realIndex < 0) return;
            Intent intent = new Intent(TasksActivity.this, AddTaskActivity.class);
            intent.putExtra(AddTaskActivity.EXTRA_TASK, task);
            intent.putExtra(AddTaskActivity.EXTRA_TASK_INDEX, realIndex);
            intent.putExtra(AddTaskActivity.EXTRA_USER_ID, currentUserId);
            intent.putExtra(AddTaskActivity.EXTRA_CATEGORIES, categories);
            editTaskLauncher.launch(intent);
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);

        setupStatusFilter();
        setupCategoryFilter();

        btnManageCategories.setOnClickListener(v -> {
            Intent intent = new Intent(TasksActivity.this, CategoriesActivity.class);
            intent.putExtra(CategoriesActivity.EXTRA_USER_ID, currentUserId);
            intent.putExtra(CategoriesActivity.EXTRA_CATEGORIES, categories);
            categoriesLauncher.launch(intent);
        });

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(TasksActivity.this, AddTaskActivity.class);
            intent.putExtra(AddTaskActivity.EXTRA_USER_ID, currentUserId);
            intent.putExtra(AddTaskActivity.EXTRA_CATEGORIES, categories);
            addTaskLauncher.launch(intent);
        });
    }

    private void setupStatusFilter() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatusFilter.setAdapter(spinnerAdapter);

        spStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupCategoryFilter() {
        String previousSelection = spCategoryFilter.getSelectedItem() == null
                ? ALL_CATEGORIES
                : spCategoryFilter.getSelectedItem().toString();
        ArrayList<String> categoryOptions = new ArrayList<>();
        categoryOptions.add(ALL_CATEGORIES);
        for (Category category : categories) {
            categoryOptions.add(category.getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoryFilter.setAdapter(spinnerAdapter);

        int selectedPosition = categoryOptions.indexOf(previousSelection);
        spCategoryFilter.setSelection(selectedPosition >= 0 ? selectedPosition : 0);
        spCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applyFilter(String status, String categoryFilter) {
        List<Task> filtered = new ArrayList<>();
        for (Task task : allTasks) {
            if (matchesStatus(task, status) && matchesCategory(task, categoryFilter)) {
                filtered.add(task);
            }
        }
        taskAdapter.setTasks(filtered);
    }

    private boolean matchesStatus(Task task, String status) {
        if ("All".equals(status)) {
            return !task.isArchived();
        }
        if ("Archived".equals(status)) {
            return task.isArchived();
        }
        return !task.isArchived() && task.getStatus().equals(status);
    }

    private boolean matchesCategory(Task task, String categoryFilter) {
        return ALL_CATEGORIES.equals(categoryFilter) || task.getCategory().equals(categoryFilter);
    }

    private void refreshFilter() {
        String selectedStatus = statusOptions.get(spStatusFilter.getSelectedItemPosition());
        String selectedCategory = spCategoryFilter.getSelectedItem() == null
                ? ALL_CATEGORIES
                : spCategoryFilter.getSelectedItem().toString();
        applyFilter(selectedStatus, selectedCategory);
    }

    private void confirmDeleteTask(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete task")
                .setMessage("This task will be removed from the list.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteTask(task))
                .show();
    }

    private void deleteTask(Task task) {
        if (allTasks.remove(task)) {
            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
            refreshFilter();
        }
    }

    private void toggleArchivedTask(Task task) {
        task.setArchived(!task.isArchived());
        Toast.makeText(
                this,
                task.isArchived() ? "Task archived" : "Task restored",
                Toast.LENGTH_SHORT
        ).show();
        refreshFilter();
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Category> readCategoriesResult(Intent data) {
        Object value = data.getSerializableExtra(CategoriesActivity.EXTRA_CATEGORIES);
        if (value instanceof ArrayList) {
            ArrayList<Category> result = new ArrayList<>((ArrayList<Category>) value);
            Category.ensureGeneralCategory(result, currentUserId);
            return result;
        }
        return null;
    }

    private void normalizeTaskCategories() {
        Category general = Category.ensureGeneralCategory(categories, currentUserId);
        for (Task task : allTasks) {
            Category category = findCategoryById(task.getCategoryId());
            if (category == null) {
                category = findCategoryByName(task.getCategory());
            }
            if (category == null) {
                category = general;
            }
            task.setCategoryId(category.getId());
            task.setCategory(category.getName());
        }
    }

    private Category findCategoryById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        for (Category category : categories) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        return null;
    }

    private Category findCategoryByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }

    private List<Task> createSampleTasks(String userId) {
        List<Task> tasks = new ArrayList<>();

        tasks.add(new Task(
                userId,
                "Test1",
                "First test task description",
                "High", Task.STATUS_IN_PROGRESS, "Work", "10 April 2026",
                Arrays.asList(
                        new Subtask("Subtask A", true),
                        new Subtask("Subtask B", false),
                        new Subtask("Subtask C", false)
                )
        ));

        tasks.add(new Task(
                userId,
                "Test2",
                "Second test task description",
                "Medium", Task.STATUS_COMPLETED, "Personal", "8 April 2026",
                Arrays.asList(
                        new Subtask("Subtask A", true),
                        new Subtask("Subtask B", true)
                )
        ));

        tasks.add(new Task(
                userId,
                "Test3",
                "Third test task description",
                "Low", Task.STATUS_IN_PROGRESS, "Study", "16 April 2026",
                Arrays.asList(
                        new Subtask("Subtask A", false)
                )
        ));

        return tasks;
    }
}

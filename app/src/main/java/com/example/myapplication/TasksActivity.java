package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    private Spinner spStatusFilter;
    private FloatingActionButton fabAddTask;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;
    private final List<String> statusOptions = Arrays.asList("All", "In Progress", "Completed", "Archived");

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks);

        rvTasks = findViewById(R.id.rv_tasks);
        spStatusFilter = findViewById(R.id.sp_category_filter);
        fabAddTask = findViewById(R.id.fab_add_task);

        allTasks = createSampleTasks();

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
            editTaskLauncher.launch(intent);
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);

        setupStatusFilter();

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(TasksActivity.this, AddTaskActivity.class);
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
                applyFilter(statusOptions.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applyFilter(String status) {
        if ("All".equals(status)) {
            List<Task> active = new ArrayList<>();
            for (Task task : allTasks) {
                if (!task.isArchived()) {
                    active.add(task);
                }
            }
            taskAdapter.setTasks(active);
        } else if ("Archived".equals(status)) {
            List<Task> archived = new ArrayList<>();
            for (Task task : allTasks) {
                if (task.isArchived()) {
                    archived.add(task);
                }
            }
            taskAdapter.setTasks(archived);
        } else {
            List<Task> filtered = new ArrayList<>();
            for (Task task : allTasks) {
                if (!task.isArchived() && task.getStatus().equals(status)) {
                    filtered.add(task);
                }
            }
            taskAdapter.setTasks(filtered);
        }
    }

    private void refreshFilter() {
        String selected = statusOptions.get(spStatusFilter.getSelectedItemPosition());
        applyFilter(selected);
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

    private List<Task> createSampleTasks() {
        List<Task> tasks = new ArrayList<>();

        tasks.add(new Task(
                "Test1",
                "First test task description",
                "High", "In Progress", "Work", "10 April 2026",
                Arrays.asList(
                        new Subtask("Subtask A", true),
                        new Subtask("Subtask B", false),
                        new Subtask("Subtask C", false)
                )
        ));

        tasks.add(new Task(
                "Test2",
                "Second test task description",
                "Medium", "Completed", "Personal", "8 April 2026",
                Arrays.asList(
                        new Subtask("Subtask A", true),
                        new Subtask("Subtask B", true)
                )
        ));

        tasks.add(new Task(
                "Test3",
                "Third test task description",
                "Low", "In Progress", "Study", "16 April 2026",
                Arrays.asList(
                        new Subtask("Subtask A", false)
                )
        ));

        return tasks;
    }
}

package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK = "extra_task";
    public static final String EXTRA_TASK_INDEX = "extra_task_index";
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_CATEGORIES = "extra_categories";

    private EditText etTitle, etDescription, etSubtaskInput;
    private Spinner spPriority, spCategory;
    private Button btnPickDate, btnAddSubtask, btnSave, btnCancel;
    private TextView tvSelectedDate, tvScreenTitle;
    private RecyclerView rvSubtasks;

    private SubtaskAdapter subtaskAdapter;
    private List<Subtask> subtasks;
    private String selectedDate = "";
    private String taskId = "";
    private String userId = "";
    private String categoryId = "";
    private String categoryName = Category.GENERAL_NAME;
    private String taskStatus = Task.STATUS_IN_PROGRESS;
    private String createdAt = "";
    private String completedAt = null;
    private boolean archived = false;
    private int editIndex = -1;
    private ArrayList<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        tvScreenTitle = findViewById(R.id.tv_add_task_title);
        etTitle = findViewById(R.id.et_task_title);
        etDescription = findViewById(R.id.et_task_description);
        spPriority = findViewById(R.id.sp_task_priority);
        spCategory = findViewById(R.id.sp_task_category);
        btnPickDate = findViewById(R.id.btn_pick_date);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        etSubtaskInput = findViewById(R.id.et_subtask_input);
        btnAddSubtask = findViewById(R.id.btn_add_subtask);
        rvSubtasks = findViewById(R.id.rv_subtasks);
        btnSave = findViewById(R.id.btn_save_task);
        btnCancel = findViewById(R.id.btn_cancel_task);

        subtasks = new ArrayList<>();
        subtaskAdapter = new SubtaskAdapter(subtasks);
        rvSubtasks.setLayoutManager(new LinearLayoutManager(this));
        rvSubtasks.setAdapter(subtaskAdapter);

        // Check if editing an existing task
        Intent intent = getIntent();
        userId = intent.getStringExtra(EXTRA_USER_ID);
        if (userId == null) {
            userId = "";
        }
        categories = readCategoriesFromIntent(intent);
        setupCategorySpinner();
        if (intent.hasExtra(EXTRA_TASK)) {
            Task task = (Task) intent.getSerializableExtra(EXTRA_TASK);
            editIndex = intent.getIntExtra(EXTRA_TASK_INDEX, -1);
            tvScreenTitle.setText("Edit Task");
            populateFields(task);
        }

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnAddSubtask.setOnClickListener(v -> addSubtask());
        btnSave.setOnClickListener(v -> saveTask());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void populateFields(Task task) {
        taskId = task.getId();
        if (task.getUserId() != null && !task.getUserId().trim().isEmpty()) {
            userId = task.getUserId();
        }
        categoryId = task.getCategoryId();
        categoryName = task.getCategory();
        createdAt = task.getCreatedAt();
        completedAt = task.getCompletedAt();
        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());
        taskStatus = task.getStatus();
        archived = task.isArchived();
        selectCategory(task);

        // Set priority spinner selection
        String priority = task.getPriority();
        String[] priorities = getResources().getStringArray(R.array.priority_options);
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].equals(priority)) {
                spPriority.setSelection(i);
                break;
            }
        }

        // Set date
        if (task.getReminderDate() != null && !task.getReminderDate().isEmpty()) {
            selectedDate = task.getReminderDate();
            tvSelectedDate.setText(selectedDate);
            tvSelectedDate.setVisibility(TextView.VISIBLE);
        }

        // Populate subtasks
        if (task.getSubtasks() != null) {
            for (Subtask s : task.getSubtasks()) {
                subtasks.add(new Subtask(s.getTitle(), s.isDone()));
            }
            subtaskAdapter.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Category> readCategoriesFromIntent(Intent intent) {
        Object value = intent.getSerializableExtra(EXTRA_CATEGORIES);
        if (value instanceof ArrayList) {
            ArrayList<Category> result = new ArrayList<>((ArrayList<Category>) value);
            Category.ensureGeneralCategory(result, userId);
            return result;
        }
        return Category.defaultCategories(userId);
    }

    private void setupCategorySpinner() {
        ArrayList<String> names = new ArrayList<>();
        for (Category category : categories) {
            names.add(category.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
        selectCategoryByName(Category.GENERAL_NAME);
    }

    private void selectCategory(Task task) {
        if (selectCategoryById(task.getCategoryId())) {
            return;
        }
        if (!selectCategoryByName(task.getCategory())) {
            selectCategoryByName(Category.GENERAL_NAME);
        }
    }

    private boolean selectCategoryById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(id)) {
                spCategory.setSelection(i);
                return true;
            }
        }
        return false;
    }

    private boolean selectCategoryByName(String name) {
        if (name == null) {
            return false;
        }
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equalsIgnoreCase(name)) {
                spCategory.setSelection(i);
                return true;
            }
        }
        return false;
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            selectedDate = sdf.format(cal.getTime());
            tvSelectedDate.setText(selectedDate);
            tvSelectedDate.setVisibility(TextView.VISIBLE);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addSubtask() {
        String text = etSubtaskInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Enter subtask title", Toast.LENGTH_SHORT).show();
            return;
        }
        subtasks.add(new Subtask(text, false));
        subtaskAdapter.notifyItemInserted(subtasks.size() - 1);
        etSubtaskInput.setText("");
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        String description = etDescription.getText().toString().trim();
        String priority = spPriority.getSelectedItem().toString();
        Category selectedCategory = categories.get(spCategory.getSelectedItemPosition());
        categoryId = selectedCategory.getId();
        categoryName = selectedCategory.getName();

        Task task = new Task(
                taskId,
                userId,
                title,
                description,
                priority,
                taskStatus,
                categoryId,
                categoryName,
                selectedDate,
                new ArrayList<>(subtaskAdapter.getSubtasks()),
                archived,
                createdAt,
                Task.nowTimestamp(),
                completedAt
        );

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_TASK, task);
        if (editIndex >= 0) {
            resultIntent.putExtra(EXTRA_TASK_INDEX, editIndex);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}

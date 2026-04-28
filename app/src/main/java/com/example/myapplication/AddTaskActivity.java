package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

    private EditText etTitle, etDescription, etCategory, etSubtaskInput;
    private Spinner spPriority;
    private Button btnPickDate, btnAddSubtask, btnSave, btnCancel;
    private TextView tvSelectedDate, tvScreenTitle;
    private RecyclerView rvSubtasks;

    private SubtaskAdapter subtaskAdapter;
    private List<Subtask> subtasks;
    private String selectedDate = "";
    private String taskId = "";
    private String userId = "";
    private String taskStatus = Task.STATUS_IN_PROGRESS;
    private String createdAt = "";
    private String completedAt = null;
    private boolean archived = false;
    private int editIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        tvScreenTitle = findViewById(R.id.tv_add_task_title);
        etTitle = findViewById(R.id.et_task_title);
        etDescription = findViewById(R.id.et_task_description);
        spPriority = findViewById(R.id.sp_task_priority);
        etCategory = findViewById(R.id.et_task_category);
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
        userId = task.getUserId();
        createdAt = task.getCreatedAt();
        completedAt = task.getCompletedAt();
        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());
        etCategory.setText(task.getCategory());
        taskStatus = task.getStatus();
        archived = task.isArchived();

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
        String category = etCategory.getText().toString().trim();
        if (category.isEmpty()) {
            category = "General";
        }

        Task task = new Task(
                taskId,
                userId,
                title,
                description,
                priority,
                taskStatus,
                category,
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

package com.example.myapplication;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Task implements Serializable {
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_COMPLETED = "Completed";

    private String id;
    private String userId;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String category;
    private String reminderDate;
    private List<Subtask> subtasks;
    private boolean archived;
    private String createdAt;
    private String updatedAt;
    private String completedAt;

    public Task(String title, String description, String priority, String status,
                String category, String reminderDate, List<Subtask> subtasks) {
        this("", title, description, priority, status, category, reminderDate, subtasks, false);
    }

    public Task(String title, String description, String priority, String status,
                String category, String reminderDate, List<Subtask> subtasks, boolean archived) {
        this("", title, description, priority, status, category, reminderDate, subtasks, archived);
    }

    public Task(String userId, String title, String description, String priority, String status,
                String category, String reminderDate, List<Subtask> subtasks) {
        this(userId, title, description, priority, status, category, reminderDate, subtasks, false);
    }

    public Task(String userId, String title, String description, String priority, String status,
                String category, String reminderDate, List<Subtask> subtasks, boolean archived) {
        this(null, userId, title, description, priority, status, category, reminderDate, subtasks,
                archived, null, null, null);
    }

    public Task(String id, String userId, String title, String description, String priority,
                String status, String category, String reminderDate, List<Subtask> subtasks,
                boolean archived, String createdAt, String updatedAt, String completedAt) {
        String now = nowTimestamp();
        this.id = isBlank(id) ? UUID.randomUUID().toString() : id;
        this.userId = userId == null ? "" : userId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.category = category;
        this.reminderDate = reminderDate;
        this.subtasks = subtasks;
        this.archived = archived;
        this.createdAt = isBlank(createdAt) ? now : createdAt;
        this.updatedAt = isBlank(updatedAt) ? now : updatedAt;
        this.completedAt = completedAt;
        syncCompletedAt();
    }

    public static String nowTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(new Date());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void touch() {
        updatedAt = nowTimestamp();
    }

    private void syncCompletedAt() {
        if (STATUS_COMPLETED.equals(status)) {
            if (isBlank(completedAt)) {
                completedAt = updatedAt;
            }
        } else {
            completedAt = null;
        }
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
        touch();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (this.status != null && this.status.equals(status)) {
            return;
        }
        this.status = status;
        touch();
        syncCompletedAt();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        touch();
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
        touch();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
        touch();
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        if (this.archived == archived) {
            return;
        }
        this.archived = archived;
        touch();
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }
}

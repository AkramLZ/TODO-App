package com.example.myapplication;

import java.io.Serializable;

public class Subtask implements Serializable {
    private String id;
    private String taskId;
    private String title;
    private String description;
    private boolean done;
    private String createdAt;
    private String updatedAt;

    public Subtask(String title, boolean done) {
        this("", title, "", done);
    }

    public Subtask(String title, String description, boolean done) {
        this("", title, description, done);
    }

    public Subtask(String taskId, String title, String description, boolean done) {
        this(Task.generateId(), taskId, title, description, done, null, null);
    }

    public Subtask(String id, String taskId, String title, String description, boolean done,
                   String createdAt, String updatedAt) {
        String now = Task.nowTimestamp();
        this.id = isBlank(id) ? Task.generateId() : id;
        this.taskId = taskId == null ? "" : taskId;
        this.title = title;
        this.description = description == null ? "" : description;
        this.done = done;
        this.createdAt = isBlank(createdAt) ? now : createdAt;
        this.updatedAt = isBlank(updatedAt) ? now : updatedAt;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void touch() {
        updatedAt = Task.nowTimestamp();
    }

    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        if (this.taskId != null && this.taskId.equals(taskId)) {
            return;
        }
        this.taskId = taskId == null ? "" : taskId;
        touch();
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
        this.description = description == null ? "" : description;
        touch();
    }

    public boolean isDone() {
        return done;
    }

    public boolean isCompleted() {
        return done;
    }

    public void setDone(boolean done) {
        if (this.done == done) {
            return;
        }
        this.done = done;
        touch();
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}

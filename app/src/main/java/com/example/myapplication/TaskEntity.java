package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String userId;
    public String title;
    public String description;
    public String priority;
    public String status;
    public String categoryId;
    public String category;
    public String reminderDate;
    public boolean archived;
    public String createdAt;
    public String updatedAt;
    public String completedAt;

    public TaskEntity(
            @NonNull String id,
            String userId,
            String title,
            String description,
            String priority,
            String status,
            String categoryId,
            String category,
            String reminderDate,
            boolean archived,
            String createdAt,
            String updatedAt,
            String completedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.categoryId = categoryId;
        this.category = category;
        this.reminderDate = reminderDate;
        this.archived = archived;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }
}

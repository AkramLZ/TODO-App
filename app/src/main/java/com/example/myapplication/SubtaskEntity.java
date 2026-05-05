package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.room3.Entity;
import androidx.room3.ForeignKey;
import androidx.room3.Index;
import androidx.room3.PrimaryKey;

@Entity(
        tableName = "subtasks",
        foreignKeys = @ForeignKey(
                entity = TaskEntity.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = {"taskId"})
)
public class SubtaskEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String taskId;
    public String title;
    public String description;
    public boolean done;
    public String createdAt;
    public String updatedAt;

    public SubtaskEntity(
            @NonNull String id,
            String taskId,
            String title,
            String description,
            boolean done,
            String createdAt,
            String updatedAt
    ) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.done = done;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

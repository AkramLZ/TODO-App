package com.example.myapplication;

import androidx.room3.Embedded;
import androidx.room3.Relation;

import java.util.List;

public class TaskWithSubtasks {
    @Embedded
    public TaskEntity task;

    @Relation(parentColumn = "id", entityColumn = "taskId")
    public List<SubtaskEntity> subtasks;
}

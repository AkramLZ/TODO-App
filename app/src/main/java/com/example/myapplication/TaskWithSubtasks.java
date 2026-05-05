package com.example.myapplication;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TaskWithSubtasks {
    @Embedded
    public TaskEntity task;

    @Relation(parentColumn = "id", entityColumn = "taskId")
    public List<SubtaskEntity> subtasks;
}

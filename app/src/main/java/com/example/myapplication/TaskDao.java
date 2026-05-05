package com.example.myapplication;

import androidx.room3.Dao;
import androidx.room3.Insert;
import androidx.room3.OnConflictStrategy;
import androidx.room3.Query;
import androidx.room3.Transaction;

import java.util.List;

@Dao
public interface TaskDao {
    @Transaction
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY createdAt ASC")
    List<TaskWithSubtasks> getTasksForUser(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertTask(TaskEntity task);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertSubtasks(List<SubtaskEntity> subtasks);

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    void deleteSubtasksForTask(String taskId);

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTaskById(String taskId);

    @Transaction
    default void upsertTaskWithSubtasks(TaskEntity task, List<SubtaskEntity> subtasks) {
        upsertTask(task);
        deleteSubtasksForTask(task.id);
        if (!subtasks.isEmpty()) {
            upsertSubtasks(subtasks);
        }
    }
}

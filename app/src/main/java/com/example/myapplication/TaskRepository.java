package com.example.myapplication;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TaskRepository {
    private TaskRepository() {
    }

    public static ArrayList<Task> loadTasks(Context context, String userId) {
        List<TaskWithSubtasks> storedTasks = AppDatabase.getInstance(context)
                .taskDao()
                .getTasksForUser(normalizedUserId(userId));
        ArrayList<Task> tasks = new ArrayList<>();
        for (TaskWithSubtasks storedTask : storedTasks) {
            tasks.add(toDomainTask(storedTask));
        }
        return tasks;
    }

    public static void saveTask(Context context, Task task) {
        AppDatabase.getInstance(context)
                .taskDao()
                .upsertTaskWithSubtasks(toTaskEntity(task), toSubtaskEntities(task));
    }

    public static void saveTasks(Context context, List<Task> tasks) {
        TaskDao dao = AppDatabase.getInstance(context).taskDao();
        for (Task task : tasks) {
            dao.upsertTaskWithSubtasks(toTaskEntity(task), toSubtaskEntities(task));
        }
    }

    public static void deleteTask(Context context, Task task) {
        AppDatabase.getInstance(context)
                .taskDao()
                .deleteTaskById(task.getId());
    }

    private static String normalizedUserId(String userId) {
        return userId == null || userId.trim().isEmpty() ? "local-user" : userId;
    }

    private static TaskEntity toTaskEntity(Task task) {
        return new TaskEntity(
                nullToEmpty(task.getId()),
                nullToEmpty(task.getUserId()),
                nullToEmpty(task.getTitle()),
                nullToEmpty(task.getDescription()),
                nullToEmpty(task.getPriority()),
                nullToEmpty(task.getStatus()),
                nullToEmpty(task.getCategoryId()),
                nullToEmpty(task.getCategory()),
                nullToEmpty(task.getReminderDate()),
                task.isArchived(),
                nullToEmpty(task.getCreatedAt()),
                nullToEmpty(task.getUpdatedAt()),
                task.getCompletedAt()
        );
    }

    private static List<SubtaskEntity> toSubtaskEntities(Task task) {
        List<Subtask> subtasks = task.getSubtasks();
        if (subtasks == null || subtasks.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<SubtaskEntity> entities = new ArrayList<>();
        String taskId = task.getId();
        for (Subtask subtask : subtasks) {
            entities.add(new SubtaskEntity(
                    nullToEmpty(subtask.getId()),
                    nullToEmpty(taskId),
                    nullToEmpty(subtask.getTitle()),
                    nullToEmpty(subtask.getDescription()),
                    subtask.isDone(),
                    nullToEmpty(subtask.getCreatedAt()),
                    nullToEmpty(subtask.getUpdatedAt())
            ));
        }
        return entities;
    }

    private static Task toDomainTask(TaskWithSubtasks storedTask) {
        List<Subtask> subtasks = new ArrayList<>();
        if (storedTask.subtasks != null) {
            for (SubtaskEntity subtask : storedTask.subtasks) {
                subtasks.add(toDomainSubtask(subtask));
            }
        }

        TaskEntity task = storedTask.task;
        return new Task(
                task.id,
                task.userId,
                task.title,
                task.description,
                task.priority,
                task.status,
                task.categoryId,
                task.category,
                task.reminderDate,
                subtasks,
                task.archived,
                task.createdAt,
                task.updatedAt,
                task.completedAt
        );
    }

    private static Subtask toDomainSubtask(SubtaskEntity entity) {
        return new Subtask(
                entity.id,
                entity.taskId,
                entity.title,
                entity.description,
                entity.done,
                entity.createdAt,
                entity.updatedAt
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

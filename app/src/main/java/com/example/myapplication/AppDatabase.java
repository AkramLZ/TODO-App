package com.example.myapplication;

import android.content.Context;

import androidx.room3.Database;
import androidx.room3.Room;
import androidx.room3.RoomDatabase;
import androidx.sqlite.driver.AndroidSQLiteDriver;

@Database(
        entities = {TaskEntity.class, SubtaskEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract TaskDao taskDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "todo_app.db"
                            )
                            .setDriver(new AndroidSQLiteDriver())
                            .build();
                }
            }
        }
        return instance;
    }
}

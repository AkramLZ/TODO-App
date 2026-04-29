package com.example.myapplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Category implements Serializable {
    public static final String GENERAL_NAME = "General";

    private String id;
    private String userId;
    private String name;
    private String color;
    private String createdAt;

    public Category(String userId, String name, String color) {
        this(UUID.randomUUID().toString(), userId, name, color, Task.nowTimestamp());
    }

    public Category(String id, String userId, String name, String color, String createdAt) {
        this.id = id;
        this.userId = userId == null ? "" : userId;
        this.name = name;
        this.color = color;
        this.createdAt = createdAt == null || createdAt.trim().isEmpty()
                ? Task.nowTimestamp()
                : createdAt;
    }

    public static ArrayList<Category> defaultCategories(String userId) {
        ArrayList<Category> categories = new ArrayList<>();
        categories.add(new Category(userId, GENERAL_NAME, "#607D8B"));
        categories.add(new Category(userId, "Work", "#3F51B5"));
        categories.add(new Category(userId, "Study", "#009688"));
        categories.add(new Category(userId, "Personal", "#8E44AD"));
        return categories;
    }

    public static Category ensureGeneralCategory(List<Category> categories, String userId) {
        for (Category category : categories) {
            if (category.isGeneral()) {
                return category;
            }
        }
        Category general = new Category(userId, GENERAL_NAME, "#607D8B");
        categories.add(0, general);
        return general;
    }

    public boolean isGeneral() {
        return GENERAL_NAME.equalsIgnoreCase(name);
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

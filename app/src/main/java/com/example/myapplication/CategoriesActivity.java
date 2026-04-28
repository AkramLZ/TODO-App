package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoriesActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORIES = "extra_categories";
    public static final String EXTRA_USER_ID = "extra_user_id";

    private static final String[] COLOR_NAMES = {
            "Blue", "Teal", "Purple", "Orange", "Green", "Red", "Gray"
    };
    private static final String[] COLOR_VALUES = {
            "#3F51B5", "#009688", "#8E44AD", "#EF9F27", "#639922", "#E24B4A", "#607D8B"
    };

    private EditText etCategoryName;
    private Spinner spCategoryColor;
    private Button btnSaveCategory, btnCancelEdit, btnDone;
    private CategoryAdapter categoryAdapter;
    private ArrayList<Category> categories;
    private String userId = "";
    private String editingCategoryId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_categories);

        etCategoryName = findViewById(R.id.et_category_name);
        spCategoryColor = findViewById(R.id.sp_category_color);
        btnSaveCategory = findViewById(R.id.btn_save_category);
        btnCancelEdit = findViewById(R.id.btn_cancel_category_edit);
        btnDone = findViewById(R.id.btn_done_categories);
        RecyclerView rvCategories = findViewById(R.id.rv_categories);

        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (userId == null) {
            userId = "";
        }
        categories = readCategoriesFromIntent();
        Category.ensureGeneralCategory(categories, userId);

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, COLOR_NAMES);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoryColor.setAdapter(colorAdapter);

        categoryAdapter = new CategoryAdapter(categories);
        categoryAdapter.setOnCategoryEditListener(this::startEditingCategory);
        categoryAdapter.setOnCategoryDeleteListener(this::confirmDeleteCategory);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);

        btnSaveCategory.setOnClickListener(v -> saveCategory());
        btnCancelEdit.setOnClickListener(v -> clearForm());
        btnDone.setOnClickListener(v -> finish());
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Category> readCategoriesFromIntent() {
        Object value = getIntent().getSerializableExtra(EXTRA_CATEGORIES);
        if (value instanceof ArrayList) {
            return new ArrayList<>((ArrayList<Category>) value);
        }
        return Category.defaultCategories(userId);
    }

    private void saveCategory() {
        String name = etCategoryName.getText().toString().trim();
        if (name.isEmpty()) {
            etCategoryName.setError("Category name is required");
            etCategoryName.requestFocus();
            return;
        }

        if (hasDuplicateName(name, editingCategoryId)) {
            etCategoryName.setError("Category already exists");
            etCategoryName.requestFocus();
            return;
        }

        String color = COLOR_VALUES[spCategoryColor.getSelectedItemPosition()];
        if (editingCategoryId.isEmpty()) {
            categories.add(new Category(userId, name, color));
            Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
        } else {
            Category category = findCategoryById(editingCategoryId);
            if (category != null) {
                if (category.isGeneral() && !Category.GENERAL_NAME.equalsIgnoreCase(name)) {
                    etCategoryName.setError("General cannot be renamed");
                    etCategoryName.requestFocus();
                    return;
                }
                category.setName(name);
                category.setColor(color);
                Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show();
            }
        }

        categoryAdapter.notifyDataSetChanged();
        clearForm();
    }

    private boolean hasDuplicateName(String name, String excludedId) {
        for (Category category : categories) {
            if (!category.getId().equals(excludedId)
                    && category.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private void startEditingCategory(Category category) {
        editingCategoryId = category.getId();
        etCategoryName.setText(category.getName());
        selectColor(category.getColor());
        btnSaveCategory.setText("Update Category");
        btnCancelEdit.setVisibility(View.VISIBLE);
    }

    private void selectColor(String color) {
        for (int i = 0; i < COLOR_VALUES.length; i++) {
            if (COLOR_VALUES[i].equalsIgnoreCase(color)) {
                spCategoryColor.setSelection(i);
                return;
            }
        }
        spCategoryColor.setSelection(0);
    }

    private void confirmDeleteCategory(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete category")
                .setMessage("Tasks in this category will move to General.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteCategory(category))
                .show();
    }

    private void deleteCategory(Category category) {
        if (categories.remove(category)) {
            if (category.getId().equals(editingCategoryId)) {
                clearForm();
            }
            categoryAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private Category findCategoryById(String id) {
        for (Category category : categories) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        return null;
    }

    private void clearForm() {
        editingCategoryId = "";
        etCategoryName.setText("");
        spCategoryColor.setSelection(0);
        btnSaveCategory.setText("Add Category");
        btnCancelEdit.setVisibility(View.GONE);
    }

    @Override
    public void finish() {
        Intent result = new Intent();
        result.putExtra(EXTRA_CATEGORIES, categories);
        setResult(RESULT_OK, result);
        super.finish();
    }
}

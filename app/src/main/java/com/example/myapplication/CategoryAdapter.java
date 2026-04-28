package com.example.myapplication;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryEditListener {
        void onCategoryEdit(Category category);
    }

    public interface OnCategoryDeleteListener {
        void onCategoryDelete(Category category);
    }

    private final List<Category> categories;
    private OnCategoryEditListener editListener;
    private OnCategoryDeleteListener deleteListener;

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    public void setOnCategoryEditListener(OnCategoryEditListener editListener) {
        this.editListener = editListener;
    }

    public void setOnCategoryDeleteListener(OnCategoryDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvName.setText(category.getName());

        GradientDrawable swatch = new GradientDrawable();
        swatch.setShape(GradientDrawable.OVAL);
        swatch.setColor(parseColor(category.getColor()));
        holder.viewColor.setBackground(swatch);

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onCategoryEdit(category);
            }
        });

        holder.btnDelete.setEnabled(!category.isGeneral());
        holder.btnDelete.setAlpha(category.isGeneral() ? 0.35f : 1f);
        holder.btnDelete.setOnClickListener(v -> {
            if (!category.isGeneral() && deleteListener != null) {
                deleteListener.onCategoryDelete(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private int parseColor(String color) {
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException e) {
            return Color.GRAY;
        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View viewColor;
        TextView tvName;
        ImageButton btnEdit, btnDelete;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.view_category_color);
            tvName = itemView.findViewById(R.id.tv_category_name);
            btnEdit = itemView.findViewById(R.id.btn_edit_category);
            btnDelete = itemView.findViewById(R.id.btn_delete_category);
        }
    }
}

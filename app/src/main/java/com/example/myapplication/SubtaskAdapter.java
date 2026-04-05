package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.SubtaskViewHolder> {

    private final List<Subtask> subtasks;

    public SubtaskAdapter(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtask, parent, false);
        return new SubtaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
        Subtask subtask = subtasks.get(position);

        holder.tvTitle.setText(subtask.getTitle());
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(subtask.isDone());

        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) ->
                subtask.setDone(isChecked));

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                subtasks.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subtasks.size();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    static class SubtaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbDone;
        TextView tvTitle;
        ImageButton btnDelete;

        SubtaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cb_subtask_done);
            tvTitle = itemView.findViewById(R.id.tv_subtask_title);
            btnDelete = itemView.findViewById(R.id.btn_delete_subtask);
        }
    }
}

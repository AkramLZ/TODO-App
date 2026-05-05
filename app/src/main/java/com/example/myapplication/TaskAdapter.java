package com.example.myapplication;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskStatusChangedListener {
        void onTaskStatusChanged(Task task);
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);
    }

    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    public interface OnTaskArchiveChangedListener {
        void onTaskArchiveChanged(Task task);
    }

    private List<Task> tasks;
    private OnTaskStatusChangedListener listener;
    private OnTaskClickListener clickListener;
    private OnTaskDeleteListener deleteListener;
    private OnTaskArchiveChangedListener archiveChangedListener;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void setOnTaskStatusChangedListener(OnTaskStatusChangedListener listener) {
        this.listener = listener;
    }

    public void setOnTaskClickListener(OnTaskClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setOnTaskDeleteListener(OnTaskDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setOnTaskArchiveChangedListener(OnTaskArchiveChangedListener archiveChangedListener) {
        this.archiveChangedListener = archiveChangedListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription());
        holder.tvPriority.setText(task.getPriority());
        holder.tvStatus.setText(task.isArchived() ? "Archived" : task.getStatus());
        holder.tvCategory.setText(task.getCategory());
        holder.tvReminder.setText(task.getReminderDate());

        int priorityColor;
        switch (task.getPriority()) {
            case "Urgent":
                priorityColor = Color.parseColor("#9C1C28");
                break;
            case "High":
                priorityColor = Color.parseColor("#E24B4A");
                break;
            case "Medium":
                priorityColor = Color.parseColor("#EF9F27");
                break;
            case "Low":
                priorityColor = Color.parseColor("#639922");
                break;
            default:
                priorityColor = Color.GRAY;
                break;
        }
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(priorityColor);
        bg.setCornerRadius(4f);
        holder.tvPriority.setBackground(bg);

        int statusColor;
        if (task.isArchived()) {
            statusColor = Color.GRAY;
        } else {
            switch (task.getStatus()) {
                case Task.STATUS_IN_PROGRESS:
                    statusColor = Color.parseColor("#378ADD"); // blue
                    break;
                case Task.STATUS_COMPLETED:
                    statusColor = Color.parseColor("#639922"); // green
                    break;
                default:
                    statusColor = Color.GRAY;
                    break;
            }
        }
        holder.tvStatus.setTextColor(statusColor);

        boolean isCompleted = Task.STATUS_COMPLETED.equals(task.getStatus());
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(isCompleted);
        holder.cbDone.setEnabled(!task.isArchived());

        if (isCompleted) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setStatus(isChecked ? Task.STATUS_COMPLETED : Task.STATUS_IN_PROGRESS);
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) {
                listener.onTaskStatusChanged(task);
            }
        });

        // Subtask count
        List<Subtask> subtasks = task.getSubtasks();
        if (subtasks != null && !subtasks.isEmpty()) {
            long done = 0;
            for (Subtask s : subtasks) {
                if (s.isDone()) done++;
            }
            holder.tvSubtasks.setText(done + "/" + subtasks.size() + " subtasks");
            holder.tvSubtasks.setVisibility(View.VISIBLE);
        } else {
            holder.tvSubtasks.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTaskClick(task, holder.getAdapterPosition());
            }
        });

        holder.btnArchive.setImageResource(task.isArchived()
                ? R.drawable.ic_unarchive_24
                : R.drawable.ic_archive_24);
        holder.btnArchive.setContentDescription(task.isArchived()
                ? "Unarchive task"
                : "Archive task");
        holder.btnArchive.setOnClickListener(v -> {
            if (archiveChangedListener != null) {
                archiveChangedListener.onTaskArchiveChanged(task);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onTaskDelete(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbDone;
        TextView tvPriority, tvStatus, tvTitle, tvDescription, tvCategory, tvReminder, tvSubtasks;
        ImageButton btnArchive, btnDelete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cb_task_done);
            tvPriority = itemView.findViewById(R.id.tv_task_priority);
            tvStatus = itemView.findViewById(R.id.tv_task_status);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDescription = itemView.findViewById(R.id.tv_task_description);
            tvCategory = itemView.findViewById(R.id.tv_task_category);
            tvReminder = itemView.findViewById(R.id.tv_task_reminder);
            tvSubtasks = itemView.findViewById(R.id.tv_task_subtasks);
            btnArchive = itemView.findViewById(R.id.btn_archive_task);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);
        }
    }
}

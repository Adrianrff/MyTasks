package com.adrapps.mytasks.Views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.adrapps.mytasks.LocalTask;
import com.adrapps.mytasks.R;

import java.util.List;

/**
 * Created by Adrian Flores on 27/3/2017.
 */

class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    private List<LocalTask> tasks;

    TaskListAdapter (Context context, List<LocalTask> tasks) {
        this.tasks = tasks;

    }

    @Override
    public TaskListAdapter.TaskListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, null);
        TaskListViewHolder holder = new TaskListViewHolder(itemLayoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(TaskListAdapter.TaskListViewHolder holder, int position) {
        holder.taskName.setText(tasks.get(position).getTitle());
        holder.dueDate.setText(String.valueOf(tasks.get(position).getDue()));

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskListViewHolder extends RecyclerView.ViewHolder {

        TextView taskName,dueDate;
        ImageView notificationImage;
        CheckBox taskCheckbox;

        TaskListViewHolder(View v) {
            super(v);
            taskName = (TextView) v.findViewById(R.id.textViewName);
            dueDate = (TextView) v.findViewById(R.id.textViewDate);
            notificationImage = (ImageView) v.findViewById(R.id.notificationImage);
            taskCheckbox = (CheckBox) v.findViewById(R.id.taskCheckbox);

        }
    }
}








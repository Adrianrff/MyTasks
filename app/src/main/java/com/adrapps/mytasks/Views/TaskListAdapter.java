package com.adrapps.mytasks.Views;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adrapps.mytasks.Helpers.DateHelper;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Interfaces.RecyclerItemClickListener;
import com.adrapps.mytasks.R;

import java.util.List;

class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> implements RecyclerItemClickListener {

    private Context context;
    private List<LocalTask> tasks;
    private int mExpandedPosition = -1;
    boolean isExpanded;
    private RecyclerView mRecyclerView;

    TaskListAdapter (Context context, List<LocalTask> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

//    @Override
//    public long getItemId(int position) {
//        return tasks.get(position).getIntId();
//    }

    @Override
    public TaskListAdapter.TaskListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("onCreate","run");
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new TaskListViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final TaskListAdapter.TaskListViewHolder holder, int position) {
        Log.d("onBind","run");
        LocalTask cTask = tasks.get(position);
        holder.taskName.setText(cTask.getTitle());
        if (cTask.getDue() == 0){
            holder.dueDate.setText(R.string.no_due_date);
        } else {
            holder.dueDate.setText(DateHelper.timeInMillsToString(cTask.getDue()));
        }
        holder.notes.setText(tasks.get(position).getNotes() == null ?
                context.getResources().getString(R.string.no_notes) : cTask.getNotes());
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    void updateItems(List<LocalTask> localTasks) {
        this.tasks.clear();
        this.tasks = localTasks;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onItemCilck() {

    }

    static class TaskListViewHolder extends RecyclerView.ViewHolder {

        TextView taskName,dueDate, notes;
        ImageView notificationImage;
        CheckBox taskCheckbox;
        RelativeLayout detail;

        TaskListViewHolder(View v) {
            super(v);
            detail = (RelativeLayout) v.findViewById(R.id.task_detail_layout);
            notes = (TextView) v.findViewById(R.id.notes_content);
            dueDate = (TextView) v.findViewById(R.id.textViewDate);
            taskName = (TextView) v.findViewById(R.id.textViewName);
            notificationImage = (ImageView) v.findViewById(R.id.notificationImage);
            taskCheckbox = (CheckBox) v.findViewById(R.id.taskCheckbox);
            Log.d("position",String.valueOf(getAdapterPosition()));
        }
    }
}








package com.adrapps.mytasks.Views;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adrapps.mytasks.Helpers.DateHelper;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.R;

import java.util.List;

class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    private List<LocalTask> tasks;
    int mExpandedPosition = -1;
    boolean isExpanded;
    RecyclerView mRecyclerView;

    TaskListAdapter (Context context, List<LocalTask> tasks) {
        this.tasks = tasks;

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }
    @Override
    public TaskListAdapter.TaskListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new TaskListViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final TaskListAdapter.TaskListViewHolder holder, int position) {
        holder.itemView.setActivated(false);
        holder.taskName.setText(tasks.get(position).getTitle());
        if (tasks.get(position).getDue() == 0){
            holder.dueDate.setText(R.string.no_due_date);
        } else {
            holder.dueDate.setText(DateHelper.timeInMillsToString(tasks.get(position).getDue()));
        }
        final boolean isExpanded = position==mExpandedPosition;
        holder.detail.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1:holder.getAdapterPosition();
                TransitionManager.beginDelayedTransition(mRecyclerView);
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateItems(List<LocalTask> localTasks) {
        this.tasks.clear();
        this.tasks = localTasks;
        notifyDataSetChanged();
    }


    private void animateLayout() {
    }



    class TaskListViewHolder extends RecyclerView.ViewHolder {

        TextView taskName,dueDate;
        ImageView notificationImage;
        CheckBox taskCheckbox;
        RelativeLayout detail;

        TaskListViewHolder(View v) {
            super(v);
            detail = (RelativeLayout) v.findViewById(R.id.task_detail_layout);
            taskName = (TextView) v.findViewById(R.id.textViewName);
            dueDate = (TextView) v.findViewById(R.id.textViewDate);
            notificationImage = (ImageView) v.findViewById(R.id.notificationImage);
            taskCheckbox = (CheckBox) v.findViewById(R.id.taskCheckbox);
            isExpanded = getAdapterPosition()==mExpandedPosition;
            detail.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        }
    }

}








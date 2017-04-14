package com.adrapps.mytasks.Views;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Helpers.DateHelper;
import com.adrapps.mytasks.Interfaces.ItemTouchHelperAdapter;
import com.adrapps.mytasks.Interfaces.ItemTouchHelperViewHolder;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.adrapps.mytasks.R;

import java.util.Collections;
import java.util.List;

class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder>
        implements ItemTouchHelperAdapter {

    private Context context;
    private List<LocalTask> tasks;
    private TaskListPresenter mPresenter;
    private LocalTask removedTask;

    TaskListAdapter(Context context, List<LocalTask> tasks, TaskListPresenter presenter) {
        this.mPresenter = presenter;
        this.context = context;
        this.tasks = tasks;
        setHasStableIds(true);
    }

    @Override
    public TaskListAdapter.TaskListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("onCreate", "run");
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new TaskListViewHolder(itemLayoutView, context, mPresenter);
    }

    @Override
    public void onBindViewHolder(TaskListAdapter.TaskListViewHolder holder, int position) {
        Log.d("onBind", String.valueOf(position));
        LocalTask cTask = tasks.get(position);
        if (!cTask.getTitle().equals("")) {
            holder.taskName.setText(cTask.getTitle());
        } else {
            holder.taskName.setText(R.string.no_task_name);
        }
        if (cTask.getDue() == 0) {
            holder.dueDate.setText(R.string.no_due_date);
        } else {
            holder.dueDate.setText(DateHelper.timeInMillsToString(cTask.getDue()));
        }
        holder.taskCheckbox.setOnCheckedChangeListener(null);
        if (cTask.getStatus() == null) {
            cTask.setStatus(Co.TASK_NEEDS_ACTION);
        }
        if (cTask.getStatus().equals(Co.TASK_COMPLETED)) {
            holder.taskCheckbox.setChecked(true);
            holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskName.setTextColor(Color.GRAY);
        } else {
            holder.taskName.setTextColor(holder.oldColors);
            holder.taskName.setPaintFlags(0);
            holder.taskCheckbox.setChecked(false);
        }
        holder.taskCheckbox.setOnCheckedChangeListener(holder);

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

    public void addItem(LocalTask task, int position) {
        tasks.add(0, task);
        notifyItemInserted(position);
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(tasks, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(tasks, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemSwiped(int position, int direction) {
        removedTask = tasks.remove(position);
        notifyItemRemoved(position);
        mPresenter.showUndoSnackBar(context.getString(R.string.task_deleted), position, removedTask);
    }

    void restoreDeletedItem(int position) {
        tasks.add(position, removedTask);
        notifyItemInserted(position);
        showToast(context.getString(R.string.task_restored));
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).getIntId();
    }

    class TaskListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            ItemTouchHelperViewHolder, CompoundButton.OnCheckedChangeListener {

        TextView taskName, dueDate;
        ImageView notificationImage;
        CheckBox taskCheckbox;
        Context context;
        TaskListPresenter mPresenter;
        ColorStateList oldColors;

        TaskListViewHolder(View v, Context context, TaskListPresenter presenter) {
            super(v);
            this.context = context;
            this.mPresenter = presenter;
            v.setOnClickListener(this);
            dueDate = (TextView) v.findViewById(R.id.textViewDate);
            taskName = (TextView) v.findViewById(R.id.textViewName);
            notificationImage = (ImageView) v.findViewById(R.id.notificationImage);
            taskCheckbox = (CheckBox) v.findViewById(R.id.taskCheckbox);
            taskCheckbox.setOnCheckedChangeListener(this);
            oldColors = taskName.getTextColors();
        }

        @Override
        public void onClick(View v) {
            LocalTask cTask = tasks.get(getAdapterPosition());
            Intent i = new Intent(context, TaskDetailActivity.class);
            i.putExtra(Co.DETAIL_TASK_TITLE, cTask.getTitle());
            if (cTask.getDue() != 0) {
                i.putExtra(Co.DETAIL_TASK_DUE,
                        DateHelper.timeInMillsToString(cTask.getDue()));
            } else {
                i.putExtra(Co.DETAIL_TASK_DUE, R.string.no_due_date);
            }
            i.putExtra(Co.DETAIL_TASK_NOTE, cTask.getNotes() == null ? R.string.no_notes : cTask.getNotes());
            i.putExtra(Co.DETAIL_TASK_ID, cTask.getTaskId());
            mPresenter.navigateToEditTask(i);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            LocalTask cTask = tasks.get(getAdapterPosition());
            Log.d("Task", cTask.getStatus());
            if (isChecked) {
                if (mPresenter.isDeviceOnline()) {
                    taskName.setPaintFlags(taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    taskName.setTextColor(Color.GRAY);
                    mPresenter.updateTaskStatus(cTask.getTaskId(), cTask.getTaskList(), Co.TASK_COMPLETED);
                } else {
                    buttonView.setChecked(false);
                    showToast(mPresenter.getString(R.string.no_internet_toast));
                }

            } else {
                if (mPresenter.isDeviceOnline()) {
                    taskName.setTextColor(oldColors);
                    taskName.setPaintFlags(0);
                    mPresenter.updateTaskStatus(cTask.getTaskId(), cTask.getTaskList(), Co.TASK_NEEDS_ACTION);
                } else {
                    buttonView.setChecked(true);
                    showToast(mPresenter.getString(R.string.no_internet_toast));
                }
            }
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(Color.WHITE);
        }

    }

}

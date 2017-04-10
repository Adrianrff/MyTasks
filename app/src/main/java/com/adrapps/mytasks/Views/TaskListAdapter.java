package com.adrapps.mytasks.Views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
    }

    @Override
    public TaskListAdapter.TaskListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("onCreate", "run");
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new TaskListViewHolder(itemLayoutView, context, mPresenter);
    }

    @Override
    public void onBindViewHolder(final TaskListAdapter.TaskListViewHolder holder, int position) {
        Log.d("onBind", "run");
        LocalTask cTask = tasks.get(position);
        holder.taskName.setText(cTask.getTitle());
        if (cTask.getDue() == 0) {
            holder.dueDate.setText(R.string.no_due_date);
        } else {
            holder.dueDate.setText(DateHelper.timeInMillsToString(cTask.getDue()));
        }
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
//        if (direction == ItemTouchHelper.LEFT) {
            removedTask = tasks.get(position);
            tasks.remove(position);
            notifyItemRemoved(position);
//        } else {

//        }

        mPresenter.showUndoSnackBar(context.getString(R.string.task_deleted),position);
    }

    void restoreDeletedItem(int position) {
        tasks.add(position,removedTask);
        notifyItemInserted(position);
        showToast(context.getString(R.string.task_restored));
    }


    class TaskListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            ItemTouchHelperViewHolder {

        TextView taskName, dueDate;
        ImageView notificationImage;
        CheckBox taskCheckbox;
        Context context;
        TaskListPresenter mPresenter;

        TaskListViewHolder(View v, Context context, TaskListPresenter presenter) {
            super(v);
            this.context = context;
            this.mPresenter = presenter;
            v.setOnClickListener(this);
            dueDate = (TextView) v.findViewById(R.id.textViewDate);
            taskName = (TextView) v.findViewById(R.id.textViewName);
            notificationImage = (ImageView) v.findViewById(R.id.notificationImage);
            taskCheckbox = (CheckBox) v.findViewById(R.id.taskCheckbox);
            Log.d("position", String.valueOf(getAdapterPosition()));

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
            i.putExtra(Co.DETAIL_TASK_NOTE, cTask.getNotes() != null ? R.string.no_notes : cTask.getNotes());
            context.startActivity(i);
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

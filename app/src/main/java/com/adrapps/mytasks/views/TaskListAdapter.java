package com.adrapps.mytasks.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.interfaces.ItemTouchHelperAdapter;
import com.adrapps.mytasks.interfaces.ItemTouchHelperViewHolder;
import com.adrapps.mytasks.presenter.TaskListPresenter;

import java.util.Collections;
import java.util.List;

class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder>
        implements ItemTouchHelperAdapter {

    private Context context;
    private List<LocalTask> tasks;
    private TaskListPresenter mPresenter;
    private LocalTask removedTask;
    private int newPos;
    private int oldPos;


    TaskListAdapter(Context context, List<LocalTask> tasks, TaskListPresenter presenter) {
        this.mPresenter = presenter;
        this.context = context;
        this.tasks = tasks;
//        setHasStableIds(true);
    }

    @Override
    public TaskListAdapter.TaskListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new TaskListViewHolder(itemLayoutView, context, mPresenter);
    }

    @Override
    public void onBindViewHolder(TaskListAdapter.TaskListViewHolder holder, int position) {
        LocalTask cTask = tasks.get(position);
        if (!cTask.getTitle().equals("")) {
            holder.taskName.setText(cTask.getTitle());
        } else {
            holder.taskName.setText(R.string.no_task_name);
        }
        if (cTask.getDue() == 0) {
            holder.dueDate.setTextColor(holder.oldDueColors);
            holder.dueDate.setTypeface(null, Typeface.NORMAL);
            holder.dueDate.setText(R.string.no_due_date);
        } else {
            if (DateUtils.isToday(cTask.getDue())) {
                holder.dueDate.setTextColor(holder.oldDueColors);
                holder.dueDate.setTypeface(null, Typeface.BOLD);
                holder.dueDate.setText(R.string.today);
            } else if (DateHelper.isTomorrow(cTask.getDue())) {
                holder.dueDate.setTextColor(holder.oldDueColors);
                holder.dueDate.setTypeface(null, Typeface.NORMAL);
                holder.dueDate.setText(R.string.tomorrow);
            } else if (DateHelper.isInThePast(cTask.getDue())) {
                holder.dueDate.setText(DateHelper.timeInMillsToString(cTask.getDue())
                        + " " + context.getString(R.string.overdue_append));
                holder.dueDate.setTypeface(null, Typeface.NORMAL);
                holder.dueDate.setTextColor(Color.RED);
            } else {
                holder.dueDate.setTextColor(holder.oldDueColors);
                holder.dueDate.setTypeface(null, Typeface.NORMAL);
                holder.dueDate.setText(DateHelper.timeInMillsToString(cTask.getDue()));
            }
        }
        holder.taskCheckbox.setOnCheckedChangeListener(null);
        if (cTask.getStatus() == null) {
            cTask.setStatus(Co.TASK_NEEDS_ACTION);
        }
        if (cTask.getStatus().equals(Co.TASK_COMPLETED)) {
            holder.taskCheckbox.setChecked(true);
            holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.dueDate.setPaintFlags(holder.dueDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.dueDate.setTextColor(Color.GRAY);
            holder.taskName.setTextColor(Color.GRAY);
        } else {
            holder.taskName.setTextColor(holder.oldTaskColors);
            holder.taskName.setPaintFlags(0);
            holder.dueDate.setPaintFlags(0);
            holder.taskCheckbox.setChecked(false);
        }
        holder.taskCheckbox.setOnCheckedChangeListener(holder);

        if (cTask.getReminder() != 0) {
//            if (mPresenter.isReminderSet((int) cTask.getReminderId())){
                holder.notificationImage.setVisibility(View.VISIBLE);
//            } else {
//                holder.notificationImage.setVisibility(View.GONE);
//            }
        } else {
            holder.notificationImage.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    void updateItems(List<LocalTask> localTasks) {
        this.tasks.clear();
        this.tasks = localTasks;
        notifyDataSetChanged();
    }

    void addItem(LocalTask task, int position) {
        tasks.add(0, task);
        notifyItemInserted(position);
        mPresenter.showEmptyRecyclerView(tasks.isEmpty());
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
        newPos = toPosition;
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemSwiped(int position, int direction) {
        if (direction == ItemTouchHelper.END){
        } else {
            removedTask = tasks.remove(position);
            notifyItemRemoved(position);
            mPresenter.showUndoSnackBar(context.getString(R.string.task_deleted), position, removedTask);
            if (tasks.isEmpty()) {
                mPresenter.showEmptyRecyclerView(true);
            }
        }
    }

    void restoreDeletedItem(int position) {
        tasks.add(position, removedTask);
        notifyItemInserted(position);
        mPresenter.showEmptyRecyclerView(false);
        showToast(context.getString(R.string.task_restored));
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).getIntId();
    }

    void updateItem(LocalTask task, int position) {
        if (position == -1){
            tasks.set(tasks.indexOf(task), task);
            notifyItemChanged(tasks.indexOf(task));
        } else {
            tasks.set(position, task);
            notifyItemChanged(position);
        }
    }


    //------------------------VIEW HOLDER-------------------------------///
    class TaskListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            ItemTouchHelperViewHolder, CompoundButton.OnCheckedChangeListener {

        TextView taskName, dueDate;
        ImageView notificationImage;
        CheckBox taskCheckbox;
        Context context;
        TaskListPresenter mPresenter;
        ColorStateList oldTaskColors;
        ColorStateList oldDueColors;

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
            oldTaskColors = taskName.getTextColors();
            oldDueColors = dueDate.getTextColors();
        }

        @Override
        public void onClick(View v) {
            mPresenter.showBottomSheet(tasks.get(getAdapterPosition()), getAdapterPosition(), true);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            LocalTask cTask = tasks.get(getAdapterPosition());
            if (isChecked) {
                    taskName.setPaintFlags(taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    dueDate.setPaintFlags(dueDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    dueDate.setTextColor(Color.GRAY);
                    taskName.setTextColor(Color.GRAY);
                    mPresenter.updateTaskStatus(cTask.getIntId(), cTask.getList(), Co.TASK_COMPLETED);
            } else {
                    dueDate.setTextColor(oldDueColors);
                    dueDate.setPaintFlags(0);
                    taskName.setTextColor(oldTaskColors);
                    taskName.setPaintFlags(0);
                    if (cTask.getDue() == 0) {
                        dueDate.setText(R.string.no_due_date);
                    } else {
                        if (DateUtils.isToday(cTask.getDue())) {
                            dueDate.setTextColor(oldDueColors);
                            dueDate.setTypeface(null, Typeface.BOLD);
                        } else if (DateHelper.isTomorrow(cTask.getDue())) {
                            dueDate.setTextColor(oldDueColors);
                            dueDate.setTypeface(null, Typeface.NORMAL);
                        } else if (DateHelper.isInThePast(cTask.getDue())) {
                            dueDate.setText(DateHelper.timeInMillsToString(cTask.getDue())
                                    + " " + context.getString(R.string.overdue_append));
                            dueDate.setTypeface(null, Typeface.NORMAL);
                            dueDate.setTextColor(Color.RED);
                        } else {
                            dueDate.setTextColor(oldDueColors);
                            dueDate.setTypeface(null, Typeface.NORMAL);
                        }
                    }
                    mPresenter.updateTaskStatus(cTask.getIntId(), cTask.getList(), Co.TASK_NEEDS_ACTION);

            }
        }

        @Override
        public void onItemSelected() {
            oldPos = getAdapterPosition();
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {

            itemView.setBackgroundColor(Color.WHITE);
            if (getAdapterPosition() >= 0) {
                LocalTask movedTask = tasks.get(getAdapterPosition());
                movedTask.setMoved(Co.MOVED);
                mPresenter.updateMovedByIntId(movedTask.getIntId(), Co.MOVED);
                if (oldPos != getAdapterPosition()) {
                    mPresenter.updateSiblingByIntId(movedTask.getIntId(),
                            newPos - 1 < 0 ? Co.MOVED_TO_FIRST : tasks.get(newPos - 1).getIntId());
                    mPresenter.updateSiblingByIntId(tasks.get(oldPos).getIntId(), oldPos - 1 < 0 ? 0 : tasks.get(oldPos - 1).getIntId());
                    if (newPos + 1 <= tasks.size() - 1) {
                        mPresenter.updateSiblingByIntId(tasks.get(newPos + 1).getIntId(), movedTask.getIntId());
                    }
                    String newTaskTempPos = null;
                    String previousTaskId;
                    if (getAdapterPosition() == 0) {
                        previousTaskId = Co.TASK_MOVED_TO_FIRST;
                        if (tasks.size() >= 2 && !mPresenter.isDeviceOnline()) {
                            if (tasks.get(getAdapterPosition() + 1).getPosition() != null){
                                String nextTaskServerPos = tasks.get(getAdapterPosition() + 1).
                                        getPosition();
                                String nextTaskServerPositionLastTwoChar =
                                        nextTaskServerPos.substring(nextTaskServerPos.length() - 2);
                                int lastTwoCharNewPos = Integer.parseInt(nextTaskServerPositionLastTwoChar) - 1;
                                newTaskTempPos = nextTaskServerPos.substring(0,
                                        nextTaskServerPos.length() - 2) + lastTwoCharNewPos;
                            } else {
                                newTaskTempPos = "0";
                            }

                        }
                    } else {
                        previousTaskId = tasks.get(getAdapterPosition() - 1).getId();
                        String previousTaskServerPos = tasks.get(getAdapterPosition() - 1).
                                getPosition();
                        if (previousTaskServerPos != null) {
                            String previousTaskServerPositionLastTwoChar =
                                    previousTaskServerPos.substring(previousTaskServerPos.length() - 1);
                            int lastTwoCharNewPos = Integer.parseInt(previousTaskServerPositionLastTwoChar) + 1;
                            newTaskTempPos = previousTaskServerPos.substring(0,
                                    previousTaskServerPos.length() - 1) + lastTwoCharNewPos;
                        } else {

                        }

                    }
                    if (newTaskTempPos != null)
                        mPresenter.setTemporaryPositionByIntId(movedTask.getIntId(), newTaskTempPos);
                    mPresenter.moveTask(movedTask, previousTaskId);
                }
            }
        }
    }
}

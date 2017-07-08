package com.adrapps.mytasks.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.AlarmHelper;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.interfaces.ItemTouchHelperAdapter;
import com.adrapps.mytasks.interfaces.ItemTouchHelperViewHolder;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SelectableHolder;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;


class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder>
      implements ItemTouchHelperAdapter {

   private final String TAG = "TaskListAdapter";
   private Context context;
   private List<LocalTask> tasks;
   private TaskListPresenter mPresenter;
   private int newPos;
   private int oldPos;
   private ActionMode mActionMode;
   private MultiSelector mMultiSelector;
   private ModalMultiSelectorCallback mActionModeCallback;
   private LinkedHashMap<LocalTask, String> moveMap;
   private boolean moved;
   private RecyclerView mRecyclerView;


   TaskListAdapter(final Context context, List<LocalTask> tasks, TaskListPresenter presenter) {
      this.mPresenter = presenter;
      this.context = context;
      this.tasks = getSortedTasks(tasks);
      mMultiSelector = new MultiSelector();
      mActionModeCallback
            = new ModalMultiSelectorCallback(mMultiSelector) {

         @Override
         public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            setActionMode(actionMode);
            moved = false;
            ((Activity) context).getMenuInflater().inflate(R.menu.list_context_menu, menu);
            mPresenter.swipeRefreshSetEnabled(false);
            mPresenter.showFab(false);

            if (moveMap != null) {
               moveMap.clear();
            } else {
               moveMap = new LinkedHashMap<>();
            }
            Co.IS_MULTISELECT_ENABLED = true;
            return true;
         }


         @Override
         public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

            if (menuItem.getItemId() == R.id.delete_items) {
               actionMode.finish();
               deleteItems();
               mMultiSelector.clearSelections();
               return true;
            }
//            if (menuItem.getItemId() == R.id.make_child) {
//               if (mMultiSelector.getSelectedPositions().size() == 1) {
//                  makeChild(mMultiSelector.getSelectedPositions().get(0));
//               }
//               return true;
//            }
            return false;
         }

         @Override
         public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return super.onPrepareActionMode(actionMode, menu);

         }

         //TODO Finish implementing making subtasks
         @Override
         public void onDestroyActionMode(ActionMode actionMode) {
            super.onDestroyActionMode(actionMode);
            mPresenter.swipeRefreshSetEnabled(true);
            mPresenter.showFab(true);
            Co.IS_MULTISELECT_ENABLED = false;
            if (moved) {
               saveTasksPositions();
            }
            if (!moveMap.isEmpty()) {
               mPresenter.moveTasks(moveMap);
            }
         }
      };
   }

   @Override
   public void onBindViewHolder(TaskListAdapter.TaskListViewHolder holder, int position) {
      LocalTask cTask = tasks.get(position);
//      holder.setSelectionModeBackgroundDrawable(getHighlightedBackground());
//      if (cTask.getParent() != null) {
//         increaseMargin(holder);
//      }
      holder.notesTitleIcon.setVisibility(cTask.getNotes() == null ? View.GONE : View.VISIBLE);
      setDueDateText(holder, cTask);
      setTitleText(holder, cTask);
//      holder.dueDateTextView.setTypeface(null, Typeface.NORMAL);
//      holder.dueDateTextView.setTextColor(holder.normalDueColor);
//      if (cTask.getDue() == 0) {
//         holder.dueDateTextView.setText(R.string.no_due_date);
//      } else {
//         holder.dueDateTextView.setText(DateHelper.millisToRelativeDateOnly(context, cTask.getDue()));
//         if (DateUtils.isToday(cTask.getDue())) {
//            holder.dueDateTextView.setTypeface(null, Typeface.BOLD);
//         } else if (DateHelper.isBeforeToday(cTask.getDue())) {
//            holder.dueDateTextView.append(" " + context.getString(R.string.overdue_append));
//            holder.dueDateTextView.setTextColor(Color.RED);
//         }
//      }
      holder.taskCheckbox.setOnCheckedChangeListener(null);
      if (cTask.getStatus() == null) {
         cTask.setStatus(Co.TASK_NEEDS_ACTION);
      }
      if (cTask.getStatus().equals(Co.TASK_COMPLETED)) {
         holder.taskCheckbox.setChecked(true);
         holder.notesTitleIcon.setColorFilter(ContextCompat.getColor(context, R.color.darkGray));
         holder.notesTitleIcon.setAlpha(0.4f);
         holder.taskCheckbox.setHighlightColor(ContextCompat.getColor(context, R.color.darkGray));
         holder.taskCheckbox.setAlpha(0.6f);
      } else {
         holder.taskCheckbox.setHighlightColor(ContextCompat.getColor(context, R.color.colorPrimary));
         holder.taskCheckbox.setAlpha(1);
         holder.taskCheckbox.setChecked(false);
      }
      holder.taskCheckbox.setOnCheckedChangeListener(holder);
      holder.notificationImage.setVisibility(cTask.getReminder() == 0 ? View.GONE : View.VISIBLE);
   }

   private void setDueDateText(TaskListViewHolder holder, LocalTask task) {
      if (task.getDue() == 0) {
         holder.dueDateTextView.setText(R.string.no_due_date);
      } else {
         holder.dueDateTextView.setText(DateHelper.millisToRelativeDateOnly(context, task.getDue()));
         if (DateHelper.isBeforeToday(task.getDue())) {
            holder.dueDateTextView.append(" " + context.getString(R.string.overdue_append));
         }
      }
      if (task.getStatus().equals(Co.TASK_COMPLETED)) {
         holder.dueDateTextView.setTextColor(Color.GRAY);
         holder.dueDateTextView.setAlpha(0.6f);
         holder.dueDateTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
      } else {
         holder.dueDateTextView.setPaintFlags(0);
         holder.dueDateTextView.setAlpha(1);
         if (DateUtils.isToday(task.getDue())) {
            holder.dueDateTextView.setTypeface(null, Typeface.BOLD);
         } else {
            holder.dueDateTextView.setTypeface(null, Typeface.NORMAL);
         }
         if (DateHelper.isBeforeToday(task.getDue()) && task.getDue() != 0) {
            holder.dueDateTextView.setTextColor(Color.RED);
         } else {
            holder.dueDateTextView.setTextColor(holder.normalDueColor);
         }
      }
   }

   private void setTitleText(TaskListViewHolder holder, LocalTask task) {
      holder.taskTitleTextView.setText(task.getTitle());
      if (task.getStatus().equals(Co.TASK_COMPLETED)) {
         holder.taskTitleTextView.setPaintFlags(/*holder.taskTitleTextView.getPaintFlags() | */Paint.STRIKE_THRU_TEXT_FLAG);
         holder.taskTitleTextView.setAlpha(0.6f);
         holder.taskTitleTextView.setTextColor(Color.GRAY);
      } else {
         holder.taskTitleTextView.setAlpha(1);
         holder.taskTitleTextView.setTextColor(holder.normalTaskColor);
         holder.taskTitleTextView.setPaintFlags(0);
      }

   }

   @Override
   public void onAttachedToRecyclerView(RecyclerView recyclerView) {
      super.onAttachedToRecyclerView(recyclerView);
      mRecyclerView = recyclerView;
   }

   private void makeChild(int position) {
      if (position > 0) {
         LocalTask task = tasks.get(position);
         task.setParent(tasks.get(position - 1).getId());
         task.setLocalModify();
         mPresenter.updateTaskParentInDb(task, tasks.get(position - 1).getId());
         notifyItemChanged(position);
//         //TODO make server call
      }
   }

   private void increaseMargin(TaskListViewHolder holder) {
      View view = holder.parentView;
      if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
         ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
         int margin = p.leftMargin;
         int marginToAdd = 32;
         p.setMargins(margin + marginToAdd, 0, 0, 0);
         view.requestLayout();
      }
   }

   public void decreaseMargin(View view, int position) {
      if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
         ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
         int margin = p.leftMargin;
         int marginToAdd = -32;
         if (margin + marginToAdd > 0) p.setMargins(margin + marginToAdd, 0, 0, 0);
         view.requestLayout();
      }
   }

   private void saveTasksPositions() {
      List<Integer> listOfTasksId = new ArrayList<>();
      for (LocalTask task : tasks) {
         listOfTasksId.add(task.getIntId());
      }
      Gson gson = new Gson();
      String jsonListOfSortedTaskIds = gson.toJson(listOfTasksId);
      mPresenter.saveStringSharedPreference(Co.TASK_ID_ORDERED_LIST, jsonListOfSortedTaskIds);
   }

   private void setActionMode(ActionMode actionMode) {
      mActionMode = actionMode;
   }

   private void deleteItems() {

      SparseArray<LocalTask> map = new SparseArray<>();
      for (int i = tasks.size(); i >= 0; i--) {
         if (mMultiSelector.isSelected(i, 0)) {
            map.put(i, tasks.get(i));
            tasks.remove(i);
            notifyItemRemoved(i);
            if (tasks.isEmpty()) {
               mPresenter.showEmptyRecyclerView(true);
            }
         }
      }
      mPresenter.showUndoSnackBar(context.getString(R.string.task_deleted), map);

   }

   @Override
   public TaskListAdapter.TaskListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View itemLayoutView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_item, parent, false);
      return new TaskListViewHolder(itemLayoutView, context, mPresenter);
   }

   private List<LocalTask> getSortedTasks(List<LocalTask> tasks) {
      String jsonListOfSortedTaskIds = mPresenter.getStringShP(Co.TASK_ID_ORDERED_LIST, null);
      List<LocalTask> tasksCopy = new ArrayList<>(tasks);
      List<LocalTask> sortedTasks = new ArrayList<>();
      if (jsonListOfSortedTaskIds != null && !jsonListOfSortedTaskIds.isEmpty() /*&& !jsonListOfSortedTaskIds.equals(Co.NO_VALUE)*/) {
         //convert JSON array into a List<Long>
         Gson gson = new Gson();
         List<Integer> listOfSortedTaskIds = gson.fromJson(jsonListOfSortedTaskIds,
               new TypeToken<List<Integer>>() {
               }.getType());

         //build sorted list
         if (listOfSortedTaskIds != null && listOfSortedTaskIds.size() > 0) {
            for (Integer id : listOfSortedTaskIds) {
               for (LocalTask task : tasksCopy) {
                  if (task.getIntId() == id) {
                     sortedTasks.add(task);
                     tasksCopy.remove(task);
                     break;
                  }
               }
            }
         }
         if (!tasksCopy.isEmpty()) {
            for (int i = tasksCopy.size() - 1; i >= 0; i--) {
               sortedTasks.add(0, tasksCopy.get(i));
            }
         }
      } else {
         return tasks;
      }
      return sortedTasks;
   }


   private StateListDrawable getHighlightedBackground() {
      ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorLightGray));
      StateListDrawable stateListDrawable = new StateListDrawable();
      stateListDrawable.addState(new int[]{16843518}, colorDrawable);
      stateListDrawable.addState(StateSet.WILD_CARD, null);
      return stateListDrawable;
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
      this.tasks = getSortedTasks(localTasks);
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

   boolean isSelectableMode() {
      if (mMultiSelector == null){
         return false;
      } else {
         return mMultiSelector.isSelectable();
      }
   }

   void leaveSelectMode() {
      mMultiSelector.clearSelections();
      mMultiSelector.setSelectable(false);
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
      mMultiSelector.setSelected(newPos, (long) tasks.get(newPos).getIntId(), true);
      mMultiSelector.setSelected(fromPosition, (long) tasks.get(fromPosition).getIntId(), false);
      notifyItemMoved(fromPosition, toPosition);
      return true;
   }

   @Override
   public void onItemSwiped(int position, int direction) {
//      if (direction == ItemTouchHelper.END) {
//      } else {
//         removedTask = tasks.remove(position);
//         notifyItemRemoved(position);
//         mPresenter.showUndoSnackBar(context.getString(R.string.task_deleted), position, removedTask);
//         if (tasks.isEmpty()) {
//            mPresenter.showEmptyRecyclerView(true);
//         }
//      }
   }

   void restoreDeletedItems(SparseArray map) {
      for (int i = 0; i < map.size(); i++) {
         tasks.add((LocalTask) map.valueAt(i));
      }
      for (int i = 0; i < map.size(); i++) {
         LocalTask task = (LocalTask) map.valueAt(i);
         tasks.remove(tasks.indexOf(task));
         tasks.add(map.keyAt(i), task);
      }
      notifyDataSetChanged();
      mPresenter.showEmptyRecyclerView(false);
      showToast(context.getString(R.string.tasks_restored));
   }

   @Override
   public long getItemId(int position) {
      return tasks.get(position).getIntId();
   }

   void updateItem(LocalTask task, int position) {
      if (position == -1) {
         tasks.set(tasks.indexOf(task), task);
         notifyItemChanged(tasks.indexOf(task));
      } else {
         tasks.set(position, task);
         notifyItemChanged(position);
      }
   }


   //------------------------VIEW HOLDER-------------------------------///
   class TaskListViewHolder extends SwappingHolder implements View.OnClickListener,
         CompoundButton.OnCheckedChangeListener, View.OnLongClickListener, SelectableHolder,
         ItemTouchHelperViewHolder {

      Space indentView;
      ImageView notesTitleIcon;
      TextView taskTitleTextView, dueDateTextView;
      ImageView notificationImage;
      CheckBox taskCheckbox;
      Context context;
      TaskListPresenter mPresenter;
      ColorStateList normalTaskColor;
      ColorStateList normalDueColor;
      RelativeLayout parentView;

      TaskListViewHolder(View v, Context context, TaskListPresenter presenter) {
         super(v, mMultiSelector);
         this.context = context;
         this.mPresenter = presenter;
         setSelectionModeBackgroundDrawable(getHighlightedBackground());
         v.setOnClickListener(this);
         v.setOnLongClickListener(this);
         dueDateTextView = (TextView) v.findViewById(R.id.textViewDate);
         notesTitleIcon = (ImageView) v.findViewById(R.id.notesIconInTitle);
         parentView = (RelativeLayout) v.findViewById(R.id.itemParent);
//         indentView = (Space) v.findViewById(R.id.indentSpace);
         taskTitleTextView = (TextView) v.findViewById(R.id.textViewName);
         notificationImage = (ImageView) v.findViewById(R.id.notificationImage);
         taskCheckbox = (CheckBox) v.findViewById(R.id.taskCheckbox);
         taskCheckbox.setOnCheckedChangeListener(this);
         normalTaskColor = taskTitleTextView.getTextColors();
         normalDueColor = dueDateTextView.getTextColors();
      }

      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

         LocalTask cTask = tasks.get(getAdapterPosition());
         if (isChecked) {
            //Task marked completed
            cTask.setStatus(Co.TASK_COMPLETED);
            cTask.setReminderNoID(0);
            cTask.setLocalModify(System.currentTimeMillis());
            cTask.setSyncStatus(Co.EDITED_NOT_SYNCED);
            cTask.setCompleted(System.currentTimeMillis());
            mPresenter.updateExistingTaskFromLocalTask(cTask);
            AlarmHelper.cancelTaskReminder(cTask, context);
            if (mPresenter.getBooleanShP(Co.DEFAULT_REMINDER_PREF_KEY, true)) {
               List<LocalTask> task = new ArrayList<>();
               task.add(cTask);
               AlarmHelper.cancelDefaultRemindersForTasks(context, task);
            }
            if (cTask.getId() != null && cTask.getListId() != null) {
               mPresenter.updateTaskStatusInServer(cTask, Co.TASK_COMPLETED);
            }
         } else {
            //Task mark not completed
            cTask.setStatus(Co.TASK_NEEDS_ACTION);
            cTask.setCompleted(0);
            if (cTask.getReminder() != 0) {
               AlarmHelper.setOrUpdateAlarm(cTask, context);
            }
            if (mPresenter.getBooleanShP(Co.DEFAULT_REMINDER_PREF_KEY, true) &&
                  cTask.getDue() != 0 && cTask.getDue() > Calendar.getInstance().getTimeInMillis()) {
               AlarmHelper.setOrUpdateDefaultRemindersForTask(context, cTask);
            }
            mPresenter.updateTaskStatusInDb(cTask.getIntId(), Co.TASK_NEEDS_ACTION);
            if (cTask.getId() != null && cTask.getListId() != null) {
               mPresenter.updateTaskStatusInServer(cTask, Co.TASK_NEEDS_ACTION);
            }
         }
         setTitleText(this, cTask);
         setDueDateText(this, cTask);
         notifyItemChanged(getAdapterPosition());
         mPresenter.updateTaskCounterForDrawer(cTask.getListIntId(), null);
      }

      @Override
      public void onItemSelected() {
         oldPos = getAdapterPosition();
      }

      @Override
      public void onItemClear() {
         if (getAdapterPosition() != oldPos) {
            moved = true;
            newPos = getAdapterPosition();
            String prevTaskId = null;
            LocalTask movedTask = tasks.get(newPos);
            if (newPos > 0) {
               prevTaskId = tasks.get(newPos - 1).getId();
            }
            moveMap.put(movedTask, prevTaskId);
         }
      }

      @Override
      public void onClick(View v) {

         if (!mMultiSelector.tapSelection(TaskListViewHolder.this)) {
            mPresenter.showBottomSheet(tasks.get(getAdapterPosition()), getAdapterPosition(), true);
         }
         if (mActionMode != null) {
            int selectedQty = mMultiSelector.getSelectedPositions().size();
            String text = selectedQty > 1 ? context.getString(R.string.selected_plural) :
                  context.getString(R.string.selected);
            if (selectedQty == 0) {
               mMultiSelector.setSelectable(false);
               mActionMode.finish();
            } else {
               if (selectedQty == 1) {
                  mActionMode.getMenu().findItem(R.id.make_child).setVisible(true);
               } else {
                  mActionMode.getMenu().findItem(R.id.make_child).setVisible(false);
               }
               mActionMode.setTitle(String.valueOf(selectedQty) + " " + text);
            }
         }
      }

      @Override
      public boolean onLongClick(View v) {
         if (!mMultiSelector.isSelectable()) {
            ((AppCompatActivity) context).startSupportActionMode(mActionModeCallback); // (2)
            mMultiSelector.setSelectable(true);
            mMultiSelector.setSelected(TaskListViewHolder.this, true);
            if (mActionMode != null) {
               mActionMode.setTitle(String.valueOf(1) + " " + context.getString(R.string.selected));
            }
            return true;
         } else {
            mMultiSelector.clearSelections();
            mMultiSelector.setSelected(TaskListViewHolder.this, true);
            if (mActionMode != null) {
               int selectedQty = mMultiSelector.getSelectedPositions().size();
               String text = selectedQty > 1 ? context.getString(R.string.selected_plural) :
                     context.getString(R.string.selected);
               mActionMode.setTitle(String.valueOf(selectedQty) + " " + text);
            }
            return false;
         }
      }


   }
}

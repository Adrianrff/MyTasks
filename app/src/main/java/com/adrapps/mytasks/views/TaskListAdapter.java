package com.adrapps.mytasks.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder>
      implements ItemTouchHelperAdapter {

   private Context context;
   private List<LocalTask> tasks;
   private TaskListPresenter mPresenter;
   private int newPos;
   private int oldPos;
   ActionMode mActionMode;
   private MultiSelector mMultiSelector;
   private ModalMultiSelectorCallback mActionModeCallback;
   private LinkedHashMap<LocalTask, String> moveMap;
   private LocalTask movedTask;
   private boolean moved;


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
            return false;
         }

         @Override
         public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return super.onPrepareActionMode(actionMode, menu);
         }

         @Override
         public void onDestroyActionMode(ActionMode actionMode) {
            super.onDestroyActionMode(actionMode);
            mPresenter.swipeRefreshSetEnabled(true);
            mPresenter.showFab(true);
            Co.IS_MULTISELECT_ENABLED = false;
            if (moved){
               saveTasksPositions();
            }
            if (!moveMap.isEmpty()){
               mPresenter.moveTasks(moveMap);
            }
         }
      };
   }

   private void saveTasksPositions() {
      List<Integer> listOfTasksId = new ArrayList<>();
      for (LocalTask task: tasks){
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
      String jsonListOfSortedTaskIds = mPresenter.getStringShP(Co.TASK_ID_ORDERED_LIST);
      List<LocalTask> tasksCopy = new ArrayList<>(tasks);
      List<LocalTask> sortedTasks = new ArrayList<>();
      if (!jsonListOfSortedTaskIds.isEmpty() && !jsonListOfSortedTaskIds.equals(Co.NO_VALUE)) {
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
         if (!tasksCopy.isEmpty()){
            for (int i = tasksCopy.size() - 1; i >= 0; i--){
               sortedTasks.add(0, tasksCopy.get(i));
            }
         }
      } else {
         return tasks;
      }
      return sortedTasks;
   }

   @Override
   public void onBindViewHolder(final TaskListAdapter.TaskListViewHolder holder, int position) {
      LocalTask cTask = tasks.get(position);
      holder.setSelectionModeBackgroundDrawable(getHighlightedBackground());
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
            holder.dueDate.setText(DateHelper.millisToDateOnly(cTask.getDue())
                  + " " + context.getString(R.string.overdue_append));
            holder.dueDate.setTypeface(null, Typeface.NORMAL);
            holder.dueDate.setTextColor(Color.RED);
         } else {
            holder.dueDate.setTextColor(holder.oldDueColors);
            holder.dueDate.setTypeface(null, Typeface.NORMAL);
            holder.dueDate.setText(DateHelper.millisToDateOnly(cTask.getDue()));
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

   private StateListDrawable getHighlightedBackground() {
      ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorLightGray));
      StateListDrawable stateListDrawable = new StateListDrawable();
      stateListDrawable.addState(new int[]{16843518}, colorDrawable);
      stateListDrawable.addState(StateSet.WILD_CARD, (Drawable) null);
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
      return mMultiSelector.isSelectable();
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

      TextView taskName, dueDate;
      ImageView notificationImage;
      CheckBox taskCheckbox;
      Context context;
      TaskListPresenter mPresenter;
      ColorStateList oldTaskColors;
      ColorStateList oldDueColors;

      TaskListViewHolder(View v, Context context, TaskListPresenter presenter) {
         super(v, mMultiSelector);
         this.context = context;
         this.mPresenter = presenter;
         v.setOnClickListener(this);
         v.setOnLongClickListener(this);
         dueDate = (TextView) v.findViewById(R.id.textViewDate);
         taskName = (TextView) v.findViewById(R.id.textViewName);
         notificationImage = (ImageView) v.findViewById(R.id.notificationImage);
         taskCheckbox = (CheckBox) v.findViewById(R.id.taskCheckbox);
         taskCheckbox.setOnCheckedChangeListener(this);
         oldTaskColors = taskName.getTextColors();
         oldDueColors = dueDate.getTextColors();
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
                  dueDate.setText(DateHelper.millisToDateOnly(cTask.getDue())
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

      }


      @Override
      public void onItemClear() {
         if (getAdapterPosition() != oldPos) {
            moved = true;
            newPos = getAdapterPosition();
            String prevTaskId = null;
            movedTask = tasks.get(newPos);
            if (newPos > 0){
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
//               mPresenter.showFab(true);
               return;
            }
            mActionMode.setTitle(String.valueOf(selectedQty) + " " + text);
         }
//
//         if (mMultiSelector.getSelectedPositions().isEmpty()) {
//            mMultiSelector.clearSelections();
//            mMultiSelector.setSelectable(false);
//            mMultiSelector.setSelected(TaskListViewHolder.this, false);
//         }
      }

      @Override
      public boolean onLongClick(View v) {
//         if (swipeLayout.isClosed()) {
//            showToast("closed");
//         } else {
//            showToast("open");
//         }
         if (!mMultiSelector.isSelectable()) {
            ((AppCompatActivity) context).startSupportActionMode(mActionModeCallback); // (2)
            mMultiSelector.setSelectable(true);
            mMultiSelector.setSelected(TaskListViewHolder.this, true);
            if (mActionMode != null) {
               mActionMode.setTitle(String.valueOf(1) + " " + context.getString(R.string.selected));
            }
//            mPresenter.showFab(false);
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
//         }
//         return false;
      }

   }
}

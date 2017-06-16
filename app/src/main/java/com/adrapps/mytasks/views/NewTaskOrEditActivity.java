package com.adrapps.mytasks.views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.DateHelper;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;
import com.nex3z.togglebuttongroup.button.CircularToggle;

import java.util.Calendar;

import static com.adrapps.mytasks.R.string.afternoon;
import static com.adrapps.mytasks.R.string.evening;
import static com.adrapps.mytasks.R.string.morning;

public class NewTaskOrEditActivity extends AppCompatActivity
      implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
      PopupMenu.OnMenuItemClickListener, SingleSelectToggleGroup.OnCheckedChangeListener {

   EditText titleTV, notesTv;
   private LinearLayout reminderDetailsLayout, reminderDateLayout;
   private LocalTask taskToEdit;
   private int position, repeatMode, repeatDay;
   private ImageView clearDate, clearReminder;
   private TextView reminderTv, dueDateTv, nextReminderTV, reminderDateTV, reminderTimeTV, repeatTV;
   private Calendar taskReminder, dueDate, tempReminder;
   private Menu repeatMenu;
   private PopupMenu repeatPopupMenu;
   private AlertDialog reminderDateDialog, reminderTimeDialog;
   private String SAME_DAY;
   private String DAY_BEFORE;
   private String WEEK_BEFORE;
   private SingleSelectToggleGroup weekdaysGroup;
   private CircularToggle mon, tue, wed, thu, fri, sat, sun;
   private SparseIntArray calendarDaysMap;
   private SparseArray<CircularToggle> daysToggleMap;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.new_task);
      SAME_DAY = getString(R.string.same_day);
      DAY_BEFORE = getString(R.string.day_before);
      WEEK_BEFORE = getString(R.string.week_before);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      if (!getIntent().hasExtra(Co.LOCAL_TASK))
         toolbar.setTitle(R.string.new_task_title);
      setSupportActionBar(toolbar);
      if (getSupportActionBar() != null) {
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      }

      taskReminder = null;
      repeatMode = 0;
      dueDate = null;
      findViewsAndSetListeners();

      if (savedInstanceState != null) {
         dueDate = (Calendar) savedInstanceState.getSerializable(Co.STATE_DUE_DATE);
         taskReminder = (Calendar) savedInstanceState.getSerializable(Co.STATE_TASK_REMINDER);
         repeatMode = savedInstanceState.getInt(Co.STATE_REPEAT_MODE, 0);
      }

      repeatPopupMenu = new PopupMenu(this, reminderTimeTV);
      repeatPopupMenu.getMenuInflater().inflate(R.menu.repeat_menu, repeatPopupMenu.getMenu());
      repeatPopupMenu.setOnMenuItemClickListener(this);

      if (getIntent().hasExtra(Co.LOCAL_TASK)) {
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
         getSupportActionBar().setTitle(R.string.task_edit_title);
         taskToEdit = (LocalTask) getIntent().getExtras().getSerializable(Co.LOCAL_TASK);
         position = getIntent().getIntExtra(Co.ADAPTER_POSITION, -1);
         if (taskToEdit != null) {
            titleTV.setText(taskToEdit.getTitle());
            notesTv.setText(taskToEdit.getNotes());
            if (taskToEdit.getDue() != 0 || dueDate != null) {
               if (dueDate == null) {
                  dueDate = Calendar.getInstance();
                  dueDate.setTimeInMillis(taskToEdit.getDue());
               }
               dueDateTv.setText(DateHelper.millisToDateOnly(dueDate.getTimeInMillis()));
               clearDate.setVisibility(View.VISIBLE);
            } else {
               dueDate = null;
               clearDate.setVisibility(View.GONE);
            }
            if (taskToEdit.getReminder() != 0 || taskReminder != null) {
               if (taskReminder == null) {
                  taskReminder = Calendar.getInstance();
                  taskReminder.setTimeInMillis(taskToEdit.getReminder());
               }
               reminderDetailsLayout.setVisibility(View.VISIBLE);
               repeatMode = taskToEdit.getRepeatMode();
               if (repeatMode != 0) {
                  reminderDateLayout.setVisibility(View.GONE);
                  if (repeatMode == Co.REMINDER_WEEKLY) {
                     weekdaysGroup.setVisibility(View.VISIBLE);
                     repeatDay = taskToEdit.getRepeatDay();

                     CircularToggle selectedToggle = daysToggleMap.get(taskToEdit.getRepeatDay());
                     if (selectedToggle != null) {
                        selectedToggle.setChecked(true);
                     }
                  } else {
                     weekdaysGroup.setVisibility(View.GONE);
                  }
               } else {
                  reminderDateLayout.setVisibility(View.VISIBLE);
               }
               onReminderChange();
            } else {
               reminderDetailsLayout.setVisibility(View.INVISIBLE);
               clearReminder.setVisibility(View.GONE);
            }
         }
      } else {
         toolbar.setTitle(getString(R.string.new_task_title));
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
         reminderDetailsLayout.setVisibility(View.GONE);
      }
   }

   @Override
   protected void onStop() {
      super.onStop();
      repeatPopupMenu.dismiss();
   }

   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putSerializable(Co.STATE_DUE_DATE, dueDate);
      outState.putSerializable(Co.STATE_TASK_REMINDER, taskReminder);
      outState.putInt(Co.STATE_REPEAT_MODE, repeatMode);
   }

   @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState) {
      try {
         super.onRestoreInstanceState(savedInstanceState);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   protected void onResume() {
      super.onResume();
      repeatPopupMenu.dismiss();
   }

   private void findViewsAndSetListeners() {
      titleTV = (EditText) findViewById(R.id.task_title_edit_text);
      dueDateTv = (TextView) findViewById(R.id.dueDateTv);
      notesTv = (EditText) findViewById(R.id.task_notes_edit_text);
      weekdaysGroup = (SingleSelectToggleGroup) findViewById(R.id.weekdays_group);
      weekdaysGroup.setOnCheckedChangeListener(this);
      mon = (CircularToggle) findViewById(R.id.mon);
      tue = (CircularToggle) findViewById(R.id.tue);
      wed = (CircularToggle) findViewById(R.id.wed);
      thu = (CircularToggle) findViewById(R.id.thu);
      fri = (CircularToggle) findViewById(R.id.fri);
      sat = (CircularToggle) findViewById(R.id.sat);
      sun = (CircularToggle) findViewById(R.id.sun);
      daysToggleMap = new SparseArray<>();
      daysToggleMap.put(Co.MONDAY, mon);
      daysToggleMap.put(Co.TUESDAY, tue);
      daysToggleMap.put(Co.WEDNESDAY, wed);
      daysToggleMap.put(Co.THURSDAY, thu);
      daysToggleMap.put(Co.FRIDAY, fri);
      daysToggleMap.put(Co.SATURDAY, sat);
      daysToggleMap.put(Co.SUNDAY, sun);
      calendarDaysMap = new SparseIntArray();
      calendarDaysMap.put(Co.MONDAY, Calendar.MONDAY);
      calendarDaysMap.put(Co.TUESDAY, Calendar.TUESDAY);
      calendarDaysMap.put(Co.WEDNESDAY, Calendar.WEDNESDAY);
      calendarDaysMap.put(Co.THURSDAY, Calendar.THURSDAY);
      calendarDaysMap.put(Co.FRIDAY, Calendar.FRIDAY);
      calendarDaysMap.put(Co.SATURDAY, Calendar.SATURDAY);
      calendarDaysMap.put(Co.SUNDAY, Calendar.SUNDAY);

      nextReminderTV = (TextView) findViewById(R.id.next_reminder_label);
      reminderDetailsLayout = (LinearLayout) findViewById(R.id.notification_layout);
      reminderDateLayout = (LinearLayout) findViewById(R.id.reminder_date_layout);
      reminderTv = (TextView) findViewById(R.id.notificationTextView);
      clearDate = (ImageView) findViewById(R.id.clearDate);
      clearReminder = (ImageView) findViewById(R.id.clearReminder);
      repeatTV = (TextView) findViewById(R.id.repeat_tv);
      reminderDateTV = (TextView) findViewById(R.id.reminder_date_tv);
      reminderTimeTV = (TextView) findViewById(R.id.reminder_time_tv);
      reminderDateTV.setOnClickListener(this);
      reminderTimeTV.setOnClickListener(this);
      repeatTV.setOnClickListener(this);
      clearDate.setOnClickListener(this);
      clearReminder.setOnClickListener(this);
      reminderTv.setOnClickListener(this);
      dueDateTv.setOnClickListener(this);
   }

   private void setRepeatMenuItems() {
      if (taskReminder != null) {
         repeatMenu = repeatPopupMenu.getMenu();
//         String sameDayWeek;
//         String sameDayMonth;
//         sameDayWeek = getString(R.string.every) + " " +
//               DateHelper.timeInMillsToDay(taskReminder.getTimeInMillis());
//         repeatMenu.findItem(R.id.weekly).setTitle(sameDayWeek);
//         int dayOfMonth = taskReminder.get(Calendar.DAY_OF_MONTH);
//         if (dayOfMonth == taskReminder.getActualMaximum(Calendar.DAY_OF_MONTH)) {
//            sameDayMonth = getString(R.string.last_day_of_month) + " " +
//                  getString(R.string.of_every_month);
//         } else {
//            sameDayMonth = (getString(R.string.on_day) + " " + taskReminder.get(Calendar.DAY_OF_MONTH) +
//                  " " + getString(R.string.of_every_month));
//         }
//         repeatMenu.findItem(R.id.monthly).setTitle(sameDayMonth);
         repeatTV.setText(repeatMenu.getItem(repeatMode).getTitle());
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.task_detail_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {

      switch (item.getItemId()) {

         case R.id.settings:
            showToast(String.valueOf(repeatDay));

            break;

         case R.id.save_task:

            //TASK EDITED
            if (getIntent().hasExtra(Co.LOCAL_TASK)) {
               Intent i = new Intent();
               if (taskReminder == null) {
                  taskToEdit.setReminderNoID(0);
               } else {
                  taskToEdit.setRepeatMode(repeatMode);
                  if (taskToEdit.getReminderId() != 0) {
                     taskToEdit.setReminderNoID(taskReminder.getTimeInMillis());
                  } else {
                     taskToEdit.setReminder(taskReminder.getTimeInMillis());
                  }
                  if (repeatMode == Co.REMINDER_WEEKLY && repeatDay > 0) {
                     taskToEdit.setRepeatDay(repeatDay);
                  }
               }

               boolean noTitleChange = taskToEdit.getTitle().trim().equals(titleTV.getText().toString().trim());
               boolean noDueDateChange = ((dueDate == null && taskToEdit.getDue() == 0) ||
                     (dueDate != null && (dueDate.getTimeInMillis() == taskToEdit.getDue())));
               boolean noNotesChange = (notesTv.getText().toString().isEmpty() &&
                     taskToEdit.getNotes() == null) ||
                     (taskToEdit.getNotes() != null && !notesTv.getText().toString().isEmpty() &&
                           notesTv.getText().toString().trim().equals(taskToEdit.getNotes()));
               if (noTitleChange && noDueDateChange && noNotesChange) {
                  i.putExtra(Co.NO_API_EDIT, true);
               } else {
                  taskToEdit.setTitle(titleTV.getText().toString());
                  if (notesTv.getText().toString().trim().length() != 0)
                     taskToEdit.setNotes(notesTv.getText().toString());
                  if (dueDate != null && dueDate.getTimeInMillis() != taskToEdit.getDue()) {
                     taskToEdit.setDue(dueDate.getTimeInMillis());
                  }
                  if (dueDate == null && taskToEdit.getDue() != 0) {
                     taskToEdit.setDue(0);
                  }
               }
               i.putExtra(Co.TASK_EDIT, true);
               i.putExtra(Co.LOCAL_TASK, taskToEdit);
               i.putExtra(Co.ADAPTER_POSITION, position);
               setResult(Activity.RESULT_OK, i);
               finish();
               break;

               //TASK CREATED
            } else {
               if (titleTV.getText().toString().trim().length() == 0) {
                  showToast(getString(R.string.empty_title_error));
                  break;
               }
               Intent i = new Intent();
               LocalTask task = new LocalTask();
               task.setTitle(titleTV.getText().toString());
               if (dueDate != null) {
                  task.setDue(dueDate.getTimeInMillis());
               }
               if (notesTv.getText().toString().trim().length() != 0)
                  task.setNotes(notesTv.getText().toString());
               if (taskReminder != null) {
                  task.setReminder(taskReminder.getTimeInMillis());
                  task.setRepeatMode(repeatMode);
                  if (repeatMode == Co.REMINDER_WEEKLY && repeatDay != 0) {
                     task.setRepeatDay(repeatDay);
                  }
               }
               i.putExtra(Co.LOCAL_TASK, task);
               i.putExtra(Co.NEW_TASK, true);
               setResult(Activity.RESULT_OK, i);
               finish();
               break;
            }
      }
      return super.onOptionsItemSelected(item);

   }

   @Override
   public void onClick(View v) {
      boolean reminderNotSet;
      switch (v.getId()) {

         //Clear date click
         case R.id.clearDate:
            dueDate = null;
            dueDateTv.setText(null);
            clearDate.setVisibility(View.GONE);
            if (reminderDetailsLayout.getVisibility() == View.VISIBLE) {
               if (reminderDateTV.getText().toString().
                     matches(SAME_DAY + "|" + DAY_BEFORE + "|" + WEEK_BEFORE)) {
                  reminderDateTV.setText(DateHelper.millisToDateOnly(taskReminder.getTimeInMillis()));
               }
            }
            break;

         case R.id.clearReminder:
            taskReminder = null;
            reminderTv.setText(null);
            reminderDateTV.setText(null);
            reminderTimeTV.setText(null);
            repeatMode = 0;
            repeatDay = 0;
            weekdaysGroup.setVisibility(View.GONE);
            repeatTV.setText(null);
            clearReminder.setVisibility(View.GONE);
            reminderDetailsLayout.setVisibility(View.GONE);
            break;

         //dueDate textview click
         case R.id.dueDateTv:
            Calendar now = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, this,
                  now.get(Calendar.YEAR),
                  now.get(Calendar.MONTH),
                  now.get(Calendar.DAY_OF_MONTH));
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePicker.show();
            break;

         case R.id.notificationTextView:
            if (reminderDetailsLayout.getVisibility() != View.VISIBLE) {
               if (dueDate != null) {
                  showReminderDateDialog();
               } else {
                  showReminderDatePicker();
               }
            }
            break;

         case R.id.reminder_date_tv:
            if (dueDate != null) {
               showReminderDateDialog();
            } else {
               showReminderDatePicker();
            }
            break;

         case R.id.reminder_time_tv:
            showReminderTimeDialog();
            break;

         case R.id.layout_same_day:
         case R.id.layout_day_before:
         case R.id.layout_week_before:
            if (taskReminder == null) {
               reminderNotSet = true;
               taskReminder = (Calendar) dueDate.clone();
            } else {
               reminderNotSet = false;
               taskReminder.set(Calendar.YEAR, dueDate.get(Calendar.YEAR));
               taskReminder.set(Calendar.MONTH, dueDate.get(Calendar.MONTH));
               taskReminder.set(Calendar.DAY_OF_MONTH, dueDate.get(Calendar.DAY_OF_MONTH));
            }
            if (reminderDateDialog != null) {
               reminderDateDialog.dismiss();
            }
            if (v.getId() == R.id.layout_day_before) {
               reminderDateTV.setText(SAME_DAY);
            }
            if (v.getId() == R.id.layout_day_before) {
               taskReminder.add(Calendar.DATE, -1);
               reminderDateTV.setText(DAY_BEFORE);
            }
            if (v.getId() == R.id.layout_week_before) {
               taskReminder.add(Calendar.DATE, -7);
               reminderDateTV.setText(WEEK_BEFORE);
            }
            if (v.getId() == R.id.layout_custom_date) {
               showReminderDatePicker();
               break;
            }
            if (reminderNotSet) {
               showReminderTimeDialog();
            } else {
               onReminderChange();
            }
            break;

         case R.id.layout_custom_date:
            if (reminderDateDialog != null) {
               reminderDateDialog.dismiss();
            }
            showReminderDatePicker();
            break;
//         case R.id.layout_same_day:
//
//            if (taskReminder == null) {
//               taskReminder = Calendar.getInstance();
//               reminderNotSet = true;
//            }
//            taskReminder = (Calendar) dueDate.clone();
//            if (reminderDateDialog != null) {
//               reminderDateDialog.dismiss();
//            }
//            if (reminderNotSet) {
//               showReminderTimeDialog();
//            }
//            reminderDateTV.setText(SAME_DAY);
//            break;
//
//         case R.id.layout_day_before:
//            if (taskReminder == null) {
//               taskReminder = Calendar.getInstance();
//            }
//            taskReminder = (Calendar) dueDate.clone();
//            taskReminder.add(Calendar.DATE, -1);
//            if (reminderDateDialog != null) {
//               reminderDateDialog.dismiss();
//            }
//            if (reminderNotSet) {
//               showReminderTimeDialog();
//            }
//            reminderDateTV.setText(DAY_BEFORE);
//            break;
//
//         case R.id.layout_week_before:
//            if (taskReminder == null) {
//               taskReminder = Calendar.getInstance();
//            }
//            taskReminder = (Calendar) dueDate.clone();
//            taskReminder.add(Calendar.DATE, -7);
//            if (reminderDateDialog != null) {
//               reminderDateDialog.dismiss();
//            }
//            showReminderTimeDialog();
//            reminderDateTV.setText(WEEK_BEFORE);
//            break;
//
//         case R.id.layout_custom_date:
//            if (reminderDateDialog != null) {
//               reminderDateDialog.dismiss();
//            }
//            showReminderDatePicker();
//            break;

         case R.id.layout_morning:
         case R.id.layout_afternoon:
         case R.id.layout_evening:
            if (reminderTimeDialog != null) {
               reminderTimeDialog.dismiss();
            }
            if (taskReminder != null) {
               taskReminder.set(Calendar.MINUTE, 0);
               taskReminder.set(Calendar.SECOND, 0);
               taskReminder.set(Calendar.MILLISECOND, 0);
            } else {
               taskReminder = Calendar.getInstance();
            }
            if (v.getId() == R.id.layout_morning) {
               taskReminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
               reminderTimeTV.setText(getString(R.string.morning));
            }
            if (v.getId() == R.id.layout_afternoon) {
               taskReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
               reminderTimeTV.setText(getString(R.string.afternoon));
            }
            if (v.getId() == R.id.layout_evening) {
               taskReminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
               reminderTimeTV.setText(getString(R.string.evening));
            }
//            if (reminderDetailsLayout.getVisibility() != View.VISIBLE) {
            onReminderChange();
//            }
            break;

         case R.id.layout_custom_time:
            if (reminderTimeDialog != null) {
               reminderTimeDialog.dismiss();
            }
            showReminderTimePicker();
            break;

//         case R.id.layout_morning:
//            if (taskReminder != null) {
//               taskReminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
//               taskReminder.set(Calendar.MINUTE, 0);
//               taskReminder.set(Calendar.SECOND, 0);
//               taskReminder.set(Calendar.MILLISECOND, 0);
//            } else {
//               taskReminder = Calendar.getInstance();
//            }
//            if (reminderTimeDialog != null) {
//               reminderTimeDialog.dismiss();
//            }
//            onReminderChange();
//            reminderTimeTV.setText(getString(R.string.morning));
//            break;
//
//         case R.id.layout_afternoon:
//            if (taskReminder != null) {
//               taskReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
//               taskReminder.set(Calendar.MINUTE, 0);
//               taskReminder.set(Calendar.SECOND, 0);
//               taskReminder.set(Calendar.MILLISECOND, 0);
//            }
//            if (reminderTimeDialog != null) {
//               reminderTimeDialog.dismiss();
//            }
//            onReminderChange();
//            reminderTimeTV.setText(getString(R.string.afternoon));
//            break;
//
//         case R.id.layout_evening:
//            if (taskReminder != null) {
//               taskReminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
//               taskReminder.set(Calendar.MINUTE, 0);
//               taskReminder.set(Calendar.SECOND, 0);
//               taskReminder.set(Calendar.MILLISECOND, 0);
//            }
//            if (reminderTimeDialog != null) {
//               reminderTimeDialog.dismiss();
//            }
//            onReminderChange();
//            reminderTimeTV.setText(getString(R.string.evening));
//            break;
//         case R.id.layout_custom_time:
//            if (reminderTimeDialog != null) {
//               reminderTimeDialog.dismiss();
//            }
//            showReminderTimePicker();

         case R.id.repeat_tv:
            setRepeatMenuItems();
            repeatPopupMenu.show();
            break;
      }

   }

   public void showReminderTimeDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      LayoutInflater inflater = this.getLayoutInflater();
      View dialogView = inflater.inflate(R.layout.reminder_time_dialog, null);
      LinearLayout morningLayout = (LinearLayout) dialogView.findViewById(R.id.layout_morning);
      LinearLayout afternoonLayout = (LinearLayout) dialogView.findViewById(R.id.layout_afternoon);
      LinearLayout eveningLayout = (LinearLayout) dialogView.findViewById(R.id.layout_evening);
      LinearLayout customTimeLayout = (LinearLayout) dialogView.findViewById(R.id.layout_custom_time);
      TextView morningTv = (TextView) dialogView.findViewById(R.id.morningTv);
      TextView afternoonTv = (TextView) dialogView.findViewById(R.id.afternoonTv);
      TextView eveningTv = (TextView) dialogView.findViewById(R.id.eveningTv);
      morningLayout.setOnClickListener(this);
      afternoonLayout.setOnClickListener(this);
      eveningLayout.setOnClickListener(this);
      customTimeLayout.setOnClickListener(this);
      builder.setView(dialogView);
      builder.setTitle("A qué hora...");
      builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_time_18dp));
      reminderTimeDialog = builder.create();
      if (taskReminder != null) {
         if (isReminderDateToday() && repeatMode == Co.REMINDER_ONE_TIME) {
            Calendar now = now();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            if (hour >= Co.EVENING_ALARM_HOUR) {
               showReminderTimePicker();
               return;
            }
            if (hour > Co.MORNING_ALARM_HOUR) {
               morningTv.setTextColor(Color.GRAY);
               morningLayout.setOnClickListener(null);
            }
            if (hour > Co.AFTERNOON_ALARM_HOUR) {
               afternoonTv.setTextColor(Color.GRAY);
               afternoonLayout.setOnClickListener(null);
            }

         }
      }
      reminderTimeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
         @Override
         public void onCancel(DialogInterface dialog) {
            if (reminderDetailsLayout.getVisibility() != View.VISIBLE) {
               taskReminder = null;
            }
         }
      });
      reminderTimeDialog.show();
   }

   public void showReminderDateDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      LayoutInflater inflater = this.getLayoutInflater();
      View dialogView = inflater.inflate(R.layout.reminder_date_dialog, null);
      LinearLayout sameDayLayout = (LinearLayout) dialogView.findViewById(R.id.layout_same_day);
      LinearLayout dayBeforeLayout = (LinearLayout) dialogView.findViewById(R.id.layout_day_before);
      LinearLayout weekBeforeLayout = (LinearLayout) dialogView.findViewById(R.id.layout_week_before);
      LinearLayout customLayout = (LinearLayout) dialogView.findViewById(R.id.layout_custom_date);
      TextView dayBeforeTV = (TextView) dialogView.findViewById(R.id.dayBeforeTv);
      TextView weekBeforeTV = (TextView) dialogView.findViewById(R.id.weekBeforeTV);
      sameDayLayout.setOnClickListener(this);
      customLayout.setOnClickListener(this);
      builder.setView(dialogView);
      builder.setTitle("Cuándo...");
      builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_date));
      Calendar weekBeforeDueDate = (Calendar) dueDate.clone();
      weekBeforeDueDate.add(Calendar.DATE, -7);
      Calendar now = Calendar.getInstance();
      weekBeforeDueDate.set(Calendar.HOUR_OF_DAY, 23);
      weekBeforeDueDate.set(Calendar.MINUTE, 59);
      if (DateUtils.isToday(dueDate.getTimeInMillis())) {
         dayBeforeTV.setTextColor(Color.GRAY);
         weekBeforeTV.setTextColor(Color.GRAY);
      } else if (weekBeforeDueDate.before(now)) {
         weekBeforeTV.setTextColor(Color.GRAY);
         dayBeforeLayout.setOnClickListener(this);
      } else {
         dayBeforeLayout.setOnClickListener(this);
         weekBeforeLayout.setOnClickListener(this);
      }
      reminderDateDialog = builder.create();
      reminderDateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
         @Override
         public void onCancel(DialogInterface dialog) {
            if (reminderDetailsLayout.getVisibility() != View.VISIBLE) {
               taskReminder = null;
            }
         }
      });
      reminderDateDialog.show();
   }

   private void showReminderDatePicker() {
      Calendar now = Calendar.getInstance();
      DatePickerDialog reminderDatePicker = new DatePickerDialog(this, reminderDatePickerListener,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH));
      reminderDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
      reminderDatePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
         }
      });
      reminderDatePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
         @Override
         public void onCancel(DialogInterface dialog) {
            if (reminderDetailsLayout.getVisibility() != View.VISIBLE) {
               taskReminder = null;
            }
         }
      });
      reminderDatePicker.show();
   }

   DatePickerDialog.OnDateSetListener reminderDatePickerListener = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
         boolean reminderSet = true;
         if (taskReminder == null) {
            taskReminder = now();
            taskReminder.add(Calendar.HOUR_OF_DAY, 1);
            taskReminder.set(Calendar.MINUTE, 0);
            taskReminder.set(Calendar.SECOND, 0);
            reminderSet = false;
         }
         taskReminder.set(Calendar.YEAR, year);
         taskReminder.set(Calendar.MONTH, month);
         taskReminder.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//         if (reminderTimeTV.getText().toString().isEmpty()) {
//            reminderDateTV.setText(DateHelper.millisToDateOnly(taskReminder.getTimeInMillis()));
//            setReminderTimeMenu();
//            reminderTimeMenu.show();
//         } else {
//            reminderDateTV.setText(DateHelper.millisToDateOnly(taskReminder.getTimeInMillis()));
//         }
         if (reminderDetailsLayout.getVisibility() != View.VISIBLE) {
            showReminderTimeDialog();
         }
         if (reminderSet) {
            onReminderChange();
         }
//         onReminderChange();
//         setReminderTextView(taskReminder);
//         if (dueDate == null) {
//            reminderDateTV.setText(DateHelper.millisToDateOnly(taskReminder.getTimeInMillis()));
//         } else {
//            reminderDateTV.setText(getString(R.string.custom));
//         }
//         nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
//         reminderDatePicker.dismiss();
//         boolean isBeforeToday = Calendar.getInstance().before(taskReminder);
//         reminderTimeMenu.show();
//         setRepeatMenuItems(taskReminder);
//         repeatMode = 0;
//         repeatTV.setText(repeatMenu.findItem(R.id.one_time).getTitle());
      }
   };

   @Override
   public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
      dueDate = Calendar.getInstance();
      dueDate.set(year, month, dayOfMonth);
      dueDate.set(Calendar.HOUR_OF_DAY, 0);
      dueDate.set(Calendar.MINUTE, 0);
      dueDate.set(Calendar.SECOND, 0);
      dueDate.set(Calendar.MILLISECOND, 0);
      if (taskReminder != null) {
         onReminderChange();
      }
      dueDateTv.setText(DateHelper.millisToDateOnly(dueDate.getTimeInMillis()));
      clearDate.setVisibility(View.VISIBLE);

   }


   private void showReminderTimePicker() {
      Calendar now = now();
      TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
         @Override
         public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            taskReminder.set(Calendar.HOUR_OF_DAY, hourOfDay);
            taskReminder.set(Calendar.MINUTE, minute);
            taskReminder.set(Calendar.SECOND, 0);
            taskReminder.set(Calendar.MILLISECOND, 0);
            onReminderChange();
         }
      }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
      timePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
         @Override
         public void onCancel(DialogInterface dialog) {
            if (reminderDetailsLayout.getVisibility() != View.VISIBLE) {
               taskReminder = null;
            }
         }
      });
      timePicker.show();
   }

   public void setReminderTextView() {
      if (taskReminder == null) {
         reminderTv.setText(null);
      } else {
         if (repeatMode == 0) {
            reminderTv.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
         } else {
            reminderTv.setText(DateHelper.millisToTimeOnly(taskReminder.getTimeInMillis()));
         }
      }
   }

   public void onReminderChange() {
      Calendar now = now();
      if (reminderDetailsLayout.getVisibility() != View.VISIBLE) {
         reminderDetailsLayout.setVisibility(View.VISIBLE);
      }
      clearReminder.setVisibility(View.VISIBLE);
      if (repeatMode == Co.REMINDER_ONE_TIME) {
         reminderDateLayout.setVisibility(View.VISIBLE);
      } else {
         setRelativeReminder();
         if (repeatMode == Co.REMINDER_WEEKLY) {
            weekdaysGroup.setVisibility(View.VISIBLE);
//            if (repeatDay > 0) {
//               daysToggleMap.get(repeatDay).setChecked(true);
//               setWeeklyReminder();
//            }
         } else {
            weekdaysGroup.setVisibility(View.GONE);
         }
         reminderDateLayout.setVisibility(View.GONE);
      }
      setReminderDateTv();
      setReminderTimeTv();
      setReminderTextView();
      setRepeatMenuItems();
      nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
      repeatTV.setText(repeatMenu.getItem(repeatMode).getTitle());
      if (taskReminder == null || taskReminder.before(now)) {
         if (taskReminder != null && taskReminder.before(now)) {
            showToast(getString(R.string.past_reminder_no_effect));
         }
      }

   }

   private void setReminderTimeTv() {
      if (taskReminder != null) {
         int reminderHour = taskReminder.get(Calendar.HOUR_OF_DAY);
         int reminderMinute = taskReminder.get(Calendar.MINUTE);
         if (reminderHour == Co.MORNING_ALARM_HOUR && reminderMinute == 0) {
            reminderTimeTV.setText(getString(morning));
         } else if (reminderHour == Co.AFTERNOON_ALARM_HOUR && reminderMinute == 0) {
            reminderTimeTV.setText(getString(afternoon));
         } else if (reminderHour == Co.EVENING_ALARM_HOUR && reminderMinute == 0) {
            reminderTimeTV.setText(getString(evening));
         } else {
            reminderTimeTV.setText(DateHelper.millisToTimeOnly(taskReminder.getTimeInMillis()));
         }
      }

   }

   private void setReminderDateTv() {
      if (isReminderSameDateAsDueDate()) {
         reminderDateTV.setText(SAME_DAY);
      } else if (isReminderOneDayBeforeDueDate()) {
         reminderDateTV.setText(DAY_BEFORE);
      } else if (isReminderOneWeekBeforeDueDate()) {
         reminderDateTV.setText(WEEK_BEFORE);
      } else {
         reminderDateTV.setText(DateHelper.millisToDateOnly(taskReminder.getTimeInMillis()));
      }
   }


   public void showToast(String msg) {
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
   }
//   @Override
//   public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//      editedReminder = (Calendar) taskReminder.clone();
//      int dayOfWeek;
//      switch (position) {
//
//         case Co.REMINDER_ONE_TIME:
//            repeatMode = Co.REMINDER_ONE_TIME;
//            if (isFirstLaunch && !customReminderDateSet) {
////               setOriginalReminderFirstLaunch();
//               break;
//            }
//            if (dueDate == null && !customReminderDateSet) {
//               setEditedReminderToday();
//            }
//            nextReminderTV.setText(DateHelper.millisToFull(editedReminder.getTimeInMillis()));
//            reminderTv.setText(DateHelper.millisToFull(editedReminder.getTimeInMillis()));
//            break;
//
//         case Co.REMINDER_DAILY:
//            repeatMode = Co.REMINDER_DAILY;
//            if (isFirstLaunch && !customReminderDateSet) {
////               setOriginalReminderFirstLaunch();
//               break;
//            }
//            setEditedReminderToday();
//            if (editedReminder.before(now)) {
//               editedReminder.add(Calendar.DATE, 1);
//            }
//            nextReminderTV.setText(DateHelper.millisToFull(editedReminder.getTimeInMillis()));
//            reminderTv.setText(DateHelper.millisToTimeOnly(editedReminder.getTimeInMillis()));
//            break;
//
//         case Co.REMINDER_DAILY_WEEKDAYS:
//
//            repeatMode = Co.REMINDER_DAILY_WEEKDAYS;
//            if (isFirstLaunch && !customReminderDateSet) {
////               setOriginalReminderFirstLaunch();
//               break;
//            }
//            setEditedReminderToday();
//            if (editedReminder.before(now)) {
//               editedReminder.add(Calendar.DATE, 1);
//            }
//            if (!DateHelper.isWeekday(editedReminder)) {
//               if (editedReminder.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
//                  editedReminder.add(Calendar.DATE, 2);
//               } else {
//                  editedReminder.add(Calendar.DATE, 1);
//               }
//            }
//            nextReminderTV.setText(DateHelper.millisToFull(editedReminder.getTimeInMillis()));
//            reminderTv.setText(DateHelper.millisToTimeOnly(editedReminder.getTimeInMillis()));
//            break;
//
//         case Co.REMINDER_WEEKLY:
//            repeatMode = Co.REMINDER_WEEKLY;
//            if (isFirstLaunch && !customReminderDateSet) {
////               setOriginalReminderFirstLaunch();
//               break;
//            }
//            if (customReminderDateSet) {
//               dayOfWeek = editedReminder.get(Calendar.DAY_OF_WEEK);
//            } else {
//               if (dueDate != null) {
//                  dayOfWeek = dueDate.get(Calendar.DAY_OF_WEEK);
//               } else {
//                  dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
//               }
//            }
//            setEditedReminderToday();
//            if (dayOfWeek != now.get(Calendar.DAY_OF_WEEK)) {
//               while (editedReminder.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
//                  editedReminder.add(Calendar.DATE, 1);
//               }
//            } else if (editedReminder.before(now)) {
//               editedReminder.add(Calendar.DATE, 7);
//            }
//
//            nextReminderTV.setText(DateHelper.millisToFull(editedReminder.getTimeInMillis()));
//            reminderTv.setText(DateHelper.millisToTimeOnly(editedReminder.getTimeInMillis()));
//            break;
//
//         case Co.REMINDER_MONTHLY:
//            repeatMode = Co.REMINDER_MONTHLY;
//            if (isFirstLaunch && !customReminderDateSet) {
////               setOriginalReminderFirstLaunch();
//               break;
//            }
//            int dayOfMonth;
//            if (customReminderDateSet) {
//               dayOfMonth = editedReminder.get(Calendar.DAY_OF_MONTH);
//            } else {
//               if (dueDate == null) {
//                  dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
//               } else {
//                  dayOfMonth = dueDate.get(Calendar.DAY_OF_MONTH);
//               }
//            }
//            setEditedReminderToday();
//            if (dayOfMonth != now.get(Calendar.DAY_OF_MONTH)) {
//               editedReminder.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//            }
//            if (editedReminder.before(now)) {
//               editedReminder.add(Calendar.MONTH, 1);
//            }
//            nextReminderTV.setText(DateHelper.millisToFull(editedReminder.getTimeInMillis()));
//            reminderTv.setText(DateHelper.millisToTimeOnly(editedReminder.getTimeInMillis()));
//            break;
//
//
//      }
//   }

//   @Override
//   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//      if (isChecked) {
//         reminderDetailsLayout.setVisibility(View.VISIBLE);
//         if (taskReminder == null) {
//            if (dueDate != null) {
//               reminderDateMenu.show();
//            } else {
//               showReminderDatePicker();
//            }
//         } else {
//            setReminderTextView();
//         }
//         repeatMode = 0;
//         repeatTV.setText(R.string.one_time_repeat_mode);
//
////         ValueAnimator va = ValueAnimator.ofInt(0, 500);
////         va.setDuration(300);
////         va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
////            public void onAnimationUpdate(ValueAnimator animation) {
////               reminderDetailsLayout.getLayoutParams().height = (Integer) animation.getAnimatedValue();
////               reminderDetailsLayout.requestLayout();
////            }
////         });
////         va.addListener(new AnimatorListenerAdapter() {
////            @Override
////            public void onAnimationStart(Animator animation) {
////               super.onAnimationStart(animation);
////               reminderDetailsLayout.setVisibility(View.VISIBLE);
////            }
////         });
////         va.addListener(new AnimatorListenerAdapter() {
////            @Override
////            public void onAnimationEnd(Animator animation) {
////               super.onAnimationEnd(animation);
////
////            }
////         });
////
////         va.start();
//      } else {
//         taskReminder = null;
//         reminderTv.setText(null);
//         reminderDateTV.setText(null);
//         reminderTimeTV.setText(null);
//         repeatTV.setText(null);
//         reminderDetailsLayout.setVisibility(View.GONE);
////         ValueAnimator va = ValueAnimator.ofInt(500, 0);
////         va.setDuration(300);
////         va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
////            public void onAnimationUpdate(ValueAnimator animation) {
////               reminderDetailsLayout.getLayoutParams().height = (Integer) animation.getAnimatedValue();
////               reminderDetailsLayout.requestLayout();
////            }
////         });
////         va.addListener(new AnimatorListenerAdapter() {
////            @Override
////            public void onAnimationEnd(Animator animation) {
////               reminderDetailsLayout.setVisibility(View.GONE);
////            }
////         });
////         va.start();
//      }
//
//   }

   @Override
   public boolean onMenuItemClick(MenuItem item) {
      switch (item.getItemId()) {

         case R.id.one_time:
            repeatMode = Co.REMINDER_ONE_TIME;
            weekdaysGroup.setVisibility(View.GONE);
            if (taskReminder != null) {
               reminderTv.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            }
            if (dueDate != null) {
               taskReminder.set(Calendar.YEAR, dueDate.get(Calendar.YEAR));
               taskReminder.set(Calendar.MONTH, dueDate.get(Calendar.MONTH));
               taskReminder.set(Calendar.DAY_OF_MONTH, dueDate.get(Calendar.DAY_OF_MONTH));
            } else {
               showReminderDatePicker();
            }
            onReminderChange();
            break;

         case R.id.daily:
            repeatMode = Co.REMINDER_DAILY;
            onReminderChange();
            break;

         case R.id.weekdays:
            repeatMode = Co.REMINDER_DAILY_WEEKDAYS;
            onReminderChange();
            break;

         case R.id.weekly:
            if (repeatMode != Co.REMINDER_WEEKLY) {
               repeatMode = Co.REMINDER_WEEKLY;
               CircularToggle toggle = daysToggleMap.get(now().get(Calendar.DAY_OF_WEEK));
               if (weekdaysGroup.getCheckedId() == toggle.getId()){
                  setWeeklyReminder();
               } else {
                  toggle.setChecked(true);
               }
            }
            break;

         case R.id.monthly:
            repeatMode = Co.REMINDER_MONTHLY;
            onReminderChange();
            break;

      }
      return true;
   }

   private Calendar now() {
      return Calendar.getInstance();
   }

   private boolean isReminderSameDateAsDueDate() {
      return dueDate != null && taskReminder != null &&
            (dueDate.get(Calendar.YEAR) == taskReminder.get(Calendar.YEAR) &&
                  dueDate.get(Calendar.MONTH) == taskReminder.get(Calendar.MONTH) &&
                  dueDate.get(Calendar.DAY_OF_MONTH) == taskReminder.get(Calendar.DAY_OF_MONTH));
   }

   private boolean isReminderOneDayBeforeDueDate() {
      if (dueDate != null) {
         Calendar reminderTemp = (Calendar) dueDate.clone();
         reminderTemp.add(Calendar.DATE, -1);

         return taskReminder != null &&
               (reminderTemp.get(Calendar.YEAR) == taskReminder.get(Calendar.YEAR) &&
                     reminderTemp.get(Calendar.MONTH) == taskReminder.get(Calendar.MONTH) &&
                     reminderTemp.get(Calendar.DAY_OF_MONTH) == taskReminder.get(Calendar.DAY_OF_MONTH));
      } else {
         return false;
      }

   }

   private boolean isReminderOneWeekBeforeDueDate() {
      if (dueDate != null) {
         Calendar reminderTemp = (Calendar) dueDate.clone();
         reminderTemp.add(Calendar.DATE, -7);

         return taskReminder != null &&
               (reminderTemp.get(Calendar.YEAR) == taskReminder.get(Calendar.YEAR) &&
                     reminderTemp.get(Calendar.MONTH) == taskReminder.get(Calendar.MONTH) &&
                     reminderTemp.get(Calendar.DAY_OF_MONTH) == taskReminder.get(Calendar.DAY_OF_MONTH));
      } else {
         return false;
      }

   }

   public boolean isReminderDateToday() {
      return DateUtils.isToday(taskReminder.getTimeInMillis());
   }

   @Override
   public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
      setWeeklyReminder();
      onReminderChange();
   }

   public void setRelativeReminder() {
      Calendar now = now();
      switch (repeatMode) {
         case Co.REMINDER_DAILY:
            weekdaysGroup.setVisibility(View.GONE);
            repeatTV.setText(getString(R.string.daily_repeat_mode));
            taskReminder.set(Calendar.YEAR, now().get(Calendar.YEAR));
            taskReminder.set(Calendar.MONTH, now().get(Calendar.MONTH));
            taskReminder.set(Calendar.DAY_OF_MONTH, now().get(Calendar.DAY_OF_MONTH));

            if (taskReminder.before(now())) {
               taskReminder.add(Calendar.DATE, 1);
            }
            break;

         case Co.REMINDER_DAILY_WEEKDAYS:
            weekdaysGroup.setVisibility(View.GONE);
            taskReminder.set(Calendar.YEAR, now().get(Calendar.YEAR));
            taskReminder.set(Calendar.MONTH, now().get(Calendar.MONTH));
            taskReminder.set(Calendar.DAY_OF_MONTH, now().get(Calendar.DAY_OF_MONTH));
            if (taskReminder.before(now)) {
               taskReminder.add(Calendar.DATE, 1);
            }
            if (!DateHelper.isWeekday(taskReminder)) {
               if (taskReminder.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                  taskReminder.add(Calendar.DATE, 2);
               } else {
                  taskReminder.add(Calendar.DATE, 1);
               }
            }
            break;

         case Co.REMINDER_WEEKLY:
            weekdaysGroup.setVisibility(View.VISIBLE);
            CircularToggle toggle = daysToggleMap.get(repeatDay);
            if (weekdaysGroup.getCheckedId() == toggle.getId()){
               setWeeklyReminder();
            } else {
               toggle.setChecked(true);
            }
            break;

         case Co.REMINDER_MONTHLY:
            repeatMode = Co.REMINDER_MONTHLY;
            weekdaysGroup.setVisibility(View.GONE);
            reminderDateLayout.setVisibility(View.GONE);
            int dayOfMonth = taskReminder.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth != now.get(Calendar.DAY_OF_MONTH)) {
               taskReminder.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
            if (taskReminder.before(now)) {
               taskReminder.add(Calendar.MONTH, 1);
            }
            break;
      }
   }

   public void setWeeklyReminder() {
      Calendar now = now();
      taskReminder.set(Calendar.YEAR, now.get(Calendar.YEAR));
      taskReminder.set(Calendar.MONTH, now.get(Calendar.MONTH));
      taskReminder.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
      if (mon.isChecked()) {
         repeatDay = Calendar.MONDAY;
      }
      if (tue.isChecked()) {
         repeatDay = Calendar.TUESDAY;
      }
      if (wed.isChecked()) {
         repeatDay = Calendar.WEDNESDAY;
      }
      if (thu.isChecked()) {
         repeatDay = Calendar.THURSDAY;
      }
      if (fri.isChecked()) {
         repeatDay = Calendar.FRIDAY;
      }
      if (sat.isChecked()) {
         repeatDay = Calendar.SATURDAY;
      }
      if (sun.isChecked()) {
         repeatDay = Calendar.SUNDAY;
      }
      if (repeatDay != taskReminder.get(Calendar.DAY_OF_WEEK)) {
         while (taskReminder.get(Calendar.DAY_OF_WEEK) != repeatDay) {
            taskReminder.add(Calendar.DATE, 1);
         }
      } else if (taskReminder.before(now)) {
         taskReminder.add(Calendar.DATE, 7);
      }
   }
}

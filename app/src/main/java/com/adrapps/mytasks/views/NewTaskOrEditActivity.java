package com.adrapps.mytasks.views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
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

import java.util.Calendar;

import static com.adrapps.mytasks.R.string.afternoon;
import static com.adrapps.mytasks.R.string.day_before;
import static com.adrapps.mytasks.R.string.evening;
import static com.adrapps.mytasks.R.string.morning;
import static com.adrapps.mytasks.R.string.same_day;
import static com.adrapps.mytasks.R.string.week_before;

public class NewTaskOrEditActivity extends AppCompatActivity
      implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
      CompoundButton.OnCheckedChangeListener, PopupMenu.OnMenuItemClickListener {

   EditText titleTV, notesTv;
   private LinearLayout notificationDetailsLayout;
   private LocalTask taskToEdit;
   private int position, repeatMode;
   ImageView clearDate;
   private TextView notificationTV, dueDateTv, nextReminderTV, reminderDateTV, reminderTimeTV, repeatTV;
   SwitchCompat notificationSwitch;
   final private Calendar now = Calendar.getInstance();
   private Calendar taskReminder, dueDate;
   private boolean isFirstLaunch;
   private DatePickerDialog reminderDatePicker;
   private int reminderYear, reminderMonth, reminderDayOfMonth,
         reminderHour, reminderMinute, dueDateYear, dueDateMonth, dueDateDayOfMonth;
   private ArrayAdapter<String> reminderDateAdapter;
   private boolean timeSet;
   private Calendar editedReminder;
   private Menu repeatMenu;
   private PopupMenu repeatPopupMenu;
   private PopupMenu reminderDateMenu;
   private PopupMenu reminderTimeMenu;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.new_task);
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

      reminderDateMenu = new PopupMenu(this, reminderDateTV);
      reminderDateMenu.getMenuInflater().inflate(R.menu.reminder_date_menu, reminderDateMenu.getMenu());
      reminderDateMenu.setOnMenuItemClickListener(this);
      reminderTimeMenu = new PopupMenu(this, reminderTimeTV);
      reminderTimeMenu.getMenuInflater().inflate(R.menu.reminder_time_menu, reminderTimeMenu.getMenu());
      reminderTimeMenu.setOnMenuItemClickListener(this);

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
               dueDateYear = dueDate.get(Calendar.YEAR);
               dueDateMonth = dueDate.get(Calendar.MONTH);
               dueDateDayOfMonth = dueDate.get(Calendar.DAY_OF_MONTH);
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
               setReminderInfo(taskReminder.getTimeInMillis());
               if (repeatMode == 0 && taskReminder.before(now)) {
                  taskReminder = null;
                  return;
               }
               notificationDetailsLayout.setVisibility(View.VISIBLE);
               notificationSwitch.setChecked(true);
               setRepeatMenu(taskReminder);
               repeatTV.setText(repeatMenu.getItem(repeatMode).getTitle());
               notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
               nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
               timeSet = true;
            } else {
               notificationSwitch.setChecked(false);
               notificationDetailsLayout.setVisibility(View.INVISIBLE);
            }
         }
      } else {
         toolbar.setTitle(getString(R.string.new_task_title));
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
      }
   }

   @Override
   protected void onStop() {
      super.onStop();
      reminderDateMenu.dismiss();
      reminderTimeMenu.dismiss();
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
      reminderDateMenu.dismiss();
      reminderTimeMenu.dismiss();
      repeatPopupMenu.dismiss();
   }

   private void findViewsAndSetListeners() {
      titleTV = (EditText) findViewById(R.id.task_title_edit_text);
      dueDateTv = (TextView) findViewById(R.id.dueDateTv);
      notesTv = (EditText) findViewById(R.id.task_notes_edit_text);
      nextReminderTV = (TextView) findViewById(R.id.next_reminder_label);
      notificationDetailsLayout = (LinearLayout) findViewById(R.id.notification_layout);
      notificationTV = (TextView) findViewById(R.id.notificationTextView);
      clearDate = (ImageView) findViewById(R.id.clearDate);
      notificationSwitch = (SwitchCompat) findViewById(R.id.notification_switch);
      repeatTV = (TextView) findViewById(R.id.repeat_tv);
      reminderDateTV = (TextView) findViewById(R.id.reminder_date_tv);
      reminderTimeTV = (TextView) findViewById(R.id.reminder_time_tv);
      reminderDateTV.setOnClickListener(this);
      reminderTimeTV.setOnClickListener(this);
      repeatTV.setOnClickListener(this);
      notificationSwitch.setOnCheckedChangeListener(this);
      clearDate.setOnClickListener(this);
      notificationTV.setOnClickListener(this);
      dueDateTv.setOnClickListener(this);
   }

   private void setReminderInfo(long reminderInMillis) {
      taskReminder = Calendar.getInstance();
      taskReminder.setTimeInMillis(reminderInMillis);
      Calendar reminderClone = (Calendar) taskReminder.clone();
      reminderYear = taskReminder.get(Calendar.YEAR);
      reminderMonth = taskReminder.get(Calendar.MONTH);
      reminderDayOfMonth = taskReminder.get(Calendar.DAY_OF_MONTH);
      reminderHour = taskReminder.get(Calendar.HOUR_OF_DAY);
      reminderMinute = taskReminder.get(Calendar.MINUTE);
      if (dueDate != null) {
         if (dueDateYear == reminderYear && dueDateMonth == reminderMonth &&
               dueDateDayOfMonth == reminderDayOfMonth) {
            reminderDateTV.setText(getString(same_day));
         } else {
            reminderClone.add(Calendar.DATE, 1);
            if (reminderClone.get(Calendar.YEAR) == dueDateYear &&
                  reminderClone.get(Calendar.MONTH) == dueDateMonth &&
                  reminderClone.get(Calendar.DAY_OF_MONTH) == dueDateDayOfMonth) {
               reminderDateTV.setText(getString(day_before));
            } else {
               reminderClone = (Calendar) taskReminder.clone();
               reminderClone.add(Calendar.DATE, 7);
               if (reminderClone.get(Calendar.YEAR) == dueDateYear &&
                     reminderClone.get(Calendar.MONTH) == dueDateMonth &&
                     reminderClone.get(Calendar.DAY_OF_MONTH) == dueDateDayOfMonth) {
                  reminderDateTV.setText(getString(week_before));
               } else {
                  reminderDateTV.setText(DateHelper.millisToDateOnly(reminderInMillis));
               }
            }
         }
      } else {
         reminderDateTV.setText(DateHelper.millisToDateOnly(reminderInMillis));
      }

      if (reminderHour == Co.MORNING_ALARM_HOUR && reminderMinute == 0) {
         reminderTimeTV.setText(getString(morning));
      } else if (reminderHour == Co.AFTERNOON_ALARM_HOUR && reminderMinute == 0) {
         reminderTimeTV.setText(getString(afternoon));
      } else if (reminderHour == Co.EVENING_ALARM_HOUR && reminderMinute == 0) {
         reminderTimeTV.setText(getString(evening));
      } else {
         reminderTimeTV.setText(DateHelper.millsToTimeOnly(reminderInMillis));
      }
      nextReminderTV.setText(DateHelper.millisToFull(reminderInMillis));
      setRepeatMenu(taskReminder);
   }

   private void setRepeatMenu(Calendar taskReminder) {
      if (taskReminder != null) {
         repeatMenu = repeatPopupMenu.getMenu();
         String sameDayWeek;
         String sameDayMonth;
         sameDayWeek = getString(R.string.every) + " " +
               DateHelper.timeInMillsToDay(taskReminder.getTimeInMillis());
         repeatMenu.findItem(R.id.same_day_of_week).setTitle(sameDayWeek);
         int dayOfMonth = taskReminder.get(Calendar.DAY_OF_MONTH);
         if (dayOfMonth == taskReminder.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            sameDayMonth = getString(R.string.last_day_of_month) + " " +
                  getString(R.string.of_every_month);
         } else {
            sameDayMonth = (getString(R.string.on_day) + " " + taskReminder.get(Calendar.DAY_OF_MONTH) +
                  " " + getString(R.string.of_every_month));
         }
         repeatMenu.findItem(R.id.same_day_of_month).setTitle(sameDayMonth);
      }
      repeatTV.setText(repeatMenu.getItem(repeatMode).getTitle());
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
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 14);
            Calendar now = Calendar.getInstance();
            now.set(Calendar.DAY_OF_WEEK, c.get(Calendar.DAY_OF_WEEK));

            showToast(DateUtils.formatDateTime(this, now.getTimeInMillis(), 0));

            break;

         case R.id.save_task:

            //TASK EDITED
            if (getIntent().hasExtra(Co.LOCAL_TASK)) {
               Intent i = new Intent();
               if (notificationSwitch.isChecked()) {
                  if (taskReminder == null) {
                     taskToEdit.setReminderNoID(0);
                  } else {
                     taskToEdit.setRepeatMode(repeatMode);
                     if (taskToEdit.getReminderId() != 0) {
                        taskToEdit.setReminderNoID(taskReminder.getTimeInMillis());
                     } else {
                        taskToEdit.setReminder(taskReminder.getTimeInMillis());
                     }
                  }
               } else {
                  if (taskToEdit.getReminderId() != 0) {
                     taskToEdit.setReminderNoID(0);
                  } else {
                     taskToEdit.setReminder(0);
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

//               if (taskToEdit.getTitle().trim().equals(titleTV.getText().toString().trim())) {
//                  if ((taskToEdit.getDue() == 0 && dueDate == null) || (dueDate != null &&
//                        dueDate.getTimeInMillis() == taskToEdit.getDue())) {
//                     if ((taskToEdit.getDue() == 0 && dueDate == null) || (dueDate != null &&
//                           dueDate.getTimeInMillis() == taskToEdit.getDue())) {
//                        String titleTV = this.titleTV.getText().toString().trim();
//                        if (notesTv.getText().toString().isEmpty() && taskToEdit.getNotes() == null) {
//                        }
//                        if (taskToEdit.getNotes() != null && !notesTv.getText().toString().isEmpty()) {
//                           if (notesTv.getText().toString().trim().equals(taskToEdit.getNotes())) {
//                              i.putExtra(Co.NO_API_EDIT, true);
//                           }
//                        }
//                        if (taskToEdit.getNotes() != null && notesTv.getText().toString().isEmpty()) {
//                           taskToEdit.setNotes(null);
//                        }
//                        if (taskToEdit.getNotes() == null && !notesTv.getText().toString().isEmpty()) {
//                           taskToEdit.setNotes(notesTv.getText().toString());
//                        }
//                     }
//                  }
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
               }
               i.putExtra(Co.LOCAL_TASK, task);
               i.putExtra(Co.NEW_TASK, true);
               setResult(Activity.RESULT_OK, i);
               finish();
               break;
            }
      }
      return super.

            onOptionsItemSelected(item);

   }

   @Override
   public void onClick(View v) {
      switch (v.getId()) {

         //Clear date click
         case R.id.clearDate:
            dueDate = null;
            dueDateTv.setText(null);
            if (taskToEdit != null) {
               taskToEdit.setDue(0);
            }
            clearDate.setVisibility(View.GONE);
            break;

         //dueDate textview click
         case R.id.dueDateTv:
//            Dialog dialog = new Dialog(this);
//            dialog.setContentView(R.layout.reminder_screen);
//            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//            dialog.show();
            DatePickerDialog datePicker = new DatePickerDialog(this, this,
                  now.get(Calendar.YEAR),
                  now.get(Calendar.MONTH),
                  now.get(Calendar.DAY_OF_MONTH));
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePicker.show();
            break;

         case R.id.notificationTextView:
            notificationSwitch.setChecked(!notificationSwitch.isChecked());
            break;

////         //Morning taskReminder click
////         case R.id.layout_morning:
////            rbMorning.setChecked(true);
////            rbCustom.setChecked(false);
////            rbEvening.setChecked(false);
////            rbAfternoon.setChecked(false);
////            taskReminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
////            taskReminder.set(Calendar.MINUTE, 0);
////            notificationDetailsLayout.setVisibility(View.VISIBLE);
////            beforeNow = DateHelper.isBeforeByAtLeastDay(dueDate) ||
////                  (DateUtils.isToday(dueDate.getTimeInMillis()) && taskReminder.before(Calendar.getInstance()));
////            setRepeatSpinnerAdapter(taskReminder.getTimeInMillis(), beforeNow);
////            repeatSpinner.setSelection(beforeNow ? repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY : repeatMode);
////            if (beforeNow) {
////               notificationTV.setText(DateHelper.millsToTimeOnly(taskReminder.getTimeInMillis()));
////            } else {
////               notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
////            }
////            whenDialog.dismiss();
////            break;
////
////         //Afternoon taskReminder click
////         case R.id.layout_afternoon:
////            rbAfternoon.setChecked(true);
////            rbMorning.setChecked(false);
////            rbCustom.setChecked(false);
////            rbEvening.setChecked(false);
////            taskReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
////            taskReminder.set(Calendar.MINUTE, 0);
////            notificationDetailsLayout.setVisibility(View.VISIBLE);
////            beforeNow = DateHelper.isBeforeByAtLeastDay(dueDate) ||
////                  (DateUtils.isToday(dueDate.getTimeInMillis()) && taskReminder.before(Calendar.getInstance()));
////            setRepeatSpinnerAdapter(taskReminder.getTimeInMillis(), beforeNow);
////            repeatSpinner.setSelection(beforeNow ? repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY : repeatMode);
////            if (beforeNow) {
////               notificationTV.setText(DateHelper.millsToTimeOnly(taskReminder.getTimeInMillis()));
////            } else {
////               notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
////            }
////            whenDialog.dismiss();
////            break;
////
////         //Evening click
////         case R.id.layout_evening:
////            rbEvening.setChecked(true);
////            rbMorning.setChecked(false);
////            rbCustom.setChecked(false);
////            rbAfternoon.setChecked(false);
////            taskReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
////            taskReminder.set(Calendar.MINUTE, 0);
////            notificationDetailsLayout.setVisibility(View.VISIBLE);
////            beforeNow = DateHelper.isBeforeByAtLeastDay(dueDate) ||
////                  (DateUtils.isToday(dueDate.getTimeInMillis()) && taskReminder.before(Calendar.getInstance()));
////            setRepeatSpinnerAdapter(taskReminder.getTimeInMillis(), beforeNow);
////            repeatSpinner.setSelection(beforeNow ? repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY : repeatMode);
////            if (beforeNow) {
////               notificationTV.setText(DateHelper.millsToTimeOnly(taskReminder.getTimeInMillis()));
////            } else {
////               notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
////            }
////            whenDialog.dismiss();
////            break;
//
//         //Custom taskReminder click
//         case R.id.layout_custom_time:
//            rbCustom.setChecked(true);
//            rbMorning.setChecked(false);
//            rbAfternoon.setChecked(false);
//            rbEvening.setChecked(false);
//            showTimePicker();
//            break;

         //Reminder textview click
//         case R.id.notificationTextView:
//            if (dueDate != null) {
//               showReminderDateDialog();
//            } else {
//               showReminderDatePicker();
//            }
//            break;

//         case R.id.layout_same_day:
//            if (taskReminder == null) {
//               taskReminder = Calendar.getInstance();
//            }
//            dateDialog.dismiss();
//            showReminderDialog();
//            break;
//
//         case R.id.layout_day_before:
//            if (taskReminder == null) {
//               taskReminder = Calendar.getInstance();
//            }
//            taskReminder.add(Calendar.DATE, -1);
//            dateDialog.dismiss();
//            showReminderDialog();
//            break;
//
//         case R.id.layout_week_before:
//            if (taskReminder == null) {
//               taskReminder = Calendar.getInstance();
//            }
//            taskReminder.add(Calendar.DATE, -7);
//            dateDialog.dismiss();
//            showReminderDialog();
//            break;
//
//         case R.id.layout_custom_date:
//            showReminderDatePicker();
//            dateDialog.dismiss();
//            break;
         case R.id.reminder_date_tv:
            if (dueDate != null) {
               reminderDateMenu.show();
            } else {
               showReminderDatePicker();
            }
            break;

         case R.id.reminder_time_tv:
            reminderTimeMenu.show();
            break;

         case R.id.repeat_tv:
            repeatPopupMenu.show();
            break;
      }

   }

   public void setReminderTimeMenuItemEnable(Calendar dueDate) {
      if (dueDate.before(now)) {

      }
   }

   private void showReminderDatePicker() {
      reminderDatePicker = new DatePickerDialog(this, reminderDatePickerListener,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH));
      reminderDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
      reminderDatePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            if (taskReminder == null) {
               notificationSwitch.setChecked(false);
            }
         }
      });
      reminderDatePicker.show();
   }



   private void showTimePicker() {
      TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
         @Override
         public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            taskReminder.set(Calendar.HOUR_OF_DAY, hourOfDay);
            taskReminder.set(Calendar.MINUTE, minute);
            taskReminder.set(Calendar.SECOND, 0);
            long reminderInMillis = taskReminder.getTimeInMillis();
            boolean isReminderBeforeNow = taskReminder.before(now);
            nextReminderTV.setText(DateHelper.millisToFull(reminderInMillis));
            reminderTimeTV.setText(R.string.custom);
            notificationTV.setText(isReminderBeforeNow ?
                  DateHelper.millsToTimeOnly(taskReminder.getTimeInMillis()) :
                  DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            setRepeatMenu(taskReminder);
            if (isReminderBeforeNow) {
               repeatMenu.findItem(R.id.one_time).setEnabled(false);
               if (repeatMode == Co.REMINDER_ONE_TIME) {
                  repeatMode = Co.REMINDER_DAILY;
                  repeatTV.setText(getString(R.string.daily_repeat_mode));
               } else {
                  repeatTV.setText(repeatMenu.getItem(repeatMode).getTitle());
               }
            } else {
               repeatMenu.findItem(R.id.one_time).setEnabled(true);
               repeatTV.setText(repeatMenu.getItem(repeatMode).getTitle());
            }
            timeSet = true;
         }
      }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
      timePicker.show();
   }

   private boolean isCustomReminderTimeSet(Calendar reminder) {
      reminderHour = reminder.get(Calendar.HOUR_OF_DAY);
      reminderMinute = reminder.get(Calendar.MINUTE);
      return !(reminderHour == Co.MORNING_ALARM_HOUR && reminderMinute == 0) &&
            !(reminderHour == Co.AFTERNOON_ALARM_HOUR && reminderMinute == 0) &&
            !(reminderHour == Co.EVENING_ALARM_HOUR && reminderMinute == 0);
   }


   @Override
   public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
      dueDate = Calendar.getInstance();
      dueDate.set(year, month, dayOfMonth);
      dueDate.set(Calendar.HOUR_OF_DAY, 0);
      dueDate.set(Calendar.MINUTE, 0);
      dueDate.set(Calendar.SECOND, 0);
      dueDate.set(Calendar.MILLISECOND, 0);
      clearDate.setVisibility(View.VISIBLE);
      dueDateTv.setText(DateHelper.millisToDateOnly(dueDate.getTimeInMillis()));
      if (reminderTimeTV.getText().toString().isEmpty()) {
         reminderTimeMenu.show();
      }

   }

   DatePickerDialog.OnDateSetListener reminderDatePickerListener = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
         if (taskReminder == null) {
            taskReminder = Calendar.getInstance();
            taskReminder.clear();
         }
         taskReminder.set(Calendar.YEAR, year);
         taskReminder.set(Calendar.MONTH, month);
         taskReminder.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         if (taskReminder.isSet(Calendar.HOUR_OF_DAY)){
            showToast(String.valueOf(taskReminder.get(Calendar.HOUR_OF_DAY)));
         } else {
            showToast("Hour not set");
         }
         notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
         if (dueDate == null) {
            reminderDateTV.setText(DateHelper.millisToDateOnly(taskReminder.getTimeInMillis()));
         } else {
            reminderDateTV.setText(getString(R.string.custom));
         }
         nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
         reminderDatePicker.dismiss();
         boolean isBeforeToday = Calendar.getInstance().before(taskReminder);
         reminderTimeMenu.show();
         setRepeatMenu(taskReminder);
         repeatMode = 0;
         repeatTV.setText(repeatMenu.findItem(R.id.one_time).getTitle());
      }
   };


   public void showToast(String msg) {
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
   }

   private void setEditedReminderToday() {
      editedReminder.set(Calendar.YEAR, now.get(Calendar.YEAR));
      editedReminder.set(Calendar.MONTH, now.get(Calendar.MONTH));
      editedReminder.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
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
//            notificationTV.setText(DateHelper.millisToFull(editedReminder.getTimeInMillis()));
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
//            notificationTV.setText(DateHelper.millsToTimeOnly(editedReminder.getTimeInMillis()));
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
//            notificationTV.setText(DateHelper.millsToTimeOnly(editedReminder.getTimeInMillis()));
//            break;
//
//         case Co.REMINDER_SAME_DAY_OF_WEEK:
//            repeatMode = Co.REMINDER_SAME_DAY_OF_WEEK;
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
//            notificationTV.setText(DateHelper.millsToTimeOnly(editedReminder.getTimeInMillis()));
//            break;
//
//         case Co.REMINDER_SAME_DAY_OF_MONTH:
//            repeatMode = Co.REMINDER_SAME_DAY_OF_MONTH;
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
//            notificationTV.setText(DateHelper.millsToTimeOnly(editedReminder.getTimeInMillis()));
//            break;
//
//
//      }
//   }

   @Override
   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      if (isChecked) {
         notificationDetailsLayout.setVisibility(View.VISIBLE);
         if (taskReminder == null) {
            timeSet = false;
            if (dueDate != null) {
               Calendar today = Calendar.getInstance();
               Calendar dueDateCopy = (Calendar) dueDate.clone();
               dueDateCopy.add(Calendar.DATE, -7);
               if (DateUtils.isToday(dueDate.getTimeInMillis())) {
                  reminderDateMenu.getMenu().getItem(1).setEnabled(false);
                  reminderDateMenu.getMenu().getItem(2).setEnabled(false);
               } if (dueDateCopy.before(today)) {
                  reminderDateMenu.getMenu().getItem(1).setEnabled(true);
                  reminderDateMenu.getMenu().getItem(2).setEnabled(false);
               } else {
                  reminderDateMenu.getMenu().getItem(1).setEnabled(true);
                  reminderDateMenu.getMenu().getItem(2).setEnabled(true);
               }
               reminderDateMenu.show();
            } else {
               showReminderDatePicker();
            }
         } else {
            notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
         }
         repeatMode = 0;
         repeatTV.setText(R.string.one_time_repeat_mode);

//         ValueAnimator va = ValueAnimator.ofInt(0, 500);
//         va.setDuration(300);
//         va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            public void onAnimationUpdate(ValueAnimator animation) {
//               notificationDetailsLayout.getLayoutParams().height = (Integer) animation.getAnimatedValue();
//               notificationDetailsLayout.requestLayout();
//            }
//         });
//         va.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//               super.onAnimationStart(animation);
//               notificationDetailsLayout.setVisibility(View.VISIBLE);
//            }
//         });
//         va.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//               super.onAnimationEnd(animation);
//
//            }
//         });
//
//         va.start();
      } else {
         taskReminder = null;
         notificationTV.setText(null);
         reminderDateTV.setText(null);
         reminderTimeTV.setText(null);
         repeatTV.setText(null);
         notificationDetailsLayout.setVisibility(View.GONE);
//         ValueAnimator va = ValueAnimator.ofInt(500, 0);
//         va.setDuration(300);
//         va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            public void onAnimationUpdate(ValueAnimator animation) {
//               notificationDetailsLayout.getLayoutParams().height = (Integer) animation.getAnimatedValue();
//               notificationDetailsLayout.requestLayout();
//            }
//         });
//         va.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//               notificationDetailsLayout.setVisibility(View.GONE);
//            }
//         });
//         va.start();
      }

   }


   @Override
   public boolean onMenuItemClick(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.morning:
            taskReminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
            taskReminder.set(Calendar.MINUTE, 0);
            notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            reminderTimeTV.setText(getString(morning));
            nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));

            break;

         case R.id.afternoon:
            taskReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
            taskReminder.set(Calendar.MINUTE, 0);
            notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            reminderTimeTV.setText(getString(afternoon));
            nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            break;

         case R.id.evening:
            taskReminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
            taskReminder.set(Calendar.MINUTE, 0);
            notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            reminderTimeTV.setText(getString(evening));
            nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            break;

         case R.id.custom_time:
            showTimePicker();
            break;

         case R.id.same_day:
            if (taskReminder == null) {
               taskReminder = Calendar.getInstance();
            }

            taskReminder.set(Calendar.YEAR, dueDate.get(Calendar.YEAR));
            taskReminder.set(Calendar.MONTH, dueDate.get(Calendar.MONTH));
            taskReminder.set(Calendar.DAY_OF_MONTH, dueDate.get(Calendar.DAY_OF_MONTH));
            notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            reminderDateTV.setText(getString(same_day));
            setRepeatMenu(taskReminder);
            if (reminderTimeTV.getText().toString().isEmpty()) {
               reminderTimeMenu.show();
            }

            break;

         case R.id.day_before:
            if (taskReminder == null) {
               taskReminder = Calendar.getInstance();
            }
            taskReminder.set(Calendar.YEAR, dueDate.get(Calendar.YEAR));
            taskReminder.set(Calendar.MONTH, dueDate.get(Calendar.MONTH));
            taskReminder.set(Calendar.DAY_OF_MONTH, dueDate.get(Calendar.DAY_OF_MONTH));
            taskReminder.add(Calendar.DATE, -1);
            notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            reminderDateTV.setText(getString(day_before));
            setRepeatMenu(taskReminder);
            if (reminderTimeTV.getText().toString().isEmpty()) {
               reminderTimeMenu.show();
            }
            break;

         case R.id.week_before:
            if (taskReminder == null) {
               taskReminder = Calendar.getInstance();
            }
            taskReminder.set(Calendar.YEAR, dueDate.get(Calendar.YEAR));
            taskReminder.set(Calendar.MONTH, dueDate.get(Calendar.MONTH));
            taskReminder.set(Calendar.DAY_OF_MONTH, dueDate.get(Calendar.DAY_OF_MONTH));
            taskReminder.add(Calendar.DATE, -7);
            notificationTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            nextReminderTV.setText(DateHelper.millisToFull(taskReminder.getTimeInMillis()));
            reminderDateTV.setText(getString(week_before));
            setRepeatMenu(taskReminder);
            if (reminderTimeTV.getText().toString().isEmpty()) {
               reminderTimeMenu.show();
            }
            break;

         case R.id.custom_date:
            showReminderDatePicker();
            break;

         case R.id.one_time:
            repeatMode = Co.REMINDER_ONE_TIME;
            if (repeatMenu != null) {
               repeatTV.setText(repeatMenu.findItem(R.id.one_time).getTitle());
            }
            break;

         case R.id.daily:
            repeatMode = Co.REMINDER_DAILY;
            if (repeatMenu != null) {
               repeatTV.setText(repeatMenu.findItem(R.id.daily).getTitle());
            }
            break;

         case R.id.weekdays:
            repeatMode = Co.REMINDER_DAILY_WEEKDAYS;
            if (repeatMenu != null) {
               repeatTV.setText(repeatMenu.findItem(R.id.weekdays).getTitle());
            }
            break;

         case R.id.same_day_of_week:
            repeatMode = Co.REMINDER_SAME_DAY_OF_WEEK;
            if (repeatMenu != null) {
               repeatTV.setText(repeatMenu.findItem(R.id.same_day_of_week).getTitle());
            }
            break;

         case R.id.same_day_of_month:
            repeatMode = Co.REMINDER_SAME_DAY_OF_MONTH;
            if (repeatMenu != null) {
               repeatTV.setText(repeatMenu.findItem(R.id.same_day_of_month).getTitle());
            }
            break;

         case R.id.custom_repeat:
            repeatMode = Co.REMINDER_CUSTOM_REPEAT;
            if (repeatMenu != null) {
               repeatTV.setText(repeatMenu.findItem(R.id.custom_repeat).getTitle());
            }
            break;

      }
      return true;
   }
}

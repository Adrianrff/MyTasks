package com.adrapps.mytasks.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.DateHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.adrapps.mytasks.R.string.date;

public class NewTaskOrEditActivity extends AppCompatActivity
      implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
      AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

   EditText titleTV, notesTv;
   private LinearLayout notificationDetailsLayout;
   private RadioButton rbMorning, rbAfternoon, rbEvening, rbCustom;
   private LocalTask taskToEdit;
   private int position, repeatMode;
   ImageView clearDate;
   private AlertDialog whenDialog, dateDialog;
   private TextView notificationTV, dueDateTv, nextReminderTV;
   private Spinner repeatSpinner;
   SwitchCompat notificationSwitch;
   final private Calendar today = Calendar.getInstance();
   private Calendar originalReminder, dateSet, editedReminder;
   private boolean isFirstLaunch, customReminderDateSet;
   private DatePickerDialog reminderDatePicker;


   @Override
   protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
   }

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
      //TODO set new reminder approach
      originalReminder = null;
      repeatMode = 0;
      titleTV = (EditText) findViewById(R.id.task_title_edit_text);
      dueDateTv = (TextView) findViewById(R.id.dueDateTv);
      notesTv = (EditText) findViewById(R.id.task_notes_edit_text);
      nextReminderTV = (TextView) findViewById(R.id.next_reminder_label);
      notificationDetailsLayout = (LinearLayout) findViewById(R.id.notification_layout);
      notificationTV = (TextView) findViewById(R.id.notificationTextView);
      clearDate = (ImageView) findViewById(R.id.clearDate);
//      clearReminder = (ImageView) findViewById(R.id.clearReminder);
      repeatSpinner = (Spinner) findViewById(R.id.reminder_repeat_spinner);
      notificationSwitch = (SwitchCompat) findViewById(R.id.notification_switch);
      notificationSwitch.setOnCheckedChangeListener(this);
      clearDate.setOnClickListener(this);
//      clearReminder.setOnClickListener(this);
      notificationTV.setOnClickListener(this);
      dueDateTv.setOnClickListener(this);
      repeatSpinner.setOnItemSelectedListener(this);

      if (getIntent().hasExtra(Co.LOCAL_TASK)) {
         isFirstLaunch = true;
         getSupportActionBar().setTitle(R.string.task_edit_title);
         taskToEdit = (LocalTask) getIntent().getExtras().getSerializable(Co.LOCAL_TASK);
         if (taskToEdit != null) {
            titleTV.setText(taskToEdit.getTitle());
            notesTv.setText(taskToEdit.getNotes());
            if (taskToEdit.getDue() != 0) {
               dueDateTv.setText(DateHelper.timeInMillsToString(taskToEdit.getDue()));
               dateSet = Calendar.getInstance();
               dateSet.setTimeInMillis(taskToEdit.getDue());
               clearDate.setVisibility(View.VISIBLE);
            } else {
               dateSet = null;
               clearDate.setVisibility(View.GONE);
            }

            position = getIntent().getIntExtra(Co.ADAPTER_POSITION, -1);
            if (taskToEdit.getReminder() != 0) {
               repeatMode = taskToEdit.getRepeatMode();
               originalReminder = Calendar.getInstance();
               originalReminder.setTimeInMillis(taskToEdit.getReminder());
               editedReminder = Calendar.getInstance();
               editedReminder.setTimeInMillis(taskToEdit.getReminder());
               notificationSwitch.setChecked(true);
               if (repeatMode == 0 && originalReminder.before(today)) {
                  originalReminder = null;
                  editedReminder = null;
                  return;
               }
               notificationTV.setText(DateHelper.millsToFull(taskToEdit.getReminder()));
               notificationDetailsLayout.setVisibility(View.VISIBLE);
               setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
               repeatSpinner.setSelection(repeatMode);
               nextReminderTV.setText(DateHelper.millsToFull(taskToEdit.getReminder()));
            } else {
               notificationSwitch.setChecked(false);
//               notificationDetailsLayout.setVisibility(View.GONE);
//               clearReminder.setVisibility(View.GONE);
            }
         }

      } else {
         toolbar.setTitle(getString(R.string.new_task_title));
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
      }
   }


   private void setRepeatSpinnerAdapter(long reminderInMills, final boolean disableOneTime) {
      List<String> categories = new ArrayList<>();
      categories.add(getString(R.string.one_time_repeat_mode));
      categories.add(getString(R.string.daily_repeat_mode));
      categories.add(getString(R.string.weekdays));
      if (dateSet == null) {
         categories.add(getString(R.string.every) + " " +
               DateHelper.timeInMillsToDay(reminderInMills));
      } else {
         categories.add(getString(R.string.every) + " " +
               DateHelper.timeInMillsToDay(dateSet.getTimeInMillis()));
      }
      if (dateSet == null) {
         categories.add(getString(R.string.on_day) + " " + DateHelper.timeInMillsToDayOfMonth(reminderInMills) +
               " " + getString(R.string.of_every_month));
      } else {
         int dayOfMonth = dateSet.get(Calendar.DAY_OF_MONTH);
         if (dayOfMonth == dateSet.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            categories.add(getString(R.string.last_day_of_month) + " " + getString(R.string.of_every_month));
         } else {
            categories.add(getString(R.string.on_day) + " " + dateSet.get(Calendar.DAY_OF_MONTH) + " " + getString(R.string.of_every_month));
         }
      }
      ArrayAdapter<String> repeatAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
               @Override
               public boolean isEnabled(int position) {
                  return !(position == 0 && disableOneTime);
               }

               @Override
               public View getDropDownView(int position, View convertView,
                                           @NonNull ViewGroup parent) {
                  View mView = super.getDropDownView(position, convertView, parent);
                  TextView mTextView = (TextView) mView;
                  if (position == 0 && disableOneTime) {
                     mTextView.setTextColor(Color.GRAY);
                  } else {
                     mTextView.setTextColor(Color.BLACK);
                  }
                  return mView;
               }
            };
      repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      repeatSpinner.setAdapter(repeatAdapter);
   }

   private void showReminderDialog() {
      AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
      LayoutInflater inflater = this.getLayoutInflater();
      final ViewGroup nullParent = null;
      View dialogView = inflater.inflate(R.layout.reminder_when_dialog, nullParent);
      dialogBuilder.setView(dialogView);
      LinearLayout morningLayout = (LinearLayout) dialogView.findViewById(R.id.layout_morning);
      LinearLayout afternoonLayout = (LinearLayout) dialogView.findViewById(R.id.layout_afternoon);
      LinearLayout eveningLayout = (LinearLayout) dialogView.findViewById(R.id.layout_evening);
      LinearLayout customLayout = (LinearLayout) dialogView.findViewById(R.id.layout_custom_time);
      rbMorning = (RadioButton) dialogView.findViewById(R.id.rb_morning);
      rbAfternoon = (RadioButton) dialogView.findViewById(R.id.rb_afternoon);
      rbEvening = (RadioButton) dialogView.findViewById(R.id.rb_evening);
      rbCustom = (RadioButton) dialogView.findViewById(R.id.rb_custom);
      morningLayout.setOnClickListener(this);
      afternoonLayout.setOnClickListener(this);
      eveningLayout.setOnClickListener(this);
      customLayout.setOnClickListener(this);
      whenDialog = dialogBuilder.create();
      whenDialog.setTitle(getString(R.string.notification_dialog_title));
      whenDialog.show();


   }

   private void showReminderDateDialog() {
      AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
      LayoutInflater inflater = this.getLayoutInflater();
      final ViewGroup nullParent = null;
      View dialogView = inflater.inflate(R.layout.reminder_date_dialog, nullParent);
      dialogBuilder.setView(dialogView);
      LinearLayout sameDayLayout = (LinearLayout) dialogView.findViewById(R.id.layout_same_day);
      LinearLayout dayBeforeLayout = (LinearLayout) dialogView.findViewById(R.id.layout_day_before);
      LinearLayout weekBeforeLayout = (LinearLayout) dialogView.findViewById(R.id.layout_week_before);
      LinearLayout customDateLayout = (LinearLayout) dialogView.findViewById(R.id.layout_custom_date);
      sameDayLayout.setOnClickListener(this);
      dayBeforeLayout.setOnClickListener(this);
      weekBeforeLayout.setOnClickListener(this);
      customDateLayout.setOnClickListener(this);
      dateDialog = dialogBuilder.create();
      dateDialog.setTitle(getString(date));
      dateDialog.show();
      dateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
         }
      });

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
               if (taskToEdit.getReminderId() != 0) {
                  if (editedReminder == null) {
                     taskToEdit.setReminderNoID(0);
                  } else {
                     taskToEdit.setReminderNoID(editedReminder.getTimeInMillis());
                     taskToEdit.setRepeatMode(repeatMode);
                  }
               } else {
                  if (editedReminder != null) {
                     taskToEdit.setReminder(editedReminder.getTimeInMillis());
                     taskToEdit.setRepeatMode(repeatMode);
                  }
               }
               if (taskToEdit.getTitle().trim().equals(titleTV.getText().toString().trim()) &&
                     (taskToEdit.getDue() == 0 && dateSet == null) || (dateSet != null &&
                     dateSet.getTimeInMillis() == taskToEdit.getDue())) {
                  if (notesTv.getText().toString().isEmpty()) {
                     if (taskToEdit.getNotes() == null) {
                        i.putExtra(Co.NO_API_EDIT, true);
                     }
                  } else if (taskToEdit.getNotes() == null) {
                     if (notesTv.getText().toString().isEmpty()) {
                        i.putExtra(Co.NO_API_EDIT, true);
                     }
                  } else if (taskToEdit.getNotes() != null && notesTv.getText().toString().isEmpty()) {
                     if (notesTv.getText().toString().trim().equals(taskToEdit.getNotes())) {
                        i.putExtra(Co.NO_API_EDIT, true);
                     }
                  }

               } else {
                  taskToEdit.setTitle(titleTV.getText().toString());
                  if (notesTv.getText().toString().trim().length() != 0)
                     taskToEdit.setNotes(notesTv.getText().toString());
                  if (dateSet != null && dateSet.getTimeInMillis() != taskToEdit.getDue()) {
                     taskToEdit.setDue(dateSet.getTimeInMillis());
                  }
                  if (dateSet == null && taskToEdit.getDue() != 0) {
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
               if (dateSet != null) {
                  task.setDue(dateSet.getTimeInMillis());
               }
               if (notesTv.getText().toString().trim().length() != 0)
                  task.setNotes(notesTv.getText().toString());
               if (editedReminder != null) {
                  task.setReminder(editedReminder.getTimeInMillis());
                  task.setRepeatMode(repeatMode);
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
      boolean beforeNow = false;
      switch (v.getId()) {

         //Clear date click
         case R.id.clearDate:
            dateSet = null;
            dueDateTv.setText(null);
            clearDate.setVisibility(View.GONE);
            if (originalReminder != null) {
               originalReminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
               originalReminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
               originalReminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
               notificationTV.setText(DateHelper.millsToTimeOnly(originalReminder.getTimeInMillis()));
               setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
               repeatSpinner.setSelection(Co.REMINDER_DAILY);
            }
            break;

         //Clear originalReminder click
//         case R.id.clearReminder:
//            originalReminder = null;
//            editedReminder = null;
//            repeatMode = 0;
//            notificationTV.setText(null);
//            clearReminder.setVisibility(View.GONE);
//            notificationDetailsLayout.setVisibility(View.INVISIBLE);
//            break;

         //Date picker click
         case R.id.dueDateTv:
            DatePickerDialog datePicker = new DatePickerDialog(this, this,
                  today.get(Calendar.YEAR),
                  today.get(Calendar.MONTH),
                  today.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
            break;

         //Morning originalReminder click
         case R.id.layout_morning:
            rbMorning.setChecked(true);
            rbCustom.setChecked(false);
            rbEvening.setChecked(false);
            rbAfternoon.setChecked(false);
            originalReminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
            originalReminder.set(Calendar.MINUTE, 0);
//            clearReminder.setVisibility(View.VISIBLE);
            notificationDetailsLayout.setVisibility(View.VISIBLE);
            beforeNow = DateHelper.isBeforeByAtLeastDay(dateSet) ||
                  (DateUtils.isToday(dateSet.getTimeInMillis()) && originalReminder.before(Calendar.getInstance()));
            setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), beforeNow);
            repeatSpinner.setSelection(beforeNow ? repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY : repeatMode);
            if (beforeNow) {
               notificationTV.setText(DateHelper.millsToTimeOnly(originalReminder.getTimeInMillis()));
            } else {
               notificationTV.setText(DateHelper.millsToFull(originalReminder.getTimeInMillis()));
            }
            whenDialog.dismiss();
            break;

         //Afternoon originalReminder click
         case R.id.layout_afternoon:
            rbAfternoon.setChecked(true);
            rbMorning.setChecked(false);
            rbCustom.setChecked(false);
            rbEvening.setChecked(false);
            originalReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
            originalReminder.set(Calendar.MINUTE, 0);
//            clearReminder.setVisibility(View.VISIBLE);
            notificationDetailsLayout.setVisibility(View.VISIBLE);
            beforeNow = DateHelper.isBeforeByAtLeastDay(dateSet) ||
                  (DateUtils.isToday(dateSet.getTimeInMillis()) && originalReminder.before(Calendar.getInstance()));
            setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), beforeNow);
            repeatSpinner.setSelection(beforeNow ? repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY : repeatMode);
            if (beforeNow) {
               notificationTV.setText(DateHelper.millsToTimeOnly(originalReminder.getTimeInMillis()));
            } else {
               notificationTV.setText(DateHelper.millsToFull(originalReminder.getTimeInMillis()));
            }
            whenDialog.dismiss();
            break;

         //Evening click
         case R.id.layout_evening:
            rbEvening.setChecked(true);
            rbMorning.setChecked(false);
            rbCustom.setChecked(false);
            rbAfternoon.setChecked(false);
            originalReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
            originalReminder.set(Calendar.MINUTE, 0);
//            clearReminder.setVisibility(View.VISIBLE);
            notificationDetailsLayout.setVisibility(View.VISIBLE);
            beforeNow = DateHelper.isBeforeByAtLeastDay(dateSet) ||
                  (DateUtils.isToday(dateSet.getTimeInMillis()) && originalReminder.before(Calendar.getInstance()));
            setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), beforeNow);
            repeatSpinner.setSelection(beforeNow ? repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY : repeatMode);
            if (beforeNow) {
               notificationTV.setText(DateHelper.millsToTimeOnly(originalReminder.getTimeInMillis()));
            } else {
               notificationTV.setText(DateHelper.millsToFull(originalReminder.getTimeInMillis()));
            }
            whenDialog.dismiss();
            break;

         //Custom originalReminder click
         case R.id.layout_custom_time:
            rbCustom.setChecked(true);
            rbMorning.setChecked(false);
            rbAfternoon.setChecked(false);
            rbEvening.setChecked(false);
            showTimePicker();
//            clearReminder.setVisibility(View.VISIBLE);
            break;

         //Reminder textview click
         case R.id.notificationTextView:
            if (dateSet != null) {
               showReminderDateDialog();
            } else {
               showReminderDatePicker();
            }
            break;

         case R.id.layout_same_day:
            if (originalReminder == null) {
               originalReminder = Calendar.getInstance();
            }
            setOriginalReminderDateSet();
            dateDialog.dismiss();
            showReminderDialog();
            break;

         case R.id.layout_day_before:
            if (originalReminder == null) {
               originalReminder = Calendar.getInstance();
            }
            setOriginalReminderDateSet();
            originalReminder.add(Calendar.DATE, -1);
            dateDialog.dismiss();
            showReminderDialog();
            break;

         case R.id.layout_week_before:
            if (originalReminder == null) {
               originalReminder = Calendar.getInstance();
            }
            setOriginalReminderDateSet();
            originalReminder.add(Calendar.DATE, -7);
            dateDialog.dismiss();
            showReminderDialog();
            break;

         case R.id.layout_custom_date:
            showReminderDatePicker();
            dateDialog.dismiss();
            break;
      }
   }


   DatePickerDialog.OnDateSetListener reminderDatePickerListener = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
         if (originalReminder == null) {
            originalReminder = Calendar.getInstance();
         }
         originalReminder.set(Calendar.YEAR, year);
         originalReminder.set(Calendar.MONTH, month);
         originalReminder.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         customReminderDateSet = true;
         showTimePicker();
         reminderDatePicker.dismiss();
      }
   };

   private void showReminderDatePicker() {
      reminderDatePicker = new DatePickerDialog(this, reminderDatePickerListener,
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH));
      reminderDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
      reminderDatePicker.show();
   }

   private void showTimePicker() {
      TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
         @Override
         public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            originalReminder.set(Calendar.HOUR_OF_DAY, hourOfDay);
            originalReminder.set(Calendar.MINUTE, minute);
            originalReminder.set(Calendar.SECOND, 0);
            notificationDetailsLayout.setVisibility(View.VISIBLE);
//            clearReminder.setVisibility(View.VISIBLE);
            boolean isBeforeToday = originalReminder.before(today);
            setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), isBeforeToday);
            notificationTV.setText(isBeforeToday ?
                  DateHelper.millsToTimeOnly(originalReminder.getTimeInMillis()) :
                  DateHelper.millsToFull(originalReminder.getTimeInMillis()));
            repeatSpinner.setSelection(isBeforeToday ? repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY : repeatMode);
         }
      }, today.get(Calendar.HOUR_OF_DAY), today.get(Calendar.MINUTE), false);
      timePicker.show();
   }


   @Override
   public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
      dateSet = Calendar.getInstance();
      dateSet.set(year, month, dayOfMonth);
      dateSet.set(Calendar.HOUR_OF_DAY, 0);
      dateSet.set(Calendar.MINUTE, 0);
      clearDate.setVisibility(View.VISIBLE);
      dueDateTv.setText(DateHelper.timeInMillsToString(dateSet.getTimeInMillis()));
   }

   public void showToast(String msg) {
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
   }

   @Override
   public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      editedReminder = (Calendar) originalReminder.clone();
      int dayOfWeek;
      switch (position) {

         case Co.REMINDER_ONE_TIME:
            repeatMode = Co.REMINDER_ONE_TIME;
            if (isFirstLaunch && !customReminderDateSet) {
               setOriginalReminderFirstLaunch();
               break;
            }
            if (dateSet == null && !customReminderDateSet) {
               setEditedReminderToday();
            }
            nextReminderTV.setText(DateHelper.millsToFull(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.millsToFull(editedReminder.getTimeInMillis()));
            break;

         case Co.REMINDER_DAILY:
            repeatMode = Co.REMINDER_DAILY;
            if (isFirstLaunch && !customReminderDateSet) {
               setOriginalReminderFirstLaunch();
               break;
            }
            setEditedReminderToday();
            if (editedReminder.before(today)) {
               editedReminder.add(Calendar.DATE, 1);
            }
            nextReminderTV.setText(DateHelper.millsToFull(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.millsToTimeOnly(editedReminder.getTimeInMillis()));
            break;

         case Co.REMINDER_DAILY_WEEKDAYS:
            repeatMode = Co.REMINDER_DAILY_WEEKDAYS;
            if (isFirstLaunch && !customReminderDateSet) {
               setOriginalReminderFirstLaunch();
               break;
            }
            setEditedReminderToday();
            if (editedReminder.before(today)) {
               editedReminder.add(Calendar.DATE, 1);
            }
            if (!DateHelper.isWeekday(editedReminder)) {
               if (editedReminder.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                  editedReminder.add(Calendar.DATE, 2);
               } else {
                  editedReminder.add(Calendar.DATE, 1);
               }
            }
            nextReminderTV.setText(DateHelper.millsToFull(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.millsToTimeOnly(editedReminder.getTimeInMillis()));
            break;

         case Co.REMINDER_SAME_DAY_OF_WEEK:
            repeatMode = Co.REMINDER_SAME_DAY_OF_WEEK;
            if (isFirstLaunch && !customReminderDateSet) {
               setOriginalReminderFirstLaunch();
               break;
            }
            if (customReminderDateSet) {
               dayOfWeek = editedReminder.get(Calendar.DAY_OF_WEEK);
            } else {
               if (dateSet != null) {
                  dayOfWeek = dateSet.get(Calendar.DAY_OF_WEEK);
               } else {
                  dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
               }
            }
            setEditedReminderToday();
            if (dayOfWeek != today.get(Calendar.DAY_OF_WEEK)) {
               while (editedReminder.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                  editedReminder.add(Calendar.DATE, 1);
               }
            } else if (editedReminder.before(today)) {
               editedReminder.add(Calendar.DATE, 7);
            }

            nextReminderTV.setText(DateHelper.millsToFull(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.millsToTimeOnly(editedReminder.getTimeInMillis()));
            break;

         case Co.REMINDER_SAME_DAY_OF_MONTH:
            repeatMode = Co.REMINDER_SAME_DAY_OF_MONTH;
            if (isFirstLaunch && !customReminderDateSet) {
               setOriginalReminderFirstLaunch();
               break;
            }
            int dayOfMonth;
            if (customReminderDateSet) {
               dayOfMonth = editedReminder.get(Calendar.DAY_OF_MONTH);
            } else {
               if (dateSet == null) {
                  dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
               } else {
                  dayOfMonth = dateSet.get(Calendar.DAY_OF_MONTH);
               }
            }
            setEditedReminderToday();
            if (dayOfMonth != today.get(Calendar.DAY_OF_MONTH)) {
               editedReminder.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
            if (editedReminder.before(today)) {
               editedReminder.add(Calendar.MONTH, 1);
            }
            nextReminderTV.setText(DateHelper.millsToFull(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.millsToTimeOnly(editedReminder.getTimeInMillis()));
            break;


      }
   }

   private void setOriginalReminderFirstLaunch() {
      nextReminderTV.setText(DateHelper.millsToFull(taskToEdit.getReminder()));
      notificationTV.setText(DateHelper.millsToFull(taskToEdit.getReminder()));
      isFirstLaunch = false;
   }

   private void setEditedReminderToday() {
      editedReminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
      editedReminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
      editedReminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
   }

   private void setOriginalReminderDateSet() {
      originalReminder.set(Calendar.YEAR, dateSet.get(Calendar.YEAR));
      originalReminder.set(Calendar.MONTH, dateSet.get(Calendar.MONTH));
      originalReminder.set(Calendar.DAY_OF_MONTH, dateSet.get(Calendar.DAY_OF_MONTH));
   }

   @Override
   public void onNothingSelected(AdapterView<?> parent) {
      repeatMode = Co.REMINDER_ONE_TIME;
   }

   @Override
   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      if (isChecked){
         ValueAnimator va = ValueAnimator.ofInt(0, 500);
         va.setDuration(300);
         va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
               notificationDetailsLayout.getLayoutParams().height = (Integer) animation.getAnimatedValue();
               notificationDetailsLayout.requestLayout();
            }
         });
         va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
               super.onAnimationStart(animation);
            }
         });
         va.start();
      } else {
         ValueAnimator va = ValueAnimator.ofInt(500, 0);
         va.setDuration(300);
         va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
               notificationDetailsLayout.getLayoutParams().height = (Integer) animation.getAnimatedValue();
               notificationDetailsLayout.requestLayout();
            }
         });
         va.addListener(new AnimatorListenerAdapter()
         {
            @Override
            public void onAnimationEnd(Animator animation)
            {
//               notificationDetailsLayout.setVisibility(View.GONE);
            }
         });
         va.start();
      }
   }
}

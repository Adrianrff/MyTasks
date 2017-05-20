package com.adrapps.mytasks.views;

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

public class NewTaskOrEditActivity extends AppCompatActivity
      implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
      AdapterView.OnItemSelectedListener {

   EditText titleTV, notesTv;
   private long selectedDateInMills = 0;
   private LinearLayout notificationDetailsLayout;
   private RadioButton rbMorning;
   private RadioButton rbAfternoon;
   private RadioButton rbEvening;
   private RadioButton rbCustom;
   private LocalTask taskToEdit;
   private int position;
   ImageView clearDate, clearReminder;
   private AlertDialog dialog;
   private TextView notificationTV, dueDateTv, nextReminderTV;
   private Spinner repeatSpinner;
   private int repeatMode;
   private boolean customNotification;
   final private Calendar today = Calendar.getInstance();
   private Calendar originalReminder;
   private Calendar dateSet;
   private Calendar editedReminder;
   private boolean isFirstLaunch;

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

      originalReminder = null;
      selectedDateInMills = 0;
      repeatMode = 0;
      customNotification = false;
      titleTV = (EditText) findViewById(R.id.task_title_edit_text);
      dueDateTv = (TextView) findViewById(R.id.dueDateTv);
      notesTv = (EditText) findViewById(R.id.task_notes_edit_text);
      nextReminderTV = (TextView) findViewById(R.id.next_reminder_label);
      notificationDetailsLayout = (LinearLayout) findViewById(R.id.notification_layout);
      notificationTV = (TextView) findViewById(R.id.notificationTextView);
      clearDate = (ImageView) findViewById(R.id.clearDate);
      clearReminder = (ImageView) findViewById(R.id.clearReminder);
      repeatSpinner = (Spinner) findViewById(R.id.repeatSpinner);
      clearDate.setOnClickListener(this);
      clearReminder.setOnClickListener(this);
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
               selectedDateInMills = taskToEdit.getDue();
               clearDate.setVisibility(View.VISIBLE);
            } else {
               clearDate.setVisibility(View.GONE);
            }

            position = getIntent().getIntExtra(Co.ADAPTER_POSITION, -1);
            if (taskToEdit.getReminder() != 0) {
               //TODO: Inconsistency in tasks with originalReminder set and
               repeatMode = taskToEdit.getRepeatMode();
               originalReminder = Calendar.getInstance();
               originalReminder.setTimeInMillis(taskToEdit.getReminder());
               editedReminder = Calendar.getInstance();
               editedReminder.setTimeInMillis(taskToEdit.getReminder());
               if (repeatMode == 0 && originalReminder.before(today)) {
                  originalReminder = null;
                  editedReminder = null;
                  notificationDetailsLayout.setVisibility(View.GONE);
                  clearReminder.setVisibility(View.GONE);
                  return;
               }
               notificationTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               notificationDetailsLayout.setVisibility(View.VISIBLE);
               setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
               repeatSpinner.setSelection(repeatMode);
               nextReminderTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
            } else {
               notificationDetailsLayout.setVisibility(View.GONE);
               clearReminder.setVisibility(View.GONE);
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
      View dialogView = inflater.inflate(R.layout.notification_dialog, nullParent);
      dialogBuilder.setView(dialogView);
      LinearLayout morningLayout = (LinearLayout) dialogView.findViewById(R.id.layout_morning);
      LinearLayout afternoonLayout = (LinearLayout) dialogView.findViewById(R.id.layout_afternoon);
      LinearLayout eveningLayout = (LinearLayout) dialogView.findViewById(R.id.layout_evening);
      LinearLayout customLayout = (LinearLayout) dialogView.findViewById(R.id.layout_custom);
      rbMorning = (RadioButton) dialogView.findViewById(R.id.rb_morning);
      rbAfternoon = (RadioButton) dialogView.findViewById(R.id.rb_afternoon);
      rbEvening = (RadioButton) dialogView.findViewById(R.id.rb_evening);
      rbCustom = (RadioButton) dialogView.findViewById(R.id.rb_custom);
      morningLayout.setOnClickListener(this);
      afternoonLayout.setOnClickListener(this);
      eveningLayout.setOnClickListener(this);
      customLayout.setOnClickListener(this);
      dialog = dialogBuilder.create();
      dialog.setTitle(getString(R.string.notification_dialog_title));
      dialog.show();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
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
               taskToEdit.setTitle(titleTV.getText().toString());
               if (notesTv.getText().toString().trim().length() != 0)
                  taskToEdit.setNotes(notesTv.getText().toString());
               if (selectedDateInMills != 0 && selectedDateInMills != taskToEdit.getDue()) {
                  taskToEdit.setDue(selectedDateInMills);
               }
               if (selectedDateInMills == 0 && taskToEdit.getDue() != 0) {
                  taskToEdit.setDue(0);
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
      Calendar cal = Calendar.getInstance();
      switch (v.getId()) {

         //Clear date click
         case R.id.clearDate:
            selectedDateInMills = 0;
            dateSet = null;
            dueDateTv.setText(null);
            clearDate.setVisibility(View.GONE);
            if (originalReminder != null) {
               originalReminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
               originalReminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
               originalReminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
               notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
               setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
               repeatSpinner.setSelection(Co.REMINDER_DAILY);
            }
            break;

         //Clear originalReminder click
         case R.id.clearReminder:
            originalReminder = null;
            editedReminder = null;
            repeatMode = 0;
            notificationTV.setText(null);
            clearReminder.setVisibility(View.GONE);
            notificationDetailsLayout.setVisibility(View.INVISIBLE);
            break;

         //Date picker click
         case R.id.dueDateTv:
            DatePickerDialog datePicker = new DatePickerDialog(this, this,
                  cal.get(Calendar.YEAR),
                  cal.get(Calendar.MONTH),
                  cal.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
            break;

         //Morning originalReminder click
         case R.id.layout_morning:
            customNotification = false;
            rbMorning.setChecked(true);
            rbCustom.setChecked(false);
            rbEvening.setChecked(false);
            rbAfternoon.setChecked(false);
            if (dateSet != null) {
               if (DateHelper.isBeforeByAtLeastDay(dateSet)) {
                  originalReminder = Calendar.getInstance();
                  originalReminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                  originalReminder.set(Calendar.MINUTE, 0);
                  notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                  repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                  dialog.dismiss();
                  break;
               } else if (DateUtils.isToday(dateSet.getTimeInMillis())) {
                  originalReminder = Calendar.getInstance();
                  originalReminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                  originalReminder.set(Calendar.MINUTE, 0);
                  if (originalReminder.before(Calendar.getInstance())) {
                     clearReminder.setVisibility(View.VISIBLE);
                     notificationDetailsLayout.setVisibility(View.VISIBLE);
                     setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                     notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                     repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                     dialog.dismiss();
                  } else {
                     setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                     repeatSpinner.setSelection(repeatMode);
                     clearReminder.setVisibility(View.VISIBLE);
                     notificationDetailsLayout.setVisibility(View.VISIBLE);
                     notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                     dialog.dismiss();
                  }
                  break;
               } else {
                  originalReminder = dateSet;
                  originalReminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                  originalReminder.set(Calendar.MINUTE, 0);
                  notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                  repeatSpinner.setSelection(repeatMode);
                  dialog.dismiss();
                  break;
               }
            } else {
               originalReminder = Calendar.getInstance();
               originalReminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
               originalReminder.set(Calendar.MINUTE, 0);
               if (originalReminder.before(today)) {
                  notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                  repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                  dialog.dismiss();
                  break;
               } else {
                  notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                  repeatSpinner.setSelection(repeatMode);
                  dialog.dismiss();
                  break;
               }

            }

            //Afternoon originalReminder click
         case R.id.layout_afternoon:
            customNotification = false;
            rbAfternoon.setChecked(true);
            rbMorning.setChecked(false);
            rbCustom.setChecked(false);
            rbEvening.setChecked(false);
            if (dateSet != null) {
               if (DateHelper.isBeforeByAtLeastDay(dateSet)) {
                  originalReminder = Calendar.getInstance();
                  originalReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                  originalReminder.set(Calendar.MINUTE, 0);
                  notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                  repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                  dialog.dismiss();
                  break;
               } else if (DateUtils.isToday(dateSet.getTimeInMillis())) {
                  originalReminder = Calendar.getInstance();
                  originalReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                  originalReminder.set(Calendar.MINUTE, 0);
                  if (originalReminder.before(Calendar.getInstance())) {
                     clearReminder.setVisibility(View.VISIBLE);
                     notificationDetailsLayout.setVisibility(View.VISIBLE);
                     setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                     notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                     repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                     dialog.dismiss();
                  } else {
                     clearReminder.setVisibility(View.VISIBLE);
                     notificationDetailsLayout.setVisibility(View.VISIBLE);
                     setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                     notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                     repeatSpinner.setSelection(repeatMode);
                     dialog.dismiss();
                  }
                  break;
               } else {
                  originalReminder = dateSet;
                  originalReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                  originalReminder.set(Calendar.MINUTE, 0);
                  notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                  repeatSpinner.setSelection(repeatMode);
                  dialog.dismiss();
                  break;
               }
            } else {
               originalReminder = Calendar.getInstance();
               originalReminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
               originalReminder.set(Calendar.MINUTE, 0);
               if (originalReminder.before(today)) {
                  notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                  repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                  dialog.dismiss();
                  break;
               } else {
                  notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                  repeatSpinner.setSelection(repeatMode);
                  dialog.dismiss();
                  break;
               }
            }

            //Evening click
         case R.id.layout_evening:
            customNotification = false;
            rbEvening.setChecked(true);
            rbMorning.setChecked(false);
            rbCustom.setChecked(false);
            rbAfternoon.setChecked(false);
            if (dateSet != null) {
               if (DateHelper.isBeforeByAtLeastDay(dateSet)) {
                  originalReminder = Calendar.getInstance();
                  originalReminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                  originalReminder.set(Calendar.MINUTE, 0);
                  notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                  repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                  dialog.dismiss();
                  break;
               } else if (DateUtils.isToday(dateSet.getTimeInMillis())) {
                  originalReminder = Calendar.getInstance();
                  originalReminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                  originalReminder.set(Calendar.MINUTE, 0);
                  if (originalReminder.before(Calendar.getInstance())) {
                     clearReminder.setVisibility(View.VISIBLE);
                     notificationDetailsLayout.setVisibility(View.VISIBLE);
                     setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                     notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                     repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                     dialog.dismiss();
                  } else {
                     clearReminder.setVisibility(View.VISIBLE);
                     notificationDetailsLayout.setVisibility(View.VISIBLE);
                     setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                     notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                     repeatSpinner.setSelection(repeatMode);
                     dialog.dismiss();
                  }
                  break;
               } else {
                  originalReminder = dateSet;
                  originalReminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                  originalReminder.set(Calendar.MINUTE, 0);
                  notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                  repeatSpinner.setSelection(repeatMode);
                  dialog.dismiss();
                  break;
               }
            } else {
               originalReminder = Calendar.getInstance();
               originalReminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
               originalReminder.set(Calendar.MINUTE, 0);
               if (originalReminder.before(today)) {
                  notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                  repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                  dialog.dismiss();
                  break;
               } else {
                  notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                  clearReminder.setVisibility(View.VISIBLE);
                  notificationDetailsLayout.setVisibility(View.VISIBLE);
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                  repeatSpinner.setSelection(repeatMode);
                  dialog.dismiss();
                  break;
               }
            }

            //Custom originalReminder click
         case R.id.layout_custom:
            rbCustom.setChecked(true);
            rbMorning.setChecked(false);
            rbAfternoon.setChecked(false);
            rbEvening.setChecked(false);
            TimePickerDialog timePicker = new TimePickerDialog(
                  NewTaskOrEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
               @Override
               public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                  //TODO: allow setting a date in custom originalReminder
                  if (dateSet == null) {
                     originalReminder = Calendar.getInstance();
                     originalReminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
                     originalReminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
                     originalReminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                     originalReminder.set(Calendar.HOUR_OF_DAY, hourOfDay);
                     originalReminder.set(Calendar.MINUTE, minute);
                  } else {
                     originalReminder = dateSet;
                     originalReminder.set(Calendar.HOUR_OF_DAY, hourOfDay);
                     originalReminder.set(Calendar.MINUTE, minute);
                  }
                  if (originalReminder.before(today)) {
                     notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                     setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                     repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                     notificationDetailsLayout.setVisibility(View.VISIBLE);
                     dialog.dismiss();
                  } else {
                     notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                     dialog.dismiss();
                     notificationDetailsLayout.setVisibility(View.VISIBLE);
                     customNotification = true;
                     setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                     repeatSpinner.setSelection(repeatMode);
                  }
               }
            }, today.get(Calendar.HOUR_OF_DAY), today.get(Calendar.MINUTE), false);
            timePicker.show();
            customNotification = true;
            clearReminder.setVisibility(View.VISIBLE);
            break;

         //Reminder textview click
         case R.id.notificationTextView:
            showReminderDialog();
            break;
      }
   }


   @Override
   public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
      Calendar today = Calendar.getInstance();
      dateSet = Calendar.getInstance();
      dateSet.set(year, month, dayOfMonth);
      dateSet.set(Calendar.HOUR_OF_DAY, 0);
      dateSet.set(Calendar.MINUTE, 0);
      selectedDateInMills = dateSet.getTimeInMillis();
      dueDateTv.setText(DateHelper.timeInMillsToString(selectedDateInMills));
      if (selectedDateInMills != 0) {
         if (DateHelper.isBeforeByAtLeastDay(dateSet)) {
            if (originalReminder != null) {
               originalReminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
               originalReminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
               originalReminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
               setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
               repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
               notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
            }
         } else if (DateUtils.isToday(selectedDateInMills)) {
            if (originalReminder != null) {
               if (originalReminder.before(Calendar.getInstance())) {
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), true);
                  notificationTV.setText(DateHelper.timeInMillsToTimeOnly(originalReminder.getTimeInMillis()));
                  repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
               } else {
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                  repeatSpinner.setSelection(repeatMode);
                  notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
               }
            }
         } else {
            if (originalReminder != null) {
                  originalReminder.set(Calendar.DAY_OF_MONTH, dateSet.get(Calendar.DAY_OF_MONTH));
                  originalReminder.set(Calendar.YEAR, dateSet.get(Calendar.YEAR));
                  originalReminder.set(Calendar.MONTH, dateSet.get(Calendar.MONTH));
                  notificationTV.setText(DateHelper.timeInMillsToFullString(originalReminder.getTimeInMillis()));
                  setRepeatSpinnerAdapter(originalReminder.getTimeInMillis(), false);
                  repeatSpinner.setSelection(repeatMode);
            }
         }
         clearDate.setVisibility(View.VISIBLE);
      } else {
         clearDate.setVisibility(View.GONE);
      }
   }

   public void showToast(String msg) {
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
   }

   @Override
   public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      editedReminder = (Calendar) originalReminder.clone();
      switch (position) {

         case Co.REMINDER_ONE_TIME:
            repeatMode = Co.REMINDER_ONE_TIME;
            if (isFirstLaunch && taskToEdit.getReminder() != 0){
               nextReminderTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               notificationTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               isFirstLaunch = false;
               break;
            }
            nextReminderTV.setText(DateHelper.timeInMillsToFullString(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.timeInMillsToFullString(editedReminder.getTimeInMillis()));
            break;

         case Co.REMINDER_DAILY:
            repeatMode = Co.REMINDER_DAILY;
            if (isFirstLaunch && taskToEdit.getReminder() != 0){
               nextReminderTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               notificationTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               isFirstLaunch = false;
               break;
            }
            editedReminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
            editedReminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
            editedReminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
            if (editedReminder.before(today)) {
               editedReminder.add(Calendar.DATE, 1);
            }
            nextReminderTV.setText(DateHelper.timeInMillsToFullString(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.timeInMillsToTimeOnly(editedReminder.getTimeInMillis()));
            break;

         case Co.REMINDER_DAILY_WEEKDAYS:
            repeatMode = Co.REMINDER_DAILY_WEEKDAYS;
            if (isFirstLaunch && taskToEdit.getReminder() != 0){
               nextReminderTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               notificationTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               isFirstLaunch = false;
               break;
            }
            editedReminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
            editedReminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
            editedReminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
            if (editedReminder.before(today)) {
               editedReminder.add(Calendar.DATE, 1);
               if (!DateHelper.isWeekday(editedReminder)) {
                  if (editedReminder.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                     editedReminder.add(Calendar.DATE, 2);
                  } else {
                     editedReminder.add(Calendar.DATE, 1);
                  }
               }
            }
            nextReminderTV.setText(DateHelper.timeInMillsToFullString(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.timeInMillsToTimeOnly(editedReminder.getTimeInMillis()));
            break;

         case Co.REMINDER_SAME_DAY_OF_WEEK:
            repeatMode = Co.REMINDER_SAME_DAY_OF_WEEK;
            if (isFirstLaunch && taskToEdit.getReminder() != 0){
               nextReminderTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               notificationTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               isFirstLaunch = false;
               break;
            }
            editedReminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
            editedReminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
            editedReminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
            if (dateSet == null) {
               if (editedReminder.before(today)) {
                  editedReminder.add(Calendar.DATE, 7);
               }
            } else {
               int dateSetDayOfWeek = dateSet.get(Calendar.DAY_OF_WEEK);
               if (today.get(Calendar.DAY_OF_WEEK) == dateSetDayOfWeek) {
                  if (editedReminder.before(today)) {
                     editedReminder.add(Calendar.DATE, 7);
                  }
               } else {
                  editedReminder.add(Calendar.DATE, (dateSetDayOfWeek + 7 - today.get(Calendar.DAY_OF_WEEK)) % 7);
                  if (editedReminder.before(today)) {
                     editedReminder.add(Calendar.DATE, 7);
                  }
               }
            }
            nextReminderTV.setText(DateHelper.timeInMillsToFullString(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.timeInMillsToTimeOnly(editedReminder.getTimeInMillis()));
            break;

         case Co.REMINDER_SAME_DAY_OF_MONTH:
            repeatMode = Co.REMINDER_SAME_DAY_OF_MONTH;
            if (isFirstLaunch && taskToEdit.getReminder() != 0){
               nextReminderTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               notificationTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
               isFirstLaunch = false;
               break;
            }
            if (dateSet == null) {
               editedReminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
               editedReminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
            } else {
               editedReminder.set(Calendar.DAY_OF_MONTH, dateSet.get(Calendar.DAY_OF_MONTH));
            }
            if (editedReminder.before(today)) {
               editedReminder.add(Calendar.MONTH, 1);
            }
            nextReminderTV.setText(DateHelper.timeInMillsToFullString(editedReminder.getTimeInMillis()));
            notificationTV.setText(DateHelper.timeInMillsToTimeOnly(editedReminder.getTimeInMillis()));
            break;


      }
   }

   @Override
   public void onNothingSelected(AdapterView<?> parent) {
      repeatMode = Co.REMINDER_ONE_TIME;
   }
}

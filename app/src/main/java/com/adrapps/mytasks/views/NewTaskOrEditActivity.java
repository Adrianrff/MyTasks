package com.adrapps.mytasks.views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
    final int hour = today.get(Calendar.HOUR_OF_DAY);
    final int minute = today.get(Calendar.MINUTE);
    private Calendar reminder, finalReminderCalendar;
    private Calendar dateSet;
    private Calendar reminderClone;

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

        reminder = null;
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
                    repeatMode = taskToEdit.getRepeatMode();
                    reminder = Calendar.getInstance();
                    reminder.setTimeInMillis(taskToEdit.getReminder());
                    if (repeatMode == 0 && reminder.before(today)) {
                        reminder = null;
                        notificationDetailsLayout.setVisibility(View.GONE);
                        clearReminder.setVisibility(View.GONE);
                        return;
                    }
                    notificationTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
                    notificationDetailsLayout.setVisibility(View.VISIBLE);
                    setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
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


    //TODO: Don't allow "one time" if selected date is in the past
    //TODO: Update reminder field (if set) if date is deleted (keep time only if "one time" is not set, delete otherwise)
    //TODO: If reminder (morning, evening or afternoon) is selected while a past date is set, then show time only and set selectedReminder to to the next date
    //TODO: when saving "selectedReminderInMillis" make sure it's not a past date
    private void setRepeatSpinnerAdapter(long reminderInMills, final boolean disableOneTime) {
        List<String> categories = new ArrayList<>();
        categories.add(getString(R.string.one_time_repeat_mode));
        categories.add(getString(R.string.daily_repeat_mode) + " (" + DateHelper.timeInMillsToSimpleTime(reminderInMills) + ")");
        categories.add(getString(R.string.weekdays) + " (" + DateHelper.timeInMillsToSimpleTime(reminderInMills) + ")");
        if (dateSet == null) {
            categories.add(getString(R.string.on_day) + " " + DateHelper.timeInMillsToDay(reminderInMills) + " " + getString(R.string.of_every_month));
        } else {
            categories.add(getString(R.string.every) + " " + DateHelper.timeInMillsToDay(dateSet.getTimeInMillis())
                    + " (" + DateHelper.timeInMillsToSimpleTime(reminderInMills) + ")");
        }
        if (dateSet == null) {
            categories.add(getString(R.string.on_day) + " " + DateHelper.timeInMillsToDayOfMonth(reminderInMills) + " " + getString(R.string.of_every_month));
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
                        if (position == 0 && disableOneTime) {

                            return false;
                        } else {
                            return true;
                        }
                    }

                    @Override
                    public View getDropDownView(int position, View convertView,
                                                ViewGroup parent) {
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

    private void showNotificationDialog() {
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
//                    if (dateSet != null) {
//                        showToast("Reminder Calendar: " + DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()) + "\n" +
//                                "Reminder Clone: " + DateHelper.timeInMillsToFullString(reminderClone.getTimeInMillis()) +
//                                "\n" + String.valueOf(repeatMode));
//                    } else {
//                        showToast("Reminder Calendar: " + DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()) + "\n" +
//                                "Reminder Clone: " + DateHelper.timeInMillsToFullString(reminderClone.getTimeInMillis()) +
//                                "\n" + String.valueOf(repeatMode) + "\n" + "Date null");
//                    }
                break;

            case R.id.save_task:

                //TASK EDITED
                if (getIntent().hasExtra(Co.LOCAL_TASK)) {
//                    Calendar reminder = Calendar.getInstance();
//                    reminder.setTimeInMillis(selectedReminderInMills);
                    Intent i = new Intent();
                    if (taskToEdit.getReminderId() != 0) {
                        if (reminder == null) {
                            taskToEdit.setReminderNoID(0);
                        } else {
                            if (reminder.getTimeInMillis() != taskToEdit.getReminder()) {
                                if (repeatMode != Co.REMINDER_ONE_TIME) {
                                    reminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
                                    reminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
                                    reminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                                }
                                taskToEdit.setReminderNoID(reminder.getTimeInMillis());
                            } else {
//                                taskToEdit.setReminderNoID(reminder.getTimeInMillis());
                            }
                            taskToEdit.setRepeatMode(repeatMode);
                        }
                    } else {
                        if (reminder != null) {
                            taskToEdit.setReminder(reminder.getTimeInMillis());
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
                    LocalTask task = new LocalTask(titleTV.getText().toString(),
                            selectedDateInMills);
                    if (notesTv.getText().toString().trim().length() != 0)
                        task.setNotes(notesTv.getText().toString());
                    if (reminder != null) {
                        task.setReminder(reminder.getTimeInMillis());
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
                if (reminder != null) {
                    reminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
                    reminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
                    reminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                    notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                    setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                    repeatSpinner.setSelection(Co.REMINDER_DAILY);
                }
                break;

            //Clear reminder click
            case R.id.clearReminder:
                reminder = null;
                reminderClone = null;
                notificationTV.setText(null);
                reminder = null;
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

            //Morning reminder click
            case R.id.layout_morning:
                customNotification = false;
                rbMorning.setChecked(true);
                rbCustom.setChecked(false);
                rbEvening.setChecked(false);
                rbAfternoon.setChecked(false);
                if (dateSet != null) {
                    if (DateHelper.isBeforeByAtLeastDay(dateSet)) {
                        reminder = Calendar.getInstance();
                        reminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                        reminder.set(Calendar.MINUTE, 0);
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                        repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                        dialog.dismiss();
                        break;
                    } else if (DateUtils.isToday(dateSet.getTimeInMillis())) {
                        reminder = Calendar.getInstance();
                        reminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                        reminder.set(Calendar.MINUTE, 0);
                        if (reminder.before(Calendar.getInstance())) {
                            clearReminder.setVisibility(View.VISIBLE);
                            notificationDetailsLayout.setVisibility(View.VISIBLE);
                            setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                            notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                            repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                            dialog.dismiss();
                        } else {
                            setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                            repeatSpinner.setSelection(repeatMode);
                            clearReminder.setVisibility(View.VISIBLE);
                            notificationDetailsLayout.setVisibility(View.VISIBLE);
                            notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                            dialog.dismiss();
                        }
                        break;
                    } else {
                        reminder = dateSet;
                        reminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                        reminder.set(Calendar.MINUTE, 0);
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                        repeatSpinner.setSelection(repeatMode);
                        dialog.dismiss();
                        break;
                    }
                } else {
                    reminder = Calendar.getInstance();
                    reminder.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                    reminder.set(Calendar.MINUTE, 0);
                    if (reminder.before(today)) {
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                        repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                        dialog.dismiss();
                        break;
                    } else {
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                        repeatSpinner.setSelection(repeatMode);
                        dialog.dismiss();
                        break;
                    }

                }

                //Afternoon reminder click
            case R.id.layout_afternoon:
                customNotification = false;
                rbAfternoon.setChecked(true);
                rbMorning.setChecked(false);
                rbCustom.setChecked(false);
                rbEvening.setChecked(false);
                if (dateSet != null) {
                    if (DateHelper.isBeforeByAtLeastDay(dateSet)) {
                        reminder = Calendar.getInstance();
                        reminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                        reminder.set(Calendar.MINUTE, 0);
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                        repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                        dialog.dismiss();
                        break;
                    } else if (DateUtils.isToday(dateSet.getTimeInMillis())) {
                        reminder = Calendar.getInstance();
                        reminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                        reminder.set(Calendar.MINUTE, 0);
                        if (reminder.before(Calendar.getInstance())) {
                            clearReminder.setVisibility(View.VISIBLE);
                            notificationDetailsLayout.setVisibility(View.VISIBLE);
                            setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                            notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                            repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                            dialog.dismiss();
                        } else {
                            clearReminder.setVisibility(View.VISIBLE);
                            notificationDetailsLayout.setVisibility(View.VISIBLE);
                            setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                            notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                            repeatSpinner.setSelection(repeatMode);
                            dialog.dismiss();
                        }
                        break;
                    } else {
                        reminder = dateSet;
                        reminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                        reminder.set(Calendar.MINUTE, 0);
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                        repeatSpinner.setSelection(repeatMode);
                        dialog.dismiss();
                        break;
                    }
                } else {
                    reminder = Calendar.getInstance();
                    reminder.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                    reminder.set(Calendar.MINUTE, 0);
                    if (reminder.before(today)) {
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                        repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                        dialog.dismiss();
                        break;
                    } else {
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
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
                        reminder = Calendar.getInstance();
                        reminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                        reminder.set(Calendar.MINUTE, 0);
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                        repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                        dialog.dismiss();
                        break;
                    } else if (DateUtils.isToday(dateSet.getTimeInMillis())) {
                        reminder = Calendar.getInstance();
                        reminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                        reminder.set(Calendar.MINUTE, 0);
                        if (reminder.before(Calendar.getInstance())) {
                            clearReminder.setVisibility(View.VISIBLE);
                            notificationDetailsLayout.setVisibility(View.VISIBLE);
                            setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                            notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                            repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                            dialog.dismiss();
                        } else {
                            clearReminder.setVisibility(View.VISIBLE);
                            notificationDetailsLayout.setVisibility(View.VISIBLE);
                            setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                            notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                            repeatSpinner.setSelection(repeatMode);
                            dialog.dismiss();
                        }
                        break;
                    } else {
                        reminder = dateSet;
                        reminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                        reminder.set(Calendar.MINUTE, 0);
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                        repeatSpinner.setSelection(repeatMode);
                        dialog.dismiss();
                        break;
                    }
                } else {
                    reminder = Calendar.getInstance();
                    reminder.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                    reminder.set(Calendar.MINUTE, 0);
                    if (reminder.before(today)) {
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                        repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                        dialog.dismiss();
                        break;
                    } else {
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notificationDetailsLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                        repeatSpinner.setSelection(repeatMode);
                        dialog.dismiss();
                        break;
                    }
                }

                //Custom reminder click
            case R.id.layout_custom:
                rbCustom.setChecked(true);
                rbMorning.setChecked(false);
                rbAfternoon.setChecked(false);
                rbEvening.setChecked(false);
                TimePickerDialog timePicker = new TimePickerDialog(
                        NewTaskOrEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (dateSet == null) {
                            reminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
                            reminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
                            reminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                            reminder.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            reminder.set(Calendar.MINUTE, minute);
                        } else {
                            reminder = dateSet;
                            reminder.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            reminder.set(Calendar.MINUTE, minute);
                        }
                        if (reminder.before(today)) {

                            notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                            setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                            repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                            notificationDetailsLayout.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        } else {
                            notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                            dialog.dismiss();
                            notificationDetailsLayout.setVisibility(View.VISIBLE);
                            customNotification = true;
                            repeatSpinner.setSelection(repeatMode);
                            setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                        }
                    }
                }, today.get(Calendar.HOUR_OF_DAY), today.get(Calendar.MINUTE), false);
                timePicker.show();
                customNotification = true;
                clearReminder.setVisibility(View.VISIBLE);
                break;

            //Reminder textview click
            case R.id.notificationTextView:
                showNotificationDialog();
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
                if (reminder != null) {
                    reminder.set(Calendar.YEAR, today.get(Calendar.YEAR));
                    reminder.set(Calendar.MONTH, today.get(Calendar.MONTH));
                    reminder.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                    setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                    repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                    notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                }
            } else if (DateUtils.isToday(selectedDateInMills)) {
                if (reminder != null) {
                    if (reminder.before(Calendar.getInstance())) {
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), true);
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminder.getTimeInMillis()));
                        repeatSpinner.setSelection(repeatMode != 0 ? repeatMode : Co.REMINDER_DAILY);
                    } else {
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                        repeatSpinner.setSelection(repeatMode);
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                    }
                }
            } else {
                if (reminder != null) {
                    if (!customNotification) {
                        reminder.set(Calendar.DAY_OF_MONTH, dateSet.get(Calendar.DAY_OF_MONTH));
                        reminder.set(Calendar.YEAR, dateSet.get(Calendar.YEAR));
                        reminder.set(Calendar.MONTH, dateSet.get(Calendar.MONTH));
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminder.getTimeInMillis()));
                        setRepeatSpinnerAdapter(reminder.getTimeInMillis(), false);
                        repeatSpinner.setSelection(repeatMode);
                    }
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
        reminderClone = (Calendar) reminder.clone();
        switch (position) {

            case 0:
                repeatMode = Co.REMINDER_ONE_TIME;
                nextReminderTV.setText(DateHelper.timeInMillsToFullString(reminderClone.getTimeInMillis()));
                notificationTV.setText(DateHelper.timeInMillsToFullString(reminderClone.getTimeInMillis()));
                break;

            case 1:
                repeatMode = Co.REMINDER_DAILY;
                reminderClone.set(Calendar.YEAR, today.get(Calendar.YEAR));
                reminderClone.set(Calendar.MONTH, today.get(Calendar.MONTH));
                reminderClone.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                if (reminderClone.before(today)) {
                    reminderClone.add(Calendar.DATE, 1);
                }
                nextReminderTV.setText(DateHelper.timeInMillsToFullString(reminderClone.getTimeInMillis()));
                notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderClone.getTimeInMillis()));
                break;

            case 2:
                repeatMode = Co.REMINDER_DAILY_WEEKDAYS;
                reminderClone.set(Calendar.YEAR, today.get(Calendar.YEAR));
                reminderClone.set(Calendar.MONTH, today.get(Calendar.MONTH));
                reminderClone.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                if (reminderClone.before(today)) {
                    reminderClone.add(Calendar.DATE, 1);
                    if (!DateHelper.isWeekday(reminderClone)) {
                        if (reminderClone.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                            reminderClone.add(Calendar.DATE, 2);
                        } else {
                            reminderClone.add(Calendar.DATE, 1);
                        }
                    }
                }
                nextReminderTV.setText(DateHelper.timeInMillsToFullString(reminderClone.getTimeInMillis()));
                notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderClone.getTimeInMillis()));
                break;

            case 3:
                repeatMode = Co.REMINDER_SAME_DAY_OF_WEEK;
                reminderClone.set(Calendar.YEAR, today.get(Calendar.YEAR));
                reminderClone.set(Calendar.MONTH, today.get(Calendar.MONTH));
                reminderClone.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                if (dateSet == null) {
                    if (reminderClone.before(today)) {
                        reminderClone.add(Calendar.DATE, 7);
                    }
                } else {
                    int dateSetDayOfWeek = dateSet.get(Calendar.DAY_OF_WEEK);
                    if (today.get(Calendar.DAY_OF_WEEK) == dateSetDayOfWeek) {
                        if (reminderClone.before(today)) {
                            reminderClone.add(Calendar.DATE, 7);
                        }
//                    } else {
//                        int diff = dateSetDayOfWeek - today.get(Calendar.DAY_OF_WEEK);
//                        if (diff > 0){
//                            reminderClone.add(Calendar.DATE, diff);
//                        } else {
//                            reminderClone.add(Calendar.DATE, 7 + diff);
//                        }
                    } else {
                        reminderClone.add(Calendar.DATE, (dateSetDayOfWeek + 7 - today.get(Calendar.DAY_OF_WEEK)) % 7);
                        if (reminderClone.before(today)) {
                            reminderClone.add(Calendar.DATE, 7);
                        }
                    }
                }
                nextReminderTV.setText(DateHelper.timeInMillsToFullString(reminderClone.getTimeInMillis()));
                notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderClone.getTimeInMillis()));
                break;

            case 4:
                repeatMode = Co.REMINDER_SAME_DAY_OF_MONTH;
                reminderClone.set(Calendar.YEAR, today.get(Calendar.YEAR));
                reminderClone.set(Calendar.MONTH, today.get(Calendar.MONTH));
                if (dateSet == null) {
                    reminderClone.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                } else {
                    reminderClone.set(Calendar.DAY_OF_MONTH, dateSet.get(Calendar.DAY_OF_MONTH));
                }
                showToast(DateHelper.timeInMillsToFullString(reminderClone.getTimeInMillis()));
                if (reminderClone.before(today)) {
                    reminderClone.add(Calendar.MONTH, 1);
                }
                nextReminderTV.setText(DateHelper.timeInMillsToFullString(reminderClone.getTimeInMillis()));
                notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderClone.getTimeInMillis()));
                break;


        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        repeatMode = Co.REMINDER_ONE_TIME;
    }
}

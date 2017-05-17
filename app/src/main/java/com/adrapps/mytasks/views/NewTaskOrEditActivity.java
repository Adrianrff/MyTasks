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
    TextView dueDateTv;
    private long selectedDateInMills = 0;
    private LinearLayout notifLayout;
    private RadioButton rbMorning;
    private RadioButton rbAfternoon;
    private RadioButton rbEvening;
    private RadioButton rbCustom;
    private LocalTask taskToEdit;
    private int position;
    private TextView customNotOption;
    ImageView clearDate, clearReminder;
    private AlertDialog dialog;
    private TextView notificationTV;
    private Spinner repeatSpinner;
    private int selectedRepeatMode;
    private boolean customNotification;
    final private Calendar today = Calendar.getInstance();
    final int hour =today.get(Calendar.HOUR_OF_DAY);
    final int minute = today.get(Calendar.MINUTE);;
    private Calendar reminderCalendar, finalReminderCalendar;
    private Calendar dateSet;

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

        reminderCalendar = null;
        selectedDateInMills = 0;
        selectedRepeatMode = 0;
        customNotification = false;
        titleTV = (EditText) findViewById(R.id.task_title_edit_text);
        dueDateTv = (TextView) findViewById(R.id.dueDateTv);
        notesTv = (EditText) findViewById(R.id.task_notes_edit_text);
        notifLayout = (LinearLayout) findViewById(R.id.notification_layout);
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
                    reminderCalendar = Calendar.getInstance();
                    reminderCalendar.setTimeInMillis(taskToEdit.getReminder());
                    selectedRepeatMode = taskToEdit.getRepeatMode();
                    notificationTV.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
                    notifLayout.setVisibility(View.VISIBLE);
                    setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), false);
                    repeatSpinner.setSelection(selectedRepeatMode);
                } else {
                    notifLayout.setVisibility(View.GONE);
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
        categories.add(getString(R.string.every) + " " + DateHelper.timeInMillsToDay(reminderInMills)
                + " (" + DateHelper.timeInMillsToSimpleTime(reminderInMills) + ")");
        categories.add(getString(R.string.on_day) + " " + DateHelper.timeInMillsToDayOfMonth(reminderInMills) + " " + getString(R.string.of_every_month));
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
        customNotOption = (TextView) dialogView.findViewById(R.id.custom_notification_option);
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
                if (reminderCalendar != null) {
                    showToast("Reminder: " + DateHelper.timeInMillsToFullString(reminderCalendar.getTimeInMillis()) +
                            "\n" + String.valueOf(selectedRepeatMode) + "\n" + DateHelper.timeInMillsToFullString(dateSet.getTimeInMillis()));
                }
                break;

            case R.id.save_task:

                //TASK EDITED
                if (getIntent().hasExtra(Co.LOCAL_TASK)) {
//                    Calendar reminderCalendar = Calendar.getInstance();
//                    reminderCalendar.setTimeInMillis(selectedReminderInMills);
                    Intent i = new Intent();
                    if (taskToEdit.getReminderId() != 0) {
                        if (reminderCalendar == null) {
                            taskToEdit.setReminderNoID(0);
                        } else {
                            if (reminderCalendar.getTimeInMillis() != taskToEdit.getReminder()) {
                                if (selectedRepeatMode != Co.REMINDER_ONE_TIME) {
                                    reminderCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
                                    reminderCalendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
                                    reminderCalendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                                }
                                taskToEdit.setReminderNoID(reminderCalendar.getTimeInMillis());
                            } else {
//                                taskToEdit.setReminderNoID(reminderCalendar.getTimeInMillis());
                            }
                            taskToEdit.setRepeatMode(selectedRepeatMode);
                        }
                    } else {
                        if (reminderCalendar != null) {
                            taskToEdit.setReminder(reminderCalendar.getTimeInMillis());
                            taskToEdit.setRepeatMode(selectedRepeatMode);
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
                    if (reminderCalendar != null) {
                        task.setReminder(reminderCalendar.getTimeInMillis());
                        task.setRepeatMode(selectedRepeatMode);
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
                dueDateTv.setText(null);
                clearDate.setVisibility(View.GONE);
                if (reminderCalendar != null) {
                    reminderCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
                    reminderCalendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
                    reminderCalendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                    notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderCalendar.getTimeInMillis()));
                    setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), true);
                    repeatSpinner.setSelection(Co.REMINDER_DAILY);
                }
                break;

            //Clear reminder click
            case R.id.clearReminder:
                reminderCalendar = null;
                notificationTV.setText(null);
                clearReminder.setVisibility(View.GONE);
                notifLayout.setVisibility(View.INVISIBLE);
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
                        reminderCalendar = Calendar.getInstance();
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                        reminderCalendar.set(Calendar.MINUTE, 0);
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderCalendar.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notifLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), true);
                        repeatSpinner.setSelection(Co.REMINDER_DAILY);
                        dialog.dismiss();
                        break;
                    } else {
                        dateSet.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                        dateSet.set(Calendar.MINUTE, 0);
                        reminderCalendar = dateSet;
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminderCalendar.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notifLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), false);
                        dialog.dismiss();
                        break;
                    }
                } else {
                    cal.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                    cal.set(Calendar.MINUTE, 0);
                    reminderCalendar = dateSet;
                    notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderCalendar.getTimeInMillis()));
                    clearReminder.setVisibility(View.VISIBLE);
                    notifLayout.setVisibility(View.VISIBLE);
                    setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), true);
                    repeatSpinner.setSelection(1);
                    dialog.dismiss();
                    break;
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
                        reminderCalendar = Calendar.getInstance();
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                        reminderCalendar.set(Calendar.MINUTE, 0);
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderCalendar.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notifLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), true);
                        repeatSpinner.setSelection(Co.REMINDER_DAILY);
                        dialog.dismiss();
                        break;
                    } else {
                        dateSet.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                        dateSet.set(Calendar.MINUTE, 0);
                        reminderCalendar = dateSet;
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminderCalendar.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notifLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), false);
                        dialog.dismiss();
                        break;
                    }
                } else {
                    cal.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                    cal.set(Calendar.MINUTE, 0);
                    reminderCalendar = cal;
                    notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderCalendar.getTimeInMillis()));
                    clearReminder.setVisibility(View.VISIBLE);
                    notifLayout.setVisibility(View.VISIBLE);
                    setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), true);
                    repeatSpinner.setSelection(1);
                    dialog.dismiss();
                    break;
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
                        reminderCalendar = Calendar.getInstance();
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                        reminderCalendar.set(Calendar.MINUTE, 0);
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderCalendar.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notifLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), true);
                        repeatSpinner.setSelection(Co.REMINDER_DAILY);
                        dialog.dismiss();
                        break;
                    } else {
                        dateSet.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                        dateSet.set(Calendar.MINUTE, 0);
                        reminderCalendar = cal;
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminderCalendar.getTimeInMillis()));
                        clearReminder.setVisibility(View.VISIBLE);
                        notifLayout.setVisibility(View.VISIBLE);
                        setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), false);
                        dialog.dismiss();
                        break;
                    }
                } else {
                    cal.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                    cal.set(Calendar.MINUTE, 0);
                    reminderCalendar = cal;
                    notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderCalendar.getTimeInMillis()));
                    clearReminder.setVisibility(View.VISIBLE);
                    notifLayout.setVisibility(View.VISIBLE);
                    setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), true);
                    repeatSpinner.setSelection(1);
                    dialog.dismiss();
                    break;
                }

                //Custom reminder click
            case R.id.layout_custom:
                rbCustom.setChecked(true);
                rbMorning.setChecked(false);
                rbAfternoon.setChecked(false);
                rbEvening.setChecked(false);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        final Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, month);
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        TimePickerDialog timePicker = new TimePickerDialog(
                                NewTaskOrEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                c.set(Calendar.MINUTE, minute);
                                reminderCalendar = c;
                                if (Calendar.getInstance().getTimeInMillis() > reminderCalendar.getTimeInMillis()) {
                                    showToast(getString(R.string.past_reminder_no_effect));
                                    reminderCalendar = null;
                                    return;
                                }
                                customNotOption.setText(DateHelper.timeInMillsToFullString(reminderCalendar.getTimeInMillis()));
                                notificationTV.setText(DateHelper.timeInMillsToFullString(reminderCalendar.getTimeInMillis()));
                                dialog.dismiss();
                                notifLayout.setVisibility(View.VISIBLE);
                                customNotification = true;
                                setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), false);

                            }
                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                        timePicker.show();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
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
                if (reminderCalendar != null) {
                    showToast("Date in the past");
                    reminderCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
                    reminderCalendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
                    reminderCalendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                    setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), true);
                    repeatSpinner.setSelection(Co.REMINDER_DAILY);
                    notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderCalendar.getTimeInMillis()));
                }
            } else if (DateUtils.isToday(selectedDateInMills)) {
                if (reminderCalendar != null) {
                    if (reminderCalendar.before(Calendar.getInstance())) {
                        setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), true);
                        notificationTV.setText(DateHelper.timeInMillsToTimeOnly(reminderCalendar.getTimeInMillis()));
                        repeatSpinner.setSelection(Co.REMINDER_DAILY);
                    } else {
                        setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), false);
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminderCalendar.getTimeInMillis()));
                    }
                }
            } else {
                if (reminderCalendar != null) {
                    if (!customNotification) {
                        reminderCalendar.set(Calendar.DAY_OF_MONTH, dateSet.get(Calendar.DAY_OF_MONTH));
                        reminderCalendar.set(Calendar.YEAR, dateSet.get(Calendar.YEAR));
                        reminderCalendar.set(Calendar.MONTH, dateSet.get(Calendar.MONTH));
                        notificationTV.setText(DateHelper.timeInMillsToFullString(reminderCalendar.getTimeInMillis()));
                        setRepeatSpinnerAdapter(reminderCalendar.getTimeInMillis(), false);
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
        switch (position) {


            case 0:
                selectedRepeatMode = Co.REMINDER_ONE_TIME;
                break;

            case 1:
                selectedRepeatMode = Co.REMINDER_DAILY;
//                if (reminderCalendar != null && (reminderCalendar.get(Calendar.HOUR) < hour ||
//                        (reminderCalendar.get(Calendar.HOUR) == hour &&
//                        reminderCalendar.get(Calendar.MINUTE) < minute))){
//
//                    reminderCalendar.add(Calendar.DATE, 1);
//                }
                break;

            case 2:
                selectedRepeatMode = Co.REMINDER_DAILY_WEEKDAYS;
//                if (reminderCalendar != null && (reminderCalendar.get(Calendar.HOUR) < hour ||
//                        (reminderCalendar.get(Calendar.HOUR) == hour &&
//                                reminderCalendar.get(Calendar.MINUTE) < minute))){
//
//                    reminderCalendar.add(Calendar.DATE, 1);
//                }
                break;

            case 3:
                selectedRepeatMode = Co.REMINDER_SAME_DAY_OF_WEEK;
//                if (reminderCalendar != null && (reminderCalendar.get(Calendar.HOUR) < hour ||
//                        (reminderCalendar.get(Calendar.HOUR) == hour &&
//                                reminderCalendar.get(Calendar.MINUTE) < minute))){
//
//                    reminderCalendar.add(Calendar.DATE, 7);
//                }
                break;

            case 4:
                selectedRepeatMode = Co.REMINDER_SAME_DAY_OF_MONTH;
//                if (reminderCalendar != null && (reminderCalendar.get(Calendar.HOUR) < hour ||
//                        (reminderCalendar.get(Calendar.HOUR) == hour &&
//                                reminderCalendar.get(Calendar.MINUTE) < minute))){
//
//                    reminderCalendar.add(Calendar.MONTH, 1);
//                }
                break;


        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedRepeatMode = Co.REMINDER_ONE_TIME;
    }
}

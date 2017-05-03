package com.adrapps.mytasks.views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewOrDetailActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        CompoundButton.OnCheckedChangeListener {

    EditText titleTV, notesTv;
    TextView dueDateTv;
    private long selectedDateInMills = 0;
    private Switch notSwitch;
    private TextView notInfo;
    private LinearLayout notifLayout;
    private RadioButton rbMorning;
    private RadioButton rbAfternoon;
    private RadioButton rbEvening;
    private RadioButton rbCustom;
    private long selectedReminderInMills;
    private LocalTask taskToEdit;
    private int position;
    private TextView customNotOption;
    ImageView clearDate, clearReminder;
    private AlertDialog dialog;
    private TextView notEdit;

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
        selectedReminderInMills = 0;
        selectedDateInMills = 0;
        titleTV = (EditText) findViewById(R.id.task_title_edit_text);
        dueDateTv = (TextView) findViewById(R.id.dueDateTv);
        notInfo = (TextView) findViewById(R.id.timeTv);
        notesTv = (EditText) findViewById(R.id.task_notes_edit_text);
        notSwitch = (Switch) findViewById(R.id.notification_switch);
        notifLayout = (LinearLayout) findViewById(R.id.notification_layout);
        clearDate = (ImageView) findViewById(R.id.clearDate);
        clearReminder = (ImageView) findViewById(R.id.clearReminder);
        notInfo.setOnClickListener(this);
        clearDate.setOnClickListener(this);
        clearReminder.setOnClickListener(this);
        dueDateTv.setOnClickListener(this);
        notSwitch.setOnCheckedChangeListener(this);
        dueDateTv.setOnClickListener(this);

        if (getIntent().hasExtra(Co.LOCAL_TASK)) {
            toolbar.setTitle(R.string.task_edit_title);
            taskToEdit = (LocalTask) getIntent().getExtras().getSerializable(Co.LOCAL_TASK);
            if (taskToEdit != null) {
                titleTV.setText(taskToEdit.getTitle());
                notesTv.setText(taskToEdit.getNotes());
                if (taskToEdit.getDue() != 0) {
                    dueDateTv.setText(DateHelper.timeInMillsToString(taskToEdit.getDue()));
                    selectedDateInMills = taskToEdit.getDue();
                    clearDate.setVisibility(View.VISIBLE);
                } else {
                    clearDate.setVisibility(View.GONE);
                }

                position = getIntent().getIntExtra(Co.ADAPTER_POSITION, -1);
                if (taskToEdit.getReminder() != 0) {
                    notSwitch.setOnCheckedChangeListener(null);
                    notSwitch.setChecked(true);
                    selectedReminderInMills = taskToEdit.getReminder();
                    notSwitch.setOnCheckedChangeListener(this);
                    notInfo.setText(DateHelper.timeInMillsToFullString(taskToEdit.getReminder()));
                    notifLayout.setVisibility(View.VISIBLE);
                }
            }

        } else {
            toolbar.setTitle(getString(R.string.new_task_title));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (notSwitch.isChecked()) {
            notifLayout.setVisibility(View.VISIBLE);
        } else {
            selectedReminderInMills = 0;
            notifLayout.setVisibility(View.GONE);
        }
    }

    private void showNotificationDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.notification_dialog, null);
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
                if (selectedDateInMills != 0) {
                    if (selectedReminderInMills == 0) {
                        notSwitch.setChecked(false);
                    }
                }
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

            case R.id.action_settings:
                break;

            case R.id.save_task:

                //TASK EDITED
                if (getIntent().hasExtra(Co.LOCAL_TASK)) {
                    Intent i = new Intent();
                    if (taskToEdit.getReminderId() != 0) {
                        if (selectedReminderInMills == 0) {
                            taskToEdit.setReminderNoID(0);
                        } else {
                            if (selectedReminderInMills != taskToEdit.getReminder()) {
                                taskToEdit.setReminderNoID(selectedReminderInMills);
                            }
                        }
                    } else {
                        if (selectedReminderInMills != 0){
                            taskToEdit.setReminder(selectedReminderInMills);
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
                    if (selectedReminderInMills != 0) {
                        task.setReminder(selectedReminderInMills);
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

        final Calendar cal = Calendar.getInstance();
        switch (v.getId()) {

            //Clear date click
            case R.id.clearDate:
                selectedDateInMills = 0;
                dueDateTv.setText(null);
                clearDate.setVisibility(View.GONE);
                break;

            //Clear reminder click
            case R.id.clearReminder:
                selectedReminderInMills = 0;
                notInfo.setText(null);
                clearReminder.setVisibility(View.GONE);
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
                rbMorning.setChecked(true);
                rbCustom.setChecked(false);
                rbEvening.setChecked(false);
                rbAfternoon.setChecked(false);
                if (selectedDateInMills != 0 || taskToEdit.getDue() != 0) {
                    cal.setTimeInMillis(selectedDateInMills == 0 ?
                            taskToEdit.getDue() : selectedDateInMills);
                    cal.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                    cal.set(Calendar.MINUTE, 0);
                    selectedReminderInMills = cal.getTimeInMillis();
                    notInfo.setText(DateHelper.timeInMillsToFullString(selectedReminderInMills));
                    dialog.dismiss();
                    clearReminder.setVisibility(View.VISIBLE);
                    break;
                } else {
                    showToast(getString(R.string.no_due_date_selected));
                    clearReminder.setVisibility(View.GONE);
                }
                break;

            //Afternoon reminder click
            case R.id.layout_afternoon:
                rbAfternoon.setChecked(true);
                rbMorning.setChecked(false);
                rbCustom.setChecked(false);
                rbEvening.setChecked(false);
                if (selectedDateInMills != 0 || taskToEdit.getDue() != 0) {
                    cal.setTimeInMillis(selectedDateInMills == 0 ?
                            taskToEdit.getDue() : selectedDateInMills);
                    cal.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                    cal.set(Calendar.MINUTE, 0);
                    selectedReminderInMills = cal.getTimeInMillis();
                    notInfo.setText(DateHelper.timeInMillsToFullString(selectedReminderInMills));
                    dialog.dismiss();
                    clearReminder.setVisibility(View.VISIBLE);
                    break;
                } else {
                    showToast(getString(R.string.no_due_date_selected));
                    clearReminder.setVisibility(View.GONE);
                    dialog.dismiss();
                }
                break;

            //Evening click
            case R.id.layout_evening:
                rbEvening.setChecked(true);
                rbMorning.setChecked(false);
                rbCustom.setChecked(false);
                rbAfternoon.setChecked(false);
                if (selectedDateInMills != 0 || taskToEdit.getDue() != 0) {
                    cal.setTimeInMillis(selectedDateInMills == 0 ?
                            taskToEdit.getDue() : selectedDateInMills);
                    cal.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                    cal.set(Calendar.MINUTE, 0);
                    selectedReminderInMills = cal.getTimeInMillis();
                    notInfo.setText(DateHelper.timeInMillsToFullString(selectedReminderInMills));
                    dialog.dismiss();
                    clearReminder.setVisibility(View.VISIBLE);
                    break;
                } else {
                    showToast(getString(R.string.no_due_date_selected));
                    clearReminder.setVisibility(View.GONE);
                    dialog.dismiss();
                }
                break;

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
                                NewOrDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                c.set(Calendar.MINUTE, minute);
                                selectedReminderInMills = c.getTimeInMillis();
                                customNotOption.setText(DateHelper.timeInMillsToFullString(selectedReminderInMills));
                                notInfo.setText(DateHelper.timeInMillsToFullString(selectedReminderInMills));
                                dialog.dismiss();
                            }
                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                        timePicker.show();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                clearReminder.setVisibility(View.VISIBLE);
                break;

            //Reminder textview click
            case R.id.timeTv:
                showNotificationDialog();

        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        selectedDateInMills = c.getTimeInMillis();
        SimpleDateFormat sdfCA = new SimpleDateFormat("d MMM yyyy HH:mm Z", Locale.getDefault());
        dueDateTv.setText(DateHelper.timeInMillsToString(selectedDateInMills));
        if (selectedDateInMills != 0) {
            clearDate.setVisibility(View.VISIBLE);
        } else {
            clearDate.setVisibility(View.GONE);
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}

package com.adrapps.mytasks.Views;

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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Helpers.DateHelper;
import com.adrapps.mytasks.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewOrDetailActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        CompoundButton.OnCheckedChangeListener {

    EditText taskTitle, taskNotes;
    TextView taskDue;
    private long selectedDateInMills = 0;
    private Switch notSwitch;
    private TextView notInfo;
    private LinearLayout notifLayout;
    private LinearLayout morningLayout;
    private LinearLayout afternoonLayout;
    private LinearLayout eveningLayout;
    private LinearLayout customLayout;
    private RadioButton rbMorning;
    private RadioButton rbAfternoon;
    private RadioButton rbEvening;
    private RadioButton rbCustom;
    private long selectedReminderInMills;
    private LocalTask taskToEdit;
    private int position;
    private TextView customNotOption;
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
        taskTitle = (EditText) findViewById(R.id.task_title_edit_text);
        taskDue = (TextView) findViewById(R.id.due_date_picker);
        notInfo = (TextView) findViewById(R.id.timeTv);
        taskNotes = (EditText) findViewById(R.id.task_notes_edit_text);
        notSwitch = (Switch) findViewById(R.id.notification_switch);
        notifLayout = (LinearLayout) findViewById(R.id.notification_layout);
        notInfo.setOnClickListener(this);
        notSwitch.setOnCheckedChangeListener(this);
        taskDue.setOnClickListener(this);

        if (getIntent().hasExtra(Co.LOCAL_TASK)) {
            toolbar.setTitle(R.string.task_edit_title);
            taskToEdit = (LocalTask) getIntent().getExtras().getSerializable(Co.LOCAL_TASK);
            if (taskToEdit != null) {
                taskTitle.setText(taskToEdit.getTitle());
                taskNotes.setText(taskToEdit.getNotes());
                if (taskToEdit.getDue() != 0) {
                    taskDue.setText(DateHelper.timeInMillsToString(taskToEdit.getDue()));
                    selectedDateInMills = taskToEdit.getDue();
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
            showNotificationDialog();
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
        morningLayout = (LinearLayout) dialogView.findViewById(R.id.layout_morning);
        afternoonLayout = (LinearLayout) dialogView.findViewById(R.id.layout_afternoon);
        eveningLayout = (LinearLayout) dialogView.findViewById(R.id.layout_evening);
        customLayout = (LinearLayout) dialogView.findViewById(R.id.layout_custom);
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
                    if (selectedReminderInMills == 0 && taskToEdit.getReminder() != 0) {
                        //clear alarm
                        taskToEdit.setReminderNoID(0);
                    }
                    if (selectedReminderInMills != 0 &&
                            selectedReminderInMills != taskToEdit.getReminder()) {
                        if (taskToEdit.getReminder() != 0) {
                            taskToEdit.setReminderNoID(selectedReminderInMills);
                            //Update alarm
                        } else {
                            taskToEdit.setReminder(selectedReminderInMills);
                            //update alarm
                        }
                    }
                    taskToEdit.setTitle(taskTitle.getText().toString());
                    if (taskNotes.getText().toString().trim().length() != 0)
                        taskToEdit.setNotes(taskNotes.getText().toString());
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
                    if (taskTitle.getText().toString().trim().length() == 0) {
                        showToast(getString(R.string.empty_title_error));
                        break;
                    }
                    Intent i = new Intent();
                    LocalTask task = new LocalTask(taskTitle.getText().toString(),
                            selectedDateInMills);
                    if (taskNotes.getText().toString().trim().length() != 0)
                        task.setNotes(taskNotes.getText().toString());
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
            case R.id.due_date_picker:
                DatePickerDialog datePicker = new DatePickerDialog(this, this,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
                break;

            case (R.id.layout_morning):
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
                    break;
                } else {
                    showToast(getString(R.string.no_due_date_selected));
                    notSwitch.setChecked(false);
                }
                break;

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
                    break;
                } else {
                    showToast(getString(R.string.no_due_date_selected));
                    dialog.dismiss();
                    notSwitch.setChecked(false);
                }
                break;

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
                    break;
                } else {
                    showToast(getString(R.string.no_due_date_selected));
                    dialog.dismiss();
                    notSwitch.setChecked(false);
                }
                break;

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
                break;
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
        taskDue.setText(DateHelper.timeInMillsToString(selectedDateInMills));
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

package com.adrapps.mytasks.Views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    private LocalTask task;
    private int position;
    private TextView customNotOption;
    private Button notButton;
    private AlertDialog dialog;
    private ImageView notEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.task_edit_title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        taskTitle = (EditText) findViewById(R.id.task_title_edit_text);
        taskDue = (TextView) findViewById(R.id.due_date_picker);
        notInfo = (TextView) findViewById(R.id.notification_info);
        taskNotes = (EditText) findViewById(R.id.task_notes_edit_text);
        notSwitch = (Switch) findViewById(R.id.notification_switch);
        notEdit = (ImageView) findViewById(R.id.notif_edit);
        notifLayout = (LinearLayout) findViewById(R.id.notification_layout);
        notEdit.setOnClickListener(this);
        notSwitch.setOnCheckedChangeListener(this);

        if (getIntent().hasExtra(Co.LOCAL_TASK)) {
            task = (LocalTask) getIntent().getExtras().getSerializable(Co.LOCAL_TASK);
            if (task != null) {
                taskTitle.setText(task.getTitle());
                taskNotes.setText(task.getNotes());
                if (task.getDue() != 0)
                    taskDue.setText(DateHelper.timeInMillsToString(task.getDue()));
                position = getIntent().getIntExtra(Co.ADAPTER_POSITION, -1);
                if (task.getReminder() != 0) {
                    notSwitch.setOnCheckedChangeListener(null);
                    notSwitch.setChecked(true);
                    notSwitch.setOnCheckedChangeListener(this);
                    notInfo.setText(DateHelper.timeInMillsToFullString(task.getReminder()));
                    notifLayout.setVisibility(View.VISIBLE);
                }
            }

        }
        taskDue.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (notSwitch.isChecked()) {
            showNotificationDialog();
            notifLayout.setVisibility(View.VISIBLE);
        } else
            notifLayout.setVisibility(View.GONE);
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
        notButton = (Button) dialogView.findViewById(R.id.notification_button);
        notButton.setOnClickListener(this);
        morningLayout.setOnClickListener(this);
        afternoonLayout.setOnClickListener(this);
        eveningLayout.setOnClickListener(this);
        customLayout.setOnClickListener(this);
        customNotOption = (TextView) dialogView.findViewById(R.id.custom_notification_option);
        dialog = dialogBuilder.create();
        dialog.setTitle(getString(R.string.notification_dialog_title));
        dialog.show();

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
                if (getIntent().hasExtra(Co.LOCAL_TASK)) {
                    if (isDeviceOnline()) {
                        Intent i = new Intent();
                        task.setReminder(selectedReminderInMills);
                        task.setTitle(taskTitle.getText().toString());
                        if (taskNotes.getText().toString().trim().length() != 0)
                            task.setNotes(taskNotes.getText().toString());
                        if (selectedDateInMills != 0)
                            task.setDue(selectedDateInMills);
                        i.putExtra(Co.TASK_EDIT, true);
                        i.putExtra(Co.LOCAL_TASK, task);
                        i.putExtra(Co.ADAPTER_POSITION, position);
                        setResult(Activity.RESULT_OK, i);
                        finish();
                    } else {
                        showToast(getString(R.string.no_internet_toast));
                    }
                    break;
                } else {
                    if (taskTitle.getText().toString().trim().length() == 0) {
                        showToast(getString(R.string.empty_title_error));
                        break;
                    }
                    if (isDeviceOnline()) {
                        Intent i = new Intent();
                        LocalTask task = new LocalTask(taskTitle.getText().toString(),
                                selectedDateInMills);
                        if (taskNotes.getText().toString().trim().length() != 0)
                            task.setNotes(taskNotes.getText().toString());
                        if (selectedReminderInMills != 0)
                            task.setReminder(selectedReminderInMills);
                        i.putExtra(Co.LOCAL_TASK, task);
                        i.putExtra(Co.NEW_TASK, true);
                        setResult(Activity.RESULT_OK, i);
                        finish();
                    } else {
                        showToast(getString(R.string.no_internet_toast));
                        break;
                    }

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
                if (selectedDateInMills != 0 || task.getDue() != 0) {
                    cal.setTimeInMillis(selectedDateInMills == 0 ?
                            task.getDue() : selectedDateInMills);
                    cal.set(Calendar.HOUR_OF_DAY, Co.MORNING_ALARM_HOUR);
                    cal.set(Calendar.MINUTE, 0);
                    selectedReminderInMills = cal.getTimeInMillis();
                    notInfo.setText(DateHelper.timeInMillsToFullString(selectedReminderInMills));
                    dialog.dismiss();
                    break;
                } else {
                    showToast(getString(R.string.no_due_date_selected));
                }
                break;

            case R.id.layout_afternoon:
                rbAfternoon.setChecked(true);
                rbMorning.setChecked(false);
                rbCustom.setChecked(false);
                rbEvening.setChecked(false);
                if (selectedDateInMills != 0 || task.getDue() != 0) {
                    cal.setTimeInMillis(selectedDateInMills == 0 ?
                            task.getDue() : selectedDateInMills);
                    cal.set(Calendar.HOUR_OF_DAY, Co.AFTERNOON_ALARM_HOUR);
                    cal.set(Calendar.MINUTE, 0);
                    selectedReminderInMills = cal.getTimeInMillis();
                    notInfo.setText(DateHelper.timeInMillsToFullString(selectedReminderInMills));
                    dialog.dismiss();
                    break;
                } else {
                    showToast(getString(R.string.no_due_date_selected));
                }
                break;

            case R.id.layout_evening:
                rbEvening.setChecked(true);
                rbMorning.setChecked(false);
                rbCustom.setChecked(false);
                rbAfternoon.setChecked(false);
                if (selectedDateInMills != 0 || task.getDue() != 0) {
                    cal.setTimeInMillis(selectedDateInMills == 0 ?
                            task.getDue() : selectedDateInMills);
                    cal.set(Calendar.HOUR_OF_DAY, Co.EVENING_ALARM_HOUR);
                    cal.set(Calendar.MINUTE, 0);
                    selectedReminderInMills = cal.getTimeInMillis();
                    notInfo.setText(DateHelper.timeInMillsToFullString(selectedReminderInMills));
                    dialog.dismiss();
                    break;
                } else {
                    showToast(getString(R.string.no_due_date_selected));
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
                            }
                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                        timePicker.show();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;

            case R.id.notification_button:
                if (rbCustom.isChecked()) {
                    dialog.dismiss();
                    break;
                } else {
                    showToast(getString(R.string.nothing_selected));
                    notSwitch.setChecked(false);
                    dialog.dismiss();
                    break;
                }

            case R.id.notif_edit:
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
        Log.d("SelectedDueFromPicker",String.valueOf(selectedDateInMills) + "\n" +
                DateHelper.timeInMillsToFullString(selectedDateInMills) + "\n" +
                sdfCA.format(selectedDateInMills));
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

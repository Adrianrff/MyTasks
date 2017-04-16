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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Helpers.DateHelper;
import com.adrapps.mytasks.R;

import java.util.Calendar;

public class TaskDetailActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        CompoundButton.OnCheckedChangeListener{

    EditText taskTitle, taskNotes;
    TextView taskDue;
    private long selectedDateInMills = 0;
    private Spinner spinner;
    private Switch notSwitch;
    String taskId, listId;
    private long reminder;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail2);
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
        spinner = (Spinner) findViewById(R.id.notification_spinner);
        notSwitch = (Switch) findViewById(R.id.notification_switch);
        notifLayout = (LinearLayout) findViewById(R.id.notification_layout);
        notSwitch.setOnCheckedChangeListener(this);
        if (getIntent().hasExtra(Co.TASK_TITLE)){
            taskTitle.setText(getIntent().getStringExtra(Co.TASK_TITLE));
            taskNotes.setText(getIntent().getStringExtra(Co.DETAIL_TASK_NOTE));
            taskDue.setText(getIntent().getStringExtra(Co.DETAIL_TASK_DUE));
            taskId = getIntent().getStringExtra(Co.DETAIL_TASK_ID);
            listId = getIntent().getStringExtra(Co.DETAIL_TASK_LIST_ID);
            reminder = getIntent().getLongExtra(Co.TASK_REMINDER,0);
        }
        taskDue.setOnClickListener(this);

        if (reminder != 0){
            notSwitch.setOnCheckedChangeListener(null);
            notSwitch.setChecked(true);
            notSwitch.setOnCheckedChangeListener(this);
            notInfo.setText(DateHelper.timeInMillsToFullString(reminder));
            notifLayout.setVisibility(View.VISIBLE);
        }
//
//        SimpleDateFormat sdfCA= new SimpleDateFormat("d MMM yyyy HH:mm Z", Locale.getDefault());
//        Toast.makeText(this,"Updated: "  +
//                sdfCA.format(getIntent().getLongExtra("updated", 0)) + "\n" +
//                "Completed: " + sdfCA.format(getIntent().getLongExtra("completed", 0)) + "\n" +
//                "Due: " +     sdfCA.format(getIntent().getLongExtra("due", 0)),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (notSwitch.isChecked()) {
            showNotificationDialog();
            notifLayout.setVisibility(View.VISIBLE);
        }
        else
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
        morningLayout.setOnClickListener(this);
        afternoonLayout.setOnClickListener(this);
        eveningLayout.setOnClickListener(this);
        customLayout.setOnClickListener(this);
        AlertDialog dialog = dialogBuilder.create();
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
                if (isDeviceOnline()) {
                    Intent i = new Intent();
                    i.putExtra(Co.TASK_EDITED_TITLE, taskTitle.getText().toString());
                    i.putExtra(Co.TASK_EDITED_NOTE, taskNotes.getText().toString());
                    i.putExtra(Co.TASK_DUE, selectedDateInMills);
                    i.putExtra(Co.TASK_REMINDER, selectedReminderInMills);
                    i.putExtra(Co.DETAIL_TASK_ID, taskId);
                    setResult(Activity.RESULT_OK, i);
                    finish();
                } else {
                    showToast(getString(R.string.no_internet_toast));
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.due_date_picker:
                Calendar c = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(this, this,
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
                break;

            case (R.id.layout_morning):
                rbMorning.setChecked(true);
                rbCustom.setChecked(false);
                rbEvening.setChecked(false);
                rbAfternoon.setChecked(false);

                break;

            case R.id.layout_afternoon:
                rbAfternoon.setChecked(true);
                rbMorning.setChecked(false);
                rbCustom.setChecked(false);
                rbEvening.setChecked(false);

                break;
            case R.id.layout_evening:
                rbEvening.setChecked(true);
                rbMorning.setChecked(false);
                rbCustom.setChecked(false);
                rbAfternoon.setChecked(false);
                break;
            case R.id.layout_custom:
                rbCustom.setChecked(true);
                rbMorning.setChecked(false);
                rbAfternoon.setChecked(false);
                rbEvening.setChecked(false);
                selectedReminderInMills = 0;
                Calendar c1 = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        final Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, month);
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        TimePickerDialog timePicker = new TimePickerDialog(TaskDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                c.set(Calendar.MINUTE, minute);
                                selectedReminderInMills = c.getTimeInMillis();
                                notInfo.setText(DateHelper.timeInMillsToFullString(selectedReminderInMills));

                            }
                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                        timePicker.show();
                    }
                },
                        c1.get(Calendar.YEAR),
                        c1.get(Calendar.MONTH),
                        c1.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
        }

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        selectedDateInMills = c.getTimeInMillis();
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

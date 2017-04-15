package com.adrapps.mytasks.Views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Helpers.DateHelper;
import com.adrapps.mytasks.R;

import java.util.Calendar;

public class TaskDetailActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener{

    EditText taskTitle, taskNotes;
    TextView taskDue;
    private long selectedDateInMills = 0;
    private Spinner spinner;
    private Switch notSwitch;
    String taskId, listId;

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
        taskNotes = (EditText) findViewById(R.id.task_notes_edit_text);
        spinner = (Spinner) findViewById(R.id.notification_spinner);
        notSwitch = (Switch) findViewById(R.id.notification_switch);
        notSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (notSwitch.isChecked())
                    spinner.setVisibility(View.VISIBLE);
                else
                    spinner.setVisibility(View.GONE);
            }
        });
        if (getIntent().hasExtra(Co.DETAIL_TASK_TITLE)){
            taskTitle.setText(getIntent().getStringExtra(Co.DETAIL_TASK_TITLE));
            taskNotes.setText(getIntent().getStringExtra(Co.DETAIL_TASK_NOTE));
            taskDue.setText(getIntent().getStringExtra(Co.DETAIL_TASK_DUE));
            taskId = getIntent().getStringExtra(Co.DETAIL_TASK_ID);
            listId = getIntent().getStringExtra(Co.DETAIL_TASK_LIST_ID);
        }
        taskDue.setOnClickListener(this);
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
                Intent i = new Intent();
                i.putExtra(Co.TASK_EDITED_TITLE, taskTitle.getText().toString());
                i.putExtra(Co.TASK_EDITED_NOTE, taskNotes.getText().toString());
                i.putExtra(Co.TASK_EDITED_DUE, selectedDateInMills);
                i.putExtra(Co.DETAIL_TASK_ID, taskId);
                setResult(Activity.RESULT_OK,i);
                finish();
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
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        selectedDateInMills = c.getTimeInMillis();
        taskDue.setText(DateHelper.timeInMillsToString(selectedDateInMills));
    }
}

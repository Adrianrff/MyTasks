package com.adrapps.mytasks.Views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.R;


/**
 * Created by Adrian Flores on 8/4/2017.
 */

public class TaskDetailActivity extends AppCompatActivity {

    TextView taskTitle, taskDue, taskNotes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.task_detail_fragment);
        taskTitle = (TextView) findViewById(R.id.task_title_content);
        taskDue = (TextView) findViewById(R.id.task_due_content);
        taskNotes = (TextView) findViewById(R.id.task_notes_content);
        if (getIntent().hasExtra(Co.DETAIL_TASK_TITLE)){
            taskTitle.setText(getIntent().getStringExtra(Co.DETAIL_TASK_TITLE));
            taskNotes.setText(getIntent().getStringExtra(Co.DETAIL_TASK_NOTE));
            taskDue.setText(getIntent().getStringExtra(Co.DETAIL_TASK_DUE));
        }

    }
}

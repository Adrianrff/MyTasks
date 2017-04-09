package com.adrapps.mytasks.Views;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adrapps.mytasks.R;

/**
 * Created by Adrian Flores on 8/4/2017.
 */

public class TaskDetailFragment extends Fragment {

    public static final String TASK_TITLE = "item_id";
    public static final String TASK_DUE = "task due date";
    public static String TASK_NOTES = "task notes";
    String taskTitle;
    String taskDueDate;
    String taskNotes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(TASK_TITLE)) {
            Activity activity = this.getActivity();
            taskTitle = getArguments().getString(TASK_TITLE);
            taskDueDate = getArguments().getString(TASK_DUE);
            taskNotes = getArguments().getString(TASK_NOTES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.task_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (taskTitle != null) {
            ((TextView) rootView.findViewById(R.id.task_title_content)).setText(taskTitle);
            ((TextView) rootView.findViewById(R.id.task_due_content)).setText(taskDueDate);
            ((TextView) rootView.findViewById(R.id.task_notes_content)).setText(taskNotes);
        }

        return rootView;
    }
}

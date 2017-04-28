package com.adrapps.mytasks.views;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.adrapps.mytasks.R;

public class DetailDialogFragment extends android.support.v4.app.DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        String title = args.getString("title");
        View v = inflater.inflate(R.layout.task_detail, container, false);
        TextView titleTv = (TextView) v.findViewById(R.id.title);
        TextView notesTv = (TextView) v.findViewById(R.id.notes);
        TextView dateTv = (TextView) v.findViewById(R.id.date);
        TextView notificationTv = (TextView) v.findViewById(R.id.notificationTextView);
        TextView reminderTimeTv = (TextView) v.findViewById(R.id.timeTv);
        TextView repeadTv = (TextView) v.findViewById(R.id.repeatTv);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                return true;
            }
        });
        toolbar.inflateMenu(R.menu.task_detail_menu);

        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

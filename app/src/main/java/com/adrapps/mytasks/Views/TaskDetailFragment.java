package com.adrapps.mytasks.Views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adrapps.mytasks.R;


/**
 * Created by Adrian Flores on 8/4/2017.
 */

public class TaskDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_detail,container, false);
        return view;
    }
}

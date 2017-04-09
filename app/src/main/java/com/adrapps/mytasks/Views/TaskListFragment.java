package com.adrapps.mytasks.Views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.adrapps.mytasks.R;

import java.util.List;

public class TaskListFragment extends Fragment implements Contract.AdapterOps {

    RecyclerView recyclerView;
    TaskListAdapter adapter;
    TaskListPresenter mPresenter;
    private List<String> taskListsIds;
    private List<String> taskListsTitles;
    Contract.MainActivityViewOps mainOps;

    public TaskListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mainOps = (Contract.MainActivityViewOps) getActivity();
        mPresenter = new TaskListPresenter(mainOps, this);
        mPresenter.setAdapterOps(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("onCreateView","run");
        return inflater.inflate(R.layout.task_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d("onViewCreated","run");
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        initRecyclerView(mPresenter.getTasksFromList(getStringSharedPreference("@default")));
    }

    private void findViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
    }

    public void initRecyclerView(List<LocalTask> tasks) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TaskListAdapter(getContext(), tasks, mPresenter);
        recyclerView.setAdapter(adapter);
    }

    public void setNavDrawerMenu() {
        mPresenter.setNavDrawerMenu();
    }

    @Override
    public void setListsIds(List<String> listIds) {
        taskListsIds = mPresenter.getListsIds();
    }

    @Override
    public void updateAdapterItems(List<LocalTask> localTasks) {
        adapter.updateItems(localTasks);
    }

    public String getStringSharedPreference(String key) {
        if (key.equals(Co.CURRENT_LIST_ID))
            return "@default";
        else
            return mPresenter.getStringSharedPreference(key);
    }

    public boolean getBooleanSharedPreference(String key) {
        return mPresenter.getBooleanSharedPreference(key);
    }

    public void saveStringSharedPreference(String key, String value) {
        mPresenter.saveStringSharedPreference(key,value);
    }

    public void setToolbarTitle(String title) {
        mPresenter.setToolbarTitle(title);
    }

    @Override
    public void updateCurrentView() {
        taskListsIds = mPresenter.getListsIds();
        taskListsTitles = mPresenter.getListsTitles();
        saveStringSharedPreference(Co.CURRENT_LIST_TITLE,
                mPresenter.getListTitleFromId(getStringSharedPreference(Co.CURRENT_LIST_ID)));
        mPresenter.setToolbarTitle(getStringSharedPreference(Co.CURRENT_LIST_TITLE));
        mPresenter.setNavDrawerMenu();
        adapter.updateItems(mPresenter.getTasksFromList(getStringSharedPreference(Co.CURRENT_LIST_ID)));
    }

    @Override
    public void setTaskListsTitles(List<String> titles) {

    }

    public void showProgressDialog() {
        mPresenter.showProgressDialog();
    }

    public void dismissProgressDialog() {
        mPresenter.dismissProgressDialog();
    }

}

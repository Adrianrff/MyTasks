package com.adrapps.mytasks.Views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.adrapps.mytasks.R;

import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment implements Contract.AdapterOps {

    RecyclerView recyclerView;
    TaskListAdapter adapter;
    TaskListPresenter mPresenter;
    private List<String> taskListsIds = new ArrayList<>();
    private List<String> listTitles = new ArrayList<>();
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
        return inflater.inflate(R.layout.task_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        initRecyclerView(mPresenter.getTasksFromList(getStringSharedPreference(Co.CURRENT_LIST_ID)));
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

    @Override
    public List<String> getListIds() {
        return taskListsIds;
    }

    @Override
    public List<String> getListTitles() {
        return listTitles;
    }

    @Override
    public void setListsIds(List<String> listIds) {
        if (!listIds.isEmpty())
            for (int i = 0; i < listIds.size(); i++) {
                taskListsIds.add(listIds.get(i));
            }
    }

    @Override
    public void setListsTitles(List<String> titles) {
        if (!titles.isEmpty())
            for (int i = 0; i < titles.size(); i++) {
                listTitles.add(titles.get(i));
            }
    }

    @Override
    public void updateAdapterItems(List<LocalTask> localTasks) {
        adapter.updateItems(localTasks);
    }

    public String getStringSharedPreference(String key) {
        if (key.equals(Co.CURRENT_LIST_ID)) {
            if (mPresenter.getStringSharedPreference(key).equals(Co.NO_VALUE)){
                return "@default";
            }
        }
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
        listTitles = mPresenter.getListsTitles();
        saveStringSharedPreference(Co.CURRENT_LIST_TITLE,
                mPresenter.getListTitleFromId(getStringSharedPreference(Co.CURRENT_LIST_ID)));
        mPresenter.setToolbarTitle(getStringSharedPreference(Co.CURRENT_LIST_TITLE));
        mPresenter.setNavDrawerMenu(listTitles);
        adapter.updateItems(mPresenter.getTasksFromList(getStringSharedPreference(Co.CURRENT_LIST_ID)));
    }

}

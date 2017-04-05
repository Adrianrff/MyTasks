package com.adrapps.mytasks.Views;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.adrapps.mytasks.APICalls.FirstRefreshAsync;
import com.adrapps.mytasks.APICalls.RefreshAllAsync;
import com.adrapps.mytasks.APICalls.SignInActivity;
import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.adrapps.mytasks.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        Contract.View, MenuItem.OnMenuItemClickListener, View.OnClickListener {

    Toolbar toolbar;
    DrawerLayout drawer;
    RecyclerView recyclerView;
    NavigationView navigationView;
    FloatingActionButton fab;
    TaskListAdapter adapter;
    ProgressBar progressBar;
    EditText taskTitle,taskDueDate;
    AppCompatSpinner notificationSpinner;
    TaskListPresenter mPresenter;
    GoogleAccountCredential mCredential;
    List<String> taskListsTitles = new ArrayList<>();
    List<String> taskListsIds = new ArrayList<>();
    String accountName;
    private ConstraintLayout newTaskLayout;
    private AppCompatSpinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getBooleanSharedPreference(Co.IS_FIRST_TIME)) {
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
            finish();
            return;
        }
        setContentView(R.layout.navigation_drawer_content);
        findViews();
        mPresenter = new TaskListPresenter(this);
        setCredentials();
        if (getIntent().hasExtra(Co.IS_FIRST_INIT)) {
            refreshFirstTime();
            return;

        }
        taskListsTitles = mPresenter.getListsTitles();
        taskListsIds = mPresenter.getListsIds();
        setUpViewsAndData();
        initRecyclerView(mPresenter.getTasksFromList(getStringSharedPreference(Co.CURRENT_LIST_ID)));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void refreshFirstTime() {
        FirstRefreshAsync firstRefresh = new FirstRefreshAsync(mPresenter, mCredential);
        firstRefresh.execute();
    }

    private void findViews() {
        taskTitle = (EditText) findViewById(R.id.title_edit_text);
        taskDueDate = (EditText) findViewById(R.id.date_edit_text);
        notificationSpinner = (AppCompatSpinner) findViewById(R.id.notification_spinner);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        newTaskLayout = (ConstraintLayout) findViewById(R.id.new_task_layout);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void setUpViewsAndData() {
        taskListsTitles = mPresenter.getListsTitles();
        taskListsIds = mPresenter.getListsIds();
        toolbar.setTitle(getStringSharedPreference(Co.CURRENT_LIST_TITLE));
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
        fab.setOnClickListener(this);
        setNavDrawerMenu();
    }

    public void setNavDrawerMenu(){
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(R.id.lists_titles_menu);
        SubMenu listsMenu = item.getSubMenu();
        listsMenu.clear();
        for (int i = 0; i < taskListsTitles.size(); i++) {
            listsMenu.add(0, i, i, taskListsTitles.get(i)).setIcon(R.drawable.ic_list).
                    setOnMenuItemClickListener(this);
        }
    }

    @Override
    public void initRecyclerView(List<LocalTask> tasks) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TaskListAdapter(this, tasks);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateCurrentView() {
        taskListsIds = mPresenter.getListsIds();
        taskListsTitles = mPresenter.getListsTitles();
        saveStringSharedPreference(Co.CURRENT_LIST_TITLE,
                mPresenter.getListTitleFromId(getStringSharedPreference(Co.CURRENT_LIST_ID)));
        toolbar.setTitle(getStringSharedPreference(Co.CURRENT_LIST_TITLE));
        setNavDrawerMenu();
        adapter.updateItems(mPresenter.getTasksFromList(getStringSharedPreference(Co.CURRENT_LIST_ID)));

    }

    @Override
    public void expandNewTaskLayout() {
        showNewTaskDialog();
//        newTaskLayout.setVisibility(View.VISIBLE);
//        AppBarLayout.LayoutParams params = new AppBarLayout.LayoutParams(
//                AppBarLayout.LayoutParams.MATCH_PARENT,
//                AppBarLayout.LayoutParams.WRAP_CONTENT);
//        newTaskLayout.setLayoutParams(params);
    }

    private void setCredentials() {
        mCredential = getCredential();
        accountName = getStringSharedPreference(Co.PREF_ACCOUNT_NAME);
        if (!accountName.equals(Co.NO_ACCOUNT_NAME)) {
            mCredential.setSelectedAccountName(accountName);
        }
    }

    @Override
    public void setTaskListsTitles(List<String> titles) {
        if (!titles.isEmpty())
            for (int i = 0; i < titles.size(); i++) {
                taskListsTitles.add(titles.get(i));
            }
    }

    @Override
    public void setListsIds(List<String> listIds){
        if (!listIds.isEmpty())
            for (int i = 0; i < listIds.size(); i++) {
                taskListsIds.add(listIds.get(i));
            }

    }
    private void refresh() {
        RefreshAllAsync refresh = new RefreshAllAsync(mPresenter, mCredential);
        refresh.execute();
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (newTaskLayout.getVisibility() == View.VISIBLE){
            collapseNewTaskLayout();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void showNewTaskDialog(){

//        final Dialog dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.new_task_dialog);
//        dialog.setCancelable(true);dialog.show();
//
//        dialog.getWindow().setGravity(Gravity.TOP);
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.new_task_dialog,null);
        EditText newTaskTitle = (EditText) v.findViewById(R.id.newTaskTitleInput);
        builder.setView(v);
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.TOP);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
            window.setAttributes(lp);
        }
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


    }

    public void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
    @Override
    public void collapseNewTaskLayout() {
        newTaskLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent i = new Intent(this, SignInActivity.class);
                startActivity(i);
                break;

            case R.id.refresh:
                refresh();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onClick(View v) {
        mPresenter.onClick(v.getId());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        saveStringSharedPreference(Co.CURRENT_LIST_ID, taskListsIds.get(item.getItemId()));
        saveStringSharedPreference(Co.CURRENT_LIST_TITLE, taskListsTitles.get(item.getItemId()));
        adapter.updateItems(mPresenter.getTasksFromList(taskListsIds.get(item.getItemId())));
        toolbar.setTitle(item.getTitle());
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public Context getAppContext() {
        return getApplicationContext();
    }

    @Override
    public void showProgress(boolean b) {
        if (b)
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void requestAuthorization(Exception e) {
        startActivityForResult(
                ((UserRecoverableAuthIOException) e).getIntent(),
                Co.REQUEST_AUTHORIZATION);
    }

    @Override
    public void updateAdapterItems(List<LocalTask> localTasks) {
        adapter.updateItems(localTasks);
    }


    public GoogleAccountCredential getCredential() {
        return GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Co.SCOPES))
                .setBackOff(new ExponentialBackOff());

    }

    @Override
    public String getStringSharedPreference(String key) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        return prefs.getString(key, "No value");
    }

    @Override
    public boolean getBooleanSharedPreference(String key) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean(key, true);
    }

    @Override
    public void saveStringSharedPreference(String key, String value) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }


}

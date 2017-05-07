package com.adrapps.mytasks.views;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adrapps.mytasks.AlarmReciever;
import com.adrapps.mytasks.R;
import com.adrapps.mytasks.api_calls.SignInActivity;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.SimpleItemTouchHelperCallback;
import com.adrapps.mytasks.interfaces.Contract;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.bumptech.glide.Glide;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        Contract.MainActivityViewOps, MenuItem.OnMenuItemClickListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    FloatingActionButton fab;
    ProgressDialog mProgress;
    ProgressBar progressBar;
    TaskListPresenter mPresenter;
    GoogleAccountCredential mCredential;
    String accountName;
    RecyclerView recyclerView;
    TaskListAdapter adapter;
    ActionBarDrawerToggle toggle;
    CoordinatorLayout coordinatorLayout;
    SwipeRefreshLayout swipeRefresh;
    TextView dateTextView;
    Dialog newListDialog;
    long selectedDueDateInMills;
    private LinearLayout emptyDataLayout;
    private long selectedReminderInMills;
    private LinearLayout noInternetLayout;
    private View headerView;
    private ImageView profilePic;
    private TextView userName;
    private TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getBooleanShP(Co.IS_FIRST_LAUNCH)) {
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
            finish();
            return;
        }
        setContentView(R.layout.main_activity);
        mPresenter = new TaskListPresenter(this);
        findViews();
        setUpViews();
        setCredentials();
        if (getBooleanShP(Co.IS_FIRST_INIT)) {
            refreshFirstTime();
            return;
        }
        initRecyclerView(mPresenter.getTasksFromListForAdapter(getStringShP(Co.CURRENT_LIST_ID)));
        setListsData();
        if (getIntent().hasExtra(Co.LOCAL_TASK)) {
            LocalTask task = (LocalTask) getIntent().getSerializableExtra(Co.LOCAL_TASK);
            Intent i = new Intent(this, NewOrDetailActivity.class);
            i.putExtra(Co.LOCAL_TASK, task);
            startActivityForResult(i, Co.TASK_DATA_REQUEST_CODE);
        }

        if (!isDeviceOnline()) {
            showNoInternetWarning(true);
        }

    }


    //-------------------------VIEWS AND DATA------------------------///

    private void findViews() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        profilePic = (ImageView) headerView.findViewById(R.id.profilePic);
        userName = (TextView) headerView.findViewById(R.id.userName);
        userEmail = (TextView) headerView.findViewById(R.id.userEmail);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        emptyDataLayout = (LinearLayout) findViewById(R.id.empty_data_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        noInternetLayout = (LinearLayout) findViewById(R.id.noInternetLayout);
    }

    @Override
    public void setUpViews() {
        mProgress = new ProgressDialog(this);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setToolbarNavigationClickListener(this);
        toggle.syncState();
        Glide.with(this).load(getStringShP(Co.USER_PIC_URL)).into(profilePic);
        userName.setText(getStringShP(Co.USER_NAME));
        userEmail.setText(getStringShP(Co.USER_EMAIL));
        navigationView.setNavigationItemSelectedListener(this);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
        fab.setOnClickListener(this);
        swipeRefresh.setOnRefreshListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fab.isShown()) {
                    fab.hide();
                    swipeRefresh.setEnabled(false);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show();
                    swipeRefresh.setEnabled(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    @Override
    public void setListsData() {
        Co.setListIds(mPresenter.getListsIds());
        Co.setListTitles(mPresenter.getListsTitles());
        setNavDrawerMenu(Co.listTitles);
    }

    @Override
    public void refreshFirstTime() {
        mPresenter.refreshFirstTime();
    }

    @Override
    public void refresh() {
        mPresenter.refresh();
    }

    @Override
    public void updateCurrentView() {
        Co.setListIds(mPresenter.getListsIds());
        Co.setListTitles(mPresenter.getListsTitles());
        boolean listStillExists = Co.listIds.contains(getStringShP(Co.CURRENT_LIST_ID));
        if (listStillExists) {
            String currentListTitle = mPresenter.getListTitleById(
                    getStringShP(Co.CURRENT_LIST_ID));
            saveStringShP(Co.CURRENT_LIST_TITLE, currentListTitle);
            mPresenter.setToolbarTitle(currentListTitle);
        } else {
            String currentListTitle = Co.listTitles.get(0);
            String currentListId = Co.listIds.get(0);
            saveStringShP(Co.CURRENT_LIST_ID, currentListId);
            saveStringShP(Co.CURRENT_LIST_TITLE, currentListTitle);
            toolbar.setTitle(currentListTitle);
        }
        setNavDrawerMenu(Co.listTitles);
        List<LocalTask> tasks = mPresenter.getTasksFromListForAdapter(getStringShP(Co.CURRENT_LIST_ID));
        showEmptyRecyclerView(tasks == null || tasks.isEmpty());
        adapter.updateItems(tasks);

    }

    @Override
    public void initRecyclerView(List<LocalTask> tasks) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TaskListAdapter(getContext(), tasks, mPresenter);
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter, this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        if (tasks == null || tasks.isEmpty()) {
            showEmptyRecyclerView(true);
        } else {
            showEmptyRecyclerView(false);
        }
    }

    @Override
    public void showEmptyRecyclerView(boolean b) {
        if (b) {
            recyclerView.setVisibility(View.GONE);
            emptyDataLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyDataLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showNoInternetWarning(boolean b) {
        if (b) {
            noInternetLayout.setVisibility(View.VISIBLE);
        } else {
            noInternetLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void showProgressDialog() {
        mProgress.setMessage(getString(R.string.please_wait));
        mProgress.show();
    }

    @Override
    public void dismissProgressDialog() {
        mProgress.dismiss();
    }

    @Override
    public void showCircularProgress(boolean b) {
        if (b)
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setNavDrawerMenu(List<String> taskListsTitles) {
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(R.id.lists_titles_menu);
        SubMenu listsMenu = item.getSubMenu();
        listsMenu.clear();
        for (int i = 0; i < taskListsTitles.size(); i++) {
            listsMenu.add(0, i, i, taskListsTitles.get(i)).setIcon(R.drawable.ic_list).
                    setOnMenuItemClickListener(this);
            if (i == taskListsTitles.size() - 1) {
                listsMenu.add(0, Co.NEW_LIST_MENU_ITEM_ID, i + 1, getString(R.string.new_list)).
                        setIcon(R.drawable.ic_add_black_24dp).
                        setOnMenuItemClickListener(this);
            }
        }
        listsMenu.getItem(Co.listTitles.indexOf(getStringShP(Co.CURRENT_LIST_TITLE))).setChecked(true);
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void showTaskDeleteUndoSnackBar(String message, final int position, final LocalTask task) {

        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Co.SNACKBAR_DURATION);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.restoreDeletedItem(position);
            }
        });
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorLightPrimary));
        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
                        event == Snackbar.Callback.DISMISS_EVENT_SWIPE ||
                        event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) {
                    if (mPresenter.getTaskIdByIntId(task.getIntId()) == null) {
                        mPresenter.deleteTaskFromDataBase(task.getIntId());
                    }
                    mPresenter.deleteTask(task.getId(), task.getList());
                }
            }
        });
        snackbar.show();
    }

    ///-----------------------------API---------------------------////

    @Override
    public void setCredentials() {
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Co.SCOPES))
                .setBackOff(new ExponentialBackOff());
        accountName = getStringShP(Co.USER_EMAIL);
        if (!accountName.equals(Co.NO_ACCOUNT_NAME)) {
            mCredential.setSelectedAccountName(accountName);
        }
    }

    @Override
    public void requestAuthorization(Exception e) {
        startActivityForResult(
                ((UserRecoverableAuthIOException) e).getIntent(),
                Co.REQUEST_AUTHORIZATION);
    }

    @Override
    public GoogleAccountCredential getCredential() {
        return mCredential;
    }

    @Override
    public boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if ((networkInfo != null && networkInfo.isConnected())) {
            showNoInternetWarning(false);
            return true;
        } else {
            showNoInternetWarning(true);
            return false;
        }
    }


    ///-------------------------CLICK HANDLES------------------------///

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.fab:
                Intent i = new Intent(this, NewOrDetailActivity.class);
                mPresenter.navigateToEditTask(i);
                break;

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.lists_titles_menu);
        SubMenu listsMenu = menuItem.getSubMenu();
        if (item.getItemId() <= Co.listTitles.size()) {
            saveStringShP(Co.CURRENT_LIST_ID, Co.listIds.get(item.getItemId()));
            saveStringShP(Co.CURRENT_LIST_TITLE, Co.listTitles.get(item.getItemId()));
            for (int i = 0; i < listsMenu.size(); i++) {
                listsMenu.getItem(i).setChecked(false);
            }
            item.setChecked(true);
            List<LocalTask> tasks = mPresenter.getTasksFromListForAdapter(Co.listIds.get(item.getItemId()));
            if (tasks == null || tasks.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyDataLayout.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyDataLayout.setVisibility(View.GONE);
            }
            adapter.updateItems(mPresenter.getTasksFromListForAdapter(Co.listIds.get(item.getItemId())));
            toolbar.setTitle(item.getTitle());
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }

        if (item.getItemId() == Co.NEW_LIST_MENU_ITEM_ID) {
            drawer.closeDrawer(GravityCompat.START);
            showNewListDialog();

        }
        return false;

    }

    public void showNewListDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialog_layout = inflater.inflate(R.layout.new_list_dialog,
                null);
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        final TextInputEditText newTaskTitle = (TextInputEditText) dialog_layout.findViewById(R.id.newTaskTitle);
        db.setView(dialog_layout);
        db.setTitle(getString(R.string.new_list));
        db.setPositiveButton(R.string.add_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!newTaskTitle.getText().toString().trim().isEmpty()) {
                    mPresenter.addList(newTaskTitle.getText().toString());
                    dialog.dismiss();
                } else {
                    showToast(getString(R.string.list_title_empty_toast));
                }
            }
        });

        db.show();
    }

    public void showEditListDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialog_layout = inflater.inflate(R.layout.new_list_dialog,
                null);
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        final TextInputEditText taskTitle = (TextInputEditText) dialog_layout.findViewById(R.id.newTaskTitle);
        db.setView(dialog_layout);
        db.setTitle(getString(R.string.edit_list));
        db.setPositiveButton(R.string.edit_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!taskTitle.getText().toString().trim().isEmpty()) {
                    mPresenter.editList(getStringShP(Co.CURRENT_LIST_ID), taskTitle.getText().toString());
                    dialog.dismiss();
                } else {
                    showToast(getString(R.string.list_title_empty_toast));
                }
            }
        });
        taskTitle.setText(getStringShP(Co.CURRENT_LIST_TITLE));
        db.show();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(navigationView))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
                showToast(getStringShP(Co.USER_PIC_URL) + "\n" + getStringShP(Co.USER_EMAIL)
                        + "\n" + getStringShP(Co.USER_NAME));
                break;

            case R.id.refresh:
                refresh();
                break;

            case R.id.editList:
                showEditListDialog();
                break;

            case R.id.deleteList:
                showConfirmationDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setMessage(R.string.confirmation_task_delete_message);
        db.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.deleteList(getStringShP(Co.CURRENT_LIST_ID));
            }
        });

        db.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }


    //-------------------------ACTIVITY METHODS---------------------------//

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle(getStringShP(Co.CURRENT_LIST_TITLE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_list_menu, menu);
        return true;
    }

    @Override
    public void navigateToEditTask(Intent i) {
        startActivityForResult(i, Co.TASK_DATA_REQUEST_CODE);
    }

    @Override
    public void addTaskToAdapter(LocalTask localTask) {
        adapter.addItem(localTask, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (requestCode == Co.TASK_DATA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                // TASK EDITED
                if (resultIntent.hasExtra(Co.TASK_EDIT)) {
                    LocalTask task = (LocalTask) resultIntent.getExtras().getSerializable(Co.LOCAL_TASK);
                    if (task != null) {
                        if (task.getReminderId() != 0) {
                            if (task.getReminder() != 0) {
                                setOrUpdateAlarm(task);
                            } else {
                                if (isReminderSet((int) task.getReminderId())) {
                                    cancelReminder(task);
                                }
                            }
                        }
                        adapter.updateItem(task, resultIntent.getIntExtra(Co.ADAPTER_POSITION, -1));
                        mPresenter.editTask(task);
                    }

                    // TASK ADDED
                } else if (resultIntent.hasExtra(Co.NEW_TASK)) {
                    LocalTask task = (LocalTask) resultIntent.getExtras().getSerializable(Co.LOCAL_TASK);
                    if (task != null) {
                        task.setTaskList(getStringShP(Co.CURRENT_LIST_ID));
                        if (task.getReminder() != 0) setOrUpdateAlarm(task);
                        mPresenter.addTask(task);
                    }
                }
            }
        }
    }

    ///----------------------------OTHER--------------------------//


    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public String getStringShP(String key) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        if (key.equals(Co.CURRENT_LIST_ID)) {
            if (prefs.getString(key, Co.NO_VALUE).equals(Co.NO_VALUE)) {
                return "@default";
            }
        }
        if (key.equals(Co.CURRENT_LIST_TITLE)) {
            if (prefs.getString(key, Co.NO_VALUE).equals(Co.NO_VALUE)) {
                if (!Co.listTitles.isEmpty()) {
                    return Co.listTitles.get(0);
                }
            }
        }

        return prefs.getString(key, Co.NO_VALUE);
    }

    @Override
    public boolean getBooleanShP(String key) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean(key, true);
    }

    @Override
    public void saveBooleanShP(String key, boolean value) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    @Override
    public void saveStringShP(String key, String value) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();

    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void showSwipeRefreshProgress(boolean b) {
        swipeRefresh.setRefreshing(b);
    }

    @Override
    public void setOrUpdateAlarm(LocalTask task) {
        if (task.getReminder() != 0 && task.getReminderId() != 0) {
            Intent intent = new Intent(this, AlarmReciever.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Co.LOCAL_TASK, task);
            intent.putExtra(Co.LOCAL_TASK, bundle);
            intent.putExtra(Co.TASK_TITLE, task.getTitle());
            intent.putExtra(Co.TASK_DUE, task.getDue());
            intent.putExtra(Co.TASK_ID, task.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    (int) task.getReminderId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager =
                    (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    task.getReminder(), pendingIntent);
        }
    }

    @Override
    public void cancelReminder(LocalTask task) {
        Intent intent = new Intent(this, AlarmReciever.class);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                (int) task.getReminderId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    @Override
    public boolean isReminderSet(int reminderId) {
        Intent intent = new Intent(this, AlarmReciever.class);
        return (PendingIntent.getBroadcast(this,
                reminderId,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null);
    }
}



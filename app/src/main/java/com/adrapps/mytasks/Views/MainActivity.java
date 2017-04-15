package com.adrapps.mytasks.Views;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.adrapps.mytasks.APICalls.RefreshAllAsync;
import com.adrapps.mytasks.APICalls.SignInActivity;
import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Helpers.DateHelper;
import com.adrapps.mytasks.Helpers.SimpleItemTouchHelperCallback;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.adrapps.mytasks.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        Contract.MainActivityViewOps, MenuItem.OnMenuItemClickListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

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
    AlertDialog newTaskDialog;
    TextView dateTextView;
    Spinner notifSpinner;
    EditText newTaskTitle;
    Switch notSwitch;
    long selectedDateInMills;
    private boolean mTwoPane;
    private LinearLayout emptyDataLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getBooleanSharedPreference(Co.IS_FIRST_LAUNCH)) {
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
        if (getBooleanSharedPreference(Co.IS_FIRST_INIT)) {
            Log.d("firstIt", "ran");
            refreshFirstTime();
            return;
        }
        initRecyclerView(mPresenter.getTasksFromList(getStringShP(Co.CURRENT_LIST_ID)));
        setUpData();

    }


    //-------------------------VIEWS AND DATA------------------------///

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        emptyDataLayout = (LinearLayout) findViewById(R.id.empty_data_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
    }

    @Override
    public void setUpViews() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.first_sync_progress_dialog));
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setToolbarNavigationClickListener(this);
        toggle.syncState();
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
    public void setUpData() {
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
        if (isDeviceOnline()) {
            RefreshAllAsync refresh = new RefreshAllAsync(mPresenter, mCredential);
            refresh.execute();
        } else {
            showToast(getString(R.string.no_internet_toast));
            showSwipeRefreshProgress(false);
        }

    }

    @Override
    public void updateCurrentView() {
        Co.setListIds(mPresenter.getListsIds());
        Co.setListTitles(mPresenter.getListsTitles());
        boolean listStillExists = Co.listIds.contains(getStringShP(Co.CURRENT_LIST_ID));
        if (listStillExists) {
            String currentListTitle = mPresenter.getListTitleFromId(
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
        List<LocalTask> tasks = mPresenter.getTasksFromList(getStringShP(Co.CURRENT_LIST_ID));
        if (tasks == null || tasks.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyDataLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyDataLayout.setVisibility(View.GONE);
        }
        adapter.updateItems(mPresenter.getTasksFromList(getStringShP(Co.CURRENT_LIST_ID)));

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
    public void showProgressDialog() {
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
        }
        listsMenu.getItem(Co.listTitles.indexOf(getStringShP(Co.CURRENT_LIST_TITLE))).setChecked(true);
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void showUndoSnackBar(String message, final int position, final LocalTask task) {

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
                    mPresenter.deleteTaskFromApi(task.getTaskId(), task.getTaskList());
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
        accountName = getStringShP(Co.PREF_ACCOUNT_NAME);
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
        return (networkInfo != null && networkInfo.isConnected());
    }


    ///-------------------------CLICK HANDLES------------------------///

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.fab:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.new_task_dialog, null);
                dialogBuilder.setView(dialogView);

                //--------------FIND VIEWS--------------///
                newTaskTitle = (EditText) dialogView.findViewById(R.id.etTaskTitle);
                notSwitch = (Switch) dialogView.findViewById(R.id.notificationSwitch);
                notifSpinner = (Spinner) dialogView.findViewById(R.id.notifSpinner);
                dateTextView = (TextView) dialogView.findViewById(R.id.datePickerTextView);

                //-----------SET LISTENERS-----------////
                notSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (notSwitch.isChecked())
                            notifSpinner.setVisibility(View.VISIBLE);
                        else
                            notifSpinner.setVisibility(View.GONE);
                    }
                });
                notifSpinner.setOnItemSelectedListener(this);
                dateTextView.setOnClickListener(this);
                newTaskDialog = dialogBuilder.create();
                Window window = newTaskDialog.getWindow();
                WindowManager.LayoutParams wlp = window != null ? window.getAttributes() : null;

                wlp.gravity = Gravity.TOP;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                if (window != null) {
                    window.setAttributes(wlp);
                }
                newTaskDialog.setCancelable(true);
                newTaskDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        selectedDateInMills = 0
                        ;
                    }
                });
                newTaskDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(newTaskTitle, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
                newTaskDialog.setTitle(getString(R.string.new_task_dialog_title));
                newTaskDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.label_add_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                newTaskDialog.show();
                newTaskDialog.getButton(AlertDialog.BUTTON_POSITIVE).
                        setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (newTaskTitle.getText().toString().matches("")) {
                                    showToast(getString(R.string.empty_title_error));
                                    return;
                                }
                                Log.d("dueDate", String.valueOf(selectedDateInMills));
                                LocalTask task = new LocalTask(newTaskTitle.getText().toString(), selectedDateInMills);
                                mPresenter.addTaskToApi(task);
                                newTaskDialog.dismiss();

                            }
                        });
                break;
            case R.id.datePickerTextView:
                Calendar c = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(this, this,
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
                break;
            case R.id.notificationSwitch:

        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        selectedDateInMills = c.getTimeInMillis();
        dateTextView.setText(DateHelper.timeInMillsToString(selectedDateInMills));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                showToast("0");
                break;
            case 1:
                showToast("1");
                break;
            case 2:
                showToast("2");
                break;
            case 3:
                showToast("3");
                break;
            case 4:
                showToast("4");
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
//        isFirstTime = true;
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
            List<LocalTask> tasks = mPresenter.getTasksFromList(Co.listIds.get(item.getItemId()));
            if (tasks == null || tasks.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyDataLayout.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyDataLayout.setVisibility(View.GONE);
            }
            adapter.updateItems(mPresenter.getTasksFromList(Co.listIds.get(item.getItemId())));
            toolbar.setTitle(item.getTitle());
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }
        return false;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
//                Intent i = new Intent(this, SignInActivity.class);
//                startActivity(i);
                break;

            case R.id.refresh:
                refresh();
                break;

        }
        return super.onOptionsItemSelected(item);
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
    public void navigateToEditTask(Intent intent) {
        startActivityForResult(intent, Co.TASK_DATA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (requestCode == Co.TASK_DATA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                LocalTask task = new LocalTask(resultIntent.getStringExtra(Co.TASK_EDITED_TITLE),
                        resultIntent.getLongExtra(Co.TASK_EDITED_DUE, 0),
                        resultIntent.getStringExtra(Co.TASK_EDITED_NOTE));
                task.setTaskId(resultIntent.getStringExtra(Co.DETAIL_TASK_ID));
                mPresenter.editTask(task);
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void showNewTaskDialog() {

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
    public boolean getBooleanSharedPreference(String key) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean(key, true);
    }

    @Override
    public void saveBooleanShP (String key, boolean value){
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

}

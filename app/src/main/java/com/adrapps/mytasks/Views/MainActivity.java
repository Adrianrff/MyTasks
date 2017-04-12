package com.adrapps.mytasks.Views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.adrapps.mytasks.APICalls.FirstRefreshAsync;
import com.adrapps.mytasks.APICalls.RefreshAllAsync;
import com.adrapps.mytasks.APICalls.SignInActivity;
import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Helpers.SimpleItemTouchHelperCallback;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.adrapps.mytasks.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;

import java.util.ArrayList;
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
    List<String> listIds = new ArrayList<>();
    List<String> listTitles = new ArrayList<>();
    Contract.AdapterOps adapterOps;
    SwipeRefreshLayout swipeRefresh;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getBooleanSharedPreference(Co.IS_FIRST_TIME)) {
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
        if (getIntent().hasExtra(Co.IS_FIRST_INIT)) {
            refreshFirstTime();
            return;
        }
        setUpData();
        initRecyclerView(mPresenter.getTasksFromList(getStringShP(Co.CURRENT_LIST_ID)));
    }


    //-------------------------VIEWS AND DATA------------------------///

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
    }

    @Override
    public void setUpViews() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.first_sync_progress_dialog));
        toolbar.setTitle(getStringShP(Co.CURRENT_LIST_TITLE));
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
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0 ||dy<0 && fab.isShown())
                {
                    fab.hide();
                    swipeRefresh.setEnabled(false);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    fab.show();
                    swipeRefresh.setEnabled(true);
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    @Override
    public void setUpData() {
        listTitles = mPresenter.getListsTitles();
        listIds = mPresenter.getListsIds();
        setNavDrawerMenu(listTitles);
    }

    private void refreshFirstTime() {
        FirstRefreshAsync firstRefresh = new FirstRefreshAsync(mPresenter, mCredential);
        firstRefresh.execute();
    }

    private void refresh() {
        RefreshAllAsync refresh = new RefreshAllAsync(mPresenter, mCredential);
        refresh.execute();
    }


    @Override
    public void updateCurrentView() {
        listIds = mPresenter.getListsIds();
        listTitles = mPresenter.getListsTitles();
        boolean listStillExists = listIds.contains(getStringShP(Co.CURRENT_LIST_ID));
        if (listStillExists) {
            String currentListTitle = mPresenter.getListTitleFromId(
                    getStringShP(Co.CURRENT_LIST_ID));
            saveStringShP(Co.CURRENT_LIST_TITLE, currentListTitle);
            mPresenter.setToolbarTitle(currentListTitle);
        } else {
            String currentListTitle = listTitles.get(0);
            String currentListId = listIds.get(0);
            saveStringShP(Co.CURRENT_LIST_ID, currentListId);
            saveStringShP(Co.CURRENT_LIST_TITLE, currentListTitle);
            toolbar.setTitle(currentListTitle);
        }
        setNavDrawerMenu(listTitles);
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
    }

    @Override
    public void setListsIds(List<String> listIds) {
        this.listIds.addAll(mPresenter.getListsIds());
    }

    @Override
    public void setListsTitles(List<String> titles) {
        this.listTitles.addAll(mPresenter.getListsTitles());
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
        listsMenu.getItem(listTitles.indexOf(getStringShP(Co.CURRENT_LIST_TITLE))).setChecked(true);
    }

    @Override
    public void navIconToBack(boolean b) {
        if (b) {
            toggle.setDrawerIndicatorEnabled(false);
//            ActionBar actionBar = getSupportActionBar();
            toggle.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
//            if (actionBar != null) {
//                actionBar.setDisplayHomeAsUpEnabled(true);
////                actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
//                actionBar.setDisplayShowHomeEnabled(true);
//            }
            toggle.syncState();
        } else {
            toggle.setDrawerIndicatorEnabled(true);
            toggle.syncState();
        }
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void showUndoSnackBar(String message, final int position, final LocalTask task) {
        Snackbar snackbar =  Snackbar.make(coordinatorLayout,message,Snackbar.LENGTH_LONG);
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
                        event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE)  {
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
//        mCredential = GoogleAccountCredential.usingOAuth2(
//                getApplicationContext(), Arrays.asList(Co.SCOPES))
//                .setBackOff(new ExponentialBackOff());
//        accountName = getStringShP(Co.PREF_ACCOUNT_NAME);
//        if (!accountName.equals(Co.NO_ACCOUNT_NAME)) {
//            mCredential.setSelectedAccountName(accountName);
//        }
        return mCredential;
    }


    ///-------------------------CLICK HANDLES------------------------///

    @Override
    public void onClick(View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.new_task_dialog, null);
        dialogBuilder.setView(dialogView);

//        EditText editText = (EditText) dialogView.findViewById(R.id.label_field);
//        editText.setText("test label");
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle(getString(R.string.new_task_dialog_title));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showToast("sdgfsdf");
            }
        });
        alertDialog.show();
//        mPresenter.onClick(v.getId());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        saveStringShP(Co.CURRENT_LIST_ID, listIds.get(item.getItemId()));
        saveStringShP(Co.CURRENT_LIST_TITLE, listTitles.get(item.getItemId()));
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.lists_titles_menu);
        SubMenu listsMenu = menuItem.getSubMenu();
        for (int i = 0; i < listsMenu.size(); i++) {
            listsMenu.getItem(i).setChecked(false);
        }
        item.setChecked(true);
        adapter.updateItems(mPresenter.getTasksFromList(listIds.get(item.getItemId())));
        toolbar.setTitle(item.getTitle());
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void pressBack() {
        onBackPressed();
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
                break;

            case android.R.id.home:
                onBackPressed();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_list_menu, menu);
        return true;
    }


    ///----------------------------OTHER--------------------------//

    @Override
    public void setAdapterOps(Contract.AdapterOps aOps) {
        this.adapterOps = aOps;
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
                return listTitles.get(0);
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
    public void showSwipeRefreshProgress(boolean b){
        swipeRefresh.setRefreshing(b);
    }

}

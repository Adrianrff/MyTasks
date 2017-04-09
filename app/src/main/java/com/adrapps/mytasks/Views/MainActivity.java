package com.adrapps.mytasks.Views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.adrapps.mytasks.APICalls.FirstRefreshAsync;
import com.adrapps.mytasks.APICalls.RefreshAllAsync;
import com.adrapps.mytasks.APICalls.SignInActivity;
import com.adrapps.mytasks.Domain.Co;
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
        View.OnClickListener {

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    FloatingActionButton fab;
    ProgressDialog mProgress;
    ProgressBar progressBar;
    TaskListPresenter mPresenter;
    GoogleAccountCredential mCredential;
    String accountName;
    ActionBarDrawerToggle toggle;
    List<String> listIds = new ArrayList<>();
    List<String> listTitles = new ArrayList<>();
    Contract.AdapterOps adapterOps;
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
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            TaskListFragment taskListFragment = new TaskListFragment();
            this.adapterOps = taskListFragment;
            ft.replace(R.id.fragmentContainer, taskListFragment);
            ft.commit();
        }
        mPresenter = new TaskListPresenter(this,adapterOps);
        findViews();
        setCredentials();
        setUpViews();
        if (getIntent().hasExtra(Co.IS_FIRST_INIT)) {
            refreshFirstTime();
        }
        setUpData();
    }




    //-------------------------VIEWS AND DATA------------------------///

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void setUpData() {
        listTitles =mPresenter.getListsTitles();
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
    public void setUpViews() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.first_sync_progress_dialog));
        toolbar.setTitle(getStringSharedPreference(Co.CURRENT_LIST_TITLE));
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setToolbarNavigationClickListener(this);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
        fab.setOnClickListener(this);
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
        listTitles =mPresenter.getListsTitles();
        listIds = mPresenter.getListsIds();
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



    ///-----------------------------API---------------------------////

    private void setCredentials() {
        mCredential = getCredential();
        accountName = getStringSharedPreference(Co.PREF_ACCOUNT_NAME);
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

    public GoogleAccountCredential getCredential() {
        return GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Co.SCOPES))
                .setBackOff(new ExponentialBackOff());
    }




    ///-------------------------CLICK HANDLES------------------------///

    @Override
    public void onClick(View v) {
        mPresenter.onClick(v.getId());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        saveStringSharedPreference(Co.CURRENT_LIST_ID, listIds.get(item.getItemId()));
        saveStringSharedPreference(Co.CURRENT_LIST_TITLE, listTitles.get(item.getItemId()));
        adapterOps.updateAdapterItems(mPresenter.getTasksFromList(listIds.get(item.getItemId())));
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
        getMenuInflater().inflate(R.menu.main, menu);
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
    public String getStringSharedPreference(String key) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        if (key.equals(Co.CURRENT_LIST_ID)) {
            if (prefs.getString(key,Co.NO_VALUE).equals(Co.NO_VALUE)){
                return "@default";
            }
        }
        if (key.equals(Co.CURRENT_LIST_TITLE)) {
            if (prefs.getString(key,Co.NO_VALUE).equals(Co.NO_VALUE)){
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
    public void saveStringSharedPreference(String key, String value) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

}

package com.adrapps.mytasks.Views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.adrapps.mytasks.Constants;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.LocalTask;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.adrapps.mytasks.R;
import com.adrapps.mytasks.RefreshAllAsync;
import com.adrapps.mytasks.SignInActivity;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;

import java.util.Arrays;
import java.util.List;

public class TaskListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Contract.View {

    Toolbar toolbar;
    DrawerLayout drawer;
    RecyclerView recyclerView;
    NavigationView navigationView;
    FloatingActionButton fab;
    TaskListAdapter adapter;
    ProgressBar progressBar;
    TaskListPresenter mPresenter;
    GoogleAccountCredential mCredential;
    String accountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getBooleanSharedPreference(Constants.IS_FIRST_TIME)) {
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
        }
        if (getIntent().hasExtra(Constants.IS_FIRST_INIT)){
            refresh();
            initRecyclerView(mPresenter.getTasksFromLlist(mPresenter.getListsIds().get(0)));
        }
        setContentView(R.layout.activity_main);
        mPresenter = new TaskListPresenter(this);
        setCredentials();
        findAndSetupViews();
        initRecyclerView(mPresenter.getTasksFromLlist(getStringSharedPreference(Constants.CURRENT_LIST)));
    }

    private void findAndSetupViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
        fab.setOnClickListener(mPresenter);

    }

    private void initRecyclerView(List<LocalTask> tasks) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TaskListAdapter(this, tasks);
        recyclerView.setAdapter(adapter);


    }

    private void setCredentials() {
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Constants.SCOPES))
                .setBackOff(new ExponentialBackOff());
        accountName = getStringSharedPreference(Constants.PREF_ACCOUNT_NAME);
        if (!accountName.equals("No value")) {
            mCredential.setSelectedAccountName(accountName);
        } else {
            showToast("Account name not set");
        }
    }

    private void refresh() {
        RefreshAllAsync refresh = new RefreshAllAsync(mPresenter, mCredential);
        refresh.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                Constants.REQUEST_AUTHORIZATION);
    }

    @Override
    public void updateAdapterItems(List<LocalTask> localTasks) {
        adapter.updateItems(localTasks);
    }


    public GoogleAccountCredential getCredential() {
        return GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Constants.SCOPES))
                .setBackOff(new ExponentialBackOff());

    }

    private String getStringSharedPreference(String key) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        return prefs.getString(key, "No value");
    }

    private boolean getBooleanSharedPreference(String key) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean(key, true);
    }

    private void saveStringSharedPreference(String key, String value) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putString(key, value);
        editor.apply();


    }

}

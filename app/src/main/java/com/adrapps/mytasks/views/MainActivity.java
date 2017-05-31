package com.adrapps.mytasks.views;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.BottomSheetBehavior;
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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.api_calls.SignInActivity;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.AlarmHelper;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.helpers.SimpleItemTouchHelperCallback;
import com.adrapps.mytasks.interfaces.Contract;
import com.adrapps.mytasks.interfaces.OnStartDragListener;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.adrapps.mytasks.receivers.AlarmReciever;
import com.bumptech.glide.Glide;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
      implements NavigationView.OnNavigationItemSelectedListener,
      Contract.MainActivityViewOps, MenuItem.OnMenuItemClickListener,
      View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, OnStartDragListener {

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
   private LinearLayout emptyDataLayout, noInternetLayout, notificationDetailLayout;
   private View bottomSheet;
   private ImageView profilePic, editIcon;
   private TextView userEmail, userName, detailTitle, detailDate, nextReminderTV,
         detailNotification, detailNotes, detailRepeat;
   private BottomSheetBehavior mBottomSheetBehavior;
   private Intent newOrEditTaskIntent;
   private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
   public static final Object sDataLock = new Object();
   private ItemTouchHelper touchHelper;

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
         Intent i = new Intent(this, NewTaskOrEditActivity.class);
         i.putExtra(Co.LOCAL_TASK, task);
         startActivityForResult(i, Co.TASK_DATA_REQUEST_CODE);
      }
      if (!isDeviceOnline()) {
         showNoInternetWarning(true);
      }
      mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
      this.newOrEditTaskIntent = new Intent(MainActivity.this, NewTaskOrEditActivity.class);
   }


   //-------------------------VIEWS AND DATA------------------------///

   @Override
   public void findViews() {
      detailTitle = (TextView) findViewById(R.id.detail_title);
      detailNotes = (TextView) findViewById(R.id.detail_notes);
      detailDate = (TextView) findViewById(R.id.detail_date);
      detailNotification = (TextView) findViewById(R.id.detail_notification_tv);
      editIcon = (ImageView) findViewById(R.id.edit_icon);
      nextReminderTV = (TextView) findViewById(R.id.next_reminder_label_bs);
      bottomSheet = findViewById(R.id.bottom_sheet);
      toolbar = (Toolbar) findViewById(R.id.toolbar);
      fab = (FloatingActionButton) findViewById(R.id.fab);
      drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
      coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
      navigationView = (NavigationView) findViewById(R.id.nav_view);
      View headerView = navigationView.getHeaderView(0);
      profilePic = (ImageView) headerView.findViewById(R.id.profilePic);
      userName = (TextView) headerView.findViewById(R.id.userName);
      userEmail = (TextView) headerView.findViewById(R.id.userEmail);
      recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
      emptyDataLayout = (LinearLayout) findViewById(R.id.empty_data_layout);
      notificationDetailLayout = (LinearLayout) findViewById(R.id.notification_layout_detail);
      detailRepeat = (TextView) findViewById(R.id.detail_repeat);
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
      editIcon.setOnClickListener(this);
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
            if (!Co.IS_MULTISELECT_ENABLED) {
               if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                  if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                     fab.show();
                     swipeRefresh.setEnabled(true);
                  }
               }
               super.onScrollStateChanged(recyclerView, newState);
            }
         }
      });
      mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
      bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
         @Override
         public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
               fab.hide();
            } else if (newState == BottomSheetBehavior.STATE_HIDDEN ||
                  newState == BottomSheetBehavior.STATE_COLLAPSED) {
               fab.show();
            }
         }

         @Override
         public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
               fab.show();
               fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
         }
      };
      mBottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
   }

   @Override
   public void setSwipeRefreshEnabled(boolean b){
      swipeRefresh.setEnabled(b);
   }

   @Override
   public boolean dispatchTouchEvent(MotionEvent event) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
         if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            Rect outRect = new Rect();
            bottomSheet.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
               mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
         }
      }
      return super.dispatchTouchEvent(event);
   }

   @Override
   public void setListsData() {
      Co.setListIds(mPresenter.getListsIds());
      Co.setListTitles(mPresenter.getListsTitles());
      setNavDrawerMenu(Co.listTitles);
   }

   @Override
   public void showBottomSheet(@Nullable final LocalTask task, final int position, boolean shouldShow) {
      if (shouldShow) {
         swipeRefresh.setEnabled(false);
         if (task != null) {
            detailTitle.setText(task.getTitle());
            detailDate.setText(task.getDue() == 0 ? getString(R.string.no_due_date) : DateHelper.millisToDateOnly(task.getDue()));
            detailNotes.setText(task.getNotes());
            if (task.getReminder() != 0) {
               detailNotification.setText(
                     task.getReminder() == 0 ? null :
                           task.getRepeatMode() == 0 ? DateHelper.millisToFull(task.getReminder()) :
                                 DateHelper.millsToTimeOnly(task.getReminder()));
               notificationDetailLayout.setVisibility(View.VISIBLE);
               nextReminderTV.setText(DateHelper.millisToFull(task.getReminder()));

               switch (task.getRepeatMode()) {

                  case Co.REMINDER_ONE_TIME:
                     detailRepeat.setText(getString(R.string.one_time_repeat_mode));
                     break;
                  case Co.REMINDER_DAILY:
                     detailRepeat.setText(
                           getString(
                                 R.string.daily_repeat_mode) +
                                 " (" + DateHelper.millsToTimeOnly(
                                 task.getReminder()) + ")");
                     break;

                  case Co.REMINDER_DAILY_WEEKDAYS:
                     detailRepeat.setText(
                           getString(R.string.weekdays) + " (" +
                                 DateHelper.millsToTimeOnly(task.getReminder()) + ")");
                     break;

                  case Co.REMINDER_SAME_DAY_OF_WEEK:
                     detailRepeat.setText(
                           getString(R.string.every) + " " +
                                 DateHelper.timeInMillsToDay(task.getReminder())
                                 + " (" + DateHelper.millsToTimeOnly(
                                 task.getReminder()) + ")");
                     break;

                  case Co.REMINDER_SAME_DAY_OF_MONTH:
                     detailRepeat.setText(
                           getString(R.string.on_day) + " " +
                                 DateHelper.millsToTimeOnly(task.getReminder()) +
                                 " " + getString(R.string.of_every_month));
                     break;

               }
            } else {
               detailNotification.setText(null);
               notificationDetailLayout.setVisibility(View.GONE);
            }
         }
         mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
         editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               newOrEditTaskIntent.putExtra(Co.LOCAL_TASK, task);
               newOrEditTaskIntent.putExtra(Co.ADAPTER_POSITION, position);
               mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                  @Override
                  public void onStateChanged(@NonNull View bottomSheet, int newState) {
                     if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        navigateToEditTask(newOrEditTaskIntent);
                        mBottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
                     }
                  }

                  @Override
                  public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                  }
               });
               mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }

         });


      } else {
         mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
         swipeRefresh.setEnabled(true);
      }
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
         String currentListTitle = null;
         try {
            currentListTitle = Co.listTitles.get(0);
         } catch (Exception e) {
            e.printStackTrace();
         }
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
      touchHelper = new ItemTouchHelper(callback);
      touchHelper.attachToRecyclerView(recyclerView);
      if (tasks == null || tasks.isEmpty()) {
         showEmptyRecyclerView(true);
      } else {
         showEmptyRecyclerView(false);
      }
   }

   @Override
   public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
      touchHelper.startDrag(viewHolder);
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
   public void showFab(boolean b) {
      if (b){
         fab.show();
      } else {
         fab.hide();
      }
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
                  setIcon(R.drawable.add_white).
                  setOnMenuItemClickListener(this);
         }
      }
      try {
         listsMenu.getItem(Co.listTitles.indexOf(getStringShP(Co.CURRENT_LIST_TITLE))).setChecked(true);
      } catch (Exception e) {
         showToast("error");
         saveStringShP(Co.CURRENT_LIST_TITLE, Co.listTitles.get(0));
         e.printStackTrace();
      }
   }

   @Override
   public void setToolbarTitle(String title) {
      toolbar.setTitle(title);
   }

   @Override
   public void showTaskDeleteUndoSnackBar(String message, final SparseArray map) {

      Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Co.SNACKBAR_DURATION);
      snackbar.setAction(R.string.undo, new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            adapter.restoreDeletedItems(map);
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
//               List<String> taskIds = new ArrayList<>();
//               String currentId = null;
//               for (int i = 0; i < tasks.size(); i++) {
//                  LocalTask currentTask = tasks.get(i);
//                  taskIds.add(currentTask.getId());
//                  currentId = currentTask.getId();
//                  int currentIntId = currentTask.getIntId();
//                  if (currentId == null || currentId.trim().isEmpty()) {
//                     currentId = mPresenter.getTaskIdByIntId(currentIntId);
//                  }
//                  if (currentId == null || currentId.trim().isEmpty()) {
//                     mPresenter.deleteTaskFromDatabase(currentIntId);
//                     AlarmHelper.cancelReminder(currentTask, MainActivity.this);
//                  }
//               }
               List<LocalTask> tasks = new ArrayList<>();
               for (int i = 0; i < map.size(); i++){
                  tasks.add((LocalTask) map.valueAt(i));
               }
               mPresenter.deleteTasks(tasks);
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
            Intent i = new Intent(this, NewTaskOrEditActivity.class);
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
      final ViewGroup nullParent = null;
      View dialog_layout = inflater.inflate(R.layout.new_list_dialog,
            nullParent);
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
      AlertDialog dialog = db.create();
      if (dialog.getWindow() != null)
         dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
      dialog.show();
   }

   public void showEditListDialog() {
      LayoutInflater inflater = LayoutInflater.from(this);
      final ViewGroup nullParent = null;
      View dialog_layout = inflater.inflate(R.layout.new_list_dialog,
            nullParent);
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
      AlertDialog dialog = db.create();
      if (dialog.getWindow() != null) {
         dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
      }
      dialog.show();
   }

   @Override
   public void onBackPressed() {
      if (drawer.isDrawerOpen(navigationView)) {
         drawer.closeDrawer(GravityCompat.START);
         return;
      }
      if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
         mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
         swipeRefresh.setEnabled(true);
         fab.show();
      }
      if (adapter.isSelectableMode()) {
         adapter.leaveSelectMode();
      } else
         super.onBackPressed();
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
      if (fab.getVisibility() != View.VISIBLE) {
         fab.show();
      }
      toolbar.setTitle(getStringShP(Co.CURRENT_LIST_TITLE));
   }

   @Override
   protected void onPause() {
      super.onPause();
      mPresenter.closeDatabases();
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
               fab.show();
               LocalTask task = (LocalTask) resultIntent.getExtras().getSerializable(Co.LOCAL_TASK);
               if (task != null) {
                  long reminderInMillis = task.getReminder();
                  if (reminderInMillis != 0) {
                     Calendar reminderCalendarObject = Calendar.getInstance();
                     reminderCalendarObject.setTimeInMillis(reminderInMillis);
                     AlarmHelper.setOrUpdateAlarm(task, this);
                  } else {
                     AlarmHelper.cancelReminder(task, this);
                  }
                  mPresenter.updateReminder(task.getIntId(), task.getReminder());
               }
               adapter.updateItem(task, resultIntent.getIntExtra(Co.ADAPTER_POSITION, -1));
               if (!resultIntent.hasExtra(Co.NO_API_EDIT)) {
                  mPresenter.editTask(task);
               }

               // TASK ADDED
            } else if (resultIntent.hasExtra(Co.NEW_TASK)) {
               LocalTask task = (LocalTask) resultIntent.getExtras().getSerializable(Co.LOCAL_TASK);
               if (task != null) {
                  task.setList(getStringShP(Co.CURRENT_LIST_ID));
                  if (task.getReminder() != 0) {
                     AlarmHelper.setOrUpdateAlarm(task, this);
                  }
                  task.setSyncStatus(0);
                  mPresenter.addTask(task);
               }
            }

         }
      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {

      switch (item.getItemId()) {

         case R.id.action_settings:
            Calendar calendar = Calendar.getInstance();
            Calendar calToday = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            String format;
            if (calendar.get(Calendar.YEAR) == calToday.get(Calendar.YEAR)) {
               format = "d MMM, h:mm a";
            } else {
               format = "d MMM yyyy, h:mm a";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
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
   public boolean isReminderSet(int reminderId) {
      Intent intent = new Intent(this, AlarmReciever.class);
      return (PendingIntent.getBroadcast(this,
            reminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE) != null);
   }

   @Override
   public void updateItem(LocalTask syncedLocalTask) {
      adapter.updateItem(syncedLocalTask, -1);
   }




}



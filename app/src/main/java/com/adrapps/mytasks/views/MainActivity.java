package com.adrapps.mytasks.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.support.v4.view.ViewCompat;
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
import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.AlarmHelper;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.helpers.ObjectHelper;
import com.adrapps.mytasks.helpers.SimpleItemTouchHelperCallback;
import com.adrapps.mytasks.interfaces.Contract;
import com.adrapps.mytasks.interfaces.OnStartDragListener;
import com.adrapps.mytasks.other.MyApplication;
import com.adrapps.mytasks.preferences.SettingsActivity;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.bumptech.glide.Glide;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
      implements NavigationView.OnNavigationItemSelectedListener,
      Contract.MainActivityViewOps, MenuItem.OnMenuItemClickListener,
      View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, OnStartDragListener, DrawerLayout.DrawerListener {

   @BindView(R.id.detail_title) TextView detailTitle;
   @BindView(R.id.detail_notes) TextView detailNotes;
   @BindView(R.id.detail_date) TextView detailDate;
   @BindView(R.id.detail_notification_tv) TextView detailNotification;
   @BindView(R.id.next_reminder_label_bs) TextView nextReminderTV;
   @BindView(R.id.edit_icon) ImageView editIcon;

   @BindView(R.id.fab) FloatingActionButton fab;
   @BindView(R.id.toolbar) Toolbar toolbar;
   @BindView(R.id.drawer_layout) DrawerLayout drawer;
   @BindView(R.id.bottom_sheet) View bottomSheet;
   @BindView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
   @BindView(R.id.recyclerView) RecyclerView recyclerView;
   @BindView(R.id.empty_data_layout) LinearLayout emptyDataLayout;
   @BindView(R.id.notification_layout_detail) LinearLayout notificationDetailLayout;
   @BindView(R.id.noInternetLayout) LinearLayout noInternetLayout;
   @BindView(R.id.progressBar) ProgressBar progressBar;
   @BindView(R.id.swipeRefresh) SwipeRefreshLayout swipeRefresh;
   @BindView(R.id.detail_repeat) TextView detailRepeat;

   NavigationView navigationView;
   ProgressDialog progressDialog;
   TaskListPresenter mPresenter;
   GoogleAccountCredential mCredential;
   String accountName;
   TaskListAdapter adapter;
   ActionBarDrawerToggle toggle;

   private ImageView profilePicContainer/*, editIcon*/;
   private TextView userEmail, userName /*detailTitle,*/ /*detailDate, nextReminderTV*/;
   private BottomSheetBehavior mBottomSheetBehavior;
   private Intent newOrEditTaskIntent;
   private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
   private ItemTouchHelper touchHelper;
   private LocalTask taskShownInBottomSheet;
   private int taskShownInBottomSheetPos;
   private boolean settingsItemSelected, newListItemSelected;
   private static final String TAG = "MainActivity";



   @Override
   protected void onCreate(Bundle savedInstanceState) {

      super.onCreate(savedInstanceState);
      if (getBooleanShP(Co.IS_FIRST_INIT, true)) {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      }
      if (getBooleanShP(Co.IS_FIRST_LAUNCH, true)) {
         Intent i = new Intent(this, SignInActivity.class);
         startActivity(i);
         finish();
         return;
      }
      setContentView(R.layout.main_activity);
      ButterKnife.bind(this);
      mPresenter = new TaskListPresenter(this);
      findViews();
      setUpViews();
      setCredentials();
      this.newOrEditTaskIntent = new Intent(MainActivity.this, NewTaskOrEditActivity.class);
      if (getBooleanShP(Co.IS_FIRST_INIT, true)) {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         refreshFirstTime();
         saveIntShP(Co.MORNING_REMINDER_PREF_KEY, Co.MORNING_DEFAULT_REMINDER_TIME);
         saveIntShP(Co.AFTERNOON_REMINDER_PREF_KEY, Co.AFTERNOON_DEFAULT_REMINDER_TIME);
         saveIntShP(Co.EVENING_REMINDER_PREF_KEY, Co.EVENING_DEFAULT_REMINDER_TIME);
         Glide.with(this)
               .load(getStringShP(Co.USER_PIC_URL, null)).
               into(profilePicContainer);
         return;
      }
      setListsData();
      setNavDrawerMenu(Co.lists);
      initRecyclerView(mPresenter.getTasksFromListForAdapter(getIntShP(Co.CURRENT_LIST_INT_ID, -1)));
      if (getIntent().hasExtra(Co.LOCAL_TASK)) {
         LocalTask task = (LocalTask) getIntent().getSerializableExtra(Co.LOCAL_TASK);
         Intent i = new Intent(this, NewTaskOrEditActivity.class);
         i.putExtra(Co.LOCAL_TASK, task);
         startActivityForResult(i, Co.TASK_DATA_REQUEST_CODE);
      }
      if (!isDeviceOnline()) {
         showNoInternetWarning(true);
      }
      if (savedInstanceState != null) {
         taskShownInBottomSheet = (LocalTask) savedInstanceState.getSerializable(Co.STATE_SHOWN_TASK);
         taskShownInBottomSheetPos = savedInstanceState.getInt(Co.SHOWN_TASK_POSITION_STATE);
         if (taskShownInBottomSheet != null && taskShownInBottomSheetPos >= 0) {
            showBottomSheet(taskShownInBottomSheet, taskShownInBottomSheetPos, true);
         }
      }
   }


   @Override
   protected void onDestroy() {
      super.onDestroy();
      RefWatcher refWatcher = MyApplication.getRefWatcher(this);
      refWatcher.watch(this);
   }

   //-------------------------VIEWS AND DATA------------------------///

   @Override
   public void findViews() {
      navigationView = (NavigationView) findViewById(R.id.nav_view);
      View headerView = navigationView.getHeaderView(0);
      userName = (TextView) headerView.findViewById(R.id.userName);
      userEmail = (TextView) headerView.findViewById(R.id.userEmail);
      profilePicContainer = (ImageView) headerView.findViewById(R.id.profilePic);
      drawer.addDrawerListener(this);
//      detailTitle = (TextView) findViewById(R.id.detail_title);
//      detailNotes = (TextView) findViewById(R.id.detail_notes);
//      detailDate = (TextView) findViewById(R.id.detail_date);
//      detailNotification = (TextView) findViewById(R.id.detail_notification_tv);
//      editIcon = (ImageView) findViewById(R.id.edit_icon);
//      nextReminderTV = (TextView) findViewById(R.id.next_reminder_label_bs);
//      bottomSheet = findViewById(R.id.bottom_sheet);
//      toolbar = (Toolbar) findViewById(R.id.toolbar);
//      fab = (FloatingActionButton) findViewById(R.id.fab);
//      drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//      coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
//      View headerView = navigationView.getHeaderView(0);
//      recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//      emptyDataLayout = (LinearLayout) findViewById(R.id.empty_data_layout);
//      notificationDetailLayout = (LinearLayout) findViewById(R.id.notification_layout_detail);
//      progressBar = (ProgressBar) findViewById(R.id.progressBar);
//      swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
//      noInternetLayout = (LinearLayout) findViewById(R.id.noInternetLayout);
//      detailRepeat = (TextView) findViewById(R.id.detail_repeat);
   }

   @Override
   public void setUpViews() {
      progressDialog = new ProgressDialog(this);
      setSupportActionBar(toolbar);
      toolbar.setOnClickListener(this);
      toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
      toggle.setToolbarNavigationClickListener(this);
      toggle.syncState();
      Glide.with(this)
            .load(getStringShP(Co.USER_PIC_URL, null))
            .placeholder(R.drawable.default_user_pic_24dp)
            .into(profilePicContainer);
      userName.setText(getStringShP(Co.USER_NAME, null));
      userEmail.setText(getStringShP(Co.USER_EMAIL, null));
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
               fab.setOnClickListener(null);
               fab.hide();
               swipeRefresh.setEnabled(false);
            }
         }

         @Override
         public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (!Co.IS_MULTISELECT_ENABLED) {
               if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                  if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                     fab.setOnClickListener(MainActivity.this);
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
               taskShownInBottomSheet = null;
               taskShownInBottomSheetPos = -1;
               fab.show();
               fab.setOnClickListener(MainActivity.this);
            }
         }

         @Override
         public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
               fab.show();
               fab.setOnClickListener(null);
               fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
         }
      };
      mBottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
   }

   @Override
   public void setSwipeRefreshEnabled(boolean b) {
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
   public void showBottomSheet(@Nullable final LocalTask task, final int position, boolean shouldShow) {
      if (shouldShow) {
         taskShownInBottomSheet = task;
         taskShownInBottomSheetPos = position;
         swipeRefresh.setEnabled(false);
         if (task != null) {
            detailTitle.setText(task.getTitle());
            detailDate.setText(task.getDue() == 0 ? getString(R.string.no_due_date) : DateHelper.millisToDateOnly(task.getDue()));
            detailNotes.setText(task.getNotes());
            if (task.getReminder() != 0) {
               String nextRem;
               nextRem = DateHelper.millisToFull(task.getReminder());
               Calendar taskReminder = Calendar.getInstance();
               taskReminder.setTimeInMillis(task.getReminder());
               detailNotification.setText(
                     task.getReminder() == 0 ? null :
                           task.getRepeatMode() == 0 ? DateHelper.millisToFull(task.getReminder()) :
                                 DateHelper.millisToTimeOnly(task.getReminder()));
               notificationDetailLayout.setVisibility(View.VISIBLE);

               switch (task.getRepeatMode()) {

                  case Co.REMINDER_ONE_TIME:
                     detailRepeat.setText(getString(R.string.one_time_repeat_mode));
                     break;
                  case Co.REMINDER_DAILY:
                     detailRepeat.setText(
                           getString(
                                 R.string.daily_repeat_mode));
                     break;

                  case Co.REMINDER_DAILY_WEEKDAYS:
                     detailRepeat.setText(
                           getString(R.string.weekdays));
                     break;

                  case Co.REMINDER_WEEKLY:
                     SparseArray<String> daysMap = new SparseArray<>();
                     daysMap.put(Co.MONDAY, getString(R.string.mondays));
                     daysMap.put(Co.TUESDAY, getString(R.string.tuesdays));
                     daysMap.put(Co.WEDNESDAY, getString(R.string.wednesdays));
                     daysMap.put(Co.THURSDAY, getString(R.string.thursdays));
                     daysMap.put(Co.FRIDAY, getString(R.string.fridays));
                     daysMap.put(Co.SATURDAY, getString(R.string.saturdays));
                     daysMap.put(Co.SUNDAY, getString(R.string.sundays));
                     detailRepeat.setText(
                           getString(R.string.weekly) + " (" + daysMap.get(task.getRepeatDay()) + ")");
                     break;

                  case Co.REMINDER_MONTHLY:
                     detailRepeat.setText(
                           getString(R.string.monthly) + " (" + taskReminder.get(Calendar.DAY_OF_MONTH) + ")");
                     int max = taskReminder.getActualMaximum(Calendar.DAY_OF_MONTH);
                     int reminderDayOfMonth = taskReminder.get(Calendar.DAY_OF_MONTH);
                     if (max == reminderDayOfMonth) {
                        nextRem = DateHelper.millisToFull(taskReminder.getTimeInMillis()) + " (" +
                              getString(R.string.last_day_of_month) + ")";
                     } else {
                        nextRem = DateHelper.millisToFull(taskReminder.getTimeInMillis());
                     }
                     break;

               }
               nextReminderTV.setText(nextRem);
            } else {
               detailNotification.setText(null);
               notificationDetailLayout.setVisibility(View.GONE);
            }
         }
         mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
         fab.hide();
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
         Log.d("showBottomSheet", "called");
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
   public void setListsData() {
      List<LocalList> lists = mPresenter.getLocalLists();
      Co.setListIds(lists);
      Co.setLists(lists);
      Co.setListTitles(lists);
      Co.setListIntIds(lists);
   }

   @Override
   public void updateView() {
      setListsData();
      List<LocalList> lists = Co.lists;
      SparseArray<LocalList> listMap = ObjectHelper.getLocalListIntIdMap(lists);
      if (!lists.isEmpty()) {
         int currentListIntId = getIntShP(Co.CURRENT_LIST_INT_ID, -1);
         String currentListId;
         String currentListTitle;
         if (Co.listIntIds.contains(currentListIntId)) {
            currentListId = listMap.get(currentListIntId).getId();
            currentListTitle = listMap.get(currentListIntId).getTitle();
            saveStringShP(Co.CURRENT_LIST_TITLE, currentListTitle);
            saveStringShP(Co.CURRENT_LIST_ID, currentListId);
         } else {
            currentListIntId = lists.get(0).getIntId();
            currentListId = lists.get(0).getId();
            currentListTitle = lists.get(0).getTitle();
            saveIntShP(Co.CURRENT_LIST_INT_ID, currentListIntId);
            saveStringShP(Co.CURRENT_LIST_ID, currentListId);
            saveStringShP(Co.CURRENT_LIST_TITLE, currentListTitle);
         }
//         try {
//            currentListTitle = Co.listTitles.get(0);
//         } catch (Exception e) {
//            Co.listTitles = mPresenter.getListsTitles();
//            currentListTitle = Co.listTitles.get(0);
//            e.printStackTrace();
//         }
//         try {
//            currentListId = Co.listIds.get(0);
//         } catch (Exception e) {
//            e.printStackTrace();
//            Co.listIds = mPresenter.getLocalListsIds();
//            currentListId = Co.listIds.get(0);
//         }
//         saveStringShP(Co.CURRENT_LIST_ID, currentListId);
//         saveStringShP(Co.CURRENT_LIST_TITLE, currentListTitle);
//         saveIntShP(Co.CURRENT_LIST_INT_ID, currentListIntId);
//         toolbar.setTitle(currentListTitle);
//
         setToolbarTitle(currentListTitle);
         setNavDrawerMenu(lists);
         List<LocalTask> tasks = mPresenter.getTasksFromListForAdapter(currentListIntId);
         showEmptyRecyclerView(tasks == null || tasks.isEmpty());
         adapter.updateItems(tasks);
      } else {
         try {
            throw new Exception("Lists database is returning empty");
         } catch (Exception e) {
            FirebaseCrash.report(e);
         }
      }

   }

   @Override
   public void setNavDrawerMenu(List<LocalList> localLists) {
      Menu menu = navigationView.getMenu();
      MenuItem listsTitlesMenuItem = menu.findItem(R.id.lists_titles_menu);
      SubMenu listsTitlesSubMenu = listsTitlesMenuItem.getSubMenu();
      listsTitlesSubMenu.clear();

      //Set menu item titles (list titles) and task counters
      for (int i = 0; i < localLists.size(); i++) {
         LocalList list = localLists.get(i);
         MenuItem currentItem = listsTitlesSubMenu.add(0, Co.LIST_ITEM_ID_SUFFIX + localLists.get(i).getIntId(), i,
               list.getTitle()).setIcon(R.drawable.ic_new_list).
               setOnMenuItemClickListener(this);
         if (currentItem.getActionView() == null) {
            currentItem.setActionView(R.layout.navdrawer_item_counter);
         }
         updateTaskCounterForDrawer(list.getIntId(), currentItem);
         View counter = currentItem.getActionView();
         if (counter instanceof TextView) {
            ((TextView) counter).setText(
                  "(" +
                        String.valueOf(mPresenter.getTasksNotCompletedFromListCount(list.getIntId())) +
                        ")");
         }
      }
      //Add new list item
      listsTitlesSubMenu.add(0, R.id.newList, 9999, getString(R.string.new_list)).
            setIcon(R.drawable.add_white).
            setOnMenuItemClickListener(this);

      //Set current list item selected
      MenuItem currentListMenuItem = listsTitlesSubMenu.
            findItem(getIntShP(Co.LIST_ITEM_ID_SUFFIX + Co.CURRENT_LIST_INT_ID, -1));
      if (currentListMenuItem != null) {
         currentListMenuItem.setChecked(true);
      } else {
         listsTitlesSubMenu.getItem(0).setChecked(true);
      }
//      try {
//         listsTitlesSubMenu.findItem(getIntShP(Co.CURRENT_LIST_INT_ID, -1));
//      } catch (Exception e) {
//         listsTitlesSubMenu.getItem(getIntShP(Co.CURRENT_LIST_ID, -1)).setChecked(true);
//         showToast("error");
//         saveStringShP(Co.CURRENT_LIST_TITLE, Co.listTitles.get(0));
//         e.printStackTrace();
//      }
   }

   @Override
   public void updateTaskCounterForDrawer(int listIntId, @Nullable MenuItem itemToUpdate) {
      if (itemToUpdate == null) {
         Menu menu = navigationView.getMenu();
         MenuItem listsTitlesMenuItem = menu.findItem(R.id.lists_titles_menu);
         SubMenu listsTitlesSubMenu = listsTitlesMenuItem.getSubMenu();
         itemToUpdate = listsTitlesSubMenu.findItem(Co.LIST_ITEM_ID_SUFFIX + listIntId);
      }
      if (itemToUpdate != null) {
         View counter = itemToUpdate.getActionView();
         if (counter != null && counter instanceof TextView) {
            ((TextView) counter).setText("(" +
                  String.valueOf(mPresenter.getTasksNotCompletedFromListCount(listIntId)) + ")");
         }
      }
   }


   @Override
   public void initRecyclerView(List<LocalTask> tasks) {
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
            layoutManager.getOrientation());
      recyclerView.addItemDecoration(dividerItemDecoration);
      recyclerView.setLayoutManager(layoutManager);
      adapter = new TaskListAdapter(this, tasks, mPresenter);
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
      if (progressDialog != null) {
         progressDialog.setMessage(getString(R.string.please_wait));
         progressDialog.show();
      }
   }

   @Override
   public void showFab(boolean b) {
      if (b) {
         fab.show();
         fab.setOnClickListener(MainActivity.this);
      } else {
         fab.hide();
      }
   }

   @Override
   public void dismissProgressDialog() {
      if (progressDialog != null && progressDialog.isShowing()) {
         progressDialog.dismiss();
      }
   }

   @Override
   public boolean isFinishing() {
      return super.isFinishing();
   }

   @Override
   public void showCircularProgress(boolean b) {
      if (progressBar != null && ViewCompat.isAttachedToWindow(progressBar)) {
         if (b)
            progressBar.setVisibility(View.VISIBLE);
         else
            progressBar.setVisibility(View.GONE);
      }
   }


   @Override
   public void setToolbarTitle(String title) {
      toolbar.setTitle(title);
   }

   @Override
   public void showDeleteSnackBar(String message, final SparseArray map) {

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
               List<LocalTask> tasks = new ArrayList<>();
               for (int i = 0; i < map.size(); i++) {
                  tasks.add((LocalTask) map.valueAt(i));
               }
               AlarmHelper.cancelDefaultRemindersForTasks(MainActivity.this, tasks);
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
      accountName = getStringShP(Co.USER_EMAIL, null);
      if (accountName != null) {
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

         case R.id.toolbar:
            showToast(getStringShP(Co.CURRENT_LIST_TITLE, null));
      }
   }

   @Override
   public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      Menu menu = navigationView.getMenu();
      MenuItem menuItem = menu.findItem(R.id.lists_titles_menu);
      SubMenu listsMenu = menuItem.getSubMenu();
      if (item.getItemId() != R.id.newList && item.getItemId() != R.id.nav_settings) {
         int listIntId = item.getItemId() - Co.LIST_ITEM_ID_SUFFIX;
         setCurrentListInfo(listIntId);
         for (int i = 0; i < listsMenu.size(); i++) {
            listsMenu.getItem(i).setChecked(false);
         }
         item.setChecked(true);
         List<LocalTask> tasks = mPresenter.getTasksFromListForAdapter(listIntId);
         if (tasks == null || tasks.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyDataLayout.setVisibility(View.VISIBLE);
         } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyDataLayout.setVisibility(View.GONE);
         }
         adapter.updateItems(tasks);
         toolbar.setTitle(item.getTitle());
         drawer.closeDrawer(GravityCompat.START);
         return false;
      } else {
         if (item.getItemId() == R.id.newList) {
            newListItemSelected = true;
            drawer.closeDrawer(GravityCompat.START);
            return false;
         }

         if (item.getItemId() == R.id.nav_settings) {
            settingsItemSelected = true;
            drawer.closeDrawer(GravityCompat.START);
            return false;
         }
      }
      return false;

   }

   public void setCurrentListInfo(int listIntId) {
      saveIntShP(Co.CURRENT_LIST_INT_ID, listIntId);
      saveStringShP(Co.CURRENT_LIST_ID, mPresenter.getlistIdByIntId(listIntId));
      saveStringShP(Co.CURRENT_LIST_TITLE, mPresenter.getListTitleByIntId(listIntId));
   }


   //DRAWER LISTENER METHODS
   @Override
   public void onDrawerSlide(View drawerView, float slideOffset) {

   }

   @Override
   public void onDrawerOpened(View drawerView) {

   }

   @Override
   public void onDrawerClosed(View drawerView) {
      if (settingsItemSelected) {
         goToSettings();
         settingsItemSelected = false;
      }
      if (newListItemSelected) {
         showNewListDialog();
         newListItemSelected = false;
      }
   }

   @Override
   public void onDrawerStateChanged(int newState) {

   }

   public void goToSettings() {
      Intent i = new Intent(this, SettingsActivity.class);
      startActivity(i);
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
               int listIntId = mPresenter.addNewListToDB(newTaskTitle.getText().toString());
               mPresenter.addNewListToServer(newTaskTitle.getText().toString(), listIntId);
               updateView();
               //FIXME list item not being highlighted as selected in navdrawer after creation
               onNavigationItemSelected(navigationView.getMenu().findItem(Co.LIST_ITEM_ID_SUFFIX + listIntId));
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
      final TextInputEditText listTitle = (TextInputEditText) dialog_layout.findViewById(R.id.newTaskTitle);
      db.setView(dialog_layout);
      db.setTitle(getString(R.string.edit_list));
      db.setPositiveButton(R.string.edit_button, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            String newName = listTitle.getText().toString().trim();
            if (!newName.isEmpty()) {
               String listId = null;
               int listIntId = getIntShP(Co.CURRENT_LIST_INT_ID, -1);
               if (listIntId >= 0) {
                  mPresenter.changeListNameInDB(listIntId, newName);
                  listId = mPresenter.getlistIdByIntId(listIntId);
               }
               if (listId != null) {
                  mPresenter.changeListNameInServer(getStringShP(Co.CURRENT_LIST_ID, null), newName);
               }
               dialog.dismiss();
               setListsData();
               updateView();
            } else {
               showToast(getString(R.string.list_title_empty_toast));
            }
         }
      });
      listTitle.setText(getStringShP(Co.CURRENT_LIST_TITLE, null));
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
         fab.setOnClickListener(MainActivity.this);
         return;
      }
      if (adapter != null) {
         if (adapter.isSelectableMode()) {
            adapter.leaveSelectMode();
         } else {
            super.onBackPressed();
         }
      } else {
         super.onBackPressed();
      }
   }


   private void showDeleteListConfirmationDialog() {
      AlertDialog.Builder db = new AlertDialog.Builder(this);
      db.setMessage(R.string.confirmation_task_delete_message);
      db.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            int listIntId = getIntShP(Co.CURRENT_LIST_INT_ID, -1);
            String listId = getStringShP(Co.CURRENT_LIST_ID, null);
            List<LocalTask> tasksFromList = mPresenter.getTasksFromList(listIntId);
            if (!tasksFromList.isEmpty()) {
               mPresenter.markTasksDeleted(tasksFromList);
            }
            if (listId == null) {
               if (listIntId >= 0) {
                  mPresenter.isDeviceOnline();
                  mPresenter.deleteListFromDB(listIntId);
                  setListsData();
                  updateView();
               }
            } else {
               mPresenter.markListDeleted(listIntId);
               mPresenter.deleteListFromServer(listId);
               setListsData();
               updateView();
            }
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
   protected void onSaveInstanceState(Bundle outState) {
      if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
         if (taskShownInBottomSheet != null) {
            outState.putSerializable(Co.STATE_SHOWN_TASK, taskShownInBottomSheet);
            outState.putInt(Co.SHOWN_TASK_POSITION_STATE, taskShownInBottomSheetPos);
         }
      }
      super.onSaveInstanceState(outState);
   }

   @Override
   protected void onResume() {
      super.onResume();
      if (fab.getVisibility() != View.VISIBLE &&
            mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
         fab.show();
         fab.setOnClickListener(MainActivity.this);
      }
      toolbar.setTitle(getStringShP(Co.CURRENT_LIST_TITLE, null));
   }

   @Override
   protected void onPause() {
      super.onPause();
      mPresenter.closeDatabases();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.task_list_menu, menu);
      String defaultListId = getStringShP(Co.DEFAULT_LIST_ID_KEY, null);
      if (defaultListId != null &&
            defaultListId.equals(getStringShP(Co.CURRENT_LIST_ID, null))) {
         menu.findItem(R.id.deleteList).setEnabled(false);
      } else {
         menu.findItem(R.id.deleteList).setEnabled(true);
      }
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
               fab.setOnClickListener(MainActivity.this);
               LocalTask task = (LocalTask) resultIntent.getExtras().getSerializable(Co.LOCAL_TASK);
               if (task != null) {
                  String listId = getStringShP(Co.CURRENT_LIST_ID, null);
                  long reminderInMillis = task.getReminder();
                  if (reminderInMillis != 0) {
                     Calendar reminderCalendarObject = Calendar.getInstance();
                     reminderCalendarObject.setTimeInMillis(reminderInMillis);
                     AlarmHelper.setOrUpdateAlarm(task, this);
                  } else {
                     AlarmHelper.cancelTaskReminder(task, this);
                  }
                  if (task.getListId() == null) {
                     task.setListId(listId);
                  }
                  task.setLocalModify();
//                  task.setSyncStatus(task.getSyncStatus() != Co.NOT_SYNCED ? Co.EDITED_NOT_SYNCED : Co.NOT_SYNCED);
                  mPresenter.updateExistingTaskFromLocalTask(task);
                  if (task.getDue() != 0 && getBooleanShP(Co.DEFAULT_REMINDER_PREF_KEY, false)) {
                     Calendar dueDate = Calendar.getInstance();
                     dueDate.setTimeInMillis(task.getDue());
                     AlarmHelper.setOrUpdateDefaultRemindersForTask(this, task);
                  } else {
                     if (AlarmHelper.isDefaultAlarmSet(this, task.getIntId())) {
                        List<LocalTask> tasks = new ArrayList<>();
                        tasks.add(task);
                        AlarmHelper.cancelDefaultRemindersForTasks(this, tasks);
                     }
                  }
                  adapter.updateItem(task, resultIntent.getIntExtra(Co.ADAPTER_POSITION, -1));
                  if (!resultIntent.hasExtra(Co.NO_API_EDIT)) {
                     mPresenter.editTaskInServer(task);
                  }
               }


               // TASK ADDED
            } else if (resultIntent.hasExtra(Co.NEW_TASK)) {
               LocalTask task = (LocalTask) resultIntent.getExtras().getSerializable(Co.LOCAL_TASK);
               if (task != null) {
                  task.setListId(getStringShP(Co.CURRENT_LIST_ID, null));
                  task.setListIntId(getIntShP(Co.CURRENT_LIST_INT_ID, -1));
                  int taskIntId = mPresenter.addTaskToDatabase(task);
                  task.setIntId(taskIntId);
                  if (taskIntId > 0) {
                     adapter.addItem(task, 0);
                     if (task.getListId() != null) {
                        mPresenter.addTask(task);
                     }
                  }
                  if (task.getReminder() != 0) {
                     AlarmHelper.setOrUpdateAlarm(task, this);
                  }
                  if (task.getDue() != 0 && getBooleanShP(Co.DEFAULT_REMINDER_PREF_KEY, false)) {
                     AlarmHelper.setOrUpdateDefaultRemindersForTask(this, task);
                  }
                  updateTaskCounterForDrawer(task.getListIntId(), null);
               }
            }

         }
      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {

      switch (item.getItemId()) {

         case R.id.refresh:
            refresh();
            break;

         case R.id.editList:
            showEditListDialog();
            break;

         case R.id.deleteList:
            showDeleteListConfirmationDialog();
            break;

         case R.id.test:
            List<LocalTask> tasks = mPresenter.getTasksFromList(getIntShP(Co.CURRENT_LIST_INT_ID, -1));
            Collections.sort(tasks, new Comparator<LocalTask>() {
               @Override
               public int compare(LocalTask o1, LocalTask o2) {
                  return (int) (o2.getDue() - o1.getDue());
               }
            });
            for (int i = 0; i < tasks.size(); i++) {
               Log.d(TAG, tasks.get(i).getTitle() + " - " +
                     DateHelper.millisToRelativeDateOnly(this, tasks.get(i).getDue()));
            }
            break;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      String defaultListId = getStringShP(Co.DEFAULT_LIST_ID_KEY, null);
      if (defaultListId != null &&
            defaultListId.equals(getStringShP(Co.CURRENT_LIST_ID, null))) {
         menu.findItem(R.id.deleteList).setEnabled(false);
      } else {
         menu.findItem(R.id.deleteList).setEnabled(true);
      }
      return true;
   }
   ///----------------------------OTHER--------------------------//


   @Override
   public void showToast(String msg) {
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
   }

   @Override
   public Context getContext() {
      return this.getApplicationContext();
   }

   @Override
   public String getStringShP(String key, String defaultValue) {
      SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());

      return prefs.getString(key, defaultValue);
   }

   @Override
   public boolean getBooleanShP(String key, boolean defaultValue) {
      SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
      return prefs.getBoolean(key, defaultValue);
   }

   @Override
   public int getIntShP(String key, int defaultValue) {
      SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
      return prefs.getInt(key, defaultValue);
   }

   @Override
   public void saveIntShP(String key, int value) {
      SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
      SharedPreferences.Editor editor = prefs.edit();
      editor.putInt(key, value);
      editor.apply();
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
   public void updateItem(LocalTask syncedLocalTask) {
      adapter.updateItem(syncedLocalTask, -1);
   }

   @Override
   public void lockScreenOrientation() {
      int currentOrientation = getResources().getConfiguration().orientation;
      if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      } else {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      }
   }

   @Override
   public void unlockScreenOrientation() {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
   }


}



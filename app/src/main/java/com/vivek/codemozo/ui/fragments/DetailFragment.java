package com.vivek.codemozo.ui.fragments;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.vivek.codemozo.R;
import com.vivek.codemozo.alarms.NotificationAlarm;
import com.vivek.codemozo.db.entry.NotificationEntry;
import com.vivek.codemozo.events.CalendarPermissionGrantedEvent;
import com.vivek.codemozo.events.SnackBarMessageDetailFragmentEvent;
import com.vivek.codemozo.db.DBContract;
import com.vivek.codemozo.handler.DBQueryHandler;
import com.vivek.codemozo.model.Contest;
import com.vivek.codemozo.model.realm.NotificationModel;
import com.vivek.codemozo.progressbar.TimerProgressBar;
import com.vivek.codemozo.realmdb.RealmConfigurations;
import com.vivek.codemozo.utils.AppUtils;
import com.vivek.codemozo.utils.CalendarUtils;
import com.vivek.codemozo.utils.CustomDate;
import com.vivek.codemozo.utils.DateUtils;
import com.vivek.codemozo.utils.Debug;
import com.vivek.codemozo.utils.TimerUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static com.vivek.codemozo.utils.AppUtils.showSnackBarMessage;


public class DetailFragment extends BaseFragment implements DBQueryHandler.OnQueryCompleteListener {

    private static final int VERIFY_EVENT_ADDED_TO_CALENDAR_QUERY = 3000;
    private static final int EVENT_DELETE_FROM_CALENDAR = 3001;
    private static final int EVENT_INSERT_TO_CALENDAR = 3002;
    private static final int VERIFY_NOTIFICATION_FOR_EVENT = 3003;
    private static final int DELETE_NOTIFICATION_FOR_EVENT = 3004;
    private static final int INSERT_NOTIFICATION_FOR_EVENT = 3005;

    private static final int CALENDAR_EVENT_VALUE_NOT_RETRIEVED = -1;
    private static final int CALENDAR_EVENT_VALUE_NOT_PRESENT = -2;
    private static final int NOTIFICATION_EVENT_VALUE_NOT_RETRIEVED = -3;
    private static final int NOTIFICATION_EVENT_VALUE_NOT_PRESENT = -4;

    public static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 45;

    private boolean CALENDAR_BUTTON_ACTIVE = false;
    private boolean NOTIFICATION_BUTTON_ACTIVE = false;

    long calendarId = CALENDAR_EVENT_VALUE_NOT_RETRIEVED;
    long notificationId = NOTIFICATION_EVENT_VALUE_NOT_RETRIEVED;
    long notificationTime = -1;

    Contest contest;
    long startTime = -1;
    long endTime = -1;
    boolean isTimerPaused = false;
    private DBQueryHandler mDBQueryHandler;
    long calendarEventId = CALENDAR_EVENT_VALUE_NOT_RETRIEVED;
    private Unbinder unbinder;
    private Realm mRealm;


    @BindView(R.id.detail_fragment_coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.contest_title_tv)
    TextView contestTitleTv;
    @BindView(R.id.contest_website_tv)
    TextView contestWebsiteTv;
    @BindView(R.id.status_tv)
    TextView statusTv;

    @BindView(R.id.contest_start_time_tv)
    TextView contestStartTime;
    @BindView(R.id.contest_start_time_ampm)
    TextView contestStartAmPm;
    @BindView(R.id.contest_start_date_tv)
    TextView contestStartDate;

    @BindView(R.id.contest_end_time_tv)
    TextView contestEndTime;
    @BindView(R.id.contest_end_time_ampm)
    TextView contestEndAmPm;
    @BindView(R.id.contest_end_date_tv)
    TextView contestEndDate;

    @BindView(R.id.contest_timezone_tv)
    TextView timeZoneTv;

    @BindView(R.id.calendar_image)
    Button calendarButton;
    @BindView(R.id.notification_image)
    Button notificationButton;
    @BindView(R.id.share_image)
    Button shareButton;
    @BindView(R.id.web_image)
    Button websiteButton;

    @BindView(R.id.progress_bar_days)
    TimerProgressBar daysProgressBar;
    @BindView(R.id.progress_bar_hours)
    TimerProgressBar hoursProgressBar;
    @BindView(R.id.progress_bar_mins)
    TimerProgressBar minsProgressBar;
    @BindView(R.id.progress_bar_sec)
    TimerProgressBar secProgressBar;

    public DetailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            contest = bundle.getParcelable(AppUtils.CONTEST_KEY);
        } else {
            Debug.e("Should not happen. DetailFragment needs to have contestId", false);
        }
        isTimerPaused = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        unbinder = ButterKnife.bind(this, view);
        mRealm = Realm.getInstance(RealmConfigurations.getNotificationConfiguration());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateDetails();
    }

    private void updateDetails() {
        if (contest != null) {
            contestWebsiteTv.setText(contest.getWebsite().getName());

            contestTitleTv.setText(contest.getEvent());
            TimeZone tz = TimeZone.getDefault();
            timeZoneTv.setText(tz.getID() + " " + tz.getDisplayName(false, TimeZone.SHORT));


            startTime = DateUtils.getEpochTime(contest.getStart());
            endTime = DateUtils.getEpochTime(contest.getEnd());
            final CustomDate startDate = new CustomDate(DateUtils.epochToDateTimeLocalShow(startTime));
            final CustomDate endDate = new CustomDate(DateUtils.epochToDateTimeLocalShow(endTime));

            final String starts = "Starts : " + startDate.getTime() + " " + startDate.getAmPm() + " " +
                    "" + startDate.getDateMonthYear();
            final String ends = "Ends : " + endDate.getTime() + " " + endDate.getAmPm() + " " +
                    "" + endDate.getDateMonthYear();

            contestStartTime.setText(startDate.getTime());
            contestStartAmPm.setText(startDate.getAmPm());
            contestStartDate.setText(startDate.getDateMonthYear());

            contestEndTime.setText(endDate.getTime());
            contestEndAmPm.setText(endDate.getAmPm());
            contestEndDate.setText(endDate.getDateMonthYear());

            statusTv.setText(DateUtils.getContestStatusString(startTime, endTime));

            final int resourceId = contest.getWebsite().getId();
            String resourceName = AppUtils.getResourceName(getActivity(), resourceId);
            final String shortResourceName = AppUtils.getGoodResourceName(resourceName);
            contestWebsiteTv.setText(shortResourceName);


            calendarButton.setOnClickListener(v -> {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_CALENDAR);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    Debug.e("dont have permission", false);
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_CALENDAR},
                            MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
                    CALENDAR_BUTTON_ACTIVE = false;

                    calendarButton.setCompoundDrawablesWithIntrinsicBounds(R
                            .mipmap.calendar_permission, 0, 0, 0);
                    calendarButton.setText(getString(R.string.permission_calendar));
                    calendarId = CALENDAR_EVENT_VALUE_NOT_RETRIEVED;
                    return;
                }
                int contestStatus = DateUtils.getContestStatus(startTime, endTime);
                if (contestStatus == AppUtils.STATUS_CONTEST_FUTURE) {
                    if (CALENDAR_BUTTON_ACTIVE) {
                        showSnackBarMessage(getActivity(), mCoordinatorLayout, String.format(getString(R.string.fetch_contest_calendar_status),
                                contest.getEvent()));
                        return;
                    }
                    toggleEventStatusInCalendar();
                } else if (contestStatus == AppUtils.STATUS_CONTEST_LIVE) {
                    showSnackBarMessage(getActivity(), mCoordinatorLayout, String.format(getString(R.string.contest_started),
                            contest.getEvent(),
                            shortResourceName));
                } else {
                    showSnackBarMessage(getActivity(), mCoordinatorLayout, String.format(getActivity().getString(R.string.contest_ended),
                            contest.getEvent(),
                            shortResourceName));
                }
            });

            notificationButton.setOnClickListener(v -> {
                int contestStatus = DateUtils.getContestStatus(startTime, endTime);
                if (contestStatus == AppUtils.STATUS_CONTEST_FUTURE) {
                    if (NOTIFICATION_BUTTON_ACTIVE) {
                        showSnackBarMessage(getActivity(), mCoordinatorLayout, String.format(getString(R.string.fetch_contest_notification_status),
                                contest.getEvent()));
                        return;
                    }
                    toggleNotificationForEvent();
                } else if (contestStatus == AppUtils.STATUS_CONTEST_LIVE) {
                    showSnackBarMessage(getActivity(), mCoordinatorLayout, String.format(getString(R.string.contest_started),
                            contest.getEvent(),
                            shortResourceName));

                } else {
                    showSnackBarMessage(getActivity(), mCoordinatorLayout, String.format(getString(R.string.contest_ended),
                            contest.getEvent(),
                            shortResourceName));
                }

            });

            shareButton.setOnClickListener(v -> {
                String sb = "Checkout this contest!!\n" +
                        contest.getEvent() + "\n" +
                        "@ " + shortResourceName + "\n" +
                        starts + "\n" +
                        ends + "\n" +
                        "#" + getString(R.string.app_name);
                onShareClick(sb);
            });


            websiteButton.setOnClickListener(v -> AppUtils.openWebsite(getActivity(), mCoordinatorLayout, contest.getUrl()));

            secProgressBar.setRingRadiusRatio(0.75f);
            secProgressBar.setProgressRingBackgroundColor(Color.TRANSPARENT);
            secProgressBar.setProgressRingForegroundColor("#e300fc");


            int contestStatus = DateUtils.getContestStatus(startTime, endTime);
            if (contestStatus == AppUtils.STATUS_CONTEST_FUTURE) {
                if (calendarEventId == CALENDAR_EVENT_VALUE_NOT_RETRIEVED) {
                    getEventCalendarStatus();
                }
                if (notificationId == NOTIFICATION_EVENT_VALUE_NOT_RETRIEVED) {
                    getEventNotificationStatus();
                }
            }

        }
    }


    private void getEventCalendarStatus() {
        Debug.c();
        CALENDAR_BUTTON_ACTIVE = true;
        try {
            mDBQueryHandler.startQuery(
                    VERIFY_EVENT_ADDED_TO_CALENDAR_QUERY,
                    null,
                    CalendarContract.Events.CONTENT_URI,
                    null,
                    CalendarContract.Events.TITLE + " = ?",
                    new String[]{contest.getEvent()},
                    null
            );
        } catch (SecurityException ex) {
            Debug.c();
            CALENDAR_BUTTON_ACTIVE = false;
        }
    }

    private void toggleNotificationForEvent() {
        NOTIFICATION_BUTTON_ACTIVE = true;
        Debug.c();
        if (notificationId >= 0) {

            if(!mRealm.isInTransaction()){
                mRealm.beginTransaction();
                RealmResults<NotificationModel> notificationQuery = mRealm.where(NotificationModel.class).equalTo("_id", notificationId).findAll();
                notificationQuery.deleteFirstFromRealm();
                mRealm.commitTransaction();

                String msg = String.format(getString(R.string.delete_notification), contest
                        .getEvent());
                notificationButton.setCompoundDrawablesWithIntrinsicBounds(R
                        .mipmap.notification_on, 0, 0, 0);
                notificationButton.setText(getString(R.string.add_notification));
                notificationId = NOTIFICATION_EVENT_VALUE_NOT_PRESENT;
                deleteNotification();
                showSnackBarMessage(getActivity(), mCoordinatorLayout, msg);
                NOTIFICATION_BUTTON_ACTIVE = false;
               // mRealm.close();
            }

        } else {
            //insert
            /*notificationTime = System.currentTimeMillis() / 1000 + 10 * DateUtils
                    .SEC_IN_ONE_MINUTE;*/
            notificationTime = startTime - DateUtils.SEC_IN_ONE_HOUR;
            Number currentMax = mRealm.where(NotificationModel.class).max("_id");
            long nextId = 0;
            if (currentMax != null) {
                nextId = currentMax.longValue() + 1;
            }
            AtomicLong primaryKeyValue = new AtomicLong(nextId);
            Long id = primaryKeyValue.getAndIncrement();

            NotificationModel notificationModel = new NotificationModel();
            mRealm.beginTransaction();
            notificationModel.set_id(id);
            notificationModel.setContest_id(contest.getId());
            notificationModel.setNotify_time(notificationTime);
            mRealm.copyToRealmOrUpdate(notificationModel);
            mRealm.commitTransaction();
          //  mRealm.close();

            String msg;
                msg = String.format(getString(R.string.insert_notification), contest
                        .getEvent());
                notificationButton.setCompoundDrawablesWithIntrinsicBounds(R
                        .mipmap.notification_off, 0, 0, 0);
                notificationButton.setText(getString(R.string.remove_notification));
                notificationId = Long.valueOf(id);
                Debug.e("inserted : " + notificationId, false);
                addNotification();

            showSnackBarMessage(getActivity(), mCoordinatorLayout, msg);
            NOTIFICATION_BUTTON_ACTIVE = false;

           //ContentValues cv = contest.toNotificationEventContentValues(notificationTime);

        }
    }

    private void toggleEventStatusInCalendar() {
        CALENDAR_BUTTON_ACTIVE = true;
        if (calendarId < 0) {
            calendarId = CalendarUtils.getCalendarId(getActivity());
        }
        if (calendarId < 0) {
            showSnackBarMessage(getActivity(), mCoordinatorLayout, getString(R.string.no_calendar_account_found));
            CALENDAR_BUTTON_ACTIVE = false;
            return;
        }
        if (calendarEventId == CALENDAR_EVENT_VALUE_NOT_RETRIEVED) {
            Debug.e("toggle can be done but event id not present : ", false);
            //fetch it now
            try {
                Cursor cursor = getActivity().getContentResolver().query(
                        CalendarContract.Events.CONTENT_URI,
                        null,
                        CalendarContract.Events.TITLE + " = ?",
                        new String[]{contest.getEvent()},
                        null
                );
                processCalendarQuery(cursor);
            } catch (SecurityException ex) {
                Debug.c();
                showSnackBarMessage(getActivity(), mCoordinatorLayout, getString(R.string.calendar_permission_denied));
                CALENDAR_BUTTON_ACTIVE = false;
                return;
            }
        }
        Debug.e("toggle can be done : " + calendarId, false);
        if (calendarEventId > 0) {
            //added, need to delete it
            Debug.e("added, need to delete it : id: " + calendarEventId, false);
            if (calendarId >= 0) {
                mDBQueryHandler.startDelete(
                        EVENT_DELETE_FROM_CALENDAR,
                        null,
                        CalendarContract.Events.CONTENT_URI,
                        CalendarContract.Events._ID + " =? ",
                        new String[]{Long.toString(calendarEventId)}
                );
            } else {
                showSnackBarMessage(getActivity(), mCoordinatorLayout, getString(R.string.no_calendar_account_found));
                CALENDAR_BUTTON_ACTIVE = false;
            }
        } else if (calendarEventId == CALENDAR_EVENT_VALUE_NOT_PRESENT) {
            //not present, need to add it
            Debug.e("not present, need to add it", false);
            if (calendarId >= 0) {
                ContentValues cv = contest.toCalendarEventContentValues(calendarId);
                mDBQueryHandler.startInsert(
                        EVENT_INSERT_TO_CALENDAR,
                        null,
                        CalendarContract.Events.CONTENT_URI,
                        cv

                );
            } else {
                showSnackBarMessage(getActivity(), mCoordinatorLayout, getString(R.string.no_calendar_account_found));
                CALENDAR_BUTTON_ACTIVE = false;
            }
        } else if (calendarEventId == CALENDAR_EVENT_VALUE_NOT_RETRIEVED) {
            //should not happen
            Debug.c();
            CALENDAR_BUTTON_ACTIVE = false;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        isTimerPaused = false;
        configureTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        isTimerPaused = true;
    }

    public void onShareClick(String msg) {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText(msg)
                .getIntent(), getString(R.string.action_share)));
    }

    private void configureTimer() {
        if (startTime != -1 && endTime != -1) {
            int contestStatus = DateUtils.getContestStatus(startTime, endTime);
            final long timeNow = System.currentTimeMillis() / 1000;
            long diff = 0;
            if (contestStatus == AppUtils.STATUS_CONTEST_FUTURE) {
                diff = startTime - timeNow;
            } else if (contestStatus == AppUtils.STATUS_CONTEST_LIVE) {
                diff = endTime - timeNow;
            }
            diff = diff * 1000;
            Debug.e("diff : " + diff, false);
            if (diff > 0) {
                new CountDownTimer(diff, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (isTimerPaused) {
                            this.cancel();
                            return;
                        }
                        long secUntilFinished = millisUntilFinished / 1000;
                        TimerUtil timerUtil = new TimerUtil(secUntilFinished);
                        long daysLeft = timerUtil.getDays();
                        daysProgressBar.setMiddleText("" + daysLeft + "d", 100.0f * daysLeft / 365);
                        long hoursLeft = timerUtil.getHours();
                        hoursProgressBar.setMiddleText("" + hoursLeft + "h", 100.0f * hoursLeft / 24);
                        long minLeft = timerUtil.getMin();
                        minsProgressBar.setMiddleText("" + minLeft + "m", 100.0f * minLeft / 60);
                        long secLeft = timerUtil.getSec();
                        secProgressBar.setMiddleText("" + secLeft + "s", 100.0f * secLeft / 60);
                    }

                    @Override
                    public void onFinish() {
                        Debug.e("", false);
                    }
                }.start();
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        mDBQueryHandler = new DBQueryHandler(context.getContentResolver(), this);
        super.onAttach(context);
    }

    void processCalendarQuery(Cursor cursor) {
        Debug.c();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    calendarEventId = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events
                            ._ID));
                    calendarButton.setCompoundDrawablesWithIntrinsicBounds(R
                            .mipmap.calendar_added, 0, 0, 0);
                    calendarButton.setText(getString(R.string.remove_calendar));
                } else {
                    calendarButton.setCompoundDrawablesWithIntrinsicBounds(R
                            .mipmap.calendar_add, 0, 0, 0);
                    calendarButton.setText(getString(R.string.add_to_calendar));
                    calendarEventId = CALENDAR_EVENT_VALUE_NOT_PRESENT;
                }
                Debug.e("calendarEventId : " + calendarEventId, false);
            } finally {
                cursor.close();
            }
        }
    }

    void getEventNotificationStatus() {
        Debug.c();
        NotificationModel result = mRealm.where(NotificationModel.class).equalTo("contest_id", contest.getId()).findFirst();
        if(result!=null){
            notificationId = result.get_id();
        }
        if(notificationId != NOTIFICATION_EVENT_VALUE_NOT_RETRIEVED){
            notificationButton.setCompoundDrawablesWithIntrinsicBounds(R
                    .mipmap.notification_off, 0, 0, 0);
            notificationButton.setText(getString(R.string.remove_notification));
        } else {
            notificationButton.setCompoundDrawablesWithIntrinsicBounds(R
                    .mipmap.notification_on, 0, 0, 0);
            notificationButton.setText(getString(R.string.add_notification));
            notificationId = NOTIFICATION_EVENT_VALUE_NOT_PRESENT;
        }
        NOTIFICATION_BUTTON_ACTIVE = false;

    }

    @Override
    public void onQueryComplete(int token, Cursor cursor) {
        Debug.e("onQueryComplete token : " + token, false);
        switch (token) {
            case VERIFY_EVENT_ADDED_TO_CALENDAR_QUERY: {
                processCalendarQuery(cursor);
                CALENDAR_BUTTON_ACTIVE = false;
            }
            break;
        }
    }

    @Override
    public void onInsertComplete(int token, Uri uri) {
        Debug.e("onInsertComplete token : " + token + " uri : " + uri, false);
        switch (token) {
            case EVENT_INSERT_TO_CALENDAR: {
                String msg;
                if (uri != null) {
                    msg = String.format(getString(R.string.event_added_calendar), contest
                            .getEvent());
                    calendarButton.setCompoundDrawablesWithIntrinsicBounds(R
                            .mipmap.calendar_added, 0, 0, 0);
                    calendarButton.setText(getString(R.string.remove_calendar));
                    calendarEventId = Long.valueOf(uri.getLastPathSegment());
                    Debug.e("inserted : " + calendarEventId, false);
                } else {
                    msg = String.format(getString(R.string.insert_event_error), contest
                            .getEvent());
                }
                showSnackBarMessage(getActivity(), mCoordinatorLayout, msg);
                CALENDAR_BUTTON_ACTIVE = false;
            }
            break;
        }
    }

    @Override
    public void onDeleteComplete(int token, int result) {
        Debug.e("onDeleteComplete token : " + token + " result : " + result, false);
        switch (token) {
            case EVENT_DELETE_FROM_CALENDAR: {
                String msg;
                if (result == 1) {
                    msg = String.format(getString(R.string.delete_event), contest
                            .getEvent());
                    calendarButton.setCompoundDrawablesWithIntrinsicBounds(R
                            .mipmap.calendar_add, 0, 0, 0);
                    calendarButton.setText(getString(R.string.add_to_calendar));
                    calendarEventId = CALENDAR_EVENT_VALUE_NOT_PRESENT;
                } else {
                    msg = String.format(getString(R.string.delete_event_error), contest
                            .getEvent());
                }
                showSnackBarMessage(getActivity(), mCoordinatorLayout, msg);
                CALENDAR_BUTTON_ACTIVE = false;
            }
            break;
        }
    }

    @Override
    public void onUpdateComplete(int token, int result) {

    }

    @Subscribe
    public void onCalendarPermissionGrant(CalendarPermissionGrantedEvent event) {
        showSnackBarMessage(getActivity(), mCoordinatorLayout, getString(R.string.calendar_permission_granted));
        calendarId = CalendarUtils.getCalendarId(getActivity());
        getEventCalendarStatus();
    }

    @Subscribe
    public void onShowErrorMsg(SnackBarMessageDetailFragmentEvent event) {
        Debug.c();
        showSnackBarMessage(getActivity(), mCoordinatorLayout, event.getMsg());
    }

    private void addNotification() {
        Debug.c();
        if (notificationTime != -1) {
            Debug.e("" + notificationTime, false);
            Intent alarmIntent = new Intent(getActivity().getApplicationContext(), NotificationAlarm.class);
            alarmIntent.putExtra(AppUtils.CONTEST_KEY, contest);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), contest.getId(),
                    alarmIntent, PendingIntent
                            .FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService
                    (Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime * 1000, pendingIntent);
            notificationTime = -1;
        }
    }

    private void deleteNotification() {
        Debug.c();
        Intent alarmIntent = new Intent(getActivity().getApplicationContext(), NotificationAlarm.class);
        alarmIntent.putExtra(AppUtils.CONTEST_KEY, contest);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), contest.getId(),
                alarmIntent, PendingIntent
                        .FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService
                (Context.ALARM_SERVICE);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }
}

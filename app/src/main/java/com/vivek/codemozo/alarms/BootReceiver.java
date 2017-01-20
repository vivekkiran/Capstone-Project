package com.vivek.codemozo.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vivek.codemozo.model.realm.NotificationModel;
import com.vivek.codemozo.realmdb.RealmConfigurations;
import com.vivek.codemozo.utils.AppUtils;
import com.vivek.codemozo.utils.Debug;

import io.realm.Realm;
import io.realm.RealmResults;


public class BootReceiver extends BroadcastReceiver {
    private Realm mRealm;

    @Override
    public void onReceive(Context context, Intent intent) {
        mRealm = Realm.getInstance(RealmConfigurations.getNotificationConfiguration());
        Debug.c();
        RealmResults<NotificationModel> results = mRealm.where(NotificationModel.class).findAll();
        // iterate over all notifications and set alarms
        if (results != null) {
            mRealm.beginTransaction();
            for (NotificationModel notificationModel : results) {
                long notificationId = notificationModel.get_id();
                long contestId = notificationModel.getContest_id();
                long timeNotification = notificationModel.getNotify_time();
                if (timeNotification > System.currentTimeMillis() / 1000) {
                    addNotification(context, notificationModel, timeNotification);
                    RealmResults<NotificationModel> notificationQuery = mRealm.where(NotificationModel.class).equalTo("_id", Long.toString(notificationId)).findAll();
                    notificationQuery.deleteAllFromRealm();
                }
                mRealm.commitTransaction();
                mRealm.close();
                Debug.e("deleted : " + notificationId, false);
            }
        }
    }


    private void addNotification(Context context, NotificationModel contest, long time) {
        Debug.e("time : " + time + " contest: " + contest.getContest_id(), false);
        Intent alarmIntent = new Intent(context, NotificationAlarm.class);
        alarmIntent.putExtra(AppUtils.CONTEST_KEY, contest.getContest_id());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, contest.getContest_id(),
                alarmIntent, PendingIntent
                        .FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService
                (Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time * 1000, pendingIntent);
    }
}

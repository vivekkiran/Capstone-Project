package com.vivek.codemozo.alarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.Spanned;

import com.vivek.codemozo.R;
import com.vivek.codemozo.model.Contest;
import com.vivek.codemozo.model.realm.NotificationModel;
import com.vivek.codemozo.realmdb.RealmConfigurations;
import com.vivek.codemozo.ui.activities.MainActivity;
import com.vivek.codemozo.utils.AppUtils;
import com.vivek.codemozo.utils.CustomDate;
import com.vivek.codemozo.utils.DateUtils;
import com.vivek.codemozo.utils.Debug;
import com.vivek.codemozo.utils.HtmlUtils;

import io.realm.Realm;
import io.realm.RealmResults;


public class NotificationAlarm extends BroadcastReceiver {

    private Realm mRealm;

    @Override
    public void onReceive(Context context, Intent intent) {
        Contest contest = intent.getParcelableExtra(AppUtils.CONTEST_KEY);
        Debug.e(contest.getEvent(), false);
        Intent newIntent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, newIntent, 0);
        mRealm = Realm.getInstance(RealmConfigurations.getNotificationConfiguration());

        Notification n;

        final int resourceId = contest.getWebsite().getId();
        String resourceName = AppUtils.getResourceName(context, resourceId);
        final String shortResourceName = AppUtils.getGoodResourceName(resourceName);

        final long starTime = DateUtils.getEpochTime(contest.getStart());
        final CustomDate startDate = new CustomDate(DateUtils.epochToDateTimeLocalShow(starTime));

        Spanned msg = HtmlUtils.fromHtml(String.format(context.getString(R.string.notification_string),
                shortResourceName, startDate.getFullTime()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            n = new NotificationCompat.Builder(context)
                    .setTicker(context.getString(R.string.app_name))
                    .setContentTitle(contest.getEvent())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentText(msg)
                    .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .setSmallIcon(R.mipmap.icon_notification)
                    .setContentIntent(pIntent)
                    .build();
        } else {
            n = new NotificationCompat.Builder(context)
                    .setTicker(context.getString(R.string.app_name))
                    .setContentTitle(contest.getEvent())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentText(msg)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pIntent)
                    .build();
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(contest.getId(), n);


        mRealm.beginTransaction();
        RealmResults<NotificationModel> notificationQuery = mRealm.where(NotificationModel.class).equalTo("_id", contest.getId()).findAll();
        notificationQuery.deleteFirstFromRealm();
        mRealm.commitTransaction();
        mRealm.close();
    }
}

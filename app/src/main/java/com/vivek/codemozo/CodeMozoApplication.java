package com.vivek.codemozo;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.vivek.codemozo.realmdb.RealmConfigurations;
import com.vivek.codemozo.sync.CodeMozoSyncAdapter;
import com.vivek.codemozo.utils.AppUtils;
import com.vivek.codemozo.utils.Debug;
import com.vivek.codemozo.utils.UserPreferences;

import io.realm.Realm;


public class CodeMozoApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        Realm.setDefaultConfiguration(RealmConfigurations.getDefaultConfiguration());
        AppUtils.cacheResources(this);
        checkAndSync();
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));

    }

    public void checkAndSync() {
        long lastSyncTime = UserPreferences.getInstance(this)
                .readValue(AppUtils.LAST_SYNC_PERFORMED, AppUtils
                        .LAST_SYNC_PERFORMED_DEFAULT_VALUE);

        long currTime = System.currentTimeMillis() / 1000;
        if (currTime - lastSyncTime > AppUtils.SIX_HOURS) {
            CodeMozoSyncAdapter.syncImmediately(this);
        }
    }

    public static void shouldNotHappen(String msg) {
        Debug.e(msg, false);
    }

}

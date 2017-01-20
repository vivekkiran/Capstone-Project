package com.vivek.codemozo.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;


import com.vivek.codemozo.BuildConfig;
import com.vivek.codemozo.R;
import com.vivek.codemozo.db.entry.ContestEntry;
import com.vivek.codemozo.db.entry.ResourceEntry;
import com.vivek.codemozo.model.Contest;
import com.vivek.codemozo.model.Website;
import com.vivek.codemozo.rest.RestApiClient;
import com.vivek.codemozo.utils.AppUtils;
import com.vivek.codemozo.utils.DateUtils;
import com.vivek.codemozo.utils.Debug;
import com.vivek.codemozo.utils.UserPreferences;
import com.vivek.codemozo.widget.CodeMozoWidget;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.schedulers.Schedulers;


public class CodeMozoSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 6 * (int) DateUtils.SEC_IN_ONE_HOUR;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;


    /**
     * Creates an {@link AbstractThreadedSyncAdapter}.
     *
     * @param context        the {@link Context} that this is running within.
     * @param autoInitialize if true then sync requests that have
     *                       {@link ContentResolver#SYNC_EXTRAS_INITIALIZE} set will be internally handled by
     *                       {@link AbstractThreadedSyncAdapter} by calling
     *                       {@link ContentResolver#setIsSyncable(Account, String, int)} with 1 if it
     */
    public CodeMozoSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Creates an {@link AbstractThreadedSyncAdapter}.
     *
     * @param context            the {@link Context} that this is running within.
     * @param autoInitialize     if true then sync requests that have
     *                           {@link ContentResolver#SYNC_EXTRAS_INITIALIZE} set will be internally handled by
     *                           {@link AbstractThreadedSyncAdapter} by calling
     *                           {@link ContentResolver#setIsSyncable(Account, String, int)} with 1 if it
     *                           is currently set to <0.
     * @param allowParallelSyncs if true then allow syncs for different accounts to run
     *                           at the same time, each in their own thread. This must be consistent with the setting
     */
    public CodeMozoSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    /**
     * Perform a sync for this account. SyncAdapter-specific parameters may
     * be specified in extras, which is guaranteed to not be null. Invocations
     * of this method are guaranteed to be serialized.
     *
     * @param account    the account that should be synced
     * @param extras     SyncAdapter-specific parameters
     * @param authority  the authority of this sync request
     * @param provider   a ContentProviderClient that points to the ContentProvider for this
     *                   authority
     * @param syncResult SyncAdapter-specific parameters
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Debug.c();
        fetchResourceInfo();
        fetchContests();
    }

    private void deleteOldContests() {
        long curr = System.currentTimeMillis() / 1000;
        long back = curr - AppUtils.ONE_WEEK;
        getContext().getContentResolver().delete(ContestEntry.CONTENT_URI_ALL,
                ContestEntry.CONTEST_END_COL + " <= ?",
                new String[]{Long.toString(back)});

        UserPreferences.getInstance(getContext())
                .writeValue(AppUtils.LAST_SYNC_PERFORMED, curr);

    }

    private void fetchResourceInfo() {
        Debug.c();
        RestApiClient.getRestApiService()
                .getResourceList(BuildConfig.API_USERNAME, BuildConfig.API_KEY)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Website.Response>() {
                    @Override
                    public void onCompleted() {
                        Debug.c();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Debug.e("Error: " + e.getMessage(), false);
                    }

                    @Override
                    public void onNext(Website.Response response) {
                        Debug.c();
                        List<Website> websites = response.websites;
                        List<ContentValues> list = new ArrayList<>();
                        for (Website website : websites) {
                            list.add(website.getContentValues());
                        }
                        Debug.e("resources adding : " + websites.size(), false);
                        ContentValues[] insert_data = new ContentValues[list.size()];
                        list.toArray(insert_data);
                        getContext().getContentResolver().bulkInsert(
                                ResourceEntry.CONTENT_URI_ALL, insert_data);
                        AppUtils.cacheResources(getContext());
                    }
                });
    }

    private void fetchContests() {
        Debug.c();
        long time = System.currentTimeMillis() / 1000;
        long lastSync = UserPreferences.getInstance(getContext())
                .readValue(AppUtils.LAST_SYNC_PERFORMED, AppUtils.LAST_SYNC_PERFORMED_DEFAULT_VALUE);
        if (lastSync == AppUtils.LAST_SYNC_PERFORMED_DEFAULT_VALUE) {
            time = time - 2 * AppUtils.ONE_DAY;
        }
        String date = DateUtils.epochToDateTimeGmt(time);
        RestApiClient.getRestApiService()
                .getContestsList(300, date, "end", BuildConfig
                                .API_USERNAME,
                        BuildConfig.API_KEY)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Contest.Response>() {
                    @Override
                    public void onCompleted() {
                        Debug.c();
                        Intent intent = new Intent(getContext(), CodeMozoWidget.class);
                        intent.setAction(AppUtils.BROADCAST_DATA_UPDATED);
                        getContext().sendBroadcast(intent);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Debug.e("Error: " + e.getMessage(), false);
                    }

                    @Override
                    public void onNext(Contest.Response response) {
                        Debug.c();
                        List<Contest> contests = response.contests;
                        List<ContentValues> list = new ArrayList<>();
                        for (Contest contest : contests) {
                            list.add(contest.getContentValues());
                        }
                        Debug.e("contests adding : " + contests.size(), false);
                        ContentValues[] insert_data = new ContentValues[list.size()];
                        list.toArray(insert_data);
                        getContext().getContentResolver().bulkInsert(
                                ContestEntry.CONTENT_URI_ALL, insert_data);

                        deleteOldContests();
                    }
                });
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.contentauthority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.contentauthority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        CodeMozoSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string
                .contentauthority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}

package com.vivek.codemozo.realmdb;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by devenv on 1/20/17.
 */

public class RealmConfigurations {
    //TODO: Experiment with RealmDB, Does not Support ContentProvider Yet, Try with MatrixCursor, Only Notifications Storage is implemented in Realm
    public static RealmConfiguration getDefaultConfiguration() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        return realmConfiguration;
    }

    public static RealmConfiguration getContestConfiguration(){
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("contest.realm")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        return realmConfiguration;
    }

    public static RealmConfiguration getNotificationConfiguration(){
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("notification.realm")
                .schemaVersion(0)
                .modules(Realm.getDefaultModule())
                .deleteRealmIfMigrationNeeded()
                .build();
        return realmConfiguration;
    }

    public static RealmConfiguration getResourceConfiguration(){
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("resource.realm")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        return realmConfiguration;
    }

}

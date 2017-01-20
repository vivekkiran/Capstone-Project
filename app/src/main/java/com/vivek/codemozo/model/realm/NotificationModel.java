package com.vivek.codemozo.model.realm;

import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by devenv on 1/20/17.
 */

public class NotificationModel extends RealmObject {
    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public int getContest_id() {
        return contest_id;
    }

    public void setContest_id(int contest_id) {
        this.contest_id = contest_id;
    }

    public Long getNotify_time() {
        return notify_time;
    }

    public void setNotify_time(Long notify_time) {
        this.notify_time = notify_time;
    }

    public Long getStart_time() {
        return start_time;
    }

    public void setStart_time(Long start_time) {
        this.start_time = start_time;
    }

    @PrimaryKey
    Long _id;
    int contest_id;
    Long notify_time;
    Long start_time;
}
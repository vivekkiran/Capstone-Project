package com.vivek.codemozo.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;

import com.vivek.codemozo.db.Provider;
import com.vivek.codemozo.db.entry.ContestEntry;
import com.vivek.codemozo.db.entry.NotificationEntry;
import com.vivek.codemozo.utils.AppUtils;
import com.vivek.codemozo.utils.DateUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class Contest implements Parcelable {

    long duration;
    String end;
    String event;
    @SerializedName("href")
    String url;
    int id;
    @SerializedName("resource")
    Website website;
    String start;

    public Contest() {

    }

    @Override
    public String toString() {
        String sb = "Contest{\n" + "Id: " + getId() + "\n" +
                "Event: " + getEvent() + "\n" +
                "Start: " + getStart() + "\n" +
                "End: " + getEnd() + "\n" +
                "Website: " + getWebsite() +
                "}\n";
        return sb;
    }

    protected Contest(Parcel in) {
        duration = in.readLong();
        end = in.readString();
        event = in.readString();
        url = in.readString();
        id = in.readInt();
        website = in.readParcelable(Website.class.getClassLoader());
        start = in.readString();
    }

    public static final Creator<Contest> CREATOR = new Creator<Contest>() {
        @Override
        public Contest createFromParcel(Parcel in) {
            return new Contest(in);
        }

        @Override
        public Contest[] newArray(int size) {
            return new Contest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(duration);
        dest.writeString(end);
        dest.writeString(event);
        dest.writeString(url);
        dest.writeInt(id);
        dest.writeParcelable(website, flags);
        dest.writeString(start);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public ContentValues getContentValues() {
        ContentValues value = new ContentValues();
        value.put(ContestEntry.CONTEST_ID_COL, getId());
        value.put(ContestEntry.CONTEST_NAME_COL, getEvent());
        value.put(ContestEntry.CONTEST_URL_COL, getUrl());
        value.put(ContestEntry.CONTEST_RESOURCE_ID_COL, getWebsite().getId());
        value.put(ContestEntry.CONTEST_START_COL, DateUtils.getEpochTime(getStart()));
        value.put(ContestEntry.CONTEST_END_COL, DateUtils.getEpochTime(getEnd()));
        value.put(ContestEntry.CONTEST_DURATION_COL, getDuration());
        return value;
    }

    public static Contest cursorToContest(Context context, Cursor cursor) {
        Contest contest = new Contest();
        contest.setId(cursor.getInt(cursor.getColumnIndex(ContestEntry.CONTEST_ID_COL)));
        contest.setEvent(cursor.getString(cursor.getColumnIndex(ContestEntry.CONTEST_NAME_COL)));
        contest.setUrl(cursor.getString(cursor.getColumnIndex(ContestEntry.CONTEST_URL_COL)));
        Website website = new Website();
        int wid = cursor.getInt(cursor.getColumnIndex(ContestEntry.CONTEST_RESOURCE_ID_COL));
        website.setId(wid);
        website.setName(AppUtils.getResourceName(context, wid));
        contest.setWebsite(website);
        contest.setStart(DateUtils.epochToDateTimeGmt(cursor.getInt(cursor.getColumnIndex
                (ContestEntry.CONTEST_START_COL))));
        contest.setEnd(DateUtils.epochToDateTimeGmt(cursor.getInt(cursor.getColumnIndex
                (ContestEntry.CONTEST_END_COL))));
        contest.setDuration(cursor.getLong(cursor.getColumnIndex(ContestEntry.CONTEST_DURATION_COL)));
        return contest;
    }

    public ContentValues toCalendarEventContentValues(long id) {
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Events.CALENDAR_ID, id);
        cv.put(CalendarContract.Events.TITLE, getEvent());
        cv.put(CalendarContract.Events.DESCRIPTION, getEvent() + " @ " + getWebsite().getName());
        cv.put(CalendarContract.Events.EVENT_LOCATION, getWebsite().getName());
        cv.put(CalendarContract.Events.DTSTART, DateUtils.getEpochTime(getStart()) * 1000);
        cv.put(CalendarContract.Events.DTEND, DateUtils.getEpochTime(getEnd()) * 1000);
        cv.put(CalendarContract.Events.EVENT_TIMEZONE, "GMT");
        return cv;
    }

    public ContentValues toNotificationEventContentValues(long time){
        ContentValues cv = new ContentValues();
        cv.put(NotificationEntry.NOTIFICATION_CONTEST_ID_COL, getId());
        cv.put(NotificationEntry.NOTIFICATION_TIME_COL, time);
        cv.put(NotificationEntry.NOTIFICATION_CONTEST_START_TIME_COL, DateUtils
                .getEpochTime(getStart()));

        return cv;
    }

    public static final class Response {
        @Expose
        @SerializedName("meta")
        public Meta meta;

        @Expose
        @SerializedName("objects")
        public List<Contest> contests = new ArrayList<>();
    }
}

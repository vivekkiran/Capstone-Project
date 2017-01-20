package com.vivek.codemozo.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vivek.codemozo.R;
import com.vivek.codemozo.events.ContestClickEvent;
import com.vivek.codemozo.model.Contest;
import com.vivek.codemozo.utils.AppUtils;
import com.vivek.codemozo.utils.CustomDate;
import com.vivek.codemozo.utils.DateUtils;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ContestAdapter extends CursorAdapter {

    final LayoutInflater mInflator;
    Context mContext;

    public ContestAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflator = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View mItem = mInflator.inflate(R.layout.contest_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        return mItem;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        final Contest contest = Contest.cursorToContest(mContext, cursor);
        mHolder.contestTitleTv.setText(contest.getEvent());
        final int resourceId = contest.getWebsite().getId();
        String resourceName = AppUtils.getResourceName(context, resourceId);
        final String shortResourceName = AppUtils.getGoodResourceName(resourceName);
        mHolder.contestWebsiteTv.setText(shortResourceName);
        final long starTime = DateUtils.getEpochTime(contest.getStart());
        final long endTime = DateUtils.getEpochTime(contest.getEnd());
        final CustomDate startDate = new CustomDate(DateUtils.epochToDateTimeLocalShow(starTime));
        final CustomDate endDate = new CustomDate(DateUtils.epochToDateTimeLocalShow(endTime));

        mHolder.contestStartTime.setText(startDate.getTime());
        mHolder.contestStartAmPm.setText(startDate.getAmPm());
        mHolder.contestStartDate.setText(startDate.getDateMonthYear());

        mHolder.contestEndTime.setText(endDate.getTime());
        mHolder.contestEndAmPm.setText(endDate.getAmPm());
        mHolder.contestEndDate.setText(endDate.getDateMonthYear());

        mHolder.statusTv.setText(DateUtils.getTimeLeftForEnd(starTime, endTime));

        Glide.with(mContext)
                .load(AppUtils.getImageForResource(resourceName))
                .error(R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher)
                .crossFade()
                .into(mHolder.resourceImageView);

        mHolder.mCardView.setOnClickListener(v -> EventBus.getDefault().post(new ContestClickEvent(contest)));
    }


    class ViewHolder {

        @BindView(R.id.card_view)
        CardView mCardView;
        @BindView(R.id.contest_title_tv)
        TextView contestTitleTv;
        @BindView(R.id.contest_website_tv)
        TextView contestWebsiteTv;
        @BindView(R.id.resource_logo)
        ImageView resourceImageView;
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

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

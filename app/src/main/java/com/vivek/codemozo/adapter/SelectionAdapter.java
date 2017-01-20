package com.vivek.codemozo.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vivek.codemozo.R;
import com.vivek.codemozo.model.Website;
import com.vivek.codemozo.utils.AppUtils;
import com.vivek.codemozo.utils.Debug;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.ViewHolder> {

    private List<Website> mWebsitesList = new ArrayList<>();

    CompetitionSettingsChangedListener settingsChangedListener;

    Context mContext;

    public SelectionAdapter(Context context, List<Website> wlist,
                            CompetitionSettingsChangedListener
                                    mListner) {
        mContext = context;
        this.mWebsitesList = wlist;
        settingsChangedListener = mListner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_item_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Website website = mWebsitesList.get(position);
        final int show = website.getShow();
        holder.checkBox.setChecked(show != 0);
        holder.resourceName.setText(AppUtils.getGoodResourceName(website.getName()));

        final int resourceId = website.getId();

        Glide.with(mContext)
                .load(AppUtils.getImageForResource(website.getName()))
                .error(R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher)
                .crossFade()
                .into(holder.websiteLogo);

        if (position % 2 == 0) {
            holder.layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryLight));
        } else {
            holder.layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.green_50));
        }
        holder.layout.setOnClickListener(v -> updateCheckBox(holder.checkBox, resourceId, website));
    }

    public void updateCheckBox(CheckBox cb, final int resourceId, final Website website) {
        int toShow;
        if (cb.isChecked()) {
            cb.setChecked(false);
            toShow = 0;
        } else {
            cb.setChecked(true);
            toShow = 1;
        }
        Debug.i("resource : " + resourceId + " change : " + toShow, false);
        settingsChangedListener.settingsChanged(resourceId, toShow);
        website.setShow(toShow);
    }

    @Override
    public int getItemCount() {
        return mWebsitesList.size();
    }

    public void resetWebsiteList(@NonNull List<Website> data) {
        mWebsitesList = data;
        notifyDataSetChanged();
    }

    @NonNull
    public List<Website> getWebsiteList() {
        return mWebsitesList;
    }

    public void clearWebsites() {
        if (!mWebsitesList.isEmpty()) {
            mWebsitesList.clear();
            notifyDataSetChanged();
        }
    }

    public interface CompetitionSettingsChangedListener {
        void settingsChanged(int competitionId, int want);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.checkBox)
        CheckBox checkBox;
        @BindView(R.id.resource_name)
        TextView resourceName;

        @BindView(R.id.website_logo)
        ImageView websiteLogo;

        @BindView(R.id.linear_layout_settings)
        LinearLayout layout;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}


package com.vivek.codemozo.ui.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vivek.codemozo.R;
import com.vivek.codemozo.adapter.ContestAdapter;
import com.vivek.codemozo.db.entry.ContestEntry;
import com.vivek.codemozo.sync.CodeMozoSyncAdapter;
import com.vivek.codemozo.utils.DateUtils;
import com.vivek.codemozo.utils.Debug;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.vivek.codemozo.utils.AppUtils.showSnackBarMessage;


public class CurrentFragment extends BaseFragment {

    private static final int CURRENT_CONTESTS_LOADER = 0;

    @BindView(R.id.list_fragment_coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.contest_list)
    ListView mListView;
    @BindView(R.id.view_error)
    RelativeLayout errorLayout;
    @BindView(R.id.view_loading)
    RelativeLayout loadingLayout;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.error_message)
    TextView errorTv;

    private Unbinder unbinder;

    @OnClick(R.id.refresh)
    void onRefreshClick() {
        showSnackBarMessage(getActivity(), mCoordinatorLayout, getString(R.string.sync_data));
        CodeMozoSyncAdapter.syncImmediately(getActivity());
    }

    ContestAdapter mAdapter;

    public CurrentFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new ContestAdapter(getActivity(), null, 0);
        mListView.setAdapter(mAdapter);
        restartLoader(null);
    }


    private void restartLoader(Bundle bundle) {
        progressBar.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        errorTv.setVisibility(View.INVISIBLE);
        LoaderManager mLoaderManager = getActivity().getSupportLoaderManager();
        if (mLoaderManager.getLoader(CURRENT_CONTESTS_LOADER) == null) {
            mLoaderManager.initLoader(CURRENT_CONTESTS_LOADER, bundle, currentContestLoaderCallbacks);
        } else {
            mLoaderManager.restartLoader(CURRENT_CONTESTS_LOADER, bundle, currentContestLoaderCallbacks);
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> currentContestLoaderCallbacks
            = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Debug.c();
            Uri uri = ContestEntry.buildLiveContestUri(DateUtils.getCurrentEpochTime());
            return new CursorLoader(
                    getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    ContestEntry.CONTEST_END_COL + " ASC"
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Debug.c();
            mAdapter.swapCursor(cursor);
            progressBar.setVisibility(View.INVISIBLE);
            if (cursor != null && cursor.getCount() > 0) {
                errorTv.setVisibility(View.INVISIBLE);
                errorLayout.setVisibility(View.INVISIBLE);
                checkForNewLoad(cursor);
            } else {
                errorTv.setText(getActivity().getString(R.string.nomatch));
                errorTv.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.egg_empty, 0, 0);
                errorTv.setContentDescription(getActivity().getString(R.string.nomatch));
                errorTv.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            Debug.c();
            mAdapter.swapCursor(null);
        }
    };

}

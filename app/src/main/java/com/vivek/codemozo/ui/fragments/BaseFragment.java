package com.vivek.codemozo.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.vivek.codemozo.CodeMozoApplication;
import com.vivek.codemozo.events.ContestClickEvent;
import com.vivek.codemozo.model.Contest;
import com.vivek.codemozo.ui.activities.MainActivity;
import com.vivek.codemozo.utils.Debug;

import org.greenrobot.eventbus.EventBus;


public abstract class BaseFragment extends Fragment {

    private Toast mToast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CodeMozoApplication application = (CodeMozoApplication) getActivity().getApplication();
    }



    protected void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        mToast.show();
    }



    protected void showToast(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT);
        mToast.show();
    }

    protected void checkForNewLoad(Cursor cursor) {
        boolean twoPane = ((MainActivity) getActivity()).isTwoPane();
        Debug.e(" twoPane: " + twoPane, false);
        if (twoPane && cursor != null && cursor.moveToFirst()) {
            final Contest contest = Contest.cursorToContest(getActivity(), cursor);
            if (contest != null) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> EventBus.getDefault().post(new ContestClickEvent(contest)));
            }
        }
    }

}

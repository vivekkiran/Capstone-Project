package com.vivek.codemozo.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.vivek.codemozo.BuildConfig;
import com.vivek.codemozo.R;
import com.vivek.codemozo.events.CalendarPermissionGrantedEvent;
import com.vivek.codemozo.events.ContestClickEvent;
import com.vivek.codemozo.events.SettingsSaveEvent;
import com.vivek.codemozo.events.SnackBarMessageDetailFragmentEvent;
import com.vivek.codemozo.model.Contest;
import com.vivek.codemozo.ui.fragments.CreditsFragment;
import com.vivek.codemozo.ui.fragments.CurrentFragment;
import com.vivek.codemozo.ui.fragments.DetailFragment;
import com.vivek.codemozo.ui.fragments.PastFragment;
import com.vivek.codemozo.ui.fragments.SelectionFragment;
import com.vivek.codemozo.ui.fragments.UpcomingFragment;
import com.vivek.codemozo.utils.AppUtils;
import com.vivek.codemozo.utils.Debug;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    private static final long DRAWER_CLOSE_DELAY_MS = 150;
    private static final String NAV_ITEM_ID = "navItemId";
    private static final String TITLE = "title";

    boolean mTwoPane = false;

    private final Handler mDrawerActionHandler = new Handler();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.adView)
    AdView mAdView;

    ActionBarDrawerToggle mDrawerToggle;

    String mTitle;
    int mNavItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.c();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if (findViewById(R.id.right_content_frame) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }

        // load saved navigation state if present
        if (null == savedInstanceState) {
            mNavItemId = R.id.nav_current_menu;
            mTitle = getString(R.string.live_contests);
        } else {
            Debug.e("SavedInstance not null", false);
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
            mTitle = savedInstanceState.getString(TITLE);
        }

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTitle);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R
                .string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // getSupportActionBar().setTitle(R.string.app_name);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null) {
            navigate(mNavigationView.getMenu().findItem(mNavItemId));
        } else {
            mNavigationView.getMenu().findItem(mNavItemId).setChecked(true);
        }
    }

    @Subscribe
    public void settingsSaveEventTriggered(SettingsSaveEvent event) {
        Debug.c();
        navigate(mNavigationView.getMenu().findItem(R.id.nav_current_menu));
    }

    private void navigate(final MenuItem menuItem) {
        // perform the actual navigation logic, updating the main content fragment etc

        int itemId = menuItem.getItemId();
        Class fragmentClass = null;
        Fragment fragment = null;
        switch (itemId) {
            case R.id.nav_current_menu:
                fragmentClass = CurrentFragment.class;
                mTitle = getString(R.string.live_contests);
                break;
            case R.id.nav_upcoming_menu:
                fragmentClass = UpcomingFragment.class;
                mTitle = getString(R.string.upcoming_contests);
                break;
            case R.id.nav_past_menu:
                fragmentClass = PastFragment.class;
                mTitle = getString(R.string.past_contest);
                break;
            case R.id.nav_settings_menu:
                fragmentClass = SelectionFragment.class;
                mTitle = getString(R.string.nav_settings);
                break;
            case R.id.nav_share_menu:
                shareApp();
                break;
            case R.id.nav_rate_menu:
                rateApp();
                break;
            case R.id.nav_feedback_menu:
                sendEmail();
                break;
            case R.id.nav_open_source_menu:
                AppUtils.openWebsite(this, null, AppUtils.GIT_URL);
                break;
            case R.id.nav_credits_menu:
                fragmentClass = CreditsFragment.class;
                mTitle = getString(R.string.nav_credits);
                break;
            default:
                fragmentClass = CurrentFragment.class;
                mTitle = getString(R.string.live_contests);
        }

        if (fragmentClass != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            changeFragment(fragment, mTitle, itemId);
            menuItem.setChecked(true);
            mNavItemId = itemId;
        } else {
            mNavigationView.getMenu().findItem(mNavItemId).setChecked(true);
        }

    }

    private void changeFragment(Fragment fragment, String title, int id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (mTwoPane && fragment instanceof DetailFragment) {
            fragmentManager.beginTransaction()
                    .replace(R.id.right_content_frame, fragment)
                    .setBreadCrumbShortTitle(id)
                    .addToBackStack(mTitle)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.left_content_frame, fragment)
                    .setBreadCrumbShortTitle(id)
                    .addToBackStack(title)
                    .commit();

            setUpTitle(title);
        }
    }

    public void setUpTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        if (menuItem.getItemId() != mNavItemId) {
            // allow some time after closing the drawer before performing real navigation
            // so the user can see what is happening
            mDrawerActionHandler.postDelayed(() -> navigate(menuItem), DRAWER_CLOSE_DELAY_MS);
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.support.v7.appcompat.R.id.home) {
            return mDrawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            int pos = getSupportFragmentManager().getBackStackEntryCount();
            if (pos < 2) {
                finish();
            } else {
                String tr = getSupportFragmentManager().getBackStackEntryAt(pos - 2).getName();
                int id = getSupportFragmentManager().getBackStackEntryAt(pos - 2).getBreadCrumbShortTitleRes();
                mNavItemId = id;
                mNavigationView.getMenu().findItem(id).setChecked(true);
                setUpTitle(tr);
            }
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, mNavItemId);
        outState.putString(TITLE, mTitle);
    }


    @Subscribe
    public void onContestListItemClick(ContestClickEvent event) {
        Contest contest = event.getContest();
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppUtils.CONTEST_KEY, contest);
        detailFragment.setArguments(bundle);
        changeFragment(detailFragment, mTitle, mNavItemId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case DetailFragment.MY_PERMISSIONS_REQUEST_WRITE_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EventBus.getDefault().post(new CalendarPermissionGrantedEvent());
                } else {
                    String text = getString(R.string.calendar_permission_denied);
                    EventBus.getDefault().post(new SnackBarMessageDetailFragmentEvent(text));
                }
                break;
            }
        }
    }

    public boolean isTwoPane() {
        return mTwoPane;
    }

    private void sendEmail() {
        String subject = String.format(getString(R.string.email_subject), getString(R.string.app_name), BuildConfig.VERSION_NAME);
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{AppUtils.EMAIL_ID});
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, "");
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, getString(R.string.send_email)));
    }

    private void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            } catch (ActivityNotFoundException e2) {
                Debug.showToastShort(getString(R.string.no_playstore), this, true);
            }

        }
    }

    private void shareApp() {
        String sb = "Checkout this app.\n#" + getString(R.string.app_name) + "\n" +
                "https://play.google.com/store/apps/details?id=" + getPackageName();
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
        share.putExtra(Intent.EXTRA_TEXT, sb);
        startActivity(Intent.createChooser(share, getString(R.string.nav_share)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}


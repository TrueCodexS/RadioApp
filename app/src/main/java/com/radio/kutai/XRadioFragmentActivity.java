/*
 * Copyright (c) 2017. YPY Global - All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *         http://ypyglobal.com/sourcecode/policy
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.radio.kutai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.triggertrap.seekarc.SeekArc;
import com.radio.kutai.constants.IXRadioConstants;
import com.radio.kutai.dataMng.TotalDataManager;
import com.radio.kutai.fragment.XRadioListFragment;
import com.radio.kutai.gdpr.GDPRManager;
import com.radio.kutai.model.ConfigureModel;
import com.radio.kutai.model.RadioModel;
import com.radio.kutai.setting.XRadioSettingManager;
import com.radio.kutai.stream.manager.YPYStreamManager;
import com.radio.kutai.stream.service.YPYStreamService;
import com.radio.kutai.ypylibs.activity.YPYFragmentActivity;
import com.radio.kutai.ypylibs.ads.AdMobAdvertisement;
import com.radio.kutai.ypylibs.ads.FBAdvertisement;
import com.radio.kutai.ypylibs.ads.YPYAdvertisement;
import com.radio.kutai.ypylibs.executor.YPYExecutorSupplier;
import com.radio.kutai.ypylibs.model.AbstractModel;
import com.radio.kutai.ypylibs.utils.ApplicationUtils;
import com.radio.kutai.ypylibs.utils.ShareActionUtils;

import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;
import hotchemi.android.rate.AppRate;


import static com.radio.kutai.XRadioShowUrlActivity.KEY_HEADER;
import static com.radio.kutai.XRadioShowUrlActivity.KEY_SHOW_URL;
import static com.radio.kutai.stream.constant.IYPYStreamConstants.ACTION_UPDATE_SLEEP_MODE;

/**
 * @author: YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://radio.com
 * Created by YPY Global on 10/19/17.
 */

public abstract class XRadioFragmentActivity<T extends ViewBinding> extends YPYFragmentActivity implements IXRadioConstants {

    public TotalDataManager mTotalMng;
    public boolean isPausing;
    public Bundle mSavedInstance;
    protected T viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onDoBeforeSetView();
        super.onCreate(savedInstanceState);

        //init view binding
        viewBinding = getViewBinding();
        View view = viewBinding.getRoot();
        setContentView(view);

        this.mTotalMng = TotalDataManager.getInstance(getApplicationContext());
        this.mSavedInstance = savedInstanceState;

        createArrayFragment();
        onRestoreFragment(savedInstanceState);

        checkConfigure();
    }

    protected void onDoBeforeSetView() {
        setUpOverlayBackground(true);
    }

    public void checkConfigure() {
        ConfigureModel configureModel = mTotalMng.getConfigureModel();
        if (configureModel == null) {
            showProgressDialog();
            YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
                mTotalMng.readConfigure(this);
                runOnUiThread(() -> {
                    dismissProgressDialog();
                    onDoWhenDone();
                    processRightToLeft();
                });
            });
        }
        else {
            onDoWhenDone();
            processRightToLeft();
        }
    }

    @Override
    public YPYAdvertisement createAds() {
        ConfigureModel model = mTotalMng.getConfigureModel();
        if (model != null) {
            String bannerId = getString(R.string.banner_id);
            String interstitialId = getString(R.string.interstitial_id);
            String adType = getString(R.string.ad_type);

            if (adType.equalsIgnoreCase(AdMobAdvertisement.ADMOB_ADS)) {
                AdMobAdvertisement mAdmob = new AdMobAdvertisement(this, bannerId, interstitialId, ADMOB_TEST_DEVICE);
                mAdmob.initAds();
                return mAdmob;
            }
            else if (adType.equalsIgnoreCase(FBAdvertisement.FB_ADS)) {
                return new FBAdvertisement(this, bannerId, interstitialId, FACEBOOK_TEST_DEVICE);
            }
        }
        return null;

    }

    protected abstract T getViewBinding();
    protected abstract void updateBackground();


    public void setUpLayoutBanner() {
        setUpBottomBanner(R.id.layout_ads, SHOW_ADS);
    }

    @Override
    public void onDestroyData() {
        super.onDestroyData();
        mTotalMng.onDestroy();
        GDPRManager.getInstance().onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPausing = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPausing) {
            isPausing = false;
            onDoWhenResume();
        }
    }

    public void onDoWhenResume() {

    }

    public void onDoWhenDone() {
        updateBackground();
        onStartCreateAds();
        setLightStatusBar(USE_LIGHT_STATUS_BAR);
        setUpLayoutBanner();
        if (ApplicationUtils.isOnline(this)) {
            onDoWhenNetworkOn();
        }
        registerNetworkBroadcastReceiver(isNetworkOn -> {
            if (isNetworkOn) {
                setUpLayoutBanner();
                onDoWhenNetworkOn();
            }
            else {
                onDoWhenNetworkOff();
            }
        });
    }


    public void onDoWhenNetworkOn() {
        if (mAdvertisement != null) {
            mAdvertisement.setUpLoopInterstitial();
        }
    }

    public void onDoWhenNetworkOff() {

    }


    public void showAppRate() {
        if (!XRadioSettingManager.getRateApp(this)) {
            AppRate.with(this).setInstallDays(NUMBER_INSTALL_DAYS) // default 10, 0 means install day.
                    .setLaunchTimes(NUMBER_LAUNCH_TIMES) // default 10
                    .setRemindInterval(REMIND_TIME_INTERVAL) // default 1
                    .setShowLaterButton(true) // default true
                    .setShowNeverButton(false) // default true
                    .setDebug(false).setOnClickButtonListener(which -> {
                if (which == -1) {
                    XRadioSettingManager.setRateApp(this, true);
                    ShareActionUtils.goToUrl(this, String.format(URL_FORMAT_LINK_APP, getPackageName()));
                }
            }).monitor();

            AppRate.showRateDialogIfMeetsConditions(this);
        }

    }


    public void goToUrl(String name, String url) {
        if (USE_INTERNAL_WEB_BROWSER) {
            Intent mIntent = new Intent(this, XRadioShowUrlActivity.class);
            mIntent.putExtra(KEY_HEADER, name);
            mIntent.putExtra(KEY_SHOW_URL, url);
            startActivity(mIntent);
        }
        else {
            ShareActionUtils.goToUrl(this, url);
        }
    }

    public void updateFavorite(AbstractModel model, int type, boolean isFav) {
        try {
            if (model != null) {
                YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
                    if (!isFav) {
                        boolean b = mTotalMng.removeModelToCache(type, model);
                        if (b) {
                            model.setFavorite(false);
                            notifyFavorite(type, model.getId(), false);
                        }
                    }
                    else {
                        AbstractModel mObject = model.cloneObject();
                        if (mObject != null) {
                            mObject.setFavorite(true);
                            mTotalMng.addModelToCache(type, mObject);
                            model.setFavorite(true);
                            notifyFavorite(type, model.getId(), true);
                        }

                    }
                });

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void notifyFavorite(int type, long id, boolean isFav) {
        if (mListFragments != null && mListFragments.size() > 0) {
            for (Fragment mFragment : mListFragments) {
                if (mFragment instanceof XRadioListFragment) {
                    ((XRadioListFragment) mFragment).notifyFavorite(id, isFav);
                }
            }
        }
    }

    public void shareContent(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            String app = String.format(getString(R.string.info_content_share), getString(R.string.app_name), String.format(URL_FORMAT_LINK_APP, getPackageName()));
            ShareActionUtils.shareInfo(this, msg + "\n" + app);
        }
    }

    public void shareRadioModel(RadioModel radioObject) {
        if (radioObject != null) {
            String msg = radioObject.getShareStr();
            shareContent(msg);

        }
    }

    public void startMusicService(String action) {
        try {
            Intent mIntent1 = new Intent(this, YPYStreamService.class);
            mIntent1.setAction(getPackageName() + action);
            startService(mIntent1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showDialogSleepMode() {
        try {
            View mView = LayoutInflater.from(this).inflate(R.layout.dialog_sleep_time, null);
            final TextView mTvInfo = mView.findViewById(R.id.tv_info);
            if (XRadioSettingManager.getSleepMode(this) > 0) {
                mTvInfo.setText(String.format(getString(R.string.format_minutes), String.valueOf(XRadioSettingManager.getSleepMode(XRadioFragmentActivity.this))));
            }
            else {
                mTvInfo.setText(R.string.title_off);
            }

            SeekArc mCircularVir = mView.findViewById(R.id.seek_sleep);
            mCircularVir.setProgressColor(getResources().getColor(R.color.colorAccent));
            mCircularVir.setArcColor(getResources().getColor(R.color.dialog_color_secondary_text));
            mCircularVir.setMax((MAX_SLEEP_MODE - MIN_SLEEP_MODE) / STEP_SLEEP_MODE + 1);
            mCircularVir.setProgressWidth(getResources().getDimensionPixelOffset(R.dimen.tiny_margin));
            mCircularVir.setProgress(XRadioSettingManager.getSleepMode(this) / STEP_SLEEP_MODE);
            mCircularVir.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
                @Override
                public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                    try {
                        XRadioSettingManager.setSleepMode(XRadioFragmentActivity.this, progress * STEP_SLEEP_MODE);
                        if (progress == 0) {
                            mTvInfo.setText(R.string.title_off);
                        }
                        else {
                            mTvInfo.setText(String.format(getString(R.string.format_minutes), String.valueOf(XRadioSettingManager.getSleepMode(XRadioFragmentActivity.this))));
                        }
                        if (YPYStreamManager.getInstance().isPrepareDone()) {
                            startMusicService(ACTION_UPDATE_SLEEP_MODE);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onStartTrackingTouch(SeekArc seekArc) {

                }

                @Override
                public void onStopTrackingTouch(SeekArc seekArc) {

                }
            });

            MaterialDialog.Builder mBuilder = createBasicDialogBuilder(R.string.title_sleep_mode, R.string.title_done, 0);
            mBuilder.customView(mView, false);
            mBuilder.onPositive((dialog, which) -> {

            });
            final MaterialDialog mDialog = mBuilder.build();
            mDialog.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getStringTimer(long millis) {
        try {
            long second = (millis / 1000) % 60;
            long minute = (millis / (1000 * 60)) % 60;
            long hour = (millis / (1000 * 60 * 60)) % 24;
            String time;
            if (hour > 0) {
                time = String.format("%02d:%02d:%02d", hour, minute, second);
                return time;
            }
            else {
                time = String.format("%02d:%02d", minute, second);
            }
            return time;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void resetTimer() {
        try {
            if (RESET_TIMER) {
                XRadioSettingManager.setSleepMode(this, 0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isHavingListStream() {
        return YPYStreamManager.getInstance().isHavingList();
    }


}

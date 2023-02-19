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
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.radio.kutai.constants.IXRadioConstants;
import com.radio.kutai.dataMng.TotalDataManager;
import com.radio.kutai.databinding.ActivitySplashBinding;
import com.radio.kutai.gdpr.GDPRManager;
import com.radio.kutai.model.ConfigureModel;
import com.radio.kutai.model.RadioModel;
import com.radio.kutai.model.UIConfigModel;
import com.radio.kutai.setting.XRadioSettingManager;
import com.radio.kutai.ypylibs.activity.YPYSplashActivity;
import com.radio.kutai.ypylibs.ads.AdMobAdvertisement;
import com.radio.kutai.ypylibs.ads.FBAdvertisement;
import com.radio.kutai.ypylibs.ads.YPYAdvertisement;
import com.radio.kutai.ypylibs.executor.YPYExecutorSupplier;
import com.radio.kutai.ypylibs.task.IYPYCallback;
import com.radio.kutai.ypylibs.utils.ApplicationUtils;
import com.radio.kutai.ypylibs.utils.IOUtils;
import com.radio.kutai.ypylibs.utils.YPYLog;

import java.io.File;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.radio.com
 * @Date:Oct 20, 2017
 */

public class XRadioSplashActivity extends YPYSplashActivity<ActivitySplashBinding> implements IXRadioConstants {

    private TotalDataManager mTotalMng;
    private boolean isAllowShowAdsWhenAskingTerm =true;
    private final Handler mHandler = new Handler();

    @Override
    protected ActivitySplashBinding getViewBinding() {
        return ActivitySplashBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpOverlayBackground(true);
        YPYLog.setDebug(DEBUG);
        mTotalMng = TotalDataManager.getInstance(getApplicationContext());
        setUpBackground(this.viewBinding.layoutBg);
    }

    @Override
    public void onInitData() {
        this.viewBinding.progressBar.setVisibility(View.VISIBLE);
        this.viewBinding.progressBar.show();
        //Delay 1500 milliseconds before doing any task to ensure privacy of admob
        showDialogTerm(() -> mHandler.postDelayed(this::startLoad,1500));

    }

    private void startLoad(){
        YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            mTotalMng.readConfigure(this);
            runOnUiThread(() -> {
                setUpBackground(this.viewBinding.layoutBg);
                onStartCreateAds();
            });
            if(!SAVE_FAVORITE_SDCARD || isGrantAllPermission(getListPermissionNeedGrant())){
                mTotalMng.readAllCache(this);
            }
            runOnUiThread(this::checkGDPR);
        });
    }

    @Override
    public File getDirectoryCached() {
        return mTotalMng.getDirectoryCached(getApplicationContext());
    }

    @Override
    public String[] getListPermissionNeedGrant() {
        if(SAVE_FAVORITE_SDCARD && !IOUtils.hasAndroid10()){
            return LIST_PERMISSIONS;
        }
        return null;
    }



    @Override
    public YPYAdvertisement createAds() {
        ConfigureModel model = mTotalMng.getConfigureModel();
        if(model!=null){
            String bannerId= getString(R.string.banner_id);
            String interstitialId = getString(R.string.interstitial_id);
            String publisherId = getString(R.string.publisher_id);
            String adType = getString(R.string.ad_type);

            if(adType.equalsIgnoreCase(AdMobAdvertisement.ADMOB_ADS)){
                AdMobAdvertisement mAdmob = new AdMobAdvertisement(this,bannerId,interstitialId,ADMOB_TEST_DEVICE );
                mAdmob.initAds();
                if(!TextUtils.isEmpty(publisherId) && (!TextUtils.isEmpty(bannerId)  || !TextUtils.isEmpty(interstitialId))){
                    GDPRManager.getInstance().init(publisherId, URL_PRIVACY_POLICY, ADMOB_TEST_DEVICE);
                }
                return mAdmob;
            }
            else if(adType.equalsIgnoreCase(FBAdvertisement.FB_ADS)){
                return new FBAdvertisement(this, bannerId, interstitialId, FACEBOOK_TEST_DEVICE);
            }
        }
        return null;

    }

    public void showDialogTerm(IYPYCallback mCallback) {
        if (!XRadioSettingManager.getAgreeTerm(this) && !TextUtils.isEmpty(URL_TERM_OF_USE) && !TextUtils.isEmpty(URL_PRIVACY_POLICY)) {
            try {
                View mView = LayoutInflater.from(this).inflate(R.layout.dialog_term_of_condition, null);
                TextView mTv = mView.findViewById(R.id.tv_term_info);

                String format = getString(R.string.format_term_and_conditional);
                String msg = String.format(format, getString(R.string.app_name), URL_TERM_OF_USE, URL_PRIVACY_POLICY);

                Spanned result;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY);
                }
                else {
                    result = Html.fromHtml(msg);
                }
                mTv.setText(result);
                mTv.setMovementMethod(LinkMovementMethod.getInstance());
                MaterialDialog.Builder mBuilder = createBasicDialogBuilder(R.string.title_term_of_use, R.string.title_agree, R.string.title_no);
                mBuilder.canceledOnTouchOutside(false);
                mBuilder.titleGravity(GravityEnum.CENTER);
                mBuilder.customView(mView, true);
                boolean b = ApplicationUtils.isSupportRTL();
                if(b){
                    mTv.setGravity(Gravity.END);
                }
                mBuilder.onPositive((dialog, which) -> {
                    XRadioSettingManager.setAgreeTerm(XRadioSplashActivity.this, true);
                    isAllowShowAdsWhenAskingTerm = false;
                    if(mCallback!=null){
                        mCallback.onAction();
                    }
                });
                mBuilder.onNegative((dialog, which) -> {
                    onDestroyData();
                    finish();
                });
                mBuilder.keyListener((dialogInterface, i, keyEvent) -> i == KeyEvent.KEYCODE_BACK);
                mBuilder.show();

            }
            catch (Exception e) {
                e.printStackTrace();
                XRadioSettingManager.setAgreeTerm(XRadioSplashActivity.this, true);
            }
            return;
        }
        if(mCallback!=null){
            mCallback.onAction();
        }
    }

    @Override
    public void onDestroyData() {
        super.onDestroyData();
        GDPRManager.getInstance().onDestroy();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void checkGDPR(){
        if(!SAVE_FAVORITE_SDCARD || isGrantAllPermission(getListPermissionNeedGrant())){
            GDPRManager.getInstance().startCheck(this,mHandler,() -> goToMainActivity(isAllowShowAdsWhenAskingTerm));
        }
        else{
            goToMainActivity(isAllowShowAdsWhenAskingTerm);
        }
    }

    public void goToMainActivity(boolean isShowAds) {
        boolean isSingleRadio = mTotalMng.isSingleRadio();
        RadioModel mSingleRadio=mTotalMng.getSingRadioModel();
        if(isSingleRadio && mSingleRadio==null){
            boolean isOnline= ApplicationUtils.isOnline(this);
            showToast(isOnline? R.string.info_single_radio_error: R.string.info_connect_to_play);
            return;
        }
        UIConfigModel mUIConfigureModel = mTotalMng.getUiConfigModel();
        boolean isMulti= mUIConfigureModel != null && mUIConfigureModel.isMultiApp();

        boolean b=SHOW_SPLASH_INTERSTITIAL_ADS && SHOW_ADS && isShowAds;
        if(isMulti && SAVE_FAVORITE_SDCARD){
            b= b & isGrantAllPermission(getListPermissionNeedGrant());
        }
        showInterstitialAd(b,() -> {
            try{
                this.viewBinding.progressBar.hide();
                this.viewBinding.progressBar.setVisibility(View.INVISIBLE);
                if(isMulti){
                    if(SAVE_FAVORITE_SDCARD && !isGrantAllPermission(getListPermissionNeedGrant())){
                        Intent mIntent = new Intent(this, XRadioGrantActivity.class);
                        startActivity(mIntent);
                        finish();
                    }
                    else{
                        Intent mIntent = new Intent(this, XMultiRadioMainActivity.class);
                        startActivity(mIntent);
                        finish();
                    }
                }
                else{
                    Intent mIntent = new Intent(this, XSingleRadioMainActivity.class);
                    startActivity(mIntent);
                    finish();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });

    }

    public boolean isGrantAllPermission(String[] grantResults) {
        if(SAVE_FAVORITE_SDCARD && !IOUtils.hasAndroid10()){
            return ApplicationUtils.isGrantAllPermission(this,grantResults);
        }
        return true;
    }


}

package com.radio.kutai.gdpr;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.radio.kutai.ypylibs.task.IYPYCallback;
import com.radio.kutai.ypylibs.utils.ApplicationUtils;
import com.radio.kutai.ypylibs.utils.YPYLog;

import java.net.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://radio.com
 * Created by YPY Global on 2/25/19.
 *
 */
public class GDPRManager {

    private static GDPRManager mInstance;
    private GDPRModel gdprModel;
    private ConsentForm form;
    private boolean isGoToFirstTime;
    private boolean showConsentDialog;

    public static GDPRManager getInstance() {
        if (mInstance==null){
            mInstance = new GDPRManager();
        }
        return mInstance;
    }

    private GDPRManager() {

    }

    public void init(@NonNull String publisherId, @NonNull String policyId, @NonNull String testId){
        if(gdprModel ==null && !TextUtils.isEmpty(publisherId) && !TextUtils.isEmpty(policyId)){
            gdprModel = new GDPRModel(publisherId,policyId,testId);
        }
    }

    public void onDestroy(){
        isGoToFirstTime =false;
        gdprModel = null;
        mInstance = null;
    }

    public void startCheck(@NonNull Context mContext,IYPYCallback mCallback) {
        startCheck(mContext,null,mCallback);
    }

    public void startCheck(@NonNull Context mContext, @Nullable Handler mHandler, IYPYCallback mCallback){
        try{
            if (!ApplicationUtils.isOnline(mContext) || gdprModel == null) {
                showConsentDialog = false;
                if (mCallback != null) {
                    mCallback.onAction();
                }
                return;
            }
            showConsentDialog = false;
            ConsentInformation consentInformation = ConsentInformation.getInstance(mContext);
            if(!TextUtils.isEmpty(gdprModel.getTestId())){
                consentInformation.addTestDevice(gdprModel.getTestId());
                consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
            }
            String[] publisherIds = {gdprModel.getPublisherId()};
            consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
                @Override
                public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                    YPYLog.e("DCM","====>onConsentInfoUpdated="+consentStatus);
                    if(consentStatus == ConsentStatus.UNKNOWN){
                        if(mHandler!=null){
                            mHandler.removeCallbacksAndMessages(null);
                        }
                        showConsentDialog= true;
                        showDialogConsent(mContext,mCallback);
                    }

                }
                @Override
                public void onFailedToUpdateConsentInfo(String errorDescription) {
                    YPYLog.e("DCM","====>onFailedToUpdateConsentInfo="+errorDescription);
                    if(mHandler!=null){
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    if (mCallback != null && !isGoToFirstTime) {
                        mCallback.onAction();
                    }
                }
            });
            ConsentStatus consentStatus = consentInformation.getConsentStatus();
            YPYLog.e("DCM", "====>consentStatus=" + consentStatus);
            if (consentStatus != null && consentStatus != ConsentStatus.UNKNOWN) {
                YPYLog.e("DCM", "====>go luon");
                isGoToFirstTime = true;
                if (mCallback != null) {
                    mCallback.onAction();
                }
            }
            else{
                if(mHandler!=null){
                    mHandler.postDelayed(() -> {
                        if(!showConsentDialog){
                            mHandler.removeCallbacksAndMessages(null);
                            if (mCallback != null) {
                                mCallback.onAction();
                            }
                        }
                    },5000);
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    public void showDialogConsent(@NonNull Context mContext, IYPYCallback mCallback){
        try {
            if (gdprModel ==null) return;
            if(!TextUtils.isEmpty(gdprModel.getUrlPolicy())){
                URL privacyUrl = new URL(gdprModel.getUrlPolicy());
                ConsentForm.Builder builder = new ConsentForm.Builder(mContext, privacyUrl);
                builder.withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        try{
                            if(form!=null){
                                form.show();
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onConsentFormOpened() {

                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        YPYLog.e("DCM", "=============>onConsentFormClosed=" + consentStatus);
                        if (mCallback != null) {
                            mCallback.onAction();
                        }
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        YPYLog.e("DCM", "=============>onConsentFormError=" + errorDescription);
                        if (mCallback != null) {
                            mCallback.onAction();
                        }
                    }
                });
                builder.withPersonalizedAdsOption();
                builder.withNonPersonalizedAdsOption();

                form = builder.build();
                form.load();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}

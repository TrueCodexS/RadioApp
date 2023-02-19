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
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import com.radio.kutai.constants.IXRadioConstants;
import com.radio.kutai.dataMng.TotalDataManager;
import com.radio.kutai.databinding.ActivityGrantPermissionBinding;
import com.radio.kutai.ypylibs.activity.YPYSplashActivity;
import com.radio.kutai.ypylibs.executor.YPYExecutorSupplier;
import com.radio.kutai.ypylibs.utils.IOUtils;
import com.radio.kutai.ypylibs.utils.ShareActionUtils;

import java.io.File;


/**
 * @author:YPY Global

 * @Email: bl911vn@gmail.com
 * @Website: www.radio.com
 * @Date:Oct 20, 2017
 */

public class XRadioGrantActivity extends YPYSplashActivity<ActivityGrantPermissionBinding> implements IXRadioConstants,View.OnClickListener {

    private TotalDataManager mTotalMng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isNeedCheckGoogleService=false;
        super.onCreate(savedInstanceState);
        setUpOverlayBackground(true);

        mTotalMng = TotalDataManager.getInstance(getApplicationContext());

        String data=String.format(getString(R.string.format_request_permission),getString(R.string.app_name));
        this.viewBinding.tvInfo.setText(Html.fromHtml(data));
        this.viewBinding.tvPolicy.setOnClickListener(this);
        this.viewBinding.tvTos.setOnClickListener(this);
        this.viewBinding.btnAllow.setOnClickListener(this);

    }

    @Override
    public void onInitData() {
        startCheckData();
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
    protected ActivityGrantPermissionBinding getViewBinding() {
        return ActivityGrantPermissionBinding.inflate(getLayoutInflater());
    }


    private void startCheckData() {
        YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            mTotalMng.readConfigure(this);
            mTotalMng.readAllCache(this);
            runOnUiThread(this::goToMainActivity);
        });
    }



    public void goToMainActivity() {
        try {
            Intent mIntent = new Intent(this, XMultiRadioMainActivity.class);
            startActivity(mIntent);
            finish();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_policy) {
            ShareActionUtils.goToUrl(this, URL_PRIVACY_POLICY);
        }
        else if (id == R.id.tv_tos) {
            ShareActionUtils.goToUrl(this, URL_TERM_OF_USE);
        }
        else if (id == R.id.btn_allow) {
            startGrantPermission();
        }
    }

    @Override
    public void onPermissionDenied() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToHome();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onUpdateUIWhenSupportRTL() {
        super.onUpdateUIWhenSupportRTL();
        this.viewBinding.tvInfo.setGravity(Gravity.END);
    }
}

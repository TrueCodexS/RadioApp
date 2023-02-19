/*
 * Copyright (c) 2018. YPY Global - All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *         http://ypyglobal.com/sourcecode/policy
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.radio.kutai.fragment;

import com.google.gson.reflect.TypeToken;
import com.radio.kutai.adapter.ThemeAdapter;
import com.radio.kutai.dataMng.XRadioNetUtils;
import com.radio.kutai.model.ThemeModel;
import com.radio.kutai.setting.XRadioSettingManager;
import com.radio.kutai.ypylibs.adapter.YPYRecyclerViewAdapter;
import com.radio.kutai.ypylibs.model.ResultModel;
import com.radio.kutai.ypylibs.utils.ApplicationUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://radio.com
 * Created by YPY Global on 4/20/18.
 */
public class FragmentTheme extends XRadioListFragment<ThemeModel> {

    private int mTypeUI;

    @Override
    public YPYRecyclerViewAdapter<ThemeModel> createAdapter(ArrayList<ThemeModel> listObjects) {
        ThemeAdapter themeAdapter= new ThemeAdapter(mContext,listObjects,mUrlHost,mSizeH,mTypeUI);
        themeAdapter.setListener(data -> {
            XRadioSettingManager.saveThemes(mContext,data,mUrlHost);
            notifyData();
            mContext.updateBackground();
        });
        return themeAdapter;
    }

    @Override
    public ResultModel<ThemeModel> getListModelFromServer() {
        ResultModel<ThemeModel> mResultModel=null;
        try{
            boolean isOnline= mConfigureMode != null && mConfigureMode.isOnlineApp();
            if(isOnline){
                if (ApplicationUtils.isOnline(mContext)) {
                    mResultModel = XRadioNetUtils.getListThemes(mUrlHost,mApiKey,0,mNumberItemPerPage,-1);
                    return mResultModel;
                }
            }
            else{
                Type mTypeToken = new TypeToken<ResultModel<ThemeModel>>(){}.getType();
                mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_THEMES,mTypeToken);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return mResultModel;
    }

    @Override
    public ResultModel<ThemeModel> getListModelFromServer(int offset, int limit) {
        boolean isOnline= mConfigureMode != null && mConfigureMode.isOnlineApp();
        if(isOnline){
            return XRadioNetUtils.getListThemes(mUrlHost,mApiKey,offset,limit,-1);
        }
        return null;
    }

    @Override
    public void setUpUI() {
        mTypeUI=mUIConfigureModel!=null?mUIConfigureModel.getUiThemes():UI_CARD_GRID;
        setUpUIRecyclerView(mTypeUI);
    }
}

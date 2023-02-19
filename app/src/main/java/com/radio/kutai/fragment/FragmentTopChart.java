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
import com.radio.kutai.adapter.RadioAdapter;
import com.radio.kutai.dataMng.XRadioNetUtils;
import com.radio.kutai.model.RadioModel;
import com.radio.kutai.ypylibs.adapter.YPYRecyclerViewAdapter;
import com.radio.kutai.ypylibs.model.ResultModel;
import com.radio.kutai.ypylibs.utils.ApplicationUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://radio.com
 * Created by YPY Global on 4/20/18.
 */
public class FragmentTopChart extends XRadioListFragment<RadioModel> {

    private int mTypeUI;

    @Override
    public YPYRecyclerViewAdapter<RadioModel> createAdapter(ArrayList<RadioModel> listObjects) {
        RadioAdapter mRadioAdapter = new RadioAdapter(mContext,listObjects,mUrlHost,mSizeH,mTypeUI);
        mRadioAdapter.setListener(mObject ->mContext.startPlayingList(mObject,listObjects));
        mRadioAdapter.setOnRadioListener((model, isFavorite) -> mContext.updateFavorite(model,TYPE_TAB_FAVORITE,isFavorite));
        return mRadioAdapter;
    }

    @Override
    public ResultModel<RadioModel> getListModelFromServer() {
        boolean isOnline= mConfigureMode != null && mConfigureMode.isOnlineApp();
        ResultModel<RadioModel> mResultModel=null;
        if(isOnline){
            if(ApplicationUtils.isOnline(mContext)){
                mResultModel = XRadioNetUtils.getListTopChartRadio(mUrlHost,mApiKey,0,mNumberItemPerPage);
            }
        }
        else{
            Type mTypeToken = new TypeToken<ResultModel<RadioModel>>(){}.getType();
            mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_RADIOS,mTypeToken);
        }
        if(mResultModel!=null && mResultModel.isResultOk()){
            ArrayList<RadioModel> mListModels = mResultModel.getListModels();
            if(!isOnline && mListModels!=null && mListModels.size()>0){
                Iterator<RadioModel> mIterator = mListModels.iterator();
                while (mIterator.hasNext()){
                    RadioModel model = mIterator.next();
                    if(model.getFeatured()!=1){
                        mIterator.remove();
                    }
                }
            }
            mContext.mTotalMng.updateFavoriteForList(mResultModel.getListModels(),TYPE_TAB_FAVORITE);
        }
        return mResultModel;
    }

    @Override
    public ResultModel<RadioModel> getListModelFromServer(int offset, int limit) {
        boolean isOnline= mConfigureMode != null && mConfigureMode.isOnlineApp();
        ResultModel<RadioModel> mResultModel=null;
        if(isOnline){
            if(ApplicationUtils.isOnline(mContext)){
                mResultModel = XRadioNetUtils.getListTopChartRadio(mUrlHost,mApiKey,offset,limit);
                if(mResultModel!=null && mResultModel.isResultOk()){
                    mContext.mTotalMng.updateFavoriteForList(mResultModel.getListModels(),TYPE_TAB_FAVORITE);
                }
            }
        }
        return mResultModel;
    }

    @Override
    public void setUpUI() {
        mTypeUI=mUIConfigureModel!=null?mUIConfigureModel.getUiTopChart():UI_FLAT_LIST;
        setUpUIRecyclerView(mTypeUI);
    }
}

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
import com.radio.kutai.adapter.GenreAdapter;
import com.radio.kutai.dataMng.XRadioNetUtils;
import com.radio.kutai.model.GenreModel;
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
public class FragmentGenre extends XRadioListFragment<GenreModel> {
    private int mTypeUI;

    @Override
    public YPYRecyclerViewAdapter<GenreModel> createAdapter(ArrayList<GenreModel> listObjects) {
        GenreAdapter genreAdapter= new GenreAdapter(mContext,listObjects,mUrlHost,mSizeH,mTypeUI);
        genreAdapter.setListener(data ->mContext.goToGenreModel(data));
        return genreAdapter;
    }

    @Override
    public void onDoWhenRefreshList() {
        super.onDoWhenRefreshList();
        if(mTypeUI==UI_MAGIC_GRID){
            setUpUIRecyclerView(mTypeUI);
        }
    }

    @Override
    public ResultModel<GenreModel> getListModelFromServer() {
        ResultModel<GenreModel> mResultModel=null;
        try{
            boolean isOnline= mConfigureMode != null && mConfigureMode.isOnlineApp();
            if(isOnline){
                if (ApplicationUtils.isOnline(mContext)) {
                    mResultModel = XRadioNetUtils.getListGenreModel(mUrlHost,mApiKey);
                    return mResultModel;
                }
            }
            else{
                Type mTypeToken = new TypeToken<ResultModel<GenreModel>>(){}.getType();
                mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_GENRES,mTypeToken);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return mResultModel;
    }

    @Override
    public void setUpUI() {
        mTypeUI=mUIConfigureModel!=null?mUIConfigureModel.getUiGenre():UI_CARD_GRID;
        setUpUIRecyclerView(mTypeUI);
    }
}

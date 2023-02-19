package com.radio.kutai.model;

import android.text.TextUtils;

import com.radio.kutai.dataMng.XRadioNetUtils;
import com.radio.kutai.ypylibs.model.AbstractModel;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://radio.com
 * Created by YPY Global on 4/20/18.
 */
public class GenreModel extends AbstractModel {

    public GenreModel(long id, String name, String image) {
        super(id, name, image);
    }

    @Override
    public String getArtWork(String urlHost) {
        if(!TextUtils.isEmpty(image) && !image.startsWith("http") && !TextUtils.isEmpty(urlHost)){
            image=urlHost+ XRadioNetUtils.FOLDER_GENRES+image;
        }
        return super.getImage();
    }
}

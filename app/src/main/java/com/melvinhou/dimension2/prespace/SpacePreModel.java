package com.melvinhou.dimension2.prespace;

import android.app.Application;

import com.melvinhou.kami.mvvm.BaseModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/11 0011 16:24
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class SpacePreModel extends BaseModel {
    public SpacePreModel(@NonNull Application application) {
        super(application);
    }


    MutableLiveData<Integer> page = new MutableLiveData();
}
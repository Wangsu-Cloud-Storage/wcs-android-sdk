package com.chinanetcenter.wcs.android.listener;

import com.chinanetcenter.wcs.android.entity.MergeBlockResult;

/**
 * @author :wangjm1
 * @version :1.0
 * @package : com.chinanetcenter.wcs.android.listener
 * @class : ${CLASS_NAME}
 * @time : 2017/5/17 ${ITME}
 * @description :TODO
 */
public interface SliceTestListener {
    public void onSuccess(MergeBlockResult result);

    public void onFailure(Exception e);
}

/**
 * @author :yanghuan
 * @version :1.6.2
 * @package : com.chinanetcenter.wcs.android.listener
 * @class : FileStringListener
 * @time : 2017/7/25 10:38
 * @description :针对糖豆视频获取token的回调
 */
package com.chinanetcenter.wcs.android.listener;

import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.internal.WcsCompletedCallback;
import com.chinanetcenter.wcs.android.network.WcsRequest;
import com.chinanetcenter.wcs.android.network.WcsResult;
import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class FileStringListener implements
        WcsCompletedCallback<WcsRequest, WcsResult> {

    @Override
    public void onSuccess(WcsRequest request, WcsResult result) {
        try {
            JSONObject jsonObject = new JSONObject(result.getResponse());
            WCSLogUtil.i(result.getResponse());
            if (jsonObject.getInt("code") == 200) {
                JSONObject data = jsonObject.getJSONObject("data");
                onSuccess(result.getStatusCode(), data.getString("uploadToken"));
            } else {
                onFailure(new OperationMessage(result.getStatusCode(), result.getResponse()));
            }
        } catch (JSONException e) {
            onFailure(new OperationMessage(0, null, e));
        }

    }

    @Override
    public void onFailure(WcsRequest request, OperationMessage operationMessage) {
        onFailure(operationMessage);
    }


    /**
     * 文件上传成功之后回调
     *
     * @param status
     * @param responseString uploadToken
     */
    public abstract void onSuccess(int status, String responseString);

    /**
     * 文件上传失败之后的回调
     *
     * @param operationMessage 操作对应的信息
     */
    public abstract void onFailure(OperationMessage operationMessage);


}

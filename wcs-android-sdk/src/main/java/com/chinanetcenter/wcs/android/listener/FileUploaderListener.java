package com.chinanetcenter.wcs.android.listener;

import com.chinanetcenter.wcs.android.api.BaseApi;

import org.json.JSONObject;

/**
 * 普通文件上传的回调，上传成功时，回调值为解析之后的JSON对象。
 */
public abstract class FileUploaderListener extends FileUploaderStringListener {

    public final void onSuccess(int status, String responseString) {
        onSuccess(status, BaseApi.parseWCSUploadResponse(responseString));
    }

    /**
     * 文件上传成功之后回调
     *
     * @param status
     * @param responseJson
     */
    public abstract void onSuccess(int status, JSONObject responseJson);

}

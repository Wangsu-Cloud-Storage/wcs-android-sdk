package com.chinanetcenter.wcs.android.api;

import android.content.Context;
import android.text.TextUtils;

import com.chinanetcenter.wcs.android.ClientConfig;
import com.chinanetcenter.wcs.android.LogRecorder;
import com.chinanetcenter.wcs.android.internal.InternalRequest;
import com.chinanetcenter.wcs.android.utils.EncodeUtils;
import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseApi {

    static final String FORM_TOKEN = "token";

    /**
     * eg：client配置
     * ClientConfig conf = new ClientConfig();
     * conf.setConnectionTimeout(15*1000); // 连接超时，默认15秒
     * conf.setSocketTimeout(15*1000); // socket超时，默认15秒
     * conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
     * conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
     */
    private volatile static InternalRequest mInternalRequest;
    private static final Object mObject = new Object();

    static synchronized InternalRequest getInternalRequest(Context context, ClientConfig config) {
        synchronized (mObject) {
            if (null == mInternalRequest) {
                mInternalRequest = new InternalRequest(config == null ? ClientConfig.getDefaultConf() : config);
            }
            LogRecorder.getInstance().setup(context.getApplicationContext());
        }
        return mInternalRequest;
    }

    static boolean isNetworkReachable() {
        return true;
    }

    public static JSONObject parseWCSUploadResponse(String responseString) {
        WCSLogUtil.d("parsing upload response : " + responseString);

        JSONObject responseJsonObject = null;
        try {
            responseJsonObject = new JSONObject(responseString);
        } catch (JSONException e) {
            WCSLogUtil.d("Try serializing as json failured, response may encoded.");
        }

        if (null == responseJsonObject) {
            responseJsonObject = new JSONObject();
            if (!TextUtils.isEmpty(responseString)) {
                String response = EncodeUtils.urlsafeDecodeString(responseString);
                WCSLogUtil.d("response string : " + response);
                String[] params = response.split("&");
                for (String param : params) {
                    int index = param.indexOf("=");
                    if (index > 0) {
                        try {
                            responseJsonObject.put(param.substring(0, index), param.substring(index + 1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return responseJsonObject;
    }

}

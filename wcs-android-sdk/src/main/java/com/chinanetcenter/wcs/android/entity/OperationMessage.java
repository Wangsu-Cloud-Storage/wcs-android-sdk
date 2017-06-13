package com.chinanetcenter.wcs.android.entity;

import android.text.TextUtils;
import android.util.Log;

import com.chinanetcenter.wcs.android.network.WcsResult;
import com.chinanetcenter.wcs.android.utils.WCSLogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class OperationMessage {

    private int status;
    private String message;
    private Throwable error;

    public static OperationMessage fromJsonString(String jsonString, String requestId, Throwable error) {
        OperationMessage errorMessage = new OperationMessage();
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                jsonObject.put(WcsResult.REQUEST_ID, requestId);
                errorMessage.message = jsonObject.toString();
//                errorMessage.status = 500;
//                errorMessage.message = "服务器内部错误";
            } catch (JSONException e) {
                Log.e("CNCLog", "json error : " + jsonString);
            }
        }
        errorMessage.error = error;
        return errorMessage;
    }

    public static OperationMessage fromJsonString(String jsonString, Throwable error) {
        OperationMessage errorMessage = new OperationMessage();
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                errorMessage.status = jsonObject.optInt("code", 500);
                errorMessage.message = jsonObject.optString("message", "服务器内部错误");
            } catch (JSONException e) {
                Log.e("CNCLog", "json error : " + jsonString);
            }
        }
        errorMessage.error = error;
        return errorMessage;
    }

    public OperationMessage() {
    }

    public OperationMessage(int status, String message) {
        this.status = status;
        this.message = message;
        this.error = null;
    }

    public OperationMessage(int status, String message, Throwable error) {
        this.status = status;
        this.message = message;
        this.error = error;
    }

    /**
     * 操作状态码
     *
     * @return
     */
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 操作结果对应的信息
     *
     * @return
     */
    public String getMessage() {
        StringBuffer formatMessage = new StringBuffer();
        if (this.message != null) {
            formatMessage.append(this.message);
        }
        if (this.error != null) {
            formatMessage.append(" { ");
            formatMessage.append("ClientMsg: ");
            formatMessage.append(WCSLogUtil.getStackTraceString(this.error));
            formatMessage.append(" }");
        }
        return formatMessage.toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.putOpt("status", status);
            jsonObject.putOpt("message", message);
            if (null != error) {
                jsonObject.putOpt("error", WCSLogUtil.getStackTraceString(this.error));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}

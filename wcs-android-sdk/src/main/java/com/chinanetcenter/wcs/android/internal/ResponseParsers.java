package com.chinanetcenter.wcs.android.internal;

import com.chinanetcenter.wcs.android.entity.MergeBlockResult;
import com.chinanetcenter.wcs.android.entity.SliceResponse;
import com.chinanetcenter.wcs.android.network.ResponseParser;
import com.chinanetcenter.wcs.android.network.WcsResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Response;

/**
 * @author :wangjm1
 * @version :1.0
 * @package : com.chinanetcenter.wcs.android.internal
 * @class : ${CLASS_NAME}
 * @time : 2017/5/11 ${ITME}
 * @description :TODO
 */
public class ResponseParsers {


    public static class BaseResponseParser implements ResponseParser<WcsResult> {
        @Override
        public WcsResult parse(Response response) throws IOException {
            WcsResult result = new WcsResult();
            result.setStatusCode(response.code());
            Map<String, String> responseHeader = parseResponseHeader(response);
            result.setRequestId(responseHeader.get(WcsResult.REQUEST_ID));
            result.setResponseHeader(responseHeader);

            String jsonStr = parseResponse(response.body().byteStream());
            result.setResponseJson(jsonStr);

            return result;
        }
    }

    public static class UploadResponseParser implements ResponseParser<UploadFileResult> {
        @Override
        public UploadFileResult parse(Response response) throws IOException {
            UploadFileResult result = new UploadFileResult();
            result.setStatusCode(response.code());
            Map<String, String> responseHeader = parseResponseHeader(response);
            result.setRequestId(responseHeader.get(WcsResult.REQUEST_ID));
            result.setResponseHeader(responseHeader);
            result.setETag("22222");// TODO: 2017/5/11 需要补充

            String jsonStr = parseResponse(response.body().byteStream());
            result.setResponseJson(jsonStr);

            return result;
        }
    }

    public static class UploadBlockResponseParser implements ResponseParser<SliceResponse> {
        @Override
        public SliceResponse parse(Response response) throws IOException {
            SliceResponse result = new SliceResponse();
            String jsonStr = parseResponse(response.body().byteStream());
            result.setResponseJson(jsonStr);
            if (response.isSuccessful()) {
                SliceResponse.fromJsonString(result, jsonStr);
            }else{
                Map<String, String> responseHeader = parseResponseHeader(response);
                result.setRequestId(responseHeader.get(WcsResult.REQUEST_ID));
            }
            return result;
        }
    }

    private static String parseResponse(InputStream in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int count = -1;
        while ((count = in.read(data, 0, 1024)) != -1) {
            outputStream.write(data, 0, count);
        }
        data = null;
        String jsonStr = new String(outputStream.toByteArray(), "utf-8");

        if (outputStream != null) {
            outputStream.close();
        }
        return jsonStr;
    }

    public static class UploadMergeBlockResponseParser implements ResponseParser<MergeBlockResult> {
        @Override
        public MergeBlockResult parse(Response response) throws IOException {
            MergeBlockResult result = new MergeBlockResult();
            String jsonStr = parseResponse(response.body().byteStream());
            result.setResponseJson(jsonStr);
            if (response.isSuccessful()) {
                MergeBlockResult.fromJsonString(result, jsonStr);
            }

            return result;
        }
    }

    public static void safeCloseResponse(Response response) {
        try {
            response.body().close();
        } catch (Exception e) {
        }
    }

    public static Map<String, String> parseResponseHeader(Response response) {
        Map<String, String> result = new HashMap<String, String>();
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.name(i), headers.value(i));
        }
        return result;
    }
}

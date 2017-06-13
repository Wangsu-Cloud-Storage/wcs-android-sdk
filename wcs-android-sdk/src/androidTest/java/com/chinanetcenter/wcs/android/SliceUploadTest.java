package com.chinanetcenter.wcs.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.chinanetcenter.wcs.android.api.FileUploader;
import com.chinanetcenter.wcs.android.listener.SliceUploaderListener;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * @author : yanghuan
 * @version : 1.0
 * @package : com.chinanetcenter.wcs.android
 * @class : SliceUploadTest
 * @time : 2017/6/9 16:15
 * @description :
 */
@RunWith(AndroidJUnit4.class)
public class SliceUploadTest {


    private static final String FILE_NAME = "PhotoTable.apk";

    @BeforeClass
    public static void setUp() throws IOException {
        WcsTestConfig.generateFiles();
        FileUploader.setUploadUrl("http://apitestuser.up0.v1.wcsapi.com");
    }

    @Test
    public void uploadSlice() throws Exception {
        FileUploader.setBlockConfigs(8, 256 * 2);
        final String filePath = InstrumentationRegistry.getTargetContext().getFilesDir() + File.separator + "500k";
        Log.d(TAG, "uploadSlice: " + filePath);
        final File file = new File(filePath);
        final CountDownLatch signal = new CountDownLatch(1);
        FileUploader.sliceUpload(filePath, InstrumentationRegistry.getTargetContext(),
                WcsTestConfig.TOKEN, file, null, new SliceUploaderListener() {
                    @Override
                    public void onSliceUploadSucceed(JSONObject reponseJSON) {
                        signal.countDown();
                        Log.d(TAG, "onSuccess: " + reponseJSON);
                    }

                    @Override
                    public void onSliceUploadFailured(HashSet<String> errorMessages) {
                        StringBuilder sb = new StringBuilder();
                        for (String string : errorMessages) {
                            sb.append(string + "\r\n");
                            Log.e(TAG, "errorMessage : " + string);
                        }
                        signal.countDown();
                    }

                    @Override
                    public void onProgress(long uploaded, long total) {
                        String percent = (float) uploaded / total * 100 + "%";
                        String progressMsg = "当前: " + uploaded + ", 总: " + total +
                                ", 比例: " + percent + "\r\n";
                        Log.d(TAG, progressMsg);
                    }

                });
        signal.await(30 * 1000, TimeUnit.SECONDS);
    }

//
//    private void testNullParams() throws InterruptedException {
//        Log.i("CNCLog", "testNullParams");
//        final String token = null;
//        final String filePath = null;
//        final CountDownLatch signal = new CountDownLatch(1);
//        getInstrumentation().runOnMainSync(new Runnable() {
//
//            @Override
//            public void run() {
//                FileUploader.upload(getInstrumentation().getContext(), token, filePath, null, new FileUploaderListener() {
//
//                    @Override
//                    public void onSuccess(int status, JSONObject responseJson) {
//                        Log.d(TAG, "response JSON : " + responseJson);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onFailure(OperationMessage operationMessage) {
//                        Log.d(TAG, "operationMessage : " + operationMessage);
//                        signal.countDown();
//                    }
//                });
//            }
//        });
//        signal.await(30 * 1000, TimeUnit.SECONDS);
//    }
//
//    private void testChinese() throws InterruptedException {
//        Log.i("CNCLog", "testChinese");
//        final String token = getToken(TEST_AK, TEST_BUCKET, "测试@#￥（*……%￥", TEST_EXPIRED, null, null);
//        final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
//        final CountDownLatch signal = new CountDownLatch(1);
//        getInstrumentation().runOnMainSync(new Runnable() {
//
//            @Override
//            public void run() {
//                FileUploader.upload(getInstrumentation().getContext(), token, filePath, null, new FileUploaderListener() {
//
//                    @Override
//                    public void onSuccess(int status, JSONObject responseJson) {
//                        Log.d(TAG, "reponse JSON : " + responseJson);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onFailure(OperationMessage operationMessage) {
//                        Log.e(TAG, "operation message : " + operationMessage);
//                        signal.countDown();
//                    }
//                });
//            }
//        });
//        signal.await(30 * 1000, TimeUnit.SECONDS);
//    }
//
//    private void testUsingCallbackUrl() throws InterruptedException {
//        Log.i("CNCLog", "testUsingCallbackUrl");
//        final String token = getToken(TEST_AK, TEST_BUCKET, "testFileUsingCallback", TEST_EXPIRED, CALLBACK_URL, null);
//        final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
//        final CountDownLatch signal = new CountDownLatch(1);
//        getInstrumentation().runOnMainSync(new Runnable() {
//            @Override
//            public void run() {
//                FileUploader.upload(getInstrumentation().getContext(), token, filePath, null, new FileUploaderListener() {
//
//                    @Override
//                    public void onSuccess(int status, JSONObject responseJson) {
//                        assertNotNull(responseJson);
//                        Log.d(TAG, "responseJSON : " + responseJson);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onFailure(OperationMessage operationMessage) {
//                        assertNotNull(operationMessage);
//                        Log.e(TAG, "operation message : " + operationMessage);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onProgress(int bytesWritten, int totalSize) {
//                        Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
//                    }
//                });
//            }
//        });
//        signal.await(30 * 1000, TimeUnit.SECONDS);
//    }
//
//    private void testUsingCallbackBody() throws InterruptedException {
//        Log.i("CNCLog", "testUsingCallbackBody");
//        String callbackBodyString = "location=$(x:location)&price=$(x:price)";
//        final String token = getToken(TEST_AK, TEST_BUCKET, "testFile", TEST_EXPIRED, CALLBACK_URL, callbackBodyString);
//        final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
//        final HashMap<String, String> callbackBody = new HashMap<String, String>();
//        callbackBody.put("x:location", "123456.001001");
//        callbackBody.put("x:price", "12321");
//        final CountDownLatch signal = new CountDownLatch(1);
//        getInstrumentation().runOnMainSync(new Runnable() {
//            @Override
//            public void run() {
//                FileUploader.upload(getInstrumentation().getContext(), token, filePath, callbackBody, new FileUploaderListener() {
//
//                    @Override
//                    public void onSuccess(int status, JSONObject responseJson) {
//                        assertNotNull(responseJson);
//                        Log.d(TAG, "responseJSON : " + responseJson);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onFailure(OperationMessage operationMessage) {
//                        assertNotNull(operationMessage);
//                        Log.e(TAG, "operation message : " + operationMessage);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onProgress(int bytesWritten, int totalSize) {
//                        Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
//                    }
//                });
//            }
//        });
//        signal.await(30 * 1000, TimeUnit.SECONDS);
//    }
//
//    public void testNormalStream() throws FileNotFoundException, InterruptedException {
//        Log.i("CNCLog", "testNormalStream");
//        final String token = "86622e227a50d49d858c2494a935bc2e4ac543a7:YzI1ZmQ3YmVjZmQ3ZGQzOGVkZDdiNGEyNzQ0MTNmY2U3YTk0MDk5NA==:eyJzY29wZSI6ImltYWdlcyIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsInJldHVybkJvZHkiOiJidWNrZXQ9JChidWNrZXQpJmZzaXplPSQoZnNpemUpJmhhc2g9JChoYXNoKSZrZXk9JChrZXkpIiwib3ZlcndyaXRlIjoxLCJmc2l6ZUxpbWl0IjowfQ==";
//        final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
//        final InputStream input = new FileInputStream(filePath);
//        final CountDownLatch signal = new CountDownLatch(1);
//        getInstrumentation().runOnMainSync(new Runnable() {
//            @Override
//            public void run() {
//
//                FileUploader.upload(getInstrumentation().getContext(), token, null, input, null, new FileUploaderListener() {
//
//                    @Override
//                    public void onSuccess(int status, JSONObject responseJson) {
//                        assertNotNull(responseJson);
//                        Log.d(TAG, "responseJSON : " + responseJson);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onFailure(OperationMessage operationMessage) {
//                        assertNotNull(operationMessage);
//                        Log.e(TAG, "operation message : " + operationMessage);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onProgress(int bytesWritten, int totalSize) {
//                        Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
//                    }
//                });
//            }
//        });
//        signal.await(30 * 1000, TimeUnit.SECONDS);
//    }
//
//    private void testNormal() throws InterruptedException {
//        Log.i("CNCLog", "testNormal");
//        final String token = getToken(TEST_AK, TEST_BUCKET, "testFile", TEST_EXPIRED, null, null);
//        final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
//        final CountDownLatch signal = new CountDownLatch(1);
//        getInstrumentation().runOnMainSync(new Runnable() {
//            @Override
//            public void run() {
//                FileUploader.upload(getInstrumentation().getContext(), token, filePath, null, new FileUploaderListener() {
//
//                    @Override
//                    public void onSuccess(int status, JSONObject responseJson) {
//                        assertNotNull(responseJson);
//                        Log.d(TAG, "responseJSON : " + responseJson);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onFailure(OperationMessage operationMessage) {
//                        assertNotNull(operationMessage);
//                        Log.e(TAG, "operation message : " + operationMessage);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onProgress(int bytesWritten, int totalSize) {
//                        Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
//                    }
//                });
//            }
//        });
//        signal.await(30 * 1000, TimeUnit.SECONDS);
//    }
//
//    private void testInvalidateToken() throws InterruptedException {
//        Log.i("CNCLog", "testInvalidateToken");
//        final String token = "fdsafdsa";
//        final String filePath = getInstrumentation().getContext().getFilesDir() + File.separator + FILE_NAME;
//        final String noExistsFilePath = "fdsafdsa";
//
//        final CountDownLatch signal = new CountDownLatch(1);
//        getInstrumentation().runOnMainSync(new Runnable() {
//
//            @Override
//            public void run() {
//                FileUploader.upload(getInstrumentation().getContext(), token, filePath, null, new FileUploaderListener() {
//
//                    @Override
//                    public void onSuccess(int status, JSONObject responseJson) {
//                        assertNotNull(responseJson);
//                        Log.e(TAG, "responseJSON : " + responseJson);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onFailure(OperationMessage operationMessage) {
//                        assertNotNull(operationMessage);
//                        Log.e(TAG, "operation message : " + operationMessage);
//                        signal.countDown();
//                    }
//
//                    @Override
//                    public void onProgress(int bytesWritten, int totalSize) {
//                        Log.d(TAG, String.format("bytes written : %s, total size : %s", bytesWritten, totalSize));
//                    }
//                });
//            }
//        });
//
//        signal.await(30 * 1000, TimeUnit.SECONDS);
//    }
//
//    private String getToken(String ak, String bucket, String key, long expired, String callbackUrl, String callbackBody) {
//        HttpClient httpClient = new DefaultHttpClient();
//        StringBuffer sb = new StringBuffer(UPLOAD_TOKEN_URL);
//        sb.append("?ak=");
//        sb.append(ak);
//        sb.append("&bucket=");
//        sb.append(bucket);
//        sb.append("&key=");
//        sb.append(EncodeUtils.urlsafeEncode(key));
//        sb.append("&expire=");
//        sb.append(expired);
//        if (!TextUtils.isEmpty(callbackUrl)) {
//            sb.append("&callBackUrl=");
//            sb.append(callbackUrl);
//        }
//        if (!TextUtils.isEmpty(callbackBody)) {
//            sb.append("&callBody=");
//            sb.append(EncodeUtils.urlsafeEncode(callbackBody));
//        }
//        sb.append("&overwrite=");
//        sb.append("1");
//        Log.d(TAG, "get string : " + sb.toString());
//        HttpGet httpGet = new HttpGet(sb.toString());
//        HttpResponse response = null;
//        try {
//            response = httpClient.execute(httpGet);
//            int statusCode = response.getStatusLine().getStatusCode();
//            byte[] responseData = getResponseData(response.getEntity());
//            String responseString = StringUtils.stringFrom(responseData);
//            Log.d(TAG, "status code : " + statusCode + "; responseString : " + responseString);
//            assertTrue(statusCode == 200);
//            if (statusCode == 200) {
//                return responseString;
//            }
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
}

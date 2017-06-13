package com.chinanetcenter.wcs.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.chinanetcenter.wcs.android.api.FileUploader;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.internal.UploadFileRequest;
import com.chinanetcenter.wcs.android.listener.FileUploaderListener;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author : yanghuan
 * @version : 1.6.1
 * @package : com.chinanetcenter.wcs.android
 * @class : SliceUploadTest
 * @time : 2017/6/9 16:15
 * @description :
 */
@RunWith(AndroidJUnit4.class)
public class UploadFileTest {

    private static final String TAG = "UploadFileTest";

    @BeforeClass
    public static void setUp() throws IOException {
        WcsTestConfig.generateFiles();
        FileUploader.setUploadUrl("http://apitestuser.up0.v1.wcsapi.com");
    }

    @Test
    public void uploadNormal() throws Exception {
        final String filePath = InstrumentationRegistry.getTargetContext().getFilesDir() + File.separator + "500k";
        Log.d(TAG, "uploadNormal: " + filePath);
        final File file = new File(filePath);
        final CountDownLatch signal = new CountDownLatch(1);
        FileUploader.upload(InstrumentationRegistry.getTargetContext(),
                WcsTestConfig.TOKEN, file, null, new FileUploaderListener() {
                    @Override
                    public void onProgress(UploadFileRequest request, long currentSize, long totalSize) {
                        String percent = ((float) currentSize / totalSize * 100) + "%";
                        String progressMsg = "当前: " + currentSize + ", 总: " + totalSize +
                                ", 比例: " + percent + "\r\n";
                        Log.d(TAG, progressMsg);
                    }

                    @Override
                    public void onFailure(OperationMessage operationMessage) {
                        Log.d(TAG, "onFailure: " + operationMessage.getMessage());
                        signal.countDown();
                    }

                    @Override
                    public void onSuccess(int status, JSONObject responseJson) {
                        signal.countDown();
                        Log.d(TAG, "onSuccess: " + responseJson);
                    }
                });
        signal.await(30 * 1000, TimeUnit.SECONDS);
    }

    @Test
    public void uploadNormalWithNoKey() throws Exception {
        String uploadToken = "db17ab5d18c137f786b67c490187317a0738f94a:MDAwNmNiMjhjYmM2OTdlOWMxNDdiYWMyOWM3OWEzMjc0ZDEwYjU0Zg==:eyJzY29wZSI6ImFwaXRlc3QtbmV0cHJvYmUiLCJkZWFkbGluZSI6IjQwNzA4ODAwMDAwMDAiLCJvdmVyd3JpdGUiOjEsImZzaXplTGltaXQiOjAsImluc3RhbnQiOjAsInNlcGFyYXRlIjowfQ==";
        final String filePath = InstrumentationRegistry.getTargetContext().getFilesDir() + File.separator + "500k";
        Log.d(TAG, "uploadNormal: " + filePath);
        final File file = new File(filePath);
        final CountDownLatch signal = new CountDownLatch(1);
        FileUploader.upload(InstrumentationRegistry.getTargetContext(),
                uploadToken, file, null, new FileUploaderListener() {

                    @Override
                    public void onFailure(OperationMessage operationMessage) {
                        Log.d(TAG, "onFailure: " + operationMessage.getMessage());
                        signal.countDown();
                    }

                    @Override
                    public void onSuccess(int status, JSONObject responseJson) {
                        //未上传key且token中没有key
                        signal.countDown();
                        Log.d(TAG, "onSuccess: " + responseJson);
                    }
                });
        signal.await(30 * 1000, TimeUnit.SECONDS);
    }


}

package com.example.wcssdktest;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.chinanetcenter.wcs.android.ClientConfig;
import com.chinanetcenter.wcs.android.Config;
import com.chinanetcenter.wcs.android.LogRecorder;
import com.chinanetcenter.wcs.android.api.FileUploader;
import com.chinanetcenter.wcs.android.api.ParamsConf;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.internal.UploadFileRequest;
import com.chinanetcenter.wcs.android.listener.FileUploaderListener;
import com.chinanetcenter.wcs.android.listener.FileUploaderStringListener;
import com.chinanetcenter.wcs.android.listener.SliceUploaderBase64Listener;
import com.chinanetcenter.wcs.android.listener.SliceUploaderListener;
import com.chinanetcenter.wcs.android.utils.WetagUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

@SuppressLint("CutPasteId")
public class MainActivity extends ActionBarActivity implements OnClickListener {

    private static final String TAG = "CNCLog";

    private final int MESSAGE_FINISH = 1;
    private final int GENERATE_SUCCESSFULLY = 2;
    private final int GENERATE_FAILED = 3;
    private final int MESSAGE_APPEND = 4;

    private Spinner mFileSizeSp;

    private EditText mBaseUrlEt;
    private EditText mTokenEt;
    private EditText mKeyEt;
    private EditText mFilenameEt;
    private EditText mMimeTypeEt;
    private EditText mSliceEt;
    private EditText mBlockEt;
    private TextView mDisplayTv;
    private ScrollView mDisplaySv;

    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private String mFilePath;

    //为了不影响fileupload 的api，利用conf进行配置参数，以供测试
    private ParamsConf conf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.slice_upload_json).setOnClickListener(this);
        findViewById(R.id.upload_normal_json).setOnClickListener(this);
        Button mSliceUploadButton = (Button) findViewById(R.id.slice_upload);
        mSliceUploadButton.setOnClickListener(this);
        Button mCancelRequestButton = (Button) findViewById(R.id.cancel_requests);
        mCancelRequestButton.setOnClickListener(this);
        Button mGenerateFilesButton = (Button) findViewById(R.id.generate_files);
        mGenerateFilesButton.setOnClickListener(this);
        Button mUploadNormalButton = (Button) findViewById(R.id.upload_normal);
        mUploadNormalButton.setOnClickListener(this);
        Button clearFileBtn = (Button) findViewById(R.id.clear_files_btn);
        clearFileBtn.setOnClickListener(this);

        mBaseUrlEt = (EditText) findViewById(R.id.baseurl_et);
        mTokenEt = (EditText) findViewById(R.id.token_et);
        mKeyEt = (EditText) findViewById(R.id.keyname_et);
        mFilenameEt = (EditText) findViewById(R.id.filename_et);
        mMimeTypeEt = (EditText) findViewById(R.id.mimetype_et);
        mSliceEt = (EditText) findViewById(R.id.slice_size_et);
        mBlockEt = (EditText) findViewById(R.id.block_size_et);
        mDisplayTv = (TextView) findViewById(R.id.display_tv);

        mDisplaySv = (ScrollView) findViewById(R.id.display_scrollview);

        mFileSizeSp = (Spinner) findViewById(R.id.file_size_sp);
        setSpAdapter();
        init();

    }

    private void setSpAdapter() {
        String[] fileStrings = getSDFileDir().list();
        for (String fileString : fileStrings) {
            Log.d(TAG, "fileString: " + fileString);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFileSizeSp.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.slice_upload:
                showLoadingDialog();
                initParams();
                sliceUpload();
                break;
            case R.id.slice_upload_json:
                showLoadingDialog();
                initParams();
                sliceUploadJSON();
                break;

            case R.id.upload_normal:
                showLoadingDialog();
                initParams();
                normalUpload();
                break;
            case R.id.upload_normal_json:
                showLoadingDialog();
                initParams();
                normalUploadJson();
                break;
            case R.id.cancel_requests:
                FileUploader.cancelRequests(this);
                break;
            case R.id.generate_files:
                showLoadingDialog();
                generateFilesAsync();
                break;
            case R.id.clear_files_btn:
                clearFiles();
                break;
            default:
                break;
        }
    }

    private void initParams() {
        FileUploader.setUploadUrl(mBaseUrlEt.getText().toString().trim());

        conf = new ParamsConf();

        conf.fileName = TextUtils.isEmpty(mFilenameEt.getText().toString()) ? "" : mFilenameEt.getText().toString();
        conf.keyName = TextUtils.isEmpty(mKeyEt.getText().toString()) ? "" : mKeyEt.getText().toString();
        conf.mimeType = TextUtils.isEmpty(mMimeTypeEt.getText().toString()) ? "" : mMimeTypeEt.getText().toString();
        FileUploader.setParams(conf);
        FileUploader.setBlockConfigs(TextUtils.isEmpty(mBlockEt.getText().toString()) ? 0 : Integer.valueOf(mBlockEt.getText().toString()), TextUtils.isEmpty(mSliceEt.getText().toString()) ? 0 : Integer.valueOf(mSliceEt.getText().toString()));
    }

    private void showLoadingDialog() {
        if (mDisplayTv != null && !TextUtils.isEmpty(mDisplayTv.getText())) {
            mDisplayTv.setText("");
        }
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("等待中...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }

    private void init() {
        Config.DEBUGGING = true;
        LogRecorder.getInstance().enableLog();
        mProgressDialog = new ProgressDialog(this);
        ClientConfig config = new ClientConfig();
        config.setMaxConcurrentRequest(10);
        FileUploader.setClientConfig(config);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_FINISH:
                        String jsonResult = (String) msg.obj;
                        mDisplayTv.append(jsonResult);
                        mDisplaySv.fullScroll(ScrollView.FOCUS_DOWN);
                        dismissLoadingDialog();
                        break;
                    case MESSAGE_APPEND:
                        mDisplayTv.append((String) msg.obj);
                        mDisplaySv.fullScroll(ScrollView.FOCUS_DOWN);
                        break;
                    case GENERATE_SUCCESSFULLY:
                        mDisplayTv.setText((String) msg.obj);
                        mDisplaySv.fullScroll(ScrollView.FOCUS_DOWN);
                        dismissLoadingDialog();
                        break;
                    case GENERATE_FAILED:
                        mDisplayTv.setText((String) msg.obj);
                        mDisplaySv.fullScroll(ScrollView.FOCUS_DOWN);
                        dismissLoadingDialog();
                        break;
                }
            }
        };
    }

    private void normalUploadJson() {

        final String filePath = getCurrentFilePath();
        Log.i(TAG, "test normal path " + filePath);
        final File file = new File(filePath);
        Log.i(TAG, "file exists " + file.exists() + " can read " + file.canRead());
//		        final String token = "db17ab5d18c137f786b67c490187317a0738f94a:Njk5OTJjN2IyY2NiOTVhODhiZDhkNTM2MjNlMmZkMjNmMjYyZjJmNA==:eyJzY29wZSI6ImltYWdlczpmaWxlMTAwayIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsIm92ZXJ3cml0ZSI6MSwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";
        //参数传递
        String token = "";
        token = mTokenEt.getText().toString();
        if (TextUtils.isEmpty(token)) {
//                    token = "db17ab5d18c137f786b67c490187317a0738f94a:Njk5OTJjN2IyY2NiOTVhODhiZDhkNTM2MjNlMmZkMjNmMjYyZjJmNA==:eyJzY29wZSI6ImltYWdlczpmaWxlMTAwayIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsIm92ZXJ3cml0ZSI6MSwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";
//                    token = "db17ab5d18c137f786b67c490187317a0738f94a:NDRlNTllZGM5NjFjMWQyMjJlY2FlNzVmYWQ0ZGVkYzdlN2Q1NGM0Zg==:eyJzY29wZSI6Indjcy1zZGstdGVzdCIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsInJldHVybkJvZHkiOiJwb3NpdGlvbj0kKHg6cG9zaXRpb24pJm1lc3NhZ2U9JCh4Om1lc3NhZ2UpIiwib3ZlcndyaXRlIjowLCJmc2l6ZUxpbWl0IjowLCJpbnN0YW50IjowLCJzZXBhcmF0ZSI6MH0=";
            //指定returnBody
//                    token="db17ab5d18c137f786b67c490187317a0738f94a:Y2I0NzEzMjQ3ZjU3ODNlOTZhMDZjMGNmZjM4NTM2NTYyMzJiZDhlYw==:eyJzY29wZSI6Indjcy1zZGstdGVzdDphYmNkd2FmIiwiZGVhZGxpbmUiOiI0MDcwODgwMDAwMDAwIiwicmV0dXJuQm9keSI6ImJ1Y2tldD0kKGJ1Y2tldCkma2V5PSQoa2V5KSIsIm92ZXJ3cml0ZSI6MSwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";
//自定义返回参数
            token = "db17ab5d18c137f786b67c490187317a0738f94a:ZmM2NmViMjhkM2Q4ZGMyZmYxNThiOWMxZDUyYWU1ZjY0NzIzYzM0NA==:eyJzY29wZSI6Indhbmd3YXlob21lIiwiZGVhZGxpbmUiOiI0MDcwODgwMDAwMDAwIiwicmV0dXJuQm9keSI6Ind3aFRlc3ROYW1lPSQoeDp0ZXN0KSZpcD0kKGlwKSIsIm92ZXJ3cml0ZSI6MSwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";                    //				final String token = "db17ab5d18c137f786b67c490187317a0738f94a:ZjRmM2FiOGY2NGY5YzhmMGFkMzA4MjQ4NDJjZWNjNTllMDNhNzkxOA==:eyJzY29wZSI6ImFwaXRlc3QtbmV0cHJvYmUiLCJkZWFkbGluZSI6IjE1MTQ3NzkyMDAwMDAiLCJvdmVyd3JpdGUiOjAsImZzaXplTGltaXQiOjAsImluc3RhbnQiOjAsInNlcGFyYXRlIjowfQ==";

        }
        HashMap<String, String> callbackBody = new HashMap<String, String>();
        callbackBody.put("x:test", "customParams");
//        callbackBody.put("myurl", "http://abc");//无效
        try {
            FileUploader.upload(MainActivity.this, token, file, callbackBody, new FileUploaderListener() {

                @Override
                public void onSuccess(int status, JSONObject responseJson) {
                    Log.d(TAG, "onSuccess: " + responseJson.toString());
                    Message msg = new Message();
                    msg.what = MESSAGE_FINISH;
                    msg.obj = "onSuccess: " + responseJson.toString();
                    mHandler.sendMessage(msg);

                }

                @Override
                public void onFailure(OperationMessage operationMessage) {
                    Log.d(TAG, "onFailure: " + operationMessage.getMessage());
                    Message msg = new Message();
                    msg.what = MESSAGE_FINISH;
                    msg.obj = "onFailure: " + operationMessage.getMessage();
                    mHandler.sendMessage(msg);
                }

                @Override
                public void onProgress(UploadFileRequest request, long currentSize, long totalSize) {
                    String percent = ((float) currentSize / totalSize * 100) + "%";
                    String progressMsg = "当前: " + currentSize + ", 总: " + totalSize +
                            ", 比例: " + percent + "\r\n";
                    Log.d(TAG, progressMsg);
                    Message.obtain(mHandler, MESSAGE_APPEND, progressMsg).sendToTarget();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 异步普通上传测试
     */
    private void normalUpload() {

        final String filePath = getCurrentFilePath();
        Log.i(TAG, "test normal path " + filePath);
        final File file = new File(filePath);
        Log.i(TAG, "file exists " + file.exists() + " can read " + file.canRead());
        //参数传递
        String token = "";
        token = mTokenEt.getText().toString();
        if (TextUtils.isEmpty(token)) {
//                    token = "db17ab5d18c137f786b67c490187317a0738f94a:Njk5OTJjN2IyY2NiOTVhODhiZDhkNTM2MjNlMmZkMjNmMjYyZjJmNA==:eyJzY29wZSI6ImltYWdlczpmaWxlMTAwayIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsIm92ZXJ3cml0ZSI6MSwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";
            //wcs-sdk-test的空间token 不带key
            token = "db17ab5d18c137f786b67c490187317a0738f94a:ZTBjNWUwNzdjMWU5MDZlNDE1ZDQ3MjA4ZGM1Nzk5Yzc1ZTg4NDExNA==:eyJzY29wZSI6Indjcy1zZGstdGVzdCIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsIm92ZXJ3cml0ZSI6MCwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";
        }
        try {
            FileUploader.upload(MainActivity.this, token, file, null, new FileUploaderStringListener() {

                @Override
                public void onSuccess(int status, String responseString) {
                    Log.d(TAG, "onSuccess: " + responseString);
                    Message msg = new Message();
                    msg.what = MESSAGE_FINISH;
                    msg.obj = "onSuccess: " + responseString;
                    mHandler.sendMessage(msg);

                }

                @Override
                public void onFailure(OperationMessage operationMessage) {
                    Log.d(TAG, "onFailure: " + operationMessage.getMessage());
                    Message msg = new Message();
                    msg.what = MESSAGE_FINISH;
                    msg.obj = "onFailure: " + operationMessage.getMessage();
                    mHandler.sendMessage(msg);
                }

                @Override
                public void onProgress(UploadFileRequest request, long currentSize, long totalSize) {
                    String percent = ((float) currentSize / totalSize * 100) + "%";
                    String progressMsg = "当前: " + currentSize + ", 总: " + totalSize +
                            ", 比例: " + percent + "\r\n";
                    Log.d(TAG, progressMsg);
                    Message.obtain(mHandler, MESSAGE_APPEND, progressMsg).sendToTarget();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 异步分片上传
     */
    private void sliceUpload() {
//        String uploadToken = "db17ab5d18c137f786b67c490187317a0738f94a:OTgyMGMxZjA5NmZlMmZjYmZmNDcyM2RhYzVhMGFmNzQ4ZDg3OTkxNw==:eyJzY29wZSI6Ind1eWlrdW46cmVzdG9yZS5pcHN3IiwiZGVhZGxpbmUiOiI0MDcwODgwMDAwMDAwIiwib3ZlcndyaXRlIjoxLCJmc2l6ZUxpbWl0IjowfQ==";
        //wcs-sdk-test的空间token 不带key
        String uploadToken = "1057f27271aa52b72fc0ff4f507fe63345c114b9:MjMzNWEwMzBiMGVkNWJmODgyZmZhZDQxNTA3ODNhMWQ4NWJkNTQ4Mw==:" +
                "eyJzY29wZSI6Indjcy1zZGstdGVzdCIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsIm92ZXJ3cml0ZSI6MCwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";
        final String filePath = getCurrentFilePath();
        // "restore.ipsw";
        Log.i(TAG, "test slice path " + filePath);
        final File file = new File(filePath);
        Log.i(TAG, "file exists " + file.exists() + " can read " + file.canRead());
        if (!TextUtils.isEmpty(mTokenEt.getText().toString())) {
            uploadToken = mTokenEt.getText().toString();
        }

        FileUploader.sliceUpload(filePath, this, uploadToken, file, null, new SliceUploaderBase64Listener() {

            @Override
            public void onSliceUploadSucceed(String string) {
                Log.d(TAG, "responseJSON : " + string);
                Message msg = new Message();
                msg.what = MESSAGE_FINISH;
                msg.obj = "responseJSON : " + string;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onProgress(long uploaded, long total) {
                String percent = (float) uploaded / total * 100 + "%";
                String progressMsg = "当前: " + uploaded + ", 总: " + total +
                        ", 比例: " + percent + "\r\n";
                Log.d(TAG, progressMsg);
                Message.obtain(mHandler, MESSAGE_APPEND, progressMsg).sendToTarget();
            }

            @Override
            public void onSliceUploadFailured(HashSet<String> errorMessages) {
                StringBuilder sb = new StringBuilder();
                for (String string : errorMessages) {
                    sb.append(string + "\r\n");
                    Log.e(TAG, "errorMessage : " + string);
                }
                Message msg = new Message();
                msg.what = MESSAGE_FINISH;
                msg.obj = sb.toString();
                mHandler.sendMessage(msg);
            }
        });
    }

    private void sliceUploadJSON() {
//        String uploadToken = "db17ab5d18c137f786b67c490187317a0738f94a:OTgyMGMxZjA5NmZlMmZjYmZmNDcyM2RhYzVhMGFmNzQ4ZDg3OTkxNw==:eyJzY29wZSI6Ind1eWlrdW46cmVzdG9yZS5pcHN3IiwiZGVhZGxpbmUiOiI0MDcwODgwMDAwMDAwIiwib3ZlcndyaXRlIjoxLCJmc2l6ZUxpbWl0IjowfQ==";
        String uploadToken =
                "1057f27271aa52b72fc0ff4f507fe63345c114b9:MjMzNWEwMzBiMGVkNWJmODgyZmZhZDQxNTA3ODNhMWQ4NWJkNTQ4Mw==:eyJzY29wZSI6Indjcy1zZGstdGVzdCIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsIm92ZXJ3cml0ZSI6MCwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";        //wcs-sdk-test的空间token 不带key
//        String uploadToken = "1057f27271aa52b72fc0ff4f507fe63345c114b9:MjMzNWEwMzBiMGVkNWJmODgyZmZhZDQxNTA3ODNhMWQ4NWJkNTQ4Mw==:" +
//            "eyJzY29wZSI6Indjcy1zZGstdGVzdCIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsIm92ZXJ3cml0ZSI6MCwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";
        final String filePath = getCurrentFilePath();
        // "restore.ipsw";
        Log.i(TAG, "test slice path " + filePath);
        final File file = new File(filePath);
        Log.i(TAG, "file exists " + file.exists() + " can read " + file.canRead());
        if (!TextUtils.isEmpty(mTokenEt.getText().toString())) {
            uploadToken = mTokenEt.getText().toString();
        }

        FileUploader.sliceUpload(filePath, this, uploadToken, file, null, new SliceUploaderListener() {

            @Override
            public void onSliceUploadSucceed(JSONObject reponseJSON) {
                Log.d(TAG, "responseJSON : " + reponseJSON.toString());
                Message msg = new Message();
                msg.what = MESSAGE_FINISH;
                msg.obj = "responseJSON : " + reponseJSON.toString();
                mHandler.sendMessage(msg);
            }

            @Override
            public void onProgress(long uploaded, long total) {
                String percent = (float) uploaded / total * 100 + "%";
                String progressMsg = "当前: " + uploaded + ", 总: " + total +
                        ", 比例: " + percent + "\r\n";
                Log.d(TAG, progressMsg);
                Message.obtain(mHandler, MESSAGE_APPEND, progressMsg).sendToTarget();
            }

            @Override
            public void onSliceUploadFailured(HashSet<String> errorMessages) {
                StringBuilder sb = new StringBuilder();
                for (String string : errorMessages) {
                    sb.append(string + "\r\n");
                    Log.e(TAG, "errorMessage : " + string);
                }
                Message msg = new Message();
                msg.what = MESSAGE_FINISH;
                msg.obj = sb.toString();
                mHandler.sendMessage(msg);
            }
        });
    }

    private void generateFilesAsync() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    generateFiles();
                    Message.obtain(mHandler, GENERATE_SUCCESSFULLY, "generate files successfully").sendToTarget();
                } catch (IOException e) {
                    Message.obtain(mHandler, GENERATE_FAILED, "generateFilesAsync Failed.").sendToTarget();
                    Log.e(TAG, "generateFilesAsync Failed.");
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private String getCurrentFilePath() {
        return mFilePath + File.separator + mFileSizeSp.getSelectedItem();
    }

    public File getSDFileDir() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (!sdCardExist) {
            return null;
        }
        sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        File dir = new File(sdDir + File.separator + "WcsSdk");
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (mFilePath == null) {
            mFilePath = dir.toString();
        }
        return dir;
    }


    private void generateFiles() throws IOException {
        if (getSDFileDir() == null) {
            Log.d(TAG, "No SDcard");
            return;
        }
        String[] sFileNameArray = {"100k", "200k", "500k", "1m", "4m", "10m", "50m", "100m", "500m", "1g"};
        long[] sFileSizeArray = {102400, 204800, 512000, 1024 * 1024, 1024 * 1024 * 4, 1024 * 1024 * 10, 1024 * 1024 * 50, 1024 * 1024 * 100, 1024 * 1024 * 500, 1024 * 1024 * 1024};

        for (int i = 0; i < sFileNameArray.length; i++) {

            String filePath = mFilePath + File.separator + sFileNameArray[i];
            File file = new File(filePath);
            if (file.exists()) {
                Log.d(TAG, filePath + " exists, hash : " + WetagUtil.getEtagHash(file));
                continue;
            }
            Log.d(TAG, "Generating File " + filePath);
            byte[] buffer = new byte[1024];
            new Random().nextBytes(buffer);
            long fileSize = sFileSizeArray[i];
            FileOutputStream fos = new FileOutputStream(file);
            for (int k = 0; k < fileSize / 1024; k++) {
                fos.write(buffer);
            }
            fos.close();
            Log.d(TAG, "Generated File " + filePath + " succeeded.");
        }
    }

    /**
     * 清空file文件夹
     */
    private void clearFiles() {
        File[] files = getSDFileDir().listFiles();
        for (File file : files) {
            if (file.isFile() && file.exists()) {
                file.delete();
            }
        }
        setSpAdapter();
    }
}

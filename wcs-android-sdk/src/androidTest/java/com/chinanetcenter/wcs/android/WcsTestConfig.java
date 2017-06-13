package com.chinanetcenter.wcs.android;

import android.util.Log;

import com.chinanetcenter.wcs.android.utils.WetagUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getContext;

/**
 * @author :wangjm1
 * @version :1.0
 * @package : com.chinanetcenter.wcs.android
 * @class : ${CLASS_NAME}
 * @time : 2017/5/12 ${ITME}
 * @description :TODO
 */
public class WcsTestConfig {

    private static final String TAG = "WcsTestConfig";
    public final static String TOKEN = "db17ab5d18c137f786b67c490187317a0738f94a:NzU1ZGJlNGJlMWY0MTVhZjNmNzZhYzY4ZDExMjIwYTJkMjA1MWNjZg==:eyJzY29wZSI6ImltYWdlczpmZHNmamtkcz09MzIxamtsPSIsImRlYWRsaW5lIjoiNDA3MDg4MDAwMDAwMCIsIm92ZXJ3cml0ZSI6MSwiZnNpemVMaW1pdCI6MCwiaW5zdGFudCI6MCwic2VwYXJhdGUiOjB9";
    //{"hash":"FhHQrR69bXnu3qXFVdp9AwBgp9qk","response":"{\"url\":\"aHR0cDovL3NwYWNlMS5zLndjczk4LmJpei5tYXRvY2xvdWQuY29tL2ZpbGUxMDBr\",\"fsize\":\"102400\",\"bucket\":\"space1\"}"}
    //eb33b9b718bc94f0161da1a039e895c19e1b7b24:MzViNGMyNGE2MjY5NGQ3ZjA2OTRhZGFkYjYwYjM0NTAyMTcxYzBkYQ==:eyJzY29wZSI6InNwYWNlMSIsImRlYWRsaW5lIjoiMTQ5ODI4OTc1MDAwMCIsIm92ZXJ3cml0ZSI6MSwiZnNpemVMaW1pdCI6MCwiY2FsbGJhY2tVcmwiOiJodHRwOi8vZGVtby5jb206ODEvY2FsbGJhY2tVcmwiLCJjYWxsYmFja0JvZHkiOiJ1cmw9JCh1cmwpJmZzaXplPSQoZnNpemUpJmJ1Y2tldD0kKGJ1Y2tldCkiLCJpbnN0YW50IjowLCJzZXBhcmF0ZSI6MH0=
    private static String[] sFileNameArray = {"100k", "200k", "500k", "1m", "4m", "10m", "50m", "100m", "500m"};
    private static long[] sFileSizeArray = {102400, 204800, 512000, 1024 * 1024, 1024 * 1024 * 4, 1024 * 1024 * 10, 1024 * 1024 * 50, 1024 * 1024 * 100, 1024 * 1024 * 500};


    public static void generateFilesAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    generateFiles();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static void generateFiles() throws IOException {
        for (int i = 0; i < sFileNameArray.length; i++) {
            String filePath = getContext().getFilesDir() + File.separator + sFileNameArray[i];
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
}

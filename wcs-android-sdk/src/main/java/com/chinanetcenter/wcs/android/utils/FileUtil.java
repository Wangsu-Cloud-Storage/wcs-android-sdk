package com.chinanetcenter.wcs.android.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    public static File getFile(Context context, Uri uri) {
        uri = fileUri(context, uri);
        try {
            return new File(uri.getPath());
        } catch (Exception e) {
        }
        return null;
    }

    private static Uri fileUri(Context context, Uri uri) {
        Uri fileUri = uri;
        if (!uri.toString().startsWith("file")) {
            String filePath;
            if (uri != null && "content".equals(uri.getScheme())) {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{
                    android.provider.MediaStore.Images.ImageColumns.DATA
                }, null, null, null);
                cursor.moveToFirst();
                filePath = cursor.getString(0);
                cursor.close();
            } else {
                filePath = uri.getPath();
            }
            fileUri = Uri.parse("file://" + filePath);
        }
        return fileUri;
    }

    public static void copyFile(String fromFile, String toFile) {
        File file = new File(toFile);
        if (file.exists()) {
            if (!file.delete()) {
                WCSLogUtil.w("failed to delete " + toFile);
            }
        }
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = new FileInputStream(fromFile);
            fos = new FileOutputStream(toFile);
            byte[] b = new byte[1024];
            int count = 0;
            while ((count = is.read(b)) != -1) {
                fos.write(b, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回当前sdcard的路径，如果sdcard不存在则返回空。
     *
     * @return
     */
    public static String getSdcardPath() {
        String sdcardRootPath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sdcardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return sdcardRootPath;
    }

}

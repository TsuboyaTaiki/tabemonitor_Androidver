/*
    Created by Tsuchiya Kazuma.

    SaveStringClass
*/

package com.example.bnv_test;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Camera extends AppCompatActivity {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + Camera.class.getSimpleName();

    public static final int CAMERA_REQUEST_CODE = 1000;

    private String packageName;
    private Intent cameraIntent;
    private Uri cameraUri;
    private Context context;

    Camera(String packageName, Context context){
        this.context = context;
        this.packageName = packageName;
    }

    public void cameraConfig(File folder) {
        Intent intent;
        Uri uri;

        // ファイル名
        String timeStamp = new SimpleDateFormat("dd_HH_mm_ss", Locale.US).format(new Date());
        String fileName = timeStamp + ".jpg";

        File photoStorageDir = new File(folder, fileName);

        uri = FileProvider.getUriForFile(
                context,
                "jp.co.casareal.camerabasicsample." + packageName,
                photoStorageDir);

        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        cameraIntent = intent;
        cameraUri = uri;
    }

    public Intent getCameraIntent(){
        return cameraIntent;
    }

    public Uri getCameraUri(){
        return cameraUri;
    }
}

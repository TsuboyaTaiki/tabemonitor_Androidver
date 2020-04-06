/*
    Created by Tsuchiya Kazuma.

    VisionApiOcrClass
*/

package com.example.bnv_test;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class VisionApiOcr extends AppCompatActivity{

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + VisionApiOcr.class.getSimpleName();

    private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY;
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;

    private Context context;
    private String packageName;
    private PackageManager packageManager;
    private SaveString saveString;

    // コンストラクタの記述
    public VisionApiOcr(
            Context context,                // プログレスダイアログを表示するアクティビティのコンテキスト
            String packageName,             // パッケージ名
            PackageManager packageManager,  // パッケージマネージャー
            SaveString saveString           // SaveStringクラス
    ){
        this.context = context;
        this.packageName = packageName;
        this.packageManager = packageManager;
        this.saveString = saveString;
    }

    // 画像から文字を認識するメソッド
    // MainActivityからはこのメソッドを使う
    public void getCharacter(Bitmap bitmap) {
        // 画像のリサイズ
        bitmap = bitmapResize(bitmap);

        // CloudVisionを呼び出す
        callCloudVision(bitmap);
    }

    // 画像のリサイズをする
    private Bitmap bitmapResize(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // width と height の値を入れ替える
        if((width / height) >= 1){
            width = width + height;
            height = width - height;
            width = width - height;
        }

        while(width > 480 || height > 640){
            Log.d(LOG_TAG, "before : width = " + width + " height = " + height);
            width = width / 2;
            height = height / 2;
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            Log.d(LOG_TAG, "after : width = " + width + " height = " + height);
        }

        return bitmap;
    }

    // CloudVisionを呼び出すメソッド
    private void callCloudVision(Bitmap bitmap) {
        try {
            AsyncTask<Object, Void, String> textDetectionTask = new TextDetectionTask(
                    prepareAnnotationRequest(bitmap, packageName, packageManager),
                    saveString,
                    context);
            textDetectionTask.execute();

        } catch (IOException e) {
            Log.d(
                    "callCloudVision",
                    "IOException : " + e);
        }
    }

    // CloudVisionに送る画像へ変換するメソッド
    private Vision.Images.Annotate prepareAnnotationRequest(
            Bitmap bitmap, String packageName, PackageManager packageManager) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(packageManager, packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            Image base64EncodedImage = new Image();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature textDetection = new Feature();
                textDetection.setType("TEXT_DETECTION");
                textDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(textDetection);
            }});

            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        annotateRequest.setDisableGZipContent(true);

        return annotateRequest;
    }

    // 文字列を取得してデータベースに検索をかけるメソッド
    private String convertResponseToString(BatchAnnotateImagesResponse response) {

        TextAnnotation label = response.getResponses().get(0).getFullTextAnnotation();
        String text;

        if(label != null){
            text = label.getText();
            text.replace(" ", "");
            text.replace("\\", "");

        } else {
            text = "Character Nothing.";
        }

        return text;
    }

    //---- TextDetectionTask Class ---------------------------------------------------------------//
    private class TextDetectionTask extends AsyncTask<Object, Void, String> {

        private Vision.Images.Annotate Request;
        private SaveString saveString;
        private ProgressDialog progressDialog;

        TextDetectionTask(
                Vision.Images.Annotate annotate,
                SaveString saveString,
                Context context
        ) {
            Request = annotate;
            this.saveString = saveString;
            progressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            // プログレスダイアログのメッセージ
            progressDialog.setMessage("レシートを読み取り中…");

            // 表示中にタップをしても消えない設定
            progressDialog.setCancelable(false);

            // プログレスダイアログの表示
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                BatchAnnotateImagesResponse response = Request.execute();

                // 取得した文字列を返す
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(
                        "doInBackground",
                        "GoogleJsonResponseException : " + e);
            } catch (IOException e) {
                Log.d(
                        "doInBackground",
                        "IOException : " + e);
            }

            return "Cloud Vision API request failed. Check logs for details.";
        }

        @Override
        protected void onPostExecute(String result) {

            // プログレスダイアログの削除
            if(progressDialog != null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }

            // saveStringクラスに取得した文字列を格納
            saveString.setString(result);
        }

    }
}
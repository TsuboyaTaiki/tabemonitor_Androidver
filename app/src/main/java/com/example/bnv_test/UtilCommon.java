package com.example.bnv_test;

import android.app.Application;
import android.util.Log;

public class UtilCommon extends Application{

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + UtilCommon.class.getSimpleName();

    private String mGlobal;  // String型のグローバル変数

    /**
     * アプリケーションの起動時に呼び出される
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        mGlobal = "";
    }

    /**
     * アプリケーション終了時に呼び出される
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(LOG_TAG, "onTerminate");
        mGlobal = "";
    }

    /**
     * グローバル変数の値を変更
     * @param global 変更する値
     */
    public void setGlobal(String global) {
        Log.d(LOG_TAG, "setGlobal");
        mGlobal = global; //元はscanFlag
    }

    /**
     * グローバル変数の値を取得
     * @return グローバル変数（mGlobal）
     */
    public String getGlobal() {
        Log.d(LOG_TAG, "getGlobal");
        return mGlobal;
    }
}

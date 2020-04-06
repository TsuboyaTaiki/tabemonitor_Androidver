package com.example.bnv_test;

public class SaveString {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + SaveString.class.getSimpleName();

    private String data = "";

    void setString(String data){
        this.data = data;
    }

    String getString(){
        return this.data;
    }

    boolean isString(){
        if(data.isEmpty()){
            return false;
        } else {
            return true;
        }
    }
}

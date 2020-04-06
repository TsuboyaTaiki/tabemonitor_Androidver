package com.example.bnv_test;

public class OtherListItem {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + OtherListItem.class.getSimpleName();

    private String mTitle = null;
    private String mAmount = null;

    /**
     * 空のコンストラクタ
     */
    public OtherListItem() {};

    /**
     * コンストラクタ
     * @param title タイトル
     * @param amount 個数
     */
    public OtherListItem(String title, String amount) {
        mTitle = title;
        mAmount = amount;
    }


    /**
     * タイトルを設定
     * @param title タイトル
     */
    public void setmTitle(String title) {
        mTitle = title;
    }

    /**
     * タイトルを設定
     * @param amount 個数
     */
    public void setmAmount(String amount){ mAmount = amount; }

    /**
     * タイトルを取得
     * @return タイトル
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * 個数(文字)を取得
     * @return 個数
     */
    public String getAmount() { return mAmount; }


}

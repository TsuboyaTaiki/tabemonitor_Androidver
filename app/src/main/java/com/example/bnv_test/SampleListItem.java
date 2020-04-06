package com.example.bnv_test;

import android.graphics.Bitmap;
import android.widget.ImageButton;


public class SampleListItem {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + SampleListItem.class.getSimpleName();

    private Bitmap mThumbnail = null;
    private ImageButton mUpward = null;
    private ImageButton mDownward = null;
    private ImageButton mDelete = null;
    private String mTitle = null;
    private String mAmount = null;

    /**
     * 空のコンストラクタ
     */
    public SampleListItem() {};

    /**
     * コンストラクタ
     * @param thumbnail サムネイル画像
     * @param title タイトル
     * @param amount 個数
     */
    public SampleListItem(Bitmap thumbnail, String title, String amount, ImageButton upward, ImageButton downward, ImageButton delete) {
        mThumbnail = thumbnail;
        mTitle = title;
        mAmount = amount;
        mUpward = upward;
        mDownward = downward;
        mDelete = delete;
    }

    /**
     * サムネイル画像を設定
     * @param thumbnail サムネイル画像
     */
    public void setmThumbnail(Bitmap thumbnail) {
        mThumbnail = thumbnail;
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
     * タイトルを設定
     * @param upward 増ボタン
     */
    public void setmUpward(ImageButton upward){ mUpward = upward; }

    /**
     * タイトルを設定
     * @param downward 減ボタン
     */
    public void setmDownward(ImageButton downward){ mDownward = downward; }

    public void setmDelete(ImageButton delete) { mDelete = delete; }

    /**
     * サムネイル画像を取得
     * @return サムネイル画像
     */
    public Bitmap getThumbnail() {
        return mThumbnail;
    }

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

    /**
     * 個数(文字)を取得
     * @return 増ボタン
     */
    public ImageButton getUpward() { return mUpward; }

    /**
     * 個数(文字)を取得
     * @return 減ボタン
     */
    public ImageButton getDownward() { return mDownward; }

    public ImageButton getDelete() { return mDelete; }
}

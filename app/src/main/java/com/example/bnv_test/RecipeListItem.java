package com.example.bnv_test;

public class RecipeListItem {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + RecipeListItem.class.getSimpleName();

    private String mRname = null;

    /**
     * 空のコンストラクタ
     */
    public RecipeListItem() {};

    /**
     * コンストラクタ
     * @param rname レシピ名
     */
    public RecipeListItem(String rname) { mRname = rname; }

    /**
     * タイトルを設定
     * @param rname レシピ名
     */
    public void setmRname(String rname) {
        mRname = rname;
    }

    /**
     * タイトルを取得
     * @return タイトル
     */
    public String getRname() {
        return mRname;
    }


}

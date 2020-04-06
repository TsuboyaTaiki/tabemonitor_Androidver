package com.example.bnv_test;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_CLASSIFICATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_FOOD_NAME;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_FOOD_NAME_HIRAGANA;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_ID;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_STOCK;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_STOCK_FLUCTUATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_UNIT_NAME;
import static com.example.bnv_test.MyDatabaseOpenHelper.TABLE_NAME_FOOD;

public class ChooseFoodsFragment extends Fragment {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + ChooseFoodsFragment.class.getSimpleName();

    private static final String RADIO_STRING_MEAT = "肉";
    private static final String RADIO_STRING_SEAFOOD = "魚介";
    private static final String RADIO_STRING_VEGETABLE = "野菜";
    private static final String RADIO_STRING_OTHER = "その他";

    private String radioflagStr = RADIO_STRING_MEAT;
    private String foodIdStr = "";
    private PopupWindow mPopupWindow;
    private UtilCommon common;
    private GridView gridView;
    private Cursor cursor;
    private MyDatabaseOpenHelper helper;
    private SQLiteDatabase db;

    private String[] array_food_id;
    private String[] array_food_name;
    private int[]    array_food_stock;
    private String[] array_food_unit_name;
    private int[]    array_food_fluctuation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate");

        // 初期化処理
        helper = new MyDatabaseOpenHelper(getContext());
        db = helper.getWritableDatabase();
        int number = helper.getFoodsAmount(db);

        array_food_id = new String[number];
        array_food_name = new String[number];
        array_food_stock = new int[number];
        array_food_unit_name = new String[number];
        array_food_fluctuation = new int[number];
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_foods, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readData();

        // 一度AddRecipeFragmentに戻ってもIDを保持
        common = (UtilCommon) getContext().getApplicationContext();
        if(foodIdStr.equals("")){
            foodIdStr = common.getGlobal();
        }

        // 決定ボタンを押した際の処理
        Button foods_Confirm = getActivity().findViewById(R.id.foods_confirm);
        foods_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                if(fragmentManager != null) {

                    // 前のフラグメントに食材データをグローバル変数に格納
                    common.setGlobal(foodIdStr);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    //遷移アニメーション
                    fragmentTransaction.setTransition(fragmentTransaction.TRANSIT_FRAGMENT_FADE);

                    //バックスタック
                    fragmentManager.popBackStack();
                }
            }
        });

        gridView = getActivity().findViewById(R.id.gridview_food_choose);
        setGridView(false);

        // ラジオグループのインスタンスと「すべて」ボタンのインスタンスを作成し、「Meat」をアクティブにする
        RadioGroup radioGroup = (RadioGroup) getActivity().findViewById(R.id.RadioGroup_choose_foods);
        radioGroup.check(R.id.Meat_choose);

        // ラジオボタンをタップした際のイベント処理
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            public void onCheckedChanged(RadioGroup group,int checkedId){

                // 押されたラジオボタンをIDで判別し、インスタンスを作成
                RadioButton radioButton = (RadioButton) getActivity().findViewById(checkedId);

                // ヘッダ用テキストビューのインスタンスを作成
                TextView headertext = (TextView) getActivity().findViewById(R.id.Rheader);

                switch(checkedId){
                    //ラジオボタン「肉」が押された際の処理
                    case R.id.Meat_choose:
                        radioflagStr = RADIO_STRING_MEAT;
                        setGridView(false);
                        break;

                    //ラジオボタン「魚」が押された際の処理
                    case R.id.Fish_choose:
                        radioflagStr = RADIO_STRING_SEAFOOD;
                        setGridView(false);
                        break;

                    //ラジオボタン「野菜」が押された際の処理
                    case R.id.Vegetable_choose:
                        radioflagStr = RADIO_STRING_VEGETABLE;
                        setGridView(false);
                        break;

                    //ラジオボタン「その他」が押された際の処理
                    case R.id.Other_choose:
                        radioflagStr = RADIO_STRING_OTHER;
                        setGridView(false);
                        break;
                }
            }
        });

        // グリッドビューをタップした際のイベント処理
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                //ポップアップウィンドウのインスタンスを作成し、「addpop_layout」をインフレート
                mPopupWindow = new PopupWindow(getContext());
                final View popupView = getLayoutInflater().inflate(R.layout.addpopup_layout, null);

                //ポップアップウィンドウのビューを設定し、背景に「addpopup_background」ファイルを割り当てる
                mPopupWindow.setContentView(popupView);
                mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.addpopup_background));

                // タップ時に他のViewでキャッチされないための設定
                mPopupWindow.setOutsideTouchable(true);
                mPopupWindow.setFocusable(true);

                // 表示サイズの設定
                float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
                mPopupWindow.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
                mPopupWindow.setWidth((int) width);
                mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

                //テキストビューにタップした食材名と単位を表示
                ((TextView) mPopupWindow.getContentView().findViewById(R.id.addpop_text)).setText(array_food_name[position]);
                ((TextView) mPopupWindow.getContentView().findViewById(R.id.addpop_unit)).setText(array_food_unit_name[position]);

                // OKボタン(ok_button)のタップイベント処理
                popupView.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Fエディットテキストのインスタンスを作成(インフレートした「popupView」からEditTextのIDを探している)
                        EditText add_editText = popupView.findViewById(R.id.addpop_edittext);

                        // 未入力の際にNullPointerExceptionが発生するのを回避するための処理
                        try {
                            // EditTextが空でなければ
                            if(!(add_editText.getText().toString().equals(""))){

                                //エディットテキストに入力された数値をint型に変換しデータベースをアップデート
                                int add_value = Integer.parseInt(add_editText.getText().toString());

                                makeFoodId(array_food_id[position], add_value);

                                setGridView(true);
                            }
                            else {
                                //例外処理に移動
                                throw new NullPointerException();
                            }
                        }
                        //例外が発生した際の処理
                        catch(NullPointerException e){
                            e.printStackTrace();
                        }

                        //ポップアップウィンドウが表示されているなら削除する
                        if (mPopupWindow.isShowing()) {
                            mPopupWindow.dismiss();
                        }
                    }
                });

                // 画面中央にポップアップを表示
                mPopupWindow.showAtLocation(getActivity().findViewById(R.id.gridview_food_choose), Gravity.CENTER, 0, 0);
            }
        });
    }

    // データベースのデータを抽出するメソッド
    private void readData(){

        cursor = db.query(
                TABLE_NAME_FOOD,
                new String[]{
                        FOOD_COLUMN_ID, FOOD_COLUMN_FOOD_NAME, FOOD_COLUMN_STOCK,
                        FOOD_COLUMN_UNIT_NAME, FOOD_COLUMN_STOCK_FLUCTUATION,
                        FOOD_COLUMN_FOOD_NAME_HIRAGANA
                },
                FOOD_COLUMN_CLASSIFICATION + " = ?",
                new String[]{radioflagStr},
                null,
                null,
                FOOD_COLUMN_FOOD_NAME_HIRAGANA + " ASC"
        );

        cursor.moveToFirst();

        // リストビュー用の配列に食材テーブルの食材名を代入
        for (int i = 0; i < cursor.getCount(); i++ ){
            array_food_id[i] = cursor.getString(0);
            array_food_name[i] = cursor.getString(1);
            array_food_stock[i] = cursor.getInt(2);
            array_food_unit_name[i] = cursor.getString(3);
            array_food_fluctuation[i] = cursor.getInt(4);
            cursor.moveToNext();
        }

        cursor.close();
    }

    // トースト作成用メソッド
    private void toastMake(String message,int x,int y){
        Toast toast = Toast.makeText(this.getContext(),message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,x,y);
        toast.show();
    }

    // レシピに必要な食材のIDと量を格納した文字列を作成するメソッド
    // String id : 食材のID
    // int amount : 使用する食材の個数
    private void makeFoodId(String id,int amount){

        String addStr = "_";    // 選択された食材が二番目以降のとき使う
        String[] recipeFoodIdSplit;

        // 同じ食材を選んだ場合の処理
        if(!foodIdStr.equals("") && foodIdStr.contains(id)) {
            recipeFoodIdSplit = foodIdStr.split("_");
            for(int i = 0; i < recipeFoodIdSplit.length; i += 2){
                if(recipeFoodIdSplit[i].equals(id)){
                    recipeFoodIdSplit[i + 1] =
                            String.valueOf(Integer.valueOf(recipeFoodIdSplit[i + 1]) + amount);
                    break;
                }
            }

            // レシピの食材IDを再連結させる
            StringBuilder foodId = new StringBuilder();
            for(int i = 0; i < recipeFoodIdSplit.length; i += 2){
                if(i != 0){
                    foodId.append("_");
                }
                foodId.append(recipeFoodIdSplit[i]);
                foodId.append("_");
                foodId.append(recipeFoodIdSplit[i + 1]);
            }
            foodIdStr = foodId.toString();
        }
        // 通常の処理
        else {
            // idにIDと量の境目となる「 _ 」を結合
            id = id.concat("_");
            // idに「 _ 」の後ろに量を結合する
            id = id.concat(String.valueOf(amount));

            if (!foodIdStr.equals("")) {
                // 先頭に「 _ 」をつけた状態にする
                addStr = addStr.concat(id);
                foodIdStr = foodIdStr.concat(addStr);
            } else {
                // 1つ目の食材のみここを通る
                foodIdStr = foodIdStr.concat(id);
            }
        }
    }

    // グリッドビューのセット
    private void setGridView(boolean positionSet){

        int position = 0;
        int y = 0;

        readData();

        //リストのインスタンスを作成
        List<Integer> imgList = new ArrayList<>();

        //配列「meats」の要素数分だけループ
        for (int i = 0; i < cursor.getCount(); i++ ){
            int imageId = getResources().getIdentifier(array_food_name[i],"drawable", getActivity().getPackageName());
            imgList.add(imageId);
        }

        if(positionSet) {
            position = gridView.getFirstVisiblePosition();
            try {
                if (gridView.getChildCount() > 0) {
                    y = gridView.getChildAt(0).getTop() - gridView.getPaddingTop();
                }
                Log.d(LOG_TAG, "position = " + position);
                Log.d(LOG_TAG, "y = " + y);
                Log.d(LOG_TAG, "getChildCount = " + gridView.getChildCount());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // グリッドビューとグリッドアダプターのインスタンスを作成
        Grid_choose_foods_Adapter gridAdapter = new Grid_choose_foods_Adapter(
                this.getContext(), R.layout.gridview_choose_food_items, imgList,
                array_food_name, array_food_id, foodIdStr.split("_")
        );

        // グリッドビューにアダプターで取得したデータをセット
        gridView.setAdapter(gridAdapter);
        gridView.setSelectionFromTop(position, y);

        //アダプターにデータの変更を通知
        gridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().setTitle("  レシピを追加");
    }

}

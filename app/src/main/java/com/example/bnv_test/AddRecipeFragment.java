package com.example.bnv_test;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_CLASSIFICATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_FOOD_NAME;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_FOOD_NAME_HIRAGANA;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_ID;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_STOCK;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_STOCK_FLUCTUATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_UNIT_NAME;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_CLASSIFICATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_DETAILS;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_FOOD_ID;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_ID;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_RECIPE_NAME;
import static com.example.bnv_test.MyDatabaseOpenHelper.TABLE_NAME_FOOD;
import static com.example.bnv_test.MyDatabaseOpenHelper.TABLE_NAME_RECIPE;

public class AddRecipeFragment extends Fragment {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + AddRecipeFragment.class.getSimpleName();

    private String radioFlagStr = "";           // レシピのカテゴリ
    private String foodIdStr = "";              // 使う食材と量を表す文字列
    private String addRecipeName = "";          // レシピ名
    private String toastMessage;
    private ListView listView;
    private SampleListItem item;
    private ArrayList<SampleListItem> listItems;
    private listViewAdapter adapter;
    private ImageButton upbtn;
    private ImageButton downbtn;
    private ImageButton delete;
    private Bundle radiostate = new Bundle();
    private UtilCommon common;
    private Cursor cursor;
    private MyDatabaseOpenHelper helper;
    private SQLiteDatabase db;

    private String[] array_food_id;
    private String[] array_food_name;
    private int[]    array_food_stock;
    private String[] array_food_unit_name;
    private int[]    array_food_fluctuation;

    // 列挙型定数宣言
    private enum getDataFromIdEnum{
        FOOD_NAME,
        UNIT_NAME,
        FOOD_FLUCTUATION
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate");

        // 初期化処理
        // グローバル変数を初期化する
        common = (UtilCommon) getContext().getApplicationContext();
        common.setGlobal("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_recipe, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        // UtilCommonクラスをインスタンス化し、グローバル変数を使用可能にする
        common = (UtilCommon) getContext().getApplicationContext();

        // グローバル変数から使う食材データを取得
        foodIdStr = common.getGlobal();

        // toastMake(foodIdStr,0,600);
        Log.d("確認", foodIdStr);

        //「ChooseFoodsFragment」でタップした食材をリストで表示するためのリストビュー
        listView = (ListView)view.findViewById(R.id.food_registration);

        //リストビューの最後に余白を追加する
        TextView empty = new TextView(getContext());
        empty.setHeight(300);
        listView.addFooterView(empty,null,false);

        // リストビューに表示する要素を設定
        listItems = new ArrayList<>();
        adapter = new listViewAdapter(getContext(),R.layout.listview_item, listItems);

        // リストを表示する
        if(!foodIdStr.equals("")) {
            String[] recipeFoodIdSplit = foodIdStr.split("_");

            // 配列の初期化
            array_food_id = new String[recipeFoodIdSplit.length / 2];
            array_food_name = new String[recipeFoodIdSplit.length / 2];
            array_food_stock = new int[recipeFoodIdSplit.length / 2];
            array_food_unit_name = new String[recipeFoodIdSplit.length / 2];
            array_food_fluctuation = new int[recipeFoodIdSplit.length / 2];

            for (int i = 0, j = 0; i < recipeFoodIdSplit.length; i += 2, j++) {
                upbtn = (ImageButton) view.findViewById(R.id.upward);
                downbtn = (ImageButton) view.findViewById(R.id.downward);

                array_food_id[j] = recipeFoodIdSplit[i];
                array_food_name[j] = getDataFromId(recipeFoodIdSplit[i], getDataFromIdEnum.FOOD_NAME);
                array_food_stock[j] = Integer.valueOf(recipeFoodIdSplit[i + 1]);
                array_food_unit_name[j] = getDataFromId(recipeFoodIdSplit[i], getDataFromIdEnum.UNIT_NAME);
                array_food_fluctuation[j] = Integer.valueOf(getDataFromId(recipeFoodIdSplit[i], getDataFromIdEnum.FOOD_FLUCTUATION));
            }
            updateList();
        }

        // レシピ画面に戻るフローティングアクションボタンのタップイベント
        FloatingActionButton fab = (FloatingActionButton)getActivity().findViewById(R.id.return_fab_recipe);
        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                FragmentManager fragmentManager = getFragmentManager();

                if(fragmentManager != null) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    //遷移アニメーション
                    fragmentTransaction.setTransition(fragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    //バックスタック
                    fragmentManager.popBackStack();
                }
            }
        });

        //「確定」ボタンのリスナー
        Button addRecipe_Confirm = getActivity().findViewById(R.id.recipe_confirm);
        addRecipe_Confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //キーボードを消す
                InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        //ラジオボタンのリスナー
        RadioGroup radioGroup = getActivity().findViewById(R.id.RadioGroup_regist);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //押されたラジオボタンをIDで判別し、インスタンスを作成
                RadioButton radioButton = getActivity().findViewById(checkedId);

                switch(checkedId){
                    //ラジオボタン「肉」が押された際の処理
                    case R.id.Meat_regist:
                        radiostate.putString("radiostate","肉");
                        radioFlagStr = "肉";

                        break;

                    //ラジオボタン「魚介類」が押された際の処理
                    case R.id.Fish_regist:
                        radiostate.putString("radiostate","魚介類");
                        radioFlagStr = "魚介類";

                        break;

                    //ラジオボタン「野菜類」が押された際の処理
                    case R.id.Vegetable_regist:
                        radiostate.putString("radiostate","野菜類");
                        radioFlagStr = "野菜類";

                        break;

                    //ラジオボタン「ご飯」が押された際の処理
                    case R.id.Other_regist:
                        radiostate.putString("radiostate","ご飯");
                        radioFlagStr = "ご飯";

                        break;
                }
            }
        });

        //「食材を追加」ボタンのリスナー
        Button choose_Foods = getActivity().findViewById(R.id.choose_foods);
        choose_Foods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                common.setGlobal(foodIdStr);

                //「ChooseFoodsFragment」に遷移
                // トランザクションを実行するためのマネージャーのインスタンスを作成
                FragmentManager fragmentManager = getFragmentManager();

                // ConsumptionFragmentのインスタンスを作成
                ChooseFoodsFragment fragment = new ChooseFoodsFragment();

                if(fragmentManager != null) {
                    //トランザクションのインスタンスを作成
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setTransition(fragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    //バックスタックを設定
                    //バックスタック :「戻る」ボタンを選択した際に前のフラグメントに戻れるようにするメソッド
                    fragmentTransaction.addToBackStack(null);

                    //「fragment_container」に追加された既存のフラグメントを置き換える
                    fragmentTransaction.replace(R.id.fragment_container, fragment);

                    //トランザクションを適用
                    fragmentTransaction.commit();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (view.getId()){
                    case R.id.upward:
                        array_food_stock[position] = array_food_stock[position] + array_food_fluctuation[position];
                        updateList();
                        break;
                    case R.id.downward:
                        array_food_stock[position] = array_food_stock[position] - array_food_fluctuation[position];     // 増減量分だけマイナスする

                        if(array_food_stock[position] <= 0) {
                            // トーストの表示
                            Toast.makeText(
                                    getContext(),
                                    array_food_name[position] + "が削除されました",
                                    Toast.LENGTH_SHORT
                            ).show();
                            // データのアップデート
                            updateData();
                        }

                        updateList();
                        break;

                    case R.id.delete:
                        // アラートダイアログの表示
                        new AlertDialog.Builder(getContext())
                                .setMessage(array_food_name[position] + "を削除しますか？")
                                .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        array_food_stock[position] = 0;

                                        // トーストの表示
                                        Toast.makeText(
                                                getContext(),
                                                array_food_name[position] + "が削除されました",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        // データのアップデート
                                        updateData();
                                        updateList();
                                    }
                                })
                                .setNegativeButton("キャンセル", null)
                                .show();
                        break;
                    default:
                        break;
                }
            }
        });

        //「登録」ボタンのリスナー
        Button regist_Recipe = getActivity().findViewById(R.id.registration);
        regist_Recipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText addRecipe_Name_edit = getActivity().findViewById(R.id.recipe_name);
                addRecipeName = addRecipe_Name_edit.getText().toString();
                if (!(addRecipeName.equals(""))){       // レシピ名の有無
                    if (!(radioFlagStr.equals(""))){    // レシピジャンルの有無
                        if(!(foodIdStr.equals(""))){    // 食材選択の有無

                            // レシピの食材IDを並び替える（考案機能を壊さないため）
                            helper.recipeFoodIdSort(foodIdStr);

                            // insertを行う
                            insertData(makeRecipeId(),addRecipeName,radioFlagStr,"",foodIdStr);

                            // グローバル変数を初期化する
                            common.setGlobal("");

                            FragmentManager fragmentManager = getFragmentManager();

                            if(fragmentManager != null) {
                                // トーストの表示
                                Toast.makeText(
                                        getContext(),
                                        addRecipeName + "が追加されました",
                                        Toast.LENGTH_LONG
                                ).show();

                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                //遷移アニメーション
                                fragmentTransaction.setTransition(fragmentTransaction.TRANSIT_FRAGMENT_FADE);

                                //バックスタック
                                fragmentManager.popBackStack();
                            }
                        }else {
                            toastMake("使用する食材を選択してください", 0, 600);
                        }
                    }else {
                        toastMake("レシピのジャンルを選択してください",0,600);
                    }
                }else {
                    toastMake("レシピの名前を入力してください",0,600);
                }
            }
        });
    }


    // データアップデート後のリストビュー更新
    private void updateList(){

        ArrayList<SampleListItem> updateListitem;
        updateListitem = new ArrayList<>();
        StringBuilder recipeFoodId = new StringBuilder("");

        for (int i = 0; i < array_food_id.length; i++) {
            item = new SampleListItem(
                    null, array_food_name[i],
                    array_food_stock[i] + array_food_unit_name[i],
                    upbtn, downbtn, delete
            );
            updateListitem.add(item);

            // レシピの食材IDを更新
            if(i != 0){
                recipeFoodId.append("_");
            }
            recipeFoodId.append(array_food_id[i]);
            recipeFoodId.append("_");
            recipeFoodId.append(array_food_stock[i]);
        }

        // グローバル変数に渡す
        foodIdStr = recipeFoodId.toString();

        int tposition = 0;
        int vposition = listView.getFirstVisiblePosition();
        try {
            if (listView.getChildCount() > 0) {
                tposition = listView.getChildAt(0).getTop() - listView.getPaddingTop();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        // 出力結果をリストビューに表示
        adapter = new listViewAdapter(this.getContext(), R.layout.listview_item, updateListitem);
        listView.setAdapter(adapter);
        listView.setSelectionFromTop(vposition,tposition);

        adapter.notifyDataSetChanged();
    }

    // リストデータのアップデート
    private void updateData(){

        int count = 0;
        String[] food_id = new String[array_food_id.length];
        String[] food_name = new String[array_food_name.length];
        int[]    food_stock = new int[array_food_stock.length];
        String[] food_unit_name = new String[array_food_unit_name.length];
        int[]    food_fluctuation = new int[array_food_fluctuation.length];

        for(int i = 0; i < array_food_id.length; i++){
            if(array_food_stock[i] > 0){
                food_id[count] = array_food_id[i];
                food_name[count] = array_food_name[i];
                food_stock[count] = array_food_stock[i];
                food_unit_name[count] = array_food_unit_name[i];
                food_fluctuation[count] = array_food_fluctuation[i];
                count++;
            }
        }

        // 初期化
        array_food_id = new String[count];
        array_food_name = new String[count];
        array_food_stock = new int[count];
        array_food_unit_name = new String[count];
        array_food_fluctuation = new int[count];

        // データ移行
        for(int i = 0; i < count; i++) {
            array_food_id[i] = food_id[i];
            array_food_name[i] = food_name[i];
            array_food_stock[i] = food_stock[i];
            array_food_unit_name[i] = food_unit_name[i];
            array_food_fluctuation[i] = food_fluctuation[i];
        }
    }

    // レシピ登録に必要なIDを作るメッソド
    private String makeRecipeId(){
        String recipeId;

        if(helper == null){
            helper = new MyDatabaseOpenHelper(getContext());
        }
        if(db == null){
            db = helper.getWritableDatabase();
        }
        cursor = db.query(
                TABLE_NAME_RECIPE, new String[]{RECIPE_COLUMN_ID},
                null, null, null,
                null, null
        );

        // 登録されているレシピの数＋1の値をrecipeIdに代入
        recipeId = String.valueOf(cursor.getCount() + 1);

        // IDが３桁になるように文字列変換
        if(cursor.getCount() < 99){
            recipeId = "0".concat(recipeId);
            if(cursor.getCount() < 9){
                recipeId = "0".concat(recipeId);
            }
        }

        cursor.close();

        return recipeId;
    }

    // レシピ登録を行うメソッド
    private void insertData(String id, String recipeName, String classification, String details, String foodId){
        if(helper == null){
            helper = new MyDatabaseOpenHelper(getContext());
        }
        if(db == null){
            db = helper.getWritableDatabase();
        }

        // cvにレシピ登録に必要な情報を置く
        ContentValues cv = new ContentValues();
        cv.put(RECIPE_COLUMN_ID, id);
        cv.put(RECIPE_COLUMN_RECIPE_NAME, recipeName);
        cv.put(RECIPE_COLUMN_CLASSIFICATION, classification);
        cv.put(RECIPE_COLUMN_DETAILS, details);
        cv.put(RECIPE_COLUMN_FOOD_ID, foodId);

        // データのinsert実行
        db.insert(TABLE_NAME_RECIPE, null, cv);
    }

    //トースト作成用メソッド
    private void toastMake(String message,int x,int y){
        Toast toast = Toast.makeText(this.getContext(),message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,x,y);
        toast.show();
    }

    // 食材IDでデータベースを検索しデータを返すメソッド
    private String getDataFromId(String foodId, getDataFromIdEnum dataUnit){

        // データベース処理の初期化
        if(helper == null){
            helper = new MyDatabaseOpenHelper(getContext());
        }
        if(db == null){
            db = helper.getWritableDatabase();
        }

        Cursor cursor = db.query(
                TABLE_NAME_FOOD,
                new String[]{
                        FOOD_COLUMN_FOOD_NAME, FOOD_COLUMN_UNIT_NAME,
                        FOOD_COLUMN_STOCK_FLUCTUATION
                },
                FOOD_COLUMN_ID + " = ?", new String[]{foodId},
                null, null, null
        );

        cursor.moveToFirst();

        try {
            switch (dataUnit) {
                case FOOD_NAME:
                    return cursor.getString(0);
                case UNIT_NAME:
                    return cursor.getString(1);
                case FOOD_FLUCTUATION:
                    return cursor.getString(2);
                default:
                    throw new Exception();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }
}
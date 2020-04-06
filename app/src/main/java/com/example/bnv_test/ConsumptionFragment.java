package com.example.bnv_test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_CLASSIFICATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_FOOD_NAME;
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


public class ConsumptionFragment extends Fragment {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + ConsumptionFragment.class.getSimpleName();

    private int[] foodRegister = new int[20];
    private SampleListItem sitem;
    private OtherListItem oitem;
    private ArrayList<SampleListItem> consumItems;
    private ArrayList<OtherListItem> otherItems;
    private ArrayList<Integer> stock;
    private ListView consumlist;
    private ListView otherlist;
    private listViewAdapter consumAdapter;
    private otherlistAdapter otherAdapter;
    private ImageButton upbtn;
    private ImageButton downbtn;
    private Button ConfirmButton;
    private Cursor cursor;
    private MyDatabaseOpenHelper helper;
    private SQLiteDatabase db ;

    private String recipeNameStr;
    private String recipeIdStr;
    private String array_recipe_id;
    private String array_recipe_name;
    private String array_recipe_classicafition;
    private String array_recipe_details;
    private String array_recipe_food_id;

    private String[] array_use_food_id;
    private String[] array_use_food_name;
    private String[] array_use_food_consume;
    private String[] array_use_food_classification;
    private String[] array_use_food_unit;
    private int[] array_use_food_stock;
    private int[] array_food_fluctuation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate");

        // 初期化処理
        helper = new MyDatabaseOpenHelper(getContext());
        db = helper.getWritableDatabase();
        int number = helper.getFoodsAmount(db);

        array_use_food_id = new String[number];
        array_use_food_name = new String[number];
        array_use_food_consume = new String[number];
        array_use_food_classification = new String[number];
        array_use_food_unit = new String[number];
        array_use_food_stock = new int[number];
        array_food_fluctuation = new int[number];
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_consumption,container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //管理画面に戻るボタンのタップイベント
        Button return_btn = (Button)getActivity().findViewById(R.id.return_button);
        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();

                if(fragmentManager != null) {
                    fragmentManager.popBackStack();
                }
            }
        });

        /*消費する食材のリストビュー*/
        /*******************************************************************************************************************************************/

        //レシピIDをString型でレシピ画面から受け取る
        recipeIdStr = getArguments().getString("recipeId");
        recipeNameStr = getArguments().getString("recipeName");
        getActivity().setTitle("  " + recipeNameStr + "で消費する食材の確認");

        // レイアウトから消費する食材のリストビューを取得
        consumlist = (ListView) view.findViewById(R.id.consum_foods);
        View cHeader = (View)getLayoutInflater().inflate(R.layout.consumlist_header,null);

        //リストビューにヘッダーを追加
        consumlist.addHeaderView(cHeader);

        // リストビューに表示する要素を設定
        consumItems = new ArrayList<>();

        stock = new ArrayList<>();

        readData();

        // リストビューごとにボタンを配置するためにitemに代入している
        for (int i = 0; !array_use_food_id[i].equals(""); i++) {
            if(!(array_use_food_classification[i].equals("def"))) {
                //画像、増減ボタンのインスタンスを作成
                upbtn = (ImageButton) view.findViewById(R.id.upward);
                downbtn = (ImageButton) view.findViewById(R.id.downward);

                sitem = new SampleListItem(null, array_use_food_name[i], array_use_food_consume[i] + array_use_food_unit[i], null, null, null);
                consumItems.add(sitem);

                // 赤文字用に食材のストック数を List に格納
                stock.add(array_use_food_stock[i]);
            }
        }

        // 出力結果をリストビューに表示
        consumAdapter = new listViewAdapter(getContext(), R.layout.consumlist_item, consumItems, stock);
        consumlist.setAdapter(consumAdapter);

        /*別途必要な食材のリストビュー*/
        /********************************************************************************************************************************************/

        // レイアウトからリストビューを取得
        otherlist = (ListView) view.findViewById(R.id.other_foods);
        View oHeader = (View)getLayoutInflater().inflate(R.layout.otherlist_header,null);

        //リストビューにヘッダーを追加
        otherlist.addHeaderView(oHeader);

        // リストビューに表示する要素を設定
        otherItems = new ArrayList<>();

        // リストビューごとにボタンを配置するためにitemに代入している
        for (int i = 0;  !(array_use_food_id[i].equals("")); i++) {
            if ((array_use_food_classification[i].equals("def"))) {
                oitem = new OtherListItem(array_use_food_name[i], array_use_food_consume[i] + array_use_food_unit[i]);
                otherItems.add(oitem);
            }
        }

        // 出力結果をリストビューに表示
        otherAdapter = new otherlistAdapter(getContext(), R.layout.otherlist_item, otherItems);
        otherlist.setAdapter(otherAdapter);

        //「確定」ボタンの処理
        ConfirmButton = getActivity().findViewById(R.id.Confirm_btn);
        ConfirmButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                for (int i = 0; !array_use_food_id[i].equals("") ; i++ ){
                    if(!(array_use_food_classification[i].equals("def"))) {
                        if (array_use_food_stock[i] - Integer.valueOf(array_use_food_consume[i]) >= 0) {
                            updateData(db, array_use_food_id[i], array_use_food_stock[i] - Integer.valueOf(array_use_food_consume[i]));
                        }
                        else if (array_use_food_stock[i] - Integer.valueOf(array_use_food_consume[i]) < 0) {
                            updateData(db, array_use_food_id[i], 0);
                        }
                    }
                }

                //レシピ画面に戻る処理
                FragmentManager fragmentManager = getFragmentManager();
                if(fragmentManager != null) {
                    fragmentManager.popBackStack();
                }

                // トーストの表示
                StringBuilder toastText = new StringBuilder();
                for(int i = 0; !(array_use_food_name[i].equals("")); i++) {
                    if(!(array_use_food_classification[i].equals("def"))) {
                        toastText.append(array_use_food_name[i]);
                        toastText.append(" が ");
                        toastText.append(array_use_food_consume[i]);
                        toastText.append(array_use_food_unit[i]);
                        toastText.append("\n");
                    }
                }
                toastText.append("冷蔵庫から消費されました。");
                Toast.makeText(getContext(), toastText.toString(), Toast.LENGTH_LONG).show();


                // 変更した食材の個数の値の保持をする
                StringBuilder updateRecipeId = new StringBuilder();
                for(int i = 0; !array_use_food_name[i].equals(""); i++){
                    updateRecipeId.append(array_use_food_id[i]);
                    updateRecipeId.append("_");
                    updateRecipeId.append(array_use_food_consume[i]);
                    if(!array_use_food_name[i + 1].equals("")) {
                        updateRecipeId.append("_");
                    }
                }

                // 確認用ログ(変更前と変更後のレシピID)
                Log.d(LOG_TAG,"" + array_recipe_food_id);
                Log.d(LOG_TAG,"" + updateRecipeId);

                updateData(db, array_recipe_id, updateRecipeId.toString());
            }
        });

        consumlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (view.getId()){
                    case R.id.upward:

                        int up_cnt = Integer.valueOf(array_use_food_consume[position]);

                        if(foodRegister[position] < 0){
                            up_cnt = foodRegister[position] + array_food_fluctuation[position];
                            foodRegister[position] = 0;
                        }else {
                            up_cnt += array_food_fluctuation[position];
                        }
                        array_use_food_consume[position] = String.valueOf(up_cnt);

                        readConsum();
                        readOther();

                        break;
                    case R.id.downward:
                        int down_cnt = Integer.valueOf(array_use_food_consume[position]);
                        int foodOfNumber;

                        foodOfNumber = down_cnt - array_food_fluctuation[position];

                        if(foodOfNumber >= 0) {
                            down_cnt -= array_food_fluctuation[position];
                        }else if(foodOfNumber == -1 || foodOfNumber == -50){
                            down_cnt = 0;
                        }else {
                            foodRegister[position] = down_cnt - array_food_fluctuation[position];
                            down_cnt = 0;
                        }
                        array_use_food_consume[position] = String.valueOf(down_cnt);

                        readConsum();
                        readOther();

                        break;
                }
            }
        });
    }

    private void readData(){
        if(helper == null){
            helper = new MyDatabaseOpenHelper(getContext());
        }

        if(db == null){
            db = helper.getWritableDatabase();
        }

        cursor = db.query(
                TABLE_NAME_RECIPE,
                new String[]{
                        RECIPE_COLUMN_ID,
                        RECIPE_COLUMN_RECIPE_NAME,
                        RECIPE_COLUMN_CLASSIFICATION,
                        RECIPE_COLUMN_DETAILS,
                        RECIPE_COLUMN_FOOD_ID},
                RECIPE_COLUMN_ID + " = ?",
                new String[]{recipeIdStr},
                null,
                null,
                null
        );

        // データの格納
        cursor.moveToFirst();
        array_recipe_id = cursor.getString(0);
        array_recipe_name = cursor.getString(1);
        array_recipe_classicafition = cursor.getString(2);
        array_recipe_details = cursor.getString(3);
        array_recipe_food_id = cursor.getString(4);
        cursor.close();

        recipeFoodIdSplit();
        readFoodName();
    }

    // レシピの食材IDを食材IDと消費数に分割する
    // Ex. 401_2_034_100_112_2_129_1_801_800g_601_適量_602_少量_603_大さじ1強_604_サラダ油
    // → array_use_food_id      = {401, 034, 112, 129, 801, 601, 602, 603, 604}
    // → array_use_food_consume  = {2, 100, 2, 1, 800g, 適量, 少量, 大さじ1強, 大さじ4}
    private void recipeFoodIdSplit(){
        // 配列の初期化を行う
        for (int i = 0; i < array_use_food_id.length; i++){
            array_use_food_id[i] = "";
            array_use_food_name[i] = "";
            array_use_food_consume[i] = "";
        }
        if(array_recipe_food_id != null){
            String[] tmp = array_recipe_food_id.split("_");
            for(int i = 0, j = 0; i < tmp.length / 2; i++, j += 2){
                array_use_food_id[i] = tmp[j];
                array_use_food_consume[i] = tmp[j + 1];
            }
        }
    }

    private void readFoodName(){
        if(helper == null){
            helper = new MyDatabaseOpenHelper(getContext());
        }

        if(db == null){
            db = helper.getWritableDatabase();
        }

        for (int i = 0 ; !array_use_food_id[i].equals("") ; i++ ) {
            cursor = db.query(
                    TABLE_NAME_FOOD,
                    new String[]{
                            FOOD_COLUMN_FOOD_NAME,
                            FOOD_COLUMN_STOCK_FLUCTUATION,
                            FOOD_COLUMN_STOCK,
                            FOOD_COLUMN_UNIT_NAME,
                            FOOD_COLUMN_CLASSIFICATION},
                    FOOD_COLUMN_ID + " = ?",
                    new String[]{array_use_food_id[i]},
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();

            array_use_food_name[i] = cursor.getString(0);
            array_food_fluctuation[i] = cursor.getInt(1);
            array_use_food_stock[i] = cursor.getInt(2);
            array_use_food_unit[i] = cursor.getString(3);
            array_use_food_classification[i] = cursor.getString(4);
        }

        cursor.close();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }


    // 消費する食材
    private void readConsum(){
        SampleListItem updateSitem;
        ArrayList<SampleListItem> updateConsumItems = new ArrayList<>();

        // リストビューごとにボタンを配置するためにitemに代入している
        for (int i = 0; !array_use_food_id[i].equals(""); i++) {
            if(!(array_use_food_classification[i].equals("def"))) {
                //画像、増減ボタンのインスタンスを作成
                updateSitem = new SampleListItem(null, array_use_food_name[i], array_use_food_consume[i] + array_use_food_unit[i], null, null, null);
                updateConsumItems.add(updateSitem);
            }
        }

        // ボタンイベント時の座標を保持
        int tposition = 0;
        int vposition = consumlist.getFirstVisiblePosition();
        try {
            if (consumlist.getCount() > 0) {
                tposition = consumlist.getChildAt(0).getTop();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        // 出力結果をリストビューに表示
        consumAdapter = new listViewAdapter(getContext(), R.layout.consumlist_item, updateConsumItems, stock);
        consumlist.setAdapter(consumAdapter);
        consumlist.setSelectionFromTop(vposition, tposition);
        consumAdapter.notifyDataSetChanged();
    }


    // 別途必要な食材
    private void readOther(){
        OtherListItem updateOitem;
        ArrayList<OtherListItem> updateOtherItems;
        // リストビューに表示する要素を設定
        updateOtherItems = new ArrayList<>();

        // リストビューごとにボタンを配置するためにitemに代入している
        for (int i = 0;  !array_use_food_id[i].equals(""); i++) {
            if((array_use_food_classification[i].equals("def"))) {
                updateOitem = new OtherListItem(array_use_food_name[i], array_use_food_consume[i] + array_use_food_unit[i]);
                updateOtherItems.add(updateOitem);
            }
        }

        // ボタンイベント時の座標を保持
        int tposition = 0;
        int vposition = otherlist.getFirstVisiblePosition();
        try {
            if (otherlist.getCount() > 0) {
                tposition = otherlist.getChildAt(0).getTop();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        // 出力結果をリストビューに表示
        otherAdapter = new otherlistAdapter(getContext(), R.layout.otherlist_item, updateOtherItems);
        otherlist.setAdapter(otherAdapter);
        otherlist.setSelectionFromTop(vposition,tposition);
        otherAdapter.notifyDataSetChanged();
    }

    // データベースのアップデート
    // 食材のストックの変更
    private void updateData(SQLiteDatabase db, String id, int stock){

        String[] arrayId = {id};

        try {
            ContentValues values = new ContentValues();
            values.put(FOOD_COLUMN_STOCK, stock);
            db.update(TABLE_NAME_FOOD, values,FOOD_COLUMN_ID+ " = ?", arrayId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // レシピIDの変更
    private void updateData(SQLiteDatabase db, String id, String recipeId){

        String[] arrayId = {id};

        try{
            ContentValues values = new ContentValues();
            values.put(RECIPE_COLUMN_FOOD_ID, recipeId);
            db.update(TABLE_NAME_RECIPE, values, RECIPE_COLUMN_ID + " = ?", arrayId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
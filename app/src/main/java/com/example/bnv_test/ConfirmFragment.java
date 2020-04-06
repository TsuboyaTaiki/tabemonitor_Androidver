package com.example.bnv_test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_ID;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_STOCK;
import static com.example.bnv_test.MyDatabaseOpenHelper.TABLE_NAME_FOOD;


public class ConfirmFragment extends Fragment {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + ConfirmFragment.class.getSimpleName();

    private int cursorCnt = 0;
    private int confirmCnt = 0;
    private String recognString;
    private ListView listView;
    private detectfoodsAdapter adapter;
    private ArrayList<SampleListItem> listItems;
    private SampleListItem item;
    private ImageButton upbtn;
    private ImageButton downbtn;
    private ImageButton delete;
    private Button confirmCansellButton;
    private Button confirmAddButton;
    private Cursor cursor;
    private MyDatabaseOpenHelper helper;
    private SQLiteDatabase db ;

    private String[] array_food_id;
    private String[] array_food_name;
    private String[] array_food_recogn_str;
    private String[] array_food_classification;
    private String[] array_food_details;
    private String[] array_recogn_list;

    private String[] confirm_id_list;
    private String[] confirm_name_list;
    private String[] confirm_food_unit_name;
    private int[] confirm_food_stock;
    private int[] confirm_food_fluctuation;
    private int[] confirm_food_add;

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
        array_food_recogn_str = new String[number];
        array_food_classification = new String[number];
        array_food_details = new String[number];
        //array_recogn_list = new String[number];
        confirm_id_list = new String[number];
        confirm_name_list = new String[number];
        confirm_food_unit_name = new String[number];
        confirm_food_stock = new int[number];
        confirm_food_fluctuation = new int[number];
        confirm_food_add = new int[number];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d("FragmentClass", "ConfirmFragment.java");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirm, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recognString = getArguments().getString("recognString");

        readData();

        cursorCnt = 0;
        while (array_food_id[cursorCnt] != null){
            array_recogn_list = array_food_recogn_str[cursorCnt].split("_");
            searchString(array_recogn_list);
            cursorCnt++;
        }

        listView = (ListView) view.findViewById(R.id.detect_foods);

        // リストビューに表示する要素を設定
        listItems = new ArrayList<>();

        // リストビューごとにボタンを配置するためにitemに代入している
        for (int i = 0; confirm_id_list[i] != null ; i++) {
            //画像、増減ボタンのインスタンスを作成
            upbtn = (ImageButton) view.findViewById(R.id.confirm_upward);
            downbtn = (ImageButton) view.findViewById(R.id.confirm_downward);
            delete = (ImageButton) view.findViewById(R.id.delete);

            item = new SampleListItem(null,confirm_name_list[i],String.valueOf(confirm_food_add[i]) + confirm_food_unit_name[i], upbtn, downbtn, delete);
            listItems.add(item);
        }

        // 出力結果をリストビューに表示
        adapter = new detectfoodsAdapter(this.getContext(), R.layout.detectlist_item, listItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (view.getId()){
                    case R.id.confirm_upward:

                        /* 追加する量の増加処理 */

                        confirm_food_add[position] += confirm_food_fluctuation[position];
                        updateList();

                        break;
                    case R.id.confirm_downward:

                        /* 追加する量の減少処理 */

                        if(confirm_food_add[position] - confirm_food_fluctuation[position] >= 0){
                            confirm_food_add[position] -= confirm_food_fluctuation[position];
                        }
                        updateList();

                        break;
                }
            }
        });

        // 「もう一度」ボタンイベント
        confirmCansellButton = (Button) view.findViewById(R.id.confirm_cancel_button);
        confirmCansellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        // 「追加」ボタンイベント
        confirmAddButton = (Button) view.findViewById(R.id.confirm_add_button);
        confirmAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; confirm_id_list[i] != null; i++){
                    confirm_food_add[i] += confirm_food_stock[i];

                    for(int j = 0; j < i; j++){
                        if(confirm_id_list[i].equals(confirm_id_list[j])){
                            confirm_food_add[i] += confirm_food_add[j];
                        }
                    }

                    updateData(db,confirm_id_list[i],confirm_food_add[i]);
                }

                // 管理画面に遷移
                BottomNavigationView navigationView = (BottomNavigationView)getActivity().findViewById(R.id.navigation);
                navigationView.setSelectedItemId(R.id.navigation_home);

                // トーストに追加した食材名と個数を表示
                StringBuilder toastText = new StringBuilder();
                for(int i = 0; confirm_name_list[i] != null; i++){
                    toastText.append(confirm_name_list[i]);
                    toastText.append(" が ");
                    toastText.append(confirm_food_add[i]);
                    toastText.append(confirm_food_unit_name[i]);
                    toastText.append("\n");
                }
                toastText.append("冷蔵庫に追加されました。");
                Toast.makeText(getContext(), toastText.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }


    /// タイトルバー
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().setTitle(" レシート確認");

    }


    /// DBからのデータ読み込み
    private void readData(){
        if(helper == null){
            helper = new MyDatabaseOpenHelper(getContext());
        }
        if(db == null){
            db = helper.getWritableDatabase();
        }
        cursor = db.query(
                TABLE_NAME_FOOD,
                new String[]{"id","food_name","recogn_str","classification"},
                FOOD_COLUMN_CLASSIFICATION + " != ?",
                new String[]{"def"},
                null,
                null,
                null
        );

        cursor.moveToFirst();

        // ※ リストビュー用の配列に食材テーブルの食材名を代入 ※
        for (int i = 0; i < cursor.getCount(); i++ ){
            array_food_id[i] = cursor.getString(0);
            array_food_name[i] = cursor.getString(1);
            array_food_recogn_str[i] = cursor.getString(2);
            array_food_classification[i] = cursor.getString(3);
            cursor.moveToNext();
        }
        cursor.close();
    }


    /// 認識した文字とDBの食材名を比較する
    private void searchString(String[] arrayString){
        for (int i = 0; i < arrayString.length; i++){
            if(recognString.contains(arrayString[i])){
                readFoodId();

                // 玉ねぎねぎ問題の解決
                recognString = recognString.replace(arrayString[i], "");

                Log.d("確認ログ", "レシートから検知しました : " + arrayString[i]);
                Log.d("確認ログ", "recoginString = " + recognString);
            }
        }
    }

    /// DBからのデータ読み込み
    private void readFoodId(){
        if(helper == null){
            helper = new MyDatabaseOpenHelper(getContext());
        }
        if(db == null){
            db = helper.getWritableDatabase();
        }
        cursor = db.query(
                TABLE_NAME_FOOD,
                new String[]{"id","food_name","stock","unit_name","fluctuation"},
                FOOD_COLUMN_ID + " = ?",
                new String[]{array_food_id[cursorCnt]},
                null,
                null,
                null
        );

        cursor.moveToFirst();

        // ※ リストビュー用の配列に食材テーブルの食材名を代入 ※
        confirm_id_list[confirmCnt] = cursor.getString(0);
        confirm_name_list[confirmCnt] = cursor.getString(1);
        confirm_food_stock[confirmCnt] = cursor.getInt(2);
        confirm_food_unit_name[confirmCnt] = cursor.getString(3);
        confirm_food_fluctuation[confirmCnt] = cursor.getInt(4);
        confirm_food_add[confirmCnt] = cursor.getInt(4);

        confirmCnt++;
        cursor.moveToNext();
        cursor.close();
    }


    // データベースのアップデート
    private void updateData(SQLiteDatabase db, String id, int stock){
        // 配列の形にしないと反映されない
        String[] str = {id};

        try {
            ContentValues values = new ContentValues();

            // IDの値をPUTするとIDまで変わってしまう
            //values.put(helper.FOOD_COLUMN_ID, id);
            values.put(FOOD_COLUMN_STOCK, stock);

            db.update(TABLE_NAME_FOOD, values, "id = ?",str);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private void updateList() {
        SampleListItem updateitem;
        ArrayList<SampleListItem> updateItems = new ArrayList<>();

        // リストビューごとにボタンを配置するためにitemに代入している
        for (int i = 0; confirm_id_list[i] != null; i++) {
            updateitem = new SampleListItem(
                    null,
                    confirm_name_list[i],
                    confirm_food_add[i] + confirm_food_unit_name[i],
                    upbtn, downbtn, delete);
            updateItems.add(updateitem);
        }

        // 出力結果をリストビューに表示
        adapter = new detectfoodsAdapter(this.getContext(), R.layout.detectlist_item, updateItems);
        listView.setAdapter(adapter);
    }
}





















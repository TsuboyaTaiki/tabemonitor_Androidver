package com.example.bnv_test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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


public class AddFragment extends Fragment {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + AddFragment.class.getSimpleName();

    private static final String RADIO_STRING_MEAT = "肉";
    private static final String RADIO_STRING_SEAFOOD = "魚介";
    private static final String RADIO_STRING_VEGETABLE = "野菜";
    private static final String RADIO_STRING_OTHER = "その他";

    private String radioflagStr = RADIO_STRING_MEAT;
    private MyDatabaseOpenHelper helper;
    private SQLiteDatabase db;
    private PopupWindow mPopupWindow;
    private Cursor cursor;
    private GridView gridView;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //fragment_dashboardをインフレートし、viewに返す
        //infrate : fragment_dashboardに指定したxmlファイルをviewとして割り当てるという役割
        return inflater.inflate(R.layout.fragment_add, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridView = getActivity().findViewById(R.id.gridview_food);

        readData();

        //管理画面に戻るフローティングアクションボタンのタップイベント
        FloatingActionButton fab = (FloatingActionButton)getActivity().findViewById(R.id.return_fab);
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

        setGridView();

        //ラジオグループのインスタンスと「すべて」ボタンのインスタンスを作成し、「Meat」をアクティブにする
        RadioGroup radioGroup = (RadioGroup) getActivity().findViewById(R.id.RadioGroupB);
        radioGroup.check(R.id.Meat);

        //ラジオボタンをタップした際のイベント処理
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            public void onCheckedChanged(RadioGroup group,int checkedId){
                //押されたラジオボタンをIDで判別し、インスタンスを作成
                RadioButton radioButton = (RadioButton) getActivity().findViewById(checkedId);

                //ヘッダ用テキストビューのインスタンスを作成
                TextView headertext = (TextView) getActivity().findViewById(R.id.Rheader);

                switch(checkedId){
                    //ラジオボタン「肉」が押された際の処理
                    case R.id.Meat:
                        radioflagStr = RADIO_STRING_MEAT;
                        setGridView();
                        break;

                    //ラジオボタン「魚」が押された際の処理
                    case R.id.Fish:
                        radioflagStr = RADIO_STRING_SEAFOOD;
                        setGridView();
                        break;

                    //ラジオボタン「野菜」が押された際の処理
                    case R.id.Vegetable:
                        radioflagStr = RADIO_STRING_VEGETABLE;
                        setGridView();
                        break;

                    //ラジオボタン「その他」が押された際の処理
                    case R.id.Other:
                        radioflagStr = RADIO_STRING_OTHER;
                        setGridView();
                        break;
                }

                // ラジオボタンイベント後の再描画
                readData();
            }
        });

        //グリッドビューをタップした際のイベント処理
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

                //選択された食材名のトーストを表示
                String message = array_food_name [position] + "が選択されました。";

                //テキストビューにタップした食材名と単位を表示
                ((TextView) mPopupWindow.getContentView().findViewById(R.id.addpop_text)).setText(array_food_name[position]);
                ((TextView) mPopupWindow.getContentView().findViewById(R.id.addpop_unit)).setText(array_food_unit_name[position]);

                //「ok_button」のタップイベント処理
                popupView.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //エディットテキストのインスタンスを作成(インフレートした「popupView」からEditTextのIDを探している)
                        EditText add_editText = popupView.findViewById(R.id.addpop_edittext);

                        //未入力の際にNullPointerExceptionが発生するのを回避するための処理
                        try {
                            // EditTextが空でなければ
                            if(!(add_editText.getText().toString().equals(""))){

                                //エディットテキストに入力された数値をint型に変換しデータベースをアップデート
                                int add_value = Integer.parseInt(add_editText.getText().toString());
                                updateData(db,array_food_id[position],array_food_stock[position] + add_value);
                                readData();

                                // トーストに追加した食材名と個数を表示
                                String toastText =
                                        array_food_name [position] + " が " +
                                                add_value + array_food_unit_name[position] + "\n" +
                                                "冷蔵庫に追加されました。";
                                Toast.makeText(getContext(), toastText, Toast.LENGTH_SHORT).show();
                            }
                            else if(add_editText.getText().toString().equals("")){
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
                mPopupWindow.showAtLocation(getActivity().findViewById(R.id.gridview_food), Gravity.CENTER, 0, 0);
            }
        });
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

    private void readData(){

        cursor = db.query(
                TABLE_NAME_FOOD,
                new String[]{
                        FOOD_COLUMN_ID, FOOD_COLUMN_FOOD_NAME,
                        FOOD_COLUMN_STOCK, FOOD_COLUMN_UNIT_NAME,
                        FOOD_COLUMN_STOCK_FLUCTUATION, FOOD_COLUMN_FOOD_NAME_HIRAGANA
                },
                FOOD_COLUMN_CLASSIFICATION + " = ?",
                new String[]{radioflagStr},
                null,
                null,
                FOOD_COLUMN_FOOD_NAME_HIRAGANA + " ASC"
        );

        cursor.moveToFirst();

        // ※ リストビュー用の配列に食材テーブルの食材名を代入 ※
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

    //トースト作成用メソッド
    private void toastMake(String message,int x,int y){
        Toast toast = Toast.makeText(this.getContext(),message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,x,y);
        toast.show();
    }

    // グリッドビューのセット
    private void setGridView(){

        List<Integer> imgList = new ArrayList<>();
        GridAdapter gridAdapter;

        readData();

        //配列「others」の要素を「imgList」に追加
        for (int i = 0; i < cursor.getCount(); i++ ){
            int imageId = getResources().getIdentifier(array_food_name[i],"drawable",getActivity().getPackageName());
            imgList.add(imageId);
        }

        //グリッドビューアダプターのインスタンスを作成
        gridAdapter = new GridAdapter(getContext(), R.layout.gridview_food_items, imgList, array_food_name);

        //グリッドビューにアダプターのデータをセット
        gridView.setAdapter(gridAdapter);

        //アダプターにデータの変更を通知
        gridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().setTitle("  手動で追加");
    }
}
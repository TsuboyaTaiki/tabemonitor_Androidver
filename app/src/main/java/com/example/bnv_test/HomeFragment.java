package com.example.bnv_test;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_CLASSIFICATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_FOOD_NAME;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_ID;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_STOCK;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_STOCK_FLUCTUATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_UNIT_NAME;
import static com.example.bnv_test.MyDatabaseOpenHelper.TABLE_NAME_FOOD;

public class HomeFragment extends Fragment {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + HomeFragment.class.getSimpleName();

    private String radioFlagStr = "すべて";
    private String toastMessage;
    private Animation outAnimation;
    private SampleListItem item;
    private ArrayList<SampleListItem> listItems;
    private ListView listView;
    private listViewAdapter adapter;
    private ImageButton upbtn;
    private ImageButton downbtn;
    private ImageButton delete;
    private TextView notFoodInMessage;
    private Bundle radiostate = new Bundle();
    private PopupWindow mPopupWindow;
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

        // array系変数の初期化
        helper = new MyDatabaseOpenHelper(getContext());
        db = helper.getWritableDatabase();
        int number = helper.getFoodsAmount(db);
        array_food_id           = new String[number];
        array_food_name         = new String[number];
        array_food_stock        = new int[number];
        array_food_unit_name    = new String[number];
        array_food_fluctuation  = new int[number];
    }

    //フラグメントが初めて UI を描画する際に呼び出されるメソッド
    //フラグメントの UI を描画するには、このメソッドからフラグメントのレイアウトのルートとなっている View を返す必要がある
    //フラグメントが UI を提示しない場合は、null を返すことができる
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        Log.d("FragmentClass", "HomeFragment.java");

        //fragment_homeをインフレートし、viewに返す
        //infrate : fragment_homeに指定したxmlファイルをviewとして割り当てるという役割
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        return view;
    }

    //Viewの生成後に呼び出されるメソッド
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        readData();

        FloatingActionButton fab = (FloatingActionButton)getActivity().findViewById(R.id.add_fab);
        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //トランザクションを実行するためのマネージャーのインスタンスを作成
                FragmentManager fragmentManager = getFragmentManager();

                //ConsumptionFragmentのインスタンスを作成
                AddFragment fragment = new AddFragment();

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


        // レイアウトからリストビューを取得
        listView = (ListView) view.findViewById(R.id.list_view);

        //リストビューにヘッダを追加
        final View header = (View)getLayoutInflater().inflate(R.layout.listview_header,null);
        listView.addHeaderView(header,null,false);
        TextView headertext = (TextView) getActivity().findViewById(R.id.Hheader);
        headertext.setText("  全て");

        //リストビューの最後に余白を追加する
        TextView empty = new TextView(getContext());
        empty.setHeight(280);
        listView.addFooterView(empty,null,false);

        // リストビューに表示する要素を設定
        listItems = new ArrayList<>();

        RadioGroup radioGroup = (RadioGroup) getActivity().findViewById(R.id.RadioGroup_home);
        RadioButton All_home = (RadioButton) getActivity().findViewById(R.id.All_home);

        //初期選択を「すべて」にする
        radioGroup.check(R.id.All_home);
        radioFlagStr = "すべて";
        radiostate.putString("radiostate",radioFlagStr);

        //ラジオボタンのリスナー
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //押されたラジオボタンをIDで判別し、インスタンスを作成
                RadioButton radioButton = (RadioButton) getActivity().findViewById(checkedId);

                //ヘッダ用テキストビューのインスタンスを作成
                TextView headertext = (TextView) getActivity().findViewById(R.id.Hheader);

                listItems = new ArrayList<>();
                adapter = new listViewAdapter(getContext(),R.layout.listview_item, listItems);

                switch(checkedId){
                    //ラジオボタン「すべて」が押された際の処理
                    case R.id.All_home:
                        radioFlagStr = "すべて";
                        radiostate.putString("radiostate",radioFlagStr);

                        //ヘッダのテキストをセット
                        headertext.setText("  全て");

                        updateList();

                        break;

                    //ラジオボタン「冷蔵室」が押された際の処理
                    case R.id.Refrigerator_home:
                        radioFlagStr = "冷蔵室";
                        radiostate.putString("radiostate",radioFlagStr);

                        //ヘッダのテキストをセット
                        headertext.setText("  冷蔵室");

                        updateList();

                        break;

                    //ラジオボタン「野菜室」が押された際の処理
                    case R.id.VegetableCompartment_home:
                        radioFlagStr = "野菜室";
                        radiostate.putString("radiostate",radioFlagStr);

                        //ヘッダのテキストをセット
                        headertext.setText("  野菜室");

                        updateList();

                        break;
                }

                // 出力結果をリストビューに表示
                listView.setAdapter(adapter);
            }
        });

        // リストビューごとにボタンを配置するためにitemに代入している
        for (int i = 0; i < cursor.getCount(); i++) {
            //画像、増減ボタンのインスタンスを作成
            //Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_check);
            upbtn = (ImageButton) view.findViewById(R.id.upward);
            downbtn = (ImageButton) view.findViewById(R.id.downward);

            item = new SampleListItem(null, array_food_name[i],array_food_stock[i] + array_food_unit_name[i], upbtn, downbtn, delete);
            listItems.add(item);
        }

        // 冷蔵庫に何もない場合に表示
        notFoodInMessage = view.findViewById(R.id.notFoodInMessage);
        notFoodInMessage.setVisibility(View.INVISIBLE);
        if(listItems == null || listItems.size() == 0){
            notFoodInMessage.setVisibility(View.VISIBLE);
            notFoodInMessage.setText("何も入っていません");
        }

        // 出力結果をリストビューに表示
        adapter = new listViewAdapter(this.getContext(), R.layout.listview_item, listItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (view.getId()) {
                    case R.id.upward:
                        array_food_stock[position] = array_food_stock[position] + array_food_fluctuation[position];  // 増減量分だけプラスする

                        try{
                            updateData(db, array_food_id[position], array_food_stock[position]);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        updateList();
                        break;

                    case R.id.downward:
                        if(array_food_stock[position] - array_food_fluctuation[position] >= 0) {                            // 値がマイナスになる場合無効にする
                            array_food_stock[position] =
                                    array_food_stock[position] - array_food_fluctuation[position];     // 増減量分だけマイナスする
                        }else if(array_food_stock[position] - array_food_fluctuation[position] < 0){
                            array_food_stock[position] = 0;
                        }

                        if(array_food_stock[position] > 0) {

                        }
                        else{
                            Toast.makeText(
                                    getContext(),
                                    array_food_name[position] + "が削除されました",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        try{
                            updateData(db, array_food_id[position], array_food_stock[position]);
                        }catch (Exception e){
                            e.printStackTrace();
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

                                        Toast.makeText(getContext(),
                                                array_food_name[position]
                                                        + "が削除されました",
                                                Toast.LENGTH_SHORT).show();

                                        try{
                                            updateData(
                                                    db,
                                                    array_food_id[position],
                                                    array_food_stock[position]
                                            );
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                        updateList();
                                    }
                                })
                                .setNegativeButton("キャンセル", null)
                                .show();

                        break;

                    case R.id.amount:
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

                        //エディットテキストのインスタンスを作成(インフレートした「popupView」からEditTextのIDを探している)
                        EditText add_editText = popupView.findViewById(R.id.addpop_edittext);
                        add_editText.setText(String.valueOf(array_food_stock[position]));

                        //「ok_button」のタップイベント処理
                        popupView.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //未入力の際にNullPointerExceptionが発生するのを回避するための処理
                                try {
                                    // EditTextが空でなければ
                                    if (!(add_editText.getText().toString().equals(""))) {

                                        //エディットテキストに入力された数値をint型に変換しデータベースをアップデート
                                        int add_value = Integer.parseInt(add_editText.getText().toString());

                                        if(add_value != array_food_stock[position]) {
                                            // トーストに追加した食材名と個数を表示
                                            String toastText =
                                                    array_food_name[position] + "を" +
                                                    (add_value > 0 ?
                                                            add_value + array_food_unit_name[position] + "\nに変更しました。" :
                                                            "削除しました。"
                                                    );
                                            Toast.makeText(getContext(), toastText, Toast.LENGTH_SHORT).show();

                                            updateData(db, array_food_id[position], add_value);
                                            readData();
                                            updateList();
                                        }
                                    } else if (add_editText.getText().toString().equals("")) {
                                        //例外処理に移動
                                        throw new NullPointerException();
                                    }
                                }
                                //例外が発生した際の処理
                                catch (NullPointerException e) {
                                    e.printStackTrace();
                                }

                                //ポップアップウィンドウが表示されているなら削除する
                                if (mPopupWindow.isShowing()) {
                                    mPopupWindow.dismiss();
                                }
                            }
                        });
                        // 画面中央にポップアップを表示
                        mPopupWindow.showAtLocation(getActivity().findViewById(R.id.list_view), Gravity.CENTER, 0, 0);
                        break;
                }
            }
        });
    }

    // Fragmentが表示されて最初のリストビュー表示メソッド
    private void readData(){
        if(helper == null){
            helper = new MyDatabaseOpenHelper(getContext());
        }

        if(db == null){
            db = helper.getWritableDatabase();
        }

        if(radioFlagStr.equals("すべて")) {
            cursor = db.query(
                    TABLE_NAME_FOOD,
                    new String[]{
                            FOOD_COLUMN_ID, FOOD_COLUMN_FOOD_NAME, FOOD_COLUMN_STOCK,
                            FOOD_COLUMN_UNIT_NAME, FOOD_COLUMN_STOCK_FLUCTUATION},
                    FOOD_COLUMN_STOCK + " != ?",
                    new String[]{"0"},
                    null,
                    null,
                    null
            );
        }else if(radioFlagStr.equals("冷蔵室")){
            cursor = db.query(
                    TABLE_NAME_FOOD,
                    new String[]{"id","food_name","stock","unit_name","fluctuation"},
                    FOOD_COLUMN_STOCK + " > ? " +
                            "AND (" + FOOD_COLUMN_CLASSIFICATION + " = ? " +
                            "OR " + FOOD_COLUMN_CLASSIFICATION + " = ? " +
                            "OR " + FOOD_COLUMN_CLASSIFICATION + " = ?)",
                    new String[]{"0","肉","魚介","その他"},
                    null,
                    null,
                    null
            );
        }else{
            cursor = db.query(
                    TABLE_NAME_FOOD,
                    new String[]{"id","food_name","stock","unit_name","fluctuation"},
                    FOOD_COLUMN_STOCK + " != ? " +
                            "AND " + FOOD_COLUMN_CLASSIFICATION + " = ?",
                    new String[]{"0","野菜"},
                    null,
                    null,
                    null
            );
        }

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

    // データアップデート後のリストビュー更新
    private void updateList(){

        ArrayList<SampleListItem> updateListitem;
        updateListitem = new ArrayList<>();

        if(helper == null){
            helper = new MyDatabaseOpenHelper(getContext());
        }

        if(db == null){
            db = helper.getWritableDatabase();
        }

        if(radioFlagStr.equals("すべて")) {
            cursor = db.query(
                    TABLE_NAME_FOOD,
                    new String[]{"id", "food_name", "stock", "unit_name", "fluctuation"},
                    FOOD_COLUMN_STOCK + " != ?",
                    new String[]{"0"},
                    null,
                    null,
                    null
            );
        }else if(radioFlagStr.equals("冷蔵室")){
            cursor = db.query(
                    TABLE_NAME_FOOD,
                    new String[]{"id","food_name","stock","unit_name","fluctuation"},
                    FOOD_COLUMN_STOCK + " != ? " +
                            "AND (" + FOOD_COLUMN_CLASSIFICATION + " = ? " +
                            "OR " + FOOD_COLUMN_CLASSIFICATION + " = ? " +
                            "OR " + FOOD_COLUMN_CLASSIFICATION + " = ?)",
                    new String[]{"0","肉","魚介","その他"},
                    null,
                    null,
                    null
            );
        }else{
            cursor = db.query(
                    TABLE_NAME_FOOD,
                    new String[]{"id","food_name","stock","unit_name","fluctuation"},
                    FOOD_COLUMN_STOCK + " != ? " +
                            "AND " + FOOD_COLUMN_CLASSIFICATION + " = ?",
                    new String[]{"0","野菜"},
                    null,
                    null,
                    null
            );
        }


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

        for (int i = 0; i < cursor.getCount(); i++) {
            item = new SampleListItem(null, array_food_name[i],array_food_stock[i] + array_food_unit_name[i], upbtn, downbtn, delete);
            updateListitem.add(item);
        }

        cursor.close();

        int y = 0;
        int position = listView.getFirstVisiblePosition();
        try {
            if (listView.getChildCount() > 0) {
                y = listView.getChildAt(0).getTop() - listView.getPaddingTop();
            }
            Log.d(LOG_TAG, "position = " + position);
            Log.d(LOG_TAG, "y = " + y + " getChildCount = " + listView.getChildCount());
        }catch (Exception e){
            e.printStackTrace();
        }

        // 出力結果をリストビューに表示
        adapter = new listViewAdapter(this.getContext(), R.layout.listview_item, updateListitem);
        listView.setAdapter(adapter);
        listView.setSelectionFromTop(position, y);

        adapter.notifyDataSetChanged();
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


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().setTitle(" 冷蔵庫");

    }
}

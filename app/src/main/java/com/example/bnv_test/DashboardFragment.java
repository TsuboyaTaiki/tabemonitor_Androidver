package com.example.bnv_test;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_CLASSIFICATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_FOOD_NAME;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_ID;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_RECOGNITION_STR;
import static com.example.bnv_test.MyDatabaseOpenHelper.FOOD_COLUMN_STOCK;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_CLASSIFICATION;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_DETAILS;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_FOOD_ID;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_ID;
import static com.example.bnv_test.MyDatabaseOpenHelper.RECIPE_COLUMN_RECIPE_NAME;
import static com.example.bnv_test.MyDatabaseOpenHelper.TABLE_NAME_FOOD;
import static com.example.bnv_test.MyDatabaseOpenHelper.TABLE_NAME_RECIPE;

public class DashboardFragment extends Fragment {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + DashboardFragment.class.getSimpleName();

    private static final String RADIO_STRING_ALL = "すべて";
    private static final String RADIO_STRING_MEAT = "肉";
    private static final String RADIO_STRING_SEAFOOD = "魚介類";
    private static final String RADIO_STRING_VEGETABLE = "野菜類";
    private static final String RADIO_STRING_RICE = "ご飯";
    private static final String RADIO_STRING_INVENTION = "考案";
    private final DashboardFragment self = this;

    private String radioFlagStr = RADIO_STRING_ALL;
    private String searchWord;
    private String toastMessage;
    private Bundle radiostate = new Bundle();
    private RecipeListItem item;
    private ListView listView;
    private ArrayList<RecipeListItem> listItems;
    private listViewAdapter_Recipe adapter;
    private SearchView searchView;
    private Cursor cursor;
    private MyDatabaseOpenHelper helper;
    private SQLiteDatabase db;

    private String[] array_recipe_id;
    private String[] array_recipe_name;
    private String[] array_recipe_classicafition;
    private String[] array_recipe_details;
    private String[] array_recipe_food_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate");

        // FragmentでMenuを表示する為に必要
        this.setHasOptionsMenu(true);

        // array系変数の初期化
        helper = new MyDatabaseOpenHelper(getContext());
        db = helper.getWritableDatabase();
        int number = helper.getRecipesAmount(db) + 10;
        array_recipe_id             = new String[number];
        array_recipe_name           = new String[number];
        array_recipe_classicafition = new String[number];
        array_recipe_details        = new String[number];
        array_recipe_food_id        = new String[number];
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //fragment_dashboardをインフレートし、viewに返す
        //infrate : fragment_dashboardに指定したxmlファイルをviewとして割り当てるという役割
        return inflater.inflate(R.layout.fragment_dashboard, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton addRecipe = (FloatingActionButton)getActivity().findViewById(R.id.add_recipe);
        addRecipe.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //トランザクションを実行するためのマネージャーのインスタンスを作成
                FragmentManager fragmentManager = getFragmentManager();

                //ConsumptionFragmentのインスタンスを作成
                AddRecipeFragment fragment = new AddRecipeFragment();

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

        //リストビューのインスタンスを作成
        listView = (ListView) view.findViewById(R.id.recipe_list);

        //リストビューにヘッダを追加
        final View header = (View)getLayoutInflater().inflate(R.layout.recipelistview_header,null);
        listView.addHeaderView(header,null,false);
        TextView headertext = (TextView) getActivity().findViewById(R.id.Rheader);
        headertext.setText(" [ 全て ]");

        //リストビューの最後に余白を追加する
        TextView empty = new TextView(getContext());
        empty.setHeight(280);
        listView.addFooterView(empty,null,false);

        readData();

        //ラジオグループのインスタンスと「すべて」ボタンのインスタンスを作成
        RadioGroup radioGroup = (RadioGroup) getActivity().findViewById(R.id.RadioGroupB);
        RadioButton radioButtonA = (RadioButton)getActivity().findViewById(R.id.All);

        // 初期選択を「すべて」にする
        radioGroup.check(R.id.All);
        radioFlagStr = RADIO_STRING_ALL;
        radiostate.putString("radiostate", radioFlagStr);

        //ラジオボタンのリスナー
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            public void onCheckedChanged(RadioGroup group,int checkedId){
                //押されたラジオボタンをIDで判別し、インスタンスを作成
                RadioButton radioButton = (RadioButton) getActivity().findViewById(checkedId);

                //ヘッダ用テキストビューのインスタンスを作成
                TextView headertext = (TextView) getActivity().findViewById(R.id.Rheader);


                listItems = new ArrayList<>();
                adapter = new listViewAdapter_Recipe(getContext(), R.layout.recipelistview_item, listItems);

                switch(checkedId){
                    //ラジオボタン「すべて」が押された際の処理
                    case R.id.All:
                        radioFlagStr = RADIO_STRING_ALL;
                        radiostate.putString("radiostate", radioFlagStr);

                        //ヘッダのテキストをセット
                        headertext.setText(" [ 全て ]");

                        updateList();

                        break;

                    //ラジオボタン「肉」が押された際の処理
                    case R.id.Meat:
                        radioFlagStr = RADIO_STRING_MEAT;
                        radiostate.putString("radiostate", radioFlagStr);

                        //ヘッダのテキストをセット
                        headertext.setText(" [ 肉料理 ]");

                        updateList();

                        break;

                    //ラジオボタン「魚」が押された際の処理
                    case R.id.Fish:
                        radioFlagStr = RADIO_STRING_SEAFOOD;
                        radiostate.putString("radiostate",radioFlagStr);

                        //ヘッダのテキストをセット
                        headertext.setText(" [ 魚料理 ]");

                        updateList();

                        break;

                    //ラジオボタン「野菜」が押された際の処理
                    case R.id.Vegetable:
                        radioFlagStr = RADIO_STRING_VEGETABLE;
                        radiostate.putString("radiostate",radioFlagStr);

                        //ヘッダのテキストをセット
                        headertext.setText(" [ 野菜料理 ]");

                        updateList();

                        break;

                    //ラジオボタン「その他」が押された際の処理
                    case R.id.Other:
                        radioFlagStr = RADIO_STRING_RICE;
                        radiostate.putString("radiostate",radioFlagStr);

                        //ヘッダのテキストをセット
                        headertext.setText(" [ ごはん ]");

                        updateList();

                        break;

                    case R.id.Suggest:
                        radioFlagStr = RADIO_STRING_INVENTION;
                        radiostate.putString("radiostate",radioFlagStr);

                        //ヘッダのテキストをセット
                        headertext.setText(" [ 考案 ]");

                        updateList();

                        break;
                }

                // 出力結果をリストビューに表示
                listView.setAdapter(adapter);
            }
        });


        //リストビューのアイテムにタップした際の処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView <?> parent,View v,int position, long id){
                //ポジションをConsumptionFragmentに渡すバンドルのインスタンスを作成
                Bundle Pbundle = new Bundle();

                // 値の設置
                Pbundle.putString("recipeId",array_recipe_id[position -1]);
                Pbundle.putString("recipeName",array_recipe_name[position -1]);

                //position 0 はヘッダに割り当てられているため何も処理しない
                if(position == 0){
                    return;
                }

                //トランザクションを実行するためのマネージャーのインスタンスを作成
                FragmentManager fragmentManager = getFragmentManager();

                //ConsumptionFragmentのインスタンスを作成
                ConsumptionFragment fragment = new ConsumptionFragment();

                if(fragmentManager != null){
                    //引数に「Pbundle」を設定
                    fragment.setArguments(Pbundle);

                    //トランザクションのインスタンスを作成
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

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
    }

    private void readData(){

        listItems = new ArrayList<>();

        // SQLiteで全てのレシピを抽出する
        if (radioFlagStr.equals(RADIO_STRING_ALL)) {
            cursor = db.query(
                    TABLE_NAME_RECIPE,
                    new String[]{
                            RECIPE_COLUMN_ID,
                            RECIPE_COLUMN_RECIPE_NAME,
                            RECIPE_COLUMN_CLASSIFICATION,
                            RECIPE_COLUMN_DETAILS,
                            RECIPE_COLUMN_FOOD_ID},
                    null,
                    null,
                    null,
                    null,
                    null
            );

        // SQLiteで分類別にレシピを抽出する
        }else {
            cursor = db.query(
                    TABLE_NAME_RECIPE,
                    new String[]{
                            RECIPE_COLUMN_ID,
                            RECIPE_COLUMN_RECIPE_NAME,
                            RECIPE_COLUMN_CLASSIFICATION,
                            RECIPE_COLUMN_DETAILS,
                            RECIPE_COLUMN_FOOD_ID},
                    RECIPE_COLUMN_CLASSIFICATION + " = ?",
                    new String[]{radioFlagStr},
                    null,
                    null,
                    null
            );
        }
        cursor.moveToFirst();

        // SQLiteで抽出したデータを変数に格納する
        for (int i = 0; i < cursor.getCount(); i++ ){
            array_recipe_id[i] = cursor.getString(0);
            array_recipe_name[i] = cursor.getString(1);
            array_recipe_classicafition[i] = cursor.getString(2);
            array_recipe_details[i] = cursor.getString(3);
            array_recipe_food_id[i] = cursor.getString(4);
            cursor.moveToNext();
        }

        cursor.close();

        // リストにデータを格納する
        for (int i = 0; cursor.getCount() > i ; i++) {
            item = new RecipeListItem(array_recipe_name[i]);
            listItems.add(item);
        }

        adapter = new listViewAdapter_Recipe(getContext(), R.layout.recipelistview_item, listItems);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    // リストのアップデート
    private void updateList(){

        ArrayList<RecipeListItem> updateListitem;
        updateListitem = new ArrayList<>();

        // データの抽出
        // すべてか考案の場合は全てのレシピを抽出する
        if (radioFlagStr.equals(RADIO_STRING_ALL) || radioFlagStr.equals(RADIO_STRING_INVENTION)) {
            cursor = db.query(
                    TABLE_NAME_RECIPE,
                    new String[]{
                            RECIPE_COLUMN_ID,
                            RECIPE_COLUMN_RECIPE_NAME,
                            RECIPE_COLUMN_CLASSIFICATION,
                            RECIPE_COLUMN_DETAILS,
                            RECIPE_COLUMN_FOOD_ID},
                    null, null, null, null, null
            );

        // すべて、考案以外の場合は分類別にレシピを抽出する
        }else {
            cursor = db.query(
                    TABLE_NAME_RECIPE,
                    new String[]{
                            RECIPE_COLUMN_ID,
                            RECIPE_COLUMN_RECIPE_NAME,
                            RECIPE_COLUMN_CLASSIFICATION,
                            RECIPE_COLUMN_DETAILS,
                            RECIPE_COLUMN_FOOD_ID},
                    RECIPE_COLUMN_CLASSIFICATION + " = ?",
                    new String[]{radioFlagStr}, null, null, null
            );
        }
        cursor.moveToFirst();

        // 抽出したデータを変数に格納する
        for (int i = 0; i < cursor.getCount(); i++ ){
            array_recipe_id[i] = cursor.getString(0);
            array_recipe_name[i] = cursor.getString(1);
            array_recipe_classicafition[i] = cursor.getString(2);
            array_recipe_details[i] = cursor.getString(3);
            array_recipe_food_id[i] = cursor.getString(4);
            cursor.moveToNext();
        }
        cursor.close();

        // 考案の場合はgetInventionCountDisplayメソッドを実行
        int countDisplay;
        if(radioFlagStr.equals(RADIO_STRING_INVENTION)){
            countDisplay = getInventionCountDisplay(cursor.getCount());
        }else{
            countDisplay = cursor.getCount();
        }

        // リストにデータを格納する
        for (int i = 0; i < countDisplay; i++) {
            item = new RecipeListItem(array_recipe_name[i]);
            updateListitem.add(item);
        }
        adapter = new listViewAdapter_Recipe(getContext(), R.layout.recipelistview_item, updateListitem);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void toastMake(String message,int x,int y){
        Toast toast = Toast.makeText(this.getContext(),message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,x,y);
        toast.show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().setTitle("  レシピ");

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Menuの設定
        inflater.inflate(R.menu.option_menu_first, menu);

        // app:actionViewClass="android.support.v7.widget.SearchView"のItemの取得
        MenuItem menuItem = menu.findItem(R.id.search_menu_search_view);

        this.searchView = (SearchView)menuItem.getActionView();

        this.searchView.setQueryHint("...レシピを検索");

        // 虫眼鏡アイコンを最初表示するかの設定
        this.searchView.setIconifiedByDefault(true);

        // Submitボタンを表示するかどうか
        this.searchView.setSubmitButtonEnabled(true);

        try {
            if (this.searchWord != null) {

                // TextView.setTextみたいなもの
                this.searchView.setQuery(this.searchWord, false);
            } else {
                String queryHint = self.getResources().getString(R.string.search_menu_search_text);
                // placeholderみたいなもの
                this.searchView.setQueryHint(queryHint);
            }
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        this.searchView.setOnQueryTextListener(self.onQueryTextListener);
    }

    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {

        // SubmitボタンorEnterKeyを押されたら呼び出されるメソッド
        @Override
        public boolean onQueryTextSubmit(String searchWord) {

            RecipeListItem updateSearchItem;
            ArrayList<RecipeListItem> updateSearchListitems;

            updateSearchListitems = new ArrayList<>();

            //「radioFlagStr」の状態を取得し、トーストを表示
            getRadioButtonState(radiostate);

            // 変数宣言
            boolean foodIdGetFlag = false;
            boolean recipeIdGetFlag = false;
            Cursor[] recipeIdNameGetCursor = new Cursor[2];

            // 検索したレシピのidを取得
            // すべてか考案の場合は全てのレシピを抽出する
            if (radioFlagStr.equals(RADIO_STRING_ALL) || radioFlagStr.equals(RADIO_STRING_INVENTION)) {
                recipeIdNameGetCursor[0] = db.rawQuery(
                        "SELECT " +
                                RECIPE_COLUMN_ID + "," +
                                RECIPE_COLUMN_RECIPE_NAME + "," +
                                RECIPE_COLUMN_CLASSIFICATION + "," +
                                RECIPE_COLUMN_DETAILS + "," +
                                RECIPE_COLUMN_FOOD_ID + " " +
                                "FROM " + TABLE_NAME_RECIPE + " " +
                                "WHERE " + RECIPE_COLUMN_RECIPE_NAME + " = ?",
                        new String[]{searchWord});

            // すべて、考案以外の場合は分類別にレシピを抽出する
            }else{
                recipeIdNameGetCursor[0] = db.rawQuery(
                        "SELECT " +
                                RECIPE_COLUMN_ID + "," +
                                RECIPE_COLUMN_RECIPE_NAME + "," +
                                RECIPE_COLUMN_CLASSIFICATION + "," +
                                RECIPE_COLUMN_DETAILS + "," +
                                RECIPE_COLUMN_FOOD_ID + " " +
                                "FROM " + TABLE_NAME_RECIPE + " " +
                                "WHERE " + RECIPE_COLUMN_RECIPE_NAME + " = ? " +
                                "AND " + RECIPE_COLUMN_CLASSIFICATION + " = ?",
                        new String[]{searchWord, radioFlagStr});
            }

            // 検索したレシピがヒットしたらtrue
            if(recipeIdNameGetCursor[0].getCount() > 0){
                recipeIdGetFlag = true;
            }

            //------------------------------------------------------------------------------------//

            if(!recipeIdGetFlag) {
                // 検索した食材のidを取得
                Cursor foodIdCursor = db.rawQuery(
                        "SELECT " + FOOD_COLUMN_RECOGNITION_STR + " " +
                                "FROM " + TABLE_NAME_FOOD + " " +
                                "WHERE " + FOOD_COLUMN_RECOGNITION_STR + " LIKE '%' || ? || '%' ESCAPE '$' " +
                                "AND " + FOOD_COLUMN_CLASSIFICATION + " != 'def' ",
                        new String[]{searchWord});
                foodIdCursor.moveToFirst();

                // 検索ワードと読み取り文字の照合
                if (foodIdCursor.getCount() != 0) {
                    // レシピの食材IDを配列の各要素に分割
                    String[] recognString = foodIdCursor.getString(0).split("_");

                    for (int i = 0; i < recognString.length; i++) {
                        if (searchWord.equals(recognString[i])) {
                            foodIdCursor = db.rawQuery(
                                    "SELECT " + FOOD_COLUMN_ID + " " +
                                            "FROM " + TABLE_NAME_FOOD + " " +
                                            "WHERE " + FOOD_COLUMN_RECOGNITION_STR + " LIKE '%' || ? || '%' ESCAPE '$' " +
                                            "AND " + FOOD_COLUMN_CLASSIFICATION + " != 'def' ",
                                    new String[]{searchWord});

                            // 検索した食材がヒットしたらtrue
                            foodIdGetFlag = true;
                        }
                    }

                    if (foodIdGetFlag) {

                        // 検索した食材名を取得
                        foodIdCursor.moveToFirst();
                        String food_id = foodIdCursor.getString(0);
                        foodIdCursor.close();

                        // 検索した食材を含むレシピ名を取得
                        if (radioFlagStr.equals(RADIO_STRING_ALL) || radioFlagStr.equals(RADIO_STRING_INVENTION)) {
                            recipeIdNameGetCursor[1] = db.rawQuery(
                                    "SELECT " +
                                            RECIPE_COLUMN_ID + "," +
                                            RECIPE_COLUMN_RECIPE_NAME + "," +
                                            RECIPE_COLUMN_CLASSIFICATION + "," +
                                            RECIPE_COLUMN_DETAILS + "," +
                                            RECIPE_COLUMN_FOOD_ID + " " +
                                            "FROM " + TABLE_NAME_RECIPE + " " +
                                            "WHERE " + RECIPE_COLUMN_FOOD_ID + " LIKE '%' || ? || '%' ESCAPE '$'",
                                    new String[]{food_id}
                            );
                        } else {
                            recipeIdNameGetCursor[1] = db.rawQuery(
                                    "SELECT " +
                                            RECIPE_COLUMN_ID + "," +
                                            RECIPE_COLUMN_RECIPE_NAME + "," +
                                            RECIPE_COLUMN_CLASSIFICATION + "," +
                                            RECIPE_COLUMN_DETAILS + "," +
                                            RECIPE_COLUMN_FOOD_ID + " " +
                                            "FROM " + TABLE_NAME_RECIPE + " " +
                                            "WHERE " + RECIPE_COLUMN_FOOD_ID + " LIKE '%' || ? || '%' ESCAPE '$' " +
                                            "AND " + RECIPE_COLUMN_CLASSIFICATION + " = ?",
                                    new String[]{food_id, radioFlagStr}
                            );
                        }
                    }
                }
            }

            // 検索結果を反映する
            if(foodIdGetFlag || recipeIdGetFlag) {
                int element;
                if(recipeIdGetFlag)
                    element = 0;
                else
                    element = 1;

                Log.d(LOG_TAG, "[" + searchWord + "]での検索ヒット数 : " + recipeIdNameGetCursor[element].getCount());

                recipeIdNameGetCursor[element].moveToFirst();
                for(int i = 0; i < recipeIdNameGetCursor[element].getCount(); i++){
                    array_recipe_id[i] = recipeIdNameGetCursor[element].getString(0);
                    array_recipe_name[i] = recipeIdNameGetCursor[element].getString(1);
                    array_recipe_classicafition[i] = recipeIdNameGetCursor[element].getString(2);
                    array_recipe_details[i] = recipeIdNameGetCursor[element].getString(3);
                    array_recipe_food_id[i] = recipeIdNameGetCursor[element].getString(4);
                    recipeIdNameGetCursor[element].moveToNext();

                    Log.d(LOG_TAG, "検索結果 : " + array_recipe_name[i]);
                }
                recipeIdNameGetCursor[element].close();

                int countDisplay;
                if(radioFlagStr.equals(RADIO_STRING_INVENTION)){
                    countDisplay = getInventionCountDisplay(recipeIdNameGetCursor[element].getCount());
                }else{
                    countDisplay = recipeIdNameGetCursor[element].getCount();
                }

                for (int i = 0; i < countDisplay; i++) {
                    updateSearchItem = new RecipeListItem(array_recipe_name[i]);
                    updateSearchListitems.add(updateSearchItem);
                }

                recipeIdNameGetCursor[element].close();
            }

            adapter = new listViewAdapter_Recipe(getContext(), R.layout.recipelistview_item, updateSearchListitems);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            return self.setSearchWord(searchWord);
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            // 入力される度に呼び出される
            return false;
        }
    };

    private boolean setSearchWord(String searchWord) {
        //ActionBar actionBar = ((ActionBarActivity)this.getActivity()).getSupportActionBar();
        //actionBar.setTitle(searchWord);
        //actionBar.setDisplayShowTitleEnabled(true);
        if (searchWord != null && !searchWord.equals("")) {
            // searchWordがあることを確認
            this.searchWord = searchWord;
        }
        // 虫眼鏡アイコンを隠す
        this.searchView.setIconified(false);
        // SearchViewを隠す
        this.searchView.onActionViewCollapsed();
        // Focusを外す
        this.searchView.clearFocus();
        return false;
    }

    public void getRadioButtonState(Bundle bundle){
        //ラジオボタンの状態を受け取り、トーストで表示
        String rState = bundle.getString("radiostate");
        //toastMake(rState + "で検索",0,600);
    }

    // レシピの考案数を返すメソッド
    private int getInventionCountDisplay(int extractions){

        int count;
        int existFoodIdCount;
        int[] recipeFoodIdAllCount;
        String[][] recipeContainingFoods;   // 冷蔵庫にある食材を含むレシピを格納
        String[][] recipeFoodIdAll;
        Cursor cursorFoodExist;
        Cursor cursorAllRecipe;
        Cursor cursorRecipe;

        String[] array_food_id;
        int[]    array_food_stock;
        String[] array_recipe_all_id;
        String[] array_recipe_all_name;
        String[] array_recipe_all_classicafition;
        String[] array_recipe_all_details;
        String[] array_recipe_all_food_id;
        String[] array_recipe_id;
        String[] array_recipe_food_id;

        // ストックが1以上の食材IDを抽出
        cursorFoodExist = db.query(
                TABLE_NAME_FOOD,
                new String[]{FOOD_COLUMN_ID, FOOD_COLUMN_STOCK},
                FOOD_COLUMN_STOCK + " > 0",
                null, null, null, null
        );
        cursorFoodExist.moveToFirst();
        array_food_id = new String[cursorFoodExist.getCount()];
        array_food_stock = new int[cursorFoodExist.getCount()];
        for(int i = 0; i < cursorFoodExist.getCount(); i++){
            array_food_id[i] = cursorFoodExist.getString(0);
            array_food_stock[i] = cursorFoodExist.getInt(1);
            cursorFoodExist.moveToNext();
        }
        cursorFoodExist.close();

        if(cursorFoodExist.getCount() != 0) {

            // 全てのレシピを抽出
            cursorAllRecipe = db.query(
                    TABLE_NAME_RECIPE,
                    new String[]{
                            RECIPE_COLUMN_ID, RECIPE_COLUMN_RECIPE_NAME, RECIPE_COLUMN_DETAILS,
                            RECIPE_COLUMN_CLASSIFICATION ,RECIPE_COLUMN_FOOD_ID
                    },
                    null, null, null,
                    null, null
            );
            cursorAllRecipe.moveToFirst();

            // 変数の初期化
            array_recipe_all_id = new String[cursorAllRecipe.getCount()];
            array_recipe_all_name = new String[cursorAllRecipe.getCount()];
            array_recipe_all_details = new String[cursorAllRecipe.getCount()];
            array_recipe_all_classicafition = new String[cursorAllRecipe.getCount()];
            array_recipe_all_food_id = new String[cursorAllRecipe.getCount()];

            // レシピの情報を抽出
            for(int i = 0; i < cursorAllRecipe.getCount(); i++){
                array_recipe_all_id[i] = cursorAllRecipe.getString(0);
                array_recipe_all_name[i] = cursorAllRecipe.getString(1);
                array_recipe_all_details[i] =  cursorAllRecipe.getString(2);
                array_recipe_all_classicafition[i] = cursorAllRecipe.getString(3);
                array_recipe_all_food_id[i] = cursorAllRecipe.getString(4);
                cursorAllRecipe.moveToNext();
            }
            cursorAllRecipe.close();

            // レシピの食材IDを格納
            recipeFoodIdAll = new String[cursorAllRecipe.getCount()][];
            for(int i = 0; i < cursorAllRecipe.getCount(); i++){
                recipeFoodIdAll[i] = array_recipe_all_food_id[i].split("_");
            }

            // 必要な食材をカウントする
            // 炒飯       -> recipeFoodIdAllCount[0] = 4
            // オムライス -> recipeFoodIdAllCount[1] = 3
            recipeFoodIdAllCount = new int[recipeFoodIdAll.length];
            for(int i = 0; i < recipeFoodIdAll.length; i++) {
                for(int j = 0; j < recipeFoodIdAll[i].length && Integer.valueOf(recipeFoodIdAll[i][j]) < 600; j += 2) {
                    recipeFoodIdAllCount[i]++;
                }
            }

            // 冷蔵庫内の食材を含む全てのレシピを検索
            // 例 : 冷蔵庫に卵があれば卵を含む全てのレシピを抽出
            recipeContainingFoods = new String[cursorFoodExist.getCount()][];
            for (int i = 0; i < array_food_id.length; i++) {
                cursorRecipe = db.query(
                        TABLE_NAME_RECIPE,
                        new String[]{RECIPE_COLUMN_ID, RECIPE_COLUMN_FOOD_ID},
                        RECIPE_COLUMN_FOOD_ID + " LIKE '%' || ? || '%' ESCAPE '$' ",
                        new String[]{array_food_id[i]}, null, null, null
                );
                cursorRecipe.moveToFirst();
                array_recipe_id = new String[cursorRecipe.getCount()];
                array_recipe_food_id = new String[cursorRecipe.getCount()];
                for(int j = 0; j < cursorRecipe.getCount(); j++){
                    array_recipe_id[j] = cursorRecipe.getString(0);
                    array_recipe_food_id[j] = cursorRecipe.getString(1);
                    cursorRecipe.moveToNext();
                }
                cursorRecipe.close();

                // 必要数 < ストック のレシピのみ抽出
                // [i][] {レシピIDを格納}
                count = 0;
                recipeContainingFoods[i] = new String[cursorRecipe.getCount()];
                for(int j = 0; j < array_recipe_id.length; j++) {
                    String[] recipeFoodId = array_recipe_food_id[j].split("_");
                    for(int k = 0; k < recipeFoodId.length; k += 2){
                        if(array_food_id[i].equals(recipeFoodId[k]) && Integer.valueOf(recipeFoodId[k + 1]) <= array_food_stock[i]){
                            recipeContainingFoods[i][count] = array_recipe_id[j];
                            count++;
                        }
                    }
                }
            }

            count = 0;
            for(int i = 0; i < array_recipe_all_id.length; i++){
                existFoodIdCount = 0;
                for(int j = 0; j < recipeContainingFoods.length; j++) {
                    for(int k = 0; k < recipeContainingFoods[j].length; k++) {
                        if (array_recipe_all_id[i].equals(recipeContainingFoods[j][k])){
                            existFoodIdCount++;
                        }
                    }
                }
                if(existFoodIdCount == recipeFoodIdAllCount[i]){
                    this.array_recipe_id[count] = array_recipe_all_id[i];
                    this.array_recipe_name[count] = array_recipe_all_name[i];
                    this.array_recipe_details[count] = array_recipe_all_details[i];
                    this.array_recipe_classicafition[count] = array_recipe_all_classicafition[i];
                    this.array_recipe_food_id[count] = array_recipe_all_food_id[i];
                    count++;
                }
            }
        }else{
            count = 0;
        }

        // 抽出したレシピの数を返す
        return count;
    }
}

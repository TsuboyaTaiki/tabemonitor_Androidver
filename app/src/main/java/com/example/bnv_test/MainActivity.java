package com.example.bnv_test;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + MainActivity.class.getSimpleName();

    private TextView mTextMessage;
    private Bundle fragmentflag = new Bundle();     //選択しているフラグメントを判別する引数
    private Bundle fragmentstate = new Bundle();    //選択しているフラグメントがHomeかNotificationsか判別する引数

    //アクティビティ作成の際に呼び出されるメソッド
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ///////////////////////
        // レーベンシュタイン距離
        testLeven();
        ///////////////////////


        Log.d(LOG_TAG, "onCreate");

        //MainActivity上でactivity_mainの表示を行う
        setContentView(R.layout.activity_main);

        //ツールバーの設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.BLACK);
        //toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);

        //最初にロードするフラグメント
        loadFragment(new HomeFragment());

        //ナビゲーションビューを取得してリスナーをつける
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

    }

    //ボトムナビゲーションのアイテムがタップされた際に呼び出されるメソッド
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {

            // HomeFragmentに遷移
            case R.id.navigation_home:
                fragmentflag.putString("flag","home");
                fragmentstate.putString("state","home");
                fragment = new HomeFragment();
                break;

            // DashboardFragmentに遷移
            case R.id.navigation_dashboard:
                fragmentflag.putString("flag","dashboard");
                fragment = new DashboardFragment();
                break;

            // CameraFragmentに遷移
            case R.id.navigation_camera:
                fragmentflag.putString("flag","notifications");
                fragmentstate.putString("state","notifications");
                fragment = new CameraFragment();
                break;

        }
        return loadFragment(fragment);
    }


    private boolean loadFragment(Fragment fragment) {

        //引数をString型で受け取る
        String getflag = fragmentflag.getString("flag");
        String getstate = fragmentstate.getString("state");

        //「HomeFragment」遷移処理
        if(getflag == "home"){

            if (fragment != null) {
                TransitionRight(fragment);
                return true;
            }
            return false;

        }
        //「DashboardFragment」遷移処理
        else if(getflag == "dashboard"){

            //遷移前のFragmentが「HomeFragment」のとき
            if(getstate == "home"){

                if (fragment != null) {
                    TransitionLeft(fragment);
                    return true;
                }
                return false;
            }
            //遷移前のFragmentが「NotificationsFragment」のとき
            else if(getstate == "notifications"){
                if (fragment != null) {
                    TransitionRight(fragment);
                    return true;
                }
                return false;
            }
            //起動時
            else{
                if (fragment != null) {
                    TransitionLeft(fragment);
                    return true;
                }
                return false;
            }

        }
        //「NotificationsFragment」遷移処理
        else{
            if (fragment != null) {
                TransitionLeft(fragment);
                return true;
            }
            return false;
        }
    }

    //トースト作成用メソッド
    private void toastMake(String message,int x,int y){
        Toast toast = Toast.makeText(this,message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,x,y);
        toast.show();
    }

    //右スライドアニメーションで遷移
    private void TransitionRight(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    //左スライドアニメーションで遷移
    private void TransitionLeft(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void setActionBarTitle(String title){
        getActionBar().setTitle(title);
    }

    public void testLeven(){
        String modelStr = "テスト";
        String targetStr = "テント";
        // Charの配列に変換
        char[] modelArray = modelStr.toCharArray();
        char[] targetArray = targetStr.toCharArray();
        // コスト
        int cost = 0;
        int costLimit = 10;

        // 距離測定ループ
        while (!modelArray.equals(targetArray) && cost < costLimit){

            cost++;
        }

        // 文字列をStringに戻す
        targetStr = "";
        for (int i = 0; targetArray.length > i; i++)targetStr = targetStr.concat(String.valueOf(targetArray[i]));


        Log.i("tag"," - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
        if(cost == costLimit)Log.i("tag","『失敗』　　モデル：" + modelStr + "　　対象：" + targetStr);
        else Log.i("tag","『成功』　コスト："+ cost +"　　モデル：" + modelArray + "　　対象：" + targetArray);
        Log.i("tag"," - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
    }
}

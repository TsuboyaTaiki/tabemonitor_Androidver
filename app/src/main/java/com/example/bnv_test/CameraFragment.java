package com.example.bnv_test;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.bnv_test.Camera.CAMERA_REQUEST_CODE;

public class CameraFragment extends Fragment {

    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + CameraFragment.class.getSimpleName();

    private Intent cameraIntent;
    private Uri cameraUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // インスタンスの生成
        Camera camera = new Camera(getActivity().getPackageName(), getContext());

        // フォルダーパスを引数としたcameraConfigメソッドを実行
        camera.cameraConfig(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));

        // Intent と Uri を格納
        cameraIntent = camera.getCameraIntent();
        cameraUri = camera.getCameraUri();

        // カメラ起動
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE) {

            switch (resultCode){
                // 撮影後処理
                case RESULT_OK:
                    if (cameraUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), cameraUri);

                            characterRecognition(bitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    break;

                // キャンセル時処理
                case RESULT_CANCELED:

                    // 管理画面に遷移
                    BottomNavigationView navigationView = (BottomNavigationView) getActivity().findViewById(R.id.navigation);
                    navigationView.setSelectedItemId(R.id.navigation_home);
                    break;

                default:
                    break;
            }
        }
    }

    private void characterRecognition(Bitmap bitmap) {

        // インスタンスを生成
        SaveString saveString = new SaveString();
        VisionApiOcr visionApiOcr = new VisionApiOcr(
                getContext(), getActivity().getPackageName(),
                getActivity().getPackageManager(), saveString);

        // getCharacterメソッドの実行(引数はBitmap画像)
        visionApiOcr.getCharacter(bitmap);

        // マルチスレッドで文字列が入るまでループする
        // その後 別のアクティビティに移動
        new Thread( () -> {
            // 文字列が入るまでループ
            while(!saveString.isString()){
                // 1秒待機しながらループ
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            // 取得した文字列を格納
            String result = saveString.getString();

            Log.d("確認ログ", " \nResult :\n" + result);


            // 読み取り終わったらConfirmFragmentに遷移
            Bundle bundle = new Bundle();

            //「position」という名前のキーに値を設定
            bundle.putString("recognString", result);

            //トランザクションを実行するためのマネージャーのインスタンスを作成
            FragmentManager fragmentManager = getFragmentManager();

            //ConsumptionFragmentのインスタンスを作成
            ConfirmFragment fragment = new ConfirmFragment();

            if(fragmentManager != null){
                //引数に「bundle」を設定
                fragment.setArguments(bundle);

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
        }).start();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().setTitle("");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }
}


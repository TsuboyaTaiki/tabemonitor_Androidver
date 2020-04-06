package com.example.bnv_test;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class listViewAdapter extends ArrayAdapter <SampleListItem> {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + listViewAdapter.class.getSimpleName();

    private int mResouce;
    private List<SampleListItem> mItems;
    private List<Integer> mStock;
    private LayoutInflater mInflater;

    listViewAdapter(Context context, int resouce, List<SampleListItem> items) {
        super(context, resouce, items);

        mResouce = resouce;
        mItems = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    listViewAdapter(Context context, int resouce, List<SampleListItem> items, List<Integer> stock) {
        super(context, resouce, items);

        mResouce = resouce;
        mItems = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mStock = stock;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;

        } else {
            view = mInflater.inflate(mResouce, null);
        }

        SampleListItem item = mItems.get(position);

        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        thumbnail.setImageBitmap(item.getThumbnail());

        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(item.getTitle());

        TextView amount = (TextView) view.findViewById(R.id.amount);
        amount.setText(item.getAmount());

        // ＋ボタンと－ボタンのインスタンス
        ImageButton upward = (ImageButton) view.findViewById(R.id.upward);
        ImageButton downward = (ImageButton) view.findViewById(R.id.downward);

        // 削除ボタンのインスタンス
        ImageButton delete = (ImageButton) view.findViewById(R.id.delete);

        // 食材が足りなければ色を赤に変える
        if(mStock != null) {
            try {
                int amountNumberOnly = Integer.parseInt(mItems.get(position).getAmount().replaceAll("[^0-9]", ""));
                int stockNumber = mStock.get(position);
                if (amountNumberOnly > stockNumber) {
                    amount.setTextColor(Color.RED);

                    // 赤文字にした食材名のログ
                    Log.d(LOG_TAG, "(赤) " + mItems.get(position).getTitle());
                }else {
                    amount.setTextColor(Color.BLACK);

                    // 黒文字にした食材名のログ
                    Log.d(LOG_TAG, "(黒) " + mItems.get(position).getTitle());
                }
                // 必要数と在庫数
                Log.d(LOG_TAG, "必要数 : " + amountNumberOnly + " 在庫数 : " + stockNumber);

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        // クリックしたときにそのポジションを返す
        upward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, R.id.upward);
            }
        });

        downward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, R.id.downward);
            }
        });

        amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, R.id.amount);
            }
        });

        // 管理画面用の削除ボタン用
        if(delete != null) {
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ListView) parent).performItemClick(view, position, R.id.delete);
                }
            });
        }

        return view;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
package com.example.bnv_test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class detectfoodsAdapter extends ArrayAdapter <SampleListItem>{

    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + detectfoodsAdapter.class.getSimpleName();

    private int mResouce;
    private List<SampleListItem> mItems;
    private LayoutInflater mInflater;


    public detectfoodsAdapter(Context context, int resouce, List<SampleListItem> items){
        super(context,resouce,items);

        mResouce = resouce;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent){
        View view;

        if(convertView != null){
            view = convertView;

        }
        else{
            view = mInflater.inflate(mResouce,null);
        }

        SampleListItem item = mItems.get(position);

        ImageView thumbnail = (ImageView)view.findViewById(R.id.thumbnail);
        thumbnail.setImageBitmap(item.getThumbnail());

        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText(item.getTitle());

        TextView amount = (TextView)view.findViewById(R.id.amount);
        amount.setText(item.getAmount());


        // ＋ボタンと－ボタンのインスタンス
        ImageButton upward = (ImageButton)view.findViewById(R.id.confirm_upward);
        ImageButton downward = (ImageButton)view.findViewById(R.id.confirm_downward);


        // クリックしたときにそのポジションを返す
        upward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, R.id.confirm_upward);
            }
        });

        downward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, R.id.confirm_downward);
            }
        });


        return view;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
package com.example.bnv_test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class otherlistAdapter extends ArrayAdapter <OtherListItem>{

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + otherlistAdapter.class.getSimpleName();

    private int mResouce;
    private List<OtherListItem> mItems;
    private LayoutInflater mInflater;

    public otherlistAdapter(Context context, int resouce, List<OtherListItem> items){
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

        OtherListItem item = mItems.get(position);

        TextView title = (TextView)view.findViewById(R.id.otitle);
        title.setText(item.getTitle());

        TextView amount = (TextView)view.findViewById(R.id.oamount);
        amount.setText(item.getAmount());

        return view;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
package com.example.bnv_test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class listViewAdapter_Recipe extends ArrayAdapter <RecipeListItem>{

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + listViewAdapter_Recipe.class.getSimpleName();

    private int mResouce;
    private List<RecipeListItem> mItems;
    private LayoutInflater mInflater;

    public listViewAdapter_Recipe(Context context, int resouce, List<RecipeListItem> items){
        super(context,resouce,items);

        mResouce = resouce;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view;

        if(convertView != null){
            view = convertView;
        }
        else{
            view = mInflater.inflate(mResouce,null);
        }

        RecipeListItem item = mItems.get(position);

        //ImageView thumbnail = (ImageView)view.findViewById(R.id.photo);
        //thumbnail.setImageBitmap(item.getPhoto());

        TextView title = (TextView)view.findViewById(R.id.rname);
        title.setText(item.getRname());

        return view;
    }
}
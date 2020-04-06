package com.example.bnv_test;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Grid_choose_foods_Adapter extends BaseAdapter {

    class ViewHolder {
        RelativeLayout relativeLayout;
        ImageView imageView;
        TextView textView;
    }

    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + Grid_choose_foods_Adapter.class.getSimpleName();

    private List<Integer> imageList = new ArrayList<>();
    private String[] names;
    private String[] id;
    private LayoutInflater inflater;
    private int layoutId;
    private String[] recipeFoodId;

    // 引数がMainActivityからの設定と合わせる
    Grid_choose_foods_Adapter(Context context, int layoutId, List<Integer> iList,
                              String[] members, String[] id, String[] recipeFoodId) {
        super();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutId = layoutId;
        imageList = iList;
        names = members;
        this.id = id;
        this.recipeFoodId = recipeFoodId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {

            // main.xml の <GridView .../> に grid_items.xml を inflate して convertView とする
            convertView = inflater.inflate(layoutId, parent, false);

            // ViewHolder を生成
            holder = new ViewHolder();

            holder.relativeLayout = convertView.findViewById(R.id.relative_layout_choose);
            holder.imageView = convertView.findViewById(R.id.image_view_choose);
            holder.textView = convertView.findViewById(R.id.text_view_choose);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        try{
            // 選択した食材を赤くする
            if(recipeFoodId != null) {
                for(int i = 0 ; i < recipeFoodId.length; i += 2) {
                    if(id[position].equals(recipeFoodId[i])) {
                        holder.relativeLayout.setBackgroundResource(R.drawable.gridview_choose_border_red);
                        break;
                    }else {
                        holder.relativeLayout.setBackgroundResource(R.drawable.gridview_choose_border);
                    }
                }
            }
            holder.imageView.setImageResource(imageList.get(position));
            holder.textView.setText(names[position]);
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public int getCount() {
        // List<String> imgList の全要素数を返す
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
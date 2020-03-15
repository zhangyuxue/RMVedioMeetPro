package com.hbajlive;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

class MeetAdapter extends BaseAdapter {
    private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
    ArrayList<HashMap<String, String>> listItem;

    public MeetAdapter(Context context,ArrayList<HashMap<String, String>> listItem) {
        this.mInflater = LayoutInflater.from(context);
        this.listItem = listItem;
    }//声明构造函数

    @Override
    public int getCount() {
        return listItem.size();
    }//这个方法返回了在适配器中所代表的数据集合的条目数

    @Override
    public Object getItem(int position) {
        return listItem.get(position);
    }//这个方法返回了数据集合中与指定索引position对应的数据项

    @Override
    public long getItemId(int position) {
        return position;
    }//这个方法返回了在列表中与指定索引对应的行id

    //利用convertView+ViewHolder来重写getView()
    static class ViewHolder
    {
        public String text;
        public TextView textTitle;
    }//声明一个外部静态类
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder ;
        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.activity_meetitem, null);
            holder.textTitle = (TextView)convertView.findViewById(R.id.meetname);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();

        }
        //holder.text = ((String) listItem.get(position).get("ItemText"));
        //holder.textTitle.setText((String) listItem.get(position).get("ItemTitle"));
//        holder.text.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.out.println("你点击了选项"+position);//bottom会覆盖item的焦点，所以要在xml里面配置android:focusable="false"
//            }
//        });

        return convertView;
    }//这个方法返回了指定索引对应的数据项的视图
}

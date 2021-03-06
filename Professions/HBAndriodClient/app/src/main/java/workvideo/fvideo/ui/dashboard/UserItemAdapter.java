package workvideo.fvideo.ui.dashboard;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import workvideo.fvideo.R;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

class UserItemAdapter extends BaseAdapter {
    private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
    ArrayList<HashMap<String, String>> listItem;

    public UserItemAdapter(Context context,ArrayList<HashMap<String, String>> listItem) {
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
        public TextView usrName;
        public TextView usrstate;
    }//声明一个外部静态类
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder ;
        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.userlistitem, null);
            holder.usrName = (TextView)convertView.findViewById(R.id.userName);
            holder.usrstate = (TextView)convertView.findViewById(R.id.usrState);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }
        final HashMap<String,String> obj = (HashMap<String,String>)getItem(position);
        holder.usrName.setText((String) obj.get("Name"));
//
        if(obj.get("State").toString().equals("1"))
        {
            holder.usrstate.setText("在线");
        }else if(obj.get("State").toString().equals("2")){
            holder.usrstate.setText("会议中");
        }
        else{
            holder.usrstate.setText("离线");
        }
//
//        holder.mycheckbox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (holder.mycheckbox.isChecked())
//                {
//                    listItem.get(position).put("ItemCheck","1");
//                }
//                else{
//                    listItem.get(position).put("ItemCheck","0");
//                }
//            }
//        });

        return convertView;
    }//这个方法返回了指定索引对应的数据项的视图
}

package com.hbajlive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbajlive.net.TcpCompare;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class InvitActivity extends AppCompatActivity {

    private ListView lvusers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invit);
        App.getInstance().actiiveInvit=this;
        lvusers = (ListView)findViewById(R.id.lvusers);
        try {
            TcpCompare.sharedCenter().getUserList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void initUserListView(JsonArray datas){
        /*定义一个以HashMap为内容的动态数组*/
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();/*在数组中存放数据*/
        for (int i = 0; i < datas.size(); i++) {
            JsonObject dataelement =  datas.get(i).getAsJsonObject();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("ItemText", dataelement.get("Msg_useruid").getAsString());//加入图片
            map.put("ItemTitle",dataelement.get("Msg_userName").getAsString());
            listItem.add(map);
        }
        MeetAdapter adapter = new MeetAdapter(this, listItem);
        lvusers.setAdapter(adapter);//为ListView绑定适配器

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                System.out.println("你点击了第" + arg2 + "行");//设置系统输出点击的行
//            }
//        });
    }
}

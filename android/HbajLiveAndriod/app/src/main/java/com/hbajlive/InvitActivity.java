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
import java.util.Vector;

import android.os.StrictMode;
import android.os.Build;

public class InvitActivity extends AppCompatActivity {

    private ListView lvusers;
    private Button invitUsers;
    InvitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invit);
        if (Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }
        App.getInstance().actiiveInvit=this;
        lvusers = (ListView)findViewById(R.id.lvusers);
        try {
            TcpCompare.getUserList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        invitUsers = (Button)findViewById(R.id.invitUsers);
        invitUsers.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    Vector<String> str;
                    str = new Vector<>();
                    for (int i=0;i<adapter.listItem.size();i++)
                    {
                        String state = adapter.listItem.get(i).get("ItemCheck").toString();
                        if (state.equals("1"))
                        {
                            str.addElement(adapter.listItem.get(i).get("ItemText").toString());
                        }
                    }
                    TcpCompare.invitUsers(str);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public void initUserListView(JsonArray datas){
        /*定义一个以HashMap为内容的动态数组*/
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();/*在数组中存放数据*/
        for (int i = 0; i < datas.size(); i++) {
            JsonObject dataelement =  datas.get(i).getAsJsonObject();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("ItemText", dataelement.get("Msg_useruid").getAsString());//加入图片
            map.put("ItemTitle",dataelement.get("Msg_userName").getAsString());
            map.put("ItemMeet","");
            if(!dataelement.get("Msg_JionMeetID").getAsString().equals(""))
            {
                map.put("ItemMeet",dataelement.get("Msg_JionMeetID").getAsString());
            }
            if(!dataelement.get("Msg_CreateMeetID").getAsString().equals(""))
            {
                map.put("ItemMeet",dataelement.get("Msg_CreateMeetID").getAsString());
            }
            map.put("ItemCheck","0");
            listItem.add(map);
        }
        adapter = new InvitAdapter(this, listItem);
        lvusers.setAdapter(adapter);//为ListView绑定适配器

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                System.out.println("你点击了第" + arg2 + "行");//设置系统输出点击的行
//            }
//        });
    }
}

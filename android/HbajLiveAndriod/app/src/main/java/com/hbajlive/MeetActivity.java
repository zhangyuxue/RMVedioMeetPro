package com.hbajlive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbajlive.net.TcpCompare;

import org.json.JSONException;

import java.util.UUID;
import java.util.HashMap;
import java.util.ArrayList;
import android.widget.AdapterView;

public class MeetActivity extends AppCompatActivity {

    Button loginbt;
    Button jionbt;
    EditText meetid;
    EditText meetname;

    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);

        App.getInstance().actiiveMeet=this;
        getVidwByID();
        try {
            TcpCompare.sharedCenter().getMeetingList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void initMeetingListView(JsonArray datas){
        /*定义一个以HashMap为内容的动态数组*/
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();/*在数组中存放数据*/
        for (int i = 0; i < datas.size(); i++) {
            JsonObject dataelement =  datas.get(i).getAsJsonObject();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("ItemText", dataelement.get("Msg_CreateMeetID").getAsString());//加入图片
            map.put("ItemTitle",dataelement.get("Msg_meetName").getAsString());
            listItem.add(map);
        }
        MeetAdapter adapter = new MeetAdapter(this, listItem);
        lv.setAdapter(adapter);//为ListView绑定适配器

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                System.out.println("你点击了第" + arg2 + "行");//设置系统输出点击的行
//            }
//        });
    }

    void getVidwByID()
    {
        loginbt= (Button) findViewById(R.id.createMeetingBt);
        jionbt= (Button) findViewById(R.id.jisonMeetingBt);
        meetid= (EditText) findViewById(R.id.meetingidstr);
        meetname= (EditText) findViewById(R.id.meetname);
        lv= (ListView) findViewById(R.id.lv);

        loginbt.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    TcpCompare.sharedCenter().createMeeting(meetid.getText().toString()
                                ,App.getInstance().gUserName,
                                        App.getInstance().gUserUID,
                                        App.getInstance().gUserPusherID,
                                        meetname.getText().toString(),
                                        App.getInstance().gUserLevel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        jionbt.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    TcpCompare.sharedCenter().jionMeeting(meetid.getText().toString()
                            ,App.getInstance().gUserName,
                            App.getInstance().gUserUID,
                            App.getInstance().gUserPusherID,
                            App.getInstance().gMeetingName,
                            App.getInstance().gUserLevel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void showMainActive()
    {
        startActivity(new Intent(MeetActivity.this, MainActivity.class));
    }
}

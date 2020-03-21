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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.media.MediaPlayer;

public class MeetActivity extends AppCompatActivity {

    Button loginbt;
    Button jionbt;
    EditText meetid;
    EditText meetname;


    MediaPlayer  mPlayer=null;

    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);

        App.getInstance().actiiveMeet=this;
        getVidwByID();



    }

    public void initMeetingListView(JsonArray datas){
        /*定义一个以HashMap为内容的动态数组*/
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();/*在数组中存放数据*/
        for (int i = 0; i < datas.size(); i++) {
            JsonObject dataelement =  datas.get(i).getAsJsonObject();
            String createid = dataelement.get("Msg_CreateMeetID").getAsString();
            if(createid.equals("0"))
                continue;
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("ItemText", dataelement.get("Msg_CreateMeetID").getAsString());//加入图片
            map.put("ItemTitle",dataelement.get("Msg_meetName").getAsString());
            listItem.add(map);
        }
        final MeetAdapter adapter = new MeetAdapter(this, listItem);
        lv.setAdapter(adapter);//为ListView绑定适配器

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                HashMap<String, String> obj = (HashMap<String, String>)adapter.getItem(arg2);
                meetid.setText(obj.get("ItemText").toString());
                meetname.setText(obj.get("ItemTitle").toString());
            }
        });
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
                    App.getInstance().gMeetingName = meetname.getText().toString();
                    TcpCompare.sharedCenter().createMeeting(meetid.getText().toString()
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
        App.getInstance().actiiveMeet=null;
        MeetActivity.this.finish();
    }

    public void showInvitDialog(JsonObject myJsonObject) {

        if(mPlayer == null)
        {
            mPlayer = MediaPlayer.create(MeetActivity.this, R.raw.audio);
            mPlayer.setLooping(true);
            mPlayer.start();
        }
        else
            return;

        AlertDialog.Builder builder = new Builder(MeetActivity.this);


        String contentstr;
        final String Msg_JionMeetID = myJsonObject.get("Msg_JionMeetID").getAsString();
        final String Msg_meetName = myJsonObject.get("Msg_meetName").getAsString();
        final String Msg_userName = myJsonObject.get("Msg_userName").getAsString();
        final String Msg_useruid = myJsonObject.get("Msg_useruid").getAsString();

        builder.setMessage(Msg_userName+"邀请您参加会议");

        builder.setTitle(Msg_meetName);

        builder.setPositiveButton("确认", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mPlayer.stop();
                mPlayer.release();
                mPlayer=null;
                try {
                    TcpCompare.sharedCenter().jionMeeting(Msg_JionMeetID
                            ,App.getInstance().gUserName,
                            App.getInstance().gUserUID,
                            App.getInstance().gUserPusherID,
                            Msg_meetName,
                            App.getInstance().gUserLevel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mPlayer.stop();
                mPlayer.release();
                mPlayer=null;
            }
        });

        builder.create().show();
    }
}

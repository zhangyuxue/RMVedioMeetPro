package com.hbajlive;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.gson.JsonObject;
import com.hbajlive.net.TcpCompare;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import workvideo.meetingSdk.Meeting;
import workvideo.meetingSdk.media.CameraView;
import workvideo.meetingSdk.media.PlayerView;
import workvideo.meetingSdk.media.Sound;
import workvideo.meetingSdk.media.Video;
import workvideo.meetingSdk.media.VideoActivity;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;
import android.content.Intent;

public class MainActivity extends VideoActivity {

    int videoIndex=0;

    Button startCamera;

    PlayerView mainView;

    PlayerView paly0;
    PlayerView paly1;
    PlayerView paly2;
    PlayerView paly3;
    CameraView cameraView;

    Button nextpagebt;
    Button prepagebt;
    Button startDesktop;
    Button invitUsers;

    protected void onSize(int width, int height){}

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        Sound.instance.stop(); //停止声音设备

        mainView.release(); //需要明确释放，否则会内存泄露
        paly0.release(); //需要明确释放，否则会内存泄露
        paly1.release(); //需要明确释放，否则会内存泄露
        paly2.release(); //需要明确释放，否则会内存泄露
        paly3.release(); //需要明确释放，否则会内存泄露

        Meeting.cleanup();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.getInstance().actiiveMain=this;

        checkPermission();

        Sound.instance.run(); //开启声音设备 (或则需要时开启)       Sound.have_jobs() ?

        cameraView = (CameraView)findViewById(R.id.cameraView);
        mainView = (PlayerView)findViewById(R.id.playerView);
        paly0 = (PlayerView)findViewById(R.id.playerView0);
        paly1 = (PlayerView)findViewById(R.id.playerView1);
        paly2 = (PlayerView)findViewById(R.id.playerView2);
        paly3 = (PlayerView)findViewById(R.id.playerView3);

        paly0.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int totalUsers = App.getInstance().gUserList.size();
                int currentindex = videoIndex+0;
                if(currentindex < totalUsers)
                {
                    JsonObject obj = App.getInstance().gUserList.elementAt(currentindex);
                    String pushid = obj.get("msg_userpushid").getAsString();

                    String loadurl = "fvideo://"+
                            App.getInstance().gStreamServer+"/"
                            +pushid;
                    mainView.load(null);
                    mainView.load(loadurl);
                    int playmast=0;
                    if (App.getInstance().gUserPusherID.equals(pushid)) {
                        playmast = Video.LayerBitVideoLow;
                    } else
                    {
                        playmast = Video.LayerBitAudio | Video.LayerBitVideoLow;
                    }
                    mainView.play(playmast);
                }

                return true;
            }
        });
        paly1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int totalUsers = App.getInstance().gUserList.size();
                int currentindex = videoIndex+1;
                if(currentindex < totalUsers)
                {
                    JsonObject obj = App.getInstance().gUserList.elementAt(currentindex);
                    String pushid = obj.get("msg_userpushid").getAsString();

                    String loadurl = "fvideo://"+
                            App.getInstance().gStreamServer+"/"
                            +pushid;
                    mainView.load(null);
                    mainView.load(loadurl);
                    int playmast=0;
                    if (App.getInstance().gUserPusherID.equals(pushid)) {
                        playmast = Video.LayerBitVideoLow;
                    } else
                    {
                        playmast = Video.LayerBitAudio | Video.LayerBitVideoLow;
                    }
                    mainView.play(playmast);
                }

                return true;
            }
        });
        paly2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int totalUsers = App.getInstance().gUserList.size();
                int currentindex = videoIndex+2;
                if(currentindex < totalUsers)
                {
                    JsonObject obj = App.getInstance().gUserList.elementAt(currentindex);
                    String pushid = obj.get("msg_userpushid").getAsString();

                    String loadurl = "fvideo://"+
                            App.getInstance().gStreamServer+"/"
                            +pushid;
                    mainView.load(null);
                    mainView.load(loadurl);
                    int playmast=0;
                    if (App.getInstance().gUserPusherID.equals(pushid)) {
                        playmast = Video.LayerBitVideoLow;
                    } else
                    {
                        playmast = Video.LayerBitAudio | Video.LayerBitVideoLow;
                    }
                    mainView.play(playmast);
                }

                return true;
            }
        });
        paly3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int totalUsers = App.getInstance().gUserList.size();
                int currentindex = videoIndex+3;
                if(currentindex < totalUsers)
                {
                    JsonObject obj = App.getInstance().gUserList.elementAt(currentindex);
                    String pushid = obj.get("msg_userpushid").getAsString();

                    String loadurl = "fvideo://"+
                            App.getInstance().gStreamServer+"/"
                            +pushid;
                    mainView.load(null);
                    mainView.load(loadurl);
                    int playmast=0;
                    if (App.getInstance().gUserPusherID.equals(pushid)) {
                        playmast = Video.LayerBitVideoLow;
                    } else
                    {
                        playmast = Video.LayerBitAudio | Video.LayerBitVideoLow;
                    }
                    mainView.play(playmast);
                }

                return true;
            }
        });

            paly0.play(Video.LayerBitAudio | Video.LayerBitVideoLow);
            paly1.play(Video.LayerBitAudio | Video.LayerBitVideoLow);
            paly2.play(Video.LayerBitAudio | Video.LayerBitVideoLow);
            paly3.play(Video.LayerBitAudio | Video.LayerBitVideoLow);

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width2 = outMetrics.widthPixels;
        int height2 = outMetrics.heightPixels;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mainView.getLayoutParams();
        params.width =width2;
        params.height =height2/4*3;
        mainView.setLayoutParams(params);

        int offsetpix = dip2px(this,50);
        RelativeLayout.LayoutParams params0 = (RelativeLayout.LayoutParams)paly0.getLayoutParams();
        params0.width =width2/4;
        params0.height =height2/4-offsetpix;
        paly0.setLayoutParams(params0);

        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)paly1.getLayoutParams();
        params1.width =width2/4;
        params1.height =height2/4-offsetpix;
        paly1.setLayoutParams(params1);

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)paly2.getLayoutParams();
        params2.width =width2/4;
        params2.height =height2/4-offsetpix;
        paly2.setLayoutParams(params2);

        RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams)paly3.getLayoutParams();
        params3.width =width2/4;
        params3.height =height2/4-offsetpix;
        paly3.setLayoutParams(params3);


        //cameraView = (CameraView)findViewById(R.id.cameraView);

        invitUsers = (Button)findViewById(R.id.invitUsers);
        invitUsers.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setClass(v.getContext(),InvitActivity.class);
                /* 取得相片后返回本画面 */
                startActivityForResult(intent, 1);


            }
        });

        startCamera = (Button)findViewById(R.id.startCamera);
        startCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                cameraView.setVisibility(View.VISIBLE);

                RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams)cameraView.getLayoutParams();
                params3.width =1;
                params3.height =1;
                cameraView.setLayoutParams(params3);

                Meeting.push_to(null);
                mainView.load(null);

                Meeting.set_mic_state(Meeting.ON);
                Video.set_source(Video.source_camera);
                String loadurl = "fvideo://"+
                        App.getInstance().gStreamServer+"/"
                        +App.getInstance().gUserPusherID;
                Meeting.push_to(loadurl);
                mainView.load(loadurl);
                mainView.play(Video.LayerBitVideoMedium);

            }
        });

        nextpagebt = (Button)findViewById(R.id.nextpagebt);
        nextpagebt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int totalUsers = App.getInstance().gUserList.size();
                int lastmode = totalUsers%4;
                if((videoIndex+3) < (totalUsers-lastmode))
                {
                    videoIndex+=3;
                }
                FlushVideos();
            }
        });

        prepagebt = (Button)findViewById(R.id.prepagebt);
        prepagebt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if((videoIndex-3) >= 0)
                {
                    videoIndex-=3;
                }
                FlushVideos();
            }
        });

        startDesktop = (Button)findViewById(R.id.startDesktop);
        startDesktop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Meeting.push_to(null);
                mainView.load(null);
                cameraView.setVisibility(View.INVISIBLE);
                Video.set_source(Video.source_screen);
                startScreenCapture();

                Meeting.set_mic_state(Meeting.ON);
                String loadurl = "fvideo://"+
                        App.getInstance().gStreamServer+"/"
                        +App.getInstance().gUserPusherID;
                Meeting.push_to(loadurl);
                mainView.load(loadurl);
                mainView.play(Video.LayerBitVideoMedium);
            }
        });

    }


    public void FlushVideos()
    {
        paly0.load(null);
        paly1.load(null);
        paly2.load(null);
        paly3.load(null);

        for (int i=videoIndex;i<App.getInstance().gUserList.size();i++) {
            JsonObject obj = App.getInstance().gUserList.elementAt(i);
            String pushid = obj.get("Msg_userpushid").getAsString();

            String loadurl = "fvideo://" +
                    App.getInstance().gStreamServer + "/"
                    + pushid;
            int realindex = i - videoIndex;
            int playmast = 0;
            if (App.getInstance().gUserPusherID.equals(pushid)) {
                playmast = Video.LayerBitVideoLow;
            } else
            {
                playmast = Video.LayerBitAudio | Video.LayerBitVideoLow;
            }
            switch (realindex)
            {
                case 0:
                {
                    paly0.load(loadurl);
                    paly0.play(playmast);
                }
                break;
                case 1:
                {
                    paly1.load(loadurl);
                    paly1.play(playmast);
                }
                break;
                case 2:
                {
                    paly2.load(loadurl);
                    paly2.play(playmast);
                }
                break;
                case 3:
                {
                    paly3.load(loadurl);
                    paly3.play(playmast);
                }
                break;
            }
        }
    }
    public static int dip2px(Context context,float dpValue){

        final float scale = context.getResources().getDisplayMetrics().density;

        return (int)(dpValue*scale+0.5f);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

}

package workvideo.fvideo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import workvideo.fvideo.net.QSUser;
import workvideo.fvideo.net.TcpCompare;
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
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.graphics.Typeface;
import android.view.KeyEvent;

public class RoomActivity extends VideoActivity {

    int videoIndex=0;

    TextView meetidTitle;

    PlayerView mainView;
    PlayerView playeraudio;

    PlayerView paly0;
    PlayerView paly1;
    PlayerView paly2;
    PlayerView paly3;
    CameraView cameraView;


    Button startCamera;
    Button nextpagebt;
    Button prepagebt;
    Button startDesktop;
    Button invitUsers;
    Button micopenclose;
    Button exitmeet;
    Typeface    typeface;

    Boolean localCamerastate=false;
    Boolean localMicstate=false;

    protected void onSize(int width, int height){}

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
            //do something.
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        App.instance.roomactive=null;
        App.instance.gRoomID="";
        App.isRoomManager=false;
        Sound.instance.stop(); //停止声音设备

        mainView.release(); //需要明确释放，否则会内存泄露
        paly0.release(); //需要明确释放，否则会内存泄露
        paly1.release(); //需要明确释放，否则会内存泄露
        paly2.release(); //需要明确释放，否则会内存泄露
        paly3.release(); //需要明确释放，否则会内存泄露
        playeraudio.release();

    }

    public void changeVideoSize(int orientation,int startx,int stary,int W,int H,PlayerView playview) {
        int resolution = playview.resolution();
        int videoWidth = resolution >>16;;
        int videoHeight = resolution &0xffff;
        if(videoHeight <=0 || videoWidth <=0) {
            videoHeight =240;
            videoWidth =320;
        }

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float ratio = Math.min(((float) W/(float) videoWidth),(float) H/(float)videoHeight);

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth *ratio);
        videoHeight = (int) Math.ceil((float) videoHeight *ratio);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)playview.getLayoutParams();

        params.width =videoWidth;
        params.height =videoHeight;


        int x = startx+(W-videoWidth)/2;
        int y = stary+(H-videoHeight)/2;

        params.leftMargin=x;
        //params.rightMargin=x;
        params.topMargin=y;
        playview.setLayoutParams(params);


        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        //mainView.setLayoutParams(new RelativeLayout.LayoutParams(videoWidth, videoHeight));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        App.instance.roomactive=this;
        if (Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        checkPermission();

        Sound.instance.run(); //开启声音设备 (或则需要时开启)       Sound.have_jobs() ?

        typeface=Typeface.createFromAsset(getAssets(),
                "fonts/iconfont.ttf");
        cameraView = (CameraView)findViewById(R.id.cameraView);
        mainView = (PlayerView)findViewById(R.id.playerView);
        playeraudio = (PlayerView)findViewById(R.id.playeraudio);
        paly0 = (PlayerView)findViewById(R.id.playerView0);
        paly1 = (PlayerView)findViewById(R.id.playerView1);
        paly2 = (PlayerView)findViewById(R.id.playerView2);
        paly3 = (PlayerView)findViewById(R.id.playerView3);

        meetidTitle = (TextView)findViewById(R.id.meetidTitle);


        paly0.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ChangePlayer(0);
                return true;
            }
        });
        paly1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ChangePlayer(1);
                return true;
            }
        });
        paly2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ChangePlayer(2);
                return true;
            }
        });
        paly3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ChangePlayer(3);
                return true;
            }
        });

        paly0.play(Video.LayerBitVideoLowest);
        paly1.play(Video.LayerBitVideoLowest);
        paly2.play(Video.LayerBitVideoLowest);
        paly3.play(Video.LayerBitVideoLowest);


        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width2 = outMetrics.widthPixels;
        int height2 = outMetrics.heightPixels;
        int offsetpix = dip2px(this,50);
        int scaledsize = height2/4 ;//(height2-offsetpix)/4;
        changeVideoSize(1,-scaledsize/2,0,width2,height2-offsetpix,mainView);

        changeVideoSize(1,0,-offsetpix/2+10,scaledsize,scaledsize,paly0);
        changeVideoSize(1,0,0,scaledsize,scaledsize,paly1);
        changeVideoSize(1,0,0,scaledsize,scaledsize,paly2);
        changeVideoSize(1,0,0,scaledsize,scaledsize,paly3);



        // 开启语音接收和推送
        String loadaudio = "faudio://"+
                App.gAudioServerIP+"/"
                +App.gRoomID+"/"
                +App.gPuhserID;
        Meeting.push(1,loadaudio);

        playeraudio.load("faudio://"+
                            App.gAudioServerIP+"/"
                            +App.gRoomID);
        playeraudio.play(Video.LayerBitAudio);

        Meeting.set_mic_state(Meeting.ON);



        exitmeet = (Button)findViewById(R.id.exitmeet);
        exitmeet.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(App.isRoomManager)
                {
                    final View v2 = v;
                    AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
                    //builder.setMessage(Msg_userName+"邀请您参加会议");
                    builder.setTitle("会议管理");

                    builder.setPositiveButton("邀请参会人", new AlertDialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            /* 使用Intent.ACTION_GET_CONTENT这个Action */
                            intent.setClass(v2.getContext(),InvitActivity.class);
                            /* 取得相片后返回本画面 */
                            startActivityForResult(intent, 1);
                        }
                    });

                    builder.setNegativeButton("结束会议", new AlertDialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            try {
                                TcpCompare.EndMeeting(App.instance.gRoomID,App.instance.gUserUid);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            RoomActivity.this.finish();
                        }
                    });

                    builder.create().show();
                }else {
                    try {
                        TcpCompare.LeaveSelf(App.instance.gRoomID,App.instance.gUserUid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    RoomActivity.this.finish();
                }



            }
        });

        invitUsers = (Button)findViewById(R.id.invitUsers);
        invitUsers.setTypeface(typeface);
        invitUsers.setText(R.string.invitopen);
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
        startCamera.setTypeface(typeface);
        startCamera.setText(R.string.videoopen);
        startCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(localCamerastate == false)
                {
                    localCamerastate=true;
                    cameraView.setVisibility(View.VISIBLE);

                    RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams)cameraView.getLayoutParams();
                    params3.width =1;
                    params3.height =1;
                    cameraView.setLayoutParams(params3);

                    Video.set_source(Video.source_camera);

                    startCamera.setText(R.string.videoclose);

                    // 开启视频推送
                    String loadurl = "fvideo://"+
                            App.instance.gVideoServerIP+"/"
                            +App.instance.gPuhserID;
                    Meeting.push(0,loadurl);
                    mainView.load(loadurl);
                    mainView.play(Video.LayerBitVideoMedium);
                }
                else
                {
                    Video.set_source(Video.source_none);
                    cameraView.setVisibility(View.INVISIBLE);

                    localCamerastate=false;
                    startCamera.setText(R.string.videoopen);
                }
            }
        });

        nextpagebt = (Button)findViewById(R.id.nextpagebt);
        nextpagebt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int totalUsers = App.instance.gUserList.size();
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


        micopenclose = (Button)findViewById(R.id.micopenclose);
        micopenclose.setTypeface(typeface);
        //设置图标(对应上面的点赞图标)(Unicode编码)
        micopenclose.setText(R.string.micopen);
        micopenclose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(localMicstate==false)
                {
                    localMicstate=true;
                    Meeting.set_mic_state(Meeting.OFF);
                    micopenclose.setText(R.string.micclose);
                }
                else
                {
                    localMicstate=false;
                    Meeting.set_mic_state(Meeting.ON);
                    micopenclose.setText(R.string.micopen);
                }
            }
        });

        startDesktop = (Button)findViewById(R.id.startDesktop);
        startDesktop.setTypeface(typeface);
        startDesktop.setText(R.string.desktopopen);
        startDesktop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                cameraView.setVisibility(View.INVISIBLE);
                Video.set_source(Video.source_screen);
                startScreenCapture();

                Meeting.set_mic_state(Meeting.ON);

                //RoomActivity.this.moveTaskToBack(true);
            }
        });
        meetidTitle.setText("会议号码:"+String.valueOf(App.instance.gRoomID));

        if(!App.isRoomManager)
        {
            exitmeet.setText("退出会议");
        }

        getUserListInMeeting(App.instance.gRoomID);
    }

    public void getUserListInMeeting(String meetid)
    {
        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        // Serialization
        QSUser user = new QSUser(-2,"","","",
                "","","",meetid,"");
        Gson gson = new Gson();
        String loginparam = gson.toJson(user);

        //MediaType  设置Content-Type 标头中包含的媒体类型值
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , loginparam);

        Request request = new Request.Builder()
                .url("http://"+App.gMeetServerIP+":8080"+"/GetUsersInMeeting")//请求的url
                .post(requestBody)
                .build();

        //创建/Call
        Call call = okHttpClient.newCall(request);
        //加入队列 异步操作
        call.enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
//                LoginActivity.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final JsonObject myJsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();

                JsonArray datas = myJsonObject.getAsJsonArray("members");
                if (datas.size() >=0)
                {
                    App.gUserList.clear();
                    for (int i=0;i<datas.size();i++)
                    {
                        App.gUserList.addElement(datas.get(i).getAsJsonObject());
                    }
                    if (App.roomactive != null)
                        App.roomactive.runOnUiThread(new Runnable() {
                            public void run() {
                                App.roomactive.FlushVideos();
                            }
                        });
                }
                else
                {
//                    LoginActivity.this.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }

            }
        });
    }

    public void ChangePlayer(int indexadd)
    {
        int totalUsers = App.instance.gUserList.size();
        int currentindex = videoIndex+indexadd;
        if(currentindex < totalUsers)
        {
            JsonObject obj = App.gUserList.elementAt(currentindex);
            String pushid = String.valueOf(obj.get("index").getAsInt());

            String loadurl = "fvideo://"+
                    App.gVideoServerIP+"/"
                    +pushid;

            mainView.load(loadurl);
        }
    }

    public void FlushVideos()
    {
        for (int i=videoIndex;i<App.gUserList.size();i++)
        {
            JsonObject obj = App.instance.gUserList.elementAt(i);
            String pushid = String.valueOf(obj.get("index").getAsInt());

            String loadurl = "fvideo://" +
                    App.gVideoServerIP + "/"
                    + pushid;

            switch (i - videoIndex)
            {
                case 0:
                {
                    paly0.load(loadurl);
                }
                break;
                case 1:
                {
                    paly1.load(loadurl);
                }
                break;
                case 2:
                {
                    paly2.load(loadurl);
                }
                break;
                case 3:
                {
                    paly3.load(loadurl);
                }
                break;
            }
        }
    }

    public void setScreenMode(String uid) {

//        for (int i=0;i<App.getInstance().gUserList.size();i++) {
//            JsonObject obj = App.getInstance().gUserList.elementAt(i);
//            String useruid = obj.get("Msg_useruid").getAsString();
//
//            if (uid.equals(useruid))
//            {
//                String pushid = obj.get("Msg_userpushid").getAsString();
//                String loadurl = "fvideo://" +
//                        App.getInstance().gStreamServer + "/"
//                        + pushid;
//
//                mainView.load(loadurl);
//            }
//        }
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

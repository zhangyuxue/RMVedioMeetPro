package workvideo.fvideo;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

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
import workvideo.meetingSdk.*;
import workvideo.meetingSdk.media.Sound;
import workvideo.meetingSdk.media.Video;
import workvideo.fvideo.net.TcpCenter;

public class App extends Application
{
    public static final String TAG ="meeting.debug";

    public static App instance =null;

    public static Boolean isRoomManager=false;
    public static Boolean isUserManager=false;
    public static String gVideoServerIP;
    public static String gAudioServerIP;
    public static String gMeetServerIP;
    public static String gUserUid;
    public static String gRoomID;
    public static String gPuhserID;
    public static Vector<JsonObject> gUserList;
    public static MainActivity mainactive=null;
    public static RoomActivity roomactive=null;
    public static int startType=0;// 0 jion 1 start 2 will


    @Override
    public void onCreate()
    {
        super.onCreate();

        gUserList = new Vector<>();

        instance =this;

        gVideoServerIP="";
        gAudioServerIP="";
        gMeetServerIP="";
        gUserUid="";
        gRoomID="";
        gPuhserID="";
        // 启动SDK
        Meeting.startup();

        // 初始化声音
        Sound.instance.Init(this);

        // 设置空白视频画面
       Video.set_blank(getAssets(), "blank");

        TcpCenter.sharedCenter().setDisconnectedCallback(new TcpCenter.OnServerDisconnectedCallbackBlock() {
            @Override
            public void callback(IOException e) {
                //textView_receive.setText(textView_receive.getText().toString() + "断开连接" + "\n");
                //
                //"链接断开"

            }
        });
        TcpCenter.sharedCenter().setConnectedCallback(new TcpCenter.OnServerConnectedCallbackBlock() {
            @Override
            public void callback() {
                //"连接成功"
                try {
                    TcpCompare.login(gUserUid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        TcpCenter.sharedCenter().setReceivedCallback(new TcpCenter.OnReceiveCallbackBlock() {
            @Override
            public void callback(final String receicedMessage) {
                //textView_receive.setText(textView_receive.getText().toString() + receicedMessage + "\n");
                final String gotStr = receicedMessage;
                final JsonObject myJsonObject = new JsonParser().parse(receicedMessage).getAsJsonObject();

                String msgtype = myJsonObject.get("Msg_type").getAsString();
                String msguid = myJsonObject.get("Msg_useruid").getAsString();
                String msgmeetid = myJsonObject.get("Msg_meetID").getAsString();
                if(msgtype.equals("InvitUsers")) {
                    App.mainactive.runOnUiThread(new Runnable() {
                        public void run() {
                            if (roomactive == null)
                                mainactive.showInvitDialog(myJsonObject);
                        }
                    });
                }
                if(msgtype.equals("TickRoom")) {
                    if(!msgmeetid.equals(App.instance.gRoomID))
                        return;
                    App.mainactive.runOnUiThread(new Runnable() {
                        public void run() {
                            if (roomactive != null)
                                roomactive.finish();
                        }
                    });
                }


                if (msguid.equals(App.instance.gUserUid))
                {
                    if(msgtype.equals("EndRoom")) {
                        App.mainactive.runOnUiThread(new Runnable() {
                            public void run() {
                                if (roomactive != null)
                                    roomactive.finish();
                            }
                        });
                    }
                    return;
                }

                if(msgtype.equals("JionMeet")) {
                    App.mainactive.runOnUiThread(new Runnable() {
                        public void run() {
                            if (roomactive != null)
                                roomactive.getUserListInMeeting(App.instance.gRoomID);
                        }
                    });

                }else if(msgtype.equals("LeftMeet")) {
                    App.mainactive.runOnUiThread(new Runnable() {
                        public void run() {
                            if (roomactive != null)
                                roomactive.getUserListInMeeting(App.instance.gRoomID);
                        }
                    });
                }else {
                }



            }
        });
    }

    void onExit()
    {
        Meeting.cleanup();

        Log.w(TAG, "onExit()");

        System.exit(0);
    }

    public void socketConnect(String ip,int port) {
        TcpCenter.sharedCenter().connect(ip,port);
    }

    public void socketDisconnect() {
        TcpCenter.sharedCenter().disconnect();
    }


}


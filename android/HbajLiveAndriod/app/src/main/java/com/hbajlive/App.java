package com.hbajlive;


import android.app.Application;
import android.content.Intent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbajlive.net.TcpCenter;
import com.hbajlive.net.TcpCompare;

import java.io.IOException;
import java.util.Vector;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.*;

import workvideo.meetingSdk.Meeting;
import workvideo.meetingSdk.media.Sound;
import workvideo.meetingSdk.media.Video;

public class App extends Application {

    private static App instance;

    private TcpCenter m_socket;
    LoginActivity activelogin=null;
    MainActivity  actiiveMain=null;
    MeetActivity  actiiveMeet=null;
    InvitActivity  actiiveInvit=null;

    public static String gUserUID;
    public static String gUserName;
    public static String gUserPusherID;
    public static String gWorkServer;
    public static String gStreamServer;
    public static String gUserLevel;
    public static String gMeetingName;

    Vector<JsonObject> gUserList;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        gUserList = new Vector<>();
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
                if(actiiveMain == null)
                {
                    activelogin.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activelogin, "链接断开", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else
                {
                    actiiveMain.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(actiiveMain, "链接断开", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
        TcpCenter.sharedCenter().setConnectedCallback(new TcpCenter.OnServerConnectedCallbackBlock() {
            @Override
            public void callback() {
                activelogin.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activelogin, "连接成功", Toast.LENGTH_SHORT).show();
                    }
                });

                try {
                    TcpCompare.sharedCenter().loginAndgetPusherID(gUserName,gUserLevel,gUserUID);
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
                final JsonObject myJsonObject = new JsonParser().parse(receicedMessage).getAsJsonObject();;
                String msgtype = myJsonObject.get("Msg_type").getAsString();
                if(msgtype.equals("login")) {
                    activelogin.runOnUiThread(new Runnable() {
                        public void run() {
                           gUserPusherID = myJsonObject.get("Msg_userpushid").getAsString();
                           activelogin.setLoginMode();
                        }
                    });
                }else if(msgtype.equals("CreateMeet")) {
                    if(actiiveMeet != null)
                        actiiveMeet.runOnUiThread(new Runnable() {
                            public void run() {
                                     actiiveMeet.showMainActive();
                        }
                    });
                }else if(msgtype.equals("JionMeet")) {
                    if (actiiveMain == null)
                    {
                        actiiveMeet.runOnUiThread(new Runnable() {
                            public void run() {

                                Boolean addif=true;
                                for (int i=0;i<gUserList.size();i++)
                                {
                                    JsonObject obj = gUserList.elementAt(i);
                                    String useruid = myJsonObject.get("Msg_useruid").getAsString();
                                    if(obj.get("Msg_useruid").getAsString().equals(useruid))
                                    {
                                        addif=false;
                                        break;
                                    }
                                }
                                if(addif)
                                {
                                    gUserList.addElement(myJsonObject);
                                    try {
                                        TcpCompare.sharedCenter().jionMeeting(
                                                myJsonObject.get("Msg_JionMeetID").getAsString(),
                                                gUserName,
                                                App.getInstance().gUserUID,
                                                App.getInstance().gUserPusherID,
                                                myJsonObject.get("Msg_meetName").getAsString(),
                                                App.getInstance().gUserLevel);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                actiiveMeet.showMainActive();

                            }
                        });
                    }
                    else
                    {
                        actiiveMain.runOnUiThread(new Runnable() {
                            public void run() {

                                Boolean addif=true;
                                for (int i=0;i<gUserList.size();i++)
                                {
                                    JsonObject obj = gUserList.elementAt(i);
                                    String useruid = myJsonObject.get("Msg_useruid").getAsString();
                                    if(obj.get("Msg_useruid").getAsString().equals(useruid))
                                    {
                                        addif=false;
                                        break;
                                    }
                                }
                                if(addif)
                                {
                                    gUserList.addElement(myJsonObject);
                                    try {
                                        TcpCompare.sharedCenter().jionMeeting(
                                                myJsonObject.get("Msg_JionMeetID").getAsString(),
                                                gUserName,
                                                App.getInstance().gUserUID,
                                                App.getInstance().gUserPusherID,
                                                myJsonObject.get("Msg_meetName").getAsString(),
                                                App.getInstance().gUserLevel);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                actiiveMain.FlushVideos();
                            }
                        });
                    }

                }else if(msgtype.equals("LeavMeeting"))
                {
                    Boolean addif=true;
                    int removindex=-1;
                    for (int i=0;i<gUserList.size();i++)
                    {
                        JsonObject obj = gUserList.elementAt(i);
                        String useruid = myJsonObject.get("Msg_useruid").getAsString();
                        if(obj.get("Msg_useruid").getAsString().equals(useruid))
                        {
                            removindex=i;
                            break;
                        }
                    }
                    if(removindex != -1)
                        gUserList.remove(removindex);
                    actiiveMain.FlushVideos();
                }else if(msgtype.equals("GetMeetingList"))
                {
                    final JsonArray meetlist = myJsonObject.get("Msg_meetlist").getAsJsonArray();

                    if (actiiveMeet != null)
                    {
                        actiiveMeet.runOnUiThread(new Runnable() {
                                                      public void run() {
                                                          actiiveMeet.initMeetingListView(meetlist);
                                                      }
                                                  });

                    }
                }else if(msgtype.equals("GetUserList"))
                {
                    final JsonArray userlist = myJsonObject.get("Msg_userlist").getAsJsonArray();

                    if (actiiveInvit != null)
                    {
                        actiiveInvit.runOnUiThread(new Runnable() {
                            public void run() {
                                actiiveInvit.initUserListView(userlist);
                            }
                        });

                    }
                }

            }
        });
    }

    public static App getInstance(){
        return instance;
    }


    public void socketConnect(String ip,int port) {
        TcpCenter.sharedCenter().connect(ip,port);
    }

    public void socketDisconnect() {
        TcpCenter.sharedCenter().disconnect();
    }

}
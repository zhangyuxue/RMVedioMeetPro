package com.hbajlive.net;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by shensky on 2018/1/15.
 */

public class TcpCompare {
    private static TcpCompare instance;
    private static final String TAG = "TcpCompare";
//    构造函数私有化
    private TcpCompare() {
        super();
        //dataTemps=new byte[2048];
        //zeroDataTemps();
    }

    /*
    *   数据部分
    * */

//    private byte[] dataTemps;
//    public void zeroDataTemps()
//    {
//        for (int i=0;i<2048;i++)
//            dataTemps[i]=0;
//    }

    public void loginAndgetPusherID(String nickName,String nickLevel,String uid) throws JSONException {
        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","login");
        root.put("Msg_userName",nickName);
        root.put("Msg_userlevel",nickLevel);
        root.put("Msg_useruid",uid);

        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
//        zeroDataTemps();
//        for (int i=0;i<rootStr.length();i++)
//        {
//            dataTemps[i]=byteArray[i];
//        }
        byte[] dataTemps=new byte[rootStr.length()+1];
        for (int i=0;i<rootStr.length();i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[rootStr.length()]='\n';
        TcpCenter.sharedCenter().send(dataTemps);

    }


    public void createMeeting(String meetID,
                              String userNick,
                              String userUID,
                              String userPushID,
                              String meetName,
                              String nickLevel)
            throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","CreateMeet");
        root.put("Msg_CreateMeetID",meetID);
        root.put("Msg_useruid",userUID);
        root.put("Msg_userName",userNick);
        root.put("Msg_userpushid",userPushID);
        root.put("Msg_userlevel",nickLevel);
        root.put("Msg_meetName",meetName);

        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[rootStr.length()+1];
        for (int i=0;i<rootStr.length();i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[rootStr.length()]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

    public void jionMeeting(String meetID,
                              String userNick,
                            String userUID,
                            String userPushID,
                            String meetName,
                            String nickLevel)
            throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","JionMeet");
        root.put("Msg_JionMeetID",meetID);
        root.put("Msg_useruid",userUID);
        root.put("Msg_userName",userNick);
        root.put("Msg_userpushid",userPushID);
        root.put("Msg_userlevel",nickLevel);
        root.put("Msg_meetName",meetName);

        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[rootStr.length()+1];
        for (int i=0;i<rootStr.length();i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[rootStr.length()]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }


    public void getMeetingList()throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","GetMeetingList");


        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[rootStr.length()+1];
        for (int i=0;i<rootStr.length();i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[rootStr.length()]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

    public void getUserList()throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","GetUserList");


        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[rootStr.length()+1];
        for (int i=0;i<rootStr.length();i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[rootStr.length()]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }


//    提供一个全局的静态方法
    public static TcpCompare sharedCenter() {
        if (instance == null) {
            synchronized (TcpCenter.class) {
                if (instance == null) {
                    instance = new TcpCompare();
                }
            }
        }
        return instance;
    }

}

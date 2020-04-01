package workvideo.fvideo.net;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by shensky on 2018/1/15.
 */

public class TcpCompare {


    static public void login(String uid) throws JSONException {
        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","Login");
        root.put("Msg_useruid",uid);

        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[byteArray.length+1];
        for (int i=0;i<byteArray.length;i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[byteArray.length]='\n';
        TcpCenter.sharedCenter().send(dataTemps);

    }

    static public void tickUsers(Vector<String> invitlist)
            throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","TickRoom");

        JSONArray list = new JSONArray();
        for (int i=0;i<invitlist.size();i++)
        {
            list.put(invitlist.get(i).toString());
        }


        root.put("Msg_invitlist",list);

        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[byteArray.length+1];
        for (int i=0;i<byteArray.length;i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[byteArray.length]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

    static public void invitUsers(Vector<String> invitlist)
            throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","InvitUsers");

        JSONArray list = new JSONArray();
        for (int i=0;i<invitlist.size();i++)
        {
            list.put(invitlist.get(i).toString());
        }


        root.put("Msg_invitlist",list);

        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[byteArray.length+1];
        for (int i=0;i<byteArray.length;i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[byteArray.length]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

    static public void LeaveSelf(String meetID,
                                 String userUID)
            throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","LeftMeet");
        root.put("Msg_meetID",meetID);
        root.put("Msg_useruid",userUID);


        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[byteArray.length+1];
        for (int i=0;i<byteArray.length;i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[byteArray.length]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

    static public void EndMeeting(String meetID,
                                   String userUID)
            throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","EndRoom");
        root.put("Msg_meetID",meetID);
        root.put("Msg_useruid",userUID);


        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[byteArray.length+1];
        for (int i=0;i<byteArray.length;i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[byteArray.length]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

    static public void jionMeeting(String meetID,
                            String userUID)
            throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","JionMeet");
        root.put("Msg_meetID",meetID);
        root.put("Msg_useruid",userUID);


        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[byteArray.length+1];
        for (int i=0;i<byteArray.length;i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[byteArray.length]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

    static public void setSreenMode(String uid)throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","SetScreenMode");
        root.put("Msg_useruid",uid);

        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[byteArray.length+1];
        for (int i=0;i<byteArray.length;i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[byteArray.length]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

    static public void getMeetingList()throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","GetMeetingList");


        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[byteArray.length+1];
        for (int i=0;i<byteArray.length;i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[byteArray.length]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

    static public void getUserList()throws JSONException {

        JSONObject root =new JSONObject();
        //msg_type:"login";
        root.put("Msg_type","GetUserList");


        String rootStr = root.toString();
        byte[] byteArray = rootStr.getBytes();
        byte[] dataTemps=new byte[byteArray.length+1];
        for (int i=0;i<byteArray.length;i++)
        {
            dataTemps[i]=byteArray[i];
        }
        dataTemps[byteArray.length]='\n';
        TcpCenter.sharedCenter().send(dataTemps);
    }

}

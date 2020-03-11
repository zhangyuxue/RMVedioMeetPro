package workvideo.meeting;

import android.app.Application;
import android.util.ArraySet;
import android.util.Log;

import workvideo.meetingSdk.*;
import workvideo.meetingSdk.media.Sound;
import workvideo.meetingSdk.media.Video;

public class App extends Application
{
    public static final String TAG ="meeting.debug";

    static App instance =null;

    MainActivity mainActivity =null;
    RoomActivity roomActivity =null;
    LobbyActivity lobbyActivity =null;

    long user_id =Meeting.empty_id;
    long room_id =Meeting.empty_id;

    String push_server =null;

    long[] channels = {0,0,0,0,0};

    UserList userList = new UserList();

    // 事件处理，在主线程中
    class Callback extends EventLoop
    {
        @Override
        protected void onLoginSuccess(long uid)
        {
            Log.w(TAG, "onLoginSuccess: "+uid);
            user_id =uid;
            mainActivity.onLoginSuccess();
        }

        @Override
        protected void onLoginFailed(long uid, int error)
        {
            Log.w(TAG, "onLoginFailed: uid="+uid + " error="+error);
            user_id =Meeting.empty_id;
            mainActivity.onLoginError(error);
        }

        @Override
        protected void onOffline() {
            user_id =Meeting.empty_id;
            room_id =Meeting.empty_id;

            if (roomActivity!=null)
                roomActivity.onOffLine();

            if (lobbyActivity!=null)
                lobbyActivity.onOffline();

            if (mainActivity!=null)
                mainActivity.onOffline();
        }

        @Override
        protected void onText(long from, long to, String body)
        {
            // 收到文字消息
            Log.w(TAG, "onText: from="+from+" to="+to);
        }

        @Override
        protected void onEcho(int sequence, long clock)
        {
            // 收到延迟探测返回
            Log.w(TAG, "onEcho");
        }

        @Override
        protected void onEnterRoom(long roomid)
        {
            room_id = roomid;
            if(lobbyActivity!=null)
                lobbyActivity.onEnterSuccess();
        }

        @Override
        protected void onEnterRoomFailed(long roomid, int error) {
            room_id =Meeting.empty_id;
            if(lobbyActivity!=null)
                lobbyActivity.onEnterError(error);
        }

        @Override
        protected void onUserRecord(int op, long uid, String uri)
        {
            // 用户列表改变 (增/删/改 记录)
            switch(op)
            {
                case Event.record_op_append:
                case Event.record_op_modify:
                    userList.add(uid,uri);
                    break;
                case Event.record_op_delete:
                    userList.remove(uid);
                    break;
            }
        }

        @Override
        protected void onChannelList(long[] list) {
            // 通道改变
            assert(list.length == Meeting.MAX_CHANNELS);
            for(int i=0; i<Meeting.MAX_CHANNELS; i++)
                channels[i] =list[i];

            if(roomActivity!=null)
                roomActivity.onChannelUpdate();
        }

        @Override
        protected void onIdle()
        {
            if (HasActivity())
            {
                if(roomActivity!=null)
                    roomActivity.onTimer();
            }
            else {
                onExit();
            }
        }
    }

    Callback mCallback;

    boolean HasActivity()
    {
        return mainActivity!=null ||
                roomActivity!=null ||
                lobbyActivity!=null ;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        instance =this;

        // 启动SDK
        Meeting.startup();

        // 初始化声音
        Sound.instance.Init(this);

        // 设置空白视频画面
        Video.set_blank(getAssets(), "blank");

        // 启动事件处理线程
        mCallback =new Callback();
        mCallback.startLoop();
    }

    void onExit()
    {
        mCallback.endLoop();

        // 如果未离线，则先离线
        if (user_id!=0)
        {
            Meeting.login(null,0,null);
            try {
                Thread.sleep(500); //等待一会，让底层有时间把注销请求发出去
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 释放SDK
        Meeting.cleanup();

        Log.w(TAG, "onExit()");

        System.exit(0);
    }
}


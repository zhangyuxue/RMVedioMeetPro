package workvideo.fvideo;

import android.app.Application;
import android.util.Log;

import workvideo.meetingSdk.*;
import workvideo.meetingSdk.media.Sound;
import workvideo.meetingSdk.media.Video;

public class App extends Application
{
    public static final String TAG ="meeting.debug";

    static App instance =null;


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
    }

    void onExit()
    {
        Meeting.cleanup();

        Log.w(TAG, "onExit()");

        System.exit(0);
    }
}



//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk.media;

import android.content.Context;
import android.os.Build;
//import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.RequiresApi;

public class PlayerView extends SurfaceView implements SurfaceHolder.Callback
{
    long mHandle =0;

    public PlayerView(Context context) {
        super(context);
        Init();
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Init();
    }

    void Init()
    {
        setZOrderMediaOverlay(true);
        this.getHolder().addCallback(this);

        mHandle =Video.player_create();
    }

    // 释放对象
    // 切记要调用，否则会内存泄露
    public void release()
    {
        Video.player_destroy(mHandle);
        mHandle =0;
    }

    // 载入信号
    public boolean load(String uri)
    {
        return Video.player_load(mHandle, uri);
    }

    // 播放
    public boolean play(int mask)
    {
        return Video.player_play(mHandle, mask);
    }

    public void stop()
    {
        Video.player_play(mHandle,0);
    }

    // 返回分辨率
    public int resolution()
    {
        return Video.player_get_resolution(mHandle);
    }

    // 返回声波指示
    public void sound_wave(byte[] wave)
    {
        Video.player_get_wave(mHandle, wave);
    }

    // 音量控制
    public int volume()
    {
        return Video.player_get_volume(mHandle);
    }
    public void set_volume(int vol)
    {
        Video.player_set_volume(mHandle, vol);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Video.player_set_surface(mHandle, surfaceHolder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Video.player_set_surface(mHandle, null);
    }
}

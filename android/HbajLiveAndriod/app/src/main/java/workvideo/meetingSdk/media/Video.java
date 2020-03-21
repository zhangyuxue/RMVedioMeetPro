
//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk.media;

import android.content.res.AssetManager;
import android.view.Surface;

import java.nio.ByteBuffer;

public class Video
{
    // 设置空白画面
    public static native boolean set_blank(AssetManager asset, String path);

    // 摄像头输出
    static native void on_camera_output(byte[] frame, int resolution, int rotation);

    // 屏幕输出
    static native void on_screen_output(ByteBuffer data, int width, int height, int stride);


    // 切换视频发送源
    public static native void set_source(int src);
    public static final int
            source_none =0,
            source_camera =1,
            source_screen =2;

    // 创建播放器
    static native long player_create();

    // 删除播放器
    static native void player_destroy(long handle);

    // 设置窗口
    static native void player_set_surface(long handle, Surface sf);

    // 载入信号
    static native boolean player_load(long handle, String uri);

    // 播放
    // mask: 层掩码
    static native boolean player_play(long handle, int mask);

    // 层
    public static final int
            LayerBitAudio           =(1<<8),  //语音
            LayerBitVideoHighest    =(1<<4),   //最高清视频
            LayerBitVideoHigh       =(1<<3),
            LayerBitVideoMedium     =(1<<2),
            LayerBitVideoLow        =(1<<1),
            LayerBitVideoLowest     =(1)
                    ;

    // 返回分辨率
    static native int player_get_resolution(long handle);

    // 返回声音波形
    static native void player_get_wave(long handle, byte[] wave);

    // 设置音量
    static native void player_set_volume(long handle, int volume);

    // 返回音量
    static native int player_get_volume(long handle);

    // 返回源 （一个信号可能包含多个源，例如混合后的语音）
    // 返回值为源数量, list[] 将被填入源ID
    static native int player_get_source(long handle, long[] list);
}

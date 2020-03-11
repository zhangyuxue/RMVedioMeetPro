
//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk;

public class Meeting
{
    static {
        System.loadLibrary("Meeting");
    }

    // 启动/关闭 SDK
    public static native boolean startup();
    public static native void cleanup();

    // 登录
    // server 是服务器地址，格式如 localhost:10003 or 192.168.0.22:8080 or [ipv6]:9999
    // uid 是用户唯一标记，可以是手机号，或身份证号，或其他数字标示
    // passwd 最少4个符号
    public static native boolean login(String server, long uid, String passwd);

    // 发送文字消息
    // to: 目标用户ID
    public static native boolean send_text(long to, String msg);

    // 发送ECHO，服务端收到后原样返回，该功能用于分隔不同的请求，以及测试传输延迟
    public static native boolean send_echo(int seq, long clock);

    // 进入房间
    // 密码可以为null
    // isOwner 是否管理员
    public static native boolean enter_room(long roomid, String passwd, boolean isOwner);

    // 常量
    public static final int
            MAX_CHANNELS =5,   //最大通道数 (一个房间内的同时发言人数)
            ON =1,
            OFF =0
            ;

    public static final long empty_id =0; // 空ID （用户或房间）

    // 开启/关闭 麦克风
    public static native void set_mic_state(int state);
    public static native int mic_state(); // 返回: 0(关闭) 1(开启)

    // 开始/关闭 推送
    // uri: "fvideo://ip:port/uid"
    // uri=null 则关闭推送
    public static native void push_to(String uri);
}


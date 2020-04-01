
//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk;

public class Admin
{
    // 设置发言者
    public static native void set_speaker(long[] list);

    // 设置邀请列表
    public static native void set_invite(long[] list, int count);

    // 设置房间属性
    // passwd 可以为null
    public static native void set_room_flag(int flag, String passwd);

    // 设置发言模式
    public static native void set_speak_mode(int mode);

    // 属性定义
    public static final int
        room_flag_passwd_require =(1),  //需要密码
        room_flag_invite_mode =(2)     //邀请模式
                ;

    // 发言模式
    public static final int
        speak_mode_queuing =(0),    //简单排队模式
        speak_mode_hosting =(1),    //主持模式
        speak_mode_activity =(2)    //活动检测模式
                ;
}

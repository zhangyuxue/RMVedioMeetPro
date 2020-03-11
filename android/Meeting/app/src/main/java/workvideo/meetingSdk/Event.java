
//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk;

public class Event {

    public long mHandle;

    public Event(long handle)
    {
        mHandle =handle;
    }

    public void release()
    {
        native_free(mHandle);
        mHandle =0;
    }

    public int getWhat()
    {
        return native_get_what(mHandle);
    }

    // 返回错误号
    public int getError()
    {
        return native_get_error(mHandle);
    }

    // 返回用户ID
    public long getUserId()
    {
        return native_read_uid(mHandle);
    }

    // 返回房间ID
    public long getRoomId()
    {
        return native_get_roomid(mHandle);
    }

    // 记录操作 (UserRecord.op)
    public static final int
        record_op_append = (0), //增加记录
        record_op_delete = (1), //删除记录
        record_op_modify =(2)   //修改记录
                ;

    // 文字消息
    public class Text
    {
        long from, to;  //来源和目的
        String body;    //消息内容
    }

    // 返回文字消息
    public Text getText()
    {
        Text t=new Text();
        long[] uid = {0,0};
        t.body =native_get_text(mHandle, uid);
        t.from =uid[0];
        t.to =uid[1];
        return t;
    }

    // 回声
    public class Echo
    {
        int seq;
        long clock;
    }

    // 返回回声
    public Echo getEcho()
    {
        Echo ec =new Echo();
        long[] clock ={0};
        ec.seq =native_get_echo(mHandle, clock);
        ec.clock =clock[0];
        return ec;
    }

    // 返回通道列表
    public long[] getChannelList()
    {
        return native_get_channel_list(mHandle);
    }

    static native long native_poll();
    static native boolean native_wait(int timeo);
    static native void native_free(long handle);

    static native int native_get_what(long event);
    static native int native_get_error(long event);
    static native long native_get_roomid(long event);
    static native long[] native_get_channel_list(long event);

    static native String native_get_text(long event, long[] from_to);
    static native int native_get_echo(long event, long[] clock);

    public static native long native_read_user(long event, long pos);
    public static native String native_read_uri(long event);
    public static native long native_read_uid(long event);
    public static native int native_read_op(long event);
}

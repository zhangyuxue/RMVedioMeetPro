
//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import workvideo.meetingSdk.media.Sound;

public abstract class EventLoop
{
    Handler mHandler;
    Thread mThread;
    volatile boolean mRunning =false;

    protected EventLoop()
    {
        mHandler =new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                if(msg.what==0) {
                    Event ev = (Event) msg.obj;
                    process(ev);
                    ev.release();
                }
                else if(msg.what==1) {
                    onIdle();
                }
            }
        };
        mThread =new Thread() {
            @Override
            public void run() {
                super.run();

                while(mRunning)
                {
                    long ev =Event.native_poll();
                    if (ev==0) {
                        checkSound();
                        if (!mHandler.hasMessages(1))
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                        Event.native_wait(100);
                        continue;
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(0,new Event(ev)));
                }
                Sound.instance.stop();
            }
        };
    }

    void checkSound()
    {
        boolean hasJobs =Sound.have_jobs();
        if (hasJobs == Sound.instance.isRunning())
            return;
        if (hasJobs) {
            Sound.instance.run();
        } else {
            Sound.instance.stop();
        }
    }

    protected abstract void onLoginSuccess(long uid);
    protected abstract void onLoginFailed(long uid, int error);
    protected abstract void onOffline();
    protected abstract void onText(long from, long to, String msg);
    protected abstract void onEcho(int sequence, long clock);
    protected abstract void onEnterRoom(long roomid);
    protected abstract void onEnterRoomFailed(long roomid, int error);
    protected abstract void onUserRecord(int op, long uid, String uri);
    protected abstract void onChannelList(long[] list);
    protected abstract void onIdle();

    void process(Event event)
    {
        switch(event.getWhat())
        {
            case 10:
                onLoginSuccess(event.getUserId());
                return;

            case 11:
                onLoginFailed(event.getUserId(), event.getError());
                return;

            case 12:
                onOffline();
                return;

            case 20:
                {
                    Event.Text t  = event.getText();
                    onText(t.from, t.to, t.body);
                }
                return;

            case 21:
                {
                    Event.Echo echo =event.getEcho();
                    onEcho(echo.seq, echo.clock);
                }
                return;

            case 30:
                onEnterRoom(event.getRoomId());
                return;

            case 31:
                onEnterRoomFailed(event.getRoomId(), event.getError());
                return;

            case 32:
                {
                    long pos =0;
                    long handle =event.mHandle;
                    while (true)
                    {
                        pos = Event.native_read_user(handle, pos);
                        if (0==pos)
                            break;

                        long uid =Event.native_read_uid(handle);
                        String uri =Event.native_read_uri(handle);
                        onUserRecord(Event.record_op_append, uid, uri);
                    }
                }
                return;

            case 33:
                {
                    int op =Event.native_read_op(event.mHandle);
                    if(op >=0) {
                        long uid =Event.native_read_uid(event.mHandle);
                        String uri =Event.native_read_uri(event.mHandle);
                        onUserRecord(op, uid, uri);
                    }
                }
                return;

            case 34:
                onChannelList(event.getChannelList());
                return;
        }
    }

    // 启动事件循环
    public void startLoop()
    {
        if(!mRunning){
            mRunning=true;
            mThread.start();
        }
    }

    // 退出事件循环
    public void endLoop()
    {
        if(mRunning){
            mRunning=false;
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

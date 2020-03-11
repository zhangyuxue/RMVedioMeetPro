package workvideo.meeting;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import workvideo.meetingSdk.*;
import workvideo.meetingSdk.media.*;

public class RoomActivity extends VideoActivity implements View.OnClickListener {

    // 通道
    class CHANNEL implements SeekBar.OnSeekBarChangeListener
    {
        static final int TitleBarCy =68, SeekBarCy =68,  MaxSeekBarCx =500, Pad=5;

        final int m_index;
        final PlayerView m_player;
        final TitleBar m_title;
        final SeekBar m_seek;
        final Rect m_bound = new Rect();
        final byte[] m_wave = {0,0,0,0,0,0,0,0,0,0,0,0};

        int m_resolution = 0;

        CHANNEL(PlayerView player, TitleBar title, SeekBar seek, int index)
        {
            m_index = index;
            m_player = player;
            m_title = title;
            m_seek = seek;

            m_player.setVisibility(View.INVISIBLE);
            m_seek.setVisibility(View.INVISIBLE);
            m_title.setVisibility(View.INVISIBLE);

            // 播放语音及视频
            player.play(Video.LayerBitAudio | Video.LayerBitVideoMedium);

            seek.setMax(100);
            seek.setPadding(0,0,0,0);
            seek.setOnSeekBarChangeListener(this);
        }

        // 载入URI
        // 目前只支持 fvideo 流
        boolean load(String uri)
        {
            return m_player.load(uri);
        }

        void release() {
            m_player.release();
        }

        void show(int left, int top, int right, int bottom)
        {
            // 载入流
            long uid =mSpeakerList[m_index];
            if(uid == App.instance.user_id)
                load(null); //是自己，卸载流
            else
                load( App.instance.userList.getUri(uid) );  //是远端流，需要载入

            // 设置标题
            m_title.setTitle(Long.toString(uid) );

            // 调整窗口
            left +=Pad;
            top +=Pad;
            right -=Pad;
            bottom -=Pad;

            if (m_bound.left==left && m_bound.top==top && m_bound.right==right && m_bound.bottom==bottom)
                return; // no change

            m_bound.set(left, top, right, bottom);
            applyBound();
            m_seek.setVisibility(View.VISIBLE);
            m_title.setVisibility(View.VISIBLE);
        }

        void applyBound()
        {
            // 显示视频
            Rect bound = new Rect();
            computeVideoBound(bound);
            if (mSendCh ==m_index) {
                m_player.setVisibility(View.INVISIBLE);
                setItemBound(mCam, bound);
                m_seek.setProgress(Sound.get_mic_volume());
            } else {
                setItemBound(m_player, bound);
                m_player.setVisibility(View.VISIBLE);
                m_seek.setProgress(m_player.volume());
            }
//            Log.w(App.TAG, "bound="+bound);

            // 显示标题和音量控制条
            Rect bound2 = new Rect(bound);
            bound2.bottom =bound.top;
            bound2.top = bound.top -TitleBarCy;
            setItemBound(m_title, bound2);

            bound2.top =bound.bottom;
            bound2.bottom =bound2.top + SeekBarCy;
            if (bound2.width() > MaxSeekBarCx)
                bound2.right =bound2.left +MaxSeekBarCx;
            setItemBound(m_seek, bound2);
        }

        void hide()
        {
            load(null);

            // 无信号则隐藏窗口
            m_bound.setEmpty();
            m_player.setVisibility(View.INVISIBLE);
            m_seek.setVisibility(View.INVISIBLE);
            m_title.setVisibility(View.INVISIBLE);
        }

        int resolution()
        {
            if (m_index ==mSendCh) {
                // 本通道作为发送通道，分辨率是摄像头的输出分辨率
                return mCam.displayResolution();
            } else {
                return m_player.resolution();
            }
        }

        void computeVideoBound(Rect vbound)
        {
            if (m_bound.isEmpty())
                return;

            m_resolution = resolution();
            int videoCx = (m_resolution >> 16);
            int videoCy = (m_resolution & 0xffff);
            if (videoCx < 1 || videoCy < 1) {
                videoCx =320;
                videoCy =240;
            }

            int maxCy = m_bound.height() - (TitleBarCy + SeekBarCy);
            int maxCx = m_bound.width();

            int cx = maxCx;
            int cy = videoCy * maxCx / videoCx;
            if (cy > maxCy) {
                cy = maxCy;
                cx = videoCx * maxCy / videoCy;
                assert (cx <= maxCx);
            }
            vbound.left =m_bound.left;
            vbound.top =m_bound.top + TitleBarCy;

            vbound.left += (maxCx - cx) >> 1;
            vbound.right = vbound.left + cx;
            vbound.top += (maxCy - cy) >> 1;
            vbound.bottom = vbound.top + cy;
        }

        void onTimer(long now)
        {
            if (mSpeakerList[m_index] == Meeting.empty_id)
                return;

            // 检查分辨率是否改变
            if (resolution() != m_resolution)
                applyBound();

            // 绘制标题和声波
            if (m_index ==mSendCh)
                Sound.get_mic_wave(m_wave);
            else
                m_player.sound_wave(m_wave);
            m_title.drawWave(m_wave);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b)
        {
            if (mSendCh ==m_index)
                Sound.set_mic_volume(i);
            else
                m_player.set_volume(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    CHANNEL[] mCH = {null, null, null, null, null};
    CameraView mCam;

    RelativeLayout mMenuBar;
    long mMenuTime = 0;
    Button mMenuButton;

    int mScreenCx = 0, mScreenCy = 0;

    long[] mSpeakerList;
    int mSendCh = -1; //发送通道，-1表示未发送
    int mSignalCount = 0; //正在发言的人数

    int mVideoSrc = Video.source_camera; //初始信号源为摄像头

    @Override
    protected void onSize(int width, int height)
    {
        // 窗口尺寸
        mScreenCx = width;
        mScreenCy = height;

        onChannelUpdate();
    }

    // 更新窗口边界
    void updateBounds()
    {
        mSendCh = -1;
        long myId = App.instance.user_id;

        int count = 0;
        int[] indexes = {-1, -1, -1, -1, -1};
        for (int i = 0; i < Meeting.MAX_CHANNELS; i++)
        {
            if (mSpeakerList[i] != Meeting.empty_id) {
                indexes[count++] = i;
                if (mSpeakerList[i] == myId)
                    mSendCh = i;
            } else {
                mCH[i].hide();
            }
        }
        mSignalCount = count;

        if (count == 0) {
            mMenuBar.setVisibility(View.VISIBLE);
            mMenuButton.setVisibility(View.INVISIBLE);

            mCH[0].show(0, 0, mScreenCx, mScreenCy);
            return;
        }

        if (count == 1) {
            mCH[indexes[0]].show(0, 0, mScreenCx, mScreenCy);
            return;
        }

        int cx, cy;
        if (mScreenCx < mScreenCy) {
            // 竖屏
            switch (count) {
                case 2:
                    cx = mScreenCx;
                    cy = mScreenCy / 2;
                    mCH[indexes[0]].show(0, 0, cx, cy);
                    mCH[indexes[1]].show(0, cy, cx, mScreenCy);
                    return;
                case 3:
                    cx = mScreenCx;
                    cy = mScreenCy / 3;
                    mCH[indexes[0]].show(0, 0, cx, cy);
                    mCH[indexes[1]].show(0, cy, cx, cy + cy);
                    mCH[indexes[2]].show(0, cy + cy, cx, mScreenCy);
                    return;
                case 4:
                    cx = mScreenCx / 2;
                    cy = mScreenCy / 2;
                    mCH[indexes[0]].show(0, 0, cx, cy);
                    mCH[indexes[1]].show(cx, 0, mScreenCx, cy);
                    mCH[indexes[2]].show(0, cy, cx, mScreenCy);
                    mCH[indexes[3]].show(cx, cy, mScreenCx, mScreenCy);
                    return;
                default:
                    cx = mScreenCx / 2;
                    cy = mScreenCy / 3;
                    mCH[0].show(0, 0, cx, cy);
                    mCH[1].show(cx, 0, mScreenCx, cy);
                    mCH[2].show(0, cy, cx, cy + cy);
                    mCH[3].show(cx, cy, mScreenCx, cy + cy);
                    mCH[4].show(0, cy + cy, cx, mScreenCy);
                    return;
            }
        } else {
            // 横屏
            switch (count) {
                case 2:
                    cx = mScreenCx / 2;
                    cy = mScreenCy;
                    mCH[indexes[0]].show(0, 0, cx, cy);
                    mCH[indexes[1]].show(cx, 0, mScreenCx, cy);
                    return;

                case 3:
                    cx = mScreenCx / 3;
                    cy = mScreenCy;
                    mCH[indexes[0]].show(0, 0, cx, cy);
                    mCH[indexes[1]].show(cx, 0, cx + cx, cy);
                    mCH[indexes[2]].show(cx + cx, 0, mScreenCx, cy);
                    return;

                case 4:
                    cx = mScreenCx / 2;
                    cy = mScreenCy / 2;
                    mCH[indexes[0]].show(0, 0, cx, cy);
                    mCH[indexes[1]].show(cx, 0, mScreenCx, cy);
                    mCH[indexes[2]].show(0, cy, cx, mScreenCy);
                    mCH[indexes[3]].show(cx, cy, mScreenCx, mScreenCy);
                    return;

                default:
                    cx = mScreenCx / 3;
                    cy = mScreenCy / 2;
                    mCH[0].show(0, 0, cx, cy);
                    mCH[1].show(cx, 0, cx + cx, cy);
                    mCH[2].show(cx + cx, 0, mScreenCx, cy);
                    mCH[3].show(0, cy, cx, mScreenCy);
                    mCH[4].show(cx, cy, cx+cx, mScreenCy);
                    return;
            }
        }
    }

    static void setItemBound(View view, Rect rect)
    {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();
        params.leftMargin =rect.left;
        params.topMargin =rect.top;
        params.width =rect.width();
        params.height =rect.height();
        view.setLayoutParams(params);
    }

    public void onChannelUpdate()
    {
        if (mScreenCx < 10 || mScreenCy <10)
            return;

        updateBounds();

        if (mSendCh ==-1)
            mCam.setVisibility(View.INVISIBLE);
        else
            mCam.setVisibility(View.VISIBLE);
    }

    // 离线了
    void onOffLine()
    {
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        App.instance.roomActivity =this;
        mSpeakerList =App.instance.channels;

        mCam =findViewById(R.id.cameraView);

        mMenuBar =findViewById(R.id.layoutMenu);
        mMenuBar.setVisibility(View.INVISIBLE);
        mMenuButton =(Button) findViewById(R.id.buttonMenu);
        mMenuButton.setOnClickListener(this);

        mMenuButton.setVisibility(View.INVISIBLE);

        assert(Meeting.MAX_CHANNELS==mCH.length);
        final int[] playerID = {
                R.id.playerView,
                R.id.playerView2,
                R.id.playerView3,
                R.id.playerView4,
                R.id.playerView5
        };
        final int[] titleID = {
                R.id.titleBar,
                R.id.titleBar2,
                R.id.titleBar3,
                R.id.titleBar4,
                R.id.titleBar5,
        };
        final int[] seekID ={
                R.id.seekBar,
                R.id.seekBar2,
                R.id.seekBar3,
                R.id.seekBar4,
                R.id.seekBar5,
        };
        for(int i=0;i<Meeting.MAX_CHANNELS;i++)
        {
            mCH[i] =new CHANNEL(
                    (PlayerView)findViewById(playerID[i]),
                    (TitleBar)findViewById(titleID[i]),
                    (SeekBar)findViewById(seekID[i]),
                    i);
        }

        // 视频采集信号源: 空 / 摄像头 / 屏幕
        Video.set_source(mVideoSrc);

        findViewById(R.id.buttonSend).setOnClickListener(this);
        findViewById(R.id.buttonSource).setOnClickListener(this);
        findViewById(R.id.buttonRotate).setOnClickListener(this);

        // 开始推流 （当有人收听时，才会真正上传，不会浪费带宽）
        String push_server =App.instance.push_server;
        if(push_server.length() > 7)
            Meeting.push_to("fvideo://"+ push_server+"/"+App.instance.user_id);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // 关闭麦克风
        Meeting.set_mic_state(Meeting.OFF);

        // 停止推流
        Meeting.push_to(null);

        // 返回大厅
        if (App.instance.room_id!=Meeting.empty_id)
            Meeting.enter_room(Meeting.empty_id,null, false);

        // 释放播放器 (不释放会内存泄露)
        for(int i=0; i<mCH.length; i++)
            mCH[i].release();

        App.instance.roomActivity =null;
    }

    public void onTimer()
    {
        // 如果很久没点击按钮，则隐藏菜单栏
        long now =System.currentTimeMillis();

        if (mMenuBar.getVisibility()==View.VISIBLE)
        {
            if (mSignalCount!=0 && (now-mMenuTime) > 5000)
                mMenuBar.setVisibility(View.INVISIBLE);
        }

        if (mMenuButton.getVisibility()==mMenuBar.getVisibility())
        {
            mMenuButton.setVisibility( mMenuBar.getVisibility()==View.VISIBLE? View.INVISIBLE : View.VISIBLE );
        }

        for (int i=0; i<Meeting.MAX_CHANNELS; i++)
        {
            mCH[i].onTimer(now);
        }
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.buttonSend:
                onClickSend(view);
                break;
            case R.id.buttonSource:
                onClickSource(view);
                break;
            case R.id.buttonRotate:
                this.rotate();
                break;
            case R.id.buttonMenu:
                mMenuBar.setVisibility(View.VISIBLE);
                mMenuTime = System.currentTimeMillis();
                break;
        }
        mMenuTime =System.currentTimeMillis();
    }

    // 切换视频源
    void onClickSource(View view)
    {
        switch(mVideoSrc)
        {
            case Video.source_camera:
                // 从摄像头切换到屏幕采集
                if(this.startScreenCapture())
                    mVideoSrc =Video.source_screen;
                else
                    mVideoSrc =Video.source_none;
                break;
            case Video.source_screen:
                // 从屏幕采集切换到空
                this.endScreenCapture();
                mVideoSrc =Video.source_none;
                break;
            default:
                // 从空切换到摄像头
                mVideoSrc =Video.source_camera;
                break;
        }
        Video.set_source(mVideoSrc);
        switch(mVideoSrc)
        {
            case Video.source_camera:
                ((Button)view).setText("采集:摄像头");
                break;
            case Video.source_screen:
                ((Button)view).setText("采集:屏幕");
            default:
                ((Button)view).setText("采集:空");
                break;
        }
    }

    void onClickSend(View view)
    {
        if(Meeting.mic_state()==Meeting.OFF){
            Meeting.set_mic_state(Meeting.ON);
            ((Button)view).setText("发送:开");
        }
        else{
            Meeting.set_mic_state(Meeting.OFF);
            ((Button)view).setText("发送:关");
        }
    }
}

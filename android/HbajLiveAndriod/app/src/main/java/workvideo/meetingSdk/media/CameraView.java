
//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class CameraView extends SurfaceView
        implements SurfaceHolder.Callback, View.OnLongClickListener
{
    Camera2 mCam = Camera2.instance;

    WindowManager mWM ;

    // 采集分辨率
    static int Resolution =(640<<16)|(480);

    public CameraView(Context context)
    {
        super(context);
        this.Init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.Init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.Init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.Init();
    }

    void Init()
    {
        mWM = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        setZOrderMediaOverlay(true);
        getHolder().addCallback(this);
        this.setOnLongClickListener(this);
    }

    public int displayResolution()
    {
        return mCam.getDisplayResolution();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {
        if (mCam.open(true))
            mCam.run(Resolution, this, mWM);
    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2)
    {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {
        mCam.close();
    }

    @Override
    public boolean onLongClick(View view)
    {
        // 切换前后
        if(view==this)
        {
            if (mCam.open(!mCam.isFacingFront())) {
                mCam.run(Resolution, this, mWM);
            }
            return true;
        }
        return false;
    }
}

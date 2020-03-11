package workvideo.meetingSdk.media;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public abstract class VideoActivity extends Activity
{
    protected boolean mFocus =false;
    protected int mScreenCx=0, mScreenCy=0, mDPI =1;

    Handler mHandler =null;
    WindowManager mWM =null;

    protected abstract void onSize(int width, int height);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Init();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState)
    {
        super.onCreate(savedInstanceState, persistentState);
        Init();
    }

    void Init()
    {
        Window wnd = getWindow();
        wnd.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        wnd.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams params = wnd.getAttributes();
        params.systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        wnd.setAttributes(params);

        mHandler =new Handler();

        mWM =(WindowManager) getSystemService(Context.WINDOW_SERVICE);

        checkPermission();
    }

    // 检查权限
    public void checkPermission()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            int HasRecorderPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            int HasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            int HasAudioSettingPermission = checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS);

            List<String> permissions = new ArrayList<String>();
            if (HasRecorderPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (HasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (HasAudioSettingPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            }
            if (!permissions.isEmpty())
            {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 0x20200222);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        mFocus =hasFocus;

        checkSize();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        Camera2.instance.updateOrientation(mWM);

        checkSize();
    }

    void checkSize()
    {
        if (!mFocus)
            return;

        Display display = mWM.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm);
        }
        else {
            display.getMetrics(dm);
        }
        if (mScreenCx==dm.widthPixels && mScreenCy==dm.heightPixels) {
            // no changed
        }
        else {
            mDPI =dm.densityDpi;
            mScreenCx = dm.widthPixels;
            mScreenCy = dm.heightPixels;
            this.onSize(mScreenCx, mScreenCy);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    // 屏幕旋转
    protected void rotate()
    {
        int angle = mWM.getDefaultDisplay().getRotation();
        if ((angle % 180)==0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    // 屏幕采集
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    class ScreenCapture extends MediaProjection.Callback
            implements ImageReader.OnImageAvailableListener
    {
        MediaProjection mMedia =null;
        MediaProjectionManager mManager =null;
        VirtualDisplay mVirtualDisplay =null;
        ImageReader mReader =null;

        int screenRequestCode =2020;

        public ScreenCapture()
        {
            mManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }

        public boolean start()
        {
            if (null !=mMedia )
                return true;

            if (null ==mManager)
                return false;

            if (0==mScreenCx || 0==mScreenCy)
                return false;

            Intent it = mManager.createScreenCaptureIntent();

            startActivityForResult(it, ++screenRequestCode);
            return true;
        }

        @Override
        public void onStop()
        {
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
                mVirtualDisplay =null;
            }
            if (mReader != null) {
                mReader.setOnImageAvailableListener(null, null);
                mReader =null;
            }
            mMedia.unregisterCallback(this);
            mMedia =null;
        }

        public void stop()
        {
            if (null != mMedia)
                mMedia.stop();
        }

        public void onStartResult(int requestCode, int resultCode, Intent data)
        {
            if (RESULT_OK == resultCode && requestCode == screenRequestCode)
            {
                mMedia = mManager.getMediaProjection(resultCode, data);
                mMedia.registerCallback(this,mHandler);

                mReader = ImageReader.newInstance(mScreenCx, mScreenCy, PixelFormat.RGBA_8888, 1);
                mVirtualDisplay = mMedia.createVirtualDisplay("mirror",
                        mScreenCx,
                        mScreenCy,
                        mDPI,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mReader.getSurface(),
                        null,
                        null);

                mReader.setOnImageAvailableListener(this, mHandler);
            }
        }

        @Override
        public void onImageAvailable(ImageReader imageReader)
        {
            Image image = imageReader.acquireLatestImage();

            final Image.Plane plane = image.getPlanes()[0];

            Video.on_screen_output(plane.getBuffer(), image.getWidth(), image.getHeight(), plane.getRowStride());

            image.close();
        }
    }
    ScreenCapture mScreen =null;

    public boolean startScreenCapture()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (mScreen == null) {
                mScreen = new ScreenCapture();
                if( mScreen.start()){
                    return true;
                }
                mScreen.stop();
                mScreen =null;
            }
        }
        return false;
    }
    public void endScreenCapture()
    {
        if (mScreen!=null) {
            mScreen.stop();
            mScreen =null;
        }
    }
    public boolean isScreenCapturing()
    {
        return mScreen!=null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (mScreen!=null)
            mScreen.onStartResult(requestCode, resultCode, data);
    }
}

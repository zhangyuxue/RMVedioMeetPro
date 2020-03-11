
//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk.media;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;
import java.util.List;

public class Camera2 implements Camera.PreviewCallback
{
    Camera mDevice =null;
    int mIndex =-1;
    int mResolution =0;
    int mRotation =0;
    int mFormat =0;
    int mFrameLen =0;
    boolean mRunning =false;

    private Camera2()
    {
    }
    public static final Camera2 instance = new Camera2();

    public int getResolution()
    {
        return mResolution;
    }
    public int getRotation()
    {
        return mRotation;
    }
    public boolean isRunning()
    {
        return mRunning;
    }

    public boolean equalResolution(int r1, int r2)
    {
        return (r1==r2) || ((r1>>16) == (r2&0xffff));
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        if (data!=null && camera==mDevice && data.length==mFrameLen)
        {
            Video.on_camera_output(data, getResolution(), getRotation());

            camera.addCallbackBuffer(data);
        }
    }

    public static int getDisplayRotation(WindowManager wm)
    {
//        WindowManager wm = (WindowManager)c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        switch (display.getRotation())
        {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public int getDisplayResolution()
    {
        int r =mResolution;
        if ((mRotation%180)==90) {
            r = (r>>16)|(r<<16);
        }
        return r;
    }

    public boolean run(int resolution, SurfaceView surfaceView, WindowManager wm)
    {
        if (null == mDevice || null==surfaceView)
            return false;

        Camera.Size size =getSize(resolution);
        if (null==size)
            return false;

        int newRes =(size.width<<16)|(size.height);

        if (mRunning)
        {
            if (equalResolution(newRes,mResolution))
                return true;

            mRunning = false;
            mDevice.stopPreview();
            mDevice.setPreviewCallback(null);
        }

        try	{
            mDevice.setPreviewDisplay(surfaceView.getHolder());

            Camera.Parameters p = mDevice.getParameters();
            p.setPreviewFormat(ImageFormat.NV21);
            p.setPreviewSize(size.width, size.height);
            mDevice.setParameters(p);
            mResolution=newRes;

            mFormat = p.getPreviewFormat();
            mFrameLen = size.width * size.height * ImageFormat.getBitsPerPixel(mFormat)/8;
            if (mFrameLen < size.width*(size.height+size.height/2))
                return false;

            updateOrientation(wm);

            mDevice.addCallbackBuffer(new byte[mFrameLen]);
            mDevice.addCallbackBuffer(new byte[mFrameLen]);
            mDevice.setPreviewCallbackWithBuffer(this);
            mDevice.startPreview();
            mRunning = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
//            setFps();
        return true;
    }

    public void stop()
    {
        if (mRunning)
        {
            mRunning=false;
            mDevice.stopPreview();
            mDevice.setPreviewCallback(null);
        }
    }

    public boolean open(boolean isFront)
    {
        int index =indexOf(isFront);
        if (mDevice!=null)
        {
            if (mIndex == index)
                return true; // already opened
            this.close();
        }
        if (index < 0)
            return false;

        try {
            mDevice = Camera.open(index);
            mIndex =index;
            mRunning =false;
            return true;
        }
        catch (Exception e) {
            mDevice=null;
            e.printStackTrace();
            return false;
        }
    }
    public void close()
    {
        mRunning =false;
        if (mDevice!=null)
        {
            mDevice.stopPreview();
            mDevice.setPreviewCallback(null);
            mDevice.release();
            mDevice=null;
            mResolution=0;
        }
    }

    public boolean isFacingFront()
    {
        if (mIndex==-1)
            return false;

        if (mIndex <Camera.getNumberOfCameras()) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mIndex, info);
            return (info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        return false;
    }

    int indexOf(boolean isFront)
    {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int count =Camera.getNumberOfCameras();
		int cand =-1;
        for (int i=0; i < count; i++)
        {
            Camera.getCameraInfo(i, info);
			cand =i;
            switch (info.facing)
            {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    if (isFront)
                        return i;
                    continue;

                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    if (!isFront)
                        return i;
                    continue;
            }
        }
        return cand;
    }

    Camera.Size getSize(int resolution)
    {
        int w =resolution >>16;
        int h =resolution &0xffff;
        if (w >1920)
            w =1920;
        else if (w < 192)
            w =192;
        if (h >1080)
            h =1080;
        else if (h < 144)
            h =144;

        Camera.Parameters p = mDevice.getParameters();
        List<Camera.Size> list = p.getSupportedPreviewSizes();
        Camera.Size size=null;
        int error=0;
        int wh = w*h;
        for (Camera.Size s:list)
        {
            int e = s.width * s.height - wh;
            if (e <0) e=-e;
            if (size==null || e <error)
            {
                size=s;
                error=e;
                if (e==0)
                    break;
            }
        }
        return size;
    }

    void setFps()
    {
        Camera.Parameters p = mDevice.getParameters();
        List<int[]> list=p.getSupportedPreviewFpsRange();
        int[] selected =null;
        for (int[] r:list)
        {
            if (selected==null || selected[0]<r[0])
                selected =r;
        }
        try	{
            int fps =selected[1];
            if (fps>32000)
                fps=32000;
            p.setPreviewFpsRange(fps,fps);
            mDevice.setParameters(p);
            return;
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        p.setPreviewFpsRange(selected[0], selected[1]);
        mDevice.setParameters(p);
    }

    public void updateOrientation(WindowManager wm)
    {
        if (mDevice==null)
            return;

        int degrees = getDisplayRotation(wm);

        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(mIndex, info);

        int rotation =0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            degrees = (info.orientation + degrees) % 360;
            rotation =degrees;
            degrees = (360 - degrees) % 360;
        }
        else {
            degrees = ( info.orientation - degrees + 360) % 360;
            rotation = degrees;
        }
        mDevice.setDisplayOrientation(degrees);
        mRotation=rotation;
    }
}

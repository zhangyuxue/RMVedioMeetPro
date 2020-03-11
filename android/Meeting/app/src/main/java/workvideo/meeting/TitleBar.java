package workvideo.meeting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TitleBar extends SurfaceView implements SurfaceHolder.Callback
{
    String mTitle;
    SurfaceHolder mHolder =null;

    int mCx, mCy;
    boolean mTitleChanged =false;

    final int WaveBarCx =3;

    Rect mWaveRect =new Rect();

    Paint mPaint, mWaveLinePaint, mWaveBackPaint;

    void Init()
    {
        this.setZOrderMediaOverlay(true);
        this.getHolder().addCallback(this);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(35);
        mPaint.setTypeface(Typeface.SANS_SERIF);

        mWaveLinePaint =new Paint();
        mWaveLinePaint.setColor(Color.GREEN);
        mWaveLinePaint.setStrokeWidth(WaveBarCx);

        mWaveBackPaint =new Paint();
        mWaveBackPaint.setColor(Color.BLACK);
    }

    public TitleBar(Context context) {
        super(context);
        Init();
    }
    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }
    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }
    @SuppressLint("NewApi")
    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Init();
    }

    void setTitle(String text)
    {
        mTitle =text;
        mTitleChanged =true;
    }

    void drawWave(byte[] wave)
    {
        if (mHolder==null)
            return;

        Canvas canvas = null;
        try {
            canvas = mHolder.lockCanvas();
            drawWave(canvas, wave);
        }
        catch (Exception e) {
        }
        if (canvas != null) {
            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    void drawWave(Canvas canvas, byte[] wave)
    {
        final int Cy =mCy;
        final int BarCx =WaveBarCx+2;

        int x =mCx - BarCx*12;

        // 清空背景
        if (mTitleChanged) {
            drawTitle(canvas);
        }
        else {
            mWaveRect.left =x-BarCx;
            mWaveRect.top =0;
            mWaveRect.right =mCx;
            mWaveRect.bottom =mCy;
            canvas.drawRect(mWaveRect, mWaveBackPaint);
        }

        // 绘制波形
        for(int i=0; i<12; i++) {
            int v =(Cy*wave[i])>>8;
            int y =(Cy-v)>>1;
            canvas.drawLine(x,y,x,y+v,mWaveLinePaint );
            x +=BarCx;
        }
    }

    void drawTitle(Canvas c)
    {
        mWaveRect.set(0,0,mCx,mCy);
        c.drawRect(mWaveRect, mWaveBackPaint);
        c.drawText(mTitle, 10, 40, mPaint);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mHolder =surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mCx =i1;
        mCy =i2;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mHolder =null;
    }
}

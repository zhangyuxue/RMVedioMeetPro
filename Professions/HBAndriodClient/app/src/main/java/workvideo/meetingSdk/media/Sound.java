
//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk.media;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Process;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioTrack;
import android.util.Log;

import static android.media.AudioTrack.PERFORMANCE_MODE_LOW_LATENCY;

class Config 
{
    public static final int SAMPLE_RATE =32000;
    public static final int BLOCK_LEN =SAMPLE_RATE/100;

    public static final String TAG ="meeting.debug.Audio";
}

class Playback 
{
    AudioTrack mTrack =null;
    Thread mThread =null;
    volatile boolean mRunning =false;

    void Loop()
    {
        short[] frame = new short[Config.BLOCK_LEN];
        while (mRunning)
        {
            Sound.playback_read(frame, Config.BLOCK_LEN);
            mTrack.write(frame, 0, Config.BLOCK_LEN);
        }
        try {
            mTrack.stop();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    static AudioTrack createTrack(boolean voice_mode, int bufferSize)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int usage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
            int content_type = AudioAttributes.CONTENT_TYPE_SPEECH;

            if (!voice_mode) {
                usage =   AudioAttributes.USAGE_MEDIA;
                content_type =AudioAttributes.CONTENT_TYPE_MUSIC;
            }

            try
            {
                AudioTrack.Builder builder = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(Config.SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_DEFAULT)
                            .build());

                builder.setBufferSizeInBytes(bufferSize);
                builder.setTransferMode(AudioTrack.MODE_STREAM);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setPerformanceMode(PERFORMANCE_MODE_LOW_LATENCY);
                }
                return builder.build();
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        try {
            return new AudioTrack(
                    voice_mode ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                    Config.SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    boolean start(boolean voiceMode)
    {
        if (mRunning)
            return true;

        int min_buffer =AudioTrack.getMinBufferSize(Config.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min_buffer <0)
            return false;

        int blocks =min_buffer/(Config.BLOCK_LEN*2);
        if (blocks<10)
            blocks=10;

        int bufferSize =Config.BLOCK_LEN * blocks *2;
        mTrack =createTrack(voiceMode, bufferSize);
        if (mTrack==null)
            return false;

        try {
            mTrack.play();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        if (mTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
            return false;

        mRunning =true;
        mThread = new Thread("AudioPlaybackThread")
        {
            @Override
            public void run() {
                super.run();
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                Loop();
            }
        };
        mThread.start();
        return true;
    }
    void reset()
    {
        if (mThread!=null)
        {
            mRunning=false;
            try {
                mThread.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            mThread =null;
        }
        if (mTrack!=null){
            mTrack.release();
            mTrack =null;
        }
    }
    boolean isRunning()
    {
        return mRunning;
    }
}

class Record
{
    AudioRecord mRecord=null;
    Thread mThread =null;
    volatile boolean mRunning =false;

    static AudioRecord createRecord(int source, int blockSize)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            try {
                AudioRecord obj = new AudioRecord.Builder()
                        .setAudioSource(source)
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(Config.SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                                .build())
                        .setBufferSizeInBytes(blockSize)
                        .build();
                return obj;
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        try {
            AudioRecord obj = new AudioRecord(
                    source,
                    Config.SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    blockSize);
            return obj;
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    boolean start()
    {
        if (mRunning)
            return true;

        int min_buffer =AudioRecord.getMinBufferSize(Config.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min_buffer <0)
            return false;

        int blocks =min_buffer /(Config.BLOCK_LEN*2) +1;
        if (blocks <10)
            blocks =10;

        int bufferSize =blocks * Config.BLOCK_LEN * 2;
        mRecord = createRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, bufferSize);
        if (null ==mRecord)
            return false;

        try {
            mRecord.startRecording();
        } 
		catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        if (mRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
        {
            return false;
        }

        mRunning =true;
        mThread =new Thread("AudioRecordThread") {
            @Override
            public void run() {
                super.run();
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                Loop();
            }
        };
        mThread.start();
        return true;
    }
    void Loop()
    {
        short[] frame = new short[Config.BLOCK_LEN];

        while (mRunning)
        {
            int got =mRecord.read(frame, 0, Config.BLOCK_LEN);
            if (got >0) {
                Sound.record_write(frame, got);
            }
        }
        try {
            mRecord.stop();
        }
        catch(Exception ex) {
        }
    }
    void reset()
    {
        if (mThread!=null)
        {
            mRunning =false;
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThread =null;
        }
        if (mRecord!=null)
        {
            mRecord.release();
            mRecord =null;
        }
    }
}

public class Sound
{
    final Playback mPlayback =new Playback();
    final Record mRecord =new Record();

    AudioManager mAM =null;

    private Sound() {}

    public static final Sound instance = new Sound();

    public void Init(Context c)
    {
        mAM = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setSpeakerOn(boolean on)
    {
        mAM.setSpeakerphoneOn(on);
    }

    public boolean isRunning()
    {
        return mPlayback.isRunning();
    }

    static boolean bluetooth_connected()
    {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null) {
            return false;
        }
        if (!ba.isEnabled()) {
            return false;
        }
        int a2dp = ba.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = ba.getProfileConnectionState(BluetoothProfile.HEADSET);
        return a2dp==BluetoothProfile.STATE_CONNECTED || headset==BluetoothProfile.STATE_CONNECTED;
    }

    public boolean run()
    {
        if (null==mAM)
            return false;

        if (mPlayback.isRunning())
            return true;

        boolean headSetOn = mAM.isWiredHeadsetOn();

        boolean succ = mPlayback.start(!headSetOn) && mRecord.start();
        if (!succ)
        {
            mPlayback.reset();
            mRecord.reset();
            return false;
        }

        if (!headSetOn) {
            mAM.setMode(AudioManager.MODE_IN_COMMUNICATION);
            if (bluetooth_connected()) {
                mAM.startBluetoothSco();
                mAM.setBluetoothScoOn(true);
                Log.w(Config.TAG, "bluetooth connected");
            }
            mAM.setSpeakerphoneOn(true);
        } else {
            mAM.setMode(AudioManager.MODE_NORMAL);
            mAM.setSpeakerphoneOn(false);
            Log.w(Config.TAG, "Wired Headset On");
        }
        mAM.setMicrophoneMute(false);
        return true;
    }

    public void stop()
    {
        mPlayback.reset();
        mRecord.reset();

        mAM.setMode(AudioManager.MODE_NORMAL);
        mAM.setSpeakerphoneOn(false);
    }

    static native void playback_read(short[] buffer, int size);
    static native void record_write(short[] buffer, int size);

    public static native boolean have_jobs();
    public static native void set_mic_volume(int vol);
    public static native int get_mic_volume();
    public static native void get_mic_wave(byte[] wave);
}


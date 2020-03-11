package workvideo.fvideo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import workvideo.meetingSdk.Meeting;
import workvideo.meetingSdk.media.CameraView;
import workvideo.meetingSdk.media.PlayerView;
import workvideo.meetingSdk.media.Sound;
import workvideo.meetingSdk.media.Video;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    PlayerView playerView;

    boolean isPlaying =false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);

        playerView =(PlayerView)findViewById(R.id.playerView);

        Sound.instance.run(); //开启声音设备 (或则需要时开启)       Sound.have_jobs() ?
    }

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
    protected void onDestroy()
    {
        super.onDestroy();

        Sound.instance.stop(); //停止声音设备

        playerView.release(); //需要明确释放，否则会内存泄露

        App.instance.onExit();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.button:
                if(Meeting.mic_state() == Meeting.OFF) {

                    EditText edit =findViewById(R.id.editText);
                    Meeting.push_to(edit.getText().toString());

                    Video.set_source(Video.source_camera);
                    Meeting.set_mic_state(Meeting.ON);

                    ((Button)view).setText("停止");
                }
                else {
                    Meeting.set_mic_state(Meeting.OFF);
                    Video.set_source(Video.source_none);
                    Meeting.push_to(null);
                    ((Button)view).setText("推送");
                }
                break;

            case R.id.button2:
                if (isPlaying) {
                    playerView.stop();
                    playerView.load(null);
                    isPlaying =false;
                    ((Button)view).setText("播放");
                }
                else {
                    EditText edit =findViewById(R.id.editText2);
                    playerView.load(edit.getText().toString());
                    playerView.play(Video.LayerBitAudio | Video.LayerBitVideoMedium);
                    isPlaying =true;
                    ((Button)view).setText("停止");
                }
                break;
        }
    }
}

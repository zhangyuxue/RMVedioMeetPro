package workvideo.fvideo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import java.util.UUID;

public class ServerSetActivity extends AppCompatActivity {

    Button conserbt;

    EditText ipVideoSteam;
    EditText ipAudioSteam;
    EditText ipWork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serverset);

        if (Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }
        getVidwByID();
    }

    void getVidwByID()
    {
        ipVideoSteam= (EditText) findViewById(R.id.ipVideoStream);
        ipAudioSteam= (EditText) findViewById(R.id.ipAudioStream);
        ipWork= (EditText) findViewById(R.id.ipWork);
        conserbt = (Button) findViewById(R.id.connectServerBt);


        conserbt.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                App.gMeetServerIP=ipWork.getText().toString();
                App.gVideoServerIP=ipVideoSteam.getText().toString();
                App.gAudioServerIP=ipAudioSteam.getText().toString();
                startActivity(new Intent(ServerSetActivity.this, LoginActivity.class));
                ServerSetActivity.this.finish();

            }
        });

    }
}

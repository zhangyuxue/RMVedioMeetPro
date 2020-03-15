package com.hbajlive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.hbajlive.net.TcpCompare;

import org.json.JSONException;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    Button conserbt;

    EditText ipSteam;
    EditText ipWork;
    EditText nickname;
    EditText nickLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        App.getInstance().activelogin=this;
        getVidwByID();

        App.getInstance().gUserUID=getUUID32();
    }

    void getVidwByID()
    {
        ipSteam= (EditText) findViewById(R.id.ipStream);
        ipWork= (EditText) findViewById(R.id.ipWork);
        conserbt = (Button) findViewById(R.id.connectServerBt);
        nickname = (EditText) findViewById(R.id.namenick);
        nickLevel = (EditText)findViewById(R.id.nickLevel);

        conserbt.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                App.getInstance().gWorkServer=ipWork.getText().toString();
                App.getInstance().gStreamServer=ipSteam.getText().toString();
                App.getInstance().gUserName = nickname.getText().toString();
                App.getInstance().gUserLevel = nickLevel.getText().toString();
                App.getInstance().socketConnect(App.getInstance().gWorkServer,10000);
            }
        });

    }
    public static String getUUID32(){

        return UUID.randomUUID().toString().replace("-", "").toLowerCase();

    }

    public void setLoginMode(){
        startActivity(new Intent(LoginActivity.this, MeetActivity.class));
    }

    public void showMainActive()
    {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }
}

package workvideo.meeting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import workvideo.meetingSdk.*;
import workvideo.meetingSdk.Error;

public class MainActivity extends Activity implements View.OnClickListener {

    TextView mStateView;

    class Config {
        SharedPreferences pref =null;

        String phone, server, passwd, push_server;

        void Load()
        {
            pref =getSharedPreferences("meeting", Activity.MODE_PRIVATE);
            phone = pref.getString("phone", "10086");
            passwd = pref.getString("passwd", "");
            server =pref.getString("server", "192.168.0.104:10003");
            push_server =pref.getString("push_server", "192.168.0.104:10005");
        }
        void Save()
        {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("phone", phone);
            editor.putString("passwd", passwd);
            editor.putString("server", server);
            editor.putString("push_server", push_server);
            editor.commit();
        }
    }
    Config mConfig = new Config();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.instance.mainActivity =this;

        findViewById(R.id.buttonLogin).setOnClickListener(this);

        mStateView =(TextView) findViewById(R.id.textView4);

        mConfig.Load();

        ((EditText)findViewById(R.id.editText)).setText(mConfig.phone);
        ((EditText)findViewById(R.id.editText2)).setText(mConfig.passwd);
        ((EditText)findViewById(R.id.editText3)).setText(mConfig.server);
        ((EditText)findViewById(R.id.editText4)).setText(mConfig.push_server);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        mConfig.Save();
        App.instance.mainActivity =null;
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId()==R.id.buttonLogin)
        {
            // 点击登录

            String phone =((EditText)findViewById(R.id.editText)).getText().toString();
            String passwd =((EditText)findViewById(R.id.editText2)).getText().toString();
            String server =((EditText)findViewById(R.id.editText3)).getText().toString();
            String push_server =((EditText)findViewById(R.id.editText4)).getText().toString();
            App.instance.push_server =push_server;

            long userid=0;
            try {
                userid =Long.parseLong(phone);
            }
            catch(Exception ex) {
                print("请输入手机号");
                return;
            }
            if(userid < 1000) {
                print("请输入有效的手机号");
                return;
            }

            if(!Meeting.login(server,userid,passwd))
                print("参数错误");
            else {
                mConfig.phone =phone;
                mConfig.passwd =passwd;
                mConfig.server =server;
                mConfig.push_server =push_server;
                print("登录中，请稍后...");
            }
        }
    }

    void print(String msg)
    {
        mStateView.setText(msg);
    }

    void onLoginSuccess()
    {
        print("登录成功");

        // 进入主界面
        startActivity(new Intent(this, LobbyActivity.class));
    }
    void onLoginError(int error)
    {
        if (error==Error.conflict)
            print("账号冲突，请30秒后再试"); //该账号还未下线
        else
            print( Error.parse(error) );
    }
    void onOffline()
    {
        print("离线了");
    }
}

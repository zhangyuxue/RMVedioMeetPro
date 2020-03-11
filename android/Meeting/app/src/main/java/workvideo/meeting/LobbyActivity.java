package workvideo.meeting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import workvideo.meetingSdk.*;
import workvideo.meetingSdk.Error;

// 大厅
public class LobbyActivity extends Activity implements View.OnClickListener {

    EditText mEditRoomId, mEditPasswd;
    TextView mStateText;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        App.instance.lobbyActivity=this;

        mEditRoomId =(EditText) findViewById(R.id.editText1);
        mEditPasswd =(EditText) findViewById(R.id.editText2);

        findViewById(R.id.buttonEnter).setOnClickListener(this);

        mStateText =(TextView) findViewById(R.id.textView);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        App.instance.lobbyActivity =null;
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();

        print("当前账号: "+App.instance.user_id);
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.buttonEnter)
        {
            long roomid =0;
            try {
                roomid = Long.parseLong(mEditRoomId.getText().toString());
            }
            catch (Exception ex)
            {
                print("请输入房间号");
                return;
            }
            if(roomid==0)
            {
                print("请输入有效的房间号");
                return;
            }

            String passwd =mEditPasswd.getText().toString();

            if (!Meeting.enter_room(roomid,passwd, roomid==App.instance.user_id) )
            {
                print("参数错误");
            }else{
                print("正在进入房间...");
            }
        }
    }

    void print(String msg)
    {
        mStateText.setText(msg);
    }

    void onEnterSuccess()
    {
        print("");

        // 转入房间
        // 注意: 返回大厅也会收到该事件 room_id==empty_id
        if (App.instance.room_id !=Meeting.empty_id)
            startActivity(new Intent(this, RoomActivity.class));
    }
    void onEnterError(int error)
    {
        if (error== Error.password_required)
            print("需要密码");
        else
            print( Error.parse(error) );
    }

    // 离线了
    void onOffline()
    {
        finish();
    }
}

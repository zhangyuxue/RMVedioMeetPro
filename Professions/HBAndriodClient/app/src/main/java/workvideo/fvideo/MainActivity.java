package workvideo.fvideo;


import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import workvideo.fvideo.net.QSUser;
import workvideo.fvideo.net.TcpCompare;
import workvideo.fvideo.ui.home.WillMeetActivity;

public class MainActivity extends AppCompatActivity{

    MediaPlayer  mPlayer=null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        App.instance.mainactive=this;
        App.instance.gRoomID="";
    }

    public void showInvitDialog(JsonObject myJsonObject) {

        if(mPlayer == null)
        {
            mPlayer = MediaPlayer.create(MainActivity.this, R.raw.audio);
            mPlayer.setLooping(true);
            mPlayer.start();
        }
        else
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);


        String contentstr;
        final String Msg_JionMeetID = myJsonObject.get("Msg_meetID").getAsString();
        String msguid = myJsonObject.get("Msg_useruid").getAsString();

        //builder.setMessage(Msg_userName+"邀请您参加会议");

        //builder.setTitle(Msg_meetName);

        builder.setPositiveButton("确认", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mPlayer.stop();
                mPlayer.release();
                mPlayer=null;
                try {
                    TcpCompare.jionMeeting(Msg_JionMeetID,App.instance.gUserUid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JionMeeting(Msg_JionMeetID);
            }
        });

        builder.setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mPlayer.stop();
                mPlayer.release();
                mPlayer=null;
            }
        });

        builder.create().show();
    }

    private void JionMeeting(String meetidstr) {
        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        // Serialization
        QSUser user = new QSUser(-2,"","","",
                "","","",meetidstr,"");
        Gson gson = new Gson();
        String loginparam = gson.toJson(user);

        //MediaType  设置Content-Type 标头中包含的媒体类型值
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , loginparam);

        Request request = new Request.Builder()
                .url("http://"+App.gMeetServerIP+":8080"+"/JionMeet")//请求的url
                .post(requestBody)
                .build();

        //创建/Call
        Call call = okHttpClient.newCall(request);
        //加入队列 异步操作
        call.enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
//                LoginActivity.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final JsonObject myJsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();

                String meetidstr = myJsonObject.get("meetid").getAsString();
                String isCreater = myJsonObject.get("state").getAsString();
                if(isCreater.equals(App.gUserUid))
                {
                    App.isRoomManager=true;
                }
                if (!meetidstr.equals(""))
                {
                    App.gRoomID = meetidstr;
                    App.mainactive.runOnUiThread(new Runnable() {
                        public void run() {
                            Intent intent = new Intent();
                            /* 使用Intent.ACTION_GET_CONTENT这个Action */
                            intent.setClass(App.mainactive, RoomActivity.class);
                            /* 取得相片后返回本画面 */
                            startActivityForResult(intent, 1);
                        }
                    });

                }
                else
                {
//                    LoginActivity.this.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }

            }
        });
    }
}

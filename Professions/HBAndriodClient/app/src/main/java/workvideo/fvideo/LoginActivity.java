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
import android.widget.Toast;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import workvideo.fvideo.net.QSUser;
import com.google.gson.*;

public class LoginActivity extends AppCompatActivity {

    Button loginbt;
    EditText phonenum;
    EditText pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        getVidwByID();

    }


    void getVidwByID()
    {
        loginbt= (Button) findViewById(R.id.createMeetingBt);
        phonenum= (EditText) findViewById(R.id.phonenum);
        pwd= (EditText) findViewById(R.id.userpwd);

        loginbt.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {


                OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10,TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .build();

                // Serialization
                QSUser user = new QSUser(-2,"",phonenum.getText().toString(),pwd.getText().toString(),
                        "","","","","");
                Gson gson = new Gson();
                String loginparam = gson.toJson(user);

                //MediaType  设置Content-Type 标头中包含的媒体类型值
                RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                        , loginparam);

                Request request = new Request.Builder()
                        .url("http://"+App.gMeetServerIP+":8080"+"/UserLogin")//请求的url
                        .post(requestBody)
                        .build();

                //创建/Call
                Call call = okHttpClient.newCall(request);
                //加入队列 异步操作
                call.enqueue(new Callback() {
                    //请求错误回调方法
                    @Override
                    public void onFailure(Call call, IOException e) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final JsonObject myJsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();
                        if (myJsonObject.get("index").getAsInt() >=0)
                        {
                            App.gUserUid = myJsonObject.get("uid").getAsString();
                            App.gPuhserID =  String.valueOf(myJsonObject.get("index").getAsInt());
                            String rolestr = myJsonObject.get("role").getAsString();
                            if(rolestr.equals("管理员"))
                                App.isUserManager=true;
                            else
                                App.isUserManager=false;
                            App.instance.socketConnect(App.instance.gMeetServerIP,10000);
                            // 进入主界面
                            Intent itent = new Intent();
                            itent.setClass(LoginActivity.this,MainActivity.class);
                            startActivity(itent);
                            LoginActivity.this.finish();
                        }
                        else
                        {
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                });
            }
        });

    }



}

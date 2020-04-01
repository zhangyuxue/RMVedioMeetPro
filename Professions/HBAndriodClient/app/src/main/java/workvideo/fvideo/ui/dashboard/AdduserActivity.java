package workvideo.fvideo.ui.dashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.util.UUID;
import android.widget.Toast;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import workvideo.fvideo.App;
import workvideo.fvideo.net.QSUser;
import com.google.gson.*;
import workvideo.fvideo.R;

public class AdduserActivity extends AppCompatActivity {

    Button loginbt;
    EditText addphonenum;
    EditText addpwd;
    EditText addnick;
    EditText adduserLevel;
    Spinner adduserRole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adduser);

        if (Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        getVidwByID();

    }


    void getVidwByID()
    {
        adduserRole = (Spinner)findViewById(R.id.adduserRole);
        String[] arrModel ={"普通","管理员"};
        ArrayAdapter<String> adapter;//数组 配置器 下拉菜单赋值用
//将可选内容与ArrayAdapter连接起来
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,arrModel);
        adduserRole.setAdapter(adapter);//将adapter 添加到spinner中
        loginbt= (Button) findViewById(R.id.addUserBt);

         addphonenum = (EditText) findViewById(R.id.addphonenum);
         addpwd = (EditText) findViewById(R.id.addpwd);
         addnick = (EditText) findViewById(R.id.addnick);
         adduserLevel= (EditText) findViewById(R.id.adduserLevel);
         adduserRole = (Spinner) findViewById(R.id.adduserRole);

        loginbt.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {



                OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10,TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .build();

                // Serialization
                QSUser user = new QSUser(-2,getUUID32(),addphonenum.getText().toString(),
                        addpwd.getText().toString(),
                        adduserLevel.getText().toString(),addnick.getText().toString(),"0","0",
                        adduserRole.getSelectedItem().toString());
                Gson gson = new Gson();
                String loginparam = gson.toJson(user);

                //MediaType  设置Content-Type 标头中包含的媒体类型值
                RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                        , loginparam);

                Request request = new Request.Builder()
                        .url("http://"+ App.instance.gMeetServerIP+":8080"+"/AddUser")//请求的url
                        .post(requestBody)
                        .build();

                //创建/Call
                Call call = okHttpClient.newCall(request);
                //加入队列 异步操作
                call.enqueue(new Callback() {
                    //请求错误回调方法
                    @Override
                    public void onFailure(Call call, IOException e) {
                        AdduserActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(AdduserActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final JsonObject myJsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();
                        if (myJsonObject.get("index").getAsInt() >=0)
                        {
                            AdduserActivity.this.finish();
                        }
                        else
                        {
                            AdduserActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(AdduserActivity.this, "用户已经存在", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                });
            }
        });

    }

    public static String getUUID32(){

        return UUID.randomUUID().toString().replace("-", "").toLowerCase();

    }

}

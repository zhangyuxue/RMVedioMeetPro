package workvideo.fvideo.ui.home;

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
import workvideo.fvideo.App;
import workvideo.fvideo.net.QSMeet;
import workvideo.fvideo.net.QSUser;
import com.google.gson.*;

import org.json.JSONException;

import workvideo.fvideo.R;
import workvideo.fvideo.net.TcpCompare;

public class StartMeetActivity extends AppCompatActivity {

    Button addstartMeetBt;
    EditText addstartmeetname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        getVidwByID();
        App.startType=1;
    }


    void getVidwByID()
    {
        addstartMeetBt= (Button) findViewById(R.id.addstartMeetBt);
        addstartmeetname= (EditText) findViewById(R.id.addstartmeetname);

        addstartMeetBt.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {


                OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10,TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .build();

                // Serialization
                QSMeet meet = new QSMeet(-2,"",addstartmeetname.getText().toString(),
                        App.instance.gUserUid, "");
                Gson gson = new Gson();
                String postparam = gson.toJson(meet);

                //MediaType  设置Content-Type 标头中包含的媒体类型值
                RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                        , postparam);

                Request request = new Request.Builder()
                        .url("http://"+App.instance.gMeetServerIP+":8080"+"/WillingMeet")//请求的url
                        .post(requestBody)
                        .build();

                //创建/Call
                Call call = okHttpClient.newCall(request);
                //加入队列 异步操作
                call.enqueue(new Callback() {
                    //请求错误回调方法
                    @Override
                    public void onFailure(Call call, IOException e) {
                        StartMeetActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(StartMeetActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final JsonObject myJsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();
                        if (myJsonObject.get("index").getAsInt() >=0)
                        {
                            App.instance.gRoomID =  myJsonObject.get("meetid").getAsString();
                            try {
                                TcpCompare.jionMeeting(App.instance.gRoomID,App.instance.gUserUid);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            StartMeetActivity.this.finish();
                        }
                        else
                        {
                            StartMeetActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(StartMeetActivity.this, "开始失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                });
            }
        });

    }



}

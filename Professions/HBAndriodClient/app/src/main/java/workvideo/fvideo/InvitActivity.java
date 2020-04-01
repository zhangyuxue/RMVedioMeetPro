package workvideo.fvideo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import android.os.StrictMode;
import android.os.Build;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import workvideo.fvideo.net.TcpCompare;

public class InvitActivity extends AppCompatActivity {

    private ListView lvusers;
    private Button invitUsers;
    Button tickUsers;
    InvitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invit);
        if (Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }

        lvusers = (ListView)findViewById(R.id.lvusers);


        tickUsers = (Button)findViewById(R.id.tickUsers);
        tickUsers.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {

                Vector<String> str;
                str = new Vector<>();
                for (int i=0;i<adapter.listItem.size();i++)
                {
                    String state = adapter.listItem.get(i).get("Check").toString();
                    if (state.equals("1"))
                    {
                        str.addElement(adapter.listItem.get(i).get("Uid").toString());
                    }
                }

                try {
                    TcpCompare.tickUsers(str);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                finish();

            }
        });

        if(!App.isRoomManager)
            tickUsers.setVisibility(View.INVISIBLE);

        invitUsers = (Button)findViewById(R.id.invitUsers);
        invitUsers.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {

                    Vector<String> str;
                    str = new Vector<>();
                    for (int i=0;i<adapter.listItem.size();i++)
                    {
                        String state = adapter.listItem.get(i).get("Check").toString();
                        if (state.equals("1"))
                        {
                            str.addElement(adapter.listItem.get(i).get("Uid").toString());
                        }
                    }

                try {
                    TcpCompare.invitUsers(str);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                finish();

            }
        });
        flushUserList();

    }

    public void flushUserList(){
        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();


        Request request = new Request.Builder()
                .url("http://"+ App.instance.gMeetServerIP+":8080"+"/GetUserList")
                .build();

        //创建/Call
        Call call = okHttpClient.newCall(request);
        //加入队列 异步操作
        call.enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
                InvitActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(App.instance.mainactive, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final JsonObject myJsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();

                InvitActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        initUserListView(myJsonObject.getAsJsonArray("members"));
                    }
                });

            }
        });
    }
    public void initUserListView(JsonArray datas){
        /*定义一个以HashMap为内容的动态数组*/
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();/*在数组中存放数据*/
        for (int i = 0; i < datas.size(); i++) {
            JsonObject dataelement =  datas.get(i).getAsJsonObject();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Name", dataelement.get("name").getAsString());//加入图片
            map.put("State",dataelement.get("state").getAsString());
            map.put("Uid",dataelement.get("uid").getAsString());
            map.put("Check","0");
            listItem.add(map);
        }
        adapter = new InvitAdapter(this, listItem);
        lvusers.setAdapter(adapter);//为ListView绑定适配器

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                System.out.println("你点击了第" + arg2 + "行");//设置系统输出点击的行
//            }
//        });
    }
}

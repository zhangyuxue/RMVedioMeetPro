package workvideo.fvideo.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import workvideo.fvideo.App;
import workvideo.fvideo.R;
import workvideo.fvideo.RoomActivity;
import workvideo.fvideo.net.QSUser;
import workvideo.fvideo.net.TcpCompare;

import android.widget.AdapterView;

import org.json.JSONException;

public class HomeFragment extends Fragment {

    View rootview;
    Button flushmeetBt;
    Button willingMeetBt;
    Button startMeetingBt;
    Button jionmeetBt;
    MeetItemAdapter adapter;
    ListView meetlistview;
    EditText meetidUse;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_home, container, false);

        return rootview;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        meetlistview = (ListView)rootview.findViewById(R.id.meetlistview);
        meetidUse = (EditText)rootview.findViewById(R.id.meetidUse);

        jionmeetBt = (Button)rootview.findViewById(R.id.jionmeetBt);
        jionmeetBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                final String meetidstr = meetidUse.getText().toString();

                App.instance.mainactive.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            TcpCompare.jionMeeting(meetidstr,App.instance.gUserUid);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                JionMeeting(meetidstr);

            }
        });

        startMeetingBt = (Button)rootview.findViewById(R.id.startMeetingBt);
        startMeetingBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setClass(v.getContext(), StartMeetActivity.class);
                /* 取得相片后返回本画面 */
                startActivityForResult(intent, 1);
            }
        });

        flushmeetBt = (Button)rootview.findViewById(R.id.flushmeetBt);
        flushmeetBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                 flushMeetList();
            }
        });
        willingMeetBt = (Button)rootview.findViewById(R.id.willingMeetBt);
        willingMeetBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setClass(v.getContext(), WillMeetActivity.class);
                /* 取得相片后返回本画面 */
                startActivityForResult(intent, 1); }
        });
    }

    public void initMeetListView(JsonArray datas){
        /*定义一个以HashMap为内容的动态数组*/
        final ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();/*在数组中存放数据*/
        for (int i = 0; i < datas.size(); i++) {
            JsonObject dataelement =  datas.get(i).getAsJsonObject();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Name", dataelement.get("meetname").getAsString());//加入图片
            map.put("State",dataelement.get("state").getAsString());
            map.put("Meetid",dataelement.get("meetid").getAsString());
            listItem.add(map);
        }
        adapter = new MeetItemAdapter(rootview.getContext(), listItem);
        meetlistview.setAdapter(adapter);//为ListView绑定适配器

        meetlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                String jionmeetid = listItem.get(arg2).get("Meetid").toString();
                meetidUse.setText(jionmeetid);
            }
        });
    }

    public void flushMeetList(){
        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();


        Request request = new Request.Builder()
                .url("http://"+ App.instance.gMeetServerIP+":8080"+"/GetMeetList")
                .build();

        //创建/Call
        Call call = okHttpClient.newCall(request);
        //加入队列 异步操作
        call.enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
                App.instance.mainactive.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(App.instance.mainactive, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final JsonObject myJsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();

                App.instance.mainactive.runOnUiThread(new Runnable() {
                    public void run() {
                        initMeetListView(myJsonObject.getAsJsonArray("meetlist"));
                    }
                });

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (App.startType)
        {
            case 1:
            {
                //开始会议
                String meetidstr = App.instance.gRoomID;
                try {
                    TcpCompare.jionMeeting(meetidstr,App.instance.gUserUid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JionMeeting(meetidstr);
            }
            break;
            case 2:
            {

            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
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
                if (!meetidstr.equals(""))
                {
                    App.gRoomID = meetidstr;
                    String isCreater = myJsonObject.get("state").getAsString();
                    if(isCreater.equals(App.gUserUid))
                    {
                        App.isRoomManager=true;
                    }
                    App.mainactive.runOnUiThread(new Runnable() {
                        public void run() {
                            Intent intent = new Intent();
                            /* 使用Intent.ACTION_GET_CONTENT这个Action */
                            intent.setClass(rootview.getContext(), RoomActivity.class);
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
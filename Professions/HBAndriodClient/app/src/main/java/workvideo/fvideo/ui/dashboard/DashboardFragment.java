package workvideo.fvideo.ui.dashboard;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import workvideo.fvideo.App;
import workvideo.fvideo.LoginActivity;
import workvideo.fvideo.MainActivity;
import workvideo.fvideo.R;

import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import workvideo.fvideo.net.QSUser;
import com.google.gson.*;

public class DashboardFragment extends Fragment {

    View rootview;

    Button pageUserFlush;
    ListView usrelistview;
    UserItemAdapter adapter;
    Button addUserNetwork;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootview= inflater.inflate(R.layout.fragment_dashboard, container, false);

        return rootview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        usrelistview = (ListView)rootview.findViewById(R.id.usrelistview);


        addUserNetwork = (Button)rootview.findViewById(R.id.addUserNetwork);
        addUserNetwork.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setClass(v.getContext(),AdduserActivity.class);
                /* 取得相片后返回本画面 */
                startActivityForResult(intent, 1); }
        });
        if (!App.isUserManager)
            addUserNetwork.setVisibility(View.INVISIBLE);

        pageUserFlush = (Button)rootview.findViewById(R.id.pageUserFlush);
        pageUserFlush.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                flushUserList();
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
            listItem.add(map);
        }
        adapter = new UserItemAdapter(rootview.getContext(), listItem);
        usrelistview.setAdapter(adapter);//为ListView绑定适配器

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                System.out.println("你点击了第" + arg2 + "行");//设置系统输出点击的行
//            }
//        });
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
                        initUserListView(myJsonObject.getAsJsonArray("members"));
                    }
                });

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        flushUserList();
        super.onActivityResult(requestCode, resultCode, data);
    }
}
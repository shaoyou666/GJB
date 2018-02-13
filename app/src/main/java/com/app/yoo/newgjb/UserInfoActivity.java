package com.app.yoo.newgjb;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class UserInfoActivity extends AppCompatActivity {
    private TextView tv_userInfo;
    private ListView lv_playlog;
    private Button bt_signIn,bt_yeb;
    private AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        appData = (AppData)this.getApplication();
        Intent i = getIntent();
        String username = i.getStringExtra("username");
        String uid = i.getStringExtra("uid");
        String account = i.getStringExtra("account");

        tv_userInfo = (TextView) findViewById(R.id.tv_userInfo);
        lv_playlog = (ListView) findViewById(R.id.lv_playlog);
        bt_signIn = (Button) findViewById(R.id.bt_sigIn);
        bt_yeb = (Button) findViewById(R.id.bt_yeb);

        List<Map<String,String>> list = new ArrayList<Map<String,String>>();
        Document doc = Jsoup.parse(i.getStringExtra("html"));
        Elements playlogs_table = doc.getElementsByClass("paylog");
        if(playlogs_table != null){
            Elements playlogs_tr = playlogs_table.get(0).select("tbody").get(0).select("tr");
            for(int j=0;j<playlogs_tr.size();j++){
                Map<String ,String> map = new HashMap<String,String>();
                map.put("title",playlogs_tr.get(j).select("td").get(1).text());
                map.put("time",playlogs_tr.get(j).select("td").get(2).text());
                map.put("coin",playlogs_tr.get(j).select("td").get(0).text());
                list.add(map);
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.lv_playlog,
                new String[]{"title","time","coin"},
                new int[]{R.id.lv_tv_title,R.id.lv_tv_time,R.id.lv_tv_coin});
        lv_playlog.setAdapter(adapter);
        tv_userInfo.setText("用户名：" + username + "，" + uid + "\n" + account);
        Element e_signIn = doc.getElementsByClass("signIn").select("a").last();
        final String signInUrl = "http://www.guajibang.com" + e_signIn.attr("href");
        bt_signIn.setText(e_signIn.text());
        if(e_signIn.text().contains("已")){
            bt_signIn.setEnabled(false);
        }
        bt_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SignInTask(signInUrl).execute((Void) null);
            }
        });
        bt_yeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInfoActivity.this,YebActivity.class);
                startActivity(intent);
            }
        });
    }

    public class SignInTask extends AsyncTask<Void, Void, String> {

        private final String signInUrl;

        SignInTask(String url) {
            signInUrl = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            try {
                Log.i("Signin",signInUrl);
                Request request = new Request.Builder()
                        .url(signInUrl)
                        .build();
                Call call = appData.okHttpClient.newCall(request);
                Response response = call.execute();
                //byte[] responseBytes = response.body().bytes();

                //result = new String(responseBytes,"GBK");
                result = URLDecoder.decode(response.body().string(), "GBK");
            }catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("Signin",result);
            //Document doc = Jsoup.parse(result);
            //String toastTxt = doc.getElementsByTag("script").html();
            try {
                //String toastTxt = URLDecoder.decode(result, "UTF-8");
                //String toastTxt = new String(result.getBytes(),"UTF-8");
                Toast.makeText(UserInfoActivity.this, result, Toast.LENGTH_LONG).show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}

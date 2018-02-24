package com.app.yoo.newgjb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
    private Button bt_signIn,bt_yeb,bt_gotoWithdraw,bt_taskRecord;
    private AppData appData;
    private Context mContext;
    private String username,uid,account,signInUrl;
    private SharedPreferences settings;
    private boolean isUserInfoUpdated;

    @Override
    protected void onResume() {
        super.onRestart();
        isUserInfoUpdated = settings.getBoolean("isUserInfoUpdated",false);
        if(isUserInfoUpdated){
            new GetUserInfoTask().execute((Void) null);
        }
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isUserInfoUpdated",false);
        editor.commit();
    }
/**/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        mContext = UserInfoActivity.this;
        settings = getSharedPreferences("settings",0);
        appData = (AppData)this.getApplication();
        Intent i = getIntent();
        username = i.getStringExtra("username");
        uid = i.getStringExtra("uid");
        account = i.getStringExtra("account");

        tv_userInfo = (TextView) findViewById(R.id.tv_userInfo);
        lv_playlog = (ListView) findViewById(R.id.lv_playlog);
        bt_signIn = (Button) findViewById(R.id.bt_sigIn);
        bt_yeb = (Button) findViewById(R.id.bt_yeb);
        bt_gotoWithdraw = (Button) findViewById(R.id.bt_gotoWithdraw);
        bt_taskRecord = (Button) findViewById(R.id.bt_taskRecord);

        ShowUserInfo(i.getStringExtra("html"));

        tv_userInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setMessage("更新主页信息？")
                        .setNegativeButton("取消",null)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new GetUserInfoTask().execute((Void) null);
                            }
                        }).show();
            }
        });

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
        bt_gotoWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,WithdrawActivity.class);
                startActivity(intent);
            }
        });
        bt_taskRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,TaskRecordActivity.class);
                startActivity(intent);
            }
        });
    }

    public void ShowUserInfo(String msg){
        List<Map<String,String>> list = new ArrayList<Map<String,String>>();
        Document doc = Jsoup.parse(msg);
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
        username = doc.getElementsByClass("uname").get(0).text();
        uid = doc.getElementsByClass("uname").select("small").text();
        account = doc.getElementsByClass("account").select("li").get(0).text();
        tv_userInfo.setText("用户名：" + username + "，" + uid + "\n" + account);
        Element e_signIn = doc.getElementsByClass("signIn").select("a").last();
        signInUrl = "http://www.guajibang.com" + e_signIn.attr("href");
        bt_signIn.setText(e_signIn.text());
        if(e_signIn.text().contains("已")){
            bt_signIn.setEnabled(false);
        }else{
            bt_signIn.setEnabled(true);
        }
    }
    public class GetUserInfoTask extends AsyncTask<Void,Void,String>{
        private ProgressDialog pd;

        GetUserInfoTask(){
            pd = new ProgressDialog(mContext);
            pd.setMessage("加载中...");
            pd.setCancelable(false);
        }
        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            ShowUserInfo(s);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String url = "http://www.guajibang.com/?user.html";
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Call call = appData.okHttpClient.newCall(request);
                Response response = call.execute();
                return response.body().string();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd.show();
        }
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
                //result = URLDecoder.decode(response.body().string(), "UTF-8");
                result = response.body().string();
            }catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("Signin",result);
            Document doc = Jsoup.parse(result);
            String toastTxt = doc.html();
            try {
                //String toastTxt = URLDecoder.decode(result, "UTF-8");
                //String toastTxt = new String(result.getBytes(),"UTF-8");
                Toast.makeText(UserInfoActivity.this, AppData.convertUnicode(result), Toast.LENGTH_LONG).show();
                new GetUserInfoTask().execute((Void) null);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}

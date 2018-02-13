package com.app.yoo.newgjb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class YebActivity extends AppCompatActivity {

    private AppData appData;
    private TextView tv_yebInfo,tv_canPurchase,tv_canWithdrwa;
    private Button bt_purchase,bt_withdraw;
    private EditText et_purchase_value,et_withdraw_value;
    private ListView lv_yeb;
    private String purchase_url,withdraw_url;
    private ProgressBar pb_wait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yeb);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tv_yebInfo = (TextView) findViewById(R.id.tv_yebInfo);
        tv_yebInfo.setText("信息加载中...");
        lv_yeb = (ListView) findViewById(R.id.lv_yeb);
        tv_canPurchase = (TextView) findViewById(R.id.tv_can_purchase);
        tv_canWithdrwa = (TextView) findViewById(R.id.tv_canWithdraw);
        bt_purchase = (Button) findViewById(R.id.bt_purchase);
        bt_withdraw = (Button) findViewById(R.id.bt_withdraw);
        et_purchase_value = (EditText) findViewById(R.id.et_purchase_value);
        et_withdraw_value = (EditText) findViewById(R.id.et_withdraw_value);
        pb_wait = (ProgressBar) findViewById(R.id.progressBar);
        pb_wait.setVisibility(View.VISIBLE);

        appData = (AppData)this.getApplication();
        new GetYebInfo().execute((Void) null);

        bt_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String money = et_purchase_value.getText().toString();
                if(money != null && !money.equals("")) {
                    new PostDataTask(purchase_url, money).execute((Void) null);
                }
            }
        });
        bt_withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String money = et_withdraw_value.getText().toString();
                if(money != null && !money.equals("")) {
                    new PostDataTask(withdraw_url, money).execute((Void) null);
                }
            }
        });
    }

    public class GetYebInfo extends AsyncTask<Void,Void,String>{

        String yebUrl = "http://www.guajibang.com/?useryeb.html";
        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            try {
                Request request = new Request.Builder()
                        .url(yebUrl)
                        .build();
                Call call = appData.okHttpClient.newCall(request);
                Response response = call.execute();
                result = response.body().string();

            }catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s != null) {
                Document doc = Jsoup.parse(s);
                String total_ye = doc.getElementsByClass("box-bill-kyje-account").first().text();
                Elements e_sy_title = doc.getElementsByClass("box-bill-info-title");
                Elements e_sy = doc.getElementsByClass("box-bill-info-account");

                if(e_sy_title.size()>4 && e_sy.size()>3) {
                    String str = e_sy_title.get(0).text() + total_ye
                            + "\n" + e_sy_title.get(1).text() + e_sy.get(0).text()
                            + "\n" + e_sy_title.get(2).text() + e_sy.get(1).text()
                            + "\n" + e_sy_title.get(3).text() + e_sy.get(2).text()
                            + "\n" + e_sy_title.get(4).text() + e_sy.get(3).text();
                    tv_yebInfo.setText(str);
                }

                Elements e_help_block = doc.getElementsByClass("help-block");
                tv_canPurchase.setText(e_help_block.get(0).text());//可转入余额
                tv_canWithdrwa.setText(e_help_block.get(2).text());//可转出余额
                Elements e_form = doc.getElementsByClass("form-horizontal");
                purchase_url = "http://www.guajibang.com" + e_form.get(0).attr("action");//转入post地址
                withdraw_url = "http://www.guajibang.com" + e_form.get(1).attr("action");//转入post地址
                Elements paylog_trs = doc.getElementsByClass("paylog").first().select("tbody").select("tr");
                List<Map<String,String>> list = new ArrayList<Map<String,String>>();
                for(int j=0;j<paylog_trs.size();j++){
                    Map<String ,String> map = new HashMap<String,String>();
                    map.put("title",paylog_trs.get(j).select("td").get(2).text());
                    map.put("time",paylog_trs.get(j).select("td").get(1).text());
                    map.put("coin",paylog_trs.get(j).select("td").get(0).text());
                    list.add(map);
                }
                SimpleAdapter adapter = new SimpleAdapter(YebActivity.this,list,R.layout.lv_playlog,
                        new String[]{"title","time","coin"},
                        new int[]{R.id.lv_tv_title,R.id.lv_tv_time,R.id.lv_tv_coin});
                lv_yeb.setAdapter(adapter);
                pb_wait.setVisibility(View.GONE);
            }
        }
    }

    public class PostDataTask extends AsyncTask<Void,Void,String >{

        private String postUrl;
        private String postMoney;
        PostDataTask(String url,String money){
            postUrl = url;
            postMoney = money;
        }
        @Override
        protected String doInBackground(Void... params) {
            try{
                RequestBody body = new FormBody.Builder()
                        .add("money",postMoney)
                        .build();
                Request request = new Request.Builder().url(postUrl).post(body).build();
                Call call = appData.okHttpClient.newCall(request);
                Response response = call.execute();
                return  response.body().string();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result != null) {
                Toast.makeText(YebActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

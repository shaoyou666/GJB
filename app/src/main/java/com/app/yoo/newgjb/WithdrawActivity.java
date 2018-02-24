package com.app.yoo.newgjb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WithdrawActivity extends AppCompatActivity {

    private Context mContext;
    private AppData appData;
    private EditText et_withdrawValue,et_jyPassword;
    private TextView tv_withdrawInfo;
    private Button bt_withdraw;
    private String strAccount,strCanWithdraw;
    private ListView lv_withdrawRecord;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        appData = (AppData)this.getApplication();
        mContext = WithdrawActivity.this;
        settings = getSharedPreferences("settings",0);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        et_jyPassword = (EditText) findViewById(R.id.et_jyPassword);
        et_jyPassword.setText("516534");
        et_withdrawValue = (EditText)  findViewById(R.id.et_withdrawValue);
        bt_withdraw = (Button) findViewById(R.id.bt_withdraw);
        tv_withdrawInfo = (TextView) findViewById(R.id.tv_withdrawinfo);
        lv_withdrawRecord = (ListView) findViewById(R.id.lv_withdrawrecord);

        new GetWithdrawInfoTask().execute((Void) null);

        bt_withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strValue = et_withdrawValue.getText().toString();
                String strPassword = et_jyPassword.getText().toString();
                if(strValue.equals("")){
                    Toast.makeText(mContext,"请输入提现金额！",Toast.LENGTH_LONG).show();
                    et_withdrawValue.requestFocus();
                }else if(strPassword.equals("")){
                    Toast.makeText(mContext,"请输入交易密码！",Toast.LENGTH_LONG).show();
                    et_jyPassword.requestFocus();
                }else {
                    new WithdrawTask(strValue, strPassword).execute((Void) null);
                }
            }
        });
    }

    public class GetWithdrawInfoTask extends AsyncTask<Void,Void,String>{
        private ProgressDialog pd;

        GetWithdrawInfoTask(){
            pd = new ProgressDialog(mContext);
            pd.setMessage("加载中...");
            pd.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            Document doc = Jsoup.parse(s);
            Elements inputs = doc.select("input");
            if(inputs.size()>2){
                strAccount = inputs.first().val();
            }
            Elements elements2 = doc.getElementsByClass("help-block");
            if(elements2.size()>1){
                strCanWithdraw = elements2.first().text();
            }
            tv_withdrawInfo.setText(strCanWithdraw + "\n" + strAccount);

            List<Map<String,String>> list = new ArrayList<Map<String,String>>();
            Elements tables = doc.getElementsByClass("table-bordered");
            if(tables.size()>0){
                Element tbody = tables.first().select("tbody").first();
                Elements trs = tbody.select("tr");
                for(int i=0;i<trs.size();i++){
                    Elements tds = trs.get(i).select("td");
                    if(tds.size()>4){
                        Map<String ,String> map = new HashMap<String,String>();
                        map.put("value","提现金额："+tds.get(1).text());
                        map.put("sxf","手续费："+tds.get(2).text());
                        map.put("jd","进度："+tds.get(3).text());
                        map.put("sqsj","申请时间："+tds.get(0).text());
                        map.put("clsj","处理时间："+tds.get(4).text());
                        list.add(map);
                    }else{
                        Map<String ,String> map = new HashMap<String,String>();
                        map.put("value",tds.get(0).text());
                        map.put("sxf","");
                        map.put("jd","");
                        map.put("sqsj","");
                        map.put("clsj","");
                        list.add(map);
                    }
                }
            }
            SimpleAdapter adapter = new SimpleAdapter(mContext,list,R.layout.lv_withdraw,
                    new String[]{"value","sxf","jd","sqsj","clsj"},
                    new int[]{R.id.tv_wd_value,R.id.tv_wd_sxf,R.id.tv_wd_jd,R.id.tv_wd_sqsj,R.id.tv_wd_clsj});
            lv_withdrawRecord.setAdapter(adapter);

            pd.dismiss();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String url = "http://www.guajibang.com/?userWithdraw.html";
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
    }

    public class WithdrawTask extends AsyncTask<Void,Void,String>{

        private ProgressDialog pd;
        private String mValue,mPassword;

        WithdrawTask(String value,String password){
            mValue = value;
            mPassword = password;
            pd = new ProgressDialog(mContext);
            pd.setMessage("加载中...");
            pd.setCancelable(false);
        }
        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(mContext,AppData.convertUnicode(s),Toast.LENGTH_LONG).show();
            new GetWithdrawInfoTask().execute((Void) null);
            pd.dismiss();
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                String postUrl = "http://www.guajibang.com/?type=withdraw";
                RequestBody body = new FormBody.Builder()
                        .add("jine", mValue)
                        .add("password",mPassword)
                        .build();
                Request request = new Request.Builder().url(postUrl).post(body).build();
                Call call = appData.okHttpClient.newCall(request);
                Response response = call.execute();
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("isUserInfoUpdated",true);
                editor.commit();

                return response.body().string();

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
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

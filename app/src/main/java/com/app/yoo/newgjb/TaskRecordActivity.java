package com.app.yoo.newgjb;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

public class TaskRecordActivity extends AppCompatActivity {

    private ListView lv_record;
    private Context mContext;
    private List<Map<String,String>> list;
    private SimpleAdapter adapter;
    private AppData appData;
    private ArrayList<String> nextURL;
    private int nextPage = 0;
    private boolean allLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_record);
        mContext = TaskRecordActivity.this;

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        appData = (AppData)this.getApplication();
        nextURL = new ArrayList<String>();
        lv_record = (ListView) findViewById(R.id.lv_taskRecord);
        list = new ArrayList<Map<String,String>>();
        adapter = new SimpleAdapter(mContext,list,R.layout.lv_withdraw,
                new String[]{"value","sxf","jd","sqsj","clsj"},
                new int[]{R.id.tv_wd_value,R.id.tv_wd_sxf,R.id.tv_wd_jd,R.id.tv_wd_sqsj,R.id.tv_wd_clsj});
        lv_record.setAdapter(adapter);
        lv_record.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 当不滚动时
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    // 判断是否滚动到底部
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        //加载更多功能的代码
                        if(!allLoaded) {
                            Log.i("nextURL", "nextPage=" + nextPage + "nextURL.size()=" + nextURL.size());
                            if (nextPage < nextURL.size()) {
                                //Log.i("nextURL",nextURL.get(nextPage)+"");
                                //Toast.makeText(mContext, "加载第" + (nextPage + 2) + "页", Toast.LENGTH_LONG).show();
                                new GetRecordTask("http://www.guajibang.com" + nextURL.get(nextPage), nextPage + 2).execute((Void) null);
                                nextPage++;
                            } else {
                                allLoaded = true;
                                Toast.makeText(mContext, "已加载所有数据，总共" + (nextPage + 1) + "页", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        new GetRecordTask("http://www.guajibang.com/?jobTask-999999.html",1).execute((Void) null);
    }

    public class GetRecordTask extends AsyncTask<Void,Void,String>{
        private ProgressDialog pd;
        private String strUrl;
        private int iPage;
        GetRecordTask(String url,int page){
            strUrl = url;
            iPage = page;
            pd = new ProgressDialog(mContext);
            pd.setMessage("正在加载第"+iPage+"页...");
            pd.setCancelable(false);
        }
        @Override
        protected String doInBackground(Void... params) {
            try {
                Request request = new Request.Builder()
                        .url(strUrl)
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
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Document doc = Jsoup.parse(s);
            Elements elements = doc.getElementsByClass("table-responsive");
            Elements trs = elements.first().select("tbody").first().select("tr");
            Elements lis = elements.first().select("tfoot").first().select("li");
            for(Element tr:trs){
                //String[]{"value","sxf","jd","sqsj","clsj"},
                Elements tds = tr.select("td");
                if(tds.size()>5){
                    Map<String ,String> map = new HashMap<String,String>();
                    map.put("value",tds.get(0).text());
                    map.put("sxf","数据："+tds.get(3).text());
                    map.put("jd","收入："+tds.get(4).text());
                    map.put("sqsj","用户名/工号："+tds.get(1).text()+"/"+tds.get(2).text());
                    map.put("clsj","日期："+tds.get(5).text());
                    list.add(map);
                }
            }
            Map<String ,String> map = new HashMap<String,String>();
            map.put("value","-------------------第"+iPage+"页-------------------");
            list.add(map);
            adapter.notifyDataSetChanged();
            if(nextURL.size()==0){
                if(lis.size()>3) {
                    for (int i = 3; i < lis.size()-2; i++) {
                        nextURL.add(lis.get(i).select("a").first().attr("href"));
                    }
                }
            }

            pd.dismiss();
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

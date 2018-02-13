package com.app.yoo.newgjb;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Created by csyoo on 2018/2/12.
 */

public class AppData extends Application {
    public final HashMap<String ,List<Cookie>> cookieStore = new HashMap<>();
    public final OkHttpClient okHttpClient= new OkHttpClient
            .Builder()
            .cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                    cookieStore.put(httpUrl.host(),list);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                    List<Cookie> cookies = cookieStore.get(httpUrl.host());
                    return cookies !=null ? cookies:new ArrayList<Cookie>();
                }
            }).build();
}

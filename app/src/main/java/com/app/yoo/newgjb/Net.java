package com.app.yoo.newgjb;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by csyoo on 2018/2/9.
 */

public class Net {

    public String cookies;
    private Context mContext;
    private SharedPreferences settings;
    Net(Context context){
        mContext = context;
        settings = mContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }


    public String getPostData(String stringURL,String postData){
        Log.i("net.getPostData","stringURL=" + stringURL + ",postData="+postData);
        String result = null;
        try {
            // 根据地址创建URL对象
            URL url = new URL(stringURL);
            // 根据URL对象打开链接
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            // 设置请求的方式
            urlConnection.setRequestMethod("POST");
            // 设置请求的超时时间
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            // 传递的数据
            String data = postData;
            // 设置请求的头
            urlConnection.setRequestProperty("Connection", "keep-alive");
            // 设置请求的头
            urlConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            // 设置请求的头
            urlConnection.setRequestProperty("Content-Length",
                    String.valueOf(data.getBytes().length));
            // 设置请求的头
            urlConnection
                    .setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36");

            urlConnection.setDoOutput(true); // 发送POST请求必须设置允许输出
            urlConnection.setDoInput(true); // 发送POST请求必须设置允许输入
            //setDoInput的默认值就是true
            //获取输出流
            OutputStream os = urlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            if (urlConnection.getResponseCode() == 200) {
                // 获取响应的输入流对象
                InputStream is = urlConnection.getInputStream();
                //保存Cookies
                cookies = urlConnection.getHeaderField("Set-Cookie");
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("cookieString", cookies);
                editor.commit();
                // 创建字节输出流对象
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // 定义读取的长度
                int len = 0;
                // 定义缓冲区
                byte buffer[] = new byte[1024];
                // 按照缓冲区的大小，循环读取
                while ((len = is.read(buffer)) != -1) {
                    // 根据读取的长度写入到os对象中
                    baos.write(buffer, 0, len);
                }
                // 释放资源
                is.close();
                baos.close();
                // 返回字符串
                result = new String(baos.toByteArray());
            } else {
                System.out.println("链接失败.........");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("net","net运行结果："+result);
        return result;
    }

    public String getCookies(){
        return this.cookies;
    }

    public String getWebData(String url){
        Log.i("net.getPostData","url=" + url);
        String result = null;
        try {
            // 请求的地址
            String spec2 = url;
            // 根据地址创建URL对象
            URL url2 = new URL(spec2);
            // 根据URL对象打开链接
            HttpURLConnection urlConnection2 = (HttpURLConnection) url2.openConnection();
            // 设置请求的方式
            urlConnection2.setRequestMethod("GET");
            //注意，把存在本地的cookie值加在请求头上
            urlConnection2.addRequestProperty("Cookie", cookies);
            if (urlConnection2.getResponseCode() == 200) {
                // 获取响应的输入流对象
                InputStream is2 = urlConnection2.getInputStream();
                // 创建字节输出流对象
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                // 定义读取的长度
                int len2 = 0;
                // 定义缓冲区
                byte buffer2[] = new byte[1024];
                // 按照缓冲区的大小，循环读取
                while ((len2 = is2.read(buffer2)) != -1) {
                    // 根据读取的长度写入到os对象中
                    baos2.write(buffer2, 0, len2);
                }
                // 释放资源
                is2.close();
                baos2.close();
                // 返回字符串
                result = new String(baos2.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("net","net运行结果："+result);
        return result;
    }

}

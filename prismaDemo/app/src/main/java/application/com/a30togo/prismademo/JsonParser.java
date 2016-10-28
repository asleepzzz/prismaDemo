package application.com.a30togo.prismademo;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;



/**
 * Created by jeromehuang on 10/26/16.
 */
public class JsonParser {

    final String TAG = "JsonParser.java";

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    public JSONObject getJSONFromUrl(String urlString) {
        // make HTTP request
        try {
            // 新建一个URL对象
            URL url = new URL(urlString);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接超时时间
            urlConn.setConnectTimeout(6 * 1000);
            // 开始连接
            urlConn.connect();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == urlConn.HTTP_OK) {
                is =urlConn.getInputStream();
                // 获取返回的数据
                try {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {

                        sb.append(line + "\n");
                    }
                    is.close();
                    json = sb.toString();

                } catch (Exception e) {
                    Log.e(TAG, "Error converting result " + e.toString());
                }


                try {
                    jObj = new JSONObject(json);
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing data " + e.toString());
                }

            } else {
            }
            urlConn.disconnect();



        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        // return JSON String
        return jObj;
    }


    public void sendToServer(String urlString) {
        // 创建一个URL对象
        HttpURLConnection conn = null;
        URL mURL = null;
        try {
            mURL = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // 调用URL的openConnection()方法,获取HttpURLConnection对象
        try {
            conn = (HttpURLConnection) mURL.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            conn.setRequestMethod("POST");// 设置请求方法为post
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        conn.setReadTimeout(5000);// 设置读取超时为5秒
        conn.setConnectTimeout(10000);// 设置连接网络超时为10秒
        conn.setDoOutput(true);// 设置此方法,允许向服务器输出内容
        // post请求的参数
        String data = "username=" ;//+ username + "&password=" + password;
        // 获得一个输出流,向服务器写数据,默认情况下,系统不允许向服务器输出内容
        OutputStream out = null;// 获得一个输出流,向服务器写数据
        try {
            out = conn.getOutputStream();
            out.write(data.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        int responseCode = 0;// 调用此方法就不必再使用conn.connect()方法
        try {
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseCode == 200) {

            InputStream is = null;
            try {
                is = conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                json = sb.toString();
                //Log.e("kevin","json "+json );

            } catch (Exception e) {
                Log.e(TAG, "Error converting result " + e.toString());
            }

            //return state;
        } else {
            Log.i(TAG, "访问失败" + responseCode);
        }
    }

}
package application.com.a30togo.prismademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeromehuang on 10/28/16.
 */
public class StyleQuery {
    final static private String SERVER_PORT = "5000";
    final static private String SERVER_IP = "10.33.130.123";


    private static List<String> styleNameList = new ArrayList<String>();
    private static List<Bitmap> bitmapList = new ArrayList<Bitmap>();

    public static String getStyle (int index) {
        return styleNameList.get(index);
    }

    public static Bitmap getBitmap (int index) {
        return bitmapList.get(index);
    }

    public static int getStyleCnt () {
        return styleNameList.size();
    }

    public static void setBitmap (int index, Bitmap input) {
        bitmapList.set(index,input);
    }

    public static void setStyle (int index, String input) {
        styleNameList.set(index,input);
    }

    public static void jsonToArrayList (String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            if (jObj != null) {
                String prefix = jObj.getString("prefix");
                JSONArray dataJsonArr = jObj.getJSONArray("style_list");
                //List<String> bitmapRes = new ArrayList<String>();
                for (int i = 0; i < dataJsonArr.length(); i++) {
                    String name = dataJsonArr.getString(i);
                    Log.e("kevin","thumbnail url : "+ "http://"+SERVER_IP+":"+SERVER_PORT+"/"+prefix.replace("%s",name));
                    try {
                        Bitmap tmpTumbnail = drawable_from_url("http://"+SERVER_IP+":"+SERVER_PORT+"/"+prefix.replace("%s",name));
                        styleNameList.add(name);
                        bitmapList.add(tmpTumbnail);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //bitmapRes.add("http://"+SERVER_IP+":"+SERVER_PORT+"/"+prefix.replace("%s",url));
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void queryServer() {
        try {
            styleNameList.clear();
            bitmapList.clear();
             jsonToArrayList(query("http://"+SERVER_IP+":"+SERVER_PORT+"/style_list"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String query(String actionUrl) throws IOException {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        URL url = new URL(actionUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        Log.e("kevin","query");
        int TIME_OUT = 6000;
        int LONG_TIME_OUT = 30000;
        con.setConnectTimeout(TIME_OUT);
        con.setReadTimeout(TIME_OUT);

        /* 允许Input、Output，不使用Cache */
        con.setDoInput(true);
        //con.setDoOutput(true);
        con.setUseCaches(false);
        /* 设置传送的method=POST */
        con.setRequestMethod("GET");
        /* setRequestProperty */
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        int responseCode = con.getResponseCode();

        InputStream is = con.getInputStream();



        int ch;
        StringBuilder b = new StringBuilder();
        while ((ch = is.read()) != -1) {
            b.append((char) ch);
        }

        is.close();
        con.disconnect();

        return b.toString();
    }


    public static Bitmap drawable_from_url(String url) throws java.net.MalformedURLException, java.io.IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection)new URL(url) .openConnection();
        connection.setRequestProperty("User-agent","Mozilla/4.0");

        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        input.close();
        connection.disconnect();
        return x;
    }
}

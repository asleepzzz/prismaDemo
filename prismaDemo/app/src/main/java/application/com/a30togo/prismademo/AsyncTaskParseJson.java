package application.com.a30togo.prismademo;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by jeromehuang on 10/26/16.
 */
public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

    final String TAG = "AsyncTaskParseJson.java";

    // set your json string url here
    String yourJsonStringUrl = "http://hmkcode.appspot.com/rest/controller/get.json";
     //="http://ip.jsontest.com/";
    // contacts JSONArray
    JSONArray dataJsonArr = null;

    @Override
    protected void onPreExecute() {}

    @Override
    protected String doInBackground(String... arg0) {

        try {

            // instantiate our json parser
            JsonParser jParser = new JsonParser();

// get json string from url
            JSONObject json = jParser.getJSONFromUrl(yourJsonStringUrl);

// get the array of users
            if (json!=null) {
                dataJsonArr = json.getJSONArray("articleList");

// loop through all users
                for (int i = 0; i < dataJsonArr.length(); i++) {

                    JSONObject c = dataJsonArr.getJSONObject(i);

// Storing each json item in variable
                    String url = c.getString("url");
                    Log.e("kevin", "url " + url);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onPostExecute(String strFromDoInBg) {}

}
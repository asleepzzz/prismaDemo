package application.com.a30togo.prismademo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeromehuang on 10/26/16.
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback,
        Camera.AutoFocusCallback {
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Button mTaskPicture;
    private Camera camera;
    private boolean isMulti;
    private TextView loading_message;
    private int resolutionSelected;
    private String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    final static private String SERVER_PORT = "5000";
    final static private String SERVER_IP = "10.33.130.123";
    final static private String API_UPLOAD1 = "file";
    final static private String TMP_NAME = "takepicture.jpg";
    private int ANDROID_ACCESS_INSTAGRAM_WEBSERVICES = 002;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全螢幕
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 無標題
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 直式
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        setContentView(R.layout.activity_camera);

        initViews();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        Intent intent = getIntent();
        isMulti = intent.getBooleanExtra("isMulti",false);
        resolutionSelected = 0;//intent.getIntExtra("resolutionSelected",0);


        loading_message = (TextView)findViewById(R.id.loading_message);

        Spinner spinner=(Spinner) findViewById(R.id.my_spinner);
        String[] lunch = {"1920x1080", "1280x720", "640x480"};
        ArrayAdapter<String> lunchList = new ArrayAdapter<String>(CameraActivity.this, android.R.layout.simple_spinner_item, lunch);
        spinner.setAdapter(lunchList);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                resolutionSelected = position;
                Camera.Parameters parameters = camera.getParameters();
                // 取得照片尺寸
                List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
                int sptw = supportedPictureSizes.get(supportedPictureSizes.size() - 1).width;
                int spth = supportedPictureSizes.get(supportedPictureSizes.size() - 1).height;

                // 取得預覽尺寸
                List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                int prvw = supportedPreviewSizes.get(0).width;
                int prvh = supportedPreviewSizes.get(0).height;

//                for (int j = 0;j<supportedPreviewSizes.size();j++) {
//                    Log.e("kevin","j= "+j+" width "+supportedPreviewSizes.get(j).width+" height "+supportedPreviewSizes.get(j).height);
//                }

                parameters.setPictureFormat(PixelFormat.JPEG);
                int width = 640;
                int height = 480;
                if (resolutionSelected ==1) {
                    width = 1280;
                    height = 720;
                } else if (resolutionSelected ==0) {
                    width = 1920;
                    height = 1080;
                }

                parameters.setPreviewSize(width, height);
                parameters.setPictureSize(width, height);

                camera.setParameters(parameters);
                camera.stopPreview();
                camera.startPreview();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loading_message.setVisibility(View.GONE);
    }


    private void initViews() {
        mSurfaceView = (SurfaceView) this.findViewById(R.id.svPreview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mTaskPicture = (Button) this.findViewById(R.id.taskPicture);
        mTaskPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    camera.autoFocus(CameraActivity.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onAutoFocus(boolean b, Camera camera) {
        if (b) {
            camera.takePicture(null, null, jpeg);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        camera = Camera.open();

        if (Build.VERSION.SDK_INT >= 8) {
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                camera.setDisplayOrientation(90);
            } else {
                camera.setDisplayOrientation(0);
            }

        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException exception) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // 取得相機參數
        Camera.Parameters parameters = camera.getParameters();
        // 取得照片尺寸
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        int sptw = supportedPictureSizes.get(supportedPictureSizes.size() - 1).width;
        int spth = supportedPictureSizes.get(supportedPictureSizes.size() - 1).height;

        // 取得預覽尺寸
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        int prvw = supportedPreviewSizes.get(0).width;
        int prvh = supportedPreviewSizes.get(0).height;

//        for (int j = 0;j<supportedPreviewSizes.size();j++) {
//            Log.e("kevin","j= "+j+" width "+supportedPreviewSizes.get(j).width+" height "+supportedPreviewSizes.get(j).height);
//        }

        parameters.setPictureFormat(PixelFormat.JPEG);
        int width = 640;
        int height = 480;
        if (resolutionSelected ==1) {
            width = 1280;
            height = 720;
        } else if (resolutionSelected ==0) {
            width = 1920;
            height = 1080;
        }

        parameters.setPreviewSize(width, height);
        parameters.setPictureSize(width, height);

        camera.setParameters(parameters);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Camera Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://application.com.a30togo.prismademo/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Camera Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://application.com.a30togo.prismademo/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public void saveBitmap(Bitmap bitmap) {
        loading_message.setVisibility(View.VISIBLE);
        FileOutputStream fOut;
        try {
            File dir = new File(sdcardPath+"/demo/");
            if (!dir.exists()) {
                dir.mkdir();
            }

            String tmp = sdcardPath+"/demo/"+TMP_NAME;
            fOut = new FileOutputStream(tmp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            try {
                fOut.flush();
                fOut.close();
                Thread accessWebServiceThread = new Thread(new WebServiceHandler(""));
                accessWebServiceThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }


            sendToGallery(this, tmp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    Camera.PictureCallback jpeg = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] imgData, Camera camera) {
            if (imgData != null) {
                Bitmap picture = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                picture = rotationBitmap(picture);
                saveBitmap(picture);
            }
        }
    };

    public Bitmap rotationBitmap(Bitmap picture) {
        Matrix matrix = new Matrix();
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            matrix.postRotate(90);
        } else {
            matrix.postRotate(0);
        }
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture,picture.getWidth(),picture.getHeight(),true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        return rotatedBitmap;
    }

    public void sendToGallery(Context ctx, String path) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(path));
        intent.setData(contentUri);
        ctx.sendBroadcast(intent);
    }



    private String uploadFileAndStringImp(String actionUrl, String newName, File uploadFile) throws IOException {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

//        if(LogUtil.DEBUG)
//            LogUtil.log(TAG, "uploadFileAndStringImp:" + actionUrl);

        URL url = new URL(actionUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        Log.e("kevin","uploadFileAndStringImp");
        int TIME_OUT = 6000;
        int LONG_TIME_OUT = 30000;
        if (isMulti) {
            con.setConnectTimeout(LONG_TIME_OUT);
            con.setReadTimeout(LONG_TIME_OUT);
        } else {
            con.setConnectTimeout(TIME_OUT);
            con.setReadTimeout(TIME_OUT);
        }

//        if(LogUtil.DEBUG)
//            LogUtil.log(TAG, "getConnectTimeout:" + con.getConnectTimeout() + ", getReadTimeout:" + con.getReadTimeout());


        /* 允许Input、Output，不使用Cache */
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        /* 设置传送的method=POST */
        con.setRequestMethod("POST");
        /* setRequestProperty */
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        /* 设置DataOutputStream */
        DataOutputStream ds = new DataOutputStream(con.getOutputStream());
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " + "name=\"userfile\";filename=\"" + newName + "\"" + end);
        ds.writeBytes(end);

        /* 取得文件的FileInputStream */
        FileInputStream fStream = new FileInputStream(uploadFile);
        /* 设置每次写入1024bytes */
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length = -1;
        /* 从文件读取数据至缓冲区 */
        while ((length = fStream.read(buffer)) != -1) {
            /* 将资料写入DataOutputStream中 */
            ds.write(buffer, 0, length);
        }
        ds.writeBytes(end);

//        if(LogUtil.DEBUG) LogUtil.log(TAG, "file written completed");

        // -----
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data;name=\"mode\"" + end);
        if (isMulti) {
            ds.writeBytes(end + URLEncoder.encode("2", "UTF-8") + end);
        } else {
            ds.writeBytes(end + URLEncoder.encode("1", "UTF-8") + end);
        }
        // -----

        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
        /* close streams */

        ds.flush();
//
//        if(LogUtil.DEBUG) LogUtil.log(TAG, "flushed");
        /* 取得Response内容 */
        InputStream is = con.getInputStream();
//        if(LogUtil.DEBUG) LogUtil.log(TAG, "getInputStream");
        int ch;
        StringBuilder b = new StringBuilder();
        while ((ch = is.read()) != -1) {
            b.append((char) ch);
        }

//        if(LogUtil.DEBUG)
//            LogUtil.log(TAG, "response:" + b.toString());
        /* 关闭DataOutputStream */
        fStream.close();
        ds.close();
        con.disconnect();

        return b.toString();
    }

    public static String getUploadApi() {
//        LogUtil.log(TAG, "getUploadApi : http://" + SERVER_IP + ":" + SERVER_PORT + "/" + API_UPLOAD1);
        return "http://" + SERVER_IP + ":" + SERVER_PORT + "/" + API_UPLOAD1;
    }

    class WebServiceHandler implements Runnable{

        private String igUrl;
        public WebServiceHandler (String url) {
            igUrl = url;
        }
        @Override
        public void run() {

            Looper.prepare();
            String tmp = sdcardPath+"/demo/"+TMP_NAME;
            File save_pic = new File(tmp);
            try {
                String serverReply = uploadFileAndStringImp(getUploadApi(),TMP_NAME,save_pic);
                try {
                    JSONObject jObj = new JSONObject(serverReply);
                    int mode =jObj.getInt("mode");
                    if (mode ==1) {
                        String url = jObj.getString("file");
                        List<String> bitmapRes = new ArrayList<String>();
                        bitmapRes.add("http://"+SERVER_IP+":"+SERVER_PORT+"/"+url);
                        TestActivity.setImgRes(bitmapRes);
                    } else {


                        if (jObj != null) {
                            JSONArray dataJsonArr = jObj.getJSONArray("file_list");
                            List<String> bitmapRes = new ArrayList<String>();
                            for (int i = 0; i < dataJsonArr.length(); i++) {
                                String url = dataJsonArr.getString(i);
                                bitmapRes.add("http://"+SERVER_IP+":"+SERVER_PORT+"/"+url);
                            }
                            TestActivity.setImgRes(bitmapRes);
                        }
                    }

                    Intent intent = new Intent(CameraActivity.this,TestActivity.class);

                    startActivity(intent);

                } catch (JSONException e) {
                    Log.e("kevin", "Error parsing data " + e.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String result = "uploadsuccess";
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("result", result);
            message.what = ANDROID_ACCESS_INSTAGRAM_WEBSERVICES;//设置消息标示
            message.obj = "zxn";
            message.setData(bundle);//消息内容
            handler.sendMessage(message);//发送消息
            Looper.loop();
        }

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String result = (String) msg.getData().get("result");
            String obj = (String) msg.obj;//
            if (result.equals("uploadsuccess")) {
                Log.e("kevin","upload success");
            }
        }

    };

}

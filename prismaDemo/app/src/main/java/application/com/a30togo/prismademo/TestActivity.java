package application.com.a30togo.prismademo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeromehuang on 10/26/16.
 */
public class TestActivity extends AppCompatActivity {
    private int ANDROID_ACCESS_INSTAGRAM_WEBSERVICES = 001;
    private ImageView mImageView;

    private int nowPicPos = 0;
    private String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private List<Bitmap> bitmapRes = new ArrayList<Bitmap>();


    static public List<String> imgRes= new ArrayList<String>();

    static public void setImgRes(List<String> input) {

            imgRes.clear();

        for (int i = 0;i< input.size();i++) {
            imgRes.add(input.get(i));
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String result = (String) msg.getData().get("result");
            String obj = (String) msg.obj;//
            if (result.equals("complete")) {
                for (int i = 0;i<imgRes.size();i++) {
                    String tmp = sdcardPath+"/demo2/"+i+".jpg";
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(tmp, options);
                    if (bitmap!=null) {
                        bitmapRes.add(bitmap);
                    }
                }
                Log.e("kevin","complete "+bitmapRes.size());

                if (imgRes.size()==1) {
                    mImageView.setImageBitmap(bitmapRes.get(nowPicPos));
                } else {
                    fadeOutAndHideImage(mImageView);
                }
            }
        }

    };

    class WebServiceHandler implements Runnable{

        private String igUrl;
        public WebServiceHandler (String url) {
            igUrl = url;
        }
        @Override
        public void run() {
            String tmp = sdcardPath+"/demo2/";
            File file = new File(tmp);
            DeleteFile(file);
            bitmapRes.clear();
            Looper.prepare();
            Toast.makeText(getApplicationContext(),"downloading",Toast.LENGTH_SHORT).show();
            for (int i = 0;i<imgRes.size();i++) {
                try {
                    saveBitmap(drawable_from_url(imgRes.get(i)),i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String result = "complete";
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

    public void DeleteFile(File file) {
        if (file.exists() == false) {
            return;
        } else {
            if (file.isFile()) {
                file.delete();
                return;
            }
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return;
                }
                for (File f : childFile) {
                    DeleteFile(f);
                }
                file.delete();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);
        mImageView = (ImageView) findViewById(R.id.img);
        Thread accessWebServiceThread = new Thread(new WebServiceHandler(""));
        accessWebServiceThread.start();
    }

    private void fadeOutAndHideImage(final ImageView img){
        Animation fadeOut = new AlphaAnimation((float)1, (float)1);
        //fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                nowPicPos %= bitmapRes.size();
                img.setImageBitmap(bitmapRes.get(nowPicPos));
                nowPicPos++;
                fadeOutAndHideImage(img);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }



    Bitmap drawable_from_url(String url) throws java.net.MalformedURLException, java.io.IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection)new URL(url) .openConnection();
        connection.setRequestProperty("User-agent","Mozilla/4.0");

        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return x;
    }

    public void saveBitmap(Bitmap bitmap,int index) {

        FileOutputStream fOut;
        try {
            File dir = new File(sdcardPath+"/demo2/");
            if (!dir.exists()) {
                dir.mkdir();
            }

            String tmp = sdcardPath+"/demo2/"+index+".jpg";
            fOut = new FileOutputStream(tmp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            try {
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            sendToGallery(this, tmp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendToGallery(Context ctx, String path) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(path));
        intent.setData(contentUri);
        ctx.sendBroadcast(intent);
    }

}

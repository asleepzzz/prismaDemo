package application.com.a30togo.prismademo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private int QUERY_STYLE = 003;

    Button mOnePic;
    Button mMultiPic;
    Toast mToast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOnePic = (Button)findViewById(R.id.button_first);
        mMultiPic = (Button)findViewById(R.id.multi_button);
        mToast = Toast.makeText(this, "Preparing Resources ...", Toast.LENGTH_LONG);
        mToast.show();

        Thread styleQueryThread = new Thread(new styleQueryHandler(""));
        styleQueryThread.start();

        mOnePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                intent.putExtra("isMulti",false);
                startActivity(intent);
            }
        });

        mMultiPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                intent.putExtra("isMulti",true);
                startActivity(intent);
            }
        });



        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);

        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    0);

        }

    }

    class styleQueryHandler implements Runnable{

        private String igUrl;
        public styleQueryHandler (String url) {
            igUrl = url;
        }
        @Override
        public void run() {

            Looper.prepare();
            StyleQuery.queryServer();

            String result = "querysuccess";
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("result", result);
            message.what = QUERY_STYLE;//设置消息标示
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
            if (result.equals("querysuccess")) {
                Log.e("kevin","querysuccess");
            }

            if(mOnePic != null) {
                mOnePic.setEnabled(true);
            }
            if(mMultiPic != null) {
                mMultiPic.setEnabled(true);
            }
            if(mToast != null) {
                mToast.cancel();
            }
        }

    };
}

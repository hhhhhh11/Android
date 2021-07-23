package com.hh.floatclock;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.util.LogUtils;

import java.text.SimpleDateFormat;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tv_localTime;
    private TextView tv_info;
    private Button btn_openSuspendedWindow;
    private Button btn_closeSuspendedWindow;
    private Button btn_testOkHttp;

    private ScheduledExecutorService scheduledExecutorService;
    private GetLocalTimeTimerTask getLocalTimeTimerTask;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        scheduledExecutorService = Executors.newScheduledThreadPool(10);
        getLocalTimeTimerTask = new GetLocalTimeTimerTask();
        scheduledExecutorService.scheduleAtFixedRate(getLocalTimeTimerTask,0, 1,TimeUnit.MILLISECONDS);
//        scheduledExecutorService.scheduleWithFixedDelay(getLocalTimeTimerTask,0,1, TimeUnit.MICROSECONDS);

        btn_openSuspendedWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFloatingService(v);
            }
        });

        btn_closeSuspendedWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.e(" FloatingService : " + FloatingService.isStarted);
                if (FloatingService.isStarted) {
                    stopService(new Intent(MyApplication.getContext(), FloatingService.class));
                }

            }
        });

        btn_testOkHttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestWithOkHttp();
            }
        });

    }


    private void initUI(){
        tv_localTime = (TextView) findViewById(R.id.tv_localTime);
        tv_info = (TextView) findViewById(R.id.tv_info);
        btn_openSuspendedWindow = (Button) findViewById(R.id.btn_open_suspendedWindow);
        btn_closeSuspendedWindow = (Button) findViewById(R.id.btn_close_suspendedWindow);
        btn_testOkHttp = (Button) findViewById(R.id.btn_testOKHttp);
    }



    public class GetLocalTimeTimerTask extends TimerTask{
        @Override
        public void run() {
            showMessage(tv_localTime,getLocalTime());
        }
    }

    /**
     * 获取当前时间(ms)
     * @return
     */
    public static String getLocalTime(){
        //误差补偿
        long currentTime = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(currentTime);
//        LogUtils.e(" 当前时间(ms) == " + time);
        return time;
    }

    /**
     * 在textview控件上显示message
     * @param textView
     * @param message
     */
    public void showMessage(TextView textView, String message){
        runOnUiThread(()->{
            textView.setText(message);
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startFloatingService(View view) {
        if (FloatingService.isStarted) {
            LogUtils.e(" FloatingService.isStarted :" + FloatingService.isStarted);
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
            //动态权限申请android.permission.SYSTEM_ALERT_WINDOW权限是比较特殊的
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
        } else {
            startService(new Intent(MainActivity.this, FloatingService.class));
        }
    }

    private void sendRequestWithOkHttp(){
        new Thread(()->{
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                showMessage(tv_info, responseData);
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }


}
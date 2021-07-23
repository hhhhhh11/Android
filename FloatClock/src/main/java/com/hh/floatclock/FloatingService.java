package com.hh.floatclock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.lidroid.xutils.util.LogUtils;

import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class FloatingService extends Service {
    public static boolean isStarted = false;

    private Context mContext;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private TextView tv_floatWindow;

    private ScheduledExecutorService scheduledExecutorService;
    private ShowFloatWindowTimerTask showFloatWindowTimerTask;

    private boolean isEsc;
    private Thread showFloatingWindowThread;


    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e(" --- onCreate --- ");
        isStarted = true;
        isEsc = false;
        mContext = getApplicationContext();
        //LocalWindowManager可通过activity.getWindowManager()或activity.getWindow().getWindowManager()获取
        //CompatModeWrapper可通过getSystemService(Context.WINDOW_SERVICE)
        // 当我们通过LocalWindowManger添加视图时，退出Activity，添加的视图也会随之消失。
        // 每一个Activity对应一个LocalWindowManger，每一个App对应一个CompatModeWrapper），
        // 所以要实现在App所在进程中运行的悬浮窗口，当然是得要获取CompatModeWrapper，而不是LocalWindowManger。
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            // 如果设置为TYPE_PHONE; 那么优先级会降低一些,即拉下通知栏不可见
            // layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //悬浮窗Window的背景格式，一般设置成PixelFormat.TRANSPARENT透明即可
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.CENTER;
        /**FLAGS表示Window的属性，通过FLAGS可以控制Window的显示特性
         *  LayoutParams.FLAG_NOT_TOUCH_MODAL : 使用了此标识，可以将点击事件传递到悬浮窗以外的区域，反之其他区域的Window将接收不到事件。
         *  LayoutParams.FLAG_NOT_FOCUSABLE : 表示悬浮窗Window不需要获取焦点，也不需要获取各种输入事件，事件会直接传递给下层的具有焦点的Window
         *  LayoutParams.FLAG_SHOW_WHEN_LOCKED : 此模式可以让Window显示在锁屏的界面上
         */
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 500;
        layoutParams.height = 100;
        layoutParams.x = 300;
        layoutParams.y = 300;
        showFloatWindowTimerTask = new ShowFloatWindowTimerTask();
        scheduledExecutorService = Executors.newScheduledThreadPool(1) ;
        showFloatingWindowThread = new Thread(new ShowFloatingWindowRunnable());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.e(" --- onStartCommand --- ");
//        showFloatingWindowThread.start();
//        scheduledExecutorService.scheduleWithFixedDelay(showFloatWindowTimerTask,0,1, TimeUnit.NANOSECONDS);
        scheduledExecutorService.scheduleAtFixedRate(showFloatWindowTimerTask,0,1, TimeUnit.MILLISECONDS);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogUtils.e(" --- onDestroy --- ");
        super.onDestroy();
        windowManager.removeView(tv_floatWindow);
        isStarted = false;
        scheduledExecutorService.shutdownNow();
        stopSelf();

    }

    public class ShowFloatWindowTimerTask extends TimerTask{
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            showFloatingWindow();
        }
    }

    public class ShowFloatingWindowRunnable implements Runnable{
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            showFloatingWindow();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        LogUtils.e(" Settings.canDrawOverlays == " + Settings.canDrawOverlays(this));
        if (Settings.canDrawOverlays(this)) {
            tv_floatWindow = new TextView(getApplicationContext());
            LogUtils.e("--- trace --- ");

            tv_floatWindow.post(new Runnable() {
                @Override
                public void run() {
                    tv_floatWindow.setText("" + MainActivity.getLocalTime());
                }
            });
            LogUtils.e(" tv_floatWindow : " + tv_floatWindow.getText().toString());
            tv_floatWindow.setBackgroundColor(Color.GRAY);
            LogUtils.e(" setBackgroundColor ");
            windowManager.addView(tv_floatWindow, layoutParams);
            LogUtils.e("--- addView ---");
            tv_floatWindow.setOnTouchListener(new FloatingOnTouchListener());
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

}
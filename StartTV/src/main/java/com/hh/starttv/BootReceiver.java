package com.hh.starttv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lidroid.xutils.util.LogUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 监听开机广播
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String action = intent.getAction();
        LogUtils.e("start_action : " + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
            // 打印开机时间
            long currentTime = System.currentTimeMillis();
            String timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime);
            LogUtils.d("开机时间 ："+timeNow);
            // com.dianshijia.newlive.entry.SplashActivity
            // com.dianshijia.newlive.home.LiveVideoActivity
            Intent startTVIntent = new Intent();
            startTVIntent.setClassName("com.dianshijia.newlive","com.dianshijia.newlive.entry.SplashActivity");
            startTVIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startTVIntent);
        }
    }

}
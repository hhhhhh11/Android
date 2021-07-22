package com.hh.starttv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView tv_startTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_startTV = (TextView) findViewById(R.id.tv_startTV);
        tv_startTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // com.dianshijia.newlive.entry.SplashActivity
                // com.dianshijia.newlive.home.LiveVideoActivity
                Intent startTVIntent = new Intent();
                startTVIntent.setClassName("com.dianshijia.newlive","com.dianshijia.newlive.entry.SplashActivity");
                MainActivity.this.startActivity(startTVIntent);
            }
        });
//        // com.dianshijia.newlive.entry.SplashActivity
//        // com.dianshijia.newlive.home.LiveVideoActivity
//        Intent startTVIntent = new Intent();
//        startTVIntent.setClassName("com.dianshijia","com.dianshijia.newlive.entry.SplashActivity");
//        startActivity(startTVIntent);

    }

}
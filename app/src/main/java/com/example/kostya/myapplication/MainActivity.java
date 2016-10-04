package com.example.kostya.myapplication;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CustomView customView = (CustomView)findViewById(R.id.custom_view1);
        customView.setBitmapDrawable(R.drawable.ic_cloud_circle_black_24dp);
        customView.setBitmapColor(Color.BLUE);
        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customView.setLineColor(Color.GREEN);
            }
        });

        customView.setCustomEventListener(new CustomView.OnCustomEventListener() {
            @Override
            public void onCollapsed() {
                Log.i("Tag","Collapsed");
            }
        });

    }
}

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
        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customView.setFigureColor(Color.BLACK);
            }
        });

    }
}

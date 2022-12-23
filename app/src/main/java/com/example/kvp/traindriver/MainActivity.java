package com.example.kvp.traindriver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.kvp.traindriver.config.SetupActivity;
import com.example.kvp.traindriver.running.RunningActivity;

public class MainActivity extends AppCompatActivity
{
    MainContext mainContext;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainContext = MainContext.getMainContext(this);
        mainContext.loadDevices(this);

        Button btnRun = findViewById(R.id.mainRunButton);
        Button btnSetup = findViewById(R.id.mainSetupButton);
        btnRun.setOnClickListener(view ->
        {
            Intent intent = new Intent(view.getContext(), RunningActivity.class);
            startActivity(intent);
        });
        btnSetup.setOnClickListener(view ->
        {
            mainContext.loadEditableDevices();
            Intent intent = new Intent(view.getContext(), SetupActivity.class);
            startActivity(intent);
        });

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(permissions, 0);
    }
}

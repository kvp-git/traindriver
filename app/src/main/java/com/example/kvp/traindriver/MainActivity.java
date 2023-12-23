package com.example.kvp.traindriver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.example.kvp.traindriver.config.SetupActivity;
import com.example.kvp.traindriver.running.RunningActivity;
import com.example.kvp.traindriver.settings.SettingsActivity;

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
        Button btnSettings = findViewById(R.id.mainSettingsButton);
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
        btnSettings.setOnClickListener(view ->
        {
            Intent intent = new Intent(view.getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        String[] permissions =
        {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.INTERNET,
        };
        boolean isNeeded = false;
        for (int t = 0; t < permissions.length; t++)
            if (checkSelfPermission(permissions[t]) != PackageManager.PERMISSION_GRANTED)
                isNeeded = true;
        if (isNeeded)
            requestPermissions(permissions, 0);
    }
}

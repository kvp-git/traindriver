package com.example.kvp.traindriver.running;

import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.kvp.traindriver.DeviceController;
import com.example.kvp.traindriver.MainContext;
import com.example.kvp.traindriver.R;

public class RunningActivity extends AppCompatActivity
{
    private void sendSpeed()
    {
        for(DeviceController dc : MainContext.getMainContext(this).getDevices())
            if (dc.isConnected)
                dc.update(this);
        new Handler(getMainLooper()).postDelayed(() -> sendSpeed(), 100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        RecyclerView rv = findViewById(R.id.runningRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new RunningDeviceAdapter(MainContext.getMainContext(this).getDeviceCount()));
        rv.getAdapter().notifyDataSetChanged();
        new Handler(getMainLooper()).postDelayed(() -> sendSpeed(), 100);
    }
}

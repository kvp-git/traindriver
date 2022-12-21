package com.example.kvp.traindriver.config;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import com.example.kvp.traindriver.MainContext;
import com.example.kvp.traindriver.R;

public class SetupActivity extends AppCompatActivity
{
    private MainContext mainContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Button btAdd = findViewById(R.id.setupAddButton);
        Button btSave = findViewById(R.id.setupSaveButton);
        RecyclerView rv = findViewById(R.id.setupRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SetupDeviceAdapter(this));
        rv.getAdapter().notifyDataSetChanged();
        mainContext = MainContext.getMainContext(this);
        mainContext.watchEditableDevices().observe(this, devices ->
                rv.getAdapter().notifyDataSetChanged());
        btAdd.setOnClickListener(view ->
                mainContext.addEditableDevice());
        btSave.setOnClickListener(view ->
                mainContext.saveEditableDevices());

    }
}

package com.example.kvp.traindriver.btscanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;

import com.example.kvp.traindriver.MainContext;
import com.example.kvp.traindriver.R;

public class BluetoothScanActivity extends AppCompatActivity
{
    MainContext mainContext;
    private int deviceNumber;
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        Button btStart = findViewById(R.id.scanStartButton);
        Button btStop = findViewById(R.id.scanStopButton);
        RecyclerView rvResults = findViewById(R.id.scanRecyclerView);
        mainContext = MainContext.getMainContext(this);
        mainContext.clearBluetoothDeviceList();
        try
        {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            deviceNumber = bundle.getInt("deviceNumber");
            btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            btAdapter = btManager.getAdapter();
            btScanner = btAdapter.getBluetoothLeScanner();
        } catch(Exception e)
        {
            Log.e("BluetoothScanActivity", "Error: " + e.toString(), e);
            finish();
        }
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(new BluetoothScanAdapter(this, deviceNumber));
        mainContext.watchBluetoothDeviceList().observe(this, list ->
                rvResults.getAdapter().notifyDataSetChanged());
        btStop.setEnabled(false);
        btStart.setOnClickListener(view ->
        {
            btScanner.startScan(leScanCallback);
            btStop.setEnabled(true);
        });
        btStop.setOnClickListener(view ->
        {
            btScanner.stopScan(leScanCallback);
            btStop.setEnabled(false);
        });
    }

    private ScanCallback leScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            BluetoothDevice device = result.getDevice();
            if (device == null)
                return;
            Log.i("leScanCallback", "device found: " + device.getName() + " " + device.getAddress());
            mainContext.addBluetoothDeviceList(
                    result.getDevice().getName(),
                    result.getDevice().getAddress(),
                    "rssi=" + result.getRssi());
        }
    };

    public void done()
    {
        btScanner.stopScan(leScanCallback);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        done();
    }

}

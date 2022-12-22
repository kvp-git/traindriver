package com.example.kvp.traindriver.btscanner;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kvp.traindriver.MainContext;
import com.example.kvp.traindriver.R;

public class BluetoothScanAdapter extends RecyclerView.Adapter<BluetoothScanAdapter.ViewHolder>
{
    AppCompatActivity activity;
    ViewGroup viewGroup;
    MainContext mainContext;
    int deviceNum;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView tvDeviceInfo;

        public ViewHolder(View view)
        {
            super(view);
            tvDeviceInfo = view.findViewById(R.id.bluetoothScanText);
        }
    }

    public BluetoothScanAdapter(AppCompatActivity activity, int deviceNum)
    {
        this.activity = activity;
        mainContext = MainContext.getMainContext(activity);
        this.deviceNum = deviceNum;
    }

    @Override
    public BluetoothScanAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        this.viewGroup = viewGroup;
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_bluetoothscan, viewGroup, false);
        return new BluetoothScanAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BluetoothScanAdapter.ViewHolder viewHolder, int position)
    {
        BluetoothDeviceDescriptor dev = mainContext.getBluetoothDeviceDescriptor(position);
        if (dev != null)
        {
            viewHolder.tvDeviceInfo.setText(dev.name + " " + dev.address + " " + dev.info);
            viewHolder.tvDeviceInfo.setOnClickListener(view ->
            {
                mainContext.setEditableDeviceAddress(deviceNum, dev.name, dev.address);
                activity.finish();
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return mainContext.getBluetoothDeviceListSize();
    }

}

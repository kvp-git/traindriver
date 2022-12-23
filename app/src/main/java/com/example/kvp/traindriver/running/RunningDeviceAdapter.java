package com.example.kvp.traindriver.running;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.kvp.traindriver.DeviceController;
import com.example.kvp.traindriver.MainContext;
import com.example.kvp.traindriver.R;

public class RunningDeviceAdapter extends RecyclerView.Adapter<RunningDeviceAdapter.ViewHolder>
{
    public ViewGroup viewGroup;
    public AppCompatActivity owner;
    public int count;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView tvName;
        public TextView tvAddress;
        public TextView tvInfo;
        public Button bConnect;
        public LinearLayout llChannels;
        public int[] directionCycles;
        public boolean isConnected;

        public ViewHolder(View view)
        {
            super(view);
            tvName = view.findViewById(R.id.fragmentDeviceName);
            tvAddress = view.findViewById(R.id.fragmentDeviceAddress);
            tvInfo = view.findViewById(R.id.fragmentDeviceInfo);
            bConnect = view.findViewById(R.id.fragmentDeviceConnect);
            llChannels = view.findViewById(R.id.fragmentChannelList);
        }
    }

    public RunningDeviceAdapter(AppCompatActivity owner, int count)
    {
        this.owner = owner;
        this.count = count;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        this.viewGroup = viewGroup;
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_device, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position)
    {
        DeviceController dc = MainContext.getMainContext(viewGroup.getContext()).getDeviceControllerById(position);
        int channelCount = dc.deviceDescriptor.channelCount;
        viewHolder.tvName.setText(dc.deviceDescriptor.name);
        viewHolder.tvAddress.setText(dc.deviceDescriptor.protocol  + ":\n" + dc.deviceDescriptor.address);
        viewHolder.isConnected = false;
        viewHolder.bConnect.setText("Connect");
        viewHolder.bConnect.setOnClickListener(view ->
        {
            if (!dc.isConnected)
                dc.connect(viewGroup.getContext());
            else
                dc.disconnect(viewGroup.getContext());
        });
        viewHolder.directionCycles = new int[channelCount];
        for (int t = 0; t < channelCount; t++)
        {
            View chView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_channel, viewHolder.llChannels, false);
            viewHolder.llChannels.addView(chView);
            SeekBar speedBar = chView.findViewById(R.id.channelValueBar);
            Button dirButton = chView.findViewById(R.id.channelDirectionButton);
            speedBar.setProgress(dc.channels[t]);
            dirButton.setText("Stop");
            speedBar.setEnabled(false);
            final int channelNum = t;
            dirButton.setOnClickListener(view ->
            {
                viewHolder.directionCycles[channelNum] = (viewHolder.directionCycles[channelNum] + 1) % 4;
                switch (viewHolder.directionCycles[channelNum])
                {
                    case 0:
                    case 2:
                        dirButton.setText("Stop");
                        speedBar.setEnabled(false);
                        break;
                    case 1:
                        dirButton.setText("Forward");
                        speedBar.setEnabled(true);
                        break;
                    case 3:
                        dirButton.setText("Reverse");
                        speedBar.setEnabled(true);
                        break;
                }
                speedBar.setProgress(0);
            });
            speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    int dir = 0;
                    switch (viewHolder.directionCycles[channelNum])
                    {
                        case 1:
                            dir = 1;
                            break;
                        case 3:
                            dir = -1;
                            break;
                    }
                    int value = dir * progress;
                    //Log.d("RunningDeviceAdapter", "speed on " + channelNum + " set to " + value);
                    dc.setChannel(viewGroup.getContext(), channelNum, value);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                }
            });
            dc.isChanged.observe(owner, isChanged ->
            {
                if (dc.chargePercent == -1)
                {
                    viewHolder.tvInfo.setText("");
                } else
                {
                  String info = "" + dc.batteryVoltage + "V " + dc.chargePercent + "%";
                    viewHolder.tvInfo.setText(info);
                }
                if (dc.isConnected != viewHolder.isConnected)
                {
                    viewHolder.isConnected = dc.isConnected;
                    if (viewHolder.isConnected)
                        viewHolder.bConnect.setText("Disconnect");
                    else
                        viewHolder.bConnect.setText("Connect");
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return count;
    }
}

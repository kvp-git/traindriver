package com.example.kvp.traindriver;

import android.content.Context;

import java.util.Collections;
/*
service "4dc591b0-857c-41de-b5f1-15abda665b0c"
command "2b8cbcc-0e25-4bda-8790-a15f53e6010f"
*/

public class RadioSbrickBTLE implements RadioInterface, BtLECallbacks
{
    DeviceDescriptor deviceDescriptor;
    DeviceController deviceController;
    BtLE btLE;

    public RadioSbrickBTLE(Context context, DeviceDescriptor deviceDescriptor, DeviceController deviceController)
    {
        this.deviceDescriptor = deviceDescriptor;
        btLE = new BtLE(context, this);
    }

    @Override
    public boolean connect(Context context)
    {
        return btLE.connect(context, deviceDescriptor.address,
                "4dc591b0-857c-41de-b5f1-15abda665b0c",
                new String[]{"2b8cbcc-0e25-4bda-8790-a15f53e6010f"});
    }

    @Override
    public boolean disconnect(Context context)
    {
        return btLE.disconnect();
    }

    @Override
    public boolean setChannels(Context context)
    {
        if (deviceController.channels.length < 4)
            return false;
        byte speedTable[] = new byte[1 + 4 * 3];
        speedTable[0] = 0x01;
        for (int t = 0; t < 4; t++)
        {
            int value = deviceController.channels[t];
            speedTable[1 + t * 3] = (byte) t;
            byte direction = (byte)((value < 0) ? 1 : 0);
            byte speed = 0;
            if(value < 0)
                speed = (byte)(-value);
            if(value > 0)
                speed = (byte)(value);
            speedTable[1 + t * 3 + 1] = direction;
            speedTable[1 + t * 3 + 2] = speed;
        }
        return btLE.writeCommand(0, speedTable);
    }

    @Override
    public boolean getChargePercent(Context context)
    {
        return false;
        // TODO!!!
        /*byte cmd[] = new byte[4];
        return btLE.writeCommand(0, cmd);*/
    }

    @Override
    public void connected()
    {
        deviceController.isConnected = true;
        byte[] cmd = new byte[2];
        cmd[0] = 0x0d; // watchdog timeout
        cmd[1] = 50; // 5 seconds
        btLE.writeCommand(0, cmd);
    }

    @Override
    public void disconnected()
    {
        deviceController.isConnected = false;
    }

    @Override
    public void readDone(int status, int characteristicNum, byte[] value)
    {
        // TODO!!!
    }

    @Override
    public void writeDone(int status)
    {
        // good (nop)
    }

    @Override
    public void dataChanged(int characteristicNum, byte[] value)
    {
        // TODO!!!
    }
}

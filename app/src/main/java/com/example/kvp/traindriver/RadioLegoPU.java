package com.example.kvp.traindriver;

import android.content.Context;
import android.util.Log;

public class RadioLegoPU implements RadioInterface, BtLECallbacks
{
    private DeviceDescriptor deviceDescriptor;
    private DeviceController deviceController;
    private BtLE btLE;
    private int portWritten;

    public RadioLegoPU(Context context, DeviceDescriptor deviceDescriptor, DeviceController deviceController)
    {
        this.deviceDescriptor = deviceDescriptor;
        this.deviceController = deviceController;
        portWritten = -1;
        btLE = new BtLE(context, this);
    }

    @Override
    public boolean connect(Context context)
    {
        try
        {
            return btLE.connect(context, deviceDescriptor.address,
                    "00001623-1212-EFDE-1623-785FEABCD123",
                    new String[]{"00001624-1212-EFDE-1623-785FEABCD123"});
        } catch (Exception e)
        {
            Log.e("RadioLegoPU", "Exception: " + e.toString(), e);
            return false;
        }
    }

    @Override
    public boolean disconnect(Context context)
    {
        try
        {
            return btLE.disconnect();
        } catch (Exception e)
        {
            Log.e("RadioLegoPU", "Exception: " + e.toString(), e);
            return false;
        }
    }

    @Override
    public boolean setChannels(Context context)
    {
        Log.d("RadioLegoPU", "sending updates for " + deviceDescriptor.address);
        if (deviceController.channels.length < 2)
            return false;
        byte cmd[] = new byte[8];
        cmd[0] = (byte)0x08; // length 8 bytes
        cmd[1] = (byte)0x00; // hub 0
        cmd[2] = (byte)0x81; // port write
        cmd[3] = (byte)0x00; // port 0
        cmd[4] = (byte)0x10; // immediate, no feedback
        cmd[5] = (byte)0x51; // direct write
        cmd[6] = (byte)0;    // 0x00
        int v = deviceController.channels[0];
        Log.d("RadioLegoPU", "channel=" + 0 + " value=" + v);
        cmd[7] = (byte)((int)(v / 2.55));
        portWritten = 0;
        return btLE.writeCommand(0, cmd);
    }

    @Override
    public boolean getChargePercent(Context context)
    {
        /*byte cmd[] = new byte[1];
        cmd[0] = 'b';
        return btLE.writeCommand(0, cmd);*/
        return false; // TODO!!!
    }

    @Override
    public void connected()
    {
        deviceController.isConnected = true;
        btLE.setNotify(1);
        deviceController.isChanged.postValue(true);
    }

    @Override
    public void disconnected()
    {
        deviceController.isConnected = false;
        deviceController.isChanged.postValue(true);
    }

    @Override
    public void readDone(int status, int characteristicNum, byte[] value)
    {
    }

    @Override
    public void writeDone(int status)
    {
        if (portWritten == 0)
        {
            byte cmd[] = new byte[8];
            cmd[0] = (byte)0x08; // length 8 bytes
            cmd[1] = (byte)0x00; // hub 0
            cmd[2] = (byte)0x81; // port write
            cmd[3] = (byte)0x01; // port 1
            cmd[4] = (byte)0x10; // immediate, no feedback
            cmd[5] = (byte)0x51; // direct write
            cmd[6] = (byte)0;    // 0x00
            int v = deviceController.channels[1];
            Log.d("RadioLegoPU", "channel=" + 1 + " value=" + v);
            cmd[7] = (byte)((int)(v / 2.55));
            portWritten = 1;
            btLE.writeCommand(0, cmd);
        } else if (portWritten == 1)
        {
            byte cmd[] = new byte[8];
            cmd[0] = (byte)0x08; // length 8 bytes
            cmd[1] = (byte)0x00; // hub 0
            cmd[2] = (byte)0x81; // port write
            cmd[3] = (byte)50;   // port 50, internal rgb led
            cmd[4] = (byte)0x10; // immediate, no feedback
            cmd[5] = (byte)0x51; // direct write
            cmd[6] = (byte)0x00; // 0x00
            int v = deviceController.channels[1] & 7; // TODO!!! this is a test only
            Log.d("RadioLegoPU", "channel=" + 50 + " value=" + v);
            cmd[7] = (byte)v;
            portWritten = 2;
            btLE.writeCommand(0, cmd);
        }
    }

    @Override
    public void dataChanged(int characteristicNum, byte[] value)
    {
        if (value.length < 1)
            return;
        Log.d("RadioLegoPU", "incoming data size=" + value.length);
        // TODO!!!
        /*
        if (value[0] == 0x00) // filter out speed command ack-s
            return;
        StringBuffer sb = new StringBuffer();
        for(int t = 0; t < value.length; t++)
            sb.append((char)value[t]);
        String batteryString = sb.toString();
        try
        {
            int c0 = deviceController.chargePercent;
            float v0 = deviceController.batteryVoltage;
            if (batteryString.compareTo(batteryDebugString) != 0)
            {
                Log.d("RadioCircuitCubeBTLE", "new data: " + batteryString);
                batteryDebugString = batteryString;
            }
            float batteryVoltage = Float.parseFloat(batteryString);
            deviceController.batteryVoltage = batteryVoltage;
            if ((batteryVoltage >= 3.6) && (batteryVoltage <= 4.2))
                deviceController.chargePercent = (int)(((batteryVoltage - 3.6) * 100.0) / 0.4);
            else if (batteryVoltage > 4.0)
                deviceController.chargePercent = 100;
            else
                deviceController.chargePercent = 0;
            Log.i("RadioCircuitCubeBTLE", "battery=" + deviceController.batteryVoltage + "V " + deviceController.chargePercent + "%");
            if ((c0 != deviceController.chargePercent) || (v0 != deviceController.batteryVoltage))
                deviceController.isChanged.postValue(true);
        } catch (Exception e)
        {
            Log.e("RadioCircuitCubeBTLE", "Exception: " + e.toString(), e);
        }
        */
    }
}

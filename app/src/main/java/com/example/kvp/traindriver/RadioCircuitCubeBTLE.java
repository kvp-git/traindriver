package com.example.kvp.traindriver;

import android.content.Context;
import android.util.Log;

/*
CIRCUIT_CUBE_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
CIRCUIT_CUBE_TX_CHRACTERISITCS_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
CIRCUIT_CUBE_RX_CHRACTERISITCS_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

ascii protocol:

output control:
 +000a+000b+000c
 +000a-255b+255c

battery level:
 b -> 3.82

*/

public class RadioCircuitCubeBTLE implements RadioInterface, BtLECallbacks
{
    DeviceDescriptor deviceDescriptor;
    DeviceController deviceController;
    boolean speedSentFlag;
    boolean batterySentFlag;
    BtLE btLE;

    public RadioCircuitCubeBTLE(Context context, DeviceDescriptor deviceDescriptor, DeviceController deviceController)
    {
        this.deviceDescriptor = deviceDescriptor;
        this.deviceController = deviceController;
        speedSentFlag = false;
        batterySentFlag = false;
        btLE = new BtLE(context, this);
    }

    @Override
    public boolean connect(Context context)
    {
        return btLE.connect(context, deviceDescriptor.address,
                "6e400001-b5a3-f393-e0a9-e50e24dcca9e", // command
                new String[]{
                        "6e400002-b5a3-f393-e0a9-e50e24dcca9e",    // transmit
                        "6e400003-b5a3-f393-e0a9-e50e24dcca9e"});  // receive
    }

    @Override
    public boolean disconnect(Context context)
    {
        return btLE.disconnect();
    }

    @Override
    public boolean setChannels(Context context)
    {
        Log.d("RadioCircuitCubeBTLE", "sending updates for " + deviceDescriptor.address);
        if (deviceController.channels.length < 3)
            return false;
        byte cmd[] = new byte[15];
        for (int t = 0; t < 3; t++)
        {
            int v = deviceController.channels[t];
            //Log.d("RunningActivity", "channel=" + t + " value=" + v);
            cmd[t * 5 + 0] = (byte)((v < 0) ? '-' : '+');
            if (v < 0)
                v *= -1;
            cmd[t * 5 + 1] = (byte)(('0' + (v / 100)));
            cmd[t * 5 + 2] = (byte)(('0') + ((v / 10) % 10));
            cmd[t * 5 + 3] = (byte)(('0') + (v % 10));
            cmd[t * 5 + 4] = (byte)('a' + t);
        }
        StringBuffer sb = new StringBuffer();
        for (int t = 0; t < 15; t++)
            sb.append((char)cmd[t]);
        Log.i("RadioCircuitCubeBTLE", "data: " + sb.toString());
        speedSentFlag = true;
        return btLE.writeCommand(0, cmd);
    }

    @Override
    public boolean getChargePercent(Context context)
    {
        byte cmd[] = new byte[1];
        cmd[0] = 'b';
        return btLE.writeCommand(0, cmd);
    }

    @Override
    public void connected()
    {
        deviceController.isConnected = true;
        btLE.setNotify(1);
    }

    @Override
    public void disconnected()
    {
        deviceController.isConnected = false;
    }

    @Override
    public void readDone(int status, int characteristicNum, byte[] value)
    {
    }

    @Override
    public void writeDone(int status)
    {
        if (speedSentFlag)
        {
            speedSentFlag = false;
            batterySentFlag = true;
            byte[] cmd = new byte[1];
            cmd[0] = 'b';
            btLE.writeCommand(0, cmd);
        } else if (batterySentFlag)
        {
            batterySentFlag = false;
        }
    }

    @Override
    public void dataChanged(int characteristicNum, byte[] value)
    {
        if (value.length < 1)
            return;
        if (value[0] == 0x00) // filter out speed command ack-s
            return;
        StringBuffer sb = new StringBuffer();
        for(int t = 0; t < value.length; t++)
            sb.append((char)value[t]);
        String batteryString = sb.toString();
        try
        {
            //Log.d("RadioCircuitCubeBTLE", "new data: " + batteryString);
            float batteryVoltage = Float.parseFloat(batteryString);
            deviceController.batteryVoltage = batteryVoltage;
            if ((batteryVoltage >= 3.00) && (batteryVoltage <= 4.2))
                deviceController.chargePercent = (int)(((batteryVoltage - 3.0) * 100.0) / 1.2);
            else if (batteryVoltage > 4.2)
                deviceController.chargePercent = 100;
            else
                deviceController.chargePercent = 0;
            Log.e("RadioCircuitCubeBTLE", "battery=" + deviceController.chargePercent + "%");
        } catch (Exception e)
        {
            Log.e("RadioCircuitCubeBTLE", "Exception: " + e.toString(), e);
        }

    }
}

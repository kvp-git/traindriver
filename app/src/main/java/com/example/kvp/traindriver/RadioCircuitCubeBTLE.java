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
    private static String LOGTAG = "RadioCircuitCubeBTLE";
    private DeviceDescriptor deviceDescriptor;
    private DeviceController deviceController;
    private boolean speedSentFlag;
    private boolean batterySentFlag;
    private BtLE btLE;
    private String batteryDebugString;

    public RadioCircuitCubeBTLE(Context context, DeviceDescriptor deviceDescriptor, DeviceController deviceController)
    {
        this.deviceDescriptor = deviceDescriptor;
        this.deviceController = deviceController;
        speedSentFlag = false;
        batterySentFlag = false;
        batteryDebugString = "";
        btLE = new BtLE(context, this);
    }

    @Override
    public boolean connect(Context context)
    {
        try
        {
            return btLE.connect(context, deviceDescriptor.address,
                    "6e400001-b5a3-f393-e0a9-e50e24dcca9e", // command
                    new String[]{
                            "6e400002-b5a3-f393-e0a9-e50e24dcca9e",    // transmit
                            "6e400003-b5a3-f393-e0a9-e50e24dcca9e"});  // receive
        } catch (Exception e)
        {
            Log.e(LOGTAG, "Exception: " + e.toString(), e);
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
            Log.e(LOGTAG, "Exception: " + e.toString(), e);
            return false;
        }
    }

    @Override
    public boolean setChannels(Context context)
    {
        Log.d(LOGTAG, "sending updates for " + deviceDescriptor.address);
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
        StringBuilder sb = new StringBuilder();
        for (int t = 0; t < cmd.length; t++)
            sb.append((char)cmd[t]);
        Log.i(LOGTAG, "data: " + sb.toString());
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
            int c0 = deviceController.chargePercent;
            float v0 = deviceController.batteryVoltage;
            if (batteryString.compareTo(batteryDebugString) != 0)
            {
                Log.d(LOGTAG, "new data: " + batteryString);
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
            Log.i(LOGTAG, "battery=" + deviceController.batteryVoltage + "V " + deviceController.chargePercent + "%");
            if ((c0 != deviceController.chargePercent) || (v0 != deviceController.batteryVoltage))
                deviceController.isChanged.postValue(true);
        } catch (Exception e)
        {
            Log.e(LOGTAG, "Exception: " + e.toString(), e);
        }
    }
}

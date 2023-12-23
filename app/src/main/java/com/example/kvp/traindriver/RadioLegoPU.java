package com.example.kvp.traindriver;

import android.content.Context;
import android.util.Log;

public class RadioLegoPU implements RadioInterface, BtLECallbacks
{
    private static String LOGTAG = "RadioLegoPU";
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

    private boolean sendPWM(int port, int value)
    {
        Log.d(LOGTAG, "port=" + port + " value=" + value);
        byte cmd[] = new byte[8];
        cmd[0] = (byte)0x08; // length 8 bytes
        cmd[1] = (byte)0x00; // hub 0
        cmd[2] = (byte)0x81; // port write
        cmd[3] = (byte)port; // port number
        cmd[4] = (byte)0x10; // immediate, no feedback
        cmd[5] = (byte)0x51; // direct write
        cmd[6] = (byte)0;    // 0x00
        cmd[7] = (byte)((int)(value / 2.55));
        return btLE.writeCommand(0, cmd);
    }

    private boolean sendRGBLed(int value)
    {
        byte cmd[] = new byte[8];
        cmd[0] = (byte)0x08; // length 8 bytes
        cmd[1] = (byte)0x00; // hub 0
        cmd[2] = (byte)0x81; // port write
        cmd[3] = (byte)50;   // port 50, internal rgb led
        cmd[4] = (byte)0x10; // immediate, no feedback
        cmd[5] = (byte)0x51; // direct write
        cmd[6] = (byte)0x00; // 0x00
        if (value < 0)
            value = -value;
        value = (value * 11) / 256;
        Log.d(LOGTAG, "port=" + 50 + " value=" + value);
        cmd[7] = (byte)value;
        return btLE.writeCommand(0, cmd);
    }

    @Override
    public boolean setChannels(Context context)
    {
        Log.d(LOGTAG, "sending updates for " + deviceDescriptor.address);
        if (deviceController.channels.length < 2)
            return false;
        portWritten = 0;
        return sendPWM(0, deviceController.channels[0]);
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
        switch (portWritten)
        {
            case 0:
                portWritten = 1;
                sendPWM(1, deviceController.channels[1]);
                break;
            case 1:
                portWritten = 50;
                int v = deviceController.channels[2];
                if ((deviceController.channels.length > 2) && (v != DeviceController.CHANNEL_UNUSED))
                    sendRGBLed(deviceController.channels[2]);
                break;
            case 50:
                // TODO!!! get charge percent here
                break;
        }
    }

    @Override
    public void dataChanged(int characteristicNum, byte[] value)
    {
        if (value.length < 1)
            return;
        Log.d(LOGTAG, "incoming data size=" + value.length);
        // TODO!!!
        /*
        if (value[0] == 0x00) // filter out speed command ack-s
            return;
        try
        {
            Log.i(LOGTAG, "battery=" + deviceController.batteryVoltage + "V " + deviceController.chargePercent + "%");
            if ((c0 != deviceController.chargePercent) || (v0 != deviceController.batteryVoltage))
                deviceController.isChanged.postValue(true);
        } catch (Exception e)
        {
            Log.e(LOGTAG, "Exception: " + e.toString(), e);
        }
        */
    }
}

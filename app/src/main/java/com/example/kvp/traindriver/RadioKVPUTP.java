package com.example.kvp.traindriver;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
  Packet size: 16 bytes
     nc -u -l 3456 | hexdump -C
  Commands:
    0x01: set output channels
      <uint8_t command>(<uint16_t channels>*4) ... <uint8_t>checksum

  Channels:
    Config 1: 4 motors
    ...TODO!!!
*/

public class RadioKVPUTP implements RadioInterface
{
    private static String LOGTAG = "RadioKVPUTP";
    private static int devicePort = 3456;
    private static int CMD_SET_CHANNELS = 0x01;
    private DeviceDescriptor deviceDescriptor;
    private DeviceController deviceController;
    private DatagramSocket datagramSocket;

    public RadioKVPUTP(Context context, DeviceDescriptor deviceDescriptor, DeviceController deviceController)
    {
        this.deviceDescriptor = deviceDescriptor;
        this.deviceController = deviceController;
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public boolean connect(Context context)
    {
        try
        {
            InetAddress inetAddress = InetAddress.getByName(deviceDescriptor.address);
            datagramSocket = new DatagramSocket();
            datagramSocket.connect(inetAddress, devicePort);
            deviceController.isConnected = true;
            deviceController.isChanged.postValue(true);
            return true;
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

            datagramSocket.disconnect();
            deviceController.isConnected = false;
            deviceController.isChanged.postValue(true);
            return true;
        } catch (Exception e)
        {
            Log.e(LOGTAG, "Exception: " + e.toString(), e);
            return false;
        }
    }

    @Override
    public boolean setChannels(Context context)
    {
        try
        {
            Log.d(LOGTAG, "sending updates for " + deviceDescriptor.address);
            if (deviceController.channels.length < 4)
                return false;
            byte cmd[] = new byte[16];
            cmd[0] = (byte)CMD_SET_CHANNELS;
            for (int t = 0; t < 4; t++)
            {
                int v = deviceController.channels[t];
                //Log.d(LOGTAG, "channel:" + t + " value: " + v);
                cmd[1 + t * 2] = (byte)(v & 0xff);
                cmd[1 + t * 2 + 1] = (byte)((v >> 8) & 0xff);
            }
            cmd[cmd.length - 1] = (byte)0xff;
            for (int t = 0; t < (cmd.length - 1); t++)
                cmd[cmd.length - 1] ^= cmd[t];
            StringBuilder sb = new StringBuilder();
            for (byte b : cmd)
                sb.append(String.format(" %02X", b));
            Log.i(LOGTAG, "data:" + sb.toString());
            DatagramPacket pkt = new DatagramPacket(cmd, cmd.length);
            datagramSocket.send(pkt);
            return true;
        } catch (Exception e)
        {
            Log.e(LOGTAG, "Exception: " + e.toString(), e);
            return false;
        }
    }

    @Override
    public boolean getChargePercent(Context context)
    {
        return false;
    }
}

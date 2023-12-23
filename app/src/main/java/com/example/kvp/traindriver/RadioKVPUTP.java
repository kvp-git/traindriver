package com.example.kvp.traindriver;

import android.content.Context;
import android.util.Log;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
  Commands:
    0x01: set output channels
      <uint8_t command>(<uint16_t channels>*4)<uint8_t>checksum

  Channels:
    Config 1: 4 motors
    Config 2: 2 motors, 2 16 bit digital outputs
    ...TODO!!!
*/

public class RadioKVPUTP implements RadioInterface
{
    private static int devicePort = 3456;
    private DeviceDescriptor deviceDescriptor;
    private DeviceController deviceController;
    private DatagramSocket datagramSocket;

    public RadioKVPUTP(Context context, DeviceDescriptor deviceDescriptor, DeviceController deviceController)
    {
        this.deviceDescriptor = deviceDescriptor;
        this.deviceController = deviceController;
    }

    @Override
    public boolean connect(Context context)
    {
        try
        {
            InetAddress inetAddress = InetAddress.getByName(deviceDescriptor.address);
            datagramSocket = new DatagramSocket();
            //datagramSocket.connect(inetAddress, devicePort); TODO!!!
            return true;
        } catch (Exception e)
        {
            Log.e("RadioKVPUTP", "Exception: " + e.toString(), e);
            return false;
        }
    }

    @Override
    public boolean disconnect(Context context)
    {
        return false;
    }

    @Override
    public boolean setChannels(Context context)
    {
        Log.d("RadioKVPUTP", "sending updates for " + deviceDescriptor.address);
        if (deviceController.channels.length < 4)
            return false;
        byte cmd[] = new byte[10];
        cmd[0] = (byte)0x01;
        for (int t = 0; t < 4; t++)
        {
            int v = deviceController.channels[t];
            cmd[1 + t * 2 + 0] = (byte)0x00; // TODO!!! int to byte[2]
            cmd[1 + t * 2 + 1] = (byte)0x00; // TODO!!!
        }
        cmd[1 + 4 * 2] = (byte)0xff; // TODO!!! checksum
        StringBuffer sb = new StringBuffer();
        for (int t = 0; t < cmd.length; t++)
        {
            sb.append(Byte.toString(cmd[t])); // TODO!!! convert to hex string
            if (t < (cmd.length - 1))
                sb.append(",");
        }
        Log.i("RadioKVPUTP", "data: " + sb.toString());
        // TODO!!! send packet
        return false;
    }

    @Override
    public boolean getChargePercent(Context context)
    {
        return false;
    }
}

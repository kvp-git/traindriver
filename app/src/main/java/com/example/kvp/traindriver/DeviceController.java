package com.example.kvp.traindriver;

import android.content.Context;
import android.util.Log;

public class DeviceController
{
    public DeviceDescriptor deviceDescriptor;
    public int channels[];
    public int chargePercent;
    public boolean isConnected;
    public RadioInterface radio;

    public DeviceController(Context context, DeviceDescriptor deviceDescriptor)
    {
        this.deviceDescriptor = deviceDescriptor;
        chargePercent = -1;
        isConnected = false;
        switch(deviceDescriptor.protocol.toLowerCase())
        {
            case "sbrick_btle":
                channels = new int[4];
                radio = new RadioSbrickBTLE(context, deviceDescriptor, this);
                break;
            case "circuitcube_btle":
                channels = new int[3];
                radio = new RadioCircuitCubeBTLE(context, deviceDescriptor, this);
                break;
            // TODO!!! wifi
            default:
                throw new UnsupportedOperationException("Protocol not supported:" + deviceDescriptor.protocol);
        }
    }
    public boolean connect(Context context)
    {
        return radio.connect(context);
    }
    public boolean disconnect(Context context)
    {
        return radio.disconnect(context);
    }
    public boolean update(Context context)
    {
        return radio.setChannels(context);
    }
    public boolean setChannel(Context context, int channelNum, int value)
    {
        Log.e("DeviceController", "channelNum=" + channelNum + " value=" + value + " setup=" + deviceDescriptor.channelSetup);
        switch (deviceDescriptor.channelSetup.toUpperCase())
        {
            case "A,B,C":
                if (channelNum > 2)
                    return false;
                channels[channelNum] = value;
                return true;
            case "A,B,C,D":
                if (channelNum > 3)
                    return false;
                channels[channelNum] = value;
                return true;
            case "AB,C":
                switch (channelNum)
                {
                    case 0:
                        channels[0] = value;
                        channels[1] = value;
                        break;
                    case 1:
                        channels[2] = value;
                        break;
                    default:
                        return false;
                }
                return true;
            case "AC,B":
                switch (channelNum)
                {
                    case 0:
                        channels[0] = value;
                        channels[2] = value;
                        break;
                    case 1:
                        channels[1] = value;
                        break;
                    default:
                        return false;
                }
                return true;
            case "AB,C,D":
                switch (channelNum)
                {
                    case 0:
                        channels[0] = value;
                        channels[1] = value;
                        break;
                    case 1:
                        channels[2] = value;
                        break;
                    case 2:
                        channels[3] = value;
                        break;
                    default:
                        return false;
                }
                return true;
            // TODO!!! other configs
            default: // unsupported channel config
                return false;
        }
    }
}

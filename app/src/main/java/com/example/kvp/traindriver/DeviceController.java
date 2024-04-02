package com.example.kvp.traindriver;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

public class DeviceController
{
    private static String LOGTAG = "DeviceController";
    public static int CHANNEL_UNUSED = -256;
    public DeviceDescriptor deviceDescriptor;
    public int channels[];
    public boolean stopFlags[];
    public float batteryVoltage;
    public int chargePercent;
    public boolean isConnected;
    public RadioInterface radio;
    public MutableLiveData<Boolean> isChanged;

    public DeviceController(Context context, DeviceDescriptor deviceDescriptor)
    {
        this.deviceDescriptor = deviceDescriptor;
        batteryVoltage = 0;
        chargePercent = -1;
        isConnected = false;
        isChanged = new MutableLiveData<>();
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
            case "lego_btle":
                channels = new int[4];
                channels[2] = CHANNEL_UNUSED;
                channels[3] = CHANNEL_UNUSED;
                radio = new RadioLegoPU(context, deviceDescriptor, this);
                break;
            case "kvp_utp_train":
                channels = new int[4];
                radio = new RadioKVPUTP(context, deviceDescriptor, this, RadioKVPUTP.TRAIN);
                break;
            case "kvp_utp_signal":
                channels = new int[1];
                radio = new RadioKVPUTP(context, deviceDescriptor, this, RadioKVPUTP.SIGNAL);
                break;
            default:
                throw new UnsupportedOperationException("Protocol not supported:" + deviceDescriptor.protocol);
        }
        stopFlags = new boolean[channels.length];
        for (int t = 0; t < stopFlags.length; t++)
            stopFlags[t] = true;
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
    public boolean setChannel(Context context, int channelNum, int speed, boolean isStop)
    {
        Log.e(LOGTAG, "channelNum=" + channelNum + " speed=" + speed + " stopFlag=" + isStop + " setup=" + deviceDescriptor.channelSetup);
        switch (deviceDescriptor.channelSetup.toUpperCase())
        {
            case "A":
                if (channelNum > 0)
                    return false;
                channels[channelNum] = speed;
                stopFlags[channelNum] = isStop;
                return true;
            case "A,B":
                if (channelNum > 1)
                    return false;
                channels[channelNum] = speed;
                stopFlags[channelNum] = isStop;
                return true;
            case "AB":
                if (channelNum > 0)
                    return false;
                channels[0] = speed;
                channels[1] = speed;
                stopFlags[0] = isStop;
                stopFlags[1] = isStop;
                return true;
            case "A-B":
                if (channelNum > 0)
                    return false;
                channels[0] = speed;
                channels[1] = -speed;
                stopFlags[0] = isStop;
                stopFlags[1] = isStop;
                return true;
            case "A,B,C":
                if (channelNum > 2)
                    return false;
                channels[channelNum] = speed;
                stopFlags[channelNum] = isStop;
                return true;
            case "A,B,C,D":
                if (channelNum > 3)
                    return false;
                channels[channelNum] = speed;
                stopFlags[channelNum] = isStop;
                return true;
            case "AB,C":
                switch (channelNum)
                {
                    case 0:
                        channels[0] = speed;
                        channels[1] = speed;
                        stopFlags[0] = isStop;
                        stopFlags[1] = isStop;
                        break;
                    case 1:
                        channels[2] = speed;
                        stopFlags[2] = isStop;
                        break;
                    default:
                        return false;
                }
                return true;
            case "A-B,C":
                switch (channelNum)
                {
                    case 0:
                        channels[0] = speed;
                        channels[1] = -speed;
                        stopFlags[0] = isStop;
                        stopFlags[1] = isStop;
                        break;
                    case 1:
                        channels[2] = speed;
                        stopFlags[2] = isStop;
                        break;
                    default:
                        return false;
                }
                return true;
            case "A,BC":
                switch (channelNum)
                {
                    case 0:
                        channels[0] = speed;
                        stopFlags[0] = isStop;
                        break;
                    case 1:
                        channels[1] = speed;
                        channels[2] = speed;
                        stopFlags[1] = isStop;
                        stopFlags[2] = isStop;
                        break;
                    default:
                        return false;
                }
                return true;
            case "AC,B":
                switch (channelNum)
                {
                    case 0:
                        channels[0] = speed;
                        channels[2] = speed;
                        stopFlags[0] = isStop;
                        stopFlags[2] = isStop;
                        break;
                    case 1:
                        channels[1] = speed;
                        stopFlags[1] = isStop;
                        break;
                    default:
                        return false;
                }
                return true;
            case "AB,C,D":
                switch (channelNum)
                {
                    case 0:
                        channels[0] = speed;
                        channels[1] = speed;
                        stopFlags[0] = isStop;
                        stopFlags[1] = isStop;
                        break;
                    case 1:
                        channels[2] = speed;
                        stopFlags[2] = isStop;
                        break;
                    case 2:
                        channels[3] = speed;
                        stopFlags[3] = isStop;
                        break;
                    default:
                        return false;
                }
                return true;
            case "AB-,C,D":
                switch (channelNum)
                {
                    case 0:
                        channels[0] = speed;
                        channels[1] = -speed;
                        stopFlags[0] = isStop;
                        stopFlags[1] = isStop;
                        break;
                    case 1:
                        channels[2] = speed;
                        stopFlags[2] = isStop;
                        break;
                    case 2:
                        channels[3] = speed;
                        stopFlags[3] = isStop;
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

package com.example.kvp.traindriver;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/*
v1:
  Packet size: 16 bytes
     nc -u -l 3456 | hexdump -C
  Commands:
    0x01: set output channels
      <uint8_t command>(<uint16_t channels>*4) ... <uint8_t>checksum
  Channels:
    Config 1: 4 motors

v2:
  4 bytes magic id 'KVP '
  2 bytes device family id
  2 bytes command id
  N bytes parameters
  4 bytes salt
  32 bytes SHA256

Device family ids:
  - 'WT' : wifi train controller
  - 'WS' : wifi signal controller

Commands:
  - 'M1' : move motor v1 (WT): int16_t channels[4]
  - 'S1' : signal set v1 (SW): uint8_t bits[2]

Examples:
  'KVP WTM1',channels[8],salt[4],hash[32]
  'KVP WSS1',bits[2],salt[4],hash[32]
*/

public class RadioKVPUTP implements RadioInterface
{
    public static final int TRAIN = 1;
    public static final int SIGNAL = 2;

    public static final int HDR_SIZE = 8;
    public static final int SALT_SIZE = 4;
    public static final int HASH_SIZE = 32;

    public static final int M1_DATA_SIZE = 8;
    public static final int S1_DATA_SIZE = 2;

    private static final String LOGTAG = "RadioKVPUTP";
    private static final int devicePort = 3456;
    private static final int CMD_SET_CHANNELS = 0x01;
    private DeviceDescriptor deviceDescriptor;
    private DeviceController deviceController;
    private DatagramSocket datagramSocket;
    private int deviceType;

    public RadioKVPUTP(Context context, DeviceDescriptor deviceDescriptor, DeviceController deviceController, int type)
    {
        this.deviceDescriptor = deviceDescriptor;
        this.deviceController = deviceController;
        this.deviceType = type;
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

    private void setHeader(byte[] pkt, String header)
    {
        byte[] hdr = header.getBytes(StandardCharsets.UTF_8);
        for (int t = 0; t < hdr.length; t++)
            pkt[t] = hdr[t];
    }

    private void checksum(byte[] pkt)
    {
        long salt = System.currentTimeMillis();
        pkt[pkt.length - (Crypto.SHA256digestSize + 4)] = (byte)(salt & 0xFF);
        pkt[pkt.length - (Crypto.SHA256digestSize + 3)] = (byte)((salt >> 8) & 0xFF);
        pkt[pkt.length - (Crypto.SHA256digestSize + 2)] = (byte)((salt >> 16) & 0xFF);
        pkt[pkt.length - (Crypto.SHA256digestSize + 1)] = (byte)((salt >> 24) & 0xFF);
        byte[] sha = Crypto.SHA256pass(pkt, 0, pkt.length - Crypto.SHA256digestSize, deviceDescriptor.password);
        if (sha != null)
            for (int t = 0; t < Crypto.SHA256digestSize; t++)
                pkt[pkt.length - Crypto.SHA256digestSize + t] = sha[t];
    }

    @Override
    public boolean setChannels(Context context)
    {
        try
        {
            Log.d(LOGTAG, "sending updates for " + deviceDescriptor.address);
            byte cmd[] = null;
            switch (deviceType)
            {
                case TRAIN:
                {
                    cmd = new byte[HDR_SIZE + M1_DATA_SIZE + SALT_SIZE + HASH_SIZE];
                    setHeader(cmd, "KVP WTM1");
                    for (int t = 0; t < 4; t++)
                    {
                        int v = deviceController.channels[t];
                        //Log.d(LOGTAG, "channel:" + t + " value: " + v);
                        cmd[HDR_SIZE + t * 2] = (byte) (v & 0xFF);
                        cmd[HDR_SIZE + t * 2 + 1] = (byte) ((v >> 8) & 0xFF);
                    }
                    checksum(cmd);
                    break;
                }
                case SIGNAL:
                {
                    cmd = new byte[HDR_SIZE + S1_DATA_SIZE + SALT_SIZE + HASH_SIZE];
                    setHeader(cmd, "KVP WSS1");
                    int v = deviceController.channels[0];
                    int p1 = 0xff; // TODO!!! value to bitmask (for testing only)
                    int p2 = 0; // TODO!!! 2nd bitmask for blinking
                    cmd[HDR_SIZE] = (byte) (p1);
                    cmd[HDR_SIZE + 1] = (byte) (p2);
                    checksum(cmd);
                    break;
                }
                default: // old packet format
                {
                    if (deviceController.channels.length < 4)
                        return false;
                    cmd = new byte[16];
                    cmd[0] = (byte) CMD_SET_CHANNELS;
                    for (int t = 0; t < 4; t++)
                    {
                        int v = deviceController.channels[t];
                        //Log.d(LOGTAG, "channel:" + t + " value: " + v);
                        cmd[1 + t * 2] = (byte) (v & 0xff);
                        cmd[1 + t * 2 + 1] = (byte) ((v >> 8) & 0xff);
                    }
                    cmd[cmd.length - 1] = (byte) 0xff;
                    for (int t = 0; t < (cmd.length - 1); t++)
                        cmd[cmd.length - 1] ^= cmd[t];
                    break;
                }
            }
            if (cmd == null)
                return false;
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

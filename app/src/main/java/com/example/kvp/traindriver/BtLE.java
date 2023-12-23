package com.example.kvp.traindriver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

public class BtLE
{
    private static String LOGTAG = "BtLE";
    public Context context;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BtLECallbacks btLECallbacks;
    private BluetoothDevice btDevice;
    private BluetoothGatt btGatt;
    private BluetoothGattCallback btGattCallback;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic[] gattCharacteristics;
    private String serviceUUID;
    private String[] characteristcsUUID;
    private boolean btReady;

    public BtLE(Context context, BtLECallbacks btLECallbacks)
    {
        this.context = context;
        this.btLECallbacks = btLECallbacks;
        btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btReady = false;
    }

    public boolean connect(Context context, String address, String serviceUUID, String[] characteristicsUUID)
    {
        btDevice = btAdapter.getRemoteDevice(address);
        this.serviceUUID = serviceUUID;
        this.characteristcsUUID = characteristicsUUID;
        if (btDevice == null)
            return false;
        btGattCallback = new BluetoothGattCallback()
        {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
            {
                String s;
                switch(newState)
                {
                    case BluetoothProfile.STATE_CONNECTED:
                        s = "connected";
                        if(!btGatt.discoverServices())
                            disconnect();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        s = "disconnected";
                        disconnected();
                        break;
                    default:
                        s = "" + newState;
                        break;
                }
                Log.e(LOGTAG, "onConnectionStateChange status=" + status + " newState=" + s);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status)
            {
                Log.e(LOGTAG, "onServicesDiscovered status=" + status);
                gattService = btGatt.getService(UUID.fromString(serviceUUID));
                gattCharacteristics = new BluetoothGattCharacteristic[characteristicsUUID.length];
                if (gattService == null)
                    disconnect();
                for (int t = 0; t < characteristcsUUID.length; t++)
                {
                    gattCharacteristics[t] = gattService.getCharacteristic(UUID.fromString(characteristcsUUID[t]));
                    if (gattCharacteristics[t] == null)
                        disconnect();
                }
                btReady = true;
                Log.e(LOGTAG, "READY: " + address);
                btLECallbacks.connected();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
            {
                //Log.d(LOGTAG, "onCharacteristicRead(" + status + ")");
                // TODO!!! status and error handling
                int num = -1; // TODO!!! look up the right characteristic number
                btLECallbacks.readDone(status, num, characteristic.getValue());
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
            {
                //Log.e(LOGTAG, "onCharacteristicWrite(" + status + ")");
                // TODO!!! status and error handling
                btLECallbacks.writeDone(status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
            {
                //Log.d(LOGTAG, "onCharacteristicChanged()");
                int num = -1; // TODO!!! look up the right characteristic number
                btLECallbacks.dataChanged(num, characteristic.getValue());
            }
        };
        btGatt = btDevice.connectGatt(context, true, btGattCallback, BluetoothDevice.TRANSPORT_LE);
        return btGatt.connect();
    }

    public boolean disconnected()
    {
        btReady = false;
        if (btGatt == null)
            return false;
        btLECallbacks.disconnected();
        return true;
    }

    public boolean disconnect()
    {
        btReady = false;
        if (btGatt == null)
            return false;
        btGatt.disconnect();
        btLECallbacks.disconnected();
        btGatt = null;
        return true;
    }

    public boolean isReady()
    {
        return btReady;
    }

    public boolean writeCommand(int characteristicNum, byte[] command)
    {
        if (gattCharacteristics.length <= characteristicNum)
            return false;
        if (gattCharacteristics[characteristicNum] == null)
            return false;
        if(!gattCharacteristics[characteristicNum].setValue(command))
            return false;
        return btGatt.writeCharacteristic(gattCharacteristics[characteristicNum]);
    }

    public boolean readResult(int characteristicNum)
    {
        if (gattCharacteristics.length <= characteristicNum)
            return false;
        if (gattCharacteristics[characteristicNum] == null)
            return false;
        return btGatt.readCharacteristic(gattCharacteristics[characteristicNum]);
    }

    public boolean setNotify(int characteristicNum)
    {
        if (gattCharacteristics.length <= characteristicNum)
            return false;
        if (gattCharacteristics[characteristicNum] == null)
            return false;
        return btGatt.setCharacteristicNotification(gattCharacteristics[characteristicNum], true);
    }

}

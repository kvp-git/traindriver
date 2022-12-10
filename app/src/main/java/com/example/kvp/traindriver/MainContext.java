package com.example.kvp.traindriver;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MainContext
{
    private static MainContext mainContext;

    public static MainContext getMainContext(Context context)
    {
        if (mainContext == null)
            mainContext = new MainContext(context);
        return mainContext;
    }

    private HashMap<Integer, DeviceController> devices;

    public MainContext(Context context)
    {
        devices = new HashMap<>();
        devices.put(0, new DeviceController(context, new DeviceDescriptor("test1", "sbrick_btle", "00:11:22:33:FF:EE", 4, "A,B,C,D")));
        devices.put(1, new DeviceController(context, new DeviceDescriptor("test2", "circuitcube_btle", "FC:58:FA:CF:62:70", 3, "A,B,C")));
        devices.put(2, new DeviceController(context, new DeviceDescriptor("test3", "circuitcube_btle", "FC:58:FA:CF:62:70", 2, "AC,B")));
    }

    public DeviceController getDeviceControllerById(int id)
    {
        return devices.get(id);
    }

    public void setDeviceController(int id, DeviceController deviceController)
    {
        devices.put(id, deviceController);
    }

    public DeviceController[] getDevices()
    {
        DeviceController[] da = new DeviceController[devices.size()];
        int t = 0;
        for (Map.Entry<Integer, DeviceController> d : devices.entrySet())
            da[t++] = d.getValue();
        return da;
    }

    public int getDeviceCount()
    {
        return devices.size();
    }
}

package com.example.kvp.traindriver;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
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

    private ArrayList<DeviceController> devices;
    private MutableLiveData<ArrayList<DeviceDescriptor>> editableDeviceList;

    public MainContext(Context context)
    {
        editableDeviceList = new MutableLiveData<>();
        devices = new ArrayList<>();
        devices.add(new DeviceController(context, new DeviceDescriptor("test1", "sbrick_btle", "00:11:22:33:FF:EE", 4, "A,B,C,D")));
        devices.add(new DeviceController(context, new DeviceDescriptor("test2", "circuitcube_btle", "FC:58:FA:CF:62:70", 3, "A,B,C")));
        devices.add(new DeviceController(context, new DeviceDescriptor("test3", "circuitcube_btle", "FC:58:FA:CF:62:70", 2, "AC,B")));
    }

    public DeviceController getDeviceControllerById(int id)
    {
        return devices.get(id);
    }

    public void setDeviceController(int id, DeviceController deviceController)
    {
        devices.set(id, deviceController);
    }

    public DeviceController[] getDevices()
    {
        DeviceController[] da = new DeviceController[devices.size()];
        for (int t = 0; t < devices.size(); t++)
            da[t] = devices.get(t);
        return da;
    }

    public int getDeviceCount()
    {
        return devices.size();
    }

    public void loadEditableDevices()
    {
        ArrayList<DeviceDescriptor> list = new ArrayList<>();
        for (int t = 0; t < devices.size(); t++)
            list.add(new DeviceDescriptor(devices.get(t).deviceDescriptor));
        editableDeviceList.setValue(list);
    }

    public ArrayList<DeviceDescriptor> getEditableDevices()
    {
        return editableDeviceList.getValue();
    }

    public MutableLiveData<ArrayList<DeviceDescriptor>> watchEditableDevices()
    {
        return editableDeviceList;
    }

    public void addEditableDevice()
    {
        ArrayList<DeviceDescriptor> list = editableDeviceList.getValue();
        list.add(new DeviceDescriptor("my device", "none", "11:22:33:44:55", 0, ""));
        editableDeviceList.setValue(list);
    }

    public void deleteEditableDevice(int index)
    {
        ArrayList<DeviceDescriptor> list = editableDeviceList.getValue();
        if ((index < 0) || (index >= list.size()))
            return;
        list.remove(index);
        editableDeviceList.setValue(list);
    }

    public void saveEditableDevices()
    {
        // TODO!!!
    }

}

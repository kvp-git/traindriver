package com.example.kvp.traindriver;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.Log;

import com.example.kvp.traindriver.btscanner.BluetoothDeviceDescriptor;

import org.json.*;

import java.util.ArrayList;

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
    private MutableLiveData<ArrayList<BluetoothDeviceDescriptor>> bluetoothDeviceList;

    public MainContext(Context context)
    {
        editableDeviceList = new MutableLiveData<>();
        bluetoothDeviceList = new MutableLiveData<>();
        devices = new ArrayList<>();
    }

    public boolean loadDevices(Context context)
    {
        devices = new ArrayList<>();
        try
        {
            SharedPreferences sharedPreferences = context.getSharedPreferences("devices", 0);
            String jsonString = sharedPreferences.getString("devices", "");
            if (jsonString.isEmpty())
                return true;
            JSONObject root = new JSONObject(jsonString);
            JSONArray devs = root.getJSONArray("devices");
            for (int n = 0; n < devs.length(); n++)
            {
                JSONObject dev = devs.getJSONObject(n);
                devices.add(new DeviceController(context, new DeviceDescriptor(
                        dev.getString("name"),
                        dev.getString("protocol"),
                        dev.getString("address"),
                        DeviceDescriptor.getChannelCount(dev.getString("channelSetup")),
                        dev.getString("channelSetup")
                )));
            }
        } catch(JSONException e)
        {
            Log.e("MainContext", "Error loading device configurations: " + e.toString(), e);
            return false;
        }
        return true;
        /*
        devices.add(new DeviceController(context, new DeviceDescriptor("test1", "sbrick_btle", "00:11:22:33:FF:EE", 4, "A,B,C,D")));
        devices.add(new DeviceController(context, new DeviceDescriptor("test2", "circuitcube_btle", "FC:58:FA:CF:62:70", 3, "A,B,C")));
        devices.add(new DeviceController(context, new DeviceDescriptor("test3", "circuitcube_btle", "FC:58:FA:CF:62:70", 2, "AC,B")));
        */
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

    public boolean saveEditableDevices(Context context)
    {
        ArrayList<DeviceDescriptor> devices = editableDeviceList.getValue();
        try
        {
            JSONArray devList = new JSONArray();
            for (DeviceDescriptor dd : devices)
            {
                JSONObject device = new JSONObject();
                device.put("name", dd.name);
                device.put("protocol", dd.protocol);
                device.put("address", dd.address);
                device.put("channelSetup", dd.channelSetup);
                devList.put(device);
            }
            JSONObject root = new JSONObject();
            root.put("devices", devList);
            String jsonString = root.toString();
            Log.e("MainContext", "json = " + jsonString);
            SharedPreferences sharedPreferences = context.getSharedPreferences("devices", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("devices", jsonString);
            if (!editor.commit())
                return false;
            return loadDevices(context);
        } catch (JSONException e)
        {
            Log.e("MainContext", "Error saving device configurations: " + e.toString(), e);
            return false;
        }
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

    public void setEditableDeviceAddress(int index, String name, String address)
    {
        ArrayList<DeviceDescriptor> list = editableDeviceList.getValue();
        if ((index < 0) || (index >= list.size()))
            return;
        DeviceDescriptor descriptor = list.get(index);
        descriptor.name = name;
        descriptor.address = address;
        editableDeviceList.setValue(list);
    }

    public MutableLiveData<ArrayList<BluetoothDeviceDescriptor>> watchBluetoothDeviceList()
    {
        return bluetoothDeviceList;
    }

    public void clearBluetoothDeviceList()
    {
        ArrayList<BluetoothDeviceDescriptor> list = new ArrayList<>();
        bluetoothDeviceList.setValue(list);
    }

    public void addBluetoothDeviceList(String name, String address, String info)
    {
        ArrayList<BluetoothDeviceDescriptor> list = bluetoothDeviceList.getValue();
        boolean found = false;
        for (BluetoothDeviceDescriptor dev : list)
            if (dev.address.compareTo(address) == 0)
            {
                found = true;
                dev.name = name;
                dev.info = info;
            }
        if (!found)
            list.add(new BluetoothDeviceDescriptor(name, address, info));
        bluetoothDeviceList.setValue(list);
    }

    public BluetoothDeviceDescriptor getBluetoothDeviceDescriptor(int position)
    {
        ArrayList<BluetoothDeviceDescriptor> list = bluetoothDeviceList.getValue();
        if ((position < 0) || (position >= list.size()))
            return null;
        return list.get(position);
    }

    public int getBluetoothDeviceListSize()
    {
        ArrayList<BluetoothDeviceDescriptor> list = bluetoothDeviceList.getValue();
        return list.size();
    }

}

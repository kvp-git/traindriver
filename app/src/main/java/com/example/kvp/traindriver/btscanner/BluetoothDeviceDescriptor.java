package com.example.kvp.traindriver.btscanner;

public class BluetoothDeviceDescriptor
{
    public String name;
    public String address;
    public String info;

    public BluetoothDeviceDescriptor(String name, String address, String info)
    {
        this.name = name;
        this.address = address;
        this.info = info;
    }
}

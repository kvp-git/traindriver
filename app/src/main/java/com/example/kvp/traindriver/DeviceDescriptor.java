package com.example.kvp.traindriver;

public class DeviceDescriptor
{
    public String name;
    public String protocol;
    public String address;
    public int channelCount;
    public String channelSetup;

    public DeviceDescriptor(String name, String protocol, String address, int channelCount, String channelSetup)
    {
        this.name = name;
        this.protocol = protocol;
        this.address = address;
        this.channelCount = channelCount;
        this.channelSetup = channelSetup;
    }
}

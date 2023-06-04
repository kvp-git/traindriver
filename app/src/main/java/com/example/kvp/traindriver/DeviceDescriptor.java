package com.example.kvp.traindriver;

import java.util.ArrayList;
import java.util.List;

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

    public DeviceDescriptor(DeviceDescriptor dd)
    {
        this.name = dd.name;
        this.protocol = dd.protocol;
        this.address = dd.address;
        this.channelCount = dd.channelCount;
        this.channelSetup = dd.channelSetup;
    }

    public static List<String> getProtocols()
    {
        ArrayList<String> list = new ArrayList<>();
        list.add("sbrick_btle");
        list.add("circuitcube_btle");
        return list;
    }

    public static List<String> getChannels(String protocol)
    {
        ArrayList<String> list = new ArrayList<>();
        switch (protocol)
        {
            case "sbrick_btle":
                list.add("A,B,C,D");
                list.add("AB,C,D");
                break;
            case "circuitcube_btle":
                list.add("A,B,C");
                list.add("AC,B");
                list.add("AB,C");
                list.add("A,BC");
                break;
        }
        return list;
    }

    public static int getChannelCount(String channelSetup)
    {
        switch (channelSetup)
        {
            case "A,B,C,D": return 4;
            case "AB,C,D": return 3;
            case "A,B,C": return 3;
            case "AC,B": return 2;
            case "AB,C": return 2;
            case "A,BC": return 2;
        }
        return 0;
    }

    public static int getTypeSelection(String protocol)
    {
        switch (protocol)
        {
            case "sbrick_btle": return 0;
            case "circuitcube_btle": return 1;
        }
        return 0;
    }

    public static int getChannelSelection(String protocol, String channelSetup)
    {
        switch (protocol)
        {
            case "sbrick_btle":
                switch (channelSetup)
                {
                    case "A,B,C,D": return 0;
                    case "AB,C,D": return 1;
                }
                break;
            case "circuitcube_btle":
                switch (channelSetup)
                {
                    case "A,B,C": return 0;
                    case "AC,B": return 1;
                    case "AB,C": return 2;
                    case "A,BC": return 3;
                }
                break;
        }
        return 0;
    }

}

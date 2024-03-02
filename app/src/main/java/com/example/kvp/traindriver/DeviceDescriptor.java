package com.example.kvp.traindriver;

import java.util.ArrayList;
import java.util.List;

public class DeviceDescriptor
{
    public String name;
    public String protocol;
    public String address;
    public String password;
    public int channelCount;
    public String channelSetup;

    public DeviceDescriptor(String name, String protocol, String address, String password, int channelCount, String channelSetup)
    {
        this.name = name;
        this.protocol = protocol;
        this.address = address;
        this.password = password;
        this.channelCount = channelCount;
        this.channelSetup = channelSetup;
    }

    public DeviceDescriptor(DeviceDescriptor dd)
    {
        this.name = dd.name;
        this.protocol = dd.protocol;
        this.address = dd.address;
        this.password = dd.password;
        this.channelCount = dd.channelCount;
        this.channelSetup = dd.channelSetup;
    }

    public static List<String> getProtocols()
    {
        ArrayList<String> list = new ArrayList<>();
        list.add("sbrick_btle");
        list.add("circuitcube_btle");
        list.add("lego_btle");
        list.add("kvp_utp_train");
        list.add("kvp_utp_signal");
        return list;
    }

    public static List<String> getChannels(String protocol)
    {
        ArrayList<String> list = new ArrayList<>();
        switch (protocol)
        {
            case "lego_btle":
                list.add("A,B");
                list.add("AB");
                list.add("A-B");
                list.add("A,B,C");
                list.add("AB,C");
                list.add("A-B,C");
                break;
            case "sbrick_btle":
            case "kvp_utp_train":
                list.add("A,B,C,D");
                list.add("AB,C,D");
                list.add("AB-,C,D");
                break;
            case "kvp_utp_signal":
                list.add("A"); // TODO!!! change this to signal select dropdown "[R,YY,YG,etc.]"
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
        // == number of ','-s in string + 1
        if (channelSetup.isEmpty())
            return 0;
        int cnt = 0;
        for (int t = 0; t < channelSetup.length(); t++)
            if (channelSetup.charAt(t) == ',')
                cnt++;
        return (cnt + 1);
    }

    public static int getTypeSelection(String protocol)
    {
        switch (protocol)
        {
            case "sbrick_btle": return 0;
            case "circuitcube_btle": return 1;
            case "lego_btle": return 2;
            case "kvp_utp_train": return 3;
            case "kvp_utp_signal": return 4;
        }
        return 0;
    }

    public static int getChannelSelection(String protocol, String channelSetup)
    {
        switch (protocol)
        {
            case "lego_btle":
                switch (channelSetup)
                {
                    case "A,B": return 0;
                    case "AB": return 1;
                    case "A-B": return 2;
                    case "A,B,C": return 3;
                    case "AB,C": return 4;
                    case "A-B,C": return 5;
                }
                break;
            case "sbrick_btle":
            case "kvp_utp_train":
                switch (channelSetup)
                {
                    case "A,B,C,D": return 0;
                    case "AB,C,D": return 1;
                    case "AB-,C,D": return 2;
                }
                break;
            case "kvp_utp_signal":
                switch (channelSetup)
                {
                    case "A": return 0;
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

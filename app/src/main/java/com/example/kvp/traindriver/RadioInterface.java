package com.example.kvp.traindriver;

import android.content.Context;

public interface RadioInterface
{
    // set DeviceDescriptor and DeviceController in the constructor
    boolean connect(Context context);
    boolean disconnect(Context context);
    boolean setChannels(Context context);
    boolean getChargePercent(Context context);
}

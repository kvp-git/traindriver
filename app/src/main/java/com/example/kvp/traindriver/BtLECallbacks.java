package com.example.kvp.traindriver;

public interface BtLECallbacks
{
    void connected();
    void disconnected();
    void readDone(int status, int characteristicNum, byte[] value);
    void writeDone(int status);
    void dataChanged(int characteristicNum, byte[] value);
}

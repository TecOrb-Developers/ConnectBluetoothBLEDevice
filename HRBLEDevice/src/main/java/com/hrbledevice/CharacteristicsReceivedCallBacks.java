package com.hrbledevice;

public interface CharacteristicsReceivedCallBacks {

    void onRotationCountReading(String value);

    void onBatteryReading(String value);

    void onConnected(String success);
}

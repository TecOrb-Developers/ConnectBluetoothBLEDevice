package com.hr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hrbledevice.CharacteristicsReceivedCallBacks;

public class CharacteristicsReceivedBroadcast extends BroadcastReceiver {
    private String batteryReading;
    private String rotationCount;
    private String connection;
    private CharacteristicsReceivedCallBacks callBacks;

    @Override
    public void onReceive(Context context, Intent intent) {

        batteryReading = intent.getStringExtra("battery");
        rotationCount = intent.getStringExtra("count");
        connection = intent.getStringExtra("status");
        if (!HRValidationHelper.isNull(rotationCount)) {
            callBacks.onRotationCountReading(rotationCount);
        } else if (!HRValidationHelper.isNull(batteryReading)) {
            callBacks.onBatteryReading(batteryReading);
        } else if (!HRValidationHelper.isNull(connection)) {
            callBacks.onConnected(connection);
        }
    }

    public void addListener(CharacteristicsReceivedCallBacks callBack) {
        this.callBacks = callBack;
    }

    public void removeListener() {
        this.callBacks = null;
    }

}

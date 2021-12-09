package com.hr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;

import com.hr.databinding.ActivityConnectDeviceBinding;
import com.hrbledevice.BLEConnectionService;
import com.hrbledevice.CharacteristicsReceivedCallBacks;
import com.hrbledevice.HRAppConstants;

public class ActivityConnectDevice extends AppCompatActivity implements View.OnClickListener, CharacteristicsReceivedCallBacks {
    private Context context;
    private ActivityConnectDeviceBinding binding;
    private String macAddress;
    private CharacteristicsReceivedBroadcast characteristicsReceivedBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connect_device);
        context = this;
        binding.buttonReadValue.setEnabled(false);
        binding.buttonWriteValue.setEnabled(false);
        binding.buttonBatteryLevel.setEnabled(false);
        binding.buttonWriteValue.setOnClickListener(this);
        binding.buttonReadValue.setOnClickListener(this);
        binding.buttonBatteryLevel.setOnClickListener(this);

        if (getIntent() != null) {
            macAddress = getIntent().getStringExtra("macAddress");
            if (macAddress != null) bindService(macAddress);
        }
        registerActivityForCharacteristics();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonReadValue:
                Intent sound = new Intent(HRAppConstants.READ_CHARACTERISTIC_BROADCAST);
                sound.putExtra("state", HRAppConstants.BUZZER_START_TO_WORK);
                context.sendBroadcast(sound);
                break;

            case R.id.buttonWriteValue:
                Intent write = new Intent(HRAppConstants.READ_CHARACTERISTIC_BROADCAST);
                write.putExtra("state", HRAppConstants.CHECK_NUMBER_ROTATIONS_CHARACTERISTIC);
                context.sendBroadcast(write);
                break;

            case R.id.buttonBatteryLevel:
                Intent battery = new Intent(HRAppConstants.READ_CHARACTERISTIC_BROADCAST);
                battery.putExtra("state", HRAppConstants.CLICK_BATTERY);
                context.sendBroadcast(battery);
                break;
        }
    }

    private void bindService(String address) {
        if (locationServiceConnection != null) {
            Intent intent = new Intent(this, BLEConnectionService.class);
            intent.putExtra("deviceAddress", address);
            context.bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    private void unBindService() {
        try {
            unbindService(locationServiceConnection);
            locationServiceConnection = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder mService) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
    }

    @Override
    public void onRotationCountReading(String value) {
        if (!HRValidationHelper.isNull(value)){
            binding.textView.setText("Count : "+value);
        }
    }

    @Override
    public void onBatteryReading(String value) {
        if (!HRValidationHelper.isNull(value)){
            binding.textView3.setText("Battery Level : "+value+"%");
        }
    }

    @Override
    public void onConnected(String success) {
        if (!HRValidationHelper.isNull(success)){
            binding.buttonReadValue.setEnabled(Boolean.parseBoolean(success));
            binding.buttonWriteValue.setEnabled(Boolean.parseBoolean(success));
            binding.buttonBatteryLevel.setEnabled(Boolean.parseBoolean(success));
            if (Boolean.parseBoolean(success)){
                binding.textView4.setText("Connected");
                binding.textView4.setTextColor(Color.GREEN);
            }else {
                binding.textView4.setText("Failed");
                binding.textView4.setTextColor(Color.RED);
            }
        }
    }

    public void registerActivityForCharacteristics() {
        characteristicsReceivedBroadcast = new CharacteristicsReceivedBroadcast();
        if (this instanceof CharacteristicsReceivedCallBacks) {
            characteristicsReceivedBroadcast.addListener((CharacteristicsReceivedCallBacks) this);
        }
        this.registerReceiver(characteristicsReceivedBroadcast, new IntentFilter(HRAppConstants.CHARACTERISTIC_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (characteristicsReceivedBroadcast != null) {
            try {
                this.unregisterReceiver(characteristicsReceivedBroadcast);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            this.registerReceiver(characteristicsReceivedBroadcast,
                    new IntentFilter(HRAppConstants.CHARACTERISTIC_BROADCAST));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
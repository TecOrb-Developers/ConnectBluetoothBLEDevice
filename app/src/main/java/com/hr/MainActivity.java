package com.hr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hr.databinding.ActivityMainBinding;
import com.hrbledevice.DialogScanningUUID;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        context = this;
        binding.viewConnect.setOnClickListener(this);
    }

    private void allowPermission(){
        TedPermission.with(MainActivity.this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        try {
                            DialogScanningUUID.newInstance("", "", "Smart-Ring", new DialogScanningUUID.ConnectDeviceCallBack() {
                                @Override
                                public void onRequestToBLEDevice(String macAddress) {
                                    startActivity(new Intent(context,ActivityConnectDevice.class).putExtra("macAddress",macAddress));
                                    finish();
                                }

                                @Override
                                public void onCloseCalled() {

                                }
                            }).show(getSupportFragmentManager(),"BleDevice");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {}
                })
                .setDeniedMessage("BLE device can not connect without permission please Enable.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.viewConnect:
                allowPermission();
                break;
        }
    }
}
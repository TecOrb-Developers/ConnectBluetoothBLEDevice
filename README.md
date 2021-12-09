# ConnectBluetoothBLEDevice
## Connect BLE device using bluetooth with android phone

![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)


An application can scan for a particular type of Bluetooth LE devices using ScanFilter . 
It can also request different types of callbacks for delivering the result.

## Features

- Connect device with device name.
- Connect device with Mac address.
- Connect device with service UUID.
- When device connected it will run in background.
- Also it will show the connection status.
- Can read and write data to device.
- Can get battery level of device.


# How it works:

1. Gradle Dependency

- Add the JitPack repository to your project's build.gradle file

```groovy
    allprojects {
        repositories {
            ...
    	    maven { url 'https://jitpack.io' }
        }
    }
```
- Add the dependency in your app's build.gradle file

```groovy
    dependencies {
        implementation 'com.github.TecOrb-Developers:ConnectBluetoothBLEDevice:0.1.0'
    }
```

2. Add permission 
```
- Manifest.permission.ACCESS_FINE_LOCATION
- Manifest.permission.BLUETOOTH
- Manifest.permission.BLUETOOTH_ADMIN
- Manifest.permission.FOREGROUND_SERVICE
```

3. Connect device with name
```
 DialogScanningUUID.
       newInstance("", "", "device name here", 
               new DialogScanningUUID.ConnectDeviceCallBack() {
                        @Override
                        public void onRequestToBLEDevice(String macAddress) {
                              startActivity(new Intent(context,ActivityConnectDevice.class)
                              .putExtra("macAddress",macAddress));
                              finish();
                            }
                         @Override
                         public void onCloseCalled() {}
                }).show(getSupportFragmentManager(),"BleDevice");
```
4. Connect device with Mac address
```
 DialogScanningUUID.
       newInstance("", "enter address here", "", 
               new DialogScanningUUID.ConnectDeviceCallBack() {
                        @Override
                        public void onRequestToBLEDevice(String macAddress) {
                              startActivity(new Intent(context,ActivityConnectDevice.class)
                              .putExtra("macAddress",macAddress));
                              finish();
                            }
                         @Override
                         public void onCloseCalled() {}
                }).show(getSupportFragmentManager(),"BleDevice");
```
5. Connect device with UUID
```
 DialogScanningUUID.
       newInstance("enter UUID", "", "", 
               new DialogScanningUUID.ConnectDeviceCallBack() {
                        @Override
                        public void onRequestToBLEDevice(String macAddress) {
                              startActivity(new Intent(context,ActivityConnectDevice.class)
                              .putExtra("macAddress",macAddress));
                              finish();
                            }
                         @Override
                         public void onCloseCalled() {}
                }).show(getSupportFragmentManager(),"BleDevice");
```

5. Call backs returns
```
- CharacteristicsReceivedCallBacks

@Override
    public void onRotationCountReading(String count) {
        if (!HRValidationHelper.isNull(count)){
            binding.textView.setText("Total Count : "+count);
        }
    }

    @Override
    public void onBatteryReading(String batteryLevel) {
        if (!HRValidationHelper.isNull(batteryLevel)){
            binding.textView3.setText("Battery Level : "+batteryLevel+"%");
        }
    }

    @Override
    public void onConnected(String connectionStatus) {
        if (!HRValidationHelper.isNull(connectionStatus)){
            if (Boolean.parseBoolean(connectionStatus)){
                binding.textView4.setText("Connected");
                binding.textView4.setTextColor(Color.GREEN);
            }else {
                binding.textView4.setText("Failed");
                binding.textView4.setTextColor(Color.RED);
            }
        }
    }

```
 
# Developers

MIT License

Copyright (c) 2019 TecOrb Technologies

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

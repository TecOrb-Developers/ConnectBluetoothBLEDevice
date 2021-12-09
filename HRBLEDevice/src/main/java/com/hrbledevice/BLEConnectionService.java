package com.hrbledevice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;
import static com.hrbledevice.UUIDUtils.batteryCharacteristicUUID;
import static com.hrbledevice.UUIDUtils.batteryServiceUUID;
import static com.hrbledevice.UUIDUtils.bluetoothDataResponseCharacteristicUUID;
import static com.hrbledevice.UUIDUtils.bluetoothDataResponseDescriptorUUID;
import static com.hrbledevice.UUIDUtils.burzzerAndVibratorMotoCharacteristicUUID;
import static com.hrbledevice.UUIDUtils.checkOrClearRotationCharacteristicUUID;
import static com.hrbledevice.UUIDUtils.particleLEDServiceUUID;

public class BLEConnectionService extends Service {

    private Notification notification;
    public static NotificationCompat.Builder notificationBuilder;
    public static NotificationManager notificationManager;
    private String deviceAddress;
    private static final int ID_SERVICE = 101;
    private Context mContext;
    private String mDeviceAddress;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private static final String TAG = BLEConnectionService.class.getSimpleName();
    public static boolean isDeviceConnected;


    public class LocalBinder extends Binder {
        public BLEConnectionService getService() {
            return BLEConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "com.ble.notification";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            NotificationChannel mChannel = new NotificationChannel("com.ble.notification", "com.ble.notification", importance);
            mChannel.enableLights(true);
            mChannel.setSound(defaultSoundUri, audioAttributes);
            mChannel.setLightColor(Color.RED);
            notificationManager.createNotificationChannel(mChannel);
        }


        notificationBuilder = new NotificationCompat.Builder(this, channelId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.cross)
                    .setContentTitle("Smart-Ring")
                    .setContentText("Connecting....")
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_PROMO)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setChannelId(channelId)
                    .setOnlyAlertOnce(true)
                    .build();
        } else {
            notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.cross)
                    .setPriority(PRIORITY_HIGH)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_PROMO)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle("Smart-Ring")
                    .setContentText("Connecting....")
                    .setOnlyAlertOnce(true)
                    .build();
        }


        startForeground(ID_SERVICE, notification);
    }

    private void initProcess() {
        onCreate(BLEConnectionService.this, deviceAddress);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = "com.ble.notification";
        String channelName = "Bledevice";
        NotificationChannel channel = new NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        deviceAddress = intent.getStringExtra("deviceAddress");
        initProcess();
        return new LocalBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mBluetoothManager != null) {
                BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
                if (bluetoothAdapter.isEnabled()) {
                    stopClient();
                }
            }
            mContext.unregisterReceiver(mBluetoothReceiver);
            mContext.unregisterReceiver(writeCharacteristicsReceiver);
            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT client. Attempting to start service discovery");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT client");

                Intent locationIntent = new Intent(HRAppConstants.CHARACTERISTIC_BROADCAST);
                locationIntent.putExtra("battery", "null");
                locationIntent.putExtra("count", "null");
                locationIntent.putExtra("status", "false");
                sendBroadcast(locationIntent);
                isDeviceConnected = false;
                if (notification != null && notificationBuilder != null) {
                    notificationBuilder.setContentText("Disconnect");
                    notificationManager.notify(ID_SERVICE, notificationBuilder.build());
                }


                gatt.close();
                startClient();

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                BluetoothGattService serviceMain = gatt.getService(particleLEDServiceUUID);
                BluetoothGattService serviceBattery = gatt.getService(batteryServiceUUID);
                if (serviceMain != null) {

                    BluetoothGattCharacteristic characteristicDataResponse = serviceMain.getCharacteristic(bluetoothDataResponseCharacteristicUUID);
                    if (characteristicDataResponse != null) {
                        mBluetoothGatt.setCharacteristicNotification(characteristicDataResponse, true);
                        BluetoothGattDescriptor descriptor = characteristicDataResponse.getDescriptor(bluetoothDataResponseDescriptorUUID);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(descriptor);
                        }
                    }
                }
                BluetoothGattCharacteristic characteristicBatteryResponse = serviceBattery.getCharacteristic(batteryCharacteristicUUID);

                if (characteristicBatteryResponse != null) {
                    mBluetoothGatt.setCharacteristicNotification(characteristicBatteryResponse, true);
                    BluetoothGattDescriptor descriptor = characteristicBatteryResponse.getDescriptor(bluetoothDataResponseDescriptorUUID);
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(descriptor);
                    }
                }
                Intent locationIntent = new Intent(HRAppConstants.CHARACTERISTIC_BROADCAST);
                locationIntent.putExtra("battery", "null");
                locationIntent.putExtra("count", "null");
                locationIntent.putExtra("status", "true");
                sendBroadcast(locationIntent);
                isDeviceConnected = true;

                if (notification != null && notificationBuilder != null) {
                    notificationBuilder.setContentText("Connected");
                    notificationManager.notify(ID_SERVICE, notificationBuilder.build());
                }

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                Intent locationIntent = new Intent(HRAppConstants.CHARACTERISTIC_BROADCAST);
                locationIntent.putExtra("battery", "null");
                locationIntent.putExtra("count", "null");
                locationIntent.putExtra("status", "false");
                sendBroadcast(locationIntent);
                isDeviceConnected = false;

                if (notification != null && notificationBuilder != null) {
                    notificationBuilder.setContentText("Disconnect");
                    notificationManager.notify(ID_SERVICE, notificationBuilder.build());
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            readCounterCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            readCounterCharacteristic(characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (batteryServiceUUID.equals(descriptor.getUuid())) {
                BluetoothGattCharacteristic characteristic = gatt.getService(batteryServiceUUID).getCharacteristic(batteryCharacteristicUUID);
                gatt.readCharacteristic(characteristic);
            } else if (bluetoothDataResponseDescriptorUUID.equals(descriptor.getUuid())) {
                BluetoothGattCharacteristic characteristic = gatt.getService(particleLEDServiceUUID).getCharacteristic(checkOrClearRotationCharacteristicUUID);
                gatt.readCharacteristic(characteristic);
            }
        }

        private void readCounterCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (batteryCharacteristicUUID.equals(characteristic.getUuid())) {
                batteryData(characteristic);
            } else if (bluetoothDataResponseCharacteristicUUID.equals(characteristic.getUuid())) {
                responseData(characteristic);
            }
        }
    };

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startClient();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopClient();
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
    };

    public void onCreate(Context context, String deviceAddress) throws RuntimeException {
        mContext = context;
        mDeviceAddress = deviceAddress;

        mBluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (!checkBluetoothSupport(mBluetoothAdapter)) {
            throw new RuntimeException("GATT client requires Bluetooth support");
        }


        mContext.registerReceiver(writeCharacteristicsReceiver,
                new IntentFilter(HRAppConstants.READ_CHARACTERISTIC_BROADCAST));

        // Register for system Bluetooth events
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBluetoothReceiver, filter);
        if (!mBluetoothAdapter.isEnabled()) {
            Log.w(TAG, "Bluetooth is currently disabled... enabling");
            mBluetoothAdapter.enable();
        } else {
            Log.i(TAG, "Bluetooth enabled... starting client");
            startClient();
        }
    }

    private boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth is not supported");
            return false;
        }

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Bluetooth LE is not supported");
            return false;
        }
        return true;
    }

    private void startClient() {
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        mBluetoothGatt = bluetoothDevice.connectGatt(mContext, false, mGattCallback);
    }

    private void stopClient() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter = null;
        }
    }

    /*========================= Write Characteristics =====================*/

    public void checkNumberRotationsCharacteristic() {
        //0xb1f301
        try {
            BluetoothGattCharacteristic interactor = mBluetoothGatt
                    .getService(particleLEDServiceUUID)
                    .getCharacteristic(checkOrClearRotationCharacteristicUUID);
            interactor.setValue(new byte[]{(byte) 0xb1, (byte) 0xf3, 0x01});

            BluetoothGattCharacteristic characteristicDataResponse = mBluetoothGatt.getService(particleLEDServiceUUID).getCharacteristic(bluetoothDataResponseCharacteristicUUID);
            if (characteristicDataResponse != null) {
                mBluetoothGatt.setCharacteristicNotification(characteristicDataResponse, true);
            }
            mBluetoothGatt.writeCharacteristic(interactor);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void buzzerStartToWork() {
        //0xb1f20101
        try {
            BluetoothGattCharacteristic interactor = mBluetoothGatt
                    .getService(particleLEDServiceUUID)
                    .getCharacteristic(burzzerAndVibratorMotoCharacteristicUUID);
            interactor.setValue(new byte[]{(byte) 0xb1, (byte) 0xf2, 0x01, 0x01});
            mBluetoothGatt.writeCharacteristic(interactor);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void clickBattery() {
        try {
            BluetoothGattCharacteristic characteristicBatteryResponse = mBluetoothGatt.getService(batteryServiceUUID)
                    .getCharacteristic(batteryCharacteristicUUID);
            mBluetoothGatt.readCharacteristic(characteristicBatteryResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void responseData(BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        int totalCount = 0;
        int firstBitValue = data[0];
        if (firstBitValue <= 0) {
            totalCount = data[0];
        } else {
            for (int i = 0; i < data.length; i++) {
                if (i == 4) {
                    totalCount += unsignedToBytes(data[i]);
                }
            }
        }
        Intent locationIntent = new Intent(HRAppConstants.CHARACTERISTIC_BROADCAST);
        locationIntent.putExtra("battery", "null");
        locationIntent.putExtra("count", String.valueOf(totalCount));
        locationIntent.putExtra("status", "null");
        sendBroadcast(locationIntent);
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    private void batteryData(BluetoothGattCharacteristic characteristic) {

        int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        Log.d(TAG, "battery level: " + batteryLevel);

        byte[] data = characteristic.getValue();

        int firstBitValue = data[0]; //& 0x64;

        Intent locationIntent = new Intent(HRAppConstants.CHARACTERISTIC_BROADCAST);
        locationIntent.putExtra("battery", String.valueOf(firstBitValue));
        locationIntent.putExtra("count", "null");
        locationIntent.putExtra("status", "null");
        sendBroadcast(locationIntent);

    }


    private final BroadcastReceiver writeCharacteristicsReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", 0);
            switch (state) {

                case HRAppConstants.CHECK_NUMBER_ROTATIONS_CHARACTERISTIC:
                    checkNumberRotationsCharacteristic();
                    break;

                case HRAppConstants.BUZZER_START_TO_WORK:
                    buzzerStartToWork();
                    break;

                case HRAppConstants.CLICK_BATTERY:
                    clickBattery();
                    break;


                default:
                    break;
            }
        }
    };


}

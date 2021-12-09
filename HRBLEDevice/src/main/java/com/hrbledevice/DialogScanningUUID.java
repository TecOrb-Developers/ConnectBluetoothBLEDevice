package com.hrbledevice;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hrbledevice.databinding.DialogSerchingDeviceBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class DialogScanningUUID extends BottomSheetDialogFragment implements View.OnClickListener {

    private Context context;
    private DialogSerchingDeviceBinding binding;
    private ConnectDeviceCallBack callBack;
    private String macAddress;
    private BluetoothAdapter mBTAdapter;
    private static BluetoothDevice device;
    private BluetoothLeScanner scanner;
    private final static int REQUEST_ENABLE_BT = 1;
    private Handler handler;
    private boolean isFoundDevice;
    private int initialRotationValue = 10;
    private String deviceName;
    private String givenMacAddress;
    private String connectDeviceUUID;

    public static DialogScanningUUID newInstance(String connectDeviceUUID,
                                                 String givenMacAddress,
                                                 String deviceName,
                                                 ConnectDeviceCallBack callBack) {
        return new DialogScanningUUID(connectDeviceUUID,givenMacAddress,deviceName,callBack);
    }

    private DialogScanningUUID(String connectDeviceUUID,
                               String givenMacAddress,
                               String deviceName,
                               ConnectDeviceCallBack callBack) {
        this.callBack = callBack;
        this.connectDeviceUUID = connectDeviceUUID;
        this.givenMacAddress = givenMacAddress;
        this.deviceName = deviceName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //bottom sheet round corners can be obtained but the while background appears to remove that we need to add this.
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_serching_device, container, false);

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    binding.getRoot().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                FrameLayout bottomSheet = (FrameLayout) dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        setCancelable(false);

        binding.viewBtnConnect.setOnClickListener(this);
        binding.viewClose.setOnClickListener(this);
        binding.viewBtnConnect.setEnabled(false);
        binding.viewBtnConnect.setAlpha(0.4f);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        scanner = mBTAdapter.getBluetoothLeScanner();// add this
        handler = new Handler();
        scheduleSendLocation();



        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(context, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (deviceName!=null){
                    discoverDeviceByName(deviceName);
                }else if (givenMacAddress!=null){
                    discoverDeviceByMacAddress(givenMacAddress);
                }else if (connectDeviceUUID!=null){
                    discoverDeviceByUUID(connectDeviceUUID);
                }else {
                    Toast.makeText(context, "Choose Connected device by", Toast.LENGTH_SHORT).show();
                }

            }
        }

    }

    public void scheduleSendLocation() {
        handler.postDelayed(new Runnable() {
            public void run() {
                initialRotationValue = initialRotationValue+10;
                binding.imageView9.animate().rotation(initialRotationValue).start();

                if (isFoundDevice){
                    handler.removeCallbacks(this);
                    return;
                }
                handler.postDelayed(this, 500);

            }
        }, 100);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void discoverDeviceByUUID(String connectDeviceUUID) {

        UUID particleLEDServiceUUID = UUID.fromString(connectDeviceUUID);
        UUID[] serviceUUIDs = new UUID[]{particleLEDServiceUUID};
        List<ScanFilter> filters = null;
        if(serviceUUIDs != null) {
            filters = new ArrayList<>();
            for (UUID serviceUUID : serviceUUIDs) {
                ScanFilter filter = new ScanFilter.Builder()
                        .setServiceUuid(new ParcelUuid(serviceUUID))
                        .build();
                filters.add(filter);
            }
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // its call multiple time depends on device packets sends
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH) // its call Scan result once when device found
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT) //One advertisement is enough for a match
                .setReportDelay(0L)
                .build();

        if (scanner!=null)
        scanner.startScan(filters, scanSettings, scanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void discoverDeviceByName(String deviceName) {
        String[] names = new String[]{deviceName};
        List<ScanFilter> filters = null;
        if(names != null) {
            filters = new ArrayList<>();
            for (String name : names) {
                ScanFilter filter = new ScanFilter.Builder()
                        .setDeviceName(name)
                        .build();
                filters.add(filter);
            }
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // its call multiple time depends on device packets sends
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH) // its call Scan result once when device found
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT) //One advertisement is enough for a match
                .setReportDelay(0L)
                .build();

        if (scanner!=null)
            scanner.startScan(filters, scanSettings, scanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void discoverDeviceByMacAddress(String givenMacAddress) {
        String[] peripheralAddresses = new String[]{givenMacAddress};// Build filters list
        List<ScanFilter> filters = null;
        if (peripheralAddresses != null) {
            filters = new ArrayList<>();
            for (String address : peripheralAddresses) {
                ScanFilter filter = new ScanFilter.Builder()
                        .setDeviceAddress(address)
                        .build();
                filters.add(filter);
            }
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // its call multiple time depends on device packets sends
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH) // its call Scan result once when device found
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT) //One advertisement is enough for a match
                .setReportDelay(0L)
                .build();

        if (scanner!=null)
            scanner.startScan(filters, scanSettings, scanCallback);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            device = result.getDevice();
            if (device != null /*&& !HRValidationHelper.isNull(device.getName()) && device.getName().equalsIgnoreCase("Smart-Ring")*/) {
                isFoundDevice = true;
                binding.viewSearching.setText(device.getName());
                macAddress = device.getAddress();
                binding.viewBtnConnect.setEnabled(true);
                binding.viewBtnConnect.setAlpha(1f);
                if (scanner!=null) scanner.stopScan(scanCallback);

            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            // Ignore for now
            Log.d("===>","==> "+results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            // Ignore for now
            Log.d("===>","==> "+errorCode);
        }
    };

    private void dismissDialog() {
        if (!((Activity) context).isFinishing()) {
            if (callBack!=null) callBack.onCloseCalled();
            dismiss();
        }
    }

    public interface ConnectDeviceCallBack {
        void onRequestToBLEDevice(String macAddress);

        void onCloseCalled();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.viewBtnConnect) {
            if (callBack != null && macAddress!=null) {
                callBack.onRequestToBLEDevice(macAddress);
                dismissDialog();
            }
        } else if (id == R.id.viewClose) {
            dismissDialog();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (deviceName!=null){
                        discoverDeviceByName(deviceName);
                    }else if (givenMacAddress!=null){
                        discoverDeviceByMacAddress(givenMacAddress);
                    }else if (connectDeviceUUID!=null){
                        discoverDeviceByUUID(connectDeviceUUID);
                    }else {
                        Toast.makeText(context, "Choose Connected device by", Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(context, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, Data);
    }
}

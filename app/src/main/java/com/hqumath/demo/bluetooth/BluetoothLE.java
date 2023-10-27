package com.hqumath.demo.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.hqumath.demo.R;
import com.hqumath.demo.app.AppExecutors;
import com.hqumath.demo.utils.CommonUtil;
import com.hqumath.demo.utils.PermissionUtil;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.security.auth.callback.Callback;

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 17:00
 * 文件描述: 经典蓝牙
 * 注意事项:
 * ****************************************************************
 */
public class BluetoothLE {
    public static final String TAG = "BluetoothClassic";
    public static final int REQUEST_ENABLE_BT = 11;
    public static final int REQUEST_ENABLE_GPS = 12;

    private Activity mContext;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLE.OnBluetoothListener onBluetoothListener;

    public BluetoothLE() {

    }

    public void init(Activity context) {
        mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//高版本已经弃用
        }
    }

    public void setOnBluetoothListener(BluetoothLE.OnBluetoothListener onBluetoothListener) {
        this.onBluetoothListener = onBluetoothListener;
    }

    /**
     * 扫描&检查权限
     */
    @SuppressLint("WrongConstant")
    public void scanWithPermission() {
        //申请权限
        String[] allPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            allPermission = new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else {
            allPermission = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        }
        if (PermissionUtil.checkPermission(mContext, allPermission)) {
            scan();
        } else {
            AndPermission.with(mContext)
                    .runtime()
                    .permission(allPermission)
                    .onGranted((permissions) -> {
                        scan();
                    })
                    .onDenied((permissions) -> {//关闭权限且不再询问
                        if (AndPermission.hasAlwaysDeniedPermission(mContext, permissions)) {
                            PermissionUtil.showSettingDialog(mContext, permissions);
                        }
                    }).start();
        }
    }

    /**
     * 扫描
     */
    @SuppressLint("MissingPermission")
    public void scan() {
        if ((bluetoothAdapter == null)) {
            CommonUtil.toast(R.string.bluetooth_not_support);
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {//打开蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            CommonUtil.toast(R.string.bluetooth_not_open);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                && !CommonUtil.isGpsOpen()) {//打开定位
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivityForResult(intent, REQUEST_ENABLE_GPS);
            CommonUtil.toast(R.string.location_not_open);
            return;
        }
        //查询已配对设备
        if (onBluetoothListener != null)
            onBluetoothListener.onGetBondedDevices(bluetoothAdapter.getBondedDevices());
        //扫描 大概12s,扫描中不能立即重新扫描 TODO
        /*if (!bluetoothAdapter.isDiscovering()) {
            if (onBluetoothListener != null)
                onBluetoothListener.onScanStart();
            bluetoothAdapter.startDiscovery();
        }*/
        bluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {

            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                result.getDevice();
            }

            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);

            }

            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        });

    }

    /**
     * 连接设备。
     * 已配对的取消配对，配对后自动连接。
     */
    @SuppressLint("MissingPermission")
    public void connectDevice(BluetoothDevice device) {
        if (bluetoothAdapter != null) {//取消扫描
            bluetoothAdapter.cancelDiscovery();
        }
        CommonUtil.toast(R.string.bluetooth_is_connecting);
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            removeBondDevice(device);//取消配对
            AppExecutors.getInstance().scheduledWork().schedule(() -> {
                device.createBond();//延时配对
            }, 1, TimeUnit.SECONDS);
        } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            device.createBond();//配对
        }
    }

    public void release() {
        //mContext.unregisterReceiver(receiver);
    }

    //判断设备是否连接
    public static boolean isDeviceConnected(BluetoothDevice device) {
        boolean isConnected = false;
        try {
            isConnected = (boolean) device.getClass().getMethod("isConnected").invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    //解绑设备
    public static boolean removeBondDevice(BluetoothDevice device) {
        boolean state = false;
        try {
            state = (boolean) device.getClass().getMethod("removeBond").invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }

    /**
     * 回调
     */
    public interface OnBluetoothListener {
        void onGetBondedDevices(Set<BluetoothDevice> pairedDevices);//刷新已配对列表

        void onScanResult(BluetoothDevice device);//扫描

        void onScanStart();

        void onScanFinish();

        void onBoundStateChanged(BluetoothDevice device, int bondState);

        void onConnectionStateChanged();
    }
}

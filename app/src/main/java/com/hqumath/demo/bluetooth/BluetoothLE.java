package com.hqumath.demo.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.hqumath.demo.R;
import com.hqumath.demo.app.AppExecutors;
import com.hqumath.demo.utils.CommonUtil;
import com.hqumath.demo.utils.LogUtil;
import com.hqumath.demo.utils.PermissionUtil;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private static final int SCAN_PERIOD = 10;//扫描时长 10s

    private boolean scanning;

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
        //扫描中不能立即重新扫描
        if (!scanning) {
            //扫描一段时间后结束
            AppExecutors.getInstance().scheduledWork().schedule(() -> {
                scanning = false;
                if (onBluetoothListener != null)
                    onBluetoothListener.onScanFinish();
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            }, SCAN_PERIOD, TimeUnit.SECONDS);
            //扫描开始
            scanning = true;
            if (onBluetoothListener != null)
                onBluetoothListener.onScanStart();
            bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        }
    }

    public void release() {
        //mContext.unregisterReceiver(receiver);
    }

    @SuppressLint("MissingPermission")
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {//扫描-发现设备
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (device != null && !TextUtils.isEmpty(device.getName())) {
                if (onBluetoothListener != null)
                    onBluetoothListener.onScanResult(device);
            }
            LogUtil.d("onScanResult " + device.getName());//TODO

        }

        /*public void onBatchScanResults(List<ScanResult> results) {
            LogUtil.d("onBatchScanResults");//TODO
        }

        public void onScanFailed(int errorCode) {
            LogUtil.d("onScanFailed");
        }*/
    };

    /**
     * 回调
     */
    public interface OnBluetoothListener {
        void onScanResult(BluetoothDevice device);//扫描

        void onScanStart();

        void onScanFinish();

        void onBoundStateChanged(BluetoothDevice device, int bondState);

        void onConnectionStateChanged();
    }
}

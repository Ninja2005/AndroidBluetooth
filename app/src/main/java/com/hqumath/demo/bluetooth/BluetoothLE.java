package com.hqumath.demo.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.hqumath.demo.R;
import com.hqumath.demo.app.AppExecutors;
import com.hqumath.demo.utils.ByteUtil;
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
    public static final String TAG = "BluetoothLE";
    public static final int REQUEST_ENABLE_BT = 11;
    public static final int REQUEST_ENABLE_GPS = 12;
    private static final int SCAN_TIME = 10;//扫描时长 10s

    public static final String SERVICE_UUID_HC04_HC08 = "0000ffe0-0000-1000-8000-00805f9b34fb";//汇承HC04 基于蓝牙5.0的模块;汇承HC08 基于蓝牙4.0的模块
    public static final String CHARACTERISTIC_UUID_HC04_HC08_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_UUID_HC04_HC08_WRITE = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private boolean isScanning;//是否正在扫描

    private Activity mContext;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLE.OnBluetoothListener onBluetoothListener;
    private BluetoothGatt bluetoothGatt;//gatt连接
    private BluetoothGattCharacteristic writeCharacteristic;//写特征

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

    @SuppressLint("MissingPermission")
    public void release() {
        disconnectDevice();
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
    private void scan() {
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
        if (!isScanning) {
            //扫描一段时间后结束
            AppExecutors.getInstance().scheduledWork().schedule(() -> {
                stopScan();
            }, SCAN_TIME, TimeUnit.SECONDS);
            //扫描开始
            isScanning = true;
            if (onBluetoothListener != null)
                onBluetoothListener.onScanStart();
            bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        }
    }

    /**
     * 停止扫描
     */
    @SuppressLint("MissingPermission")
    private void stopScan() {
        if (isScanning) {
            isScanning = false;
            if (onBluetoothListener != null)
                onBluetoothListener.onScanFinish();
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        }
    }

    @SuppressLint("MissingPermission")
    public void connectDevice(BluetoothDevice device) {
        stopScan();
        //CommonUtil.toast(R.string.bluetooth_is_connecting);
        writeCharacteristic = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = device.connectGatt(mContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            bluetoothGatt = device.connectGatt(mContext, false, gattCallback);
        }
    }

    @SuppressLint("MissingPermission")
    public void disconnectDevice() {
        stopScan();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt = null;
        }
    }

    /**
     * 写入数据
     *
     * @param data
     */
    @SuppressLint("MissingPermission")
    public boolean write(byte[] data) {
        if (bluetoothGatt == null || writeCharacteristic == null) {
            return false;
        }
        writeCharacteristic.setValue(data);
        return bluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    /**
     * 扫描回调
     */
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
        }

        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            LogUtil.d(TAG, "onBatchScanResults");
        }

        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            LogUtil.d(TAG, "onScanFailed, errorCode=" + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        //连接状态变化 连接中/已连接/断开中/已断开
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            LogUtil.d(TAG, "onConnectionStateChange, status=" + status + " newState=" + newState);
            if (onBluetoothListener != null)
                onBluetoothListener.onConnectionStateChanged(gatt, status, newState);
            switch (newState) {
                case BluetoothGatt.STATE_CONNECTED:
                    gatt.discoverServices();//去发现服务
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    gatt.close();
                    bluetoothGatt = null;
                    break;
            }
        }

        //发现服务
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            LogUtil.d(TAG, "onServicesDiscovered, status=" + status);
            BluetoothGattCharacteristic readCharacteristic = null;//读特征
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> list = gatt.getServices();
                for (BluetoothGattService service : list) {//服务
                    LogUtil.d("service uuid: " + service.getUuid());
                    if (TextUtils.equals(service.getUuid().toString(), SERVICE_UUID_HC04_HC08)) {
                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {//特征
                            if (TextUtils.equals(characteristic.getUuid().toString(), CHARACTERISTIC_UUID_HC04_HC08_NOTIFY)) {
                                readCharacteristic = characteristic;
                            }
                            if (TextUtils.equals(characteristic.getUuid().toString(), CHARACTERISTIC_UUID_HC04_HC08_WRITE)) {
                                writeCharacteristic = characteristic;
                            }
                        }
                        break;
                    }
                }
            }
            //打开通知通道
            if (readCharacteristic != null) {
                gatt.setCharacteristicNotification(readCharacteristic, true);
                //重新写入描述？
                /*for (BluetoothGattDescriptor descriptor : readCharacteristic.getDescriptors()) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }*/
            }
            //打开读写通道结果
            if (onBluetoothListener != null)
                onBluetoothListener.onServicesDiscovered(readCharacteristic != null && writeCharacteristic != null);
        }

        //读取数据回调
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            byte[] data = characteristic.getValue();
            LogUtil.d(TAG, "onCharacteristicRead: " + ByteUtil.bytesToHexWithSpace(data));
        }

        //写入数据回调
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            byte[] data = characteristic.getValue();
            LogUtil.d(TAG, "onCharacteristicWrite: " + ByteUtil.bytesToHexWithSpace(data));
        }

        //通知数据回调
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //LogUtil.d("onCharacteristicWrite: " + ByteUtil.bytesToHexWithSpace(characteristic.getValue()));
            if (onBluetoothListener != null)
                onBluetoothListener.onRead(characteristic.getValue());
        }
    };

    /**
     * 回调
     */
    public interface OnBluetoothListener {
        void onScanResult(BluetoothDevice device);//扫描

        void onScanStart();

        void onScanFinish();

        void onConnectionStateChanged(BluetoothGatt gatt, int status, int newState);//连接状态变化

        void onServicesDiscovered(boolean result);//打开读写通道结果

        void onRead(byte[] data);//读取数据
    }
}

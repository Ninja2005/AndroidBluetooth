package com.hqumath.demo.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.hqumath.demo.R;
import com.hqumath.demo.utils.CommonUtil;
import com.hqumath.demo.utils.PermissionUtil;
import com.yanzhenjie.permission.AndPermission;

import java.util.Set;

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 17:00
 * 文件描述: 经典蓝牙
 * 注意事项:
 * ****************************************************************
 */
public class BluetoothClassic {
    public static final String TAG = "BluetoothClassic";
    public static final int REQUEST_ENABLE_BT = 11;
    public static final int REQUEST_ENABLE_GPS = 12;

    private Activity mContext;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothClassic.OnBluetoothListener onBluetoothListener;

    public BluetoothClassic() {

    }

    public void init(Activity context) {
        mContext = context;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//高版本已经弃用
        }
        //注册广播接收者
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        mContext.registerReceiver(receiver, intentFilter);
    }

    public void setOnBluetoothListener(BluetoothClassic.OnBluetoothListener onBluetoothListener) {
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
        //扫描 大概12s,扫描中不能立即重新扫描
        if (!bluetoothAdapter.isDiscovering()) {
            if (onBluetoothListener != null)
                onBluetoothListener.onScanStart();
            bluetoothAdapter.startDiscovery();
        }
    }

    public void release() {
        mContext.unregisterReceiver(receiver);
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

    /**
     * 广播接收。扫描、配对、连接
     */
    @SuppressLint("MissingPermission")
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {//扫描-发现设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !TextUtils.isEmpty(device.getName())) {
                    if (onBluetoothListener != null)
                        onBluetoothListener.onScanResult(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {//扫描结束
                if (onBluetoothListener != null)
                    onBluetoothListener.onScanFinish();
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {//配对状态变化
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                if (device == null) {
                    return;
                }
                if (onBluetoothListener != null)
                    onBluetoothListener.onBoundStateChanged(device, bondState);
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {//连接状态变化
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int connectState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                if (device == null) {
                    return;
                }
                if (connectState == BluetoothAdapter.STATE_DISCONNECTED || connectState == BluetoothAdapter.STATE_CONNECTED) {
                    if (onBluetoothListener != null)
                        onBluetoothListener.onConnectionStateChanged();
                }
            }
        }
    };

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

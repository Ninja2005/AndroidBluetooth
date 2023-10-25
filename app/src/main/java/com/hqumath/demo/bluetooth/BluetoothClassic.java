package com.hqumath.demo.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.hqumath.demo.R;
import com.hqumath.demo.utils.CommonUtil;

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
            return;
        }
    }

    public void release() {
        mContext.unregisterReceiver(receiver);
    }

    /**
     * 广播接收。扫描、配对、连接
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {


        }
    };
}

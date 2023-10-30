package com.hqumath.demo.ui.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hqumath.demo.R;
import com.hqumath.demo.adapter.MyRecyclerAdapters;
import com.hqumath.demo.base.BaseActivity;
import com.hqumath.demo.base.BaseRecyclerAdapter;
import com.hqumath.demo.bluetooth.BluetoothClassic;
import com.hqumath.demo.bluetooth.BluetoothLE;
import com.hqumath.demo.databinding.ActivityBluetoothClassicBinding;
import com.hqumath.demo.databinding.ActivityBluetoothLeBinding;
import com.hqumath.demo.dialog.DialogUtil;
import com.hqumath.demo.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 9:35
 * 文件描述: 经典蓝牙 连接&断开
 * 注意事项:
 * ****************************************************************
 */
public class BluetoothLEActivity extends BaseActivity {
    private ActivityBluetoothLeBinding binding;
    private BluetoothLE bluetoothLE;//低功耗蓝牙
    private MyRecyclerAdapters.BluetoothDeviceRecyclerAdapter connectedAdapter;
    private MyRecyclerAdapters.BluetoothDeviceRecyclerAdapter disconnectedAdapter;

    private List<BluetoothDevice> connectedDevices = new ArrayList<>();//已配对设备列表
    private List<BluetoothDevice> disconnectedDevices = new ArrayList<>();//可用设备列表

    @Override
    protected View initContentView(Bundle savedInstanceState) {
        binding = ActivityBluetoothLeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initListener() {
        binding.btnScan.setOnClickListener(v -> {
            showLoading();
            bluetoothLE.scanWithPermission();
        });

        //已配对设备
        connectedAdapter = new MyRecyclerAdapters.BluetoothDeviceRecyclerAdapter(mContext, connectedDevices);
        connectedAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //onClickBluetoothDevice(pairedDevices.get(position));
            }
        });
        binding.rvConnected.setAdapter(connectedAdapter);
        //可用设备
        disconnectedAdapter = new MyRecyclerAdapters.BluetoothDeviceRecyclerAdapter(mContext, disconnectedDevices);
        disconnectedAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //onClickBluetoothDevice(availableDevices.get(position));
            }
        });
        binding.rvDisconnected.setAdapter(disconnectedAdapter);
    }

    @Override
    protected void initData() {
        bluetoothLE = new BluetoothLE();
        bluetoothLE.init(mContext);
        bluetoothLE.setOnBluetoothListener(new BluetoothLE.OnBluetoothListener() {

            @Override
            public void onScanResult(BluetoothDevice device) {
                //不添加重复的mac
                for (BluetoothDevice item : connectedDevices) {
                    if (item.getAddress().equals(device.getAddress())) {
                        return;
                    }
                }
                for (BluetoothDevice item : disconnectedDevices) {
                    if (item.getAddress().equals(device.getAddress())) {
                        return;
                    }
                }
                disconnectedDevices.add(device);
                disconnectedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanStart() {
                disconnectedDevices.clear();
                disconnectedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinish() {
                dismissLoading();
            }

            @Override
            public void onBoundStateChanged(BluetoothDevice device, int bondState) {

            }

            @Override
            public void onConnectionStateChanged() {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭蓝牙
        if (bluetoothLE != null) {
            bluetoothLE.release();
            bluetoothLE = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothClassic.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {//蓝牙已打开
                bluetoothLE.scan();
            } else {
                CommonUtil.toast(R.string.bluetooth_not_open);
            }
        } else if (requestCode == BluetoothClassic.REQUEST_ENABLE_GPS) {
            if (CommonUtil.isGpsOpen()) {
                bluetoothLE.scan();
            } else {
                CommonUtil.toast(R.string.location_not_open);
            }
        }
    }

    /**
     * 点击蓝牙设备
     *
     * @param device
     */
    private void onClickBluetoothDevice(BluetoothDevice device) {
        dismissLoading();//取消扫描
        if (BluetoothClassic.isDeviceConnected(device)) {//已连接，提示断开连接
            DialogUtil dialog = new DialogUtil(mContext);
            dialog.setTitle(R.string.tips);
            dialog.setMessage(R.string.bluetooth_disconnect);
            dialog.setTwoConfirmBtn(R.string.button_ok, v1 -> {
                BluetoothClassic.removeBondDevice(device);
            });
            dialog.setTwoCancelBtn(R.string.button_cancel, null);
            dialog.show();
            return;
        }
//        bluetoothLE.connectDevice(device);//连接
    }
}

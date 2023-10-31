package com.hqumath.demo.ui.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hqumath.demo.R;
import com.hqumath.demo.adapter.MyRecyclerAdapters;
import com.hqumath.demo.base.BaseActivity;
import com.hqumath.demo.base.BaseRecyclerAdapter;
import com.hqumath.demo.bluetooth.BluetoothClassic;
import com.hqumath.demo.bluetooth.BluetoothLE;
import com.hqumath.demo.databinding.ActivityBluetoothLeBinding;
import com.hqumath.demo.dialog.DialogUtil;
import com.hqumath.demo.utils.ByteUtil;
import com.hqumath.demo.utils.CommonUtil;
import com.hqumath.demo.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

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
    private MyRecyclerAdapters.BluetoothLEDeviceRecyclerAdapter disconnectedAdapter;

    private BluetoothDevice connectedDevice;//已连接设备
    private List<BluetoothDevice> disconnectedDevices = new ArrayList<>();//未连接设备列表
    private BluetoothDevice waitConnectDevice;//断开旧设备后，待连接的新设备

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
        //已连接
        binding.llConnectDevice.setOnClickListener(v -> {
            onClickBluetoothDevice(connectedDevice);
        });
        //未连接设备
        disconnectedAdapter = new MyRecyclerAdapters.BluetoothLEDeviceRecyclerAdapter(mContext, disconnectedDevices);
        disconnectedAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                onClickBluetoothDevice(disconnectedDevices.get(position));
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
                if (connectedDevice != null && connectedDevice.getAddress().equals(device.getAddress())) {
                    return;
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
            @SuppressLint("MissingPermission")
            public void onConnectionStateChanged(BluetoothGatt gatt, int status, int newState) {//workTread
                binding.getRoot().post(() -> {
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTING:
                        case BluetoothProfile.STATE_DISCONNECTING:
                            showLoading();
                            break;
                        case BluetoothProfile.STATE_CONNECTED:
                            dismissLoading();
                            BluetoothDevice device = gatt.getDevice();
                            //未连接设备列表 => 已连接设备
                            disconnectedDevices.remove(device);
                            connectedDevice = device;
                            disconnectedAdapter.notifyDataSetChanged();
                            binding.llConnectDevice.setVisibility(View.VISIBLE);
                            binding.tvConnectDeviceName.setText(device.getName());
                            binding.tvConnectDeviceAddress.setText(device.getAddress());
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            dismissLoading();
                            BluetoothDevice device2 = gatt.getDevice();
                            //已连接设备 => 未连接设备列表
                            if (connectedDevice == device2) {
                                connectedDevice = null;
                                disconnectedDevices.add(device2);
                                disconnectedAdapter.notifyDataSetChanged();
                                binding.llConnectDevice.setVisibility(View.GONE);
                            }
                            if (waitConnectDevice != null) {
                                bluetoothLE.connectDevice(waitConnectDevice);
                            }
                            break;
                    }
                });
            }

            @Override
            public void onServicesDiscovered(boolean result) {
                LogUtil.d(BluetoothLE.TAG, "发现服务" + (result ? "成功" : "失败"));
            }

            @Override
            public void onRead(byte[] data) {
                //LogUtil.d(BluetoothLE.TAG, "onRead: " + ByteUtil.bytesToHexWithSpace(data));
                binding.getRoot().post(() -> {
                    binding.edtReceiveData.setText(ByteUtil.bytesToHexWithSpace(data));
                });
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
                bluetoothLE.scanWithPermission();
            } else {
                CommonUtil.toast(R.string.bluetooth_not_open);
            }
        } else if (requestCode == BluetoothClassic.REQUEST_ENABLE_GPS) {
            if (CommonUtil.isGpsOpen()) {
                bluetoothLE.scanWithPermission();
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
        if (device == connectedDevice) {//点击已连接设备，提示断开连接
            DialogUtil dialog = new DialogUtil(mContext);
            dialog.setTitle(R.string.tips);
            dialog.setMessage(R.string.bluetooth_disconnect);
            dialog.setTwoConfirmBtn(R.string.button_ok, v1 -> {
                waitConnectDevice = null;
                bluetoothLE.disconnectDevice();
            });
            dialog.setTwoCancelBtn(R.string.button_cancel, null);
            dialog.show();
        } else {//点击未连接设备列表
            if (connectedDevice == null) {//连接新设备
                bluetoothLE.connectDevice(device);
            } else {//先断开旧设备，再连接新设备
                waitConnectDevice = device;
                bluetoothLE.disconnectDevice();
            }
        }
    }
}

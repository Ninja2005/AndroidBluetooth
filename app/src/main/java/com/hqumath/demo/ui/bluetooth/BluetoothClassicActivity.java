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
import com.hqumath.demo.databinding.ActivityBluetoothClassicBinding;
import com.hqumath.demo.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 9:35
 * 文件描述: 经典腊月
 * 注意事项:
 * ****************************************************************
 */
public class BluetoothClassicActivity extends BaseActivity {
    private ActivityBluetoothClassicBinding binding;
    private BluetoothClassic bluetoothClassic;//蓝牙手柄
    private MyRecyclerAdapters.BluetoothDeviceRecyclerAdapter pairedAdapter;
    private MyRecyclerAdapters.BluetoothDeviceRecyclerAdapter availableAdapter;

    private List<BluetoothDevice> pairedDevices = new ArrayList<>();//已配对设备列表
    private List<BluetoothDevice> availableDevices = new ArrayList<>();//可用设备列表

    @Override
    protected View initContentView(Bundle savedInstanceState) {
        binding = ActivityBluetoothClassicBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initListener() {
        binding.btnScan.setOnClickListener(v -> {
            showLoading();
            bluetoothClassic.scanWithPermission();
        });

        //已配对设备
        pairedAdapter = new MyRecyclerAdapters.BluetoothDeviceRecyclerAdapter(mContext, pairedDevices);
        pairedAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                dismissLoading();//取消扫描
//                onItemClickWireless(pairedDevices.get(position));
            }
        });
        binding.rvPaired.setAdapter(pairedAdapter);
        //可用设备
        availableAdapter = new MyRecyclerAdapters.BluetoothDeviceRecyclerAdapter(mContext, availableDevices);
        availableAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                dismissLoading();//取消扫描
//                onItemClickWireless(availableDevices.get(position));
            }
        });
        binding.rvAvailable.setAdapter(availableAdapter);
    }

    @Override
    protected void initData() {
        bluetoothClassic = new BluetoothClassic();
        bluetoothClassic.init(mContext);
        bluetoothClassic.setOnBluetoothListener(new BluetoothClassic.OnBluetoothListener() {
            @Override
            public void onGetBondedDevices(Set<BluetoothDevice> pairedDevices1) {
                if (pairedDevices != null) {
                    pairedDevices.clear();
                    pairedDevices.addAll(pairedDevices1);
                    pairedAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScanResult(BluetoothDevice device) {
                //不添加重复的mac
                for (BluetoothDevice item : pairedDevices) {
                    if (item.getAddress().equals(device.getAddress())) {
                        return;
                    }
                }
                for (BluetoothDevice item : availableDevices) {
                    if (item.getAddress().equals(device.getAddress())) {
                        return;
                    }
                }
                availableDevices.add(device);
                availableAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanStart() {
                availableDevices.clear();
                availableAdapter.notifyDataSetChanged();
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
        if (bluetoothClassic != null) {
            bluetoothClassic.release();
            bluetoothClassic = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothClassic.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {//蓝牙已打开
                bluetoothClassic.scan();
            } else {
                CommonUtil.toast(R.string.bluetooth_not_open);
            }
        } else if (requestCode == BluetoothClassic.REQUEST_ENABLE_GPS) {
            if (CommonUtil.isGpsOpen()) {
                bluetoothClassic.scan();
            } else {
                CommonUtil.toast(R.string.location_not_open);
            }
        }
    }
}

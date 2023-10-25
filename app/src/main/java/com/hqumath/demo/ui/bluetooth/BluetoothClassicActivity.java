package com.hqumath.demo.ui.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hqumath.demo.R;
import com.hqumath.demo.base.BaseActivity;
import com.hqumath.demo.bluetooth.BluetoothClassic;
import com.hqumath.demo.databinding.ActivityBluetoothClassicBinding;
import com.hqumath.demo.utils.CommonUtil;

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

    @Override
    protected View initContentView(Bundle savedInstanceState) {
        binding = ActivityBluetoothClassicBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initListener() {
        binding.btnScan.setOnClickListener(v -> {
            bluetoothClassic.scanWithPermission();
        });
    }

    @Override
    protected void initData() {
        bluetoothClassic = new BluetoothClassic();
        bluetoothClassic.init(mContext);
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

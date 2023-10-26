package com.hqumath.demo.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.hqumath.demo.R;
import com.hqumath.demo.base.BaseRecyclerAdapter;
import com.hqumath.demo.base.BaseRecyclerViewHolder;
import com.hqumath.demo.bean.ReposEntity;
import com.hqumath.demo.bluetooth.BluetoothClassic;

import java.util.List;

public class MyRecyclerAdapters {

    //我的仓库
    public static class ReposRecyclerAdapter extends BaseRecyclerAdapter<ReposEntity> {
        public ReposRecyclerAdapter(Context context, List<ReposEntity> mData) {
            super(context, mData, R.layout.recycler_item_repos);
        }

        @Override
        public void convert(BaseRecyclerViewHolder holder, int position) {
            ReposEntity data = mData.get(position);
            holder.setText(R.id.tv_name, data.getName());
            holder.setText(R.id.tv_description, data.getDescription());
            holder.setText(R.id.tv_author, data.getOwner().getLogin());
        }
    }

    //蓝牙设备
    @SuppressLint("MissingPermission")
    public static class BluetoothDeviceRecyclerAdapter extends BaseRecyclerAdapter<BluetoothDevice> {
        public BluetoothDeviceRecyclerAdapter(Context context, List<BluetoothDevice> mData) {
            super(context, mData, R.layout.recycler_item_bluetooth_device);
        }

        @Override
        public void convert(BaseRecyclerViewHolder holder, int position) {
            BluetoothDevice data = mData.get(position);
            boolean isConnected = BluetoothClassic.isDeviceConnected(data);
            holder.setText(R.id.tvName, data.getName());
            holder.setText(R.id.tvAddress, data.getAddress());
            holder.setText(R.id.tvState, isConnected ? "已连接" : "");
        }
    }
}

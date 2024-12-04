# AndroidBluetooth

> 本文实现了经典蓝牙的查找、配对、连接，低功耗蓝牙的查找、连接、通信。

## 1.Android系统蓝牙

### 1.1 Android系统支持经典蓝牙和低功耗蓝牙：

*   Classic Bluetooth(经典蓝牙)。
    用于大量的通信时，例如：音频、短信和电话。
*   Bluetooth Low Energy(低功耗蓝牙)。
    用于低频的通信时，例如：心率监测器、无线键盘，要求Android4.3及更高版本。

### 1.2 蓝牙堆栈的常规结构

蓝牙应用通过 Binder 与蓝牙进程进行通信。蓝牙进程使用 JNI 与蓝牙堆栈通信，并向开发者提供对各种蓝牙配置文件的访问权限。如下图：
![AndroidBluetooth](img/2.gif)

## 2.经典蓝牙

### 2.1 申请蓝牙权限

```java
    <!--蓝牙 API30及以下需要申请4个权限，API31及以上时权限列表将不会列出这些权限-->
    <!--蓝牙 连接、通信-->
    <uses-permission
        android:name="android.permission.BLUETOOTH"
        android:maxSdkVersion="30" />
    <!--蓝牙 扫描、设置-->
    <uses-permission
        android:name="android.permission.BLUETOOTH_ADMIN"
        android:maxSdkVersion="30" />
    <!--蓝牙 API30及以下需要定位权限 -->
    <uses-permission
        android:name="android.permission.ACCESS_COARSE_LOCATION"
        android:maxSdkVersion="30" />
    <uses-permission
        android:name="android.permission.ACCESS_FINE_LOCATION"
        android:maxSdkVersion="30" />

    <!--蓝牙 API31及以上需要申请3个权限-->
    <!--蓝牙 扫描，可不申请定位权限-->
    <uses-permission
        android:name="android.permission.BLUETOOTH_SCAN"
        android:usesPermissionFlags="neverForLocation"
        tools:targetApi="s" />
    <!--蓝牙 广播，被发现-->
    <!--<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />-->
    <!--蓝牙 连接、通信-->
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

### 2.2 设置蓝牙

```java
//蓝牙适配器
BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
//注册广播接收者
IntentFilter intentFilter = new IntentFilter();
intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
mContext.registerReceiver(receiver, intentFilter);
//广播接收者
private final BroadcastReceiver receiver = new BroadcastReceiver() {
   public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (BluetoothDevice.ACTION_FOUND.equals(action)) {//扫描-发现设备
         BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {//扫描结束
      } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {//配对状态变化
         BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
         int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
      } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {//连接状态变化
         BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
         int connectState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
      }
   }
};
```

### 2.3 查找蓝牙设备

```java
//查询已配对设备
Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//扫描 大概12s,扫描中不能立即重新扫描
if (!bluetoothAdapter.isDiscovering()) {
   bluetoothAdapter.startDiscovery();
}
```

### 2.3 连接蓝牙设备

```java
public void connectDevice(BluetoothDevice device) {
   if (bluetoothAdapter != null) {//取消扫描
      bluetoothAdapter.cancelDiscovery();
    }
   //已配对的取消配对，配对后自动连接。
   if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
      removeBondDevice(device);//取消配对
      AppExecutors.getInstance().scheduledWork().schedule(() -> {
         device.createBond();//延时配对
      }, 1, TimeUnit.SECONDS);
   } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            device.createBond();//配对
   }
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
```

## 3.低功耗蓝牙

### 3.1 查找蓝牙设备

```java
//扫描一段时间后结束
AppExecutors.getInstance().scheduledWork().schedule(() -> {
   bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
}, 10, TimeUnit.SECONDS);
bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
```

### 3.2 连接蓝牙设备

```java
//连接
bluetoothGatt = device.connectGatt(mContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
//断开连接
bluetoothGatt.disconnect();
```

### 3.3 写入数据

```java
byte[] data;
writeCharacteristic.setValue(data);
bluetoothGatt.writeCharacteristic(writeCharacteristic);
```

## 4.效果图

![效果图](img/1.gif)
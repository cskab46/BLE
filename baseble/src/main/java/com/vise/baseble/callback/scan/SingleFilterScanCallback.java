package com.vise.baseble.callback.scan;

import android.bluetooth.le.ScanCallback;

import com.vise.baseble.ViseBle;
import com.vise.baseble.model.BluetoothLeDevice;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description: 设置扫描指定的单个设备，一般是设备名称和Mac地址
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 17/9/12 22:16.
 */
public class SingleFilterScanCallback extends ViseScanCallback {
    private AtomicBoolean hasFound = new AtomicBoolean(false);
    private String deviceName;//指定设备名称
    private String deviceMac;//指定设备Mac地址

    public SingleFilterScanCallback(IScanCallback ViseScanCallback, ViseLeScanCallback ViseLeScanCallback) {
        super(ViseScanCallback, ViseLeScanCallback);
    }

    public ViseScanCallback setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public ViseScanCallback setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
        return this;
    }

    @Override
    public BluetoothLeDevice onFilter(BluetoothLeDevice bluetoothLeDevice) {
        BluetoothLeDevice tempDevice = null;
        if (!hasFound.get()) {
            if (bluetoothLeDevice != null && bluetoothLeDevice.getAddress() != null && deviceMac != null
                    && deviceMac.equalsIgnoreCase(bluetoothLeDevice.getAddress().trim())) {
                hasFound.set(true);
                isScanning = false;
                removeHandlerMsg();
                ViseBle.getInstance().stopScan(SingleFilterScanCallback.this);
                tempDevice = bluetoothLeDevice;
                bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
                mViseScanCallback.onScanFinish(bluetoothLeDeviceStore);
            } else if (bluetoothLeDevice != null && bluetoothLeDevice.getName() != null && deviceName != null
                    && deviceName.equalsIgnoreCase(bluetoothLeDevice.getName().trim())) {
                hasFound.set(true);
                isScanning = false;
                removeHandlerMsg();
                ViseBle.getInstance().stopScan(SingleFilterScanCallback.this);
                tempDevice = bluetoothLeDevice;
                bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
                mViseScanCallback.onScanFinish(bluetoothLeDeviceStore);
            }
        }
        return tempDevice;
    }
}

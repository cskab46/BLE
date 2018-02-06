package com.vise.baseble.callback.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import com.vise.baseble.ViseBle;
import com.vise.baseble.common.BleConfig;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 扫描设备回调
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 17/8/1 22:58.
 */
public class ViseScanCallback extends ScanCallback implements BluetoothAdapter.LeScanCallback, IScanFilter{
    protected Handler handler = new Handler(Looper.myLooper());
    protected boolean isScan = true;//是否开始扫描
    protected boolean isScanning = false;//是否正在扫描
    protected BluetoothLeDeviceStore bluetoothLeDeviceStore;//用来存储扫描到的设备
    protected IScanCallback mViseScanCallback;//扫描结果回调
    protected ParcelUuid Service_UUID = ParcelUuid
            .fromString("0000b81d-0000-1000-8000-00805f9b35fb"); // Service_UUID 用于过滤scan结果
    public ViseLeScanCallback mViseLeScanCallback;

    public ViseScanCallback(IScanCallback ViseScanCallback, ViseLeScanCallback ViseLeScanCallback) {
        this.mViseScanCallback = ViseScanCallback;
        this.mViseLeScanCallback = ViseLeScanCallback;
        if (ViseScanCallback == null || ViseLeScanCallback == null) {
            throw new NullPointerException("this ViseScanCallback is null!");
        }
        bluetoothLeDeviceStore = new BluetoothLeDeviceStore();
    }

//    public ViseScanCallback(IScanCallback ViseScanCallback) {
//        this.ViseScanCallback = ViseScanCallback;
//        if (ViseScanCallback == null) {
//            throw new NullPointerException("this ViseScanCallback is null!");
//        }
//        bluetoothLeDeviceStore = new BluetoothLeDeviceStore();
//    }


    public ViseScanCallback setScan(boolean scan) {
        isScan = scan;
        return this;
    }

    public void setServiceUUID (ParcelUuid uuid) {
        Service_UUID = uuid;
        return;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void scan() {
        if (isScan) {
            if (isScanning) {
                return;
            }
            bluetoothLeDeviceStore.clear();
            if (BleConfig.getInstance().getScanTimeout() > 0) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isScanning = false;

                        if (ViseBle.getInstance().getBluetoothAdapter() != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                ViseBle.getInstance().getBluetoothAdapter().getBluetoothLeScanner().stopScan(ViseScanCallback.this);
                            } else {
                                ViseBle.getInstance().getBluetoothAdapter().stopLeScan(ViseScanCallback.this);
                            }

                        }

                        if (bluetoothLeDeviceStore.getDeviceMap() != null
                                && bluetoothLeDeviceStore.getDeviceMap().size() > 0) {
                            mViseScanCallback.onScanFinish(bluetoothLeDeviceStore);
                        } else {
                            mViseScanCallback.onScanTimeout();
                        }
                    }
                }, BleConfig.getInstance().getScanTimeout());
            }
            isScanning = true;
            if (ViseBle.getInstance().getBluetoothAdapter() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ViseBle.getInstance().getBluetoothAdapter().getBluetoothLeScanner().startScan(buildScanFilters(), buildScanSettings(), ViseScanCallback.this);
                } else {
                    ViseBle.getInstance().getBluetoothAdapter().startLeScan(ViseScanCallback.this);
                }
            }
        } else {
            isScanning = false;
            if (ViseBle.getInstance().getBluetoothAdapter() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ViseBle.getInstance().getBluetoothAdapter().getBluetoothLeScanner().stopScan(ViseScanCallback.this);
                } else {
                    ViseBle.getInstance().getBluetoothAdapter().stopLeScan(ViseScanCallback.this);
                }
            }
        }
    }

    public ViseScanCallback removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
        bluetoothLeDeviceStore.clear();
        return this;
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        BluetoothLeDevice bluetoothLeDevice = new BluetoothLeDevice(bluetoothDevice, rssi, scanRecord, System.currentTimeMillis());
        BluetoothLeDevice filterDevice = onFilter(bluetoothLeDevice);
        ViseLog.i("onLeScan ");
        if (filterDevice != null) {
            bluetoothLeDeviceStore.addDevice(filterDevice);
            mViseScanCallback.onDeviceFound(filterDevice);
        }
    }

    //com.vise.baseble.callback.scan.IScanFilter
    @Override
    public BluetoothLeDevice onFilter(BluetoothLeDevice bluetoothLeDevice) {
        return bluetoothLeDevice;
    }

    // Todo add scanresult to bluetoothLeDeviceStore
    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        mViseLeScanCallback.onBatchScanResults(results);
        for (ScanResult result : results) {
            onScanResult(0,result);
        }
        ViseLog.i("onBatchScanResults ");
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        mViseLeScanCallback.onScanResult(callbackType, result);
        ViseLog.i("onScanResult ");
        BluetoothLeDevice bluetoothLeDevice = new BluetoothLeDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());
        BluetoothLeDevice filterDevice = onFilter(bluetoothLeDevice);
        if (filterDevice != null) {
            bluetoothLeDeviceStore.addDevice(filterDevice);
            mViseScanCallback.onDeviceFound(filterDevice);
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        mViseLeScanCallback.onScanFailed(errorCode);
        ViseLog.i("onScanFailed ");
    }

    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(Service_UUID);
        scanFilters.add(builder.build());

        return scanFilters;
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        return builder.build();
    }

}

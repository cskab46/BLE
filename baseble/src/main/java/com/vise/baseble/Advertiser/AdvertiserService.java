package com.vise.baseble.Advertiser;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;

import com.vise.log.ViseLog;

import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/2/6 0006.
 */

public class AdvertiserService extends Service{

    private static final String TAG = AdvertiserService.class.getSimpleName();

    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    public static boolean running = false;

    public static int ADVERTISING_TIMEOUT = 30;

    public static ParcelUuid Service_UUID = ParcelUuid
            .fromString("0000b81d-0000-1000-8000-00805f9b35fb");

    private long TIMEOUT = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

    private Handler mHandler;

    private Runnable timeoutRunnable;

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private AdvertiseCallback mAdvertiseCallback;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        running = true;
        initialize();
        startAdvertising();
        setTimeout();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        running = false;
        stopAdvertising();
        mHandler.removeCallbacks(timeoutRunnable);
        stopForeground(true);
        super.onDestroy();
    }

    public void initialize() {
        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                } else {
                    ViseLog.e("Error: BluetoothLeAdvertiser object null ");
                }
            } else {
                ViseLog.e("Error: BluetoothManager object null ");
            }
        }
    }

    public void startAdvertising() {
        if (mAdvertiseCallback == null) {

            mAdvertiseCallback = new SampleAdvertiseCallback();
            AdvertiseSettings setting = builAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();
            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.startAdvertising(setting, data, mAdvertiseCallback);
            }
        }
    }

    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mBluetoothLeAdvertiser = null;
        }
    }

    public void setTimeout() {
        mHandler = new Handler();
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                ViseLog.d("AdvertiserService has reached timeout of "+ TIMEOUT +"milliseconds, stop advertising.");
//                stopSelf(); // never stop advertiser
            }
        };
        mHandler.postDelayed(timeoutRunnable, TIMEOUT);
    }

    private AdvertiseSettings builAdvertiseSettings() {
        AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
        settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingBuilder.setTimeout(0);
        return settingBuilder.build();
    }

    private AdvertiseData buildAdvertiseData() {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(Service_UUID);
        dataBuilder.setIncludeDeviceName(true);
        return  dataBuilder.build();
    }

    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            ViseLog.d("Advertise success!");

        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            ViseLog.e("Advertise failure!");
            stopSelf(); // end service
        }

    }

}

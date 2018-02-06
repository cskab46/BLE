package com.vise.baseble.callback.scan;

import android.bluetooth.le.ScanResult;

import java.util.List;

/**
 * Created by Administrator on 2018/2/6 0006.
 */

public abstract  class ViseLeScanCallback {
    public void onScanResult(int callbackType, ScanResult result) {
    }

    /**
     * Callback when batch results are delivered.
     *
     * @param results List of scan results that are previously scanned.
     */
    public void onBatchScanResults(List<ScanResult> results) {
    }

    /**
     * Callback when scan could not be started.
     *
     * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
     */
    public void onScanFailed(int errorCode) {
    }
}

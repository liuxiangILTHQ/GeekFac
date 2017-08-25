package com.geecare.blelibrary.callback;

import com.geecare.blelibrary.model.BleDevice;

/**
 * Created by Administrator on 2016/7/18.
 */
public interface IBleScan
{
    /**
     * 搜索到的设备
     * @param bleDevice
     */
    void getScanBleDevices(BleDevice bleDevice);

    void updateBleDevice(int pos,int rssi);

    void getScanStatus(boolean isScanning);
}

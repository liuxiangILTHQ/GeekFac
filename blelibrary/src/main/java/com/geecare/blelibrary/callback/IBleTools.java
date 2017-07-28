package com.geecare.blelibrary.callback;

import android.app.Activity;
import android.content.Context;

import com.geecare.blelibrary.model.BleDevice;

/**
 * Created by Administrator on 2016/7/18.
 */
public interface IBleTools
{
    public void init(Context context);
    boolean isBleEnabled();

    void openBle(Activity context);

    void setBleStateCallback(IBleState iBleState);

    /**
     * 蓝牙搜索回调
     *
     * @param iBleScan
     */
    void setBleScanCallback(IBleScan iBleScan);

    /**
     * 蓝牙连接状态回调
     *
     * @param iBleConn
     */
    void setBleConnCallback(IBleConn iBleConn);
    /**
     * 蓝牙数据回调
     *
     * @param iBleComm
     */
    void setBleCommCallback(IBleComm iBleComm);

    /**
     * 开始搜索设备
     */
    boolean startScan(String flag);

    /**
     * 停止搜索
     */
    boolean stopScan();


    /**
     * 连接设备
     * @param macAdd
     */
    boolean connect(String macAdd);

    /**
     * 获取需要连接或者已经连接着的设备
     *
     * @return
     */
    BleDevice getCurrentBleDevice();

    /**
     * 蓝牙设备是否连接着
     *
     * @return
     */
    boolean isConnected();

    /**
     * 断开蓝牙连接
     */
    boolean disconnect(IDisconnect iDisconnect);



}

package com.geecare.blelibrary.callback;

/**
 * Created by Administrator on 2016/7/18.
 */
public interface IBleConn
{
    /**
     * 蓝牙连接状态
     * 蓝牙关闭 -2;硬件正在连接 -1;硬件连接失败 0;硬件连接成功 1;
     * @param staus
     */
    void connectStaus(int staus);
}

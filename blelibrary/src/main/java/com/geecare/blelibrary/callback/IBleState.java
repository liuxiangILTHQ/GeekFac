package com.geecare.blelibrary.callback;

/**
 * Created by Administrator on 2016/7/18.
 */
public interface IBleState
{
    /**
     * 蓝牙开关状态
     * 蓝牙关闭 0;打开 1;
     * @param staus
     */
    void stateChange(int staus);
}

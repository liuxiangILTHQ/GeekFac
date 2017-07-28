package com.geecare.blelibrary;

import java.util.UUID;


public class Constants
{

    public static final String DEVICE="DEVICE";
    public static final String PEE_ADDR="PEE_ADDR";
    public static final String TEMP_ADDR="TEMP_ADDR";
    public static final String BOTTLE_ADDR="BOTTLE_ADDR";
    public static final String PEE_SERVICE_UUID_REVERSE = "9ecadc240ee5a9e093f3a3b50100406e";
    public static final UUID PEE_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    // Transmit(tx) 发送
    public static final UUID PEE_WRITE_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    // Receive(rx) 接收
    public static final UUID PEE_READ_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    // Receive(rx) 接收 蓝牙缓存数据
    public static final UUID RX1_CHAR_UUID = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e");



    public static final String TEMP_SERVICE_UUID_REVERSE = "123456780ee5a9e093f3a3b50100406e";
    public static final String IFOTEMP_SERVICE_UUID_REVERSE = "03010009180f180a18";//"03010009180f180a18";
    public static final String JONETEMP_SERVICE_UUID_REVERSE = "030918";//"03010009180f180a18";

    public static final String BOTTLE_SERVICE_UUID_REVERSE = "9ecadc240ee5a9e093f3a3b50400406e";
    /** ========================广播事件标记==========================*/


    public final static int ACTION_REQUEST_ENABLE=1000;

    public final static int ACTION_BLE_ENABLE = 1;


    // 蓝牙关闭
    public final static int ACTION_BLE_NOT_ENABLE = 0;

    // 硬件正在连接
    public final static int PEE_ACTION_DEVICE_CONNECTING = -1;
    // 硬件连接失败
    public final static int PEE_ACTION_DEVICE_CONNECT_FAIL = 0;
    // 硬件连接成功
    public final static int PEE_ACTION_DEVICE_CONNECT_SUCCESS = 1;

}

package com.geekid.geekfactest.ble;

import java.util.UUID;

/**
 * Created by Administrator on 2016/5/27.
 */
public class BleConstants
{

    public final static String EXTRA_DATA = "iPINTO.EXTRA_DATA";
    /** ======================== UUID==========================*/
    /**
     * 嘘嘘扣
     */

    public static final String PEE_SERVICE_UUID_REVERSE = "9ecadc240ee5a9e093f3a3b50100406e";
    public static final UUID PEE_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    // Transmit(tx) 发送
    public static final UUID PEE_WRITE_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    // Receive(rx) 接收
    public static final UUID PEE_READ_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    // Receive(rx) 接收 蓝牙缓存数据
    public static final UUID RX1_CHAR_UUID = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e");

    /**
     * 体温计
     */
    public static int type = 0;//0为geecare,1为ifo
    public static final String TEMP_SERVICE_UUID_REVERSE = "123456780ee5a9e093f3a3b50100406e";
    // Geecare温度计蓝牙特征服务UUID
    public static final UUID TEMP_SERVICE_UUID_G = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E78563412");
    public static final UUID TEMP_WRITE_CHAR_UUID_G = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E78563412");
    public static final UUID TEMP_READ_CHAR_UUID_G = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E78563412");


    // ifo  06094a2d4f6e65031900030201060303091807ffd9f71082095c000000000000000000000000000000000000000000000000000000000000000000000000
    public static final String IFOTEMP_SERVICE_UUID_REVERSE = "03010009180f180a18";//"03010009180f180a18";
    public static final String JONETEMP_SERVICE_UUID_REVERSE = "030918";//"03010009180f180a18";

    // 温度值蓝牙特征服务UUID
    public static final UUID TEMP_SERVICE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
    // 温度值蓝牙特征值UUID
    public static final UUID TEMP_CHAR_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb");
    // 电池电量蓝牙特征服务UUID
    public static final UUID TEMP_BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    // 电池电量蓝牙特征值UUID
    public static final UUID TEMP_BATTERY_CHAR_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
    // 串口蓝牙特征服务UUID
    public static final UUID TEMP_UART_SERVICE_UUID = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
    // 串口蓝牙写的特征值UUID
    public static final UUID TEMP_UART_WRITE_CHAR_UUID = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");
    // 串口蓝牙读的特征值UUID
    public static final UUID TEMP_UART_READ_CHAR_UUID = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");

    /**
    * 奶瓶070946656564657202010611079ecadc240ee5a9e093f3a3b50400406e000000000000000000000000000000000000000000000000000000000000000000
    */
    public static final String BOTTLE_SERVICE_UUID_REVERSE = "9ecadc240ee5a9e093f3a3b50400406e";
    public static final UUID BOTTLE_SERVICE_UUID_G = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID BOTTLE_WRITE_CHAR_UUID_G = UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID BOTTLE_READ_CHAR_UUID_G = UUID.fromString("6e400006-b5a3-f393-e0a9-e50e24dcca9e");


    /** ========================广播事件标记==========================*/

    // 蓝牙关闭
    public final static String ACTION_BLE_NOT_ENABLE = "ACTION_BLE_NOT_ENABLE";

    /**
     * 嘘嘘扣
     */
    // 温度湿度数据有更新
    public final static String PEE_ACTION_DATA_UPDATED = "PEE_ACTION_DATA_UPDATED";
    // 电量数据更新
    public final static String PEE_ACTION_DEVICE_SOC_UPDATED = "PEE_ACTION_SOC_UPDATED";
    // 硬件连接
    public final static String PEE_ACTION_DEVICE_CONNECTING = "PEE_ACTION_CONNECTING";
    // 硬件连接失败
    public final static String PEE_ACTION_DEVICE_CONNECT_FAIL = "PEE_ACTION_CONNECT_FAIL";
    // 硬件连接成功
    public final static String PEE_ACTION_DEVICE_CONNECT_SUCCESS = "PEE_ACTION_CONNECT_SUCCESS";

    public final static String PEE_ACTION_DEVICE_CONNECT_FAIL1 = "PEE_ACTION_CONNECT_FAIL1";
    // 硬件连接成功
    public final static String PEE_ACTION_DEVICE_CONNECT_SUCCESS1 = "PEE_ACTION_CONNECT_SUCCESS1";
    public final static String ACTION_ALARM_TIP = "ACTION_ALARM_TIP";

    public final static String PEE_ACTION_DATA_COMING = "PEE_hex_data_coming";
    public final static String PEE_ACTION_ROM_VER_COMING = "PEE_ACTION_ROM_VER_COMING";
    public final static String PEE_ACTION_SN_COMING = "PEE_ACTION_SN_COMING";



    public final static String PEE_ACTION_CHANGE_DIAPER = "PEE_ACTION_CHANGE_DIAPER";
    public final static String ACTION_CHANGE_DIAPER = "ACTION_CHANGE_DIAPER";
    public final static String PEE_ACTION_COSTOM_DATA_UPDATE = "PEE_ACTION_COSTOM_DATA_UPDATE";

    /**
        体温计
     */
    // 温度湿度数据有更新
    public final static String TEMP_ACTION_DATA_UPDATED = "TEMP_ACTION_DATA_UPDATED";
    // 电量数据更新
    public final static String TEMP_ACTION_DEVICE_SOC_UPDATED = "TEMP_ACTION_DEVICE_SOC_UPDATED";
    // 硬件连接失败
    public final static String TEMP_ACTION_DEVICE_CONNECTING = "TEMP_ACTION_DEVICE_CONNECTING";
    // 硬件连接失败
    public final static String TEMP_ACTION_DEVICE_CONNECT_FAIL = "TEMP_ACTION_DEVICE_CONNECT_FAIL";
    // 硬件连接成功
    public final static String TEMP_ACTION_DEVICE_CONNECT_SUCCESS = "TEMP_ACTION_DEVICE_CONNECT_SUCCESS";


    public final static String TEMP_ACTION_ROM_VER_COMING = "TEMP_ACTION_ROM_VER_COMING";
    public final static String TEMP_ACTION_SN_COMING = "TEMP_ACTION_SN_COMING";

    public final static String TEMP_ACTION_DATA_COMING = "TEMP_ACTION_DATA_COMING";

    // 数据提取标记
    public final static String TEMP_EXTRA_DATA = "TEMP_EXTRA_DATA";

    /**
     * 奶瓶
     */
    // 温度湿度数据有更新
    public final static String BOTTLE_ACTION_DATA_UPDATED = "BOTTLE.ACTION_DATA_UPDATED";
    // 电量数据更新
    public final static String BOTTLE_ACTION_DEVICE_SOC_UPDATED = "BOTTLE.ACTION_SOC_UPDATED";
    // 硬件连接
    public final static String BOTTLE_ACTION_DEVICE_CONNECTING = "BOTTLE.ACTION_CONNECTING";
    // 硬件连接失败
    public final static String BOTTLE_ACTION_DEVICE_CONNECT_FAIL = "BOTTLE.ACTION_CONNECT_FAIL";
    // 硬件连接成功
    public final static String BOTTLE_ACTION_DEVICE_CONNECT_SUCCESS = "BOTTLE.ACTION_CONNECT_SUCCESS";

    public final static String BOTTLE_ACTION_ROM_VER_COMING = "BOTTLE_ACTION_ROM_VER_COMING";
    public final static String BOTTLE_ACTION_SN_COMING = "BOTTLE_ACTION_SN_COMING";

    public final static String BOTTLE_ACTION_DATA_COMING = "BOTTLE_ACTION_DATA_COMING";

    public final static String ACTION_WARM_STATUS = "ACTION_WARM_STATUS";

    public final static String ACTION_WARM_SWITCH = "ACTION_WARM_SWITCH";

    public final static String ACTION_TEMP_F_COMING = "ACTION_TEMP_F_COMING";




}

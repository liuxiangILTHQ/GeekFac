package com.geekid.geekfactest.ble;

import android.content.Context;

import com.geekid.geekfactest.AppContext;
import com.geekid.geekfactest.model.DataInfo;
import com.geekid.geekfactest.utils.LogUtils;

public class CommandManager
{
    private Context context;

    public CommandManager(Context context)
    {
        this.context = context;
    }

    public interface IXuxukouData{
        void temphumReceive(int temp,int hum);
        void batteryReceive(int batt);
        void verReceive(String ver);
        void snReceive(String sn);
        void otherReceive(String s);
    }
    IXuxukouData iXuxukouData;
    public void setCommandParse(IXuxukouData iXuxukouData){
        this.iXuxukouData=iXuxukouData;
    }

    public static String byteToHexString(byte[] bArray)
    {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++)
        {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString)
    {
        if (hexString == null || hexString.equals(""))
        {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++)
        {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c)
    {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String hexstringToChar(String s)
    {
        String ss = "";
        for (int i = 0; i < s.length(); i = i + 2)
        {
            String tt = s.substring(i, i + 2);
            int code = Integer.parseInt(tt, 16);
            char ch = (char) code;
            ss += ch;
        }
        return ss;
    }

    public static String int2DecStr(int n)
    {
        String str = Integer.toHexString(n);
        int l = str.length();
        if (l % 2 != 0)
        {
            str = "0" + str;
        }

        return str;
    }

    public int decStr2Int(String dec)
    {
        return Integer.parseInt(dec, 16);
    }

    /**
     * 接收的返回数
     *
     * @param value
     */
    public void onRecvXuxukouData(byte[] value)
    {
        String valueStr = byteToHexString(value);
        //AppContext.logInfo("onRecvXuxukouData:" + valueStr);
        //mCallbacks.onRecvHexStr(valueStr);
        if (value.length == 12)//valueStr:00 00 00 00 12 00 41 60 50 60 00 02
        {
            iXuxukouData.snReceive(valueStr);
            BleUtils.broadcastUpdate(context, BleConstants.PEE_ACTION_DATA_COMING, valueStr);
            return;
        }
        // if (!valueStr.startsWith("55") || value.length != 20) {
        // return;
        // }
        // �?查校验码
        int len = value[0];
        byte checkNum = 0x00;
        for (int i = 0; i < len; i++)
        {
            checkNum ^= value[i];
        }
        if (checkNum != value[len])
        {
            return;
        }
        switch (value[1])
        {
            case 0x01:// 温度湿度 0x06, 0x01, 0x01, 0x44, 0x02, 0xAD�?0x8B
                int temperature = decStr2Int(valueStr.substring(4, 8));
                int humidity = decStr2Int(valueStr.substring(8, 12));
                // AppContext.logInfo("temperature:"+temperature+" humidity:"+humidity);
                DataInfo dataInfos = new DataInfo();
                dataInfos.setTemperature(temperature);
                dataInfos.setHumidity(humidity);
                dataInfos.setTime(System.currentTimeMillis());
                iXuxukouData.temphumReceive(temperature,humidity);
                BleUtils.broadcastUpdate(context, BleConstants.PEE_ACTION_DATA_UPDATED, dataInfos);
                break;
            case 0x02:// 电量 0x03, 0x02, 0x50, 0x74
                int battery = decStr2Int(valueStr.substring(4, 6));
                // AppContext.logInfo("battery:"+battery);
                iXuxukouData.batteryReceive(battery);
                BleUtils.broadcastUpdate(context, BleConstants.PEE_ACTION_DEVICE_SOC_UPDATED, battery + "");
                break;
            case 0x03:// 0x04, 0x03, 0x01, 0x0A, 0x95
                int xuxu_type = decStr2Int(valueStr.substring(4, 6));
                int ver = decStr2Int(valueStr.substring(6, 8));
                AppContext.logInfo("rom_ver:" + xuxu_type + "" + ver);
                iXuxukouData.verReceive(xuxu_type + "" + ver);
                BleUtils.broadcastUpdate(context, BleConstants.PEE_ACTION_DATA_COMING, xuxu_type + "" + ver);
                break;
            case 0x31:// 06 31 00 00 10 43 64
                String senId = valueStr.substring(10, 12) + valueStr.substring(8, 10) + valueStr.substring(6, 8) + valueStr.substring(4, 6);
                AppContext.logInfo("senId:" + senId);
                iXuxukouData.otherReceive(senId);
                BleUtils.broadcastUpdate(context, BleConstants.PEE_ACTION_DATA_COMING, senId);
                break;
            default:
                break;
        }
    }

    public void onTempRecvBattery(byte[] value)
    {
        String valueStr = byteToHexString(value);
        try
        {
            int battery = decStr2Int(valueStr.substring(0, 2));
            LogUtils.i("battery:" + battery);
            BleUtils.broadcastUpdate(context, BleConstants.TEMP_ACTION_DEVICE_SOC_UPDATED, battery + "");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onTempRecvData(byte[] value)
    {
        String valueStr = byteToHexString(value);
        LogUtils.i("onTempRecvData:" + valueStr);
        if (BleConstants.type == 1)
        {
            int data1 = decStr2Int(valueStr.substring(2, 4));
            int data2 = decStr2Int(valueStr.substring(4, 6));
            if (data1 < 0)
            {
                data1 = data1 & 0x00FF;
                data2 = data2 & 0x00FF;
            }
            int temperature = ((data1) | (data2 << 8)) & 0x00FFFFFF;

            DataInfo dataInfos = new DataInfo();
            dataInfos.setTemperature(temperature);
            dataInfos.setTime(System.currentTimeMillis());
            BleUtils.broadcastUpdate(context, BleConstants.TEMP_ACTION_DATA_UPDATED, dataInfos);
            LogUtils.i("temperature:" + temperature);
            //BleUtils.broadcastUpdate(context, BleConstants.TEMP_ACTION_DATA_UPDATED, temperature+"");
            //mTempCallbacks.OnRecvDataInfo(temperature);
        } else if (BleConstants.type == 0)
        {
            if (value.length == 12)//valueStr:00 00 00 00 12 00 41 60 50 60 00 02
            {
                BleUtils.broadcastUpdate(context, BleConstants.TEMP_ACTION_DATA_COMING, valueStr);
                return;
            }
            //valueStr FFFFFFFFFFFFFFFFFFFFFFFF   000000001200916061200005
            //AA56312E3030 版本号
            if (valueStr.startsWith("AA"))//AA313033
            {
                String rom_ver = valueStr.substring(2, valueStr.length());
                BleUtils.broadcastUpdate(context, BleConstants.TEMP_ACTION_DATA_COMING, rom_ver);
                return;
            }
            int v = value[1];
            switch (v)
            {
                case 0x01:
                    int temperature = decStr2Int(valueStr.substring(4, 8)) * 10;
                    DataInfo dataInfos = new DataInfo();
                    dataInfos.setTemperature(temperature);
                    dataInfos.setTime(System.currentTimeMillis());
                    BleUtils.broadcastUpdate(context, BleConstants.TEMP_ACTION_DATA_UPDATED, dataInfos);
                    LogUtils.i("temperature:" + temperature);
                    //mTempCallbacks.OnRecvDataInfo(temperature);
                    break;
                case 0x02://04020006
                    int battery = decStr2Int(valueStr.substring(4, 6));
                    LogUtils.i("battery:" + battery);
                    BleUtils.broadcastUpdate(context, BleConstants.TEMP_ACTION_DEVICE_SOC_UPDATED, battery + "");
                    //mCallbacks.onRecvBattery(battery);
                    break;
            }
        }
    }

    int currTemp=0;

    public void onRecvBottleData(byte[] value)
    {
        String valueStr = byteToHexString(value);

        //AppContext.logInfo("onRecvXuxukouData:" + valueStr);
        //mCallbacks.onRecvHexStr(valueStr);
        switch (value[1])
        {
            case 0x01:// 温度 0xAA, 0x01, 0x02, 0x01, 0x44, 0xEC  AA01020118B0
                int temperature = decStr2Int(valueStr.substring(6, 10));
                currTemp=temperature;
                //dataInfos.setTemperature(temperature);
                //dataInfos.setHumidity(0);
                //dataInfos.setTime(System.currentTimeMillis());
                //BleUtils.broadcastUpdate(context, BleConstants.BOTTLE_ACTION_DATA_UPDATED, dataInfos);
                //AppContext.logInfo("temperature:"+temperature);
                //mCallbacks.OnRecvTemp(temperature);
                break;
            case 0x02:// 角度AA02020173D8
                int pos = decStr2Int(valueStr.substring(6, 8));
                int angle = decStr2Int(valueStr.substring(8, 10));
                DataInfo dataInfos = new DataInfo();
                dataInfos.setTemperature(currTemp);
                dataInfos.setHumidity(angle);
                dataInfos.setTime(System.currentTimeMillis());
                BleUtils.broadcastUpdate(context, BleConstants.BOTTLE_ACTION_DATA_UPDATED, dataInfos);
                //AppContext.logInfo("pos:"+pos+",angle:"+angle);
                //mCallbacks.onRecvAngle(pos, angle);
                break;
            case 0x03:// 电量0xAA, 0x03, 0x01, 0x50, 0x9E]  4th byte
                int battery = decStr2Int(valueStr.substring(6, 8));
                AppContext.logInfo("battery:" + battery);
                BleUtils.broadcastUpdate(context, BleConstants.BOTTLE_ACTION_DEVICE_SOC_UPDATED, battery + "");
                //mCallbacks.onRecvBattery(battery);
                break;
            case 0x04:// 饮奶AA040720161013174319D1
                int year1 = decStr2Int(valueStr.substring(6, 8));
                int year2 = decStr2Int(valueStr.substring(8, 10));
                int month = decStr2Int(valueStr.substring(10, 12));
                int day = decStr2Int(valueStr.substring(12, 14));
                int hour = decStr2Int(valueStr.substring(14, 16));
                int min = decStr2Int(valueStr.substring(16, 18));
                int ms = decStr2Int(valueStr.substring(18, 20));
                String time = year1 + "" + year2 + "-" + month + "-" + day + " " + hour + ":" + min + ":" + ms;
                AppContext.logInfo("feed time:" + time);
                //mCallbacks.onFeed(time);
                break;
            case 0x05:// 紫外线消毒开启或者关闭[0xAA, 0x05, 0x07, 0x00, 0x00, 0x00, 0x01, 0x01, 0x09, 0x0F, 0x15
                int open = decStr2Int(valueStr.substring(6, 8));
                AppContext.logInfo("uv open:" + open);
                //mCallbacks.onRecvUVOpen(open);
                break;
            //AA060C000000001200815090110080  00000003100117022800001
            case 0x06://设备序列号 [0xAA, 0x05, 0x07, 0x00, 0x00, 0x00, 0x01, 0x01, 0x09, 0x0F, 0x15
                String sn = valueStr.substring(6);
                AppContext.logInfo("sn:" + sn);
                //mCallbacks.onRecvSn(sn);
                BleUtils.broadcastUpdate(context, BleConstants.BOTTLE_ACTION_DATA_COMING, sn);
                break;
            case 0x07:// AA0702010AA4
                int rom_ver1 = decStr2Int(valueStr.substring(6, 8));
                int rom_ver2 = decStr2Int(valueStr.substring(8, 10));
                String rom_ver = rom_ver1 + "" + rom_ver2;
                AppContext.logInfo("rom_ver:" + rom_ver);
                BleUtils.broadcastUpdate(context, BleConstants.BOTTLE_ACTION_DATA_COMING, rom_ver);
                //mCallbacks.onRecvRomVer(rom_ver);
                break;
            case 0x08:// 加热功能开关状态0xAA, 0x07, 0x02, 0x01, 0x44, 0xEC
                int warm_open = decStr2Int(valueStr.substring(6, 8));
                BleUtils.broadcastUpdate(context, BleConstants.ACTION_WARM_SWITCH, warm_open+"");
                //AppContext.logInfo("warm_open:" + warm_open);
                //mCallbacks.onRecvWarmOpen(warm_open);
                break;
            case 0x09:// 本地存储的3 次饮奶动作的时间.
                break;
            case 0x10:
                break;
            case 0x11:
                break;
            case 0x12:
                break;
            case 0x13://反馈设置的温度阈值
                int tem_f = decStr2Int(valueStr.substring(6, 8));
                AppContext.logInfo("tem_f:" + tem_f);
                BleUtils.broadcastUpdate(context, BleConstants.ACTION_TEMP_F_COMING, tem_f+"");
                //mCallbacks.onRecvTempF(tem_f);
                break;
            case 0x14://是否达到设置的温度阈值,已经加热到指定温度
                int tem_f_status = decStr2Int(valueStr.substring(6, 8));
                AppContext.logInfo("tem_f_status:" + tem_f_status);
                break;
            case 0x15://反馈奶瓶实时的加热状态
                int warm_status = decStr2Int(valueStr.substring(6, 8));
                //AppContext.logInfo("warm_status:" + warm_status);
                //mCallbacks.onRecvWarmStatus(warm_status);
                BleUtils.broadcastUpdate(context, BleConstants.ACTION_WARM_STATUS, warm_status + "");
                break;
            case 0x16://反馈奶瓶实时的工作模式，0x01 为饮奶模式，0x00 为冲泡模式
                int work_status = decStr2Int(valueStr.substring(6, 8));
                //AppContext.logInfo("work_status:" + work_status);
                //mCallbacks.onRecvWorkStatus(work_status);
                break;
            default:
                break;
        }
    }


}

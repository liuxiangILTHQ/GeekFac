package com.geekid.geekfactest.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.geecare.blelibrary.Constants;
import com.geekid.geekfactest.AppContext;

public class BottleService extends Service
{

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothGatt mBluetoothGatt;

    private String mDeviceAddress;
    private int mConnectState = BluetoothProfile.STATE_DISCONNECTED;

    private IBinder mBinder;
    private CommandManager mWatereverCmdMgr;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mBinder = new LocalBinder();
        mWatereverCmdMgr = new CommandManager(this);
    }

    /**
     * 当被startService启动时（可被执行多次�?
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // return super.onStartCommand(intent, flags, startId);
        AppContext.logDebug("BottleService onStart!");
        if (intent != null)
        {
            if (intent.getStringExtra(Constants.BOTTLE_ADDR) != null)
            {
                mDeviceAddress = intent.getStringExtra(Constants.BOTTLE_ADDR);
                connect(mDeviceAddress);
            }
            if (myHandler != null)
            {
                myHandler.removeCallbacksAndMessages(null);
                myHandler.sendEmptyMessage(0);
            } else
            {
                AppContext.logDebug("myHandler is null");
            }
        }
        return START_STICKY;
    }

    /**
     * 当被bindService绑定时（也可多次�?
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    /**
     * 当被unbindService解除绑定时（也可多次�?
     */
    @Override
    public boolean onUnbind(Intent intent)
    {
        // 断开连接、清理资�?
        // close();
        return true;
    }

    /**
     * 当被stopService通知停止�? �? 被unbindSerivce后（bindService启动的情形）
     */
    @Override
    public void onDestroy()
    {
        AppContext.logDebug("Service onDestroy!");
        // 取消循环任务
        jobHandler.removeCallbacksAndMessages(null);
        myHandler.removeCallbacksAndMessages(null);
        // 释放掉占用的资源
        disconnect();
        super.onDestroy();
    }

    public void removeHandler()
    {
        jobHandler.removeCallbacksAndMessages(null);
        // 取消循环任务
        myHandler.removeCallbacksAndMessages(null);
    }

    public class LocalBinder extends Binder
    {
        public BottleService getService()
        {
            return BottleService.this;
        }
    }

    private int times = 0;// 重连次数
    // 蓝牙连接�?�?
    private Handler myHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            //AppContext.logDebug("myHandler run ");
            if (!BleUtils.isSupportBLE(BottleService.this) || !BleUtils.isBluttoothEnable())
            {
                BleUtils.broadcastUpdate(BottleService.this, BleConstants.ACTION_BLE_NOT_ENABLE);
                myHandler.sendEmptyMessageDelayed(0, 8000);
                return;
            }
            if (null != mDeviceAddress && !"".equals(mDeviceAddress))
            {
                if (isConnected(mDeviceAddress))
                {
                    readRssi();
                    //AppContext.logDebug("isConnected");
                    myHandler.sendEmptyMessageDelayed(0, 3000);
                } else
                {
                    AppContext.logDebug(mDeviceAddress + " not Connected");
                    times++;
                    if (times >= 3)
                    {
                        disconnect();
                        times = 0;
                    }
                    if (mConnectState == BluetoothProfile.STATE_DISCONNECTED)
                    {
                        AppContext.logDebug("myHandler not connected,begin connect " + mDeviceAddress);
                        connect(mDeviceAddress);
                    }
                    myHandler.sendEmptyMessageDelayed(0, 8000);
                }

            }

        }
    };

    final int What=1000;
    // 蓝牙连接成功才启动次任务循环
    private Handler jobHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 1000)
            {// 蓝牙连接成功
                times = 0;
                myHandler.removeCallbacksAndMessages(null);
                myHandler.sendEmptyMessageDelayed(0, 1000);

                write("5515");//get warm switch
                jobHandler.sendEmptyMessageDelayed(What+1, 300);

            }else if (msg.what == What+1)
            {
                write("5514");//get tempf

                //write("55100128");//set tempf
                jobHandler.sendEmptyMessageDelayed(What+2, 300);
            }
            else if (msg.what == What+2)
            {
                write("551201");//open warm
                jobHandler.sendEmptyMessageDelayed(What+3, 3000);
            }else if (msg.what == What+3)
            {
                write("55100128");//set tempf
            }

        }
    };


    public static long start_time = 0;
    public static long end_time = 0;

    public synchronized boolean connect(final String address)
    {
        if (address == null || "".equals(address.trim()))
        {
            // AppContext.logError("unspecified device address.");
            return false;
        }
        // TODO 如果已经连接了设备，应先关闭
        mConnectState = BluetoothProfile.STATE_DISCONNECTED;
        start_time = System.currentTimeMillis();
        disconnect();

        if (mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
            {
                AppContext.logError("Unable to initialize BluetoothManager.");
                BleUtils.broadcastUpdate(BottleService.this, BleConstants.ACTION_BLE_NOT_ENABLE);
                return false;
            }
        }
        if (mBluetoothAdapter == null)
        {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
            {
                AppContext.logError("Unable to obtain a BluetoothAdapter.");
                BleUtils.broadcastUpdate(BottleService.this, BleConstants.ACTION_BLE_NOT_ENABLE);
                return false;
            }
        }

        mDeviceAddress = address;
        connect_step2(); // 执行第二�?
        return true;
    }

    /**
     * 连接第二�?
     */
    private synchronized void connect_step2()
    {
        if (mBluetoothAdapter == null || mDeviceAddress == null)
        {
            disconnect();// 20150609
            return;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        if (device == null)
        {
            AppContext.logInfo("connect_step2()  Device not found.");
            disconnect();// 20150609
            return;
        }

        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        if (mBluetoothGatt == null)
        {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            AppContext.logInfo("connect_step2()  connectGatt");
        }
        mConnectState = BluetoothProfile.STATE_CONNECTING;
        BleUtils.broadcastUpdate(BottleService.this, BleConstants.BOTTLE_ACTION_DEVICE_CONNECTING);
    }

    /**
     * (3)蓝牙GATT回调�?
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            super.onReadRemoteRssi(gatt, rssi, status);
            BleUtils.broadcastUpdate(BottleService.this, "rssi_coming", rssi + "");
            AppContext.logInfo("onReadRemoteRssi():" + rssi);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            // 建立连接
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                // 成功建立连接后，启动服务发现
                boolean result = mBluetoothGatt.discoverServices();
                if (!result)
                {
                    AppContext.logInfo("FAIL to start service discovery.");
                    mConnectState = BluetoothProfile.STATE_DISCONNECTED;
                    disconnect();
                    // close();
                } else
                {
                    AppContext.logInfo("SUCCESS start service discovery.");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                AppContext.logInfo("Disconnected from GATT server.");
                disconnect();
                // TODO 是否�?�?
                // AppContext.logInfo("reconnect");
                // connect(Store.getInstance(BLEService.this).getString("deviceAddr",
                // ""));//
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            AppContext.logInfo("onServicesDiscovered status " + status);
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                AppContext.logInfo("BluetoothGatt.GATT_SUCCESS");
                BluetoothGattService rxService = mBluetoothGatt.getService(BleConstants.BOTTLE_SERVICE_UUID_G);
                if (rxService == null)
                {
                    AppContext.logInfo("Rx service not found!");
                    mConnectState = BluetoothProfile.STATE_DISCONNECTED;
                    disconnect();
                    return;
                }

                BluetoothGattCharacteristic txChar = rxService.getCharacteristic(BleConstants.BOTTLE_READ_CHAR_UUID_G);
                if (txChar == null)
                {
                    AppContext.logInfo("Tx charateristic not found!");
                    mConnectState = BluetoothProfile.STATE_DISCONNECTED;
                    disconnect();
                    return;
                }

                boolean bol;
                //boolean bol = mBluetoothGatt.setCharacteristicNotification(txChar, true);
                //AppContext.logInfo("setCharacteristicNotification " + bol+" getProperties "+txChar.getProperties());
                for (int i = 0; i < txChar.getDescriptors().size(); i++)
                {
                    BluetoothGattDescriptor descriptor = txChar.getDescriptors().get(i);
                    AppContext.logInfo("descriptor.getUuid:" + descriptor.getUuid() + " getValue:" + descriptor.getValue());
                    BluetoothGattCharacteristic bc = descriptor.getCharacteristic();
                    //descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }

                for (int i = 0; i < txChar.getDescriptors().size(); i++)
                {
                    BluetoothGattDescriptor descriptor = txChar.getDescriptors().get(i);
                    AppContext.logInfo("descriptor.getUuid:" + descriptor.getUuid() + " getValue:" + CommandManager.byteToHexString(descriptor.getValue()));

                }
                bol = mBluetoothGatt.setCharacteristicNotification(txChar, true);
                AppContext.logInfo("setCharacteristicNotification " + bol + " getProperties " + txChar.getProperties());

                mConnectState = BluetoothProfile.STATE_CONNECTED;

                // 蓝牙连接成功
                AppContext.isConnected = true;
                end_time = System.currentTimeMillis();
                BleUtils.broadcastUpdate(BottleService.this, BleConstants.BOTTLE_ACTION_DEVICE_CONNECT_SUCCESS);
                jobHandler.sendEmptyMessageDelayed(1000, 1500);
            } else
            {
                AppContext.logInfo("onServicesDiscovered received: " + status);
                mConnectState = BluetoothProfile.STATE_DISCONNECTED;
                disconnect();
                BleUtils.broadcastUpdate(BottleService.this, BleConstants.BOTTLE_ACTION_DEVICE_CONNECT_FAIL);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status)
        {
            AppContext.logInfo("onCharacteristicRead" + characteristic.getUuid());
            if (status == BluetoothGatt.GATT_SUCCESS && BleConstants.BOTTLE_READ_CHAR_UUID_G.equals(characteristic.getUuid()))
            {
                try
                {
                    //mWatereverCmdMgr.onRecvXuxukouData(characteristic.getValue());
                } catch (Exception e)
                {
                    AppContext.logInfo("处理接收数据失败", e);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            //AppContext.logInfo("onCharacteristicChanged getUuid " + characteristic.getUuid());
            if (BleConstants.BOTTLE_READ_CHAR_UUID_G.equals(characteristic.getUuid()))
            {
                try
                {
                    String valueStr = CommandManager.byteToHexString(characteristic.getValue());
                    Log.d("BLEService", "onCharacteristicChanged:" + valueStr);
                    mWatereverCmdMgr.onRecvBottleData(characteristic.getValue());
                } catch (Exception e)
                {
                    AppContext.logInfo("处理接收数据失败", e);
                }
            }
        }
    };

    /**
     * 向设备发送一条命�?/数据 注意水杯有些命令是分多次发�?�的，注意再次封装此方法 处理接收数据的时候也是一样，要注意是否是分多次发回的�?
     *
     * @param value
     */
    public void writeRXCharacteristic(byte[] value) throws Exception
    {
        if (null != mBluetoothGatt)
        {
            BluetoothGattService RxService = mBluetoothGatt.getService(BleConstants.BOTTLE_SERVICE_UUID_G);
            // AppContext.logInfo("mBluetoothGatt null"+ mBluetoothGatt);
            if (RxService == null)
            {
                // AppContext.logInfo("Rx service not found!");
                mConnectState = BluetoothProfile.STATE_DISCONNECTED;
                disconnect();
                throw new Exception("DEVICE DISCONNECTED");
            }

            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(BleConstants.BOTTLE_WRITE_CHAR_UUID_G);
            if (RxChar == null)
            {
                // AppContext.logInfo("Rx charateristic not found!");
                mConnectState = BluetoothProfile.STATE_DISCONNECTED;
                disconnect();
                throw new Exception("DEVICE DISCONNECTED");
                /*
				 * broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART); return;
				 */
            }

            RxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);// lx
            RxChar.setValue(value);
            boolean status = mBluetoothGatt.writeCharacteristic(RxChar);

            // AppContext.logInfo("write TXchar - status=" + status);
        } else
        {
            // AppContext.logInfo("Send Command fail! Gatt is null.");
            mConnectState = BluetoothProfile.STATE_DISCONNECTED;
            disconnect();
            // throw new Exception("DEVICE DISCONNECTED");
            AppContext.logInfo("DEVICE DISCONNECTED");
        }
    }

    public void readRssi()
    {
        mBluetoothGatt.readRemoteRssi();
    }

    public boolean isConnected(String address)
    {
        if (!BleUtils.isSupportBLE(this))
        {
            return false;
        }
        // 蓝牙被禁用了
        if (!BleUtils.isBluttoothEnable())
        {
            BleUtils.broadcastUpdate(this, BleConstants.ACTION_BLE_NOT_ENABLE);
            return false;
        }

        if (this.mConnectState == BluetoothProfile.STATE_CONNECTING)
        {
            return false;
        }
        if (mConnectState == BluetoothProfile.STATE_DISCONNECTED)
        {
            return false;
        }

        if (null != this.mDeviceAddress && this.mDeviceAddress.equals(address))
        {
            if (null != mBluetoothGatt)
            {
                if (mConnectState == BluetoothProfile.STATE_CONNECTED)
                {
                    return true;
                }
            }
        }

        return false;
    }

    // 判断是否已经连上设备
    public boolean isConnected()
    {
        if (null != this.mDeviceAddress && !"".equals(this.mDeviceAddress))
        {
            if (isConnected(this.mDeviceAddress))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 断开连接
     */
    public void disconnect()
    {
        try
        {
            AppContext.isConnected = false;
            jobHandler.removeCallbacksAndMessages(null);
            AppContext.logInfo("i disconnect");
            mConnectState = BluetoothProfile.STATE_DISCONNECTED;

            if (null != mBluetoothGatt)
            {
                try
                {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            BleUtils.broadcastUpdate(this, BleConstants.BOTTLE_ACTION_DEVICE_CONNECT_FAIL);
            mBluetoothGatt = null;
            mBluetoothAdapter = null;
            mBluetoothManager = null;
            // mGattCallback=null;
        } catch (Exception e)
        {

        }
        // jobHandler.sendEmptyMessageDelayed(1001, 1000);
    }

    //AA6900
    // 5501 手机主动获取温度和湿度
    // 55020c+12字节sn
    // 5503 手机获取设备序列号
    // 5504 手机发送空中升级指令
    // 5506 获取固件版本号
    public void write(String hexString)
    {
        byte[] data = mWatereverCmdMgr.hexStringToBytes(hexString);
        AppContext.logInfo(hexString);
        // for (int i = 0; i < data.length; i++)
        // {
        // AppContext.logInfo("byte data:" + data[i]);
        // }
        try
        {
            writeRXCharacteristic(data);
        } catch (Exception e)
        {
            AppContext.logInfo("writeRXCharacteristic Exception");
            e.printStackTrace();
        }
    }
}

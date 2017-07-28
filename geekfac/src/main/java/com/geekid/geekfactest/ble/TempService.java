package com.geekid.geekfactest.ble;

import android.annotation.SuppressLint;
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
import com.geekid.geekfactest.utils.LogUtils;
import com.geekid.geekfactest.utils.SharedPreferencesUtils;

import java.util.List;
import java.util.UUID;


public class TempService extends Service
{
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private static BluetoothGatt mBluetoothGatt;

    List<BluetoothGattService> bluetoothGattServiceList;

    private String mDeviceAddress;
    private int mConnectState = BluetoothProfile.STATE_DISCONNECTED;

    private IBinder mBinder;
    private CommandManager mWatereverCmdMgr;

    /**
     * 当Service被创建时（只会执行一次）
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
        //context=this;
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
        LogUtils.d("TEMP Service onStart!");
        if(intent.getStringExtra(Constants.TEMP_ADDR)!=null)
        {
            mDeviceAddress = intent.getStringExtra(Constants.TEMP_ADDR);
            //SharedPreferencesUtils.putString(this, Constants.TEMP_ADDR,mDeviceAddress);
            connect(mDeviceAddress);
        }

        if (myHandler != null)
        {
            myHandler.removeCallbacksAndMessages(null);
            myHandler.sendEmptyMessage(0);
        } else
        {
           LogUtils.i("myHandler is null");
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
        LogUtils.i("Service onDestroy!");
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
        public TempService getService()
        {
            return TempService.this;
        }
    }

    private int times = 0;// 重连次数
    private Handler myHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            LogUtils.i("myHandler run ");
            if (!BleUtils.isSupportBLE(TempService.this)
                    || !BleUtils.isBluttoothEnable())
            {
                BleUtils.broadcastUpdate(TempService.this,BleConstants.ACTION_BLE_NOT_ENABLE);
                myHandler.sendEmptyMessageDelayed(0, 8000);
                return;
            }
            if (null != mDeviceAddress && !"".equals(mDeviceAddress))
            {
                if (isConnected(mDeviceAddress))
                {
                    // AppContext.logDebug("isConnected");
                } else
                {
                    LogUtils.i(mDeviceAddress + " not Connected");
                    times++;

                    if (times >= 3)
                    {
                        disconnect();
                        times = 0;
                        mDeviceAddress = SharedPreferencesUtils.getString(TempService.this,Constants.TEMP_ADDR, "");
                    }
                    if (mConnectState == BluetoothProfile.STATE_DISCONNECTED)
                    {
                        LogUtils.i("myHandler not connected,begin connect "
                                        + mDeviceAddress);
                        connect(mDeviceAddress);
                    }
                }
                myHandler.sendEmptyMessageDelayed(0, 5000);
            } else
            {
                mDeviceAddress = SharedPreferencesUtils.getString(TempService.this,Constants.TEMP_ADDR, "");
                // AppContext.logDebug("no deivce addr get addr: " +
                // mDeviceAddress);
                myHandler.sendEmptyMessage(0);
                // myHandler.sendEmptyMessageDelayed(0, 5000);
            }

        }
    };

    // 蓝牙连接成功才启动次任务循环
    private Handler jobHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 1000)
            {// 蓝牙连接成功
                times = 0;
                if (BleConstants.type == 1)
                {
                    Log.d("lx","get BATTERY");
                    get(BleConstants.TEMP_BATTERY_SERVICE_UUID,
                            BleConstants.TEMP_BATTERY_CHAR_UUID,
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else if (BleConstants.type == 0)
                {
                    //write("5606");//AA56312E3030   V1.00
                    //write("5603");//FFFF FFFF FFFF FFFF FFFF FFFF

                    //broadcastUpdate(ACTION_ROM_VER_COMING);
                }
                myHandler.removeCallbacksAndMessages(null);
                myHandler.sendEmptyMessageDelayed(0, 1000);
            } else if (msg.what == 1001)
            {// 断开蓝牙

            } else if (msg.what == 2000)
            {

            }
        }
    };
    public static long start_time=0;
    public static long end_time=0;
    /**
     * (2)连接到水�?
     *
     * @param address
     * @return
     */
    public synchronized boolean connect(final String address)
    {
        if (address == null || "".equals(address.trim()))
        {
            // AppContext.logError("unspecified device address.");
            return false;
        }
        start_time=System.currentTimeMillis();
        // TODO 如果已经连接了设备，应先关闭
        mConnectState = BluetoothProfile.STATE_DISCONNECTED;
        disconnect();
        //isDeviceScanned = false;

        if (mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
            {
                BleUtils.broadcastUpdate(TempService.this,BleConstants.ACTION_BLE_NOT_ENABLE);
                return false;
            }
        }
        if (mBluetoothAdapter == null)
        {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
            {
                BleUtils.broadcastUpdate(TempService.this,BleConstants.ACTION_BLE_NOT_ENABLE);
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
        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(mDeviceAddress);
        if (device == null)
        {
            LogUtils.i("connect_step2()  Device not found.");
            disconnect();// 20150609
            return;
        }

        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        if (mBluetoothGatt == null)
        {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            LogUtils.i("connect_step2()  connectGatt");
        }
        mConnectState = BluetoothProfile.STATE_CONNECTING;
        BleUtils.broadcastUpdate(TempService.this,BleConstants.TEMP_ACTION_DEVICE_CONNECTING);
    }

    public void write(String s)
    {
        if (BleConstants.type == 1)
        {
            // byte[] value = hexStringToBytes(s);
            BluetoothGattService bluetoothGattServic = mBluetoothGatt
                    .getService(BleConstants.TEMP_UART_SERVICE_UUID);
            BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattServic
                    .getCharacteristic(BleConstants.TEMP_UART_WRITE_CHAR_UUID);

            bluetoothGattCharacteristic
                    .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            bluetoothGattCharacteristic.setValue(s);

            boolean status = mBluetoothGatt
                    .writeCharacteristic(bluetoothGattCharacteristic);
            mBluetoothGatt.setCharacteristicNotification(
                    bluetoothGattCharacteristic, true);
        } else if (BleConstants.type == 0)
        {
            AppContext.logInfo("write:"+s);
            byte[] value = mWatereverCmdMgr.hexStringToBytes(s);
            BluetoothGattService bluetoothGattServic = mBluetoothGatt
                    .getService(BleConstants.TEMP_SERVICE_UUID_G);
            BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattServic
                    .getCharacteristic(BleConstants.TEMP_WRITE_CHAR_UUID_G);

            bluetoothGattCharacteristic
                    .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            bluetoothGattCharacteristic.setValue(value);

            boolean status = mBluetoothGatt
                    .writeCharacteristic(bluetoothGattCharacteristic);
            mBluetoothGatt.setCharacteristicNotification(
                    bluetoothGattCharacteristic, true);
        }

    }

    public void read()
    {
        if (BleConstants.type == 1)
        {
            BluetoothGattService bluetoothGattServic = mBluetoothGatt
                    .getService(BleConstants.TEMP_UART_SERVICE_UUID);
            BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattServic
                    .getCharacteristic(BleConstants.TEMP_UART_READ_CHAR_UUID);

            for (int k = 0; k < bluetoothGattCharacteristic.getDescriptors().size(); k++)
            {
                BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic
                        .getDescriptors().get(k);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
            mBluetoothGatt.setCharacteristicNotification(
                    bluetoothGattCharacteristic, true);
        } else if (BleConstants.type == 0)
        {
            BluetoothGattService bluetoothGattServic = mBluetoothGatt
                    .getService(BleConstants.TEMP_SERVICE_UUID_G);
            BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattServic
                    .getCharacteristic(BleConstants.TEMP_READ_CHAR_UUID_G);

            for (int k = 0; k < bluetoothGattCharacteristic.getDescriptors().size(); k++)
            {
                BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptors().get(k);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
            mBluetoothGatt.setCharacteristicNotification(
                    bluetoothGattCharacteristic, true);
        }
    }

    public void get(UUID serviceUUID, UUID charUUID, byte[] value)
    {
        BluetoothGattService bluetoothGattServic = mBluetoothGatt.getService(serviceUUID);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattServic.getCharacteristic(charUUID);
        for (int k = 0; k < bluetoothGattCharacteristic.getDescriptors().size(); k++)
        {
            BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic
                    .getDescriptors().get(k);
            // BluetoothGattCharacteristic bc=descriptor.getCharacteristic();
            descriptor.setValue(value);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        mBluetoothGatt.setCharacteristicNotification(
                bluetoothGattCharacteristic, true);
    }



    /**
     * (3)蓝牙GATT回调
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
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
                    LogUtils.i("FAIL to start service discovery.");
                    mConnectState = BluetoothProfile.STATE_DISCONNECTED;
                    disconnect();
                    // close();
                } else
                {
                    LogUtils.i("SUCCESS start service discovery.");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                LogUtils.i("Disconnected from GATT server.");
                disconnect();
                // AppContext.logInfo("reconnect");
                // connect(Store.getInstance(BLEService.this).getString("deviceAddr",
                // ""));//
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            LogUtils.i("onServicesDiscovered status " + status);
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                bluetoothGattServiceList = mBluetoothGatt.getServices();
                for (int i = 0; i < bluetoothGattServiceList.size(); i++)
                {
                    BluetoothGattService bluetoothGattService = bluetoothGattServiceList
                            .get(i);
                    if (bluetoothGattService.getUuid().equals(BleConstants.TEMP_SERVICE_UUID))
                    {
                        BleConstants.type = 1;
                        break;
                    } else if (bluetoothGattService.getUuid().equals(BleConstants.TEMP_SERVICE_UUID_G))
                    {
                        BleConstants.type = 0;
                        break;
                    }
                }
                for (int i = 0; i < bluetoothGattServiceList.size(); i++)
                {
                    BluetoothGattService bluetoothGattService = bluetoothGattServiceList
                            .get(i);
                    LogUtils.i("Service UUID-" + i + ":" + bluetoothGattService.getUuid());

                    List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList = bluetoothGattService
                            .getCharacteristics();
                    for (int j = 0; j < bluetoothGattCharacteristicList.size(); j++)
                    {
                        BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattCharacteristicList
                                .get(j);
                        LogUtils.i("Characteristic UUID-" + i + "-" + j + ":"
                                + bluetoothGattCharacteristic.getUuid());
                        if (bluetoothGattCharacteristic.getUuid().equals(BleConstants.TEMP_CHAR_UUID))
                        {
                            LogUtils.i("TEMP_CHAR_UUID");
                            get(BleConstants.TEMP_SERVICE_UUID,
                                    BleConstants.TEMP_CHAR_UUID,
                                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

                        } else if (bluetoothGattCharacteristic.getUuid().equals(BleConstants.TEMP_BATTERY_CHAR_UUID))
                        {
                            LogUtils.i("BATTERY_CHAR_UUID");
                            get(BleConstants.TEMP_BATTERY_SERVICE_UUID,
                                    BleConstants.TEMP_BATTERY_CHAR_UUID,
                                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        } else if (bluetoothGattCharacteristic.getUuid()
                                .equals(BleConstants.TEMP_UART_WRITE_CHAR_UUID))
                        {

                        } else if (bluetoothGattCharacteristic.getUuid()
                                .equals(BleConstants.TEMP_UART_READ_CHAR_UUID))
                        {

                        }
                        // mBluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic,
                        // true);
                    }
                }

                read();

                LogUtils.i("BluetoothGatt.GATT_SUCCESS");
                end_time=System.currentTimeMillis();
                mConnectState = BluetoothProfile.STATE_CONNECTED;

                AppContext.isConnected = true;
                BleUtils.broadcastUpdate(TempService.this,BleConstants.TEMP_ACTION_DEVICE_CONNECT_SUCCESS);

                jobHandler.sendEmptyMessageDelayed(1000, 1500);
            } else
            {
                LogUtils.i("onServicesDiscovered received: " + status);
                mConnectState = BluetoothProfile.STATE_DISCONNECTED;
                disconnect();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic
        )
        {
            //AppContext.logInfo("onCharacteristicChanged getUuid " + characteristic.getUuid());
            if (BleConstants.TEMP_CHAR_UUID.equals(characteristic.getUuid()) || BleConstants.TEMP_READ_CHAR_UUID_G.equals(characteristic.getUuid()))
            {
                try
                {
                    mWatereverCmdMgr.onTempRecvData(characteristic.getValue());
                } catch (Exception e)
                {
                    LogUtils.i("处理接收数据失败"+e);
                }
            } else if (BleConstants.TEMP_BATTERY_CHAR_UUID.equals(characteristic.getUuid()))
            {
                LogUtils.i("BATTERY_CHAR_UUID qqq");
                try
                {
                    mWatereverCmdMgr.onTempRecvBattery(characteristic.getValue());
                } catch (Exception e)
                {
                    LogUtils.i("处理接收数据失败"+e);
                }
            }
        }
    };

    /**
     * �?查是否已经连接到水杯
     *
     * @param address
     * @return
     */
    @SuppressLint("NewApi")
    public boolean isConnected(String address)
    {
        if (!BleUtils.isSupportBLE(this))
        {
            return false;
        }
        // 蓝牙被禁用了
        if (!BleUtils.isBluttoothEnable())
        {
            BleUtils.broadcastUpdate(TempService.this,BleConstants.ACTION_BLE_NOT_ENABLE);
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
            //jobHandler.removeCallbacks(saveData);
            jobHandler.removeCallbacksAndMessages(null);

            LogUtils.i("i disconnect");
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
            BleUtils.broadcastUpdate(TempService.this,BleConstants.TEMP_ACTION_DEVICE_CONNECT_FAIL);
            mBluetoothGatt = null;
            mBluetoothAdapter = null;
            mBluetoothManager = null;
            mDeviceAddress = null;
            // mGattCallback=null;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}


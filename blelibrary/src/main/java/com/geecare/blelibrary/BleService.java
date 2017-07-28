package com.geecare.blelibrary;

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

import com.geecare.blelibrary.callback.IBleComm;
import com.geecare.blelibrary.callback.IBleConn;
import com.geecare.blelibrary.callback.IBleScan;
import com.geecare.blelibrary.model.BleDevice;

public class BleService extends Service
{

    public static final String LOG_TAG = "blelib";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothGatt mBluetoothGatt;

    private BleDevice bleDevice;
    private String mDeviceName;
    private String mDeviceAddress;
    private int mConnectState = BluetoothProfile.STATE_DISCONNECTED;

    private IBinder mBinder;
    private IBleConn iBleConn;
    private CommandManager mWatereverCmdMgr;
    private BleTools bleTools;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mBinder = new LocalBinder();
        mWatereverCmdMgr = new CommandManager();

        bleTools = BleTools.getInstantce();


        if (isAutoConnect)
        {
            myHandler.removeCallbacksAndMessages(null);
            myHandler.sendEmptyMessage(0);
        }
    }


    public void setBleCallbacks(IBleConn iBleConn, IBleComm bleComm)
    {
        this.iBleConn = iBleConn;
        //mWatereverCmdMgr.setBleComm(bleComm);
    }

    private boolean isAutoConnect = true;//是否自动重连

    //设置是否自动重连
    public void setConnectAuto(boolean auto)
    {
        if (auto != isAutoConnect)
        {
            //if (auto)
            {
                myHandler.removeCallbacksAndMessages(null);
                myHandler.sendEmptyMessage(0);
            }
        }
        isAutoConnect = auto;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // return super.onStartCommand(intent, flags, startId);
        //Log.d(LOG_TAG, "PeeService onStart!");

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
        super.onDestroy();
        Log.d(LOG_TAG, "Service onDestroy!");

        // 释放掉占用的资源
        disconnect();
        removeHandler();
        bleTools.stopScan();
        bleTools = null;

    }

    private void removeHandler()
    {
        jobHandler.removeCallbacksAndMessages(null);
        // 取消循环任务
        myHandler.removeCallbacksAndMessages(null);
    }

    public class LocalBinder extends Binder
    {
        public BleService getService()
        {
            return BleService.this;
        }
    }

    private int times = 0;// 重连次数
    private Handler myHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Log.d(LOG_TAG, "myHandler run ");
            if (!BleTools.getInstantce().isBleEnabled() || !initBle())
            {
                iBleConn.connectStaus(Constants.ACTION_BLE_NOT_ENABLE);
                myHandler.sendEmptyMessageDelayed(0, 5000);
                return;
            }
            if (null == mDeviceAddress || ("").equals(mDeviceAddress))
            {
                Log.d(LOG_TAG, "myHandler run mDeviceAddress =null");
                myHandler.sendEmptyMessageDelayed(0, 5000);
                return;
            }
            if (!isAutoConnect)
            {
                return;
            }

            if (isConnected(mDeviceAddress))
            {
                //Log.d(LOG_TAG, mDeviceAddress+" isConnected");
                myHandler.sendEmptyMessageDelayed(0, 5000);
            } else
            {
                //AlarmContext.logInfo( mDeviceAddress + " not Connected");
                if (bleTools.getScanState())
                {
                    myHandler.sendEmptyMessageDelayed(0, 5000);
                    return;
                }

                if (mConnectState == BluetoothProfile.STATE_DISCONNECTED)
                {
                    //AlarmContext.logInfo( "myHandler not connected,begin connect " + mDeviceAddress);
                    setNeedScan(true);
                    connect(bleDevice);
                } else if (mConnectState == BluetoothProfile.STATE_CONNECTING)
                {
                    times++;
                    if (times > 4)
                    {
                        disconnect();
                        times = 0;
                    }

                }
                myHandler.sendEmptyMessageDelayed(0, 5000);
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
            }
        }
    };

    private boolean needScan = false;

    public void setNeedScan(boolean needScan)
    {
        this.needScan = needScan;
    }

    public boolean initBle()
    {
        if (mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
            {
                iBleConn.connectStaus(Constants.ACTION_BLE_NOT_ENABLE);
                return false;
            }
        }
        if (mBluetoothAdapter == null)
        {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null)
            {
                iBleConn.connectStaus(Constants.ACTION_BLE_NOT_ENABLE);
                return false;
            }
        }
        if (!mBluetoothAdapter.isEnabled())
        {
            iBleConn.connectStaus(Constants.ACTION_BLE_NOT_ENABLE);
            return false;
        }

        return true;
    }


    public synchronized boolean connect(BleDevice bDevice)
    {
        if (bDevice == null)
        {
            return false;
        }
        setConnectAuto(true);

        this.bleDevice = bDevice;

        String address = bleDevice.getBleMacAddr();
        if (address == null || "".equals(address.trim()))
        {
            return false;
        }
        mDeviceAddress = address;
        Log.d(LOG_TAG, "connect " + address);
        disconnect();

        boolean b = initBle();
        if (b)
        {
            Log.d(LOG_TAG, "needScan " + needScan);
            if (needScan)
            {
                scan();
            } else
            {
                bleTools.stopScan();
                connect_step2();
            }

        }
        return b;
    }

    private void scan()
    {
        bleTools.setBleScanCallback(new IBleScan()
        {
            @Override
            public void getScanBleDevices(BleDevice bleDevice)
            {
                if (bleDevice != null)
                {
                    if (bleDevice.getBleMacAddr().equals(mDeviceAddress))
                    {
                        Log.d(LOG_TAG, " found bledev");
                        bleTools.stopScan();
                        connect_step2();
                        return;
                    }
                }

            }

            @Override
            public void getScanStatus(boolean isScanning)
            {

            }
        });
        bleTools.startScan("");
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                bleTools.stopScan();
            }
        }, 8000);
    }

    private synchronized void connect_step2()
    {
        if (mBluetoothAdapter == null)
        {
            disconnect();// 20150609
            return;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        if (device == null)
        {
            //Log.d(LOG_TAG, "connect_step2()  Device not found.");
            disconnect();// 20150609
            return;
        }
        mDeviceName = device.getName();
        // We want to directly connect to the device, so we are setting the
        // autoConnect parameter to false.
        if (mBluetoothGatt == null)
        {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            Log.d(LOG_TAG, device.getName() + " connect_step2()  connectGatt");
        }
        mConnectState = BluetoothProfile.STATE_CONNECTING;
        iBleConn.connectStaus(Constants.PEE_ACTION_DEVICE_CONNECTING);
    }

    /**
     * (3)蓝牙GATT回调�?
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
                    //AlarmContext.logInfo( "FAIL to start service discovery.");
                    disconnect();
                } else
                {
                    //AlarmContext.logInfo( "SUCCESS start service discovery.");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.d(LOG_TAG, "Disconnected from GATT server.");
                disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            //AlarmContext.logInfo("onServicesDiscovered status " + status);
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                //AlarmContext.logInfo("BluetoothGatt.GATT_SUCCESS");
                BluetoothGattService rxService = mBluetoothGatt.getService(Constants.PEE_SERVICE_UUID);
                if (rxService == null)
                {
                    //AlarmContext.logInfo("Rx service not found!");
                    disconnect();
                    return;
                }

                BluetoothGattCharacteristic txChar = rxService.getCharacteristic(Constants.PEE_READ_CHAR_UUID);
                if (txChar == null)
                {
                    //AlarmContext.logInfo("Tx charateristic not found!");
                    disconnect();
                    return;
                }

                //AppContext.logInfo("setCharacteristicNotification " + bol+" getProperties "+txChar.getProperties());
                for (int i = 0; i < txChar.getDescriptors().size(); i++)
                {
                    BluetoothGattDescriptor descriptor = txChar.getDescriptors().get(i);
                    //AlarmContext.logInfo( "descriptor.getUuid:" + descriptor.getUuid() + " getValue:" + descriptor.getValue());
                    //BluetoothGattCharacteristic bc = descriptor.getCharacteristic();
                    //descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }

                boolean bol = mBluetoothGatt.setCharacteristicNotification(txChar, true);
                //AlarmContext.logInfo("setCharacteristicNotification " + bol + " getProperties " + txChar.getProperties());
                Log.d(LOG_TAG, " connect success");
                mConnectState = BluetoothProfile.STATE_CONNECTED;

                // 蓝牙连接成功
                iBleConn.connectStaus(Constants.PEE_ACTION_DEVICE_CONNECT_SUCCESS);
                jobHandler.sendEmptyMessageDelayed(1000, 1500);
            } else
            {
                //AlarmContext.logInfo( "onServicesDiscovered received: " + status);
                disconnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status)
        {
            //AppContext.logInfo("onCharacteristicRead" + characteristic.getUuid());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            //AppContext.logInfo("onCharacteristicChanged getUuid " + characteristic.getUuid());
            if (Constants.PEE_READ_CHAR_UUID.equals(characteristic.getUuid()))
            {
                try
                {
                    //Log.d(LOG_TAG, "onCharacteristicChanged:");
                    //mWatereverCmdMgr.OnRecvShireData(characteristic.getValue());
                } catch (Exception e)
                {
                    //AlarmContext.logInfo( "处理接收数据失败");
                }
            }
        }
    };

    public void sendDataToBle(String hexString)
    {
        byte[] data = CommandManager.hexStringToBytes(hexString);
        //AppContext.logInfo("after " + hexString);
        try
        {
            writeRXCharacteristic(data);
        } catch (Exception e)
        {
            //AppContext.logInfo("writeRXCharacteristic Exception");
            e.printStackTrace();
        }
    }

    public String sendData(String hexString)
    {
        int count = 0;
        byte[] data = CommandManager.hexStringToBytes(hexString);
        for (int i = 0; i < data.length; i++)
        {
            count += data[i] & 0xff;
        }
        String cc = Integer.toHexString(count);
        int len = cc.length();
        if (len >= 2)
        {
            cc = cc.substring(len - 2, len);
        } else if (len < 2)
        {
            cc = "0" + cc;
        }
        hexString += cc;
        return hexString;
    }

    /**
     * 向设备发送一条命�?/数据 注意水杯有些命令是分多次发�?�的，注意再次封装此方法 处理接收数据的时候也是一样，要注意是否是分多次发回的�?
     *
     * @param value
     */
    public void writeRXCharacteristic(byte[] value) throws Exception
    {
        if (null != mBluetoothGatt)
        {
            BluetoothGattService RxService = mBluetoothGatt.getService(Constants.PEE_SERVICE_UUID);
            // AppContext.logInfo("mBluetoothGatt null"+ mBluetoothGatt);
            if (RxService == null)
            {
                // AppContext.logInfo("Rx service not found!");
                disconnect();
                throw new Exception("DEVICE DISCONNECTED");
            }

            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(Constants.PEE_WRITE_CHAR_UUID);
            if (RxChar == null)
            {
                // AppContext.logInfo("Rx charateristic not found!");
                disconnect();
                throw new Exception("DEVICE DISCONNECTED");
            }

            RxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);// lx
            RxChar.setValue(value);
            boolean status = mBluetoothGatt.writeCharacteristic(RxChar);

            // AppContext.logInfo("write TXchar - status=" + status);
        } else
        {
            // AppContext.logInfo("Send Command fail! Gatt is null.");
            disconnect();
            // throw new Exception("DEVICE DISCONNECTED");
            //AlarmContext.logInfo("DEVICE DISCONNECTED");
        }
    }

    public BleDevice getConnectedDevice()
    {
        return bleDevice;
    }

    public boolean isConnected(String address)
    {
        if (!BleTools.getInstantce().isBleEnabled())
        {
            return false;
        }
        // 蓝牙被禁用了
        if (!initBle())
        {
            return false;
        }

        if (this.mConnectState != BluetoothProfile.STATE_CONNECTED)
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

    /**
     * 断开连接
     */
    public void disconnect()
    {
        try
        {
            jobHandler.removeCallbacksAndMessages(null);
            Log.d(LOG_TAG, "i disconnect");
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
            iBleConn.connectStaus(Constants.PEE_ACTION_DEVICE_CONNECT_FAIL);
            mBluetoothGatt = null;
            //mBluetoothAdapter = null;
            //mBluetoothManager = null;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}

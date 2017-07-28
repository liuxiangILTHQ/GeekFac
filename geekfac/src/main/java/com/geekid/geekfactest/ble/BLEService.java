package com.geekid.geekfactest.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.geecare.blelibrary.Constants;
import com.geecare.blelibrary.model.BleDevice;
import com.geekid.geekfactest.AppContext;
import com.geekid.geekfactest.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;


public class BLEService extends Service
{
    public static final String DEV_ADDR = "DEV_ADDR";
    public static int devType = -1;

    public static final String ACTION_FOUND_DEV = "ACTION_FOUND_DEV";
    public static final String ACTION_FOUND_NO = "ACTION_FOUND_NO";
    public static final String ACTION_CONNECT_FAIL = "ACTION_CONNECT_FAIL";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothGatt mBluetoothGatt;
    private String mDeviceAddress;
    // 标记当前连接状态
    private int mConnectState = BluetoothProfile.STATE_DISCONNECTED;

    private IBinder mBinder;

    /**
     * 当Service被创建时（只会执行一次）
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
        AppContext.logInfo("Service onCreate!");
        mBinder = new LocalBinder();
    }

    /**
     * 当被startService启动时（可被执行多次）
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // return super.onStartCommand(intent, flags, startId);
        AppContext.logInfo("Service onStart!");
//        if (intent.getStringExtra(DEV_ADDR) != null)
//        {
//            mDeviceAddress = intent.getStringExtra(DEV_ADDR);
//            connect(mDeviceAddress);
//        }
        return START_STICKY;
    }

    /**
     * 当被bindService绑定时（也可多次）
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    /**
     * 当被unbindService解除绑定时（也可多次）
     */
    @Override
    public boolean onUnbind(Intent intent)
    {
        // 断开连接、清理资源
        // close();
        return true;
    }

    /**
     * 当被stopService通知停止时 或 被unbindSerivce后（bindService启动的情形）
     */
    @Override
    public void onDestroy()
    {
        AppContext.logInfo("Service onDestroy!");
        // 释放掉占用的资源
        disconnect();
        super.onDestroy();
    }

    public class LocalBinder extends Binder
    {
        public BLEService getService()
        {
            return BLEService.this;
        }
    }

    private boolean initBLE()
    {
        AppContext.logDebug("initBLE");
        if (mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
            {
                AppContext.logError("Unable to initialize BluetoothManager.");
                //AppContext.broadcastUpdate(BLEService.this, ACTION_BLE_NOT_ENABLE);
                return false;
            }
        }
        if (mBluetoothAdapter == null)
        {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
            {
                AppContext.logError("Unable to obtain a BluetoothAdapter.");
                //AppContext.broadcastUpdate(BLEService.this, ACTION_BLE_NOT_ENABLE);
                return false;
            }
        }
        return true;
    }

    public synchronized boolean connect(final String address, boolean scan)
    {
        devType = -1;
        if (address == null || "".equals(address.trim()))
        {
            return false;
        }
        mConnectState = BluetoothProfile.STATE_DISCONNECTED;
        disconnect();
        if (!initBLE())
        {
            return false;
        }

        mDeviceAddress = address;
        if (scan)
        {
            startScan();
        } else
        {
            connect_step2();
        }

        return true;
    }

    List<BleDevice> deviceList;
    private boolean isScan = false;

    private synchronized void startScan()
    {
        if (!initBLE())
        {
            return;
        }
        AppContext.logInfo("startScan");
        isScan = true;
        deviceList = new ArrayList<>();
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                stopScan();
                if (deviceList.size() == 1)
                {
                    connect(deviceList.get(0).getBleMacAddr(), false);
                }
            }
        }, 8000);
    }

    public synchronized void stopScan()
    {
        try
        {
            if (mBluetoothAdapter != null)
            {
                AppContext.logInfo("stopScan");
                isScan = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
        {
            if (mDeviceAddress == null || mDeviceAddress.equals(""))
            {
                final String data = CommandManager.byteToHexString(scanRecord).toUpperCase();
                //if (data.contains(BLEService.S))
                {
                    final BleDevice bleDevice = new BleDevice();
                    bleDevice.setBleName(device.getName());
                    bleDevice.setBleMacAddr(device.getAddress());
                    bleDevice.setRssi(rssi);
                    for (BleDevice bd : deviceList)
                    {
                        if (bleDevice.getBleMacAddr().equals(bd.getBleMacAddr()))
                        {
                            bd.setRssi(rssi);
                            return;
                        }
                    }
                    deviceList.add(bleDevice);
                }
            } else
            {
                if (device != null && device.getAddress().equals(mDeviceAddress))
                {
                    stopScan();
                    connect(mDeviceAddress, false);
                    return;
                }
            }
        }
    };

    /**
     * 连接第二步
     */
    private synchronized void connect_step2()
    {
        if (mBluetoothAdapter == null)
        {
            return;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        if (device == null)
        {
            disconnect();// 20150609
            // 广播连接失败
            //AppContext.broadcastUpdate(BLEService.this, ACTION_DEVICE_CONNECT_FAIL);
            return;
        }
        if (mBluetoothGatt == null)
        {
            AppContext.logInfo("connect_step2()  connectGatt");
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        }
        mConnectState = BluetoothProfile.STATE_CONNECTING;
        //AppContext.broadcastUpdate(BLEService.this, ACTION_DEVICE_CONNECTING);
    }

    /**
     * (3)蓝牙GATT回调类
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            AppContext.logInfo("onConnectionStateChange "+status+","+newState);
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
                } else
                {
                    AppContext.logInfo("SUCCESS start service discovery.");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                AppContext.logInfo("Disconnected from GATT server.");
                disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            AppContext.logInfo("onServicesDiscovered "+status);
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                List<BluetoothGattService> rxServices = mBluetoothGatt.getServices();
                if (rxServices.size() > 0)
                {
                    for (BluetoothGattService rxService : rxServices)
                    {

                        if (rxService.getUuid().equals(BleConstants.PEE_SERVICE_UUID))
                        {
                            //LogUtils.e("嘘嘘扣 -- mDeviceAddress :"+mDeviceAddress);
                            devType = 0;
                            Intent intent=new Intent(BLEService.this,PeeService.class);
                            intent.putExtra(Constants.PEE_ADDR,mDeviceAddress);
                            BLEService.this.startService(intent);
                            SharedPreferencesUtils.putString(BLEService.this,Constants.PEE_ADDR, mDeviceAddress);
                            BleUtils.broadcastUpdate(BLEService.this, ACTION_FOUND_DEV, devType + "");

                            stopSelf();
                            break;
                        }
                    if (rxService.getUuid().equals(BleConstants.TEMP_SERVICE_UUID)||rxService.getUuid().equals(BleConstants.TEMP_SERVICE_UUID_G))
                    {
                        //LogUtils.e("体温计 -- mDeviceAddress :"+mDeviceAddress);
                        devType = 1;
                        Intent intent=new Intent(BLEService.this,TempService.class);
                        intent.putExtra(Constants.TEMP_ADDR,mDeviceAddress);
                        BLEService.this.startService(intent);
                        SharedPreferencesUtils.putString(BLEService.this,Constants.TEMP_ADDR, mDeviceAddress);
                        BleUtils.broadcastUpdate(BLEService.this, ACTION_FOUND_DEV, devType + "");
                        stopSelf();
                        break;
                    }
                    }

                }
                if (devType == -1)
                {
                    BleUtils.broadcastUpdate(BLEService.this, ACTION_FOUND_NO);
                    stopSelf();
                }
                //mConnectState = BluetoothProfile.STATE_CONNECTED;

                // 蓝牙连接成功
                //AppContext.isConnected = true;
                //AppContext.broadcastUpdate(BLEService.this, ACTION_DEVICE_CONNECT_SUCCESS);
            } else
            {
                // AppContext.logInfo("onServicesDiscovered received: " +
                // status);
                mConnectState = BluetoothProfile.STATE_DISCONNECTED;
                disconnect();
                //AppContext.broadcastUpdate(BLEService.this, ACTION_DEVICE_CONNECT_FAIL);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status)
        {

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {

        }
    };


    /**
     * 断开连接
     */
    public void disconnect()
    {
        try
        {

            devType = -1;
            //AppContext.isConnected = false;
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
            //AppContext.broadcastUpdate(BLEService.this, ACTION_FOUND_NO);
            mBluetoothGatt = null;
            mBluetoothAdapter = null;
            mBluetoothManager = null;
            //mDeviceAddress = null;
            // mGattCallback=null;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}

package com.geecare.blelibrary;

import android.app.Activity;
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
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.geecare.blelibrary.callback.IBleComm;
import com.geecare.blelibrary.callback.IBleConn;
import com.geecare.blelibrary.callback.IBleScan;
import com.geecare.blelibrary.callback.IBleState;
import com.geecare.blelibrary.callback.IBleTools;
import com.geecare.blelibrary.callback.IDataResult;
import com.geecare.blelibrary.callback.IDisconnect;
import com.geecare.blelibrary.model.BleDevice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Created by Administrator on 2016/7/18.
 */
public class BleTools implements IBleTools
{
    private String TAG = "blelib";
    private static volatile BleTools bleTools;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private List<BleDevice> deviceList = new ArrayList<BleDevice>();
    private boolean mScanning = false;
    private int mConnectState = BluetoothProfile.STATE_DISCONNECTED;
    private BleDevice mBleDevice;
    private String mCurrentMacAddr;
    private boolean isAutoConnect = false;//是否自动连接

    private boolean isBleEnabled = false;

    private IBleState iBleState;
    private IBleScan iBleScan;
    private IBleConn iBleConn;
    private IBleComm iBleComm;
    private Context context;

    private static ExecutorService executorService = Executors.newFixedThreadPool(3);

    private Handler mHandler;
    private Context mContext;

    @Override
    public void init(Context context)
    {
        Log.d("lx", "init");
        mContext = context;
        mHandler = getHandler();
//        if (executorService == null)
//        {
//            executorService = Executors.newFixedThreadPool(3);
//        }
    }

    private Handler getHandler()
    {
        if (mHandler == null)
        {
            mHandler = new Handler();
        }
        return mHandler;
    }

    @Override
    public void setBleScanCallback(IBleScan iBleScan)
    {
        this.iBleScan = iBleScan;
    }

    @Override
    public void setBleConnCallback(IBleConn iBleConn)
    {
        this.iBleConn = iBleConn;
    }

    @Override
    public void setBleCommCallback(IBleComm iBleComm)
    {
        this.iBleComm = iBleComm;
    }

    @Override
    public void setBleStateCallback(IBleState iBleState)
    {
        this.iBleState = iBleState;
    }

    public void removeBleScanCallback()
    {
        iBleScan = null;
    }

    public void removeBleConnCallback()
    {
        iBleConn = null;
    }

    public void removeBleCommCallback()
    {
        iBleComm = null;
    }

    public void removeBleStateCallback()
    {
        iBleState = null;
    }

    public IBleState getBleStateCallback()
    {
        return myBleState;
    }

    private IBleState myBleState = new IBleState()
    {
        @Override
        public void stateChange(int staus)
        {
            if (iBleState != null)
                iBleState.stateChange(staus);
            isBleEnabled = (staus == Constants.ACTION_BLE_ENABLE);
            Log.d(TAG, "ble isEnabled " + isBleEnabled);
            if (isBleEnabled)
            {
                startConnectTask();
            }
        }
    };

    private BleTools()
    {
    }

    public boolean getScanState()
    {
        return mScanning;
    }

    public static BleTools getInstantce()
    {
        if (bleTools == null)
        {
            synchronized (BleTools.class)
            {
                if (bleTools == null)
                {
                    bleTools = new BleTools();
                }
            }
        }
        return bleTools;
    }

    private BluetoothAdapter getBluetoothAdapter()
    {
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
        {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        return bluetoothAdapter;
    }

    public boolean isBleSupport()
    {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            return false;
        }
        if (null == mBluetoothAdapter)
        {
            mBluetoothAdapter = getBluetoothAdapter();
        }
        if (mBluetoothAdapter == null)
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean isBleEnabled()
    {
        if (null == mBluetoothAdapter)
        {
            mBluetoothAdapter = getBluetoothAdapter();
        }
        if (mBluetoothAdapter == null)
        {
            return false;
        } else
        {
            isBleEnabled = mBluetoothAdapter.isEnabled();
            return isBleEnabled;
        }
    }


    @Override
    public void openBle(Activity context)
    {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableIntent, Constants.ACTION_REQUEST_ENABLE);
        }
    }


    /**
     * 开始搜索设备
     */
    @Override
    public synchronized boolean startScan(String flag)
    {
        if (mScanning)
        {
            return false;
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        //if (!isBleEnabled)
        {
            Log.d(TAG, "mBluetoothAdapter not");
            mBluetoothGatt = null;
            return false;
        }
        executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(TAG, "startScan");

                mScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                if (iBleScan != null)
                {
                    getHandler().post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            iBleScan.getScanStatus(true);
                        }
                    });

                }

                getHandler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        stopScan();
                    }
                }, 6000);
//                new Timer().schedule(new TimerTask()
//                {
//                    @Override
//                    public void run()
//                    {
//                        stopScan();
//                    }
//                }, 6000);
            }
        });

        return true;
    }

    static class scanTask implements Runnable
    {
        @Override
        public void run()
        {

        }
    }


    /**
     * 停止搜索
     */
    @Override
    public synchronized boolean stopScan()
    {
        if (!mScanning)
        {
            return false;
        }

        if (!isBleEnabled)
        {
            //Log.d(TAG, "ble close");
            mBluetoothGatt = null;
            //return false;
        }
        Log.d(TAG, "stopScan");
        mScanning = false;
        if (iBleScan != null)
        {
            getHandler().post(new Runnable()
            {
                @Override
                public void run()
                {
                    iBleScan.getScanStatus(false);
                }
            });

        }
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        return true;
    }

    public synchronized boolean scanAndConnect(final String mAdd)
    {
        isAutoConnect = true;
        Log.d(TAG, "isAutoConnect " + isAutoConnect);
        mCurrentMacAddr = mAdd;
        startScan("");
        return true;
    }

    private boolean isTaskRunning = false;
    final Runnable task = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(TAG, "task loop");
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
            {
                if (mConnectState == BluetoothProfile.STATE_CONNECTING)
                {
                    getHandler().postDelayed(task, 12000);
                    return;
                }
                if (isConnected())
                {
                    isTaskRunning = false;
                    Log.d(TAG, "task remove");
                    getHandler().removeCallbacksAndMessages(null);
                } else
                {
                    startScan("");
                    getHandler().postDelayed(task, 12000);
                }
            }
        }
    };

    private void startConnectTask()
    {
        Log.d(TAG, "startConnectTask");
        if (isTaskRunning)
        {
            return;
        }
        if (context != null && isAutoConnect)
        {
            isTaskRunning = true;
            Log.d(TAG, "startConnectTask post");
            getHandler().post(task);
        }
    }


    /**
     * 注册某个需要保持蓝牙连接的界面，若界面不需要连接了，则反注册unregister
     */
    public void register(Context context)
    {
        this.context = context;
    }

    public void unregister()
    {
        context = null;
    }

    /***
     * 设置蓝牙是否需要重连
     * @param isAutoConnect
     */
    public void setAutoConnect(boolean isAutoConnect)
    {
        this.isAutoConnect = isAutoConnect;
    }


    /**
     * 释放资源，否则会造成内存泄漏
     */
    public void release()
    {
        deviceList.clear();
        mHandler.removeCallbacksAndMessages(null);
        iBleState = null;
        iBleScan = null;
        iBleConn = null;
        iBleComm = null;
        //executorService.shutdown();
        //executorService = null;
        //bleTools = null;
    }

    public void refresh()
    {
        try
        {

            Method localMethod = mBluetoothGatt.getClass().getMethod("refresh");
            if (localMethod != null)
            {
                localMethod.invoke(mBluetoothGatt);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void closeDevice()
    {
        if (mBluetoothGatt != null)
        {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    private void realyDisconnected()
    {
        mConnectState = BluetoothProfile.STATE_DISCONNECTED;
        if (iBleConn != null)
            iBleConn.connectStaus(Constants.PEE_ACTION_DEVICE_CONNECT_FAIL);

        closeDevice();
        Log.d(TAG, "disconnect,need auto connect ? " + isAutoConnect);
        if (iDisconnect != null)
            iDisconnect.disconnect(true);

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
        {
            startConnectTask();
        }
    }

    IDisconnect iDisconnect;

    /**
     * 断开蓝牙连接
     */
    @Override
    public synchronized boolean disconnect(IDisconnect idisconnect)
    {
        this.iDisconnect = idisconnect;
        if (mConnectState == BluetoothProfile.STATE_DISCONNECTED)
        {
            if (iDisconnect != null)
                iDisconnect.disconnect(true);
            return false;
        }

        if (mConnectState == BluetoothProfile.STATE_CONNECTING)
        {
            return false;
        }
        if (null != mBluetoothGatt)
        {
            Log.d(TAG, "i disconnect");
            mBluetoothGatt.disconnect();
            //mBluetoothGatt.close();
        }
        return true;
    }


    Future future;

    @Override
    public synchronized boolean connect(final String mAddr)
    {
        if (mAddr == null || mAddr.equals(""))
        {
            return false;
        }
        mCurrentMacAddr = mAddr;
        Log.d(TAG, "to connect " + mAddr);
        if (mScanning)
        {
            stopScan();
        }
        //if (future != null)
        //    Log.d(TAG, "last connect " + future.isDone());
        isAutoConnect = true;
        //mConnectState=BluetoothProfile.STATE_CONNECTING;
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Log.d(TAG, "ble not enabled");
            //startConnectTask();
            return false;
        }
        future = executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(TAG, "submit connect");
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mCurrentMacAddr);
                if (device == null)
                {
                    Log.d(TAG, "RemoteDevice not found.");
                    //disconnect();// 20150609
                    return;
                }
                if (mBluetoothGatt == null)
                {
                    Log.d(TAG, " new connect");
                    mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
                    Log.d(TAG, device.getName() + " connect_step2()  connectGatt");
                } else
                {
                    Log.d(TAG, " reconnect");
                    mBluetoothGatt.connect();
                }
                mConnectState = BluetoothProfile.STATE_CONNECTING;
                iBleConn.connectStaus(Constants.PEE_ACTION_DEVICE_CONNECTING);
            }
        });
        //Log.d(TAG, "now connect " + future.isDone());
        try
        {
            future.get();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        //Log.d(TAG, "connect true");
        return true;
    }

    IDataResult iDataResult;

    public void readRssi(IDataResult iDataResult)
    {
        this.iDataResult = iDataResult;
        if (mBluetoothGatt != null)
            mBluetoothGatt.readRemoteRssi();
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (iDataResult != null)
            {
                iDataResult.getResult(rssi + "");
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            Log.d(TAG, "onConnectionStateChange " + status + " " + newState);
            //STATE_CONNECTING    = 1
            //STATE_DISCONNECTING =3
            // 建立连接
            if (newState == BluetoothProfile.STATE_CONNECTED)//2
            {
                // 成功建立连接后，启动服务发现
                boolean result = mBluetoothGatt.discoverServices();
                if (!result)
                {
                    Log.d(TAG, "FAIL to start service discovery");
                    //disconnect();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)//0
            {
                Log.d(TAG, "Disconnected from GATT server.");
                realyDisconnected();
                //disconnect();
            }
            if (status == BluetoothGatt.GATT_SUCCESS)
            {

            } else
            {
                Log.d(TAG, "Disconnected from GATT server.1111");
                realyDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            //AlarmContext.logInfo("onServicesDiscovered status " + status);
            if (status == BluetoothGatt.GATT_SUCCESS)//0
            {
                //AlarmContext.logInfo("BluetoothGatt.GATT_SUCCESS");
                BluetoothGattService rxService = mBluetoothGatt.getService(Constants.PEE_SERVICE_UUID);
                if (rxService == null)
                {
                    Log.d(TAG, "Rx service not found!");
                    //disconnect();
                    return;
                }
                BluetoothGattCharacteristic txChar = rxService.getCharacteristic(Constants.PEE_READ_CHAR_UUID);
                if (txChar == null)
                {
                    Log.d(TAG, "Tx charateristic not found!");
                    //disconnect();
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

                mBluetoothGatt.setCharacteristicNotification(txChar, true);
                //AlarmContext.logInfo("setCharacteristicNotification " + bol + " getProperties " + txChar.getProperties());
                Log.d(TAG, " connect success");
                mConnectState = BluetoothProfile.STATE_CONNECTED;

                // 蓝牙连接成功
                iBleConn.connectStaus(Constants.PEE_ACTION_DEVICE_CONNECT_SUCCESS);
            } else
            {
                Log.d(TAG, "onServicesDiscovered received: " + status);
                //disconnect();
            }
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
                    if (iBleComm != null)
                        iBleComm.OnRecvData(characteristic.getValue());
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    };

    public boolean writeRXCharacteristic(byte[] value) throws Exception
    {
        if (null != mBluetoothGatt)
        {
            BluetoothGattService RxService = mBluetoothGatt.getService(Constants.PEE_SERVICE_UUID);
            // AppContext.logInfo("mBluetoothGatt null"+ mBluetoothGatt);
            if (RxService == null)
            {
                // AppContext.logInfo("Rx service not found!");
                //disconnect();
                throw new Exception("DEVICE DISCONNECTED");
            }

            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(Constants.PEE_WRITE_CHAR_UUID);
            if (RxChar == null)
            {
                // AppContext.logInfo("Rx charateristic not found!");
                //disconnect();
                throw new Exception("DEVICE DISCONNECTED");
            }

            RxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);// lx
            RxChar.setValue(value);
            return mBluetoothGatt.writeCharacteristic(RxChar);
            // AppContext.logInfo("write TXchar - status=" + status);
        } else
        {
            // AppContext.logInfo("Send Command fail! Gatt is null.");
            //disconnect();
            throw new Exception("DEVICE DISCONNECTED");
        }
        //return false;
    }

    public boolean sendDataToBle(String hex)
    {
        byte[] data = CommandManager.hexStringToBytes(hex);
        try
        {
            writeRXCharacteristic(data);
            return true;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取当前的设备
     *
     * @return
     */
    @Override
    public BleDevice getCurrentBleDevice()
    {
        return mBleDevice;
    }

    /**
     * 蓝牙设备是否连接着
     *
     * @return
     */
    public boolean isConnected()
    {
        return mConnectState == BluetoothProfile.STATE_CONNECTED;
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
        {
            final String data = CommandManager.byteToHexString(scanRecord).toLowerCase();
            //Log.d("lx",device.getName()+","+data);
            if (data.contains(Constants.PEE_SERVICE_UUID_REVERSE)
                    || data.contains(Constants.TEMP_SERVICE_UUID_REVERSE)
                    || data.contains(Constants.IFOTEMP_SERVICE_UUID_REVERSE)
                    || data.contains(Constants.JONETEMP_SERVICE_UUID_REVERSE)
                    || data.contains(Constants.BOTTLE_SERVICE_UUID_REVERSE))
            {
                final BleDevice bleDevice = new BleDevice();
                bleDevice.setBleName(device.getName());
                bleDevice.setBleMacAddr(device.getAddress());
                bleDevice.setRssi(rssi);
                bleDevice.setProductType("");
                if (data.contains(Constants.PEE_SERVICE_UUID_REVERSE))
                {
                    bleDevice.setProductType("嘘嘘扣");
                } else if (data.contains(Constants.TEMP_SERVICE_UUID_REVERSE))
                {
                    bleDevice.setProductType("Geecare体温计");
                } else if (data.contains(Constants.IFOTEMP_SERVICE_UUID_REVERSE))
                {
                    bleDevice.setProductType("IFO体温计");
                } else if (data.contains(Constants.JONETEMP_SERVICE_UUID_REVERSE))
                {
                    bleDevice.setProductType("J-ONE体温计");
                } else if (data.contains(Constants.BOTTLE_SERVICE_UUID_REVERSE))
                {
                    bleDevice.setProductType("奶瓶宝");
                }

                //Log.d(TAG, "addr:" + device.getAddress() + " rssi:" + rssi);
                if (isAutoConnect && mCurrentMacAddr != null)
                {
                    if (mCurrentMacAddr.equals(device.getAddress()))
                    {
                        stopScan();
                        Log.d(TAG, "found " + mCurrentMacAddr);
                        mBleDevice = bleDevice;
                        connect(mCurrentMacAddr);
                        return;
                    }
                }
//                for (BleDevice bd : deviceList)
//                {
//                    //已经搜索到的设备，直接更新其信号值
//                    if (bleDevice.getBleMacAddr().equals(bd.getBleMacAddr()))
//                    {
//                        bd.setRssi(rssi);
//                        if (iBleScan != null && deviceList.size() > 0)
//                        {
//                            getHandler().post(new Runnable()
//                            {
//                                @Override
//                                public void run()
//                                {
//                                    iBleScan.getScanBleDevices(bleDevice);
//                                }
//                            });
//                        }
//                        return;
//                    }
//                }
                for(int i=0;i<deviceList.size();i++)
                {
                    final BleDevice bd=deviceList.get(i);
                    if (bleDevice.getBleMacAddr().equals(bd.getBleMacAddr()))
                    {
                        bd.setRssi(rssi);
                        final int pos=i;
                        if (iBleScan != null)
                        {
                            getHandler().post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    iBleScan.updateBleDevice(pos,rssi);
                                }
                            });
                        }
                        return;
                    }

                }

                synchronized (this)
                {
                    deviceList.add(bleDevice);
                    final BleDevice bd = deviceList.get(deviceList.size() - 1);
                    //
                    //Log.d(TAG, "addr:" + b.getBleMacAddr() + " rssi:" + b.getRssi());
                    if (iBleScan != null && deviceList.size() > 0)
                    {
                        getHandler().post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                iBleScan.getScanBleDevices(bd);
                            }
                        });
                    }
                }

            }

        }
    };

}

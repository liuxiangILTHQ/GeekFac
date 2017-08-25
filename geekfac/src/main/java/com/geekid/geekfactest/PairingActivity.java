package com.geekid.geekfactest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geecare.blelibrary.BleTools;
import com.geecare.blelibrary.Constants;
import com.geecare.blelibrary.callback.IBleScan;
import com.geecare.blelibrary.callback.IBleState;
import com.geecare.blelibrary.model.BleDevice;
import com.geekid.geekfactest.ble.BleUtils;
import com.geekid.geekfactest.ble.BottleService;
import com.geekid.geekfactest.ble.TempService;
import com.geekid.geekfactest.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

public class PairingActivity extends Activity
{
    public static final String TAG = "DeviceListActivity";
    List<BleDevice> deviceList;
    private DeviceAdapter deviceAdapter;

    private ListView mDeviceList;
    private TextView mScanTips, ver_tv;
    private Button mBleScanButton;
    private ProgressBar mScanLoading;

    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onResume()
    {
        super.onResume();
        // 询问是否打开蓝牙
//        if (!BleUtils.isBluttoothEnable())
//        {
//            BleUtils.enableBluetooth(PairingActivity.this);
//        } else
//        {
//            checkAndroidVerAndScan();
//        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pairing);

        //Intent intent = new Intent("com.geekid.other.other");
        //startActivity(intent);
        BleTools.getInstantce().init(getApplicationContext());
        if (!BleTools.getInstantce().isBleSupport())
        {
            Toast.makeText(PairingActivity.this, "此设备不支持蓝牙ble",
                    Toast.LENGTH_LONG).show();
            return;
        }

        mScanLoading = (ProgressBar) findViewById(R.id.pairing_scan_loading);
        mScanTips = (TextView) findViewById(R.id.pairing_tips);
        mDeviceList = (ListView) findViewById(R.id.pairing_device_list);
        mBleScanButton = (Button) findViewById(R.id.waterever_pairing_ble_op_btn);

        mDeviceList.setOnItemClickListener(mDeviceClickListener);
        mBleScanButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!BleTools.getInstantce().getScanState())
                {
                    BleTools.getInstantce().startScan("");
                } else
                {
                    BleTools.getInstantce().stopScan();
                }
            }
        });
        deviceList = new ArrayList<BleDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        mDeviceList.setAdapter(deviceAdapter);

        BleTools.getInstantce().setBleStateCallback(new IBleState()
        {
            @Override
            public void stateChange(int staus)
            {
                if (staus == Constants.ACTION_BLE_ENABLE)
                {
                    Toast.makeText(PairingActivity.this, "蓝牙已启用", Toast.LENGTH_SHORT).show();
                    //checkAndroidVerAndScan();
                } else if (staus == Constants.ACTION_BLE_NOT_ENABLE)
                {
                    Toast.makeText(PairingActivity.this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                }

            }
        });
        BleTools.getInstantce().setBleScanCallback(new IBleScan()
        {
            @Override
            public void getScanBleDevices(BleDevice bleDevice)
            {
                deviceList.add(bleDevice);
                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void updateBleDevice(int pos, int rssi)
            {
                deviceList.get(pos).setRssi(rssi);
                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void getScanStatus(final boolean isScanning)
            {
                if (isScanning)
                {
                    mBleScanButton.setText("取消扫描");
                    mScanTips.setText("正在扫描...");
                    mScanLoading.setVisibility(View.VISIBLE);
                } else
                {
                    if (deviceList.size() < 1)
                    {
                        mScanTips.setText("未能寻找到蓝牙设备");
                    } else
                    {
                        mScanTips.setText("请从上面列表中选择您的设备");
                    }
                    mScanLoading.setVisibility(View.GONE);
                    mBleScanButton.setText("启动扫描");
                }
            }

        });

        if (!BleTools.getInstantce().isBleEnabled())
        {
            mScanTips.setText("请打开蓝牙");
            BleTools.getInstantce().openBle(this);
        } else
        {
            checkAndroidVerAndScan();
        }

    }

    private boolean checkP(final String pe, final int re, final String msg)
    {
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, pe);
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED)
        {
            //判断是否需要 向用户解释，为什么要申请该权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, pe))
            {
                mScanTips.setText(msg + ",请点击允许");
                new AlertDialog.Builder(PairingActivity.this)
                        .setMessage(msg + ",请点击允许")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(PairingActivity.this, new String[]{pe}, re);
                            }
                        })
                        .create().show();
            } else
            {
                //此时用户已点击拒绝授权，并且不再询问
                mScanTips.setText(msg + "，请到应用权限设置中点击允许");
                gotoSetting(msg + "，请到应用权限设置中点击允许");
            }
            return false;
        }
        return true;
    }

    private static final int REQUEST_FINE_LOCATION = 0;
    private static final int REQUEST_FINE_LOCATION1 = 1;
    String a = Manifest.permission.ACCESS_COARSE_LOCATION;
    String a1 = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private void checkAndroidVerAndScan()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkP(a, REQUEST_FINE_LOCATION, "蓝牙需要位置定位权限") &&
                    checkP(a1, REQUEST_FINE_LOCATION1, "蓝牙需要存储权限"))
            {
                //Toast.makeText(this, "蓝牙已获取定位权限", Toast.LENGTH_SHORT).show();
                BleTools.getInstantce().startScan("");
            }
        } else
        {
            BleTools.getInstantce().startScan("");
        }
    }

    public void gotoSetting(String msg)
    {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                //builder.setTitle("提示");
                .setPositiveButton("设置", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        try
                        {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent);
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        //unregisterReceiver(mReceiver);
        BleTools.getInstantce().stopScan();
        BleTools.getInstantce().release();
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    BleTools.getInstantce().startScan("");
                } else
                {
                    mScanTips.setText("已拒绝授权，请到应用权限设置中点击允许");
                }
                break;
            case REQUEST_FINE_LOCATION1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //scanLeDevice(true);
                } else
                {
                    mScanTips.setText("已拒绝授权，请到应用权限设置中点击允许");
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case BleUtils.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK)
                {
                    //Toast.makeText(this, "已启用蓝牙", Toast.LENGTH_SHORT).show();
                    //checkAndroidVerAndScan();
                }
        }
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id
        )
        {
            BleTools.getInstantce().stopScan();

            final BleDevice mDevice = deviceList.get(position);
            String type = mDevice.getProductType();
            String mDeviceAddress = mDevice.getBleMacAddr();

            if (type.equals("嘘嘘扣"))
            {
                //Intent intent = new Intent(PairingActivity.this, PeeService.class);
                //intent.putExtra(Constants.PEE_ADDR, mDeviceAddress);
                //PairingActivity.this.startService(intent);
                SharedPreferencesUtils.putString(PairingActivity.this, Constants.PEE_ADDR, mDeviceAddress);
                Intent intent1 = new Intent(PairingActivity.this, XuxukouActivity.class);
                intent1.putExtra(Constants.DEVICE, mDevice);
                PairingActivity.this.startActivity(intent1);


            } else if (type.contains("体温计"))
            {
                Intent intent = new Intent(PairingActivity.this, TempService.class);
                intent.putExtra(Constants.TEMP_ADDR, mDeviceAddress);
                PairingActivity.this.startService(intent);
                SharedPreferencesUtils.putString(PairingActivity.this, Constants.TEMP_ADDR, mDeviceAddress);
                Intent intent1 = new Intent(PairingActivity.this, TempActivity.class);
                intent1.putExtra(Constants.DEVICE, mDevice);
                PairingActivity.this.startActivity(intent1);
            } else if (type.equals("奶瓶宝"))
            {
                Intent intent = new Intent(PairingActivity.this, BottleService.class);
                intent.putExtra(Constants.BOTTLE_ADDR, mDeviceAddress);
                PairingActivity.this.startService(intent);
                SharedPreferencesUtils.putString(PairingActivity.this, Constants.BOTTLE_ADDR, mDeviceAddress);
                Intent intent1 = new Intent(PairingActivity.this, FeederActivity.class);
                intent1.putExtra(Constants.DEVICE, mDevice);
                PairingActivity.this.startActivity(intent1);
            }

        }
    };

    class DeviceAdapter extends BaseAdapter
    {
        Context context;
        List<BleDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BleDevice> devices)
        {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount()
        {
            return devices.size();
        }

        @Override
        public Object getItem(int position)
        {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View vg = inflater.inflate(
                    R.layout.activity_waterever_pairing_device_item, null);
            BleDevice device = devices.get(position);
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));

            tvname.setText(device.getProductType() + "-" + device.getBleName() + "  "
                    + device.getBleMacAddr() + "  " + device.getRssi());
            return vg;
        }
    }

}

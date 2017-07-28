package com.geekid.geekfactest.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;

import com.geekid.geekfactest.model.DataInfo;


public class BleUtils
{
	public static final int REQUEST_ENABLE_BT = 2;


	public static boolean isSupportBLE(Context ctx)
	{
		if (!ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
		{
			return false;
		}
		return true;
	}

	public static boolean isBluttoothEnable()
	{
		if (null == BluetoothAdapter.getDefaultAdapter())
		{
			return false;
		}
		return BluetoothAdapter.getDefaultAdapter().isEnabled();
	}


	public static void enableBluetooth(Activity ctx)
	{
		if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
		{
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			ctx.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
	}

    public static String getBlueMac(Context context){

        //AdvertisingIdClient;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if(mBluetoothAdapter==null){
            return "";
        }
        return mBluetoothAdapter.getAddress();


    }


    public static void broadcastUpdate(Context context, final String action, String data) {
        final Intent intent = new Intent(action);
        intent.putExtra("content", data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void broadcastUpdate(Context context, final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }



    public static void broadcastUpdate(Context context, final String action, DataInfo dataInfo)
    {
        final Intent intent = new Intent(action);
        intent.putExtra(BleConstants.EXTRA_DATA, dataInfo);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}

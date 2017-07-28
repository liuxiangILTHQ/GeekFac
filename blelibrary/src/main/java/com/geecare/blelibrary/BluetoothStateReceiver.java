package com.geecare.blelibrary;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.geecare.blelibrary.callback.IBleState;

/**
 * Created by Administrator on 2017/6/29.
 */

public class BluetoothStateReceiver extends BroadcastReceiver
{
    public static final String TAG = "BlueToothStatusReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive---------"+intent.getAction());
        switch(intent.getAction()){
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                IBleState iBleState=BleTools.getInstantce().getBleStateCallback();
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                Log.d(TAG, "onReceive---------"+blueState);
                switch(blueState){
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive---------STATE_TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive---------STATE_ON");
                        if(iBleState!=null)
                            iBleState.stateChange(Constants.ACTION_BLE_ENABLE);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive---------STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"onReceive---------STATE_OFF");
                        if(iBleState!=null)
                            iBleState.stateChange(Constants.ACTION_BLE_NOT_ENABLE);
                        break;

                }
                break;
        }
    }
}

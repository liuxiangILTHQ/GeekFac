package com.geekid.geekfactest;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

import com.geekid.geekfactest.ble.BleConstants;
import com.geekid.geekfactest.ble.PeeService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 应用上下文
 * 
 */
@SuppressLint("SimpleDateFormat")
public class AppContext {
	
	public static final int[] musicId = {  R.raw.beep };

	public static final String APP_NAME = "iPinto";

	public static final String LOG_TAG = "GeekFac";


	public static String version = "";
	public static boolean isConnected = true;

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final SimpleDateFormat DATE_FORMAT_DAY = new SimpleDateFormat("yyyy-MM-dd");

	public static final SimpleDateFormat DATE_FORMAT_INT = new SimpleDateFormat("yyyyMMddHHmmss");

	public static final SimpleDateFormat DATE_FORMAT_DAY_INT = new SimpleDateFormat("yyyyMMdd");

	public static final SimpleDateFormat DATE_FORMAT_TIME_12 = new SimpleDateFormat("hh:mm");
	public static final SimpleDateFormat DATE_FORMAT_TIME_24 = new SimpleDateFormat("HH:mm:ss");

	public static final SimpleDateFormat DATE_FORMAT_MONTH_DAY = new SimpleDateFormat("MM-dd");
	
	public static final SimpleDateFormat DATE_FORMAT_YEAR_MONTH = new SimpleDateFormat("yyyy-MM");
	
	public static final SimpleDateFormat DATE_FORMAT_HOUR_MIN = new SimpleDateFormat("HH:mm");

	// 配置名称
	public static final String SHARED_PREFERENCES_NAME = "iPinto_config.pref";


	public static void logDebug(String msg)
	{
		Log.d(LOG_TAG, msg);
	}


	public static void logInfo(String msg)
	{
		Log.i(LOG_TAG, msg);
	}

	public static void logInfo(String msg, Throwable e)
	{
		Log.i(LOG_TAG, msg, e);
	}


	public static void logWarn(String msg, Throwable e)
	{
		Log.w(LOG_TAG, msg, e);
	}

	public static void logError(String msg)
	{
		Log.e(LOG_TAG, msg);
	}


	public static void saveSnInfo(Context context, String proType, String customType,String time,String from,String to)
	{
		if (null == context)
		{
			return;
		}
		SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		editor.putString("proType", proType);
		editor.putString("customType", customType);
		editor.putString("time", time);
		editor.putString("from", from);
		editor.putString("to", to);

		editor.commit();
	}
	
	public static void setSharedPreferencesStringKey(Context context, String key,String value) {
		if (null == context)
		{
			return;
		}
		SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static String getSharedPreferencesStringKey(Context context, String key) {
		try {
			return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(key, "");
		} catch (Exception e) {
			logWarn("读取配置信息出错！", e);
		}
		return "";
	}



	public static IntentFilter getPeeServiceIntentFilter()
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BleConstants.ACTION_BLE_NOT_ENABLE);
        intentFilter.addAction(BleConstants.PEE_ACTION_DATA_UPDATED);
        intentFilter.addAction(BleConstants.PEE_ACTION_DEVICE_CONNECTING);
        intentFilter.addAction(BleConstants.PEE_ACTION_DEVICE_CONNECT_FAIL);
        intentFilter.addAction(BleConstants.PEE_ACTION_DEVICE_CONNECT_SUCCESS);

        intentFilter.addAction(BleConstants.PEE_ACTION_DATA_COMING);
        intentFilter.addAction(BleConstants.PEE_ACTION_DEVICE_SOC_UPDATED);
        intentFilter.addAction("rssi_coming");

        intentFilter.addAction("shire_bind");
        intentFilter.addAction("shire_unbind");
        intentFilter.addAction("shire_sync_time");
        intentFilter.addAction("shire_get_time");
        intentFilter.addAction("shire_upload");
		return intentFilter;
	}

    public static IntentFilter getTempServiceIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConstants.ACTION_BLE_NOT_ENABLE);
        intentFilter.addAction(BleConstants.TEMP_ACTION_DATA_UPDATED);
        intentFilter.addAction(BleConstants.TEMP_ACTION_DEVICE_CONNECTING);
        intentFilter.addAction(BleConstants.TEMP_ACTION_DEVICE_CONNECT_FAIL);
        intentFilter.addAction(BleConstants.TEMP_ACTION_DEVICE_CONNECT_SUCCESS);

        intentFilter.addAction(BleConstants.TEMP_ACTION_DATA_COMING);
        intentFilter.addAction(BleConstants.TEMP_ACTION_DEVICE_SOC_UPDATED);
        intentFilter.addAction("rssi_coming");

        return intentFilter;
    }

    public static IntentFilter getBootleServiceIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConstants.ACTION_BLE_NOT_ENABLE);
        intentFilter.addAction(BleConstants.BOTTLE_ACTION_DATA_UPDATED);
        intentFilter.addAction(BleConstants.BOTTLE_ACTION_DEVICE_CONNECTING);
        intentFilter.addAction(BleConstants.BOTTLE_ACTION_DEVICE_CONNECT_SUCCESS);
        intentFilter.addAction(BleConstants.BOTTLE_ACTION_DEVICE_CONNECT_FAIL);

        intentFilter.addAction(BleConstants.BOTTLE_ACTION_DEVICE_SOC_UPDATED);
        intentFilter.addAction(BleConstants.BOTTLE_ACTION_DATA_COMING);

        intentFilter.addAction(BleConstants.ACTION_WARM_STATUS);
        intentFilter.addAction(BleConstants.ACTION_WARM_SWITCH);
        intentFilter.addAction(BleConstants.ACTION_TEMP_F_COMING);
        intentFilter.addAction("rssi_coming");

        return intentFilter;
    }

	public static String getAppExternalStorageDir(String deviceName)
	{
		String path = Environment.getExternalStorageDirectory() + "/" + deviceName + ".txt";
		File f = new File(path);
		if (!f.exists())
		{
			try
			{
				f.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		} else
		{
			// f.delete();
		}
		return path;
	}

	public static String getDateStr(long time)
	{
		String sDateTime = null;
		Date dt = new Date();
		dt.setTime(time);
		sDateTime = AppContext.DATE_FORMAT.format(dt);
		return sDateTime;
	}
	
	public static String getDateStr(SimpleDateFormat sdf,long time)
	{
		String sDateTime = null;
		Date dt = new Date();
		dt.setTime(time);
		sDateTime = sdf.format(dt);
		return sDateTime;
	}

	// 获取当前月
	public static int getCurrentMonth()
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.MONTH);
	}

	// 0代表1月
	public static int getDaysByMonth(int month)
	{
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int[] monthDay = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
			monthDay[1]++;
		return monthDay[month];
	}

	public static long getDateLong_start(long time)
	{
		String sDateTime = null;
		Date dt = new Date();
		dt.setTime(time);
		sDateTime = AppContext.DATE_FORMAT_DAY.format(dt) + " 00:00:00";
		// AppContext.logInfo(sDateTime+" "+getDate(sDateTime).getTime());
		return getDate(sDateTime).getTime();
	}

	public static long getDateLong_end(long time)
	{
		String sDateTime = null;
		Date dt = new Date();
		dt.setTime(time);
		sDateTime = AppContext.DATE_FORMAT_DAY.format(dt) + " 23:59:59";
		// AppContext.logInfo(sDateTime+" "+getDate(sDateTime).getTime());
		return getDate(sDateTime).getTime();
	}

	public static long getDateLong(int year, int month, int day, int hour, int min, int sec)
	{
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);//
		cal.set(Calendar.DAY_OF_MONTH, day);//

		cal.set(Calendar.HOUR_OF_DAY, hour);
		
		cal.set(Calendar.MINUTE, min);//
		cal.set(Calendar.SECOND, sec);

		return cal.getTimeInMillis();

	}

	public static Date getDateByLong(long time)
	{
		Date date = new Date();
		date.setTime(time);

		return date;
	}

	public static Date getDate(String time)
	{

		Date date = null;
		try
		{
			date = AppContext.DATE_FORMAT.parse(time);
		} catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public static boolean isBLEServiceRunning(Context context)
	{
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if (PeeService.class.getName().equals(service.service.getClassName()))
			{
				return true;
			}
		}
		return false;
	}
	
	public static String getString(String s,int len){
		String ss=s;
		int n=s.length();
		if(n<len){
			for(int i=0;i<len-n;i++){
				ss="0"+ss;
			}
		}
		return ss;
	}
    /**
     * 从assets目录中复制整个文件夹内容
     *
     * @param context Context 使用CopyFiles类的Activity
     * @param oldPath String 原文件路径 如：/aa
     * @param newPath String 复制后路径 如：xx:/bb/cc
     */
    public static void copyFilesFassets(Context context, String oldPath, String newPath)
    {
        try
        {
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0)
            {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames)
                {
                    copyFilesFassets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else
            {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1)
                {// 循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void showDialog(Context context) {
        new AlertDialog.Builder(context)
        .setMessage("确认退出吗？")
        //builder.setTitle("提示");
        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
        .create().show();
    }

    public static void showTip(Context context,String msg) {
        new AlertDialog.Builder(context).setMessage(msg).create().show();
    }

}

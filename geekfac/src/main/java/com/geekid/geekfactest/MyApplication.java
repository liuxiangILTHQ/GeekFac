package com.geekid.geekfactest;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.geecare.blelibrary.BleTools;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/6/7.
 */

public class MyApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        AppContext.logInfo("BleTools init");
        BleTools.getInstantce().init(getApplicationContext());

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        //DisplayMetrics dm =new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;// 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
        int densityDPI = dm.densityDpi;// 屏幕密度（每寸像素：120/160/240/320）
        int screenWidthDip = dm.widthPixels;// 屏幕像素（dip，如：320dip）
        int screenHeightDip = dm.heightPixels;// 屏幕高像素（dip，如：533dip）
        float x=dm.xdpi;//x轴上每寸的实际像素
        float y=dm.ydpi;
        //dm.scaledDensity
        Log.d("lx","p:"+ density+" "+densityDPI+" "+screenWidthDip+" "+screenHeightDip+" "+x+" "+y);

        double dd=Math.sqrt(Math.pow(screenWidthDip/x,2)+Math.pow(screenHeightDip/y,2));
        Log.d("lx","p:"+ dd);

    }
    
    /**
     * Double类型保留指定位数的小数，返回double类型（四舍五入）
     * newScale 为指定的位数
     */
    private static double formatDouble(double d,int newScale) {
        BigDecimal bd = new BigDecimal(d);
        return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}

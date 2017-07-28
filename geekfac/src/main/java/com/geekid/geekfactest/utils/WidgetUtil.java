package com.geekid.geekfactest.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class WidgetUtil {
	public static int Dp2Px(Context context, float dp)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	public static int Px2Dp(Context context, float px)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	/**
	 * * 将px值转换为sp值，保证文字大小不变 * * @param pxValue * @param fontScale *
	 * （DisplayMetrics类中属性scaledDensity） * @return
	 */
	public static int px2sp(Context context, float pxValue)
	{
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * * 将sp值转换为px值，保证文字大小不变 * * @param spValue * @param fontScale *
	 * （DisplayMetrics类中属性scaledDensity） * @return
	 */
	public static int sp2px(Context context, float spValue)
	{
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	public static float px2dp_f(Context context, float px)
	{
		final float density = context.getResources().getDisplayMetrics().density;

		String test = "density:" + context.getResources().getDisplayMetrics().density + "|";
		test += "scaledDensity:" + context.getResources().getDisplayMetrics().scaledDensity + "|";
		test += "widthPixels:" + context.getResources().getDisplayMetrics().widthPixels + "|";
		test += "heightPixels:" + context.getResources().getDisplayMetrics().heightPixels + "|";
		test += "densityDpi:" + context.getResources().getDisplayMetrics().densityDpi + "|";
		test += "xdpi:" + context.getResources().getDisplayMetrics().xdpi + "|";
		test += "ydpi:" + context.getResources().getDisplayMetrics().ydpi + "|";
		test += "DENSITY_DEFAULT:" + DisplayMetrics.DENSITY_DEFAULT + "|";
		test += "DENSITY_LOW:" + DisplayMetrics.DENSITY_LOW + "|";
		test += "DENSITY_MEDIUM:" + DisplayMetrics.DENSITY_MEDIUM + "|";
		test += "DENSITY_HIGH:" + DisplayMetrics.DENSITY_HIGH + "|";
		test += "DENSITY_XHIGH:" + DisplayMetrics.DENSITY_XHIGH + "|";
		test += "DENSITY_XXHIGH:" + DisplayMetrics.DENSITY_XXHIGH + "|";
		test += "DENSITY_XXXHIGH:" + DisplayMetrics.DENSITY_XXXHIGH + "|";

		return px / density + 0.5f;
	}

	/**
	 * 根据不同的分别率，返回相应的值
	 * 
	 * @param context
	 * @param px
	 * @return
	 */
	public static float getXhBasePx(Context context, float px)
	{
		final float density = context.getResources().getDisplayMetrics().density;

		return density * px / 2.0f;
	}
}

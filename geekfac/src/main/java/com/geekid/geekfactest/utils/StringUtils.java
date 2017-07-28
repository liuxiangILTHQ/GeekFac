package com.geekid.geekfactest.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class StringUtils
{
	
	public final static String UTF_8 = "utf-8";
	

	public static String getStreamString(InputStream ios){
		
		if(ios !=null){
			try {
				BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader(ios));

				 StringBuffer tStringBuffer = new StringBuffer();

				 String sTempOneLine = new String("");

				 while ((sTempOneLine = tBufferedReader.readLine()) != null){

					 	tStringBuffer.append(sTempOneLine);
				 }
				 
				 return tStringBuffer.toString();
			} catch (Exception e) {
				// TODO: handle exception
			}
		
		}
		return null;
	}

	/** 判断字符串是否有值，如果为null或者是空字符串或者只有空格或者为"null"字符串，则返回true，否则则返回false */
	public static boolean isEmpty(String value) {
		if (value != null && !"".equalsIgnoreCase(value.trim()) && !"null".equalsIgnoreCase(value.trim())) {
			return false;
		} else {
			return true;
		}
	}

	/** 判断多个字符串是否相等，如果其中有一个为空字符串或者null，则返回false，只有全相等才返回true */
	public static boolean isEquals(String... agrs) {
		String last = null;
		for (int i = 0; i < agrs.length; i++) {
			String str = agrs[i];
			if (isEmpty(str)) {
				return false;
			}
			if (last != null && !str.equalsIgnoreCase(last)) {
				return false;
			}
			last = str;
		}
		return true;
	}

	/**
	 * 返回一个高亮spannable
	 * @param content 文本内容
	 * @param color   高亮颜色
	 * @param start   起始位置
	 * @param end     结束位置
	 * @return 高亮spannable
	 */
	public static CharSequence getHighLightText(String content, int color, int start, int end) {
		if (TextUtils.isEmpty(content)) {
			return "";
		}
		start = start >= 0 ? start : 0;
		end = end <= content.length() ? end : content.length();
		SpannableString spannable = new SpannableString(content);
		CharacterStyle span = new ForegroundColorSpan(color);
		spannable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	/**
	 * 获取链接样式的字符串，即字符串下面有下划线
	 * @param resId 文字资源
	 * @return 返回链接样式的字符串
	 */
//	public static Spanned getHtmlStyleString(int resId) {
//		StringBuilder sb = new StringBuilder();
//		sb.append("<a href=\"\"><u><b>").append(UIUtils.getString(resId)).append(" </b></u></a>");
//		return Html.fromHtml(sb.toString());
//	}

	/** 格式化文件大小，不保留末尾的0 */
	public static String formatFileSize(long len) {
		return formatFileSize(len, false);
	}

	/** 格式化文件大小，保留末尾的0，达到长度一致 */
	public static String formatFileSize(long len, boolean keepZero) {
		String size;
		DecimalFormat formatKeepTwoZero = new DecimalFormat("#.00");
		DecimalFormat formatKeepOneZero = new DecimalFormat("#.0");
		if (len < 1024) {
			size = String.valueOf(len + "B");
		} else if (len < 10 * 1024) {
			// [0, 10KB)，保留两位小数
			size = String.valueOf(len * 100 / 1024 / (float) 100) + "K";
		} else if (len < 100 * 1024) {
			// [10KB, 100KB)，保留一位小数
			size = String.valueOf(len * 10 / 1024 / (float) 10) + "K";
		} else if (len < 1024 * 1024) {
			// [100KB, 1MB)，个位四舍五入
			size = String.valueOf(len / 1024) + "K";
		} else if (len < 10 * 1024 * 1024) {
			// [1MB, 10MB)，保留两位小数
			if (keepZero) {
				size = String.valueOf(formatKeepTwoZero.format(len * 100 / 1024 / 1024 / (float) 100)) + "M";
			} else {
				size = String.valueOf(len * 100 / 1024 / 1024 / (float) 100) + "M";
			}
		} else if (len < 100 * 1024 * 1024) {
			// [10MB, 100MB)，保留一位小数
			if (keepZero) {
				size = String.valueOf(formatKeepOneZero.format(len * 10 / 1024 / 1024 / (float) 10)) + "M";
			} else {
				size = String.valueOf(len * 10 / 1024 / 1024 / (float) 10) + "M";
			}
		} else if (len < 1024 * 1024 * 1024) {
			// [100MB, 1GB)，个位四舍五入
			size = String.valueOf(len / 1024 / 1024) + "M";
		} else {
			// [1GB, ...)，保留两位小数
			size = String.valueOf(len * 100 / 1024 / 1024 / 1024 / (float) 100) + "G";
		}
		return size;
	}


	/**
	 * 距离转换
	 * @param dis 距离
	 * @param main 最小多少
	 * @param max 最大多少
	 * @return
	 */
	public static String getDistance(String dis,int main,int max){
		int distance=Integer.valueOf(dis);
		String distanceStr;
		if(distance<main){
			distanceStr=distance+"m";
		}else if(distance >=main && distance<max){ //保留一位小数
			float   scale   =  (float) (distance/1000.0);
			DecimalFormat   fnum   =   new   DecimalFormat("##0.0");
			distanceStr=fnum.format(scale)+"km";
		}else{
			distanceStr=(distance/1000)+"km";
		}
		return distanceStr;
	}
}

package com.geekid.geekfactest.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * SharedPreferences 缓存
 * @author 李文方
 *
 */
public class SharedPreferencesUtils
{


	private static final String CACHE_FILE_NAME = "geecare";
	private static SharedPreferences mSharedPreferences;

	/**
	 * @param context
	 * @param key 要取的数据的键
	 * @param defValue 缺省值
	 * @return
	 */
	public static boolean getBoolean(Context context, String key, boolean defValue) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		return mSharedPreferences.getBoolean(key, defValue);
	}

	/**
	 * 存储一个boolean类型数据
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void putBoolean(Context context, String key, boolean value) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		mSharedPreferences.edit().putBoolean(key, value).commit();
	}

	/**
	 * 存储一个String类型的数据
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void putString(Context context, String key, String value) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		mSharedPreferences.edit().putString(key, value).commit();
	}

	/**
	 * 根据key取出一个String类型的值
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static String getString(Context context, String key, String defValue) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		String value=mSharedPreferences.getString(key, defValue);
		if(StringUtils.isEmpty(value)){
			return defValue;
		}
		return value;
	}



	/**
	 * 存储一个int类型的数据
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void putInt(Context context, String key, int value) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		mSharedPreferences.edit().putInt(key, value).commit();

	}

	/**
	 * 存储一个int类型的数据
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void putLong(Context context, String key, long value) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		mSharedPreferences.edit().putLong(key, value).commit();

	}

	/**
	 * 存储一个float类型的数据
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void putFloat(Context context, String key, float value) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		mSharedPreferences.edit().putFloat(key, value).commit();
	}

	/**
	 * 根据key取出一个String类型的值
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static int getInt(Context context, String key, int defValue) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		return mSharedPreferences.getInt(key, defValue);
	}


	/**
	 * 根据key取出一个String类型的值
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static long getLong(Context context, String key, int defValue) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		return mSharedPreferences.getLong(key, defValue);
	}


	/**
	 * 根据key取出一个String类型的值
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static float getFloat(Context context, String key, float defValue) {
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		return mSharedPreferences.getFloat(key, defValue);
	}

	/**
	 * 存储一个List类型的数据 （一个activity 只能用一次）
	 * @param context
	 * @param key
	 */
	public static void putArray(Context context, String key, List<String> list) {

		if(mSharedPreferences == null) {

			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		Editor mEdit1 = mSharedPreferences.edit();
		mEdit1.putInt("Status_size",list.size());
		for(int i=0;i<list.size();i++) {
			mEdit1.remove(key + i);
			mEdit1.putString(key + i, list.get(i));

		}
		mEdit1.commit();
	}



	/**
	 *获取一个List类型的数据（一个activity 只能用一次）
	 * @param context
	 * @param key
	 */
	public static List<String> getArray(Context context, String key) {

		List<String> list=new ArrayList<String>();
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		list.clear();
		int size = mSharedPreferences.getInt("Status_size", 0);

		for(int i=0;i<size;i++) {
			list.add(mSharedPreferences.getString(key + i, null));

		}
		return list;


	}



	/**
	 * 存储一个List类型的数据 
	 * @param context
	 * @param key
	 */
	public static void putArrayList(Context context, String key, List<String> list) {

		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		Editor edit = mSharedPreferences.edit();
		try {
			String liststr = SceneList2String(list);

			edit.putString(key,liststr);
			edit.commit();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}


	/**
	 *获取一个List类型的数据
	 * @param context
	 * @param key
	 */
	public static List<String> getArrayList(Context context, String key) {

		List<String> list=new ArrayList<String>();
		if(mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(CACHE_FILE_NAME, Context.MODE_PRIVATE);
		}
		String liststr = mSharedPreferences.getString(key, "");
		try {
			list = String2SceneList(liststr);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}


	public static String SceneList2String(List<String> SceneList) throws IOException {
		// 实例化一个ByteArrayOutputStream对象，用来装载压缩后的字节文件。
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		// 然后将得到的字符数据装载到ObjectOutputStream
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				byteArrayOutputStream);
		// writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
		objectOutputStream.writeObject(SceneList);
		// 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
		String SceneListString = new String(Base64.encode( byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
		// 关闭objectOutputStream
		objectOutputStream.close();
		return SceneListString;

	}

	@SuppressWarnings("unchecked")
	public static List<String> String2SceneList(String SceneListString)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		byte[] mobileBytes = Base64.decode(SceneListString.getBytes(),
				Base64.DEFAULT);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				mobileBytes);
		ObjectInputStream objectInputStream = new ObjectInputStream(
				byteArrayInputStream);
		List<String> SceneList = (List<String>) objectInputStream
				.readObject();
		objectInputStream.close();
		return SceneList;
	}




}

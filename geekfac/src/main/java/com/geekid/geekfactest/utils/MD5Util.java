package com.geekid.geekfactest.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * 采用MD5加密解密
 * @author tfq
 * @datetime 2011-10-13
 */
public class MD5Util
{

	/***
	 * MD5加码 生成32位md5码
	 * @throws UnsupportedEncodingException 
	 */
	public static String string2MD5(String inStr){
		MessageDigest md5 = null;
		try{
			md5 = MessageDigest.getInstance("MD5");
		}catch (Exception e){
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}
				
		byte[] byteArray = null;
		try
		{
			byteArray = inStr.getBytes("utf-8");
		} catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//char[] charArray = inStr.toCharArray();
				//byte[] byteArray = new byte[charArray.length];
		//for (int i = 0; i < charArray.length; i++)
		//	byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++){
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();

	}

	/**
	 * 加密解密算法 执行一次加密，两次解密
	 */ 
	public static String convertMD5(String inStr){

		char[] a = inStr.toCharArray();
		for (int i = 0; i < a.length; i++){
			a[i] = (char) (a[i] ^ 't');
		}
		String s = new String(a);
		return s;

	}
    public static byte[] hexStringToBytes(String hexString)
    {
        if (hexString == null || hexString.equals(""))
        {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++)
        {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static int[] hexStringToInt(String hexString)
    {
        if (hexString == null || hexString.equals(""))
        {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        int[] d = new int[length];
        for (int i = 0; i < length; i++)
        {
            int pos = i * 2;
            //d[i] =
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c)
    {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

	// 测试主函数
	public static void main(String args[]) {
		String s = new String("abc");
		System.out.println("原始：" + s);
		System.out.println("MD5后：" + string2MD5(s));
		System.out.println("加密的：" + convertMD5(s));
		System.out.println("解密的：" + convertMD5(convertMD5(s)));

        //550807e10301110e329a
        int count=10000000;
        String ss="550807e10301110e32";
        byte[] b=hexStringToBytes(ss);
        for(int i=0;i<b.length;i++){
            //count+=b[i]&0xff;

        }
        byte bb=(byte)count;
        System.out.println("原始1：" + bb+","+Integer.toHexString(count&0xff));
        System.out.println("原始1：" + bb+","+Integer.toHexString(bb&0xff));
        String cc=Integer.toHexString(count);
        System.out.println("原始：" + cc);
        int len=cc.length();
        if(len>=2){
            cc=cc.substring(len-2,len);
        }else if(len<2){
            cc="0"+cc;
        }
        System.out.println("原始：" + cc);

		
	 

	}
}

package com.geecare.blelibrary;

public class CommandManager
{
    public CommandManager()
    {
    }

    private int decStr2Int(String dec)
    {
        return Integer.parseInt(dec, 16);
    }

    public static String byteToHexString(byte[] bArray)
    {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++)
        {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
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

    private static byte charToByte(char c)
    {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String int2DecStr(int n)
    {
        String str = Integer.toHexString(n);
        int l = str.length();
        if (l % 2 != 0)
        {
            str = "0" + str;
        }

        return str;
    }


    public static String hexstringToChar(String s)
    {
        String ss = "";
        for (int i = 0; i < s.length(); i = i + 2)
        {
            String tt = s.substring(i, i + 2);
            int code = Integer.parseInt(tt, 16);
            char ch = (char) code;
            ss += ch;
        }
        return ss;
    }



}

package su.os3.lbkx;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;

public class lbkxArrayUtil {
    public static byte[] byteArrayConc(byte[]... args){
        byte[] finalArray=new byte[0];
        for (byte[] arg : args){
            finalArray=ArrayUtils.addAll(finalArray, arg);
        }
        return finalArray;
    }

    public static String[] stringArrayConc(String[]... args){
        String[] finalArray=new String[0];
        for (String[] arg : args){
            finalArray=ArrayUtils.addAll(finalArray, arg);
        }
        return finalArray;
    }

    public static byte[] doubleToByte(double value){
        return ByteBuffer.wrap(new byte[8]).putDouble(value).array();
    }

    public static byte[] longToByte(long value){
        return ByteBuffer.wrap(new byte[8]).putLong(value).array();
    }

    public static byte[] hexStringToByte(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] intToByte(int value){
        return ByteBuffer.wrap(new byte[4]).putInt(value).array();
    }

    public static short byteToShort(byte[] value){
        return ByteBuffer.wrap(value).getShort();
    }

    public static double byteToDouble(byte[] value){
        return ByteBuffer.wrap(value).getDouble();
    }
}

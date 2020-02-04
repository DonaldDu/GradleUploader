package com.dhy.uploader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

import javax.annotation.Nullable;


public class SignUtils {

    private static String bytes2Hex(byte[] src) {
        char[] res = new char[src.length * 2];
        final char[] hexDigits = "0123456789abcdef".toCharArray();
        for (int i = 0, j = 0; i < src.length; i++) {
            res[j++] = hexDigits[src[i] >>> 4 & 0x0f];
            res[j++] = hexDigits[src[i] & 0x0f];
        }
        return new String(res);
    }

    @Nullable
    public static String getMd5ByFile(File file) {
        String value = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);

            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[8192];
            int byteCount;
            while ((byteCount = in.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }
            value = bytes2Hex(digester.digest());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }


    public static boolean checkMd5(File file, String md5) {
        String fileMd5 = getMd5ByFile(file);
        return md5.equals(fileMd5);
    }


    public static boolean checkMd5(String filePath, String md5) {
        return checkMd5(new File(filePath), md5);
    }
}
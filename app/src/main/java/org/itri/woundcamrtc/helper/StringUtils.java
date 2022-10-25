package org.itri.woundcamrtc.helper;

import android.util.Base64;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class StringUtils {

    // 使用CBC模式, 在初始化Cipher對象時, 需要增加參數, 初始化向量IV : IvParameterSpec iv = new IvParameterSpec(key.getBytes());
    public static final String transformation = "DES/ECB/PKCS5Padding";
    // NOPadding: 使用NOPadding模式時, 原文長度必須是8byte的整數倍
    //public static final String transformation = "DES/ECB/NOPadding";
    public static final String key = "12345678";

    public StringUtils() {
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isValidDate(String inDate, String defaultFormat) {
        String format = "yyyy-MM-dd HH-mm-ss-SSS";
        if (isNotBlank(defaultFormat))
            format = defaultFormat;

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (Exception pe) {
            return false;
        }
        return true;
    }

    public static String getMD5(String str, String defaultStr) {
        String ret = defaultStr;
        try {
            // 生成一個MD5加密計算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 計算md5函數
            md.update(str.getBytes());
            // digest()最後確定返回md5 hash值，返回值為8為字符串。因為md5 hash值是16位的hex值，實際上就是8位的字符
            // BigInteger函數則將8位的字符串轉換成16位hex值，用字符串來表示；得到字符串形式的hash值
            ret = new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            //throw new SpeedException("MD5加密出現錯誤");
            e.printStackTrace();
        }
        return ret;
    }

    public static String encryptByDES(String msg) throws Exception {
        // 獲取Cipher
        Cipher cipher = Cipher.getInstance(transformation);

        // 指定密鑰規則
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "DES");

        // 指定模式(加密)和密鑰
        // 創建初始向量
        IvParameterSpec iv = new IvParameterSpec(key.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        //  cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        // 加密
        byte[] bytes = cipher.doFinal(msg.getBytes());
        // 輸出加密後的數據
        // com.sun.org.apache.xml.internal.security.utils.Base64
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static String decryptByDES(String encrypted) throws Exception {
        // 獲取Cipher
        Cipher cipher = Cipher.getInstance(transformation);

        // 指定密鑰規則
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "DES");

        // 指定模式(解密)和密鑰
        // 創建初始向量
        IvParameterSpec iv = new IvParameterSpec(key.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        //  cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        // 解碼密文
        // com.sun.org.apache.xml.internal.security.utils.Base64
        byte[] decode = Base64.decode(encrypted, Base64.DEFAULT);
        // 解密
        byte[] bytes = cipher.doFinal(decode);
        // 輸出解密後的數據
        return new String(bytes);
    }
}

package org.itri.woundcamrtc.helper;


import java.nio.ByteBuffer;
import java.util.Formatter;

public class BytesHelper {

    /**
     * Converts a hex string to bytes.
     *
     * @param s a hex string
     * @return byte array
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Converts a byte array to hex string.
     *
     * @param bytes a byte array.
     * @param pos   the start position
     * @param len   number of bytes to be converted
     * @return hex string.
     */
    public static String byteArrayToHexString(byte[] bytes, int pos, int len) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        Formatter formatter = new Formatter(sb);
        for (int i = pos; i < pos + len; ++i) {
            formatter.format("%02x", bytes[i]);
        }
        return sb.toString();
    }

    /**
     * Reads the remaining bytes in a ByteBuffer into a byte[].
     *
     * @param byteBuffer byte buffer to read from.
     * @return byte[] containing the bytes read.
     */
    public static byte[] readBytesFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] buffer = new byte[byteBuffer.remaining()];
        byteBuffer.get(buffer);
        return buffer;
    }

    /**
     * Reads the remaining bytes in a ByteBuffer into a byte[] without consuming.
     *
     * @param byteBuffer byte buffer to read from.
     * @return byte[] containing the bytes read.
     */
    public static byte[] readBytesFromByteBufferWithoutConsume(ByteBuffer byteBuffer) {
        byte[] buffer = new byte[byteBuffer.remaining()];
        byteBuffer.duplicate().get(buffer);
        return buffer;
    }


    public static ByteBuffer longToByteBuffer(long num) {
        return ByteBuffer.allocate(Long.SIZE / 8).putLong(0, num);
    }

    public static long longFromByteBuffer(ByteBuffer byteBuffer) {
        return ByteBuffer.wrap(readBytesFromByteBuffer(byteBuffer)).getLong();
    }

    public static ByteBuffer intToByteBuffer(int num) {
        return ByteBuffer.allocate(Integer.SIZE / 8).putInt(0, num);
    }

    public static int intFromByteBuffer(ByteBuffer byteBuffer) {
        return ByteBuffer.wrap(readBytesFromByteBuffer(byteBuffer)).getInt();
    }

    public static ByteBuffer doubleToByteBuffer(double val) {
        return ByteBuffer.allocate(Double.SIZE / 8).putDouble(val);
    }

    public static double doubleFromByteBuffer(ByteBuffer byteBuffer) {
        return ByteBuffer.wrap(readBytesFromByteBuffer(byteBuffer)).getDouble();
    }

    public static ByteBuffer byteToByteBuffer(byte num) {
        return ByteBuffer.allocate(1).put(num);
    }

    public static byte byteFromByteBuffer(ByteBuffer byteBuffer) {
        return ByteBuffer.wrap(readBytesFromByteBuffer(byteBuffer)).get();
    }

    public static byte[] toBytes(long num) {
        return readBytesFromByteBuffer(longToByteBuffer(num));
    }


}
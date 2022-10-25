package org.itri.woundcamrtc.helper;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.util.IOUtils;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.R;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Created by Admin on 2017/6/3.
 */

public class FileHelper {

    private static String TAG = "FileHelper";
    private static File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);

    public static boolean isExist(String filePath, String filename, boolean delete) {
        try {
            File file = new File(filePath + "/"+filename);
            Log.v("FileHelper","filepath :"+file.getAbsolutePath());
            if (file.exists()) {
                if (delete)
                    file.delete();
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void copyIni(Context mContext, String newPath, String filename, Boolean overwrite) throws IOException {
        File file = new File(newPath);

        if (!file.exists()) {
            if (!file.mkdirs()) {
                try {
                    if (!file.createNewFile()) {
                        Log.e(TAG, "file is not can create.");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "file is not can create." + e);
                    e.printStackTrace();
                }
            }
        }

        file = new File(newPath + File.separator + filename);
        if (!overwrite && file.exists()) {
            Log.d(TAG, "資料已存在，不再複製");
        } else {
            Log.d(TAG, "資料不存在，開始複製");
            InputStream mInput = mContext.getAssets().open(filename);
            String outFileName = newPath + File.separator + filename;
            OutputStream mOutput = new FileOutputStream(outFileName);
            byte[] mBuffer = new byte[1024];
            int mLength;
            while ((mLength = mInput.read(mBuffer)) > 0) {
                Log.d(TAG, "複製...");
                mOutput.write(mBuffer, 0, mLength);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mOutput.flush();
            mOutput.close();
            mInput.close();
        }
    }

    public static Mat imreadSecret(String filename) {
        try {
            filename = filename.replace("//","/");
            FileInputStream ins = new FileInputStream(filename);
            byte[] targetArray = IOUtils.toByteArray(ins);
            if (filename.toLowerCase().endsWith(".jpg")) {
                if (targetArray[0xb1] == 0x10) {

                    int chn = 0;
                    try {
                        String chs = ServiceHelper.getSerialNumber("0");
                        chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                    } catch (Exception e) {
                    }
                    if (targetArray[0xb2] == (byte) chn) {
                        targetArray[0xb1] = (byte) 0xff;
                        targetArray[0xb2] = (byte) 0xc4;
                    }
                }
            } else if (filename.toLowerCase().endsWith(".png")) {
                if (targetArray[1] == 0x10) {

                    int chn = 0;
                    try {
                        String chs = ServiceHelper.getSerialNumber("0");
                        chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                    } catch (Exception e) {
                    }
                    if (targetArray[2] == (byte) chn) {
                        targetArray[1] = 0x50;
                        targetArray[2] = (byte) 0x4e;
                    }
                }
            }
            return Imgcodecs.imdecode(new MatOfByte(targetArray), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        } catch (Exception ex) {
            return null;
        }
    }

    public static InputStream inputStreamSecret(String fileName) throws IOException {
        fileName = fileName.replace("//","/");
        InputStream ins = new FileInputStream(fileName);
        if (fileName.toLowerCase().endsWith(".png")) {
            byte[] targetArray = IOUtils.toByteArray(ins);
            if (targetArray[1] == 0x10) {

                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                if (targetArray[2] == (byte) chn) {
                    targetArray[1] = 0x50;
                    targetArray[2] = (byte) 0x4e;
                }
            }
            ins = new ByteArrayInputStream(targetArray);
        } else if (fileName.toLowerCase().endsWith(".jpg")) {
            byte[] targetArray = IOUtils.toByteArray(ins);
            if (targetArray[0xb1] == 0x10) {

                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                if (targetArray[0xb2] == (byte) chn) {
                    targetArray[0xb1] = (byte) 0xff;
                    targetArray[0xb2] = (byte) 0xc4;
                }
            }
            ins = new ByteArrayInputStream(targetArray);
        }
        return ins;

    }

    public static InputStream inputStreamSecret(String fileName, InputStream ins) throws IOException {
        if (fileName.toLowerCase().endsWith(".png")) {
            byte[] targetArray = IOUtils.toByteArray(ins);
            if (targetArray[1] == 0x10) {

                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                if (targetArray[2] == (byte) chn) {
                    targetArray[1] = 0x50;
                    targetArray[2] = (byte) 0x4e;
                }
            }
            ins = new ByteArrayInputStream(targetArray);
        } else if (fileName.toLowerCase().endsWith(".jpg")) {
            byte[] targetArray = IOUtils.toByteArray(ins);
            if (targetArray[0xb1] == 0x10) {

                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                if (targetArray[0xb2] == (byte) chn) {
                    targetArray[0xb1] = (byte) 0xff;
                    targetArray[0xb2] = (byte) 0xc4;
                }
            }
            ins = new ByteArrayInputStream(targetArray);
        }
        return ins;
    }

    public static boolean imwriteSecret(String filename, Mat content) {
        String type = ".jpg";
        if (filename.toLowerCase().endsWith(".jpg"))
            type = ".jpg";
        else if (filename.toLowerCase().endsWith(".png"))
            type = ".png";
        if (content == null || content.empty()) {
            return false;
        }

        MatOfByte bytemat = new MatOfByte();
        Imgcodecs.imencode(type, content, bytemat);
        byte[] targetArray = bytemat.toArray();
        InputStream ins = null;
        if (type.equals(".png")) {
            int chn = 0;
            try {
                String chs = ServiceHelper.getSerialNumber("0");
                chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
            } catch (Exception e) {
            }
            targetArray[1] = 0x10;
            targetArray[2] = (byte) chn;
            ins = new ByteArrayInputStream(targetArray);

        } else if (type.equals(".jpg")) {
            int chn = 0;
            try {
                String chs = ServiceHelper.getSerialNumber("0");
                chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
            } catch (Exception e) {
            }
            targetArray[0xb1] = (byte) 0x10;
            targetArray[0xb2] = (byte) chn;
            ins = new ByteArrayInputStream(targetArray);
        }
        OutputStream outfile = null;
        try {
            outfile = new FileOutputStream(filename);
            byte[] buffer = new byte[10 * 1024]; // or other buffer size
            int read;

            while ((read = ins.read(buffer)) != -1) {
                outfile.write(buffer, 0, read);
            }

            outfile.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (outfile != null) {
                    outfile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean overwriteFileSecret(String filePath) {
        filePath = filePath.replace("//", "/");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(filePath, "rw");
            if (filePath.toLowerCase().endsWith(".png")) {
                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                raf.seek(1);
                raf.writeByte(0x10);
                raf.seek(2);
                raf.writeByte((byte) chn);
            } else if (filePath.toLowerCase().endsWith(".jpg")) {
                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                raf.seek(0xb1);
                raf.writeByte(0x10);
                raf.seek(0xb2);
                raf.writeByte((byte) chn);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (raf != null) {
                    raf.getFD().sync();
                    raf.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static boolean decodeFileSecret(String filePath) {
        filePath = filePath.replace("//", "/");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(filePath, "rw");
            if (filePath.toLowerCase().endsWith(".png")) {
                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                raf.seek(2);
                byte targetByte = raf.readByte();
                //if (targetByte == (byte) chn) {
                    raf.seek(1);
                    raf.writeByte(0x50);
                    raf.seek(2);
                    raf.writeByte(0x4e);
                //}
            } else if (filePath.toLowerCase().endsWith(".jpg")) {
                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                raf.seek(0xb2);
                byte targetByte = raf.readByte();
                //if (targetByte == (byte) chn) {
                    raf.seek(0xb1);
                    raf.writeByte(0xff);
                    raf.seek(0xb2);
                    raf.writeByte(0xc4);
                //}
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (raf != null) {
                    raf.getFD().sync();
                    raf.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static boolean rewriteFileSecret(String filePath) {
        InputStream ins = null;
        OutputStream outfile = null;
        try {
            filePath = filePath.replace("//","/");

            ins = new FileInputStream(filePath);

            if (filePath.toLowerCase().endsWith(".png")) {
                byte[] targetArray = IOUtils.toByteArray(ins);
                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                targetArray[1] = 0x10;
                targetArray[2] = (byte) chn;
                ins = new ByteArrayInputStream(targetArray);

            } else if (filePath.toLowerCase().endsWith(".jpg")) {
                byte[] targetArray = IOUtils.toByteArray(ins);
                int chn = 0;
                try {
                    String chs = ServiceHelper.getSerialNumber("0");
                    chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
                } catch (Exception e) {
                }
                targetArray[0xb1] = (byte) 0x10;
                targetArray[0xb2] = (byte) chn;
                ins = new ByteArrayInputStream(targetArray);
            }

            try {
                outfile = new FileOutputStream(filePath);
                byte[] buffer = new byte[10 * 1024]; // or other buffer size
                int read;

                while ((read = ins.read(buffer)) != -1) {
                    outfile.write(buffer, 0, read);
                }

                outfile.flush();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outfile != null) {
                        outfile.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ins != null) {
                    ins.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static void txt_decrypt(String from, int day) throws Exception {
        Log.v(TAG, "解密");
        String txtdecrypt = from.substring(from.lastIndexOf("/") + 1).replace("_datax.txt", "_data.txt");
        FileInputStream fis = new FileInputStream(from);
        FileOutputStream fos = new FileOutputStream(file.getPath() + "/" + txtdecrypt, false);
        int n = fis.read();
        //循环写入
        while (n != -1) {//解密算法，可行定义，与加密算法相逆
            if (day * n != 1) {
                fos.write(n - day);
            } else {
                fos.write(n);
            }
            n = fis.read();
        }
        //关闭输入输出流
        fis.close();
        File file = new File(from);
        file.delete();
        fos.getFD().sync();
        fos.close();
    }

    public static void txt_encryption(String from, int day) throws Exception {
        Log.v(TAG, "加密");

        String txtencryption = from.substring(from.lastIndexOf("/") + 1).replace("_data.txt", "_datax.txt");
        FileInputStream fis = new FileInputStream(from);
        FileOutputStream fos = new FileOutputStream(file.getPath() + "/" + txtencryption, false);
        int n = fis.read();
        int count = 0;
        //循环写入
        while (n != -1) {//加密算法，可行定义，与解密算法相逆
            if (day * n != 1) {
                fos.write(n + day);
            } else {
                fos.write(n);
            }
            n = fis.read();
        }
        fis.close();
        File file = new File(from);
        file.delete();
        fos.getFD().sync();
        fos.close();
    }

    public static Mat inputStream2Mat(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = bis.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
        os.close();
        bis.close();

        Mat encoded = new Mat(1, os.size(), 0);
        encoded.put(0, 0, os.toByteArray());

        Mat decoded = Imgcodecs.imdecode(encoded, -1);
        encoded.release();
        return decoded;
    }

    public static InputStream mat2InputStream(Mat mat)
    {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mob);
        byte[] byteArray = mob.toArray();
        return new ByteArrayInputStream(byteArray);
    }
}

package org.itri.woundcamrtc.preview;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.itri.woundcamrtc.helper.*;
import org.json.JSONException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;
import static org.itri.woundcamrtc.AppResultReceiver.SAVE_DIR;

/**
 * Created by schung on 2020/04/08.
 */

public class FileUtility {

    private static final String TAG = "FileUtility";

    // 在系統的Download文件夾下創建一個文件夾，用來保存所有圖片
    public static final String DOWNLOAD_DIR_NAME = "/WoundCamRtc/CameraImg";



    /**
     * 取得圖片檔資訊
     * @param ctx
     * @return 圖片檔資訊. K: ImageId(檔案名稱), V: ImagePath(檔案絕對路徑)
     */
    public static Map<String, String> getImageListFromDownload(final Context ctx) {

        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
        if (!mPicDir.exists()) {
            Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + "不存在");
            return null;
        }
        //Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + ", R:" + mPicDir.canRead() + ", W:" + mPicDir.canWrite());
        //

        String[] filenames = mPicDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                //Log.d(TAG, "# " + filename);
                //if (filename.endsWith(".jpg") || filename.endsWith(".png")) {
                if (filename.endsWith("jpg.jpg")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (filenames == null) {
            Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + " 沒有 .jpg 圖片");
            return null;
        }
        //
        //Arrays.sort(filenames,Collections.reverseOrder());
        Map<String, String> map = new TreeMap<String, String>();
        for (String filename : filenames) {
            // Log.d(TAG, filename);
            // filename 是相對路徑格式
            try {
                File file = new File(mPicDir, filename);
                if (file.exists()) {
                    String imageId = file.getName();
                    String imagePath = file.getAbsolutePath();
                    map.put(imageId, imagePath);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Get Photo File Error", ex);
            }
        }
        //
        return map;
    }

    public static Map<String, String> getAllListFromDownload(final Context ctx) {
        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
        if (!mPicDir.exists()) {
            Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + "不存在");
            return null;
        }
        //Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + ", R:" + mPicDir.canRead() + ", W:" + mPicDir.canWrite());
        //

        String[] filenames = mPicDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                Log.d(TAG, "getAllListFromDownload :　"+filename);

                if (filename.endsWith("jpg.jpg") || filename.endsWith("thm.png") || filename.endsWith(".txt")) {
                    Log.v(TAG,"TEst1 : "+ filename);
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (filenames == null) {
            Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + " 沒有可用的檔案");
            return null;
        }
        //
        //Arrays.sort(filenames,Collections.reverseOrder());
//        Map<String, String> map = new TreeMap<String, String>();
        Map<String, String> map = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        for (String filename : filenames) {
            if(dataEncrypt==true){
                Log.v(TAG,"TEst1filename : "+ filename);
                if(filename.endsWith("_datax.txt")){

                    File sercrettxt=new File(mPicDir,filename);
                    Log.d(TAG, "sercrettxt :　"+sercrettxt.getAbsolutePath());
                    try {
                        FileHelper.txt_decrypt(sercrettxt.getAbsolutePath(),18);
                        filename=sercrettxt.getName().replace("_datax.txt","_data.txt");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
            // filename 是相對路徑格式
            try {
                File file = new File(mPicDir, filename);
                if (file.exists()) {

                    String fileId = file.getName();
                    String filePath = file.getAbsolutePath();
                    map.put(fileId, filePath);


                }
            } catch (Exception ex) {
                Log.e(TAG, "Get Photo File Error", ex);
            }
        }
        //
        return map;
    }

    /**
     * 刪除圖片檔
     * @param ctx
     * @param imagePaths 圖片檔檔名列表
     */
    public static void delImageFromDownload(final Context ctx, String[] imagePaths) {
        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
        for (String imagePath : imagePaths) {
            File delFile = new File(imagePath); //欲刪除檔案
            if (delFile.exists()) {
                Log.v(TAG, "欲刪除的檔案名稱:" + delFile.getName());
                String delFileParam[] = delFile.getName().split("_");   //切割刪除檔案之檔名內容
                String[] fileNames = mPicDir.list();    //尋找 Download>carameImg 檔案夾所有檔案

                //Log.v(TAG, "所有符合檔名部分字串的檔案數:" + findAllMatchFiles.length);

                for (String fileName : fileNames) {
                    try {
                        int del3dNum = 0;
                        int delThermalNum = 0;
                        File compareFile = new File(mPicDir, fileName);
                        if (compareFile.exists()) {  //如果比較的檔案存在
                            String compareFileName = compareFile.getName();
                            String compareFileParam[] = compareFileName.split("_");
                            delThermalNum = Integer.parseInt(delFileParam[2]) + 99;
                            del3dNum = Integer.parseInt(delFileParam[2]) + 199;

                            if(compareFileName.equals(delFile.getName()) || (compareFileParam[0].equals(delFileParam[0]) && compareFileParam[1].equals(delFileParam[1]) && compareFileParam[2].equals(delFileParam[2]) && compareFileParam[3].equals("gai.jpg")||compareFileParam[3].equals("jpg2.jpg"))
                                    || (compareFileParam[0].equals(delFileParam[0]) && compareFileParam[1].equals(delFileParam[1])
                                && (compareFileParam[2].equals(String.valueOf(del3dNum)) || compareFileParam[2].equals(String.valueOf(delThermalNum))))){    //名稱符合且結尾為jpg
                                compareFile.delete();    //刪除JPG檔案
                                Log.d(TAG, "執行刪除圖片完畢: " + compareFile.getName());

                                /*if(findAllMatchFiles.length - 1 == 0){  //檢查同檔名之檔案數於圖檔刪除後的數量是否為0，為0則刪除同檔名之txt檔
                                    String delTxtName = compareFileParam[0] + "_" + compareFileParam[1] + "_13_data.txt";
                                    File delTxt = new File(mPicDir.getPath() + File.separator + delTxtName );
                                    delTxt.delete();
                                    Log.d(TAG, "執行刪除TXT完畢: " + delTxt.getName());
                                }*/
                            }
                            Log.v(TAG, "-------------截止線----------");
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Get Photo File Error", ex);
                    }
                }

                File [] findAllMatchFiles = mPicDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.startsWith(delFileParam[0]);   //挑出所有符合檔案名稱字串條件的檔案
                        //return name.startsWith(delFileParam[0]) && name.endsWith(".jpg");
                    }
                });
                String delTxtName="";
                Log.d(TAG, "剩餘檔案數量: " + findAllMatchFiles.length);
                if(findAllMatchFiles.length - 1 == 0){  //檢查同檔名之檔案數於圖檔刪除後的數量是否為0，為0則刪除同檔名之txt檔
                    if(dataEncrypt==false){
                        delTxtName = delFileParam[0] + "_" + delFileParam[1] + "_13_data.txt";
                    }else{
                         delTxtName = delFileParam[0] + "_" + delFileParam[1] + "_13_datax.txt";
                    }

                    Log.d(TAG, "刪除TXT" + delTxtName);
                    File delTxt = new File(mPicDir.getPath() + File.separator + delTxtName );
                    delTxt.delete();
                    Log.d(TAG, "執行刪除TXT完畢: " + delTxt.getName());
                }
            }
            else {
                Log.d(TAG, "檔案 " + imagePath + " 不存在");
            }
        }
    }

    // 取得照片的Metadata資料

    /**
     * 取得照片的Txt資料
     * @param ctx
     * @param groupId 病患該次看診編號(evlId)
     * @return 照片的Txt資料
     */
    public static TxtData getTxtDataFromDownload(final Context ctx, final String groupId) {
        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
        if (!mPicDir.exists()) {
            Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + "不存在");
            return null;
        }
        /*
        String[] filenames = mPicDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.startsWith(groupId) && filename.endsWith("_13_data.txt")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        // 理論上至多一個檔案
        TxtData output = null;
        if (filenames.length > 0) {
            File target = new File(mPicDir, filenames[0]);
            try {
                output = TxtData.parse(new FileReader(target));
            } catch (Exception ex) {
                Log.e(TAG, "Get TxtData " + target.getName() + " Error", ex);
                output = null;
            }
        } else {
            Log.w(TAG, "沒有找到 " + groupId + "*_13_data.txt 檔");
        }
        */
        Log.d(TAG, "讀取Txt資料: evlId=[" + groupId + "]");
        // groupId格式為 evlId: yyyy-MM-dd HH-mm-ss-SSS
        // 要組成txt檔名, 格式: yyyy-MM-dd HH-mm-ss-SSS_yyyy-MM-dd_13_data.txt

        //解密TXT

        String filename = groupId + "_"  + groupId.substring(0, 10) + "_13_data.txt";
        File target = new File(mPicDir, filename);
        TxtData output = null;
        if (target.exists()) {
            try {
                output = TxtData.parse(new FileReader(target));
            } catch (Exception ex) {
                Log.e(TAG, "Get TxtData " + target.getName() + " Error", ex);
                output = null;
            }


        } else {
            if (dataEncrypt == false) {
                return null;
            } else {
                 filename = groupId + "_"  + groupId.substring(0, 10) + "_13_datax.txt";
                try { mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                    FileHelper.txt_decrypt(mPicDir.getAbsolutePath()+"/"+filename,18);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                output = TxtData.parse(new FileReader(target));
                Log.v(TAG,"output : "+output);
                if (dataEncrypt == false) {
                    return null;
                } else {
                    filename = groupId + "_"  + groupId.substring(0, 10) + "_13_data.txt";
                    try { mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                        FileHelper.txt_encryption(mPicDir.getAbsolutePath()+"/"+filename,18);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return output;

        }
        //
        return output;

    }

    /**
     * 儲存照片的Txt資料
     * @param ctx
     * @param data Txt資料
     * @return 儲存結果
     */
    public static boolean saveTxtDataToDownload(final Context ctx, final TxtData data) {
        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
        if (!mPicDir.exists()) {
            Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + "不存在");
            return false;
        }
        Log.d(TAG, "儲存Txt資料: evlId=[" + data.getEvlId() + "]");
        // groupId格式為 evlId: yyyy-MM-dd HH-mm-ss-SSS
        // 要組成txt檔名, 格式: yyyy-MM-dd HH-mm-ss-SSS_yyyy-MM-dd_13_data.txt
        if (dataEncrypt == false) {

        } else {
            String filename_sercret = data.getEvlId() + "_"  + data.getEvlId().substring(0, 10)  + "_13_datax.txt";
            File target_sercret = new File(mPicDir, filename_sercret);

            try {
                FileHelper.txt_decrypt(target_sercret.getAbsolutePath(),18);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        String filename = data.getEvlId() + "_"  + data.getEvlId().substring(0, 10) + "_13_data.txt";

        File target = new File(mPicDir, filename);
        if (!target.exists()) {
            Log.w(TAG, "沒有找到 " + filename + " 檔");
            return false;
        }
        //
        try {
            //TxtData data = TxtData.parse(new FileReader(target));
            FileWriter fwr = new FileWriter(target, false);
            fwr.write(data.toTxtString());
            fwr.close();

            if (dataEncrypt == false) {

            } else {
                try {
                    FileHelper.txt_encryption(target.getAbsolutePath(),18);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "Update TxtData " + target.getName() + " Error", ex);
            return false;
        }
    }

}
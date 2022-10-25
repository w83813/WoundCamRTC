package com.serenegiant.usb.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.util.Log;
import android.view.TextureView;

import com.shlll.libusbcamera.USBCameraHelper;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.helper.FileHelper;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;


import static org.opencv.core.CvType.CV_16UC1;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_8U;

public class ImageSaverFlir implements Runnable {
    private String TAG = ImageSaverFlir.class.getSimpleName();
    private ByteBuffer buffer;
    private double kelvin;
    private USBCameraHelper mUSBCameraHelper;
    private String filePath = "";
    private TextureView textureView;
    public double vmin = 0;
    public double vmax = 0;

//    public ImageSaverFlir(ByteBuffer buffer, double kelvin, AbstractUVCCameraHandler.CameraThread cameraThread, String filePath, TextureView textureView) {
public ImageSaverFlir(ByteBuffer buffer, double kelvin, USBCameraHelper usbCameraHelper, String filePath, TextureView textureView) {
        this.buffer = buffer;
        this.kelvin = kelvin;
        this.mUSBCameraHelper = usbCameraHelper;
        this.filePath = filePath;
        this.textureView = textureView;
    }

    @Override
    public void run() {
        try {
//            Mat writingMat = new Mat();
            Mat matResized = new Mat();
            Mat matFinal = new Mat();
            Mat matSrc = new Mat(120, 160, CV_16UC1, buffer);
            Bitmap mBitmap = null;
//            if (Build.MODEL.endsWith("MPD100") || Build.MODEL.endsWith("MPD500"))
            mBitmap = Bitmap.createBitmap(120, 160, Bitmap.Config.ARGB_8888);
//            else
//                mBitmap = Bitmap.createBitmap(160, 120, Bitmap.Config.ARGB_8888);
            try {
                //Imgproc.resize(matSrc, matResized, new org.opencv.core.Size(160, 120), 0, 0, Imgproc.INTER_LINEAR);
                rotateMatCW(matSrc, matResized, AppResultReceiver.thermalImageRotateAngle);

                double vmin = 0;
                double vmax = 0;
                if (AppResultReceiver.touchPointThermalDisplay == 0) {
                    // auto gan
                    Core.MinMaxLocResult mmlr = Core.minMaxLoc(matResized);
                    vmin = mmlr.minVal;
                    vmax = mmlr.maxVal;
                } else if (AppResultReceiver.touchPointThermalDisplay == 1) {

                    if (AppResultReceiver.touchPointThermalFormula == 35) {
                        // set max & min
                        vmin = (32 - 8 + 273.15f) / 0.0096f;
                        vmax = (32 + 8 + 273.15f) / 0.0096f;
                    } else {
                        vmin = ((((32 - 8) + 273.15f) * 1.0f / 0.01f) - kelvin + 8192);
                        vmax = ((((32 + 8) + 273.15f) * 1.0f / 0.01f) - kelvin + 8192);
                    }
                    double diff = (vmax -  vmin)/2.0;

                    // 顯示範圍從26~40度, 但有可能拍攝物比這範圍還小, 所以依照片中央ROI, 根據溫度實際值, 重新調整顯示的範圍
                    Core.MinMaxLocResult mmlr = Core.minMaxLoc(matResized.submat(60,100,40,80));
                    if (vmin < mmlr.minVal)
                        vmin = mmlr.minVal;
                    if (vmax > mmlr.maxVal)
                        vmax = mmlr.maxVal;
                    if (vmin > vmax) {
                        double tmp = vmin;
                        vmin = vmax;
                        vmax = tmp;
                    }

                    if ((vmax-vmin)<diff){
                        vmin = vmax - diff;
                    }
                } else if (AppResultReceiver.touchPointThermalDisplay == 2) {
                    if (AppResultReceiver.touchPointThermalFormula == 35) {
                        // set max & min
                        vmin = (AppResultReceiver.touchPointThermalBaseCelsius - AppResultReceiver.touchPointThermalBaseCelsiusRange + 273.15f) / 0.0096f;
                        vmax = (AppResultReceiver.touchPointThermalBaseCelsius + AppResultReceiver.touchPointThermalBaseCelsiusRange + 273.15f) / 0.0096f;
                    } else {
                        vmin = ((((AppResultReceiver.touchPointThermalBaseCelsius - AppResultReceiver.touchPointThermalBaseCelsiusRange) + 273.15f) * 1.0f / 0.01f) - kelvin + 8192);
                        vmax = ((((AppResultReceiver.touchPointThermalBaseCelsius + AppResultReceiver.touchPointThermalBaseCelsiusRange) + 273.15f) * 1.0f / 0.01f) - kelvin + 8192);
                    }
                } else if (AppResultReceiver.touchPointThermalDisplay == 3){
                    if (AppResultReceiver.touchPointThermalFormula == 35) {
                        double center_value = matResized.get(matResized.rows() / 2, matResized.cols() / 2)[0];
                        center_value = center_value * 0.0096f - 273.15f;
                        vmin = (center_value - 1.5 + 273.15f) / 0.0096f;
                        vmax = (center_value + 1.5 + 273.15f) / 0.0096f;
                    } else {
                        double center_value = matResized.get(matResized.rows() / 2, matResized.cols() / 2)[0];
                        double center_celsius = (((center_value - 8192) + kelvin) * 0.01f) - 273.15f;
                        vmin = (((center_celsius - 1.5 + 273.15f) * 1.0f / 0.01f) - kelvin + 8192);
                        vmax = (((center_celsius + 1.5 + 273.15f) * 1.0f / 0.01f) - kelvin + 8192);
                    }
                } else {
                    if (AppResultReceiver.touchPointThermalFormula == 35) {
                        double center_value = matResized.get(matResized.rows() / 2, matResized.cols() / 2)[0];
                        center_value = center_value * 0.0096f - 273.15f;
                        vmin = (center_value - 0.5 + 273.15f) / 0.0096f;
                        vmax = (center_value + 0.5 + 273.15f) / 0.0096f;
                    } else {
                        double center_value = matResized.get(matResized.rows() / 2, matResized.cols() / 2)[0];
                        double center_celsius = (((center_value - 8192) + kelvin) * 0.01f) - 273.15f;
                        vmin = (((center_celsius - 0.5 + 273.15f) * 1.0f / 0.01f) - kelvin + 8192);
                        vmax = (((center_celsius + 0.5 + 273.15f) * 1.0f / 0.01f) - kelvin + 8192);
                    }
                }

                if (AppResultReceiver.touchPointThermalFormula == 35) {
                    double center_value = matResized.get((int) (matResized.rows() * AppResultReceiver.touchPointYp), (int) (matResized.cols() * AppResultReceiver.touchPointXp))[0];

                    //20200819
                    AppResultReceiver.touchPointThermalCelsius = center_value * 0.0096f - 273.15f;

                    //20200825
//                    AppResultReceiver.touchPointThermalCelsius = (((float)(center_value / 100) + ((float)(center_value % 100) *0.01f))-273.15);

                } else {
                    double center_value = matResized.get((int) (matResized.rows() * AppResultReceiver.touchPointYp), (int) (matResized.cols() * AppResultReceiver.touchPointXp))[0];

                    //20190909
                    AppResultReceiver.touchPointThermalCelsius = (((center_value - 8192) + kelvin) * 0.01f) - 273.15f;

                    //20200803
//                    double calibrated_kelvin = (8192 - (kelvin * ((center_value - 8192) / (center_value - kelvin))));
//                    AppResultReceiver.touchThermalPointCelsius = (calibrated_kelvin * 0.01f - 273.15f) * 0.01f + AppResultReceiver.touchThermalOffsetCelsius;

                    //compensated_tempC = 0.0217 * (frameBuffer[i] - 8192) +  (k_temp/100) - 272.15)
                    //AppResultReceiver.touchThermalPointCelsius =  0.0217 * ((center_value - 8192.0) +  (kelvin/100.0)) - 273.155;
                    //Log.d("", "touchThermalPointCelsius:"+AppResultReceiver.touchThermalPointCelsius);
                }

                Mat matDisplay = new Mat();
                double alpha = 255.0 / (vmax - vmin);
                matResized.convertTo(matDisplay, CV_8U, alpha, -vmin * alpha);

                //Imgproc.applyColorMap(matDepth, matFinal, Imgproc.COLORMAP_RAINBOW);

                Imgproc.cvtColor(matDisplay, matFinal, Imgproc.COLOR_GRAY2RGB);
                Core.LUT(matFinal, AppResultReceiver.getLutThermalColorMap(AppResultReceiver.mMainActivity), matFinal);
                matDisplay.release();
                //Log.e(TAG, "ThermalImage w:"+matFinal.cols()+", h:"+matFinal.rows());

                try {
                    if (mUSBCameraHelper != null && mUSBCameraHelper.doCapture) {
//                        Log.e(TAG, "Time Thermal onCapturing: " + AppResultReceiver.lastThermalSnapshotTimems);

                        if (AppResultReceiver.lastThermalSnapshotMat == null) {
                            AppResultReceiver.lastThermalSnapshotMat = new Mat();
                        } else {
                            try {
                                AppResultReceiver.lastThermalSnapshotMat.release();
                            } catch (Exception ex) {
                            }
                            AppResultReceiver.lastThermalSnapshotMat = null;
                            AppResultReceiver.lastThermalSnapshotMat = new Mat();
                        }


                        long timeoffset = AppResultReceiver.lastThermalSnapshotTimems - AppResultReceiver.lastPicSnapshotTimems;
                        if (AppResultReceiver.lastPicSnapshotTimems != 0 && (Math.abs(timeoffset) < 60 || (timeoffset > 100))) {
                            Log.d(TAG, "Thermal image captured");

                            if (Build.MODEL.endsWith("MPD100") || Build.MODEL.endsWith("MPD500"))
                                Imgproc.resize(matFinal, AppResultReceiver.lastThermalSnapshotMat, new org.opencv.core.Size(120, 160), 0, 0, Imgproc.INTER_LINEAR);
                            else {
                                rotateMatCW(matFinal, AppResultReceiver.lastThermalSnapshotMat, -AppResultReceiver.thermalImageRotateAngle);
                                Imgproc.resize(AppResultReceiver.lastThermalSnapshotMat, AppResultReceiver.lastThermalSnapshotMat, new org.opencv.core.Size(120, 160), 0, 0, Imgproc.INTER_LINEAR);
                            }
                            Imgproc.cvtColor(AppResultReceiver.lastThermalSnapshotMat, AppResultReceiver.lastThermalSnapshotMat, Imgproc.COLOR_RGB2BGR);
                            Imgcodecs.imwrite(filePath, AppResultReceiver.lastThermalSnapshotMat);
                            Log.d(TAG, "last Thermal size raw.rows:"+AppResultReceiver.lastThermalSnapshotMat.rows()+",raw.cols:"+AppResultReceiver.lastThermalSnapshotMat.cols());

                            if (AppResultReceiver.dataEncrypt) {
                                try {
                                    Thread.sleep(100);
                                }catch (Exception ex){}
                                FileHelper.overwriteFileSecret(filePath);
                            }

                            Mat matResizedTemperature = new Mat();
                            if (AppResultReceiver.touchPointThermalFormula == 35) {
                                matResized.convertTo(matResizedTemperature, CV_32FC1, 0.0095f, -272.15f);
                            } else {
                                matResized.convertTo(matResizedTemperature, CV_32FC1, 1f, -8192 + kelvin);
                                matResizedTemperature.convertTo(matResizedTemperature, CV_32FC1, 0.01f, -273.15f);
                            }

                            float[] aResizedTemperature = new float[(int) (matResizedTemperature.total() * matResizedTemperature.channels())];
                            matResizedTemperature.get(0, 0, aResizedTemperature);
                            saveToFile(aResizedTemperature, filePath.substring(0, filePath.lastIndexOf(".png")) + ".raw");
                            matResizedTemperature.release();

                            Log.e(TAG, "Time Thermal saveToFile: " + AppResultReceiver.lastThermalSnapshotTimems);

                            try {
                                Thread.sleep(300);
                                AppResultReceiver.lastThermalSnapshotMat.release();
                            } catch (Exception ex) {
                            }

                            if (AppResultReceiver.dataEncrypt) {
                            //    FileHelper.rewriteFileSecret(filePath);

//                                InputStream ins = null;
//                                OutputStream outfile = null;
//                                try {
//                                    ins = new FileInputStream(filePath);
//
//                                    if (filePath.toLowerCase().endsWith(".png")) {
//                                        byte[] targetArray = IOUtils.toByteArray(ins);
//                                        String chs = ServiceHelper.getSerialNumber("0");
//                                        int chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
//                                        targetArray[1] = 0x10;
//                                        targetArray[2] = (byte) chn;
//                                        ins = new ByteArrayInputStream(targetArray);
//                                    } else if (filePath.toLowerCase().endsWith(".jpg")) {
//                                        byte[] targetArray = IOUtils.toByteArray(ins);
//                                        String chs = ServiceHelper.getSerialNumber("0");
//                                        int chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
//                                        targetArray[0xb1] = (byte) 0x10;
//                                        targetArray[0xb2] = (byte) chn;
//                                        ins = new ByteArrayInputStream(targetArray);
//                                    }
//
//                                    try {
//                                        outfile = new FileOutputStream(filePath);
//                                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
//                                        int read;
//
//                                        while ((read = ins.read(buffer)) != -1) {
//                                            outfile.write(buffer, 0, read);
//                                        }
//
//                                        outfile.flush();
//                                    } catch (Exception e) {
//                                    } finally {
//                                        try {
//                                            if (outfile != null) {
//                                                outfile.close();
//                                            }
//                                        } catch (Exception e) {
//                                        }
//                                    }
//
//                                } catch (Exception e) {
//                                } finally {
//                                    try {
//                                        if (ins != null) {
//                                            ins.close();
//                                        }
//                                    } catch (Exception e) {
//                                    }
//                                }
                            }
                            AppResultReceiver.lastThermalSnapshotMat = null;
                            AppResultReceiver.lastThermalSnapshotTimems = 0;
                            mUSBCameraHelper.doCapture = false;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                org.opencv.android.Utils.matToBitmap(matFinal, mBitmap);
                AppResultReceiver.mMainActivity.updateTextureView(textureView, mBitmap);
//                Canvas canvas = null;
//                try {
//                    if (textureView!=null)
//                        canvas = textureView.lockCanvas();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//                try {
//                    if (mBitmap!=null && canvas!=null)
//                        canvas.drawBitmap(mBitmap, 0, 0, null);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//                try {
//                    if (textureView!=null && canvas!=null)
//                        textureView.unlockCanvasAndPost(canvas);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    matSrc.release();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                matSrc = null;
                try {
                    matResized.release();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                matResized = null;
                try {
                    matFinal.release();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                matFinal = null;
//                try {
//                    mBitmap.recycle();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//                mBitmap = null;
//                try {
//                    writingMat.release();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//                writingMat = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                buffer.clear();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            try {
                if (mUSBCameraHelper.semaphore != null) {
                    synchronized (mUSBCameraHelper.semaphore) {
                        mUSBCameraHelper.semaphore.release();
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            mUSBCameraHelper = null;
        }
    }

    private void rotateMatCW(Mat src, Mat dst, double deg) {
        if (deg == 270 || deg == -90) {
            // Rotate clo rotateMatCW(matResized,matResized,-90);ckwise 270 degrees
            Core.transpose(src, dst);
            Core.flip(dst, dst, 0);
        } else if (deg == 180 || deg == -180) {
            // Rotate clockwise 180 degrees
            Core.flip(src, dst, -1);
        } else if (deg == 90 || deg == -270) {
            // Rotate clockwise 90 degrees
            Core.transpose(src, dst);
            Core.flip(dst, dst, 1);
        } else if (deg == 360 || deg == 0 || deg == -360) {
            if (src != dst) {
                src.copyTo(dst);
            }
        } else {
//        cv::Point2f src_center(src.cols / 2.0F, src.rows / 2.0F);
//        cv::Mat rot_mat = getRotationMatrix2D(src_center, 360 - deg, 1.0);
//        warpAffine(src, dst, rot_mat, src.size());
        }
    }

    private void saveToFile(float[] floatarray, String filePath) {
        FileOutputStream output = null;
        DataOutputStream doutput = null;
        try {
            output = new FileOutputStream(filePath);
            doutput = new DataOutputStream(output);
            for (int i = 0; i < floatarray.length; ++i)
                doutput.writeFloat(floatarray[i]);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                doutput.close();
            } catch (Exception e) {
            }
            try {
                output.close();
            } catch (Exception e) {
            }
        }
    }


//    private void showY16(final ByteBuffer frame, TextureView textureView) {
//                double vmin, vmax, alpha, center_value, center_celsius;
//                //vmin=7583;
//                //vmax=8083;
//                //float fpatemp_f = fpatemp * 1.8f - 459.67f;
//                //value_min = ((0.05872 * (float)minValue - 472.22999f + fpatemp_f));
//                // value_min= (value_min - 32.0f) / 1.8f;
//
//
//                //value_min = 0.0217 *((float)minValue - 8292)+ fpatemp - 273.16;
//                //float celsius = (((value - 8192) + kelvin) * 0.01f) - 273.15f;
//                // value = ((celsius + 273.15f) / 0.01f) -kelvin +8192
//                // 27~38, 30~36
//                if (appContext->adaptive_type==APT_AUTO)
//                {
//                    minMaxLoc(tmp, &vmin, &vmax);
//                }
//                else if (appContext->adaptive_type==APT_SETTING)
//                {
//                    if (appContext->temperature_low<0 || appContext->temperature_high<0)
//                        minMaxLoc(tmp, &vmin, &vmax);
//                    if (appContext->temperature_low>-1)
//                        vmin = (((appContext->temperature_low+ 273.15f)*1.0f / 0.01f) -kelvin +8192);
//                    if (appContext->temperature_high>-1)
//                        vmax = (((appContext->temperature_high+ 273.15f)*1.0f / 0.01f) -kelvin +8192);
//                }
//                else if (appContext->adaptive_type==APT_CENTER)
//                {
//                    center_value = tmp.at<uint16_t>(tmp.rows/2,tmp.cols/2);
//                    center_celsius = (((center_value - 8192) + kelvin) * 0.01f) - 273.15f;
//                    vmin = (((center_celsius-1+ 273.15f)*1.0f / 0.01f) -kelvin +8192);
//                    vmax = (((center_celsius+1+ 273.15f)*1.0f / 0.01f) -kelvin +8192);
//                }
//                else if (appContext->adaptive_type==APT_CENTER_HALF)
//                {
//                    center_value = tmp.at<uint16_t>(tmp.rows/2,tmp.cols/2);
//                    center_celsius = (((center_value - 8192) + kelvin) * 0.01f) - 273.15f;
//                    vmin = (((center_celsius-0.5+ 273.15f)*1.0f / 0.01f) -kelvin +8192);
//                    vmax = (((center_celsius+0.5+ 273.15f)*1.0f / 0.01f) -kelvin +8192);
//                }
//                tmp.convertTo(tmp, CV_8U, 255.0 / (vmax - vmin),-vmin * alpha);
//
//                _image= Mat(tmp.size(),CV_8UC3);
//                //applyColorMap(tmp, _image, COLORMAP_JET);
//                //applyColorMap(tmp, _image, COLORMAP_INFERNO);
//                applyColorMap(tmp, _image, color_map);
//    }
}

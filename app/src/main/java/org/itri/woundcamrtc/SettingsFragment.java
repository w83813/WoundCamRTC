package org.itri.woundcamrtc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import org.itri.woundcamrtc.R;

import static android.hardware.Camera.Parameters.FLASH_MODE_AUTO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_INFINITY;
import static android.hardware.Camera.Parameters.SCENE_MODE_AUTO;
import static android.hardware.Camera.Parameters.WHITE_BALANCE_AUTO;
import static android.hardware.Camera.Parameters.WHITE_BALANCE_INCANDESCENT;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
  public static final String KEY_PREF_PREV_SIZE = "preview_size";
  public static final String KEY_PREF_PIC_SIZE = "picture_size";
  public static final String KEY_PREF_VIDEO_SIZE = "video_size";
  public static final String KEY_PREF_FLASH_MODE = "flash_mode";
  public static final String KEY_PREF_FOCUS_MODE = "focus_mode";
  public static final String KEY_PREF_WHITE_BALANCE = "white_balance";
  public static final String KEY_PREF_SCENE_MODE = "scene_mode";
  public static final String KEY_PREF_GPS_DATA = "gps_data";
  public static final String KEY_PREF_EXPOS_COMP = "exposure_compensation";
  public static final String KEY_PREF_JPEG_QUALITY = "jpeg_quality";
  public static final String KEY_PREF_VIDEO_BITRATE = "video_bitrate";

  public static final String KEY_PREF_UPLOAD_RAWFILE = "upload_rawfile";
  public static final String KEY_PREF_DOWNSAMPLE_TYPE = "downsample_type";
  public static final String KEY_PREF_FOCUS_AREA = "focus_area";
  public static final String KEY_PREF_THERMAL_TYPE = "thermal_type";
  public static final String KEY_PREF_THERMAL_BOARD_IP = "thermalBoardIP";

  public static final String KEY_PREF_SHUT_BTN = "shut_btn";
  public static final String KEY_PREF_DEBUG_TAG = "debug_tag";
  public static final String KEY_PREF_DEBUG_LEVEL = "debug_level";
  public static final String KEY_PREF_APP_LANGUAGE = "app_language";

  public static final String KEY_PREF_FILE_ROOT_PATH = "file_root_path";
  static Camera mCamera;
  static Camera.Parameters mParameters;
  public static Boolean openView = false;


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    openView = true;
    View view = super.onCreateView(inflater, container, savedInstanceState);
    view.bringToFront();
    view.setBackgroundColor(getResources().getColor(android.R.color.black));
    addPreferencesFromResource(R.xml.preferences);

    //loadSupportedPreviewSize();
    //loadSupportedPictureSize();
    //loadSupportedVideoeSize();
    loadSupportedFlashMode();
    loadSupportedFocusMode();
    loadSupportedWhiteBalance();
    //loadSupportedSceneMode();
    loadSupportedExposeCompensation();
    loadSupportedVideoBitrate();
    loadSupportedFocusArea();

    SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getActivity());
    setDefault(prefs);

    initSummary(getPreferenceScreen());

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    getPreferenceScreen().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    try {
      AppResultReceiver.mMainActivity.returnFromSetting();
      AppResultReceiver.mMainActivity.button_bodyPart.setVisibility(View.VISIBLE);
      AppResultReceiver.mMainActivity.button_ownerId.setVisibility(View.VISIBLE);
    } catch (Exception e) {
    }
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    updatePrefSummary(findPreference(key));
    switch (key) {
      case KEY_PREF_PREV_SIZE:
        setPreviewSize(sharedPreferences.getString(key, ""));
        break;
      case KEY_PREF_PIC_SIZE:
        setPictureSize(sharedPreferences.getString(key, ""));
        break;
      case KEY_PREF_FOCUS_MODE:
        setFocusMode(sharedPreferences.getString(key, FOCUS_MODE_CONTINUOUS_PICTURE));
        break;
      case KEY_PREF_FLASH_MODE:
        setFlashMode(sharedPreferences.getString(key, FLASH_MODE_AUTO));
        break;
      case KEY_PREF_WHITE_BALANCE:
        setWhiteBalance(sharedPreferences.getString(key, WHITE_BALANCE_AUTO));
        break;
      case KEY_PREF_SCENE_MODE:
        setSceneMode(sharedPreferences.getString(key, SCENE_MODE_AUTO));
        break;
      case KEY_PREF_EXPOS_COMP:
        setExposComp(sharedPreferences.getString(key, "0"));
        break;
      case KEY_PREF_JPEG_QUALITY:
        setJpegQuality(sharedPreferences.getString(key, getDefaultJpegQuality()));
        break;
      case KEY_PREF_GPS_DATA:
        setGpsData(sharedPreferences.getBoolean(key, false));
        break;
      case KEY_PREF_VIDEO_BITRATE:
        setVideoBitrate(sharedPreferences.getString(key, getDefaultVideoBitrate()));
        break;
      case KEY_PREF_UPLOAD_RAWFILE:
        setUploadRawfile(sharedPreferences.getString(key, "0"));
        break;
      case KEY_PREF_DOWNSAMPLE_TYPE:
        setDownsampleType(sharedPreferences.getString(key, "5"));
        break;
      case KEY_PREF_THERMAL_TYPE:
        setThermalType(sharedPreferences.getString(key, "0"));
        AppResultReceiver.mMainActivity.setThermalBoardParams();
        break;
      case KEY_PREF_SHUT_BTN:
        //AppResultReceiver.mMainActivity.setShutBtnParams();
        break;
      case KEY_PREF_THERMAL_BOARD_IP:
        AppResultReceiver.mMainActivity.showToast("Please restart APP.");
        break;
      case KEY_PREF_FOCUS_AREA:
        setFocusArea(sharedPreferences.getString(key, getDefaultFocusArea()));
        break;
      case KEY_PREF_DEBUG_TAG:
        setDebugTag(sharedPreferences.getString(key, ""));
        break;
      case KEY_PREF_DEBUG_LEVEL:
        setDebugLevel(sharedPreferences.getString(key, getDefaultDebugLevel()));
        break;

    }

    // mCamera.stopPreview();
    // mCamera.setParameters(mParameters);
    //mCamera.startPreview();
  }

  private static void initSummary(Preference pref) {
    if (pref instanceof PreferenceGroup) {
      PreferenceGroup prefGroup = (PreferenceGroup) pref;
      for (int i = 0; i < prefGroup.getPreferenceCount(); i++) {
        initSummary(prefGroup.getPreference(i));
      }
    } else {
      updatePrefSummary(pref);
    }
  }

  private static void updatePrefSummary(Preference pref) {
    if (pref instanceof ListPreference) {
      pref.setSummary(((ListPreference) pref).getEntry());
    } else if (pref instanceof EditTextPreference) {
      pref.setSummary(((EditTextPreference) pref).getText());
    } else {
    }
  }

  private void stringListToListPreference(List<String> list, String key) {
    final CharSequence[] charSeq = list.toArray(new CharSequence[list.size()]);
    ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(key);
    listPref.setEntries(charSeq);
    listPref.setEntryValues(charSeq);
  }

  private void cameraSizeListToListPreference(List<Camera.Size> list, String key) {
    List<String> stringList = new ArrayList<>();
    for (Camera.Size size : list) {
      String stringSize = size.width + "x" + size.height;
      stringList.add(stringSize);
    }
    stringListToListPreference(stringList, key);
  }

  public static void passCamera(Camera camera) {
    mCamera = camera;
    mParameters = camera.getParameters();
  }

  private void loadSupportedPreviewSize() {
    cameraSizeListToListPreference(mParameters.getSupportedPreviewSizes(), KEY_PREF_PREV_SIZE);
  }

  private void loadSupportedPictureSize() {
    cameraSizeListToListPreference(mParameters.getSupportedPictureSizes(), KEY_PREF_PIC_SIZE);
  }

  private void loadSupportedVideoeSize() {
    cameraSizeListToListPreference(mParameters.getSupportedVideoSizes(), KEY_PREF_VIDEO_SIZE);
  }

  private void loadSupportedFlashMode() {
    stringListToListPreference(mParameters.getSupportedFlashModes(), KEY_PREF_FLASH_MODE);
  }

  private void loadSupportedFocusMode() {
    stringListToListPreference(mParameters.getSupportedFocusModes(), KEY_PREF_FOCUS_MODE);
  }

  private void loadSupportedWhiteBalance() {
    stringListToListPreference(mParameters.getSupportedWhiteBalance(), KEY_PREF_WHITE_BALANCE);
  }

  private void loadSupportedSceneMode() {
    stringListToListPreference(mParameters.getSupportedSceneModes(), KEY_PREF_SCENE_MODE);
  }

  private void loadSupportedFocusArea() {
    List<String> comp = new ArrayList<>();
    comp.add("point");
    comp.add("shut");
    stringListToListPreference(comp, KEY_PREF_FOCUS_AREA);
  }

  private void loadSupportedShutBtn() {
    List<String> comp = new ArrayList<>();
    comp.add("c");
    comp.add("l");
    comp.add("r");
    stringListToListPreference(comp, KEY_PREF_SHUT_BTN);
  }

  private void loadSupportedVideoBitrate() {
    List<String> comp = new ArrayList<>();
    comp.add("4320");
    comp.add("2160");
    comp.add("720");
    comp.add("360");
    comp.add("90");
    stringListToListPreference(comp, KEY_PREF_VIDEO_BITRATE);
  }

  private void loadSupportedExposeCompensation() {
    int minExposComp = mParameters.getMinExposureCompensation();
    int maxExposComp = mParameters.getMaxExposureCompensation();
    List<String> exposComp = new ArrayList<>();
    for (int value = minExposComp; value <= maxExposComp; value++) {
      exposComp.add(Integer.toString(value));
    }
    stringListToListPreference(exposComp, KEY_PREF_EXPOS_COMP);
  }

  private static String getDefaultPreviewSize() {
    Camera.Size previewSize = mParameters.getPreviewSize();
    return previewSize.width + "x" + previewSize.height;
  }

  private static String getDefaultPictureSize() {
    Camera.Size pictureSize = mParameters.getPictureSize();
    return pictureSize.width + "x" + pictureSize.height;
  }

  private static String getDefaultVideoSize() {
    Camera.Size VideoSize = mParameters.getPreferredPreviewSizeForVideo();
    return VideoSize.width + "x" + VideoSize.height;
  }

  private static String getDefaultFocusMode() {
//        List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
//        if (supportedFocusModes.contains("continuous-picture")) {
//            return "continuous-picture";
//        }
//        return "continuous-video";
    return FOCUS_MODE_CONTINUOUS_PICTURE;
  }

  private static String getDefaultJpegQuality() {
    int val = mParameters.getJpegQuality();
    return Integer.toString(val);
  }

  private static String getDefaultVideoBitrate() {
    return "360";
  }

  private static String getDefaultFocusArea() {
    return "shut";
  }

  private static String getDefaultShutBtn() {
    return "c";
  }

  private static String getDefaultDebugLevel() {
    return "0";
  }

  private static String getDefaultAppLanguage() {
    return "en";
  }

  private static void setPreviewSize(String value) {
    String[] split = value.split("x");
    mParameters.setPreviewSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
  }

  private static void setPictureSize(String value) {
    String[] split = value.split("x");
    mParameters.setPictureSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
  }

  private static void setFocusMode(String value) {
    mParameters.setFocusMode(value);
  }

  private static void setFlashMode(String value) {
    mParameters.setFlashMode(value);
  }

  private static void setWhiteBalance(String value) {

    //AWB_MODE_INCANDESCENT：用於室內白zhi燈的白平衡設置，色溫大概2700K。
    //AWB_MODE_WARM_FLUORESCENT：用於熒光燈的白平衡設置，色溫大概3000K。
    //AWB_MODE_FLUORESCENT：用於熒光燈的白平衡設置，色溫大概5000K。
    //AWB_MODE_DAYLIGHT：用于晴天的白平衡設置，色溫大概5500K。
    //AWB_MODE_CLOUDY_DAYLIGHT：用於陰天的白平衡設置，色溫大概6500K。
    //AWB_MODE_SHADE：用於陰影處的白平衡設置，色溫大概7500K。
    //AWB_MODE_TWILIGHT：用於日出/日落的白平衡設置，色溫大概15000K。

    mParameters.setWhiteBalance(value);
  }

  private static void setSceneMode(String value) {
    mParameters.setSceneMode(value);
  }

  private static void setExposComp(String value) {
    mParameters.setExposureCompensation(Integer.parseInt(value));
  }

  private static void setJpegQuality(String value) {
    mParameters.setJpegQuality(Integer.parseInt(value));
  }

  private static void setVideoBitrate(String value) {
    //mParameters.setJpegQuality(Integer.parseInt(value));
  }


  private static void setGpsData(Boolean value) {
    if (value.equals(false)) {
      mParameters.removeGpsData();
    }
  }


  private static void setFocusArea(String value) {
    AppResultReceiver.FOCUS_AREA_TYPE = value;

    try {
      if (AppResultReceiver.FOCUS_AREA_TYPE.equals("point")) {
        AppResultReceiver.mMainActivity.setFocusedBorder(-1, -1, true);
        AppResultReceiver.mMainActivity.setFocusingBorder(-1, -1, true);
      } else {
        AppResultReceiver.mMainActivity.setFocusedBorder(-1, -1, false);
        AppResultReceiver.mMainActivity.setFocusingBorder(-1, -1, false);
      }
    } catch (Exception ex) {
    }
  }

  private static void setDebugTag(String value) {
    AppResultReceiver.DEBUG_TAG = value;
  }

  private static void setUploadRawfile(String value) {
    try {
      AppResultReceiver.uploadRawfile = Integer.parseInt(value);
    } catch (Exception e) {
      AppResultReceiver.uploadRawfile = 0;
    }
  }

  private static void setDownsampleType(String value) {
    try {
      AppResultReceiver.grabcutDownsampleType = Integer.parseInt(value);
    } catch (Exception e) {
      AppResultReceiver.grabcutDownsampleType = 8;
    }
  }

  private static void setThermalType(String value) {
    try {
      AppResultReceiver.touchPointThermalDisplay = Integer.parseInt(value);
    } catch (Exception e) {
      AppResultReceiver.touchPointThermalDisplay = 0;
    }
  }

  private static void setDebugLevel(String value) {
    try {
      AppResultReceiver.DEBUG_LEVEL = Integer.parseInt(value);
    } catch (Exception e) {
      AppResultReceiver.DEBUG_LEVEL = AppResultReceiver.DEBUG_IDLE;
    }
  }

  public static void setDefault(SharedPreferences sharedPrefs) {
    String valPreviewSize = sharedPrefs.getString(KEY_PREF_PREV_SIZE, null);
    if (valPreviewSize == null) {
      SharedPreferences.Editor editor = sharedPrefs.edit();
      try {
        editor.putString(KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
      } catch (Exception ex) {
      }
      try {
        editor.putString(KEY_PREF_PIC_SIZE, getDefaultPictureSize());
      } catch (Exception ex) {
      }
      try {
        editor.putString(KEY_PREF_VIDEO_SIZE, getDefaultVideoSize());
      } catch (Exception ex) {
      }
      editor.putString(KEY_PREF_FLASH_MODE, FLASH_MODE_AUTO);
      editor.putString(KEY_PREF_FOCUS_MODE, FOCUS_MODE_CONTINUOUS_PICTURE);
      editor.putString(KEY_PREF_WHITE_BALANCE, WHITE_BALANCE_AUTO);
      editor.putString(KEY_PREF_SCENE_MODE, SCENE_MODE_AUTO);
      editor.putString(KEY_PREF_GPS_DATA, "false");
      editor.putString(KEY_PREF_EXPOS_COMP, "0");
      try {
        editor.putString(KEY_PREF_JPEG_QUALITY, getDefaultJpegQuality());
      } catch (Exception ex) {
      }
      editor.putString(KEY_PREF_VIDEO_BITRATE, getDefaultVideoBitrate());
      editor.putString(KEY_PREF_FOCUS_AREA, getDefaultFocusArea());
      editor.putString(KEY_PREF_UPLOAD_RAWFILE, "0");
      editor.putString(KEY_PREF_DOWNSAMPLE_TYPE, "5");
      editor.putString(KEY_PREF_THERMAL_TYPE, "0");
      editor.putString(KEY_PREF_SHUT_BTN, "c");
      editor.putString(KEY_PREF_THERMAL_BOARD_IP, "192.168.1.101");
      editor.putString(KEY_PREF_DEBUG_TAG, "12345");
      editor.putString(KEY_PREF_DEBUG_LEVEL, getDefaultDebugLevel());
      editor.putString(KEY_PREF_APP_LANGUAGE, getDefaultAppLanguage());
      editor.apply();
    }
  }
}

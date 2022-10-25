package org.itri.woundcamrtc.jsbridge;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.helper.DBTableHelper;
import org.json.JSONException;
import org.json.JSONObject;


public class BridgeImpl implements IBridge {

    protected static DBTableHelper database = null;
    protected static ResultReceiver mReceiver = null;

    protected void finalize() throws Throwable {
        try {
            database.close();
            database = null;
        } finally {
            super.finalize();
        }
    }

    public static void setReceiver(ResultReceiver receiver) {
        mReceiver = receiver;
    }

    private static JSONObject getJSONObject(int code, String msg, JSONObject result) {
        JSONObject object = new JSONObject();
        try {
            object.put("code", code);
            object.put("msg", msg);
            object.putOpt("result", result);
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void closeApp(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
        try {
            Bundle mBundle = new Bundle();
            mBundle.putString(AppResultReceiver.KEY_RECEIVER_MESSAGE, "");
            mBundle.putInt(AppResultReceiver.KEY_RECEIVER_FLAG, 1);
            mReceiver.send(AppResultReceiver.JAVASCRIPT_CLOSE_APP, mBundle);
//            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @JavascriptInterface
    public static void pageReady(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
        Bundle mBundle = new Bundle();
        mBundle.putString(AppResultReceiver.KEY_RECEIVER_MESSAGE, "");
        mBundle.putInt(AppResultReceiver.KEY_RECEIVER_FLAG, 1);
        mReceiver.send(AppResultReceiver.JAVASCRIPT_WEBVIEW_PAGEREADY, mBundle);
    }

    @JavascriptInterface
    public static void pageEvent(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
        Bundle mBundle = new Bundle();
        try {
            mBundle.putString(AppResultReceiver.KEY_RECEIVER_MESSAGE, param.get("msg").toString());
        } catch (JSONException e) {
            e.printStackTrace();
            mBundle.putString(AppResultReceiver.KEY_RECEIVER_MESSAGE, "");
        }
        mBundle.putInt(AppResultReceiver.KEY_RECEIVER_FLAG, 1);
        mReceiver.send(AppResultReceiver.JAVASCRIPT_WEBVIEW_PAGEEVENT, mBundle);
    }


    public static void uploadCurData(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
        Bundle mBundle = new Bundle();
        mBundle.putString(AppResultReceiver.KEY_RECEIVER_MESSAGE, param.toString());
        mBundle.putInt(AppResultReceiver.KEY_RECEIVER_FLAG, 1);
        mReceiver.send(AppResultReceiver.JAVASCRIPT_WEBVIEW_UPLOADDATA, mBundle);
        if (null != callback) {
            try {
                JSONObject object = new JSONObject();
                object.put("key", "");
                callback.apply(JSBridgeCallback.CALLBACK.ONESHOT, getJSONObject(0, "ok", object));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void getLastKeyboardId(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
        Bundle mBundle = new Bundle();
        mBundle.putString(AppResultReceiver.KEY_RECEIVER_MESSAGE, param.toString());
        mBundle.putInt(AppResultReceiver.KEY_RECEIVER_FLAG, 1);
        mReceiver.send(AppResultReceiver.JAVASCRIPT_LAST_KEYBOARD_ID, mBundle);
    }

    public static void getAllUserData(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
        Bundle mBundle = new Bundle();
        mBundle.putString(AppResultReceiver.KEY_RECEIVER_MESSAGE, "");
        try {
            mBundle.putString(AppResultReceiver.KEY_RECEIVER_MESSAGE, param.getString("msg"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBundle.putInt(AppResultReceiver.KEY_RECEIVER_FLAG, 1);
        mReceiver.send(AppResultReceiver.JAVASCRIPT_GET_ALL_USERDATA, mBundle);
    }


    public static void loginServer(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
        Bundle mBundle = new Bundle();
        mBundle.putString(AppResultReceiver.KEY_RECEIVER_MESSAGE, param.toString());
        mBundle.putInt(AppResultReceiver.KEY_RECEIVER_FLAG, 1);
        mReceiver.send(AppResultReceiver.JAVASCRIPT_TESTING, mBundle);
    }


//    public static void set977(final WebView webView, final JSBridgeCallback callback, Object val1, Object val2, Object val3) {
//
//        if (null != callback) {
//            try {
//                JSONObject object = new JSONObject();
//                object.put("barClass", val1);
//                object.put("dataType", val2);
//                object.put("evlValue", val3);
//                callback.apply(JSBridgeCallback.CALLBACK.ONESHOT, getJSONObject(0, "ok", object));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }


    //    public static void deviceReady(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
//        String message = param.optString("msg");
//        if (null != callback) {
//            try {
//                JSONObject object = new JSONObject();
//                object.put("key1", "value1");
//                object.put("key2", "value2");
//                callback.apply(JSBridgeCallback.CALLBACK.ONESHOT, getJSONObject(0, "ok", object));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void openYahoo(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
//        String message = param.optString("msg");
//        if (null != callback) {
//            try {
//                goToUrl(webView, "http://www.yahoo.com.tw");
//                JSONObject object = new JSONObject();
//                object.put("key1", "toast1");
//                object.put("key2", "toast2");
//                callback.apply(JSBridgeCallback.CALLBACK.ONESHOT, getJSONObject(0, "ok", object));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private static void goToUrl(final WebView webView, String url) {
//        Intent intent = new Intent();
//        intent.putExtra("message", url);
//        intent.setAction("HEALTHCARE.REDIRECT_URL");
//        webView.getContext().sendBroadcast(intent);
//    }
//
    public static void showToast(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
        String message = param.optString("msg");
        Toast.makeText(webView.getContext(), message, Toast.LENGTH_LONG).show();
        if (null != callback) {
            try {
                JSONObject object = new JSONObject();
                object.put("key1", "toast1");
                object.put("key2", "toast2");
                callback.apply(JSBridgeCallback.CALLBACK.ONESHOT, getJSONObject(0, "ok", object));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
//
//
//    public static void testLoadingThread(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
//
//        final NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(webView.getContext());
//        if (mNfcAdapter == null) {
//            Toast.makeText(webView.getContext(), "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
//
//        } else if (!mNfcAdapter.isEnabled()) {
//            Toast.makeText(webView.getContext(), "Please turn on NFC.", Toast.LENGTH_LONG).show();
//        } else {
//
//            final Bundle opts = new Bundle();
//            opts.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 500);
//
//            int flags = NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
//            flags |= NfcAdapter.FLAG_READER_NFC_A;
//            final int finalFlags = flags;
//
//            final ProgressDialog progressDialog = progressDialog(webView.getContext());
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        // wait for dialog show
//                        while (!progressDialog.isShowing()) {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                break;
//                            }
//                        }
//
//                        final Tag[] aa = {null};
//                        mNfcAdapter.enableReaderMode((Activity) webView.getContext(), new NfcAdapter.ReaderCallback() {
//                            @Override
//                            public void onTagDiscovered(Tag tag) {
//                                aa[0] = tag;
//                            }
//                        }, finalFlags, opts);
//
//                        // do my job
//                        int i = 10;
//                        while (i <= 100) {
//                            try {
//                                if (!progressDialog.isShowing()) {
//                                    break;
//                                }
//                                Thread.sleep(500);//线程休眠500毫秒
//                                if (aa[0]!=null) {
//                                    String tagId= ByteArrayToHexString(aa[0].getId());
//                                    JSONObject object = new JSONObject();
//                                    object.put("key", tagId);
//                                    if (null != callback)
//                                        callback.apply(JSBridgeCallback.CALLBACK.RECEIVING, getJSONObject(0, "ok", object));
//                                    aa[0]=null;
//                                }
//                                progressDialog.incrementProgressBy(1);//每次增加1
//                                i++;
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        progressDialog.dismiss();
//
//                        mNfcAdapter.disableReaderMode((Activity) webView.getContext());
//
//                        JSONObject object = new JSONObject();
//                        object.put("key", "");
//                        if (null != callback)
//                            callback.apply(JSBridgeCallback.CALLBACK.STOP, getJSONObject(0, "ok", object));
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            progressDialog.show();
//        }
//    }
//
//    public static void testNFCThread(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
//
//        final NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(webView.getContext());
//        if (mNfcAdapter == null) {
//            Toast.makeText(webView.getContext(), "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
//        } else if (!mNfcAdapter.isEnabled()) {
//            Toast.makeText(webView.getContext(), "Please turn on NFC.", Toast.LENGTH_LONG).show();
//        } else {
//            final Bundle opts = new Bundle();
//            opts.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 500);
//
//            int flags = NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
//            flags |= NfcAdapter.FLAG_READER_NFC_A;
//            final int finalFlags = flags;
//
//            final ProgressDialog progressDialog = progressDialog(webView.getContext());
//            progressDialog.setTitle("正在讀取中...");
//            progressDialog.setMessage("請等待3秒...");
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        // wait for dialog show
//                        while (!progressDialog.isShowing()) {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                break;
//                            }
//                        }
//
//                        final Tag[] tags = {null};
//                        mNfcAdapter.enableReaderMode((Activity) webView.getContext(), new NfcAdapter.ReaderCallback() {
//                            @Override
//                            public void onTagDiscovered(Tag tag) {
//                                tags[0] = tag;
//                            }
//                        }, finalFlags, opts);
//
//                        // do my job
//                        int i = 10;
//                        while (i <= 1000) {
//                            try {
//                                if (!progressDialog.isShowing()) {
//                                    break;
//                                }
//                                Thread.sleep(500);//线程休眠500毫秒
//                                if (tags[0]!=null) {
//                                    progressDialog.setMessage("請等待"+i+"秒...");
//                                    String tagId= ByteArrayToHexString(tags[0].getId());
//                                    JSONObject object = new JSONObject();
//                                    object.put("key", tagId);
//                                    if (null != callback)
//                                        callback.apply(JSBridgeCallback.CALLBACK.RECEIVING, getJSONObject(0, "ok", object));
//                                    tags[0]=null;
//                                }
////                                progressDialog.incrementProgressBy(1);//每次增加1
//                                i++;
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        progressDialog.dismiss();
//                        mNfcAdapter.disableReaderMode((Activity) webView.getContext());
//
//                        JSONObject object = new JSONObject();
//                        object.put("key", "");
//                        if (null != callback)
//                            callback.apply(JSBridgeCallback.CALLBACK.STOP, getJSONObject(0, "ok", object));
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            progressDialog.show();
//        }
//    }
//
//    public static ProgressDialog progressDialog(final Context context) {
//        final ProgressDialog progressDialog = new ProgressDialog(context);//获得ProgressDialog对象
//        progressDialog.setTitle("正在网络请求...");
////设置进度对话框样式
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressDialog.setMax(100);//设置最大进度
//        progressDialog.setProgress(10);//设置初始进度
////设置按钮
//        progressDialog.setButton( "隐藏", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                progressDialog.dismiss();
//            }
//        });
//        progressDialog.onStart();//启动进度条
//        return progressDialog;
//    }

    private static String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

//    public static void testDialogThread(final WebView webView, JSONObject param, final JSBridgeCallback callback) {
//        final AlertDialog alertDialog=showChangeLangDialog(webView.getContext());
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    // wait for dialog show
//                    while (!alertDialog.isShowing()) {
//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException e) {
//                            break;
//                        }
//                    }
//
//                    // do my job
//                    int i = 10;
//                    while (i <=100) {
//                        try {
//                            if (!alertDialog.isShowing()) {
//                                break;
//                            }
//                            Thread.sleep(500);
//                            JSONObject object = new JSONObject();
//                            object.put("key", "dialog" + i);
//                            if (null != callback)
//                                callback.apply(JSBridgeCallback.CALLBACK.RECEIVING, getJSONObject(0, "ok", object));
//                            i++;
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    JSONObject object = new JSONObject();
//                    object.put("key", "stop" + i);
//                    if (null != callback)
//                        callback.apply(JSBridgeCallback.CALLBACK.STOP, getJSONObject(0, "ok", object));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//        alertDialog.show();
//    }
//
//    public static AlertDialog showChangeLangDialog(final Context context) {
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//        LayoutInflater inflater = LayoutInflater.from(context);
//        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
//        dialogBuilder.setView(dialogView);
//
//        final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
//
//        dialogBuilder.setTitle("Custom dialog");
//        dialogBuilder.setMessage("Enter text below");
//        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//
//                String editText = edt.getText().toString().trim();
//
//                if (!editText.equals(""))
//                {
//                    dbConfigData = DBTableHelper.getInstance(context);
//                    dbConfigData.addOrUpdateRaw("setting","server_url", editText);
//                }
//
//                dialog.dismiss();
//            }
//        });
////        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
////            public void onClick(DialogInterface dialog, int whichButton) {
////                //pass
////            }
////        });
//
//        dbConfigData = DBTableHelper.getInstance(context);
//        List<Map<String, Object>> list = dbConfigData.querySQLData("setting","server_url");
//        if (list.size()>0) {
//            Map<String, Object> settingMap = list.get(0);
//            String serverUrl = (String) settingMap.get("_VALUE");
//            if (!serverUrl.equals(""))
//            {
//                edt.setText(serverUrl);
//            }
//        }
//        list.clear();
//        list=null;
//
//        AlertDialog alertDialog = dialogBuilder.create();
//        alertDialog.setCancelable(false);// 不可以用“返回键”取消
//        alertDialog.setContentView(dialogView, new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT));
////      loadDialog.show();
//        return alertDialog;
//    }


}
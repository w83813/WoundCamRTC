package com.github.yoojia.anyversion;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.widget.ProgressBar;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
class Downloads {

    static final Set<Long> KEEPS = new HashSet<>();

    public void destroy(Context context) {
        DownloadManager download = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        for (long id : KEEPS) {
            download.remove(id);
            KEEPS.remove(id);
        }
    }

//    AlertDialog dialog;

    public long submit(Context context, Version version) {
        final DownloadManager download = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(version.URL);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(version.name);
        // kernoli 2018/06/26
//        String ROOT_FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +File.separator+"WoundCam";
//        request.setDestinationInExternalFilesDir(context, ROOT_FOLDER_PATH, "");
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        final long id = download.enqueue(request);

        KEEPS.add(id);
        return id;
    }
}

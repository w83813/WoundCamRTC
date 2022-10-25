package org.itri.woundcamrtc.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class EmailHelper {

    private static String TAG = "EmailHelper";

    public static boolean sendEmailSimpleIntent(Context context, String to, String subject, String bodyText) {
        String mailto = "mailto:" + to +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(bodyText);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));
        try {
            context.startActivity(emailIntent);
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public static boolean sendEmailByIntent(Context context, String to, String subject, String text) {
        Log.i(TAG, "sendEmailByIntent()");
        String[] TO = {""};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
//        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            Log.i(TAG, "Finished sending email...");
            return true;
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}

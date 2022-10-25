package org.itri.woundcamrtc.ocr.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import org.itri.woundcamrtc.R;
import org.itri.woundcamrtc.ocr.ScannerActivity;
import org.itri.woundcamrtc.ocr.camera.CameraManager;

import static android.app.Activity.RESULT_OK;


public class ImageDialog extends Dialog {

    private Bitmap bmp;

    private String title;
    private String result;
    private Button btnOK;
    private Button btnCancel;
    private Activity mActivity;

    public ImageDialog(Activity activity) {
        super(activity);
        mActivity = activity;
    }

    public ImageDialog addBitmap(Bitmap bmp) {
        if (bmp != null) {
            this.bmp = bmp;
        }
        return this;
    }

    public ImageDialog addTitle(String title) {
        if (title != null) {
            this.title = title;
        }
        return this;
    }

    public void addResult(String param) {
        if (param != null) {
            this.result = param;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_image_dialog);

        ImageView imageView = (ImageView) findViewById(R.id.image_dialog_imageView);
        TextView textView = (TextView) findViewById(R.id.image_dialog_textView);

        if (bmp != null) {
            imageView.setImageBitmap(bmp);
        }

        if (title != null) {
            textView.setText(this.title);
        }

        btnOK = (Button) findViewById(R.id.btnOK);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOK.setEnabled(false);
                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_TEXT, result);
                mActivity.setResult(RESULT_OK, intent);
                mActivity.finish();
            }
        });

        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCancel.setEnabled(false);
                dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        if (bmp != null)
            bmp.recycle();
        bmp = null;
        System.gc();
        super.dismiss();
    }
}
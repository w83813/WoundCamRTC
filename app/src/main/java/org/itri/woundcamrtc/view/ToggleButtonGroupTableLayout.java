package org.itri.woundcamrtc.view;

import android.content.Context;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.itri.woundcamrtc.BodyPartActivity;
import org.itri.woundcamrtc.MainActivity;

/**
 * @author kevin Hsieh
 */
public class ToggleButtonGroupTableLayout extends TableLayout implements OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private RadioButton activeRadioButton;
    private BodyPartActivity bActivity;

    /**
     * @param context
     */
    public ToggleButtonGroupTableLayout(Context context) {
        super(context);

        // TODO Auto-generated constructor stub
    }

    /**
     * @param context
     * @param attrs
     */
    public ToggleButtonGroupTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onClick(View v) {
        final RadioButton rb = (RadioButton) v;
        if (activeRadioButton != null) {
            activeRadioButton.setChecked(false);
        }
        rb.setChecked(true);
        activeRadioButton = rb;

        bActivity.checkedBodyPartBtn(activeRadioButton.getText().toString());
    }


    /* (non-Javadoc)
     * @see android.widget.TableLayout#addView(android.view.View, int, android.view.ViewGroup.LayoutParams)
     */
    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setChildrenOnClickListener((TableRow) child);
    }


    /* (non-Javadoc)
     * @see android.widget.TableLayout#addView(android.view.View, android.view.ViewGroup.LayoutParams)
     */
    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        setChildrenOnClickListener((TableRow) child);
    }


    private void setChildrenOnClickListener(TableRow tr) {
        final int c = tr.getChildCount();
        for (int i = 0; i < c; i++) {
            final View v = tr.getChildAt(i);
            if (v instanceof RadioButton) {
                v.setOnClickListener(this);
            }
        }
    }

    public int getCheckedRadioButtonId() {
        if (activeRadioButton != null) {
            return activeRadioButton.getId();
        }

        return -1;
    }

    public void startBodyPartChoose(BodyPartActivity bodyPartActivity) {
        bActivity = bodyPartActivity;
    }
}

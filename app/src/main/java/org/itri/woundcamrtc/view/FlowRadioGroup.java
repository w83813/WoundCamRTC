package org.itri.woundcamrtc.view;

import android.content.Context;
import android.nfc.Tag;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kevin Hsieh
 */
public class FlowRadioGroup extends RadioGroup {
    List<List<View>> mAllViews;//儲存所有行的所有View
    List<Integer> mLineHeight;//儲存每一行的行高

    public FlowRadioGroup(Context context) {
        this(context, null);
    }

    public FlowRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);
    }

    /**
     * 獲取選中按鈕的索引,從開始, 未選中返回 -1
     */
    public int getCheckedRadioButtonIndex() {
        return indexOfChild(findViewById(getCheckedRadioButtonId()));
    }

    /**
     * 獲取選中按鈕的文字,未選中 返回 空字串
     */
    public String getCheckedRadioButtonText() {
        if (getCheckedRadioButtonId() == -1) {
            return "";
        }
        return ((RadioButton) findViewById(getCheckedRadioButtonId())).getText().toString();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int childCount = getChildCount();
        int x = 0;
        int y = 0;
        int row = 0;

        for (int index = 0; index < childCount; index++) {
            final View child = getChildAt(index);
            if (child.getVisibility() != View.GONE) {
                child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                // 此处增加onlayout中的换行判断，用于计算所需的高度
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                x += width;
                y = row * height + height;
                if (x > maxWidth) {
                    x = width;
                    row++;
                    y = row * height + height;
                }
            }
        }
        // 设置容器所需的宽度和高度
        setMeasuredDimension(maxWidth, y);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        int maxWidth = r - l;
        int x = 0;
        int y = 0;
        int row = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = this.getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                x += width;
                y = row * height + height;
                if (x > maxWidth) {
                    x = width;
                    row++;
                    y = row * height + height;
                }
                child.layout(x - width, y - height, x, y);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        check(getChildAt(0).getId());//預設按鈕
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getCheckedRadioButtonIndex());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCheckedStateForView(ss.checkIndex, true);
    }

    private void setCheckedStateForView(int checkIndex, boolean checked) {
        View checkedView = getChildAt(checkIndex);
        if (checkedView != null && checkedView instanceof RadioButton) {
            ((RadioButton) checkedView).setChecked(checked);
        }
    }

    public static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private int checkIndex;//選中按鈕的索引

        public SavedState(Parcelable parcel, int checkIndex) {
            super(parcel);
            this.checkIndex = checkIndex;
        }

        private SavedState(Parcel in) {
            super(in);
            checkIndex = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(checkIndex);
        }
    }
}

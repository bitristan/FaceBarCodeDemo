package com.qrcode.r.sdk;

import android.graphics.Path;
import android.graphics.Rect;

/**
 * 边框装饰父类
 */
public abstract class QRBorder {

    protected int mWidth;

    protected int mHeight;

    protected Rect mInsideRect;

    protected int mBoxSize;

    protected int mWidthLeftBoxCount;
    protected int mWidthRightBoxCount;
    protected int mHeightTopBoxCount;
    protected int mHeightBottomBoxCount;

    public QRBorder() {
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Rect getInsideArea() {
        return new Rect(mInsideRect);
    }

    public int getBoxSize() {
        return mBoxSize;
    }

    public abstract Path getClipPath();

    public int getWidthExtendBoxCount() {
        return mWidthLeftBoxCount;
    }

    public int getHeightExtendBoxCount() {
        return mHeightTopBoxCount;
    }
}

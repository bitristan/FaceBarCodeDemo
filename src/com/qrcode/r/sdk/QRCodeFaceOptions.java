package com.qrcode.r.sdk;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Created by robert on 13-12-23.
 */
public class QRCodeFaceOptions implements QRCodeOptionsInterface {
    private static final int DEFAULT_SIZE = 500;
    private static final int PREDEFINED_COLOR[] = new int[]{
            Color.parseColor("#dd349d"), Color.parseColor("#868686"), Color.parseColor("#128b37"), Color.parseColor("#287ed4"),
    };

    public String mQrContent;
    public Bitmap mFaceBmp;

    public ErrorCorrectionLevel errorLevel;

    public int mSize;
    public int mColor = Color.parseColor("#dd349d");

    @Override
    public void checkArguments() {
        if (mQrContent == null) {
            throw new IllegalArgumentException("content should not be null.");
        }

        if (mFaceBmp == null) {
            throw new IllegalArgumentException("must have a background image");
        }

        if (mSize <= 0) {
            mSize = DEFAULT_SIZE;
        }

//        if (mColor <= 0) {
//            mColor = PREDEFINED_COLOR[(int) (Math.random() * 4)];
//        }
    }

    @Override
    public QRCodePixelReleaseEffect getQRCodeReleaseEffect() {
        return QRCodePixelReleaseEffect.FACE;
    }

    @Override
    public String getContent() {
        return mQrContent;
    }
}

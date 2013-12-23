package com.qrcode.r.sdk;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Created by zhangdi on 13-12-20.
 */
public class QRCodeGradientOptions implements QRCodeOptionsInterface {

    /**
     * 遮盖的图片
     */
    public Bitmap maskBitmap;

    /**
     * 带边框的图片
     */
    public Bitmap borderBitmap;

    /**
     * 进行遮盖的图片
     */
    public Bitmap frontBitmap;

    /**
     * 渐变开始颜色
     */
    public int startColor = Color.RED;

    /**
     * 渐变结束颜色
     */
    public int endColor = Color.BLACK;

    /**
     * 默认的QR输出大小
     */
    public int defaultQRSize;

    /**
     * 编码内容
     */
    public String qrContent;

    /**
     * 纠错级别
     */
    public ErrorCorrectionLevel errorLevel;

    @Override
    public void checkArguments() {
        if (defaultQRSize == 0) {
            throw new IllegalArgumentException("defaultQRSize can't be 0");
        }
        if (maskBitmap == null) {
            throw new IllegalArgumentException("maskBitmap can't be NULL");
        }
        if (borderBitmap == null) {
            throw new IllegalArgumentException("borderBitmap can't be NULL");
        }
        if (frontBitmap == null) {
            throw new IllegalArgumentException("frontBitmap can't be NULL");
        }
        if (errorLevel == null) {
            errorLevel = ErrorCorrectionLevel.H;
        }
    }

    @Override
    public QRCodePixelReleaseEffect getQRCodeReleaseEffect() {
        return QRCodePixelReleaseEffect.GRADIENT;
    }

    @Override
    public String getContent() {
        return qrContent;
    }

}

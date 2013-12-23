package com.qrcode.r.sdk;

import android.graphics.Bitmap;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Created by michael on 13-12-19.
 */
public class QRCodePixelOptions implements QRCodeOptionsInterface {

    /**
     * 混合的背景色
     */
    public Bitmap backgroundBitmap;

    /**
     * 遮盖的图片
     */
    public Bitmap maskBitmap;

    /**
     * 遮盖图片的大小，注意这个大小不是像素的大小，是对应的图片的色块数
     */
    public int maskRectCount;

    /**
     * 最后进行整体遮罩的图片
     */
    public Bitmap frontBitmap;

    /**
     * 默认的QR输出大小，对于像素化效果，会做一个自适应
     * 所以输出的QR bitmap大小不一定就是默认值
     */
    public int defaultQRSize;

    public String qrContent;

    /**
     * true:表示漏底色，二维码区域使用地图颜色
     * false:表示遮盖底色，二维码区域使用黑色遮盖
     */
//    public boolean maskBackground;

    /**
     * 纠错级别
     */
    public ErrorCorrectionLevel errorLevel;

    public QRCodePixelReleaseEffect qrCodeRelaeseEffect;

    @Override
    public String getContent() {
        return qrContent;
    }

    @Override
    public QRCodePixelReleaseEffect getQRCodeReleaseEffect() {
        return qrCodeRelaeseEffect;
    }

    @Override
    public void checkArguments() {
        if (defaultQRSize == 0) {
            throw new IllegalArgumentException("defaultQRSize can't be 0");
        }
        if (qrCodeRelaeseEffect == null) {
            throw new IllegalArgumentException("qrCodeRelaeseEffect can't be NULL");
        }
        if (backgroundBitmap == null) {
            throw new IllegalArgumentException("backgroundBitmap can't be empty under PIXEL effect");
        }
        if (maskBitmap != null && maskRectCount == 0) {
            throw new IllegalArgumentException("maskRectCount can't be 0 when maskBitmap != null");
        }

        if (errorLevel == null) {
            errorLevel = ErrorCorrectionLevel.H;
        }
    }
}

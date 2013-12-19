package com.qrcode.r.sdk;

import android.graphics.Bitmap;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Created by michael on 13-12-19.
 */
public class QRCodeOptions {

//    /**
//     * 背景图片的全路径
//     */
//    public String backgroundImageFullPath;

    public Bitmap backgroundBitmap;

    /**
     * 默认的QR输出大小，对于像素化效果，会做一个自适应
     * 所以输出的QR bitmap大小不一定就是默认值
     */
    public int defaultQRSize;

    public String qrContent;

    /**
     * 纠错级别
     */
    public ErrorCorrectionLevel errorLevel;

    /**
     * 二维码效果
     */
    public static enum QRCodeRelaeseEffect {
        PIXEL,
    }

    public QRCodeRelaeseEffect qrCodeRelaeseEffect;
}

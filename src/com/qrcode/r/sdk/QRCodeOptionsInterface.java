package com.qrcode.r.sdk;

/**
 * Created by michael on 13-12-20.
 */
public interface QRCodeOptionsInterface {

    /**
     * 二维码效果
     */
    public static enum QRCodePixelReleaseEffect {
        PIXEL,
        PIXEL_Border,
        GRADIENT,
        FACE,
    }

    void checkArguments();

    QRCodePixelReleaseEffect getQRCodeReleaseEffect();

    String getContent();

}

package com.qrcode.r.sdk;

/**
 * Created by michael on 13-12-20.
 */
public interface QRCodeOptionsInterface {

    /**
     * 二维码效果
     */
    public static enum QRCodePixelRelaeseEffect {
        PIXEL,
        PIXEL_Border,
    }

    void checkArguments();

    QRCodePixelRelaeseEffect getQRCodeReleaseEffect();

    String getContent();

}

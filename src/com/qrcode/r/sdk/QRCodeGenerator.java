package com.qrcode.r.sdk;

import android.graphics.Bitmap;

/**
 * Created by michael on 13-12-19.
 */
public class QRCodeGenerator {

    public static Bitmap createQRCode(QRCodeOptionsInterface opt) {
        checkOptionArgument(opt);

        if (opt.getQRCodeReleaseEffect() == QRCodeOptionsInterface.QRCodePixelReleaseEffect.PIXEL) {
            QREffectInterface obj = new PixelQREffect();
            return obj.makeEffectQRCode(opt.getContent(), opt);
        } else if (opt.getQRCodeReleaseEffect() == QRCodeOptionsInterface.QRCodePixelReleaseEffect.PIXEL_Border) {
            QREffectInterface obj = new PixelBorderQREffect();
            return obj.makeEffectQRCode(opt.getContent(), opt);
        } else if (opt.getQRCodeReleaseEffect() == QRCodeOptionsInterface.QRCodePixelReleaseEffect.GRADIENT) {
            QREffectInterface obj = new GradientQREffect();
            return obj.makeEffectQRCode(opt.getContent(), opt);
        } else if (opt.getQRCodeReleaseEffect() == QRCodeOptionsInterface.QRCodePixelReleaseEffect.FACE) {
            QREffectInterface obj = new FaceQREffect();
            return obj.makeEffectQRCode(opt.getContent(), opt);
        }

        return null;
    }

    private static void checkOptionArgument(QRCodeOptionsInterface opt) {
        if (opt == null) {
            throw new IllegalArgumentException("Option can't be NULL");
        }

        opt.checkArguments();
    }

}

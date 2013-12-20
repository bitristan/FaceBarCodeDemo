package com.qrcode.r.sdk;

import android.graphics.Bitmap;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Created by michael on 13-12-19.
 */
public class QRCodeGenerator {

    public static Bitmap createQRCode(QRCodeOptions opt) {
        checkOptionArgument(opt);

        if (opt.qrCodeRelaeseEffect == QRCodeOptions.QRCodeRelaeseEffect.PIXEL) {
            QREffectInterface obj = new PixelQREffect();
            return obj.makeEffectQRCode(opt.qrContent, opt);
        } else if (opt.qrCodeRelaeseEffect == QRCodeOptions.QRCodeRelaeseEffect.PIXEL_Border) {
            QREffectInterface obj = new PixelBorderQREffect();
            return obj.makeEffectQRCode(opt.qrContent, opt);
        }

        return null;
    }

    private static void checkOptionArgument(QRCodeOptions opt) {
        if (opt == null) {
            throw new IllegalArgumentException("Option can't be NULL");
        }
        if (opt.defaultQRSize == 0) {
            throw new IllegalArgumentException("defaultQRSize can't be 0");
        }
        if (opt.qrCodeRelaeseEffect == null) {
            throw new IllegalArgumentException("qrCodeRelaeseEffect can't be NULL");
        }
        if (opt.qrCodeRelaeseEffect == QRCodeOptions.QRCodeRelaeseEffect.PIXEL
            && opt.backgroundBitmap == null) {
            throw new IllegalArgumentException("backgroundBitmap can't be empty under PIXEL effect");
        }

        if (opt.errorLevel == null) {
            opt.errorLevel = ErrorCorrectionLevel.H;
        }
    }

}

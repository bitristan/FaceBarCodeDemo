package com.qrcode.r.sdk;

import android.graphics.Bitmap;
import android.text.TextUtils;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

/**
 * Created by michael on 13-12-19.
 */
public abstract class QREffectInterface {

    abstract Bitmap makeEffectQRCode(String content, QRCodeOptionsInterface opt);

    protected QRCode encodeQrcode(String content, ErrorCorrectionLevel level) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        QRCode code = null;
        try {
            code = Encoder.encode(content, level, null);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return code;
    }

}

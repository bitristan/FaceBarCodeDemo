package com.qrcode.r.sdk;

import android.graphics.Bitmap;

/**
 * Created by michael on 13-12-19.
 */
public interface QREffectInterface {

    Bitmap makeEffectQRCode(String content, com.qrcode.r.sdk.QRCodeOptions opt);

}

package com.qrcode.r.sdk;

import android.graphics.*;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;
import com.robert.image.compose.demo.QRCodeUtils;

/**
 * Created by zhangdi on 13-12-20.
 */
public class GradientQREffect implements QREffectInterface {

    @Override
    public Bitmap makeEffectQRCode(String content, QRCodeOptions opt) {
        QRCode qrCode = QRCodeUtils.encodeQrcode(opt.qrContent);

        ByteMatrix input = qrCode.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        // 四周各有1点阵的边框和2点阵的padding
        int qrWidth = inputWidth + 6;
        int qrHeight = inputHeight + 6;
        int outputWidth = Math.max(opt.defaultQRSize, qrWidth);
        int outputHeight = Math.max(opt.defaultQRSize, qrHeight);

        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        int realWidth = qrWidth * multiple;
        int realHeight = qrHeight * multiple;

        Bitmap out = Bitmap.createBitmap(realWidth, realHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.drawARGB(200, 255, 255, 255);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);

        // 渐变开始结束颜色
        int startColor = Color.RED;
        int endColor = Color.BLACK;


        Rect box = new Rect();

        // inputX从－3到inputWidth+2, inputY从－3到inputHeight+2
        for (int inputY = -3, outputY = 0; inputY < inputHeight + 3; inputY++, outputY += multiple) {
            for (int inputX = -3, outputX = 0; inputX < inputWidth + 3; inputX++, outputX += multiple) {
                box.set(outputX, outputY, outputX + multiple, outputY + multiple);

                float ratio = (inputY + 3) / qrHeight;
                paint.setColor(getGradientColor(startColor, endColor, ratio));
                
                if (isSet(input, inputX, inputY)) {
                    canvas.drawRect(box, paint);
                }
            }
        }

        return out;
    }

    private boolean isSet(ByteMatrix input, int inputX, int inputY) {
        if (inputX == -3 || inputX == input.getWidth() + 2 || inputY == -3 || inputY == input.getHeight() + 2)
            return true;
        if ((inputX >= -2 && inputX <= -1) || (inputX >= input.getWidth() && inputX <= input.getWidth() + 1))
            return false;
        if ((inputY >= -2 && inputY <= -1) || (inputY >= input.getHeight() && inputY <= input.getHeight() + 1))
            return false;
        return input.get(inputX, inputY) == 1;
    }

    private int getGradientColor(int startColor, int endColor, float ratio) {
        if (ratio <= 0.000001) {
            return startColor;
        }
        if (ratio >= 1.0) {
            return endColor;
        }

        int a1 = Color.alpha(startColor);
        int r1 = Color.red(startColor);
        int g1 = Color.green(startColor);
        int b1 = Color.blue(startColor);

        int a2 = Color.alpha(endColor);
        int r2 = Color.red(endColor);
        int g2 = Color.green(endColor);
        int b2 = Color.blue(endColor);

        int a3 = (int) (a1 + (a2 - a1) * ratio);
        int r3 = (int) (r1 + (r2 - r1) * ratio);
        int g3 = (int) (g1 + (g2 - g1) * ratio);
        int b3 = (int) (b1 + (b2 - b1) * ratio);

        return Color.argb(a3, r3, g3, b3);
    }

}

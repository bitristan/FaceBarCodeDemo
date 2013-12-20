package com.qrcode.r.sdk;

import android.graphics.*;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;
import com.robert.image.compose.demo.QRCodeUtils;

import java.util.concurrent.CancellationException;

/**
 * Created by michael on 13-12-19.
 */
public class PixelBorderQREffect extends PixelQREffect {

    @Override
    public Bitmap makeEffectQRCode(String content, QRCodeOptions opt) {
        QRCode qrCode = QRCodeUtils.encodeQrcode(opt.qrContent);

        ByteMatrix input = qrCode.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth;
        int qrHeight = inputHeight;
        int outputWidth = Math.max(opt.defaultQRSize, qrWidth);
        int outputHeight = Math.max(opt.defaultQRSize, qrHeight);

        QRBorder qrBorder = new QRCircleBorder(outputWidth, outputHeight, input.getWidth());
        int multiple = qrBorder.getBoxSize();

        Bitmap pixelBt = mosaic(opt.backgroundBitmap, outputWidth, outputHeight, multiple);
        pixelBt.setHasAlpha(true);

        Bitmap out = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
        out.setHasAlpha(true);
        Canvas canvas = new Canvas(out);
        canvas.clipPath(qrBorder.getClipPath());
        canvas.drawARGB(200, 255, 255, 255);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);

        paint.setAlpha(150);
        ColorMatrix allMatrix = new ColorMatrix();
        ColorMatrix colorMatrix = new ColorMatrix();
//        //饱和度
        colorMatrix.setSaturation(6);
        allMatrix.postConcat(colorMatrix);
        //对比度
        float contrast = (float) ((10 + 64) / 128.0);
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[]{contrast, 0, 0, 0, 0, 0,
                                   contrast, 0, 0, 0,// 改变对比度
                                   0, 0, contrast, 0, 0, 0, 0, 0, 1, 0});
        allMatrix.postConcat(cMatrix);
//
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        canvas.drawBitmap(pixelBt, 0, 0, paint);
        paint.setColorFilter(null);
        paint.setAlpha(160);

        Rect qrRegionRect = qrBorder.getInsideArea();

        //绘制中心二维码
//        for (int inputY = 0, outputY = qrRegionRect.top; inputY < inputHeight; inputY++, outputY += multiple) {
//            for (int inputX = 0, outputX = qrRegionRect.left; inputX < inputWidth; inputX++, outputX += multiple) {
//                qrBox.left = outputX;
//                qrBox.right = outputX + multiple;
//                qrBox.top = outputY;
//                qrBox.bottom = outputY + multiple;
//
//                if (input.get(inputX, inputY) == 1) {
//                    canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
//                }
//            }
//        }
        drawQRCodeRect(canvas, input, qrRegionRect, qrBorder.getBoxSize(), inputWidth, inputHeight, paint);
        //左侧
        int moveLeft = (2 + inputWidth) * qrBorder.getBoxSize();
        qrRegionRect.left = qrRegionRect.left - moveLeft;
        qrRegionRect.right = qrRegionRect.right - moveLeft;
        drawQRCodeRect(canvas, input, qrRegionRect, qrBorder.getBoxSize(), inputWidth, inputHeight, paint);
        //上侧
        qrRegionRect = qrBorder.getInsideArea();
        int moveTop = (2 + inputHeight) * qrBorder.getBoxSize();
        qrRegionRect.top = qrRegionRect.top - moveTop;
        qrRegionRect.bottom = qrRegionRect.bottom - moveTop;
        drawQRCodeRect(canvas, input, qrRegionRect, qrBorder.getBoxSize(), inputWidth, inputHeight, paint);
        //右
        qrRegionRect = qrBorder.getInsideArea();
        int moveRight = (2 + inputWidth) * qrBorder.getBoxSize();
        qrRegionRect.left = qrRegionRect.left + moveRight;
        qrRegionRect.right = qrRegionRect.right + moveRight;
        drawQRCodeRect(canvas, input, qrRegionRect, qrBorder.getBoxSize(), inputWidth, inputHeight, paint);
        //下
        qrRegionRect = qrBorder.getInsideArea();
        int moveBottom = (2 + inputHeight) * qrBorder.getBoxSize();
        qrRegionRect.top = qrRegionRect.top + moveBottom;
        qrRegionRect.bottom = qrRegionRect.bottom + moveBottom;
        drawQRCodeRect(canvas, input, qrRegionRect, qrBorder.getBoxSize(), inputWidth, inputHeight, paint);

        return out;
    }

    private void drawQRCodeRect(Canvas canvas, ByteMatrix input, Rect qrRegionRect, int multiple
                                   , int qrWidth, int qrHeight, Paint paint) {
        Rect qrBox = new Rect();
        for (int inputY = 0, outputY = qrRegionRect.top; inputY < qrHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = qrRegionRect.left; inputX < qrWidth; inputX++, outputX += multiple) {
                qrBox.left = outputX;
                qrBox.right = outputX + multiple;
                qrBox.top = outputY;
                qrBox.bottom = outputY + multiple;

                if (input.get(inputX, inputY) == 1) {
                    canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                }
            }
        }
    }

}

package com.qrcode.r.sdk;

import android.graphics.*;
import android.util.Log;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;

/**
 * Created by michael on 13-12-19.
 */
public class PixelBorderQREffect extends PixelQREffect {

    @Override
    public Bitmap makeEffectQRCode(String content, QRCodeOptionsInterface option) {
        QRCodePixelOptions opt = (QRCodePixelOptions) option;

        QRCode qrCode = encodeQrcode(opt.qrContent, opt.errorLevel);

        ByteMatrix input = qrCode.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth;
        int qrHeight = inputHeight;
        int orgWidth = Math.max(opt.defaultQRSize, qrWidth);
        int orgHeight = Math.max(opt.defaultQRSize, qrHeight);

        QRBorder qrBorder = new QRCircleBorder(orgWidth, orgHeight, input.getWidth());
        int multiple = qrBorder.getBoxSize();

        Bitmap pixelBt = mosaic(opt.backgroundBitmap, qrBorder.getWidth(), qrBorder.getHeight(), multiple);
        pixelBt.setHasAlpha(true);

        Bitmap out = Bitmap.createBitmap(qrBorder.getWidth(), qrBorder.getHeight(), Bitmap.Config.ARGB_8888);
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
        drawQRCodeRect(canvas, input, qrBorder.getInsideArea(), qrBorder.getBoxSize(), inputWidth, inputHeight, paint);


//        Log.d("PixelBorderQREffect", "begin");
//        Log.d("PixelBorderQREffect", input.toString());
//        Log.d("PixelBorderQREffect", ">>>>>>>>>>");
        //调整input矩阵
        //左上角
        for (int x = 0, x1 = qrWidth - 9; x < 8; ++x, ++x1) {
            for (int y = 0, y1 = qrHeight - 9; y < 8; ++y, ++y1) {
                input.set(x, y, input.get(x1, y1));
            }
        }
//        Log.d("PixelBorderQREffect", "完成左上角");
//        Log.d("PixelBorderQREffect", input.toString());
        //右上角
        for (int x = qrWidth - 9, x1 = qrWidth - 9; x < qrWidth - 1; ++x, ++x1) {
            for (int y = 0, y1 = qrHeight - 9; y < 8; ++y, ++y1) {
                input.set(x, y, input.get(x1, y1));
            }
        }
//        Log.d("PixelBorderQREffect", "完成右上角");
//        Log.d("PixelBorderQREffect", input.toString());
        //左下角
        for (int x = 0, x1 = qrWidth - 9; x < 8; ++x, ++x1) {
            for (int y = qrHeight - 9, y1 = qrHeight - 9; y < qrHeight - 1; ++y, ++y1) {
                input.set(x, y, input.get(x1, y1));
            }
        }
//        Log.d("PixelBorderQREffect", "完成左下角");
//        Log.d("PixelBorderQREffect", input.toString());

        //为了提高效率，先将二维码绘制到一个buffer
        Rect qrRegionRect = qrBorder.getInsideArea();
        Bitmap qrCodeBuffer = Bitmap.createBitmap(qrRegionRect.width(), qrRegionRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas1 = new Canvas();
        canvas1.setBitmap(qrCodeBuffer);
        canvas1.drawARGB(0, 0, 0, 0);
        //将二维码绘制到buffer
        paint.setAlpha(255);
        drawQRCodeRect(canvas1, input, new Rect(0, 0, qrRegionRect.width(), qrRegionRect.height()), qrBorder.getBoxSize(), inputWidth, inputHeight, paint);
        paint.setAlpha(160);

        //左侧
        int moveLeft = (2 + inputWidth) * qrBorder.getBoxSize();
        qrRegionRect.left = qrRegionRect.left - moveLeft;
        qrRegionRect.right = qrRegionRect.right - moveLeft;
        canvas.drawBitmap(qrCodeBuffer, qrRegionRect.left, qrRegionRect.top, paint);
//        drawQRCodeRect(canvas, input, qrRegionRect, qrBorder.getBoxSize(), inputWidth, inputHeight, paint);
        //上侧
        qrRegionRect = qrBorder.getInsideArea();
        int moveTop = (2 + inputHeight) * qrBorder.getBoxSize();
        qrRegionRect.top = qrRegionRect.top - moveTop;
        qrRegionRect.bottom = qrRegionRect.bottom - moveTop;
        canvas.drawBitmap(qrCodeBuffer, qrRegionRect.left, qrRegionRect.top, paint);
//        drawQRCodeRect(canvas, input, qrRegionRect, qrBorder.getBoxSize(), inputWidth, inputHeight, paint);
        //右
        qrRegionRect = qrBorder.getInsideArea();
        int moveRight = (2 + inputWidth) * qrBorder.getBoxSize();
        qrRegionRect.left = qrRegionRect.left + moveRight;
        qrRegionRect.right = qrRegionRect.right + moveRight;
        canvas.drawBitmap(qrCodeBuffer, qrRegionRect.left, qrRegionRect.top, paint);
//        drawQRCodeRect(canvas, input, qrRegionRect, qrBorder.getBoxSize(), inputWidth, inputHeight, paint);
        //下
        qrRegionRect = qrBorder.getInsideArea();
        int moveBottom = (2 + inputHeight) * qrBorder.getBoxSize();
        qrRegionRect.top = qrRegionRect.top + moveBottom;
        qrRegionRect.bottom = qrRegionRect.bottom + moveBottom;
        canvas.drawBitmap(qrCodeBuffer, qrRegionRect.left, qrRegionRect.top, paint);
//        drawQRCodeRect(canvas, input, qrRegionRect, qrBorder.getBoxSize(), inputWidth, inputHeight, paint);

        //左上
        qrRegionRect = qrBorder.getInsideArea();
        qrRegionRect.left = qrRegionRect.left - moveLeft;
        qrRegionRect.right = qrRegionRect.right - moveLeft;
        qrRegionRect.top = qrRegionRect.top - moveTop;
        qrRegionRect.bottom = qrRegionRect.bottom - moveTop;
        canvas.drawBitmap(qrCodeBuffer, qrRegionRect.left, qrRegionRect.top, paint);

        //右上
        qrRegionRect = qrBorder.getInsideArea();
        qrRegionRect.left = qrRegionRect.left + moveRight;
        qrRegionRect.right = qrRegionRect.right + moveRight;
        qrRegionRect.top = qrRegionRect.top - moveTop;
        qrRegionRect.bottom = qrRegionRect.bottom - moveTop;
        canvas.drawBitmap(qrCodeBuffer, qrRegionRect.left, qrRegionRect.top, paint);

        //左下
        qrRegionRect = qrBorder.getInsideArea();
        qrRegionRect.left = qrRegionRect.left - moveLeft;
        qrRegionRect.right = qrRegionRect.right - moveLeft;
        qrRegionRect.top = qrRegionRect.top + moveBottom;
        qrRegionRect.bottom = qrRegionRect.bottom + moveBottom;
        canvas.drawBitmap(qrCodeBuffer, qrRegionRect.left, qrRegionRect.top, paint);

        //右下
        qrRegionRect = qrBorder.getInsideArea();
        qrRegionRect.left = qrRegionRect.left + moveRight;
        qrRegionRect.right = qrRegionRect.right + moveRight;
        qrRegionRect.top = qrRegionRect.top + moveBottom;
        qrRegionRect.bottom = qrRegionRect.bottom + moveBottom;
        canvas.drawBitmap(qrCodeBuffer, qrRegionRect.left, qrRegionRect.top, paint);

        return out;
    }

    private void drawQRCodeRect(Canvas canvas, ByteMatrix input, Rect qrRegionRect, int multiple
                                   , int qrWidth, int qrHeight, Paint paint) {
        Rect qrBox = new Rect();
        int r = (int) (multiple * 0.4);
        for (int inputY = 0, outputY = qrRegionRect.top; inputY < qrHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = qrRegionRect.left; inputX < qrWidth; inputX++, outputX += multiple) {
                qrBox.left = outputX;
                qrBox.right = outputX + multiple;
                qrBox.top = outputY;
                qrBox.bottom = outputY + multiple;

                if (input.get(inputX, inputY) == 1) {
//                    canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                    drawRoundRect(canvas, new RectF(outputX, outputY, outputX + multiple, outputY + multiple), paint, r
                                  , (isSet(input, inputX - 1, inputY - 1) || isSet(input, inputX, inputY - 1) || isSet(input, inputX - 1, inputY))
                                  , (isSet(input, inputX, inputY - 1) || isSet(input, inputX + 1, inputY - 1) || isSet(input, inputX + 1, inputY))
                                  , (isSet(input, inputX, inputY + 1) || isSet(input, inputX, inputY - 1) || isSet(input, inputX - 1, inputY))
                                  , (isSet(input, inputX + 1, inputY) || isSet(input, inputX + 1, inputY + 1) || isSet(input, inputX, inputY + 1)));
                }
            }
        }
    }

    private boolean isSet(ByteMatrix matrix, int row, int column) {
        if (matrix == null) {
            return false;
        }

        if (row >= matrix.getWidth() || row < 0) return false;
        if (column >= matrix.getHeight() || column < 0) return false;
//        if (row == -1 || row == matrix.getWidth() || column == -1
//                || column == matrix.getHeight()) {
//            return false;
//        }
//
//        int x, y = 0;
//        if (row < 0 || column < 0) {
//            x = (row + 1 + matrix.getWidth()) % matrix.getWidth();
//            y = (column + 1 + matrix.getHeight()) % matrix.getHeight();
//        } else if (row > matrix.getWidth() - 1
//                       || column > matrix.getHeight() - 1) {
//            x = (row - 1 + matrix.getWidth()) % matrix.getWidth();
//            y = (column - 1 + matrix.getHeight()) % matrix.getHeight();
//        } else {
//            x = row % matrix.getWidth();
//            y = column % matrix.getHeight();
//        }

        return matrix.get(row, column) == 1;
    }

    private void drawRoundRect(Canvas canvas, RectF rect, Paint paint,
                               int radius, boolean leftTop, boolean rightTop, boolean leftBottom,
                               boolean rightBottom) {
        float roundRadius[] = new float[8];
        roundRadius[0] = leftTop ? 0 : radius;
        roundRadius[1] = leftTop ? 0 : radius;
        roundRadius[2] = rightTop ? 0 : radius;
        roundRadius[3] = rightTop ? 0 : radius;
        roundRadius[4] = rightBottom ? 0 : radius;
        roundRadius[5] = rightBottom ? 0 : radius;
        roundRadius[6] = leftBottom ? 0 : radius;
        roundRadius[7] = leftBottom ? 0 : radius;

        Path path = new Path();
        path.addRoundRect(rect, roundRadius, Path.Direction.CCW);
        canvas.drawPath(path, paint);
    }

}

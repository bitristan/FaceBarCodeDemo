package com.qrcode.r.sdk;

import android.graphics.*;
import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;

/**
 * Created by zhangdi on 13-12-20.
 */
public class GradientQREffect extends QREffectInterface {

    @Override
    public Bitmap makeEffectQRCode(String content, QRCodeOptionsInterface options) {
        QRCodeGradientOptions opt = (QRCodeGradientOptions) options;

        QRCode qrCode = encodeQrcode(opt.qrContent, opt.errorLevel);

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
        int bgColor = Color.WHITE;
        canvas.drawColor(bgColor);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);

        // 渐变开始结束颜色
        int startColor = opt.startColor;
        int endColor = opt.endColor;

        int roundRectRadius = (int) (0.3 * multiple);

        RectF box = new RectF();

        // inputX从－3到inputWidth+2, inputY从－3到inputHeight+2
        for (int inputY = -3, outputY = 0; inputY < inputHeight + 3; inputY++, outputY += multiple) {
            for (int inputX = -3, outputX = 0; inputX < inputWidth + 3; inputX++, outputX += multiple) {
                box.set(outputX, outputY, outputX + multiple, outputY + multiple);

                float ratio = (float) (inputY + 3) / qrHeight;
                paint.setColor(getGradientColor(startColor, endColor, ratio));

                // 边框和padding不做液化
                if (inputX < 0 || inputX > inputWidth - 1 || inputY < 0 || inputY > inputWidth - 1) {
                    if (isSet(input, inputX, inputY)) {
                        canvas.drawRect(box, paint);
                    }
                } else {
                    if (isSet(input, inputX, inputY)) {
                        drawRoundRect(
                                canvas,
                                box,
                                paint,
                                roundRectRadius,
                                isSet(input, inputX - 1, inputY - 1)
                                        || isSet(input, inputX, inputY - 1)
                                        || isSet(input, inputX - 1, inputY),
                                isSet(input, inputX, inputY - 1)
                                        || isSet(input, inputX + 1,
                                        inputY - 1)
                                        || isSet(input, inputX + 1, inputY),
                                isSet(input, inputX, inputY + 1)
                                        || isSet(input, inputX - 1,
                                        inputY + 1)
                                        || isSet(input, inputX - 1, inputY),
                                isSet(input, inputX + 1, inputY)
                                        || isSet(input, inputX + 1,
                                        inputY + 1)
                                        || isSet(input, inputX, inputY + 1));
                    } else {
                        if (isSet(input, inputX, inputY - 1)
                                && isSet(input, inputX - 1, inputY)) {
                            drawAntiRoundRect(canvas, paint, roundRectRadius, box, 1);
                        }
                        if (isSet(input, inputX, inputY - 1)
                                && isSet(input, inputX + 1, inputY)) {
                            drawAntiRoundRect(canvas, paint, roundRectRadius, box, 2);
                        }
                        if (isSet(input, inputX, inputY + 1)
                                && isSet(input, inputX + 1, inputY)) {
                            drawAntiRoundRect(canvas, paint, roundRectRadius, box, 3);
                        }
                        if (isSet(input, inputX - 1, inputY)
                                && isSet(input, inputX, inputY + 1)) {
                            drawAntiRoundRect(canvas, paint, roundRectRadius, box, 4);
                        }
                    }
                }
            }
        }

        int frontWidth = (int) (0.5 * realWidth);
        int frontHeight = (int) (0.5 * realHeight);

        int binStartColor = getGradientColor(startColor, endColor, (realHeight - frontHeight) / 2.0f / realHeight);
        int binEndColor = getGradientColor(startColor, endColor, (realHeight + frontHeight) / 2.0f / realHeight);
        // 二值化
//        Bitmap front = binarization(opt.frontBitmap, bgColor, binStartColor, binEndColor);
        Rect frontRect = new Rect((realWidth - frontWidth) / 2, (realHeight - frontHeight) / 2, (realWidth + frontWidth) / 2, (realHeight + frontHeight) / 2);
        Bitmap scaleFront = Bitmap.createScaledBitmap(opt.frontBitmap, frontRect.width(), frontRect.height(), false);
        //现将图片做一次缩放，目的是为了减小处理的像素数量
        scaleFront = convertGrayImg(scaleFront);
        Bitmap front = bitmapHSB(scaleFront, binStartColor, binEndColor);
        if (opt.frontBitmap != null && !opt.frontBitmap.isRecycled()) {
            opt.frontBitmap.recycle();
            opt.frontBitmap = null;
        }

        Bitmap scaleBroder = Bitmap.createScaledBitmap(opt.borderBitmap, frontRect.width(), frontRect.height(), false);
        if (opt.borderBitmap != null && !opt.borderBitmap.isRecycled()) {
            opt.borderBitmap.recycle();
            opt.borderBitmap = null;
        }
        Bitmap border = borderGradient(scaleBroder, binStartColor, binEndColor);
        if (scaleBroder != null && !scaleBroder.isRecycled()) {
            scaleBroder.recycle();
            scaleBroder = null;
        }

        // 加边框
        front = borderFront(border.getWidth(), border.getHeight(), border, front);

        // 遮盖切割
        Bitmap scaleMask = Bitmap.createScaledBitmap(opt.maskBitmap, frontRect.width(), frontRect.height(), false);
        if (opt.maskBitmap != null && !opt.maskBitmap.isRecycled()) {
            opt.maskBitmap.recycle();
            opt.maskBitmap = null;
        }
        front = maskFront(scaleMask.getWidth(), scaleMask.getHeight(), scaleMask, front);

        Rect faceRect = new Rect(0, 0, front.getWidth(), front.getHeight());
        canvas.drawBitmap(front, faceRect, frontRect, paint);

        return out;
    }

    private boolean isSet(ByteMatrix input, int inputX, int inputY) {
        if (inputX == -3 || inputX == input.getWidth() + 2 || inputY == -3 || inputY == input.getHeight() + 2)
            return true;
        if ((inputX >= -2 && inputX <= -1) || (inputX >= input.getWidth() && inputX <= input.getWidth() + 1))
            return false;
        if ((inputY >= -2 && inputY <= -1) || (inputY >= input.getHeight() && inputY <= input.getHeight() + 1))
            return false;
        if (inputX < -3 || inputX > input.getWidth() + 2 || inputY < -3 || inputY > input.getHeight() + 2)
            return false;
        return input.get(inputX, inputY) == 1;
    }

    private Bitmap bitmapHSB(Bitmap bt, int startColor, int endColor) {
        Bitmap out = Bitmap.createBitmap(bt.getWidth(), bt.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.WHITE);

        float[] start = new float[3];
        float[] end = new float[3];
        Color.colorToHSV(startColor, start);
        start[1] = 0;
        start[2] = 0;
        Color.colorToHSV(endColor, end);
        end[1] = 0;
        end[2] = 0;
        bt = covertBitmapWithHSBWithHChanged(bt, start, end);

        ColorMatrix allMatrix = new ColorMatrix();
        ColorMatrix colorMatrix = new ColorMatrix();
        setContrast(colorMatrix, 160, 120);
        allMatrix.postConcat(colorMatrix);
//
        paint.setColorFilter(new ColorMatrixColorFilter(allMatrix));

        canvas.drawBitmap(bt, 0, 0, paint);

        return out;
    }

    private static void setContrast(ColorMatrix cm, int contrast, int illumination) {
        //contrast 0~200
        float c = (contrast * 1.0f - 100) / 100;
        float matrixContrast = (float) Math.tan((45 + 44 * c) / 180 * Math.PI);

        float matrixIllumination = (illumination - 100) / (1.0f * 100);
        float translate = -127.5f * (1 - matrixIllumination) * matrixContrast + 127.5f * (1 + matrixIllumination);

        cm.set(new float[]{matrixContrast, 0, 0, 0, translate,
                              0, matrixContrast, 0, 0, translate,
                              0, 0, matrixContrast, 0, translate,
                              0, 0, 0, 1, 0});
    }

    private Bitmap covertBitmapWithHSBWithHChanged(Bitmap bt, float[] startHSVAdjust, float[] endHSVAdjust) {
        int w = bt.getWidth(), h = bt.getHeight();
        int[] pix = new int[w * h];
        bt.getPixels(pix, 0, w, 0, 0, w, h);

        float hueDeta = (endHSVAdjust[0] - startHSVAdjust[0]) / h;
        float[] pixelHSV = new float[3];
        int alpha = 0xFF << 24;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int color = pix[w * i + j];
                Color.colorToHSV(color, pixelHSV);

                pixelHSV[0] = startHSVAdjust[0] + hueDeta * i;
                pixelHSV[1]  = 1 - pixelHSV[2];
                color = Color.HSVToColor(pixelHSV);
                color = alpha | color;
                pix[w * i + j] = color;
            }
        }

        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        result.setPixels(pix, 0, w, 0, 0, w, h);
        return result;
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

    private void drawAntiRoundRect(Canvas canvas, Paint paint, int radius,
                                   RectF rect, int direction) {
        if (direction == 1) {
            Path path = new Path();
            path.moveTo(rect.left, rect.top);
            path.lineTo(rect.left, rect.top + radius);
            path.addArc(new RectF(rect.left, rect.top, rect.left + radius * 2,
                    rect.top + radius * 2), 180, 90);
            path.lineTo(rect.left, rect.top);
            path.close();
            canvas.drawPath(path, paint);
        } else if (direction == 2) {
            Path path = new Path();
            path.moveTo(rect.right, rect.top);
            path.lineTo(rect.right - radius, rect.top);
            path.addArc(new RectF(rect.right - 2 * radius, rect.top,
                    rect.right, rect.top + radius * 2), 270, 90);
            path.lineTo(rect.right, rect.top);
            path.close();
            canvas.drawPath(path, paint);
        } else if (direction == 3) {
            Path path = new Path();
            path.moveTo(rect.right, rect.bottom);
            path.lineTo(rect.right, rect.bottom - radius);
            path.addArc(new RectF(rect.right - 2 * radius, rect.bottom - 2
                    * radius, rect.right, rect.bottom), 0, 90);
            path.lineTo(rect.right, rect.bottom);
            path.close();
            canvas.drawPath(path, paint);
        } else if (direction == 4) {
            Path path = new Path();
            path.moveTo(rect.left, rect.bottom);
            path.lineTo(rect.left + radius, rect.bottom);
            path.addArc(new RectF(rect.left, rect.bottom - 2 * radius,
                    rect.left + 2 * radius, rect.bottom), 90, 90);
            path.lineTo(rect.left, rect.bottom);
            path.close();
            canvas.drawPath(path, paint);
        }
    }

    private Bitmap borderFront(int width, int height, Bitmap border, Bitmap front) {
        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect src = new Rect(0, 0, front.getWidth(), front.getHeight());
        Rect dst = new Rect(0, 0, width, height);
        canvas.drawBitmap(front, src, dst, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        src.set(0, 0, border.getWidth(), border.getHeight());
        canvas.drawBitmap(border, src, dst, paint);

        return out;
    }

    private Bitmap maskFront(int width, int height, Bitmap mask, Bitmap front) {
        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect src = new Rect(0, 0, front.getWidth(), front.getHeight());
        Rect dst = new Rect(0, 0, width, height);
        canvas.drawBitmap(front, src, dst, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        src.set(0, 0, mask.getWidth(), mask.getHeight());
        canvas.drawBitmap(mask, src, dst, paint);

        return out;
    }

    private Bitmap borderGradient(Bitmap border, int startColor, int endColor) {
        int width = border.getWidth();
        int height = border.getHeight();
        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] pixels = new int[width * height];
        border.getPixels(pixels, 0, width, 0, 0, width, height);

        int offset;
        int gradientColor;
        for (int y = 0; y < height; y++) {
            gradientColor = getGradientColor(startColor, endColor, y / (float) height);
            for (int x = 0; x < width; x++) {
                offset = width * y + x;
                int a = pixels[offset] >>> 24;
                if (pixels[offset] == Color.BLACK) {
                    out.setPixel(x, y, gradientColor);
                }
            }
        }

        return out;
    }

    /**
     * 灰度图转化
     *
     * @param bt
     * @return
     */
    public Bitmap convertGrayImg(Bitmap bt) {
        int w = bt.getWidth(), h = bt.getHeight();
        int[] pix = new int[w * h];
        bt.getPixels(pix, 0, w, 0, 0, w, h);

        int alpha = 0xFF << 24;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // 获得像素的颜色
                int color = pix[w * i + j];
                int red = ((color & 0x00FF0000) >> 16);
                int green = ((color & 0x0000FF00) >> 8);
                int blue = color & 0x000000FF;
//                color = (red + green + blue) / 3;
                color = (red * 77 + green * 151 + blue * 28) >> 8;
                color = alpha | (color << 16) | (color << 8) | color;
                pix[w * i + j] = color;
            }
        }
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        result.setPixels(pix, 0, w, 0, 0, w, h);
        return result;
    }

    /**
     * 二值化
     *
     * @param bitmap
     * @return
     */
    private Bitmap binarization(Bitmap bitmap, int lowColor, int highStartColor, int highEndColor) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixels[] = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        Binarizer binarizer = new HybridBinarizer(source);

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        try {
            BitMatrix matrix = binarizer.getBlackMatrix();
            int highColor;
            for (int i = 0; i < height; i++) {
                highColor = getGradientColor(highStartColor, highEndColor, i / (float) height);
                for (int j = 0; j < width; j++) {
                    if (matrix.get(j, i)) {
                        result.setPixel(j, i, highColor);
                    } else {
                        result.setPixel(j, i, lowColor);
                    }
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

}

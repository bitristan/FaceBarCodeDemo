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
import com.robert.image.compose.demo.QRCodeUtils;

/**
 * Created by zhangdi on 13-12-20.
 */
public class GradientQREffect extends QREffectInterface {

    @Override
    public Bitmap makeEffectQRCode(String content, QRCodeOptionsInterface options) {
        QRCodeGradientOptions opt = (QRCodeGradientOptions) options;

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

        int frontWidth = (int) (0.4 * realWidth);
        int frontHeight = (int) (0.4 * realHeight);

        int binStartColor = getGradientColor(startColor, endColor, (realHeight - frontHeight) / 2.0f / realHeight);
        int binEndColor = getGradientColor(startColor, endColor, (realHeight + frontHeight) / 2.0f / realHeight);
        // 二值化
        Bitmap front = binarization(opt.frontBitmap, bgColor, binStartColor, binEndColor);

        // 边框渐变
        Bitmap border = borderGradient(opt.borderBitmap, binStartColor, binEndColor);
        // 加边框
        front = borderFront(border.getWidth(), border.getHeight(), border, front);

        // 遮盖切割
        front = maskFront(opt.maskBitmap.getWidth(), opt.maskBitmap.getHeight(), opt.maskBitmap, front);

        Rect frontRect = new Rect((realWidth - frontWidth) / 2, (realHeight - frontHeight) / 2, (realWidth + frontWidth) / 2, (realHeight + frontHeight) / 2);
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

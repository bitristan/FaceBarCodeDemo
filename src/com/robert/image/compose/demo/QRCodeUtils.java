package com.robert.image.compose.demo;

import android.content.Context;
import android.graphics.*;
import android.media.FaceDetector;
import android.text.TextUtils;
import android.util.Log;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import com.qrcode.sdk.QRCodeGenerator;
import com.qrcode.sdk.QRCodeOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by michael on 13-12-17.
 */
public class QRCodeUtils {

    public static Bitmap makePointQRCodeBt(String content, int size, int color, String textContent, int textSize) {
        final QRCodeOptions options = new QRCodeOptions();
        options.outWidth = size;
        options.outHeight = size;
        options.outBackgroundColor = Color.WHITE;
        options.outForegroundColor = color;
//        options.outGradientColor = -65528;
//        options.outGradientType = QRCodePixelOptions.GradientType.BACKSLASH;
//        options.outBorderType = QRCodePixelOptions.BorderType.ROUND;
        options.outShape = QRCodeOptions.Shape.ROUND;
        options.outErrorCorrectionLevel = ErrorCorrectionLevel.M;
        options.outRadiuspercent = 0.7f;
        options.textSize = textSize;
        options.textContent = textContent;

        QRCodeGenerator QRCodeGenerator = new QRCodeGenerator(content);

        try {
            return QRCodeGenerator.generate(options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap makeWaterQRCodeBt(String content, int size, int color, String textContent, int textSize) {
        final QRCodeOptions options = new QRCodeOptions();
        options.outWidth = size;
        options.outHeight = size;
        options.outBackgroundColor = Color.WHITE;
        options.outForegroundColor = color;
//        options.outGradientColor = -65528;
//        options.outGradientType = QRCodePixelOptions.GradientType.BACKSLASH;
//        options.outBorderType = QRCodePixelOptions.BorderType.ROUND;
        options.outShape = QRCodeOptions.Shape.WATER;
        options.outErrorCorrectionLevel = ErrorCorrectionLevel.M;
        options.outRadiuspercent = 0.7f;
        options.textSize = textSize;
        options.textContent = textContent;

        QRCodeGenerator QRCodeGenerator = new QRCodeGenerator(content);

        try {
            return QRCodeGenerator.generate(options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap mosaic(Bitmap original, int outWidth, int outHeight, int dot) {
        Bitmap bitmap = Bitmap.createScaledBitmap(original, outWidth, outHeight, false);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        Paint paint = new Paint();
        paint.setDither(true);

        int dotS = dot * dot;
        int w_count = outWidth / dot;
        int h_count = outHeight / dot;

        for (int i = 0; i < w_count; i++) {
            for (int j = 0; j < h_count; j++) {
                int rr = 0;
                int gg = 0;
                int bb = 0;
                for (int k = 0; k < dot; k++) {
                    for (int l = 0; l < dot; l++) {
                        int dotColor = bitmap.getPixel(i * dot + k, j * dot + l);
                        rr += Color.red(dotColor);
                        gg += Color.green(dotColor);
                        bb += Color.blue(dotColor);
                    }
                }
                rr = rr / dotS;
                gg = gg / dotS;
                bb = bb / dotS;
                paint.setColor(Color.rgb(rr, gg, bb));
                canvas.drawRect(new Rect(i * dot, j * dot, i * dot + dot, j * dot + dot), paint);
//                for (int k = 0; k < dot; k++) {
//                    for (int l = 0; l < dot; l++) {
//                        bitmap.setPixel(i * dot + k, j * dot + l, Color.rgb(rr, gg, bb));
//                    }
//                }
            }
        }

        return bitmap;
    }

    public static QRCode encodeQrcode(String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        QRCode code = null;
        try {
            code = Encoder.encode(content, ErrorCorrectionLevel.M, null);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return code;
    }


    /**
     * 二值化
     *
     * @param bitmap
     * @return
     */
    public static Bitmap binarization(Bitmap bitmap, int lowColor, int highColor) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixels[] = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        Binarizer binarizer = new HybridBinarizer(source);

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        try {
            BitMatrix matrix = binarizer.getBlackMatrix();
            for (int i = 0; i < height; i++) {
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

    /**
     * 如果有不同于 inputColor 的color，返回实际的color，否则返回input
     */
    public static int hasColorOnRect(Bitmap src, Rect serchRect, int inputColor) {
        int width = serchRect.width();
        int height = serchRect.height();

        if (width == 0 || height == 0) return inputColor;

        int color = inputColor;
        if (width == 1 && height == 1) {
            color = src.getPixel(serchRect.left, serchRect.top);
            return color != inputColor ? color : inputColor;
        }
        if (width == 1 && height > 1) {
            for (int start = 0; start < height; ++start) {
                color = src.getPixel(serchRect.left, serchRect.top + start);
                if (color != inputColor) return color;
            }
            return inputColor;
        }
        if (width > 1 && height == 1) {
            for (int start = 0; start < width; ++start) {
                color = src.getPixel(serchRect.left + start, serchRect.top);
                if (color != inputColor) return color;
            }
            return inputColor;
        }
        if (width > 1 && height > 1) {
            int colorMerge = 0xffffffff;
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
//                    Log.d("getColorOnBitmap", String.format("current searchX = (%s) and searchY = (%s)" +
//                                                                " x = (%s), y = (%s), Pixel Color = (%s)"
//                                                               , serchRect.left + x, serchRect.top + y
//                                                               , x, y
//                                                        , Integer.toHexString(src.getPixel(serchRect.left + x, serchRect.top + y))));
                    color = src.getPixel(serchRect.left + x, serchRect.top + y);
                    if (color != inputColor) return color;
                }
            }

            Log.d("getColorOnBitmap", "return Color : " + Integer.toHexString(colorMerge));

            return inputColor;
        }

        return inputColor;
    }

    public static int getColorOnBitmap(Bitmap src, Rect serchRect, int defaultColor) {
        int width = serchRect.width();
        int height = serchRect.height();

//        Log.d("getColorOnBitmap", String.format("bitmap width = (%s), height = (%s), Rect = (%s)", src.getWidth()
//                                                   , src.getHeight(), serchRect.toString()));

        if (width == 0 || height == 0) return defaultColor;

        if (width == 1 && height == 1) {
            return src.getPixel(serchRect.left, serchRect.top);
        }
        if (width == 1 && height > 1) {
            int colorMerge = 0xffffffff;
            for (int start = 0; start < height; ++start) {
                colorMerge = colorMerge & src.getPixel(serchRect.left, serchRect.top + start);
            }
            return colorMerge;
        }
        if (width > 1 && height == 1) {
            int colorMerge = 0xffffffff;
            for (int start = 0; start < width; ++start) {
                colorMerge = colorMerge & src.getPixel(serchRect.left + start, serchRect.top);
            }
            return colorMerge;
        }
        if (width > 1 && height > 1) {
            int colorMerge = 0xffffffff;
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
//                    Log.d("getColorOnBitmap", String.format("current searchX = (%s) and searchY = (%s)" +
//                                                                " x = (%s), y = (%s), Pixel Color = (%s)"
//                                                               , serchRect.left + x, serchRect.top + y
//                                                               , x, y
//                                                        , Integer.toHexString(src.getPixel(serchRect.left + x, serchRect.top + y))));
                    colorMerge = colorMerge & src.getPixel(serchRect.left + x, serchRect.top + y);
                }
            }

//            Log.d("getColorOnBitmap", "return Color : " + Integer.toHexString(colorMerge));

            return colorMerge;
        }

        return defaultColor;
    }

    public static boolean isInArea(int x, int y, Rect area) {
        return x > area.left && x < area.right && y > area.top && y < area.bottom;
    }

    public static boolean isAreaBounds(int x, int y, Rect area) {
        int width = area.right - area.left;
        int height = area.bottom - area.top;
        int centerX = area.left + width / 2;
        int centerY = area.top + height / 2;

        return Math.hypot(x - centerX, y - centerY) > Math.min(width, height) / 2;
    }

    public static Bitmap makeFaceQRCodeBt(int size, int color, Bitmap faceBmp) {
        int default_size = 500;

        int width = default_size;
        int height = default_size;
        float centerPercent = 0.4f;
        int purpleColor = Color.parseColor("#6700d8");

        QRCode qrcode = encodeQrcode("hello, worldskldf;klsfjlsjfsfjpweriupqwruwperuw");
        ByteMatrix input = qrcode.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth;
        int qrHeight = inputHeight;
        int outputWidth = Math.max(width, qrWidth);
        int outputHeight = Math.max(height, qrHeight);

        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

        RectF faceRect = new RectF();
        float faceScaleCoefficient = 1.0f;
        Bitmap detectFaceBmp = faceBmp.copy(Bitmap.Config.RGB_565, true);
        FaceDetector.Face faces[] = new FaceDetector.Face[1];
        FaceDetector detector = new FaceDetector(detectFaceBmp.getWidth(), detectFaceBmp.getHeight(), 1);
        int count = detector.findFaces(detectFaceBmp, faces);
        if (count > 0) {
            FaceDetector.Face face = faces[0];
            PointF centerPoint = new PointF();
            face.getMidPoint(centerPoint);
            float eyesDistance = face.eyesDistance();

            if (Math.abs(eyesDistance) > 0.000001) {
                float horizentalW = eyesDistance * 5 / 2;
                float verticalH = eyesDistance * 3 / (2 * 0.7f);
                faceRect.left = centerPoint.x - horizentalW / 2;
                faceRect.top = centerPoint.y - verticalH / 2;
                faceRect.right = centerPoint.x + horizentalW / 2;
                faceRect.bottom = centerPoint.y + verticalH / 2 + verticalH / 4;
            }
        }
        detectFaceBmp.recycle();
        boolean hasFace = faceRect.width() > 0;

        int maxCenterSize = (int) (width * centerPercent);
        int centerLeft = (width - maxCenterSize) / 2;
        Rect centerRect = new Rect(centerLeft, centerLeft, centerLeft + maxCenterSize, centerLeft + maxCenterSize);
        if (faceRect.width() > maxCenterSize) {
            faceScaleCoefficient = maxCenterSize / faceRect.width();
        }
        System.out.println("center size:" + maxCenterSize);
        System.out.println("faceScale:" + faceScaleCoefficient);
        System.out.println("facerect width:" + faceRect.width());

        Bitmap topBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(topBmp);
        canvas.drawColor(Color.argb(0, 0, 0, 0));
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(purpleColor);

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    if (isInArea(outputX, outputY, centerRect)) {
                        System.out.println("is In Area");
                        boolean needDrawn = true;
                        for (int i = 0; i < multiple; i++) {
                            for (int j = 0; j < multiple; j++) {
                                if (Math.hypot(outputX + j - width/2, outputY + i - height/2) < maxCenterSize / 2) {
                                    needDrawn = false;
                                    break;
                                }
                            }
                        }
                        if (needDrawn) {
                            canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                        }
                    } else {
                        canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                    }
                }
            }
        }

//        for (int row = centerLeft; row < centerLeft + maxCenterSize; row++) {
//            for (int column = centerLeft; column < centerLeft + maxCenterSize; column++) {
//                if (Math.hypot(row - width/2, column - height/2) < maxCenterSize / 2) {
//                    topBmp.setPixel(column, row, Color.argb(0, 0, 0, 0));
//                }
//            }
//        }

        float radius = multiple / 10.0f;
        //Bitmap scaledFaceBmp = Bitmap.createScaledBitmap(faceBmp, size, size, false);
        Bitmap scaledFaceBmp = binarization(scaleFaceBitmap(faceBmp, width, faceScaleCoefficient, faceRect), Color.WHITE, Color.parseColor("#ae4eff"));
        Bitmap bottomBmp = scaledFaceBmp;
//        Bitmap bottomBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        canvas = new Canvas(bottomBmp);
//        canvas.drawColor(purpleColor);
//        paint.setColor(Color.WHITE);
//
//        int colorInBitmap;
//        for (int row = 0; row < height; row++) {
//            for (int column = 0; column < width; column++) {
//                colorInBitmap = scaledFaceBmp.getPixel(column, row);
//                if (isSkinArea(colorInBitmap)) {
//                    bottomBmp.setPixel(column, row, Color.WHITE);
//                }
//            }
//        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.RED);

        canvas.drawBitmap(bottomBmp, 0, 0, null);
        saveBitmapToFile(bottomBmp,"/sdcard/test/bottom.jpg");
        canvas.drawBitmap(topBmp, 0, 0, null);
        return bitmap;
        //return bottomBmp;
    }

    public static Bitmap scaleFaceBitmap(Bitmap faceBmp, int size, float faceScaleCoefficient, RectF faceRect) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.parseColor("#6700d8"));
        Canvas canvas = new Canvas(bitmap);
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setColor(Color.WHITE);

//        float radius = 10.0f;
//        for (float row = 0; row < size; row+=radius) {
//            for (float column = 0; column < size; column+=radius) {
//                canvas.drawCircle(column + radius, row + radius, radius, paint);
//            }
//        }

        int scaledWidth = (int) (faceScaleCoefficient * faceBmp.getWidth());
        int scaledHeight = (int) (faceScaleCoefficient * faceBmp.getHeight());
        int posX = (int) (size / 2 - ((faceRect.left + faceRect.width() / 2) * faceScaleCoefficient));
        int posY = (int) (size / 2 - ((faceRect.top + faceRect.height() / 2) * faceScaleCoefficient));
        Bitmap scaledFaceBmp = Bitmap.createScaledBitmap(faceBmp, scaledWidth, scaledHeight, false);
        canvas.drawBitmap(scaledFaceBmp, posX, posY, null);

//        int colorInBitmap;
//        for (int row = 0; row < size; row++) {
//            for (int column = 0; column < size; column++) {
//                colorInBitmap = bitmap.getPixel(column, row);
//                bitmap.setPixel(column, row, addGrayToColor(colorInBitmap, 0.8f));
//            }
//        }

        return bitmap;
    }

    public static int addGrayToColor(int color, float percent) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        r = (int) (r * percent + 255 * (1 - percent));
        g = (int) (g * percent + 255 * (1 - percent));
        b = (int) (b * percent + 255 * (1 - percent));
        return Color.argb(0xFF, r, g, b);
    }

    public static boolean saveBitmapToFile(Bitmap bitmap, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(path));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isInCenterArea(int size, int row, int column) {
        int centerSize = (int) (size * 0.7f);
        int left = (size - centerSize) / 2;
        int right = (size + centerSize) / 2;
        return row > left && row < right && column > left && column < right;
    }

    public static boolean isSkinArea(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        if (r < 95 || g < 40 || b < 20) {
            return false;
        }
        if (Math.max(Math.max(r, g), b) - Math.min(Math.min(r, g), b) < 15) {
            return false;
        }
        if (Math.abs(r - g) < 15 || r < g || r < b) {
            return false;
        }
        return true;
    }

}

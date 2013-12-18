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
//        options.outGradientType = QRCodeOptions.GradientType.BACKSLASH;
//        options.outBorderType = QRCodeOptions.BorderType.ROUND;
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
//        options.outGradientType = QRCodeOptions.GradientType.BACKSLASH;
//        options.outBorderType = QRCodeOptions.BorderType.ROUND;
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

    public static Bitmap mosaic(Bitmap original, int dot) {
//        Bitmap bitmap = original.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        Paint paint = new Paint();
        paint.setDither(true);

        int dotS = dot * dot;
        int w = original.getWidth();
        int h = original.getHeight();
        for (int i = 0; i < w / dot; i++) {
            for (int j = 0; j < h / dot; j++) {
                int rr = 0;
                int gg = 0;
                int bb = 0;
                for (int k = 0; k < dot; k++) {
                    for (int l = 0; l < dot; l++) {
                        int dotColor = original.getPixel(i * dot + k, j * dot + l);
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
            code = Encoder.encode(content, ErrorCorrectionLevel.H, null);
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
            FileOutputStream fos = new FileOutputStream(new File("/sdcard/test/out.jpg"));
            result.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
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
}

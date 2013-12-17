package com.qrcode.sdk;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

/**
 * Created by michael on 13-12-17.
 */
public class QRCodeUtil {

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


}

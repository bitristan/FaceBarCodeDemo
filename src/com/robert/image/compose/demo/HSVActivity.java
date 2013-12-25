package com.robert.image.compose.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by michael on 13-12-25.
 */
public class HSVActivity extends Activity {

    private ImageView mImage;
    private EditText mH;
    private EditText mS;
    private EditText mV;
    private View make;

    private int startColor = 0xff27b52a;
    private int endColor = 0xff1ca08a;

    private float[] hsvAdjust = new float[3];
    private Bitmap grayImage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hsv_just);
        mImage = (ImageView) findViewById(R.id.image);
        mH = (EditText) findViewById(R.id.hue);
        mS = (EditText) findViewById(R.id.su);
        mV = (EditText) findViewById(R.id.bright);
        grayImage = ((BitmapDrawable) getResources().getDrawable(R.drawable.test_hsv)).getBitmap();
        grayImage = convertGrayImg(grayImage);
        mImage.setImageBitmap(grayImage);

        mH.setText("120.0");
        mS.setText("0.0");
        mV.setText("0.0");

        make = findViewById(R.id.make);

        make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hsvAdjust[0] = Float.valueOf(mH.getText().toString());
                hsvAdjust[1] = Float.valueOf(mS.getText().toString());
                hsvAdjust[2] = Float.valueOf(mV.getText().toString());

//                final Bitmap bt = covertBitmapWithHSB(grayImage);
                Bitmap bt = bitmapHSB(grayImage, startColor, endColor);
                mImage.setImageBitmap(bt);
                Toast.makeText(getApplicationContext(), "调整成功", Toast.LENGTH_LONG).show();
            }
        });
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

    public Bitmap covertBitmapWithHSBWithHChanged(Bitmap bt, float[] startHSVAdjust, float[] endHSVAdjust) {
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

    public Bitmap covertBitmapWithHSB(Bitmap bt) {
        int w = bt.getWidth(), h = bt.getHeight();
        int[] pix = new int[w * h];
        bt.getPixels(pix, 0, w, 0, 0, w, h);

        float[] pixelHSV = new float[3];
        int alpha = 0xFF << 24;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int color = pix[w * i + j];

                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                Color.RGBToHSV(red, green, blue, pixelHSV);

                pixelHSV[0] = hsvAdjust[0];
                pixelHSV[1] = pixelHSV[1] + hsvAdjust[1];
//                if (pixelHSV[1] < 0.0f) {
//                    pixelHSV[1] = 0.0f;
//                } else if (pixelHSV[1] > 1.0f) {
//                    pixelHSV[1] = 1.0f;
//                }
//
//                pixelHSV[2] = pixelHSV[2] + hsvAdjust[2];
//                if (pixelHSV[2] < 0.0f) {
//                    pixelHSV[2] = 0.0f;
//                } else if (pixelHSV[2] > 1.0f) {
//                    pixelHSV[2] = 1.0f;
//                }

                color = Color.HSVToColor(pixelHSV);
                color = alpha | color;
                pix[w * i + j] = color;
            }
        }

        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        result.setPixels(pix, 0, w, 0, 0, w, h);
        return result;
    }
}
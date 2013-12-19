package com.robert.image.compose.demo;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;


/**
 * Created by michael on 13-12-18.
 */
public class PixelActivity extends Activity {

    private ImageView mInImageView;

    private ImageView mOutImageView;

    private Handler mHandler = new Handler(Looper.myLooper());

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pixel_activity);
        mInImageView = (ImageView) findViewById(R.id.in);
        mOutImageView = (ImageView) findViewById(R.id.out);

        asyncLoadImage(Environment.getExternalStorageDirectory() + "/test/009.jpg");
    }

    private void asyncLoadImage(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap org = BitmapFactory.decodeFile(path);
                if (org != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            long begin = System.currentTimeMillis();
                            Log.d("asyncLoadImage", "[[PixelActivity::asyncLoadImage]] BEGIN current time : " + begin);
//                            Bitmap pixel = QRCodeUtils.mosaic(org, 100);
                            long end = System.currentTimeMillis();
                            Log.d("asyncLoadImage", "[[PixelActivity::asyncLoadImage]] END current time : " + end);
                            Log.d("asyncLoadImage", "[[PixelActivity::asyncLoadImage]] cost : (" + (end - begin)/1000 + ")s");
                            mInImageView.setImageBitmap(org);
                            mOutImageView.setImageBitmap(makePixelQRCode(Config.QRCODE_CONTENT, org, Config.QRCODE_DEFAULT_SIZE));
//                            mInImageView.setImageBitmap(QRCodeUtils.mosaic(Bitmap.createScaledBitmap(org, Config.QRCODE_DEFAULT_SIZE, Config.QRCODE_DEFAULT_SIZE, false), 13));
                        }
                    });
                }
            }
        }).start();
    }

    private Bitmap makePixelQRCode(String qrContent, Bitmap background, int defaultQRCodeSize) {
        QRCode qrCode = QRCodeUtils.encodeQrcode(qrContent);

        ByteMatrix input = qrCode.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth;
        int qrHeight = inputHeight;
        int outputWidth = Math.max(defaultQRCodeSize, qrWidth);
        int outputHeight = Math.max(defaultQRCodeSize, qrHeight);

        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);

        //四周各空两个点整
        int realWidth = multiple * (inputWidth + 4);
        int realHeight = multiple * (inputHeight + 4);

        int leftPadding = multiple * 2;
        int topPadding = multiple * 2;

        Bitmap pixelBt = QRCodeUtils.mosaic(background, realWidth, realHeight, multiple);
        pixelBt.setHasAlpha(true);

        Bitmap out = Bitmap.createBitmap(realWidth, realHeight, Bitmap.Config.ARGB_8888);
        out.setHasAlpha(true);
        Canvas canvas = new Canvas(out);
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
        cMatrix.set(new float[] { contrast, 0, 0, 0, 0, 0,
                                    contrast, 0, 0, 0,// 改变对比度
                                    0, 0, contrast, 0, 0, 0, 0, 0, 1, 0 });
        allMatrix.postConcat(cMatrix);
//
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        canvas.drawBitmap(pixelBt, 0, 0, paint);
        paint.setColorFilter(null);
        paint.setAlpha(160);

        Rect box = new Rect();

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                box.left = outputX;
                box.right = outputX + multiple;
                box.top = outputY;
                box.bottom = outputY + multiple;

//                if (isInArea(outputX, outputY, centerArea) && qrcodeFaceBmp != null && !isAreaBounds(outputX, outputY, centerArea)) {
//                    canvas.drawBitmap(qrcodeFaceBmp, faceBox, box, paint);
//                } else {
                    if (input.get(inputX, inputY) == 1) {
                        canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                    }
//                }
            }
        }

        return out;
    }

}
package com.robert.image.compose.demo;

import android.app.Activity;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.com.robert.image.demonew.Config;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;
import com.qrcode.r.sdk.QRCodeGenerator;
import com.qrcode.r.sdk.QRCodeOptionsInterface;
import com.qrcode.r.sdk.QRCodePixelOptions;

/**
 * Created by michael on 13-12-18.
 */
public class PixelActivity extends Activity {

    private ImageView mInImageView;
    private ImageView mOutImageView;
    private ImageView mInImageView1;
    private ImageView mOutImageView1;

    private Bitmap mQRCodeBt;
    private Bitmap mQRCodeBt1;

    private ViewPager mViewPager;

    private Handler mHandler = new Handler(Looper.myLooper());

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pixel_activity);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object o) {
                return view == o;
            }

            @Override
            public void destroyItem(View arg0, int arg1, Object arg2) {
                ((ViewGroup) arg0).removeView((View) arg2);
            }

            @Override
            public Object instantiateItem(View arg0, int pos) {
                View ret = getLayoutInflater().inflate(R.layout.pixel_one, null);
                switch (pos) {
                    case 0:
                        mOutImageView = (ImageView) ret.findViewById(R.id.out);
                        mInImageView = (ImageView) ret.findViewById(R.id.in);
                        break;
                    case 1:
                        mOutImageView1 = (ImageView) ret.findViewById(R.id.out);
                        mInImageView1 = (ImageView) ret.findViewById(R.id.in);
                        break;
                }
                ((ViewPager) arg0).addView(ret);

                return ret;
            }
        });

        mInImageView = (ImageView) findViewById(R.id.in);
        mOutImageView = (ImageView) findViewById(R.id.out);

        asyncLoadImage(Environment.getExternalStorageDirectory() + "/test/test.jpg");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pixedl, menu);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
//                File outPath = new File("/sdcard/qrcode_image/");
//                if (!outPath.exists()) {
//                    outPath.mkdir();
//                }
//                Utils.saveBitmapToFile(mQRCodeBt, outPath.getAbsolutePath() + "/pixel.jpg");
                new Thread(new Utils.SaveRunnable(getApplicationContext(), mQRCodeBt)).start();

                Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
                break;
        }

        return true;
    }

    private void asyncLoadImage(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                final Bitmap org = BitmapFactory.decodeFile(path);
                final Bitmap org = (Bitmap) ((BitmapDrawable) (getResources().getDrawable(R.drawable.hehua))).getBitmap();
                if (org != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            long begin = System.currentTimeMillis();
                            Log.d("asyncLoadImage", "[[PixelActivity::asyncLoadImage]] BEGIN current time : " + begin);
//                            Bitmap pixel = QRCodeUtils.mosaic(org, 100);
                            long end = System.currentTimeMillis();
                            Log.d("asyncLoadImage", "[[PixelActivity::asyncLoadImage]] END current time : " + end);
                            Log.d("asyncLoadImage", "[[PixelActivity::asyncLoadImage]] cost : (" + (end - begin) / 1000 + ")s");
                            mInImageView.setImageBitmap(org);

                            QRCodePixelOptions opt = new QRCodePixelOptions();
                            opt.backgroundBitmap = org;
                            opt.qrCodeRelaeseEffect = QRCodeOptionsInterface.QRCodePixelReleaseEffect.PIXEL;
                            opt.qrContent = Config.QRCODE_CONTENT;
                            opt.defaultQRSize = Config.QRCODE_DEFAULT_SIZE;
//                            opt.errorLevel = ErrorCorrectionLevel;
                            opt.maskBitmap = (Bitmap) ((BitmapDrawable) (getResources().getDrawable(R.drawable.aaa))).getBitmap();
                            opt.maskRectCount = 3;
//                            opt.frontBitmap = (Bitmap) ((BitmapDrawable) (getResources().getDrawable(R.drawable.pre_f_1))).getBitmap();
                            QRCodeGenerator.createQRCode(opt);
                            mQRCodeBt = QRCodeGenerator.createQRCode(opt);
                            mOutImageView.setImageBitmap(mQRCodeBt);

                            QRCodePixelOptions opt1 = new QRCodePixelOptions();
                            opt1.backgroundBitmap = org;
                            opt1.qrCodeRelaeseEffect = QRCodeOptionsInterface.QRCodePixelReleaseEffect.PIXEL_Border;
                            opt1.qrContent = Config.QRCODE_CONTENT;
                            opt1.defaultQRSize = Config.QRCODE_DEFAULT_SIZE;
                            opt1.errorLevel = ErrorCorrectionLevel.M;
//                            opt1.maskBackground = true;
                            QRCodeGenerator.createQRCode(opt1);
                            mQRCodeBt1 = QRCodeGenerator.createQRCode(opt1);
//                            QRCodePixelOptions opt1 = new QRCodePixelOptions();
//                            opt1.backgroundBitmap = org;
//                            opt1.qrCodeRelaeseEffect = QRCodeOptionsInterface.QRCodePixelRelaeseEffect.PIXEL;
//                            opt1.qrContent = Config.QRCODE_CONTENT;
//                            opt1.defaultQRSize = Config.QRCODE_DEFAULT_SIZE;
//                            opt1.preBt = (Bitmap) ((BitmapDrawable) (getResources().getDrawable(R.drawable.pre3))).getBitmap();
//                            opt1.preSize = 3;
//                            opt1.preF = (Bitmap) ((BitmapDrawable) (getResources().getDrawable(R.drawable.pre_f))).getBitmap();
                            mOutImageView1.setImageBitmap(mQRCodeBt1);
                            mInImageView1.setImageBitmap(org);
                        }
                    });
                }
            }
        }).start();
    }

    private Bitmap makePixelQRCode(String qrContent, Bitmap background, int defaultQRCodeSize) {
        //preview bt
        Bitmap pre = ((BitmapDrawable) getResources().getDrawable(R.drawable.pre)).getBitmap();


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
        float contrast = (float) (140 / 128.0);
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[]{contrast, 0, 0, 0, 0, 0,
                                   contrast, 0, 0, 0,// 改变对比度
                                   0, 0, contrast, 0, 0, 0, 0, 0, 1, 0});
        allMatrix.postConcat(cMatrix);
//
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        canvas.drawBitmap(pixelBt, 0, 0, paint);

//        paint.setAlpha(100);
//        Rect preRect = new Rect(0, 0, pre.getWidth(), pre.getHeight());
//        canvas.drawBitmap(pre, preRect, new Rect(0, 0, realWidth, realHeight), paint);

        paint.setColorFilter(null);
        paint.setAlpha(160);

        Rect box = new Rect();

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                box.left = outputX;
                box.right = outputX + multiple;
                box.top = outputY;
                box.bottom = outputY + multiple;

                if (input.get(inputX, inputY) == 1) {
                    canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                }
            }
        }

        return out;
    }

}
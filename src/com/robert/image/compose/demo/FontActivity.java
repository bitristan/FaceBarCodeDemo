package com.robert.image.compose.demo;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.ArrayList;

/**
 * Created by michael on 13-12-17.
 */
public class FontActivity extends Activity implements View.OnClickListener {

    private static final String QRCODE_DEFAULT_CONTENT = "BEGIN:VCARD\nVERSION:3.0\nFN:Ting\nPHOTO;VALUE=uri:http://tp3.sinaimg.cn/1668659954/180/5679291057/1\nTEL;CELL;VOICE:18612560521\nURL:http://lzem.me\nEND:VCARD";
    private static final int QRCODE_DEFAULT_SIZE = 500;
    private static final float QRCODE_CENTER_AREA_PERCENTAGE = 0.6f;

    private ViewPager mViewPager;

    private EditText mInput;

    private Button mButton;

    private ActionBar mActionBar;

    private PagerAdapter mPagerAdapter;

    private int mCurrentSelectColort = Color.RED;

    private ArrayList<Bitmap> mData = new ArrayList<Bitmap>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.font);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mInput = (EditText) findViewById(R.id.text);
        mButton = (Button) findViewById(R.id.ok);

        mActionBar = getActionBar();
        mActionBar.setTitle("字体混合");

        mButton.setOnClickListener(this);

        mInput.setText("疆");
        initUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mData.clear();
        mViewPager.setAdapter(null);
        mPagerAdapter = null;
    }

    private void initUI() {
        findViewById(R.id.c1).setOnClickListener(this);
        findViewById(R.id.c2).setOnClickListener(this);
        findViewById(R.id.c3).setOnClickListener(this);
        findViewById(R.id.c4).setOnClickListener(this);
        findViewById(R.id.c5).setOnClickListener(this);
        findViewById(R.id.c6).setOnClickListener(this);

        asyncUpdateUI();
    }

    private void asyncUpdateUI() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String text = mInput.getText().toString();
                showQRCode(text);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPagerAdapter = new QRImageAdapter(getLayoutInflater(), mData);
                        mViewPager.setAdapter(mPagerAdapter);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        String text = mInput.getText().toString();
        switch (v.getId()) {
            case R.id.ok:
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(this, "输入不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                mCurrentSelectColort = ((ColorDrawable)v.getBackground()).getColor();
        }

        asyncUpdateUI();
    }

    private void showQRCode(String text) {
        QRCode qrCode = QRCodeUtils.encodeQrcode(QRCODE_DEFAULT_CONTENT);
        Bitmap show = composeBinarization1(generateFontBt(text, QRCODE_DEFAULT_SIZE, mCurrentSelectColort), qrCode, mCurrentSelectColort);
        Bitmap ret = QRCodeUtils.makeWaterQRCodeBt(QRCODE_DEFAULT_CONTENT, QRCODE_DEFAULT_SIZE, mCurrentSelectColort, text, 150);
        Bitmap ret1 = QRCodeUtils.makePointQRCodeBt(QRCODE_DEFAULT_CONTENT, QRCODE_DEFAULT_SIZE, mCurrentSelectColort, text, 150);
        mData.clear();
        mData.add(show);
        mData.add(ret);
        mData.add(ret1);
    }

    public Bitmap generateFontBt(String text, int btSize, int color) {
        Bitmap ret = Bitmap.createBitmap(btSize, btSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(ret);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setTextSize(200);
        paint.setTextAlign(Paint.Align.CENTER);

//        Rect textRect = new Rect();
//        paint.getTextBounds(text, 0, 1, textRect);

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        // 计算文字高度
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        // 计算文字baseline
        float textBaseY = btSize - (btSize - fontHeight) / 2 - fontMetrics.bottom;

        canvas.drawText(text, btSize / 2, textBaseY, paint);

        return ret;
    }

//    public Bitmap composeBinarization2(Bitmap mergeBitmap, QRCode qrcode, int color) {
//        Bitmap ret = QRCodeUtils.makeWaterQRCodeBt(QRCODE_DEFAULT_CONTENT, QRCODE_DEFAULT_SIZE, mCurrentSelectColort);
//
//        Canvas canvas = new Canvas(ret);
//        Paint paint = new Paint();
//        paint.setDither(true);
//        paint.setAntiAlias(true);
//        paint.setColor(color);
//        canvas.drawBitmap(mergeBitmap, 0, 0, paint);
//
//        return ret;
//    }

    public Bitmap composeBinarization1(Bitmap mergeBitmap, QRCode qrcode, int color) {
        int width = QRCODE_DEFAULT_SIZE;
        int height = QRCODE_DEFAULT_SIZE;

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

//        Log.d("composeBinarization1", String.format("multiple = (%s), leftPadding = (%s), topPadding = (%s)", multiple, leftPadding, topPadding));

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(color);

        Rect box = new Rect();

        canvas.drawBitmap(mergeBitmap, 0, 0, paint);

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                box.left = outputX;
                box.right = outputX + multiple;
                box.top = outputY;
                box.bottom = outputY + multiple;

                int colorOnMergeBt = QRCodeUtils.getColorOnBitmap(mergeBitmap, box, Color.WHITE);
//                Log.d("composeBinarization1", "search color : " + Integer.toHexString(colorOnMergeBt));
                if (colorOnMergeBt == Color.WHITE) {
                    paint.setColor(color);
                    if (input.get(inputX, inputY) == 1) {
                        canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                    }
                }
//                else {
//                    paint.setColor(colorOnMergeBt);
//                    canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
//                }
            }
        }

        return bitmap;
    }

    public Bitmap composeBinarization(Bitmap mergeBitmap, QRCode qrcode, int color) {
        int width = QRCODE_DEFAULT_SIZE;
        int height = QRCODE_DEFAULT_SIZE;

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

        int centerSize = (int) (width * QRCODE_CENTER_AREA_PERCENTAGE);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap qrcodeFaceBmp = null;
        qrcodeFaceBmp = Bitmap.createScaledBitmap(mergeBitmap, centerSize, centerSize, false);
        qrcodeFaceBmp = QRCodeUtils.binarization(qrcodeFaceBmp, Color.argb(0xFF, 0xFF, 0xFF, 0xFF), Color.argb(0xFF, 0xFF, 0x00, 0x00));
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(color);

        Rect box = new Rect();
        Rect faceBox = new Rect();
        Rect centerArea = new Rect((width - centerSize) / 2, (height - centerSize) / 2, (width + centerSize) / 2, (height + centerSize) / 2);

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                box.left = outputX;
                box.right = outputX + multiple;
                box.top = outputY;
                box.bottom = outputY + multiple;

                faceBox.left = outputX - centerArea.left;
                faceBox.top = outputY - centerArea.top;
                faceBox.right = faceBox.left + multiple;
                faceBox.bottom = faceBox.top + multiple;

                if (QRCodeUtils.isInArea(outputX, outputY, centerArea) && qrcodeFaceBmp != null
                        && !QRCodeUtils.isAreaBounds(outputX, outputY, centerArea)) {
                    canvas.drawBitmap(qrcodeFaceBmp, faceBox, box, paint);
                } else {
                    if (input.get(inputX, inputY) == 1) {
                        canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                    }
                }
            }
        }

        return bitmap;
    }
}
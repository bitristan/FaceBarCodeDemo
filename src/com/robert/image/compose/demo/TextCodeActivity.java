package com.robert.image.compose.demo;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

public class TextCodeActivity extends Activity implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {
    private static final String QRCODE_DEFAULT_CONTENT = "BEGIN:VCARD\nVERSION:3.0\nFN:Ting\nPHOTO;VALUE=uri:http://tp3.sinaimg.cn/1668659954/180/5679291057/1\nTEL;CELL;VOICE:18612560521\nURL:http://lzem.me\nEND:VCARD";
    private static final int QRCODE_DEFAULT_SIZE = 500;
    private static final float QRCODE_CENTER_AREA_PERCENTAGE = 0.7f;

    private static final String DEFAULT_TEXT = "龗";
    private static final float DEFAULT_TEXTSIZE = 300.0f;

    Button mGenerateBt;
    EditText mContentEt;

    ImageView mPreviewIv;
    SeekBar mTextsizeSb;

    float mTextSize;

    Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.textcode);

        mGenerateBt = (Button) findViewById(R.id.generate_bt);
        mContentEt = (EditText) findViewById(R.id.content_et);
        mPreviewIv = (ImageView) findViewById(R.id.preview_iv);
        mTextsizeSb = (SeekBar) findViewById(R.id.textsize_sb);

        mGenerateBt.setOnClickListener(this);
        mPreviewIv.setOnTouchListener(this);
        mTextsizeSb.setOnSeekBarChangeListener(this);

        mContentEt.setText(DEFAULT_TEXT);
        mTextsizeSb.setMax(QRCODE_DEFAULT_SIZE);
        mTextsizeSb.setProgress((int) DEFAULT_TEXTSIZE);

        showQrcode();
    }

    public Bitmap generateBitmapWithText() {
        String text = null;
        if (mContentEt.getText() != null) {
            text = mContentEt.getText().toString();
        }
        if (TextUtils.isEmpty(text)) {
            text = DEFAULT_TEXT;
        } else {
            text = text.substring(0, 1);
        }

        //int width = (int) (QRCODE_DEFAULT_SIZE * QRCODE_CENTER_AREA_PERCENTAGE);
        int width = QRCODE_DEFAULT_SIZE;
        Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(false);
        paint.setColor(Color.BLACK);
        paint.setTextSize(mTextSize);

        int xPos = (canvas.getWidth() - (int) paint.measureText(text)) / 2;
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        //canvas.drawText(text, xPos, yPos, paint);

        int border = 5;
        paint.setColor(Color.RED);
        canvas.drawText(text, xPos + border, yPos, paint);
        canvas.drawText(text, xPos - border, yPos, paint);
        canvas.drawText(text, xPos, yPos + border, paint);
        canvas.drawText(text, xPos, yPos + border, paint);
        canvas.drawText(text, xPos + border, yPos + border, paint);
        canvas.drawText(text, xPos + border, yPos - border, paint);
        canvas.drawText(text, xPos - border, yPos + border, paint);
        canvas.drawText(text, xPos - border, yPos - border, paint);

        paint.setColor(Color.BLACK);
        canvas.drawText(text, xPos, yPos, paint);

        return bitmap;
    }

    public QRCode encodeQrcode(String content) {
        if (content == null) {
            content = QRCODE_DEFAULT_CONTENT;
        }

        QRCode code = null;
        try {
            code = Encoder.encode(content, ErrorCorrectionLevel.H, null);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return code;
    }

    public Bitmap composeBinarization() {
        int width = QRCODE_DEFAULT_SIZE;
        int height = QRCODE_DEFAULT_SIZE;

        QRCode qrcode = encodeQrcode(null);
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

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);

        Bitmap textBitmap = generateBitmapWithText();
        canvas.drawBitmap(textBitmap, 0, 0, paint);

        Rect box = new Rect();

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                box.left = outputX;
                box.right = outputX + multiple;
                box.top = outputY;
                box.bottom = outputY + multiple;

                int colorOnMergeBt = QRCodeUtils.getColorOnBitmap(textBitmap, box, Color.WHITE);
                if (colorOnMergeBt == Color.WHITE) {
                    if (input.get(inputX, inputY) == 1) {
                        canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                    }
                }
            }
        }

        return bitmap;
    }

    public void showQrcode() {
        mPreviewIv.setImageBitmap(composeBinarization());
    }

    public void justShowText() {
        mPreviewIv.setImageBitmap(generateBitmapWithText());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.generate_bt:
                showQrcode();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.preview_iv) {
            switch ((event.getAction() & MotionEvent.ACTION_MASK)) {
                case MotionEvent.ACTION_DOWN:
                    justShowText();
                    break;
                case MotionEvent.ACTION_UP:
                    showQrcode();
                    break;
            }

            return true;
        }

        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTextSize = progress;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showQrcode();
            }
        }, 1000);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
package com.robert.image.compose.demo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import com.qrcode.sdk.QRCodeGenerator;
import com.qrcode.sdk.QRCodeOptions;

import java.io.*;

public class FaceComposeActivity extends Activity implements View.OnClickListener {
    private static final int MAX_FACES = 1;

    private static final String QRCODE_DEFAULT_CONTENT = "BEGIN:VCARD\nVERSION:3.0\nFN:Ting\nPHOTO;VALUE=uri:http://tp3.sinaimg.cn/1668659954/180/5679291057/1\nTEL;CELL;VOICE:18612560521\nURL:http://lzem.me\nEND:VCARD";
    private static final int QRCODE_DEFAULT_SIZE = 500;
    private static final float QRCODE_CENTER_AREA_PERCENTAGE = 0.4f;

    private static final String IMAGE_DIR = "image";

    private static final int TAKE_PICTURE = 10000;

    String images[];

    ImageView mPreviewIv;
    ImageView mOriginalIv;

    Button mLeftBt;
    Button mRightBt;

    Bitmap mOriginal;
    int mCurrentImageIndex = 0;

    PointF mCenterPoint = new PointF();
    float mEyeDistance;
    BitMatrix mSkinArea;

    boolean mStepForward = true;

    private ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mActionBar = getActionBar();

        initImages();

        mPreviewIv = (ImageView) findViewById(R.id.preview_iv);
        mOriginalIv = (ImageView) findViewById(R.id.original_iv);

        mLeftBt = (Button) findViewById(R.id.left_bt);
        mRightBt = (Button) findViewById(R.id.right_bt);

        mPreviewIv.setClickable(true);
        mPreviewIv.setOnClickListener(this);
        mPreviewIv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                    loadOriginalBitmap(mCurrentImageIndex, new Point((int) event.getX(), (int) event.getY()));
                }
                return false;
            }
        });
        mPreviewIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                loadOriginalBitmap(mCurrentImageIndex, null);
                return true;
            }
        });

        mLeftBt.setOnClickListener(this);
        mRightBt.setOnClickListener(this);

        loadOriginalBitmap(mCurrentImageIndex, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
                    mOriginal = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/test/1000.jpg");
                    mOriginalIv.setImageBitmap(mOriginal);

                    QRCode qrcode = encodeQrcode(null);
                    Bitmap composedBmp = composeBinarization(null, qrcode);
                    mPreviewIv.setImageBitmap(composedBmp);
                    break;
            }
        }
    }

    private void initImages() {
        try {
            images = getAssets().list(IMAGE_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOriginalBitmap(int index, Point point) {
        if (images == null) {
            return;
        }

        mCurrentImageIndex = (index + images.length) % images.length;
        String path = IMAGE_DIR + "/" + images[mCurrentImageIndex];

        try {
            InputStream stream = getAssets().open(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);
            if (options.outWidth > 800 || options.outHeight > 800) {
                options.inSampleSize = 2;
            }
            options.inJustDecodeBounds = false;
            mOriginal = BitmapFactory.decodeStream(stream, null, options);
            mOriginalIv.setImageBitmap(mOriginal);

            long start = System.nanoTime();

//            Bitmap faceAreaBmp = cutFace();
//            if (faceAreaBmp != null) {
//                mSkinArea = detectSkinArea(faceAreaBmp);
//                faceAreaBmp = showSkinArea(mSkinArea);
//            }

//            detectFace();
//            Bitmap backgroundBmp = binarization(mOriginal);
            QRCode qrcode = encodeQrcode(null);
            //Bitmap composedBmp = composeBinarization(null, qrcode);
            Bitmap composedBmp = makeFaceQRCodeBt(mOriginal, point);
            mPreviewIv.setImageBitmap(composedBmp);


//            Bitmap bitmap = showSkinArea(mSkinArea);
//            mPreviewIv.setImageBitmap(bitmap);
            long end = System.nanoTime();
            System.out.println("total time is: " + (end - start));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap cutFace() {
        int count = detectFace();
        if (count <= 0) {
            return null;
        }

        RectF rectF = caculateFaceRect();
        Rect rect = new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);

        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mOriginal, rect, new RectF(0, 0, width, height), null);
        return bitmap;
    }

    public RectF caculateFaceRect() {
        RectF rect = new RectF();

        if (Math.abs(mEyeDistance) < 0.000001) {
            return rect;
        }

        float width = mEyeDistance * 5 / 2;
        float height = mEyeDistance * 3 / (2 * 0.7f);
        rect.left = mCenterPoint.x - width / 2;
        rect.top = mCenterPoint.y - height / 2;
        rect.right = mCenterPoint.x + width / 2;
        rect.bottom = mCenterPoint.y + height / 2 + height / 4;

        return rect;
    }

    private boolean isFace(int outputX, int outputY, RectF faceArea) {
//        if (!mSkinArea.get(outputX, outputY)) {
//            return false;
//        }

        if (outputX > faceArea.left && outputX < faceArea.right && outputY > faceArea.top
                && outputY < faceArea.bottom) {
            return true;
        }

        return false;
    }

    public Bitmap composeBinarization(Bitmap backgroundBmp, QRCode qrcode) {
        //int width = backgroundBmp.getWidth();
        //int height = backgroundBmp.getHeight();
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
        Bitmap faceAreaBmp = cutFace();
        if (faceAreaBmp == null) {
            faceAreaBmp = mOriginal;
        }
        Bitmap qrcodeFaceBmp = null;
        qrcodeFaceBmp = Bitmap.createScaledBitmap(faceAreaBmp, centerSize, centerSize, false);
        qrcodeFaceBmp = binarization(qrcodeFaceBmp);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

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

                if (isInArea(outputX, outputY, centerArea) && qrcodeFaceBmp != null && !isAreaBounds(outputX, outputY, centerArea)) {
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

    public Bitmap binarization(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixels[] = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        Binarizer binarizer = new HybridBinarizer(source);
        //Binarizer binarizer = new GlobalHistogramBinarizer(source);

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        try {
            BitMatrix matrix = binarizer.getBlackMatrix();
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (matrix.get(j, i)) {
                        result.setPixel(j, i, Color.argb(0xFF, 0xFF, 0x00, 0x00));
                    } else {
                        result.setPixel(j, i, Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
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

    private int detectFace() {
        Bitmap bitmap = mOriginal.copy(Bitmap.Config.RGB_565, true);

        FaceDetector.Face faces[] = new FaceDetector.Face[MAX_FACES];
        FaceDetector detector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), MAX_FACES);
        int count = detector.findFaces(bitmap, faces);
        if (count > 0) {
            FaceDetector.Face face = faces[0];
            face.getMidPoint(mCenterPoint);
            mEyeDistance = face.eyesDistance();
        } else {
            mCenterPoint.x = 0.0f;
            mCenterPoint.y = 0.0f;
            mEyeDistance = 0.0f;
        }

        bitmap.recycle();
        bitmap = null;

        return count;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.preview_iv:
                break;
            case R.id.left_bt:
                mStepForward = false;
                loadOriginalBitmap(mCurrentImageIndex - 1, null);
                break;
            case R.id.right_bt:
                mStepForward = true;
                loadOriginalBitmap(mCurrentImageIndex + 1, null);
                break;
            default:
                break;
        }
    }

    private Bitmap showSkinArea(BitMatrix skinAreaMatrix) {
        int width = skinAreaMatrix.getWidth();
        int height = skinAreaMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (skinAreaMatrix.get(j, i)) {
                    bitmap.setPixel(j, i, Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
                } else {
                    bitmap.setPixel(j, i, Color.argb(0xFF, 0x00, 0x00, 0x00));
                }
            }
        }
        return bitmap;
    }

    private BitMatrix detectSkinArea(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap target = bitmap;
        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            target = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
        BitMatrix matrix = new BitMatrix(width, height);

        int color;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                color = target.getPixel(j, i);
                if (isSkinArea(color)) {
                    matrix.set(j, i);
                }
            }
        }

        return matrix;
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

    /**
     * @param faceBmp
     * @param customCenterPoint 支持自定义中心区域
     * @return
     */
    private Bitmap makeFaceQRCodeBt(Bitmap faceBmp, Point customCenterPoint) {
        int default_size = 500;

        int width = default_size;
        int height = default_size;
        float centerPercent = 0.4f;
        int purpleColor = Color.parseColor("#6700d8");

        faceBmp = Bitmap.createScaledBitmap(faceBmp, width, width, false);

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

        Rect faceRect = new Rect();
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
                faceRect.left = (int) (centerPoint.x - horizentalW / 2);
                faceRect.top = (int) (centerPoint.y - verticalH / 2);
                faceRect.right = (int) (centerPoint.x + horizentalW / 2);
                faceRect.bottom = (int) (centerPoint.y + verticalH / 2 + verticalH / 4);
            }
        }
        detectFaceBmp.recycle();
        boolean hasFace = faceRect.width() > 0;

        int maxCenterSize = (int) (width * centerPercent);
        int centerLeft = (width - maxCenterSize) / 2;
        Rect centerRect = new Rect(centerLeft, centerLeft, centerLeft + maxCenterSize, centerLeft + maxCenterSize);
        if (faceRect.width() > maxCenterSize) {
            faceScaleCoefficient = (float) maxCenterSize / faceRect.width();
        }

        Bitmap bottomBmp = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        bottomBmp.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bottomBmp);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

//        if (hasFace) {
//            if (Math.abs(faceScaleCoefficient - 1.0f) > 0.0001) {
//                int scaledWidth = (int) (faceScaleCoefficient * faceBmp.getWidth());
//                int scaledHeight = (int) (faceScaleCoefficient * faceBmp.getHeight());
//                Bitmap scaledFaceBmp = Bitmap.createScaledBitmap(faceBmp, scaledWidth, scaledHeight, false);
//                int posX = width / 2 - scaledWidth / 2;
//                int posY = width / 2 - scaledHeight / 2;
//                canvas.drawBitmap(scaledFaceBmp, posX, posY, null);
//                faceRect.left = (int) (posX + faceRect.left * faceScaleCoefficient);
//                faceRect.top = (int) (posY + faceRect.top * faceScaleCoefficient);
//                faceRect.right = faceRect.left + faceRect.width();
//                faceRect.bottom = faceRect.top + faceRect.height();
//            } else {
//                canvas.drawBitmap(faceBmp, (width - faceBmp.getWidth()) / 2, (height - faceBmp.getHeight()) / 2, null);
//            }
//        } else {
//            canvas.drawBitmap(faceBmp, (width - faceBmp.getWidth()) / 2, (height - faceBmp.getHeight()) / 2, null);
//        }
        canvas.drawBitmap(faceBmp, 0, 0, null);
        bottomBmp = binarization(bottomBmp, Color.WHITE, purpleColor);
        bottomBmp = mosaic(brightenBitmap(bottomBmp));

        Bitmap topBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(topBmp);
        canvas.drawColor(Color.argb(0, 0, 0, 0));

//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(Color.RED);
//        canvas.drawRect(faceRect, paint);
//        paint.setStyle(Paint.Style.FILL);

        paint.setColor(purpleColor);

        if (customCenterPoint != null) {
            faceRect.left = customCenterPoint.x - maxCenterSize / 2;
            faceRect.top = customCenterPoint.y - maxCenterSize / 2;
            faceRect.right = faceRect.left + maxCenterSize;
            faceRect.bottom = faceRect.top + maxCenterSize;
        } else {
            if (!hasFace) {
                faceRect = centerRect;
            }
        }

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    if (isFinderPatterns(input, inputX, inputY)) {
                        canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                        continue;
                    }

                    if (isInArea(outputX, outputY, faceRect)) {
                        boolean isAllInCenterCircle = true;
                        for (int i = 0; i < multiple; i++) {
                            for (int j = 0; j < multiple; j++) {
                                if (Math.hypot((outputX + j - (faceRect.left + faceRect.width() / 2.0f)) / (faceRect.width() / 2),
                                        (outputY + i - (faceRect.top + faceRect.height() / 2.0f)) / (faceRect.height() / 2)) > 1) {
                                    isAllInCenterCircle = false;
                                    break;
                                }
                            }
                        }

                        if (!isAllInCenterCircle) {
                            canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                        }
                    } else {
                        paint.setColor(purpleColor);
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

        //float radius = multiple / 10.0f;
        //Bitmap scaledFaceBmp = Bitmap.createScaledBitmap(faceBmp, size, size, false);

        //Bitmap scaledFaceBmp = binarization(scaleFaceBitmap(faceBmp, width, faceScaleCoefficient, faceRect), Color.WHITE, Color.parseColor("#ae4eff"));
        //Bitmap bottomBmp = scaledFaceBmp;
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
        canvas.drawBitmap(topBmp, 0, 0, null);
        return bitmap;
        //return bottomBmp;
    }

    public Bitmap brightenBitmap(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        ColorMatrix lightenColorMatrix = new ColorMatrix();
        lightenColorMatrix.set(new float[]{
                1, 0, 0, 0, 0.25f * 255,
                0, 1, 0, 0, 0.25f * 255,
                0, 0, 1, 0, 0.25f * 255,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(lightenColorMatrix));
        canvas.drawBitmap(original, 0, 0, paint);
        return result;
    }

    public Bitmap scaleFaceBitmap(Bitmap faceBmp, int size, float faceScaleCoefficient, RectF faceRect) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.BLACK);
        //bitmap.eraseColor(Color.parseColor("#6700d8"));
        Canvas canvas = new Canvas(bitmap);


        int scaledWidth = (int) (faceScaleCoefficient * faceBmp.getWidth());
        int scaledHeight = (int) (faceScaleCoefficient * faceBmp.getHeight());
        int posX = (int) (size / 2 - ((faceRect.left + faceRect.width() / 2) * faceScaleCoefficient));
        int posY = (int) (size / 2 - ((faceRect.top + faceRect.height() / 2) * faceScaleCoefficient));
        Bitmap scaledFaceBmp = Bitmap.createScaledBitmap(faceBmp, scaledWidth, scaledHeight, false);
        canvas.drawBitmap(scaledFaceBmp, posX, posY, null);

        return bitmap;
    }

    public boolean saveBitmapToFile(Bitmap bitmap, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(path));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isInCenterArea(int size, int row, int column) {
        int centerSize = (int) (size * 0.7f);
        int left = (size - centerSize) / 2;
        int right = (size + centerSize) / 2;
        return row > left && row < right && column > left && column < right;
    }

    public boolean isSkinArea(int color) {
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

    public boolean isFinderPatterns(ByteMatrix matrix, int row, int col) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        if (row >= 0 && row <= 6 && col >= 0 && col <= 6) {
            return true;
        }
        if (row >= 0 && row <= 6 && col >= width - 7 && col <= width - 1) {
            return true;
        }
        if (col >= 0 && col <= 6 && row >= height - 7 && row <= height - 1) {
            return true;
        }
        return false;
    }


    private Bitmap mosaic(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap bitmap = original.copy(Bitmap.Config.ARGB_8888, true);

        int dot = 5;
        for (int i = 0; i < width / dot; i++) {
            for (int j = 0; j < height / dot; j++) {
                int rr = 0;
                int gg = 0;
                int bb = 0;
                for (int k = 0; k < dot; k++) {
                    for (int l = 0; l < dot; l++) {
                        int dotColor = original.getPixel(i * dot + k, j
                                * dot + l);
                        rr += Color.red(dotColor);
                        gg += Color.green(dotColor);
                        bb += Color.blue(dotColor);
                    }
                }
                rr = rr / (dot * dot);
                gg = gg / (dot * dot);
                bb = bb / (dot * dot);
                for (int k = 0; k < dot; k++) {
                    for (int l = 0; l < dot; l++) {
                        bitmap.setPixel(i * dot + k, j * dot + l,
                                Color.rgb(rr, gg, bb));
                    }
                }
            }
        }
        return bitmap;
    }


}

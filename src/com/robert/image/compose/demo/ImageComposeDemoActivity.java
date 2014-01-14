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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.com.robert.image.demonew.GradientActivity;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.io.*;

public class ImageComposeDemoActivity extends Activity implements View.OnClickListener {
    private static final int MAX_FACES = 1;

    public static final String QRCODE_DEFAULT_CONTENT = "BEGIN:VCARD\nVERSION:3.0\nFN:Ting\nPHOTO;VALUE=uri:http://tp3.sinaimg.cn/1668659954/180/5679291057/1\nTEL;CELL;VOICE:18612560521\nURL:http://lzem.me\nEND:VCARD";
    public static final int QRCODE_DEFAULT_SIZE = 500;
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

        mLeftBt.setOnClickListener(this);
        mRightBt.setOnClickListener(this);

        loadOriginalBitmap(mCurrentImageIndex);

        //Bitmap bitmap = mosaic(mOriginal);
        //Bitmap bitmap = halo(mOriginal, 50, 50, 100);
        //mPreviewIv.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera:
                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "/test/1000.jpg"));
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(openCameraIntent, TAKE_PICTURE);
                break;
            case R.id.text:
                startActivity(new Intent(this, FontActivity.class));
                break;
            case R.id.text_stroke:
                startActivity(new Intent(this, TextCodeActivity.class));
                break;
            case R.id.face_compose:
                startActivity(new Intent(this, FaceComposeActivity.class));
                break;
            case R.id.pixel:
                startActivity(new Intent(this, PixelActivity.class));
                break;
            case R.id.gradient:
                startActivity(new Intent(this, GradientActivity.class));
                break;
            default:
                break;
        }

        return true;
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

    private void loadOriginalBitmap(int index) {
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
            Bitmap composedBmp = composeBinarization(null, qrcode);
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

    private Bitmap mosaic(Bitmap original) {
        Bitmap bitmap = original.copy(Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);

        final int MAX_FACES = 4;
        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
        FaceDetector detector = new FaceDetector(bitmap.getWidth(),
                bitmap.getHeight(), MAX_FACES);
        int num = detector.findFaces(bitmap, faces);

        if (num > 0) {
            for (FaceDetector.Face face : faces) {
                if (face == null) {
                    continue;
                }
                PointF point = new PointF();
                face.getMidPoint(point);

                int width = (int) face.eyesDistance() * 3;
                int height = width;

                float left = point.x - width / 2;
                float top = point.y - height / 2;

                if (left <= 0) {
                    left = 0;
                }
                if (top <= 0) {
                    top = 0;
                }

                Bitmap dist = Bitmap.createBitmap(bitmap, (int) left,
                        (int) top, width, height);
                int w = dist.getWidth();
                int h = dist.getHeight();
                int dot = 20;
                for (int i = 0; i < w / dot; i++) {
                    for (int j = 0; j < h / dot; j++) {
                        int rr = 0;
                        int gg = 0;
                        int bb = 0;
                        for (int k = 0; k < dot; k++) {
                            for (int l = 0; l < dot; l++) {
                                int dotColor = dist.getPixel(i * dot + k, j
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
                                dist.setPixel(i * dot + k, j * dot + l,
                                        Color.rgb(rr, gg, bb));
                            }
                        }
                    }
                }

                canvas.drawBitmap(dist, left, top, paint);

                dist.recycle();
            }
        }
        original.recycle();
        return bitmap;
    }

    public Bitmap halo(Bitmap bmp, int x, int y, float r) {
        // 高斯矩阵
        int[] gauss = new int[]{1, 2, 1, 2, 4, 2, 1, 2, 1};

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        PointF point = mCenterPoint;

        int pixR = 0;
        int pixG = 0;
        int pixB = 0;

        int pixColor = 0;

        int newR = 0;
        int newG = 0;
        int newB = 0;

        int delta = 50; // 值越小图片会越亮，越大则越暗

        int idx = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1, length = height - 1; i < length; i++) {
            for (int k = 1, len = width - 1; k < len; k++) {
                idx = 0;
                int distance = (int) (Math.pow(k - x, 2) + Math.pow(i - y, 2));
                // 不是中心区域的点做模糊处理
                if (distance > r * r) {
                    for (int m = -1; m <= 1; m++) {
                        for (int n = -1; n <= 1; n++) {
                            pixColor = pixels[(i + m) * width + k + n];
                            pixR = Color.red(pixColor);
                            pixG = Color.green(pixColor);
                            pixB = Color.blue(pixColor);

                            newR = newR + (int) (pixR * gauss[idx]);
                            newG = newG + (int) (pixG * gauss[idx]);
                            newB = newB + (int) (pixB * gauss[idx]);
                            idx++;
                        }
                    }

                    newR /= delta;
                    newG /= delta;
                    newB /= delta;

                    newR = Math.min(255, Math.max(0, newR));
                    newG = Math.min(255, Math.max(0, newG));
                    newB = Math.min(255, Math.max(0, newB));

                    pixels[i * width + k] = Color.argb(255, newR, newG, newB);

                    newR = 0;
                    newG = 0;
                    newB = 0;
                }
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        long end = System.currentTimeMillis();
        return bitmap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.preview_iv:
                break;
            case R.id.left_bt:
                mStepForward = false;
                loadOriginalBitmap(mCurrentImageIndex - 1);
                break;
            case R.id.right_bt:
                mStepForward = true;
                loadOriginalBitmap(mCurrentImageIndex + 1);
                break;
            default:
                break;
        }
    }

    private boolean isInArea(int x, int y, Rect area) {
        return x > area.left && x < area.right && y > area.top && y < area.bottom;
    }

    private boolean isAreaBounds(int x, int y, Rect area) {
        int width = area.right - area.left;
        int height = area.bottom - area.top;
        int centerX = area.left + width / 2;
        int centerY = area.top + height / 2;

        return Math.hypot(x - centerX, y - centerY) > Math.min(width, height) / 2;
    }

    private boolean isSkinArea(int r, int g, int b) {
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
                if (isSkinArea(Color.red(color), Color.green(color), Color.blue(color))) {
                    matrix.set(j, i);
                }
            }
        }

        return matrix;
    }

    private Bitmap lightenAndContrast(Bitmap original) {
        if (original != null) {
            return original;
        }

        int width = original.getWidth();
        int height = original.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        ColorMatrix lightenColorMatrix = new ColorMatrix();
        lightenColorMatrix.set(new float[]{
                1, 0, 0, 0, 20,
                0, 1, 0, 0, 20,
                0, 0, 1, 0, 20,
                0, 0, 0, 1, 0
        });
        ColorMatrix contrastColorMatrix = new ColorMatrix();
        float contrastCoefficient = 1.25f;
        contrastColorMatrix.set(new float[]{
                contrastCoefficient, 0, 0, 0, 128 * (1 - contrastCoefficient),
                0, contrastCoefficient, 0, 0, 128 * (1 - contrastCoefficient),
                0, 0, contrastCoefficient, 0, 128 * (1 - contrastCoefficient),
                0, 0, 0, 1, 0
        });
        lightenColorMatrix.postConcat(contrastColorMatrix);
        paint.setColorFilter(new ColorMatrixColorFilter(lightenColorMatrix));
        canvas.drawBitmap(original, 0, 0, paint);
        return result;
    }

    private boolean saveBitmapToFile(Bitmap bitmap, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(path));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}

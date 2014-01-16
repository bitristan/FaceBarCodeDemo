package com.qrcode.r.sdk;

import android.graphics.*;
import android.media.FaceDetector;
import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;
import com.imagefilter.IImageFilter;
import com.imagefilter.Image;
import com.imagefilter.effect.*;

/**
 * Created by robert on 13-12-23.
 */
public class FaceQREffect extends QREffectInterface {
    private static final int MAX_FACES = 1;
    private static final float CENTER_PERCENT = 0.3f;
    private static final int DEFAULT_BORDER = 2;
    private static final int RECT_BORDER = 1;

    @Override
    Bitmap makeEffectQRCode(String content, QRCodeOptionsInterface opt) {
        QRCodeFaceOptions options = (QRCodeFaceOptions) opt;
        int width = options.mSize;
        int height = options.mSize;

        int color = options.mColor;
        Bitmap faceBmp = options.mFaceBmp;
        int border = DEFAULT_BORDER;

        //如果图片小余二维码输出的大小,将图片处理成一样的大小
        if (faceBmp.getWidth() < width) {
            faceBmp = Bitmap.createScaledBitmap(faceBmp, width, height, true);
        }

        if (faceBmp.getWidth() > faceBmp.getHeight()) {
            if (faceBmp.getWidth() > width) {
                faceBmp = Bitmap.createScaledBitmap(faceBmp, width, width * faceBmp.getHeight() / faceBmp.getWidth(), false);
            }
        } else {
            if (faceBmp.getHeight() > width) {
                faceBmp = Bitmap.createScaledBitmap(faceBmp, width * faceBmp.getWidth() / faceBmp.getHeight(), width, false);
            }
        }

        int faceLeftPos = (width - faceBmp.getWidth()) / 2;
        int faceTopPos = (width - faceBmp.getHeight()) / 2;

        QRCode qrcode = encodeQrcode(options.getContent(), options.errorLevel);
        ByteMatrix input = qrcode.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth + border + RECT_BORDER * 2;
        int qrHeight = inputHeight + border + RECT_BORDER * 2;
        int outputWidth = Math.max(width, qrWidth);
        int outputHeight = Math.max(height, qrHeight);

        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

        int maxCenterSize = (int) (width * CENTER_PERCENT);
        int centerLeft = (width - maxCenterSize) / 2;
        Rect centerRect = new Rect(centerLeft, centerLeft, centerLeft + maxCenterSize, centerLeft + maxCenterSize);

        Rect faceRect = new Rect();
        float faceScaleCoefficient = 1.0f;
        Bitmap detectFaceBmp = faceBmp.copy(Bitmap.Config.RGB_565, true);
        FaceDetector.Face faces[] = new FaceDetector.Face[1];
        FaceDetector detector = new FaceDetector(detectFaceBmp.getWidth(), detectFaceBmp.getHeight(), MAX_FACES);
        boolean findFace = false;
        int count = detector.findFaces(detectFaceBmp, faces);
        if (count > 0) {
            //识别出人脸
            findFace = true;
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

            if (faceRect.width() > maxCenterSize) {
                int detaX = (faceRect.width() - maxCenterSize) / 2;
                faceRect.left = faceRect.left + detaX;
                faceRect.right = faceRect.right - detaX;
            }
            if (faceRect.height() > maxCenterSize) {
                int deatH = (faceRect.height() - maxCenterSize) / 2;
                faceRect.top = faceRect.top + deatH;
                faceRect.bottom = faceRect.bottom - deatH;
            }
        } else {
            //没有识别出人脸
            findFace = false;
            faceBmp = Bitmap.createScaledBitmap(faceBmp, maxCenterSize, maxCenterSize, false);
            faceLeftPos = (width - faceBmp.getWidth()) / 2;
            faceTopPos = (width - faceBmp.getHeight()) / 2;

            faceRect = centerRect;
        }
        detectFaceBmp.recycle();

        faceBmp = makeFilter(faceBmp, new ConvolutionFilter());
        faceBmp = makeFilter(faceBmp, new BrightContrastFilter(0.1f, 0.0f));
        faceBmp = makeFilter(faceBmp, new BrightContrastFilter(0.0f, 0.1f));
//        if (findFace) {
//            faceBmp = makeFilter(faceBmp, new AutoLevelFilter());
//        }
        faceBmp = makeFilter(faceBmp, new BigBrotherCustomFilter(Color.blue(color), Color.green(color), Color.red(color)));

        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);

        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvasTop = new Canvas(out);
        canvasTop.drawColor(Color.WHITE);
        paint.setColor(color);
        paint.setAlpha(150);

        if (findFace) {
            canvasTop.drawBitmap(faceBmp, faceLeftPos, faceTopPos, paint);
        }
        paint.setAlpha(0xff);

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    if (isFinderPatterns(input, inputX, inputY)) {
                        canvasTop.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                        continue;
                    }

                    if (findFace && isInArea(outputX, outputY, faceRect)) {
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
                            canvasTop.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                        }
                    } else {
                        paint.setColor(color);
                        canvasTop.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                    }
                }
            }
        }

        if (!findFace) {
            canvasTop.drawBitmap(faceBmp, faceLeftPos, faceTopPos, paint);
        }

        paint.setColor(color);
        paint.setAlpha(0xff);
        paint.setStrokeWidth(multiple);
        canvasTop.drawLine(0, 0, width, 0, paint);//上
        canvasTop.drawLine(0, height, width, height , paint);//下
        canvasTop.drawLine(0, 0, 0, height, paint);//左
        canvasTop.drawLine(width, 0, width, height, paint);//右

        return out;
    }

    private Bitmap brightBitmap1(Bitmap bt) {
        Bitmap out = Bitmap.createBitmap(bt.getWidth(), bt.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.WHITE);

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

    private Bitmap binarization(Bitmap bitmap, int lowColor, int highColor) {
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

    private Bitmap brightenBitmap(Bitmap original) {
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

    public Bitmap makeFilter(Bitmap bt, IImageFilter filter) {
        try {
            Image img = new Image(bt);
            if (filter != null) {
                img = filter.process(img);
                img.copyPixelsFromBuffer();
            }
            return img.getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Bitmap scaleFaceBitmap(Bitmap faceBmp, int size, float faceScaleCoefficient, RectF faceRect) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int scaledWidth = (int) (faceScaleCoefficient * faceBmp.getWidth());
        int scaledHeight = (int) (faceScaleCoefficient * faceBmp.getHeight());
        int posX = (int) (size / 2 - ((faceRect.left + faceRect.width() / 2) * faceScaleCoefficient));
        int posY = (int) (size / 2 - ((faceRect.top + faceRect.height() / 2) * faceScaleCoefficient));
        Bitmap scaledFaceBmp = Bitmap.createScaledBitmap(faceBmp, scaledWidth, scaledHeight, false);
        canvas.drawBitmap(scaledFaceBmp, posX, posY, null);

        return bitmap;
    }

    private boolean isFinderPatterns(ByteMatrix matrix, int row, int col) {
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

    private boolean isInArea(int x, int y, Rect area) {
        return x > area.left && x < area.right && y > area.top && y < area.bottom;
    }

}

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

/**
 * Created by robert on 13-12-23.
 */
public class FaceQREffect extends QREffectInterface {
    private static final int MAX_FACES = 1;
    private static final float CENTER_PERCENT = 0.4f;
    private static final int DEFAULT_BORDER = 2;

    @Override
    Bitmap makeEffectQRCode(String content, QRCodeOptionsInterface opt) {
        QRCodeFaceOptions options = (QRCodeFaceOptions) opt;
        int width = options.mSize;
        int height = options.mSize;

        int color = options.mColor;
        Bitmap faceBmp = options.mFaceBmp;
        int border = DEFAULT_BORDER;

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

        QRCode qrcode = encodeQrcode(options.getContent(), ErrorCorrectionLevel.H);
        ByteMatrix input = qrcode.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth + border;
        int qrHeight = inputHeight + border;
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

            if (faceRect.width() > maxCenterSize) {
                faceScaleCoefficient = (float) maxCenterSize / faceRect.width();
                faceBmp = Bitmap.createScaledBitmap(faceBmp, (int) (faceBmp.getWidth() * faceScaleCoefficient), (int) (faceBmp.getHeight() * faceScaleCoefficient), false);
                int faceRectWidth = (int) (faceRect.width() * faceScaleCoefficient);
                int faceRectHeight = (int) (faceRect.height() * faceScaleCoefficient);

                faceRect.left = (int) (faceRect.left * faceScaleCoefficient);
                faceRect.top = (int) (faceRect.top * faceScaleCoefficient);
                faceRect.right = faceRect.left + faceRectWidth;
                faceRect.bottom = faceRect.top + faceRectHeight;

                faceLeftPos = (width - faceBmp.getWidth()) / 2;
                faceTopPos = (height - faceBmp.getHeight()) / 2;
            }

            faceRect.left += faceLeftPos;
            faceRect.top += faceTopPos;
            faceRect.right += faceLeftPos;
            faceRect.bottom += faceTopPos;
        } else {
            faceBmp = Bitmap.createScaledBitmap(faceBmp, maxCenterSize, maxCenterSize, false);
            faceLeftPos = (width - faceBmp.getWidth()) / 2;
            faceTopPos = (width - faceBmp.getHeight()) / 2;

            faceRect = centerRect;
        }
        detectFaceBmp.recycle();

        Bitmap bottomBmp = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bottomBmp);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);

        faceBmp = binarization(faceBmp, Color.WHITE, color);
        faceBmp = brightenBitmap(faceBmp);
        System.out.println("width = " + width + "; face width = " + faceBmp.getWidth());
        canvas.drawBitmap(faceBmp, faceLeftPos, faceTopPos, null);

        Bitmap topBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(topBmp);
        canvas.drawColor(Color.argb(0, 0, 0, 0));
        paint.setColor(color);

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
                        paint.setColor(color);
                        canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                    }
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.RED);

        canvas.drawBitmap(bottomBmp, 0, 0, null);
        canvas.drawBitmap(topBmp, 0, 0, null);
        return bitmap;
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

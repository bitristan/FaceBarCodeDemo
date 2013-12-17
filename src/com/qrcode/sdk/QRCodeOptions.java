package com.qrcode.sdk;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeOptions {
	public static final int COLOR_UNSET = -1;

	public static final int DEFAULT_WIDTH = 300;
	public static final int DEFAULT_HEIGHT = 300;
	public static final int DEFAULT_PADDING = 2;
	public static final int DEFAULT_FOREGROUND_COLOR = Color.BLACK;
	public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
	public static final ErrorCorrectionLevel DEFAULT_ERROR_CORRECTION_LEVEL = ErrorCorrectionLevel.H;

	public static enum Shape {
		NORMAL, ROUND, WATER,
	}

	public static enum GradientType {
		NORMAL, ROUND, SLASH, BACKSLASH, HORIZONTAL, VERTICAL,
	}

	public static enum ComposeType {
		SIMPLE, ALTERNATIVE,
	}

	public static enum BorderType {
		ROUND, RHOMBUS,
	}

	public int outWidth;
	public int outHeight;
	public int outForegroundColor;
	public int outBackgroundColor;
	public int outFinderPatternColor;
	public int outFinderPointColor;
	public int outFinderBorderColor;
	public int outPadding;
	public Shape outShape;
	public float outRadiuspercent;
	public GradientType outGradientType;
	public int outGradientColor;
	public ErrorCorrectionLevel outErrorCorrectionLevel;
	public Bitmap outBackgroundImage;
	public ComposeType outComposeType;
	public BorderType outBorderType;

	public QRCodeOptions() {
		outWidth = DEFAULT_WIDTH;
		outHeight = DEFAULT_HEIGHT;
		outPadding = DEFAULT_PADDING;
		outForegroundColor = DEFAULT_FOREGROUND_COLOR;
		outBackgroundColor = DEFAULT_BACKGROUND_COLOR;
		outFinderPatternColor = COLOR_UNSET;
		outFinderPointColor = COLOR_UNSET;
		outFinderBorderColor = COLOR_UNSET;
		outShape = Shape.NORMAL;
		outGradientType = GradientType.NORMAL;
		outErrorCorrectionLevel = DEFAULT_ERROR_CORRECTION_LEVEL;
	}
}

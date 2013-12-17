package com.qrcode.sdk;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class AngryBirdOptions {

	public static final int DEFAULT_WIDTH = 300;
	public static final int DEFAULT_HEIGHT = 300;
	public static final int DEFAULT_PADDING = 2;
	public static final ErrorCorrectionLevel DEFAULT_ERROR_CORRECTION_LEVEL = ErrorCorrectionLevel.H;
	public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;

	public int outWidth;
	public int outHeight;
	public int outPadding;
	public ErrorCorrectionLevel outErrorCorrectionLevel;
	public Bitmap bar1;
	public Bitmap hbar2;
	public Bitmap vbar2;
	public Bitmap bird;
	public Bitmap finder;
	public Bitmap background;

	public AngryBirdOptions() {
		outWidth = DEFAULT_WIDTH;
		outHeight = DEFAULT_HEIGHT;
		outPadding = DEFAULT_PADDING;
		outErrorCorrectionLevel = DEFAULT_ERROR_CORRECTION_LEVEL;
	}
}

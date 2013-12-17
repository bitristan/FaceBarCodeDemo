package com.qrcode.sdk;

import android.graphics.Path;
import android.graphics.Rect;

public abstract class QRBorder {

	protected int mWidth;

	protected int mHeight;

	protected int mLeftPadding;

	protected int mTopPadding;

	protected Rect mInsideRect;

	protected int mBoxSize;

	public QRBorder(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	public int getLeftPadding() {
		return mLeftPadding;
	}

	public int getTopPadding() {
		return mTopPadding;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public Rect getInsideArea() {
		return mInsideRect;
	}
	
	public int getBoxSize() {
		return mBoxSize;
	}

	public abstract Path getClipPath();

}

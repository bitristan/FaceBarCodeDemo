package com.qrcode.sdk;

import android.graphics.Path;
import android.graphics.Rect;

public class QRRhombusBorder extends QRBorder {

	public QRRhombusBorder(int width, int height, int qrDimension) {
		super(width, height);

		int insideWidth = Math.min(width, height) >> 1;
		mBoxSize = insideWidth / qrDimension;
		int padding = (insideWidth % qrDimension) >> 1;

		mLeftPadding = (width >> 1) - insideWidth + (padding << 1);
		mTopPadding = (height >> 1) - insideWidth + (padding << 1);

		mInsideRect = new Rect((width >> 1) - (insideWidth >> 1) + padding,
				(height >> 1) - (insideWidth >> 1) + padding, (width >> 1)
						+ (insideWidth >> 1) - padding, (height >> 1)
						+ (insideWidth >> 1) - padding);
	}

	@Override
	public Path getClipPath() {
		Path path = new Path();
		path.moveTo(mLeftPadding, mHeight >> 1);
		path.lineTo(mWidth >> 1, mTopPadding);
		path.lineTo(mWidth - mLeftPadding, mHeight >> 1);
		path.lineTo(mWidth >> 1, mHeight - mTopPadding);
		path.close();
		return path;
	}

}

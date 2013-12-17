package com.qrcode.sdk;

import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;

public class QRCircleBorder extends QRBorder {

	private int mDiameter;

	public QRCircleBorder(int width, int height, int qrDimension) {
		super(width, height);
		mDiameter = Math.min(width, height);

		int insideWidth = (int) (mDiameter * Math.sqrt(2.0) / 2);
		mBoxSize = insideWidth / qrDimension;
		int padding = (insideWidth % qrDimension) >> 1;
		int diameterPadding = (int) (Math.sqrt(2.0) * padding);
		mDiameter -= (diameterPadding << 1);

		mLeftPadding = (width - mDiameter) >> 1;
		mTopPadding = (height - mDiameter) >> 1;

		mInsideRect = new Rect((width >> 1) - (insideWidth >> 1) + padding,
				(height >> 1) - (insideWidth >> 1) + padding, (width >> 1)
						+ (insideWidth >> 1) - padding, (height >> 1)
						+ (insideWidth >> 1) - padding);
	}

	@Override
	public Path getClipPath() {
		Path path = new Path();
		path.addCircle(mWidth / 2.0f, mHeight / 2.0f, mDiameter / 2.0f,
				Direction.CCW);
		return path;
	}

}

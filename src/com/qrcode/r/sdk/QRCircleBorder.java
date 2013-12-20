package com.qrcode.r.sdk;

import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;

public class QRCircleBorder extends QRBorder {

	private int mDiameter;

    public int mWidthLeftBoxCount;
    public int mWidthRightBoxCount;
    public int mHeightTopBoxCount;
    public int mHeightBottomBoxCount;

	public QRCircleBorder(int width, int height, int qrWidth) {
		super(width, height);
		mDiameter = Math.min(width, height);

		int insideWidth = (int) (mDiameter * Math.sqrt(2.0) / 2) - 20;
		mBoxSize = insideWidth / qrWidth;
        int widthBoxCount = width / mBoxSize;
        int heightBoxCount = height / mBoxSize;
        mWidthLeftBoxCount = (widthBoxCount - 4 - qrWidth) / 2;
        mWidthRightBoxCount = (widthBoxCount - 4 - qrWidth) / 2;
        mHeightTopBoxCount = (heightBoxCount - 4 - qrWidth) / 2;
        mHeightBottomBoxCount = (heightBoxCount - 4 - qrWidth) / 2;

        mLeftPadding = (width % mBoxSize) >> 1;
        mTopPadding = (height % mBoxSize) >> 1;

        mInsideRect = new Rect(mLeftPadding + (mWidthLeftBoxCount + 2) * mBoxSize
                                , mTopPadding + (mHeightTopBoxCount + 2) * mBoxSize
                                , mLeftPadding + (mWidthLeftBoxCount + 2 + qrWidth) * mBoxSize
                                , mTopPadding + (mHeightTopBoxCount + 2 + qrWidth) * mBoxSize);

		mDiameter = (mWidthLeftBoxCount * 2 + 4 + qrWidth) * mBoxSize;

		mLeftPadding = (width - mDiameter) >> 1;
		mTopPadding = (height - mDiameter) >> 1;
	}

	@Override
	public Path getClipPath() {
		Path path = new Path();
		path.addCircle(mWidth / 2.0f, mHeight / 2.0f, mDiameter / 2.0f, Direction.CCW);
		return path;
	}

}

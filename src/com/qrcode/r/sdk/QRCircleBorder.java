package com.qrcode.r.sdk;

import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;

public class QRCircleBorder extends QRBorder {

	private int mDiameter;

	public QRCircleBorder(int width, int height, int qrWidth) {
		mDiameter = Math.min(width, height);

		int insideWidth = mDiameter * 3 / 5;
		mBoxSize = insideWidth / qrWidth;
        int widthBoxCount = width / mBoxSize;
        int heightBoxCount = height / mBoxSize;
        mWidthLeftBoxCount = (widthBoxCount - 4 - qrWidth) / 2;
        mWidthRightBoxCount = (widthBoxCount - 4 - qrWidth) / 2;
        mHeightTopBoxCount = (heightBoxCount - 4 - qrWidth) / 2;
        mHeightBottomBoxCount = (heightBoxCount - 4 - qrWidth) / 2;

        //做一次矫正
        mWidth = (widthBoxCount + 2) * mBoxSize;
        mHeight = (heightBoxCount + 2) * mBoxSize;

        mInsideRect = new Rect((mWidthLeftBoxCount + 3) * mBoxSize
                                , (mHeightTopBoxCount + 3) * mBoxSize
                                , (mWidthLeftBoxCount + 3 + qrWidth) * mBoxSize
                                , (mHeightTopBoxCount + 3 + qrWidth) * mBoxSize);

		mDiameter = mWidth;
	}

	@Override
	public Path getClipPath() {
		Path path = new Path();
		path.addCircle(mWidth / 2.0f, mHeight / 2.0f, mDiameter / 2.0f, Direction.CCW);
		return path;
	}

}

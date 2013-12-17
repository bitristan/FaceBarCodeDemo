package com.robert.image.compose.demo;

import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by michael on 13-12-17.
 */
public class QRImageAdapter extends PagerAdapter {

    private LayoutInflater mLayoutInflater;
    private ArrayList<Bitmap> mData = new ArrayList<Bitmap>();

    public QRImageAdapter(LayoutInflater lf, ArrayList<Bitmap> data) {
        mLayoutInflater = lf;
        mData.addAll(data);
    }

    public void setNotify(ArrayList<Bitmap> data) {
        mData.clear();
        mData.addAll(data);
        this.notifyDataSetChanged();
    }

    @Override
    public String getPageTitle(int position) {
        return "效果" + (position + 1);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView ret = (ImageView) mLayoutInflater.inflate(R.layout.font_image, null);
        ret.setImageBitmap(mData.get(position));

        container.addView(ret);

        return ret;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public void finishUpdate(View arg0) {
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View arg0) {
    }
}

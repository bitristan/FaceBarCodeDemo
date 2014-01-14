package com.com.robert.image.demonew;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.qrcode.r.sdk.QRCodeGenerator;
import com.qrcode.r.sdk.QRCodeGradientOptions;
import com.robert.image.compose.demo.R;

import java.io.IOException;
import java.lang.ref.SoftReference;

/**
 * Created by zhangdi on 13-12-20.
 */
public class GradientActivity extends BaseActivity implements View.OnClickListener {

    private ViewPager mViewPager;
    private QRPagerAdapter mPageAdapter;

//    private ImageView mPreviewIv;
//    private Bitmap mQRCodeBt;

    private View mV1, mV2, mV3, mV4, mV5, mV6, mV7, mV8;

    private int mStartColor;
    private int mEndColor;

    private Handler mHandler = new Handler();

    private String mQRContent;

    private int mResID;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gradient);

        enableHomeButton("渐变");

        mQRContent = getIntent().getStringExtra(Config.KEY_QRCODE_CONTENT);
        mResID = getIntent().getIntExtra(Config.KEY_RES_ID, 0);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mPageAdapter = new QRPagerAdapter(this);
        mViewPager.setAdapter(mPageAdapter);

        mV1 = findViewById(R.id.c1);
        mV1.setOnClickListener(this);
        mV2 = findViewById(R.id.c2);
        mV2.setOnClickListener(this);
        mV3 = findViewById(R.id.c3);
        mV3.setOnClickListener(this);
        mV4 = findViewById(R.id.c4);
        mV4.setOnClickListener(this);
        mV5 = findViewById(R.id.c5);
        mV5.setOnClickListener(this);
        mV6 = findViewById(R.id.c6);
        mV6.setOnClickListener(this);
        mV7 = findViewById(R.id.c7);
        mV7.setOnClickListener(this);
        mV8 = findViewById(R.id.c8);
        mV8.setOnClickListener(this);

        mStartColor = ((ColorDrawable) mV1.getBackground()).getColor();
        mEndColor = ((ColorDrawable) mV5.getBackground()).getColor();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.c1:
            case R.id.c5:
                mStartColor = ((ColorDrawable) mV1.getBackground()).getColor();
                mEndColor = ((ColorDrawable) mV5.getBackground()).getColor();
                break;
            case R.id.c2:
            case R.id.c6:
                mStartColor = ((ColorDrawable) mV2.getBackground()).getColor();
                mEndColor = ((ColorDrawable) mV6.getBackground()).getColor();
                break;
            case R.id.c3:
            case R.id.c7:
                mStartColor = ((ColorDrawable) mV3.getBackground()).getColor();
                mEndColor = ((ColorDrawable) mV7.getBackground()).getColor();
                break;
            case R.id.c4:
            case R.id.c8:
                mStartColor = ((ColorDrawable) mV4.getBackground()).getColor();
                mEndColor = ((ColorDrawable) mV8.getBackground()).getColor();
                break;
        }
        mPageAdapter.clearCache();
        mPageAdapter.notifyDataSetChanged();
//        generateQRCode();
    }

//    private void generateQRCode() {
//        new Thread() {
//            @Override
//            public void run() {
//                QRCodeGradientOptions opt = new QRCodeGradientOptions();
//                opt.qrContent = Config.QRCODE_CONTENT;
//                opt.defaultQRSize = Config.QRCODE_DEFAULT_SIZE;
//                opt.startColor = mStartColor;
//                opt.endColor = mEndColor;
//                try {
//                    opt.maskBitmap = BitmapFactory.decodeStream(getAssets().open("image/a1.png"));
//                    opt.borderBitmap = BitmapFactory.decodeStream(getAssets().open("image/b1.png"));
//                    opt.frontBitmap = BitmapFactory.decodeStream(getAssets().open("image/000.jpg"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mQRCodeBt = QRCodeGenerator.createQRCode(opt);
//
//                mHandler.removeCallbacksAndMessages(null);
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mPreviewIv.setImageBitmap(mQRCodeBt);
//                    }
//                });
//            }
//        }.start();
//    }

    class QRPagerAdapter extends PagerAdapter {

        private Context mContext;

        private SparseArray<SoftReference<Bitmap>> mQRBitmaps = new SparseArray<SoftReference<Bitmap>>();

        public QRPagerAdapter(Context context) {
            mContext = context;
        }

        public void clearCache() {
            mQRBitmaps.clear();
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.gradient_one, null);
            final ImageView imageview = (ImageView) view.findViewById(R.id.image);

            Bitmap cacheBitmap = null;
            if (mQRBitmaps != null) {
                SoftReference<Bitmap> weakBitmap = mQRBitmaps.get(position);
                if (weakBitmap != null) {
                    cacheBitmap = weakBitmap.get();
                }
            }
            if (cacheBitmap != null && !cacheBitmap.isRecycled()) {
                imageview.setImageBitmap(cacheBitmap);
            } else {
                new Thread() {
                    @Override
                    public void run() {
                        QRCodeGradientOptions opt = new QRCodeGradientOptions();
                        opt.qrContent = mQRContent;
                        opt.defaultQRSize = Config.QRCODE_DEFAULT_SIZE;
                        opt.startColor = mStartColor;
                        opt.endColor = mEndColor;
                        Bitmap frontBt = ((BitmapDrawable) getResources().getDrawable(mResID)).getBitmap();
                        try {
                            if (position == 0) {
                                opt.maskBitmap = BitmapFactory.decodeStream(getAssets().open("image/a1.png"));
                                opt.borderBitmap = BitmapFactory.decodeStream(getAssets().open("image/b1.png"));
//                                opt.frontBitmap = BitmapFactory.decodeStream(getAssets().open("image/000.jpg"));
                                opt.frontBitmap = frontBt;
                            } else if (position == 1) {
                                opt.maskBitmap = BitmapFactory.decodeStream(getAssets().open("image/a2.png"));
                                opt.borderBitmap = BitmapFactory.decodeStream(getAssets().open("image/b2.png"));
//                                opt.frontBitmap = BitmapFactory.decodeStream(getAssets().open("image/001.jpg"));
                                opt.frontBitmap = frontBt;
                            } else if (position == 2) {
                                opt.maskBitmap = BitmapFactory.decodeStream(getAssets().open("image/a3.png"));
                                opt.borderBitmap = BitmapFactory.decodeStream(getAssets().open("image/b3.png"));
//                                opt.frontBitmap = BitmapFactory.decodeStream(getAssets().open("image/002.jpg"));
                                opt.frontBitmap = frontBt;
                            } else if (position == 3) {
                                opt.maskBitmap = BitmapFactory.decodeStream(getAssets().open("image/a4.png"));
                                opt.borderBitmap = BitmapFactory.decodeStream(getAssets().open("image/b4.png"));
//                                opt.frontBitmap = BitmapFactory.decodeStream(getAssets().open("image/003.jpg"));
                                opt.frontBitmap = frontBt;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final Bitmap bitmap = QRCodeGenerator.createQRCode(opt);
                        mQRBitmaps.put(position, new SoftReference<Bitmap>(bitmap));

                        mHandler.removeCallbacksAndMessages(null);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageview.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }
}
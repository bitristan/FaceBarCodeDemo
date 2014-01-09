package com.robert.image.compose.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.imagefilter.IImageFilter;
import com.imagefilter.Image;
import com.imagefilter.effect.*;
import com.qrcode.r.sdk.QRCodeFaceOptions;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michael on 14-1-8.
 */
public class FaceCompose1Activity extends Activity {

    private ViewPager mViewPager;

    private ImageView mOriginImage;

    private Bitmap mOriginBt;

    private Handler mHandler = new Handler(Looper.myLooper());

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_compose1);

        initUI();
    }

    private void initUI() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mOriginImage = (ImageView) findViewById(R.id.original_iv);

        PagerTitleStrip titleStrip = (PagerTitleStrip) findViewById(R.id.title);

        mViewPager.setAdapter(new FacePageAdapter(getApplicationContext()));

        try {
//            mOriginBt = BitmapFactory.decodeStream(getAssets().open("image/005.jpg"));
            mOriginBt = ((BitmapDrawable) getResources().getDrawable(R.drawable.hehua)).getBitmap();

//            float[] start = new float[3];
//            float[] end = new float[3];
//            Color.colorToHSV(Color.rgb(214, 1, 143), start);
//            start[1] = 0;
//            start[2] = 0;
//            Color.colorToHSV(Color.rgb(214, 1, 143), end);
//            end[1] = 0;
//            end[2] = 0;

//            mOriginBt = covertBitmapWithHSBWithHChanged(mOriginBt, start, end);

            mOriginImage.setImageBitmap(mOriginBt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private Bitmap covertBitmapWithHSBWithHChanged(Bitmap bt, float[] startHSVAdjust, float[] endHSVAdjust) {
//        int w = bt.getWidth(), h = bt.getHeight();
//        int[] pix = new int[w * h];
//        bt.getPixels(pix, 0, w, 0, 0, w, h);
//
//        float hueDeta = (endHSVAdjust[0] - startHSVAdjust[0]) / h;
//        float[] pixelHSV = new float[3];
//        int alpha = 0xFF << 24;
//        for (int i = 0; i < h; i++) {
//            for (int j = 0; j < w; j++) {
//                int color = pix[w * i + j];
//                Color.colorToHSV(color, pixelHSV);
//
//                pixelHSV[0] = startHSVAdjust[0] + hueDeta * i;
//                pixelHSV[1] = 1 - pixelHSV[2];
//                color = Color.HSVToColor(pixelHSV);
//                color = alpha | color;
//                pix[w * i + j] = color;
//            }
//        }
//
//        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        result.setPixels(pix, 0, w, 0, 0, w, h);
//        return result;
//    }

    public static List<IImageFilter> getFilterList() {
        ArrayList<IImageFilter> ret = new ArrayList<IImageFilter>();
        ret.add(new ThresholdFilter());
        ret.add(new AutoAdjustFilter());
        ret.add(new AutoLevelFilter());
        ret.add(new BigBrotherFilter());
        ret.add(new BilinearDistort());
        ret.add(new BlackWhiteFilter());
        ret.add(new BrickFilter());
        ret.add(new BrightContrastFilter());
        ret.add(new BulgeFilter(-97));
        ret.add(new CleanGlassFilter());
        ret.add(new ColorQuantizeFilter());
        ret.add(new ColorToneFilter(0x00FF00, 192));
        ret.add(new ConvolutionFilter());
        ret.add(new EdgeFilter());
        ret.add(new FeatherFilter());
        ret.add(new GradientFilter());
        ret.add(new GradientMapFilter());
        ret.add(new HistogramEqualFilter());
        ret.add(new NoiseFilter());
        ret.add(new RadialDistortionFilter());
        ret.add(new RainBowFilter());
        ret.add(new RaiseFrameFilter(20));
        ret.add(new RectMatrixFilter());
        ret.add(new ReflectionFilter(true));
        ret.add(new ReliefFilter());
        ret.add(new RippleFilter(38, 15, true));
        ret.add(new TwistFilter(27, 106));
        ret.add(new WaveFilter(25, 10));

        return ret;
    }

    private final class FacePageAdapter extends PagerAdapter {

        private Context mContext;

        private List<IImageFilter> mFilterList;

        private SparseArray<SoftReference<Bitmap>> mQRBitmaps = new SparseArray<SoftReference<Bitmap>>();

        public FacePageAdapter(Context context) {
            mContext = context;
            mFilterList = getFilterList();
        }

        public void clearCache() {
            mQRBitmaps.clear();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
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
        public int getCount() {
            return mFilterList.size() + 4;
        }

        @Override
        public String getPageTitle(int position) {
            return mFilterList.get(position).getClass().getSimpleName();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.face_image_item, null);
            final ImageView imageview = (ImageView) view.findViewById(R.id.preview_iv);

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
                        QRCodeFaceOptions opt = new QRCodeFaceOptions();
                        opt.mQrContent = Config.QRCODE_CONTENT;
                        opt.mSize = 500;
                        opt.errorLevel = ErrorCorrectionLevel.H;
                        opt.mFaceBmp = mOriginBt;
                        Bitmap composedBmp = null;
                        if (position == 0) {
                            opt.mColor = Color.rgb(214, 1, 143);
                            composedBmp = com.qrcode.r.sdk.QRCodeGenerator.createQRCode(opt);
                        } else if (position == 1) {
                            opt.mColor = Color.rgb(115, 115, 115);
                            composedBmp = com.qrcode.r.sdk.QRCodeGenerator.createQRCode(opt);
                        } else if (position == 2) {
                            opt.mColor = Color.rgb(16, 125, 40);
                            composedBmp = com.qrcode.r.sdk.QRCodeGenerator.createQRCode(opt);
                        } else if (position == 3) {
                            opt.mColor = Color.rgb(28, 99, 209);
                            composedBmp = com.qrcode.r.sdk.QRCodeGenerator.createQRCode(opt);
                        } else {
                            composedBmp = makeFilter(mOriginBt, mFilterList.get(position - 4));
                        }
                        mQRBitmaps.put(position, new SoftReference<Bitmap>(composedBmp));

                        final Bitmap show = composedBmp;

                        mHandler.removeCallbacksAndMessages(null);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageview.setImageBitmap(show);
                            }
                        });
                    }
                }.start();
            }

            container.addView(view);
            return view;
        }
    }

    public static Bitmap makeFilter(Bitmap bt, IImageFilter filter) {
        try {
            Image img = new Image(bt);
            if (filter != null) {
                img = filter.process(img);
                img.copyPixelsFromBuffer();
            }
            return img.getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
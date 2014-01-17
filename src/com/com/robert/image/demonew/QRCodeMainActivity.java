package com.com.robert.image.demonew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.robert.image.compose.demo.R;

/**
 * Created by michael on 14-1-13.
 */
public class QRCodeMainActivity extends BaseActivity {

    private ViewPager mViewPager;
    private ImageSelectPagerAdapter mImageSelectPagerAdapter;

    private int[] mResList = { R.drawable.hehua, R.drawable.test_123456, R.drawable.test_1234567, R.drawable.test_hsv
                                , R.drawable.test_1, R.drawable.test_2, R.drawable.test_3 };

    private EditText mContentET;

    private View mGradientBt;
    private View mFaceBt;
    private View mFilterBt;

    private int mCurrentResID;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qrcode_main);

        enableHomeButton(getString(R.string.app_name));
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeButtonEnabled(false);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mImageSelectPagerAdapter = new ImageSelectPagerAdapter(getApplicationContext(), mResList);
        mViewPager.setAdapter(mImageSelectPagerAdapter);

        mContentET = (EditText) findViewById(R.id.qr_content);
        mGradientBt = findViewById(R.id.gradient);
        mFaceBt = findViewById(R.id.face);
        mFilterBt = findViewById(R.id.filter);

        mContentET.setText(Config.QRCODE_CONTENT);
        mGradientBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentResID == 0) {
                    Toast.makeText(getApplicationContext(), "没有选择资源", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(mContentET.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "二维码内容不能为空", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent i = new Intent();
                i.setClass(getApplicationContext(), GradientActivity.class);
                i.putExtra(Config.KEY_QRCODE_CONTENT, mContentET.getText().toString());
                i.putExtra(Config.KEY_RES_ID, mCurrentResID);
                startActivity(i);
            }
        });

        mFaceBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentResID == 0) {
                    Toast.makeText(getApplicationContext(), "没有选择资源", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(mContentET.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "二维码内容不能为空", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent i = new Intent();
                i.setClass(getApplicationContext(), FaceCompose1Activity.class);
                i.putExtra(Config.KEY_QRCODE_CONTENT, mContentET.getText().toString());
                i.putExtra(Config.KEY_RES_ID, mCurrentResID);
                startActivity(i);
            }
        });

        mFilterBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentResID == 0) {
                    Toast.makeText(getApplicationContext(), "没有选择资源", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(mContentET.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "二维码内容不能为空", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent i = new Intent();
                i.setClass(getApplicationContext(), ImageFilterActivity.class);
                i.putExtra(Config.KEY_QRCODE_CONTENT, mContentET.getText().toString());
                i.putExtra(Config.KEY_RES_ID, mCurrentResID);
                startActivity(i);
            }
        });
    }


    private class ImageSelectPagerAdapter extends PagerAdapter {

        private Context mContext;

        private int[] mResList;

        public ImageSelectPagerAdapter(Context context, int[] resList) {
            mContext = context;
            mResList = resList;
        }

        @Override
        public int getCount() {
            if (mResList != null) {
                return mResList.length;
            }

            return 0;
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
            ImageView imageview = (ImageView) view.findViewById(R.id.image);
            final View cover = view.findViewById(R.id.cover);
            final CheckBox cb = (CheckBox) view.findViewById(R.id.choose_cb);
            cb.setVisibility(View.VISIBLE);
            if (mResList[position] == mCurrentResID) {
                cb.setChecked(true);
                cover.setVisibility(View.VISIBLE);
            } else {
                cb.setChecked(false);
                cover.setVisibility(View.GONE);
            }

            imageview.setImageResource(mResList[position]);
            imageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCurrentResID != mResList[position]) {
                        mCurrentResID = mResList[position];
                        cb.setChecked(true);
                        cover.setVisibility(View.VISIBLE);
                        notifyDataSetChanged();
                    } else {
                        mCurrentResID = 0;
                        cb.setChecked(false);
                        cover.setVisibility(View.GONE);
                        notifyDataSetChanged();
                    }
                }
            });

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }
}
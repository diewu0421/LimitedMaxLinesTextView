package com.example.zenglw.maxlinetextview.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zenglw.maxlinetextview.R;

public class LimitedLineTextView extends android.support.v7.widget.AppCompatTextView implements View.OnClickListener {
    private int maxLines;
    private String mDrawText;
    private static final String TAG = LimitedLineTextView.class.getSimpleName();
    private Rect mRect;
    private Paint mPaint;
    private float mTvSize;
    private int mHeight;


    public LimitedLineTextView(Context context) {
        this(context, null);
    }

    public LimitedLineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LimitedLineTextView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setText("niadsfasdfasdfhaoiadsfasdfasdfhaoiadsfasdfasdfhaoiadsfasdfasdfhaoiadsfasdfasdfhao");
//        setText("...展示");
        mDrawText = getText().toString();
//        setBackgroundResource();
//        setBackground(getResources().getDrawable(android.R.attr.selectableItemBackground));
//        setBackgroundDrawable(getResources().getDrawable(android.R.attr.selectableItemBackground));
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.attr.selectableItemBackground);
        setOnClickListener(this);
//        setBackgroundColor(Color.RED);
        post(new Runnable() {
            @Override
            public void run() {
//                Log.e(TAG, "run: length " + getText().length());
//                     int view = getLastCharIndexForLimitTextView(LimitedLineTextView.this, getText().toString(), getMeasuredWidth(), 2);
//                     Log.e(TAG, "run: index = " + view);
                mHeight = getMeasuredHeight();
                limitStringTo140(getText().toString(), LimitedLineTextView.this, getMeasuredWidth(), LimitedLineTextView.this);
                int[] ints = measureTextViewHeight(LimitedLineTextView.this, getText().toString(), getMeasuredWidth(), 3);
                Log.e(TAG, "run: lastIndex = " + ints[0] + "  height " + ints[1]);
                getLayoutParams().height = ints[1];
                requestLayout();
            }
        });
    }

    /**
     * 限制为300字符，并且添加showmore和show more的点击事件
     *
     * @param summerize
     * @param textView
     * @param width
     * @param clickListener textview的clickListener
     */
    public void limitStringTo140(String summerize, final TextView textView, int width, final View.OnClickListener clickListener) {
        final long startTime = System.currentTimeMillis();
        if (textView == null) return;
//        int width = textView.getWidth();//在recyclerView和ListView中，由于复用的原因，这个TextView可能以前就画好了，能获得宽度
        if (width == 0) width = 1000;//获取textview的实际宽度，这里可以用各种方式（一般是dp转px写死）填入TextView的宽度
        int lastCharIndex = getLastCharIndexForLimitTextView(textView, summerize, width, 3);
        if (lastCharIndex < 0) {//如果行数没超过限制
            textView.setText(summerize);
            return;
        }
        //如果超出了行数限制
        textView.setMovementMethod(LinkMovementMethod.getInstance());//this will deprive the recyclerView's focus
//        if (lastCharIndex < 0) lastCharIndex = 300;
        String explicitText = null;
        String showmore = "...展示";
        if (summerize.charAt(lastCharIndex) == '\n') {//manual enter
            explicitText = summerize.substring(0, lastCharIndex);
        } else {//TextView auto enter
            Log.i("Alex", "the last char of this line is --" + lastCharIndex + "  length =- " + showmore.length());
            explicitText = summerize.substring(0, lastCharIndex + 1 - 3);
        }
        int sourceLength = explicitText.length();
        explicitText = explicitText + showmore;
        final SpannableString mSpan = new SpannableString(explicitText);
        final String finalSummerize = summerize;
        mSpan.setSpan(new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(textView.getResources().getColor(R.color.colorPrimary));
                ds.setAntiAlias(true);
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {//"...show more" click event
                Log.i("Alex", "click showmore");
                textView.setText(finalSummerize);
                textView.setOnClickListener(null);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        if (clickListener != null)
//                            textView.setOnClickListener(clickListener);//prevent the double click
                    }
                }, 20);
            }
        }, sourceLength, explicitText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(mSpan);
        Log.i("Alex", "字符串处理耗时" + (System.currentTimeMillis() - startTime));
    }

    /**
     * 在不绘制textView的情况下算出textView的高度，并且根据最大行数得到应该显示最后一个字符的下标，请在主线程顺序执行，第一个返回值是最后一个字符的下标，第二个返回值是TextView最终应该占用的高度
     *
     * @param textView
     * @param content
     * @param width
     * @param maxLine
     * @return
     */
    public int[] measureTextViewHeight(TextView textView, String content, int width, int maxLine) {
        Log.i("Alex", "宽度是" + width);
        TextPaint textPaint = textView.getPaint();
        StaticLayout staticLayout = new StaticLayout(content, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        int[] result = new int[2];
        if (staticLayout.getLineCount() > maxLine) {//如果行数超出限制
            int lastIndex = staticLayout.getLineStart(maxLine) - 1;
            result[0] = lastIndex;
            result[1] = new StaticLayout(content.substring(0, lastIndex), textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, false).getHeight();
            return result;
        } else {//如果行数没有超出限制
            result[0] = -1;
            result[1] = staticLayout.getHeight();
            return result;
        }
    }


    /**
     * get the last char index for max limit row,if not exceed the limit,return -1
     *
     * @param textView
     * @param content
     * @param width
     * @param maxLine
     * @return
     */
    public int getLastCharIndexForLimitTextView(TextView textView, String content, int width, int maxLine) {
        Log.i("Alex", "宽度是" + width);
        TextPaint textPaint = textView.getPaint();
        StaticLayout staticLayout = new StaticLayout(content, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        if (staticLayout.getLineCount() > maxLine)
            return staticLayout.getLineStart(maxLine) - 1;//exceed
        else return -1;//not exceed the max line
    }

    @Override
    public void onClick(View v) {
        final int height = v.getMeasuredHeight();
        Log.e(TAG, "onClick: height = " + height);

        setText(mDrawText);
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "xxx", 0, 1).setDuration(300);
        final int offset = mHeight - height;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {


                float value = (float) animation.getAnimatedValue();
                int mfactor = (int) (value * offset);
//                setHeight((int) (mfactor + height));
                getLayoutParams().height = mfactor + height;
                requestLayout();
                Log.e(TAG, "onAnimationUpdate: value = " + value + "   mfactor = " + mfactor);
            }
        });
        animator.start();

        //拟制到案
//        setOnClickListener(null);
    }
}
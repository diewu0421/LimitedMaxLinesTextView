package com.example.zenglw.maxlinetextview.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.zenglw.maxlinetextview.R;

public class LimitedLineTextView extends android.support.v7.widget.AppCompatTextView implements View.OnClickListener {
    private String mDrawText;
    private static final String TAG = LimitedLineTextView.class.getSimpleName();
    private int mHeight;
    private ObjectAnimator mAnimator;
    private int mMaxLine;

    public LimitedLineTextView(Context context) {
        this(context, null);
    }

    public LimitedLineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LimitedLineTextView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LimitedLineTextView);
        //获取自定义的最大显示行数，默认为最大为3行
        mMaxLine = array.getInteger(R.styleable.LimitedLineTextView_MaxLine, 3);
        array.recycle();
        mDrawText = getText().toString();
        setOnClickListener(this);
        post(mRunnable);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mHeight = getMeasuredHeight();
            showLimitedText(getText().toString(), LimitedLineTextView.this, getMeasuredWidth());
            int[] ints = measureTextViewHeight(LimitedLineTextView.this, getText().toString(), getMeasuredWidth(), mMaxLine);
            Log.e(TAG, "run: lastIndex = " + ints[0] + "  height " + ints[1]);
            getLayoutParams().height = ints[1];
            requestLayout();
        }
    };

    /**
     * 根据最大行显示文本
     * @param allText
     * @param textView
     * @param width
     */
    public void showLimitedText(String allText, final TextView textView, int width) {
        if (textView == null) return;
//        int width = textView.getWidth();//在recyclerView和ListView中，由于复用的原因，这个TextView可能以前就画好了，能获得宽度
        if (width == 0) width = 1000;//获取textview的实际宽度，这里可以用各种方式（一般是dp转px写死）填入TextView的宽度
        int lastCharIndex = getLastCharIndex(textView, allText, width, mMaxLine);
        //如果行数没超过限制
        if (lastCharIndex < 0) {
            textView.setText(allText);
            return;
        }
        //如果超出了行数限制
        textView.setMovementMethod(LinkMovementMethod.getInstance());//this will deprive the recyclerView's focus
//        if (lastCharIndex < 0) lastCharIndex = 300;
        String LimitedText;
        String showMoreText = "...展示";

        if (allText.charAt(lastCharIndex) == '\n') {
            //如果文本中有换行符
            LimitedText = allText.substring(0, lastCharIndex);
        } else {
            //
            LimitedText = allText.substring(0, lastCharIndex + 1 - 3);
        }
        int sourceLength = LimitedText.length();
        LimitedText = LimitedText + showMoreText;
        final SpannableString mSpan = new SpannableString(LimitedText);
        mSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) { }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                //设置文本的颜色，去掉下划线
                ds.setColor(textView.getResources().getColor(R.color.colorPrimary));
                ds.setAntiAlias(true);
                ds.setUnderlineText(false);
            }

        }, sourceLength, LimitedText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(mSpan);
    }

    /**
     * 测量文本在显示最大行之后真实的高度
     *
     * @param textView
     * @param content
     * @param width
     * @param maxLine
     * @return 返回一个大小为2的int数据，ints[0]表示在最大行显示的最后一个下标 ints[1]表示在最大行显示的真实高度
     */
    public int[] measureTextViewHeight(TextView textView, String content, int width, int maxLine) {
        TextPaint textPaint = textView.getPaint();
        StaticLayout staticLayout = new StaticLayout(content, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        int[] result = new int[2];
        if (staticLayout.getLineCount() > maxLine) {
            //如果行数超出限制
            int lastIndex = staticLayout.getLineStart(maxLine) - 1;
            result[0] = lastIndex;
            result[1] = new StaticLayout(content.substring(0, lastIndex), textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, false).getHeight();
            return result;
        } else {
            //如果行数没有超出限制
            result[0] = -1;
            result[1] = staticLayout.getHeight();
            return result;
        }
    }

    /**
     * 获取最大显示行的最后一个下标
     *
     * @param textView
     * @param content
     * @param width
     * @param maxLine
     * @return 如果获取到，则返回对应的下标，如果没有获取到，则返回-1
     */
    public int getLastCharIndex(TextView textView, String content, int width, int maxLine) {
        TextPaint textPaint = textView.getPaint();
        StaticLayout staticLayout = new StaticLayout(content, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        if (staticLayout.getLineCount() > maxLine)
            //超过最大行
            return staticLayout.getLineStart(maxLine) - 1;
        else
            //没有超过最大行
            return -1;
    }

    @Override
    public void onClick(View v) {
        final int height = v.getMeasuredHeight();
        final int offset = mHeight - height;
        //点击则显示最大能显示的内容
        setText(mDrawText);
        mAnimator = ObjectAnimator.ofFloat(this, "xxx", 0, 1).setDuration(300);
        mAnimator.setInterpolator(new FastOutLinearInInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                int mfactor = (int) (value * offset);
                //改变高度
                getLayoutParams().height = mfactor + height;
                requestLayout();
            }
        });
        mAnimator.start();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator.removeAllListeners();
            mAnimator.removeAllUpdateListeners();
            mAnimator = null;
        }
    }
}
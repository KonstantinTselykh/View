package com.example.kostya.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * Created by kostya on 27.09.16.
 */

public class CustomView extends View {
    OnCustomEventListener mListener;

    //vertex && vertex settings
    private Vertex mLeftTopVtx;
    private Vertex mRightBottomVtx;
    private Vertex mLeftBottomVtx;
    private Vertex mRightTopVtx;
    private Vertex mCenterVtx;
    private Paint mLineColorPaint;
    private Paint mBitmapColorPaint;
    private Paint mCentralFigureColorPaint;
    private int mFigureSize;
    private int mWidth, mHeight;

    //animation && animation settings
    private AnimatorSet mAnimatorSet;
    private int mAnimationCount;
    private int mAnimationSpeedInMS;
    private int mPaintAlpha;
    private int mBitmapPicture;
    private ObjectAnimator mWrappedBlink;
    private ObjectAnimator mUnwrappedBlink;
    private Bitmap mBitmap;
    private Bitmap mCenterBitmap;
    private AnimatorSet mLeftTop;

    public CustomView(Context context, AttributeSet attrs){
        super(context,attrs);
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomView,
                0, 0
        );

        int color;
        int lineWidth;
        try{
            mFigureSize = (int)array.getDimension(R.styleable.CustomView_figureSize,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                            getResources().getDisplayMetrics()));
            color = array.getColor(R.styleable.CustomView_lineColor,
                    Color.BLUE);
            mAnimationSpeedInMS = array.getInt(R.styleable.CustomView_animationSpeed,
                    450);
            mBitmapPicture = array.getResourceId(R.styleable.CustomView_figureDrawable,
                    -1);
            lineWidth = array.getInt(R.styleable.CustomView_lineWidth, 2);
        } finally {
            array.recycle();
        }

        init(color,lineWidth);
    }

    public interface  OnCustomEventListener{
        void onCollapsed();
    }

    public void setCustomEventListener(OnCustomEventListener customEventListener){
        mListener = customEventListener;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

        int desiredWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250,
                getResources().getDisplayMetrics());
        int desiredHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250,
                getResources().getDisplayMetrics());

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Measure mWidth
        if(widthMode == MeasureSpec.EXACTLY){
            //MUST be this size
            if(widthSize > desiredWidth)
                mWidth = desiredWidth;
            else {
                mWidth = widthSize;
                mFigureSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                        getResources().getDisplayMetrics());
            }
        } else if(widthMode == MeasureSpec.AT_MOST){
            //Cant be bigger than
            mWidth = desiredWidth / 4;
        } else{
            //be  whatever I want
            mWidth = desiredWidth;
        }

        if(heightMode == MeasureSpec.EXACTLY){
            //MUST be this size
            if(heightSize > desiredHeight)
                mHeight = desiredHeight;
            else {
                mHeight = heightSize;
                mFigureSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                        getResources().getDisplayMetrics());
            }
        } else if(heightMode == MeasureSpec.AT_MOST){
            //Cant be bigger than
            mHeight = desiredHeight / 4;
        } else{
            //be  whatever I want
            mHeight = desiredHeight;
        }



        if(mWidth!= mHeight && mWidth < mHeight)
            mHeight = mWidth;
        else
            mWidth = mHeight;

        setMeasuredDimension(mWidth,mHeight);
        initVertex();
    }

    @Override
    public int getMinimumHeight() {
        return super.getMinimumHeight();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(mAnimationCount == 0)
            mPaintAlpha = 0;
        mCentralFigureColorPaint.setAlpha(mPaintAlpha);
        drawLines(canvas);
        drawBitmap(canvas);

        handler.removeMessages(0);
        handler.sendEmptyMessageDelayed(0, 1000 / 30);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            invalidate();
        }
    };

    //user interactions
    public void setLineWidth(float mWidth){
        mLineColorPaint.setStrokeWidth(mWidth);
    }

    public void setPaintAlpha(int mPaintAlpha) {
        this.mPaintAlpha = mPaintAlpha;
    }

    public void setAnimationSpeedInMS(int speedMS){
        mAnimationSpeedInMS = speedMS;
    }

    public void setLineColor(int color){
        mLineColorPaint.setColor(color);
    }

    public void setBitmapColor(int color){
        ColorFilter colorFilter = new LightingColorFilter(color,color);
        mBitmapColorPaint.setColorFilter(colorFilter);

    }

    public void setCentralFigureColor(int color){
        ColorFilter colorFilter = new LightingColorFilter(color,color);
        mCentralFigureColorPaint.setColorFilter(colorFilter);
    }

    public void setBitmapDrawable(int res){
        mBitmapPicture = res;
    }

    public int getPaintAlpha() {
        return mPaintAlpha;
    }

    private void init(int color, float lineWidth){
        mLineColorPaint = new Paint();
        mLineColorPaint.setStyle(Paint.Style.FILL);
        mLineColorPaint.setStrokeWidth(lineWidth);
        mLineColorPaint.setColor(color);

        mBitmapColorPaint = new Paint();
        mBitmapColorPaint.setStyle(Paint.Style.FILL);
        mBitmapColorPaint.setColor(color);

        mCentralFigureColorPaint = new Paint();
        mCentralFigureColorPaint.setStyle(Paint.Style.FILL);
        mCentralFigureColorPaint.setColor(color);

    }

    private void initVertex() {
        mWidth = mWidth - mFigureSize;
        mHeight = mHeight - mFigureSize;
        mLeftTopVtx = new Vertex(0, 0);
        mRightBottomVtx = new Vertex(mWidth, mHeight);
        mRightTopVtx = new Vertex(mWidth, 0);
        mLeftBottomVtx = new Vertex(0, mHeight);
        mCenterVtx = new Vertex(mWidth/2, mHeight/2);

        createBitmap();
        startAnimations();
    }

    private void createBitmap(){
        int image = getBitmapDrawable(); //get user image
        if(image == -1) {
            mBitmap = Bitmap.createBitmap(mFigureSize, mFigureSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mBitmap);
            canvas.drawRect(0, 0, mFigureSize, mFigureSize, mLineColorPaint);
        } else {
            mBitmap = BitmapFactory.decodeResource(getResources(), image);
        }
        int size = mBitmap.getWidth();
        if(size > mFigureSize || size == 0){
            size = mFigureSize;
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mFigureSize, mFigureSize, true);
        }
        mFigureSize = size;
        mCenterBitmap = Bitmap.createScaledBitmap(mBitmap, mFigureSize * 2, mFigureSize * 2, true);
    }

    private void startAnimations(){
        setAnimator();
        mAnimatorSet.start();
        //mAnimatorSet.setInterpolator(new ReverseInterpolator());

        mWrappedBlink = ObjectAnimator.ofInt(CustomView.this, "mPaintAlpha", 0 ,255);
        mWrappedBlink.setDuration(500);
        mWrappedBlink.setRepeatCount(2);

        mUnwrappedBlink = ObjectAnimator.ofFloat(CustomView.this, "alpha", 0f, 6f);
        mUnwrappedBlink.setDuration(500);
        mUnwrappedBlink.setRepeatCount(2);

        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mPaintAlpha = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mWrappedBlink.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mWrappedBlink.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(mAnimationCount == 0) {
                    mUnwrappedBlink.start();
                }

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationCount++;
                mAnimatorSet.start();
                if(mAnimationCount == 2){
                    if(mListener!= null)
                        mListener.onCollapsed();
                    mAnimationCount = 0;
                    startAnimations();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    private void setAnimator(){

        ObjectAnimator leftTopVtxAnimation = ObjectAnimator.ofInt(mLeftTopVtx,"top",0,mHeight/2);
        leftTopVtxAnimation.setDuration(mAnimationSpeedInMS);

        ObjectAnimator leftBottomVtxAnimation = ObjectAnimator.ofInt(mLeftBottomVtx,"left",0,mHeight/2);
        leftBottomVtxAnimation.setDuration(mAnimationSpeedInMS);

        ObjectAnimator rightBottomVtxAnimation = ObjectAnimator.ofInt(mRightBottomVtx,"top",mWidth,mHeight/2 );
        rightBottomVtxAnimation.setDuration(mAnimationSpeedInMS);
        rightBottomVtxAnimation.setStartDelay(mAnimationSpeedInMS - mAnimationSpeedInMS/2);

        ObjectAnimator rightTopVtxAnimation = ObjectAnimator.ofInt(mRightTopVtx,"left",mWidth,mHeight/2);
        rightTopVtxAnimation.setDuration(mAnimationSpeedInMS);
        rightTopVtxAnimation.setStartDelay(mAnimationSpeedInMS);

        //second animation
        ObjectAnimator leftTopToCenter = ObjectAnimator.ofInt(mLeftTopVtx,"left",0,mHeight/2);
        leftTopToCenter.setDuration(mAnimationSpeedInMS);
        leftTopToCenter.setStartDelay(mAnimationSpeedInMS);

        ObjectAnimator leftBottomToCenter = ObjectAnimator.ofInt(mLeftBottomVtx, "top", mWidth , mHeight/2);
        leftBottomToCenter.setDuration(mAnimationSpeedInMS);
        leftBottomToCenter.setStartDelay(mAnimationSpeedInMS);

        ObjectAnimator rightBottomToCenter = ObjectAnimator.ofInt(mRightBottomVtx,"left",mWidth, mHeight/2);
        rightBottomToCenter.setDuration(mAnimationSpeedInMS);
        rightBottomToCenter.setStartDelay(mAnimationSpeedInMS);

        ObjectAnimator rightTopToCenter = ObjectAnimator.ofInt(mRightTopVtx,"top",0,mHeight/2);
        rightTopToCenter.setDuration(mAnimationSpeedInMS);
        rightTopToCenter.setStartDelay(mAnimationSpeedInMS);

        //new animation
        ObjectAnimator leftTopToRightBottomX = ObjectAnimator.ofInt(mLeftTopVtx, "left",mHeight / 2, mHeight);
        ObjectAnimator leftTopToRightBottomY = ObjectAnimator.ofInt(mLeftTopVtx, "top", mHeight / 2, mHeight);

        ObjectAnimator leftBottomToRightTopX = ObjectAnimator.ofInt(mLeftBottomVtx, "left", mHeight / 2, mWidth);
        ObjectAnimator leftBottomToRightTopY = ObjectAnimator.ofInt(mLeftBottomVtx, "top", mHeight/2, 0);

        ObjectAnimator rightTopToLeftBottomX = ObjectAnimator.ofInt(mRightTopVtx, "left", mHeight / 2, 0);
        ObjectAnimator rightTopToLeftBottomY = ObjectAnimator.ofInt(mRightTopVtx, "top", mHeight / 2, mWidth);

        ObjectAnimator rightBottomToLeftTopX = ObjectAnimator.ofInt(mRightBottomVtx, "left", mWidth / 2, 0 );
        ObjectAnimator rightBottomToLeftTopY = ObjectAnimator.ofInt(mRightBottomVtx, "top",  mWidth / 2, 0);

        AnimatorSet leftTopAnimatorSet = new AnimatorSet();
        leftTopAnimatorSet.play(leftTopVtxAnimation);
        leftTopAnimatorSet.play(leftTopToRightBottomX).with(leftTopToRightBottomY).after(leftTopToCenter);
        leftTopAnimatorSet.setDuration(mAnimationSpeedInMS);

        AnimatorSet leftBottomAnimatorSet = new AnimatorSet();
        leftBottomAnimatorSet.play(leftBottomVtxAnimation);
        leftBottomAnimatorSet.play(leftBottomToRightTopX).with(leftBottomToRightTopY);//.after(leftBottomToCenter);
        leftBottomAnimatorSet.setDuration(mAnimationSpeedInMS);
        leftBottomAnimatorSet.setStartDelay(mAnimationSpeedInMS);

        AnimatorSet rightBottomAnimatorSet = new AnimatorSet();
        rightBottomAnimatorSet.play(rightBottomVtxAnimation);
        rightBottomAnimatorSet.play(rightBottomToLeftTopX).with(rightBottomToLeftTopY).after(rightBottomToCenter);
        rightBottomAnimatorSet.setDuration(mAnimationSpeedInMS);
        rightBottomAnimatorSet.setStartDelay(mAnimationSpeedInMS  * 2);

        AnimatorSet rightTopAnimatorSet = new AnimatorSet();
        rightTopAnimatorSet.play(rightTopVtxAnimation);//.before(rightTopToCenter);
        rightTopAnimatorSet.play(rightTopToLeftBottomX).with(rightTopToLeftBottomY).after(rightTopToCenter);
        rightTopAnimatorSet.setDuration(mAnimationSpeedInMS);
        rightTopAnimatorSet.setStartDelay(mAnimationSpeedInMS * 3);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.play(leftTopAnimatorSet);
        mAnimatorSet.play(leftBottomAnimatorSet);
        mAnimatorSet.play(rightBottomAnimatorSet);
        mAnimatorSet.play(rightTopAnimatorSet);

        //mAnimatorSet.play(leftTopAnimatorSet).before(leftBottomAnimatorSet);
        //mAnimatorSet.play(rightTopAnimatorSet);//.before(rightTopAnimatorSet);
        //mAnimatorSet.play(rightBottomAnimatorSet);//.before(rightTopAnimatorSet);
        //mAnimatorSet.play(rightTopAnimatorSet);

//        mAnimatorSet.play(leftTopVtxAnimation);
//        mAnimatorSet.play(leftBottomVtxAnimation);
//        mAnimatorSet.play(rightBottomVtxAnimation).with(leftTopToCenter);
//        mAnimatorSet.play(rightTopVtxAnimation).with(leftBottomToCenter);
//        mAnimatorSet.play(rightBottomToCenter).with(rightTopToCenter);

//        mAnimatorSet.play(leftTopVtxAnimation).before(leftTopToCenter).with(leftTopToCenter);
//        mAnimatorSet.play(leftBottomVtxAnimation);
//        mAnimatorSet.play(rightBottomVtxAnimation);
//        mAnimatorSet.play(rightTopVtxAnimation);
    }

    private void drawBitmap(Canvas canvas){

        canvas.drawBitmap(mBitmap, mLeftTopVtx.left, mLeftTopVtx.top, mBitmapColorPaint);
        canvas.drawBitmap(mBitmap, mLeftBottomVtx.left, mLeftBottomVtx.top, mBitmapColorPaint);
        canvas.drawBitmap(mBitmap, mRightTopVtx.left, mRightTopVtx.top, mBitmapColorPaint);
        canvas.drawBitmap(mBitmap, mRightBottomVtx.left, mRightBottomVtx.top, mBitmapColorPaint);
        canvas.drawBitmap(mCenterBitmap, mCenterVtx.left - mFigureSize/2, mCenterVtx.top - mFigureSize/2, mCentralFigureColorPaint);
    }

    private void drawLines(Canvas canvas){
        //left top - right top
        canvas.drawLine(
                mLeftTopVtx.left + mFigureSize/2, mLeftTopVtx.top + mFigureSize/2, //from
                mRightTopVtx.left + mFigureSize/2, mRightTopVtx.top + mFigureSize/2,   //to
                mLineColorPaint    //color
        );

        //left top - left bottom
        canvas.drawLine(
                mLeftTopVtx.left + mFigureSize/2, mLeftTopVtx.top + mFigureSize/2,
                mLeftBottomVtx.left +mFigureSize/2, mLeftBottomVtx.top + mFigureSize/2,
                mLineColorPaint
        );

        //left top - right bottom (diagonal)
        canvas.drawLine(
                mLeftTopVtx.left + mFigureSize/2, mLeftTopVtx.top + mFigureSize/2,
                mRightBottomVtx.left + mFigureSize/2, mRightBottomVtx.top + mFigureSize/2,
                mLineColorPaint
        );

        //right top - left bottom
        canvas.drawLine(
                mRightTopVtx.left + mFigureSize/2, mRightTopVtx.top + mFigureSize/2,
                mLeftBottomVtx.left + mFigureSize/2, mLeftBottomVtx.top + mFigureSize/2,
                mLineColorPaint
        );

        //right bottom - left bottom
        canvas.drawLine(
                mLeftBottomVtx.left +mFigureSize/2, mLeftBottomVtx.top + mFigureSize/2,
                mRightBottomVtx.left + mFigureSize/2, mRightBottomVtx.top + mFigureSize/2,
                mLineColorPaint
        );

        //right top - right bottom
        canvas.drawLine(
                mRightTopVtx.left + mFigureSize/2, mRightTopVtx.top + mFigureSize/2,
                mRightBottomVtx.left + mFigureSize/2, mRightBottomVtx.top + mFigureSize/2,
                mLineColorPaint
        );
    }

    private class Vertex {
        int left;
        int top;
        // mWidth, mHeight

        Vertex(int left, int top) {
            this.left = left;
            this.top = top;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        @Override
        public String toString() {
            return "Vertex{" +
                    "left=" + left +
                    ", top=" + top +
                    '}';
        }
    }

    //reverse animation
    private class ReverseInterpolator implements Interpolator {
        private final Interpolator delegate;

        ReverseInterpolator(Interpolator delegate){
            this.delegate = delegate;
        }

        ReverseInterpolator(){
            this(new LinearInterpolator());
        }

        @Override
        public float getInterpolation(float paramFloat){
            return Math.abs(paramFloat - 1f);
            //return 1 - delegate.getInterpolation(paramFloat);
        }
    }

    private int getBitmapDrawable(){
        return mBitmapPicture;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        Random randomInt = new Random();
        mAnimationSpeedInMS = randomInt.nextInt(700);
        return super.onTouchEvent(event);
    }
}

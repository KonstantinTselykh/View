package com.example.kostya.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;

import java.util.Random;

/**
 * Created by kostya on 27.09.16.
 */

public class CustomView extends View {
    private static final String TAG = CustomView.class.getSimpleName();
    private Paint mFigurePaint;
    private Vertex mLeftTopVtx;
    private Vertex mRightBottomVtx;
    private Vertex mLeftBottomVtx;
    private Vertex mRightTopVtx;
    private Vertex mCenterVtx;
    private int paintAlpha;

    private Paint mCentralPaint;

    private int mFigureSize = 20;
    private int mWidth, mHeight;
    private AnimatorSet mAnimatorSet;
    private int mAnimationCount;
    
    private int mRectSpeed = 300;
    private int mLineSpeed = 200;

    private ObjectAnimator mWrappedAnimation;
    private ObjectAnimator mUnwrappedAnimation;

    public CustomView(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
        setWillNotDraw(false);
    }

    private void init(){
        mFigurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFigurePaint.setStyle(Paint.Style.FILL);
        mFigurePaint.setColor(getResources().getColor(R.color.colorAccent));

        mCentralPaint = new Paint();
        mCentralPaint.setStyle(Paint.Style.FILL);
        mCentralPaint.setColor(getResources().getColor(R.color.colorAccent));
    }
    
    public void setFigureColor(int color){
        mFigurePaint.setColor(color);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

        int desiredWidth = mRectSpeed;
        int desiredHeight = mRectSpeed;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Measure width
        if(widthMode == MeasureSpec.EXACTLY){
            //MUST be this size
            if(widthSize > desiredWidth)
                mWidth = desiredWidth;
            else {
                mWidth = widthSize;
                mFigureSize = 10;
            }
        } else if(widthMode == MeasureSpec.AT_MOST){
            //Cant be bigger than
            mWidth = desiredWidth;
            mWidth = Math.min(desiredWidth,widthSize);
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
                mFigureSize = 10;
            }
        } else if(heightMode == MeasureSpec.AT_MOST){
            //Cant be bigger than
            mHeight = desiredHeight;
            mHeight = Math.min(desiredHeight,heightSize);
        } else{
            //be  whatever I want
            mHeight = desiredHeight;
        }

        setMeasuredDimension(mWidth,mHeight);

        mWidth = getWidth() - mFigureSize;
        mHeight = getHeight() - mFigureSize;

        if(mWidth!= mHeight && mWidth < mHeight)
            mHeight = mWidth;
        else
            mWidth = mHeight;

        initVertex();
    }

    private void initVertex() {
        mLeftTopVtx = new Vertex(mFigureSize, mFigureSize);
        mRightBottomVtx = new Vertex(mWidth, mHeight);
        mRightTopVtx = new Vertex(mWidth, mFigureSize);
        mLeftBottomVtx = new Vertex(mFigureSize, mHeight);
        mCenterVtx = new Vertex(mWidth/2, mHeight/2);

        startAnimations();
    }

    private void startAnimations(){
        setAnimator();
        mAnimatorSet.start();
        mAnimatorSet.setInterpolator(new ReverseInterpolator());

        mWrappedAnimation = ObjectAnimator.ofInt(CustomView.this, "paintAlpha", 0 ,255);
        mWrappedAnimation.setDuration(500);
        mWrappedAnimation.setRepeatCount(2);

        mUnwrappedAnimation = ObjectAnimator.ofFloat(CustomView.this, "alpha", 0f, 6f);
        mUnwrappedAnimation.setDuration(500);
        mUnwrappedAnimation.setRepeatCount(2);

        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                paintAlpha = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mWrappedAnimation.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mWrappedAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(mAnimationCount == 0)
                    mUnwrappedAnimation.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationCount++;
                mAnimatorSet.start();
                if(mAnimationCount == 2){
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
        leftTopVtxAnimation.setDuration(mRectSpeed);

        ObjectAnimator leftBottomVtxAnimation = ObjectAnimator.ofInt(mLeftBottomVtx,"left",0,mHeight/2);
        leftBottomVtxAnimation.setDuration(mRectSpeed);

        ObjectAnimator rightBottomVtxAnimation = ObjectAnimator.ofInt(mRightBottomVtx,"top",mWidth,mHeight/2 );
        rightBottomVtxAnimation.setDuration(mRectSpeed);

        ObjectAnimator rightTopVtxAnimation = ObjectAnimator.ofInt(mRightTopVtx,"left",mWidth,mHeight/2);
        rightTopVtxAnimation.setDuration(mRectSpeed);

        //second animation
        ObjectAnimator leftTopToCenter = ObjectAnimator.ofInt(mLeftTopVtx,"left",0,mHeight/2);
        leftTopToCenter.setDuration(mLineSpeed);

        ObjectAnimator leftBottomToCenter = ObjectAnimator.ofInt(mLeftBottomVtx, "top", mWidth , mHeight/2);
        leftBottomToCenter.setDuration(mLineSpeed);

        ObjectAnimator rightBottomToCenter = ObjectAnimator.ofInt(mRightBottomVtx,"left",mWidth, mHeight/2);
        rightBottomToCenter.setDuration(mLineSpeed);

        ObjectAnimator rightTopToCenter = ObjectAnimator.ofInt(mRightTopVtx,"top",0,mHeight/2);
        rightTopToCenter.setDuration(mLineSpeed);

        mAnimatorSet = new AnimatorSet();

        //animation play
        mAnimatorSet.play(leftTopVtxAnimation).before(leftBottomVtxAnimation);
        mAnimatorSet.play(leftBottomVtxAnimation).with(rightBottomVtxAnimation);
        mAnimatorSet.play(rightBottomVtxAnimation).with(rightTopVtxAnimation);

        //second animation
        mAnimatorSet.play(rightBottomToCenter).after(rightBottomVtxAnimation);
        mAnimatorSet.play(leftTopToCenter).with(leftBottomVtxAnimation);
        mAnimatorSet.play(rightTopToCenter).after(rightTopVtxAnimation);
        mAnimatorSet.play(leftBottomToCenter).with(leftBottomVtxAnimation);

    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
//        Log.d(TAG, "onDraw: vtx " + mLeftBottomVtx);
        mCentralPaint.setAlpha(paintAlpha);
        Log.d(TAG, "onDraw: paint.paintAlpha: " + mCentralPaint.getAlpha());
        drawRect(canvas);
        drawLines(canvas);
        postInvalidate();
    }

    public int getPaintAlpha() {
        return paintAlpha;
    }

    public void setPaintAlpha(int paintAlpha) {
        this.paintAlpha = paintAlpha;
    }

    private void drawRect(Canvas canvas){
        //rectangles
        canvas.drawRect(mLeftTopVtx.left, mLeftTopVtx.top,
                mLeftTopVtx.left + mFigureSize, mLeftTopVtx.top + mFigureSize,
                mFigurePaint); //left top rect

        canvas.drawRect(mRightBottomVtx.left,mRightBottomVtx.top,
                mRightBottomVtx.left + mFigureSize, mRightBottomVtx.top + mFigureSize,
                mFigurePaint); //right bottom rect

        canvas.drawRect(mLeftBottomVtx.left,mLeftBottomVtx.top,
                mLeftBottomVtx.left + mFigureSize, mLeftBottomVtx.top + mFigureSize,
                mFigurePaint); //left bottom rect

        canvas.drawRect(mRightTopVtx.left,mRightTopVtx.top,
                mRightTopVtx.left + mFigureSize, mRightTopVtx.top + mFigureSize,
                mFigurePaint); //right top rect

        canvas.drawRect(mCenterVtx.left,mCenterVtx.top,
                mCenterVtx.left + mFigureSize, mCenterVtx.top + mFigureSize,
                mCentralPaint);
    }




    private void drawLines(Canvas canvas){
        //left top - right top
        canvas.drawLine(
                mLeftTopVtx.left + mFigureSize/2, mLeftTopVtx.top + mFigureSize/2, //from
                mRightTopVtx.left + mFigureSize/2, mRightTopVtx.top + mFigureSize/2,   //to
                mFigurePaint    //color
        );

        //left top - left bottom
        canvas.drawLine(
                mLeftTopVtx.left + mFigureSize/2, mLeftTopVtx.top + mFigureSize/2,
                mLeftBottomVtx.left +mFigureSize/2, mLeftBottomVtx.top + mFigureSize/2,
                mFigurePaint
        );

        //left top - right bottom (diagonal)
        canvas.drawLine(
                mLeftTopVtx.left + mFigureSize/2, mLeftTopVtx.top + mFigureSize/2,
                mRightBottomVtx.left + mFigureSize/2, mRightBottomVtx.top + mFigureSize/2,
                mFigurePaint
        );

        //right top - left bottom
        canvas.drawLine(
                mRightTopVtx.left + mFigureSize/2, mRightTopVtx.top + mFigureSize/2,
                mLeftBottomVtx.left + mFigureSize/2, mLeftBottomVtx.top + mFigureSize/2,
                mFigurePaint
        );

        //right bottom - left bottom
        canvas.drawLine(
                mLeftBottomVtx.left +mFigureSize/2, mLeftBottomVtx.top + mFigureSize/2,
                mRightBottomVtx.left + mFigureSize/2, mRightBottomVtx.top + mFigureSize/2,
                mFigurePaint
        );

        //right top - right bottom
        canvas.drawLine(
                mRightTopVtx.left + mFigureSize/2, mRightTopVtx.top + mFigureSize/2,
                mRightBottomVtx.left + mFigureSize/2, mRightBottomVtx.top + mFigureSize/2,
                mFigurePaint
        );
    }

    private class Vertex {
        int left;
        int top;
        // width, height

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

    private class ReverseInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float paramFloat){
            return Math.abs(paramFloat - 1f);
        }
    }

    private void changeAnimationSpeed(){
        Random randomInt = new Random();
        mRectSpeed = randomInt.nextInt(700);
        mLineSpeed = randomInt.nextInt(300);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        Log.i(TAG, "Reach onTouch");
        changeAnimationSpeed();
        return super.onTouchEvent(event);
    }
}

package com.example.kostya.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Created by kostya on 27.09.16.
 */

public class CustomView extends View {

    //vertex && vertex settings
    private Vertex mLeftTopVtx;
    private Vertex mRightBottomVtx;
    private Vertex mLeftBottomVtx;
    private Vertex mRightTopVtx;
    private Vertex mCenterVtx;
    private Paint mLineColor;
    private Paint mCentralFigureColor;
    private int figureSize;
    private int width, height;

    //animation && animation settings
    private AnimatorSet mAnimatorSet;
    private int animationCount;
    private int animationSpeedInMS;
    private int paintAlpha;
    private int bitmapPicture;
    private ObjectAnimator mWrappedBlink;
    private ObjectAnimator mUnwrappedBlink;
    private Bitmap mBitmap;
    ColorFilter colorFilter;

    public CustomView(Context context, AttributeSet attrs){
        super(context,attrs);
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomView,
                0, 0
        );

        int color;
        try{
            figureSize = (int)array.getDimension(R.styleable.CustomView_figureSize,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                    getResources().getDisplayMetrics()));
            color = array.getColor(R.styleable.CustomView_lineColor,
                    Color.BLUE);
            animationSpeedInMS = array.getInt(R.styleable.CustomView_animationSpeed,
                    450);
            bitmapPicture = array.getResourceId(R.styleable.CustomView_figureDrawable,
                    R.drawable.ic_android_black_24dp);

        } finally {
            array.recycle();
        }

        init(color);
    }

    private void init(int color){
        mLineColor = new Paint();
        mLineColor.setStyle(Paint.Style.FILL);
        mLineColor.setColor(color);

        mCentralFigureColor = new Paint();
        mCentralFigureColor.setStyle(Paint.Style.FILL);
        mCentralFigureColor.setColor(color);
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

        //Measure width
        if(widthMode == MeasureSpec.EXACTLY){
            //MUST be this size
            if(widthSize > desiredWidth)
                width = desiredWidth;
            else {
                width = widthSize;
                figureSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                        getResources().getDisplayMetrics());;
            }
        } else if(widthMode == MeasureSpec.AT_MOST){
            //Cant be bigger than
            width = desiredWidth;
            width = Math.min(desiredWidth,widthSize);
        } else{
            //be  whatever I want
            width = desiredWidth;
        }

        if(heightMode == MeasureSpec.EXACTLY){
            //MUST be this size
            if(heightSize > desiredHeight)
                height = desiredHeight;
            else {
                height = heightSize;
                figureSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                        getResources().getDisplayMetrics());;
            }
        } else if(heightMode == MeasureSpec.AT_MOST){
            //Cant be bigger than
            height = desiredHeight;
            height = Math.min(desiredHeight,heightSize);
        } else{
            //be  whatever I want
            height = desiredHeight;
        }

        setMeasuredDimension(width,height);

        width = width - figureSize;
        height = height - figureSize;

        if(width!= height && width < height)
            height = width;
        else
            width = height;

        initVertex();
    }

    private void initVertex() {
        mLeftTopVtx = new Vertex(figureSize, figureSize);
        mRightBottomVtx = new Vertex(width, height);
        mRightTopVtx = new Vertex(width, figureSize);
        mLeftBottomVtx = new Vertex(figureSize, height);
        mCenterVtx = new Vertex(width/2, height/2);

        startAnimations();
    }

    private void startAnimations(){
        setAnimator();
        mAnimatorSet.start();
        mAnimatorSet.setInterpolator(new ReverseInterpolator());

        mWrappedBlink = ObjectAnimator.ofInt(CustomView.this, "paintAlpha", 0 ,255);
        mWrappedBlink.setDuration(500);
        mWrappedBlink.setRepeatCount(2);

        mUnwrappedBlink = ObjectAnimator.ofFloat(CustomView.this, "alpha", 0f, 6f);
        mUnwrappedBlink.setDuration(500);
        mUnwrappedBlink.setRepeatCount(2);

        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                paintAlpha = 0;
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
                if(animationCount == 0)
                    mUnwrappedBlink.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animationCount++;
                mAnimatorSet.start();
                if(animationCount == 2){
                    animationCount = 0;
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

        //first animation
        ObjectAnimator leftTopVtxAnimation = ObjectAnimator.ofInt(mLeftTopVtx,"top",0,height/2);
        leftTopVtxAnimation.setDuration(animationSpeedInMS);

        ObjectAnimator leftBottomVtxAnimation = ObjectAnimator.ofInt(mLeftBottomVtx,"left",0,height/2);
        leftBottomVtxAnimation.setDuration(animationSpeedInMS);
        leftBottomVtxAnimation.setStartDelay(animationSpeedInMS - animationSpeedInMS/2);

        ObjectAnimator rightBottomVtxAnimation = ObjectAnimator.ofInt(mRightBottomVtx,"top",width,height/2 );
        rightBottomVtxAnimation.setDuration(animationSpeedInMS);
        rightBottomVtxAnimation.setStartDelay(animationSpeedInMS - animationSpeedInMS/2);

        ObjectAnimator rightTopVtxAnimation = ObjectAnimator.ofInt(mRightTopVtx,"left",width,height/2);
        rightTopVtxAnimation.setDuration(animationSpeedInMS);
        rightTopVtxAnimation.setStartDelay(animationSpeedInMS);

        //second animation
        ObjectAnimator leftTopToCenter = ObjectAnimator.ofInt(mLeftTopVtx,"left",0,height/2);
        leftTopToCenter.setDuration(animationSpeedInMS);
        leftTopToCenter.setStartDelay(animationSpeedInMS - animationSpeedInMS/2);

        ObjectAnimator leftBottomToCenter = ObjectAnimator.ofInt(mLeftBottomVtx, "top", width , height/2);
        leftBottomToCenter.setDuration(animationSpeedInMS);
        rightTopVtxAnimation.setStartDelay(animationSpeedInMS);

        ObjectAnimator rightBottomToCenter = ObjectAnimator.ofInt(mRightBottomVtx,"left",width, height/2);
        rightBottomToCenter.setDuration(animationSpeedInMS);
        rightBottomToCenter.setStartDelay(animationSpeedInMS + animationSpeedInMS/2);

        ObjectAnimator rightTopToCenter = ObjectAnimator.ofInt(mRightTopVtx,"top",0,height/2);
        rightTopToCenter.setDuration(animationSpeedInMS);
        rightTopToCenter.setStartDelay(animationSpeedInMS - animationSpeedInMS/2);


        mAnimatorSet = new AnimatorSet();

        mAnimatorSet.play(leftTopVtxAnimation);
        mAnimatorSet.play(leftBottomVtxAnimation);
        mAnimatorSet.play(rightBottomVtxAnimation).with(leftTopToCenter);
        mAnimatorSet.play(rightTopVtxAnimation).with(leftBottomToCenter);
        mAnimatorSet.play(rightBottomToCenter).with(rightTopToCenter);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        mCentralFigureColor.setAlpha(paintAlpha);
        drawBitmap(canvas);
        drawLines(canvas);
        postInvalidate();
    }

    public void changeBitmapColor(int color){
        colorFilter = new LightingColorFilter( color, color );
        mLineColor.setColorFilter(colorFilter);
    }

    private void drawBitmap(Canvas canvas){
        int image = getBitmapDrawable(); //get user image
        if(image == 0)
            image = R.drawable.ic_pets_black_36dp;

        mBitmap = BitmapFactory.decodeResource(getResources(),image);
        //changeBitmapColor();
        int size = mBitmap.getWidth();
        if(size > figureSize || size == 0){
            size = figureSize;
            mBitmap = Bitmap.createScaledBitmap(mBitmap, figureSize, figureSize, true);
        }
        figureSize = size;
        Bitmap centerBitmap = Bitmap.createScaledBitmap(mBitmap, figureSize * 2, figureSize * 2, true);

        //mBitmap.eraseColor(Color.YELLOW);


        canvas.drawBitmap(mBitmap, mLeftTopVtx.left,mLeftTopVtx.top,mLineColor);
        canvas.drawBitmap(mBitmap, mLeftBottomVtx.left, mLeftBottomVtx.top, mLineColor);
        canvas.drawBitmap(mBitmap, mRightTopVtx.left, mRightTopVtx.top, mLineColor);
        canvas.drawBitmap(mBitmap, mRightBottomVtx.left, mRightBottomVtx.top, mLineColor);
        canvas.drawBitmap(centerBitmap, mCenterVtx.left - figureSize/2, mCenterVtx.top - figureSize/2, mCentralFigureColor);
    }

    private void drawLines(Canvas canvas){
        //left top - right top
        canvas.drawLine(
                mLeftTopVtx.left + figureSize/2, mLeftTopVtx.top + figureSize/2, //from
                mRightTopVtx.left + figureSize/2, mRightTopVtx.top + figureSize/2,   //to
                mLineColor    //color
        );

        //left top - left bottom
        canvas.drawLine(
                mLeftTopVtx.left + figureSize/2, mLeftTopVtx.top + figureSize/2,
                mLeftBottomVtx.left +figureSize/2, mLeftBottomVtx.top + figureSize/2,
                mLineColor
        );

        //left top - right bottom (diagonal)
        canvas.drawLine(
                mLeftTopVtx.left + figureSize/2, mLeftTopVtx.top + figureSize/2,
                mRightBottomVtx.left + figureSize/2, mRightBottomVtx.top + figureSize/2,
                mLineColor
        );

        //right top - left bottom
        canvas.drawLine(
                mRightTopVtx.left + figureSize/2, mRightTopVtx.top + figureSize/2,
                mLeftBottomVtx.left + figureSize/2, mLeftBottomVtx.top + figureSize/2,
                mLineColor
        );

        //right bottom - left bottom
        canvas.drawLine(
                mLeftBottomVtx.left +figureSize/2, mLeftBottomVtx.top + figureSize/2,
                mRightBottomVtx.left + figureSize/2, mRightBottomVtx.top + figureSize/2,
                mLineColor
        );

        //right top - right bottom
        canvas.drawLine(
                mRightTopVtx.left + figureSize/2, mRightTopVtx.top + figureSize/2,
                mRightBottomVtx.left + figureSize/2, mRightBottomVtx.top + figureSize/2,
                mLineColor
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

    //reverse animation
    private class ReverseInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float paramFloat){
            return Math.abs(paramFloat - 1f);
        }
    }

    //sets visibility of central figure
    public int getPaintAlpha() {
        return paintAlpha;
    }

    public void setPaintAlpha(int paintAlpha) {
        this.paintAlpha = paintAlpha;
    }

    //user interactions
    public void setLineColor(int color){
        mLineColor.setColor(color);
    }

    private void setAnimationSpeedInMS(int speedMS){
        animationSpeedInMS = speedMS;
    }

    public void setBitmapDrawable(int res){
        bitmapPicture = res;
    }

    private int getBitmapDrawable(){
        return bitmapPicture;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
       return super.onTouchEvent(event);
    }
}

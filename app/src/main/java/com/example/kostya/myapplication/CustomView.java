package com.example.kostya.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
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

    //vertex && vertex settings
    private Vertex mLeftTopVtx;
    private Vertex mRightBottomVtx;
    private Vertex mLeftBottomVtx;
    private Vertex mRightTopVtx;
    private Vertex mCenterVtx;
    private Paint mLineColor;
    private Paint mFigureColor;
    private int figureSize;
    private int width, height;

    //animation && animation settings
    private AnimatorSet mAnimatorSet;
    private int animationCount;
    private int animationSpeed;
    private int paintAlpha;
    private int bitmapPicture;
    private ObjectAnimator mWrappedAnimation;
    private ObjectAnimator mUnwrappedAnimation;

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
            animationSpeed = array.getInt(R.styleable.CustomView_animationSpeed,
                    300);
        } finally {
            array.recycle();
        }

        init(color);
    }

    private void init(int color){
        mLineColor = new Paint();
        mLineColor.setStyle(Paint.Style.FILL);
        mLineColor.setColor(color);

        mFigureColor = new Paint();
        mFigureColor.setStyle(Paint.Style.FILL);
        mFigureColor.setColor(color);
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

        //width = getWidth() - figureSize;
        //height = getHeight() - figureSize;
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
                if(animationCount == 0)
                    mUnwrappedAnimation.start();
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
        leftTopVtxAnimation.setDuration(animationSpeed);

        ObjectAnimator leftBottomVtxAnimation = ObjectAnimator.ofInt(mLeftBottomVtx,"left",0,height/2);
        leftBottomVtxAnimation.setDuration(animationSpeed);

        ObjectAnimator rightBottomVtxAnimation = ObjectAnimator.ofInt(mRightBottomVtx,"top",width,height/2 );
        rightBottomVtxAnimation.setDuration(animationSpeed);

        ObjectAnimator rightTopVtxAnimation = ObjectAnimator.ofInt(mRightTopVtx,"left",width,height/2);
        rightTopVtxAnimation.setDuration(animationSpeed);

        //second animation
        ObjectAnimator leftTopToCenter = ObjectAnimator.ofInt(mLeftTopVtx,"left",0,height/2);
        leftTopToCenter.setDuration(animationSpeed);

        ObjectAnimator leftBottomToCenter = ObjectAnimator.ofInt(mLeftBottomVtx, "top", width , height/2);
        leftBottomToCenter.setDuration(animationSpeed);

        ObjectAnimator rightBottomToCenter = ObjectAnimator.ofInt(mRightBottomVtx,"left",width, height/2);
        rightBottomToCenter.setDuration(animationSpeed);

        ObjectAnimator rightTopToCenter = ObjectAnimator.ofInt(mRightTopVtx,"top",0,height/2);
        rightTopToCenter.setDuration(animationSpeed);

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

    //add a chance to user to choose a picture
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        mFigureColor.setAlpha(paintAlpha);
        Log.d(TAG, "onDraw: paint.paintAlpha: " + mFigureColor.getAlpha());
        drawBitmap(canvas);
        drawLines(canvas);
        postInvalidate();
    }

    private void drawBitmap(Canvas canvas){
        int image = getBitmapDrawable();

        if(image == 0)
            image = R.drawable.ic_android_black_24dp;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),image);

        int size = bitmap.getWidth();
        if(size > figureSize || size == 0){
            size = figureSize;
             bitmap = Bitmap.createScaledBitmap(bitmap,figureSize, figureSize, true);
        }
        figureSize = size;
        Bitmap centerBitmap = Bitmap.createScaledBitmap(bitmap,figureSize * 2, figureSize * 2, true);

        canvas.drawBitmap(bitmap, mLeftTopVtx.left, mLeftTopVtx.top, mLineColor);
        canvas.drawBitmap(bitmap, mLeftBottomVtx.left, mLeftBottomVtx.top, mLineColor);
        canvas.drawBitmap(bitmap, mRightTopVtx.left, mRightTopVtx.top, mLineColor);
        canvas.drawBitmap(bitmap, mRightBottomVtx.left, mRightBottomVtx.top, mLineColor);
        canvas.drawBitmap(centerBitmap, mCenterVtx.left - figureSize/2, mCenterVtx.top - figureSize/2, mFigureColor);
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
    public void setFigureColor(int color){
        mLineColor.setColor(color);
    }

    public void setCentralFigurePaint(int color){
        mFigureColor.setColor(color);
    }

    private void changeAnimationSpeed(){
        Random randomInt = new Random();
        animationSpeed = randomInt.nextInt(700);
    }

    public void setBitmapDrawable(int res){
        bitmapPicture = res;
    }

    private int getBitmapDrawable(){
        return bitmapPicture;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        Log.i(TAG, "Reach onTouch");
        changeAnimationSpeed();
        return super.onTouchEvent(event);
    }
}

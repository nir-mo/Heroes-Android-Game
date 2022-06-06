package com.nirmo.heroesgame.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class GifView extends View {
    private Movie gif;
    private long gifStartTime;
    private int movieMeasuredMovieWidth;
    private int movieMeasuredMovieHeight;
    private float movieLeft;
    private float movieTop;
    private float movieScale;

    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GifView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setGif(int rawResourceId) {
        java.io.InputStream inputStream = getContext().getResources().openRawResource(rawResourceId);
        this.gif = Movie.decodeStream(inputStream);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (gif != null) {
            int movieWidth = gif.width();
            int movieHeight = gif.height();

            /*
             * Calculate horizontal scaling
             */
            float scaleH = 1f;
            int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);

            if (measureModeWidth != View.MeasureSpec.UNSPECIFIED) {
                int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
                if (movieWidth > maximumWidth) {
                    scaleH = (float) movieWidth / (float) maximumWidth;
                }
            }

            /*
             * calculate vertical scaling
             */
            float scaleW = 1f;
            int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);

            if (measureModeHeight != View.MeasureSpec.UNSPECIFIED) {
                int maximumHeight = View.MeasureSpec.getSize(heightMeasureSpec);
                if (movieHeight > maximumHeight) {
                    scaleW = (float) movieHeight / (float) maximumHeight;
                }
            }
            /*
             * calculate overall scale
             */
            movieScale = 1f / Math.max(scaleH, scaleW);
            movieMeasuredMovieWidth = (int) (movieWidth * movieScale);
            movieMeasuredMovieHeight = (int) (movieHeight * movieScale);

            setMeasuredDimension(movieMeasuredMovieWidth, movieMeasuredMovieHeight);
        } else {
            /*
             * No movie set, just set minimum available size.
             */
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        /*
         * Calculate movieLeft / movieTop for drawing in center
         */
        movieLeft = (getWidth() - movieMeasuredMovieWidth) / 2f;
        movieTop = (getHeight() - movieMeasuredMovieHeight) / 2f;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        long now = SystemClock.uptimeMillis();
        if (gifStartTime == 0) {   // first time
            gifStartTime = now;
        }

        if (gif != null) {
            int duration = gif.duration();
            if (duration == 0) {
                duration = 1000;
            }
            int relTime = (int) ((now - gifStartTime) % duration);
            gif.setTime(relTime);
            canvas.save();
            canvas.scale(movieScale, movieScale);
            gif.draw(canvas, movieLeft / movieScale, movieTop / movieScale);
            canvas.restore();
            invalidate();
        }
    }
}

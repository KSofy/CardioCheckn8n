package com.example.cardiocheck;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Vista personalizada que dibuja ondas suaves animadas.
 */
public class WaveView extends View {

    private Paint paint;
    private float phase = 0f;
    private ValueAnimator animator;

    public WaveView(Context context) { super(context); init(); }
    public WaveView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(0x661890FF); // azul suave translúcido

        animator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        animator.setDuration(5000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            phase = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float centerY = h * 0.5f;
        float amp = h * 0.15f;
        int lines = 3;
        for (int l = 0; l < lines; l++) {
            float offset = l * 0.6f;
            float prevX = 0;
            float prevY = centerY;
            for (int x = 0; x < w; x++) {
                float y = (float) (centerY + amp * Math.sin((x / (float) w) * 4 * Math.PI + phase + offset));
                if (x > 0) canvas.drawLine(prevX, prevY, x, y, paint);
                prevX = x;
                prevY = y;
            }
        }
    }
}


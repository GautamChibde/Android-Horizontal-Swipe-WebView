package com.chibde.webviewhorizontal;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;

/**
 * @author gautam chibde on 22/6/17.
 */

public class HorizontalWebView extends WebView {
    private float x1 = -1;
    private int pageCount = 0;

    public HorizontalWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                float x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > 100) {
                    // Left to Right swipe action
                    if (x2 > x1) {
                        turnPageLeft();
                        return true;
                    }

                    // Right to left swipe action
                    else {
                        turnPageRight();
                        return true;
                    }

                }
                break;
        }
        return true;
    }

    private int current_x = 0;

    private void turnPageLeft() {
        if (getCurrentPage() > 0) {
            int scrollX = getPrevPagePosition();
            loadAnimation(scrollX);
            current_x = scrollX;
            scrollTo(scrollX, 0);
        }
    }

    private int getPrevPagePosition() {
        int prevPage = getCurrentPage() - 1;
        return (int) Math.ceil(prevPage * this.getMeasuredWidth());
    }

    private void turnPageRight() {
        if (getCurrentPage() < pageCount - 1) {
            int scrollX = getNextPagePosition();
            loadAnimation(scrollX);
            current_x = scrollX;
            scrollTo(scrollX, 0);
        }
    }

    private void loadAnimation(int scrollX) {
        ObjectAnimator anim = ObjectAnimator.ofInt(this, "scrollX",
                current_x, scrollX);
        anim.setDuration(500);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    private int getNextPagePosition() {
        int nextPage = getCurrentPage() + 1;
        return (int) Math.ceil(nextPage * this.getMeasuredWidth());
    }

    public int getCurrentPage() {
        return (int) (Math.ceil((double) getScrollX() / this.getMeasuredWidth()));
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}

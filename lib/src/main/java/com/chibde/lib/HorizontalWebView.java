package com.chibde.lib;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;

/**
 * @author gautam chibde on 22/6/17.
 */

public class HorizontalWebView extends WebView {
    private float x1 = -1;
    private int pageCount = 0;
    private int currentPage = 0;
    private int current_x = 0;

    public HorizontalWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectCSS();
                injectJavascript();
            }
        });

        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                int pageCount = Integer.parseInt(message);
                HorizontalWebView.this.setPageCount(pageCount);
                result.confirm();
                return true;
            }
        });
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

    private void turnPageLeft() {
        if (currentPage > 0) {
            int scrollX = getPrevPagePosition();
            loadAnimation(scrollX);
            current_x = scrollX;
            scrollTo(scrollX, 0);
        }
    }

    private int getPrevPagePosition() {
        return (int) Math.ceil(--currentPage * this.getMeasuredWidth());
    }

    private void turnPageRight() {
        if (currentPage < pageCount - 1) {
            int paddingOffset = 10;
            int scrollX = getNextPagePosition();
            loadAnimation(scrollX + paddingOffset);
            current_x = scrollX + paddingOffset;
            scrollTo(scrollX + paddingOffset, 0);
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
        return (int) Math.ceil(++currentPage * this.getMeasuredWidth());
    }

    private void injectJavascript() {
        String js = "function initialize(){\n" +
                "    var d = document.getElementsByTagName('body')[0];\n" +
                "    var ourH = window.innerHeight - 20;\n" +
                "    var ourW = window.innerWidth - (2*20);\n" +
                "    var fullH = d.offsetHeight;\n" +
                "    var pageCount = Math.floor(fullH/ourH)+1;\n" +
                "    var currentPage = 0;\n" +
                "    var newW = pageCount*window.innerWidth - (2*20);\n" +
                "    d.style.height = ourH+'px';\n" +
                "    d.style.width = newW+'px';\n" +
                "    d.style.margin = 0;\n" +
                "    d.style.webkitColumnGap = '40px';\n" +
                "    d.style.webkitColumnCount = pageCount;\n" +
                "    return pageCount;\n" +
                "}";
        this.loadUrl("javascript:" + js);
        this.loadUrl("javascript:alert(initialize())");
    }

    private void injectCSS() {
        try {
            InputStream inputStream = getContext().getAssets().open("style.css");
            byte[] buffer = new byte[inputStream.available()];
            if (inputStream.read(buffer) == 0) return;
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            this.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}

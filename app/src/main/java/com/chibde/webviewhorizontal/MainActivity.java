package com.chibde.webviewhorizontal;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private HorizontalWebView wv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        wv = (HorizontalWebView) findViewById(R.id.web_view);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                injectJavascript();
            }
        });

        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                int pageCount = Integer.parseInt(message);
                wv.setPageCount(pageCount);
                result.confirm();
                return true;
            }
        });
        wv.loadUrl("file:///android_asset/ch03.html");   // now it will not fail here
    }

    private void injectJavascript() {
        String js = "function initialize(){\n" +
                "    var d = document.getElementsByTagName('body')[0];\n" +
                "    var ourH = window.innerHeight;\n" +
                "    var ourW = window.innerWidth;\n" +
                "    var fullH = d.offsetHeight;\n" +
                "    var pageCount = Math.floor(fullH/ourH)+1;\n" +
                "    var currentPage = 0;\n" +
                "    var newW = pageCount*ourW;\n" +
                "    d.style.height = ourH+'px';\n" +
                "    d.style.width = newW+'px';\n" +
                "    d.style.margin = 0;\n" +
                "    d.style.webkitColumnCount = pageCount;\n" +
                "    return pageCount;\n" +
                "}";
        wv.loadUrl("javascript:" + js);
        wv.loadUrl("javascript:alert(initialize())");
    }
}

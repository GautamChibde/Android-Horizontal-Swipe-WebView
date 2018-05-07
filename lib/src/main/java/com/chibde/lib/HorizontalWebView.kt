package com.chibde.lib

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Base64
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient


/**
 * @author gautam chibde on 22/6/17.
 */

class HorizontalWebView(context: Context,
                        attrs: AttributeSet) : WebView(context, attrs) {
    private var x1 = -1f
    private var pageCount = 0
    private var currentPage = 0
    private var currentX = 0

    private val prevPagePosition: Int
        get() = Math.ceil((--currentPage * this.measuredWidth).toDouble()).toInt()

    private val nextPagePosition: Int
        get() = Math.ceil((++currentPage * this.measuredWidth).toDouble()).toInt()

    init {
        this.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                injectCSS()
                injectJavascript()
            }
        }

        this.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                val pageCount = Integer.parseInt(message)
                this@HorizontalWebView.setPageCount(pageCount)
                result.confirm()
                return true
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> super.onTouchEvent(event)
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                return super.onTouchEvent(event)
            }
            MotionEvent.ACTION_UP -> {
                val x2 = event.x
                val deltaX = x2 - x1
                if (Math.abs(deltaX) > 30) {
                    // Left to Right swipe action
                    return if (x2 > x1) {
                        turnPageLeft(deltaX)
                        true
                    } else {
                        turnPageRight(deltaX)
                        true
                    }// Right to left swipe action
                }
            }
            else -> super.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    private fun turnPageLeft(deltaX: Float) {
        if (currentPage > 0) {
            val scrollX = prevPagePosition
            loadAnimation(scrollX, deltaX)
            currentX = scrollX
            scrollTo(scrollX, 0)
        }
    }

    private fun turnPageRight(deltaX: Float) {
        if (currentPage < pageCount - 1) {
            val paddingOffset = 10
            val scrollX = nextPagePosition
            loadAnimation(scrollX + paddingOffset, deltaX)
            currentX = scrollX + paddingOffset
            scrollTo(scrollX + paddingOffset, 0)
        }
    }

    private fun loadAnimation(scrollX: Int, deltaX: Float) {
        val anim = ObjectAnimator.ofInt(this, "scrollX",
                currentX - deltaX.toInt(), scrollX)
        anim.duration = 500
        anim.interpolator = LinearInterpolator()
        anim.start()
    }

    private fun injectJavascript() {
        val js = "function initialize(){\n" +
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
                "}"
        this.loadUrl("javascript:$js")
        this.loadUrl("javascript:alert(initialize())")
    }

    private fun injectCSS() {
        try {
            val inputStream = context.assets.open("style.css")
            val buffer = ByteArray(inputStream.available())
            if (inputStream.read(buffer) == 0) return
            inputStream.close()
            val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
            this.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun setPageCount(pageCount: Int) {
        this.pageCount = pageCount
    }
}
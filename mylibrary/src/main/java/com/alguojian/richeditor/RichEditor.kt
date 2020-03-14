package com.alguojian.richeditor

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.alguojian.richeditor.RichUtils.currentTime
import com.alguojian.richeditor.RichUtils.decodeResource
import com.alguojian.richeditor.RichUtils.toBase64
import com.alguojian.richeditor.RichUtils.toBitmap
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

class RichEditor @SuppressLint("SetJavaScriptEnabled") constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : WebView(context, attrs, defStyleAttr) {
    enum class Type {
        BOLD, ITALIC, SUBSCRIPT, SUPERSCRIPT, STRIKETHROUGH, UNDERLINE, H1, H2, H3, H4, H5, H6, ORDEREDLIST, UNORDEREDLIST, JUSTIFYCENTER, JUSTIFYFULL, JUSTIFYLEFT, JUSTIFYRIGHT
    }

    interface OnTextChangeListener {
        fun onTextChange(text: String)
    }

    interface OnTextContentListener {
        fun onContent(content: String)
    }

    interface OnDecorationStateListener {
        fun onStateChangeListener(
            text: String,
            types: List<Type>
        )
    }

    interface AfterInitialLoadListener {
        fun onAfterInitialLoad(isReady: Boolean)
    }

    private var isReady = false
    private var mContents: String = ""
    private var mMaxHeight = -1
    private var mTextChangeListener: OnTextChangeListener? = null
    private var onTextContentListener: OnTextContentListener? = null
    private var mDecorationStateListener: OnDecorationStateListener? = null
    private var mLoadListener: AfterInitialLoadListener? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(
        context,
        attrs,
        R.attr.webViewStyle
    )

    private fun createWebviewClient(): EditorWebViewClient {
        return EditorWebViewClient()
    }

    fun setOnTextChangeListener(listener: OnTextChangeListener?) {
        mTextChangeListener = listener
    }

    fun setOnTextContentListener(listener: OnTextContentListener) {
        onTextContentListener = listener
    }

    fun setOnDecorationChangeListener(listener: OnDecorationStateListener) {
        mDecorationStateListener = listener
    }

    fun setOnInitialLoadListener(listener: AfterInitialLoadListener?) {
        mLoadListener = listener
    }

    fun setMaxHeight(maxHeight: Int) {
        mMaxHeight = maxHeight
    }

    private fun callback(text: String) {
        mContents = text.replaceFirst(CALLBACK_SCHEME.toRegex(), "")
        if (mTextChangeListener != null) {
            mTextChangeListener!!.onTextChange(mContents)
        }
        if (onTextContentListener != null) {
            onTextContentListener!!.onContent(getText())
        }
    }

    private fun stateCheck(text: String) {
        val state = text.replaceFirst(STATE_SCHEME.toRegex(), "").toUpperCase(Locale.ENGLISH)
        val types: MutableList<Type> = ArrayList()
        for (type in Type.values()) {
            if (TextUtils.indexOf(state, type.name) != -1) {
                types.add(type)
            }
        }
        if (mDecorationStateListener != null) {
            mDecorationStateListener!!.onStateChangeListener(state, types)
        }
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        val attrsArray = intArrayOf(
            R.attr.gravity
        )
        val ta = context.obtainStyledAttributes(attrs, attrsArray)
        val gravity = ta.getInt(0, View.NO_ID)
        when (gravity) {
            Gravity.START -> exec("javascript:RE.setTextAlign(\"left\")")
            Gravity.END -> exec("javascript:RE.setTextAlign(\"right\")")
            Gravity.TOP -> exec("javascript:RE.setVerticalAlign(\"top\")")
            Gravity.BOTTOM -> exec("javascript:RE.setVerticalAlign(\"bottom\")")
            Gravity.CENTER_VERTICAL -> exec("javascript:RE.setVerticalAlign(\"middle\")")
            Gravity.CENTER_HORIZONTAL -> exec("javascript:RE.setTextAlign(\"center\")")
            Gravity.CENTER -> {
                exec("javascript:RE.setVerticalAlign(\"middle\")")
                exec("javascript:RE.setTextAlign(\"center\")")
            }
        }
        ta.recycle()
    }

    // No handling
    var html: String?
        get() = mContents
        set(contents) {
            var contents = contents
            if (contents == null) {
                contents = ""
            }
            try {
                exec("javascript:RE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');")
            } catch (e: UnsupportedEncodingException) {
                // No handling
            }
            mContents = contents
        }

    fun setEditorFontColor(color: Int) {
        val hex = convertHexColorString(color)
        exec("javascript:RE.setBaseTextColor('$hex');")
    }

    fun scrollToBottom() {
        scrollTo(0, computeVerticalScrollRange())
    }

    fun setEditorFontSize(px: Int) {
        exec("javascript:RE.setBaseFontSize('" + px + "px');")
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        exec(
            "javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
                    + "px');"
        )
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        // still not support RTL.
        setPadding(start, top, end, bottom)
    }

    fun setEditorBackgroundColor(color: Int) {
        setBackgroundColor(color)
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
    }

    override fun setBackgroundResource(resid: Int) {
        val bitmap = decodeResource(context, resid)
        val base64 = toBase64(bitmap)
        bitmap.recycle()
        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64,$base64)');")
    }

    override fun setBackground(background: Drawable) {
        val bitmap = toBitmap(background)
        val base64 = toBase64(bitmap)
        bitmap.recycle()
        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64,$base64)');")
    }

    fun setBackground(url: String) {
        exec("javascript:RE.setBackgroundImage('url($url)');")
    }

    fun setEditorWidth(px: Int) {
        exec("javascript:RE.setWidth('" + px + "px');")
    }

    fun setEditorHeight(px: Int) {
        exec("javascript:RE.setHeight('" + px + "px');")
    }

    fun setPlaceholder(placeholder: String) {
        exec("javascript:RE.setPlaceholder('$placeholder');")
    }

    fun setInputEnabled(inputEnabled: Boolean) {
        exec("javascript:RE.setInputEnabled($inputEnabled)")
    }

    fun loadCSS(cssFile: String) {
        val jsCSSImport = "(function() {" +
                "    var head  = document.getElementsByTagName(\"head\")[0];" +
                "    var link  = document.createElement(\"link\");" +
                "    link.rel  = \"stylesheet\";" +
                "    link.type = \"text/css\";" +
                "    link.href = \"" + cssFile + "\";" +
                "    link.media = \"all\";" +
                "    head.appendChild(link);" +
                "}) ();"
        exec("javascript:$jsCSSImport")
    }

    private fun getText(): String {
        if (!html.isNullOrEmpty()) {
            val doc = Jsoup.parse(html);
            return doc.body().text();
        }
        return "";
    }

    fun queryCommandState(command: String) {
        exec("javascript:RE.queryCommandState('$command');")
    }

    fun undo() {
        exec("javascript:RE.undo();")
    }

    fun redo() {
        exec("javascript:RE.redo();")
    }

    fun setBold(isBold: Boolean) {
        exec("javascript:RE.setBold($isBold);")
    }

    fun setItalic(isItalic: Boolean) {
        exec("javascript:RE.setItalic($isItalic);")
    }

    fun setSubscript() {
        exec("javascript:RE.setSubscript();")
    }

    fun setSuperscript() {
        exec("javascript:RE.setSuperscript();")
    }

    fun setStrikeThrough() {
        exec("javascript:RE.setStrikeThrough();")
    }

    fun setUnderline(isUnderline: Boolean) {
        exec("javascript:RE.setUnderline($isUnderline);")
    }

    fun setLineHeight(heightInPixel: Int) {
        exec("javascript:RE.setLineHeight('" + heightInPixel + "px');")
    }

    fun setTextColor(color: Int) {
        exec("javascript:RE.backuprange();")
        val hex = convertHexColorString(color)
        exec("javascript:RE.setTextColor('$hex');")
    }

    fun setTextBackgroundColor(color: Int) {
        exec("javascript:RE.backuprange();")
        val hex = convertHexColorString(color)
        exec("javascript:RE.setTextBackgroundColor('$hex');")
    }

    fun setFontSize(fontSize: Int) {
        if (fontSize > 7 || fontSize < 1) {
            Log.e("RichEditor", "Font size should have a value between 1-7")
        }
        exec("javascript:RE.setFontSize('$fontSize');")
    }

    fun removeFormat() {
        exec("javascript:RE.removeFormat();")
    }

    fun setHeading(heading: Int) {
        exec("javascript:RE.setHeading('$heading');")
    }

    fun setIndent() {
        exec("javascript:RE.setIndent();")
    }

    fun setOutdent() {
        exec("javascript:RE.setOutdent();")
    }

    fun setAlignLeft() {
        exec("javascript:RE.setJustifyLeft();")
    }

    fun setAlignCenter() {
        exec("javascript:RE.setJustifyCenter();")
    }

    fun setAlignRight() {
        exec("javascript:RE.setJustifyRight();")
    }

    fun setBlockquote() {
        exec("javascript:RE.setBlockquote();")
    }

    fun setBullets() {
        exec("javascript:RE.setBullets();")
    }

    fun setNumbers() {
        exec("javascript:RE.setNumbers();")
    }

    fun insertText(text: String) {
        exec("javascript:RE.insertText($text);")
    }

    fun deleteOneWord() {
        exec("javascript:RE.deleteOneWord();")
    }

    fun insertImage(url: String, alt: String, imageWidthPercent: Int) {
        exec("javascript:RE.backuprange();")
        exec("javascript:RE.insertImage('$url', '$alt','$imageWidthPercent');")
    }

    fun insertImageWrapWidth(url: String, alt: String) {
        exec("javascript:RE.backuprange();")
        exec("javascript:RE.insertImageWrapWidth('$url', '$alt');")
    }

    fun insertLink(href: String, title: String) {
        exec("javascript:RE.backuprange();")
        exec("javascript:RE.insertLink('$href', '$title');")
    }

    fun insertTodo() {
        exec("javascript:RE.backuprange();")
        exec("javascript:RE.setTodo('$currentTime');")
    }

    fun focusEditor() {
        requestFocus()
        exec("javascript:RE.focus();")
    }

    fun refreshState() {
        exec("javascript:RE.enabledEditingItems();")
    }

    fun clearFocusEditor() {
        exec("javascript:RE.blurFocus();")
    }

    private fun convertHexColorString(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

    private fun exec(trigger: String) {
        if (isReady) {
            load(trigger)
        } else {
            postDelayed({ exec(trigger) }, 100)
        }
    }

    private fun load(trigger: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null)
        } else {
            loadUrl(trigger)
        }
    }

    private inner class EditorWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            isReady = url.equals(SETUP_HTML, ignoreCase = true)
            if (mLoadListener != null) {
                mLoadListener!!.onAfterInitialLoad(isReady)
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val decode: String
            decode = try {
                URLDecoder.decode(url, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                return false
            }
            if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0) {
                callback(decode)
                return true

                //字符串中第一次出现子字符串的字符位置,标识是否已什么开头
            } else if (TextUtils.indexOf(url, STATE_SCHEME) == 0) {
                stateCheck(decode)
                return true
            }
            return super.shouldOverrideUrlLoading(view, url)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mMaxHeight > -1 && measuredHeight > mMaxHeight) {
            setMeasuredDimension(measuredWidth, mMaxHeight)
        }
    }

    private var clickTime: Long = 0
    private var clickCount = 0
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - clickTime < 500) {
                    clickCount++
                } else {
                    clickCount = 1
                }
                clickTime = System.currentTimeMillis()
                canUpdate = clickCount < 2
            }
        }
        return super.onTouchEvent(event)
    }

    var canUpdate = true

    companion object {
        private const val SETUP_HTML = "file:///android_asset/editor.html"
        private const val CALLBACK_SCHEME = "re-callback://"
        private const val STATE_SCHEME = "re-state://"
    }

    init {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        settings.javaScriptEnabled = true
        webChromeClient = WebChromeClient()
        webViewClient = createWebviewClient()
        loadUrl(SETUP_HTML)
        applyAttributes(context, attrs)
    }
}
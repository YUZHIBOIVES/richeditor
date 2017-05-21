package com.invisible.richeditor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ives.yeung on 2016/11/10.
 */

public class DeletableEditText extends EditText {

    public static final String TAG = "YYAndroid";
    public static final int FORMAT_BOLD = 0x01;
    public static final int FORMAT_ITALIC = 0x02;
    public static final int FORMAT_UNDERLINED = 0x03;
    public static final int FORMAT_STRIKETHROUGH = 0x04;
    public static final int FORMAT_BULLET = 0x05;
    public static final int FORMAT_QUOTE = 0x06;
    public static final int FORMAT_LINK = 0x07;
    public static final int FORMAT_FONTCOLOR = 0x08;
    public static final int FORMAT_BGCOLOR = 0x09;
    public static final int FORMAT_TEXTSIZE = 0x10;

    public static final int FONT_BLACK = 0;
    public static final int FONT_GREEN= 1;
    public static final int FONT_BLUE = 2;
    public static final int FONT_RED = 3;
    public static final int FONT_YELLOW = 4;
    public static final int FONT_PURPLE = 5;

    public DeletableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DeletableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DeletableEditText(Context context) {
        super(context);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new DeleteInputConnection(super.onCreateInputConnection(outAttrs),
                true);
    }

    private class DeleteInputConnection extends InputConnectionWrapper {

        public DeleteInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }

    }

    /******************************** 添加字体样式功能 **************************************/

    /****粗体样式*****/
    public void bold(boolean valid) {
        if (valid) {
            styleValid(Typeface.BOLD, getSelectionStart(), getSelectionEnd());
        } else {
            styleInvalid(Typeface.BOLD, getSelectionStart(), getSelectionEnd());
        }
    }

    /****斜体样式*****/
    public void italic(boolean valid) {
        if (valid) {
            styleValid(Typeface.ITALIC, getSelectionStart(), getSelectionEnd());
        } else {
            styleInvalid(Typeface.ITALIC, getSelectionStart(), getSelectionEnd());
        }
    }

    protected void styleInvalid(int style, int start, int end) {
        switch (style) {
            case Typeface.NORMAL:
            case Typeface.BOLD:
            case Typeface.ITALIC:
            case Typeface.BOLD_ITALIC:
                break;
            default:
                return;
        }

        if (start >= end) {
            return;
        }
        //找出有Spans格式的串,遍历每个串remove格式
        StyleSpan[] spans = getEditableText().getSpans(start, end, StyleSpan.class);
        Log.d(TAG,"spans size : "+spans.length);
        List<CustomEdittextPart> list = new ArrayList<>();

        for (StyleSpan span : spans) {
            Log.d(TAG,"for span...");
            if (span.getStyle() == style) {
                list.add(new CustomEdittextPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
                Log.d(TAG,"span start : "+getEditableText().getSpanStart(span) + " span end : "
                        + getEditableText().getSpanEnd(span));
                getEditableText().removeSpan(span);
            }
        }

        for (CustomEdittextPart part : list) {
            if (part.isValid()) {
                if (part.getStart() < start) {
                    Log.d(TAG,"CParts-->part start = "+part.getStart());
                    styleValid(style, part.getStart(), start);
                }

                if (part.getEnd() > end) {
                    Log.d(TAG,"CParts-->part end = "+part.getEnd());
                    styleValid(style, end, part.getEnd());
                }
            }
        }
    }

    protected void styleValid(int style, int start, int end) {
        switch (style) {
            case Typeface.NORMAL:
            case Typeface.BOLD:
            case Typeface.ITALIC:
            case Typeface.BOLD_ITALIC:
                break;
            default:
                return;
        }
        //无效
        if (start >= end) {
            return;
        }
        getEditableText().setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public boolean contains(int format) {
        switch (format) {
            case FORMAT_BOLD:
                return containStyle(Typeface.BOLD, getSelectionStart(), getSelectionEnd());
            case FORMAT_ITALIC:
                return containStyle(Typeface.ITALIC, getSelectionStart(), getSelectionEnd());
            case FORMAT_UNDERLINED:
                return containUnderline(getSelectionStart(), getSelectionEnd());
            case FORMAT_FONTCOLOR:
                return containFontColor(getSelectionStart(), getSelectionEnd());
            case FORMAT_BGCOLOR:
                return containbgColor(getSelectionStart(), getSelectionEnd());
            case FORMAT_TEXTSIZE:
                return containtextSize(getSelectionStart(), getSelectionEnd());
//            case FORMAT_STRIKETHROUGH:
//                return containStrikethrough(getSelectionStart(), getSelectionEnd());
//            case FORMAT_BULLET:
//                return containBullet();
//            case FORMAT_QUOTE:
//                return containQuote();
//            case FORMAT_LINK:
//                return containLink(getSelectionStart(), getSelectionEnd());
            default:
                return false;
        }
    }

    protected boolean containStyle(int style, int start, int end) {
        switch (style) {
            case Typeface.NORMAL:
            case Typeface.BOLD:
            case Typeface.ITALIC:
            case Typeface.BOLD_ITALIC:
                break;
            default:
                return false;
        }
        if (start > end) {
            return false;
        }
        //沒有選中狀態
        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                StyleSpan[] before = getEditableText().getSpans(start - 1, start, StyleSpan.class);
                StyleSpan[] after = getEditableText().getSpans(start, start + 1, StyleSpan.class);
                return before.length > 0 && after.length > 0 && before[0].getStyle() == style && after[0].getStyle() == style;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            // Make sure no duplicate characters be added
            for (int i = start; i < end; i++) {
                //逐個字符遍歷，如果有SPan格式的話spans size為1，否則為0
                StyleSpan[] spans = getEditableText().getSpans(i, i + 1, StyleSpan.class);
                Log.d(TAG,"contains --> i = "+i + " spans size : "+spans.length);
                for (StyleSpan span : spans) {
                    if (span.getStyle() == style) {
                        //進一步判斷此字符是否有SPan格式
                        builder.append(getEditableText().subSequence(i, i + 1).toString());
                        break;
                    }
                }
            }
            //選中全部字符都有SPan格式 才返回true。否則false
            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }

    /****下划线样式*****/
    public void underline(boolean valid) {
        if (valid) {
            underlineValid(getSelectionStart(), getSelectionEnd());
        } else {
            underlineInvalid(getSelectionStart(), getSelectionEnd());
        }
    }

    protected void underlineValid(int start, int end) {
        if (start >= end) {
            return;
        }
        getEditableText().setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    protected void underlineInvalid(int start, int end) {
        if (start >= end) {
            return;
        }
        UnderlineSpan[] spans = getEditableText().getSpans(start, end, UnderlineSpan.class);
        List<CustomEdittextPart> list = new ArrayList<>();

        for (UnderlineSpan span : spans) {
            list.add(new CustomEdittextPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
            getEditableText().removeSpan(span);
        }

        for (CustomEdittextPart part : list) {
            if (part.isValid()) {
                if (part.getStart() < start) {
                    underlineValid(part.getStart(), start);
                }
                if (part.getEnd() > end) {
                    underlineValid(end, part.getEnd());
                }
            }
        }
    }

    protected boolean containUnderline(int start, int end) {
        if (start > end) {
            return false;
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                UnderlineSpan[] before = getEditableText().getSpans(start - 1, start, UnderlineSpan.class);
                UnderlineSpan[] after = getEditableText().getSpans(start, start + 1, UnderlineSpan.class);
                return before.length > 0 && after.length > 0;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (getEditableText().getSpans(i, i + 1, UnderlineSpan.class).length > 0) {
                    builder.append(getEditableText().subSequence(i, i + 1).toString());
                }
            }

            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }

    /****字体颜色样式*****/
    public void fontColor(boolean valid, int color ) {
        if (valid) {
            fontColorValid(getSelectionStart(), getSelectionEnd(), color);
        } else {
            fontColorInvalid(getSelectionStart(), getSelectionEnd());
        }
    }

    protected void fontColorValid(int start, int end, int color) {
        if (start >= end) {
            return;
        }
        //先把原有的颜色移除
        ForegroundColorSpan[] spans = getEditableText().getSpans(start, end, ForegroundColorSpan.class);
        Log.d(TAG,"fontColorInvalid-->spans size : "+spans.length);
        for (ForegroundColorSpan span : spans) {
            getEditableText().removeSpan(span);
        }
        Log.d(TAG,"color = "+color);
        switch (color) {
            case FONT_BLACK:
                getEditableText().setSpan(new ForegroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case FONT_BLUE:
                getEditableText().setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case FONT_GREEN:
                getEditableText().setSpan(new ForegroundColorSpan(Color.GREEN), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case FONT_RED:
                getEditableText().setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case FONT_YELLOW:
                getEditableText().setSpan(new ForegroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case FONT_PURPLE:
                getEditableText().setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            default:
                break;
        }
    }

    protected void fontColorInvalid(int start, int end) {
        if (start >= end) {
            return;
        }
        ForegroundColorSpan[] spans = getEditableText().getSpans(start, end, ForegroundColorSpan.class);
        Log.d(TAG,"fontColorInvalid-->spans size : "+spans.length);
        List<CustomEdittextPart> list = new ArrayList<>();

        for (ForegroundColorSpan span : spans) {
            list.add(new CustomEdittextPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
            getEditableText().removeSpan(span);
        }
//        for (CustomEdittextPart part : list) {
//            if (part.isValid()) {
//                if (part.getStart() < start) {
//                    fontColorValid(part.getStart(), start);
//                }
//                if (part.getEnd() > end) {
//                    fontColorValid(end, part.getEnd());
//                }
//            }
//        }
    }

    protected boolean containFontColor(int start, int end) {
        if (start > end) {
            return false;
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                ForegroundColorSpan[] before = getEditableText().getSpans(start - 1, start, ForegroundColorSpan.class);
                ForegroundColorSpan[] after = getEditableText().getSpans(start, start + 1, ForegroundColorSpan.class);
                return before.length > 0 && after.length > 0;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (getEditableText().getSpans(i, i + 1, ForegroundColorSpan.class).length > 0) {
                    builder.append(getEditableText().subSequence(i, i + 1).toString());
                }
            }

            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }

    /****背景颜色样式*****/
    public void bgColor(boolean valid) {
        if (valid) {
            bgColorValid(getSelectionStart(), getSelectionEnd());
        } else {
            bgColorInvalid(getSelectionStart(), getSelectionEnd());
        }
    }

    protected void bgColorValid(int start, int end) {
        if (start >= end) {
            return;
        }
        getEditableText().setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    protected void bgColorInvalid(int start, int end) {
        if (start >= end) {
            return;
        }
        BackgroundColorSpan[] spans = getEditableText().getSpans(start, end, BackgroundColorSpan.class);
        List<CustomEdittextPart> list = new ArrayList<>();

        for (BackgroundColorSpan span : spans) {
            list.add(new CustomEdittextPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
            getEditableText().removeSpan(span);
        }

        for (CustomEdittextPart part : list) {
            if (part.isValid()) {
                if (part.getStart() < start) {
                    bgColorValid(part.getStart(), start);
                }
                if (part.getEnd() > end) {
                    bgColorValid(end, part.getEnd());
                }
            }
        }
    }

    protected boolean containbgColor(int start, int end) {
        if (start > end) {
            return false;
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                BackgroundColorSpan[] before = getEditableText().getSpans(start - 1, start, BackgroundColorSpan.class);
                BackgroundColorSpan[] after = getEditableText().getSpans(start, start + 1, BackgroundColorSpan.class);
                return before.length > 0 && after.length > 0;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (getEditableText().getSpans(i, i + 1, BackgroundColorSpan.class).length > 0) {
                    builder.append(getEditableText().subSequence(i, i + 1).toString());
                }
            }

            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }

    /****背景字体大小*****/
    public void textSize(boolean valid) {
        if (valid) {
            textSizeValid(getSelectionStart(), getSelectionEnd());
        } else {
            textSizeInvalid(getSelectionStart(), getSelectionEnd());
        }
    }

    protected void textSizeValid(int start, int end) {
        if (start >= end) {
            return;
        }
        getEditableText().setSpan(new AbsoluteSizeSpan(24, true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    protected void textSizeInvalid(int start, int end) {
        if (start >= end) {
            return;
        }
        AbsoluteSizeSpan[] spans = getEditableText().getSpans(start, end, AbsoluteSizeSpan.class);
        List<CustomEdittextPart> list = new ArrayList<>();

        for (AbsoluteSizeSpan span : spans) {
            list.add(new CustomEdittextPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
            getEditableText().removeSpan(span);
        }

        for (CustomEdittextPart part : list) {
            if (part.isValid()) {
                if (part.getStart() < start) {
                    textSizeValid(part.getStart(), start);
                }
                if (part.getEnd() > end) {
                    textSizeValid(end, part.getEnd());
                }
            }
        }
    }

    protected boolean containtextSize(int start, int end) {
        if (start > end) {
            return false;
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                AbsoluteSizeSpan[] before = getEditableText().getSpans(start - 1, start, AbsoluteSizeSpan.class);
                AbsoluteSizeSpan[] after = getEditableText().getSpans(start, start + 1, AbsoluteSizeSpan.class);
                return before.length > 0 && after.length > 0;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (getEditableText().getSpans(i, i + 1, AbsoluteSizeSpan.class).length > 0) {
                    builder.append(getEditableText().subSequence(i, i + 1).toString());
                }
            }
            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }
}

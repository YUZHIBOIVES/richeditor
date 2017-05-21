package com.invisible.richeditor;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.invisible.yricheditor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ives.yeung on 2016/11/10.
 */

@SuppressLint({"NewApi", "InflateParams"})
public class RichTextEditor extends ScrollView {

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

    private static final int EDIT_PADDING = 10; // edittext常规padding是10dp
    private static final int EDIT_FIRST_PADDING_TOP = 10; // 第一个EditText的paddingTop值

    private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    private LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private LayoutInflater inflater;
    private OnKeyListener keyListener; // 所有EditText的软键盘监听器
    private OnClickListener btnListener; // 图片右上角红叉按钮监听器
    private OnFocusChangeListener focusListener; // 所有EditText的焦点监听listener
    private EditText lastFocusEdit; // 最近被聚焦的EditText
    private LayoutTransition mTransitioner; // 只在图片View添加或remove时，触发transition动画
    private int editNormalPadding = 0; //
    private int disappearingImageIndex = 0;
    private Context mContext;


    public RichTextEditor(Context context) {
        this(context, null);
    }

    public RichTextEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        inflater = LayoutInflater.from(context);
        // 1. 初始化allLayout
        allLayout = new LinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        allLayout.setBackgroundColor(Color.WHITE);
        setupLayoutTransitions();
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        addView(allLayout, layoutParams);

        // 2. 初始化键盘退格监听
        // 主要用来处理点击回删按钮时，view的一些列合并操作
        keyListener = new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    EditText edit = (EditText) v;
                    onBackspacePress(edit);
                }
                return false;
            }
        };

        // 3. 图片叉掉处理
        btnListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                RelativeLayout parentView = (RelativeLayout) v.getParent();
                onImageCloseClick(parentView);
            }
        };

        focusListener = new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lastFocusEdit = (EditText) v;
                    ((MainActivity)mContext).setClickListenerForEditor();
                }
            }
        };

        LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        editNormalPadding = dip2px(EDIT_PADDING);
        EditText firstEdit = createEditText("input here",
                dip2px(EDIT_FIRST_PADDING_TOP));
        allLayout.addView(firstEdit, firstEditParam);
        lastFocusEdit = firstEdit;
//        lastFocusEdit.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                lastFocusEdit.requestFocus();
//            }
//        },1000);
    }

    /**
     * 处理软键盘backSpace回退事件
     *
     * @param editTxt 光标所在的文本输入框
     */
    private void onBackspacePress(EditText editTxt) {
        int startSelection = editTxt.getSelectionStart();
        // 只有在光标已经顶到文本输入框的最前方，在判定是否删除之前的图片，或两个View合并
        if (startSelection == 0) {
            int editIndex = allLayout.indexOfChild(editTxt);
            View preView = allLayout.getChildAt(editIndex - 1); // 如果editIndex-1<0,
            // 则返回的是null
            if (null != preView) {
                if (preView instanceof RelativeLayout) {
                    // 光标EditText的上一个view对应的是图片
                    onImageCloseClick(preView);
                } else if (preView instanceof EditText) {
                    // 光标EditText的上一个view对应的还是文本框EditText
//                    String str1 = editTxt.getText().toString();
//                    EditText preEdit = (EditText) preView;
//                    String str2 = preEdit.getText().toString();
                    Spannable str1 = editTxt.getText();
                    EditText preEdit = (EditText) preView;
                    Spannable str2 = preEdit.getText();

                    // 合并文本view时，不需要transition动画
                    allLayout.setLayoutTransition(null);
                    allLayout.removeView(editTxt);
                    allLayout.setLayoutTransition(mTransitioner); // 恢复transition动画

                    // 文本合并
//                    preEdit.setText(str2 + str1);
                    preEdit.setText(TextUtils.concat(str2, "\n", str1));
//                    preEdit.setText(str2);
//                    preEdit.append(str1);
                    preEdit.requestFocus();
                    preEdit.setSelection(str2.length(), str2.length());//设置光标位置
                    lastFocusEdit = preEdit;//最新聚焦的Edittext
                }
            }
        }
    }

    /**
     * 处理图片叉掉的点击事件
     *
     * @param view 整个image对应的relativeLayout view
     * @type 删除类型 0代表backspace删除 1代表按红叉按钮删除
     */
    private void onImageCloseClick(View view) {
        if (!mTransitioner.isRunning()) {
            disappearingImageIndex = allLayout.indexOfChild(view);//保存删除图片位置
            allLayout.removeView(view);
        }
    }

    /**
     * 生成文本输入框
     */
    private EditText createEditText(String hint, int paddingTop) {
        EditText editText = (EditText) inflater.inflate(R.layout.edit_item,
                null);
        editText.setOnKeyListener(keyListener);
        editText.setTag(viewTagIndex++);
        editText.setPadding(editNormalPadding, paddingTop, editNormalPadding, 0);
        editText.setHint(hint);
        editText.setOnFocusChangeListener(focusListener);
        return editText;
    }

    /**
     * 生成图片View
     */
    private RelativeLayout createImageLayout() {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.edit_imageview, null);
        layout.setTag(viewTagIndex++);
        View closeView = layout.findViewById(R.id.image_close);
        closeView.setTag(layout.getTag());
        closeView.setOnClickListener(btnListener);
        return layout;
    }

    /**
     * 根据绝对路径添加view
     *
     * @param imagePath
     */
    public void insertImage(String imagePath) {
        Bitmap bmp = getScaledBitmap(imagePath, getWidth());
        insertImage(bmp, imagePath);
    }

    /**
     * 插入多张图片
     */
    private void insertImage(Bitmap bitmap, String imagePath) {
//        String lastEditStr = lastFocusEdit.getText().toString();
        Spannable lastEditStr = lastFocusEdit.getText();//注意此处获取的是Spannable
        int cursorIndex = lastFocusEdit.getSelectionStart();
//        final String editStr1 = lastEditStr.substring(0, cursorIndex).trim();
        final Spannable editStr1 = (Spannable)lastEditStr.subSequence(0, cursorIndex);
        //{ @截開后需要從新設置一個新的Style實例，不然合併時回Style消失
        checkWhatStyleShouldApply(editStr1);
        //}
        int lastEditIndex = allLayout.indexOfChild(lastFocusEdit);
        if (lastEditStr.length() == 0 || editStr1.length() == 0) {
            // 如果EditText为空，或者光标已经顶在了editText的最前面，则直接插入图片，并且EditText下移即可
            addImageViewAtIndex(lastEditIndex, bitmap, imagePath);
//            allLayout.clearFocus();
            lastFocusEdit.requestFocus();
            lastFocusEdit.setSelection(editStr1.length(), editStr1.length());
//            lastFocusEdit.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
// .requestFocus();
//                    lastFocusEdit.setSelection(editStr1.length(), editStr1.length());
//                }
//            },600);
        } else {
            // 如果EditText非空且光标不在最顶端，则需要添加新的imageView和EditText
            lastFocusEdit.setText(editStr1);
//            String editStr2 = lastEditStr.substring(cursorIndex).trim();
            Spannable editStr2 = (Spannable)lastEditStr.subSequence(cursorIndex,lastEditStr.length());
            //判断是否需要在最底部加一行
            if (allLayout.getChildCount() - 1 == lastEditIndex
                    || editStr2.length() > 0) {
                addEditTextAtIndex(lastEditIndex + 1, editStr2);
            }

            addImageViewAtIndex(lastEditIndex + 1, bitmap, imagePath);
            lastFocusEdit.requestFocus();
            lastFocusEdit.setSelection(0, 0);//此处光标位置
//            lastFocusEdit.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    lastFocusEdit.requestFocus();
//                    lastFocusEdit.setSelection(editStr1.length(), editStr1.length());
//                }
//            },500);
        }
//        hideKeyBoard();
    }

    /**
     * 隐藏小键盘
     */
    public void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(lastFocusEdit.getWindowToken(), 0);
    }

    /**
     * 在特定位置插入EditText
     *
     * @param index   位置
     * @param editStr EditText显示的文字
     */
    private void addEditTextAtIndex(final int index, Spannable editStr) {
        EditText editText2 = createEditText("", getResources()
                .getDimensionPixelSize(R.dimen.edit_padding_top));
        editText2.setText(editStr);

        // 请注意此处，EditText添加、或删除不触动Transition动画
        allLayout.setLayoutTransition(null);
        allLayout.addView(editText2, index);
        allLayout.setLayoutTransition(mTransitioner); // remove之后恢复transition动画
        editText2.requestFocus();//注意此处新添加的Edittext需要获取焦点,此时的lastFocusEdit已经改变
    }

    /**
     * 在特定位置添加ImageView
     */
    private void addImageViewAtIndex(final int index, Bitmap bmp,
                                     String imagePath) {
        final RelativeLayout imageLayout = createImageLayout();
        DataImageView imageView = (DataImageView) imageLayout
                .findViewById(R.id.edit_imageView);
        imageView.setImageBitmap(bmp);
        imageView.setBitmap(bmp);
        imageView.setAbsolutePath(imagePath);

        // 调整imageView的高度
        int imageHeight = getWidth() * bmp.getHeight() / bmp.getWidth();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, imageHeight);
        imageView.setLayoutParams(lp);

        // onActivityResult无法触发动画，此处post处理
//        allLayout.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                allLayout.addView(imageLayout, index);
////                lastFocusEdit.requestFocus();
////                lastFocusEdit.setSelection(0,0);
//            }
//        }, 200);
        allLayout.addView(imageLayout, index);
    }

    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width view的宽度
     */
    private Bitmap getScaledBitmap(String filePath, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int sampleSize = options.outWidth > width ? options.outWidth / width
                + 1 : 1;
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 初始化transition动画
     */
    private void setupLayoutTransitions() {
        mTransitioner = new LayoutTransition();
        allLayout.setLayoutTransition(mTransitioner);
        mTransitioner.addTransitionListener(new LayoutTransition.TransitionListener() {

            @Override
            public void startTransition(LayoutTransition transition,
                                        ViewGroup container, View view, int transitionType) {

            }

            @Override
            public void endTransition(LayoutTransition transition,
                                      ViewGroup container, View view, int transitionType) {
//                lastFocusEdit.requestFocus();
//                lastFocusEdit.setSelection(1,1);
                if (!transition.isRunning()
                        && transitionType == LayoutTransition.CHANGE_DISAPPEARING) {
                    // transition动画结束，合并EditText
                    mergeEditText();
                }
            }
        });
        mTransitioner.setDuration(300);
    }

    /**
     * 图片删除的时候，如果上下方都是EditText，则合并处理
     */
    private void mergeEditText() {
        View preView = allLayout.getChildAt(disappearingImageIndex - 1);
        View nextView = allLayout.getChildAt(disappearingImageIndex);
        if (preView != null && preView instanceof EditText && null != nextView
                && nextView instanceof EditText) {
            EditText preEdit = (EditText) preView;
            EditText nextEdit = (EditText) nextView;
//            String str1 = preEdit.getText().toString();
//            String str2 = nextEdit.getText().toString();
            Spannable str1 = preEdit.getText();
            Spannable str2 = nextEdit.getText();
//            String mergeText = "";
            Spannable mergeText = null;
            if (str2.length() > 0) {
//                mergeText = str1 + "\n" + str2;
                preEdit.setText(TextUtils.concat(str1,"\n",str2));
//                preEdit.setText(str1);
////                preEdit.append("\n");
//                preEdit.append(str2);
            } else {
//                mergeText = str1;
                preEdit.setText(str1);
            }

            allLayout.setLayoutTransition(null);
            allLayout.removeView(nextEdit);
//            preEdit.setText(mergeText);
            preEdit.requestFocus();
            preEdit.setSelection(str1.length(), str1.length());
            allLayout.setLayoutTransition(mTransitioner);
        }
    }

    /**
     * dp和pixel转换
     *
     * @param dipValue dp值
     * @return 像素值
     */
    public int dip2px(float dipValue) {
        float m = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    /**
     * 对外提供的接口, 生成编辑数据上传
     */
    public List<EditData> buildEditData() {
        List<EditData> dataList = new ArrayList<EditData>();
        int num = allLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            View itemView = allLayout.getChildAt(index);
            EditData itemData = new EditData();
            if (itemView instanceof EditText) {
                EditText item = (EditText) itemView;
                itemData.inputStr = item.getText().toString();
            } else if (itemView instanceof RelativeLayout) {
                DataImageView item = (DataImageView) itemView
                        .findViewById(R.id.edit_imageView);
                itemData.imagePath = item.getAbsolutePath();
                itemData.bitmap = item.getBitmap();
            }
            dataList.add(itemData);
        }

        return dataList;
    }

    class EditData {
        String inputStr;
        String imagePath;
        Bitmap bitmap;
    }

    public EditText getEditor(){
        return lastFocusEdit;
    }

    private void checkWhatStyleShouldApply(Spannable sp) {
        if (getStyleString(0,sp.length(),sp, Typeface.BOLD).length() == sp.length()) {
            sp.setSpan(new StyleSpan(Typeface.BOLD), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (getStyleString(0,sp.length(),sp,Typeface.ITALIC).length() == sp.length()) {
            sp.setSpan(new StyleSpan(Typeface.ITALIC), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (getBgColorString(0, sp.length(), sp).length() == sp.length()) {
            sp.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (getBgColorString(0, sp.length(), sp).length() == sp.length()) {
            sp.setSpan(new ForegroundColorSpan(Color.GREEN), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
//        sp.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        sp.setSpan(new ForegroundColorSpan(Color.GREEN), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private String getStyleString(int start,int end,Spannable sp,int style) {
        StringBuilder builder = new StringBuilder();

        // Make sure no duplicate characters be added
        for (int i = start; i < end; i++) {
            //逐個字符遍歷，如果有SPan格式的話spans size為1，否則為0
            StyleSpan[] spans = sp.getSpans(i, i + 1, StyleSpan.class);
            for (StyleSpan span : spans) {
                if (span.getStyle() == style) {
                    //進一步判斷此字符是否有SPan格式
                    builder.append(sp.subSequence(i, i + 1).toString());
                    break;
                }
            }
        }
        return builder.toString();
    }

    private String getBgColorString(int start,int end,Spannable sp) {
        StringBuilder builder = new StringBuilder();

        for (int i = start; i < end; i++) {
            if (sp.getSpans(i, i + 1, BackgroundColorSpan.class).length > 0) {
                builder.append(sp.subSequence(i, i + 1).toString());
            }
        }
        return builder.toString();
    }

    private String getTextColorString(int start,int end,Spannable sp) {
        StringBuilder builder = new StringBuilder();

        for (int i = start; i < end; i++) {
            if (sp.getSpans(i, i + 1, ForegroundColorSpan.class).length > 0) {
                builder.append(sp.subSequence(i, i + 1).toString());
            }
        }
        return builder.toString();
    }

    public EditText getLastFocusEditText() {
        return lastFocusEdit;
    }
}


package com.invisible.richeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.invisible.yricheditor.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ives.yeung on 2016/11/10.
 */

@SuppressLint("SimpleDateFormat")
public class MainActivity extends FragmentActivity implements View.OnClickListener,
        View.OnFocusChangeListener{
    private static final int REQUEST_CODE_PICK_IMAGE = 1023;
    private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1022;
    private static final int ALLHIDE = -1;
    private static final int FONTCOLOR = 1;
    private static final int PARAGRAPH = 2;
    private static final int BGCOLOR = 3;
    private InputMethodManager imm;
    private Handler mKeyboardHandler;
    private RichTextEditor editor;
    private ImageButton mInsertImgBtn;
    private ImageButton mOpenCameraBtn;
    private ImageButton mBoldBtn;
    private ImageButton mItalicBtn;
    private ImageButton mUnderLineBtn;
    private ImageButton mFontColorBtn;
    private ImageButton mBgColorBtn;
    private ImageButton mTextSizeBtn;
    private ImageButton mBlack;
    private ImageButton mBlue;
    private ImageButton mGreen;
    private ImageButton mRed;
    private ImageButton mYellow;
    private ImageButton mPurple;
    private LinearLayout mFontColorLayout;
    private RelativeLayout mMainView;

    private static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    private File mCurrentPhotoFile;// 照相机拍照得到的图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mKeyboardHandler = new Handler();
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        initViews();
    }

    private void initViews(){
        mMainView = (RelativeLayout) findViewById(R.id.main_view);
        editor = (RichTextEditor) findViewById(R.id.richEditor);
        mInsertImgBtn = (ImageButton)findViewById(R.id.button1);
        mOpenCameraBtn = (ImageButton)findViewById(R.id.button2);
        mBoldBtn = (ImageButton)findViewById(R.id.button3);
        mItalicBtn = (ImageButton)findViewById(R.id.button4);
        mUnderLineBtn = (ImageButton)findViewById(R.id.button5);
        mFontColorBtn = (ImageButton)findViewById(R.id.button6);
        mBgColorBtn = (ImageButton)findViewById(R.id.button7);
        mTextSizeBtn = (ImageButton) findViewById(R.id.button8);
        mFontColorLayout = (LinearLayout) findViewById(R.id.font_color_layout);
        mBlack = (ImageButton) findViewById(R.id.color_black);
        mGreen = (ImageButton) findViewById(R.id.color_green);
        mBlue = (ImageButton) findViewById(R.id.color_blue);
        mYellow = (ImageButton) findViewById(R.id.color_yellow);
        mPurple = (ImageButton) findViewById(R.id.color_purple);
        mRed = (ImageButton) findViewById(R.id.color_red);
        mInsertImgBtn.setOnClickListener(this);
        mOpenCameraBtn.setOnClickListener(this);
        mBoldBtn.setOnClickListener(this);
        mItalicBtn.setOnClickListener(this);
        mUnderLineBtn.setOnClickListener(this);
        mFontColorBtn.setOnClickListener(this);
        mBgColorBtn.setOnClickListener(this);
        mTextSizeBtn.setOnClickListener(this);
        mFontColorLayout.setOnClickListener(this);
        mBlack.setOnClickListener(this);
        mBlue.setOnClickListener(this);
        mRed.setOnClickListener(this);
        mYellow.setOnClickListener(this);
        mGreen.setOnClickListener(this);
        mPurple.setOnClickListener(this);
        editor.getLastFocusEditText().setOnClickListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
//        if (view instanceof EditText) {
//            hideFontView();
//        }
    }

    @Override
    public void onClick(View v) {
        editor.hideKeyBoard();
        if (v.getId() == mInsertImgBtn.getId()) {
            // 打开系统相册
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");// 相片类型
            intent.putExtra("gallery-multi-select", true);
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        } else if (v instanceof EditText) {
            showOrHideIMM(ALLHIDE);
        }else if (v.getId() == mOpenCameraBtn.getId()) {
            // 打开相机
            openCamera();
        } else if (v.getId() == mBoldBtn.getId()) {
            // 设置粗体
            if (editor.getEditor() != null) {
                ((DeletableEditText)editor.getEditor()).bold(!((DeletableEditText) editor.getEditor()).contains(DeletableEditText.FORMAT_BOLD));
            }
        } else if (v.getId() == mItalicBtn.getId()) {
            // 设置斜体
            if (editor.getEditor() != null) {
                ((DeletableEditText)editor.getEditor()).italic(!((DeletableEditText) editor.getEditor()).contains(DeletableEditText.FORMAT_ITALIC));
            }
        } else if (v.getId() == mUnderLineBtn.getId()) {
            //设置下划线
            if (editor.getEditor() != null) {
                ((DeletableEditText)editor.getEditor()).underline(!((DeletableEditText) editor.getEditor()).contains(DeletableEditText.FORMAT_UNDERLINED));
            }
        } else if (v.getId() == mFontColorBtn.getId()) {
            //设置字体颜色
            if (editor.getEditor() != null) {
//                ((DeletableEditText)editor.getEditor()).fontColor(!((DeletableEditText) editor.getEditor()).contains(DeletableEditText.FORMAT_FONTCOLOR));
                showOrHideIMM(FONTCOLOR);
            }
        } else if (v.getId() == mBgColorBtn.getId()) {
            //设置字体背景颜色
            if (editor.getEditor() != null) {
                ((DeletableEditText)editor.getEditor()).bgColor(!((DeletableEditText) editor.getEditor()).contains(DeletableEditText.FORMAT_BGCOLOR));
            }
        } else if (v.getId() == mTextSizeBtn.getId()) {
            //设置字体大小
            if (editor.getEditor() != null) {
                ((DeletableEditText)editor.getEditor()).textSize(!((DeletableEditText) editor.getEditor()).contains(DeletableEditText.FORMAT_TEXTSIZE));
            }
        } else if (v.getId() == mBlack.getId()) {
            ((DeletableEditText)editor.getEditor()).fontColor(true,DeletableEditText.FONT_BLACK);
        } else if (v.getId() == mGreen.getId()) {
            ((DeletableEditText)editor.getEditor()).fontColor(true,DeletableEditText.FONT_GREEN);
        } else if (v.getId() == mRed.getId()) {
            ((DeletableEditText)editor.getEditor()).fontColor(true,DeletableEditText.FONT_RED);
        } else if (v.getId() == mBlue.getId()) {
            ((DeletableEditText)editor.getEditor()).fontColor(true,DeletableEditText.FONT_BLUE);
        } else if (v.getId() == mYellow.getId()) {
            ((DeletableEditText)editor.getEditor()).fontColor(true,DeletableEditText.FONT_YELLOW);
        } else if (v.getId() == mPurple.getId()) {
            ((DeletableEditText)editor.getEditor()).fontColor(true,DeletableEditText.FONT_PURPLE);
        }

    }

    private void showOrHideIMM(final int type) {
        if (mFontColorBtn.getTag() == null && mBgColorBtn.getTag() == null && type != ALLHIDE) {
            // 隐藏软键盘
            imm.hideSoftInputFromWindow(editor.getEditor().getWindowToken(), 0);
            try {
                mKeyboardHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (type == FONTCOLOR) {
                            showFontView();
                        } else if (type == PARAGRAPH) {
//                            showParagraphView();
                        } else if (type == BGCOLOR) {

                        }
                    }
                }, 200);
            } catch (Exception e) {
            }
            // 显示對應View
        } else if (mFontColorBtn.getTag() == null && mBgColorBtn.getTag() == "1" && type == FONTCOLOR) {
//            hideParagraphView();
            showFontView();
        } else if (mFontColorBtn.getTag() == "1" && mBgColorBtn.getTag() == null && type == BGCOLOR) {
            hideFontView();
//            showParagraphView();
        } else {
            // 显示软键盘
            hideFontView();
//            hideParagraphView();
            imm.showSoftInput(editor.getEditor(), 0);
        }
    }

    private void showFontView() {
        mFontColorBtn.setTag("1");
        mFontColorLayout.setVisibility(View.VISIBLE);
    }

    private void hideFontView() {
        mFontColorBtn.setTag(null);
        mFontColorLayout.setVisibility(View.GONE);
    }

    /**
     * 负责处理编辑数据提交等事宜，请自行实现
     */
    protected void dealEditData(List<RichTextEditor.EditData> editList) {
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
            } else if (itemData.imagePath != null) {
            }

        }
    }

    protected void openCamera() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();// 创建照片的存储目录
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
            final Intent intent = getTakePickIntent(mCurrentPhotoFile);
            startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
        } catch (ActivityNotFoundException e) {
        }
    }

    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    /**
     * 用当前时间给取得的图片命名
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date) + ".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
//			Uri uri = data.getData();
//			insertBitmap(getRealFilePath(uri));
            //////////
            ArrayList<String> imagesGot = new ArrayList<>();
            ArrayList<Parcelable> uriList = new ArrayList<>();
            if (data.getParcelableArrayListExtra("fileList") != null) {
                uriList.addAll(data.getParcelableArrayListExtra("fileList"));
            } else {
                uriList.add(data.getData());
            }
            for (Parcelable parcelable : uriList) {
                imagesGot.add(getImagePath((Uri) parcelable, this));
            }
            List<String> images = new ArrayList<>();
            for (int i = 0; i < imagesGot.size(); i++) {
                String path = imagesGot.get(i);
                File file = new File(path);
                if (file.exists()) {
//                    images.add(path);
                    //插入图片images
                    insertBitmap(path);
                } else {
                    break;
                }
            }
        } else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
            insertBitmap(mCurrentPhotoFile.getAbsolutePath());
        }
    }

    /*获取图片路径*/
    public static String getImagePath(Uri uri, Activity context) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String ImagePath = cursor.getString(columIndex);
            cursor.close();
            return ImagePath;
        }
        return uri.toString();
    }

    /**
     * 添加图片到富文本剪辑器
     *
     * @param imagePath
     */
    private void insertBitmap(String imagePath) {
        editor.insertImage(imagePath);
    }

    /**
     * 根据Uri获取图片文件的绝对路径
     */
    public String getRealFilePath(final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public void setClickListenerForEditor() {
        editor.getLastFocusEditText().setOnClickListener(this);
    }
}

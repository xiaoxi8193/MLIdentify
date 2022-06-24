package com.ys.identify.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.mlplugin.card.gcr.MLGcrCapture;
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureConfig;
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureFactory;
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureResult;
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureUIConfig;
import com.ys.identify.R;
import com.ys.identify.dialog.AddPictureDialog;
import com.ys.identify.entity.GeneralCardResult;
import com.ys.identify.processor.gcr.GeneralCardProcessor;
import com.ys.identify.processor.gcr.officercard.OfficerCardProcessor;

import java.io.IOException;

public class GeneralCardRecognitionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GeneralCardRecognitionActivity";

    private Uri mImageUri;

    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final int REQUEST_CODE = 10;

    private static final int REQUEST_IMAGE_SELECT_FROM_ALBUM = 1000;

    private static final int REQUEST_IMAGE_CAPTURE = 1001;

    private Object object = false;

    private ImageView frontImg;
    private ImageView frontSimpleImg;
    private ImageView frontDeleteImg;
    private LinearLayout frontAddView;
    private TextView showResult;

    private CardType cardTypeEnum = CardType.OFFICERCARD;

    private Bitmap imageBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_card_recognition);
        initComponent();
    }

    private void initComponent() {
        frontImg = findViewById(R.id.avatar_img);
        frontSimpleImg = findViewById(R.id.avatar_sample_img);
        frontDeleteImg = findViewById(R.id.avatar_delete);
        frontAddView = findViewById(R.id.avatar_add);
        showResult = findViewById(R.id.show_result);

        frontAddView.setOnClickListener(this);
        frontDeleteImg.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.avatar_add:
                showChoosePicDialog();
                break;
            case R.id.avatar_delete:
                showFrontDeleteImage();
                break;
            case R.id.back:
                finish();
                break;
            default:
                break;
        }
    }

    public enum CardType {
        OFFICERCARD
    }

    private void showChoosePicDialog() {
        AddPictureDialog addPictureDialog = new AddPictureDialog(this,AddPictureDialog.TYPE_CUSTOM);
        addPictureDialog.setClickListener(new AddPictureDialog.ClickListener() {
            @Override
            public void takePicture() {
                if (!isGranted(Manifest.permission.CAMERA)) {
                    requestPermission(PERMISSIONS, REQUEST_CODE);
                    return;
                } else {
                    detectPhoto(null, callback);
                }
            }

            @Override
            public void selectImage() {
                startChooseImageIntentForResult();
            }

            @Override
            public void doExtend() {
                if (!isGranted(Manifest.permission.CAMERA)) {
                    requestPermission(PERMISSIONS, REQUEST_CODE);
                    return;
                } else {
                    detectPreview(null, callback);
                }
            }
        });
        addPictureDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i(TAG, "onActivityResult requestCode " + requestCode + ", resultCode " + resultCode);
        if (requestCode == REQUEST_IMAGE_SELECT_FROM_ALBUM && resultCode == RESULT_OK) {
            if (intent != null) {
                mImageUri = intent.getData();
            }
            tryReloadAndDetectInImage();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            tryReloadAndDetectInImage();
        }
    }

    // take a picture
    private void detectPhoto(Object object, MLGcrCapture.Callback callback) {
        // 创建通用卡证识别配置器。可设置通用卡证识别的语种。
        MLGcrCaptureConfig cardConfig = new MLGcrCaptureConfig.Factory().setLanguage("zh").create();
        // 创建通用卡证识别界面配置器。
        MLGcrCaptureUIConfig uiConfig = new MLGcrCaptureUIConfig.Factory()
                // 设置扫描框颜色。
                .setScanBoxCornerColor(Color.BLUE)
                // 设置扫描框中的提示文字，建议少于30个字符。
                .setTipText(getResources().getString(R.string.capture_tip))
                .setOrientation(MLGcrCaptureUIConfig.ORIENTATION_AUTO).create();
        // 方式一：根据自定义的卡证识别界面配置器，创建通用卡证识别处理器。
        MLGcrCapture ocrManager = MLGcrCaptureFactory.getInstance().getGcrCapture(cardConfig, uiConfig);
        // 方式二：使用默认界面，创建通用卡证识别处理器。
        //MLGcrCapture ocrManager = MLGcrCaptureFactory.getInstance().getGcrCapture(cardConfig);
        // 绑定通用卡证识别处理器和处理结果回调函数。
        ocrManager.capturePhoto(this, object, callback);
    }

    // video stream
    private void detectPreview(Object object, MLGcrCapture.Callback callback) {
        MLGcrCaptureConfig cardConfig = new MLGcrCaptureConfig.Factory().setLanguage("zh").create();
        MLGcrCaptureUIConfig uiConfig = new MLGcrCaptureUIConfig.Factory()
                .setTipText(getResources().getString(R.string.vedio_tip))
                .setOrientation(MLGcrCaptureUIConfig.ORIENTATION_AUTO).create();
        MLGcrCapture ocrManager = MLGcrCaptureFactory.getInstance().getGcrCapture(cardConfig, uiConfig);
        ocrManager.capturePreview(this, object, callback);
    }

    // local image
    private void detectLocalImage(Bitmap bitmap, Object object, MLGcrCapture.Callback callback) {
        // 创建通用卡证识别配置器。可设置通用卡证识别的语种
        MLGcrCaptureConfig config = new MLGcrCaptureConfig.Factory().setLanguage("zh").create();
        MLGcrCapture ocrManager = MLGcrCaptureFactory.getInstance().getGcrCapture(config);
        // bitmap 为需要识别的Bitmap类型卡证图像，支持的图片格式包括：jpg/jpeg/png/bmp
        ocrManager.captureImage(bitmap, object, callback);
    }

    //创建识别结果回调函数，重载onResult、onCanceled、onFailure、onDenied四个方法。
    // onResult表示返回了结果，MLGcrCaptureResult为卡证识别返回的结果，onCanceled表示用户取消，
    // onFailure表示识别失败，onDenied表示相机不可用等场景。
    private MLGcrCapture.Callback callback = new MLGcrCapture.Callback() {
        // 此方法需要返回状态码：
        // MLGcrCaptureResult.CAPTURE_CONTINUE：表示返回的结果不满足要求（无结果或返回结果有误等），在视频流或拍照模式下，会继续识别。
        // MLGcrCaptureResult.CAPTURE_STOP：表示返回的结果满足要求，停止识别。
        @Override
        public int onResult(MLGcrCaptureResult result, Object object) {
            Log.i(TAG, "callback onRecSuccess");
            // 识别结果处理，开发者需要根据应用场景，实现自己的后处理逻辑，提取有效信息，返回对应的状态码。
            if (result == null) {
                Log.e(TAG, "callback onRecSuccess result is null");
                return MLGcrCaptureResult.CAPTURE_CONTINUE;// 不满足要求，继续识别
            }

            // 满足要求的结果进行处理。
            GeneralCardProcessor idCard = null;
            GeneralCardResult cardResult = null;

            if (cardTypeEnum == CardType.OFFICERCARD) {
                 idCard = new OfficerCardProcessor(result.text);
            }

            if (idCard != null) {
                cardResult = idCard.getResult();
            }

            showFrontImage(result.cardBitmap);
            displayResult(cardResult);

            // If the results don't match
            if (cardResult == null || cardResult.name.isEmpty()
//                    || cardResult.gender.isEmpty()
//                    || cardResult.department.isEmpty() || cardResult.job.isEmpty()
//                    || cardResult.rank.isEmpty() || cardResult.type.isEmpty()
//                    || cardResult.number.isEmpty() || cardResult.useDate.isEmpty()
            ) {
                return MLGcrCaptureResult.CAPTURE_CONTINUE;// 不满足要求，继续识别
            }

            displayResult(cardResult);
            return MLGcrCaptureResult.CAPTURE_STOP;// 处理结束，退出识别
        }

        // 用户取消处理。
        @Override
        public void onCanceled() {
            Log.i(TAG, "callback onRecCanceled");
        }

        // 识别不到任何文字信息或识别过程发生系统异常的回调方法。
        // retCode：错误码。
        // bitmap：检测失败的卡证图片。
        @Override
        public void onFailure(int retCode, Bitmap bitmap) {
            // 识别异常处理

        }

        // 相机不支持等场景处理
        @Override
        public void onDenied() {
            Log.i(TAG, "callback onCameraDenied");
        }
    };

    private void displayResult(GeneralCardResult result) {
        if (result == null) {
            return;
        }
        if (showResult.getText().length() != 0) {
            showResult.setText("");
        }
        StringBuilder builder = new StringBuilder();

        builder.append("name: ");
        builder.append(result.name);
        builder.append(System.lineSeparator());

        builder.append("gender: ");
        builder.append(result.gender);
        builder.append(System.lineSeparator());

        builder.append("department: ");
        builder.append(result.department);
        builder.append(System.lineSeparator());

        builder.append("job: ");
        builder.append(result.job);
        builder.append(System.lineSeparator());

        builder.append("rank: ");
        builder.append(result.rank);
        builder.append(System.lineSeparator());

        builder.append("type: ");
        builder.append(result.type);
        builder.append(System.lineSeparator());

        builder.append("number: ");
        builder.append(result.number);
        builder.append(System.lineSeparator());

        builder.append("useDate: ");
        builder.append(result.useDate);

        showResult.setText(builder.toString());
    }

    private void showFrontImage(Bitmap bitmap) {
        frontImg.setVisibility(View.VISIBLE);
        frontImg.setImageBitmap(bitmap);
        frontSimpleImg.setVisibility(View.GONE);
        frontAddView.setVisibility(View.GONE);
        frontDeleteImg.setVisibility(View.VISIBLE);
    }

    private void showFrontDeleteImage() {
        frontImg.setVisibility(View.GONE);
        frontSimpleImg.setVisibility(View.VISIBLE);
        frontAddView.setVisibility(View.VISIBLE);
        frontDeleteImg.setVisibility(View.GONE);
    }

    private void startChooseImageIntentForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_IMAGE_SELECT_FROM_ALBUM);
    }

    private void tryReloadAndDetectInImage() {
        if (mImageUri == null) {
            return;
        }
        Bitmap mTryImageBitmap = null;
        try {
            mTryImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
        } catch (IOException error) {
            Log.e(TAG, "Failed to get bitmap from uri: " + error.getMessage());
        }
        detectLocalImage(mTryImageBitmap, null, callback);
    }

    private boolean isGranted(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            int checkSelfPermission = this.checkSelfPermission(permission);
            return checkSelfPermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    private boolean requestPermission(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (!this.isGranted(permissions[0])) {
            this.requestPermissions(permissions, requestCode);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageBitmap != null && !imageBitmap.isRecycled()) {
            imageBitmap.recycle();
            imageBitmap = null;
        }
    }
}

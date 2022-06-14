package com.ys.identify;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.mlplugin.card.icr.cn.MLCnIcrCapture;
import com.huawei.hms.mlplugin.card.icr.cn.MLCnIcrCaptureConfig;
import com.huawei.hms.mlplugin.card.icr.cn.MLCnIcrCaptureFactory;
import com.huawei.hms.mlplugin.card.icr.cn.MLCnIcrCaptureResult;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "IDCardRecognition";

    private boolean lastType = false; // false: front， true：back.

    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final int REQUEST_CODE = 10;
    private static final int INT_REQUEST_CODE = 20;

    private ImageView frontImg;
    private ImageView backImg;
    private ImageView frontSimpleImg;
    private ImageView backSimpleImg;
    private ImageView frontDeleteImg;
    private ImageView backDeleteImg;
    private LinearLayout frontAddView;
    private LinearLayout backAddView;
    private TextView showResult;
    private String lastFrontResult = "";
    private String lastBackResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        initComponent();
    }

    private void initComponent() {
        this.frontImg = this.findViewById(R.id.avatar_img);
        this.backImg = this.findViewById(R.id.emblem_img);
        this.frontSimpleImg = this.findViewById(R.id.avatar_sample_img);
        this.backSimpleImg = this.findViewById(R.id.emblem_sample_img);
        this.frontDeleteImg = this.findViewById(R.id.avatar_delete);
        this.backDeleteImg = this.findViewById(R.id.emblem_delete);
        this.frontAddView = this.findViewById(R.id.avatar_add);
        this.backAddView = this.findViewById(R.id.emblem_add);
        this.showResult = this.findViewById(R.id.show_result);
        this.frontAddView.setOnClickListener(this);
        this.backAddView.setOnClickListener(this);
        this.frontDeleteImg.setOnClickListener(this);
        this.backDeleteImg.setOnClickListener(this);
        this.findViewById(R.id.back).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.avatar_add:
                Log.i(TAG, "onClick avatar_img");
                // 设置识别身份证的正反面。
                // true：正面。
                // false：反面。
                this.lastType = true;
                if (!this.isGranted(Manifest.permission.CAMERA)) {
                    this.requestPermission(PERMISSIONS, REQUEST_CODE);
                    return;
                } else {
                    this.startCaptureActivity(idCallback, this.lastType);
                }
                break;
            case R.id.emblem_add:
                Log.i(TAG, "onClick emblem_img");
                this.lastType = false;
                if (!this.isGranted(Manifest.permission.CAMERA)) {
                    this.requestPermission(PERMISSIONS, REQUEST_CODE);
                    return;
                } else {
                    this.startCaptureActivity(this.idCallback, this.lastType);
                }
                break;
            case R.id.avatar_delete:
                Log.i(TAG, "onClick avatar_delete");
                this.showFrontDeleteImage();
                this.lastFrontResult = "";
                break;
            case R.id.emblem_delete:
                Log.i(TAG, "onClick emblem_delete");
                this.showBackDeleteImage();
                this.lastBackResult = "";
                break;
            case R.id.back:
                //this.finish();
                break;
            default:
                break;
        }
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

    //设置识别参数，调用识别器capture/captureImage接口进行识别，识别结果会通过1中的回调函数返回。
    private void startCaptureActivity(MLCnIcrCapture.CallBack callBack, boolean isFront) {
        Log.i(TAG, "startCaptureActivity");
        MLCnIcrCaptureConfig config =
                new MLCnIcrCaptureConfig.Factory().setFront(isFront).create();
        MLCnIcrCapture icrCapture = MLCnIcrCaptureFactory.getInstance().getIcrCapture(config);

        icrCapture.capture(callBack, this);
    }

    private MLCnIcrCapture.CallBack idCallback = new MLCnIcrCapture.CallBack() {
        @Override
        public void onSuccess(MLCnIcrCaptureResult idCardResult){
            // 识别成功处理。
            Log.i(TAG, "IdCallBack onRecSuccess");
            if (idCardResult == null) {
                Log.i(TAG, "IdCallBack onRecSuccess idCardResult is null");
                return;
            }
            Bitmap bitmap = idCardResult.cardBitmap;
            if (lastType){
                Log.i(TAG,"FRONT");
                showFrontImage(bitmap);
                lastFrontResult = formatIdCardResult(idCardResult,true);
            } else {
                Log.i(TAG,"BACK");
                showBackImage(bitmap);
                lastBackResult = formatIdCardResult(idCardResult,false);
            }
            showResult.setText(lastFrontResult);
            showResult.append(lastBackResult);
        }
        @Override
        public void onCanceled(){
            // 用户取消处理。
            Log.i(TAG, "IdCallBack onRecCanceled");
        }

        // 识别不到任何文字信息或识别过程发生系统异常的回调方法。
        // retCode：错误码。
        // bitmap：检测失败的身份证图片。
        @Override
        public void onFailure(int retCode, Bitmap bitmap){
            // 识别异常处理。
            Toast.makeText(getApplicationContext(), R.string.get_data_failed, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "IdCallBack onRecFailed: " + retCode);
        }
        @Override
        public void onDenied(){
            // 相机不支持等场景处理。
            Log.i(TAG, "IdCallBack onCameraDenied");
        }
    };

    private String formatIdCardResult(MLCnIcrCaptureResult result, boolean isFront) {
        Log.i(TAG, "formatIdCardResult");
        StringBuilder resultBuilder = new StringBuilder();
        if (isFront) {
            resultBuilder.append("Name：");
            resultBuilder.append(result.name);
            resultBuilder.append(System.lineSeparator());

            resultBuilder.append("Sex：");
            resultBuilder.append(result.sex);
            resultBuilder.append(System.lineSeparator());

            resultBuilder.append("Nation: ");
            resultBuilder.append(result.nation);
            resultBuilder.append(System.lineSeparator());

            resultBuilder.append("IDNum: ");
            resultBuilder.append(result.idNum);
            resultBuilder.append(System.lineSeparator());

            resultBuilder.append("Address: ");
            resultBuilder.append(result.address);
            resultBuilder.append(System.lineSeparator());

            resultBuilder.append("Birthday: ");
            resultBuilder.append(result.birthday);
            resultBuilder.append(System.lineSeparator());

            Log.i(TAG, "front result: " + resultBuilder.toString());
        } else {
            resultBuilder.append("ValidDate: ");
            resultBuilder.append(result.validDate);
            resultBuilder.append(System.lineSeparator());

            resultBuilder.append("Authority: ");
            resultBuilder.append(result.authority);
            resultBuilder.append(System.lineSeparator());
            Log.i(TAG, "back result: " + resultBuilder.toString());
        }
        return resultBuilder.toString();
    }

    private void showFrontImage(Bitmap bitmap) {
        Log.i(TAG, "showFrontImage");
        this.frontImg.setVisibility(View.VISIBLE);
        this.frontImg.setImageBitmap(bitmap);
        this.frontSimpleImg.setVisibility(View.GONE);
        this.frontAddView.setVisibility(View.GONE);
        this.frontDeleteImg.setVisibility(View.VISIBLE);
    }

    private void showBackImage(Bitmap bitmap) {
        this.backImg.setVisibility(View.VISIBLE);
        this.backImg.setImageBitmap(bitmap);
        this.backAddView.setVisibility(View.GONE);
        this.backSimpleImg.setVisibility(View.GONE);
        this.backDeleteImg.setVisibility(View.VISIBLE);
    }

    private void showFrontDeleteImage() {
        this.frontImg.setVisibility(View.GONE);
        this.frontSimpleImg.setVisibility(View.VISIBLE);
        this.frontAddView.setVisibility(View.VISIBLE);
        this.frontDeleteImg.setVisibility(View.GONE);
    }

    private void showBackDeleteImage() {
        this.backImg.setVisibility(View.GONE);
        this.backAddView.setVisibility(View.VISIBLE);
        this.backSimpleImg.setVisibility(View.VISIBLE);
        this.backDeleteImg.setVisibility(View.GONE);
    }
}
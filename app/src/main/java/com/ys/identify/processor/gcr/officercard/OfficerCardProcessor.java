package com.ys.identify.processor.gcr.officercard;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.huawei.hms.mlsdk.text.MLText;
import com.ys.identify.entity.BlockItem;
import com.ys.identify.entity.GeneralCardResult;
import com.ys.identify.processor.gcr.GeneralCardProcessor;
import com.ys.identify.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OfficerCardProcessor implements GeneralCardProcessor {
    private static final String TAG = "OfficerCardProcessor";

    private final MLText text;

    public OfficerCardProcessor(MLText text) {
        this.text = text;
    }

    @Override
    public GeneralCardResult getResult() {
        List<MLText.Block> blocks = text.getBlocks();
        if (blocks.isEmpty()) {
            Log.i(TAG, "Result blocks is empty");
            return null;
        }

        ArrayList<BlockItem> officerOriginItems = officerGetOriginItems(blocks);

        String name = "";
        String gender = "";
        String department = "";
        String job = "";
        String rank = "";
        String type = "";
        String number = "";
        String useDate = "";

        String valid = "";

        boolean nameFlag = false;
        boolean genderFlag = false;
        boolean departmentFlag = false;
        boolean jobFlag = false;
        boolean rankFlag = false;
        boolean typeFlag = false;
        boolean numberFlag = false;
        boolean useDateFlag = false;

        boolean validFlag = false;

        BlockItem blockItemName = officerOriginItems.stream().min(Comparator.comparing(BlockItem::getRectCenterY)).get();

        String nameResult = blockItemName.text;
        if (!nameResult.isEmpty()) {
            name = nameResult;
        }

        for (BlockItem item : officerOriginItems){
            String officerTempStr = item.text;
            if (!numberFlag) {
                String officerResult = tryGetOfficerNumber(officerTempStr);
                if (!officerResult.isEmpty()) {
                    number = officerResult;
                    numberFlag = true;
                }
            }
        }


//        for (BlockItem item : officerOriginItems) {
//            String officerTempStr = item.text;
//            Rect rect = item.rect;
//            int height = rect.centerY();
//
//
//            if (!nameFlag) {
//                String officerResult = officerTempStr;
//                if (!officerResult.isEmpty()) {
//                    name = officerResult;
//                    nameFlag = true;
//                }
//            }
//
//            if (!genderFlag) {
//                String officerResult = officerTempStr;
//                if (!officerResult.isEmpty()) {
//                    gender = officerResult;
//                    genderFlag = true;
//                }
//            }
//
//            if (!departmentFlag) {
//                String officerResult = officerTempStr;
//                if (!officerResult.isEmpty()) {
//                    department = officerResult;
//                    departmentFlag = true;
//                }
//            }
//
//            if (!jobFlag) {
//                String officerResult = officerTempStr;
//                if (!officerResult.isEmpty()) {
//                    job = officerResult;
//                    jobFlag = true;
//                }
//            }
//
//            if (!rankFlag) {
//                String officerResult = officerTempStr;
//                if (!officerResult.isEmpty()) {
//                    rank = officerResult;
//                    rankFlag = true;
//                }
//            }
//
//            if (!typeFlag) {
//                String officerResult = officerTempStr;
//                if (!officerResult.isEmpty()) {
//                    type = officerResult;
//                    typeFlag = true;
//                }
//            }
//
//            if (!numberFlag) {
//                String officerResult = officerTempStr;
//                if (!officerResult.isEmpty()) {
//                    number = officerResult;
//                    numberFlag = true;
//                }
//            }
//
//            if (!useDateFlag) {
//                String officerResult = officerTempStr;
//                if (!officerResult.isEmpty()) {
//                    useDate = officerResult;
//                    useDateFlag = true;
//                }
//            }
//        }

        Log.i(TAG, "valid: " + valid);
        Log.i(TAG, "number: " + number);

        return new GeneralCardResult(name, gender, department, job, rank, type, number, useDate);
    }

    private ArrayList<BlockItem> officerGetOriginItems(List<MLText.Block> blocks) {
        ArrayList<BlockItem> originItems = new ArrayList<>();

        for (MLText.Block block : blocks) {
            // Add in behavior units
            List<MLText.TextLine> lines = block.getContents();
            for (MLText.TextLine line : lines) {
                String officerText = line.getStringValue();
                officerText = StringUtils.filterString(officerText, "[^\u4e00-\u9fa5a-zA-Z0-9\\.\\-,<\\(\\)\\s]");
                Log.d(TAG, "text: " + officerText);
                Point[] points = line.getVertexes();
                Rect rect = new Rect(points[0].x, points[0].y, points[2].x, points[2].y);
                BlockItem item = new BlockItem(officerText, rect);
                originItems.add(item);
            }
        }
        return originItems;
    }

    private String tryGetValidDate(String originStr) {
        return StringUtils.getCorrectValidDate(originStr);
    }

    private String tryGetCardNumber(String originStr) {
        return StringUtils.getHomeCardNumber(originStr);
    }

    private String tryGetOfficerNumber(String originStr){
        return StringUtils.getOfficerNumber(originStr);
    }
}

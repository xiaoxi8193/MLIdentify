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

        String valid = "";
        String number = "";
        boolean numberFlag = false;
        boolean validFlag = false;

        for (BlockItem item : officerOriginItems) {
            String officerTempStr = item.text;

            if (!validFlag) {
                String officerResult = tryGetValidDate(officerTempStr);
                if (!officerResult.isEmpty()) {
                    valid = officerResult;
                    validFlag = true;
                }
            }

            if (!numberFlag) {
                String officerResult = tryGetCardNumber(officerTempStr);
                if (!officerResult.isEmpty()) {
                    number = officerResult;
                    numberFlag = true;
                }
            }
        }

        Log.i(TAG, "valid: " + valid);
        Log.i(TAG, "number: " + number);

        return new GeneralCardResult(valid, number);
    }

    private ArrayList<BlockItem> officerGetOriginItems(List<MLText.Block> blocks) {
        ArrayList<BlockItem> originItems = new ArrayList<>();

        for (MLText.Block block : blocks) {
            // Add in behavior units
            List<MLText.TextLine> lines = block.getContents();
            for (MLText.TextLine line : lines) {
                String officerText = line.getStringValue();
                officerText = StringUtils.filterString(officerText, "[^a-zA-Z0-9\\.\\-,<\\(\\)\\s]");
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
}

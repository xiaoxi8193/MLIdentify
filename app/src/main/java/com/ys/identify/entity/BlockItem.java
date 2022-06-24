package com.ys.identify.entity;

import android.graphics.Rect;

/**
 * Re-encapsulate the return result of OCR in Block
 */
public class BlockItem {
    public final String text;
    public final Rect rect;

    public BlockItem(String text, Rect rect) {
        this.text = text;
        this.rect = rect;
    }

    public String getText() {
        return text;
    }

    public Rect getRect() {
        return rect;
    }

    public int getRectCenterY(){
        return rect.centerY();
    }
}

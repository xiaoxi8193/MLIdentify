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
}

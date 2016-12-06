package com.noisyz.largeimageview.overlay.impl.image;

import android.graphics.Bitmap;

/**
 * Created by imac on 16.10.16.
 */

public class ImageOverlayItem {

    private Bitmap itemBitmapPart;
    private int horizontalIndex, verticalIndex, scaleFactor;
    private float scale, width, height;
    private boolean needLoadBitmap;

    public ImageOverlayItem(int horizontalIndex, int verticalIndex, float width, float height) {
        this.horizontalIndex = horizontalIndex;
        this.verticalIndex = verticalIndex;
        this.width = width;
        this.height = height;
        needLoadBitmap = true;
    }

    public boolean isScaleFactorChanged(int scaleFactor) {
        return scaleFactor != this.scaleFactor;
    }

    public int getHorizontalIndex() {
        return horizontalIndex;
    }

    public int getVerticalIndex() {
        return verticalIndex;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setItemBitmapPart(Bitmap bitmap, int scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.itemBitmapPart = bitmap;
        float widthScale = width / bitmap.getWidth();
        float heightScale = height / bitmap.getHeight();
        this.scale = widthScale > heightScale ? widthScale : heightScale;
        needLoadBitmap = false;
    }

    public float getScaleFactor() {
        return scale;
    }

    public Bitmap getItemBitmapPart() {
        return itemBitmapPart;
    }

    public void release() {
        itemBitmapPart = null;
        needLoadBitmap = true;
    }

    public boolean needLoadBitmap() {
        return needLoadBitmap;
    }

    public void notifyScaleFactorUpdated() {
        needLoadBitmap = true;
    }

}

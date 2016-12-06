package com.noisyz.largeimageview.overlay;

import android.graphics.Canvas;

/**
 * Created by imac on 21.10.16.
 */

public interface Overlay {

    void onDraw(Canvas canvas);

    void release();

    interface Callback{
        void needRedrawOverlay();
    }
}

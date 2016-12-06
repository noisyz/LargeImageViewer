package com.noisyz.largeimageview.overlay.impl.image;

import android.graphics.Matrix;
import android.graphics.Rect;

import com.noisyz.largeimageview.math.MathUtils;

import static com.noisyz.largeimageview.math.MathUtils.DX;
import static com.noisyz.largeimageview.math.MathUtils.DY;


/**
 * Created by imac on 24.10.16.
 */

public class ImageOverlayAdapter {

    private static final float OUTSIDE_VISIBLE_ZONE = 0f;

    private float[][] cornersPosition;

    private Rect rectLoad;
    private Matrix matrixDraw;

    private MathUtils mathUtils, visibilityMathUtils, debugMathUtils;
    private ImageStateManager stateManager;

    public ImageOverlayAdapter(ImageStateManager stateManager) {
        this.stateManager = stateManager;
        this.mathUtils = new MathUtils();
        this.visibilityMathUtils = new MathUtils();
        this.debugMathUtils = new MathUtils();
        this.rectLoad = new Rect();
        this.matrixDraw = new Matrix();
        cornersPosition = new float[2][4];
    }

    public Matrix getDrawMatrix(int horizontalIndex, int verticalIndex, float width, float height, float scaleFactor) {
        matrixDraw.reset();
        float zoom = stateManager.getZoom();
        float left = (horizontalIndex - (float) stateManager.getHorizontalSize() / 2) * width;
        float top = (verticalIndex - (float) stateManager.getVerticalSize() / 2) * height;
        matrixDraw.postTranslate(left * zoom, top * zoom);
        matrixDraw.postRotate(mathUtils.toDegrees(stateManager.getAngle()));
        matrixDraw.preScale(scaleFactor * zoom, scaleFactor * zoom);
        matrixDraw.postTranslate(stateManager.getImagePositionX(), stateManager.getImagePositionY());
        return matrixDraw;
    }

    public Rect getRectLoad(int horizontalIndex, int verticalIndex,
                            float rectWidth, float rectHeight) {
        int left = (int) (horizontalIndex * rectWidth);
        int top = (int) (verticalIndex * rectHeight);
        int right = (int) (left + rectWidth);
        int bottom = (int) (top + rectHeight);
        rectLoad.set(left, top, right, bottom);
        return rectLoad;
    }

    public boolean isVisible(int horizontalIndex, int verticalIndex,
                             float rectWidth, float rectHeight) {


        float angle = stateManager.getAngle();
        float zoom = stateManager.getZoom();

        float imageHalfWidth = stateManager.getImageWidth() * zoom / 2;
        float imageHalfHeight = stateManager.getImageHeight() * zoom / 2;

        float left = horizontalIndex * rectWidth * zoom;
        float top = verticalIndex * rectHeight * zoom;
        float right = (horizontalIndex + 1) * rectWidth * zoom;
        float bottom = (verticalIndex + 1) * rectHeight * zoom;

        cornersPosition[DX][0] = left;
        cornersPosition[DY][0] = top;

        cornersPosition[DX][1] = right;
        cornersPosition[DY][1] = top;

        cornersPosition[DX][2] = right;
        cornersPosition[DY][2] = bottom;

        cornersPosition[DX][3] = left;
        cornersPosition[DY][3] = bottom;

        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;

        float[] centerUpdates = debugMathUtils.getRotationPositionUpdates(angle, centerX, centerY,
                imageHalfWidth, imageHalfHeight);
        centerX += centerUpdates[DX];
        centerY += centerUpdates[DY];

        left = centerX;
        right = centerX;
        top = centerY;
        bottom = centerY;

        for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
            float[] updates = visibilityMathUtils.getRotationPositionUpdates(angle,
                    cornersPosition[DX][cornerIndex], cornersPosition[DY][cornerIndex],
                    imageHalfWidth, imageHalfHeight);
            cornersPosition[DX][cornerIndex] += updates[DX];
            cornersPosition[DY][cornerIndex] += updates[DY];

            if (cornersPosition[DX][cornerIndex] < left)
                left = cornersPosition[DX][cornerIndex];

            if (cornersPosition[DX][cornerIndex] > right)
                right = cornersPosition[DX][cornerIndex];

            if (cornersPosition[DY][cornerIndex] < top)
                top = cornersPosition[DY][cornerIndex];

            if (cornersPosition[DY][cornerIndex] > bottom)
                bottom = cornersPosition[DY][cornerIndex];

        }

        left += stateManager.getImagePositionX() - imageHalfWidth;
        top += stateManager.getImagePositionY() - imageHalfHeight;
        right += stateManager.getImagePositionX() - imageHalfWidth;
        bottom += stateManager.getImagePositionY() - imageHalfHeight;

        boolean horizontalVisibility = right >= -OUTSIDE_VISIBLE_ZONE &&
                left <= stateManager.getCameraWidth() + OUTSIDE_VISIBLE_ZONE;
        boolean verticalVisibility = bottom >= -OUTSIDE_VISIBLE_ZONE &&
                top <= stateManager.getCameraHeight() + OUTSIDE_VISIBLE_ZONE;

        return horizontalVisibility && verticalVisibility;
    }
}

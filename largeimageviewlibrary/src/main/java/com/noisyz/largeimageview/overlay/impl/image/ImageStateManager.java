package com.noisyz.largeimageview.overlay.impl.image;


import com.noisyz.largeimageview.math.MathUtils;
import com.noisyz.largeimageview.overlay.impl.camera.Camera;

import static com.noisyz.largeimageview.math.MathUtils.DX;
import static com.noisyz.largeimageview.math.MathUtils.DY;

/**
 * Created by imac on 05.12.16.
 */

public class ImageStateManager extends Camera {

    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 10f;

    private int MAX_SCALE_FACTOR;

    private float cos, sin, maxX, maxY;
    private int imageWidth, imageHeight, horizontalSize, verticalSize;

    private final MathUtils mathUtils;


    public ImageStateManager(int imageWidth, int imageHeight, int horizontalSize, int verticalSize) {
        super();
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.horizontalSize = horizontalSize;
        this.verticalSize = verticalSize;
        mathUtils = new MathUtils();
        updateRotateData();
    }

    @Override
    public void rotate(float dAngle, float dAngleDeg, float centerX, float centerY) {
        super.rotate(dAngle, mathUtils.toDegrees(dAngle), centerX, centerY);
        updateRotateData();
        updatePositionByRotation(-dAngle, centerX, centerY);
    }

    private void updateRotateData() {
        cos = (float) Math.cos(rotate);
        sin = (float) Math.sin(rotate);
        calculateOverscrollValues();
    }

    private void updatePositionByRotation(float newAngle, float averageX, float averageY) {
        float[] updates = mathUtils.getRotationPositionUpdates(newAngle, positionX, positionY, averageX, averageY);
        updatePosition(updates[DX], updates[DY]);
    }


    @Override
    public void updateZoom(float dZoom, float centerX, float centerY) {
        dZoom = validateDZoom(dZoom);
        super.updateZoom(dZoom, centerX, centerY);
        calculateOverscrollValues();
        updatePositionByZoom(dZoom, centerX, centerY);
    }

    private float validateDZoom(float dZoom) {
        if (zoom * dZoom >= MAX_ZOOM) {
            dZoom = MAX_ZOOM / zoom;
        }
        if (zoom * dZoom <= MIN_ZOOM) {
            dZoom = MIN_ZOOM / zoom;
        }
        return dZoom;
    }


    private void updatePositionByZoom(float dZoom, float centerX, float centerY) {
        float dX = (positionX - centerX) * (dZoom - 1);
        float dY = (positionY - centerY) * (dZoom - 1);
        updatePosition(dX, dY);
    }

    @Override
    public void updatePosition(float dX, float dY) {
        dX = getHorizontalOverscrolled(positionX, dX);
        dY = getVerticalOverscrolled(positionY, dY);
        super.updatePosition(dX, dY);
    }

    private float getHorizontalOverscrolled(float imagePositionX, float dX) {
        float left = -maxX / 2;
        float right = maxX / 2;
        if (imagePositionX + dX - cameraWidth < left)
            dX = left - (imagePositionX - cameraWidth);
        else if (imagePositionX + dX > right)
            dX = right - imagePositionX;
        return dX;
    }

    private float getVerticalOverscrolled(float imagePositionY, float dY) {
        float top = -maxY / 2;
        float bottom = maxY / 2;
        if (imagePositionY + dY - cameraHeight < top) {
            dY = top - (imagePositionY - cameraHeight);
        } else if (imagePositionY + dY > bottom) {
            dY = bottom - imagePositionY;
        }
        return dY;
    }

    @Override
    public void updateSize(float width, float height) {
        super.updateSize(width, height);
        calculateOverscrollValues();
        calculateMaxZoom(width, height);
    }


    private void calculateOverscrollValues() {
        maxX = (Math.abs(imageWidth * cos) + Math.abs(imageHeight * sin)) * zoom;
        maxY = (Math.abs(imageHeight * cos) + Math.abs(imageWidth * sin)) * zoom;
    }

    private void calculateMaxZoom(float cameraWidth, float cameraHeight) {
        float zoomPart = cameraWidth > cameraHeight ? cameraWidth : cameraHeight;
        float widthZoom = imageWidth / zoomPart;
        float heightZoom = imageHeight / zoomPart;
        MAX_SCALE_FACTOR = (int) (widthZoom < heightZoom ? widthZoom : heightZoom);
    }

    public int getScaleFactor() {
        int scaleFactor = (int) (MAX_SCALE_FACTOR - zoom);
        if (scaleFactor < 1)
            scaleFactor = 1;
        return scaleFactor;
    }

    public float getAngle() {
        return rotate;
    }

    public float getCameraWidth() {
        return cameraWidth;
    }

    public float getCameraHeight() {
        return cameraHeight;
    }

    public float getImagePositionX() {
        return positionX;
    }

    public float getImagePositionY() {
        return positionY;
    }

    public float getZoom() {
        return zoom;
    }

    public int getVerticalSize() {
        return verticalSize;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getHorizontalSize() {
        return horizontalSize;
    }
}

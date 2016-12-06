package com.noisyz.largeimageview.overlay.impl.image;

import com.noisyz.largeimageview.math.MathUtils;
import com.noisyz.largeimageview.overlay.impl.camera.UpdateCallback;

import static com.noisyz.largeimageview.math.MathUtils.DX;
import static com.noisyz.largeimageview.math.MathUtils.DY;

/**
 * Created by imac on 05.12.16.
 */

public class ImageStateManager {

    private int MAX_SCALE_FACTOR;
    private static final float MIN_ZOOM = 0.1f;

    private float cameraWidth, cameraHeight, imagePositionX, imagePositionY,
            zoom, angle, cos, sin, maxX, maxY;
    private int imageWidth, imageHeight, horizontalSize, verticalSize;

    private final MathUtils mathUtils;

    private UpdateCallback cameraCallback;

    public ImageStateManager(UpdateCallback cameraCallback,
                             int imageWidth, int imageHeight, int horizontalSize, int verticalSize) {
        this.cameraCallback = cameraCallback;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.horizontalSize = horizontalSize;
        this.verticalSize = verticalSize;
        zoom = 1;
        mathUtils = new MathUtils();
    }

    public void updateRotation(float dAngle, float centerX, float centerY) {
        angle -= dAngle;
        angle = (float) (angle % (2 * Math.PI));
        if (angle > 0)
            angle -= 2 * Math.PI;
        updateRotateData();
        updatePositionByRotation(-dAngle, centerX, centerY);
        cameraCallback.updateAngle(angle, mathUtils.toDegrees(dAngle), dAngle, centerX, centerY);
    }

    private void updateRotateData() {
        cos = (float) Math.cos(angle);
        sin = (float) Math.sin(angle);
        calculateOverscrollValues();
    }

    private void updatePositionByRotation(float newAngle, float averageX, float averageY) {
        float[] updates = mathUtils.getRotationPositionUpdates(newAngle, imagePositionX, imagePositionY, averageX, averageY);
        updatePosition(updates[DX], updates[DY]);
    }

    public void updateZoom(float dZoom, float centerX, float centerY) {
        float MAX_ZOOM = 10;
        if (zoom * dZoom > MAX_ZOOM) {
            dZoom = MAX_ZOOM / zoom;
        }
        if (zoom * dZoom < MIN_ZOOM) {
            dZoom = MIN_ZOOM / zoom;
        }
        this.zoom *= dZoom;
        updatePositionByZoom(dZoom, centerX, centerY);
        calculateOverscrollValues();

        cameraCallback.updateZoom(zoom, dZoom, centerX, centerY);
    }

    private void updatePositionByZoom(float dZoom, float centerX, float centerY) {
        float dX = (imagePositionX - centerX) * (dZoom - 1);
        float dY = (imagePositionY - centerY) * (dZoom - 1);
        updatePosition(dX, dY);
    }

    public void updatePosition(float dX, float dY) {
        imagePositionX += getHorizontalOverscrolled(imagePositionX, dX);
        imagePositionY += getVerticalOverscrolled(imagePositionY, dY);
        cameraCallback.updatePosition(imagePositionX, imagePositionY, dX, dY);
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

    public void updateCameraSize(float width, float height) {
        this.cameraWidth = width;
        this.cameraHeight = height;
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
        return angle;
    }

    public float getCameraWidth() {
        return cameraWidth;
    }

    public float getCameraHeight() {
        return cameraHeight;
    }

    public float getImagePositionX() {
        return imagePositionX;
    }

    public float getImagePositionY() {
        return imagePositionY;
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

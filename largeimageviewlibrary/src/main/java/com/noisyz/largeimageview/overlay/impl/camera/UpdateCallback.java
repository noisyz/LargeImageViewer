package com.noisyz.largeimageview.overlay.impl.camera;

/**
 * Created by imac on 05.12.16.
 */

public interface UpdateCallback {
    void updatePosition(float x, float y, float dX, float dY);

    void updateZoom(float zoom, float dZoom, float zoomCenterX, float zoomCenterY);

    void updateAngle(float angle, float dAngle, float dRadians, float rotationCenterX, float rotationCenterY);

    void updateCameraSize(float width, float height);

    void onTap(float x, float y);
}

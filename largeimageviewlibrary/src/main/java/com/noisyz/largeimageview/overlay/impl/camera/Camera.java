package com.noisyz.largeimageview.overlay.impl.camera;


import com.noisyz.largeimageview.math.Vector2D;
import com.noisyz.largeimageview.overlay.impl.image.ImageOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imac on 16.10.16.
 */


//visible part data wrapper
//allow interact other overlays with view
public class Camera {

    protected float zoom, rotate, positionX, positionY, cameraWidth, cameraHeight;
    private List<UpdateCallback> updateCallbacks;

    public Camera() {
        zoom = 1;
        rotate = 0;
    }

    public void addUpdateCallback(UpdateCallback updateCallback) {
        if (updateCallbacks == null)
            updateCallbacks = new ArrayList<>();
        this.updateCallbacks.add(updateCallback);
    }

    public void removeUpdateCallback(UpdateCallback updateCallback) {
        if (updateCallbacks.contains(updateCallback))
            updateCallbacks.remove(updateCallback);
    }


    public void updateSize(float width, float height) {
        this.cameraWidth = width;
        this.cameraHeight = height;
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks) {
                updateCallback.updateCameraSize(width, height);
                updateCallback.updateZoom(zoom, 1, 0, 0);
                updateCallback.updatePosition(positionX, positionY, 0, 0);
            }
    }

    public void updatePosition(float dX, float dY) {
        positionX += dX;
        positionY += dY;
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks) {
                updateCallback.updatePosition(positionX, positionY, dX, dY);
            }
    }

    public void updateZoom(float dZoom, float zoomCenterX, float zoomCenterY) {
        this.zoom *= dZoom;
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks)
                updateCallback.updateZoom(zoom, dZoom, zoomCenterX, zoomCenterY);
    }


    public void rotate(float dAngle, float dAngleDec, float rotationCenterX, float rotationCenterY) {
        rotate -= dAngle;
        rotate = (float) (rotate % (2 * Math.PI));
        if (rotate > 0)
            rotate -= 2 * Math.PI;
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks)
                updateCallback.updateAngle(rotate, dAngleDec, dAngle, rotationCenterX, rotationCenterY);
    }


    public void tap(float x, float y) {
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks)
                updateCallback.onTap(x, y);
    }

    public void release() {
        updateCallbacks.clear();
        updateCallbacks = null;
    }
}

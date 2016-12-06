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
public class Camera implements UpdateCallback {

    private float zoom, rotate;
    private Vector2D position;
    private UpdateCallback rootUpdateCallback;
    private List<UpdateCallback> updateCallbacks;

    public void init(UpdateCallback rootUpdateCallback){
        this.position = new Vector2D();
        position.set(0, 0);
        zoom = 1;
        rotate = 0;
        this.rootUpdateCallback = rootUpdateCallback;
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
        if (rootUpdateCallback != null)
            rootUpdateCallback.updateCameraSize(width, height);
    }

    public void updatePosition(Vector2D newPosition) {
        position.add(newPosition);
        if(rootUpdateCallback!=null)
            rootUpdateCallback.updatePosition(position.getX(), position.getY(), newPosition.getX(), newPosition.getY());
    }

    public void updateZoom(float dZoom, float zoomCenterX, float zoomCenterY) {
        this.zoom *= dZoom;
        if (rootUpdateCallback != null)
            rootUpdateCallback.updateZoom(zoom, dZoom, zoomCenterX, zoomCenterY);
    }


    public void rotate(float dRadians, float dAngle, float rotationCenterX, float rotationCenterY) {
        rotate -= dAngle;
        if (rootUpdateCallback != null)
            rootUpdateCallback.updateAngle(rotationCenterX, dAngle, dRadians, rotationCenterX, rotationCenterY);
    }

    @Override
    public void updatePosition(float x, float y, float dX, float dY) {
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks) {
                updateCallback.updatePosition(position.getX(), position.getY(), dX, dY);
            }
    }

    @Override
    public void updateZoom(float zoom, float dZoom, float zoomCenterX, float zoomCenterY) {
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks)
                updateCallback.updateZoom(zoom, dZoom, zoomCenterX, zoomCenterY);
    }

    @Override
    public void updateAngle(float angle, float dAngle, float dRadians, float rotationCenterX, float rotationCenterY) {
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks)
                updateCallback.updateAngle(rotate, dAngle, dRadians, rotationCenterX, rotationCenterY);
    }

    @Override
    public void updateCameraSize(float width, float height) {
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks) {
                updateCallback.updateCameraSize(width, height);
                updateCallback.updateZoom(zoom, 1, 0, 0);
                updateCallback.updatePosition(position.getX(), position.getY(), 0, 0);
            }
    }

    @Override
    public void onTap(float x, float y) {
        if (updateCallbacks != null)
            for (UpdateCallback updateCallback : updateCallbacks)
                updateCallback.onTap(x, y);
    }

    public void tap(float x, float y) {
        onTap(x, y);
    }

    public void release() {
        updateCallbacks.clear();
        position = null;
        updateCallbacks = null;
    }
}

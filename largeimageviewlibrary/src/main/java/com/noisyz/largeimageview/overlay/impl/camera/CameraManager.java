package com.noisyz.largeimageview.overlay.impl.camera;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.noisyz.largeimageview.gestures.TouchManager;
import com.noisyz.largeimageview.math.MathUtils;
import com.noisyz.largeimageview.math.Vector2D;


/**
 * Created by imac on 16.10.16.
 */

public class CameraManager implements View.OnTouchListener{

    private TouchManager touchManager;
    private Camera camera;
    private MathUtils mathUtils;
    private float cameraWidth, cameraHeight;

    public CameraManager() {
        this.mathUtils = new MathUtils();
        touchManager = new TouchManager(2);
    }

    public void updateCamera(Camera camera){
        this.camera = camera;
        updateCameraSize(cameraWidth, cameraHeight);
    }

    public void addCameraCallback(UpdateCallback updateCallback) {
        if (camera != null)
            this.camera.addUpdateCallback(updateCallback);
    }

    public void removeCameraCallback(UpdateCallback updateCallback) {
        if (camera != null)
            camera.removeUpdateCallback(updateCallback);
    }

    public void updateCameraSize(float width, float height) {
        this.cameraWidth = width;
        this.cameraHeight = height;
        if (camera != null)
            camera.updateSize(width, height);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchManager.update(event);
        Vector2D tap = touchManager.detectTap(event);
        if (tap != null) {
            camera.tap(tap.getX(), tap.getY());
        }
        if (touchManager.getPressCount() == 1) {
            //map has been moved
            Vector2D newPosition = touchManager.moveDelta(0);
            camera.updatePosition(newPosition.getX(), newPosition.getY());
        } else {
            if (touchManager.getPressCount() == 2) {

                //user manipulate by two fingers. Calculating zoom and rotation
                Vector2D current = touchManager.getVector(0, 1);
                Vector2D previous = touchManager.getPreviousVector(0, 1);

                Vector2D average = touchManager.average(0, 1);

                float currentDistance = current.getLength();
                float previousDistance = previous.getLength();

                float dZoom = 1;

                if (previousDistance != 0) {
                    dZoom = currentDistance / previousDistance;
                }
                float dAngle = Vector2D.getSignedAngleBetween(current, previous);

                camera.rotate(dAngle, mathUtils.toDegrees(dAngle), average.getX(), average.getY());
                camera.updateZoom(dZoom, average.getX(), average.getY());
            }
        }
        return true;
    }

    public void release() {
        touchManager = null;
        if (camera != null)
            camera.release();
        camera = null;
    }

    public interface CameraCallback {
        void createCamera(Camera camera);
    }
}

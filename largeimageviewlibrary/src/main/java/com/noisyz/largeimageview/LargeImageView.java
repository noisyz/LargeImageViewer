package com.noisyz.largeimageview;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.noisyz.largeimageview.overlay.Overlay;
import com.noisyz.largeimageview.overlay.impl.camera.Camera;
import com.noisyz.largeimageview.overlay.impl.camera.CameraManager;
import com.noisyz.largeimageview.overlay.impl.camera.UpdateCallback;
import com.noisyz.largeimageview.overlay.impl.image.ImageOverlay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LargeImageView extends View implements CameraManager.CameraCallback, Overlay.Callback, ViewTreeObserver.OnGlobalLayoutListener {

    private List<Overlay> overlays;
    private CameraManager cameraManager;
    private ImageOverlay imageOverlay;
    private BitmapFactory.Options options;

    private final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    public LargeImageView(Context context) {
        super(context);
        init();
    }

    public LargeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LargeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        overlays = new ArrayList<>();

        cameraManager = new CameraManager();

        //allow interact with screen events to all overlays
        imageOverlay = new ImageOverlay(this);
        imageOverlay.setOverlayChangedListener(this);

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void setImageResource(int imageResourceID) {
        Uri uri = Uri.parse("android.resource://" + getContext().getPackageName() + "/drawable/" + imageResourceID);
        try {
            InputStream stream = getContext().getContentResolver().openInputStream(uri);
            BitmapFactory.decodeResource(getResources(), imageResourceID, options);
            loadImage(stream, options.outWidth, options.outHeight);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setImageFromAssets(String fileName) {
        try {
            InputStream inputStream = getContext().getAssets().open(fileName);
            BitmapFactory.decodeStream(inputStream, null, options);
            loadImage(inputStream, options.outWidth, options.outHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setImageFromFile(File file) {
        if (file.exists())
            try {
                BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                InputStream inputStream = new FileInputStream(file);
                loadImage(inputStream, options.outWidth, options.outHeight);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
    }

    //load map from chart data wrapper
    public void loadImage(InputStream inputStream, int imageWidth, int imageHeight) {
        imageOverlay.release();
        imageOverlay.loadImage(inputStream, imageWidth, imageHeight);
    }

    public void addUpdateCallback(UpdateCallback updateCallback) {
        if (cameraManager != null)
            cameraManager.addCameraCallback(updateCallback);
    }

    public void removeUpdateCallback(UpdateCallback updateCallback) {
        if (cameraManager != null)
            cameraManager.removeCameraCallback(updateCallback);
    }

    public void addOverlay(Overlay overlay) {
        overlays.add(overlay);
    }

    public void removeOverlay(Overlay overlay) {
        overlays.remove(overlay);
    }

    @Override
    public void createCamera(Camera camera) {
        cameraManager.updateCamera(camera);
        setOnTouchListener(cameraManager);
        addUpdateCallback(imageOverlay);
    }

    //view size has been changed
    @Override
    public void onGlobalLayout() {
        cameraManager.updateCameraSize(getMeasuredWidth(), getMeasuredHeight());
    }

    //called when view need to bee redrawn
    @Override
    public void needRedrawOverlay() {
        post(updateUI);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        imageOverlay.onDraw(canvas);
        for (Overlay overlay : overlays)
            if (overlay != null)
                overlay.onDraw(canvas);
    }


    public void release() {
        for (Overlay overlay : overlays)
            if (overlay != null)
                overlay.release();
        cameraManager.release();
    }
}

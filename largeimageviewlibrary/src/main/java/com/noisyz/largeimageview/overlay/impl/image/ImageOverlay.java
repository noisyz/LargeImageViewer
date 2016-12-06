package com.noisyz.largeimageview.overlay.impl.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import com.noisyz.largeimageview.overlay.Overlay;
import com.noisyz.largeimageview.overlay.impl.camera.UpdateCallback;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by imac on 16.10.16.
 */

public class ImageOverlay implements Overlay, UpdateCallback {

    private UpdateImageThread[] updateImageThreads;
    private ImageStateManager imageStateManager;
    private Callback imageOverlayChangedListener;
    private UpdateCallback cameraCallback;

    public ImageOverlay(@NonNull UpdateCallback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    public void loadImage(InputStream inputStream, int imageWidth, int imageHeight) {
        release();
        createImageOverlayGrid(inputStream, imageWidth, imageHeight);
    }


    //calculate map grid with parts of math bitmap
    private void createImageOverlayGrid(InputStream inputStream, int imageWidth, int imageHeight) {
        int GRID_SIZE_HORIZONTAL = 10;
        int GRID_SIZE_VERTICAL = 10;
        //load bitmap decoder
        for (int i = 10; i < 30; i++) {
            if (imageWidth % i == 0) {
                GRID_SIZE_HORIZONTAL = i;
            }
            if (imageHeight % i == 0) {
                GRID_SIZE_VERTICAL = i;
            }
        }

        imageStateManager = new ImageStateManager(cameraCallback,
                imageWidth, imageHeight, GRID_SIZE_HORIZONTAL, GRID_SIZE_VERTICAL);

        ImageOverlayItem[] overlayItems = new ImageOverlayItem[GRID_SIZE_HORIZONTAL * GRID_SIZE_VERTICAL];

        float width = imageWidth / GRID_SIZE_HORIZONTAL;
        float height = imageHeight / GRID_SIZE_VERTICAL;

        for (int horizontalIndex = 0; horizontalIndex < GRID_SIZE_HORIZONTAL; horizontalIndex++)
            for (int verticalIndex = 0; verticalIndex < GRID_SIZE_VERTICAL; verticalIndex++) {
                int index = GRID_SIZE_HORIZONTAL > GRID_SIZE_VERTICAL ?
                        GRID_SIZE_VERTICAL * horizontalIndex + verticalIndex :
                        GRID_SIZE_HORIZONTAL * verticalIndex + horizontalIndex;
                overlayItems[index] = new ImageOverlayItem(horizontalIndex, verticalIndex, width, height);
            }
        initUpdateImageThreads(inputStream, overlayItems);
    }

    private void initUpdateImageThreads(InputStream inputStream,
                                        ImageOverlayItem[] overlayItems) {
        int threadsCount = 4;

        updateImageThreads = new UpdateImageThread[threadsCount];
        int itemsCount = overlayItems.length / threadsCount;
        int lastPartItemCount = overlayItems.length % threadsCount;
        for (int threadIndex = 0; threadIndex < threadsCount; threadIndex++) {
            int last = itemsCount;
            if (threadIndex == threadsCount - 1)
                last += lastPartItemCount;
            ImageOverlayItem[] imageOverlayItems = new ImageOverlayItem[last];
            System.arraycopy(overlayItems, itemsCount * threadIndex, imageOverlayItems, 0, last);
            //create adapter
            ImageOverlayAdapter imageOverlayAdapter = new ImageOverlayAdapter(imageStateManager);
            updateImageThreads[threadIndex] = new UpdateImageThread(imageOverlayItems,
                    imageOverlayAdapter, inputStream);
            updateImageThreads[threadIndex].start();
        }
    }

    public void setImageOverlayChangedListener(Callback listener) {
        this.imageOverlayChangedListener = listener;
    }

    @Override
    public void updatePosition(float x, float y, float dX, float dY) {
        //update Image and camera position
        if (imageStateManager != null)
            imageStateManager.updatePosition(dX, dY);

        updateUI();
    }

    @Override
    public void updateZoom(float zoom, float dZoom, float centerX, float centerY) {
        if (imageStateManager != null)
            imageStateManager.updateZoom(dZoom, centerX, centerY);

        if (updateImageThreads != null)
            for (UpdateImageThread updateImageThread : updateImageThreads)
                updateImageThread.updateScaleFactor();

        updateUI();
    }


    @Override
    public void updateAngle(float angle, float dAngle, float dRadians, float rotationCenterX, float rotationCenterY) {
        if (imageStateManager != null)
            imageStateManager.updateRotation(dRadians, rotationCenterX, rotationCenterY);

        updateUI();
    }

    @Override
    public void updateCameraSize(float width, float height) {
        if (imageStateManager != null)
            imageStateManager.updateCameraSize(width, height);

    }

    @Override
    public void onTap(float x, float y) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        if (updateImageThreads != null)
            for (UpdateImageThread updateImageThread : updateImageThreads)
                for (ImageOverlayItem imageOverlayItem : updateImageThread.getOverlayItems())
                    if (imageOverlayItem != null) {
                        Bitmap bitmap = imageOverlayItem.getItemBitmapPart();
                        if (bitmap != null) {
                            Matrix matrix = updateImageThread.getImageOverlayAdapter().
                                    getDrawMatrix(imageOverlayItem.getHorizontalIndex(),
                                            imageOverlayItem.getVerticalIndex(), imageOverlayItem.getWidth(),
                                            imageOverlayItem.getHeight(), imageOverlayItem.getScaleFactor());
                            canvas.drawBitmap(bitmap, matrix, updateImageThread.getPaint());
                        }
                    }
    }

    //call view redraw method
    private void updateUI() {
        if (imageOverlayChangedListener != null) {
            imageOverlayChangedListener.needRedrawOverlay();
        }
    }

    @Override
    public void release() {
        if (updateImageThreads != null)
            for (UpdateImageThread updateImageThread : updateImageThreads) {
                updateImageThread.stopThread();
            }
        releaseItems();
        updateImageThreads = null;
    }

    private void releaseItems() {
        if (updateImageThreads != null)
            for (UpdateImageThread updateImageThread : updateImageThreads)
                for (ImageOverlayItem ImageOverlayItem : updateImageThread.getOverlayItems())
                    ImageOverlayItem.release();
    }


    //thread to background Image parts loading
    private class UpdateImageThread extends Thread {

        private ImageOverlayItem[] overlayItems;
        private BitmapRegionDecoder bitmapRegionDecoder;
        private boolean isAttached;
        private BitmapFactory.Options loadOptions;
        private Paint paint;
        private ImageOverlayAdapter imageOverlayAdapter;
        private InputStream inputStream;


        public UpdateImageThread(ImageOverlayItem[] overlayItems, ImageOverlayAdapter imageOverlayAdapter,
                                 InputStream inputStream) {
            this.inputStream = inputStream;
            this.imageOverlayAdapter = imageOverlayAdapter;
            this.overlayItems = overlayItems;
            isAttached = true;
            loadOptions = new BitmapFactory.Options();
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        public void updateScaleFactor() {
            this.loadOptions.inSampleSize = imageStateManager.getScaleFactor();
        }

        public Paint getPaint() {
            return paint;
        }

        public void stopThread() {
            isAttached = false;
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public ImageOverlayAdapter getImageOverlayAdapter() {
            return imageOverlayAdapter;
        }

        public ImageOverlayItem[] getOverlayItems() {
            return overlayItems;
        }

        @Override
        public void run() {
            if (inputStream != null)
                try {
                    this.bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
                    //while view is visible
                    while (isAttached) {
                        for (ImageOverlayItem imageOverlayItem : overlayItems)
                            if (imageOverlayItem != null) {

                                //detect Image part visibility
                                boolean isVisible = imageOverlayAdapter.isVisible(imageOverlayItem.getHorizontalIndex(),
                                        imageOverlayItem.getVerticalIndex(), imageOverlayItem.getWidth(), imageOverlayItem.getHeight());

                                //check current bitmap compress value
                                if (imageOverlayItem.isScaleFactorChanged(loadOptions.inSampleSize)) {
                                    imageOverlayItem.notifyScaleFactorUpdated();
                                }

                                //load Image part to show
                                if (isVisible && imageOverlayItem.needLoadBitmap()) {
                                    Rect rect = imageOverlayAdapter.getRectLoad(imageOverlayItem.getHorizontalIndex(),
                                            imageOverlayItem.getVerticalIndex(), imageOverlayItem.getWidth(), imageOverlayItem.getHeight());
                                    final Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, loadOptions);
                                    if (bitmap != null) {
                                        imageOverlayItem.setItemBitmapPart(bitmap, loadOptions.inSampleSize);
                                        updateUI();
                                    }
                                }

                                //remove unused bitmap
                                if (!isVisible) {
                                    imageOverlayItem.release();
                                }
                            }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }
}

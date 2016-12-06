package com.noisyz.largeimageview.gestures;

import android.view.MotionEvent;

import com.noisyz.largeimageview.math.Vector2D;


public class TouchManager {

    private static final float TAP_MAX_RADIUS = 5;
    private static final long TAP_MAX_PERIOD = 200;
    private static final long TAP_DELAY = 1000;
    private final int maxNumberOfTouchPoints;

    private final Vector2D[] points;
    private final Vector2D[] previousPoints;

    private Vector2D tap;
    private long lastTapEvent;

    public TouchManager(final int maxNumberOfTouchPoints) {
        this.maxNumberOfTouchPoints = maxNumberOfTouchPoints;
        points = new Vector2D[maxNumberOfTouchPoints];
        previousPoints = new Vector2D[maxNumberOfTouchPoints];
    }

    public boolean isPressed(int index) {
        return points[index] != null;
    }

    public int getPressCount() {
        int count = 0;
        for (Vector2D point : points) {
            if (point != null)
                ++count;
        }
        return count;
    }

    public Vector2D moveDelta(int index) {
        if (isPressed(index)) {
            Vector2D previous = previousPoints[index] != null ? previousPoints[index] : points[index];
            return Vector2D.subtract(points[index], previous);
        } else {
            return new Vector2D();
        }
    }

    private static Vector2D getVector(Vector2D a, Vector2D b) {
        if (a == null || b == null)
            throw new RuntimeException("can't do this on nulls");

        return Vector2D.subtract(b, a);
    }

    public Vector2D average(int... index) {
        float x = 0;
        float y = 0;
        for (Vector2D point : points)
            if (point != null) {
                x += point.getX();
                y += point.getY();
            }
        return new Vector2D(x / index.length, y / index.length);
    }

    public Vector2D getPoint(int index) {
        return points[index] != null ? points[index] : new Vector2D();
    }

    public Vector2D getPreviousPoint(int index) {
        return previousPoints[index] != null ? previousPoints[index] : new Vector2D();
    }

    public Vector2D getVector(int indexA, int indexB) {
        return getVector(points[indexA], points[indexB]);
    }

    public Vector2D getPreviousVector(int indexA, int indexB) {
        if (previousPoints[indexA] == null || previousPoints[indexB] == null)
            return getVector(points[indexA], points[indexB]);
        else
            return getVector(previousPoints[indexA], previousPoints[indexB]);
    }

    public void update(MotionEvent event) throws ArrayIndexOutOfBoundsException {
        int actionCode = event.getAction() & MotionEvent.ACTION_MASK;

        if (actionCode == MotionEvent.ACTION_POINTER_UP || actionCode == MotionEvent.ACTION_UP) {
            int index = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
            if (index < maxNumberOfTouchPoints)
                previousPoints[index] = points[index] = null;
        } else {
            for (int i = 0; i < maxNumberOfTouchPoints; ++i) {
                if (i < event.getPointerCount()) {
                    int index = event.getPointerId(i);
                    if (index < maxNumberOfTouchPoints) {
                        Vector2D newPoint = new Vector2D(event.getX(i), event.getY(i));

                        if (points[index] == null)
                            points[index] = newPoint;
                        else {
                            if (previousPoints[index] != null) {
                                previousPoints[index].set(points[index]);
                            } else {
                                previousPoints[index] = new Vector2D(newPoint);
                            }
                            points[index].set(newPoint);
                        }
                    }
                } else {
                    previousPoints[i] = points[i] = null;
                }
            }
        }
    }

    public Vector2D detectTap(MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (tap == null) {
                    tap = new Vector2D();
                }
                if (System.currentTimeMillis() - lastTapEvent > TAP_DELAY) {
                    tap.set(motionEvent.getX(), motionEvent.getY());
                    lastTapEvent = System.currentTimeMillis();
                }
                return null;
            case MotionEvent.ACTION_UP:
                boolean checkRadius = Math.abs(motionEvent.getX() - tap.getX()) < TAP_MAX_RADIUS
                        && Math.abs(motionEvent.getY() - tap.getY()) < TAP_MAX_RADIUS;
                boolean checkPeriod = System.currentTimeMillis() - lastTapEvent < TAP_MAX_PERIOD;
                if (checkPeriod && checkRadius)
                    return tap;
        }
        return null;
    }
}

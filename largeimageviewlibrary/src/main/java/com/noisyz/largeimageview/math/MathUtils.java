package com.noisyz.largeimageview.math;

/**
 * Created by imac on 04.11.16.
 */

public class MathUtils {
    public static final int DX = 0;
    public static final int DY = 1;


    public static final int INVALID_QUARTER = -1;
    public static final int QUARTER_1 = 0;
    public static final int QUARTER_2 = 1;
    public static final int QUARTER_3 = 2;
    public static final int QUARTER_4 = 3;

    private final float[] rotationPositionUpdates = new float[2];

    public float toDegrees(float angle) {
        return (float) (angle * 180.0 / Math.PI);
    }

    public float toRadians(float degrees) {
        return (float) (degrees * Math.PI / 180f);
    }

    public float[] getRotationPositionUpdates(float newAngle, float rotatePointX, float rotatePointY, float rotateCenterX, float rotateCenterY) {
        return getRotationPositionUpdates(rotationPositionUpdates, newAngle, rotatePointX, rotatePointY, rotateCenterX, rotateCenterY);
    }


    //calculate map point updates after rotation
    public float[] getRotationPositionUpdates(float[] result, float newAngle, float rotatePointX, float rotatePointY, float rotateCenterX, float rotateCenterY) {

        float rotateX = rotatePointX - rotateCenterX;
        float rotateY = rotatePointY - rotateCenterY;

        float dAngle = getDAngle(rotateX, rotateY);

        float radius = (float) Math.sqrt(rotateX * rotateX + rotateY * rotateY);

        float currentAngle = dAngle + newAngle;

        float cosd = (float) Math.cos(dAngle);
        float sind = (float) Math.sin(dAngle);
        float cos = (float) Math.cos(currentAngle);
        float sin = (float) Math.sin(currentAngle);

        result[DX] = radius * (cos - cosd);
        result[DY] = radius * (sin - sind);

        return result;
    }

    //calculate point angle around rotation center
    public float getDAngle(float rotateX, float rotateY) {
        int quarter = getQuarter(rotateX, rotateY);

        float dAngle = 0;

        if (rotateY == 0) {
            if (rotateX > 0)
                dAngle = 0;
            else if (rotateX < 0)
                dAngle = toRadians(-180);
        } else {
            dAngle = (float) Math.atan((rotateX) / (rotateY));
            if (quarter == QUARTER_1 || quarter == QUARTER_2) {
                dAngle += toRadians(270);
            } else if (quarter == QUARTER_3 || quarter == QUARTER_4) {
                dAngle += toRadians(90);
            }
            dAngle = -dAngle;
        }
        return dAngle;
    }


    public int getQuarter(float rotateX, float rotateY) {
        int quarter = INVALID_QUARTER;
        if (rotateX >= 0 && rotateY >= 0)
            quarter = QUARTER_1;
        if (rotateX < 0 && rotateY >= 0)
            quarter = QUARTER_2;
        if (rotateX < 0 && rotateY < 0)
            quarter = QUARTER_3;
        if (rotateX >= 0 && rotateY < 0)
            quarter = QUARTER_4;
        return quarter;
    }

}

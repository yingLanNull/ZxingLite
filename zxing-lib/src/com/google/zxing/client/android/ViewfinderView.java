/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;
    private static final int CORNER_RECTANGLE_WIDTH = 5;
    private static final int CORNER_RECTANGLE_LENGTH = 20;
    private static final int RECTANGLE_EDGE_OFFSET = 10;
    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int laserColor;
    private final int resultPointColor;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;
    private int i = 0;
    private Rect mRect;
    private GradientDrawable mDrawable;
    private Drawable lineDrawable;
    public Rect cornerFrame;
    public Drawable scanBitmap;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();

        scanBitmap = getResources().getDrawable(R.drawable.qrcode_scan);
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        laserColor = resources.getColor(R.color.viewfinder_laser);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<ResultPoint>(5);
        lastPossibleResultPoints = null;
        mRect = new Rect();
        int left = getResources().getColor(R.color.green);
        int center = getResources().getColor(R.color.green);
        int right = getResources().getColor(R.color.green);
        lineDrawable = getResources().getDrawable(R.drawable.launcher_icon); //扫描时候，上下移动的图片
        mDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[] { left,
                left, center, right, right });


    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }

        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        drawCustomView(canvas, frame);
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

//        if (resultBitmap != null) {
//            // Draw the opaque result bitmap over the scanning rectangle
//            paint.setAlpha(CURRENT_POINT_OPACITY);
//            canvas.drawBitmap(resultBitmap, null, frame, paint);
//        } else {
//
//            // Draw a red "laser scanner" line through the middle to show decoding is active
//            paint.setColor(laserColor);
//            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
//            int middle = frame.height() / 2 + frame.top;
//            canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
//
//            float scaleX = frame.width() / (float) previewFrame.width();
//            float scaleY = frame.height() / (float) previewFrame.height();
//
//            List<ResultPoint> currentPossible = possibleResultPoints;
//            List<ResultPoint> currentLast = lastPossibleResultPoints;
//            int frameLeft = frame.left;
//            int frameTop = frame.top;
//            if (currentPossible.isEmpty()) {
//                lastPossibleResultPoints = null;
//            } else {
//                possibleResultPoints = new ArrayList<ResultPoint>(5);
//                lastPossibleResultPoints = currentPossible;
//                paint.setAlpha(CURRENT_POINT_OPACITY);
//                paint.setColor(resultPointColor);
//                synchronized (currentPossible) {
//                    for (ResultPoint point : currentPossible) {
//                        canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
//                                frameTop + (int) (point.getY() * scaleY),
//                                POINT_SIZE, paint);
//                    }
//                }
//            }
//            if (currentLast != null) {
//                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
//                paint.setColor(resultPointColor);
//                synchronized (currentLast) {
//                    float radius = POINT_SIZE / 2.0f;
//                    for (ResultPoint point : currentLast) {
//                        canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
//                                frameTop + (int) (point.getY() * scaleY),
//                                radius, paint);
//                    }
//                }
//            }
//
//            // Request another update at the animation interval, but only repaint the laser line,
//            // not the entire viewfinder mask.
//            postInvalidateDelayed(ANIMATION_DELAY,
//                    frame.left - POINT_SIZE,
//                    frame.top - POINT_SIZE,
//                    frame.right + POINT_SIZE,
//                    frame.bottom + POINT_SIZE);
//        }
        // 上下左右四个角
        drawCustomView(canvas, frame);
        // 扫描效果
        drawScanLine(canvas, frame);
    }

    /**
     * 自定义的扫描效果
     *
     * @param canvas
     * @param frameRect
     */
    public void drawCustomView(Canvas canvas, Rect frameRect) {
        paint.setColor(getResources().getColor(R.color.green));
        if (cornerFrame == null) {
            cornerFrame = new Rect(frameRect);
            cornerFrame.left = cornerFrame.left - RECTANGLE_EDGE_OFFSET;
            cornerFrame.right = cornerFrame.right + RECTANGLE_EDGE_OFFSET;
            cornerFrame.top = cornerFrame.top - RECTANGLE_EDGE_OFFSET;
            cornerFrame.bottom = cornerFrame.bottom + RECTANGLE_EDGE_OFFSET;
        }
        canvas.drawRect(cornerFrame.left, cornerFrame.top, cornerFrame.left + CORNER_RECTANGLE_LENGTH, cornerFrame.top + CORNER_RECTANGLE_WIDTH, paint);
        canvas.drawRect(cornerFrame.left, cornerFrame.top, cornerFrame.left + CORNER_RECTANGLE_WIDTH, cornerFrame.top + CORNER_RECTANGLE_LENGTH, paint);

        canvas.drawRect(cornerFrame.right - CORNER_RECTANGLE_LENGTH, cornerFrame.top, cornerFrame.right, cornerFrame.top + CORNER_RECTANGLE_WIDTH, paint);
        canvas.drawRect(cornerFrame.right - CORNER_RECTANGLE_WIDTH, cornerFrame.top, cornerFrame.right, cornerFrame.top + CORNER_RECTANGLE_LENGTH, paint);

        canvas.drawRect(cornerFrame.left, cornerFrame.bottom - CORNER_RECTANGLE_WIDTH, cornerFrame.left + CORNER_RECTANGLE_LENGTH, cornerFrame.bottom, paint);
        canvas.drawRect(cornerFrame.left, cornerFrame.bottom - CORNER_RECTANGLE_LENGTH, cornerFrame.left + CORNER_RECTANGLE_WIDTH, cornerFrame.bottom, paint);

        canvas.drawRect(cornerFrame.right - CORNER_RECTANGLE_LENGTH, cornerFrame.bottom - CORNER_RECTANGLE_WIDTH, cornerFrame.right, cornerFrame.bottom, paint);
        canvas.drawRect(cornerFrame.right - CORNER_RECTANGLE_WIDTH, cornerFrame.bottom - CORNER_RECTANGLE_LENGTH, cornerFrame.right, cornerFrame.bottom, paint);

    }

    /**
     * 扫描线
     *
     * @param canvas
     * @param frame
     */
    private void drawScanLine(Canvas canvas, Rect frame) {
        if (i + scanBitmap.getMinimumHeight() < frame.bottom - frame.top) {
            scanBitmap.setBounds(frame.left, frame.top + i, frame.right, frame.top + i + scanBitmap.getMinimumHeight());
            scanBitmap.draw(canvas);
            i += 2;
        } else {
            i = -scanBitmap.getMinimumHeight();
        }
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

}

package nl.xs4all.pebbe.fracland;

// TODO: pinch -> scale
// TODO: double move -> translate
// zie boek blz 215 en verder

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    private final float mDensity;

    public MyGLSurfaceView(Context context, Bundle savedInstanceState, float density) {
        super(context);

        mDensity = density;

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();
        mRenderer.restoreInstanceState(savedInstanceState);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void saveInstanceState(Bundle outState) {
        mRenderer.saveInstanceState(outState);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;
    private float mPreviousD;
    private long mPreviousT;
    private boolean mSingle = false;
    private boolean mDouble = false;

    @Override
    public boolean onTouchEvent(MotionEvent e) {


                int pointerCount = e.getPointerCount();

        float x0 = e.getX();
        float y0 = e.getY();
        float x1 = 0;
        float y1 = 0;

        if (pointerCount == 2) {
            x1 = e.getX(1);
            y1 = e.getY(1);
        }

        int action = e.getActionMasked();

        if (pointerCount == 1) {

            if (mSingle && action == MotionEvent.ACTION_MOVE) {

                float dx = x0 - mPreviousX;
                float dy = y0 - mPreviousY;

                if (Math.abs(dx) > Math.abs(dy)) {
                    mRenderer.setAngleH(
                            mRenderer.getAngleH() -
                                    dx * TOUCH_SCALE_FACTOR / mDensity / mRenderer.getZoom());  // = 180.0f / 320

                } else {
                    float a = mRenderer.getAngleV() + dy * TOUCH_SCALE_FACTOR / mDensity / mRenderer.getZoom();
                    if (a > 89.9f) {
                        a = 89.9f;
                    } else if (a < -30) {
                        a = -30;
                    }
                    mRenderer.setAngleV(a);
                }

                requestRender();

            } else if (action == MotionEvent.ACTION_DOWN) {
                // reset na dubbel tap
                long t = System.currentTimeMillis();
                if (t - mPreviousT < 200) {
                    mRenderer.reset();
                    requestRender();
                }
                mPreviousT = t;
            }
            mPreviousX = x0;
            mPreviousY = y0;

            mSingle = true;
            mDouble = false;

        } else if (pointerCount == 2) {

            float dx = x0 - x1;
            float dy = y0 - y1;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            float x = (x0 + x1) / 2;
            float y = (y0 + y1) / 2;

            if (action == MotionEvent.ACTION_MOVE && mDouble) {
                float d = dist - mPreviousD;
                float zoom = mRenderer.getZoom();
                float zm = zoom + d / 150 / mDensity;
                if (zm < 1) {
                    zm = 1;
                } else if (zm > 5) {
                    zm = 5;
                }
                mRenderer.setZoom(zm);

                float xo = mRenderer.getXO() + (x - mPreviousX) * TOUCH_SCALE_FACTOR / 90 / mDensity / zoom;
                float yo = mRenderer.getYO() + (y - mPreviousY) * TOUCH_SCALE_FACTOR / 90 / mDensity / zoom;
                if (xo < -1)
                    xo = -1;
                if (xo > 1)
                    xo = 1;
                if (yo < -1)
                    yo = -1;
                if (yo > 1)
                    yo = 1;
                mRenderer.setXO(xo);
                mRenderer.setYO(yo);

                requestRender();
            }

            mPreviousD = dist;
            mPreviousX = x;
            mPreviousY = y;

            mSingle = false;
            mDouble = true;

        } else {
            mSingle = false;
            mDouble = false;

        }

        return true;
    }

}

package nl.xs4all.pebbe.fracland;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView /* implements GestureDetector.OnDoubleTapListener */ {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                if (Math.abs(dx) > Math.abs(dy)) {
                    mRenderer.setAngleH(
                            mRenderer.getAngleH() -
                                    dx * TOUCH_SCALE_FACTOR);  // = 180.0f / 320

                } else {
                    float a = mRenderer.getAngleV() + dy * TOUCH_SCALE_FACTOR;
                    if (a > 89) {
                        a = 89;
                    } else if (a < -89) {
                        a = -89;
                    }
                    mRenderer.setAngleV(a);
                }

                requestRender();
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}

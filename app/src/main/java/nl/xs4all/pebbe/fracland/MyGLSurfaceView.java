package nl.xs4all.pebbe.fracland;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context, Bundle savedInstanceState) {
        super(context);

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

    public void scroll(float dx, float dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            mRenderer.setAngleH(
                    mRenderer.getAngleH() -
                            dx * TOUCH_SCALE_FACTOR);  // = 180.0f / 320
        } else {
            float a = mRenderer.getAngleV() + dy * TOUCH_SCALE_FACTOR;
            if (a > 89.9f) {
                a = 89.9f;
            } else if (a < -30) {
                a = -30;
            }
            mRenderer.setAngleV(a);
        }
        requestRender();
    }

    public void reset() {
        mRenderer.reset();
        requestRender();
    }

    public void scale(float f) {
        mRenderer.scale(f);
        requestRender();
    }

    public void move(float x, float y) {
        int pos[] = new int[2];
        getLocationOnScreen(pos);
        mRenderer.move(x - pos[0], y - pos[1]);
        requestRender();
    }
}

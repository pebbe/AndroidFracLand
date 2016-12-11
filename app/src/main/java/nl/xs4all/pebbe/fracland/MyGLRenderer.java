package nl.xs4all.pebbe.fracland;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private final static String angelHState = "nl.xs4all.pebbe.fracland.ANGLEH";
    private final static String angelVState = "nl.xs4all.pebbe.fracland.ANGLEV";
    private final static String seedState = "nl.xs4all.pebbe.fracland.SEED";

    private Pyramid pyramid;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float mAngleH;
    private float mAngleV;
    private long mSeed;

    private void init() {
        MyLog.i("BEGIN MyGLRenderer.init");
        mAngleH = -5;
        mAngleV = 20;
        mSeed = System.currentTimeMillis();
        MyLog.i("END MyGLRenderer.init");
    }

    public void reset() {
        MyLog.i("BEGIN MyGLRenderer.reset");
        init();
        pyramid.init(mSeed);
        MyLog.i("END MyGLRenderer.reset");
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        MyLog.i("BEGIN MyGLRenderer.restoreInstanceState");
        init();
        if (savedInstanceState != null) {
            mAngleH = savedInstanceState.getFloat(angelHState, mAngleH);
            mAngleV = savedInstanceState.getFloat(angelVState, mAngleV);
            mSeed = savedInstanceState.getLong(seedState, mSeed);
        }
        MyLog.i("END MyGLRenderer.restoreInstanceState " + mAngleH + " " + mAngleV + " " + mSeed);
    }

    public void saveInstanceState(Bundle outState) {
        MyLog.i("BEGIN MyGLRenderer.saveInstanceState " + mAngleH + " " + mAngleV + " " + mSeed);
        outState.putFloat(angelHState, mAngleH);
        outState.putFloat(angelVState, mAngleV);
        outState.putLong(seedState, mSeed);
        MyLog.i("END MyGLRenderer.saveInstanceState");
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        MyLog.i("BEGIN MyGLRenderer.onSurfaceCreated");
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // enable face culling feature
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        // nodig als objecten niet convex zijn
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        pyramid = new Pyramid();
        pyramid.init(mSeed);
        MyLog.i("END MyGLRenderer.onSurfaceCreated");
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        MyLog.i("BEGIN MyGLRenderer.onDrawFrame");

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)

        float h = mAngleH / 180 * (float) PI;
        float v = mAngleV / 180 * (float) PI;

        Matrix.setLookAtM(mViewMatrix, 0,
                30 * (float) (sin(h) * cos(v)), 30 * (float) sin(v), 30 * (float) (cos(h) * cos(v)),
                //(float)(sin(h)*cos(v)), (float)sin(v), (float)(cos(h)*cos(v)),
                0, 0, 0,
                0, 1, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        pyramid.draw(mMVPMatrix);
        MyLog.i("END MyGLRenderer.onDrawFrame");
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        MyLog.i("BEGIN MyGLRenderer.onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);

        float ratio = ((float) width) / (float) height;

        float xmul;
        float ymul;
        if (ratio > 1.0f) {
            xmul = 1.15f * ratio;
            ymul = 1.15f;
        } else {
            xmul = 1.15f;
            ymul = 1.15f / ratio;
        }


        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -xmul, xmul, -ymul, ymul, 28, 32);
        MyLog.i("END MyGLRenderer.onSurfaceChanged");
    }

    public static int loadShader(int type, String shaderCode) {
        MyLog.i("BEGIN MyGLRenderer.loadShader");

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        checkGlError("glShaderSource");
        GLES20.glCompileShader(shader);
        checkGlError("glCompileShader");

        MyLog.i("END MyGLRenderer.loadShader");
        return shader;
    }

    public float getAngleH() {
        MyLog.i("BEGIN/END MyGLRenderer.getAngleH");
        return mAngleH;
    }

    public float getAngleV() {
        MyLog.i("BEGIN/END MyGLRenderer.getAngleV");
        return mAngleV;
    }

    public void setAngleH(float angle) {
        MyLog.i("BEGIN MyGLRenderer.setAngleH");
        mAngleH = angle;
        MyLog.i("END MyGLRenderer.setAngleH");
    }

    public void setAngleV(float angle) {
        MyLog.i("BEGIN MyGLRenderer.setAngleV");
        mAngleV = angle;
        MyLog.i("END MyGLRenderer.setAngleV");
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("MyGLRenderer", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

}

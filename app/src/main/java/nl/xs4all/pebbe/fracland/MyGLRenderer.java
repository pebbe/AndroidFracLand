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

    private FracLand fracland;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float mAngleH;
    private float mAngleV;
    private long mSeed;

    private void init() {
        mAngleH = -5;
        mAngleV = 20;
        mSeed = System.currentTimeMillis();
    }

    public void reset() {
        init();
        fracland.init(mSeed);
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        init();
        if (savedInstanceState != null) {
            mAngleH = savedInstanceState.getFloat(angelHState, mAngleH);
            mAngleV = savedInstanceState.getFloat(angelVState, mAngleV);
            mSeed = savedInstanceState.getLong(seedState, mSeed);
        }
    }

    public void saveInstanceState(Bundle outState) {
        outState.putFloat(angelHState, mAngleH);
        outState.putFloat(angelVState, mAngleV);
        outState.putLong(seedState, mSeed);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // enable face culling feature
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        // nodig als objecten niet convex zijn
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        fracland = new FracLand();
        fracland.init(mSeed);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

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

        fracland.draw(mMVPMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
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
    }

    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        checkGlError("glShaderSource");
        GLES20.glCompileShader(shader);
        checkGlError("glCompileShader");

        return shader;
    }

    public float getAngleH() {
        return mAngleH;
    }

    public float getAngleV() {
        return mAngleV;
    }

    public void setAngleH(float angle) {
        mAngleH = angle;
    }

    public void setAngleV(float angle) {
        mAngleV = angle;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("MyGLRenderer", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

}

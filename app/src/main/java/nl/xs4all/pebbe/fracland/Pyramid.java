package nl.xs4all.pebbe.fracland;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Pyramid {

    private static final String TAG = "PYRAMID";

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colorBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMatrixHandle;

    private final String vertexShaderCode = "" +
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec3 vertexColor;" +
            "varying vec3 color;" +
            "void main() {" +
            "    gl_Position = uMVPMatrix * vPosition;" +
            "    color = vertexColor;" +
            "}";

    private final String fragmentShaderCode = "" +
            "precision mediump float;" +
            "varying vec3 color;" +
            "void main() {" +
            "    gl_FragColor = vec4(color, 1.0);" +
            "}";

    static final int COORDS_PER_VERTEX = 3;
    static float triCoords[] = {
            // rood : linksvoor
            0, 1, 0,   // A
            -1, 0, -1, // B
            0, 0, 1,   // C

            // groen : rechtsvoor
            0, 1, 0,   // A
            0, 0, 1,   // C
            1, 0, -1,  // D

            // geel: achter
            0, 1, 0,   // A
            1, 0, -1,  // D
            -1, 0, -1, // B

            // blau: onder
            -1, 0, -1, // B
            1, 0, -1,  // D
            0, 0, 1,   // C
    };
    private final int vertexCount = triCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static final int COLORS_PER_VERTEX = 3;
    static float triColors[] = {
            1, .5f, .5f,
            .5f, 0, 0,
            .5f, 0, 0,

            .5f, 1, .5f,
            0, .5f, 0,
            0, .5f, 0,

            1, 1, .5f,
            .5f, .5f, 0,
            .5f, .5f, 0,

            0, 0, 1,
            0, 0, 1,
            0, 0, 1
    };
    private final int colorCount = triColors.length / COLORS_PER_VERTEX;
    private final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per vertex

    public Pyramid() {
        Log.i(TAG, "BEGIN Pyramid");

        ByteBuffer bb = ByteBuffer.allocateDirect(triCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triCoords);
        vertexBuffer.position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(triColors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(triColors);
        colorBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        MyGLRenderer.checkGlError("glAttachShader vertexShader");
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        MyGLRenderer.checkGlError("glAttachShader fragmentShader");
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
        MyGLRenderer.checkGlError("glLinkProgram");

        Log.i(TAG, "END Pyramid");

    }

    public void draw(float[] mvpMatrix) {
        Log.i(TAG, "BEGIN draw");

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        MyGLRenderer.checkGlError("glUseProgram");

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        MyGLRenderer.checkGlError("glGetAttribLocation vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        MyGLRenderer.checkGlError("glEnableVertexAttribArray vPosition");
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        MyGLRenderer.checkGlError("glVertexAttribPointer vPosition");

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vertexColor");
        MyGLRenderer.checkGlError("glGetAttribLocation vertexColor");
        GLES20.glEnableVertexAttribArray(mColorHandle);
        MyGLRenderer.checkGlError("glEnableVertexAttribArray vertexColor");
        GLES20.glVertexAttribPointer(
                mColorHandle, COLORS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                colorStride, colorBuffer);
        MyGLRenderer.checkGlError("glVertexAttribPointer vertexColor");


        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv uMVPMatrix");

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        MyGLRenderer.checkGlError("glDrawArrays");

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(mColorHandle);
        MyGLRenderer.checkGlError("glDisableVertexAttribArray mColorHandle");
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        MyGLRenderer.checkGlError("glDisableVertexAttribArray mPositionHandle");

        Log.i(TAG, "END draw");
    }
}

package nl.xs4all.pebbe.fracland;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class Pyramid {

    private static final String TAG = "PYRAMID";

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
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
            // linksvoor
            0, 1, 0,   // A
            -1, -.3f, -.5774f, // B
            0, -.3f, 1.1547f,   // C

            // rechtsvoor
            0, 1, 0,   // A
            0, -.3f, 1.1547f,   // C
            1, -.3f, -.5774f,  // D

            // achter
            0, 1, 0,   // A
            1, -.3f, -.5774f,  // D
            -1, -.3f, -.5774f, // B

            // onder
            -1, -.3f, -.5774f, // B
            1, -.3f, -.5774f,  // D
            0, -.3f, 1.1547f,   // C
    };
    private final int vertexCount = triCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static final int COLORS_PER_VERTEX = 3;
    static float triColors[] = new float[36];
    private final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per vertex

    public void init(long seed) {
        MyLog.i("BEGIN Pyramid.init " + seed);
        Random rnd = new Random();
        rnd.setSeed(seed);
        float p1 = 1;
        float p2 = .5f;
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                p1 = .3f;
                p2 = .3f;
            }
            float r = rnd.nextFloat();
            float g = rnd.nextFloat();
            float b = rnd.nextFloat();
            triColors[9 * i + 0] = r * p1;
            triColors[9 * i + 3] = r * p2;
            triColors[9 * i + 6] = r * p2;
            triColors[9 * i + 1] = g * p1;
            triColors[9 * i + 4] = g * p2;
            triColors[9 * i + 7] = g * p2;
            triColors[9 * i + 2] = b * p1;
            triColors[9 * i + 5] = b * p2;
            triColors[9 * i + 8] = b * p2;
        }
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
        MyLog.i("END Pyramid.init");
    }

    public Pyramid() {
        MyLog.i("BEGIN Pyramid.Pyramid");
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
        MyLog.i("END Pyramid.Pyramid");
    }

    public void draw(float[] mvpMatrix) {
        MyLog.i("BEGIN Pyramid.draw");
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
        MyLog.i("END Pyramid.draw");
    }
}

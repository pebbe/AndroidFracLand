package nl.xs4all.pebbe.fracland;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class FracLand {

    private static final int LVL = 64; // gehele macht van 2

    // lichtbron:
    private static final float X = 2;
    private static final float Y = -4;
    private static final float Z = 10;
    private static float lenXYZ = 10.954451f; // sqrt(X^2 + Y^2 + Z^2)
    private static float SUB = -.2f;
    private static float DOWN = .5f;

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMatrixHandle;

    private final static float hSqrt3 = .8660254f;

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
    static float coords[] = new float[9 * (LVL * LVL + 6 * LVL + 2)];
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static final int COLORS_PER_VERTEX = 3;
    static float colors[] = new float[9 * (LVL * LVL + 6 * LVL + 2)];
    private final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per vertex

    private int vertexCount;

    private float z[][] = new float[LVL + 1][LVL + 1];

    private float toX(int x, int y) {
        return 2 * (((float) x) / ((float) LVL) + .5f * ((float) y) / (float) LVL) - 1;
    }

    private float toY(int x, int y) {
        return 2 * (.8660254f * ((float) y) / (float) LVL) - .57735f;
    }

    void driehoek(int x1, int y1, float z1, int x2, int y2, float z2, int x3, int y3, float z3, float r, float g, float b) {
        coords[3 * vertexCount] = toX(x1, y1);
        coords[3 * vertexCount + 1] = z1 - DOWN;
        coords[3 * vertexCount + 2] = toY(x1, y1);
        colors[3 * vertexCount] = r;
        colors[3 * vertexCount + 1] = g;
        colors[3 * vertexCount + 2] = b;
        vertexCount++;

        coords[3 * vertexCount] = toX(x2, y2);
        coords[3 * vertexCount + 1] = z2 - DOWN;
        coords[3 * vertexCount + 2] = toY(x2, y2);
        colors[3 * vertexCount] = r;
        colors[3 * vertexCount + 1] = g;
        colors[3 * vertexCount + 2] = b;
        vertexCount++;

        coords[3 * vertexCount] = toX(x3, y3);
        coords[3 * vertexCount + 1] = z3 - DOWN;
        coords[3 * vertexCount + 2] = toY(x3, y3);
        colors[3 * vertexCount] = r;
        colors[3 * vertexCount + 1] = g;
        colors[3 * vertexCount + 2] = b;
        vertexCount++;
    }

    private float cos1(int xi, int yi) {
        float xn, yn, zn, ax, ay, az, bx, by, bz;

        ax = hSqrt3;
        ay = .5f;
        az = LVL*(z[xi + 1][yi] - z[xi][yi]);

        bx = 0;
        by = 1;
        bz = LVL*(z[xi][yi + 1] - z[xi][yi]);

        // normaalvector:
        xn = ay * bz - az * by;
        yn = az * bx - ax * bz;
        zn = ax * by - ay * bx;

        return (xn * X + yn * Y + zn * Z) / (float)Math.sqrt(xn * xn + yn * yn + zn * zn) / lenXYZ;
    }

    private float cos2(int xi, int yi) {
        float xn, yn, zn, ax, ay, az, bx, by, bz;

        ax = hSqrt3;
        ay = -.5f;
        az = LVL*(z[xi+1][yi] - z[xi][yi+1]);

        bx = hSqrt3;
        by = .5f;
        bz = LVL*(z[xi+1][yi+1] - z[xi][yi+1]);

        // normaalvector:
        xn = ay*bz - az*by;
        yn = az*bx - ax*bz;
        zn = ax*by - ay*bx;

        return (xn*X + yn*Y + zn*Z) / (float)Math.sqrt(xn*xn+yn*yn+zn*zn) / lenXYZ;
}

    private float min0(float f) {
        if (f < 0) {
            return 0;
        }
        return f;
    }

    public void init(long seed) {

        Random rnd = new Random();
        rnd.setSeed(seed);

        vertexCount = 0;

        driehoek(
                0, 0, 0,
                0, LVL, 0,
                LVL, 0, 0,
                0, .3f, .7f);

        driehoek(
                0, 0, SUB,
                LVL, 0, SUB,
                0, LVL, SUB,
                .2f, .2f, .2f);

        z[0][0] = rnd.nextFloat() * 2 - 1;
        z[LVL][0] = rnd.nextFloat() * 2 - 1;
        z[0][LVL] = rnd.nextFloat() * 2 - 1;

        for (int step = LVL; step > 1; step /= 2) {
            float mul = ((float) step) / (float) LVL;
            for (int x = 0; x < LVL; x += step) {
                for (int y = 0; x + y < LVL; y += step) {
                    z[x + step / 2][y] = (z[x][y] + z[x + step][y]) / 2 + (rnd.nextFloat() * 2 - 1) * mul;
                    z[x][y + step / 2] = (z[x][y] + z[x][y + step]) / 2 + (rnd.nextFloat() * 2 - 1) * mul;
                    z[x + step / 2][y + step / 2] = (z[x][y + step] + z[x + step][y]) / 2 + (rnd.nextFloat() * 2 - 1) * mul;
                }
            }
        }

        for (int x = 0; x < LVL; x++) {
            for (int y = 0; x + y < LVL; y++) {
                if (z[x][y] > 0 || z[x][y + 1] > 0 || z[x + 1][y] > 0) {
                    float c = cos1(x, y);
                    driehoek(
                            x, y, z[x][y],
                            x, y + 1, z[x][y + 1],
                            x + 1, y, z[x + 1][y],
                            0, .5f * c + .5f, 0);
                }
                if (x + y < LVL - 1 && (z[x][y + 1] > 0 || z[x + 1][y] > 0 || z[x + 1][y + 1] > 0)) {
                    float c = cos2(x, y);
                    driehoek(
                            x, y + 1, z[x][y + 1],
                            x + 1, y + 1, z[x + 1][y + 1],
                            x + 1, y, z[x + 1][y],
                            0, .5f * c + .5f, 0);
                }
            }
            driehoek(
                    0, x, min0(z[0][x]),
                    0, x, SUB,
                    0, x + 1, min0(z[0][x + 1]),
                    .5f, .5f, .5f);
            driehoek(
                    0, x, SUB,
                    0, x + 1, SUB,
                    0, x + 1, min0(z[0][x + 1]),
                    .5f, .5f, .5f);
            driehoek(
                    x, 0, SUB,
                    x, 0, min0(z[x][0]),
                    x + 1, 0, min0(z[x + 1][0]),
                    .3f, .3f, .3f);
            driehoek(
                    x + 1, 0, SUB,
                    x, 0, SUB,
                    x + 1, 0, min0(z[x + 1][0]),
                    .3f, .3f, .3f);
            driehoek(
                    x, LVL - x, min0(z[x][LVL - x]),
                    x, LVL - x, SUB,
                    x + 1, LVL - x - 1, min0(z[x + 1][LVL - x - 1]),
                    .4f, .4f, .4f);
            driehoek(
                    x, LVL - x, SUB,
                    x + 1, LVL - x - 1, SUB,
                    x + 1, LVL - x - 1, min0(z[x + 1][LVL - x - 1]),
                    .4f, .4f, .4f);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(9 * (LVL * LVL + 6 * LVL + 2) * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(9 * (LVL * LVL + 6 * LVL + 2) * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }

    public FracLand() {
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
    }

    public void draw(float[] mvpMatrix) {
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
    }
}

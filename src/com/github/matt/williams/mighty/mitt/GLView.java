package com.github.matt.williams.mighty.mitt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.AttributeSet;

import com.github.matt.williams.android.gl.FragmentShader;
import com.github.matt.williams.android.gl.Program;
import com.github.matt.williams.android.gl.Projection;
import com.github.matt.williams.android.gl.Utils;
import com.github.matt.williams.android.gl.VertexShader;
import com.github.matt.williams.mighty.mitt.HandTrackerService.Listener;

public class GLView extends GLSurfaceView implements Renderer, Listener {

    private static final String TAG = "GLView";
    private Program mProgram;
    private Projection mProjection;
    private float[] mCubeVertices;
    private float[] mCubeNormals;
    private static float[] COLOR_MATRIX;
    private FingerMap mFingerMap = new FingerMap();
    private final Map<Finger, float[]> mFingerAngles = new HashMap<Finger, float[]>();

    static {
        COLOR_MATRIX = new float[] {1.0f, 0.0f, 0.0f, 0.0f,
                                    0.0f, 1.0f, 0.0f, 0.0f,
                                    0.0f, 0.0f, 1.0f, 0.0f,
                                    0.0f, 0.0f, 0.0f, 1.0f};
    }


    public GLView(Context context) {
        super(context);
        initialize();
    }

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void setFingerMap(FingerMap fingerMap) {
        mFingerMap = fingerMap;
    }

    @Override
    public void onFingerUpdate(Finger finger, float[] angles) {
        android.util.Log.e(TAG, finger + " - " + angles[1] * 180 / Math.PI);
        mFingerAngles.put(finger, angles);
    }

    private void initialize() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 0, 16, 0);
        setRenderer(this);

        mCubeVertices = loadJsonData("cube.vertices.json");
        mCubeNormals = loadJsonData("cube.normals.json");
    }

    private float[] loadJsonData(String filename) {
        float[] vertices = null;
        try {
            JSONArray jsonArray = (JSONArray)new JSONTokener(JSONUtils.readStream(getContext().getAssets().open(filename))).nextValue();
            vertices = new float[jsonArray.length()];
            for (int ii = 0; ii < vertices.length; ii++) {
                vertices[ii] = (float)jsonArray.getDouble(ii);
            }
        } catch (JSONException e) {
            android.util.Log.e(TAG, "Failed to parse " + filename, e);
        } catch (IOException e) {
            android.util.Log.e(TAG, "Failed to read " + filename, e);
        }
        return vertices;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgram = new Program(new VertexShader(getResources().getString(R.string.vertexShader)),
                               new FragmentShader(getResources().getString(R.string.fragmentShader)));

        mProjection = new Projection(40.0f, 60.0f, 0.0f);
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        mProjection.setRotationMatrix(matrix);

        GLES20.glDisable(GLES20.GL_BLEND);
        Utils.checkErrors("glDisable(GLES20.GL_BLEND)");

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        Utils.checkErrors("glEnable(GLES20.GL_DEPTH_TEST)");
        GLES20.glDepthMask(true);
        Utils.checkErrors("glDepthMask");

        GLES20.glClearColor(0, 0, 0, 0);
        Utils.checkErrors("glClearColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        float[] matrix = new float[16];

        System.arraycopy(mProjection.getViewMatrix(), 0, matrix, 0, 16);
        Matrix.translateM(matrix, 0, 0, -3.0f, -10.0f);
        Matrix.scaleM(matrix, 0, 5.0f, 4.0f, 1.0f);

        drawCube(matrix, 0.5f, 0.5f, 0.5f, 0.5f);

        System.arraycopy(mProjection.getViewMatrix(), 0, matrix, 0, 16);
        Matrix.translateM(matrix, 0, -2.0f, 0, -10.0f);
        drawFinger(matrix, Finger.Thumb, 1.0f);

        System.arraycopy(mProjection.getViewMatrix(), 0, matrix, 0, 16);
        Matrix.translateM(matrix, 0, -1.0f, 0, -10.0f);
        drawFinger(matrix, Finger.Fore, 2.0f);

        System.arraycopy(mProjection.getViewMatrix(), 0, matrix, 0, 16);
        Matrix.translateM(matrix, 0, 0.0f, 0, -10.0f);
        drawFinger(matrix, Finger.Middle, 2.5f);

        System.arraycopy(mProjection.getViewMatrix(), 0, matrix, 0, 16);
        Matrix.translateM(matrix, 0, 1.0f, 0, -10.0f);
        drawFinger(matrix, Finger.Ring, 2.0f);

        System.arraycopy(mProjection.getViewMatrix(), 0, matrix, 0, 16);
        Matrix.translateM(matrix, 0, 2.0f, 0, -10.0f);
        drawFinger(matrix, Finger.Little, 1.5f);
    }

    private void drawFinger(float[] matrix, Finger finger, float length)
    {
        if (mFingerMap.getAddress(finger) != null) {
            float[] maybeAngles = mFingerAngles.get(finger);
            float[] angles = (maybeAngles != null) ? new float[] {maybeAngles[0] * 180 / (float)Math.PI, maybeAngles[1] * 180 / (float)Math.PI, maybeAngles[2] * 180 / (float)Math.PI} : new float[] {0, 0, 0};
            float[] angles1 = new float[] {angles[0], (angles[1] < 90) ? 0 : angles[1] - 90, angles[2]};
            float[] angles2 = new float[] {0, (angles[1] < 90) ? angles[1] : 90, 0};
            drawFinger(matrix, angles1, angles2, length, 0.8f, 0, 0, 0.5f);
        } else {
            drawFinger(matrix, new float[] {0, 0, 0}, new float[] {0, 0, 0}, length, 0.5f, 0.5f, 0.5f, 0.5f);
        }
    };

    private void drawFinger(float[] matrix, float[] angles1, float[] angles2, float length, float r, float g, float b, float mix) {
        float[] boneMatrix = new float[16];
        Matrix.translateM(matrix, 0, 0, length/2 - 0.75f, 0);
        Matrix.translateM(matrix, 0, 0, -length/2 - 0.25f, 0);
        Matrix.rotateM(matrix, 0, angles1[2], 0, 0, -1);
        Matrix.rotateM(matrix, 0, angles1[0], 0, -1, 0);
        Matrix.rotateM(matrix, 0, angles1[1], -1, 0, 0);
        Matrix.translateM(matrix, 0, 0, length / 2 + 0.25f, 0);
        System.arraycopy(matrix, 0, boneMatrix, 0, 16);
        Matrix.scaleM(matrix, 0, 0.9f, length, 0.9f);
        drawCube(matrix, r, g, b, mix);

        System.arraycopy(boneMatrix, 0, matrix, 0, 16);
        Matrix.translateM(matrix, 0, 0, length + 0.25f, 0);
        Matrix.translateM(matrix, 0, 0, -length / 2 - 0.25f, 0);
        Matrix.rotateM(matrix, 0, angles2[2], 0, 0, -1);
        Matrix.rotateM(matrix, 0, angles2[0], 0, -1, 0);
        Matrix.rotateM(matrix, 0, angles2[1], -1, 0, 0);
        Matrix.translateM(matrix, 0, 0, length / 2 + 0.25f, 0);
        Matrix.scaleM(matrix, 0, 0.9f, length, 0.9f);
        drawCube(matrix, r, g, b, mix);
    }

    private void drawCube(float[] matrix, float r, float g, float b, float mix) {
        mProgram.use();
        mProgram.setUniform("matrix", matrix);
        mProgram.setUniform("baseColor", r, g, b);
        mProgram.setUniform("colorMix", mix);
        mProgram.setUniform("colorMatrix", COLOR_MATRIX);
        mProgram.setVertexAttrib("vertex", mCubeVertices, 3);
        mProgram.setVertexAttrib("normal", mCubeNormals, 3);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mCubeVertices.length / 3);
    }
}

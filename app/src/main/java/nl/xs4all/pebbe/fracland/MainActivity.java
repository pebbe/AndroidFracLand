package nl.xs4all.pebbe.fracland;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private MyGLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyLog.i("BEGIN MainActivity.onCreate");
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new MyGLSurfaceView(this, savedInstanceState);
        setContentView(mGLView);
        MyLog.i("END MainActivity.onCreate");
    }

    @Override
    protected void onPause() {
        MyLog.i("BEGIN MainActivity.onPause");
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
        MyLog.i("END MainActivity.onPause");
    }

    @Override
    protected void onResume() {
        MyLog.i("BEGIN MainActivity.onResume");
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
        MyLog.i("END MainActivity.onResume");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MyLog.i("BEGIN MainActivity.onSaveInstanceState");
        super.onSaveInstanceState(outState);
        mGLView.saveInstanceState(outState);
        MyLog.i("END MainActivity.onSaveInstanceState");
    }

}

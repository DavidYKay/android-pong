package com.kongentertainment.pong;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

public class PongActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Create our Preview view and set it as the content of our
    // Activity
    //mGLSurfaceView = new CubeSurfaceView(this);
    mGLSurfaceView = new PongSurfaceView(this);
    setContentView(mGLSurfaceView);
    mGLSurfaceView.requestFocus();
    mGLSurfaceView.setFocusableInTouchMode(true);
  }

  @Override
  protected void onResume() {
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    super.onResume();
    mGLSurfaceView.onResume();
  }

  @Override
  protected void onPause() {
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    super.onPause();
    mGLSurfaceView.onPause();
  }

  private GLSurfaceView mGLSurfaceView;
}

/**
 * Holds the game state.
 */
class GameThread extends Thread {
  private static final String TAG = "GameThread";
  private GameSurfaceView mPongSurfaceView;
  private boolean mRun;
  private long mStartTime;

  public GameThread(GameSurfaceView pongSurfaceView) {
    mPongSurfaceView = pongSurfaceView;
    mRun = true;
  }

  static final int FRAMES_PER_SECOND = 25;
  static final int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;

  /**
   * getTickCount() returns the current number of milliseconds 
   * that have elapsed since the system was started
   */
  private long getTickCount() {
    long currentTime = System.currentTimeMillis();
    return currentTime - mStartTime;
  }

  @Override
  public void run() {
    Log.v(TAG, "run()");
    mStartTime = System.currentTimeMillis(); 

    long nextGameTick = getTickCount();
    long sleepTime = 0;

    while (mRun) {
      Log.v(TAG, "run loop");
      // Update game state.
      mPongSurfaceView.updateGameState();
      // Render.
      mPongSurfaceView.requestRender();

      // Calculate time until next tick.
      nextGameTick += SKIP_TICKS;
      sleepTime = nextGameTick - getTickCount();
      if( sleepTime >= 0 ) {
        try {
          // Sleep until next tick.
          Thread.sleep( sleepTime );
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      else {
        // Shit, we are running behind!
        Log.e(TAG, "Running behind!");
      }
    }
    Log.v(TAG, "FINISHED run loop");
  }

  public void setRunning(boolean running) {
    mRun = running;
  }
}

/**
 * A surface and a renderer for a game of pong.
 */
class PongSurfaceView extends GLSurfaceView implements GameSurfaceView {

  private static final String TAG = "PongSurfaceView";

  private Thread mThread;

  public PongSurfaceView(Context context) {
    super(context);
    mRenderer = new PongRenderer();
    setRenderer(mRenderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    mThread = new GameThread(this);
    mThread.start();
  }

  public void updateGameState() {
    Log.v(TAG, "onTick");
    // mRenderer.mAngleX += 1;
    requestRender();
  }

  @Override public boolean onTrackballEvent(MotionEvent e) {
    //mRenderer.mAngleX += e.getX() * TRACKBALL_SCALE_FACTOR;
    //mRenderer.mAngleY += e.getY() * TRACKBALL_SCALE_FACTOR;

    requestRender();
    return true;
  }

  @Override public boolean onTouchEvent(MotionEvent e) {
    float x = e.getX();
    float y = e.getY();
    //switch (e.getAction()) {
    //case MotionEvent.ACTION_MOVE:
    //  float dx = x - mPreviousX;
    //  float dy = y - mPreviousY;
    //  mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;
    //  mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;
    //  requestRender();
    //}
    //mPreviousX = x;
    //mPreviousY = y;
    return true;
  }

  private class PongRenderer implements GLSurfaceView.Renderer {
    public PongRenderer() {
      mPaddles[0] = new Paddle(Player.HUMAN, new Point(10, 210));
      mPaddles[1] = new Paddle(Player.COMPUTER, new Point(10, 10));
    }

    public void onDrawFrame(GL10 gl) {
      /*
       * Usually, the first thing one might want to do is to clear
       * the screen. The most efficient way of doing this is to use
       * glClear().
       */

      gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

      /*
       * Now we're ready to draw some 3D objects
       */

      gl.glMatrixMode(GL10.GL_MODELVIEW);
      for (Paddle paddle : mPaddles) {
        gl.glLoadIdentity();

        // Position the paddle based on its current location.
        Point location = paddle.getLocation();

        if (paddle.getPlayer() == Player.COMPUTER) {
          gl.glTranslatef(0.0f, 1.0f, -3.0f);
        } else {
          gl.glTranslatef(0.0f, -1.0f, -3.0f);
        }
        //gl.glTranslatef(location.x, location.y, -3.0f);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        paddle.draw(gl);
      }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
      gl.glViewport(0, 0, width, height);

      /*
       * Set our projection matrix. This doesn't have to be done
       * each time we draw, but usually a new projection needs to
       * be set when the viewport is resized.
       */

      float ratio = (float) width / height;
      gl.glMatrixMode(GL10.GL_PROJECTION);
      gl.glLoadIdentity();
      gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
      /*
       * By default, OpenGL enables features that improve quality
       * but reduce performance. One might want to tweak that
       * especially on software renderer.
       */
      gl.glDisable(GL10.GL_DITHER);

      /*
       * Some one-time OpenGL initialization can be made here
       * probably based on features of this particular context
       */
      gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_FASTEST);


      gl.glClearColor(1,1,1,1);
      gl.glEnable(GL10.GL_CULL_FACE);
      gl.glShadeModel(GL10.GL_SMOOTH);
      gl.glEnable(GL10.GL_DEPTH_TEST);
    }

    private Paddle[] mPaddles = new Paddle[2];
  }


  private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
  private final float TRACKBALL_SCALE_FACTOR = 36.0f;

  private PongRenderer mRenderer;
}

/**
 * Implement a simple rotation control.
 *
 */
class CubeSurfaceView extends GLSurfaceView implements GameSurfaceView {

  private static final String TAG = "CubeSurfaceView";

  private Thread mThread;

  public CubeSurfaceView(Context context) {
    super(context);
    mRenderer = new CubeRenderer();
    setRenderer(mRenderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    mThread = new GameThread(this);
    //mThread.setRunning(true);
    mThread.start();
  }

  public void updateGameState() {
    Log.v(TAG, "onTick");
    mRenderer.mAngleX += 1;
    requestRender();
  }

  @Override public boolean onTrackballEvent(MotionEvent e) {
    mRenderer.mAngleX += e.getX() * TRACKBALL_SCALE_FACTOR;
    mRenderer.mAngleY += e.getY() * TRACKBALL_SCALE_FACTOR;
    requestRender();
    return true;
  }

  @Override public boolean onTouchEvent(MotionEvent e) {
    float x = e.getX();
    float y = e.getY();
    switch (e.getAction()) {
    case MotionEvent.ACTION_MOVE:
      float dx = x - mPreviousX;
      float dy = y - mPreviousY;
      mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;
      mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;
      requestRender();
    }
    mPreviousX = x;
    mPreviousY = y;
    return true;
  }

  /**
   * Render a cube.
   */
  private class CubeRenderer implements GLSurfaceView.Renderer {
    public CubeRenderer() {
      mCube = new Cube();
    }

    public void onDrawFrame(GL10 gl) {
      /*
       * Usually, the first thing one might want to do is to clear
       * the screen. The most efficient way of doing this is to use
       * glClear().
       */

      gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

      /*
       * Now we're ready to draw some 3D objects
       */

      gl.glMatrixMode(GL10.GL_MODELVIEW);
      gl.glLoadIdentity();
      gl.glTranslatef(0, 0, -3.0f);
      gl.glRotatef(mAngleX, 0, 1, 0);
      gl.glRotatef(mAngleY, 1, 0, 0);

      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
      gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

      mCube.draw(gl);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
      gl.glViewport(0, 0, width, height);

      /*
       * Set our projection matrix. This doesn't have to be done
       * each time we draw, but usually a new projection needs to
       * be set when the viewport is resized.
       */

      float ratio = (float) width / height;
      gl.glMatrixMode(GL10.GL_PROJECTION);
      gl.glLoadIdentity();
      gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
      /*
       * By default, OpenGL enables features that improve quality
       * but reduce performance. One might want to tweak that
       * especially on software renderer.
       */
      gl.glDisable(GL10.GL_DITHER);

      /*
       * Some one-time OpenGL initialization can be made here
       * probably based on features of this particular context
       */
      gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
          GL10.GL_FASTEST);


      gl.glClearColor(1,1,1,1);
      gl.glEnable(GL10.GL_CULL_FACE);
      gl.glShadeModel(GL10.GL_SMOOTH);
      gl.glEnable(GL10.GL_DEPTH_TEST);
    }
    private Cube mCube;
    public float mAngleX;
    public float mAngleY;
  }


  private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
  private final float TRACKBALL_SCALE_FACTOR = 36.0f;

  private CubeRenderer mRenderer;

  private float mPreviousX;
  private float mPreviousY;
}

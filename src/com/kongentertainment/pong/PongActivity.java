package com.kongentertainment.pong;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
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

  static final int FRAMES_PER_SECOND = 30;
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
      //Log.v(TAG, "run loop");
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
    //Log.v(TAG, "FINISHED run loop");
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

  public static class Rules {
    
    public static final float INITIAL_SPEED = 0.2f;

    public static final PointF ORIGIN = new PointF(0.0f, 0.0f);
    public static final float MAX_X =  5.0f;
    public static final float MIN_X = -5.0f;
    public static final float MAX_Y =  8.0f;
    public static final float MIN_Y = -8.0f;
    public static final PointF MAX_LOCATION = new PointF(MAX_X, MAX_Y);
    public static final PointF MIN_LOCATION = new PointF(MIN_X, MIN_Y);

  }

  private Thread mThread;

  private PongBall mBall;
  private Paddle[] mPaddles = new Paddle[2];

  public PongSurfaceView(Context context) {
    super(context);

    // Init model.
    mBall = new PongBall();
    mBall.hit(Rules.INITIAL_SPEED, Rules.INITIAL_SPEED);
    mPaddles[0] = new Paddle(Player.HUMAN    , new PointF(-1.0f, -7.0f));
    mPaddles[1] = new Paddle(Player.COMPUTER , new PointF(-1.0f,  7.0f));

    // Init renderer.
    mRenderer = new PongRenderer();
    setRenderer(mRenderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    mRenderer.addRenderable(mPaddles[0]);
    mRenderer.addRenderable(mPaddles[1]);
    mRenderer.addRenderable(mBall);

    // Init thread.
    mThread = new GameThread(this);
    mThread.start();

  }

  public void updateGameState() {
    //Log.v(TAG, "updateGameState");
    // For now, just animate the paddles back and forth.

    final float PADDLE_SPEED = 0.1f;
    final float DIST_THRESHOLD = 0.04f;

    // Move human paddle.
    Paddle human = mPaddles[0];
    human.move(mSpeedX);
    mSpeedX = 0.0f;
    
    // Move computer paddle.
    Paddle computer = mPaddles[1];

    // This logic makes the computer paddle chase the human.
    // Its speed is limited by PADDLE_SPEED.
    float delta = human.getLocation().x - computer.getLocation().x;
    float speed = 0.0f;
    if (
        (delta > 0 && delta < 0 + DIST_THRESHOLD) || 
        (delta < 0 && delta > 0 - DIST_THRESHOLD)
    ) {
      // NOP. We're close enough.
      Log.v(TAG, String.format(
            "Computer staying STILL. Delta: %f", delta));
    } else {
      // Looks like we're moving.
      if (delta > 0.0f) {
        speed = Math.min(
            delta, PADDLE_SPEED);
      } else {
        speed = Math.max(
            delta, -PADDLE_SPEED);
        Log.v(TAG, String.format(
            "Computer moving LEFT. Speed: %f", speed));
      }
      // Enact the move.
      computer.move(speed);
    }

    // Resolve ball hits.

    // Resolve ball-on-wall hits.
    if (mBall.getLocation().x > Rules.MAX_X ||
        mBall.getLocation().x < Rules.MIN_X
    ) {
      mBall.hit(-mBall.getVector().x,
                 mBall.getVector().y);
    }
    if (mBall.getLocation().y > Rules.MAX_Y ||
        mBall.getLocation().y < Rules.MIN_Y
    ) {
      mBall.hit(mBall.getVector().x,
                -mBall.getVector().y);
    }
    
    // Move the ball.
    mBall.move();

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
    Log.v(TAG, String.format("onTouchEvent: (%f, %f)", x, y));
    switch (e.getAction()) {
    case MotionEvent.ACTION_DOWN:
      // If we're putting our hand down, we're at zero speed AND zero previous position.
      mSpeedX    = 0.0f;
    case MotionEvent.ACTION_UP:
      // If we're raising our finger, we want to keep speed, but not last position.
      mPreviousX = 0.0f;
      break;
    case MotionEvent.ACTION_MOVE:
      float dx = x - mPreviousX;
      mSpeedX = dx * TOUCH_SCALE_FACTOR;
      Log.v(TAG, String.format("DX: %f. Speed: %f", dx, mSpeedX));
      //mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;

      //float dy = y - mPreviousY;
      //mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;
      //requestRender();
      break;
    }
    mPreviousX = x;
    //mPreviousY = y;
    return true;
  }

  private class PongRenderer implements GLSurfaceView.Renderer {

    private static final float ZOOM_FACTOR = -10.0f;

    private ArrayList<Renderable> mRenderables = new ArrayList<Renderable>();
    
    public PongRenderer() {
    }
    
    public boolean addRenderable(Renderable renderable) {
      return mRenderables.add(renderable);
    }
    
    public boolean Renderable(Renderable renderable) {
      return mRenderables.remove(renderable);
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
      // Save current matrix for later use.
      gl.glPushMatrix();
      for (Renderable renderable : mRenderables) {
        gl.glLoadIdentity();

        // Position the paddle based on its current location.
        PointF location = renderable.getLocation();
        gl.glTranslatef(location.x, location.y, ZOOM_FACTOR);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        renderable.draw(gl);
      }
      gl.glPopMatrix();
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

  }

  //private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
  private final float TOUCH_SCALE_FACTOR = 180.0f / (320 * 50);
  private final float TRACKBALL_SCALE_FACTOR = 36.0f;

  private PongRenderer mRenderer;
  /** Previous touch position. */
  private float mPreviousX;
  /** Speed of the human touch. */
  private float mSpeedX;
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

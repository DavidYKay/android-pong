package com.kongentertainment.pong;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;
import android.util.Log;

import com.kongentertainment.pong.PongSurfaceView.Rules;

public class PongBall extends PongObject {
 
  private static final String TAG = "PongBall";
  
  private static final float SCALING_FACTOR = 0.3f;
  private static final PointF DEFAULT_SIZE = new PointF(SCALING_FACTOR, SCALING_FACTOR);
  
  private Cube mCube;
  private PointF mVector;

  public PongBall() {
    this(Rules.getOrigin());
  }

  public PongBall(PointF location) {
    mLocation = location;
    mCube = new Cube();
    mVector = new PointF(0.0f, 0.0f);
    mSize = DEFAULT_SIZE;
  }

  /**
   * Default hit. Flips speed vector both ways.
   */
  public void hit() {
    mVector.negate();
  }

  /**
   * Depending on where we got hit from, we react differently.
   */ 
  public void hit(float newX, float newY) {
    mVector = new PointF(newX, newY);

    // TODO: Add collision prediction.

    // TODO: Add collision detection.

    //mVector.negate();
    
  }

  /**
   * Continue moving at previous speed.
   */
  public void move() {
    Log.v(TAG, "ball moving at speed: " +  mVector);
    mLocation.offset(mVector.x, mVector.y);
  }
  
  public void teleport(PointF location) {
    mLocation = location;
  }
  
  public PointF getVector() {
    return mVector;
  }

  public void draw(GL10 gl) {
    gl.glScalef(SCALING_FACTOR, SCALING_FACTOR, SCALING_FACTOR);
    // TODO Remember to scale down the model.
    mCube.draw(gl);
  }
}

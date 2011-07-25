package com.kongentertainment.pong;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;
import android.util.Log;

import com.kongentertainment.pong.PongSurfaceView.Rules;

public class PongBall extends PongObject {
 
  private static final String TAG = "PongBall";
  
  private Cube mCube;
  private PointF mVector;

  public PongBall() {
    this(Rules.ORIGIN);
  }

  public PongBall(PointF location) {
    mLocation = location;
    mCube = new Cube();
    mVector = new PointF(0.0f, 0.0f);
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
  
  public PointF getVector() {
    return mVector;
  }

  public void draw(GL10 gl) {
    // TODO Remember to scale down the model.
    mCube.draw(gl);
  }
}
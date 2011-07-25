package com.kongentertainment.pong;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;

/**
 * Abstract class to hold position and model code.
 * @author dk
 *
 */
public abstract class PongObject {

  protected PointF mLocation;

  public PointF getLocation() {
    return mLocation;
  }
  
  public void draw(GL10 gl) {
    // Stub method.
  }

}

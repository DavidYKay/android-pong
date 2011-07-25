package com.kongentertainment.pong;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;

/**
 * Abstract class to hold position and model code.
 * @author dk
 *
 */
public abstract class PongObject implements Renderable {

  protected PointF mLocation;
  protected PointF mSize;

  /**
   * Location refers to the center of mass.
   */
  public PointF getLocation() {
    return mLocation;
  }
  
  /**
   * Size refers to the height, width of the given object.
   */
  public PointF getSize() {
    return mSize;
  }

  /**
   * The bounding box is the colliding area of the given object.
   */
  public BoundingBox getBoundingBox() {
    // Bounding box
    return new BoundingBox(
      mLocation.x - (mSize.x / 2),
      mLocation.y - (mSize.y / 2),
      mLocation.x + (mSize.x / 2),
      mLocation.y + (mSize.y / 2)
    );
  }
  
  /**
   * This is where the magic happens. OpenGL rendering action.
   */
  public void draw(GL10 gl) {
    // Stub method.
  }

}

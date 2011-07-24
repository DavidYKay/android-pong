package com.kongentertainment.pong;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;

public class Paddle {

  private static final float MAX_X =  5.0f;
  private static final float MIN_X = -5.0f;
  private static final float MAX_Y =  1.0f;
  private static final float MIN_Y = -1.0f;

  private static final PointF MAX_LOCATION = new PointF(MAX_X, MAX_Y);
  private static final PointF MIN_LOCATION = new PointF(MIN_X, MIN_Y);

  private Player mPlayer;
  private PointF mLocation;
  
  //TODO Remove this and handle rendering ourselves.
  private Rectangle mRectangle;

  public Paddle(Player player, PointF location) {
    mPlayer = player;
    mLocation = location;
    mRectangle = new Rectangle();
  }

  /**
   * Move laterally by delta.
   */
  public void move(float delta) {
    
    mLocation.offset(delta, 0);

    if (mLocation.x < MIN_LOCATION.x) {
      mLocation.x = MIN_LOCATION.x;
    }
    if (mLocation.x > MAX_LOCATION.x) {
      mLocation.x = MAX_LOCATION.x;
    }

  }

  public Player getPlayer() {
    return mPlayer;
  }
  public PointF getLocation() {
    return mLocation;
  }

  public void draw(GL10 gl) {
    // TODO Implement this ourselves.
    mRectangle.draw(gl);
  }

}

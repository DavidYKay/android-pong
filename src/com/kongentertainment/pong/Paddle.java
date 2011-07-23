package com.kongentertainment.pong;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;

public class Paddle {

  private static final PointF MAX_LOCATION = new PointF(2.0f, 1.0f);
  private static final PointF MIN_LOCATION = new PointF(-2.0f, -1.0f);

  private Player mPlayer;
  private PointF mLocation;
  
  //TODO Remove this and handle rendering ourselves.
  private Cube mCube;

  public Paddle(Player player, PointF location) {
    mPlayer = player;
    mLocation = location;
    mCube = new Cube();
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
    //if (mLocation.y < MIN_LOCATION.y) {
    //  mLocation.y = MIN_LOCATION.y;
    //}
    //if (mLocation.y > MAX_LOCATION.y) {
    //  mLocation.y = MAX_LOCATION.y;
    //}

  }

  public Player getPlayer() {
    return mPlayer;
  }
  public PointF getLocation() {
    return mLocation;
  }

  public void draw(GL10 gl) {
    // TODO Implement this ourselves.
    mCube.draw(gl);
  }

}

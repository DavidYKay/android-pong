package com.kongentertainment.pong;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Point;

public class Paddle {

  private Player mPlayer;
  private Point mLocation;
  
  //TODO Remove this and handle rendering ourselves.
  private Cube mCube;

  public Paddle(Player player, Point location) {
    mPlayer = player;
    mLocation = location;
    mCube = new Cube();
  }

  /**
   * Move laterally by delta.
   */
  public void move(int delta) {
    mLocation.offset(delta, 0);
  }

  public Player getPlayer() {
    return mPlayer;
  }
  public Point getLocation() {
    return mLocation;
  }

  public void draw(GL10 gl) {
    // TODO Implement this ourselves.
    mCube.draw(gl);
  }

}

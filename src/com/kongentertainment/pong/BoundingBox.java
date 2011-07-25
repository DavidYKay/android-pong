package com.kongentertainment.pong;

public class BoundingBox {

  public final float minX;
  public final float minY;
  public final float maxX;
  public final float maxY;

  public BoundingBox(
      float minX,
      float minY,
      float maxX,
      float maxY
      ) {
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
  }

  public boolean collides(BoundingBox other) {
    return collides(
        other.minX,
        other.minY,
        other.maxX,
        other.maxY
        );
  }

  public boolean collides(float minX, float minY, float maxX, float maxY) {

    if(maxX <= this.minX || minX >= this.maxX) {
      return false;
    }
    if(maxY <= this.minY || minY >= this.maxY) {
      return false;
    }
    return true ;
  }

}

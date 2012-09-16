/*
 * @(#)Angle.java 1.0
 * Copyright (C) 2004,2005 Aravind Alwan
 *
 * This file is part of CRANKS.
 *
 * CRANKS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * CRANKS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. A copy of the GNU
 * General Public License is provided in LICENSE.txt, which is located
 * in the installation directory of CRANKS.
 *
 * You may also obtain a copy of the GNU General Public License
 * by writing to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA  02110-1301  USA
 */

package cranks.geom;

public class Angle extends GeometricalObject {
  
  public static final long serialVersionUID = -8711849589184961600L;

  private double ang; //value of the angle in radians
  public final static double LEAST_COUNT = 1e-5; //resolution limit in radians
  public final static int TYPE = 1;

  //Default Constructor
  public Angle() {
    ang = 0.0;
    visible = false;
    super.TYPE = TYPE;
  }

  //Create an angle object out of a given angle value in radians
  public Angle(double a) {
    ang=a;
    normalizeAngle();
    visible = false;
    super.TYPE = TYPE;
  }

  //Set value to a double angle value in radians
  public void setAngle(double p) {
    ang = p;
    normalizeAngle();
  }

  //Set value to that of another angle
  public void setAngle(Angle p) {
    setAngle(p.getAngle());
  }

  //Return angle value
  public double getAngle() {
    return ang;
  }

  //Used to maintain value between 0 and 2*PI
  public void normalizeAngle() {
    while (ang >= 2*Math.PI) ang -= 2*Math.PI;
    while (ang < 0 ) ang += 2*Math.PI;
    if (ang < Angle.LEAST_COUNT) ang = 0;
  }

  public boolean canIntersect(GeometricalObject object2) {
    return false;
  }

  public void move(Point translation, Angle rotation, Point fixedPoint) {
    this.setAngle(add(rotation));
  }

  //Addition
  public Angle add(Angle a) {
    Angle temp = new Angle(ang + a.getAngle());
    return temp;
  }

  //Subtraction
  public Angle sub(Angle a) {
    Angle temp = new Angle(ang - a.getAngle());
    return temp;
  }

  //Multiplication
  public Angle mul(double a) {
    Angle temp = new Angle(ang * a);
    return temp;
  }

  //Increment operator
  public double inc(double ang1) {
    ang += ang1;
    normalizeAngle();
    return ang;
  }

  //Decrement operator
  public double dec(double ang1) {
    ang -= ang1;
    normalizeAngle();
    return ang;
  }

  //Compare with angle2
  public boolean equalTo(Angle angle2) {
    return equalTo(angle2.getAngle());
  }

  //Compare with a fixed angle value
  public boolean equalTo(double angleValue) {
    return (Math.abs(getAngle() - angleValue) < Angle.LEAST_COUNT);
  }

}
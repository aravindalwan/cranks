/*
 * @(#)Link.java 1.0
 * Copyright (C) 2005 Aravind Alwan
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

package cranks.mech;
import cranks.geom.Line;
import cranks.geom.Point;
import cranks.geom.Angle;

public class Link implements java.io.Serializable {
  
  private static final long serialVersionUID = 5584663720822886022L;
  
  private int number; // Stores the Link Number, used in Chains
  private Joint preJoint;
  private Joint postJoint;
  private double length; // Stores the Length parameter of the link
  private boolean ternary;
  private double ternaryLength; // Stores the ternary length of the Link
  private Angle ternaryAngle; // Stores The angle at which it is ternary

  public Link() {
    this(0);
  }

  public Link(double Length) {
    number = -1;
    length = Length;
    ternary = false;
    ternaryLength = 0;
    ternaryAngle = new Angle();
  }

  public void setLength(double Length) {
    length = Length;
  }

  public double getLength() {
    return length;
  }

  public void setNumber(int n) {
    number = n;
  }

  public int getNumber() {
    return number;
  }

  public void setTernary(boolean Ternary, double tLength, Angle tAngle) {
    ternary = Ternary;
    ternaryLength = tLength;
    ternaryAngle.setAngle(tAngle.getAngle());
  }

  public boolean isTernary() {
    return ternary;
  }
  
  public double getTernaryLength() {
    return ternaryLength;
  }
  
  public Angle getTernaryAngle() {
    return ternaryAngle;
  }

  public Point getCouplerPoint() {
    if (ternary) {
      Angle slopeOfLink = (new Line(preJoint, postJoint)).getSlope();
      return (new Line(getPreviousJoint(), slopeOfLink.add(ternaryAngle),
                        ternaryLength)).getEnd();
    }
    else
      return null;
  }

  public void setPreviousJoint(Joint newJoint) {
    preJoint = newJoint;
  }

  public void setPostJoint(Joint newJoint) {
    postJoint = newJoint;
  }

  public Joint getPreviousJoint() {
    return preJoint;
  }

  public Joint getPostJoint() {
    return postJoint;
  }

  public void connectTo(Link existingLink) {
    Joint newJoint = new Joint(existingLink, this);
    existingLink.setPostJoint(newJoint);
    setPreviousJoint(newJoint);
  }

}
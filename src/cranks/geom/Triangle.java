/*
 * @(#)Triangle.java 1.0
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Vector;

public class Triangle extends GeometricalObject {
  
  public static final long serialVersionUID =  8233347832741587157L;
  
  private Line side1;
  private Line side2;
  private Line side3;

  public final static int TYPE = 5;

  //Constructors for the triangle class

  //Default Constructor.
  public Triangle() {
    this(new Point(), new Point(0,1), new Point(1,0));
  }

  //Constructs a triangle, given a line and a point
  //Note: Does not check if the point lies on the line or not
  public Triangle(Point Point1, Line Line1) {
    super();
    side1 = Line1;
    side2 = new Line(Line1.getEnd(), Point1);
    side3 = new Line(Point1, Line1.getStart());
    color = Color.black;
    visible = true;
    super.TYPE = TYPE;
  }

  //Constructs a triangle, given three points
  //Add lines to Object Vector<GeometricalObject> after circle construction
  //Note: Does not check if the points are collinear
  public Triangle(Point P1, Point P2, Point P3) {
    super();
    side1 = new Line(P1, P2);
    side2 = new Line(P2, P3);
    side3 = new Line(P3, P1);
    color = Color.black;
    visible = true;
    super.TYPE = TYPE;
  }

  public Line[] getSides() {
    Line[] sides = new Line[3];
    sides[0] = side1;
    sides[1] = side2;
    sides[2] = side3;
    return sides;
  }

  public Point[] getVertices() {
    Point[] vertices = new Point[3];
    vertices[0] = side1.getStart();
    vertices[1] = side2.getStart();
    vertices[2] = side3.getStart();
    return vertices;
  }

  public void removeAssociations() {
    side1.removeAssociationWith(this);
    side2.removeAssociationWith(this);
    side3.removeAssociationWith(this);
  }

  public void addToObjects(Vector<GeometricalObject> Objects) {
    if (!(Objects.contains(this))) {
      side1.addToObjects(Objects);
      side1.associateWith(this);
      side2.addToObjects(Objects);
      side2.associateWith(this);
      side3.addToObjects(Objects);
      side3.associateWith(this);
      Objects.addElement(this);
      setNumber(Objects.size());
    }
  }

  public void removeFromObjects(Vector<GeometricalObject> Objects) {
    removeAssociations();
    if (Objects.contains(this))
      Objects.removeElement(this);
  }

  //checks to see if the given Object has an intersection with the triangle
  public boolean canIntersect(GeometricalObject object2) {
    return false;
  }

  public void move(Point Translation, Angle Rotation, Point FixedPoint) {
    side1.getStart().move(Translation, Rotation, FixedPoint);
    side2.getStart().move(Translation, Rotation, FixedPoint);
    side3.getStart().move(Translation, Rotation, FixedPoint);
  }

  public void draw(Graphics2D g2d) {
    if (isVisible()) {
      //Triangles are never drawn, as the points and lines would
      //already be drawn separately. Use this method only if the
      //triangle region is to be filled with some color
    }
  }

  public String toString() {
    return ("Triangle "+Integer.toString(getNumber()));
  }

}
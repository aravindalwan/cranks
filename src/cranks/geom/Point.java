/*
 * @(#)Point.java 1.0
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

public class Point extends GeometricalObject {
  
  public static final long serialVersionUID = 2053043539907946057L;
  
  private double x, y; //X and Y coordinates of the point
  //Associated objects
  private Vector<GeometricalObject> assocObjects = new
                                                    Vector<GeometricalObject>();
  //Number of each type of associated objects. A point can be associated with
  //three types of objects: lines, circles and triangles
  private int[] numObjects = new int[3];
  //Spatial resolution limit between two points
  public final static double LEAST_COUNT = 1e-3;
  //Distance upto which a click on the point is registered
  public final static double CLICK_RESOLUTION = 1;
  public final static int TYPE = 2;

  //Default Constructor
  public Point() {
    super();
    x = 0.0;
    y = 0.0;
    color = Color.black;
    visible = true;
    super.TYPE = TYPE;
  }

  //Construct using x & y co-ordinates
  public Point(double X, double Y) {
  	super();
    x = X;
  	y = Y;
  	color = Color.black;
    visible = true;
    super.TYPE = TYPE;
  }

  //Set c-ordinates
  public void setCoordinates(double m, double n) {
    x = m;
    y = n;
    for(int i = 0; i < assocObjects.size(); i++)
      if (assocObjects.elementAt(i).getType() == Line.TYPE)
        ((Line)assocObjects.elementAt(i)).setLengthAndSlope();
  }

  public void setColor() {
    if (assocObjects.isEmpty())
      setColor(Color.black);
    else {
      if (numObjects[0] > 0)
        setColor(Color.red);
      if (numObjects[1] > 0)
        setColor(Color.blue);
      if (numObjects[2] > 0)
        setColor(Color.magenta);
    }
  }

  public void associateWith(GeometricalObject object1) {
    if ((object1.getType()>=Line.TYPE) && (object1.getType()<=Triangle.TYPE)) {
      if (!(assocObjects.contains(object1))) {
        assocObjects.addElement(object1);
        numObjects[object1.getType() - Line.TYPE]++;
        setColor();
      }
    }
  }

  public void removeAssociationWith(GeometricalObject object1) {
    if ((object1.getType() >= Line.TYPE) && (object1.getType()<=Triangle.TYPE)) {
      if (assocObjects.contains(object1)) {
        assocObjects.remove(object1);
        numObjects[object1.getType() - Line.TYPE]--;
        setColor();
      }
    }
  }

  //Add this point to objects vector
  //Be careful to use only main obj Vector<GeometricalObject> or else this
  //point's number will not be set properly
  public void addToObjects(Vector<GeometricalObject> Objects) {
    if (!(Objects.contains(this))) {
      Objects.addElement(this);
      setNumber(Objects.size());
    }
  }

  public void removeFromObjects(Vector<GeometricalObject> Objects) {
    if (Objects.contains(this)) {
      Objects.removeElement(this);
      Vector<GeometricalObject> copyOfAssocObjects = new
                                                    Vector<GeometricalObject>();
      for (int i = 0; i<assocObjects.size(); i++)
        copyOfAssocObjects.add(assocObjects.elementAt(i));
      for (int i = 0; i<copyOfAssocObjects.size(); i++)
        copyOfAssocObjects.elementAt(i).removeFromObjects(Objects);
    }
  }

  //return X-co-ordinate
  public double getX() {
    return x;
  }

  //return Y-co-ordinate
  public double getY() {
    return y;
  }

  public Vector<GeometricalObject> getAssocObjects() {
    return assocObjects;
  }

  //To check if it can intersect with object2
  public boolean canIntersect(GeometricalObject object2) {
    if (object2.getType() == Line.TYPE) {
      if (equalTo(((Line)object2).getStart())) return true;
      Line line2 = new Line(this, ((Line)object2).getStart());
      return ((Line)object2).isParallel(line2);
    }
    if (object2.getType() == Circle.TYPE)
      return (Math.abs(distanceTo(((Circle)object2).getCentre()) -
               ((Circle)object2).getRadius()) < Point.LEAST_COUNT);
    else
    return false;
  }

  //To check if the mouse pointer is close enough to "click" an object
  public boolean canClick(GeometricalObject object2) {
    if (object2.getType() == Point.TYPE) {
      return (distanceTo((Point)object2) <= Point.CLICK_RESOLUTION);
    }
    if (object2.getType() == Line.TYPE) {
      return (Math.abs(distanceTo(((Line)object2).getStart()) +
                       distanceTo(((Line)object2).getEnd()) -
                       ((Line)object2).getLength()) <= Point.CLICK_RESOLUTION);
    }
    if (object2.getType() == Circle.TYPE)
      return (Math.abs(distanceTo(((Circle)object2).getCentre()) -
               ((Circle)object2).getRadius()) <= Point.CLICK_RESOLUTION);
    if (object2.getType() == Triangle.TYPE)
      return (canClick(((Triangle)object2).getSides()[0]) ||
              canClick(((Triangle)object2).getSides()[1]) ||
              canClick(((Triangle)object2).getSides()[2]));
    else
    return false;
  }

  //To check if two points are geometrically the same, within resolution limit
  public boolean equalTo(Point point2) {
    return (Math.sqrt(Math.pow(getX() - point2.getX(), 2) +
            Math.pow(getY() - point2.getY(), 2)) < Point.LEAST_COUNT);
  }

  //Returns distance to point2
  public double distanceTo(Point point2) {
    double distance = Math.sqrt(Math.pow(getX()-point2.getX(), 2) +
            Math.pow(getY()-point2.getY(), 2));
    if (distance < Point.LEAST_COUNT) distance = 0;
    return Math.abs(distance);
  }

  public void move(Point Translation, Angle Rotation, Point FixedPoint) {
    setCoordinates(getX()+Translation.getX(), getY()+Translation.getY());
    if (!Rotation.equalTo(0)) {
      if (!equalTo(FixedPoint)) {
        Line tempLine = new Line(FixedPoint, this);
        tempLine = new Line(FixedPoint, tempLine.getSlope().add(Rotation),
                            tempLine.getLength());
        setCoordinates(tempLine.getEnd().getX(), tempLine.getEnd().getY());
      }
    }
  }

  public void draw(Graphics2D g2d) {
    if (isVisible()) {
      g2d.setPaint(getColor());
      g2d.fill(new java.awt.geom.Ellipse2D.Double(getX() - 2, getY() - 2, 
                    4, 4));
      g2d.setColor(Color.black);
    }
  }

  public String toString() {
    return ("Point "+Integer.toString(getNumber()));
  }

  public String locationToString() {
    return ("(" + getX() + "," + getY() + ")");
  }

}//end of class point


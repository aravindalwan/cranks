/*
 * @(#)Line.java 1.0
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

/**
 * This class actually creates a line segment. There is no infinite line that 
 * is used anywhere. However, intersections are calculated as though the line
 * is infinite.
 */

public class Line extends GeometricalObject {
  
  public static final long serialVersionUID = 3345058273143466620L;
  
  public static final String PROP_LENGTH = "length";
  public static final String PROP_ANGLE = "angle";

  //variables that define the line. Slope angle is stored in radians
  private Point start;
  private Point end;
  private Angle slope;
  private double length;

  //Vector<GeometricalObject> of associated objects
  private Vector<GeometricalObject> assocObjects = new
                                                    Vector<GeometricalObject>();
  private int[] numObjects = new int[1];

  public final static int TYPE = 3;

  //Constructors for the line class

  //Default constructor
  public Line() {
    super();
    start = new Point();
    end = new Point (0,1);
    slope = new Angle();
    length = 0;
    setLengthAndSlope();
    color = Color.black;
    visible = true;
    super.TYPE = TYPE;
  }

  //Line connecting two points
  public Line(Point Start, Point End) {
    super();
    start = Start;
    end = End;
    slope = new Angle();
    length = 0;
    setLengthAndSlope();
    color = Color.black;
    visible = true;
    super.TYPE = TYPE;
  }

  //Line from Start to a point at distance Length such that the line
  //makes an angle Slope with the positive x-axis
  public Line(Point Start, Angle Slope, double Length) {
    super();
    start = Start;
    end = new Point(Start.getX()+Length*
      Math.cos(Slope.getAngle()), Start.getY()+
      Length*Math.sin(Slope.getAngle()));
    slope = Slope;
    length = Length;
    setLengthAndSlope();
    color = Color.black;
    visible = true;
    super.TYPE = TYPE;
  }

  //Sets Slope angle between 0 and PI
  public void setLengthAndSlope() {
    Angle oldSlope = getSlope();
    double oldLength = getLength();
    double slopeAngle = Math.atan2(
      (end.getY()-start.getY()), (end.getX()-start.getX()));
    slope = new Angle(slopeAngle);
    if (!slope.equalTo(oldSlope))
      propertySupport.firePropertyChange(PROP_ANGLE, oldSlope, slope);
    length = Math.abs(Math.sqrt(Math.pow(start.getX()-end.getX(), 2)+
                                Math.pow(start.getY()-end.getY(), 2)));
    if (length != oldLength)
      propertySupport.firePropertyChange(PROP_LENGTH, new Double(oldLength), 
                                                      new Double(length));
  }

  public void setColor() {
    if (assocObjects.isEmpty())
      setColor(Color.black);
    else
      if (numObjects[0] > 0)
        setColor(Color.magenta);
  }

  public void removeAssociations() {
    start.removeAssociationWith(this);
    end.removeAssociationWith(this);
  }

  public void associateWith(GeometricalObject object1) {
    if (!(assocObjects.contains(object1))) {
      if (object1.getType() == Triangle.TYPE) {
        numObjects[object1.getType() - Triangle.TYPE]++;
        assocObjects.add(object1);
        start.associateWith(object1);
        end.associateWith(object1);
      }
      setColor();
    }
  }

  public void removeAssociationWith(GeometricalObject object1) {
    if (assocObjects.contains(object1)) {
      assocObjects.remove(object1);
      if (object1.getType() == Triangle.TYPE) {
        numObjects[object1.getType() - Triangle.TYPE]--;
        start.removeAssociationWith(object1);
        end.removeAssociationWith(object1);
      }
      setColor();
    }
  }

  public void addToObjects(Vector<GeometricalObject> Objects) {
    if (!(Objects.contains(this))) {
      start.addToObjects(Objects);
      end.addToObjects(Objects);
      start.associateWith(this);
      end.associateWith(this);
      Objects.addElement(this);
      setNumber(Objects.size());
    }
  }

  public void removeFromObjects(Vector<GeometricalObject> Objects) {
    removeAssociations();
    if (Objects.contains(this)) {
      Objects.remove(this);
      Vector<GeometricalObject> copyOfAssocObjects = new
                                                    Vector<GeometricalObject>();
      for (int i = 0; i<assocObjects.size(); i++)
        copyOfAssocObjects.addElement(assocObjects.elementAt(i));
      for (int i = 0; i<copyOfAssocObjects.size(); i++) {
        GeometricalObject o = copyOfAssocObjects.elementAt(i);
        if (o.getType() == Triangle.TYPE)
          o.removeFromObjects(Objects);
      }
    }
  }

  //returns distance from Start to End
  public double getLength() {
    return length;
  }

  //returns Starting point
  public Point getStart() {
    return start;
  }

  //returns End point
  public Point getEnd() {
    return end;
  }

  //returns slope angle
  public Angle getSlope() {
    return slope;
  }

  public Vector<GeometricalObject> getAssocObjects() {
    return assocObjects;
  }

  //checks to see if the given Object has an intersection with the line
  public boolean canIntersect(GeometricalObject object2) {
   if (object2.getType() == Line.TYPE)
      return !(getSlope().equalTo(((Line) object2).getSlope()) ||
          getSlope().add(new Angle(Math.PI)).equalTo(((Line)object2).getSlope())
          ); // to see if two lines are parallel or not
   else
   if (object2.getType() == Circle.TYPE) {
      Angle slopePerpToLine = getSlope().add(new Angle(Math.PI/2));
      Line radiusPerpToLine = new Line(((Circle)object2).getCentre(),
                                        slopePerpToLine, 1);
      //need not call CanIntersect()
      Point intersection = intersect(radiusPerpToLine)[0];
      return (((Circle)object2).getCentre().distanceTo(intersection) -
              ((Circle)object2).getRadius() <= Point.LEAST_COUNT);
   }
   else
      return false;
  }

  //gives intersection point with another object
  public Point[] intersect(GeometricalObject object2) {
    Point[] intersectionPoints = new Point[2];

    if (canIntersect(object2)) { //Intersect pt exists iff objects can intersect

      if (object2.getType() == Line.TYPE) { //is a line
        Line line2 = (Line)object2;
        intersectionPoints[1] = null; //No second intersection point
        if (this.getSlope().equalTo(Math.PI/2)) { //line1 (this) is || to y-axis
          intersectionPoints[0] = new Point(this.getStart().getX(),
                Math.tan(line2.getSlope().getAngle())*
                (this.getStart().getX() - line2.getStart().getX()) +
                line2.getStart().getY());
        }
        else if (line2.getSlope().equalTo(Math.PI/2)) { // line2 || to y-axis
          intersectionPoints[0] = new Point(line2.getStart().getX(),
                Math.tan(this.getSlope().getAngle())*
                (line2.getStart().getX() - this.getStart().getX()) +
                this.getStart().getY());
        }
        else { //slopes of both lines are finite
          double slope1 = Math.tan(this.getSlope().getAngle());
          double slope2 = Math.tan(line2.getSlope().getAngle());
          double intercept1 = getStart().getY() - slope1*getStart().getX();
          double intercept2 = line2.getStart().getY() -
                              slope2*line2.getStart().getX();
          double x = (intercept2 - intercept1)/(slope1 - slope2);
          double y = slope1*x + intercept1;
          intersectionPoints[0] = new Point(x,y);
        }
      }

      if (object2.getType() == Circle.TYPE) { //is a circle
        Circle circle1 = (Circle)object2;
        if (circle1.getCentre().canIntersect(this)) {
          intersectionPoints[0] = (new Line(circle1.getCentre(),
             getSlope().add(new Angle(Math.PI)), circle1.getRadius())).getEnd();
          intersectionPoints[1] = (new Line(circle1.getCentre(),
             getSlope(), circle1.getRadius())).getEnd();
        }
        else {
          Angle slopePerpToLine = getSlope().add(new Angle(Math.PI/2));
          Line radiusPerpToLine = new Line(circle1.getCentre(),slopePerpToLine,1);
          Point intersection =
              intersect(radiusPerpToLine)[0]; //needn't call CanIntersect()
          slopePerpToLine =
                      (new Line(circle1.getCentre(),intersection)).getSlope();
          double perpDistance = circle1.getCentre().distanceTo(intersection);
          Angle ang = new Angle(Math.acos(perpDistance/circle1.getRadius()));
          if (perpDistance >= circle1.getRadius())
            ang = new Angle(0);
          intersectionPoints[0] = (new Line(circle1.getCentre(),
                  slopePerpToLine.add(ang), circle1.getRadius())).getEnd();
          intersectionPoints[1] = (new Line(circle1.getCentre(),
                  slopePerpToLine.sub(ang), circle1.getRadius())).getEnd();
        }
      }
    }
    else {
      // This should not happen
      // CanIntersect() should be called before this function is called
    }
  return intersectionPoints;
  }
  
  //Returns the mid-point of the line
  public Point midPoint() {
    return new Point((start.getX()+end.getX())/2, (start.getY()+end.getY())/2);
  }

  public boolean isParallel(Line line2) {
    return ((getSlope().equalTo(line2.getSlope()))||
            (getSlope().equalTo(line2.getSlope().add(new Angle(Math.PI)))));
  }

  public void move(Point Translation, Angle Rotation, Point FixedPoint) {
    start.move(Translation, Rotation, FixedPoint);
    end.move(Translation, Rotation, FixedPoint);
    slope.move(Translation, Rotation, FixedPoint);
  }
  
  public void propertyChange(java.beans.PropertyChangeEvent pce) {
    if (((pce.getSource() instanceof Line) && 
            (pce.getPropertyName() == Line.PROP_LENGTH)) || 
        ((pce.getSource() instanceof Circle) &&
            (pce.getPropertyName() == Circle.PROP_RADIUS)))
      end.setCoordinates(start.getX() + 
          ((Double)pce.getNewValue()).doubleValue()*Math.cos(slope.getAngle()), 
        start.getY() + 
          ((Double)pce.getNewValue()).doubleValue()*Math.sin(slope.getAngle()));
    if ((pce.getSource() instanceof Line)&&(pce.getPropertyName()==Line.PROP_ANGLE))
      end.setCoordinates(start.getX() + 
          length*Math.cos(((Angle)pce.getNewValue()).getAngle()), 
        start.getY() + length*Math.sin(((Angle)pce.getNewValue()).getAngle()));
  }

  public void draw(Graphics2D g2d) {
    if (isVisible()) {
      g2d.setColor(getColor());
      g2d.draw(new java.awt.geom.Line2D.Double(start.getX(), start.getY(),
                        end.getX(), end.getY()));
      g2d.setColor(Color.black);
    }
  }

  public String toString() {
    return ("Line "+Integer.toString(getNumber()));
  }

}
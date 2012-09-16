/*
 * @(#)Circle.java 1.0
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

import cranks.geom.*;

public class Circle extends GeometricalObject {
  
  public static final long serialVersionUID = -4574528465407551734L;
  
	private Point centre;
	private double radius;

  public static final int TYPE = 4;
  public static final String PROP_RADIUS = "radius";

	//Constructors for the circle class

	//Default Constructor
	public Circle() {
    super();
    centre = new Point();
    radius = 1;
    color = Color.black;
    visible = true;
    super.TYPE = TYPE;
	}

	//Constructs a circle, given the centre point and the radius
	public Circle(Point Centre, double Radius) {
    super();
    centre = Centre;
		radius = Radius;
    color = Color.black;
    visible = true;
    super.TYPE = TYPE;
	}

	//Constructs a circle, given three points lying on the circle
	//Note: The circle must EXIST or else this will fail!!!
	public Circle(Point P1, Point P2, Point P3) {
    super();
    Line p1p2 = new Line(P1, P2);
		Line p2p3 = new Line(P2, P3);
		Point mp_p1p2 = p1p2.midPoint(); //Mid-point of P1P2
    Point mp_p2p3 = p2p3.midPoint(); //Mid-point of P1P2
		Angle newSlope1 = p1p2.getSlope().add(new Angle(Math.PI/2));
    Angle newSlope2 = p2p3.getSlope().add(new Angle(Math.PI/2));
    Line perp_p1p2 = new Line(mp_p1p2, newSlope1, 1);
    Line perp_p2p3 = new Line(mp_p2p3, newSlope2, 1);
    if (perp_p1p2.canIntersect(perp_p2p3))
        centre = perp_p1p2.intersect(perp_p2p3)[0];
    else {
        //Give error message saying "no circle exists" - not implemented
    }
    radius = centre.distanceTo(P1);
    color = Color.black;
    visible = true;
    super.TYPE = TYPE;
	}

	public Point getCentre() {
	  return centre;
	}

	public double getRadius() {
	  return radius;
	}

	public void setCentre(Point Centre) {
	  centre = Centre;
	}

	public void setRadius(double Radius) {
    double oldRadius = radius;
	  radius = Radius;
    if (radius != oldRadius)
      propertySupport.firePropertyChange(PROP_RADIUS, new Double(oldRadius), 
                                                      new Double(radius));
	}

  public void removeAssociations() {
    centre.removeAssociationWith(this);
  }

  public void addToObjects(Vector<GeometricalObject> Objects) {
    if (!(Objects.contains(this))) {
      centre.addToObjects(Objects);
      centre.associateWith(this);
      Objects.addElement(this);
      setNumber(Objects.size());
    }
  }

  public void removeFromObjects(Vector<GeometricalObject> Objects) {
    removeAssociations();
    if (Objects.contains(this))
      Objects.removeElement(this);
  }

  //checks to see if the given Object has an intersection with the circle
  public boolean canIntersect(GeometricalObject object2) {
    if (object2.getType() == Line.TYPE) {
      Angle slopePerpToLine = ((Line)object2).getSlope().add(
                                                          new Angle(Math.PI/2));
      Line radiusPerpToLine = new Line(getCentre(), slopePerpToLine, 1);
      //no need to call CanIntersect()
      Point intersection = radiusPerpToLine.intersect((Line)object2)[0];
      return (getCentre().distanceTo(intersection) - getRadius() <=
                                        Point.LEAST_COUNT);
    }
    else
    if (object2.getType() == Circle.TYPE)
      if (getCentre().equalTo(((Circle)object2).getCentre()))
        return false;
      else
        return ((getCentre().distanceTo(((Circle)object2).getCentre()) -
               getRadius() - ((Circle)object2).getRadius() <= Point.LEAST_COUNT)
        && (Math.abs(getRadius() - ((Circle)object2).getRadius()) - getCentre().
            distanceTo(((Circle)object2).getCentre()) <= Point.LEAST_COUNT));
    else
      return false;
  }

  //gives intersection points with another object
  public Point[] intersect(GeometricalObject object2) {
    Point[] intersectionPoints = new Point[2];
    if (canIntersect(object2)) { // Intersection pts exist iff objects intersect

      if (object2.getType() == Line.TYPE) { //is a line
        Line line1 = (Line)object2;
        if (getCentre().canIntersect(line1)) {
          intersectionPoints[0] = (new Line(getCentre(), line1.getSlope().add(
                                   new Angle(Math.PI)), getRadius())).getEnd();
          intersectionPoints[1] = (new Line(getCentre(),
                  line1.getSlope(), getRadius())).getEnd();
        }
        else {
          Angle slopePerpToLine = line1.getSlope().add(new Angle(Math.PI/2));
          Line radiusPerpToLine = new Line(getCentre(), slopePerpToLine, 1);
          //no need to call CanIntersect()
          Point intersection = radiusPerpToLine.intersect(line1)[0];
          slopePerpToLine = (new Line(getCentre(), intersection)).getSlope();
          double perpDistance = getCentre().distanceTo(intersection);
          Angle ang = new Angle(Math.acos(perpDistance/getRadius()));
          if (perpDistance >= getRadius())
            ang = new Angle(0);
          intersectionPoints[0] = (new Line(getCentre(),
                  slopePerpToLine.add(ang), getRadius())).getEnd();
          intersectionPoints[1] = (new Line(getCentre(),
                  slopePerpToLine.sub(ang), getRadius())).getEnd();
        }
      }

      if (object2.getType() == Circle.TYPE) { //is a circle
        Circle circle2 = (Circle)object2;
        Line lineJoiningCentres = new Line(getCentre(), circle2.getCentre());
        double c1c2 = lineJoiningCentres.getLength();
        double r1 = getRadius();
        double r2 = circle2.getRadius();
        Angle ang = new Angle(Math.acos((r1*r1 + c1c2*c1c2 - r2*r2)/(2*r1*c1c2)));
        if (Math.abs((r1*r1 + c1c2*c1c2 - r2*r2)/(2*r1*c1c2)) > 1)
          ang = new Angle(0);
        intersectionPoints[0] = (new Line(getCentre(),
                lineJoiningCentres.getSlope().add(ang), getRadius())).getEnd();
        intersectionPoints[1] = (new Line(getCentre(),
                lineJoiningCentres.getSlope().sub(ang), getRadius())).getEnd();
      }
    }
    else {
      // This should not happen
      // CanIntersect() should be called before this function is called
    }
  return intersectionPoints;
  }

  public void move(Point Translation, Angle Rotation, Point FixedPoint) {
    centre.move(Translation, Rotation, FixedPoint);
  }
  
  public void propertyChange(java.beans.PropertyChangeEvent pce) {
    if (((pce.getSource() instanceof Line) && 
            (pce.getPropertyName() == Line.PROP_LENGTH)) || 
        ((pce.getSource() instanceof Circle) &&
            (pce.getPropertyName() == Circle.PROP_RADIUS)))
      setRadius(((Double)pce.getNewValue()).doubleValue());
  }

  public void draw(Graphics2D g2d) {
    if (isVisible()) {
      g2d.setColor(getColor());
      g2d.draw(new java.awt.geom.Ellipse2D.Double(centre.getX() - getRadius(), 
                        centre.getY() - getRadius(),2*getRadius(), 2*getRadius()));
      g2d.setColor(Color.black);
    }
  }

  public String toString() {
    return ("Circle "+Integer.toString(getNumber()));
  }

}
/*
 * @(#)FourBarMechanism.java 1.0
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;
import java.awt.geom.Arc2D;
import java.util.Vector;
import java.io.*;
import cranks.geom.Circle;
import cranks.geom.Point;
import cranks.geom.Angle;

public class FourBarMechanism implements java.io.Serializable {
  
  private static final long serialVersionUID = -2411165030030348739L;

  private boolean grashof, elbowUp, drawTrace, redesignable;
  private int largest, smallest, fixed, direction, animationDelay, numTracePoints;
  private Link[] links = new Link[4];
  private Angle inputAngle, increment;
  private Range rotationRange;
  private Vector<Point> ternaryTrace;

  public FourBarMechanism() {
    this(100, 120, 140, 160);
  }

  public FourBarMechanism(double link1,double link2,double link3,double link4) {
    links[0] = new Link(link1);
    links[1] = new Link(link2);
    links[2] = new Link(link3);
    links[3] = new Link(link4);
    links[1].connectTo(links[0]);
    links[2].connectTo(links[1]);
    links[3].connectTo(links[2]);
    links[0].connectTo(links[3]);
    fixed = 0;
    inputAngle = new Angle();
    increment = new Angle(Math.PI/180); //Dummy value
    animationDelay = 0;
    ternaryTrace = new Vector<Point>();
    initialize();
    elbowUp = true;
    drawTrace = false;
    redesignable = false;
    direction = 1;
  }

  public void initialize() {
    updateChainProperties();
    numTracePoints = (rotationRange.getRange().getAngle() == 0)
                     ?((int)Math.round(2*Math.PI/increment.getAngle()))
                     :((int)Math.round(
               rotationRange.getRange().getAngle()/increment.getAngle()));
    ternaryTrace = new Vector<Point>(numTracePoints);
  }

  private void updateChainProperties() {
    if (isMechanism()) {
      setExtremes();
      setGrashof();
      setJointRanges();
      inputAngle.setAngle(rotationRange.getStart().add(new Angle(Math.PI/180)));
      double l1 = getLink(1).getLength();
      double l2 = getLink(2).getLength();
      double l3 = getLink(3).getLength();
      double l4 = getLink(4).getLength();
      getLink(1).getPostJoint().setCoordinates(-(l1/2), 0);
      getLink(2).getPostJoint().setCoordinates(l2*Math.cos(inputAngle.getAngle())-(l1/2),
                                         l2*Math.sin(inputAngle.getAngle()));
      getLink(4).getPostJoint().setCoordinates(l1/2, 0);
      Circle circle1 = new Circle(getLink(2).getPostJoint(), l3);
      Circle circle2 = new Circle(getLink(4).getPostJoint(), l4);
      Point newJoint = (elbowUp)?(circle1.intersect(circle2)[0]):
                                 (circle1.intersect(circle2)[1]);
      if (newJoint != null)
        getLink(3).getPostJoint().setCoordinates(newJoint.getX(), newJoint.getY());
    }
  }

  public void setJointRanges() {
    int oldValueOfFixed = fixed;
    for (int i = 0; i<4; i++) {
      fixed = i;
      Range rangeToBeSet = links[fixed].getPostJoint().getRotationRange();
      double l1 = getLink(1).getLength();
      double l2 = getLink(2).getLength();
      double l3 = getLink(3).getLength();
      double l4 = getLink(4).getLength();
      double arg1 = ((Math.pow(l1, 2) + Math.pow(l2, 2) - Math.pow((l4 - l3), 2))
                    / (2 * l1 * l2));
      double arg2 = ((Math.pow(l1, 2) + Math.pow(l2, 2) - Math.pow((l4 + l3), 2))
                    / (2 * l1 * l2));
      if ( (Math.abs(arg1) < 1) && (Math.abs(arg2) >=1))
        rangeToBeSet.setRange(Math.acos(arg1), 2*Math.PI-(Math.acos(arg1)));
      else
      if ( (Math.abs(arg1) >= 1) && (Math.abs(arg2) >= 1))
        rangeToBeSet.setRange(0.0, 2 * Math.PI);
      else
      if ( (Math.abs(arg1) >= 1) && (Math.abs(arg2) < 1))
        rangeToBeSet.setRange(-(Math.acos(arg2)), Math.acos(arg2));
      else
      if ( (Math.abs(arg1) < 1) && (Math.abs(arg2) < 1))
        rangeToBeSet.setRange(Math.acos(arg1), Math.acos(arg2));
    }
    fixed = oldValueOfFixed;
    rotationRange = getLink(1).getPostJoint().getRotationRange();
  }

  public void setExtremes() {
    largest = 0;
    smallest = 0;
    for (int i = 1; i < 4; i++) {
      if (links[i].getLength() > links[largest].getLength())
        largest = i;
      if (links[i].getLength() < links[smallest].getLength())
        smallest = i;
    }
  }

  public void setGrashof() {
    double sumOfLinks = 0;
    for (int i = 0; i<4; i++)
      sumOfLinks += links[i].getLength();
    grashof = ((2 * (links[largest].getLength() +
                     links[smallest].getLength())) < sumOfLinks);
  }

  public void setTernaryLink(boolean Ternary, double tLength, Angle tAngle) {
    for (int i = 0; i<4; i++)
      links[i].setTernary(Ternary, tLength, tAngle);
  }

  public Link nextLink(Link Current) {
    int n = Current.getNumber();
    n = (n == 4)?(1):(n + 1);
    return links[n - 1];
  }

  public void setChainLink(int n, double l) {
    getLink(n).setLength(l);
  }

  public void setElbow(boolean value) {
    elbowUp = value;
  }

  public boolean isGrashof() {
    return grashof;
  }

  public boolean isElbowUp() {
    return elbowUp;
  }

  public Link getLink(int n) {
    if (n > 0) {
      int linkIndex = n - 1 + fixed;
      while (linkIndex >= 4)
        linkIndex -= 4;
      return links[linkIndex];
    }
    else
      return null;
  }

  public boolean isMechanism() {
    setExtremes();
    double sumOfLengths = 0;
    for (int i = 0; i<4; i++)
      sumOfLengths += links[i].getLength();
    return (sumOfLengths > (2 * links[largest].getLength()));
  }

  public void setFixed(int value) {
    fixed = value - 1;
  }
  
  public int getFixed() {
    return (fixed + 1);
  }

  public void setInputAngle(Angle value) {
    inputAngle.setAngle(value.getAngle());
  }

  public void setIncrement(Angle value) {
    increment.setAngle(value.getAngle());
  }

  public void toggleTrace() {
    drawTrace = !drawTrace;
  }

  public void setDirection(int dir) {
    if (dir >=0 )
      direction = 1;
    else 
      direction = -1;
  }

  public void toggleDirection() {
    direction *= -1;
  }
  
  public int getDirection() {
    return direction;
  }

  public void setAnimationDelay(int delay) {
    animationDelay = delay;
  }

  public int getAnimationDelay() {
    return animationDelay;
  }

  public void setRedesignable(boolean value) {
    redesignable = value;
  }

  public boolean isRedesignable() {
    return redesignable;
  }

  public void updateJointPositions() {
    double l1 = getLink(1).getLength();
    double l2 = getLink(2).getLength();
    double l3 = getLink(3).getLength();
    double l4 = getLink(4).getLength();
    getLink(2).getPostJoint().setCoordinates(l2*Math.cos(inputAngle.getAngle())-(l1/2),
                                       l2*Math.sin(inputAngle.getAngle()));
    Circle circle1 = new Circle(getLink(2).getPostJoint(), l3);
    Circle circle2 = new Circle(getLink(4).getPostJoint(), l4);
    Point[] points = circle1.intersect(circle2);
    if ((points[0] != null) && (points[1] != null)) {
      Point oldJoint = getLink(3).getPostJoint();
      Point newJoint = (points[0].distanceTo(oldJoint) < 
                        points[1].distanceTo(oldJoint)) ?
                          (points[0]) : (points[1]);
      getLink(3).getPostJoint().setCoordinates(newJoint.getX(), newJoint.getY());
    }
    //Point newJoint = (elbowUp)?(circle1.intersect(circle2)[0]):
    //                           (circle1.intersect(circle2)[1]);
    //if (newJoint != null)
    //  getLink(3).getPostJoint().setCoordinates(newJoint.getX(), newJoint.getY());
  }

  public void step() {
    if (!rotationRange.getStart().equalTo(rotationRange.getEnd()))
      if (((direction == 1) && (rotationRange.getEnd().
                  sub(inputAngle).getAngle() <= increment.getAngle())) ||
          ((direction == -1) && (inputAngle.sub(
              rotationRange.getStart()).getAngle() <= increment.getAngle())))
        toggleDirection();
    inputAngle.inc(increment.getAngle()*direction);
    updateJointPositions();
    if (getLink(3).isTernary()) {
      Point couplerPoint = getLink(3).getCouplerPoint();
      couplerPoint.removeAssociations();
      if (ternaryTrace.size() < numTracePoints)
        ternaryTrace.addElement(couplerPoint);
    }
  }

  public void drawMechanism(Graphics2D g2d) {
    BasicStroke stroke = new BasicStroke(4.0f, BasicStroke.CAP_ROUND,
                                               BasicStroke.JOIN_ROUND);
    g2d.setStroke(stroke);
    g2d.setColor(Color.magenta);
    g2d.draw(new Line2D.Double(getLink(3).getPreviousJoint().getX(),
                               getLink(3).getPreviousJoint().getY(),
                               getLink(3).getPostJoint().getX(),
                               getLink(3).getPostJoint().getY()));
    if (getLink(3).isTernary()) {
      if (drawTrace) {
        for (int i = 0; i<ternaryTrace.size(); i++) {
          Point tracePoint = ternaryTrace.elementAt(i);
          g2d.setPaint(Color.red);
          g2d.fill(new Arc2D.Double(tracePoint.getX() - 2, tracePoint.getY() - 2,
                                  4, 4, 0, 360, Arc2D.OPEN));
        }
      }
      g2d.setPaint(Color.magenta);
      g2d.draw(new Line2D.Double(getLink(3).getPreviousJoint().getX(),
                                 getLink(3).getPreviousJoint().getY(),
                                 getLink(3).getCouplerPoint().getX(),
                                 getLink(3).getCouplerPoint().getY()));
      g2d.draw(new Line2D.Double(getLink(3).getCouplerPoint().getX(),
                                 getLink(3).getCouplerPoint().getY(),
                                 getLink(3).getPostJoint().getX(),
                                 getLink(3).getPostJoint().getY()));
    }
    g2d.setPaint(Color.red);
    g2d.fill(new Arc2D.Double(getLink(1).getPreviousJoint().getX() - 10,
                              getLink(1).getPreviousJoint().getY() - 10,
                              20, 20, 165, 210, Arc2D.CHORD));
    g2d.fill(new Arc2D.Double(getLink(1).getPostJoint().getX() - 10,
                              getLink(1).getPostJoint().getY() - 10,
                              20, 20, 165, 210, Arc2D.CHORD));
    g2d.setPaint(Color.black);
    g2d.draw(new Line2D.Double(getLink(1).getPreviousJoint().getX(),
                               getLink(1).getPreviousJoint().getY(),
                               getLink(1).getPostJoint().getX(),
                               getLink(1).getPostJoint().getY()));
    g2d.setPaint(Color.green);
    g2d.draw(new Line2D.Double(getLink(2).getPreviousJoint().getX(),
                               getLink(2).getPreviousJoint().getY(),
                               getLink(2).getPostJoint().getX(),
                               getLink(2).getPostJoint().getY()));
    g2d.setPaint(Color.blue);
    g2d.draw(new Line2D.Double(getLink(4).getPreviousJoint().getX(),
                               getLink(4).getPreviousJoint().getY(),
                               getLink(4).getPostJoint().getX(),
                               getLink(4).getPostJoint().getY()));
  }
  
  public void copyMechanism(FourBarMechanism mech) {
    setFixed(1);
    for (int i = 1; i <= 4; i++)
      setChainLink(i, mech.getLink(i).getLength());
    setTernaryLink(mech.getLink(3).isTernary(),mech.getLink(3).getTernaryLength(),
                   mech.getLink(3).getTernaryAngle());
    setFixed(mech.getFixed());
    setDirection(mech.getDirection());
    setElbow(mech.isElbowUp());
    setRedesignable(mech.isRedesignable());
    initialize();
  }

}
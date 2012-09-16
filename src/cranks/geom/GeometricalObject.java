/*
 * @(#)GeometricalObject.java 1.0
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

/* Object construction
 * -------------------
 * Add all real objects to the objects vector immediately after object
 * construction using AddToObjects method. This will automatically add all
 * subsidiary objects that form a part of that object.
 * Note : Angles are never added.
 *
 * Color and display rules for objects
 * ----------------------------------
 * Later rules have priority over the earlier ones
 * All geometrical objects and their labels are visible when initialised
 * Angles are never displayed
 * When initialised, all objects & labels take on the color scheme as follows :
 * Point - Black    Line - Red     Circle - Blue     Triangle - Magenta
 * All points that become starting point of lines become red
 * All points that become centres of circles become blue
 * All points and lines that become part of triangles become magenta
 *
 * Object type number
 * ------------------
 * Every object has a type number : 1-Point, 2-Angle, 3-Line, 4-Circle, 5-Triangle
 * Generally, all display rules give priority to higher type objects
 */
package cranks.geom;

import java.beans.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Vector;

/**
 * Defines an abstract Geometrical Object, which is characterized by its color,
 * visibility, a number ID and a type indicator denoting object type. Also
 * provides prototypes for required methods that subclasses must override.
 *
 * @author Aravind Alwan
 */
public abstract class GeometricalObject implements java.io.Serializable, 
                                                        PropertyChangeListener {
  
  private static final long serialVersionUID = -4128167300974621174L;
  
  /**
   * Support for binding properties between geometrical objects
   */
  protected PropertyChangeSupport propertySupport;
  
  /**
   * Object color, which is black by default.
   */
  protected Color color;
  /**
   * Indicates whether object and its label are painted on the screen or not.
   */
  protected boolean visible;
  /**
   * Unique number for every user-constructed object that denotes its rank
   * according to the chronological order of creation.
   */
  protected int objectNumber;
  /**
   * Object type, which provides an easy way of identifying its sub-class
   */
  protected int TYPE;
  
  public GeometricalObject() {
    propertySupport = new PropertyChangeSupport(this);
  }
  
  public void addPropertyChangeListener(String prop, 
                                              PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(prop, listener);
  }
  
  public void removePropertyChangeListener(String prop, 
                                              PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(prop, listener);
  }

  public void propertyChange(PropertyChangeEvent pce) {
  }
  
  /**
   * Returns a <CODE>Color</CODE> object that encapsulates the color of the
   * object when drawn on the screen.
   *
   * @return object color as a <CODE>Color</CODE> object
   */
  public Color getColor() {
    return color;
  }

  /**
   * Sets the object color to the specified value.
   * <P><B>Note: </B>Do not call this method explicitly. Use <CODE>SetColor</CODE>
   * instead.
   *
   * @param newColor the new color of the object
   */
  protected void setColor (Color newColor) {
    color = newColor;
  }

  /**
   * Sets the object color according to a pre-defined set of display rules. 
   * Subclasses must override this method and must set the color according 
   * to these rules.
   */
  public void setColor() {
  }

  /**
   * Returns <CODE>true</CODE> if the object is actually drawn on the screen.
   *
   * @return a boolean value indicating the object's visibility status
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Sets the visibility status of the object to the specified value.
   *
   * @param Visible the new visibility status of the object
   */
  public void setVisible(boolean Visible) {
    visible = Visible;
  }

  /**
   * Toggles of the visibility of the object.
   */
  public void toggleVisible() {
    visible = !visible;
  }

  /**
   * Returns the object ID or number
   *
   * @return the number of the object
   */
  public int getNumber() {
    return objectNumber;
  }

  /**
   * Sets the object number to the specified value.
   * <P><B>Note: </B> This method should be called with caution and one 
   * must ensure that the new number of the object is always equal to 
   * one more than its index in the Vector of objects in which it is 
   * stored. When constructing new objects it is not necessary to call 
   * <CODE>setNumber</CODE> separately, as this is already done in 
   * <CODE>addToObjects</CODE>.
   *
   * @param number the new object number
   */
  public void setNumber(int number) {
    objectNumber = number;
  }

  /**
   * Returns type of the sub-class that extends <CODE>GeometricalObject</CODE>.
   *
   * @return the type of the object
   */
  public int getType() {
    return TYPE;
  }

  /**
   * Returns true for those objects for which an intersection is valid.
   *
   * @param Object2 the object with which an intersection is sought
   * @return the boolean value indicating whether intersection is possible
   */
  public abstract boolean canIntersect(GeometricalObject Object2);

  /**
   * Returns the intersection with Object2 as an array of Geometrical Objects.
   *
   * @param Object2 an argument
   * @return the intersection as a <CODE>GeometricalObject</CODE> array
   */
  public GeometricalObject[] intersect(GeometricalObject Object2) {
    return null;
  }

  /**
   * Equivalent to the paint method for this object. Draws the object assuming
   * the co-ordinates of a normal <CODE>Graphics</CODE> object <I>g</I> (i.e. 
   * (0,0) is the top left corner). The dimensions of the drawing area of 
   * <I>g</I> are used to transform the drawing such that (0,0) is located 
   * at the bottom left corner. Thus, for the user, all the drawing takes 
   * place in the first quadrant, with the <I>x</I> & <I>y</I> axes 
   * aligned with the bottom and left edges of the drawing area.
   *
   * @param g the <CODE>Graphics</CODE> context on which the object is drawn
   * @param d the <CODE>Dimension</CODE> of the drawing area.
   */
  public void draw(Graphics2D g2d) {
  }

  /**
   * To perform a translation followed by rotation about a fixed point. The
   * <I>x</I> & <I>y</I> components of the translation vector are contained 
   * in the <I>x</I> & <I>y</I> co-ordinates of the point Translation. If 
   * the FixedPoint is a part of this object then it will move during 
   * translation and rotation will take place about the new point obtained 
   * after translation.
   *
   * @param Translation a <CODE>Point</CODE> object encapsulating the coordinates 
   * of the translation vector
   * @param Rotation an <CODE>Angle</CODE> object specifying the rotation angle
   * @param FixedPoint the point about which rotation is to be performed
   */
  public abstract void move(Point Translation, Angle Rotation, Point FixedPoint);

  /**
   * Adds this object to the <CODE>Vector</CODE> of user-constructed 
   * objects and sets its object number. If the Vector does not contain 
   * this object, its subsidiary objects are recursively added first to 
   * the Objects vector, followed by this object. 
   * <P><B>Note:</B> Angle objects are never added to the object vector.
   *
   * @param Objects the <CODE>Vector</CODE> to which this object is to be added
   */
  public void addToObjects(Vector<GeometricalObject> Objects) {
  }

  /**
   * Removes this object from the Objects vector, leaving behind its
   * subsidiary objects, if any.
   *
   * @param Objects the <CODE>Vector</CODE> from which from the object is to be 
   * removed
   */
  public void removeFromObjects(Vector<GeometricalObject> Objects) {
  }

  /**
   * Associates this object with Object2 of higher type, if it is a subsidiary
   * object of Object2.
   *
   * @param Object2 the parent object with which this object is to be associated
   */
  public void associateWith(GeometricalObject Object2) {
  }

  /**
   * Removes the association with Object2 of higher type e.g. during removal 
   * of Object2.
   *
   * @param Object2 the specified object
   */
  public void removeAssociationWith(GeometricalObject Object2) {
  }

  /**
   * Removes references to this object from all subsidiary objects. This is
   * called during removal of this object.
   */
  public void removeAssociations() {
  }

}
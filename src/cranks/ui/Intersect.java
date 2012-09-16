/*
 * @(#)Intersect.java 1.0
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

package cranks.ui;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.undo.ConstructionStep;
import cranks.geom.Line;
import cranks.geom.Circle;
import cranks.geom.Point;
import cranks.geom.GeometricalObject;

/**
 * This class is used to find the intersection between two objects
 */

 public class Intersect extends EditDialog implements PropertyChangeListener{

  public static final int INTERSECT_ONE_POINT = 1;
  public static final int INTERSECT_TWO_POINTS = 2;

  JOptionPane optionPane;
  JPanel pMainPanel = new JPanel();
  JLabel lObject1 = new JLabel("Object 1");
  JComboBox cObject1 = new JComboBox();
  JLabel lObject2 = new JLabel("Object 2");
  JComboBox cObject2 = new JComboBox();

  public Intersect(JFrame frame, String title, Vector<GeometricalObject> Objects) {
    super(frame, title, false, Objects);
    setID(8);
    jbInit();
    pack();
  }

  private void jbInit() {

    pMainPanel.setLayout(new GridLayout(2, 2, 20, 10));
    pMainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    pMainPanel.add(lObject1);
    pMainPanel.add(cObject1);
    pMainPanel.add(lObject2);
    pMainPanel.add(cObject2);

    Object[] options = {"Ok", "Cancel"};
    optionPane = new JOptionPane(pMainPanel, JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
    setContentPane(optionPane);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new JDialogWindowAdapter(this));
    optionPane.addPropertyChangeListener(this);
  }

  public void propertyChange(PropertyChangeEvent e) {
    if (isVisible() && (e.getSource() == optionPane) &&
        (JOptionPane.VALUE_PROPERTY.equals(e.getPropertyName()))) {
      Object value = optionPane.getValue();

      if (value == JOptionPane.UNINITIALIZED_VALUE) {
        return;
      }

      //Reset the JOptionPane's value.
      //If you don't do this, then if the user
      //presses the same button next time, no
      //property change event will be fired.
      optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
      if (value.equals("Ok")) {
        intersectObjects();
      }
      else { //user closed dialog or clicked cancel
        constructionCancelled();
        clearAndHide();
      }
    }
  }

  public void clearAndHide() {
    dispose();
  }

  private void intersectObjects() {
    GeometricalObject object1 = (GeometricalObject)cObject1.getSelectedItem();
    GeometricalObject object2 = (GeometricalObject)cObject2.getSelectedItem();
    ConstructionStep step;
    if ((object1 instanceof Line) && (object2 instanceof Line))
      step = new ConstructionStep(this, new Object[]{object1,
                                                    object2}, INTERSECT_ONE_POINT);
    else
      step = new ConstructionStep(this, new Object[]{object1,
                                                    object2}, INTERSECT_TWO_POINTS);
    doConstruction(step);
  }

  public void doConstruction(ConstructionStep step) {
    if (step.getConstructionType() == INTERSECT_ONE_POINT) {
      GeometricalObject object1 = (GeometricalObject)step.getInputs()[0];
      GeometricalObject object2 = (GeometricalObject)step.getInputs()[1];
      Point[] intersectionPoints = new Point[2];
      if ((object1.canIntersect(object2)) && (object1.getType() != Point.TYPE)) {
        intersectionPoints = (Point[])object1.intersect(object2);
        intersectionPoints[0].addToObjects(objects);
        clearAndHide();
        step.setOutputs(intersectionPoints);
        fireUndoableEditUpdate(new UndoableEditEvent(this, step));
      }
      else {
        JOptionPane.showMessageDialog(this, "Objects cannot intersect",
                                "Try Again", JOptionPane.ERROR_MESSAGE);
        initDialog();
      }
    }
    else if (step.getConstructionType() == INTERSECT_TWO_POINTS) {
      GeometricalObject object1 = (GeometricalObject)step.getInputs()[0];
      GeometricalObject object2 = (GeometricalObject)step.getInputs()[1];
      Point[] intersectionPoints = new Point[2];
      if ((object1.canIntersect(object2)) && (object1.getType() != Point.TYPE)) {
        intersectionPoints = (Point[])object1.intersect(object2);
        intersectionPoints[0].addToObjects(objects);
        intersectionPoints[1].addToObjects(objects);
        clearAndHide();
        step.setOutputs(intersectionPoints);
        fireUndoableEditUpdate(new UndoableEditEvent(this, step));
      }
      else {
        JOptionPane.showMessageDialog(this, "Objects cannot intersect",
                                "Try Again", JOptionPane.ERROR_MESSAGE);
        initDialog();
      }
    }
    else
      System.err.println("Bad construction type");
  }

  public void undoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == INTERSECT_ONE_POINT) {
      Point[] intersectionPoints = (Point[])step.getOutputs();
      intersectionPoints[0].removeFromObjects(objects);
      for (int i = 0; i<objects.size(); i++)
        objects.elementAt(i).setNumber(i+1);
    }
    else if (step.getConstructionType() == INTERSECT_TWO_POINTS) {
      Point[] intersectionPoints = (Point[])step.getOutputs();
      intersectionPoints[0].removeFromObjects(objects);
      intersectionPoints[1].removeFromObjects(objects);
      for (int i = 0; i<objects.size(); i++)
        objects.elementAt(i).setNumber(i+1);
    }
    else
      System.err.println("Bad construction type");
  }

  public void redoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == INTERSECT_ONE_POINT) {
      Point[] intersectionPoints = (Point[])step.getOutputs();
      intersectionPoints[0].addToObjects(objects);
    }
    else if (step.getConstructionType() == INTERSECT_TWO_POINTS) {
      Point[] intersectionPoints = (Point[])step.getOutputs();
      intersectionPoints[0].addToObjects(objects);
      intersectionPoints[1].addToObjects(objects);
    }
    else
      System.err.println("Bad construction type");
  }

  public void initDialog() {
    showObjects();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  //Show a list of intersectable objects in the dialog box
  private void showObjects() {
    cObject1.removeAllItems();
    cObject2.removeAllItems();
    //display all lines and circles.
    for(int t=(objects.size()-1);t>=0;t--) {
      GeometricalObject ob = objects.elementAt(t);
      if ((ob.getType() == Line.TYPE) || (ob.getType() == Circle.TYPE)) {
        cObject1.addItem(ob);
        cObject2.addItem(ob);
      }
    }
  }

  public String getPresentationName(ConstructionStep step) {
    if ((step.getConstructionType() == INTERSECT_ONE_POINT) ||
        (step.getConstructionType() == INTERSECT_TWO_POINTS))
      return ("Intersect " + step.getInputs()[0] + " with " + step.getInputs()[1]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    if (step.getConstructionType() == INTERSECT_ONE_POINT)
      return ("Intersect " + step.getInputs()[0] + " with " + step.getInputs()[1] +
              " - Output: " + step.getOutputs()[0]);
    else if (step.getConstructionType() == INTERSECT_TWO_POINTS)
      return ("Intersect " + step.getInputs()[0] + " with " + step.getInputs()[1] +
              " - Output: " + step.getOutputs()[0] + ", " + step.getOutputs()[1]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

}
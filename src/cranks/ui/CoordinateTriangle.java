/*
 * @(#)CoordinateTriangle.java 1.0
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.undo.ConstructionStep;
import cranks.geom.Triangle;
import cranks.geom.Line;
import cranks.geom.Point;
import cranks.geom.GeometricalObject;

/**
 * This class is used to draw triangles from existing points and lines
 */

public class CoordinateTriangle extends EditDialog implements PropertyChangeListener {

  public static final int THREE_POINTS = 1;
  public static final int POINT_LINE = 2;

  JTabbedPane tabbedPane = new JTabbedPane();
  JOptionPane optionPane;

  JPanel pThreePoints = new JPanel();
  JLabel lPoint1 = new JLabel("Select Point 1");
  JComboBox cPoint1 = new JComboBox(); //to select point1
  JLabel lPoint2 = new JLabel("Select Point 2");
  JComboBox cPoint2 = new JComboBox(); //to select point2
  JLabel lPoint3 = new JLabel("Select Point 3");
  JComboBox cPoint3 = new JComboBox(); //to select point3

  JPanel pPointLine = new JPanel();
  JLabel lPoint = new JLabel("Select Point");
  JComboBox cPoint = new JComboBox(); //to select point
  JLabel lLine = new JLabel("Select Line");
  JComboBox cLine = new JComboBox(); //to select line

  public CoordinateTriangle(JFrame frame, String title,
                            Vector<GeometricalObject> Objects) {
    super(frame, title, false, Objects);
    setID(4);
    jbInit();
    pack();
  }

  private void jbInit() {

    pThreePoints.setLayout(new GridLayout(3,2, 20, 10));
    pThreePoints.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    pThreePoints.add(lPoint1);
    pThreePoints.add(cPoint1);
    pThreePoints.add(lPoint2);
    pThreePoints.add(cPoint2);
    pThreePoints.add(lPoint3);
    pThreePoints.add(cPoint3);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.weightx = 0.5;
    gbc.weighty = 0.5;

    pPointLine.setLayout(new GridBagLayout()); //Creating the centre-radius panel
    pPointLine.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    gbc.gridx = 0;
    gbc.gridy = 0;
    pPointLine.add(lPoint, gbc);
    gbc.gridx = 1;
    pPointLine.add(cPoint, gbc);
    gbc.gridx = 0;
    gbc.gridy = 1;
    pPointLine.add(lLine, gbc);
    gbc.gridx = 1;
    pPointLine.add(cLine, gbc);

    tabbedPane.addTab("Three Points", null, pThreePoints,
                            "Specify Three Points");
    tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
    tabbedPane.addTab("Point-Line", null, pPointLine,
                            "Specify line and point not on line");
    tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

    Object[] options = {"Ok", "Cancel"};
    optionPane = new JOptionPane(tabbedPane, JOptionPane.PLAIN_MESSAGE,
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
        createTriangle();
      }
      else { //user closed dialog or clicked cancel
        constructionCancelled();
        clearAndHide();
      }
    }
  }

  private void createTriangle() {
    if (tabbedPane.getSelectedComponent() == pThreePoints) {
      Point point1 = (Point)cPoint1.getSelectedItem();
      Point point2 = (Point)cPoint2.getSelectedItem();
      Point point3 = (Point)cPoint3.getSelectedItem();
      ConstructionStep step = new ConstructionStep(this, new Object[]{point1,
                          point2, point3}, THREE_POINTS);
      doConstruction(step);
    }
    if (tabbedPane.getSelectedComponent() == pPointLine) {
      Point vertexPoint = (Point)cPoint.getSelectedItem();
      Line edgeLine = (Line)cLine.getSelectedItem();
      ConstructionStep step = new ConstructionStep(this,
                          new Object[]{vertexPoint, edgeLine}, POINT_LINE);
      doConstruction(step);
    }
  }

  public void doConstruction(ConstructionStep step) {
    if (step.getConstructionType() == THREE_POINTS) {
      Point P1 = (Point)step.getInputs()[0];
      Point P2 = (Point)step.getInputs()[1];
      Point P3 = (Point)step.getInputs()[2];
      Line line12 = new Line(P1, P2);
      Line line23 = new Line(P2, P3);
      if (!((line12.getSlope().equalTo(line23.getSlope()))||(P1.equalTo(P2))
          || (P2.equalTo(P3)) || (P3.equalTo(P1))))  {
        Triangle newTriangle = new Triangle(P1, P2, P3);
        newTriangle.addToObjects(objects);
        clearAndHide();
        step.setOutputs(new Object[]
             {newTriangle.getSides()[0], newTriangle.getSides()[1],
              newTriangle.getSides()[2], newTriangle});
        fireUndoableEditUpdate(new UndoableEditEvent(this, step));
      }
      else {
        JOptionPane.showMessageDialog(this, "Please select three distinct"+
              " non-collinear points", "Try Again", JOptionPane.ERROR_MESSAGE);
        initDialog();
      }
    }
    else if (step.getConstructionType() == POINT_LINE) {
      Point P1 = (Point)step.getInputs()[0];
      Line L1 = (Line)step.getInputs()[1];
      if (!(P1.canIntersect(L1))) {
        Triangle newTriangle = new Triangle(P1, L1);
        newTriangle.addToObjects(objects);
        clearAndHide();
        step.setOutputs(new Object[]
          {newTriangle.getSides()[1], newTriangle.getSides()[2], newTriangle});
        fireUndoableEditUpdate(new UndoableEditEvent(this, step));
      }
      else {
        JOptionPane.showMessageDialog(this, "Please select a point that does "+
              "not lie on the line", "Try Again", JOptionPane.ERROR_MESSAGE);
        initDialog();
      }
    }
    else
      System.err.println("Bad constructon type");
  }

  public void undoConstruction(ConstructionStep step) {
    if ((step.getConstructionType() == THREE_POINTS) ||
        (step.getConstructionType() == POINT_LINE)) {
      ((GeometricalObject)step.getOutputs()[0]).removeFromObjects(objects);
      ((GeometricalObject)step.getOutputs()[1]).removeFromObjects(objects);
      ((GeometricalObject)step.getOutputs()[2]).removeFromObjects(objects);
      for (int i = 0; i<objects.size(); i++)
        objects.elementAt(i).setNumber(i+1);
    }
    else
      System.err.println("Bad constructon type");
  }

  public void redoConstruction(ConstructionStep step) {
    if ((step.getConstructionType() == THREE_POINTS) ||
        (step.getConstructionType() == POINT_LINE)) {
      for (int i = 0; i<step.getOutputs().length; i++)
      ((GeometricalObject)step.getOutputs()[i]).addToObjects(objects);
    }
    else
      System.err.println("Bad constructon type");
  }

  public void clearAndHide() {
    dispose();
  }

  public void initDialog() {
    showObjects();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void showObjects() {
    cPoint1.removeAllItems();
    cPoint2.removeAllItems();
    cPoint3.removeAllItems();
    cPoint.removeAllItems();
    cLine.removeAllItems();

    for (int i = objects.size()-1; i>=0; i--) {
      GeometricalObject o = (GeometricalObject)(objects.elementAt(i));
      if (o.getType() == Point.TYPE) {
        cPoint1.addItem(o);
        cPoint2.addItem(o);
        cPoint3.addItem(o);
        cPoint.addItem(o);
      }
      if (o.getType() == Line.TYPE) {
        cLine.addItem(o);
      }
    }
  }

  public String getPresentationName(ConstructionStep step) {
    if ((step.getConstructionType() == THREE_POINTS) ||
        (step.getConstructionType() == POINT_LINE))
      return ("Construct " + step.getOutputs()[step.getOutputs().length - 1]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    String displayName = "";
    if (step.getConstructionType() == THREE_POINTS)
      displayName = "Construct triangle using " + step.getInputs()[0] + " , " +
              step.getInputs()[1] + " and " + step.getInputs()[2];
    else if (step.getConstructionType() == POINT_LINE)
      displayName = "Construct triangle from " + step.getInputs()[0] + " and " +
              step.getInputs()[1];
    else {
      System.err.println("Bad construction type");
      return "";
    }
    displayName +=  " - Output: ";
    for(int i = 0; i < step.getOutputs().length - 1; i++)
      displayName += step.getOutputs()[i] + ", ";
    displayName += step.getOutputs()[step.getOutputs().length - 1];
    return displayName;
  }

}
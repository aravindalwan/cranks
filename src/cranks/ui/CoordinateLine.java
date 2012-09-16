/*
 * @(#)CoordinateLine.java 1.0
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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.KeyEvent;
import java.util.Vector;
import cranks.geom.Line;
import cranks.geom.Circle;
import cranks.geom.Point;
import cranks.geom.Angle;
import cranks.geom.GeometricalObject;
import cranks.undo.ConstructionStep;

/**
 * This class is used to draw lines using the one point and two point formula
 */

public class CoordinateLine extends EditDialog implements PropertyChangeListener,
                                                                ActionListener {

  public static final int ONE_POINT = 1;
  public static final int TWO_POINTS = 2;

  JTabbedPane tabbedPane = new JTabbedPane();
  JOptionPane optionPane;

  JPanel pOnePoint = new JPanel(); //panel for one-point formula
  JPanel pPoint = new JPanel();
  JLabel lSelectPoint = new JLabel("Select Point");
  JComboBox cPoint = new JComboBox();
  JPanel pLength = new JPanel();
  JRadioButton rbLengthValue = new JRadioButton("Value", true);
  JTextField tfLength = new JTextField();
  JRadioButton rbLengthReference = new JRadioButton("Reference", false);
  JComboBox cLengthReference = new JComboBox();
  ButtonGroup bgLength = new ButtonGroup();
  JPanel pSlope = new JPanel();
  JRadioButton rbSlopeValue = new JRadioButton("Value (Degrees)", true);
  JTextField tfSlope = new JTextField();
  JRadioButton rbSlopeReference = new JRadioButton("Reference", false);
  JComboBox cSlopeReference = new JComboBox();
  ButtonGroup bgSlope = new ButtonGroup();
  
  JPanel pTwoPoints = new JPanel(); //panel for two-point formula
  JLabel lSelectPoint1 = new JLabel("Select Start");
  JComboBox cPoint1 = new JComboBox();

  JLabel lSelectPoint2 = new JLabel("Select End");
  JComboBox cPoint2 = new JComboBox();

  public CoordinateLine(JFrame frame, String title,
                        Vector<GeometricalObject> Objects) {
    super(frame, title, false, Objects);
    setID(2);
    jbInit();
    pack();
  }

  private void jbInit() {
    pOnePoint.setLayout(new BoxLayout(pOnePoint, BoxLayout.PAGE_AXIS)); //setting one-point panel
    pOnePoint.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    pPoint.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    pPoint.add(lSelectPoint);
    pPoint.add(cPoint);
    pLength.setLayout(new GridLayout(2, 2, 20, 10));
    pLength.setBorder(BorderFactory.createCompoundBorder(
                      BorderFactory.createTitledBorder("Length"), 
                      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    pLength.add(rbLengthValue);
    pLength.add(tfLength);
    tfLength.setDragEnabled(true);
    pLength.add(rbLengthReference);
    pLength.add(cLengthReference);
    bgLength.add(rbLengthValue);
    bgLength.add(rbLengthReference);
    rbLengthValue.addActionListener(this);
    rbLengthReference.addActionListener(this);
    pSlope.setLayout(new GridLayout(2, 2, 20, 10));
    pSlope.setBorder(BorderFactory.createCompoundBorder(
                     BorderFactory.createTitledBorder("Slope"), 
                     BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    pSlope.add(rbSlopeValue);
    pSlope.add(tfSlope);
    tfSlope.setDragEnabled(true);
    pSlope.add(rbSlopeReference);
    pSlope.add(cSlopeReference);
    bgSlope.add(rbSlopeValue);
    bgSlope.add(rbSlopeReference);
    rbSlopeValue.addActionListener(this);
    rbSlopeReference.addActionListener(this);
    pOnePoint.add(pPoint);
    pOnePoint.add(pLength);
    pOnePoint.add(pSlope);
    
    pTwoPoints.setLayout(new GridBagLayout()); //setting the two-point panel
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 0.5;
    gbc.weighty = 0.5;
    pTwoPoints.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    gbc.gridx = 0;
    gbc.gridy = 0;
    pTwoPoints.add(lSelectPoint1, gbc);
    gbc.gridx = 1;
    pTwoPoints.add(cPoint1, gbc);
    gbc.gridy = 1;
    gbc.gridx = 0;
    pTwoPoints.add(lSelectPoint2, gbc);
    gbc.gridx = 1;
    pTwoPoints.add(cPoint2, gbc);

    tabbedPane.addTab("Point - Slope", null, pOnePoint,
                    "Specify Starting Point and Slope");
    tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
    tabbedPane.addTab("Point - Point", null, pTwoPoints,
                    "Specify Two Points");
    tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

    Object[] options = {"Ok", "Copy Field Value", "Cancel"};
    optionPane = new JOptionPane(tabbedPane, JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);
    setContentPane(optionPane);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new JDialogWindowAdapter(this));
    optionPane.addPropertyChangeListener(this);
  }

  public void propertyChange(PropertyChangeEvent e){
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
        createLine();
      }
      else if (value.equals("Copy Field Value")) {
        mfInstance.ActionShowFieldValue.invoke();
      }
      else { //user closed dialog or clicked cancel
        constructionCancelled();
        clearAndHide();
      }
    }
  }
  
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() instanceof JRadioButton)
      setEnabled();
  }

  public void initDialog() {
    showObjects();
    setLocationRelativeTo(null);
    setEnabled();
    setVisible(true);
  }
  
  public void setEnabled() {
    tfLength.setEnabled(rbLengthValue.isSelected());
    cLengthReference.setEnabled(rbLengthReference.isSelected());
    tfSlope.setEnabled(rbSlopeValue.isSelected());
    cSlopeReference.setEnabled(rbSlopeReference.isSelected());
  }

  public void clearAndHide() {
    tfLength.setText("");
    tfSlope.setText("");
    dispose();
  }

  private void showObjects() {
    cPoint.removeAllItems();
    cLengthReference.removeAllItems();
    cSlopeReference.removeAllItems();
    cPoint1.removeAllItems();
    cPoint2.removeAllItems();
    for(int t=(objects.size()-1);t>=0;t--) {
      if (objects.elementAt(t).getType() == Point.TYPE) {
        cPoint.addItem(objects.elementAt(t));
        cPoint1.addItem(objects.elementAt(t));
        cPoint2.addItem(objects.elementAt(t));
      }
      if (objects.elementAt(t).getType() == Line.TYPE) {
        cLengthReference.addItem(objects.elementAt(t));
        cSlopeReference.addItem(objects.elementAt(t));
      }
      if (objects.elementAt(t).getType() == Circle.TYPE) {
        cLengthReference.addItem(objects.elementAt(t));
      }
    }
  }

  private void createLine() {
    ConstructionStep step = null;
    if(tabbedPane.getSelectedComponent() == pOnePoint) {///1 point
      try {
        Object length, slopeDegrees;
        if (rbLengthValue.isSelected())
          length = new Double(Double.parseDouble(tfLength.getText()));
        else
          length = (GeometricalObject)cLengthReference.getSelectedItem();
        if (rbSlopeValue.isSelected())
          slopeDegrees = new Double(Double.parseDouble(tfSlope.getText()));
        else
          slopeDegrees = (GeometricalObject)cSlopeReference.getSelectedItem();
        Point p = (Point)cPoint.getSelectedItem();
        step = new ConstructionStep(this, new Object[]{p, length,
                            slopeDegrees}, ONE_POINT);
        doConstruction(step);
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Please enter numerical value",
                                    "Try Again", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    if(tabbedPane.getSelectedComponent() == pTwoPoints) {///2 point
      Point p1 = (Point)cPoint1.getSelectedItem();
      Point p2 = (Point)cPoint2.getSelectedItem();
      step = new ConstructionStep(this, new Object[]{p1, p2}, TWO_POINTS);
      doConstruction(step);
    }
  }

  public void doConstruction(ConstructionStep step) {
    if (step.getConstructionType() == ONE_POINT) {
      Point p = (Point)step.getInputs()[0];
      Object obj1 = step.getInputs()[1];
      Object obj2 = step.getInputs()[2];
      boolean lengthReference = (obj1 instanceof GeometricalObject);
      boolean slopeReference = (obj2 instanceof GeometricalObject);
      double length;
      Angle slope;
      if (lengthReference) {
        if (step.getInputs()[1] instanceof Line)
          length = ((Line)obj1).getLength();
        else 
          length = ((Circle)obj1).getRadius();
      }
      else {
        length = ((Double)obj1).doubleValue();
        if (!(Math.abs(length) > 0)) {
          JOptionPane.showMessageDialog(this, "Please enter non-zero value for Length",
                    "Try Again", JOptionPane.ERROR_MESSAGE);
          initDialog();
        }
      }
      if (slopeReference)
        slope = new Angle(((Line)obj2).getSlope().getAngle());
      else {
        double slopeDegrees = ((Double)obj2).doubleValue();
        slope = new Angle(Math.toRadians(slopeDegrees));
      }
      Line newLine = new Line(p, slope, length);
      newLine.addToObjects(objects);
      if (lengthReference)
        if (obj1 instanceof Line) {
          ((Line)obj1).addPropertyChangeListener(Line.PROP_LENGTH, newLine);
          newLine.addPropertyChangeListener(Line.PROP_LENGTH, (Line)obj1);
        }
        else {
          ((Circle)obj1).addPropertyChangeListener(Circle.PROP_RADIUS, newLine);
          newLine.addPropertyChangeListener(Circle.PROP_RADIUS, (Circle)obj1);
        }
      if (slopeReference) {
        ((Line)obj2).addPropertyChangeListener(Line.PROP_ANGLE, newLine);
        newLine.addPropertyChangeListener(Line.PROP_ANGLE, (Line)obj2);
      }        
      clearAndHide();
      step.setOutputs(new Object[]{newLine.getEnd(), newLine});
      fireUndoableEditUpdate(new UndoableEditEvent(this, step));
    }
    else if (step.getConstructionType() == TWO_POINTS) {
      Point p1 = (Point)step.getInputs()[0];
      Point p2 = (Point)step.getInputs()[1];
      if (!p1.equalTo(p2)) {
        Line newLine = new Line(p1, p2);
        newLine.addToObjects(objects);
        clearAndHide();
        step.setOutputs(new Object[]{newLine});
        fireUndoableEditUpdate(new UndoableEditEvent(this, step));
      }
      else {
        JOptionPane.showMessageDialog(this, "Please select two different points",
                                  "Try Again", JOptionPane.ERROR_MESSAGE);
        initDialog();
      }
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void undoConstruction(ConstructionStep step) {
    if ((step.getConstructionType() == ONE_POINT) ||
        (step.getConstructionType() == TWO_POINTS)) {
      for(int i = 0; i < step.getOutputs().length; i++)
        ((GeometricalObject)step.getOutputs()[i]).removeFromObjects(objects);
      for (int i = 0; i<objects.size(); i++)
        objects.elementAt(i).setNumber(i+1);
      if (step.getConstructionType() == ONE_POINT) {
        Object obj1 = step.getInputs()[1];
        Object obj2 = step.getInputs()[2];
        Line newLine = (Line)step.getOutputs()[1];
        if (!(obj1 instanceof Double))
          if (obj1 instanceof Line) {
            ((Line)obj1).removePropertyChangeListener(Line.PROP_LENGTH, newLine);
            newLine.removePropertyChangeListener(Line.PROP_LENGTH, (Line)obj1);
          }
          else {
            ((Circle)obj1).removePropertyChangeListener(Circle.PROP_RADIUS, newLine);
            newLine.removePropertyChangeListener(Circle.PROP_RADIUS, (Circle)obj1);
          }
        if (!(obj2 instanceof Double)) {
          ((Line)obj2).removePropertyChangeListener(Line.PROP_ANGLE, newLine);
          newLine.removePropertyChangeListener(Line.PROP_ANGLE, (Line)obj2);
        }
      }
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void redoConstruction(ConstructionStep step) {
    if ((step.getConstructionType() == ONE_POINT) ||
        (step.getConstructionType() == TWO_POINTS)) {
      for(int i = 0; i < step.getOutputs().length; i++)
        ((GeometricalObject)step.getOutputs()[i]).addToObjects(objects);
      if (step.getConstructionType() == ONE_POINT) {
        Object obj1 = step.getInputs()[1];
        Object obj2 = step.getInputs()[2];
        Line newLine = (Line)step.getOutputs()[1];
        if (!(obj1 instanceof Double))
          if (obj1 instanceof Line) {
            ((Line)obj1).addPropertyChangeListener(Line.PROP_LENGTH, newLine);
            newLine.addPropertyChangeListener(Line.PROP_LENGTH, (Line)obj1);
          }
          else {
            ((Circle)obj1).addPropertyChangeListener(Circle.PROP_RADIUS, newLine);
            newLine.addPropertyChangeListener(Circle.PROP_RADIUS, (Circle)obj1);
          }
        if (!(obj2 instanceof Double)) {
          ((Line)obj2).addPropertyChangeListener(Line.PROP_ANGLE, newLine);
          newLine.addPropertyChangeListener(Line.PROP_ANGLE, (Line)obj2);
        }
      }
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public String getPresentationName(ConstructionStep step) {
    if ((step.getConstructionType() == ONE_POINT) ||
        (step.getConstructionType() == TWO_POINTS))
      return ("Construct " + step.getOutputs()[0]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    if (step.getConstructionType() == ONE_POINT) {
      return ("Construct line from " + step.getInputs()[0] + " of length " +
              step.getInputs()[1] + " with a slope of " + step.getInputs()[2] +
              " - Output: " + step.getOutputs()[0] + ", " + step.getOutputs()[1]);
    }
    else if (step.getConstructionType() == TWO_POINTS)
      return ("Construct line from " + step.getInputs()[0] + " to " +
              step.getInputs()[1]  + " - Output: " + step.getOutputs()[0]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

}

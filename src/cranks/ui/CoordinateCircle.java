/*
 * @(#)CoordinateCircle.java 1.0
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
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.undo.ConstructionStep;
import cranks.geom.Line;
import cranks.geom.Circle;
import cranks.geom.Point;
import cranks.geom.GeometricalObject;

/**
 * This class is used to draw circles
 */

public class CoordinateCircle extends EditDialog implements PropertyChangeListener,
                                                            ActionListener {

  public static final int CENTRE_RADIUS = 1;
  public static final int THREE_POINTS = 2;

  JTabbedPane tabbedPane = new JTabbedPane();
  JOptionPane optionPane;

  JPanel pCentreRadius = new JPanel();
  JPanel pCentre = new JPanel();
  JLabel lCentre = new JLabel("Centre");
  JComboBox cCentrePoint = new JComboBox(); //to select centre
  JPanel pRadius = new JPanel(); //to specify radius
  JRadioButton rbRadiusValue = new JRadioButton("Value", true);
  JTextField tfRadius = new JTextField(); //by value
  JRadioButton rbRadiusReference = new JRadioButton("Reference", false);
  JComboBox cRadiusReference = new JComboBox(); //with reference to another object
  ButtonGroup bgRadius = new ButtonGroup();

  JPanel pThreePoints = new JPanel();
  JLabel lPoint1 = new JLabel("Point 1");
  JComboBox cPoint1 = new JComboBox(); //to select point1
  JLabel lPoint2 = new JLabel("Point 2");
  JComboBox cPoint2 = new JComboBox(); //to select point2
  JLabel lPoint3 = new JLabel("Point 3");
  JComboBox cPoint3 = new JComboBox(); //to select point3

  public CoordinateCircle(JFrame frame, String title,
                          Vector<GeometricalObject> Objects) {
    super(frame, title, false, Objects);
    setID(1);
    jbInit();
    pack();
  }

  private void jbInit() {
    pCentreRadius.setLayout(new BoxLayout(pCentreRadius, BoxLayout.PAGE_AXIS));
    pCentreRadius.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    pCentre.add(lCentre);
    pCentre.add(cCentrePoint);
    pRadius.setLayout(new GridLayout(2, 2, 20, 10));
    pRadius.setBorder(BorderFactory.createCompoundBorder(
                      BorderFactory.createTitledBorder("Radius"), 
                      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    pRadius.add(rbRadiusValue);
    pRadius.add(tfRadius);
    tfRadius.setDragEnabled(true);
    pRadius.add(rbRadiusReference);
    pRadius.add(cRadiusReference);
    bgRadius.add(rbRadiusValue);
    bgRadius.add(rbRadiusReference);
    rbRadiusValue.addActionListener(this);
    rbRadiusReference.addActionListener(this);
    pCentreRadius.add(pCentre);
    pCentreRadius.add(pRadius);

    pThreePoints.setLayout(new GridLayout(3,2, 20, 20));
    pThreePoints.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    pThreePoints.add(lPoint1);
    pThreePoints.add(cPoint1);
    pThreePoints.add(lPoint2);
    pThreePoints.add(cPoint2);
    pThreePoints.add(lPoint3);
    pThreePoints.add(cPoint3);

    tabbedPane.addTab("Centre & Radius", null, pCentreRadius,
                    "Specify Centre Point and Radius");
    tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
    tabbedPane.addTab("Three Points", null, pThreePoints,
                    "Specify Three Points");
    tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

    Object[] options = {"Ok", "Copy Field Value", "Cancel"};
    optionPane = new JOptionPane(tabbedPane, JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);
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
        createCircle();
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
  
  public void setEnabled() {
    tfRadius.setEnabled(rbRadiusValue.isSelected());
    cRadiusReference.setEnabled(rbRadiusReference.isSelected());
  }

  public void clearAndHide() {
    tfRadius.setText("");
    dispose();
  }

  private void createCircle() {
    if (tabbedPane.getSelectedComponent() == pCentreRadius) {
      Point centre = (Point)cCentrePoint.getSelectedItem();
      Object radius;
      try {
        if (rbRadiusValue.isSelected())
          radius = new Double(Double.parseDouble(tfRadius.getText()));
        else
          radius = (GeometricalObject)cRadiusReference.getSelectedItem();
      } catch (NumberFormatException e) {
        tfRadius.selectAll();
        JOptionPane.showMessageDialog(this, "Please enter numerical value for radius",
                                      "Try Again", JOptionPane.ERROR_MESSAGE);
        tfRadius.requestFocus(true);
        return;
      }
      ConstructionStep step = new ConstructionStep(this, new Object[]{centre,
                                                        radius}, CENTRE_RADIUS);
      doConstruction(step);
    }
    if (tabbedPane.getSelectedComponent() == pThreePoints) {
      Point point1 = (Point)cPoint1.getSelectedItem();
      Point point2 = (Point)cPoint2.getSelectedItem();
      Point point3 = (Point)cPoint3.getSelectedItem();
      ConstructionStep step = new ConstructionStep(this, new Object[]{point1,
                                point2, point3}, THREE_POINTS);
      doConstruction(step);
    }
  }

  public void doConstruction(ConstructionStep step) {
    if (step.getConstructionType() == CENTRE_RADIUS) {
      Point centre = (Point)step.getInputs()[0];
      Object obj1 = step.getInputs()[1];
      boolean radiusReference = (obj1 instanceof GeometricalObject);
      double radius;
      if (radiusReference) {
        if (step.getInputs()[1] instanceof Line)
          radius = ((Line)obj1).getLength();
        else 
          radius = ((Circle)obj1).getRadius();
      }
      else {
        radius = ((Double)obj1).doubleValue();
        if (!(radius > 0)) {
          JOptionPane.showMessageDialog(this, "Please enter positive value for " +
                              "radius", "Try Again", JOptionPane.ERROR_MESSAGE);
          initDialog();
        }
      }
      Circle newCircle = new Circle(centre, radius);
      newCircle.addToObjects(objects);
      if (radiusReference)
        if (obj1 instanceof Line) {
          ((Line)obj1).addPropertyChangeListener(Line.PROP_LENGTH, newCircle);
          newCircle.addPropertyChangeListener(Circle.PROP_RADIUS, (Line)obj1);
        }
        else {
          ((Circle)obj1).addPropertyChangeListener(Circle.PROP_RADIUS, newCircle);
          newCircle.addPropertyChangeListener(Circle.PROP_RADIUS, (Circle)obj1);
        }
      clearAndHide();
      step.setOutputs(new Object[]{newCircle});
      fireUndoableEditUpdate(new UndoableEditEvent(this, step));
    }
    else if (step.getConstructionType() == THREE_POINTS) {
      Point point1 = (Point)step.getInputs()[0];
      Point point2 = (Point)step.getInputs()[1];
      Point point3 = (Point)step.getInputs()[2];
      Line line12 = new Line(point1, point2);
      Line line23 = new Line(point2, point3);
      if (!((line12.isParallel(line23))||(point1.equalTo(point2))
          || (point2.equalTo(point3)) || (point3.equalTo(point1))))  {
        Circle newCircle = new Circle(point1, point2, point3);
        newCircle.addToObjects(objects);
        clearAndHide();
        step.setOutputs(new Object[]{newCircle.getCentre(), newCircle});
        fireUndoableEditUpdate(new UndoableEditEvent(this, step));
      }
      else {
        JOptionPane.showMessageDialog(this, "Select three distinct"
             + " non-collinear points", "Try Again", JOptionPane.ERROR_MESSAGE);
        initDialog();
      }
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void undoConstruction(ConstructionStep step) {
    if ((step.getConstructionType() == CENTRE_RADIUS) ||
        (step.getConstructionType() == THREE_POINTS)) {
      for(int i = 0; i < step.getOutputs().length; i++)
        ((GeometricalObject)step.getOutputs()[i]).removeFromObjects(objects);
      for (int i = 0; i<objects.size(); i++)
        objects.elementAt(i).setNumber(i+1);
      if (step.getConstructionType() == CENTRE_RADIUS) {
        Object obj1 = step.getInputs()[1];
        Circle newCircle = (Circle)step.getOutputs()[0];
        if (!(obj1 instanceof Double))
          if (obj1 instanceof Line) {
            ((Line)obj1).removePropertyChangeListener(Line.PROP_LENGTH, newCircle);
            newCircle.removePropertyChangeListener(Circle.PROP_RADIUS, (Line)obj1);
          }
          else {
           ((Circle)obj1).removePropertyChangeListener(Circle.PROP_RADIUS, newCircle);
            newCircle.removePropertyChangeListener(Circle.PROP_RADIUS, (Circle)obj1);
         }
      }
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void redoConstruction(ConstructionStep step) {
    if ((step.getConstructionType() == CENTRE_RADIUS) ||
        (step.getConstructionType() == THREE_POINTS)) {
      for(int i = 0; i < step.getOutputs().length; i++)
        ((GeometricalObject)step.getOutputs()[i]).addToObjects(objects);
      if (step.getConstructionType() == CENTRE_RADIUS) {
        Object obj1 = step.getInputs()[1];
        Circle newCircle = (Circle)step.getOutputs()[0];
        if (!(obj1 instanceof Double))
          if (obj1 instanceof Line) {
            ((Line)obj1).addPropertyChangeListener(Line.PROP_LENGTH, newCircle);
            newCircle.addPropertyChangeListener(Circle.PROP_RADIUS, (Line)obj1);
          }
          else {
           ((Circle)obj1).addPropertyChangeListener(Circle.PROP_RADIUS, newCircle);
            newCircle.addPropertyChangeListener(Circle.PROP_RADIUS, (Circle)obj1);
         }
      }
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void initDialog() {
    showObjects();
    setLocationRelativeTo(null);
    setEnabled();
    setVisible(true);
  }

  private void showObjects() {
    cCentrePoint.removeAllItems();
    cRadiusReference.removeAllItems();
    cPoint1.removeAllItems();
    cPoint2.removeAllItems();
    cPoint3.removeAllItems();

    //display the object numbers of all the points clicked by the user
    for(int t = (objects.size()-1); t >= 0; t--){
      if (objects.elementAt(t).getType() == Point.TYPE) {
        cPoint1.addItem(objects.elementAt(t));
        cPoint2.addItem(objects.elementAt(t));
        cPoint3.addItem(objects.elementAt(t));
        cCentrePoint.addItem(objects.elementAt(t));
      }
      if ((objects.elementAt(t).getType() == Line.TYPE) ||
          (objects.elementAt(t).getType() == Circle.TYPE))
        cRadiusReference.addItem(objects.elementAt(t));
    }
  }

  public String getPresentationName(ConstructionStep step) {
    if ((step.getConstructionType() == CENTRE_RADIUS) ||
        (step.getConstructionType() == THREE_POINTS))
      return ("Construct " + step.getOutputs()[step.getOutputs().length - 1]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    if (step.getConstructionType() == CENTRE_RADIUS)
      return ("Construct circle with centre " + step.getInputs()[0] +
              " and radius of length " + step.getInputs()[1] + " - Output: " +
              step.getOutputs()[0]);
    else if (step.getConstructionType() == THREE_POINTS)
      return ("Construct circle using " + step.getInputs()[0] + " , " +
              step.getInputs()[1] + " and " + step.getInputs()[2] + " - Output: " +
              step.getOutputs()[0] + ", " + step.getOutputs()[1]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

}
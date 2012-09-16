/*
 * @(#)ModifyPoint.java 1.0
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.undo.ConstructionStep;
import cranks.geom.Line;
import cranks.geom.Point;
import cranks.geom.GeometricalObject;

/**
 * This class is used to add and modify points
 */

public class ModifyPoint extends EditDialog implements PropertyChangeListener,
                             ActionListener {

  public static final int MODIFY_POINT = 1;

  JPanel pMainPanel = new JPanel();
  JOptionPane optionPane;

  JLabel lSelectPoint = new JLabel("Select Point");
  JComboBox cSelectPoint = new JComboBox();
  JLabel lNewCoordinates = new JLabel("Change To (X,Y)");
  JTextField tfNewCoordinatesX = new JTextField(5);
  JTextField tfNewCoordinatesY = new JTextField(5);

  public ModifyPoint(JFrame frame,String title,Vector<GeometricalObject> Objects) {
    super(frame, title, false, Objects);
    setID(9);
    jbInit();
    pack();
  }

  private void jbInit() {

    pMainPanel.setLayout(new GridLayout(2, 3, 20, 10));
    pMainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    pMainPanel.add(lSelectPoint);
    pMainPanel.add(cSelectPoint);
    cSelectPoint.setActionCommand("Point Selected");
    cSelectPoint.addActionListener(this);
    pMainPanel.add(new JLabel(""));
    pMainPanel.add(lNewCoordinates);
    pMainPanel.add(tfNewCoordinatesX);
    tfNewCoordinatesX.setDragEnabled(true);
    pMainPanel.add(tfNewCoordinatesY);
    tfNewCoordinatesY.setDragEnabled(true);

    Object[] options = {"Ok", "Copy Field Value", "Cancel"};
    optionPane = new JOptionPane(pMainPanel, JOptionPane.PLAIN_MESSAGE,
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
        modifyPoint();
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

  public void actionPerformed(ActionEvent e) {
    if (isVisible() && e.getActionCommand().equals("Point Selected")) {
      Point tempPoint = (Point)cSelectPoint.getSelectedItem();
      tfNewCoordinatesX.setText(String.valueOf(tempPoint.getX()));
      tfNewCoordinatesY.setText(String.valueOf(tempPoint.getY()));
    }
  }

  private void modifyPoint() {
    try {
      double p_x = Double.parseDouble(tfNewCoordinatesX.getText());
      double p_y = Double.parseDouble(tfNewCoordinatesY.getText());
      Point oldPoint = (Point)cSelectPoint.getSelectedItem();
      ConstructionStep step = new ConstructionStep(this,
        new Object[]{new Double(oldPoint.getX()), new Double(oldPoint.getY()),
        oldPoint, new Double(p_x), new Double(p_y)}, MODIFY_POINT);
      doConstruction(step);
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this, "Please enter numerical value",
                                  "Try Again", JOptionPane.ERROR_MESSAGE);
      return;
    }
  }

  public void doConstruction(ConstructionStep step) {
    if (step.getConstructionType() == MODIFY_POINT) {
      Point tempPoint = (Point)step.getInputs()[2];
      tempPoint.setCoordinates(((Double)step.getInputs()[3]).doubleValue(),
                         ((Double)step.getInputs()[4]).doubleValue());
      Vector<GeometricalObject> assocObjects = tempPoint.getAssocObjects();
      for (int i = 0; i<assocObjects.size(); i++) {
        GeometricalObject o = assocObjects.elementAt(i);
        if (o.getType() == Line.TYPE)
          ((Line)o).setLengthAndSlope();
      }
      clearAndHide();
      step.setOutputs(new Object[]{tempPoint});
      fireUndoableEditUpdate(new UndoableEditEvent(this, step));
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void undoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == MODIFY_POINT) {
      Point tempPoint = (Point)step.getInputs()[2];
      tempPoint.setCoordinates(((Double)step.getInputs()[0]).doubleValue(),
                         ((Double)step.getInputs()[1]).doubleValue());
      Vector<GeometricalObject> assocObjects = tempPoint.getAssocObjects();
      for (int i = 0; i<assocObjects.size(); i++) {
        GeometricalObject o = assocObjects.elementAt(i);
        if (o.getType() == Line.TYPE)
          ((Line)o).setLengthAndSlope();
      }
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void redoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == MODIFY_POINT) {
      Point tempPoint = (Point)step.getInputs()[2];
      tempPoint.setCoordinates(((Double)step.getInputs()[3]).doubleValue(),
                         ((Double)step.getInputs()[4]).doubleValue());
      Vector<GeometricalObject> assocObjects = tempPoint.getAssocObjects();
      for (int i = 0; i<assocObjects.size(); i++) {
        GeometricalObject o = assocObjects.elementAt(i);
        if (o.getType() == Line.TYPE)
          ((Line)o).setLengthAndSlope();
      }
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void initDialog() {
    showPoints();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  //Display all point objects in the dialog box for modification
  private void showPoints() {
    cSelectPoint.removeAllItems();
    //display the object numbers of all the points clicked by the user
    for(int t=(objects.size()-1);t>=0;t--){
      if (((GeometricalObject)(objects.elementAt(t))).getType() == Point.TYPE) {
        cSelectPoint.addItem(objects.elementAt(t));
      }
    }
  }

  public void clearAndHide() {
    tfNewCoordinatesX.setText("");
    tfNewCoordinatesY.setText("");
    dispose();
  }

  public String getPresentationName(ConstructionStep step) {
    if (step.getConstructionType() == MODIFY_POINT)
      return ("Modify " + step.getInputs()[2]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    if (step.getConstructionType() == MODIFY_POINT)
      return ("Modify co-ordinates of " + step.getInputs()[2] + " to " +
                ((Point)step.getOutputs()[0]).locationToString());
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

}
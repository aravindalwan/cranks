/*
 * @(#)CoordinatePoint.java 1.0
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
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.geom.Point;
import cranks.geom.GeometricalObject;
import cranks.undo.ConstructionStep;

/**
 * This class is used to add points by specifying coordinates
 */

public class CoordinatePoint extends EditDialog implements PropertyChangeListener {

  public static final int ADD_POINT = 1;

  JPanel pMainPanel = new JPanel();
  JOptionPane optionPane;

  JLabel lNewPoint = new JLabel("New Point (X,Y)");
  JTextField tfNewPointX = new JTextField(5);
  JTextField tfNewPointY = new JTextField(5);

  public CoordinatePoint(JFrame frame, String title,
                         Vector<GeometricalObject> Objects) {
    super(frame, title, false, Objects);
    setID(3);
    jbInit();
    pack();
  }

  private void jbInit() {

    pMainPanel.setLayout(new GridBagLayout());
    pMainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.weightx = 0.5;
    gbc.weighty = 0.5;
    gbc.gridx = 0;
    gbc.gridy = 0;
    pMainPanel.add(lNewPoint, gbc);
    gbc.gridx = 1;
    pMainPanel.add(tfNewPointX, gbc);
    tfNewPointX.setDragEnabled(true);
    gbc.gridx = 2;
    pMainPanel.add(tfNewPointY, gbc);
    tfNewPointY.setDragEnabled(true);

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
        addPoint();
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

  private void addPoint() {
    try {
      double p_x = Double.parseDouble(tfNewPointX.getText());
      double p_y = Double.parseDouble(tfNewPointY.getText());
      ConstructionStep step = new ConstructionStep(this,
                new Object[]{new Double(p_x), new Double(p_y)}, ADD_POINT);
      doConstruction(step);
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this, "Please enter numerical value",
                                  "Try Again", JOptionPane.ERROR_MESSAGE);
      return;
    }
  }

  public void doConstruction(ConstructionStep step) {
    if (step.getConstructionType() == ADD_POINT) {
      Point newPoint = new Point(((Double)step.getInputs()[0]).doubleValue(),
                        ((Double)step.getInputs()[1]).doubleValue());
      newPoint.addToObjects(objects);
      clearAndHide();
      step.setOutputs(new Object[]{newPoint});
      fireUndoableEditUpdate(new UndoableEditEvent(this, step));
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void undoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == ADD_POINT) {
      ((GeometricalObject)step.getOutputs()[0]).removeFromObjects(objects);
      for (int i = 0; i<objects.size(); i++)
        objects.elementAt(i).setNumber(i+1);
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void redoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == ADD_POINT) {
      ((GeometricalObject)step.getOutputs()[0]).addToObjects(objects);
    }
    else {
      System.err.println("Bad construction type");
    }
  }

  public void initDialog() {
    setLocationRelativeTo(null);
    setVisible(true);
  }

  public void clearAndHide() {
    tfNewPointX.setText("");
    tfNewPointY.setText("");
    dispose();
  }

  public String getPresentationName(ConstructionStep step) {
    if (step.getConstructionType() == ADD_POINT)
      return ("Add " + step.getOutputs()[0]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    if (step.getConstructionType() == ADD_POINT)
      return ("Add point " + ((Point)step.getOutputs()[0]).locationToString() + 
               " - Output: " + step.getOutputs()[0]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

}
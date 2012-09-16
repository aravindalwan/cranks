/*
 * @(#)Move.java 1.0
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
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.undo.ConstructionStep;
import cranks.geom.Line;
import cranks.geom.Point;
import cranks.geom.Angle;
import cranks.geom.GeometricalObject;

/**
 * This class is used to translate and rotate objects
 */

public class Move extends EditDialog implements PropertyChangeListener,
                                             ItemListener, ActionListener {

  public static final int MOVE = 1;

  JOptionPane optionPane;

  JPanel pMainPanel = new JPanel();

  JPanel pSelectObject = new JPanel();
  JComboBox cSelectObject = new JComboBox();

  JPanel pTranslation = new JPanel();
  JCheckBox cbEnableTranslation = new JCheckBox("Enable");
  JLabel lSelectLine = new JLabel("Select Line");
  JComboBox cSelectLine = new JComboBox();
  JLabel lTranslation = new JLabel("Translation (X,Y)");
  JTextField tfTranslationX = new JTextField();
  JTextField tfTranslationY = new JTextField();

  JPanel pRotation = new JPanel();
  JCheckBox cbEnableRotation = new JCheckBox("Enable");
  JLabel lSelectPoint = new JLabel("Select Fixed Point");
  JComboBox cSelectPoint = new JComboBox();
  JLabel lRotation = new JLabel("Angle (Degrees)");
  JTextField tfRotation = new JTextField();

  public Move(JFrame frame, String title, Vector<GeometricalObject> Objects) {
    super(frame, title, false, Objects);
    setID(10);
    jbInit();
    pack();
  }

  private void jbInit() {

    pMainPanel.setLayout(new BoxLayout(pMainPanel, BoxLayout.PAGE_AXIS));

    pSelectObject.setBorder(BorderFactory.createTitledBorder("Select Object"));
    pSelectObject.add(cSelectObject, BorderLayout.CENTER);

    pTranslation.setBorder(BorderFactory.createCompoundBorder(
                           BorderFactory.createTitledBorder("Translation"),
                           BorderFactory.createEmptyBorder(10,10,10,10)));
    pTranslation.setLayout(new GridLayout(2, 3, 20, 10));
    pTranslation.add(cbEnableTranslation);
    cbEnableTranslation.addItemListener(this);
    pTranslation.add(lSelectLine);
    pTranslation.add(cSelectLine);
    cSelectLine.addActionListener(this);
    pTranslation.add(lTranslation);
    pTranslation.add(tfTranslationX);
    pTranslation.add(tfTranslationY);

    pRotation.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Rotation"),
                        BorderFactory.createEmptyBorder(10,10,10,10)));
    pRotation.setLayout(new GridLayout(2, 3, 20, 10));
    pRotation.add(cbEnableRotation);
    cbEnableRotation.addItemListener(this);
    pRotation.add(lSelectPoint);
    pRotation.add(cSelectPoint);
    pRotation.add(lRotation);
    pRotation.add(tfRotation);

    pMainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    pMainPanel.add(pSelectObject);
    pMainPanel.add(pTranslation);
    pMainPanel.add(pRotation);

    Object[] options = {"Ok", "Copy Field Value", "Cancel"};
    optionPane = new JOptionPane(pMainPanel, JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);
    setContentPane(optionPane);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new JDialogWindowAdapter(this));
    optionPane.addPropertyChangeListener(this);
  }

  public void itemStateChanged(ItemEvent e) {
    if ((e.getSource() == cbEnableTranslation) ||
        (e.getSource() == cbEnableRotation)) {
      setEnabled();
      if (cbEnableTranslation.isSelected() && cbEnableRotation.isSelected())
        JOptionPane.showMessageDialog(this,
          "       Rotation will be performed after translation.\n"
         +"If Fixed Point is part of Object, it will also translate.\n"
         +"  Hence, rotation will occur about new Fixed Point.",
          "Warning", JOptionPane.WARNING_MESSAGE);
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == cSelectLine) {
      Line L1 = (Line)cSelectLine.getSelectedItem();
      if (L1 != null) {
        tfTranslationX.setText(
            Double.toString(L1.getEnd().getX() - L1.getStart().getX()));
        tfTranslationY.setText(
            Double.toString(L1.getEnd().getY() - L1.getStart().getY()));
      }
      else {
        tfTranslationX.setText("");
        tfTranslationY.setText("");
      }
    }
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
        moveObject();
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

  private void moveObject() {
    double translationX = 0;
    double translationY = 0;
    Point fixedPoint = new Point();
    double rotation = 0;
    if (cbEnableTranslation.isSelected())
      try {
        translationX = Double.parseDouble(tfTranslationX.getText());
        translationY = Double.parseDouble(tfTranslationY.getText());
      }
      catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Please enter numerical value",
                                      "Try Again", JOptionPane.ERROR_MESSAGE);
        return;
      }
    if (cbEnableRotation.isSelected()) {
      try {
        fixedPoint = (Point)cSelectPoint.getSelectedItem();
        rotation = Math.toRadians(Double.parseDouble(tfRotation.getText()));
      }
      catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Please enter numerical value",
                                      "Try Again", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    GeometricalObject o = (GeometricalObject)cSelectObject.getSelectedItem();
    ConstructionStep step = new ConstructionStep(this, new Object[] {o,
              new Double(translationX), new Double(translationY),
              new Double(rotation), fixedPoint}, MOVE);
    doConstruction(step);
  }

  public void doConstruction(ConstructionStep step) {
    if (step.getConstructionType() == MOVE) {
      GeometricalObject o = (GeometricalObject)step.getInputs()[0];
      Point translation = new Point(((Double)step.getInputs()[1]).doubleValue(),
                                    ((Double)step.getInputs()[2]).doubleValue());
      Angle rotation = new Angle(((Double)step.getInputs()[3]).doubleValue());
      Point fixedPoint = (Point)step.getInputs()[4];
      if (fixedPoint.getAssocObjects().contains(o)) {
        fixedPoint = new Point(fixedPoint.getX(), fixedPoint.getY());
        fixedPoint.move(translation, new Angle(0), new Point());
        step.getInputs()[4] = fixedPoint;
      }
      o.move(translation, rotation, fixedPoint);
      clearAndHide();
      step.setOutputs(new Object[]{o});
      fireUndoableEditUpdate(new UndoableEditEvent(this, step));
    }
    else
      System.err.println("Bad construction type");
  }

  public void undoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == MOVE) {
      GeometricalObject o = (GeometricalObject)step.getInputs()[0];
      Point translation = new Point(((Double)step.getInputs()[1]).doubleValue(),
                                    ((Double)step.getInputs()[2]).doubleValue());
      Angle rotation = new Angle(((Double)step.getInputs()[3]).doubleValue());
      Point fixedPoint = (Point)step.getInputs()[4];
      rotation = rotation.mul(-1);
      translation = new Point(translation.getX()*(-1), translation.getY()*(-1));
      o.move(new Point(), rotation, fixedPoint);
      o.move(translation, new Angle(0), fixedPoint);
    }
    else
      System.err.println("Bad construction type");
  }

  public void redoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == MOVE) {
      GeometricalObject o = (GeometricalObject)step.getInputs()[0];
      Point translation = new Point(((Double)step.getInputs()[1]).doubleValue(),
                                    ((Double)step.getInputs()[2]).doubleValue());
      Angle rotation = new Angle(((Double)step.getInputs()[3]).doubleValue());
      Point fixedPoint = (Point)step.getInputs()[4];
      o.move(translation, rotation, fixedPoint);
    }
    else
      System.err.println("Bad construction type");
  }

  public void clearAndHide() {
    dispose();
  }

  public void initDialog() {
    cbEnableTranslation.setSelected(false);
    cbEnableRotation.setSelected(false);
    setEnabled();
    showObjects();
    tfTranslationX.setText("");
    tfTranslationY.setText("");
    tfRotation.setText("");
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void showObjects() {
    cSelectObject.removeAllItems();
    cSelectLine.removeAllItems();
    cSelectPoint.removeAllItems();
    for (int i = objects.size()-1; i >= 0; i--) {
      GeometricalObject o = (GeometricalObject)(objects.elementAt(i));
      cSelectObject.addItem(o);
      if (o.getType() == Line.TYPE)
        cSelectLine.addItem(o);
      if (o.getType() == Point.TYPE)
        cSelectPoint.addItem(o);
    }
  }

  private void setEnabled() {
    boolean enable = cbEnableTranslation.isSelected();
    for (int i = 1; i<pTranslation.getComponentCount(); i++)
      pTranslation.getComponent(i).setEnabled(enable);
    enable = cbEnableRotation.isSelected();
    for (int i = 1; i<pRotation.getComponentCount(); i++)
      pRotation.getComponent(i).setEnabled(enable);
  }
  public String getPresentationName(ConstructionStep step) {
    if (step.getConstructionType() == MOVE)
      return ("Move " + step.getInputs()[0]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    if (step.getConstructionType() == MOVE)
      return ("Translate " + step.getInputs()[0] + " by " +
         ((Point)step.getInputs()[1]).locationToString() + " units and rotate it by " +
         Math.toDegrees(((Double)step.getInputs()[2]).doubleValue()) + " about " +
         step.getInputs()[3]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

}
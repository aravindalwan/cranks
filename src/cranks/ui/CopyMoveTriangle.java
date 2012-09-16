/*
 * @(#)CopyMoveTriangle.java 1.0
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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.undo.ConstructionStep;
import cranks.geom.Triangle;
import cranks.geom.Line;
import cranks.geom.Point;
import cranks.geom.Angle;
import cranks.geom.GeometricalObject;

/**
 * This class is used to create a copy of a triangle with one of its sides
 * aligned with another line of the same length as the side
 */

public class CopyMoveTriangle extends EditDialog implements PropertyChangeListener,
                                      ActionListener {
  public static final int COPY_MOVE = 1;

  JOptionPane optionPane;
  JPanel pMainPanel = new JPanel();

  JPanel pObjectSelection = new JPanel();
  JLabel lTriangle = new JLabel("Select Triangle");
  JComboBox cTriangle = new JComboBox();
  JLabel lPickSide = new JLabel("Pick Side to align");
  JComboBox cPickSide = new JComboBox();
  JLabel lNewSide = new JLabel("Select new Side");
  JComboBox cNewSide = new JComboBox();
  JPanel pOrientation = new JPanel();
  JLabel lOrientation = new JLabel("");
  JComboBox cSelectOrientation = new JComboBox();

  private boolean initialized;

  public CopyMoveTriangle(JFrame frame, String title,
                          Vector<GeometricalObject> Objects) {
    super(frame, title, false, Objects);
    setID(5);
    jbInit();
    pack();
    initialized = false;
  }

  private void jbInit() {

    pMainPanel.setLayout(new GridLayout(2,1));

    pObjectSelection.setLayout(new GridLayout(4,2,20,10));
    pObjectSelection.setBorder(BorderFactory.createEmptyBorder(20,20,0,20));
    pObjectSelection.add(lTriangle);
    pObjectSelection.add(cTriangle);
    cTriangle.addActionListener(this);
    cTriangle.setActionCommand("Triangle");
    pObjectSelection.add(lPickSide);
    pObjectSelection.add(cPickSide);
    cPickSide.addActionListener(this);
    cPickSide.setActionCommand("Old Side");
    pObjectSelection.add(lNewSide);
    pObjectSelection.add(cNewSide);
    cNewSide.addActionListener(this);
    cNewSide.setActionCommand("New Side");

    pOrientation.setLayout(new BoxLayout(pOrientation, BoxLayout.Y_AXIS));
    pOrientation.setBorder(BorderFactory.createCompoundBorder(
                              BorderFactory.createEmptyBorder(5,20,5,20),
                              BorderFactory.createTitledBorder("Orientation")));
    pOrientation.add(lOrientation);
    pOrientation.add(cSelectOrientation);
    lOrientation.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    lOrientation.setHorizontalAlignment(SwingConstants.CENTER);
    cSelectOrientation.setMaximumSize(new Dimension(100, 25));
    pMainPanel.add(pObjectSelection);
    pMainPanel.add(pOrientation);

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
        copyAndMoveTriangle();
      }
      else { //user closed dialog or clicked cancel
        constructionCancelled();
        clearAndHide();
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (initialized && (e.getActionCommand().equalsIgnoreCase("Triangle"))) {
      Triangle T1 = (Triangle)(cTriangle.getSelectedItem());
      Line[] sides = T1.getSides();
      cPickSide.removeAllItems();
      for (int i = 0; i<sides.length; i++)
        cPickSide.addItem(sides[i]);
      lPickSide.setVisible(true);
      cPickSide.setVisible(true);
    }
    if (initialized && ((e.getActionCommand().equalsIgnoreCase("Old Side")) ||
                        (e.getActionCommand().equalsIgnoreCase("New Side")))) {
      Line selectedSide = (Line)(cPickSide.getSelectedItem());
      Line newSide = (Line)(cNewSide.getSelectedItem());
      lOrientation.setText("<HTML><CENTER>Select final position of<p>" +
                           selectedSide.getStart() + " of " + selectedSide +
                           "<p>after move</CENTER></HTML>");
      cSelectOrientation.removeAllItems();
      cSelectOrientation.addItem(newSide.getStart());
      cSelectOrientation.addItem(newSide.getEnd());
      pOrientation.setVisible(true);
      lOrientation.setVisible(true);
      cSelectOrientation.setVisible(true);
    }
  }

  private void copyAndMoveTriangle() {
    Triangle tri = (Triangle)(cTriangle.getSelectedItem());
    Line selectedSide = (Line)(cPickSide.getSelectedItem());
    Line newSide = (Line)(cNewSide.getSelectedItem());
    boolean orientation = (cSelectOrientation.getSelectedIndex() == 0);
    ConstructionStep step = new ConstructionStep(this, new Object[] {tri,
              selectedSide, newSide, new Boolean(orientation)}, COPY_MOVE);
    doConstruction(step);
  }

  public void doConstruction(ConstructionStep step) {
    if (step.getConstructionType() == COPY_MOVE) {
      Triangle tri = (Triangle)step.getInputs()[0];
      Line selectedSide = (Line)step.getInputs()[1];
      Line newSide = (Line)step.getInputs()[2];
      boolean orientation = ((Boolean)step.getInputs()[3]).booleanValue();
      if (Math.abs(selectedSide.getLength()-newSide.getLength()) < Point.LEAST_COUNT) {
        Point[] vertices = tri.getVertices();
        Point ptOppSelSide = new Point();
        if (vertices[0].canIntersect(selectedSide))
          if (vertices[1].canIntersect(selectedSide))
            ptOppSelSide.setCoordinates(vertices[2].getX(), vertices[2].getY());
          else
            ptOppSelSide.setCoordinates(vertices[1].getX(), vertices[1].getY());
        else
          ptOppSelSide.setCoordinates(vertices[0].getX(), vertices[0].getY());

        Line line1 = (orientation)?
                  (new Line(selectedSide.getStart(), newSide.getStart())):
                  (new Line(selectedSide.getStart(), newSide.getEnd()));
        Line line2 = (orientation)?
                  (new Line(selectedSide.getEnd(), newSide.getEnd())):
                  (new Line(selectedSide.getEnd(), newSide.getStart()));
        Point centreOfRotation;
        Angle angleOfRotation;
        if (line1.getLength() < Point.LEAST_COUNT) {
          centreOfRotation = new Point(line1.getStart().getX(),line1.getStart().getY());
          angleOfRotation = (new Line(centreOfRotation, line2.getEnd())).getSlope().
                    sub((new Line(centreOfRotation, line2.getStart())).getSlope());
        }
        else if (line2.getLength() < Point.LEAST_COUNT) {
          centreOfRotation = new Point(line2.getStart().getX(),line2.getStart().getY());
          angleOfRotation = (new Line(centreOfRotation, line1.getEnd())).getSlope().
                    sub((new Line(centreOfRotation, line1.getStart())).getSlope());
        }
        else {
          Line perpLine1 = new Line(line1.midPoint(),line1.getSlope().
                              add(new Angle(Math.PI/2)),1);
          Line perpLine2 = new Line(line2.midPoint(),line2.getSlope().
                              add(new Angle(Math.PI/2)),1);
          if (perpLine1.canIntersect(perpLine2)) {
            centreOfRotation = perpLine1.intersect(perpLine2)[0];
            angleOfRotation = (new Line(centreOfRotation, line1.getEnd())).getSlope().
                    sub((new Line(centreOfRotation, line1.getStart())).getSlope());
          }
          else {
            Point translation = new Point(
                      line1.getEnd().getX() - line1.getStart().getX(),
                      line1.getEnd().getY() - line1.getStart().getY());
            ptOppSelSide.move(translation, new Angle(0), new Point());
            Triangle newTriangle = new Triangle(ptOppSelSide, newSide);
            newTriangle.addToObjects(objects);
            clearAndHide();
            step.setOutputs(new Object[]{ptOppSelSide, newTriangle.getSides()[1],
                        newTriangle.getSides()[2], newTriangle});
            fireUndoableEditUpdate(new UndoableEditEvent(this, step));
            return;
          }
        }
        ptOppSelSide.move(new Point(), angleOfRotation, centreOfRotation);
        Triangle newTriangle = new Triangle(ptOppSelSide, newSide);
        newTriangle.addToObjects(objects);
        clearAndHide();
        step.setOutputs(new Object[]{ptOppSelSide, newTriangle.getSides()[1],
                    newTriangle.getSides()[2], newTriangle});
        fireUndoableEditUpdate(new UndoableEditEvent(this, step));
      }
      else {
        JOptionPane.showMessageDialog(this, "Lengths of old & new sides must be equal",
                        "Try Again", JOptionPane.ERROR_MESSAGE);
        initDialog();
      }
    }
    else
      System.err.println("Bad construction type");
  }

  public void undoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == COPY_MOVE) {
      ((GeometricalObject)step.getOutputs()[0]).removeFromObjects(objects);
      for (int i = 0; i<objects.size(); i++)
        objects.elementAt(i).setNumber(i+1);
    }
    else
      System.err.println("Bad construction type");
  }

  public void redoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == COPY_MOVE) {
      for (int i = 0; i<step.getOutputs().length; i++)
      ((GeometricalObject)step.getOutputs()[i]).addToObjects(objects);
    }
    else
      System.err.println("Bad construction type");
  }

  public void initDialog() {
    initialized = false;
    showTriangles();
    lPickSide.setVisible(false);
    cPickSide.setVisible(false);
    pOrientation.setVisible(false);
    lOrientation.setVisible(false);
    cSelectOrientation.setVisible(false);
    initialized = true;
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void showTriangles() {
    cTriangle.removeAllItems();
    cPickSide.removeAllItems();
    cNewSide.removeAllItems();
    cSelectOrientation.removeAllItems();
    lOrientation.setText("");
    for (int i = (objects.size() - 1); i>=0; i--) {
      GeometricalObject o = objects.elementAt(i);
      if (o.getType() == Line.TYPE)
        cNewSide.addItem(o);
      if (o.getType() == Triangle.TYPE)
        cTriangle.addItem(o);
    }
  }

  public void clearAndHide() {
    dispose();
    initialized = false;
  }

  public String getPresentationName(ConstructionStep step) {
    if (step.getConstructionType() == COPY_MOVE)
      return ("Copy & Move " + step.getInputs()[0]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    if (step.getConstructionType() == COPY_MOVE) {
      String displayName = "Copy " + step.getInputs()[0] + " and move onto " +
         step.getInputs()[2]+", aligning "+((Line)step.getInputs()[1]).getStart()+
         " of "+step.getInputs()[1]+" with ";
      displayName = (((Boolean)step.getInputs()[3]).booleanValue())?
                      (displayName + ((Line)step.getInputs()[2]).getStart()):
                      (displayName + ((Line)step.getInputs()[2]).getEnd());
      displayName +=  " - Output: ";
      for(int i = 0; i < step.getOutputs().length - 1; i++)
        displayName += step.getOutputs()[i] + ", ";
      displayName += step.getOutputs()[step.getOutputs().length - 1];
      return displayName;
    }
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

}
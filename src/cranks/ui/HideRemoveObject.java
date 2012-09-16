/*
 * @(#)HideRemoveObject.java 1.0
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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.geom.GeometricalObject;
import cranks.undo.ConstructionStep;

/**
 * This class is used to hide objects or to remove them from objects vector.
 * Note: Removal of a point/line also removes other connected objects
 */

public class HideRemoveObject extends EditDialog implements PropertyChangeListener {

  public static final int REMOVE_OBJECT = 1;
  public static final int HIDE_OBJECT = 2;

  JTabbedPane tabbedPane = new JTabbedPane();
  JOptionPane optionPane;

  JPanel pRemoveObject = new JPanel();
  JLabel lRemoveObject = new JLabel("Select Object");
  JComboBox cRemoveObject = new JComboBox();

  JPanel pHideObject = new JPanel();
  JPanel pObjects = new JPanel();
  JScrollPane scrollPane = new JScrollPane(pObjects);

  public HideRemoveObject(JFrame frame, String title,
                          Vector<GeometricalObject> Objects) {
    super(frame, title, false, Objects);
    setID(7);
    jbInit();
    pack();
  }

  private void jbInit() {

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 0.5;
    gbc.weighty = 0.5;
    gbc.insets = new Insets(10,5,10,5);

    pRemoveObject.setLayout(new GridBagLayout());
    pRemoveObject.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    gbc.gridx = 0;
    gbc.gridy = 0;
    pRemoveObject.add(lRemoveObject, gbc);
    gbc.gridx = 1;
    pRemoveObject.add(cRemoveObject, gbc);

    pHideObject.setLayout(new GridLayout(1,0));
    pObjects.setLayout(new BoxLayout(pObjects, BoxLayout.PAGE_AXIS));
    scrollPane.setPreferredSize(new Dimension(200, 200));
    pHideObject.add(scrollPane);

    tabbedPane.addTab("Hide Object", null, pHideObject,
                    "Change visibility status of objects");
    tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
    //tabbedPane.addTab("Remove Object", null, pRemoveObject,
    //                "Remove object");
    //tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

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
        hideOrRemoveObject();
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

  private void hideOrRemoveObject() {
    if (tabbedPane.getSelectedComponent() == pHideObject) {
      Vector<Integer> selectedObj = new Vector<Integer>();
      for (int i = 0; i<pObjects.getComponentCount(); i++)
        if (((JCheckBox)pObjects.getComponent(i)).isSelected())
          selectedObj.addElement(new Integer(pObjects.getComponentCount() - i));
      Boolean[] visibilityToggled = new Boolean[objects.size()];
      for (int i = 0; i<objects.size(); i++)
        if (objects.elementAt(i).isVisible()) {
          visibilityToggled[i] = Boolean.FALSE;
          for (int j = 0; j<selectedObj.size(); j++)
            if (selectedObj.elementAt(j).intValue() - 1 == i) {
              visibilityToggled[i] = Boolean.TRUE;
              break;
            }
        }
        else {
          visibilityToggled[i] = Boolean.TRUE;
          for (int j = 0; j<selectedObj.size(); j++)
            if (selectedObj.elementAt(j).intValue() - 1 == i) {
              visibilityToggled[i] = Boolean.FALSE;
              break;
            }
        }
      ConstructionStep step = new ConstructionStep(this, visibilityToggled,
                                                    HIDE_OBJECT);
      doConstruction(step);
    }
    if (tabbedPane.getSelectedComponent() == pRemoveObject) {
      GeometricalObject ob = (GeometricalObject)cRemoveObject.getSelectedItem();
      ConstructionStep step = new ConstructionStep(this, new Object[]{ob},
                                                    REMOVE_OBJECT);
      doConstruction(step);
    }
  }

  public void doConstruction(ConstructionStep step) {
    if (step.getConstructionType() == HIDE_OBJECT) {
      Boolean[] visibilityToggled = (Boolean[])step.getInputs();
      for (int i = 0; i<objects.size(); i++)
        if (visibilityToggled[i].booleanValue())
          objects.elementAt(i).toggleVisible();
      clearAndHide();
      step.setOutputs(visibilityToggled);
      fireUndoableEditUpdate(new UndoableEditEvent(this, step));
    }
    else if (step.getConstructionType() == REMOVE_OBJECT) {
      GeometricalObject ob = (GeometricalObject)step.getInputs()[0];
      ob.removeFromObjects(objects);
      for (int i = 0; i<objects.size(); i++)
        objects.elementAt(i).setNumber(i+1);
      clearAndHide();
      step.setOutputs(new Object[]{});
      fireIrreversibleEditUpdate();
    }
    else
      System.err.println("Bad construction type");
  }

  public void undoConstruction(ConstructionStep step) {
    if (step.getConstructionType() == HIDE_OBJECT) {
      Boolean[] visibilityToggled = (Boolean[])step.getOutputs();
      for (int i = 0; i<objects.size(); i++)
        if (visibilityToggled[i].booleanValue())
          objects.elementAt(i).toggleVisible();
    }
    else
      System.err.println("Bad construction type");
  }

  public void redoConstruction(ConstructionStep step) {
    undoConstruction(step);
  }

  public void initDialog() {
    showObjects();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void showObjects() {
    cRemoveObject.removeAllItems();
    pObjects.removeAll();
    for(int t = (objects.size()-1); t >= 0; t--) {
      cRemoveObject.addItem(objects.elementAt(t));
      JCheckBox checkbox = new JCheckBox(objects.elementAt(t).toString(),
                                       !(objects.elementAt(t).isVisible()));
      pObjects.add(checkbox);
    }
  }

  public String getPresentationName(ConstructionStep step) {
    if (step.getConstructionType() == HIDE_OBJECT)
      return "(Un)Hide Objects";
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    if (step.getConstructionType() == HIDE_OBJECT) {
      String displayName = "Toggle visibility of ";
      for (int i = 0; i<step.getInputs().length; i++)
        if (((Boolean)step.getInputs()[i]).booleanValue())
          displayName += (i+1) + ", ";
      displayName = displayName.substring(0, displayName.length() - 2);
      return displayName;
    }
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

}
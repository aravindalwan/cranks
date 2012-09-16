/*
 * @(#)FieldValue.java 1.0
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.geom.Triangle;
import cranks.geom.Line;
import cranks.geom.Circle;
import cranks.geom.Point;
import cranks.geom.GeometricalObject;

/**
 * This class brings up a dialog to display the values of sub-fields of objects
 * in the main object vector in the form of a tree. The value of any selected
 * field is displayed in a text field. This displayed value can be dragged and
 * dropped onto any text field in any other dialog.
 */

public class FieldValue extends JDialog implements PropertyChangeListener,
                                      TreeSelectionListener {
  JOptionPane optionPane;

  JPanel pMainPanel = new JPanel();
  JTree tree;
  DefaultMutableTreeNode root = new DefaultMutableTreeNode("List of Objects");
  DefaultMutableTreeNode[] objectType = new DefaultMutableTreeNode[4];
  JScrollPane scrollPane;
  JPanel pDisplayPanel = new JPanel();
  JTextField tfDisplayValue = new JTextField(5);

  Vector<GeometricalObject> objects;

  public FieldValue(JFrame frame, String title, Vector<GeometricalObject> Objects) {
    super(frame, title, false);
    objects = Objects;
    jbInit();
    pack();
  }

  private void jbInit() {

    pMainPanel.setLayout(new BorderLayout());
    pMainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

    initializeTree();
    pDisplayPanel.setLayout(new GridLayout(1,0));
    pDisplayPanel.setBorder(BorderFactory.createTitledBorder("Field Value"));
    tfDisplayValue.setEditable(false);
    tfDisplayValue.setDragEnabled(true);
    pDisplayPanel.add(tfDisplayValue);

    pMainPanel.add(pDisplayPanel, BorderLayout.PAGE_END);

    optionPane = new JOptionPane(pMainPanel, JOptionPane.PLAIN_MESSAGE,
                                            JOptionPane.DEFAULT_OPTION);
    setContentPane(optionPane);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new JDialogWindowAdapter(this));
    optionPane.addPropertyChangeListener(this);
  }

  /** Required by TreeSelectionListener interface. */
  public void valueChanged(TreeSelectionEvent e) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                       tree.getLastSelectedPathComponent();

    if (node == null) return;

    if (node.isLeaf()) {
      if (!((node.getUserObject()) instanceof String)) { //String
        FieldInfo fi = (FieldInfo)node.getUserObject();
        tfDisplayValue.setText(fi.fieldDisplayValue());
        return;
      }
    }
    tfDisplayValue.setText("");
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
      clearAndHide();
    }
  }

  public void initDialog() {
    setLocationRelativeTo(null);
    initializeTree();
    setVisible(true);
  }

  private void initializeTree() {
    if (scrollPane != null)
      pMainPanel.remove(scrollPane);
    root.removeAllChildren();
    //Create the nodes.
    objectType[0] = new DefaultMutableTreeNode("Points");
    objectType[1] = new DefaultMutableTreeNode("Lines");
    objectType[2] = new DefaultMutableTreeNode("Circles");
    objectType[3] = new DefaultMutableTreeNode("Triangles");
    for (int i = 0; i<4; i++) {
      root.add(objectType[i]);
      createNodes(objectType[i], i+2);
    }

    //Create a tree that allows one selection at a time.
    tree = new JTree(root);
    tree.getSelectionModel().setSelectionMode
                            (TreeSelectionModel.SINGLE_TREE_SELECTION);
    //Listen for changes in selection.
    tree.addTreeSelectionListener(this);
    //Create the scroll pane and add the tree to it.
    scrollPane = new JScrollPane(tree);
    pMainPanel.add(scrollPane, BorderLayout.CENTER);
  }

  private void createNodes(DefaultMutableTreeNode top, int type) {
    DefaultMutableTreeNode object = null;
    DefaultMutableTreeNode field = null;
    DefaultMutableTreeNode subField = null;
    Vector<GeometricalObject> assocObjects;
    for (int i = 0; i<objects.size(); i++) {
      GeometricalObject o = objects.elementAt(i);
      if (o.getType() == type) {//should be added to top
        object = new DefaultMutableTreeNode(String.valueOf(o.getNumber()));
        top.add(object);

        switch (type) {
          case 2: //point
            Point p = ((Point)o);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("X Co-ordinate", p.getX()));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Y Co-ordinate", p.getY()));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Visibility", p.isVisible()));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("AssociatedObjects", "Empty"));
            object.add(field);
            assocObjects = p.getAssocObjects();
            for (int j = 0; j<assocObjects.size(); j++) {
              GeometricalObject ob = assocObjects.elementAt(j);
              subField = new DefaultMutableTreeNode(
                new FieldInfo(Integer.toString(ob.getNumber()), ob));
              field.add(subField);
            }
          break;
          case 3: //line
            Line l = ((Line)o);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Start Point", l.getStart()));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("End Point", l.getEnd()));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Slope (Degrees)", 
                                    Math.toDegrees(l.getSlope().getAngle())));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Length", l.getLength()));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Visibility", l.isVisible()));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("AssociatedObjects", "Empty"));
            object.add(field);
            assocObjects = l.getAssocObjects();
            for (int j = 0; j<assocObjects.size(); j++) {
              GeometricalObject ob = assocObjects.elementAt(j);
              subField = new DefaultMutableTreeNode(
                new FieldInfo(Integer.toString(ob.getNumber()), ob));
              field.add(subField);
            }
          break;
          case 4:
            Circle c = ((Circle)o);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Centre Point", c.getCentre()));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Radius", c.getRadius()));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Visibility", c.isVisible()));
            object.add(field);
          break;
          case 5:
            Triangle t = ((Triangle)o);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Side 1", t.getSides()[0]));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Side 2", t.getSides()[1]));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Side 3", t.getSides()[2]));
            object.add(field);
            field = new DefaultMutableTreeNode(
                      new FieldInfo("Visibility", t.isVisible()));
            object.add(field);
          break;
          default :
        }
      }
    }
  }

  public void clearAndHide() {
    dispose();
  }

}

class FieldInfo {
  
  private String fieldName;
  private Object fieldObject;

  public FieldInfo(String field, Object FieldObject) {
    fieldName = field;
    fieldObject = FieldObject;
  }

  public String toString() {
    return fieldName;
  }

  public String fieldDisplayValue() {
    return fieldObject.toString();
  }
}


/*
 * @(#)CreateMechanism.java 1.0
 * Copyright (C) 2005 Aravind Alwan
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.ui.MenuAction;
import cranks.geom.Triangle;
import cranks.geom.Line;
import cranks.geom.Circle;
import cranks.geom.Point;
import cranks.geom.Angle;
import cranks.geom.GeometricalObject;
import cranks.mech.FourBarMechanism;

/**
 * This class is used to create a mechanism from an existing drawing, by
 * asking the user to specify the required links from among the objects drawn.
 */

public class CreateMechanism extends JDialog implements PropertyChangeListener,
                                                 ActionListener {

  JPanel pMainPanel = new JPanel();
  JOptionPane optionPane;

  JPanel pLinks = new JPanel();
  JLabel lLink1 = new JLabel("Link 1");
  JComboBox cLink1 = new JComboBox();
  JLabel lLink2 = new JLabel("Link 2");
  JComboBox cLink2 = new JComboBox();
  JLabel lLink3 = new JLabel("Link 3");
  JComboBox cLink3 = new JComboBox();
  JLabel lLink4 = new JLabel("Link 4");
  JComboBox cLink4 = new JComboBox();
  JLabel lFixedLink = new JLabel("Fixed Link");
  JComboBox cFixedLink = new JComboBox();

  JPanel pTernary = new JPanel();
  JRadioButton rbTernary = new JRadioButton("Enable");
  JLabel lTernaryLink = new JLabel("Ternary Link");
  JComboBox cTernaryLink = new JComboBox();

  MainFrame mfInstance;
  Vector<GeometricalObject> objects;
  FourBarMechanism mechanism;

  public CreateMechanism(JFrame frame, String title,
            Vector<GeometricalObject> Objects, FourBarMechanism Mechanism) {
    super(frame, title, false);
    mfInstance = (MainFrame)frame;
    objects = Objects;
    mechanism = Mechanism;
    jbInit();
    pack();
  }

  private void jbInit() {

    pMainPanel.setLayout(new BoxLayout(pMainPanel, BoxLayout.PAGE_AXIS));
    pMainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    pLinks.setLayout(new GridLayout(6, 2, 20, 10));
    pLinks.setBorder(BorderFactory.createEmptyBorder(0,20,0,20));
    pLinks.add(lLink1);
    pLinks.add(cLink1);
    pLinks.add(lLink2);
    pLinks.add(cLink2);
    pLinks.add(lLink3);
    pLinks.add(cLink3);
    pLinks.add(lLink4);
    pLinks.add(cLink4);
    pLinks.add(lFixedLink);
    pLinks.add(cFixedLink);
    for (int i = 0; i<4; i++)
      cFixedLink.addItem("Link " + Integer.toString(i + 1));

    pTernary.setLayout(new GridLayout(1, 3, 20, 10));
    pTernary.setBorder(BorderFactory.createCompoundBorder(
                       BorderFactory.createTitledBorder("Ternary Link"),
                       BorderFactory.createEmptyBorder(10,20,10,20)));
    pTernary.add(rbTernary);
    rbTernary.setActionCommand("Enable Ternary Link");
    rbTernary.addActionListener(this);
    pTernary.add(lTernaryLink);
    pTernary.add(cTernaryLink);

    pMainPanel.add(pLinks);
    pMainPanel.add(pTernary);

    Object[] options = {"Ok", "Cancel"};
    optionPane = new JOptionPane(pMainPanel, JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
    setContentPane(optionPane);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new JDialogWindowAdapter(this));
    optionPane.addPropertyChangeListener(this);

  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Enable Ternary Link"))
      setEnabled();
  }

  private void setEnabled() {
    boolean enable = rbTernary.isSelected();
    for (int i = 1; i<pTernary.getComponentCount(); i++)
      pTernary.getComponent(i).setEnabled(enable);
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
        extractMechanismFromDrawing();
      }
      else { //user closed dialog or clicked cancel
        clearAndHide();
      }
    }
  }

  private void extractMechanismFromDrawing() {
    Line[] lines = new Line[4];
    lines[0] = (Line)cLink1.getSelectedItem();
    lines[1] = (Line)cLink2.getSelectedItem();
    lines[2] = (Line)cLink3.getSelectedItem();
    lines[3] = (Line)cLink4.getSelectedItem();
    for (int i = 0; i<4; i++) {
      int nextLink = (i == 3)?(0):(i + 1);
      int prevLink = (i == 0)?(3):(i - 1);
      if ((!(test(lines[i].getStart(), lines[prevLink]) &&
             test(lines[i].getEnd(), lines[nextLink]))    &&
           !(test(lines[i].getStart(), lines[nextLink]) &&
             test(lines[i].getEnd(), lines[prevLink])))     ||
          (lines[i].equals(lines[nextLink]))) {
        JOptionPane.showMessageDialog(this, "Links are not properly connected",
                        "Try Again", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    String fixedLink = (String)cFixedLink.getSelectedItem();
    int fixed = Integer.parseInt(fixedLink.substring(fixedLink.length()-1));
    double ternaryLength = 0;
    Angle ternaryAngle = new Angle();
    if (rbTernary.isSelected()) {
      Triangle ternaryLink = (Triangle)cTernaryLink.getSelectedItem();
      Line link3 = (fixed > 2)?(lines[(fixed-1)-2]):(lines[(fixed-1)+2]);
      if (!(link3.getAssocObjects().contains(ternaryLink))) {
        JOptionPane.showMessageDialog(this, "Ternary link is not connected to "+
                  "mechanism", "Try Again", JOptionPane.ERROR_MESSAGE);
        return;
      }
      Line link2 = (fixed == 4)?lines[0]:lines[(fixed - 1) + 1];
      Point intersectionOfLink2Link3 = (test(link3.getStart(), link2))?
                  (link3.getStart()):(link3.getEnd());
      Line ternaryLine = null;
      for (int i = 0; i<3; i++)
        if ( test(intersectionOfLink2Link3, ternaryLink.getSides()[i]) &&
             !ternaryLink.getSides()[i].equals(link3))
           ternaryLine = ternaryLink.getSides()[i];
      ternaryLength = ternaryLine.getLength();
      ternaryAngle = getAngle(ternaryLine, link3);
    }
    
    mechanism.setFixed(1);
    for (int i = 0; i<4; i++)
      mechanism.setChainLink(i + 1, lines[i].getLength());
    mechanism.setTernaryLink(rbTernary.isSelected(),ternaryLength,ternaryAngle);
    mechanism.setFixed(fixed);
    setElbowStatus(lines, fixed);
    mechanism.initialize();
    mechanism.setRedesignable(true);
    mfInstance.ActionEnableAnimationMode.invoke();
    clearAndHide();
  }
  
  private void setElbowStatus(Line[] lines, int fixed) {
    int lineNumber1 = fixed - 1;
    int lineNumber2 = (fixed == 4) ? 0 : fixed;
    int lineNumber3 = (fixed > 2) ? (fixed - 3) : (fixed + 1);
    int lineNumber4 = (fixed == 1) ? 3 : (fixed - 2);
    Point joint23 = lines[lineNumber2].intersect(lines[lineNumber3])[0];
    Point joint34 = lines[lineNumber3].intersect(lines[lineNumber4])[0];
    Point joint41 = lines[lineNumber4].intersect(lines[lineNumber1])[0];
    Circle circleLink3 = new Circle(joint23, lines[lineNumber3].getLength());
    Circle circleLink4 = new Circle(joint41, lines[lineNumber4].getLength());
    mechanism.setElbow(joint34.equalTo(circleLink3.intersect(circleLink4)[0]));
  }

  private boolean test(Point P, Line L) {
    return (L.getStart().equalTo(P) || L.getEnd().equalTo(P));
  }

  private Angle getAngle(Line line1, Line line2) {
    double angleBetweenLines = line1.getSlope().sub(line2.getSlope()).getAngle();
    angleBetweenLines = Math.abs(angleBetweenLines - Math.PI);
    return new Angle(angleBetweenLines);
  }

  public void clearAndHide() {
    dispose();
  }

  public void initDialog() {
    showObjects();
    setLocationRelativeTo(null);
    rbTernary.setSelected(false);
    setEnabled();
    setVisible(true);
  }

  private void showObjects() {
    cLink1.removeAllItems();
    cLink2.removeAllItems();
    cLink3.removeAllItems();
    cLink4.removeAllItems();
    cTernaryLink.removeAllItems();
    for (int i = objects.size() - 1; i >= 0; i--) {
      GeometricalObject o = objects.elementAt(i);
      if (o instanceof Line) {
        cLink1.addItem(o);
        cLink2.addItem(o);
        cLink3.addItem(o);
        cLink4.addItem(o);
      }
      if (o instanceof Triangle) {
        cTernaryLink.addItem(o);
      }
    }
  }

}
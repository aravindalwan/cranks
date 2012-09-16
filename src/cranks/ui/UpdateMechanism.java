/*
 * @(#)UpdateMechanism.java 1.0
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import cranks.mech.FourBarMechanism;
import cranks.geom.Angle;

/**
 * This class is used to update the properties of a mechanism, which are
 * specified by the user through the lengths of its links and their angular
 * relationships.
 */

public class UpdateMechanism extends JDialog implements PropertyChangeListener,
                                                 ActionListener {
  JPanel pMainPanel = new JPanel();
  JOptionPane optionPane;

  JPanel pLinks = new JPanel();
  JLabel lLink1 = new JLabel("Link 1");
  JTextField tfLink1 = new JTextField();
  JLabel lLink2 = new JLabel("Link 2");
  JTextField tfLink2 = new JTextField();
  JLabel lLink3 = new JLabel("Link 3");
  JTextField tfLink3 = new JTextField();
  JLabel lLink4 = new JLabel("Link 4");
  JTextField tfLink4 = new JTextField();
  JLabel lFixedLink = new JLabel("Fixed Link");
  JComboBox cFixedLink = new JComboBox();
  JCheckBox cbElbowUp = new JCheckBox("Elbow Up");

  JPanel pTernary = new JPanel();
  JRadioButton rbTernary = new JRadioButton("Enable");
  JLabel lTernaryLength = new JLabel("Ternary Length");
  JTextField tfTernaryLength = new JTextField();
  JLabel lTernaryAngle = new JLabel("Ternary Angle (Degrees)");
  JTextField tfTernaryAngle = new JTextField();

  JPanel pRotation = new JPanel();
  ButtonGroup bgRotation = new ButtonGroup();
  JRadioButton rbClockwise = new JRadioButton("Clockwise");
  JRadioButton rbAnticlockwise = new JRadioButton("Anticlockwise");

  MainFrame mfInstance;
  FourBarMechanism mechanism;

  public UpdateMechanism(JFrame frame, String title, FourBarMechanism Mechanism) {
    super(frame, title, false);
    mfInstance = (MainFrame)frame;
    mechanism = Mechanism;
    jbInit();
    pack();
  }

  private void jbInit() {

    pMainPanel.setLayout(new BoxLayout(pMainPanel, BoxLayout.PAGE_AXIS));
    pMainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    pLinks.setLayout(new GridLayout(6, 2, 20, 10));
    pLinks.setBorder(BorderFactory.createEmptyBorder(0,20,20,20));
    pLinks.add(lLink1);
    pLinks.add(tfLink1);
    tfLink1.setDragEnabled(true);
    pLinks.add(lLink2);
    pLinks.add(tfLink2);
    tfLink2.setDragEnabled(true);
    pLinks.add(lLink3);
    pLinks.add(tfLink3);
    tfLink3.setDragEnabled(true);
    pLinks.add(lLink4);
    pLinks.add(tfLink4);
    tfLink4.setDragEnabled(true);
    pLinks.add(lFixedLink);
    pLinks.add(cFixedLink);
    for (int i = 0; i<4; i++)
      cFixedLink.addItem("Link " + Integer.toString(i + 1));
    pLinks.add(cbElbowUp);
    cbElbowUp.setSelected(true);

    pTernary.setLayout(new GridLayout(3, 2, 20, 10));
    pTernary.setBorder(BorderFactory.createCompoundBorder(
                       BorderFactory.createTitledBorder("Ternary Link"),
                       BorderFactory.createEmptyBorder(10,20,20,20)));
    pTernary.add(rbTernary);
    rbTernary.setActionCommand("Enable Ternary Link");
    rbTernary.addActionListener(this);
    pTernary.add(new JLabel(""));
    pTernary.add(lTernaryLength);
    pTernary.add(tfTernaryLength);
    tfTernaryLength.setDragEnabled(true);
    pTernary.add(lTernaryAngle);
    pTernary.add(tfTernaryAngle);
    tfTernaryAngle.setDragEnabled(true);

    pRotation.setLayout(new GridLayout(1, 2, 20, 10));
    pRotation.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Rotation"),
                        BorderFactory.createEmptyBorder(10,20,10,20)));
    pRotation.add(rbClockwise);
    rbClockwise.setSelected(true);
    pRotation.add(rbAnticlockwise);
    rbAnticlockwise.setActionCommand("Anticlockwise rotation");
    bgRotation.add(rbClockwise);
    bgRotation.add(rbAnticlockwise);

    pMainPanel.add(pLinks);
    pMainPanel.add(pTernary);
    pMainPanel.add(pRotation);

    Object[] options = {"Ok", "Copy Field Value", "Cancel"};
    optionPane = new JOptionPane(pMainPanel, JOptionPane.PLAIN_MESSAGE,
                   JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);
    setContentPane(optionPane);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new JDialogWindowAdapter(this));
    optionPane.addPropertyChangeListener(this);
    initializeFieldValues();

  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Enable Ternary Link"))
      setEnabled();
  }

  private void setEnabled() {
    for (int i = 1; i<pTernary.getComponentCount(); i++)
      pTernary.getComponent(i).setEnabled(rbTernary.isSelected());
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
        setLinkLengths();
      }
      else if (value.equals("Copy Field Value")) {
        mfInstance.ActionShowFieldValue.invoke();
      }
      else { //user closed dialog or clicked cancel
        clearAndHide();
      }
    }
  }

  private void setLinkLengths() {
    try {
      double[] linkLengths = new double[4];
      linkLengths[0] = Double.parseDouble(tfLink1.getText());
      linkLengths[1] = Double.parseDouble(tfLink2.getText());
      linkLengths[2] = Double.parseDouble(tfLink3.getText());
      linkLengths[3] = Double.parseDouble(tfLink4.getText());
      double ternaryLength = 0;
      Angle ternaryAngle = new Angle();
      if (rbTernary.isSelected()) {
        ternaryLength = Double.parseDouble(tfTernaryLength.getText());
        if (ternaryLength <= 0) {
          JOptionPane.showMessageDialog(this, "Please enter positive value " +
                "for length", "Try Again", JOptionPane.ERROR_MESSAGE);
          return;
        }
        ternaryAngle = new Angle(Math.toRadians(Double.parseDouble(
                                     tfTernaryAngle.getText())));
      }
      for (int i = 0; i<4; i++)
        if (linkLengths[i] <= 0) {
          JOptionPane.showMessageDialog(this, "Please enter positive value " +
                "for length", "Try Again", JOptionPane.ERROR_MESSAGE);
          return;
        }
      String fixedLink = (String)cFixedLink.getSelectedItem();
      int fixed = Integer.parseInt(fixedLink.substring(fixedLink.length()-1));
      int direction = (rbClockwise.isSelected())?(-1):(1);
      boolean elbowUp = cbElbowUp.isSelected();
      FourBarMechanism newMechanism = new FourBarMechanism(linkLengths[0],
                  linkLengths[1], linkLengths[2], linkLengths[3]);
      if (!newMechanism.isMechanism()) {
        JOptionPane.showMessageDialog(this, "This is not a valid mechanism.\n"
          + "Please check link lengths", "Try Again", JOptionPane.ERROR_MESSAGE);
        return;
      }
      mechanism.setFixed(1);
      for (int i = 0; i<4; i++)
        mechanism.setChainLink(i + 1, linkLengths[i]);
      mechanism.setTernaryLink(rbTernary.isSelected(),ternaryLength,ternaryAngle);
      mechanism.setFixed(fixed);
      mechanism.setDirection(direction);
      mechanism.setElbow(elbowUp);
      mechanism.initialize();
      clearAndHide();
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this, "Please enter numerical value ",
                                    "Try Again", JOptionPane.ERROR_MESSAGE);
      return;
    }
  }

  public void clearAndHide() {
    dispose();
  }

  public void initDialog() {
    setLocationRelativeTo(null);
    initializeFieldValues();
    setEnabled();
    setVisible(true);
  }

  private void initializeFieldValues() {
    int index = mechanism.getFixed() - 1 - 4;
    tfLink1.setText(Double.toString(mechanism.getLink(1 - index).getLength()));
    tfLink2.setText(Double.toString(mechanism.getLink(2 - index).getLength()));
    tfLink3.setText(Double.toString(mechanism.getLink(3 - index).getLength()));
    tfLink4.setText(Double.toString(mechanism.getLink(4 - index).getLength()));
    if (mechanism.getLink(3).isTernary()) {
      tfTernaryLength.setText(Double.toString(
                                      mechanism.getLink(1).getTernaryLength()));
      tfTernaryAngle.setText(Double.toString(
            Math.toDegrees(mechanism.getLink(1).getTernaryAngle().getAngle())));
      rbTernary.setSelected(true);
    }
    else {
      tfTernaryLength.setText("");
      tfTernaryAngle.setText("");
      rbTernary.setSelected(false);
    }
    cbElbowUp.setSelected(mechanism.isElbowUp());
    cFixedLink.setSelectedIndex(mechanism.getFixed() - 1);
  }

}
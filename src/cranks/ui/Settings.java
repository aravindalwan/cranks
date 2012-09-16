/*
 * @(#)Settings.java 1.0
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
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import cranks.geom.Angle;
import cranks.mech.FourBarMechanism;

/**
 * This class is used to control the animation speed and angular increment
 * @author Aravind Alwan
 */

public class Settings extends JDialog implements PropertyChangeListener {

  private JPanel pMainPanel = new JPanel();
  private JOptionPane optionPane;

  private JPanel pAnimationDelay = new JPanel();
  private JSlider sAnimationDelay = new JSlider();

  private JPanel pAngularIncrement = new JPanel();
  private JSpinner spAngularIncrement;
  private SpinnerModel spmAngularIncrement;
  private JFormattedTextField ftfAngularIncrement;

  private JPanel pZoom = new JPanel();
  private JSpinner spZoom;
  private SpinnerModel spmZoom;
  private JFormattedTextField ftfZoom;
  
  private FourBarMechanism mechanism;
  private int oldAngularIncrement = 2;
  private MainFrame mfInstance;

  public Settings(JFrame frame,String title,FourBarMechanism Mechanism) {
    super(frame, title, false);
    jbInit();
    pack();
    mechanism = Mechanism;
    mfInstance = (MainFrame)frame;
    updateSettings();
  }

  private void jbInit() {

    pMainPanel.setLayout(new BoxLayout(pMainPanel, BoxLayout.PAGE_AXIS));
    pMainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    pAnimationDelay.setLayout(new BorderLayout());
    pAnimationDelay.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Animation Delay (ms)"),
                        BorderFactory.createEmptyBorder(10,10,10,10)));
    pAnimationDelay.add(sAnimationDelay, BorderLayout.CENTER);
    sAnimationDelay.setMinimum(0);
    sAnimationDelay.setMaximum(200);
    sAnimationDelay.setMajorTickSpacing(50);
    sAnimationDelay.setMinorTickSpacing(5);
    sAnimationDelay.setPaintLabels(true);
    sAnimationDelay.setPaintTicks(true);
    sAnimationDelay.setSnapToTicks(true);
    sAnimationDelay.setValue(0);

    pAngularIncrement.setLayout(new BorderLayout());
    pAngularIncrement.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder("Angular Increment (Degrees)"),
                 BorderFactory.createEmptyBorder(10,80,10,80)));
    spmAngularIncrement = new SpinnerNumberModel(oldAngularIncrement, 1, 20, 1);
    spAngularIncrement = new JSpinner(spmAngularIncrement);
    ftfAngularIncrement = ((JSpinner.DefaultEditor)spAngularIncrement.getEditor()).
                               getTextField();
    ftfAngularIncrement.setHorizontalAlignment(JTextField.CENTER);
    ftfAngularIncrement.setEditable(false);
    pAngularIncrement.add(spAngularIncrement, BorderLayout.CENTER);
    
    pZoom.setLayout(new BorderLayout());
    pZoom.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder("Zoom (%)"),
                 BorderFactory.createEmptyBorder(10,80,10,80)));
    spmZoom = new SpinnerNumberModel(100, 10, 400, 10);
    spZoom = new JSpinner(spmZoom);
    ftfZoom = ((JSpinner.DefaultEditor)spZoom.getEditor()).getTextField();
    ftfZoom.setHorizontalAlignment(JTextField.CENTER);
    ftfZoom.setEditable(false);
    pZoom.add(spZoom, BorderLayout.CENTER);

    pMainPanel.add(pAnimationDelay);
    pMainPanel.add(pAngularIncrement);
    pMainPanel.add(pZoom);
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
        updateSettings();
      }
      else { //user closed dialog or clicked cancel
        clearAndHide();
      }
    }
  }

  public void clearAndHide() {
    dispose();
  }

  public void updateSettings() {
    int angularIncrement = ((Integer)ftfAngularIncrement.getValue()).intValue();
    mechanism.setAnimationDelay(sAnimationDelay.getValue());
    mechanism.setIncrement(new Angle(Math.toRadians(angularIncrement)));
    if (oldAngularIncrement != angularIncrement)
      mechanism.initialize();
    oldAngularIncrement = angularIncrement;
    int zoom = ((Integer)ftfZoom.getValue()).intValue();
    mfInstance.setZoom((double)zoom/100);
    clearAndHide();
  }

  public void initDialog() {
    spAngularIncrement.setValue(new Integer(oldAngularIncrement));
    sAnimationDelay.setValue(mechanism.getAnimationDelay());
    setLocationRelativeTo(null);
    setVisible(true);
  }

}
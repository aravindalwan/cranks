/*
 * @(#)RedesignMechanism.java 1.0
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

package cranks.undo;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.util.Vector;
import cranks.ui.MainFrame;
import cranks.ui.DrawingPanel;
import cranks.ui.JDialogWindowAdapter;
import cranks.mech.FourBarMechanism;

/**
 * This class is used to redesign a mechanism that was created from a drawing.
 * It successively gives a textual description of the nature of each edit. The 
 * user has the freedom to proceed with the original construction or to modify 
 * the step, but not to alter the nature or sequence of construction.
 */

public class RedesignMechanism extends JDialog implements ActionListener, 
                                                                      Runnable { 
  
  JPanel pMainPanel = new JPanel();
  
  JPanel pTask = new JPanel();
  JLabel lTaskDescription = new JLabel("");
  JProgressBar pbTaskProgress = new JProgressBar();
  
  JPanel pButtons = new JPanel();
  JButton bBack = new JButton("< Back");
  JButton bNext = new JButton("Proceed >");
  JButton bModify = new JButton("Modify");
  JButton bCancel = new JButton("Cancel Redesign");

  private MainFrame mfInstance;
  private ConstructionProcedure constructProcedure;
  private FourBarMechanism mechanism;
  private Vector<ConstructionStep> constructSteps;
  private ConstructionStep currentStep;
  private Thread taskThread;
  private boolean started;

  public RedesignMechanism(JFrame frame, String title, ConstructionProcedure cp,
                                  FourBarMechanism Mechanism) {
    super(frame, title, false);
    jbInit();
    pack();
    mfInstance = (MainFrame)frame;
    constructProcedure = cp;
    mechanism = Mechanism;
    constructSteps = new Vector<ConstructionStep>();
  }

  private void jbInit() {
    
    pMainPanel.setLayout(new BorderLayout());
    
    pTask.setLayout(new BorderLayout());
    pTask.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
    pbTaskProgress.setStringPainted(true);
    pTask.add(lTaskDescription, BorderLayout.CENTER);
    lTaskDescription.setHorizontalAlignment(JLabel.CENTER);
    lTaskDescription.setPreferredSize(new Dimension(0, 50));
    pTask.add(pbTaskProgress, BorderLayout.SOUTH);

    pButtons.setLayout(new BorderLayout());
    pButtons.add(new JSeparator(), BorderLayout.NORTH);
    Box buttonBox = new Box(BoxLayout.LINE_AXIS);
    buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
    buttonBox.add(bBack);
    bBack.addActionListener(this);
    bBack.setActionCommand("Back");
    buttonBox.add(Box.createHorizontalStrut(10));
    buttonBox.add(bNext);
    bNext.addActionListener(this);
    bNext.setActionCommand("Next");
    buttonBox.add(Box.createHorizontalStrut(30));
    buttonBox.add(bModify);
    bModify.addActionListener(this);
    bModify.setActionCommand("Modify");
    buttonBox.add(Box.createHorizontalStrut(30));
    buttonBox.add(bCancel);
    bCancel.addActionListener(this);
    bCancel.setActionCommand("Cancel");
    pButtons.add(buttonBox, java.awt.BorderLayout.EAST);
    
    pMainPanel.add(pTask, BorderLayout.CENTER);
    pMainPanel.add(pButtons, BorderLayout.SOUTH);
    setContentPane(pMainPanel);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        cancelRedesign();
      }
    });
    setLocation(100, 50);
  }
  
  public void initDialog() {
    if (!mechanism.isRedesignable()) {
      JOptionPane.showMessageDialog(mfInstance, "No valid mechanism has been " +
         "constructed so far.\nPlease construct mechanism first", "Redesign",
         JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (!(constructProcedure.canUndo())) {
      JOptionPane.showMessageDialog(mfInstance, "No construction steps exist. " +
         "\nPlease perform some construction first", "Redesign",
         JOptionPane.ERROR_MESSAGE);
      return;
    }
    redesign();
  }

  public void clearAndHide() {
    dispose();
  }

  public void actionPerformed(ActionEvent ae) {
    if (isVisible()) {
      if (ae.getActionCommand() == "Back") {
        constructProcedure.undo();
        if (currentStep != null)
          setAndDisplayStep(currentStep.getNumber() - 1);
        else 
          setAndDisplayStep(constructSteps.size());
        mfInstance.repaint();
      }
      if (ae.getActionCommand() == "Next") {
        currentStep.setModify(false);
        constructProcedure.listenForEdit();
        currentStep.redesign();
        ConstructionStep edit = constructProcedure.mostRecentEdit();
        if (edit.getEditor() instanceof DrawingPanel)
          mfInstance.ActionEnableDrawingMode.invoke(); //switch to drawing mode
        setAndDisplayStep(currentStep.getNumber() + 1);
        mfInstance.repaint();
      }
      if (ae.getActionCommand() == "Modify") {
        currentStep.setModify(true);
        setButtonStatus(true, false);
        startThread();
      }
      if (ae.getActionCommand() == "Cancel") {
        cancelRedesign();
      }
      if (ae.getActionCommand() == "Finish") {
        mfInstance.enableMenus(true);
        clearAndHide();
        mfInstance.ActionCreateMechanism.invoke();
      }
    }
  }

  public void redesign() {
    constructProcedure.initializeRedesign(constructSteps);
    setAndDisplayStep(1);
    pbTaskProgress.setMinimum(0);
    pbTaskProgress.setMaximum(constructSteps.size());
    pbTaskProgress.setValue(0);
    mfInstance.enableMenus(false);
    setVisible(true);
  }
  
  private void setAndDisplayStep(int index) {
    if (index <= constructSteps.size()) {
      currentStep = constructSteps.elementAt(index - 1);
      lTaskDescription.setText("<HTML><CENTER>" + currentStep.toString() +
                                 "</CENTER></HTML>");
      pbTaskProgress.setValue(index - 1);
      setButtonStatus(false, false); //Buttons are always set when current step is changed
    }
    else {
      currentStep = null;
      lTaskDescription.setText("<HTML><CENTER>" + "Create Mechanism !" +
                                 "</CENTER></HTML>");
      pbTaskProgress.setValue(index);
      setButtonStatus(false, true); //Buttons are always set when current step is changed
    }
  }
  
  private void setButtonStatus(boolean modifyStep, boolean finish) {
    bBack.setEnabled(finish || !((currentStep.getNumber() == 1) || modifyStep));
    if (finish) {
      bNext.setText("Finish");
      bNext.setActionCommand("Finish");
    }
    else {
      bNext.setText("Proceed >");
      bNext.setActionCommand("Next");
    }
    bNext.setEnabled(!modifyStep);
    bModify.setEnabled(!modifyStep && !finish);
  }

  private void startThread() {
    if (taskThread == null) {
      taskThread = new Thread(this);
      started = true;
      taskThread.start();
    }
  }

  public void run() {
    if (started) {
        constructProcedure.listenForEdit();
        currentStep.redesign();
        ConstructionStep edit = constructProcedure.mostRecentEdit();
        if (edit == null) {
          stopThread(false);
          return;
        }
        while ((edit.getEditor() != currentStep.getEditor())
            || (edit.getConstructionType() != currentStep.getConstructionType())) {
          constructProcedure.undo();
          JOptionPane.showMessageDialog(mfInstance, "Please do not change the " +
            "type of construction", "Try Again", JOptionPane.ERROR_MESSAGE);
          constructProcedure.listenForEdit();
          currentStep.redesign();
          edit = constructProcedure.mostRecentEdit();
          if (edit == null) {
            stopThread(false);
            return;
          }
        }
        if (edit.getEditor() instanceof DrawingPanel)
          mfInstance.ActionEnableDrawingMode.invoke(); //switch back to drawing mode
        mfInstance.repaint();
      }
      stopThread(true);
    }

  private void stopThread(boolean editOccured) {
    taskThread = null;
    started = false;
    if (editOccured)
      setAndDisplayStep(currentStep.getNumber() + 1);
    else
      setAndDisplayStep(currentStep.getNumber());
  }
  
  private void cancelRedesign() {
    int n = JOptionPane.showConfirmDialog(mfInstance,
        "Are you sure you want to cancel the redesign procedure ?", 
        "Cancel Redesign", JOptionPane.YES_NO_OPTION);
    if (n == JOptionPane.YES_OPTION) {
      mfInstance.enableMenus(true);
      clearAndHide();
      constructProcedure.cancelRedesign(constructSteps);
      mfInstance.ActionEnableDrawingMode.invoke();
    }
  }

  /*public void propertyChange(PropertyChangeEvent e) {
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
      if (value.equals("Proceed")) {
        if (!buttonPressed) {
          buttonPressed = true;
          modifyStep = false;
        }
        else
          JOptionPane.showMessageDialog(mfInstance, "Please complete this step ",
                  "Complete step", JOptionPane.ERROR_MESSAGE);
      }
      else if (value.equals("Modify")) {
        if (!buttonPressed) {
          buttonPressed = true;
          modifyStep = true;
        }
        else
          JOptionPane.showMessageDialog(mfInstance, "Please complete this step ",
                  "Complete step", JOptionPane.ERROR_MESSAGE);
      }
      else { //user closed dialog or clicked cancel redesign
        cancelRedesign();
      }
    }
  }

*/
  
}
/*
 * @(#)ConstructionProcedure.java 1.0
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

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.*;
import java.util.Vector;
import java.io.*;
import cranks.ui.MainFrame;
import cranks.mech.FourBarMechanism;

/**
 * This class is used to record the individual steps involved in
 * designing a mechanism
 */

public class ConstructionProcedure extends UndoManager {

  private transient MainFrame mfInstance;
  private boolean undoableEditOccured;
  private boolean editStatusBeforeRedesign;
  private ConstructionStep lastEdit;

  public ConstructionProcedure(MainFrame mainFrame) {
    super();
    mfInstance = mainFrame;
    undoableEditOccured = false;
    editStatusBeforeRedesign = false;
  }

  public synchronized void undoableEditHappened(UndoableEditEvent e) {
    if (e.getEdit() != null) { //construction occured
      super.undoableEditHappened(e);
      ((ConstructionStep)e.getEdit()).setNumber(edits.size());
      lastEdit = (ConstructionStep)editToBeUndone();
    }
    undoableEditOccured = true;
    mfInstance.updateUndoRedoStatus();
    notifyAll();
  }

  public void listenForEdit() {
    undoableEditOccured = false;
    lastEdit = null;
  }

  public synchronized ConstructionStep mostRecentEdit() {
    while (!undoableEditOccured)
      try {
        wait();
      }
      catch (InterruptedException e) {
        System.err.println ("Waiting thread was interrupted");
        e.printStackTrace();
      }
    return lastEdit; //returns null if construction was cancelled
  }
  
  public void irreversibleEditHappened() {
    discardAllEdits();
    undoableEditOccured = false;
    mfInstance.updateUndoRedoStatus();
  }

  public boolean hasBeenEdited() {
    return (canUndo() && !((ConstructionStep)editToBeUndone()).hasBeenSaved());
  }
  
  public void undo() {
    super.undo();
    mfInstance.updateUndoRedoStatus();
  }

  public void redo() {
    super.redo();
    mfInstance.updateUndoRedoStatus();
  }

  public void initializeRedesign(Vector<ConstructionStep> constructSteps) {
    if (canRedo())
      trimEdits(((ConstructionStep)editToBeUndone()).getNumber(), edits.size() - 1);
    if (canUndo())
      undoTo(edits.elementAt(0));
    constructSteps.removeAllElements();
    for (int i = 0; i<edits.size(); i++)
      constructSteps.addElement((ConstructionStep)edits.elementAt(i));
    edits = new Vector<UndoableEdit>(getLimit());
    undoableEditOccured = false;
    mfInstance.updateUndoRedoStatus();
  }

  public void cancelRedesign(Vector<ConstructionStep> constructSteps) {
    if (canUndo())
      undoTo(edits.elementAt(0));
    edits = new Vector<UndoableEdit>(getLimit());
    for (int i = 0; i<constructSteps.size(); i++)
      edits.addElement(constructSteps.elementAt(i));
    if (canRedo())
      redoTo(edits.elementAt(edits.size() - 1));
    undoableEditOccured = true;
    mfInstance.updateUndoRedoStatus();
  }
  
  public void storeSteps(ObjectOutputStream out) throws IOException {
    if (canRedo())
      trimEdits(((ConstructionStep)editToBeUndone()).getNumber(), edits.size() - 1);
    if (canUndo()) {
      for(int i = 0; i < (edits.size() - 1); i++)
        ((ConstructionStep)edits.elementAt(i)).setSaved(false);
      ((ConstructionStep)edits.elementAt(edits.size() - 1)).setSaved(true);
      undoTo(edits.elementAt(0));
    }
    out.writeInt(edits.size());
    for(int i = 0; i < edits.size(); i++)
      out.writeObject((ConstructionStep)edits.elementAt(i));
    if (canRedo())
      redoTo(edits.elementAt(edits.size() - 1));
    undoableEditOccured = false;
    mfInstance.updateUndoRedoStatus();
  }
  
  public void restoreSteps(ObjectInputStream in) throws IOException {
    if (canUndo())
      undoTo(edits.elementAt(0));
    int numOfConstructionSteps = in.readInt();
    for(int i = 0; i < numOfConstructionSteps; i++) {
      try {
        ConstructionStep step = (ConstructionStep)in.readObject();
        step.associateEditor(mfInstance);
        step.redo();
        addEdit(step);
      } catch (ClassNotFoundException cnfe) {
        cnfe.printStackTrace();
      }
    }
    undoableEditOccured = false;
    mfInstance.updateUndoRedoStatus();
  }
  
  public Vector<ConstructionStep> getStorableFormat() {
    Vector<ConstructionStep> stepsToBeStored = new Vector<ConstructionStep>();
    if (canUndo()) {
      int numSteps = ((ConstructionStep)editToBeUndone()).getNumber();
      for(int i = 0; i < numSteps; i++)
        stepsToBeStored.addElement((ConstructionStep)edits.elementAt(i));
    }
    return stepsToBeStored;
  }

}
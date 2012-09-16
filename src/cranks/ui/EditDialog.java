/*
 * @(#)EditDialog.java 1.0
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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import java.util.Vector;
import cranks.geom.GeometricalObject;
import cranks.undo.ConstructionProcedure;
import cranks.undo.ConstructionStep;

/**
 * This is an abstract class for dialogs that produce edits i.e those that
 * change the drawing in any way. All such dialogs must extend this class
 */

public abstract class EditDialog extends JDialog implements Editor {

  protected int id;
  protected MainFrame mfInstance;
  protected Vector<GeometricalObject> objects;
  EventListenerList listenerList = new EventListenerList();

  public EditDialog(JFrame frame, String title, boolean modal,
                    Vector<GeometricalObject> Objects) {
    super(frame, title, modal);
    id = 0;
    mfInstance = (MainFrame)frame;
    objects = Objects;
    addUndoableEditListener(mfInstance.constructProcedure);
    setLocationRelativeTo(null);
  }
  
  public void setID(int number) {
    id = number;
  }
  
  public int getID() {
    return id;
  }

  public void addUndoableEditListener(UndoableEditListener listener) {
    listenerList.add(UndoableEditListener.class, listener);
  }

  public void fireUndoableEditUpdate(UndoableEditEvent e) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == UndoableEditListener.class) {
        ((UndoableEditListener)listeners[i+1]).undoableEditHappened(e);
      }
    }
  }

  public void fireIrreversibleEditUpdate() {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == UndoableEditListener.class) {
        ((ConstructionProcedure)listeners[i+1]).irreversibleEditHappened();
      }
    }
  }

  public void redesign(ConstructionStep step) {
    if (step.needsModification())
      initDialog();
    else {
      Object[] inputs = step.getInputs();
      for (int i = 0; i<inputs.length; i++) {
        if (inputs[i] instanceof GeometricalObject)
          inputs[i] = objects.elementAt(
                      ((GeometricalObject)inputs[i]).getNumber() - 1);
      }
      step = new ConstructionStep(step.getEditor(), inputs,
                                  step.getConstructionType());
      doConstruction(step);
    }
  }

  public abstract void initDialog();
  
  protected void constructionCancelled() {
    fireUndoableEditUpdate(new UndoableEditEvent(this, null));
  }
  
}
/*
 * @(#)ConstructionStep.java 1.0
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

import javax.swing.undo.AbstractUndoableEdit;
import cranks.ui.Editor;
import cranks.ui.MainFrame;

public class ConstructionStep extends AbstractUndoableEdit implements 
                                                          java.io.Serializable {

  private static final long serialVersionUID = -6783330993546307707L;
  
  private int number;
  private transient Editor editGenerator;
  private int editorID;
  private Object[] inputParams;
  private Object[] outputParams;
  private int constructionType;
  private boolean modify;
  private boolean saved;

  public ConstructionStep(Editor EditGenerator, Object[] InputParams,
                          int ConstructionType) {
    super();
    number = -1;
    editGenerator = EditGenerator;
    editorID = editGenerator.getID();
    inputParams = InputParams;
    constructionType = ConstructionType;
    modify  = false;
    saved = false;
  }

  public boolean isComplete() {
    return (outputParams != null);
  }

  public int getNumber() {
    return number;
  }

  public int getConstructionType() {
    return constructionType;
  }

  public Object[] getInputs() {
    return inputParams;
  }

  public Object[] getOutputs() {
    return outputParams;
  }

  public boolean needsModification() {
    return modify;
  }

  public Editor getEditor() {
    return editGenerator;
  }

  public void setNumber(int value) {
    number = value;
  }

  public void setInputs(Object[] inParams) {
    inputParams = inParams;
  }

  public void setOutputs(Object[] outParams) {
    outputParams = outParams;
  }

  public void setModify(boolean value) {
    modify = value;
  }
  
  public boolean hasBeenSaved() {
    return saved;
  }
  
  public void setSaved(boolean Saved) {
    saved = Saved;
  }

  public void undo() {
    super.undo();
    editGenerator.undoConstruction(this);
  }

  public void redo() {
    super.redo();
    editGenerator.redoConstruction(this);
  }

  public void redesign() {
    editGenerator.redesign(this);
  }
  
  public void associateEditor(MainFrame mf) {
    editGenerator = mf.getEditor(editorID);
  }

  public String getPresentationName() {
    return editGenerator.getPresentationName(this);
  }

  public String toString() {
    String output = "";
    if (isComplete())
      output = "Step " + getNumber() + ": "+editGenerator.getDisplayName(this); 
    return output;
  }

}
/*
 * @(#)FileChoose.java 1.0
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

import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.*;
import java.util.Vector;
import cranks.geom.GeometricalObject;
import cranks.mech.FourBarMechanism;
import cranks.undo.ConstructionProcedure;
import cranks.undo.ConstructionStep;

//this class is used for the file chooser dialog

public class FileChoose extends JFileChooser {
  SERFilter serFilter;
  TXTFilter textFilter;
  MainFrame mfInstance;
  Vector<GeometricalObject> objects;
  FourBarMechanism mechanism;
  ConstructionProcedure constructProcedure;
  File currentFile;

  public static final int OPEN_WORKSPACE = 1;
  public static final int SAVE_WORKSPACE = 2;
  public static final int SAVE_WORKSPACE_AS = 3;

  public FileChoose(JFrame frame, Vector<GeometricalObject> Objects, 
                    FourBarMechanism mech, ConstructionProcedure proc) {
    super();
    serFilter = new SERFilter();
    textFilter = new TXTFilter();
    mfInstance = (MainFrame)frame;
    objects = Objects;
    mechanism = mech;
    constructProcedure = proc;
    currentFile = null;
  }

  public boolean initDialog(int mode) {
    int returnval;
    addFileFilters(mode);
    boolean success = false;
    switch(mode) {
      case OPEN_WORKSPACE:
        success = openFile();      
      break;
      
      case SAVE_WORKSPACE:
        if ((currentFile != null) && (currentFile.exists()))
          success = saveFile();
        else
          success = saveFileAs();
      break;
      
      case SAVE_WORKSPACE_AS:
        success = saveFileAs();
      break;
      
      default:
    }
    return success;
  }
  
  private boolean openFile() {
    int returnval = showOpenDialog(mfInstance);
    if (returnval == JFileChooser.APPROVE_OPTION) {
      File newFile = getSelectedFile();
      if (!serFilter.accept(newFile)) {
        showErrorDialog("Unknown file type");
        return false;
      }
      try {
        ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(newFile.getPath()));
        mechanism.copyMechanism((FourBarMechanism)in.readObject());
        constructProcedure.restoreSteps(in);
        in.close();
        currentFile = newFile;
        mfInstance.updateUndoRedoStatus();
        return true;
      } catch (IOException ioe) {
        ioe.printStackTrace();
      } catch (ClassNotFoundException cnfe) {
        System.err.println("Bad file format");
        cnfe.printStackTrace();
      }
    }
    return false;
  }
  
  private boolean saveFile() {
    try {
      ObjectOutputStream out = new ObjectOutputStream(
                  new FileOutputStream(currentFile.getPath()));
      out.writeObject(mechanism);
      constructProcedure.storeSteps(out);
      out.close();
      mfInstance.setTitleString();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  } 
  
  private boolean saveFileAs() {
    int returnval = showSaveDialog(mfInstance);
    if (returnval == JFileChooser.APPROVE_OPTION) {
      if (getFileFilter() == textFilter)
        return saveTextFile();
      currentFile = getSelectedFile();
      try {
        if (!currentFile.exists()) {
          if (!serFilter.accept(currentFile))
            currentFile = new File(currentFile.getAbsolutePath()+"."+SERFilter.SER);
          currentFile.createNewFile();
        }
        else 
          if (!serFilter.accept(currentFile)) {
            showErrorDialog("Unknown file type");
            return false;
          }
        return saveFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }
  
  private boolean saveTextFile() {
    File textFile = getSelectedFile();
    try {
      if (!textFile.exists()) {
        if (!textFilter.accept(textFile))
          textFile = new File(textFile.getAbsolutePath()+"."+TXTFilter.TXT);
        textFile.createNewFile();
      }
      else 
        if (!textFilter.accept(textFile)) {
          showErrorDialog("Unknown file type");
          return false;
        }
      Vector<ConstructionStep> steps = constructProcedure.getStorableFormat();
      FileWriter f = new FileWriter(textFile);
      f.write(getOutputString(steps));
      f.close();  
      return true;
    } catch (IOException e) {
      showErrorDialog("Please ensure file is not being used by any other process");
    }
    return false;
  }
  
  private String getOutputString(Vector<ConstructionStep> steps) {
    String output = mfInstance.getTitle();
    if (output.endsWith("*"))
      output = output.substring(0, output.length() - 1);
    output  += ("\n\nConstruction Procedure\n");
    for(int i = 0; i < steps.size(); i++)
      output += (steps.elementAt(i).toString() + "\n");
    return output;
  }
  
  private void addFileFilters(int mode) {
    resetChoosableFileFilters();
    if (mode == OPEN_WORKSPACE)
      addChoosableFileFilter(serFilter);
    else if ((mode == SAVE_WORKSPACE) || (mode == SAVE_WORKSPACE_AS)) {
      addChoosableFileFilter(serFilter);
      addChoosableFileFilter(textFilter);
      setFileFilter(serFilter);
    }
  }
  
  public boolean askForSavingWorkspace() {
    int n = JOptionPane.showConfirmDialog(mfInstance,
        "Do you want to save the changes you made to the workspace ?", 
        "Save Workspace", JOptionPane.YES_NO_CANCEL_OPTION);
    if (n == JOptionPane.YES_OPTION)
      return initDialog(FileChoose.SAVE_WORKSPACE);
    else
      return ((n != JOptionPane.CANCEL_OPTION) && (n != JOptionPane.CLOSED_OPTION));
  }
  
  public void newWorkspace() {
    currentFile = null;
  }
  
  private void showErrorDialog(String error) {
    JOptionPane.showMessageDialog(this, error,"Error", JOptionPane.ERROR_MESSAGE);
    return;
  }

  //Used to obtain the extension of a file
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');
    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }
    return ext;
  }
  
  public String getCurrentFileName() {
    if ((currentFile != null) && currentFile.exists())
      return currentFile.getName();
    else 
      return "";
  }

}

class SERFilter extends javax.swing.filechooser.FileFilter {
  
  public static final String SER = "ser";
  
  public boolean accept(File f) {
    if (f.isDirectory())
      return true;
    String ext = FileChoose.getExtension(f);
    return ((ext != null) && (ext.equalsIgnoreCase(SER)));
  }
  
  public String getDescription() {
    return "Serialized Workspace File (*.ser)";
  }
  
}

class TXTFilter extends javax.swing.filechooser.FileFilter {
  
  public static final String TXT = "txt";
  
  public boolean accept(File f) {
    if (f.isDirectory())
      return true;
    String ext = FileChoose.getExtension(f);
    return ((ext != null) && (ext.equalsIgnoreCase(TXT)));
  }
  
  public String getDescription() {
    return "Text File (*.txt)";
  }
  
}

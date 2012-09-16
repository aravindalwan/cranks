/*
 * @(#)MainFrame.java 1.0
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
import java.awt.event.*;
import java.awt.print.*;
import java.util.Vector;
import java.lang.reflect.*;
import cranks.geom.GeometricalObject;
import cranks.mech.FourBarMechanism;
import cranks.undo.RedesignMechanism;
import cranks.undo.ConstructionProcedure;

public class MainFrame extends JFrame {

  //Title string
  public static final String TITLE = "CRANKS 1.0";
  
  //MainFrame instance that is shared by all other classes
  private static MainFrame mfInstance;
  
  //Splash screen
  private static SplashScreen splash;
  
  //Panel to perform the rendering
  private static DrawingPanel drawPanel; 
  
  //Reference to applet which invokes the program, when run as applet
  private static BaseApplet baseApp; 
  
  //Printer job object for scheduling printing jobs
  private static PrinterJob printJob;

  //Used to animate the mechanism
  private static FourBarMechanism mechanism;

  //List of objects drawn
  private static Vector<GeometricalObject> objects;

  //Undoable edit listener that stores construction steps and allows redesign
  public static ConstructionProcedure constructProcedure;

  //List of all registered edit-producing components
  private static Vector<Editor> editors;
  
  //Used to draw circles
  public static CoordinateCircle coordCircle;

  //Used to draw lines using equations
  public static CoordinateLine coordLine;

  //Used to add points
  public static CoordinatePoint coordPoint;

  //Used to modify points
  public static ModifyPoint changePoint;

  //Used to draw triangles
  public static CoordinateTriangle coordTriangle;

  //Used to create a copy of a triangle aligned to line of the same length
  //as one of the sides of the truangle
  public static CopyMoveTriangle copyMoveTri;

  //Used to get values of sub fields of objects. It also allows these values
  //to be dragged and dropped onto text fields in other dialogs.
  public static FieldValue objectFieldValue;

  //Used to perform file operations 
  public static FileChoose fChoose;

  //Used to (un)hide and remove objects
  public static HideRemoveObject hideRemObject;

  //Used to obtain the intersection between two objects
  public static Intersect intersectObjects;

  //Used to translate and rotate objects
  public static Move moveObject;

  //Used to change program settings
  public static Settings setting;

  //Used to update link lengths of a mechanism
  public static UpdateMechanism updateMech;

  //Used to create a mechanism from an existing drawing
  public static CreateMechanism createMech;

  //Used to redesign a mechanism that has been created from a drawing
  public static RedesignMechanism redesignMech;
  
  //Used to provide help
  public static Help userHelp;

  //Used to enable/disable menu actions
  private static boolean menubarEnabled = true;

  //Menu Actions used
  public Vector<MenuAction> actions;
  public MenuAction ActionNewWorkspace;
  public MenuAction ActionOpenWorkspace;
  public MenuAction ActionSaveWorkspace;
  public MenuAction ActionSaveWorkspaceAs;
  public MenuAction ActionPageSetup;
  public MenuAction ActionPrint;
  public MenuAction ActionExit;
  public MenuAction ActionPanView;
  public MenuAction ActionSettings;
  public MenuAction ActionEnableAnimationMode;
  public MenuAction ActionSetLinkLengths;
  public MenuAction ActionStartAnimation;
  public MenuAction ActionPauseAnimation;
  public MenuAction ActionToggleTrace;
  public MenuAction ActionEnableDrawingMode;
  public MenuAction ActionPointAddMouse;
  public MenuAction ActionPointAddCoordinates;
  public MenuAction ActionPointModify;
  public MenuAction ActionLineAddMouse;
  public MenuAction ActionLineAddCoordinates;
  public MenuAction ActionCircleAddCoordinates;
  public MenuAction ActionTriangleAddMouse;
  public MenuAction ActionTriangleAddCoordinates;
  public MenuAction ActionTriangleCopyMove;
  public MenuAction ActionIntersect;
  public MenuAction ActionTranslateRotate;
  public MenuAction ActionHide;
  public MenuAction ActionClearAll;
  public MenuAction ActionShowFieldValue;
  public MenuAction ActionUndo;
  public MenuAction ActionRedo;
  public MenuAction ActionCreateMechanism;
  public MenuAction ActionRedesign;
  public MenuAction ActionHelp;
  public MenuAction ActionAbout;
  
  //Default constructor used to initialize mfInstance
  public MainFrame() {
    super(TITLE);
    objects = new Vector<GeometricalObject>();
    mechanism = new FourBarMechanism();
  }

  //Dummy constructor used to construct an instance of MainFrame only for 
  //displaying the frame
  public MainFrame(BaseApplet applet) {
    baseApp = applet;
  }

  public static void main(String args[]) {
    //null signifies that program will be run as an Application, not Applet
    MainFrame.start(null);
  }
  
  //Creates a dummy instance in order to create and display frame (mfInstance)
  public static void start(BaseApplet applet) {
    (new MainFrame(applet)).createAndShowFrame();
  }
  
  //Creates and displays the program frame
  private void createAndShowFrame() {
    //set nice Decorations and Rendering defaults
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
    
    //Create and set up the window
    mfInstance = new MainFrame();
    splash = new SplashScreen(mfInstance);
    
    if (isApplet()) {
      mfInstance.baseApp = baseApp;
      baseApp.add(splash.getSplash());
    }
    else {
      //Schedule a job for the event-dispatching thread:
      //displaying the splash screen.
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          splash.setVisible(true);
        }
      });
      mfInstance.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      mfInstance.addWindowListener(new MainFrameWindowAdapter(mfInstance));
    }
    
    //Instantiate ConstructionProcedure - used as an edit listener
    constructProcedure = new ConstructionProcedure(mfInstance);
    
    //Create and set up the content pane
    mfInstance.createMenuBar(); //also initializes & registers the MenuActions
    drawPanel = new DrawingPanel(mfInstance, objects, mechanism);
    mfInstance.setContentPane(drawPanel);

    //Create dialogs used by UI
    coordCircle = new CoordinateCircle(mfInstance,"Draw Circles", objects);
    coordLine = new CoordinateLine(mfInstance, "Draw Lines", objects);
    coordPoint = new CoordinatePoint(mfInstance,"Add Points", objects);
    changePoint = new ModifyPoint(mfInstance,"Modify Points", objects);
    coordTriangle = new CoordinateTriangle(mfInstance, "Draw Triangles", objects);
    copyMoveTri = new CopyMoveTriangle(mfInstance,"Copy and Move Triangle", objects);
    objectFieldValue = new FieldValue(mfInstance, "Get Field Value", objects);
    if (!isApplet())
      fChoose = new FileChoose(mfInstance, objects, mechanism, constructProcedure); 
    hideRemObject = new HideRemoveObject(mfInstance,"Hide Objects",objects);
    intersectObjects = new Intersect(mfInstance, "Intersect Objects", objects);
    updateMech = new UpdateMechanism(mfInstance, "Set Link Lengths", mechanism);
    moveObject = new Move(mfInstance, "Translate and Rotate Objects", objects);
    setting = new Settings(mfInstance, "Settings", mechanism);
    createMech = new CreateMechanism(mfInstance,"Create Mechanism",objects,mechanism);
    userHelp = new Help(mfInstance, "Help");
    redesignMech = new RedesignMechanism(mfInstance, "Redesign Mechanism",
                                         constructProcedure, mechanism);
    mfInstance.registerEditors();
    
    //Setup printing capabilities if in application mode
    if (!isApplet()) {
      printJob = PrinterJob.getPrinterJob();
      Book bk = new Book();
      bk.append(drawPanel, printJob.defaultPage());
      printJob.setPageable(bk);
    }

    mfInstance.updateUndoRedoStatus();
    mfInstance.addComponentListener(new MainframeComponentAdapter(mfInstance));
    mfInstance.setTitleString();
    mfInstance.pack();

    //Center the window
    mfInstance.setLocationRelativeTo(null);
    
    try {
      Thread.currentThread().sleep(2000); //Pause to allow user to see splash screen
    } catch (Exception e) {
    }
    
    //Schedule a job for the event-dispatching thread:
    //displaying the application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mfInstance.setVisible(true);
      }
    });
    
    if (!isApplet()) {
      //Schedule a job for the event-dispatching thread:
      //removing the splash screen.
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          splash.setVisible(false);
        }
      });
    }
  }

  //handle event for resizing the window
  public void componentResized(ComponentEvent ce) {
    if (ce.getComponent() == mfInstance) {
      drawPanel.resizeComponents();
    }
  }
  
  public boolean isApplet() {
    return (baseApp != null);
  }
  
  //Register all edit-producing components 
  private void registerEditors() {
    //Initialize the editors vector
    editors = new Vector<Editor>();
    
    //Add all editors to the editors vector
    Field[] fields = mfInstance.getClass().getDeclaredFields();
    for(int i = 0; i < fields.length; i++) {
      try {
        Object obj = fields[i].get(mfInstance);
        if (obj instanceof Editor)
          editors.addElement((Editor)obj);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    //Sort the vector in increasing order of editor IDs
    Editor[] editorArray = new Editor[1];
    editorArray = editors.toArray(editorArray);
    for(int i = 0; i < editorArray.length; i++)
      editors.setElementAt(editorArray[i], editorArray[i].getID() - 1);
  }
  
  public Editor getEditor(int number) {
    return editors.elementAt(number - 1);
  }
  
  public double getZoom() {
    return drawPanel.getZoom();
  }
  
  public void setZoom(double value) {
    drawPanel.setZoom(value);
  }
  
  public void setTitleString() {
    String fileName = (fChoose != null)?(fChoose.getCurrentFileName()):("");
    String title = TITLE + " - ";
    if (fileName.length() > 0)
      title += fileName;
    else
      title += "Untitled";
    if (constructProcedure.hasBeenEdited())
      title += "*";
    setTitle(title);
  }

  public void updateUndoRedoStatus() {
    JMenuBar mb = mfInstance.getJMenuBar();
    ActionUndo.setEnabled(constructProcedure.canUndo());
    ActionRedo.setEnabled(constructProcedure.canRedo());
    ActionUndo.putValue(Action.NAME, constructProcedure.getUndoPresentationName());
    ActionRedo.putValue(Action.NAME, constructProcedure.getRedoPresentationName());
    setTitleString();
  }

  public void enableMenus(boolean value) {
    menubarEnabled = value;
    for(int i = 0; i < actions.size(); i++)
      actions.elementAt(i).setEnabled(value);
  }

  //to create the menu bar
  private void createMenuBar() {
    createActions();
    JMenuBar menuBar = new JMenuBar();

    //Menu 1 - File menu
    JMenu menu1 = new JMenu("File");
    menu1.setMnemonic(KeyEvent.VK_F);
    menu1.add(ActionNewWorkspace);
    menu1.add(ActionOpenWorkspace);
    menu1.addSeparator();
    menu1.add(ActionSaveWorkspace);
    menu1.add(ActionSaveWorkspaceAs);
    menu1.addSeparator();
    menu1.add(ActionPageSetup);
    menu1.add(ActionPrint);
    menu1.addSeparator();
    menu1.add(ActionExit);
    //End of Menu 1

    //Menu2 - View
    JMenu menu2 = new JMenu("View");
    menu2.setMnemonic(KeyEvent.VK_V);
    menu2.add(ActionPanView);
    menu2.add(ActionSettings);
    //End of Menu 2
    
    //Menu 3 - Animation
    JMenu menu3 = new JMenu("Animation");
    menu3.setMnemonic(KeyEvent.VK_A);
    menu3.add(ActionEnableAnimationMode);
    menu3.add(ActionSetLinkLengths);
    menu3.add(ActionStartAnimation);
    menu3.add(ActionPauseAnimation);
    menu3.add(ActionToggleTrace);
    //End of Menu 3

    //Menu 4 - Drawing
    JMenu menu4 = new JMenu("Drawing");
    menu4.setMnemonic(KeyEvent.VK_D);
    menu4.add(ActionEnableDrawingMode);
    
    JMenu menu4_1 = new JMenu("Point");
    menu4_1.setMnemonic(KeyEvent.VK_P);
    menu4_1.add(ActionPointAddMouse);
    menu4_1.add(ActionPointAddCoordinates);
    menu4_1.add(ActionPointModify);
    menu4.add(menu4_1);
    
    JMenu menu4_2 = new JMenu("Line");
    menu4_2.setMnemonic(KeyEvent.VK_L);
    menu4_2.add(ActionLineAddMouse);
    menu4_2.add(ActionLineAddCoordinates);
    menu4.add(menu4_2);
    
    JMenu menu4_3 = new JMenu("Circle");
    menu4_3.setMnemonic(KeyEvent.VK_C);
    menu4_3.add(ActionCircleAddCoordinates);
    menu4.add(menu4_3);
    
    JMenu menu4_4 = new JMenu("Triangle");
    menu4_4.setMnemonic(KeyEvent.VK_T);
    menu4_4.add(ActionTriangleAddMouse);
    menu4_4.add(ActionTriangleAddCoordinates);
    menu4_4.add(ActionTriangleCopyMove);
    menu4.add(menu4_4);
    
    menu4.add(ActionIntersect);
    menu4.add(ActionTranslateRotate);
    menu4.add(ActionHide);
    menu4.add(ActionClearAll);
    menu4.add(ActionShowFieldValue);
    //End of Menu 4

    //Menu 5 - Construction
    JMenu menu5 = new JMenu("Construction");
    menu5.setMnemonic(KeyEvent.VK_C);
    menu5.add(ActionUndo);
    menu5.add(ActionRedo);
    menu5.add(ActionCreateMechanism);
    menu5.add(ActionRedesign);
    //End of Menu 5

    //Menu6 - Help
    JMenu menu6 = new JMenu("Help");
    menu6.setMnemonic(KeyEvent.VK_H);
    menu6.add(ActionHelp);
    menu6.add(ActionAbout);
    //End of Menu 6
    
    //Add the menus to the menubar

    if (!isApplet())
      menuBar.add(menu1); //File 
    menuBar.add(menu2); //View
    menuBar.add(menu3); //Animation
    menuBar.add(menu4); //Drawing
    menuBar.add(menu5); //Construction
    menuBar.add(menu6); //Help
    
    setJMenuBar(menuBar);
  }

//########################### Menu Item Actions ################################
  
  private void createActions() {
    //Opening a new workspace
    ActionNewWorkspace = new MenuAction("New WorkSpace", KeyEvent.VK_N, "1_1", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        boolean proceed = true;
        if (constructProcedure.hasBeenEdited())
          proceed = fChoose.askForSavingWorkspace();
        if (proceed) {
          fChoose.newWorkspace();
          drawPanel.changeMode(DrawingPanel.OPERATING_MODE_CLEAR,
                                   DrawingPanel.MOUSE_MODE_NONE);
          constructProcedure.irreversibleEditHappened();
        }
      }
    };
    
    //Opening an existing workspace
    ActionOpenWorkspace = new MenuAction("Open WorkSpace", KeyEvent.VK_O, "1_2", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        boolean proceed = true;
        if (constructProcedure.hasBeenEdited())
          proceed = fChoose.askForSavingWorkspace();
        if (proceed) {
          if (fChoose.initDialog(FileChoose.OPEN_WORKSPACE))
            drawPanel.changeMode(DrawingPanel.OPERATING_MODE_DRAWING,
                                 DrawingPanel.MOUSE_MODE_NONE);
        }
      }
    };
    
    //Saving the current workspace
    ActionSaveWorkspace = new MenuAction("Save WorkSpace", KeyEvent.VK_S, "1_3", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        fChoose.initDialog(FileChoose.SAVE_WORKSPACE);
      }
    };

    //Saving into a workspace under a different name
    ActionSaveWorkspaceAs = new MenuAction("Save WorkSpace As", KeyEvent.VK_A, 
          "1_4", null) {
      public void actionPerformed(ActionEvent ae) {
        fChoose.initDialog(FileChoose.SAVE_WORKSPACE_AS);
      }
    };

    //Showing the Page Setup dialog
    ActionPageSetup = new MenuAction("Page Setup", KeyEvent.VK_G, "1_5", null) {
      public void actionPerformed(ActionEvent ae) {
        printJob.pageDialog(printJob.defaultPage());
      }
    };

    //Printing
    ActionPrint = new MenuAction("Print", KeyEvent.VK_P, "1_6", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        if (printJob.printDialog())
        try {
          printJob.print();
        }
        catch (PrinterException pe) {
          pe.printStackTrace();
        }
      }
    };

    //Exiting the programme
    ActionExit = new MenuAction("Exit", KeyEvent.VK_X, "1_7", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        drawPanel.pauseAnimation();
        if (!isApplet()) {
          if (constructProcedure.hasBeenEdited()) {
            if (!fChoose.askForSavingWorkspace())
              return;
          }
          System.exit(0);
        }
      }
    };

    //Panning the view
    ActionPanView = new MenuAction("Pan View", KeyEvent.VK_P, "2_2", null) {
      public void actionPerformed(ActionEvent ae) {
        drawPanel.changeMode(DrawingPanel.OPERATING_MODE_PANNING,
                            DrawingPanel.MOUSE_MODE_NONE);
      }
    };
    
    //Changing the program settings
    ActionSettings = new MenuAction("Settings", KeyEvent.VK_T, "2_2", null) {
      public void actionPerformed(ActionEvent ae) {
        drawPanel.pauseAnimation();
        setting.initDialog();
      }
    };
    
    //Switching to animation mode
    ActionEnableAnimationMode = new MenuAction("Enable Animation Mode",
                 KeyEvent.VK_A, "3_1", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        drawPanel.changeMode(DrawingPanel.OPERATING_MODE_ANIMATION,
                      DrawingPanel.MOUSE_MODE_NONE);
      }
    };
    
    //Displaying the link properties dialog
    ActionSetLinkLengths = new MenuAction("Set Link Lengths", KeyEvent.VK_L, "3_2", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableAnimationMode.invoke();
        drawPanel.pauseAnimation();
        updateMech.initDialog();
      }
    };
    
    //Starting the animation
    ActionStartAnimation = new MenuAction("Start Animation", KeyEvent.VK_T, "3_3", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableAnimationMode.invoke();
        drawPanel.startAnimation();
      }
    };
    
    //Pausing the animation
    ActionPauseAnimation = new MenuAction("Pause Animation", KeyEvent.VK_U, "3_4", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableAnimationMode.invoke();
        drawPanel.pauseAnimation();
      }
    };
    
    //Toggling the display of trace of ternary link
    ActionToggleTrace = new MenuAction("Toggle Trace", KeyEvent.VK_R, "3_5", null) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableAnimationMode.invoke();
        mechanism.toggleTrace();
      }
    };
    
    //Switching to drawing mode
    ActionEnableDrawingMode = new MenuAction("Enable Drawing Mode", KeyEvent.VK_D, 
          "4_1", KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        drawPanel.pauseAnimation();
        drawPanel.changeMode(DrawingPanel.OPERATING_MODE_DRAWING,
                             DrawingPanel.MOUSE_MODE_NONE);
      }
    };
    
    //Drawing points using the mouse
    ActionPointAddMouse = new MenuAction("Add using Mouse", KeyEvent.VK_M, "4_1_1", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_P, 
                              (ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK))) {
      public void actionPerformed(ActionEvent ae) {
        drawPanel.changeMode(DrawingPanel.OPERATING_MODE_DRAWING,
                             DrawingPanel.MOUSE_MODE_POINT);
      }
    };
    
    //Drawing points using co-ordinates
    ActionPointAddCoordinates = new MenuAction("Add using Co-ordinates", 
                KeyEvent.VK_C, "4_1_2", 
                KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.SHIFT_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        coordPoint.initDialog();
      }
    };
    
    //Modifying co-ordinates of a point
    ActionPointModify = new MenuAction("Modify Co-ordinates", KeyEvent.VK_D, "4_1_3", 
                 null) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        changePoint.initDialog();
      }
    };
    
    //Drawing lines using the mouse
    ActionLineAddMouse = new MenuAction("Add using Mouse", KeyEvent.VK_M, "4_2_1", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_L, 
                              (ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK))) {
      public void actionPerformed(ActionEvent ae) {
        drawPanel.changeMode(DrawingPanel.OPERATING_MODE_DRAWING,
                             DrawingPanel.MOUSE_MODE_LINE);
      }
    };
    
    //Drawing lines using co-ordinates
    ActionLineAddCoordinates = new MenuAction("Add using Co-ordinates", 
                KeyEvent.VK_C, "4_2_2", 
                KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.SHIFT_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        coordLine.initDialog();
      }
    };
    
    //Drawing circles using co-ordinates
    ActionCircleAddCoordinates = new MenuAction("Add using Coordinates", 
                KeyEvent.VK_C, "4_3_1", 
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.SHIFT_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        coordCircle.initDialog();
      }
    };
    
    //Drawing triangles using the mouse
    ActionTriangleAddMouse=new MenuAction("Add using Mouse",KeyEvent.VK_M,"4_4_1", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_T, 
                              (ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK))) {
      public void actionPerformed(ActionEvent ae) {
        drawPanel.changeMode(DrawingPanel.OPERATING_MODE_DRAWING,
                             DrawingPanel.MOUSE_MODE_TRIANGLE);
      }
    };
    
    //Drawing triangles using coordinates
    ActionTriangleAddCoordinates = new MenuAction("Add using Co-ordinates", 
                KeyEvent.VK_C, "4_4_2", 
                KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.SHIFT_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        coordTriangle.initDialog();
      }
    };
    
    //Copying and moving triangles
    ActionTriangleCopyMove = new MenuAction("Copy and Move", KeyEvent.VK_P, 
                                                                "4_4_3", null) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        copyMoveTri.initDialog();
      }
    };
    
    //Displaying the dialog to intersect two objects
    ActionIntersect = new MenuAction("Intersect Objects", KeyEvent.VK_I, "4_2", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        intersectObjects.initDialog();
      }
    };
    
    //Displaying the dialog to translate and rotate objects
    ActionTranslateRotate = new MenuAction("Translate/Rotate Object", 
                 KeyEvent.VK_R, "4_3", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        moveObject.initDialog();
      }
    };
    
    //Hiding & removing objects - Remove facility currently disabled
    ActionHide = new MenuAction("Hide Object", KeyEvent.VK_H, "4_4", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        hideRemObject.initDialog();
      }
    };
    
    //Clearing the  work area
    ActionClearAll = new MenuAction("Clear All", KeyEvent.VK_A, "4_5", null) {
      public void actionPerformed(ActionEvent ae) {
        int n = JOptionPane.showConfirmDialog(mfInstance,
            "Are you sure you want to clear ?", "Clear Drawing",
            JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
          drawPanel.changeMode(DrawingPanel.OPERATING_MODE_CLEAR,
                                   DrawingPanel.MOUSE_MODE_NONE);
          drawPanel.repaint();
          constructProcedure.irreversibleEditHappened();
        }
      }
    };
    
    //To get values of sub-fields of an object
    ActionShowFieldValue = new MenuAction("Properties", KeyEvent.VK_S, "4_6", 
              KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.ALT_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        objectFieldValue.initDialog();
      }
    };
    
    //Undo
    ActionUndo = new MenuAction("Undo", KeyEvent.VK_U, "5_1", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        constructProcedure.undo();
      }
    };
    
    //Redo
    ActionRedo = new MenuAction("Redo", KeyEvent.VK_R, "5_2", 
                 KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        constructProcedure.redo();
      }
    };
    
    //Create mechanism from drawing
    ActionCreateMechanism = new MenuAction("Create Mechanism", KeyEvent.VK_C, "5_3", 
                                                                         null) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        createMech.initDialog();
      }
    };
    
    //Redesign mechanism
    ActionRedesign = new MenuAction("Redesign Mechanism", KeyEvent.VK_D, "5_4", 
                                                                         null) {
      public void actionPerformed(ActionEvent ae) {
        ActionEnableDrawingMode.invoke();
        redesignMech.initDialog();
      }
    };
    
    //Show help contents
    ActionHelp = new MenuAction("Help Contents", KeyEvent.VK_H, "6_1", 
               KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.SHIFT_MASK)) {
      public void actionPerformed(ActionEvent ae) {
        userHelp.initDialog();
      }
    };
    
    //Show About dialog
    ActionAbout = new MenuAction("About", KeyEvent.VK_A, "6_2", null) {
      public void actionPerformed(ActionEvent ae) {
        JOptionPane.showMessageDialog(mfInstance, splash.getSplash(), "About", 
                                      JOptionPane.PLAIN_MESSAGE);
      }
    };
    
    actions = new Vector<MenuAction>();
    int counter = 0;
    Field[] fields = mfInstance.getClass().getDeclaredFields();
    for(int i = 0; i < fields.length; i++) {
      try {
        Object obj = fields[i].get(mfInstance);
        if (obj instanceof MenuAction)
          actions.addElement((MenuAction)obj);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    
  }
  
}

//############################# Adapter Classes ################################

class MainframeComponentAdapter extends ComponentAdapter{
  MainFrame mfInstance;

  public MainframeComponentAdapter(MainFrame Adaptee) {
    mfInstance = Adaptee;
  }

  public void componentResized(ComponentEvent ce) {
    mfInstance.componentResized(ce);
  }
}

class MainFrameWindowAdapter extends WindowAdapter {
  MainFrame mfInstance;

  public MainFrameWindowAdapter(MainFrame mf) {
    mfInstance = mf;
  }

  public void windowClosing(WindowEvent we) {
    mfInstance.ActionExit.invoke();
  }
}

/*
 * @(#)DrawingPanel.java 1.0
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
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.*;
import java.awt.print.*;
import java.util.Vector;
import java.util.Arrays;
import cranks.mech.FourBarMechanism;
import cranks.geom.Triangle;
import cranks.geom.Line;
import cranks.geom.GeometricalObject;
import cranks.undo.ConstructionProcedure;
import cranks.undo.ConstructionStep;

/**
 * This custom drawing panel is used to implement the thread for animation
 *
 * OPERATING MODES
 * --------- -----
 * OPERATING_MODE_NONE: no operation mode selected
 * OPERATING_MODE_CLEAR: to clear workspace
 * OPERATING_MODE_ANIMATION: animation mode
 * OPERATING_MODE_DRAWING: design mode
 * OPERATING_MODE_PANNING: view panning mode
 *
 * MOUSE MODES
 * ----- -----
 * MOUSE_MODE_NONE: no drawing with mouse
 * MOUSE_MODE_POINT: drawing points with mouse
 * MOUSE_MODE_LINE: drawing lines with mouse joining every 2 points clicked
 * MOUSE_MODE_TRIANGLE: drawing triangles with mouse joining every 3 points clicked
 */

public class DrawingPanel extends JPanel implements Runnable, ActionListener,
                                             Printable, MouseListener, Editor, MouseMotionListener{

  //constants for use when querying state of particular mode
  public static final int OPERATION = 1;
  public static final int MOUSE = 2;

  //constants defining the applet mode
  public static final int OPERATING_MODE_NONE = 0;
  public static final int OPERATING_MODE_CLEAR = 1;
  public static final int OPERATING_MODE_ANIMATION = 2;
  public static final int OPERATING_MODE_DRAWING = 3;
  public static final int OPERATING_MODE_PANNING = 4;

  //constants defining the mouse mode
  public static final int MOUSE_MODE_NONE = 0;
  public static final int MOUSE_MODE_POINT = 1;
  public static final int MOUSE_MODE_LINE = 2;
  public static final int MOUSE_MODE_TRIANGLE = 3;

  //constants that specify the nature of construction performed using mouse
  public final static int POINT = 1;
  public final static int LINE = 2;
  public final static int TRIANGLE = 3;

  ///threading part
  private transient Thread animthread;
  private boolean animationRunning = false, allowRightClicks = true;
  private int operatingMode; //used to set the operating mode
  private int oldOperatingMode; //contains old value of operating mode
  private int mouseMode; //used to set the mouse operating mode

  private cranks.geom.Point pan = new cranks.geom.Point();
  private cranks.geom.Point oldPan = new cranks.geom.Point();
  private double zoom;
  private Dimension drawingArea;
  private JPanel pButtons = new JPanel();
  private JLabel lStatusMessage;

  private int id;
  private Vector<GeometricalObject> objects; //Vector containing objects
  private MainFrame mfInstance;
  private FourBarMechanism mechanism;
  private boolean clickable;
  private int mouseClickCounter = 0;
  private cranks.geom.Point mousePosition = new cranks.geom.Point();
  private java.awt.Point mouseScreenPosition = new java.awt.Point();
  private cranks.geom.Point[] mouseClicks = new cranks.geom.Point[3];

  private Cursor cursorDefault = new Cursor(0); //Default arrow cursor;
  private Cursor cursorHand = new Cursor(12); //Hand-shaped cursor
  private Cursor cursorCrosshair = new Cursor(1); //Cross-Hair cursor

  private JPopupMenu popMenu = new JPopupMenu();
  private JMenu[] popSubMenu = new JMenu[4];

  public DrawingPanel(JFrame mainFrame, Vector<GeometricalObject> Objects,
                      FourBarMechanism Mechanism) {
    id = 6;
    objects = Objects;
    mfInstance = (MainFrame)mainFrame;
    addUndoableEditListener(mfInstance.constructProcedure);
    mechanism = Mechanism;
    zoom = 1;
    jbInit();
  }

  private void jbInit() {
    addMouseListener(this);
    addMouseMotionListener(this);

    setLayout(new BorderLayout());
    setBackground(Color.white);
    pButtons = createBottomPanel();
    add(pButtons, BorderLayout.PAGE_END);
    initializePopupMenu();
    setCursor(new Cursor(1));
    changeMode(OPERATING_MODE_NONE, MOUSE_MODE_NONE);
    setStatusMessage();
    setPreferredSize(new Dimension(500, 500));
    setOpaque(true);
  }

  // Create the bottom pane.
  private JPanel createBottomPanel() {

    JPanel newPanel = new JPanel(new GridBagLayout());
    newPanel.setCursor(new Cursor(0));
    lStatusMessage = new JLabel("", JLabel.CENTER);
    lStatusMessage.setCursor(new Cursor(0));
    JButton bStartAnimation = new JButton("Start Animation");
    bStartAnimation.setMnemonic('t');
    JButton bPauseAnimation = new JButton("Pause Animation");
    bPauseAnimation.setMnemonic('u');
    JButton bStep = new JButton("Step");
    bStep.setMnemonic('e');
    JButton bToggleDirection = new JButton("Toggle Direction");
    bToggleDirection.setMnemonic('g');

    //Lay them out.
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.NONE;
    gbc.insets = new Insets(5,5,5,5);
    gbc.weightx = 0.5;
    gbc.weighty = 0.5;
    gbc.gridx = 0;
    gbc.gridy = 0;
    newPanel.add(bStartAnimation, gbc);
    gbc.gridx = 1;
    newPanel.add(bPauseAnimation, gbc);
    gbc.gridx = 2;
    newPanel.add(bStep, gbc);
    gbc.gridx = 3;
    newPanel.add(bToggleDirection, gbc);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    newPanel.add(lStatusMessage, gbc);
    newPanel.setBackground(Color.white);
    newPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    
    for(int i = 0; i < newPanel.getComponentCount() - 1; i++) {
      JButton button = ((JButton)newPanel.getComponent(i));
      button.setCursor(cursorHand);
      button.addActionListener(this);
      button.setActionCommand(button.getText());
    }
    return newPanel;
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
      if (listeners[i] == ConstructionProcedure.class) {
        ((ConstructionProcedure)listeners[i+1]).irreversibleEditHappened();
      }
    }
  }

  public void start() {
    if (animthread == null) {
      animthread = new Thread(this);
      animthread.setPriority(Thread.NORM_PRIORITY);
      animthread.start();
    }
  }

  public void stop() {
    animthread = null;
  }

  public void run() {
    while (animationRunning) { 
      mechanism.step();
      repaint();
      try {
        Thread.sleep(mechanism.getAnimationDelay());
      } catch (InterruptedException e) {
        System.err.println("No thread exists");
      }
    }
    stop();
  }

  public void pauseAnimation() {
    animationRunning = false;
  }

  public void startAnimation() {
    animationRunning = true;
    start();
  }
  //end of threading part

  public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
    if (pi >= 1) {
      return Printable.NO_SUCH_PAGE;
    }
    Graphics2D g2d = (Graphics2D)g;
    AffineTransform oldTransformState = g2d.getTransform();
    g2d.translate(pf.getImageableX(), pf.getImageableY());
    double scale_x = g2d.getClipBounds().getWidth()/drawingArea.getWidth();
    double scale_y = g2d.getClipBounds().getHeight()/drawingArea.getHeight();
    double scale = Math.min(scale_x, scale_y);
    if (scale > 1) 
      scale = 1;
    g2d.scale(scale, scale);
    paintComponent(g2d);
    g2d.setTransform(oldTransformState);
    return Printable.PAGE_EXISTS;
  }
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Color.white);
    g.fillRect(0, 0, drawingArea.width, drawingArea.height);
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
    AffineTransform oldTransformState = g2d.getTransform();
    g2d.translate(drawingArea.getWidth()/2, drawingArea.getHeight()/2);
    g2d.scale(zoom, zoom);
    g2d.translate(-drawingArea.getWidth()/2, -drawingArea.getHeight()/2);
    g2d.translate(pan.getX(), pan.getY());
    g2d.translate(0, drawingArea.getHeight());
    g2d.transform(new AffineTransform(1, 0, 0, -1, 0, 0));
    updateImage(g2d);
    g2d.setTransform(oldTransformState);
    g.setClip(pButtons.getLocation().x, pButtons.getLocation().y,
              pButtons.getWidth(), pButtons.getHeight());
  }

  public void resizeComponents() {
    drawingArea = new Dimension(getSize().width - 10, 
                                      getSize().height - pButtons.getHeight());
    repaint();
  }

  private void updateImage(Graphics2D g2d){
    switch(operatingMode){
      case OPERATING_MODE_ANIMATION: //To animate a mechanism
        drawMechanism(g2d);
      break;

      case OPERATING_MODE_DRAWING: //To design a mechanism
        drawObjects(g2d);///draw objects
        if (mouseClickCounter > 0) {
          g2d.setColor(Color.gray);
          for (int i = 0; i<mouseClickCounter; i++)
             g2d.fill(new java.awt.geom.Ellipse2D.Double(
                 mouseClicks[i].getX() - 2, mouseClicks[i].getY() - 2, 4, 4));
          g2d.setColor(Color.black);
        }
      break;

      case OPERATING_MODE_CLEAR: //To clear the work area
        clearArea(g2d);
        changeMode(OPERATING_MODE_NONE, MOUSE_MODE_NONE);
      break;
      
      case OPERATING_MODE_PANNING:
        if (oldOperatingMode == OPERATING_MODE_ANIMATION)
          drawMechanism(g2d);
        else if (oldOperatingMode == OPERATING_MODE_DRAWING)
          drawObjects(g2d);
      break;
      
      default:
    }
  }

  //To animate the mechanism
  private void drawMechanism(Graphics2D g2d) {
    AffineTransform oldTransformState = g2d.getTransform();
    g2d.translate(drawingArea.getWidth()/2, drawingArea.getHeight()/2);
    mechanism.drawMechanism(g2d);
    g2d.setTransform(oldTransformState);
  }

  //To draw the different objects on the screen
  private void drawObjects(Graphics2D g2d) {
    for (int i = 0; i<objects.size(); i++)
      objects.elementAt(i).draw(g2d);
  }

  //To clear the drawing area and the object vector
  public void clearArea(Graphics2D g2d) {
    //the graphics object has already been cleared in the paint() method
    //so only the vector will be cleared so that user can redraw the figures
    objects.removeAllElements();
  }
  
  public double getZoom() {
    return zoom;
  }
  
  public void setZoom(double value) {
    zoom = value;
    repaint();
  }

  //To set the mode
  public void changeMode(int a, int b) {
    pauseAnimation();
    oldOperatingMode = operatingMode;
    operatingMode = a;
    mouseMode = b;
    mouseClickCounter = 0;
    for (int i = 0; i<pButtons.getComponentCount() - 1; i++)
      pButtons.getComponent(i).setEnabled(a == OPERATING_MODE_ANIMATION);
    setStatusMessage();
    repaint();
  }

  //To obtain the current operating mode
  public int getMode(int modeSelected) {
    if (modeSelected == OPERATION)
      return operatingMode;
    else if (modeSelected == MOUSE)
      return mouseMode;
    else
      System.err.println("Unrecognized mode");
      return -1;
  }

  //To display text indicating the current working mode
  private void setStatusMessage() {
    String s = "No mode selected "+operatingMode+" "+mouseMode;
    switch (operatingMode) {
      case OPERATING_MODE_NONE: //no operation mode selected
        s = "No mode selected";
      break;

      case OPERATING_MODE_ANIMATION: //animation mode
        s = "Animation mode";
      break;

      case OPERATING_MODE_DRAWING: //drawing mode
        switch (mouseMode) {
          case MOUSE_MODE_NONE:
            s = "Drawing mode";
          break;

          case MOUSE_MODE_POINT:
            s = "Drawing points using mouse";
          break;

          case MOUSE_MODE_LINE:
            s = "Drawing lines using mouse";
          break;

          case MOUSE_MODE_TRIANGLE:
            s = "Drawing triangles using mouse";
          break;
        }
      break;
      
      case OPERATING_MODE_PANNING:
        s = "Drag mouse to pan view";
      break;
      
      default:
    }
    lStatusMessage.setText(s);
  }

  //this function is used to initialize the pop up menu
  private void initializePopupMenu() {
    popMenu.add(mfInstance.ActionIntersect);
    popMenu.add(mfInstance.ActionTranslateRotate);
    popMenu.add(mfInstance.ActionHide);
    popMenu.add(mfInstance.ActionShowFieldValue);

    popSubMenu[0] = new JMenu("Point");
    popSubMenu[0].add(mfInstance.ActionPointAddMouse);
    popSubMenu[0].add(mfInstance.ActionPointAddCoordinates);
    popSubMenu[0].add(mfInstance.ActionPointModify);

    popSubMenu[1] = new JMenu("Line");
    popSubMenu[1].add(mfInstance.ActionLineAddMouse);
    popSubMenu[1].add(mfInstance.ActionLineAddCoordinates);

    popSubMenu[2] = new JMenu("Circle");
    popSubMenu[2].add(mfInstance.ActionCircleAddCoordinates);

    popSubMenu[3] = new JMenu("Triangle");
    popSubMenu[3].add(mfInstance.ActionTriangleAddMouse);
    popSubMenu[3].add(mfInstance.ActionTriangleAddCoordinates);
    popSubMenu[3].add(mfInstance.ActionTriangleCopyMove);
  }

  //capture the mouse move event
  public void mouseMoved(MouseEvent e) {
    mousePosition.setCoordinates(
          (int)(drawingArea.getWidth()/2 + 
            (e.getX() - drawingArea.getWidth()/2)/zoom - pan.getX()),
          (int)(drawingArea.getHeight()/2 - 
            (e.getY() - drawingArea.getHeight()/2)/zoom + pan.getY()));
    //check if the point is over an object
    clickable = false;
    String toolTipText = "<HTML>";
    if (getMode(OPERATION) == OPERATING_MODE_DRAWING) {
      for (int i = 0; i<objects.size(); i++) {
        GeometricalObject ob = objects.elementAt(i);
        if (ob.isVisible() && mousePosition.canClick(ob))
          toolTipText += (ob.toString() + "<P>");
      }
    }
    if (toolTipText.equalsIgnoreCase("<HTML>"))
      toolTipText = null;
    else
      toolTipText += "</HTML>";
    setToolTipText(toolTipText);
    if (getMode(OPERATION) == OPERATING_MODE_DRAWING) {
      if (clickable = (toolTipText != null))
        setCursor(cursorHand);
      else
        setCursor(cursorCrosshair);
    }
    else
      setCursor(cursorDefault);
    if ((e.getX()<drawingArea.getWidth()) && (e.getY()<drawingArea.getHeight())) {
      setStatusMessage();
      if (getMode(DrawingPanel.OPERATION) != DrawingPanel.OPERATING_MODE_PANNING)
        lStatusMessage.setText(lStatusMessage.getText()+"  ("+
          (int)mousePosition.getX()+","+(int)mousePosition.getY()+")");
    }
  }
  
  public void mousePressed(MouseEvent e) {
    if (getMode(OPERATION) == OPERATING_MODE_PANNING) {
      mouseScreenPosition.setLocation(e.getX(), e.getY());
      oldPan.setCoordinates(pan.getX(), pan.getY());
    }
  }
  
  public void mouseDragged(MouseEvent e) {
    if (getMode(OPERATION) == OPERATING_MODE_PANNING) {
      pan.setCoordinates(oldPan.getX() + e.getX() - mouseScreenPosition.getX(), 
                         oldPan.getY() + e.getY() - mouseScreenPosition.getY());
      lStatusMessage.setText("Panning ("+(int)pan.getX()+","+(int)(-pan.getY())+")");
      repaint();
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (getMode(OPERATION) == OPERATING_MODE_PANNING)
      changeMode(oldOperatingMode, MOUSE_MODE_NONE);
  }
  
  public void mouseClicked(MouseEvent e) {
    //create rectangle from current drawingArea of applet
    Rectangle r=new Rectangle(drawingArea);
    //check if clicked point is within drawingArea
    if(r.contains(e.getX(), e.getY())) {
      //check if we are in drawing mode
      if(operatingMode == OPERATING_MODE_DRAWING) {
        //check for left clicks and add objects to the vector
        if(e.getButton()==1) {
          //check if we are in the mouse drawing mode
          if (mouseMode != MOUSE_MODE_NONE) {
            //store point clicked and then increment click counter
            mousePosition = new cranks.geom.Point(
                (int)(drawingArea.getWidth()/2 + 
                  (e.getX() - drawingArea.getWidth()/2)/zoom - pan.getX()),
                (int)(drawingArea.getHeight()/2 - 
                  (e.getY() - drawingArea.getHeight()/2)/zoom + pan.getY()));
            mouseClicks[mouseClickCounter++] = mousePosition;
            mousePosition = new cranks.geom.Point();
            //check if num of clicks so far is equal to num of points
            //needed to draw object of type specified in mouseMode
            if (mouseClickCounter == mouseMode) {
              //create the object and add it to objects vector
              GeometricalObject objectToBeAdded = null;
              ConstructionStep step = null;
              switch (mouseMode) {
              	case MOUSE_MODE_POINT:
                  objectToBeAdded = mouseClicks[0];
                  step = new ConstructionStep(this,
                              new Object[]{objectToBeAdded}, POINT);
                break;

              	case MOUSE_MODE_LINE:
              	  objectToBeAdded = new Line(mouseClicks[0], mouseClicks[1]);
                  step = new ConstructionStep(this,
                              new Object[]{objectToBeAdded}, LINE);
              	break;

              	case MOUSE_MODE_TRIANGLE:
              	  objectToBeAdded = new Triangle(mouseClicks[0], mouseClicks[1],
              	                                 mouseClicks[2]);
                  step = new ConstructionStep(this,
                              new Object[]{objectToBeAdded}, TRIANGLE);
                break;
              	default :
              }
              doConstruction(step);
              mouseClickCounter = 0;
            }
          }
        }
        //check for right clicks and bring up popup menu
        if ((getMode(OPERATION) == OPERATING_MODE_DRAWING) 
            && (e.getButton()==3) && allowRightClicks) {
          for(int i = 0; i < popMenu.getComponentCount(); i++)
            if (i >= 4)
              popMenu.remove(i);
          
          boolean[] selectedTypes = new boolean[4];
          Arrays.fill(selectedTypes, false);
          //check if the point is over an object
          if (clickable) {
            for (int i = 0; i<objects.size(); i++) {
              GeometricalObject ob = objects.elementAt(i);
              if (mousePosition.canClick(ob)) {
                selectedTypes[ob.getType() - 2] = true;
              }
            }
            for (int i = 0; i<4; i++) {
              if (selectedTypes[i]) {
                popMenu.add(popSubMenu[i]);
              }
            }
          }
          add(popMenu);
          popMenu.show(e.getComponent(), e.getX(), e.getY());
        }
      }
    }
    repaint(); //force repainting
  }

  public void mouseEntered(MouseEvent e) {}
  
  public void mouseExited(MouseEvent e) {}
  
  public void enablePopupMenu(boolean value) {
    allowRightClicks = value;
  }

  public void doConstruction(ConstructionStep step) {
    GeometricalObject objectToBeAdded = (GeometricalObject)step.getInputs()[0];
    objectToBeAdded.addToObjects(objects);
    if (step.getConstructionType() == POINT) {
      cranks.geom.Point p = (cranks.geom.Point)objectToBeAdded;
      step.setOutputs(new Object[]{p});
      fireUndoableEditUpdate(new UndoableEditEvent(this, step));
    }
    else if (step.getConstructionType() == LINE) {
      Line l = (Line)objectToBeAdded;
      step.setOutputs(new Object[]{l.getStart(), l.getEnd(), l});
      fireUndoableEditUpdate(new UndoableEditEvent(this, step));
    }
    else if (step.getConstructionType() == TRIANGLE) {
      Triangle t = (Triangle)objectToBeAdded;
      step.setOutputs(new Object[]
        {t.getSides()[0].getStart(), t.getSides()[1].getStart(),
         t.getSides()[0], t.getSides()[2].getStart(), t.getSides()[1],
         t.getSides()[2], t});
      fireUndoableEditUpdate(new UndoableEditEvent(this, step));
    }
    else
      System.err.println("Bad constructon type");
  }

  public void undoConstruction(ConstructionStep step) {
    if ((step.getConstructionType() == POINT) ||
        (step.getConstructionType() == LINE) ||
        (step.getConstructionType() == TRIANGLE)) {
      for (int i = 0; i<step.getOutputs().length; i++)
        ((GeometricalObject)step.getOutputs()[i]).removeFromObjects(objects);
      for (int i = 0; i<objects.size(); i++)
        objects.elementAt(i).setNumber(i+1);
    }
    else
      System.err.println("Bad constructon type");
  }

  public void redoConstruction(ConstructionStep step) {
    if ((step.getConstructionType() == POINT) ||
        (step.getConstructionType() == LINE) ||
        (step.getConstructionType() == TRIANGLE)) {
      ((GeometricalObject)step.getOutputs()[step.getOutputs().length - 1]).
                addToObjects(objects);
    }
    else
      System.err.println("Bad constructon type");
  }

  public void redesign(ConstructionStep step) {
    if (!step.needsModification()) {
      step = new ConstructionStep(step.getEditor(), step.getInputs(),
                                  step.getConstructionType());
      doConstruction(step);
    }
    if (step.getConstructionType() == POINT)
      changeMode(OPERATING_MODE_DRAWING, MOUSE_MODE_POINT);
    else if (step.getConstructionType() == LINE)
      changeMode(OPERATING_MODE_DRAWING, MOUSE_MODE_LINE);
    else if (step.getConstructionType() == TRIANGLE)
      changeMode(OPERATING_MODE_DRAWING, MOUSE_MODE_TRIANGLE);
  }

  ///this is the action listener for the pop up menu
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("Start Animation"))
      bStartAnimation_actionPerformed();
    if (command.equals("Pause Animation"))
      bPauseAnimation_actionPerformed();
    if (command.equals("Step"))
      bStep_actionPerformed();
    if (command.equals("Toggle Direction"))
      bToggleDirection_actionPerformed();
  }

  //this is used to start the animation
  public void bStartAnimation_actionPerformed() {
    startAnimation();
  }

  //this is used to stop the animation
  public void bPauseAnimation_actionPerformed() {
    pauseAnimation();
  }

  //this is used to step through the animation
  public void bStep_actionPerformed() {
    pauseAnimation();
    mechanism.step();
    repaint();
  }

  //this is used to toggle direction of rotation
  public void bToggleDirection_actionPerformed() {
    mechanism.toggleDirection();
  }

  public String getPresentationName(ConstructionStep step) {
 	  if ((step.getConstructionType() == POINT) ||
 	      (step.getConstructionType() == LINE) ||
 	      (step.getConstructionType() == TRIANGLE))
 	    return ("Draw " + step.getInputs()[0]);
    else {
      System.err.println("Bad construction type");
      return "";
    }
  }

  public String getDisplayName(ConstructionStep step) {
    String displayName = "";
    switch (step.getConstructionType()) {
    	case POINT:
        cranks.geom.Point p = (cranks.geom.Point)(step.getInputs()[0]);
        displayName = "Draw point at " + p.locationToString();
    	break;

    	case LINE:
        Line l = (Line)(step.getInputs()[0]);
        displayName = "Draw line from " +
                l.getStart().locationToString() + " to " + l.getEnd().locationToString();
      break;

    	case TRIANGLE:
        Triangle t = (Triangle)(step.getInputs()[0]);
        displayName = "Draw triangle using points " +
            t.getSides()[0].getStart().locationToString() + "," +
            t.getSides()[1].getStart().locationToString() + " and " +
            t.getSides()[2].getStart().locationToString();
    	break;

    	default :
    	  System.err.println("Bad construction type");
    }
    displayName +=  " - Output: ";
    for(int i = 0; i < step.getOutputs().length - 1; i++)
      displayName += step.getOutputs()[i] + ", ";
    displayName += step.getOutputs()[step.getOutputs().length - 1];
    return displayName;
  }
}
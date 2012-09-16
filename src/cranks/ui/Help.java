/*
 * @(#)Help.java 1.0
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
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import java.beans.*;
import java.awt.Dimension;
import java.io.*;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;
import java.net.URL;
import cranks.resource.ResourceManager;

/**
 * This class is used to provide help to users in order to use this project.
 */

public class Help extends JDialog implements PropertyChangeListener, 
                                                         TreeSelectionListener {
  private JTabbedPane tpHelp = new JTabbedPane();
  private JOptionPane optionPane;
  private JSplitPane sppHelp;

  private JTree tree;
  private JScrollPane scpTopics;
          
  private JEditorPane epDisplay;
  private JScrollPane scpDisplay;
  
  private boolean initialized = true;

  public Help(JFrame frame, String title) {
    super(frame, title, false);
    jbInit();
    pack();
  }

  private void jbInit() {
    tpHelp.setPreferredSize(new Dimension(600, 500));
    tree = createTree();
    tree.addTreeSelectionListener(this);
    tree.getSelectionModel().setSelectionMode(
                                      TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setMinimumSize(new Dimension(20, 20));
    scpTopics = new JScrollPane(tree);
    
    epDisplay = createDisplay(); 
    epDisplay.setMinimumSize(new Dimension(20, 20));
    scpDisplay = new JScrollPane(epDisplay);
     
    sppHelp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scpTopics, scpDisplay);
    sppHelp.setOneTouchExpandable(true);
    sppHelp.setDividerLocation(150);
    tpHelp.addTab("Contents", sppHelp);
    
    Object[] options = {"Close"};
    optionPane = new JOptionPane(tpHelp, JOptionPane.PLAIN_MESSAGE,
                   JOptionPane.OK_OPTION, null, options, options[0]);
    setContentPane(optionPane);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new JDialogWindowAdapter(this));
    optionPane.addPropertyChangeListener(this);
  }
  
  public JTree createTree() {
    DefaultMutableTreeNode top = new DefaultMutableTreeNode("Contents");
    DefaultMutableTreeNode[] nodeLevels = new DefaultMutableTreeNode[0];
    // open tree data 
    URL url = ResourceManager.getResource("tree.txt");

  	try {
	    // convert url to buffered string
	    InputStream is = url.openStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader reader = new BufferedReader(isr);

	    // read one line at a time, put into tree
	    String line = reader.readLine();
      int numLevels = 0;
      
      if (line != null){
        while (line.startsWith("#")) 
          line = reader.readLine();
        numLevels = Integer.parseInt(line);
        line = reader.readLine();
        nodeLevels = new DefaultMutableTreeNode[numLevels + 1];
        nodeLevels[0] = top;
      }
	    while(line != null) {
        if (!line.startsWith("#")) {
          int level = Integer.parseInt(line.substring(0,1));
          line = line.substring(line.indexOf(",") + 1);
          String nodeDescription = line.substring(0, line.indexOf(","));
          String nodeURL = line.substring(line.indexOf(",") + 1, line.length());
          nodeLevels[level] = new DefaultMutableTreeNode(new 
                                           HelpTopic(nodeDescription, nodeURL));
          nodeLevels[level - 1].add(nodeLevels[level]);
        }
        line = reader.readLine();
	    }
    } catch (IOException e) {
      showErrorDialog("Unable to read resource tree.txt", true);
    } catch (NumberFormatException nfe) {
      showErrorDialog("Invalid format tree.txt", true);
    }

    return new JTree(top) {
	    public java.awt.Insets getInsets() {
        return new java.awt.Insets(5,5,5,5);
	    }
    };
  }

  public JEditorPane createDisplay() {
    JEditorPane display = null;
    try {
	    URL url = null;
	    try {
    		url = ResourceManager.getResource("help/index.html");
      } catch (Exception e) {
        showErrorDialog("Unable to open help file index.html", true);
      }
      if(url != null) {
        display = new JEditorPane(url);
        display.setEditable(false);
        display.addHyperlinkListener(createHyperLinkListener());
      }
    } catch (IOException e) {
      showErrorDialog(e.getMessage(), true);
    }
    display.setEditable(false);
    return display;
  }
  
  private HyperlinkListener createHyperLinkListener() {
    return new HyperlinkListener() {
	    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          if (e instanceof HTMLFrameHyperlinkEvent)
            ((HTMLDocument)epDisplay.getDocument()).processHTMLFrameHyperlinkEvent(
              (HTMLFrameHyperlinkEvent)e);
          else {
            if (e.getURL().getProtocol().equalsIgnoreCase("html")) {
              try {
                Desktop.browse(e.getURL());
              } catch (DesktopException de) {
                showErrorDialog("Could not open default external browser", false);
              }
            }
            if (e.getURL().getProtocol().equalsIgnoreCase("file")) {
              try {
                epDisplay.setPage(e.getURL());
              } catch (IOException ioe) {
                showErrorDialog(ioe.getMessage(), true);
              }
            }
          }
        }
	    }
    };
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
      clearAndHide();
    }
  }
  
  public void valueChanged(TreeSelectionEvent tse) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.
                                                 getLastSelectedPathComponent();
    if (node.isLeaf())
      try {
        epDisplay.setPage(((HelpTopic)node.getUserObject()).getUrl());
      } catch (IOException e) {
        showErrorDialog("Unable to open help file", true);
      }
  }
  
  public void showErrorDialog(String error, boolean criticalError) {
    JOptionPane.showMessageDialog(this, 
            "The system encountered the following error :\n" + error,
            "Error", JOptionPane.ERROR_MESSAGE);
    initialized = !criticalError;
  }
  
  public void clearAndHide() {
    dispose();
  }

  public void initDialog() {
    setLocationRelativeTo(null);
    if (initialized)
      setVisible(true);
    else
      showErrorDialog("Help files missing or corrupted", true);
  }
}

class HelpTopic {
  
  private String name;
  private URL topic;
  
  public HelpTopic(String Name, String Topic) {
    name = Name;
    if ((Topic != null ) && (Topic != ""))
      topic = ResourceManager.getResource(Topic);
    else 
      topic = null;
  }
  
  public String toString() {
    return name;
  }
  
  public URL getUrl() {
    return topic;
  }
  
}
/*
 * @(#)SplashScreen.java 1.0
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
import cranks.resource.ResourceManager;

/**
 * This class creates a splash screen containing copyright information
 *
 * @author Aravind Alwan
 */
public class SplashScreen extends JWindow {
  
  private JPanel pSplash;
  private JPanel pPicture;
  private JLabel lPicture;
  private JLabel lCopyright;
  
  /** Creates a new instance of SplashScreen */
  public SplashScreen(JFrame mainFrame) {
    super(mainFrame);
    jbInit();
    pack();
    java.awt.Rectangle screenRect = mainFrame.getGraphicsConfiguration().getBounds();
    setLocation(screenRect.x + screenRect.width/2 - getSize().width/2,
		screenRect.y + screenRect.height/2 - getSize().height/2);
  }
  
  private void jbInit() {
    pSplash = new JPanel(new BorderLayout());
    pSplash.setBorder(BorderFactory.createLineBorder(java.awt.Color.black));
    
    pPicture = new JPanel();
    pPicture.setBorder(BorderFactory.createLineBorder(java.awt.Color.black));
    lPicture = new JLabel(new ImageIcon(ResourceManager.getResource("splash.jpg")));
    pPicture.add(lPicture);
    
    lCopyright = new JLabel(getCopyrightNotice());
    lCopyright.setHorizontalAlignment(JLabel.CENTER);
    
    pSplash.add(pPicture, BorderLayout.CENTER);
    pSplash.add(lCopyright, BorderLayout.SOUTH);
    
    setContentPane(pSplash);
  }
  
  private String getCopyrightNotice() {
    return "<HTML><CENTER>CRANKS version 1.0, Copyright (C) 2004, 2005 Aravind " +
           "Alwan<P><P>CRANKS comes with ABSOLUTELY NO WARRANTY. This is free " +
           "software,<P>and you are welcome to redistribute it under the<P>" +
           "GNU General Public License<P>For details please see the file " +
           "COPYING in the CRANKS installation directory</CENTER></HTML>";
  }
  
  public JPanel getSplash() {
    return pSplash;
  }
  
}

/*
 * @(#)BaseApplet.java 1.0
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

public class BaseApplet extends JApplet {

  public static MainFrame mf_1;

  //Initialize the applet
  public void init() {
    String vers = System.getProperty("java.version");
    if (vers.compareTo("1.5.0") < 0) {
      JOptionPane.showMessageDialog(this, "This program needs JRE 1.5.0" +
       " or higher", "Version Conflict", JOptionPane.ERROR_MESSAGE);
    }
    else {
      MainFrame.start(this);
    }
  }
}

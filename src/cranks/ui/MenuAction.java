/*
 * @(#)MenuAction.java 1.0
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

public abstract class MenuAction extends AbstractAction {
  
  public MenuAction(String Name, int MnemonicKey, String ActionCommand, 
                    KeyStroke AcceleratorKey) {
    super(Name);
    putValue(Action.ACTION_COMMAND_KEY, ActionCommand);
    putValue(Action.MNEMONIC_KEY, MnemonicKey);
    putValue(Action.ACCELERATOR_KEY, AcceleratorKey);
  }
  
  public void invoke() {
    actionPerformed(null);
  }
  
}
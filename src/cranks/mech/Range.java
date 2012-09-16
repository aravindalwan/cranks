/*
 * @(#)Range.java 1.0
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

package cranks.mech;

import cranks.geom.Angle;

public class Range implements java.io.Serializable {
  
  private static final long serialVersionUID = 2594384221429344267L;

/**
 * Range is defined by a Lower Limit angle and an Upper Limit angle, denoted
 * by the start and end angles respectively.
 */

  private Angle start;
  private Angle end;

  public Range() {
    start = new Angle();
    end = new Angle();
  }

  public void setRange(double p, double q) {
    start.setAngle(p);
    end.setAngle(q);
  }

  public Angle getStart() {
    return start;
  }

  public Angle getEnd() {
    return end;
  }

  public Angle getRange() {
    return end.sub(start);
  }

}
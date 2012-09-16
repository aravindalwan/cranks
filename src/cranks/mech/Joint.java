/*
 * @(#)Joint.java 1.0
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

import cranks.geom.Point;

public class Joint extends Point {

  private Link preLink;
  private Link postLink;
  private Range rotationRange;

  public Joint() {
    this(null, null);
  }

  public Joint(Link PreLink, Link PostLink) {
    preLink = PreLink;
    postLink = PostLink;
    rotationRange = new Range();
  }

  public Link getPreviousLink() {
    return preLink;
  }

  public Link getNextLink() {
    return postLink;
  }

  public Range getRotationRange() {
    return rotationRange;
  }

  public void setRotationRange(Range newRange) {
    rotationRange.setRange(newRange.getStart().getAngle(),
                           newRange.getEnd().getAngle());
  }

}
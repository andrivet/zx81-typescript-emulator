/* EightyOne  - A Windows ZX80/81/clone emulator.
 * Copyright (C) 2003-2006 Michael D Wynne
 * Java translation (C) 2006 Simon Holdsworth
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * 
 *
 */
package jtyone.display;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * This class allows display of the Jtyone emulator as an application or
 * applet.
 * 
 * @author Simon Holdsworth
 */
public class AccCanvas 
extends Canvas
  {
  private Dimension mSize;
  private AccDraw mDisplayDrawer;
  
  public AccCanvas(AccDraw dd)
    {
    mDisplayDrawer = dd;
    }
  
  public void setRequiredSize( Dimension size )
    {
    mSize = size;
    setSize(size);
    }
  
  public void paint(Graphics g)
    {
    mDisplayDrawer.RedrawDisplay(g);
    }
  
  // The follow methods are all to ensure that the canvas stays at
  // the required size.  Not really necessary for an application, but
  // needed for an applet to make sure there isn't any minor scaling
  // that would distort the display.
  public void setSize(Dimension d)
    {
    if( mSize != null &&
        !d.equals(mSize) )
      return;
    super.setSize(d);
    }
  
  public void setSize(int w, int h)
    {
    if( mSize != null &&
        (w != mSize.width || h != mSize.height) )
      return;
    
    super.setSize(w,h);
    }
  
  public void setBounds(Rectangle r)
    {
    if( mSize != null &&
        (r.width != mSize.width || r.height != mSize.height) )
      return;
    
    super.setBounds(r);
    }

  public void setBounds(int x, int y, int w, int h)
    {
    if( mSize != null &&
        (w != mSize.width || h != mSize.height) )
      return;
    
    super.setBounds(x,y,w,h);
    }

  public Dimension getMinimumSize() 
    {
    return mSize;
    }
  
  public Dimension getPreferredSize() 
    {
    return mSize;
    }
  
  public Dimension getMaximumSize() 
    {
    return mSize;
    }
  }
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
 * zx81config.c
 *
 */

package jtyone.config;

import jtyone.display.Scanline;
import jtyone.io.Tape;

public abstract class Machine
  {
  public abstract void initialise(ZX81Config config);
  public abstract int do_scanline(Scanline line);
  public abstract void writebyte(int Address, int Data);
  public abstract int readbyte(int Address);
  public abstract int opcode_fetch(int Address);
  public abstract void writeport(int Address, int Data);
  public abstract int readport(int Address);
  public abstract int contendmem(int Address, int states, int time);
  public abstract int contendio(int Address, int states, int time);
  public abstract void reset();
  public abstract void nmi();
  public abstract void exit();
  public abstract boolean stop();
  public abstract Tape getTape();
  
  public int clockspeed;
  public int tperscanline;
  public int tperframe;
  public int intposition;
  public int scanlines;
  public String CurRom;
  
  public int[] memory;
  }
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

/*
#include <string.h>
#include <dir.h>

#include "zx81config.h"

*/

  // From zx81config.h...

  //typedef struct
public class TVOptions
  {
  //int AdvancedEffects;
  //int DotCrawl;
  //int Interlaced;
  //int DisableAdvanced;
  public boolean AdvancedEffects;
  public boolean DotCrawl;
  public boolean Interlaced;
  public boolean DisableAdvanced;
  public int frequency;
  //} TV;
  }
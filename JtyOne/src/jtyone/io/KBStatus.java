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
 * kbstatus.cpp
 */

//---------------------------------------------------------------------------

package jtyone.io;

import java.awt.event.KeyEvent;

import jtyone.config.ZX81Config;
import jtyone.config.ZX81ConfigDefs;


/*
#include <vcl.h>
#pragma hdrstop

#include "kbstatus.h"
#include "zx81.h"
#include "zx81config.h"
*/

public class KBStatus
implements ZX81ConfigDefs
  {
  int PCShift=1;
  //unsigned int PCALT=0;
  int PCALT=0;
  
  // FROM zx81/zx81.h
  private static final int kbD0 = 1;
  private static final int kbD1 = 2;
  private static final int kbD2 = 4;
  private static final int kbD3 = 8;
  private static final int kbD4 = 16;
  //private static final int kbD5 = 32;

  private static final int kbA8  = 0;
  private static final int kbA9  = 1;
  private static final int kbA10 = 2;
  private static final int kbA11 = 3;
  private static final int kbA12 = 4;
  private static final int kbA13 = 5;
  private static final int kbA14 = 6;
  private static final int kbA15 = 7;
  
  // For convenience in the following tables.
  static final int VK_SHIFT = KeyEvent.VK_SHIFT;
  static final int VK_RETURN = KeyEvent.VK_ENTER;
  static final int VK_SPACE = KeyEvent.VK_SPACE;
  static final int VK_NUMPAD0 = KeyEvent.VK_NUMPAD0;
  static final int VK_NUMPAD1 = KeyEvent.VK_NUMPAD1;
  static final int VK_NUMPAD2 = KeyEvent.VK_NUMPAD2;
  static final int VK_NUMPAD3 = KeyEvent.VK_NUMPAD3;
  static final int VK_NUMPAD4 = KeyEvent.VK_NUMPAD4;
  static final int VK_NUMPAD5 = KeyEvent.VK_NUMPAD5;
  static final int VK_NUMPAD6 = KeyEvent.VK_NUMPAD6;
  static final int VK_NUMPAD7 = KeyEvent.VK_NUMPAD7;
  static final int VK_NUMPAD8 = KeyEvent.VK_NUMPAD8;
  static final int VK_NUMPAD9 = KeyEvent.VK_NUMPAD9;
  static final int VK_MULTIPLY = KeyEvent.VK_MULTIPLY;
  static final int VK_DIVIDE = KeyEvent.VK_DIVIDE;
  static final int VK_SUBTRACT = KeyEvent.VK_SUBTRACT;
  static final int VK_ADD = KeyEvent.VK_ADD;
  static final int VK_DECIMAL = KeyEvent.VK_DECIMAL;
  static final int VK_BACK = KeyEvent.VK_BACK_SPACE;
  static final int VK_DOWN = KeyEvent.VK_DOWN;
  static final int VK_UP = KeyEvent.VK_UP;
  static final int VK_LEFT = KeyEvent.VK_LEFT;
  static final int VK_RIGHT = KeyEvent.VK_RIGHT;
  static final int VK_CONTROL = KeyEvent.VK_CONTROL;
  static final int VK_MENU = KeyEvent.VK_ALT;   // TODO: is this correct?
  static final int VK_QUOTE = KeyEvent.VK_QUOTE;

    //struct kb *KeyMap;
  static kb[] KeyMap;
  
//  struct kb KBZX81[]=
  static int[][] KBZX81_ints =
    {
      { 0, VK_SHIFT , kbA8, kbD0, 255,255 },
      { 0, VK_RETURN , kbA14, kbD0, 255,255 },
      { 0, VK_SPACE , kbA15, kbD0, 255,255 },

      { 0, 'A' , kbA9, kbD0, 255,255 },
      { 0, 'B' , kbA15, kbD4, 255,255 },
      { 0, 'C' , kbA8, kbD3, 255,255 },
      { 0, 'D' , kbA9, kbD2, 255,255 },
      { 0, 'E' , kbA10, kbD2, 255,255 },
      { 0, 'F' , kbA9, kbD3, 255,255 },
      { 0, 'G' , kbA9, kbD4, 255,255 },
      { 0, 'H' , kbA14, kbD4, 255,255 },
      { 0, 'I' , kbA13, kbD2, 255,255 },
      { 0, 'J' , kbA14, kbD3, 255,255 },
      { 0, 'K' , kbA14, kbD2, 255,255 },
      { 0, 'L' , kbA14, kbD1, 255,255 },
      { 0, 'M' , kbA15, kbD2, 255,255 },
      { 0, 'N' , kbA15, kbD3, 255,255 },
      { 0, 'O' , kbA13, kbD1, 255,255 },
      { 0, 'P' , kbA13, kbD0, 255,255 },
      { 0, 'Q' , kbA10, kbD0, 255,255 },
      { 0, 'R' , kbA10, kbD3, 255,255 },
      { 0, 'S' , kbA9, kbD1, 255,255 },
      { 0, 'T' , kbA10, kbD4, 255,255 },
      { 0, 'U' , kbA13, kbD3, 255,255 },
      { 0, 'V' , kbA8, kbD4, 255,255 },
      { 0, 'W' , kbA10, kbD1, 255,255 },
      { 0, 'X' , kbA8, kbD2, 255,255 },
      { 0, 'Y' , kbA13, kbD4, 255,255 },
      { 0, 'Z' , kbA8, kbD1, 255,255 },

      { 0, '1' , kbA11, kbD0, 255,255 },
      { 0, '2' , kbA11, kbD1, 255,255 },
      { 0, '3' , kbA11, kbD2, 255,255 },
      { 0, '4' , kbA11, kbD3, 255,255 },
      { 0, '5' , kbA11, kbD4, 255,255 },
      { 0, '6' , kbA12, kbD4, 255,255 },
      { 0, '7' , kbA12, kbD3, 255,255 },
      { 0, '8' , kbA12, kbD2, 255,255 },
      { 0, '9' , kbA12, kbD1, 255,255 },
      { 0, '0' , kbA12, kbD0, 255,255 },

      { 0, VK_NUMPAD1 , kbA11, kbD0, 255,255 },
      { 0, VK_NUMPAD2 , kbA11, kbD1, 255,255 },
      { 0, VK_NUMPAD3 , kbA11, kbD2, 255,255 },
      { 0, VK_NUMPAD4 , kbA11, kbD3, 255,255 },
      { 0, VK_NUMPAD5 , kbA11, kbD4, 255,255 },
      { 0, VK_NUMPAD6 , kbA12, kbD4, 255,255 },
      { 0, VK_NUMPAD7 , kbA12, kbD3, 255,255 },
      { 0, VK_NUMPAD8 , kbA12, kbD2, 255,255 },
      { 0, VK_NUMPAD9 , kbA12, kbD1, 255,255 },
      { 0, VK_NUMPAD0 , kbA12, kbD0, 255,255 },

      //{ 0, VK_MULTIPLY , kbA8, kbD4, 255,255 },
      //{ 0, VK_DIVIDE , kbA15, kbD4, 255,255 },
      { 0, VK_MULTIPLY , kbA15, kbD4, kbA8, kbD0 },
      { 0, VK_DIVIDE , kbA8, kbD4, kbA8, kbD0 },

      //{ 1, 186, kbA8, kbD2, kbA8, kbD0 },         // ;
      { 1, ';', kbA8, kbD2, kbA8, kbD0 },           // ;
      { 2, ';', kbA8, kbD1, kbA8, kbD0 },           // : (Shift ;)
      //{ 1, 189, kbA14, kbD3, kbA8, kbD0 },        // -
      { 1, '-', kbA14, kbD3, kbA8, kbD0 },          // -
      //{ 1, VK_SUBTRACT, kbA14, kbD3, kbA8, kbD0 },
      { 0, VK_SUBTRACT, kbA14, kbD3, kbA8, kbD0 },

      //{ 1, 187, kbA14, kbD1, kbA8, kbD0 },        // =
      //{ 2, 187, kbA14, kbD2, kbA8, kbD0 },        // + (Shift =)
      { 1, '=', kbA14, kbD1, kbA8, kbD0 },          // =
      { 2, '=', kbA14, kbD2, kbA8, kbD0 },          // + (Shift =)
      //{ 2, VK_ADD, kbA14, kbD2, kbA8, kbD0 },     // + (Shift numpad+)
      { 0, VK_ADD, kbA14, kbD2, kbA8, kbD0 },       // + (numpad+)

      //{ 1, 188, kbA15, kbD1, kbA8, kbD0 },        // ,
      //{ 2, 188, kbA15, kbD3, kbA8, kbD0 },        // < (Shift ,)
      { 1, ',', kbA15, kbD1, kbA8, kbD0 },          // ,
      { 2, ',', kbA15, kbD3, kbA8, kbD0 },          // < (Shift ,)

      //{ 1, 190 , kbA15, kbD1, 255,255 },          // .
      { 1, '.' , kbA15, kbD1, 255,255 },            // .
      //{ 1, VK_DECIMAL , kbA15, kbD1, 255,255 },   // . (numpad.)
      { 0, VK_DECIMAL , kbA15, kbD1, 255,255 },     // . (numpad.)
      //{ 2, 190 , kbA15, kbD2, kbA8, kbD0 },       // > (Shift .)
      { 2, '.' , kbA15, kbD2, kbA8, kbD0 },         // > (Shift .)
      //{ 1, 191 , kbA8, kbD4, kbA8, kbD0 },        // /
      //{ 2, 191 , kbA8, kbD3, kbA8, kbD0 },        // ? (Shift /)
      //{ 0, 219 , kbA13, kbD2, kbA8, kbD0 },       // [
      //{ 0, 221 , kbA13, kbD1, kbA8, kbD0 },       // ]
      //{ 2, 222 , kbA13, kbD0, kbA8, kbD0 },       // #
      //{ 0, 192 , kbA13, kbD0, kbA8, kbD0 },       // '
      { 1, '/' , kbA8, kbD4, kbA8, kbD0 },          // /
      { 2, '/' , kbA8, kbD3, kbA8, kbD0 },          // ? (Shift /)
      { 0, '[' , kbA13, kbD2, kbA8, kbD0 },         // ( ([ or { these are not in ZX81 character set)
      { 0, ']' , kbA13, kbD1, kbA8, kbD0 },         // ) (] or } these are not in ZX81 character set)
      { 1, 520 , kbA13, kbD0, kbA8, kbD0 },         // " (# or ~ these are not in ZX81 character set)
      { 0, VK_QUOTE , kbA13, kbD0, kbA8, kbD0 },    // " (' or @ these are not in ZX81 character set)

      //{ 2, 188, kbA15, kbD3, kbA8, kbD0 },
      //{ 2, 190, kbA15, kbD2, kbA8, kbD0 },

      { 0, VK_BACK , kbA12, kbD0, kbA8, kbD0 },
      { 0, VK_LEFT , kbA11, kbD4, kbA8, kbD0 },
      { 0, VK_DOWN , kbA12, kbD4, kbA8, kbD0 },
      { 0, VK_UP , kbA12, kbD3, kbA8, kbD0 },
      { 0, VK_RIGHT , kbA12, kbD2, kbA8, kbD0 },

      { 0, VK_CONTROL , kbA14, kbD0, kbA8 , kbD0 },

      {0, 0, 0, 0, 0, 0 }
    };
  
  // TODO: the rest of the keyboards need fixing as for the ZX81 above.
  
  //struct kb KBZX80[]=
  static int[][] KBZX80_ints =  
    {
      { 0, VK_SHIFT , kbA8, kbD0, 255,255 },
      { 0, VK_RETURN , kbA14, kbD0, 255,255 },
      { 0, VK_SPACE , kbA15, kbD0, 255,255 },

      { 0, 'A' , kbA9, kbD0, 255,255 },
      { 0, 'B' , kbA15, kbD4, 255,255 },
      { 0, 'C' , kbA8, kbD3, 255,255 },
      { 0, 'D' , kbA9, kbD2, 255,255 },
      { 0, 'E' , kbA10, kbD2, 255,255 },
      { 0, 'F' , kbA9, kbD3, 255,255 },
      { 0, 'G' , kbA9, kbD4, 255,255 },
      { 0, 'H' , kbA14, kbD4, 255,255 },
      { 0, 'I' , kbA13, kbD2, 255,255 },
      { 0, 'J' , kbA14, kbD3, 255,255 },
      { 0, 'K' , kbA14, kbD2, 255,255 },
      { 0, 'L' , kbA14, kbD1, 255,255 },
      { 0, 'M' , kbA15, kbD2, 255,255 },
      { 0, 'N' , kbA15, kbD3, 255,255 },
      { 0, 'O' , kbA13, kbD1, 255,255 },
      { 0, 'P' , kbA13, kbD0, 255,255 },
      { 0, 'Q' , kbA10, kbD0, 255,255 },
      { 0, 'R' , kbA10, kbD3, 255,255 },
      { 0, 'S' , kbA9, kbD1, 255,255 },
      { 0, 'T' , kbA10, kbD4, 255,255 },
      { 0, 'U' , kbA13, kbD3, 255,255 },
      { 0, 'V' , kbA8, kbD4, 255,255 },
      { 0, 'W' , kbA10, kbD1, 255,255 },
      { 0, 'X' , kbA8, kbD2, 255,255 },
      { 0, 'Y' , kbA13, kbD4, 255,255 },
      { 0, 'Z' , kbA8, kbD1, 255,255 },

      { 0, '1' , kbA11, kbD0, 255,255 },
      { 0, '2' , kbA11, kbD1, 255,255 },
      { 0, '3' , kbA11, kbD2, 255,255 },
      { 0, '4' , kbA11, kbD3, 255,255 },
      { 0, '5' , kbA11, kbD4, 255,255 },
      { 0, '6' , kbA12, kbD4, 255,255 },
      { 0, '7' , kbA12, kbD3, 255,255 },
      { 0, '8' , kbA12, kbD2, 255,255 },
      { 0, '9' , kbA12, kbD1, 255,255 },
      { 0, '0' , kbA12, kbD0, 255,255 },

      { 0, VK_NUMPAD1 , kbA11, kbD0, 255,255 },
      { 0, VK_NUMPAD2 , kbA11, kbD1, 255,255 },
      { 0, VK_NUMPAD3 , kbA11, kbD2, 255,255 },
      { 0, VK_NUMPAD4 , kbA11, kbD3, 255,255 },
      { 0, VK_NUMPAD5 , kbA11, kbD4, 255,255 },
      { 0, VK_NUMPAD6 , kbA12, kbD4, 255,255 },
      { 0, VK_NUMPAD7 , kbA12, kbD3, 255,255 },
      { 0, VK_NUMPAD8 , kbA12, kbD2, 255,255 },
      { 0, VK_NUMPAD9 , kbA12, kbD1, 255,255 },
      { 0, VK_NUMPAD0 , kbA12, kbD0, 255,255 },

      { 0, VK_MULTIPLY , kbA8, kbD4, 255,255 },
      { 0, VK_DIVIDE , kbA15, kbD4, 255,255 },

      { 1, 186, kbA8, kbD2, kbA8, kbD0 },
      { 2, 186, kbA8, kbD1, kbA8, kbD0 },
      { 1, 189, kbA14, kbD3, kbA8, kbD0 },
      { 1, VK_SUBTRACT, kbA14, kbD3, kbA8, kbD0 },

      { 1, 187, kbA14, kbD1, kbA8, kbD0 },
      { 2, 187, kbA14, kbD2, kbA8, kbD0 },
      { 2, VK_ADD, kbA14, kbD2, kbA8, kbD0 },

      { 1, 188, kbA15, kbD1, kbA8, kbD0 },
      { 2, 188, kbA15, kbD3, kbA8, kbD0 },

      { 1, 190 , kbA15, kbD1, 255,255 },
      { 1, VK_DECIMAL , kbA15, kbD1, 255,255 },
      { 2, 190 , kbA15, kbD2, kbA8, kbD0 },
      { 1, 191 , kbA8, kbD4, kbA8, kbD0 },
      { 2, 191 , kbA8, kbD3, kbA8, kbD0 },
      { 0, 219 , kbA13, kbD2, kbA8, kbD0 },
      { 0, 221 , kbA13, kbD1, kbA8, kbD0 },
      { 2, 222 , kbA13, kbD4, kbA8, kbD0 },

      //{ 2, 188, kbA15, kbD3, kbA8, kbD0 },
      //{ 2, 190, kbA15, kbD2, kbA8, kbD0 },

      { 0, VK_BACK , kbA12, kbD0, kbA8, kbD0 },
      { 0, VK_LEFT , kbA11, kbD4, kbA8, kbD0 },
      { 0, VK_DOWN , kbA12, kbD4, kbA8, kbD0 },
      { 0, VK_UP , kbA12, kbD3, kbA8, kbD0 },
      { 0, VK_RIGHT , kbA12, kbD2, kbA8, kbD0 },

      { 0, VK_CONTROL , kbA14, kbD0, kbA8 , kbD0 },

      {0, 0, 0, 0, 0, 0 }
    };
  
  //struct kb KBACE[]=
  static int[][] KBACE_ints =
    {
      { 0, VK_SHIFT , kbA8, kbD0, 255,255 },
      { 0, VK_CONTROL , kbA8, kbD1, 255,255 },

      { 0, VK_RETURN , kbA14, kbD0, 255,255 },
      { 0, VK_SPACE , kbA15, kbD0, 255,255 },

      { 0, 'A' , kbA9, kbD0, 255,255 },
      { 0, 'B' , kbA15, kbD3, 255,255 },
      { 0, 'C' , kbA8, kbD4, 255,255 },
      { 0, 'D' , kbA9, kbD2, 255,255 },
      { 0, 'E' , kbA10, kbD2, 255,255 },
      { 0, 'F' , kbA9, kbD3, 255,255 },
      { 0, 'G' , kbA9, kbD4, 255,255 },
      { 0, 'H' , kbA14, kbD4, 255,255 },
      { 0, 'I' , kbA13, kbD2, 255,255 },
      { 0, 'J' , kbA14, kbD3, 255,255 },
      { 0, 'K' , kbA14, kbD2, 255,255 },
      { 0, 'L' , kbA14, kbD1, 255,255 },
      { 0, 'M' , kbA15, kbD1, 255,255 },
      { 0, 'N' , kbA15, kbD2, 255,255 },
      { 0, 'O' , kbA13, kbD1, 255,255 },
      { 0, 'P' , kbA13, kbD0, 255,255 },
      { 0, 'Q' , kbA10, kbD0, 255,255 },
      { 0, 'R' , kbA10, kbD3, 255,255 },
      { 0, 'S' , kbA9, kbD1, 255,255 },
      { 0, 'T' , kbA10, kbD4, 255,255 },
      { 0, 'U' , kbA13, kbD3, 255,255 },
      { 0, 'V' , kbA15, kbD4, 255,255 },
      { 0, 'W' , kbA10, kbD1, 255,255 },
      { 0, 'X' , kbA8, kbD3, 255,255 },
      { 0, 'Y' , kbA13, kbD4, 255,255 },
      { 0, 'Z' , kbA8, kbD2, 255,255 },

      { 0, '1' , kbA11, kbD0, 255,255 },
      { 0, '2' , kbA11, kbD1, 255,255 },
      { 0, '3' , kbA11, kbD2, 255,255 },
      { 0, '4' , kbA11, kbD3, 255,255 },
      { 0, '5' , kbA11, kbD4, 255,255 },
      { 0, '6' , kbA12, kbD4, 255,255 },
      { 0, '7' , kbA12, kbD3, 255,255 },
      { 0, '8' , kbA12, kbD2, 255,255 },
      { 0, '9' , kbA12, kbD1, 255,255 },
      { 0, '0' , kbA12, kbD0, 255,255 },

      { 1, 186, kbA13, kbD1, kbA8, kbD1 },
      { 2, 186, kbA8, kbD2, kbA8, kbD1 },
      //{ 1, 192, kbA13, kbD1, kbA8, kbD1 },
      { 2, 192, kbA11, kbD1, kbA8, kbD1 },
      { 1, 222, kbA11, kbD2, kbA8, kbD1 },
      { 2, 222, kbA9, kbD0, kbA8, kbD1 },
      { 1, 219, kbA13, kbD4, kbA8, kbD1 },
      { 1, 221, kbA13, kbD3, kbA8, kbD1 },
      { 2, 219, kbA9, kbD3, kbA8, kbD1 },
      { 2, 221, kbA9, kbD4, kbA8, kbD1 },
      { 1, 189, kbA14, kbD3, kbA8, kbD1 },
      { 2, 189, kbA12, kbD0, kbA8, kbD1 },
      { 1, 187, kbA14, kbD1, kbA8, kbD1 },
      { 2, 187, kbA14, kbD2, kbA8, kbD1 },
      { 1, 189, kbA14, kbD3, kbA8, kbD1 },
      { 2, 189, kbA12, kbD0, kbA8, kbD1 },
      { 1, 188, kbA15, kbD2, kbA8, kbD1 },
      { 1, 190, kbA15, kbD1, kbA8, kbD1 },
      { 2, 188, kbA10, kbD3, kbA8, kbD1 },
      { 2, 190, kbA10, kbD4, kbA8, kbD1 },
      { 1, 191, kbA15, kbD4, kbA8, kbD1 },
      { 2, 191, kbA8, kbD4, kbA8, kbD1 },
      { 1, 220, kbA9, kbD2, kbA8, kbD1 },
      { 2, 220, kbA9, kbD1, kbA8, kbD1 },

      { 0, VK_BACK , kbA12, kbD0, kbA8, kbD0 },
      { 0, VK_LEFT , kbA11, kbD4, kbA8, kbD0 },
      { 0, VK_UP , kbA12, kbD4, kbA8, kbD0 },
      { 0, VK_DOWN , kbA12, kbD3, kbA8, kbD0 },
      { 0, VK_RIGHT , kbA12, kbD2, kbA8, kbD0 },
      {0, 0, 0, 0, 0, 0 }
    };
  
  //struct kb KBLAMBDA[]=
  static int[][] KBLAMBDA_ints = 
    {
      { 0, VK_SHIFT , kbA8, kbD0, 255,255 },
      { 0, VK_RETURN , kbA14, kbD0, 255,255 },
      { 0, VK_SPACE , kbA15, kbD0, 255,255 },

      { 0, 'A' , kbA9, kbD0, 255,255 },
      { 0, 'B' , kbA15, kbD4, 255,255 },
      { 0, 'C' , kbA8, kbD3, 255,255 },
      { 0, 'D' , kbA9, kbD2, 255,255 },
      { 0, 'E' , kbA10, kbD2, 255,255 },
      { 0, 'F' , kbA9, kbD3, 255,255 },
      { 0, 'G' , kbA9, kbD4, 255,255 },
      { 0, 'H' , kbA14, kbD4, 255,255 },
      { 0, 'I' , kbA13, kbD2, 255,255 },
      { 0, 'J' , kbA14, kbD3, 255,255 },
      { 0, 'K' , kbA14, kbD2, 255,255 },
      { 0, 'L' , kbA14, kbD1, 255,255 },
      { 0, 'M' , kbA15, kbD2, 255,255 },
      { 0, 'N' , kbA15, kbD3, 255,255 },
      { 0, 'O' , kbA13, kbD1, 255,255 },
      { 0, 'P' , kbA13, kbD0, 255,255 },
      { 0, 'Q' , kbA10, kbD0, 255,255 },
      { 0, 'R' , kbA10, kbD3, 255,255 },
      { 0, 'S' , kbA9, kbD1, 255,255 },
      { 0, 'T' , kbA10, kbD4, 255,255 },
      { 0, 'U' , kbA13, kbD3, 255,255 },
      { 0, 'V' , kbA8, kbD4, 255,255 },
      { 0, 'W' , kbA10, kbD1, 255,255 },
      { 0, 'X' , kbA8, kbD2, 255,255 },
      { 0, 'Y' , kbA13, kbD4, 255,255 },
      { 0, 'Z' , kbA8, kbD1, 255,255 },

      { 0, '1' , kbA11, kbD0, 255,255 },
      { 0, '2' , kbA11, kbD1, 255,255 },
      { 0, '3' , kbA11, kbD2, 255,255 },
      { 0, '4' , kbA11, kbD3, 255,255 },
      { 0, '5' , kbA11, kbD4, 255,255 },
      { 0, '6' , kbA12, kbD4, 255,255 },
      { 0, '7' , kbA12, kbD3, 255,255 },
      { 0, '8' , kbA12, kbD2, 255,255 },
      { 0, '9' , kbA12, kbD1, 255,255 },
      { 0, '0' , kbA12, kbD0, 255,255 },

      { 0, VK_NUMPAD1 , kbA11, kbD0, 255,255 },
      { 0, VK_NUMPAD2 , kbA11, kbD1, 255,255 },
      { 0, VK_NUMPAD3 , kbA11, kbD2, 255,255 },
      { 0, VK_NUMPAD4 , kbA11, kbD3, 255,255 },
      { 0, VK_NUMPAD5 , kbA11, kbD4, 255,255 },
      { 0, VK_NUMPAD6 , kbA12, kbD4, 255,255 },
      { 0, VK_NUMPAD7 , kbA12, kbD3, 255,255 },
      { 0, VK_NUMPAD8 , kbA12, kbD2, 255,255 },
      { 0, VK_NUMPAD9 , kbA12, kbD1, 255,255 },
      { 0, VK_NUMPAD0 , kbA12, kbD0, 255,255 },

      { 0, VK_MULTIPLY , kbA8, kbD4, 255,255 },
      { 0, VK_DIVIDE , kbA15, kbD4, 255,255 },

      { 1, 186, kbA12, kbD4, kbA8, kbD0 },
      //{ 2, 186, kbA8, kbD1, kbA8, kbD0 }, // No : on the lambda
      { 1, 189, kbA14, kbD2, kbA8, kbD0 },
      { 1, VK_SUBTRACT, kbA14, kbD2, kbA8, kbD0 },

      { 1, 187, kbA12, kbD0, kbA8, kbD0 },
      { 2, 187, kbA14, kbD1, kbA8, kbD0 },
      { 2, VK_ADD, kbA14, kbD2, kbA8, kbD0 },

      { 1, 188, kbA12, kbD3, kbA8, kbD0 },
      { 2, 188, kbA13, kbD1, kbA8, kbD0 },

      { 1, 190 , kbA15, kbD1, 255,255 },
      { 1, VK_DECIMAL , kbA15, kbD1, 255,255 },
      { 2, 190 , kbA13, kbD0, kbA8, kbD0 },
      { 1, 191 , kbA14, kbD4, kbA8, kbD0 },

      { 0, 219 , kbA12, kbD2, kbA8, kbD0 },
      { 0, 221 , kbA12, kbD1, kbA8, kbD0 },

      // { 2, 191 , kbA8, kbD3, kbA8, kbD0 }, // Lambda has no ? symbol!
      { 2, 222 , kbA11, kbD4, kbA8, kbD0 },

      //{ 2, 188, kbA15, kbD3, kbA8, kbD0 },
      //{ 2, 190, kbA15, kbD2, kbA8, kbD0 },

      { 0, VK_BACK , kbA15, kbD1, kbA8, kbD0 },
      { 0, VK_LEFT , kbA11, kbD4, kbA8, kbD0 },
      { 0, VK_DOWN , kbA12, kbD4, kbA8, kbD0 },
      { 0, VK_UP , kbA12, kbD3, kbA8, kbD0 },
      { 0, VK_RIGHT , kbA12, kbD2, kbA8, kbD0 },

      { 0, VK_CONTROL , kbA14, kbD0, kbA8 , kbD0 },

      {0, 0, 0, 0, 0, 0 }
    };
  
  //struct kb KBSPEC[]=
  static int[][] KBSPEC_ints = 
    {
      { 0, VK_SHIFT , kbA8, kbD0, 255,255 },
      { 0, VK_RETURN , kbA14, kbD0, 255,255 },
      { 0, VK_SPACE , kbA15, kbD0, 255,255 },

      { 0, 'A' , kbA9, kbD0, 255,255 },
      { 0, 'B' , kbA15, kbD4, 255,255 },
      { 0, 'C' , kbA8, kbD3, 255,255 },
      { 0, 'D' , kbA9, kbD2, 255,255 },
      { 0, 'E' , kbA10, kbD2, 255,255 },
      { 0, 'F' , kbA9, kbD3, 255,255 },
      { 0, 'G' , kbA9, kbD4, 255,255 },
      { 0, 'H' , kbA14, kbD4, 255,255 },
      { 0, 'I' , kbA13, kbD2, 255,255 },
      { 0, 'J' , kbA14, kbD3, 255,255 },
      { 0, 'K' , kbA14, kbD2, 255,255 },
      { 0, 'L' , kbA14, kbD1, 255,255 },
      { 0, 'M' , kbA15, kbD2, 255,255 },
      { 0, 'N' , kbA15, kbD3, 255,255 },
      { 0, 'O' , kbA13, kbD1, 255,255 },
      { 0, 'P' , kbA13, kbD0, 255,255 },
      { 0, 'Q' , kbA10, kbD0, 255,255 },
      { 0, 'R' , kbA10, kbD3, 255,255 },
      { 0, 'S' , kbA9, kbD1, 255,255 },
      { 0, 'T' , kbA10, kbD4, 255,255 },
      { 0, 'U' , kbA13, kbD3, 255,255 },
      { 0, 'V' , kbA8, kbD4, 255,255 },
      { 0, 'W' , kbA10, kbD1, 255,255 },
      { 0, 'X' , kbA8, kbD2, 255,255 },
      { 0, 'Y' , kbA13, kbD4, 255,255 },
      { 0, 'Z' , kbA8, kbD1, 255,255 },

      { 0, '1' , kbA11, kbD0, 255,255 },
      { 0, '2' , kbA11, kbD1, 255,255 },
      { 0, '3' , kbA11, kbD2, 255,255 },
      { 0, '4' , kbA11, kbD3, 255,255 },
      { 0, '5' , kbA11, kbD4, 255,255 },
      { 0, '6' , kbA12, kbD4, 255,255 },
      { 0, '7' , kbA12, kbD3, 255,255 },
      { 0, '8' , kbA12, kbD2, 255,255 },
      { 0, '9' , kbA12, kbD1, 255,255 },
      { 0, '0' , kbA12, kbD0, 255,255 },

      { 0, VK_NUMPAD1 , kbA11, kbD0, 255,255 },
      { 0, VK_NUMPAD2 , kbA11, kbD1, 255,255 },
      { 0, VK_NUMPAD3 , kbA11, kbD2, 255,255 },
      { 0, VK_NUMPAD4 , kbA11, kbD3, 255,255 },
      { 0, VK_NUMPAD5 , kbA11, kbD4, 255,255 },
      { 0, VK_NUMPAD6 , kbA12, kbD4, 255,255 },
      { 0, VK_NUMPAD7 , kbA12, kbD3, 255,255 },
      { 0, VK_NUMPAD8 , kbA12, kbD2, 255,255 },
      { 0, VK_NUMPAD9 , kbA12, kbD1, 255,255 },
      { 0, VK_NUMPAD0 , kbA12, kbD0, 255,255 },

      { 0, VK_MULTIPLY , kbA8, kbD4, 255,255 },
      { 0, VK_DIVIDE , kbA15, kbD4, 255,255 },

      { 1, 186, kbA13, kbD1, kbA15, kbD1 },
      { 2, 186, kbA8, kbD1, kbA15, kbD1 },
      { 1, 189, kbA14, kbD3, kbA15, kbD1 },
      { 2, 189, kbA12, kbD0, kbA15, kbD1 },
      { 1, VK_SUBTRACT, kbA14, kbD3, kbA15, kbD1 },

      { 1, 187, kbA14, kbD1, kbA15, kbD1 },
      { 2, 187, kbA14, kbD2, kbA15, kbD1 },
      { 2, VK_ADD, kbA14, kbD2, kbA15, kbD1 },

      { 1, 188, kbA15, kbD3, kbA15, kbD1 },
      { 2, 188, kbA10, kbD3, kbA15, kbD1 },

      { 1, 190 , kbA15, kbD2, kbA15, kbD1 },
      { 2, 190 , kbA10, kbD4, kbA15, kbD1 },
      { 1, VK_DECIMAL , kbA15, kbD1, 255,255 },
      { 1, 191 , kbA8, kbD4, kbA15, kbD1 },
      { 2, 191 , kbA8, kbD3, kbA15, kbD1 },
      { 1, 192 , kbA12, kbD3, kbA15, kbD1 },
      { 2, 192 , kbA11, kbD1, kbA15, kbD1 },
      { 0, 219 , kbA12, kbD2, kbA15, kbD1 },
      { 0, 221 , kbA12, kbD1, kbA15, kbD1 },
      { 0, 222 , kbA11, kbD2, kbA15, kbD1 },

      //{ 2, 188, kbA15, kbD3, kbA8, kbD0 },
      //{ 2, 190, kbA15, kbD2, kbA8, kbD0 },

      { 0, VK_BACK , kbA12, kbD0, kbA8, kbD0 },
      { 0, VK_LEFT , kbA11, kbD4, kbA8, kbD0 },
      { 0, VK_DOWN , kbA12, kbD4, kbA8, kbD0 },
      { 0, VK_UP , kbA12, kbD3, kbA8, kbD0 },
      { 0, VK_RIGHT , kbA12, kbD2, kbA8, kbD0 },

      { 0, VK_CONTROL , kbA15, kbD1, 255,255 },

      {0, 0, 0, 0, 0, 0 }
    };
  //---------------------------------------------------------------------------
  
  //#pragma package(smart_init)
  
  // For convenience in the code below.
  public static int[] ZXKeyboard = new int[8];

  static kb[] KBZX81 = intTokb(KBZX81_ints);
  static kb[] KBZX80 = intTokb(KBZX80_ints);
  static kb[] KBACE  = intTokb(KBACE_ints);
  static kb[] KBLAMBDA = intTokb(KBLAMBDA_ints);
  static kb[] KBSPEC = intTokb(KBSPEC_ints);
  
  static kb[] intTokb(int[][] ints)
    {
    kb[] kb = new kb[ints.length];
    for( int i = 0; i < ints.length; i++ )
      kb[i] = new kb(ints[i][0],ints[i][1],ints[i][2],ints[i][3],ints[i][4],ints[i][5]);
    return kb;
    }
  
  private ZX81Config mConfig;
  
  //void PCKbInit(void)
  public KBStatus(ZX81Config config)
    {
    mConfig = config;
    
    for(int i=0;i<8;i++) ZXKeyboard[i]=0;

    switch(mConfig.zx81opts.machine)
      {
      case MACHINEZX80:
        KeyMap=KBZX80;
        break;
  
      case MACHINESPEC48:
        KeyMap=KBSPEC;
        break;
  
      case MACHINEACE:
        KeyMap=KBACE;
        break;
  
      case MACHINELAMBDA:
        KeyMap=KBLAMBDA;
        break;
  
      default:
        KeyMap=KBZX81;
        break;
      }
    }
  
  //int PCFindKey(Word key)
  public
  int PCFindKey(int key)
    {
    int i=0;

    //while (KeyMap[i].WinKey)
    while (KeyMap[i].WinKey!=0)
      {
      if (KeyMap[i].WinKey == key) return(i);
      i++;
      }

    return(-1);
    }
  
  //void PCSetKey(WORD dest, int source, int shift)
  public  
  void PCSetKey(int dest, int source, int shift)
    {
    int d;

    d=PCFindKey(dest);

    if (d!=-1)
      {
      KeyMap[d].Addr1 = KeyMap[source].Addr1;
      KeyMap[d].Data1 = KeyMap[source].Data1;

      //if (shift)
      if (shift!=0)
        {
        KeyMap[d].Addr2 = kbA8;
        KeyMap[d].Data2 = kbD0;
        }
      else
        {
        KeyMap[d].Addr2 = 255;
        KeyMap[d].Data2 = 255;
        }
      }
    }
  
  public  
  void PCKeySetCTRL(int key)
    {
    int Kctrl;

    if (mConfig.zx81opts.machine==MACHINESPEC48 || mConfig.zx81opts.machine==MACHINEACE) return;

    //if (!key)
    if (key!=0)
      {
      Kctrl=PCFindKey(VK_RETURN);
      PCSetKey(VK_CONTROL,Kctrl,1);
      }
    else
      {
      Kctrl=PCFindKey(key);
      PCSetKey(VK_CONTROL,Kctrl,0);
      }
    }
  
  public
  void PCKeySetCursor(char left, char down, char up, char right, int shift)
    {
    int Kleft,Kdown, Kright, Kup;
    //char temp;

    Kleft=PCFindKey(left);
    Kdown=PCFindKey(down);
    Kup=PCFindKey(up);
    Kright=PCFindKey(right);

    PCSetKey(VK_LEFT,Kleft,shift);
    PCSetKey(VK_DOWN,Kdown,shift);
    PCSetKey(VK_UP,Kup,shift);
    PCSetKey(VK_RIGHT,Kright,shift);
    }
  
  //void PCKeyDown(WORD key)
  public 
  void PCKeyDown(int key)
    {
    int i=0;
    if (key==VK_SHIFT) PCShift=2;
    // TODO: PCALT=(GetKeyState(VK_MENU)&128);
    PCALT=0;

    //if (PCALT) return;
    //while (KeyMap[i].WinKey)
    if (PCALT!=0) return;
    while (KeyMap[i].WinKey!=0)
      {
      if ((KeyMap[i].WinKey == key) &&
          ((KeyMap[i].Shift==PCShift) || (KeyMap[i].Shift==0)))
        {
        if (key>=186 && key<=222 && mConfig.zx81opts.machine==MACHINESPEC48)
            ZXKeyboard[kbA8] &= ~kbD0;

        ZXKeyboard[KeyMap[i].Addr1] |= KeyMap[i].Data1;
        if (KeyMap[i].Addr2!=255)
            ZXKeyboard[KeyMap[i].Addr2] |= KeyMap[i].Data2;
        return;
        }
      i++;
      }
    }
  
  //void PCKeyUp(WORD key)
  public 
  void PCKeyUp(int key)
    {
    int i=0;

    if (key==VK_SHIFT) PCShift=1;

    //while (KeyMap[i].WinKey)
    while (KeyMap[i].WinKey!=0)
      {
      if (KeyMap[i].WinKey == key) /* &&
              ((KeyMap[i].Shift==PCShift) || (KeyMap[i].Shift==0))) */
        {
        ZXKeyboard[KeyMap[i].Addr1] &= ~KeyMap[i].Data1;
        if (KeyMap[i].Addr2!=255)
                ZXKeyboard[KeyMap[i].Addr2] &= ~KeyMap[i].Data2;

        //if (PCShift==2) ZXKeyboard[kbA8] |= kbD0;
        }
      i++;
      }
    if (PCShift==2) ZXKeyboard[kbA8] |= kbD0;
    }
  
  //void PCAllKeysUp()
  public 
  void PCAllKeysUp()
    {
    int i;
    for(i=0; i<8; i++) ZXKeyboard[i]=0;
    }
  }

//struct kb
//{
//  BYTE Shift;
//  WORD WinKey;
//  BYTE Addr1, Data1, Addr2, Data2;
//};
class kb
  {
  int Shift;
  int WinKey;
  int Addr1, Data1, Addr2, Data2;
  
  kb(int s,int w,int a1,int d1,int a2, int d2)
    { Shift=s; WinKey=w; Addr1=a1; Data1=d1; Addr2=a2; Data2=d2; }
  }
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
 * rompatch.c
 */

package jtyone.zx81;

import jtyone.config.Machine;
import jtyone.io.Tape;
import jtyone.z80.Z80;

/*
#include "z80.h"
#include "zx81.h"
#include "snap.h"
#include "zx81config.h"
#include "WavCInterface.h"
#include "rompatch.h"

extern int TZXByte, TZXEventCounter;
#define TZX_BYTE_EMPTY -1
*/

public class ROMPatch
  {

  //public static int TZXByte = 0;
  //public static int TZXEventCounter = 0;
  
  private static int pop16(Machine machine, Z80 z80)
    {
    int h,l;

    l=machine.memory[z80.SP++];
    h=machine.memory[z80.SP++];
    return((h<<8) | l);
    }

  public static int PatchTest(Machine machine, Z80 z80)
    {
    int b = machine.memory[z80.PC];
    
    if (z80.PC==0x0356 && b == 0x1f )  // ZX81, start loading
      {
      byte[] currentProgram = machine.getTape().getNextEntry();
      if( currentProgram != null )
        {
        // Skip the ZX81 program name.
        // TODO: really ought to compare the ZX81 program name with that being
        // loaded (if any).
        int pos = 0;
        while( (currentProgram[pos++] & 0x80) == 0 );
        for( int i = pos; i < currentProgram.length; i++ )
          machine.memory[0x4009+i-pos] = currentProgram[i]&0xff;                   
        // Note: can't do arraycopy as memory is ints, currentProgram is bytes... 
        
        pop16(machine,z80);
        return 0x0207;    // ZX81, load complete.
        }
      }
    /*
    if (pc==0x0207 && b==0x21)
            WavStop(); // ZX81, Normal Display, Stop Tape
    if (pc==0x0203 && b==0xc9) WavStop(); // Lambda, Normal Display, Stop Tape
    if (pc==0x0203 && b==0xc3) WavStop(); // ZX80, Normal Display, Stop Tape
    if ((pc==0x0356 && b==0x1f) || (pc==0x19b3 && b==0x07))
                    // ZX81, Lambda, Get Byte - Start loading
      {
      WavStart();
      if (TZXByte!=TZX_BYTE_EMPTY)
        {
        B.set(TZXByte);
        pc=pop16();
        TZXByte=TZX_BYTE_EMPTY;
        TZXEventCounter=0;
        }
      }
    */
    
    if (z80.PC==0x0222 && b == 0x3E )  // ZX80, start loading
      {
      byte[] currentProgram = machine.getTape().getNextEntry();
      if( currentProgram != null )
        {
        for( int i = 0; i < currentProgram.length; i++ )
          machine.memory[0x4000+i] = currentProgram[i]&0xff;                   
        // Note: can't do arraycopy as memory is ints, currentProgram is bytes... 
        
        pop16(machine,z80);
        return 0x0203;    // ZX80, load complete.
        }
      }
    
    /*
    if (pc==0x0222 && b==0x3E)
                    // ZX80, Get Byte - Start loading
      {
      WavStart();

      if (TZXByte!=TZX_BYTE_EMPTY)
        {
        ZX81.memory[HL.get()]=TZXByte;
        pc=0x0248;
        TZXByte=TZX_BYTE_EMPTY;
        TZXEventCounter=0;
        }
      }

    if (pc==0x02ff && b==0xcd)            // ZX81, Save Delay, Start Saving
    {
            WavStartRec();
            if (FlashSaveable()) DE.set(0x0001);  // If FlashSaving, remove Save Delay
    }

    if (pc==0x0d0d && b==0x16)            // Lambda, Save Delay, Start Saving
    {
            WavStartRec();
            //if (FlashSaveable()) z80.de.w=0x0001;  // If FlashSaving, remove Save Delay
    }

    if (pc==0x01BA && b==0x3E)            // ZX80, Save Delay, Start Saving
    {
            WavStartRec();
            if (FlashSaveable()) DE.set(0x0001);  // If FlashSaving, remove Save Delay
    }

    if (FlashSaveable() && ((pc==0x031e && b==0x5e)
                             || (pc==0x17Ed && b==0x37)))
            // ZX81, Lambda, Out Byte - Save Byte
    {
                    WavRecordByte(ZX81.memory[HL.get()]);
                    PC.set(pop16());
    }

    if (FlashSaveable() && pc==0x1cb && b==0x11) // ZX81, Lambda, Out Byte - Save Byte
    {
                    WavRecordByte(ZX81.memory[HL.get()]);
                    PC.set(0x01f3);
    }
    */
    return(z80.PC);
    }

  // TODO: Stub methods
  public static final int TZX_BYTE_EMPTY = -1;
  public static void WavStop() { }
  public static void WavStart() { }
  public static void WavStartRec() { }
  public static void WavRecordByte(int b) { }
  public static boolean FlashSaveable() { return false; }
  }
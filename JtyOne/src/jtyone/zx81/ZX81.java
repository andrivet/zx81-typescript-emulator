/* 
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
 * zx81.c
 *
 */

/*
#include <stdlib.h>
#include <fcntl.h>
#include <io.h>

#include "zx81.h"
#include "z80\z80.h"
#include "snap.h"
#include "zx81config.h"
#include "WavCInterface.h"
#include "sound.h"
#include "dev8255.h"
#include "serialport.h"
#include "zxprinter_c.h"
#include "rompatch.h"
#include "tzxman.h"
*/

package jtyone.zx81;

import java.io.IOException;

import jtyone.config.Machine;
import jtyone.config.ZX81Config;
import jtyone.config.ZX81ConfigDefs;
import jtyone.config.ZX81Options;
import jtyone.config.ZX97Options;
import jtyone.display.AccDraw;
import jtyone.display.Scanline;
import jtyone.io.KBStatus;
import jtyone.io.Snap;
import jtyone.io.SoundDefs;
import jtyone.io.Tape;
import jtyone.z80.Register;
import jtyone.z80.Z80;

public final class ZX81
extends Machine
implements ZX81ConfigDefs, SoundDefs // Allow use of constant names directly.
  {

//#define HBLANKCOLOUR (0*16)
//#define VBLANKCOLOUR (0*16)
  //private static final int HBLANKCOLOUR = (0*16);
  private static final int VBLANKCOLOUR = (0*16);

//#define LASTINSTNONE  0
//#define LASTINSTINFE  1
//#define LASTINSTOUTFE 2
//#define LASTINSTOUTFD 3
//#define LASTINSTOUTFF 4
  private static final int LASTINSTNONE  = 0;
  private static final int LASTINSTINFE  = 1;
  private static final int LASTINSTOUTFE = 2;
  private static final int LASTINSTOUTFD = 3;
  private static final int LASTINSTOUTFF = 4;

//extern void DebugUpdate(void);
//extern void add_blank(SCANLINE *line, int borrow, BYTE colour);
//extern int CRC32Block(char *memory, int romlen);
//extern long noise;
//extern int SelectAYReg;
 
  int border=7, ink=0, paper=7;
  //int NMI_generator=0;
  //int HSYNC_generator=0;
  //int rowcounter=0;
  public boolean NMI_generator=false;
  public boolean HSYNC_generator=false;
  public int rowcounter=0;
  int hsync_counter=207;
  public int borrow=0;
  
  int event_next_event;
  int configbyte=0;
  //int setborder=0;
  boolean setborder=false;
  //int zx81_stop=0;
  boolean zx81_stop=false;
  int LastInstruction;
  int MemotechMode=0;
  int HaltCount;

//BYTE memory[1024 * 1024];
//BYTE font[512];
//BYTE memhrg[1024];
//BYTE ZXKeyboard[8];
  private int[] font = new int[1024];
  private int[] memhrg = new int[1024];
 
  int shift_register=0, shift_reg_inv, shift_store=0;

  //int int_pending=0;
  boolean int_pending=false;
  
//extern void font_load(char *filename, void *address, int size);
//extern void ZXPrinterWritePort(unsigned char Data);
//extern unsigned char ZXPrinterReadPort(void);
//extern int shift_register, shift_reg_inv;
//extern long noise;

  // Added here to avoid needing to update original code
  private Z80 z80;
  private ZX81Options zx81opts; 
  private ZX97Options zx97opts;
  private Tape mTape;

//BYTE get_i_reg(void)
//int get_i_reg()
//{
//        return(z80.i);
//}

//void zx81_initialise(void)
  public 
  void initialise(ZX81Config config)
    {
    zx81opts = config.zx81opts; 
    zx97opts = config.zx97opts;
    z80 = new Z80(this);
    Snap snap = new Snap(config);
    mTape = new Tape();
    
    memory = new int[64 * 1024];

    try
      {
      int i, romlen;
      //z80_init();
  
      for(i=0;i<65536;i++) memory[i]=7;
  
      //romlen=memory_load(machine.CurRom, 0, 65536);
      romlen=snap.memory_load(CurRom, 0, 65536);
      zx81opts.romcrc=CRC32Block(memory,romlen);
  
      //if (zx81.extfont) font_load("lmbfnt.rom",font,512);
      //if (zx81.chrgen==CHRGENDK) romlen+=memory_load("dkchr.rom",8192,65536);
      if (zx81opts.extfont) snap.font_load("lmbfnt.rom",font,512);
      if (zx81opts.chrgen==CHRGENDK) romlen+=snap.memory_load("dkchr.rom",8192,65536);
  
      if (zx81opts.shadowROM && romlen<=8192)
      {
              for(i=0;i<8192;i++) memory[i+8192]=memory[i];
              zx81opts.ROMTOP=16383;
      }
      else    zx81opts.ROMTOP=romlen-1;
  
      if (zx81opts.machine==MACHINEZX97LE)
      {
              for(i=0;i<8191;i++) memory[i+0xa000]=memory[i+0x2000];
              for(i=0;i<16384;i++) zx97opts.bankmem[i]=memory[i+0x4000];
              //for(i=8192;i<32768;i++) memory[i]=0x07;
              zx81opts.ROMTOP=8191;
      }
  
      //if (zx81.truehires==HIRESMEMOTECH) memory_load("memohrg.rom", 8192, 2048);
      //if (zx81.truehires==HIRESG007) memory_load("g007hrg.rom",10240,2048);
      if (zx81opts.truehires==HIRESMEMOTECH) snap.memory_load("memohrg.rom", 8192, 2048);
      if (zx81opts.truehires==HIRESG007) snap.memory_load("g007hrg.rom",10240,2048);
  
      if (zx81opts.machine==MACHINELAMBDA) { ink=7; paper=border=0; }
      else { ink=0; paper=border=7; }
  
      //NMI_generator=0;
      //HSYNC_generator=0;
      NMI_generator=false;
      HSYNC_generator=false;
      MemotechMode=0;
      
      //z80_reset();
      //d8255_reset();
      //d8251reset();
      //z80_reset();
      z80.reset();
      }
    catch( IOException exc )
      {
      exc.printStackTrace();
      }
    }

//void zx81_writebyte(int Address, int Data)
  public 
  void writebyte(int Address, int Data)
    {
    //noise = (noise<<8) | Data;
    // TODO: AccDraw.noise = (AccDraw.noise<<8) | Data;

    /* TODO:
    if (zx81.aytype == AY_TYPE_QUICKSILVA)
    {
            if (Address == 0x7fff) SelectAYReg=Data&15;
            if (Address == 0x7ffe) sound_ay_write(SelectAYReg,Data);
    }

    if (zx81.colour==COLOURLAMBDA && Address>=8192 && Address<16384)
    {
            Address = (Address&1023)+8192;
            memory[Address]=Data;
            return;
    }

    if (zx81.machine==MACHINEZX97LE)
    {
            if (zx97.protect08 && Address<0x2000) return;
            if (zx97.protectab && Address>=0xa000 && Address<0xc000) return;

            if (Address>=49152)
            {
                    //if (!(d8255_read(D8255PRTB)&16)) return;
                    if ((d8255_read(D8255PRTB)&16) == 0) return;
                    if (zx97.protectb0 && ((d8255_read(D8255PRTB)&15)==0)) return;
                    if (zx97.protectb115 && ((d8255_read(D8255PRTB)&15)>0)) return;

                    zx97.bankmem[(Address&16383) +  16384*(d8255_read(D8255PRTB)&15)]=Data;                  
                    return;
            }

            if (zx97.bankswitch && Address>0x7fff && Address<=0x9fff) Address -=8000;
            if (zx97.bankswitch && Address<8192) Address += 0x8000;
    }
    */
    if (zx81opts.chrgen==CHRGENQS && Address>=0x8400 && Address<=0x87ff)
    {
            font[Address-0x8400]=Data;
            //zx81.enableqschrgen=1;
            zx81opts.enableqschrgen=true;
    }
    
    if (Address>zx81opts.RAMTOP) Address = (Address&(zx81opts.RAMTOP));

    if (Address<=zx81opts.ROMTOP && zx81opts.protectROM)
    {
            if ((zx81opts.truehires==HIRESMEMOTECH) && (Address<1024))
                            memhrg[Address]=Data;
            return;
    }

    if (Address>8191 && Address<16384 && zx81opts.shadowROM && zx81opts.protectROM) return;
    if (Address<10240 && zx81opts.truehires==HIRESMEMOTECH) return;
    if (Address>=10240 && Address<12288 && zx81opts.truehires==HIRESG007) return;

    memory[Address]=Data;
    }

//BYTE zx81_readbyte(int Address)
  public 
  int readbyte(int Address)
    {
    int data;

    /* TODO:
    if (zx81.colour==COLOURLAMBDA && ((Address>=8192 && Address<16384)
                                      || (Address>=49152 && Address<57344)))
    {
            Address = (Address&1023)+8192;
            data=memory[Address];
            return(data);
    }

    if (zx81.machine==MACHINEZX97LE)
    {
            if (zx97.bankswitch && Address<8192) Address+=0x8000;
            else if (zx97.bankswitch && (Address>=0x8000 && Address<=0x9fff)) Address-=0x8000;

            if (Address>=49152)
            {
                    data=zx97.bankmem[(Address&16383) + (d8255_read(D8255PRTB)&15)*16384];
                    //noise = (noise<<8) | data;
                    AccDraw.noise = (AccDraw.noise<<8) | data;
                    return(data);
            }
    }
    */
    if (Address<=zx81opts.RAMTOP) data=memory[Address];
    else data=memory[(Address&(zx81opts.RAMTOP-16384))+16384];

    //if ((Address<1024 && (zx81.truehires==HIRESMEMOTECH)) && (I&1))
    /* TODO:
    if ((Address<1024 && (zx81.truehires==HIRESMEMOTECH)) && ((z80.i.get()&1)!=0))
                    data=memhrg[Address];

    if ((Address>=0x0c00 && Address<=0x0cff) && (zx81.truehires==HIRESG007))
            data=memory[Address+8192];

    if ((Address<256 || (Address>=512 && Address<768))
            //&& (I&1) && (zx81.truehires==HIRESG007))
              && ((z80.i.get()&1)!=0) && (zx81.truehires==HIRESG007))
                    data=memory[Address+8192];
    */
    //noise = (noise<<8) | data;
    // TODO: AccDraw.noise = (AccDraw.noise<<8) | data;
    return(data);
    }

// BYTE opcode_fetch(int Address)
//
// Given an address, opcode fetch return the byte at that memory address,
// modified depending on certain circumstances.
// It also loads the video shift register and generates video noise.
//
// If Address is less than M1NOT, all code is executed,
// the shift register is cleared and video noise is set to what is on
// the data bus.
//
// If Address >= M1NOT, and bit 6 of the fetched opcode is not set
// a NOP is returned and we load the shift register accordingly,
// depending on which video system is in use (WRX/Memotech/etc.)
//
// The ZX81 has effectively two busses.  The ROM is on the first bus
// while (usually) RAM is on the second.  In video generation, the ROM
// bus is used to get character bitmap data while the second bus
// is used to get the display file.  This is important because depending
// on which bus RAM is placed, it can either be used for extended
// Fonts OR WRX style hi-res graphics, but never both.

//BYTE zx81_opcode_fetch(int Address)
  public
  int opcode_fetch(int Address)
    {
    //int NewAddress, inv;
    //int opcode, bit6, update=0;
    boolean inv, update=false;
    int opcode;
    boolean bit6 = false;
    //BYTE data;
    int data;

    if (Address<zx81opts.m1not)
    {
            // This is not video related, so just return the opcode
            // and make some noise onscreen.
            //data = zx81_readbyte(Address);
            //noise |= data;
            data = readbyte(Address);
            // TODO: AccDraw.noise |= data;
            return(data);
    }

    // We can only execute code below M1NOT.  If an opcode fetch occurs
    // above M1NOT, we actually fetch (address&32767).  This is important
    // because it makes it impossible to place the display file in the
    // 48-64k region if a 64k RAM Pack is used.  How does the real
    // Hardware work?

    //data = zx81_readbyte((Address>=49152)?Address&32767:Address);
    data = readbyte((Address>=49152)?Address&32767:Address);
    opcode=data;
    //bit6=opcode&64;
    bit6=(opcode&64)!=0;

    // Since we got here, we're generating video (ouch!)
    // Bit six of the opcode is important.  If set, the opcode
    // gets executed and nothing appears onscreen.  If unset
    // the Z80 executes a NOP and the code is used to somehow
    // generate the TV picture (exactly how depends on which
    // display method is used)

    if (!bit6) opcode=0;
    //inv = data&128;
    inv = (data&128)!=0;

    // First check for WRX graphics.  This is easy, we just create a
    // 16 bit Address from the IR Register pair and fetch that byte
    // loading it into the video shift register.
    if (zx81opts.truehires==HIRESWRX && z80.I>=zx81opts.maxireg && !bit6)
    {
            //data=zx81_readbyte((z80.i<<8) | (z80.r7 & 128) | ((z80.r-1) & 127));
            //update=1;
            data=readbyte((z80.I<<8) | (z80.R7 & 128) | ((z80.R-1) & 127));
            update=true;
    }
    //else if ((z80.i&1) && MemotechMode)
    /*
    else if (MemotechMode!=0 && (z80.I&1)!=0 )
    {
            // Next Check Memotech Hi-res.  Memotech is only enabled
            // When the I register is odd.

            //extern int RasterY;
            //extern SCANLINE *BuildLine;
 
                //if ((opcode!=118 || BuildLine->scanline_len<66) && RasterY>=56 && RasterY<=(56+192))
            if ((opcode!=118 || AccDraw.BuildLine.scanline_len<66) && AccDraw.RasterY>=56 && AccDraw.RasterY<=(56+192))
            {
                    opcode=0;
                    inv=(MemotechMode==3);
                    //update=1;
                    update=true;
            }
    }
    //else if ((z80.i&1) && (zx81.truehires==HIRESG007))
    else if (zx81.truehires==HIRESG007 && (z80.I&1)!=0 )
    {
            // Like Memotech, G007 is enabled when I is odd.
            // However, it is much simpler, in that it disables
            // the bit 6 detection entirely and relies on the R
            // register to generate an interupt at the right time.

            opcode=0;
            //inv=0;
            //update=1;
            inv = false;
            update=true;
    }
    */
    else if (!bit6)
    {
            // If we get here, we're generating normal Characters
            // (or pseudo Hi-Res), but we still need to figure out
            // where to get the bitmap for the character from

            // First try to figure out which character set we're going
            // to use if CHR$x16 is in use.  Else, standard ZX81
            // character sets are only 64 characters in size.

            //if ((zx81.chrgen==CHRGENCHR16 && (z80.i&1))
            if ((zx81opts.chrgen==CHRGENCHR16 && (z80.I&1)!=0)
                    || (zx81opts.chrgen==CHRGENQS && zx81opts.enableqschrgen))
                    data = ((data&128)>>1)|(data&63);
            else    data = data&63;


            // If I points to ROM, OR I points to the 8-16k region for
            // CHR$x16, we'll fetch the bitmap from there.
            // Lambda and the QS Character board have external memory
            // where the character set is stored, so if one of those
            // is enabled we better fetch it from the dedicated
            // external memory.
            // Otherwise, we can't get a bitmap from anywhere, so
            // display 11111111 (??What does a real ZX81 do?).

            if (z80.I<64 || (z80.I>=128 && z80.I<192 && zx81opts.chrgen==CHRGENCHR16))
            {
                    if (zx81opts.extfont || (zx81opts.chrgen==CHRGENQS && zx81opts.enableqschrgen))
                            data= font[(data<<3) | rowcounter];
                    //else    data=zx81_readbyte(((z80.i&254)<<8) + (data<<3) | rowcounter);
                    else    data=readbyte(((z80.I&254)<<8) + (data<<3) | rowcounter);
            }
            else data=255;

            //update=1;
            update=true;
    }

    if (update)
    {
            // Update gets set to true if we managed to fetch a bitmap from
            // somewhere.  The only time this doesn't happen is if we encountered
            // an opcode with bit 6 set above M1NOT.

            /* TODO:
            if (zx81.colour==COLOURLAMBDA)
            {
                    int c;

                    // If Lambda colour is enabled, we had better fetch
                    // the ink and paper colour from memory too.

                    //c=zx81_readbyte((Address&1023)+8192);
                    c=readbyte((Address&1023)+8192);

                    ink = c&15;
                    paper = (c>>4) & 15;

                    if (setborder)
                    {
                            border=paper;
                            //setborder=0;
                            setborder=false;
                    }
            }
            */

            // Finally load the bitmap we retrieved into the video shift
            // register, remembering to make some video noise too.

            shift_register |= data;
            shift_reg_inv |= inv? 255:0;
            //if (zx81.machine==MACHINELAMBDA) noise |= (Address>>8);
            //else noise |= z80.i;
            // TODO: if (zx81.machine==MACHINELAMBDA) AccDraw.noise |= (Address>>8);
            // TODO: else AccDraw.noise |= z80.i.get();
            return(0);
    }
    else
      {
      // This is the fallthrough for when we found an opcode with
      // bit 6 set in the display file.  We actually execute these
      // opcodes, and generate the noise.

      //noise |= data;
      // TODO: AccDraw.noise |= data;
      return(opcode);
      }
    }
  
//void writeport(int Address, int Data, int *tstates)
  public
  void writeport(int Address, int Data )
    {
    switch(Address&255)
    {
    /* TODO:
    case 0x01:
            configbyte=Data;
            break;
    case 0x0f:
            if (zx81.aytype==AY_TYPE_ZONX)
                    sound_ay_write(SelectAYReg, Data);
            break;

    case 0xdf:
            if (zx81.aytype==AY_TYPE_ACE) sound_ay_write(SelectAYReg, Data);
            if (zx81.aytype==AY_TYPE_ZONX) SelectAYReg=Data&15;
            break;
    case 0x3f:
            if (zx81.aytype==AY_TYPE_FULLER)
                    SelectAYReg=Data&15;
    case 0x5f:
            if (zx81.aytype==AY_TYPE_FULLER)
                    sound_ay_write(SelectAYReg, Data);
            break;

    case 0x73:
            if (zx81.ts2050) d8251writeDATA(Data);
            break;
    case 0x77:
            if (zx81.ts2050) d8251writeCTRL(Data);
            break;

    case 0xc7:
            d8255_write(D8255PRTA,Data);
            break;

    case 0xcf:
            d8255_write(D8255PRTB,Data);
            break;

    case 0xd7:
            d8255_write(D8255PRTC,Data);
            break;

    case 0xdd:
            if (zx81.aytype==AY_TYPE_ACE) SelectAYReg=Data;
            break;

    case 0xfb:
            if (zx81.zxprinter) ZXPrinterWritePort(Data);
            break;
    */
    case 0xfd:
            if (zx81opts.machine==MACHINEZX80) break;
            LastInstruction = LASTINSTOUTFD;
            //NMI_generator=0;
            // Nasty Hack Alert!
            //tstates-=7;
            break;
    case 0xfe:
            if (zx81opts.machine==MACHINEZX80) break;
            LastInstruction = LASTINSTOUTFE;
            //NMI_generator=1;
            break;
    default:
            break;
    }

    //if (!LastInstruction) LastInstruction=LASTINSTOUTFF;
    if (LastInstruction==0) LastInstruction=LASTINSTOUTFF;
    if ((zx81opts.machine != MACHINELAMBDA) && zx81opts.vsyncsound)
            sound_beeper(1);
    //if (!HSYNC_generator) rowcounter=0;
    //if (sync_len) sync_valid=SYNCTYPEV;
    //HSYNC_generator=1;        
    }

  private int beeper = 0;
//BYTE zx81_readport(int Address, int *tstates)
  public
  int readport(int Address)
    {
    //static int beeper;
    //setborder=1;
    setborder=true;
    //if (!(Address&1))
    if ((Address&1) == 0)
    {
            //BYTE keyb, data=0;
            int keyb, data=0;
            int i;
            if ((zx81opts.machine!=MACHINELAMBDA) && zx81opts.vsyncsound)
                    sound_beeper(0);
            if (zx81opts.NTSC) data|=64;
            if (!GetEarState()) data |= 128;

            LastInstruction=LASTINSTINFE;
            //if (!NMI_generator)
            //{
            //        HSYNC_generator=0;
            //        if (sync_len==0) sync_valid=0;
            //}

            keyb=Address/256;
            for(i=0; i<8; i++)
            {
                    //if (! (keyb & (1<<i)) ) data |= ZXKeyboard[i];
                    if ( (keyb & (1<<i)) == 0 ) data |= KBStatus.ZXKeyboard[i];
            }
            //return(~data);
            return((~data)&0xff);
            
    }
    else
            switch(Address&255)
            {
            case 0x01:
            {
                    // TODO: what's this about?!?!
                    //char *config;

                    //config=(char *)(&zx81);
                    //return(config[configbyte]);
                    return(0);
            }

            case 0x5f:
                    if (zx81opts.truehires==HIRESMEMOTECH) MemotechMode=(Address>>8);
                    return(255);

            case 0x73:
                    if (zx81opts.ts2050) return(d8251readDATA());

            case 0x77:
                    if (zx81opts.ts2050) return(d8251readCTRL());

            case 0xdd:
                    if (zx81opts.aytype==AY_TYPE_ACE)
                            return(sound_ay_read(SelectAYReg));

            case 0xf5:
                    beeper = 1-beeper;
                    if ((zx81opts.machine==MACHINELAMBDA) && zx81opts.vsyncsound)
                            sound_beeper(beeper);
                    return(255);
            case 0xfb:
                    if (zx81opts.zxprinter) return(ZXPrinterReadPort());
            default:
                    break;
            }
    return(255);
    }

  public
  int contendmem(int Address, int states, int time)
    {
    return(time);
    }

  public
  int contendio(int Address, int states, int time)
    {
    return(time);
    }

//void ramwobble(int now)
  public
  void ramwobble(boolean now)
    {
    int start, length, data;
    int i;

    start=zx81opts.ROMTOP+1;
    length=zx81opts.RAMTOP-start;
    data=random(256);

    //if (now || !random(64))
    if (now || random(64) == 0)
      for(i=0;i<length;i++) memory[start+i] ^= data;
    }
  /*void ramwobble(void)
    {
    int start, length, data, addr;
    int row, col;
    int type,i;

    start=zx81.ROMTOP+1;
    length=zx81.RAMTOP-start;
    addr=random(length);
    data=random(256);
    if (random(2)) data |= 64;

    row=addr&127;
    col=(addr>>7)&127;


    switch(3)//random(64))
      {
      case 0:
              memory[addr+start]=data;
              break;
  
      case 1:
              for(i=0;i<128;i++) memory[start+(col<<7)+i]=data;
              break;
  
      case 2:
              for(i=0;i<128;i++) memory[start+(i<<7)+row]=data;
              break;
  
      case 3:
              for(i=0;i<length;i++) memory[start+i] ^= data;
  
      default:
              break;
      }
    }
*/

  //private static int DEBUG_TSTOTAL = 0;

//int zx81_do_scanline(SCANLINE *CurScanLine)
  public
  int do_scanline(Scanline CurScanLine)
    {
    //int PrevRev=0, PrevBit=0, PrevGhost=0;
    int tstotal=0;
/*    
    if( AccDraw.scanLineNumber == 9187 )
          System.out.println("DS9187: SL="+CurScanLine.scanline_len+" SYL="+CurScanLine.sync_len+
        " borrow="+borrow);
*/ 
//CurScanLine->scanline_len=0;
    CurScanLine.scanline_len=0;
    
    int MaxScanLen = (zx81opts.single_step? 1:420);
    
    //if (CurScanLine->sync_valid)
    if (CurScanLine.sync_valid!=0)
    {
            //add_blank(CurScanLine, borrow, HSYNC_generator ? (16*paper) : VBLANKCOLOUR );
            //borrow=0;
            //CurScanLine->sync_valid=0;
            //CurScanLine->sync_len=0;
            CurScanLine.add_blank(borrow, HSYNC_generator ? (16*paper) : VBLANKCOLOUR );
            borrow=0;
            CurScanLine.sync_valid=0;
            CurScanLine.sync_len=0;
    }
/*    
    if( AccDraw.scanLineNumber == 9187 )
      System.out.println("DS9187: SL="+CurScanLine.scanline_len+" SYL="+CurScanLine.sync_len+
        " hc="+hsync_counter+" hg="+HSYNC_generator);
*/        
    do
    {
            LastInstruction=LASTINSTNONE;
            z80.PC = ROMPatch.PatchTest(this,z80);
            //ts = z80_do_opcode();
            int ts = z80.do_opcode();
    
            if (int_pending)
            {
                    //ts += z80_interrupt(ts);
                    ts += z80.interrupt(ts);
                    paper=border;
                    //int_pending=0;
                    int_pending=false;
            }
    
            //frametstates += ts;
            // TODO: AccDraw.frametstates += ts;
            WavClockTick(ts, !HSYNC_generator);
            if (zx81opts.zxprinter) ZXPrinterClockTick(ts);
    
            shift_store=shift_register;
            int pixels=ts<<1;
            
/*            
                if( AccDraw.scanLineNumber == 9187 )
      System.out.println("9187: pix="+pixels+" ts="+ts);
*/
            for (int i=0; i<pixels; i++)
            {
                    int colour, bit;
    
                    bit=((shift_register^shift_reg_inv)&32768);
    
                    //if (HSYNC_generator) colour = (bit ? ink:paper)<<4;
                    if (HSYNC_generator) colour = (bit!=0 ? ink:paper)<<4;
                    else colour=VBLANKCOLOUR;
    
                    /* TODO:
                    if (zx81.dirtydisplay)
                    {
                            //if (PrevGhost) colour|=4;
                            if (PrevGhost!=0) colour|=4;
                            PrevGhost=0;
    
                            //if (PrevBit && (PrevRev || zx81.simpleghost))
                            if (PrevBit!=0 && (PrevRev!=0 || zx81.simpleghost))
                                    { colour|=2; PrevGhost=1; }
    
                            //if (noise&1) colour|=1;
                            //noise>>=1;
                            if ((AccDraw.noise&1)!=0) colour|=1;
                            AccDraw.noise>>=1;
                            PrevRev=shift_reg_inv&32768;
                            PrevBit= bit;
                    }
                    */
    
                    //CurScanLine->scanline[CurScanLine->scanline_len++] = colour;
                    CurScanLine.scanline[CurScanLine.scanline_len++] = colour;
    
                    shift_register<<=1;
                    shift_reg_inv<<=1;
            }
    
            switch(LastInstruction)
            {
            case LASTINSTOUTFD:
                    //NMI_generator=0;
                    NMI_generator=false;
                    if (!HSYNC_generator) rowcounter=0;
                    //if (CurScanLine->sync_len) CurScanLine->sync_valid=SYNCTYPEV;
                    if (CurScanLine.sync_len!=0) CurScanLine.sync_valid=SYNCTYPEV;
                    //HSYNC_generator=1;
                    HSYNC_generator=true;
                    break;
            case LASTINSTOUTFE:
                    //NMI_generator=1;
                    NMI_generator=true;
                    if (!HSYNC_generator) rowcounter=0;
                    //if (CurScanLine->sync_len) CurScanLine->sync_valid=SYNCTYPEV;
                    if (CurScanLine.sync_len!=0) CurScanLine.sync_valid=SYNCTYPEV;
                    //HSYNC_generator=1;
                    HSYNC_generator=true;
                    break;
            case LASTINSTINFE:
                    if (!NMI_generator)
                    {
                            //HSYNC_generator=0;
                            HSYNC_generator=false;
                            //if (CurScanLine->sync_len==0) CurScanLine->sync_valid=0;
                            if (CurScanLine.sync_len==0) CurScanLine.sync_valid=0;
                            HaltCount=0;
                    }
                    break;
            case LASTINSTOUTFF:
                    if (!HSYNC_generator) rowcounter=0;
                    //if (CurScanLine->sync_len) CurScanLine->sync_valid=SYNCTYPEV;
                    if (CurScanLine.sync_len!=0) CurScanLine.sync_valid=SYNCTYPEV;
                    //HSYNC_generator=1;
                    HSYNC_generator=true;
                    break;
            default:
                    break;
            }
    
            hsync_counter -= ts;
    
            //if (!(z80.r & 64))
            if( (z80.R & 64) == 0 )
              //int_pending=1;
              int_pending=true;
            //if (!HSYNC_generator) CurScanLine->sync_len += ts;
            if (!HSYNC_generator) CurScanLine.sync_len += ts;
/*  
            if( AccDraw.scanLineNumber == 9187 && CurScanLine.sync_valid == 0 )
//            System.out.println("9187: sv==0: SL="+CurScanLine.scanline_len+" SYL="+CurScanLine.sync_len+
//                " hc="+hsync_counter+" hg="+HSYNC_generator);
System.out.println("9187: sv==0: hc="+hsync_counter+" FC="+(z80.F()&0x01));                            
*/
            if (hsync_counter<=0)
            {
                    if (NMI_generator)
                    {
                            int nmilen;
                            //nmilen = z80_nmi(CurScanLine->scanline_len);
                            nmilen = z80.nmi(CurScanLine.scanline_len);
                            //if (nmilen!=11) add_blank(nmilen-11,60);
                            hsync_counter -= nmilen;
                            ts += nmilen;
                    }
    
                    //borrow=-hsync_counter;
                    //if (HSYNC_generator && CurScanLine->sync_len==0)
                    borrow=-hsync_counter;
                    if (HSYNC_generator && CurScanLine.sync_len==0)
                    {
                            //CurScanLine->sync_len=10;
                            //CurScanLine->sync_valid=SYNCTYPEH;
                            //if (CurScanLine->scanline_len>=(machine.tperscanline*2))
                            //        CurScanLine->scanline_len=machine.tperscanline*2;
                            CurScanLine.sync_len=10;
                            CurScanLine.sync_valid=SYNCTYPEH;
                            if (CurScanLine.scanline_len>=(tperscanline*2))
                              CurScanLine.scanline_len=tperscanline*2;
                            //for(i=0;i<24;i++) CurScanLine->scanline[i]=HBLANKCOLOUR;
                            rowcounter = (++rowcounter)&7;
                    }
                    hsync_counter += tperscanline;
            }
    
            tstotal += ts;
    
            // TODO: DebugUpdate();
    
    //} while(CurScanLine->scanline_len<MaxScanLen && !CurScanLine->sync_valid && !zx81_stop);
    } while(CurScanLine.scanline_len<MaxScanLen && CurScanLine.sync_valid==0 && !zx81_stop);
    
    //if (CurScanLine->sync_valid==SYNCTYPEV)
    if (CurScanLine.sync_valid==SYNCTYPEV)
    {
            hsync_counter=tperscanline;
            //borrow=0;
    }
    
    //if( Z80Macros.DEBUG ) System.out.println("TSTOTAL = "+tstotal);
    //DEBUG_TSTOTAL += tstotal;
    //if( Z80Macros.DEBUG ) System.out.println("Full TSTOTAL = "+DEBUG_TSTOTAL);
    
    //System.out.println("do_scanline, tstotal="+tstotal+",sl="+CurScanLine.scanline_len);
    //try { System.in.read(); } catch(IOException exc) {}
    
    return(tstotal);
    }

  // From Machine
  public void reset() {}
  public void nmi() {}
  public void exit() {}
  public boolean stop() { return zx81_stop; }
  public Tape getTape() { return mTape; }

  //TODO: stub methods/values.
  void sound_ay_write(int a, int b) {}
  int sound_ay_read(int a) {return 0;}
  int d8255_read(int a) {return 0;}
  int D8255PRTA = 0;
  int D8255PRTB = 0;
  int D8255PRTC = 0;
  void d8255_write(int a, int b) {}
  void d8251writeDATA(int b) {}
  void d8251writeCTRL(int b) {}
  int CRC32Block(int[] memory, int romlen) {return 0;}
  int SelectAYReg;
  void DebugUpdate() {}
  void ZXPrinterWritePort(int b) {}
  int ZXPrinterReadPort() {return 0;}
  void sound_beeper(int a) {}
  boolean GetEarState() {return false;}
  int d8251readDATA() {return 0;}
  int d8251readCTRL() {return 0;}
  int random(int a) { return 0;}
  void WavClockTick(int a, boolean b) {}
  void ZXPrinterClockTick(int a) {}
  }
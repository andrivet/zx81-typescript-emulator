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
public class ZX81Options
  {
  //      CFGBYTE emuid, major,minor,testver;
  //      CFGBYTE dirtydisplay;
  //      CFGBYTE machine;
  //      CFGBYTE extfont;
  //      CFGBYTE shadowROM;
  //      CFGBYTE RAM816k;
  //      CFGBYTE protectROM;
  //      CFGBYTE truehires;
  //      CFGBYTE NTSC;
  //      CFGBYTE inverse;
  //      CFGBYTE aysound;
  //      CFGBYTE aytype;
  //      CFGBYTE single_step;
  //      CFGBYTE vsyncsound;
  //      CFGBYTE beepersound;
  //      CFGBYTE ts2050;
  //      CFGBYTE ace96k;
  //      CFGBYTE TZXin, TZXout;
  //      CFGBYTE audioout,audioin;
  //      CFGBYTE colour;
  //      CFGBYTE debug1, debug2;
  //      CFGBYTE autoload;
  //      CFGBYTE wobble;
  //      CFGBYTE chrgen, enableqschrgen;
  //      CFGBYTE bordersize;
  //      CFGBYTE simpleghost;
  //      CFGBYTE maxireg;
  //      CFGBYTE zxprinter;
  public int emuid, major,minor,testver;
  public boolean dirtydisplay;
  public int machine;
  public boolean extfont;
  public boolean shadowROM;
  public int RAM816k;
  public boolean protectROM;
  public int truehires;
  public boolean NTSC;
  public int inverse;
  public int aysound;
  public int aytype;
  public boolean single_step;
  public boolean vsyncsound;
  public int beepersound;
  public boolean ts2050;
  public int ace96k;
  public int TZXin, TZXout;
  public int audioout,audioin;
  public int colour;
  public int debug1, debug2;
  public int autoload;
  public int wobble;
  public int chrgen;
  public boolean enableqschrgen;
  public int bordersize;
  public boolean simpleghost;
  public int maxireg;
  public boolean zxprinter;
  
  public int RAMTOP;
  public int ROMTOP;
  public int m1not;
  //int romtype;
  public int romcrc;
  public int frameskip;
  public int speedup;
  public int UseRShift;
  
  //char ROM80[256];
  //char ROM81[256];
  //char ROMACE[256];
  //char ROMTS1000[256];
  //char ROMTS1500[256];
  //char ROMLAMBDA[256];
  //char ROMPC8300[256];
  //char ROMTK85[256];
  //char ROM97LE[256];
  //char ROMR470[256];
  //char ROMSP48[256];
  //char ROMSP128[256];
  //char ROMSPP2[256];
  //char ROMSPP3[256];
  //char ROMSPP3E[256];
  //char ROMSPP3ECF[256];
  //char ROMTC2048[256];
  //char ROMTS2068[256];
  //char ROMSPSE[256];
  //char ROMDock[256];
  //char ROMZXCF[256];
  //char ROMZX8BIT[256];
  //char ROMZX16BIT[256];
  //char ROMQL[256];
  //char ROMPLUSD[256];
  //char ROMDISCIPLE[256];
  //char ROMOPUSD[256];
  //char ROMBETADISC[256];
  //char ROMUSPEECH[256];
  //char cwd[256];
  //char temppath[256];
  //char inipath[256];
  //char configpath[256];
  //char mydocs[256];
  //char machinename[256];
  public String ROM80;
  public String ROM81;
  public String ROMACE;
  public String ROMTS1000;
  public String ROMTS1500;
  public String ROMLAMBDA;
  public String ROMPC8300;
  public String ROMTK85;
  public String ROM97LE;
  public String ROMR470;
  public String ROMSP48;
  public String ROMSP128;
  public String ROMSPP2;
  public String ROMSPP3;
  public String ROMSPP3E;
  public String ROMSPP3ECF;
  public String ROMTC2048;
  public String ROMTS2068;
  public String ROMSPSE;
  public String ROMDock;
  public String ROMZXCF;
  public String ROMZX8BIT;
  public String ROMZX16BIT;
  public String ROMQL;
  public String ROMPLUSD;
  public String ROMDISCIPLE;
  public String ROMOPUSD;
  public String ROMBETADISC;
  public String ROMUSPEECH;
  public String cwd;
  public String temppath;
  public String inipath;
  public String configpath;
  public String mydocs;
  public String machinename;
  //} ZX81;
  }
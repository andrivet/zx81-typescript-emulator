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

import jtyone.io.Mouse;

/*
#include <string.h>
#include <dir.h>

#include "zx81config.h"

*/

  // From zx81config.h...

public class ZX81Config
implements ZX81ConfigDefs
  {
  //ZX81 zx81;
  //ZX97 zx97;
  //SPECTRUM spectrum;
  //TV tv;
  //MACHINE machine;
  //MOUSE mouse;          
  public ZX81Options zx81opts = new ZX81Options();
  public ZX97Options zx97opts = new ZX97Options();
  public SpectrumOptions spectrum = new SpectrumOptions();
  public TVOptions tv = new TVOptions();
  public Machine machine;
  public Mouse mouse = new Mouse();      
  public boolean autoload;
  
//void load_config(void)
  public 
  void load_config()
    {
    zx81opts.emuid=EMUID;
    zx81opts.major=MAJORVERSION;
    zx81opts.minor=MINORVERSION;
//#ifdef TESTVERSION
//        zx81.testver=1+TESTVERSION-'A';
//#else
    zx81opts.testver=0;
//#endif

    zx81opts.machine=MACHINEZX81;
    zx81opts.dirtydisplay=false;
    //zx81.shadowROM=0;
    zx81opts.shadowROM=false;
    zx81opts.RAM816k=1;
    //zx81.protectROM=1;
    zx81opts.protectROM=true;
    zx81opts.RAMTOP=32767;
    zx81opts.ROMTOP=8191;
    //zx81.NTSC=0;
    zx81opts.NTSC=false;
    //zx81.truehires=HIRESWRX;
    zx81opts.truehires=HIRESDISABLED;
    zx81opts.inverse=0;
    //zx81.extfont=0;
    zx81opts.extfont=false;
    zx81opts.frameskip=0;
    zx81opts.aysound=0;
    zx81opts.aytype=0;
    //zx81.vsyncsound=0;
    zx81opts.vsyncsound=false;
    zx81opts.beepersound=0;
    //zx81.ts2050=0;
    zx81opts.ts2050=false;
    zx81opts.TZXin=0;
    zx81opts.TZXout=0;
    zx81opts.colour=0;
    zx81opts.audioout=0;
    zx81opts.audioin=0;
    zx81opts.romcrc=-1;
    //zx81.romtype=1;
    zx81opts.autoload=0;
    zx81opts.wobble=0;
    zx81opts.chrgen=CHRGENSINCLAIR;
    //zx81.enableqschrgen=0;
    zx81opts.enableqschrgen=false;
    //zx81.simpleghost=1;
    zx81opts.simpleghost=true;
    zx81opts.maxireg=32;
    //zx81.zxprinter=1;
    zx81opts.zxprinter=true;
    machine.clockspeed=3250000;  
    zx81opts.speedup=0;
    zx81opts.UseRShift=0;
    
    machine.tperscanline=207;
    machine.tperframe=312*207;
    machine.intposition=0;

    zx81opts.bordersize=BORDERNORMAL;

    zx81opts.debug1=zx81opts.debug2=0;

    //zx81.single_step=0;
    zx81opts.single_step=false;

    //strcpy(zx81.ROM80, "zx80.rom");
    //strcpy(zx81.ROM81, "zx81.rom");
    //strcpy(zx81.ROMACE, "ace.rom");
    //strcpy(zx81.ROMTS1000, "zx81.rom");
    //strcpy(zx81.ROMTS1500, "ts1500.rom");
    //strcpy(zx81.ROMLAMBDA, "lambda.rom");
    //strcpy(zx81.ROMPC8300, "8300.rom");
    //strcpy(zx81.ROMTK85, "tk85.rom");
    //strcpy(zx81.ROM97LE, "zx97.rom");
    //strcpy(zx81.ROMR470, "ringo470.rom");
    //strcpy(zx81.ROMSP48, "spec48.rom");
    //strcpy(zx81.ROMSP128, "spec128.rom");
    //strcpy(zx81.ROMSPP2, "specp2.rom");
    //strcpy(zx81.ROMSPP3, "specp3.rom");
    //strcpy(zx81.ROMSPP3E, "specp3e.rom");
    //strcpy(zx81.ROMSPP3ECF, "specp3ecf.rom");
    //strcpy(zx81.ROMTC2048, "tc2048.rom");
    //strcpy(zx81.ROMTS2068, "ts2068.rom");
    //strcpy(zx81.ROMSPSE, "specse.rom");
    //strcpy(zx81.ROMZXCF, "zxcflba.rom");
    //strcpy(zx81.ROMZX8BIT, "zx8blbs.rom");
    //strcpy(zx81.ROMZX16BIT, "zxidelbs.rom");
    //strcpy(zx81.ROMQL, "ql_js.rom");
    //strcpy(zx81.ROMPLUSD,"plusd.rom");
    //strcpy(zx81.ROMDISCIPLE,"disciple.rom");
    //strcpy(zx81.ROMOPUSD,"opusd.rom");
    //strcpy(zx81.ROMBETADISC,"trdos.rom");
    //strcpy(zx81.ROMUSPEECH,"uspeech.rom");

    //strcpy(zx81.machinename,"EightyOne");
    
    zx81opts.ROM80 = "zx80.rom";
    zx81opts.ROM81 = "zx81.rom";
    zx81opts.ROMACE = "ace.rom";
    zx81opts.ROMTS1000 = "zx81.rom";
    zx81opts.ROMTS1500 = "ts1500.rom";
    zx81opts.ROMLAMBDA = "lambda.rom";
    zx81opts.ROMPC8300 = "8300.rom";
    zx81opts.ROMTK85 = "tk85.rom";
    zx81opts.ROM97LE = "zx97.rom";
    zx81opts.ROMR470 = "ringo470.rom";
    zx81opts.ROMSP48 = "spec48.rom";
    zx81opts.ROMSP128 = "spec128.rom";
    zx81opts.ROMSPP2 = "specp2.rom";
    zx81opts.ROMSPP3 = "specp3.rom";
    zx81opts.ROMSPP3E = "specp3e.rom";
    zx81opts.ROMSPP3ECF = "specp3ecf.rom";
    zx81opts.ROMTC2048 = "tc2048.rom";
    zx81opts.ROMTS2068 = "ts2068.rom";
    zx81opts.ROMSPSE = "specse.rom";
    zx81opts.ROMZXCF = "zxcflba.rom";
    zx81opts.ROMZX8BIT = "zx8blbs.rom";
    zx81opts.ROMZX16BIT = "zxidelbs.rom";
    zx81opts.ROMQL = "ql_js.rom";
    zx81opts.ROMPLUSD ="plusd.rom";
    zx81opts.ROMDISCIPLE ="disciple.rom";
    zx81opts.ROMOPUSD ="opusd.rom";
    zx81opts.ROMBETADISC ="trdos.rom";
    zx81opts.ROMUSPEECH ="uspeech.rom";

    zx81opts.machinename = "EightyOne";

    //zx97.bankswitch=0;
    //zx97.protect08=1;
    //zx97.protectab=0;
    //zx97.protectb0=1;
    //zx97.protectb115=0;
    zx97opts.bankswitch=false;
    zx97opts.protect08=true;
    zx97opts.protectab=false;
    zx97opts.protectb0=true;
    zx97opts.protectb115=false;
    zx97opts.saveram=0;

    //tv.AdvancedEffects=0;
    //tv.DotCrawl=0;
    //tv.Interlaced=0;
    //tv.DisableAdvanced=0;
    tv.AdvancedEffects=false;
    tv.DotCrawl=false;
    tv.Interlaced=false;
    tv.DisableAdvanced=false;
    tv.frequency=50;

    spectrum.uspeech=0;
    spectrum.kbissue=SPECKBISS3;
    spectrum.driveatype=DRIVE3INCHSS;
    spectrum.drivebtype=DRIVE3INCHSS;
    //spectrum.driveaimg[0]='\0';
    //spectrum.drivebimg[0]='\0';
    spectrum.driveaimg="";
    spectrum.drivebimg="";
    spectrum.drivebusy=0;
    spectrum.kmouse=0;
    spectrum.HDType=HDNONE;
    spectrum.WriteProtectJumper=0;
    spectrum.MFVersion=MFNONE;

    mouse.x=0;
    mouse.y=0;
    mouse.buttons=0;
    mouse.lastx=0;
    mouse.lasty=0;
    
    autoload = true;
    }
  }
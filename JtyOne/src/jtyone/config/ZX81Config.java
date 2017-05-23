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


public class ZX81Config
implements ZX81ConfigDefs
  {
  public ZX81Options zx81opts = new ZX81Options();
  public TVOptions tv = new TVOptions();
  public Machine machine;
  public Mouse mouse = new Mouse();      
  public boolean autoload;
  
  public
  void load_config()
    {
    zx81opts.emuid=EMUID;
    zx81opts.major=MAJORVERSION;
    zx81opts.minor=MINORVERSION;
    zx81opts.testver=0;

    zx81opts.machine=MACHINEZX81;
    zx81opts.dirtydisplay=false;
    zx81opts.shadowROM=false;
    zx81opts.RAM816k=1;
    zx81opts.protectROM=true;
    zx81opts.RAMTOP=32767;
    zx81opts.ROMTOP=8191;
    zx81opts.NTSC=false;
    zx81opts.truehires=HIRESDISABLED;
    zx81opts.inverse=0;
    zx81opts.extfont=false;
    zx81opts.frameskip=0;
    zx81opts.aysound=0;
    zx81opts.aytype=0;
    zx81opts.vsyncsound=false;
    zx81opts.beepersound=0;
    zx81opts.ts2050=false;
    zx81opts.TZXin=0;
    zx81opts.TZXout=0;
    zx81opts.colour=0;
    zx81opts.audioout=0;
    zx81opts.audioin=0;
    zx81opts.romcrc=-1;
    zx81opts.autoload=0;
    zx81opts.wobble=0;
    zx81opts.chrgen=CHRGENSINCLAIR;
    zx81opts.enableqschrgen=false;
    zx81opts.simpleghost=true;
    zx81opts.maxireg=32;
    zx81opts.zxprinter=true;
    machine.clockspeed=3250000;  
    zx81opts.speedup=0;
    zx81opts.UseRShift=0;
    
    machine.tperscanline=207;
    machine.tperframe=312*207;
    machine.intposition=0;

    zx81opts.bordersize=BORDERNORMAL;
    zx81opts.debug1=zx81opts.debug2=0;
    zx81opts.single_step=false;
    zx81opts.ROM81 = "zx81.rom";
    zx81opts.machinename = "ZX81";

    tv.AdvancedEffects=false;
    tv.DotCrawl=false;
    tv.Interlaced=false;
    tv.DisableAdvanced=false;
    tv.frequency=50;

    mouse.x=0;
    mouse.y=0;
    mouse.buttons=0;
    mouse.lastx=0;
    mouse.lasty=0;
    
    autoload = true;
    }
  }
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

package zx81emulator.config;

public class ZX81Options {
    public int emuid, major, minor, testver;
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
    public int audioout, audioin;
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
    public int romcrc;
    public int frameskip;
    public int speedup;
    public int UseRShift;

    public String ROM81;
    public String cwd;
    public String machinename;
}
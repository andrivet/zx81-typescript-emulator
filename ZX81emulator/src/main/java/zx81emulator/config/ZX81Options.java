/* ZX81emulator  - A ZX81 emulator.
 * EightyOne Copyright (C) 2003-2006 Michael D Wynne
 * JtyOne Java translation (C) 2006 Simon Holdsworth and others.
 * ZX81emulator Javascript JSweet transcompilation (C) 2017 Sebastien Andrivet.
 *
 * This file is part of ZX81emulator.
 *
 * ZX81emulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZX81emulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package zx81emulator.config;

public class ZX81Options {
    public int emuid, major, minor, testver;
    public boolean dirtydisplay;
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
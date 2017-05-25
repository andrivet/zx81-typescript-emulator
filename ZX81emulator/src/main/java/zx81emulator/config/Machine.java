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

import zx81emulator.display.Scanline;
import zx81emulator.io.Tape;

import java.io.IOException;

public abstract class Machine {
    public abstract void initialise(ZX81Config config) throws IOException;

    public abstract int do_scanline(Scanline line);

    public abstract void writebyte(int Address, int Data);

    public abstract int readbyte(int Address);

    public abstract int opcode_fetch(int Address);

    public abstract void writeport(int Address, int Data);

    public abstract int readport(int Address);

    public abstract int contendmem(int Address, int states, int time);

    public abstract int contendio(int Address, int states, int time);

    public abstract boolean stop();

    public abstract Tape getTape();

    public int clockspeed;
    public int tperscanline;
    public int tperframe;
    public int intposition;
    public String CurRom;

    public int[] memory;
}
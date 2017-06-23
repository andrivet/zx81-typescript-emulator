/* ZX81emulator  - A ZX81 emulator.
 * EightyOne Copyright (C) 2003-2006 Michael D Wynne
 * JtyOne Java translation (C) 2006 Simon Holdsworth and others.
 * ZX81emulator Typescript/Javascript transcompilation (C) 2017 Sebastien Andrivet.
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
 * along with ZX81emulator.  If not, see <http://www.gnu.org/licenses/>.
 */

namespace zx81emulator.config
{
    import Scanline = zx81emulator.display.Scanline;
    import Tape = zx81emulator.io.Tape;

    export abstract class Machine
    {
        public abstract initialise(config: ZX81Config);
        public abstract do_scanline(line: Scanline): number;
        public abstract writebyte(Address: number, Data: number);
        public abstract readbyte(Address: number): number;
        public abstract opcode_fetch(Address: number): number;
        public abstract writeport(Address: number, Data: number);
        public abstract readport(Address: number): number;
        public abstract contendmem(Address: number, states: number, time: number): number;
        public abstract contendio(Address: number, states: number, time: number): number;
        public abstract stop(): boolean;
        public abstract getTape(): Tape;

        public tperscanline: number;
        public tperframe: number;
        public CurRom: string;
        public memory: number[];

        constructor()
        {
            this.tperscanline = 0;
            this.tperframe = 0;
        }
    }
}


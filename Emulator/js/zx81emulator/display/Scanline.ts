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

namespace zx81emulator.display
{
    export class Scanline
    {
        public sync_len: number;
        public sync_valid: number;
        public scanline_len: number;
        public scanline: number[] = new Array(4000);

        public add_blank(tstates: number, colour: number)
        {
            while ((tstates-- > 0))
            {
                this.scanline[this.scanline_len++] = colour;
                this.scanline[this.scanline_len++] = colour;
            }
            ;
        }

        constructor()
        {
            this.sync_len = 0;
            this.sync_valid = 0;
            this.scanline_len = 0;
        }
    }
}


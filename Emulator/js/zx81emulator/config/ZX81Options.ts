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
    export class ZX81Options
    {
        public protectROM: boolean;
        public chrgen: number;
        public enableqschrgen: boolean;
        public RAMTOP: number;
        public ROMTOP: number;
        public m1not: number;
        public ROM81: string;

        constructor()
        {
            this.protectROM = false;
            this.chrgen = 0;
            this.enableqschrgen = false;
            this.RAMTOP = 0;
            this.ROMTOP = 0;
            this.m1not = 0;
        }
    }
}


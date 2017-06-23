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
    const SYNCTYPEH : number = 1;
    const SYNCTYPEV : number = 2;
    const CHRGENSINCLAIR : number = 0;
    const CHRGENDK : number = 1;
    const CHRGENQS : number = 2;
    const CHRGENCHR16 : number = 3;
    const CHRGENLAMBDA : number = 4;

    export class ZX81Config
    {
        public zx81opts: ZX81Options = new ZX81Options();
        public machine: Machine;
        public autoload: boolean;

        public load_config()
        {
            this.zx81opts.protectROM = true;
            this.zx81opts.RAMTOP = 32767;
            this.zx81opts.ROMTOP = 8191;
            this.zx81opts.chrgen = CHRGENSINCLAIR;
            this.zx81opts.enableqschrgen = false;
            this.zx81opts.m1not = 32768;
            this.machine.tperscanline = 207;
            this.machine.tperframe = 312 * 207;
            this.zx81opts.ROM81 = "zx81.rom";
            this.autoload = true;
        }

        constructor()
        {
            this.autoload = false;
        }
    }
}


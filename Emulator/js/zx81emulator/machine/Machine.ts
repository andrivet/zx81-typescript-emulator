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

import Scanline from "../display/Scanline";

export default abstract class Machine
{
    public readonly tperscanline: number = 207;
    public readonly tperframe: number = 312 * 207;

    public abstract do_scanline(scanLine: Scanline): number;
    public abstract readbyte(address: number): number;
    public abstract writebyte(address: number, data: number);
    public abstract contendmem(address: number, states: number, time: number): number;
    public abstract contendio(address: number, states: number, time: number): number;
    public abstract readport(address: number): number;
    public abstract writeport(address: number, data: number);
    public abstract opcode_fetch(address: number): number;
}

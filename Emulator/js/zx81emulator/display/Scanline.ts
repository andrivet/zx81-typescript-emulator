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


export default class Scanline
{
    private sync_len: number = 0;
    private sync_valid: number = 0;
    private scanline_len: number = 0;
    private scanline: number[] = new Array(4000);

    public add_blank(tstates: number, color: number): void
    {
        while (tstates-- > 0)
        {
            this.scanline[this.scanline_len++] = color;
            this.scanline[this.scanline_len++] = color;
        }

        this.sync_valid = 0;
        this.sync_len = 0;
    }

    public get_length(): number
    {
        return this.scanline_len;
    }

    public reset(length: number = 0): void
    {
        this.scanline_len = length;
    }

    public get_pixel(i: number): number
    {
        return this.scanline[i];
    }

    public add_pixel(color: number): void
    {
        this.scanline[this.scanline_len++] = color;
    }

    public next_line(): number
    {
        this.sync_valid = 1;
        return this.scanline_len + 1;
    }

    public get_sync_length(): number
    {
        return this.sync_len;
    }

    public check_sync_length(min: number): boolean
    {
        if(this.sync_len < min)
            this.sync_valid = 0;
        return this.sync_valid !== 0;
    }

    public get_sync_valid(): number
    {
        return this.sync_valid;
    }

    public set_sync_valid(sync_valid: number): void
    {
        this.sync_valid = sync_valid;
    }

    public reset_sync(length: number, valid: number): void
    {
        this.sync_len = length;
        this.sync_valid = valid;
    }

    public add_sync_length(add: number): void
    {
        this.sync_len += add;
    }
}



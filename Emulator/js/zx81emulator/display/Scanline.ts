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
    private syncLength: number = 0;
    private syncValid: number = 0;
    private scanlineLength: number = 0;
    private scanline: number[] = new Array(4000);

    public addBlank(tstates: number, color: number): void
    {
        while (tstates-- > 0)
        {
            this.scanline[this.scanlineLength++] = color;
            this.scanline[this.scanlineLength++] = color;
        }

        this.syncValid = 0;
        this.syncLength = 0;
    }

    public getLength(): number
    {
        return this.scanlineLength;
    }

    public reset(length: number = 0): void
    {
        this.scanlineLength = length;
    }

    public getPixel(i: number): number
    {
        return this.scanline[i];
    }

    public addPixel(color: number): void
    {
        this.scanline[this.scanlineLength++] = color;
    }

    public nextLine(): number
    {
        this.syncValid = 1;
        return this.scanlineLength + 1;
    }

    public getSyncLength(): number
    {
        return this.syncLength;
    }

    public checkSyncLength(min: number): boolean
    {
        if(this.syncLength < min)
            this.syncValid = 0;
        return this.syncValid !== 0;
    }

    public getSyncValid(): number
    {
        return this.syncValid;
    }

    public setSynValid(sync_valid: number): void
    {
        this.syncValid = sync_valid;
    }

    public resetSync(length: number, valid: number): void
    {
        this.syncLength = length;
        this.syncValid = valid;
    }

    public addSyncLength(add: number): void
    {
        this.syncLength += add;
    }
}



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

import { MasterRegister, Register, RegisterHigh, RegisterLow } from "./Register";

export abstract class RegisterPair
{
    public abstract get(): number;
    public abstract set(rp: RegisterPair | number): void;
    public abstract inc(): void;
    public abstract dec(): void;
    public abstract add(a: number): void;
    public abstract getRH(): Register;
    public abstract getRL(): Register;
}


export class MasterRegisterPair extends RegisterPair
{
    word: number = 0;

    public hi(): number     { return this.word >> 8; }
    public lo(): number     { return this.word & 0xFF; }
    public get(): number    { return this.word; }
    public setHi(h: number) { this.word = ((h & 0xFF) << 8) + (this.word & 0xFF); }
    public setLo(l: number) { this.word = (this.word & 0xff00) + (l & 0xFF); }

    public set(v: number): void;
    public set(r: Register): void;
    public set(p: number | Register): void
    {
        this.word = (p instanceof Register ? p.get() : p) & 0xffff;
    }

    public inc(): void                      { this.word = (this.word + 1) & 0XFFFF; }
    public dec(): void                      { this.word = (this.word - 1) & 0XFFFF; }
    public add(a: number): void             { this.word = (this.word + a) & 0XFFFF; }
    public getRH(): Register                { return new RegisterHigh(this); }
    public getRL(): Register                { return new RegisterLow(this); }
}

export class SlaveRegisterPair extends RegisterPair
{
    private hi: MasterRegister = new MasterRegister();
    private low: MasterRegister = new MasterRegister();

    public get(): number { return (this.hi.get() << 8) + this.low.get(); }

    public set(v: number): void;
    public set(r: Register): void;
    public set(p: number | Register): void
    {
        let word: number = (p instanceof Register ? p.get() : p) & 0XFFFF;
        this.hi.set(word >> 8);
        this.low.set(word & 0xFF);
    }

    public inc()
    {
        this.set((this.hi.get() << 8) + this.low.get() + 1);
    }

    public dec()
    {
        this.set((this.hi.get() << 8) + this.low.get() - 1);
    }

    public add(a: number)
    {
        this.set((this.hi.get() << 8) + this.low.get() + a);
    }

    public getRH(): Register
    {
        return this.hi;
    }

    public getRL(): Register
    {
        return this.low;
    }
}

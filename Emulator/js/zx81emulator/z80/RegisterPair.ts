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

namespace zx81emulator.z80
{
    export interface RegisterPair
    {
        get(): number;
        set(rp: RegisterPair | number): void;
        inc(): void;
        dec(): void;
        add(a: number): void;
        getRH(name: string): Register;
        getRL(name: string): Register;
        toString(): string;
        getName(): string;
    }

    export class MasterRegisterPair implements RegisterPair
    {
        word: number;
        private name: string;

        constructor(name: string)
        {
            this.word = 0;
            this.name = name;
        }

        public hi(): number     { return this.word >> 8; }
        public lo(): number     { return this.word & 0xFF; }
        public get(): number    { return this.word; }
        public setHi(h: number) { this.word = ((h & 0xFF) << 8) + (this.word & 0xFF); }
        public setLo(l: number) { this.word = (this.word & 0xff00) + (l & 0xFF); }

        public set(v: number): void;
        public set(r: Register): void;
        public set(p: number | Register): void
        {
            if(p instanceof Register)
                this.word = p.get();
            else
                this.word = p & 0xffff;
        }

        public inc(): void                      { this.word = (this.word + 1) & 0XFFFF; }
        public dec(): void                      { this.word = (this.word - 1) & 0XFFFF; }
        public add(a: number): void             { this.word = (this.word + a) & 0XFFFF; }
        public getRH(name: string): Register    { return new RegisterHigh(this, name); }
        public getRL(name: string): Register    { return new RegisterLow(this, name); }

        public toString(): string   { return "$" + (this.get() + 0x10000).toString(0x10) .substring(1).toUpperCase(); }
        public getName(): string    { return this.name; }
    }

    export class SlaveRegisterPair implements RegisterPair
    {
        private hi: MasterRegister;
        private low: MasterRegister;
        private name: string;

        constructor(name: string)
        {
            this.name = name;
        }

        public get(): number { return (this.hi.value << 8) + this.low.value; }

        public set(v: number): void;
        public set(r: Register): void;
        public set(p: number | Register): void
        {
            if(p instanceof Register)
            {
                let word: number = p.get();
                this.hi.value = word >> 8;
                this.low.value = word & 0xFF;
            }
            else
            {
                this.hi.value = (p >> 8) & 0xFF;
                this.low.value = p & 0xFF;
            }
        }

        public inc()
        {
            let word: number = ((this.hi.value << 8) + this.low.value + 1) & 0XFFFF;
            this.hi.value = word >> 8;
            this.low.value = word & 0xFF;
        }

        public dec()
        {
            let word: number = ((this.hi.value << 8) + this.low.value - 1) & 0XFFFF;
            this.hi.value = word >> 8;
            this.low.value = word & 0xFF;
        }

        public add(a: number)
        {
            let word: number = ((this.hi.value << 8) + this.low.value + a) & 0XFFFF;
            this.hi.value = word >> 8;
            this.low.value = word & 0xFF;
        }

        public getRH(name: string): Register
        {
            this.hi = new MasterRegister(name);
            return this.hi;
        }

        public getRL(name: string): Register
        {
            this.low = new MasterRegister(name);
            return this.low;
        }

        public toString(): string   { return "$" + (this.get() + 0x10000).toString(0x10) .substring(1).toUpperCase(); }
        public getName(): string    { return this.name; }
    }
}


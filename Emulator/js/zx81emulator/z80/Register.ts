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
    export abstract class Register
    {
        public name: string;

        public abstract get(): number;
        public abstract set(v: number): void;
        public abstract set(r: Register): void;
        public abstract inc(): void;
        public abstract dec(): void;
        public abstract and(a: number): void;
        public abstract or(o: number): void;
        public abstract add(a: number): void;

        public toString(): string { return "$" + (this.get() + 0x100).toString(0x10) .substring(1).toUpperCase(); }
    }

    export class RegisterHigh extends Register
    {
        private rp: MasterRegisterPair;

        constructor(rp: MasterRegisterPair, name: string)
        {
            super();
            this.rp = rp;
            this.name = name;
        }

        public get(): number { return this.rp.hi(); }

        public set(v: number): void;
        public set(r: Register): void;
        public set(p: number | Register): void
        {
            if(p instanceof Register)
                this.rp.setHi(p.get());
            else
                this.rp.setHi(p);
        }

        public inc(): void          { this.rp.setHi(this.rp.hi() + 1); }
        public dec(): void          { this.rp.setHi(this.rp.hi() - 1); }
        public and(a: number): void { this.rp.word &= ((a << 8) | 0xFF); }
        public or(o: number): void  { this.rp.word |= (o << 8); }
        public add(a: number): void { this.rp.setHi(this.rp.hi() + a); }
    }


    export class RegisterLow extends Register
    {
        private rp: MasterRegisterPair;

        constructor(rp: MasterRegisterPair, name: string)
        {
            super();
            this.rp = rp;
            this.name = name;
        }

        public get(): number { return this.rp.lo(); }

        public set(v: number): void;
        public set(r: Register): void;
        public set(p: number | Register): void
        {
            if(p instanceof Register)
                this.rp.setLo(p.get());
            else
                this.rp.setLo(p);
        }

        public inc(): void              { this.rp.setLo(this.rp.lo() + 1); }
        public dec(): void              { this.rp.setLo(this.rp.lo() - 1); }
        public and(a: number): void     { this.rp.word &= (a | 0xff00); }
        public or(o: number): void      { this.rp.word |= o; }
        public add(a: number): void     { this.rp.setLo(this.rp.lo() + a); }
    }


    export class value8 extends Register
    {
        private value: number;

        constructor(name: string)
        {
            super();
            this.value = 0;
            this.value = 0;
            this.name = name;
        }

        public get(): number { return this.value; }

        public set(v: number): void;
        public set(r: Register): void;
        public set(p: number | Register): void
        {
            if(p instanceof Register)
                this.value = p.get();
            else
                this.value = p & 0xFF;
        }

        public inc()            { this.value = (this.value + 1) & 0xFF; }
        public dec()            { this.value = (this.value - 1) & 0xFF; }
        public and(a: number)   { this.value = this.value & a; }
        public or(o: number)    { this.value = this.value | o; }
        public add(a: number)   { this.value = (this.value + a) & 0xFF; }
    }


    export class MasterRegister extends Register
    {
        value: number;

        constructor(name: string)
        {
            super();
            this.value = 0;
            this.value = 0;
            this.name = name;
        }

        public get(): number { return this.value; }

        public set(v: number): void;
        public set(r: Register): void;
        public set(p: number | Register): void
        {
            if(p instanceof Register)
                this.value = p.get();
            else
                this.value = p & 0xFF;
        }

        public inc()            { this.value = (this.value + 1) & 0xFF; }
        public dec()            { this.value = (this.value - 1) & 0xFF; }
        public and(a: number)   { this.value = this.value & a; }
        public or(o: number)    { this.value = this.value | o; }
        public add(a: number)   { this.value = (this.value + a) & 0xFF; }
    }
}


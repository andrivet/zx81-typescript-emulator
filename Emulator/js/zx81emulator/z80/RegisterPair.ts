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

namespace zx81emulator.z80 {
    export interface RegisterPair {
        get() : number;

        set(rp? : any) : any;

        inc();

        dec();

        add(a : number);

        getRH(name : string) : Register;

        getRL(name : string) : Register;

        toString() : string;

        getName() : string;
    }

    export class MasterRegisterPair implements RegisterPair {
        word : number;

        private name : string;

        constructor(name : string) {
            this.word = 0;
            this.name = name;
        }

        public hi() : number {
            return this.word >> 8;
        }

        public lo() : number {
            return this.word & 255;
        }

        public get() : number {
            return this.word;
        }

        public setHi(h : number) {
            this.word = ((h & 255) << 8) + (this.word & 255);
        }

        public setLo(l : number) {
            this.word = (this.word & 65280) + (l & 255);
        }

        public set$int(w : number) {
            this.word = w & 65535;
        }

        public set(rp? : any) : any {
            if(((rp != null && (rp["__interfaces"] != null && rp["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp.constructor != null && rp.constructor["__interfaces"] != null && rp.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    this.word = rp.get();
                })();
            } else if(((typeof rp === 'number') || rp === null)) {
                return <any>this.set$int(rp);
            } else throw new Error('invalid overload');
        }

        public inc() {
            this.word = (this.word + 1) & 65535;
        }

        public dec() {
            this.word = (this.word - 1) & 65535;
        }

        public add(a : number) {
            this.word = (this.word + a) & 65535;
        }

        public getRH(name : string) : Register {
            return new RegisterHigh(this, name);
        }

        public getRL(name : string) : Register {
            return new RegisterLow(this, name);
        }

        public toString() : string {
            return "$" + javaemul.internal.IntegerHelper.toHexString(this.get() + 65536).substring(1).toUpperCase();
        }

        public getName() : string {
            return this.name;
        }
    }
    MasterRegisterPair["__class"] = "zx81emulator.z80.MasterRegisterPair";
    MasterRegisterPair["__interfaces"] = ["zx81emulator.z80.RegisterPair"];



    export class SlaveRegisterPair implements RegisterPair {
        private hi : MasterRegister;

        private low : MasterRegister;

        private name : string;

        constructor(name : string) {
            this.name = name;
        }

        public get() : number {
            return (this.hi.value << 8) + this.low.value;
        }

        public set$int(w : number) {
            this.hi.value = (w >> 8) & 255;
            this.low.value = w & 255;
        }

        public set(rp? : any) : any {
            if(((rp != null && (rp["__interfaces"] != null && rp["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp.constructor != null && rp.constructor["__interfaces"] != null && rp.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    let word : number = rp.get();
                    this.hi.value = word >> 8;
                    this.low.value = word & 255;
                })();
            } else if(((typeof rp === 'number') || rp === null)) {
                return <any>this.set$int(rp);
            } else throw new Error('invalid overload');
        }

        public inc() {
            let word : number = ((this.hi.value << 8) + this.low.value + 1) & 65535;
            this.hi.value = word >> 8;
            this.low.value = word & 255;
        }

        public dec() {
            let word : number = ((this.hi.value << 8) + this.low.value - 1) & 65535;
            this.hi.value = word >> 8;
            this.low.value = word & 255;
        }

        public add(a : number) {
            let word : number = ((this.hi.value << 8) + this.low.value + a) & 65535;
            this.hi.value = word >> 8;
            this.low.value = word & 255;
        }

        public getRH(name : string) : Register {
            this.hi = new MasterRegister(name);
            return this.hi;
        }

        public getRL(name : string) : Register {
            this.low = new MasterRegister(name);
            return this.low;
        }

        public toString() : string {
            return "$" + javaemul.internal.IntegerHelper.toHexString(this.get() + 65536).substring(1).toUpperCase();
        }

        public getName() : string {
            return this.name;
        }
    }
    SlaveRegisterPair["__class"] = "zx81emulator.z80.SlaveRegisterPair";
    SlaveRegisterPair["__interfaces"] = ["zx81emulator.z80.RegisterPair"];


}


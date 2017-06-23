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
    import Machine = zx81emulator.config.Machine;

    export class Z80 {
        static FLAG_C : number = 1;

        static FLAG_N : number = 2;

        static FLAG_P : number = 4;

        static FLAG_V : number; public static FLAG_V_$LI$() : number { if(Z80.FLAG_V == null) Z80.FLAG_V = Z80.FLAG_P; return Z80.FLAG_V; };

        static FLAG_3 : number = 8;

        static FLAG_H : number = 16;

        static FLAG_5 : number = 32;

        static FLAG_Z : number = 64;

        static FLAG_S : number = 128;

        static halfcarry_add_table : number[]; public static halfcarry_add_table_$LI$() : number[] { if(Z80.halfcarry_add_table == null) Z80.halfcarry_add_table = [0, Z80.FLAG_H, Z80.FLAG_H, Z80.FLAG_H, 0, 0, 0, Z80.FLAG_H]; return Z80.halfcarry_add_table; };

        static halfcarry_sub_table : number[]; public static halfcarry_sub_table_$LI$() : number[] { if(Z80.halfcarry_sub_table == null) Z80.halfcarry_sub_table = [0, 0, Z80.FLAG_H, 0, Z80.FLAG_H, 0, Z80.FLAG_H, Z80.FLAG_H]; return Z80.halfcarry_sub_table; };

        static overflow_add_table : number[]; public static overflow_add_table_$LI$() : number[] { if(Z80.overflow_add_table == null) Z80.overflow_add_table = [0, 0, 0, Z80.FLAG_V_$LI$(), Z80.FLAG_V_$LI$(), 0, 0, 0]; return Z80.overflow_add_table; };

        static overflow_sub_table : number[]; public static overflow_sub_table_$LI$() : number[] { if(Z80.overflow_sub_table == null) Z80.overflow_sub_table = [0, Z80.FLAG_V_$LI$(), 0, 0, 0, 0, Z80.FLAG_V_$LI$(), 0]; return Z80.overflow_sub_table; };

        static sz53_table : number[]; public static sz53_table_$LI$() : number[] { if(Z80.sz53_table == null) Z80.sz53_table = new Array(256); return Z80.sz53_table; };

        static parity_table : number[]; public static parity_table_$LI$() : number[] { if(Z80.parity_table == null) Z80.parity_table = new Array(256); return Z80.parity_table; };

        static sz53p_table : number[]; public static sz53p_table_$LI$() : number[] { if(Z80.sz53p_table == null) Z80.sz53p_table = new Array(256); return Z80.sz53p_table; };

        private machine : Machine;

        public BC : MasterRegisterPair;

        public DE : MasterRegisterPair;

        public HL : MasterRegisterPair;

        public AF : RegisterPair;

        public A : MasterRegister;

        public F : MasterRegister;

        public B : Register;

        public C : Register;

        public D : Register;

        public E : Register;

        public H : Register;

        public L : Register;

        public IX : RegisterPair;

        public IY : RegisterPair;

        public IXH : Register;

        public IXL : Register;

        public IYH : Register;

        public IYL : Register;

        public SP : number;

        public PC : number;

        public AF_ : number;

        public BC_ : number;

        public DE_ : number;

        public HL_ : number;

        public I : number;

        public R : number;

        public R7 : number;

        public IFF1 : number;

        public IFF2 : number;

        public IM : number;

        public halted : number;

        private tempreg : value8 = new value8("value8");

        public constructor(machine : Machine) {
            this.SP = 0;
            this.PC = 0;
            this.AF_ = 0;
            this.BC_ = 0;
            this.DE_ = 0;
            this.HL_ = 0;
            this.I = 0;
            this.R = 0;
            this.R7 = 0;
            this.IFF1 = 0;
            this.IFF2 = 0;
            this.IM = 0;
            this.halted = 0;
            this.tstates = 0;
            this.AF = new SlaveRegisterPair("AF");
            this.BC = new MasterRegisterPair("BC");
            this.DE = new MasterRegisterPair("DE");
            this.HL = new MasterRegisterPair("HL");
            this.IX = new MasterRegisterPair("IX");
            this.IY = new MasterRegisterPair("IY");
            this.A = <MasterRegister>this.AF.getRH("A");
            this.F = <MasterRegister>this.AF.getRL("F");
            this.B = this.BC.getRH("B");
            this.C = this.BC.getRL("C");
            this.D = this.DE.getRH("D");
            this.E = this.DE.getRL("E");
            this.H = this.HL.getRH("H");
            this.L = this.HL.getRL("L");
            this.IXH = this.IX.getRH("IXH");
            this.IXL = this.IX.getRL("IXL");
            this.IYH = this.IY.getRH("IYH");
            this.IYL = this.IY.getRL("IYL");
            this.machine = machine;
            this.init_tables();
        }

        private init_tables() {
            let i : number;
            let j : number;
            let k : number;
            let parity : number;
            for(i = 0; i < 256; i++) {
                Z80.sz53_table_$LI$()[i] = i & (Z80.FLAG_3 | Z80.FLAG_5 | Z80.FLAG_S);
                j = i;
                parity = 0;
                for(k = 0; k < 8; k++) {
                    parity ^= j & 1;
                    j >>= 1;
                }
                Z80.parity_table_$LI$()[i] = (parity !== 0?0:Z80.FLAG_P);
                Z80.sz53p_table_$LI$()[i] = Z80.sz53_table_$LI$()[i] | Z80.parity_table_$LI$()[i];
            }
            Z80.sz53_table_$LI$()[0] |= Z80.FLAG_Z;
            Z80.sz53p_table_$LI$()[0] |= Z80.FLAG_Z;
        }

        public reset() {
            this.AF.set(0);
            this.BC.set(0);
            this.DE.set(0);
            this.HL.set(0);
            this.IX.set(0);
            this.IY.set(0);
            this.SP = this.PC = 0;
            this.AF_ = this.BC_ = this.DE_ = this.HL_ = 0;
            this.I = this.R = this.R7 = 0;
            this.IFF1 = this.IFF2 = this.IM = 0;
            this.halted = 0;
        }

        public interrupt(ts : number) : number {
            if(this.IFF1 !== 0) {
                if(this.halted !== 0) {
                    this.PC++;
                    this.halted = 0;
                }
                this.IFF1 = this.IFF2 = 0;
                this.machine.writebyte(--this.SP, this.PC >> 8);
                this.machine.writebyte(--this.SP, this.PC & 255);
                this.R = (this.R + 1) & 255;
                switch((this.IM)) {
                    case 0:
                        this.PC = 56;
                        return (13);
                    case 1:
                        this.PC = 56;
                        return (13);
                    case 2:
                    {
                        let inttemp : number = (this.I << 8) | 255;
                        this.PC = this.machine.readbyte(inttemp++) + (this.machine.readbyte(inttemp) << 8);
                        return (19);
                    };
                    default:
                        return (12);
                }
            }
            return (0);
        }

        public nmi(ts : number) : number {
            let waitstates : number = 0;
            this.IFF1 = 0;
            if(this.halted !== 0) {
                this.halted = 0;
                this.PC++;
                waitstates = ((ts / 2|0)) - this.machine.tperscanline;
                waitstates = 4 - waitstates;
                if(waitstates < 0) waitstates = 0;
            }
            this.machine.writebyte(--this.SP, this.PC >> 8);
            this.machine.writebyte(--this.SP, this.PC & 255);
            this.R = (this.R + 1) & 255;
            this.PC = 102;
            return (4 + waitstates);
        }

        private tstates : number;

        private contend$int$int(address : number, time : number) {
            this.tstates += this.machine.contendmem(address, this.tstates, time);
        }

        public contend(rp? : any, time? : any) : any {
            if(((rp != null && (rp["__interfaces"] != null && rp["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp.constructor != null && rp.constructor["__interfaces"] != null && rp.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp === null) && ((typeof time === 'number') || time === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    this.tstates += this.machine.contendmem(rp.get(), this.tstates, time);
                })();
            } else if(((typeof rp === 'number') || rp === null) && ((typeof time === 'number') || time === null)) {
                return <any>this.contend$int$int(rp, time);
            } else throw new Error('invalid overload');
        }

        private contend_io$int$int(port : number, time : number) {
            this.tstates += this.machine.contendio(port, this.tstates, time);
        }

        public contend_io(rp? : any, time? : any) : any {
            if(((rp != null && (rp["__interfaces"] != null && rp["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp.constructor != null && rp.constructor["__interfaces"] != null && rp.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp === null) && ((typeof time === 'number') || time === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    this.tstates += this.machine.contendio(rp.get(), this.tstates, time);
                })();
            } else if(((typeof rp === 'number') || rp === null) && ((typeof time === 'number') || time === null)) {
                return <any>this.contend_io$int$int(rp, time);
            } else throw new Error('invalid overload');
        }

        public AND(r? : any) : any {
            if(((r != null && r instanceof zx81emulator.z80.Register) || r === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    this.A.value &= r.get();
                    this.F.set(Z80.FLAG_H | Z80.sz53p_table_$LI$()[this.A.value]);
                })();
            } else if(((typeof r === 'number') || r === null)) {
                return <any>this.AND$int(r);
            } else throw new Error('invalid overload');
        }

        private AND$int(value : number) {
            this.A.value &= value;
            this.F.set(Z80.FLAG_H | Z80.sz53p_table_$LI$()[this.A.value]);
        }

        private ADC(value : number) {
            let adctemp : number = this.A.value + value + (this.F.value & Z80.FLAG_C);
            let lookup : number = ((this.A.value & 136) >> 3) | (((value) & 136) >> 2) | ((adctemp & 136) >> 1);
            this.A.set(adctemp);
            this.F.set(((adctemp & 256) > 0?Z80.FLAG_C:0) | Z80.halfcarry_add_table_$LI$()[lookup & 7] | Z80.overflow_add_table_$LI$()[lookup >> 4] | Z80.sz53_table_$LI$()[this.A.value]);
        }

        private ADC16(value : number) {
            let add16temp : number = this.HL.word + value + (this.F.value & Z80.FLAG_C);
            let lookup : number = ((this.HL.word & 34816) >> 11) | ((value & 34816) >> 10) | ((add16temp & 34816) >> 9);
            this.HL.set(add16temp);
            this.F.set(((add16temp & 65536) > 0?Z80.FLAG_C:0) | Z80.overflow_add_table_$LI$()[lookup >> 4] | (this.H.get() & (Z80.FLAG_3 | Z80.FLAG_5 | Z80.FLAG_S)) | Z80.halfcarry_add_table_$LI$()[lookup & 7] | (this.HL.word === 0?0:Z80.FLAG_Z));
        }

        private ADD(value : number) {
            let addtemp : number = this.A.value + value;
            let lookup : number = ((this.A.value & 136) >> 3) | (((value) & 136) >> 2) | ((addtemp & 136) >> 1);
            this.A.set(addtemp);
            this.F.set(((addtemp & 256) > 0?Z80.FLAG_C:0) | Z80.halfcarry_add_table_$LI$()[lookup & 7] | Z80.overflow_add_table_$LI$()[lookup >> 4] | Z80.sz53_table_$LI$()[this.A.value]);
        }

        public ADD16(rp1? : any, rp2? : any) : any {
            if(((rp1 != null && (rp1["__interfaces"] != null && rp1["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp1.constructor != null && rp1.constructor["__interfaces"] != null && rp1.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp1 === null) && ((rp2 != null && (rp2["__interfaces"] != null && rp2["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp2.constructor != null && rp2.constructor["__interfaces"] != null && rp2.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp2 === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    let add16temp : number = rp1.get() + rp2.get();
                    let lookup : number = ((rp1.get() & 2048) >> 11) | ((rp2.get() & 2048) >> 10) | ((add16temp & 2048) >> 9);
                    this.tstates += 7;
                    rp1.set(add16temp);
                    this.F.set((this.F.value & (Z80.FLAG_V_$LI$() | Z80.FLAG_Z | Z80.FLAG_S)) | ((add16temp & 65536) > 0?Z80.FLAG_C:0) | ((add16temp >> 8) & (Z80.FLAG_3 | Z80.FLAG_5)) | Z80.halfcarry_add_table_$LI$()[lookup]);
                })();
            } else if(((rp1 != null && (rp1["__interfaces"] != null && rp1["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp1.constructor != null && rp1.constructor["__interfaces"] != null && rp1.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp1 === null) && ((typeof rp2 === 'number') || rp2 === null)) {
                return <any>this.ADD16$zx81emulator_z80_RegisterPair$int(rp1, rp2);
            } else throw new Error('invalid overload');
        }

        private ADD16$zx81emulator_z80_RegisterPair$int(rp1 : RegisterPair, value : number) {
            let add16temp : number = rp1.get() + value;
            let lookup : number = ((rp1.get() & 2048) >> 11) | ((value & 2048) >> 10) | ((add16temp & 2048) >> 9);
            this.tstates += 7;
            rp1.set(add16temp);
            this.F.set((this.F.value & (Z80.FLAG_V_$LI$() | Z80.FLAG_Z | Z80.FLAG_S)) | ((add16temp & 65536) > 0?Z80.FLAG_C:0) | ((add16temp >> 8) & (Z80.FLAG_3 | Z80.FLAG_5)) | Z80.halfcarry_add_table_$LI$()[lookup]);
        }

        private BIT(bit : number, value : Register) {
            this.F.set((this.F.value & Z80.FLAG_C) | (value.get() & (Z80.FLAG_3 | Z80.FLAG_5)) | ((value.get() & (1 << bit)) > 0?Z80.FLAG_H:(Z80.FLAG_P | Z80.FLAG_H | Z80.FLAG_Z)));
        }

        private BIT7(value : Register) {
            this.F.set((this.F.value & Z80.FLAG_C) | (value.get() & (Z80.FLAG_3 | Z80.FLAG_5)) | ((value.get() & 128) > 0?(Z80.FLAG_H | Z80.FLAG_S):(Z80.FLAG_P | Z80.FLAG_H | Z80.FLAG_Z)));
        }

        private CALL() {
            let calltempl : number = this.machine.readbyte(this.PC++);
            this.contend(this.PC, 1);
            let calltemph : number = this.machine.readbyte(this.PC++);
            this.PUSH16(this.PC);
            this.PC = calltempl + (calltemph << 8);
        }

        private CP(value : number) {
            let cptemp : number = this.A.value - value;
            let lookup : number = ((this.A.value & 136) >> 3) | (((value) & 136) >> 2) | ((cptemp & 136) >> 1);
            this.F.set(((cptemp & 256) > 0?Z80.FLAG_C:(cptemp > 0?0:Z80.FLAG_Z)) | Z80.FLAG_N | Z80.halfcarry_sub_table_$LI$()[lookup & 7] | Z80.overflow_sub_table_$LI$()[lookup >> 4] | (value & (Z80.FLAG_3 | Z80.FLAG_5)) | (cptemp & Z80.FLAG_S));
        }

        private DEC(reg : Register) {
            this.F.set((this.F.value & Z80.FLAG_C) | ((reg.get() & 15) > 0?0:Z80.FLAG_H) | Z80.FLAG_N);
            reg.dec();
            this.F.or((reg.get() === 127?Z80.FLAG_V_$LI$():0) | Z80.sz53_table_$LI$()[reg.get()]);
        }

        public IN(reg? : any, rp? : any) : any {
            if(((reg != null && reg instanceof zx81emulator.z80.Register) || reg === null) && ((rp != null && (rp["__interfaces"] != null && rp["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp.constructor != null && rp.constructor["__interfaces"] != null && rp.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    let port : number = rp.get();
                    this.contend_io(port, 3);
                    reg.set(this.machine.readport(port));
                    this.F.set((this.F.value & Z80.FLAG_C) | Z80.sz53p_table_$LI$()[reg.get()]);
                })();
            } else if(((reg != null && reg instanceof zx81emulator.z80.Register) || reg === null) && ((typeof rp === 'number') || rp === null)) {
                return <any>this.IN$zx81emulator_z80_Register$int(reg, rp);
            } else if(((typeof reg === 'number') || reg === null) && ((rp != null && (rp["__interfaces"] != null && rp["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp.constructor != null && rp.constructor["__interfaces"] != null && rp.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp === null)) {
                return <any>this.IN$int$zx81emulator_z80_RegisterPair(reg, rp);
            } else throw new Error('invalid overload');
        }

        private IN$zx81emulator_z80_Register$int(reg : Register, port : number) {
            this.contend_io(port, 3);
            reg.set(this.machine.readport(port));
            this.F.set((this.F.value & Z80.FLAG_C) | Z80.sz53p_table_$LI$()[reg.get()]);
        }

        private IN$int$zx81emulator_z80_RegisterPair(value : number, rp : RegisterPair) {
            this.contend_io(rp, 3);
            this.machine.readport(rp.get());
            this.F.set((this.F.value & Z80.FLAG_C) | Z80.sz53p_table_$LI$()[value]);
        }

        private INC(reg : Register) {
            reg.inc();
            this.F.set((this.F.value & Z80.FLAG_C) | (reg.get() === 128?Z80.FLAG_V_$LI$():0) | ((reg.get() & 15) > 0?0:Z80.FLAG_H) | Z80.sz53_table_$LI$()[reg.get()]);
        }

        private LD16_NNRR(value : number) {
            this.contend(this.PC, 3);
            let ldtemp : number = this.machine.readbyte(this.PC++);
            this.contend(this.PC, 3);
            ldtemp |= this.machine.readbyte(this.PC++) << 8;
            this.contend(ldtemp, 3);
            this.machine.writebyte(ldtemp++, value & 255);
            this.contend(ldtemp, 3);
            this.machine.writebyte(ldtemp, value >> 8);
        }

        private LD16_RRNN() : number {
            this.contend(this.PC, 3);
            let ldtemp : number = this.machine.readbyte(this.PC++);
            this.contend(this.PC, 3);
            ldtemp |= this.machine.readbyte(this.PC++) << 8;
            this.contend(ldtemp, 3);
            this.contend(ldtemp, 3);
            return this.machine.readbyte(ldtemp++) + (this.machine.readbyte(ldtemp) << 8);
        }

        private JP() {
            let jptemp : number = this.PC;
            this.PC = this.machine.readbyte(jptemp++) + (this.machine.readbyte(jptemp) << 8);
        }

        private JR() {
            this.contend(this.PC, 1);
            this.contend(this.PC, 1);
            this.contend(this.PC, 1);
            this.contend(this.PC, 1);
            this.contend(this.PC, 1);
            let dist : number = this.machine.readbyte(this.PC);
            dist = (dist < 128?dist:dist - 256);
            this.PC += dist;
        }

        public OR(r? : any) : any {
            if(((r != null && r instanceof zx81emulator.z80.Register) || r === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    this.A.value |= r.get();
                    this.F.set(Z80.sz53p_table_$LI$()[this.A.value]);
                })();
            } else if(((typeof r === 'number') || r === null)) {
                return <any>this.OR$int(r);
            } else throw new Error('invalid overload');
        }

        private OR$int(value : number) {
            this.A.value |= value;
            this.F.set(Z80.sz53p_table_$LI$()[this.A.value]);
        }

        public OUT(rp? : any, reg? : any) : any {
            if(((rp != null && (rp["__interfaces"] != null && rp["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp.constructor != null && rp.constructor["__interfaces"] != null && rp.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp === null) && ((reg != null && reg instanceof zx81emulator.z80.Register) || reg === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    this.contend_io(rp.get(), 3);
                    this.machine.writeport(rp.get(), reg.get());
                })();
            } else if(((rp != null && (rp["__interfaces"] != null && rp["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0 || rp.constructor != null && rp.constructor["__interfaces"] != null && rp.constructor["__interfaces"].indexOf("zx81emulator.z80.RegisterPair") >= 0)) || rp === null) && ((typeof reg === 'number') || reg === null)) {
                return <any>this.OUT$zx81emulator_z80_RegisterPair$int(rp, reg);
            } else if(((typeof rp === 'number') || rp === null) && ((reg != null && reg instanceof zx81emulator.z80.Register) || reg === null)) {
                return <any>this.OUT$int$zx81emulator_z80_Register(rp, reg);
            } else throw new Error('invalid overload');
        }

        private OUT$zx81emulator_z80_RegisterPair$int(rp : RegisterPair, value : number) {
            this.contend_io(rp.get(), 3);
            this.machine.writeport(rp.get(), value);
        }

        private OUT$int$zx81emulator_z80_Register(port : number, reg : Register) {
            this.contend_io(port, 3);
            this.machine.writeport(port, reg.get());
        }

        private POP16() : number {
            this.contend(this.SP, 3);
            this.contend(this.SP, 3);
            return this.machine.readbyte(this.SP++) + (this.machine.readbyte(this.SP++) << 8);
        }

        private PUSH16(value : number) {
            this.SP--;
            this.contend(this.SP, 3);
            this.machine.writebyte(this.SP, value >> 8);
            this.SP--;
            this.contend(this.SP, 3);
            this.machine.writebyte(this.SP, value & 255);
        }

        private RET() {
            this.PC = this.POP16();
        }

        private RL(reg : Register) {
            let rltemp : number = reg.get();
            reg.set((rltemp << 1) | (this.F.value & Z80.FLAG_C));
            this.F.set((rltemp >> 7) | Z80.sz53p_table_$LI$()[reg.get()]);
        }

        private RLC(reg : Register) {
            let before : number = reg.get();
            let newValue : number = (before << 1) | (before >> 7);
            reg.set(newValue);
            let after : number = reg.get();
            this.F.set((after & Z80.FLAG_C) | Z80.sz53p_table_$LI$()[after]);
        }

        private RR(reg : Register) {
            let rrtemp : number = reg.get();
            reg.set((reg.get() >> 1) | (this.F.value << 7));
            this.F.set((rrtemp & Z80.FLAG_C) | Z80.sz53p_table_$LI$()[reg.get()]);
        }

        private RRC(reg : Register) {
            this.F.set(reg.get() & Z80.FLAG_C);
            reg.set((reg.get() >> 1) | (reg.get() << 7));
            this.F.or(Z80.sz53p_table_$LI$()[reg.get()]);
        }

        private RST(value : number) {
            this.PUSH16(this.PC);
            this.PC = value;
        }

        private SBC(value : number) {
            let sbctemp : number = this.A.value - (value) - (this.F.value & Z80.FLAG_C);
            let lookup : number = ((this.A.value & 136) >> 3) | (((value) & 136) >> 2) | ((sbctemp & 136) >> 1);
            this.A.set(sbctemp);
            this.F.set(((sbctemp & 256) > 0?Z80.FLAG_C:0) | Z80.FLAG_N | Z80.halfcarry_sub_table_$LI$()[lookup & 7] | Z80.overflow_sub_table_$LI$()[lookup >> 4] | Z80.sz53_table_$LI$()[this.A.value]);
        }

        private SBC16(value : number) {
            let sub16temp : number = this.HL.word - (value) - (this.F.value & Z80.FLAG_C);
            let lookup : number = ((this.HL.word & 34816) >> 11) | (((value) & 34816) >> 10) | ((sub16temp & 34816) >> 9);
            this.HL.set(sub16temp);
            this.F.set(((sub16temp & 65536) > 0?Z80.FLAG_C:0) | Z80.FLAG_N | Z80.overflow_sub_table_$LI$()[lookup >> 4] | (this.H.get() & (Z80.FLAG_3 | Z80.FLAG_5 | Z80.FLAG_S)) | Z80.halfcarry_sub_table_$LI$()[lookup & 7] | (this.HL.word > 0?0:Z80.FLAG_Z));
        }

        private SLA(reg : Register) {
            this.F.set(reg.get() >> 7);
            reg.set(reg.get() << 1);
            this.F.or(Z80.sz53p_table_$LI$()[reg.get()]);
        }

        private SLL(reg : Register) {
            this.F.set(reg.get() >> 7);
            reg.set((reg.get() << 1) | 1);
            this.F.or(Z80.sz53p_table_$LI$()[reg.get()]);
        }

        private SRA(reg : Register) {
            this.F.set(reg.get() & Z80.FLAG_C);
            reg.set((reg.get() & 128) | (reg.get() >> 1));
            this.F.or(Z80.sz53p_table_$LI$()[reg.get()]);
        }

        private SRL(reg : Register) {
            this.F.set(reg.get() & Z80.FLAG_C);
            reg.set(reg.get() >> 1);
            this.F.or(Z80.sz53p_table_$LI$()[reg.get()]);
        }

        private SUB(value : number) {
            let subtemp : number = this.A.value - (value);
            let lookup : number = ((this.A.value & 136) >> 3) | (((value) & 136) >> 2) | ((subtemp & 136) >> 1);
            this.A.set(subtemp);
            this.F.set(((subtemp & 256) > 0?Z80.FLAG_C:0) | Z80.FLAG_N | Z80.halfcarry_sub_table_$LI$()[lookup & 7] | Z80.overflow_sub_table_$LI$()[lookup >> 4] | Z80.sz53_table_$LI$()[this.A.value]);
        }

        public XOR(r? : any) : any {
            if(((r != null && r instanceof zx81emulator.z80.Register) || r === null)) {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() => {
                    this.A.set(this.A.value ^ r.get());
                    this.F.set(Z80.sz53p_table_$LI$()[this.A.value]);
                })();
            } else if(((typeof r === 'number') || r === null)) {
                return <any>this.XOR$int(r);
            } else throw new Error('invalid overload');
        }

        private XOR$int(value : number) {
            this.A.set(this.A.value ^ (value));
            this.F.set(Z80.sz53p_table_$LI$()[this.A.value]);
        }

        public do_opcode() : number {
            this.tstates = 0;
            this.contend(this.PC, 4);
            this.R = (this.R + 1) & 255;
            let opcode : number = this.machine.opcode_fetch(this.PC++);
            switch((opcode)) {
                case 0:
                    break;
                case 1:
                    this.contend(this.PC, 3);
                    this.C.set(this.machine.readbyte(this.PC++));
                    this.contend(this.PC, 3);
                    this.B.set(this.machine.readbyte(this.PC++));
                    break;
                case 2:
                    this.contend(this.BC, 3);
                    this.machine.writebyte(this.BC.word, this.A.value);
                    break;
                case 3:
                    this.tstates += 2;
                    this.BC.inc();
                    break;
                case 4:
                    this.INC(this.B);
                    break;
                case 5:
                    this.DEC(this.B);
                    break;
                case 6:
                    this.contend(this.PC, 3);
                    this.B.set(this.machine.readbyte(this.PC++));
                    break;
                case 7:
                    this.A.set((this.A.value << 1) | (this.A.value >> 7));
                    this.F.set((this.F.value & (Z80.FLAG_P | Z80.FLAG_Z | Z80.FLAG_S)) | (this.A.value & (Z80.FLAG_C | Z80.FLAG_3 | Z80.FLAG_5)));
                    break;
                case 8:
                {
                    let wordtemp : number = this.AF.get();
                    this.AF.set(this.AF_);
                    this.AF_ = wordtemp;
                };
                    break;
                case 9:
                    this.ADD16(this.HL, this.BC);
                    break;
                case 10:
                    this.contend(this.BC, 3);
                    this.A.set(this.machine.readbyte(this.BC.word));
                    break;
                case 11:
                    this.tstates += 2;
                    this.BC.dec();
                    break;
                case 12:
                    this.INC(this.C);
                    break;
                case 13:
                    this.DEC(this.C);
                    break;
                case 14:
                    this.contend(this.PC, 3);
                    this.C.set(this.machine.readbyte(this.PC++));
                    break;
                case 15:
                    this.F.set((this.F.value & (Z80.FLAG_P | Z80.FLAG_Z | Z80.FLAG_S)) | (this.A.value & Z80.FLAG_C));
                    this.A.set((this.A.value >> 1) | (this.A.value << 7));
                    this.F.or(this.A.value & (Z80.FLAG_3 | Z80.FLAG_5));
                    break;
                case 16:
                    this.tstates++;
                    this.contend(this.PC, 3);
                    this.B.dec();
                    if(this.B.get() !== 0) {
                        this.JR();
                    }
                    this.PC++;
                    break;
                case 17:
                    this.contend(this.PC, 3);
                    this.E.set(this.machine.readbyte(this.PC++));
                    this.contend(this.PC, 3);
                    this.D.set(this.machine.readbyte(this.PC++));
                    break;
                case 18:
                    this.contend(this.DE, 3);
                    this.machine.writebyte(this.DE.word, this.A.value);
                    break;
                case 19:
                    this.tstates += 2;
                    this.DE.inc();
                    break;
                case 20:
                    this.INC(this.D);
                    break;
                case 21:
                    this.DEC(this.D);
                    break;
                case 22:
                    this.contend(this.PC, 3);
                    this.D.set(this.machine.readbyte(this.PC++));
                    break;
                case 23:
                {
                    let bytetemp : number = this.A.value;
                    this.A.set((this.A.value << 1) | (this.F.value & Z80.FLAG_C));
                    this.F.set((this.F.value & (Z80.FLAG_P | Z80.FLAG_Z | Z80.FLAG_S)) | (this.A.value & (Z80.FLAG_3 | Z80.FLAG_5)) | (bytetemp >> 7));
                };
                    break;
                case 24:
                    this.contend(this.PC, 3);
                    this.JR();
                    this.PC++;
                    break;
                case 25:
                    this.ADD16(this.HL, this.DE);
                    break;
                case 26:
                    this.contend(this.DE, 3);
                    this.A.set(this.machine.readbyte(this.DE.word));
                    break;
                case 27:
                    this.tstates += 2;
                    this.DE.dec();
                    break;
                case 28:
                    this.INC(this.E);
                    break;
                case 29:
                    this.DEC(this.E);
                    break;
                case 30:
                    this.contend(this.PC, 3);
                    this.E.set(this.machine.readbyte(this.PC++));
                    break;
                case 31:
                {
                    let bytetemp : number = this.A.value;
                    this.A.set((this.A.value >> 1) | (this.F.value << 7));
                    this.F.set((this.F.value & (Z80.FLAG_P | Z80.FLAG_Z | Z80.FLAG_S)) | (this.A.value & (Z80.FLAG_3 | Z80.FLAG_5)) | (bytetemp & Z80.FLAG_C));
                };
                    break;
                case 32:
                    this.contend(this.PC, 3);
                    if((this.F.value & Z80.FLAG_Z) === 0) {
                        this.JR();
                    }
                    this.PC++;
                    break;
                case 33:
                    this.contend(this.PC, 3);
                    this.L.set(this.machine.readbyte(this.PC++));
                    this.contend(this.PC, 3);
                    this.H.set(this.machine.readbyte(this.PC++));
                    break;
                case 34:
                    this.LD16_NNRR(this.HL.word);
                    break;
                case 35:
                    this.tstates += 2;
                    this.HL.inc();
                    break;
                case 36:
                    this.INC(this.H);
                    break;
                case 37:
                    this.DEC(this.H);
                    break;
                case 38:
                    this.contend(this.PC, 3);
                    this.H.set(this.machine.readbyte(this.PC++));
                    break;
                case 39:
                {
                    let add : number = 0;
                    let carry : number = (this.F.value & Z80.FLAG_C);
                    if(((this.F.value & Z80.FLAG_H) !== 0) || ((this.A.value & 15) > 9)) add = 6;
                    if(carry !== 0 || (this.A.value > 159)) add |= 96;
                    if(this.A.value > 153) carry = 1;
                    if((this.F.value & Z80.FLAG_N) !== 0) {
                        this.SUB(add);
                    } else {
                        if((this.A.value > 144) && ((this.A.value & 15) > 9)) add |= 96;
                        this.ADD(add);
                    }
                    this.F.set((this.F.value & ~(Z80.FLAG_C | Z80.FLAG_P)) | carry | Z80.parity_table_$LI$()[this.A.value]);
                };
                    break;
                case 40:
                    this.contend(this.PC, 3);
                    if((this.F.value & Z80.FLAG_Z) !== 0) {
                        this.JR();
                    }
                    this.PC++;
                    break;
                case 41:
                    this.ADD16(this.HL, this.HL);
                    break;
                case 42:
                    this.HL.set(this.LD16_RRNN());
                    break;
                case 43:
                    this.tstates += 2;
                    this.HL.dec();
                    break;
                case 44:
                    this.INC(this.L);
                    break;
                case 45:
                    this.DEC(this.L);
                    break;
                case 46:
                    this.contend(this.PC, 3);
                    this.L.set(this.machine.readbyte(this.PC++));
                    break;
                case 47:
                    this.A.set(this.A.value ^ 255);
                    this.F.set((this.F.value & (Z80.FLAG_C | Z80.FLAG_P | Z80.FLAG_Z | Z80.FLAG_S)) | (this.A.value & (Z80.FLAG_3 | Z80.FLAG_5)) | (Z80.FLAG_N | Z80.FLAG_H));
                    break;
                case 48:
                    this.contend(this.PC, 3);
                    if((this.F.value & Z80.FLAG_C) === 0) {
                        this.JR();
                    }
                    this.PC++;
                    break;
                case 49:
                    this.contend(this.PC, 3);
                    this.contend(this.PC, 3);
                    this.SP = this.machine.readbyte(this.PC++) + (this.machine.readbyte(this.PC++) << 8);
                    break;
                case 50:
                    this.contend(this.PC, 3);
                {
                    let wordtemp : number = this.machine.readbyte(this.PC++);
                    this.contend(this.PC, 3);
                    wordtemp |= this.machine.readbyte(this.PC++) << 8;
                    this.contend(wordtemp, 3);
                    this.machine.writebyte(wordtemp, this.A.value);
                };
                    break;
                case 51:
                    this.tstates += 2;
                    this.SP++;
                    break;
                case 52:
                    this.contend(this.HL, 4);
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.INC(bytetemp);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 53:
                    this.contend(this.HL, 4);
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.DEC(bytetemp);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 54:
                    this.contend(this.PC, 3);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.PC++));
                    break;
                case 55:
                    this.F.and(~(Z80.FLAG_N | Z80.FLAG_H));
                    this.F.or((this.A.value & (Z80.FLAG_3 | Z80.FLAG_5)) | Z80.FLAG_C);
                    break;
                case 56:
                    this.contend(this.PC, 3);
                    if((this.F.value & Z80.FLAG_C) !== 0) {
                        this.JR();
                    }
                    this.PC++;
                    break;
                case 57:
                    this.ADD16(this.HL, this.SP);
                    break;
                case 58:
                {
                    let wordtemp : number;
                    this.contend(this.PC, 3);
                    wordtemp = this.machine.readbyte(this.PC++);
                    this.contend(this.PC, 3);
                    wordtemp |= (this.machine.readbyte(this.PC++) << 8);
                    this.contend(wordtemp, 3);
                    this.A.set(this.machine.readbyte(wordtemp));
                };
                    break;
                case 59:
                    this.tstates += 2;
                    this.SP--;
                    break;
                case 60:
                    this.INC(this.A);
                    break;
                case 61:
                    this.DEC(this.A);
                    break;
                case 62:
                    this.contend(this.PC, 3);
                    this.A.set(this.machine.readbyte(this.PC++));
                    break;
                case 63:
                    this.F.set((this.F.value & (Z80.FLAG_P | Z80.FLAG_Z | Z80.FLAG_S)) | ((this.F.value & Z80.FLAG_C) !== 0?Z80.FLAG_H:Z80.FLAG_C) | (this.A.value & (Z80.FLAG_3 | Z80.FLAG_5)));
                    break;
                case 64:
                    break;
                case 65:
                    this.B.set(this.C);
                    break;
                case 66:
                    this.B.set(this.D);
                    break;
                case 67:
                    this.B.set(this.E);
                    break;
                case 68:
                    this.B.set(this.H);
                    break;
                case 69:
                    this.B.set(this.L);
                    break;
                case 70:
                    this.contend(this.HL, 3);
                    this.B.set(this.machine.readbyte(this.HL.word));
                    break;
                case 71:
                    this.B.set(this.A);
                    break;
                case 72:
                    this.C.set(this.B);
                    break;
                case 73:
                    break;
                case 74:
                    this.C.set(this.D);
                    break;
                case 75:
                    this.C.set(this.E);
                    break;
                case 76:
                    this.C.set(this.H);
                    break;
                case 77:
                    this.C.set(this.L);
                    break;
                case 78:
                    this.contend(this.HL, 3);
                    this.C.set(this.machine.readbyte(this.HL.word));
                    break;
                case 79:
                    this.C.set(this.A);
                    break;
                case 80:
                    this.D.set(this.B);
                    break;
                case 81:
                    this.D.set(this.C);
                    break;
                case 82:
                    break;
                case 83:
                    this.D.set(this.E);
                    break;
                case 84:
                    this.D.set(this.H);
                    break;
                case 85:
                    this.D.set(this.L);
                    break;
                case 86:
                    this.contend(this.HL, 3);
                    this.D.set(this.machine.readbyte(this.HL.word));
                    break;
                case 87:
                    this.D.set(this.A);
                    break;
                case 88:
                    this.E.set(this.B);
                    break;
                case 89:
                    this.E.set(this.C);
                    break;
                case 90:
                    this.E.set(this.D);
                    break;
                case 91:
                    break;
                case 92:
                    this.E.set(this.H);
                    break;
                case 93:
                    this.E.set(this.L);
                    break;
                case 94:
                    this.contend(this.HL, 3);
                    this.E.set(this.machine.readbyte(this.HL.word));
                    break;
                case 95:
                    this.E.set(this.A);
                    break;
                case 96:
                    this.H.set(this.B);
                    break;
                case 97:
                    this.H.set(this.C);
                    break;
                case 98:
                    this.H.set(this.D);
                    break;
                case 99:
                    this.H.set(this.E);
                    break;
                case 100:
                    break;
                case 101:
                    this.H.set(this.L);
                    break;
                case 102:
                    this.contend(this.HL, 3);
                    this.H.set(this.machine.readbyte(this.HL.word));
                    break;
                case 103:
                    this.H.set(this.A);
                    break;
                case 104:
                    this.L.set(this.B);
                    break;
                case 105:
                    this.L.set(this.C);
                    break;
                case 106:
                    this.L.set(this.D);
                    break;
                case 107:
                    this.L.set(this.E);
                    break;
                case 108:
                    this.L.set(this.H);
                    break;
                case 109:
                    break;
                case 110:
                    this.contend(this.HL, 3);
                    this.L.set(this.machine.readbyte(this.HL.word));
                    break;
                case 111:
                    this.L.set(this.A);
                    break;
                case 112:
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.B.get());
                    break;
                case 113:
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.C.get());
                    break;
                case 114:
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.D.get());
                    break;
                case 115:
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.E.get());
                    break;
                case 116:
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.H.get());
                    break;
                case 117:
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.L.get());
                    break;
                case 118:
                    this.halted = 1;
                    this.PC--;
                    break;
                case 119:
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.A.value);
                    break;
                case 120:
                    this.A.set(this.B);
                    break;
                case 121:
                    this.A.set(this.C);
                    break;
                case 122:
                    this.A.set(this.D);
                    break;
                case 123:
                    this.A.set(this.E);
                    break;
                case 124:
                    this.A.set(this.H);
                    break;
                case 125:
                    this.A.set(this.L);
                    break;
                case 126:
                    this.contend(this.HL, 3);
                    this.A.set(this.machine.readbyte(this.HL.word));
                    break;
                case 127:
                    break;
                case 128:
                    this.ADD(this.B.get());
                    break;
                case 129:
                    this.ADD(this.C.get());
                    break;
                case 130:
                    this.ADD(this.D.get());
                    break;
                case 131:
                    this.ADD(this.E.get());
                    break;
                case 132:
                    this.ADD(this.H.get());
                    break;
                case 133:
                    this.ADD(this.L.get());
                    break;
                case 134:
                    this.contend(this.HL, 3);
                    this.ADD(this.machine.readbyte(this.HL.word));
                    break;
                case 135:
                    this.ADD(this.A.get());
                    break;
                case 136:
                    this.ADC(this.B.get());
                    break;
                case 137:
                    this.ADC(this.C.get());
                    break;
                case 138:
                    this.ADC(this.D.get());
                    break;
                case 139:
                    this.ADC(this.E.get());
                    break;
                case 140:
                    this.ADC(this.H.get());
                    break;
                case 141:
                    this.ADC(this.L.get());
                    break;
                case 142:
                    this.contend(this.HL, 3);
                    this.ADC(this.machine.readbyte(this.HL.word));
                    break;
                case 143:
                    this.ADC(this.A.get());
                    break;
                case 144:
                    this.SUB(this.B.get());
                    break;
                case 145:
                    this.SUB(this.C.get());
                    break;
                case 146:
                    this.SUB(this.D.get());
                    break;
                case 147:
                    this.SUB(this.E.get());
                    break;
                case 148:
                    this.SUB(this.H.get());
                    break;
                case 149:
                    this.SUB(this.L.get());
                    break;
                case 150:
                    this.contend(this.HL, 3);
                    this.SUB(this.machine.readbyte(this.HL.word));
                    break;
                case 151:
                    this.SUB(this.A.get());
                    break;
                case 152:
                    this.SBC(this.B.get());
                    break;
                case 153:
                    this.SBC(this.C.get());
                    break;
                case 154:
                    this.SBC(this.D.get());
                    break;
                case 155:
                    this.SBC(this.E.get());
                    break;
                case 156:
                    this.SBC(this.H.get());
                    break;
                case 157:
                    this.SBC(this.L.get());
                    break;
                case 158:
                    this.contend(this.HL, 3);
                    this.SBC(this.machine.readbyte(this.HL.word));
                    break;
                case 159:
                    this.SBC(this.A.get());
                    break;
                case 160:
                    this.AND(this.B);
                    break;
                case 161:
                    this.AND(this.C);
                    break;
                case 162:
                    this.AND(this.D);
                    break;
                case 163:
                    this.AND(this.E);
                    break;
                case 164:
                    this.AND(this.H);
                    break;
                case 165:
                    this.AND(this.L);
                    break;
                case 166:
                    this.contend(this.HL, 3);
                    this.AND(this.machine.readbyte(this.HL.word));
                    break;
                case 167:
                    this.AND(this.A.get());
                    break;
                case 168:
                    this.XOR(this.B);
                    break;
                case 169:
                    this.XOR(this.C);
                    break;
                case 170:
                    this.XOR(this.D);
                    break;
                case 171:
                    this.XOR(this.E);
                    break;
                case 172:
                    this.XOR(this.H);
                    break;
                case 173:
                    this.XOR(this.L);
                    break;
                case 174:
                    this.contend(this.HL, 3);
                    this.XOR(this.machine.readbyte(this.HL.word));
                    break;
                case 175:
                    this.XOR(this.A);
                    break;
                case 176:
                    this.OR(this.B);
                    break;
                case 177:
                    this.OR(this.C);
                    break;
                case 178:
                    this.OR(this.D);
                    break;
                case 179:
                    this.OR(this.E);
                    break;
                case 180:
                    this.OR(this.H);
                    break;
                case 181:
                    this.OR(this.L);
                    break;
                case 182:
                    this.contend(this.HL, 3);
                    this.OR(this.machine.readbyte(this.HL.word));
                    break;
                case 183:
                    this.OR(this.A);
                    break;
                case 184:
                    this.CP(this.B.get());
                    break;
                case 185:
                    this.CP(this.C.get());
                    break;
                case 186:
                    this.CP(this.D.get());
                    break;
                case 187:
                    this.CP(this.E.get());
                    break;
                case 188:
                    this.CP(this.H.get());
                    break;
                case 189:
                    this.CP(this.L.get());
                    break;
                case 190:
                    this.contend(this.HL, 3);
                    this.CP(this.machine.readbyte(this.HL.word));
                    break;
                case 191:
                    this.CP(this.A.get());
                    break;
                case 192:
                    this.tstates++;
                    if((this.F.value & Z80.FLAG_Z) === 0) {
                        this.RET();
                    }
                    break;
                case 193:
                    this.BC.set(this.POP16());
                    break;
                case 194:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_Z) === 0) {
                        this.JP();
                    } else this.PC += 2;
                    break;
                case 195:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    this.JP();
                    break;
                case 196:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_Z) === 0) {
                        this.CALL();
                    } else this.PC += 2;
                    break;
                case 197:
                    this.tstates++;
                    this.PUSH16(this.BC.word);
                    break;
                case 198:
                    this.contend(this.PC, 3);
                    this.ADD(this.machine.readbyte(this.PC++));
                    break;
                case 199:
                    this.tstates++;
                    this.RST(0);
                    break;
                case 200:
                    this.tstates++;
                    if((this.F.value & Z80.FLAG_Z) !== 0) {
                        this.RET();
                    }
                    break;
                case 201:
                    this.RET();
                    break;
                case 202:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_Z) > 0) {
                        this.JP();
                    } else this.PC += 2;
                    break;
                case 203:
                {
                    this.contend(this.PC, 4);
                    let opcode2 : number = this.machine.opcode_fetch(this.PC++);
                    this.R = (this.R + 1) & 255;
                    this.do_opcode_CB(opcode2);
                };
                    break;
                case 204:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_Z) > 0) {
                        this.CALL();
                    } else this.PC += 2;
                    break;
                case 205:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    this.CALL();
                    break;
                case 206:
                    this.contend(this.PC, 3);
                    this.ADC(this.machine.readbyte(this.PC++));
                    break;
                case 207:
                    this.tstates++;
                    this.RST(8);
                    break;
                case 208:
                    this.tstates++;
                    if((this.F.value & Z80.FLAG_C) === 0) {
                        this.RET();
                    }
                    break;
                case 209:
                    this.DE.set(this.POP16());
                    break;
                case 210:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_C) === 0) {
                        this.JP();
                    } else this.PC += 2;
                    break;
                case 211:
                    this.contend(this.PC, 4);
                    this.OUT(this.machine.readbyte(this.PC++) + (this.A.value << 8), this.A);
                    break;
                case 212:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_C) === 0) {
                        this.CALL();
                    } else this.PC += 2;
                    break;
                case 213:
                    this.tstates++;
                    this.PUSH16(this.DE.word);
                    break;
                case 214:
                    this.contend(this.PC, 3);
                    this.SUB(this.machine.readbyte(this.PC++));
                    break;
                case 215:
                    this.tstates++;
                    this.RST(16);
                    break;
                case 216:
                    this.tstates++;
                    if((this.F.value & Z80.FLAG_C) > 0) {
                        this.RET();
                    }
                    break;
                case 217:
                {
                    let wordtemp : number = this.BC.word;
                    this.BC.set(this.BC_);
                    this.BC_ = wordtemp;
                    wordtemp = this.DE.word;
                    this.DE.set(this.DE_);
                    this.DE_ = wordtemp;
                    wordtemp = this.HL.word;
                    this.HL.set(this.HL_);
                    this.HL_ = wordtemp;
                };
                    break;
                case 218:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_C) > 0) {
                        this.JP();
                    } else this.PC += 2;
                    break;
                case 219:
                {
                    this.contend(this.PC, 4);
                    let intemp : number = this.machine.readbyte(this.PC++) + (this.A.value << 8);
                    this.contend_io(intemp, 3);
                    this.A.set(this.machine.readport(intemp));
                };
                    break;
                case 220:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_C) > 0) {
                        this.CALL();
                    } else this.PC += 2;
                    break;
                case 221:
                {
                    this.contend(this.PC, 4);
                    let opcode2 : number = this.machine.opcode_fetch(this.PC++);
                    this.R = (this.R + 1) & 255;
                    this.do_opcode_DDFD(opcode2, this.IX, this.IXL, this.IXH);
                };
                    break;
                case 222:
                    this.contend(this.PC, 3);
                    this.SBC(this.machine.readbyte(this.PC++));
                    break;
                case 223:
                    this.tstates++;
                    this.RST(24);
                    break;
                case 224:
                    this.tstates++;
                    if((this.F.value & Z80.FLAG_P) === 0) {
                        this.RET();
                    }
                    break;
                case 225:
                    this.HL.set(this.POP16());
                    break;
                case 226:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_P) === 0) {
                        this.JP();
                    } else this.PC += 2;
                    break;
                case 227:
                {
                    let bytetempl : number = this.machine.readbyte(this.SP);
                    let bytetemph : number = this.machine.readbyte(this.SP + 1);
                    this.contend(this.SP, 3);
                    this.contend(this.SP + 1, 4);
                    this.contend(this.SP, 3);
                    this.contend(this.SP + 1, 5);
                    this.machine.writebyte(this.SP, this.L.get());
                    this.machine.writebyte(this.SP + 1, this.H.get());
                    this.L.set(bytetempl);
                    this.H.set(bytetemph);
                };
                    break;
                case 228:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_P) === 0) {
                        this.CALL();
                    } else this.PC += 2;
                    break;
                case 229:
                    this.tstates++;
                    this.PUSH16(this.HL.word);
                    break;
                case 230:
                    this.contend(this.PC, 3);
                    this.AND(this.machine.readbyte(this.PC++));
                    break;
                case 231:
                    this.tstates++;
                    this.RST(32);
                    break;
                case 232:
                    this.tstates++;
                    if((this.F.value & Z80.FLAG_P) > 0) {
                        this.RET();
                    }
                    break;
                case 233:
                    this.PC = this.HL.word;
                    break;
                case 234:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_P) > 0) {
                        this.JP();
                    } else this.PC += 2;
                    break;
                case 235:
                {
                    let wordtemp : number = this.DE.word;
                    this.DE.set(this.HL);
                    this.HL.set(wordtemp);
                };
                    break;
                case 236:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_P) > 0) {
                        this.CALL();
                    } else this.PC += 2;
                    break;
                case 237:
                {
                    this.contend(this.PC, 4);
                    let opcode2 : number = this.machine.opcode_fetch(this.PC++);
                    this.R = (this.R + 1) & 255;
                    this.do_opcode_ED(opcode2);
                };
                    break;
                case 238:
                    this.contend(this.PC, 3);
                {
                    let bytetemp : number = this.machine.readbyte(this.PC++);
                    this.XOR(bytetemp);
                };
                    break;
                case 239:
                    this.tstates++;
                    this.RST(40);
                    break;
                case 240:
                    this.tstates++;
                    if((this.F.value & Z80.FLAG_S) === 0) {
                        this.RET();
                    }
                    break;
                case 241:
                    this.AF.set(this.POP16());
                    break;
                case 242:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_S) === 0) {
                        this.JP();
                    } else this.PC += 2;
                    break;
                case 243:
                    this.IFF1 = this.IFF2 = 0;
                    break;
                case 244:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_S) === 0) {
                        this.CALL();
                    } else this.PC += 2;
                    break;
                case 245:
                    this.tstates++;
                    this.PUSH16(this.AF.get());
                    break;
                case 246:
                    this.contend(this.PC, 3);
                {
                    let bytetemp : number = this.machine.readbyte(this.PC++);
                    this.OR(bytetemp);
                };
                    break;
                case 247:
                    this.tstates++;
                    this.RST(48);
                    break;
                case 248:
                    this.tstates++;
                    if((this.F.value & Z80.FLAG_S) !== 0) {
                        this.RET();
                    }
                    break;
                case 249:
                    this.tstates += 2;
                    this.SP = this.HL.word;
                    break;
                case 250:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_S) > 0) {
                        this.JP();
                    } else this.PC += 2;
                    break;
                case 251:
                    this.IFF1 = this.IFF2 = 1;
                    break;
                case 252:
                    this.contend(this.PC, 3);
                    this.contend(this.PC + 1, 3);
                    if((this.F.value & Z80.FLAG_S) > 0) {
                        this.CALL();
                    } else this.PC += 2;
                    break;
                case 253:
                {
                    this.contend(this.PC, 4);
                    let opcode2 : number = this.machine.opcode_fetch(this.PC++);
                    this.R = (this.R + 1) & 255;
                    this.do_opcode_DDFD(opcode2, this.IY, this.IYL, this.IYH);
                };
                    break;
                case 254:
                    this.contend(this.PC, 3);
                {
                    let bytetemp : number = this.machine.readbyte(this.PC++);
                    this.CP(bytetemp);
                };
                    break;
                case 255:
                    this.tstates++;
                    this.RST(56);
                    break;
            }
            this.R = this.R & 127;
            return (this.tstates);
        }

        private do_opcode_CB(opcode2 : number) {
            switch((opcode2)) {
                case 0:
                    this.RLC(this.B);
                    break;
                case 1:
                    this.RLC(this.C);
                    break;
                case 2:
                    this.RLC(this.D);
                    break;
                case 3:
                    this.RLC(this.E);
                    break;
                case 4:
                    this.RLC(this.H);
                    break;
                case 5:
                    this.RLC(this.L);
                    break;
                case 6:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.RLC(bytetemp);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 7:
                    this.RLC(this.A);
                    break;
                case 8:
                    this.RRC(this.B);
                    break;
                case 9:
                    this.RRC(this.C);
                    break;
                case 10:
                    this.RRC(this.D);
                    break;
                case 11:
                    this.RRC(this.E);
                    break;
                case 12:
                    this.RRC(this.H);
                    break;
                case 13:
                    this.RRC(this.L);
                    break;
                case 14:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.RRC(bytetemp);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 15:
                    this.RRC(this.A);
                    break;
                case 16:
                    this.RL(this.B);
                    break;
                case 17:
                    this.RL(this.C);
                    break;
                case 18:
                    this.RL(this.D);
                    break;
                case 19:
                    this.RL(this.E);
                    break;
                case 20:
                    this.RL(this.H);
                    break;
                case 21:
                    this.RL(this.L);
                    break;
                case 22:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.RL(bytetemp);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 23:
                    this.RL(this.A);
                    break;
                case 24:
                    this.RR(this.B);
                    break;
                case 25:
                    this.RR(this.C);
                    break;
                case 26:
                    this.RR(this.D);
                    break;
                case 27:
                    this.RR(this.E);
                    break;
                case 28:
                    this.RR(this.H);
                    break;
                case 29:
                    this.RR(this.L);
                    break;
                case 30:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.RR(bytetemp);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 31:
                    this.RR(this.A);
                    break;
                case 32:
                    this.SLA(this.B);
                    break;
                case 33:
                    this.SLA(this.C);
                    break;
                case 34:
                    this.SLA(this.D);
                    break;
                case 35:
                    this.SLA(this.E);
                    break;
                case 36:
                    this.SLA(this.H);
                    break;
                case 37:
                    this.SLA(this.L);
                    break;
                case 38:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.SLA(bytetemp);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 39:
                    this.SLA(this.A);
                    break;
                case 40:
                    this.SRA(this.B);
                    break;
                case 41:
                    this.SRA(this.C);
                    break;
                case 42:
                    this.SRA(this.D);
                    break;
                case 43:
                    this.SRA(this.E);
                    break;
                case 44:
                    this.SRA(this.H);
                    break;
                case 45:
                    this.SRA(this.L);
                    break;
                case 46:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.SRA(bytetemp);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 47:
                    this.SRA(this.A);
                    break;
                case 48:
                    this.SLL(this.B);
                    break;
                case 49:
                    this.SLL(this.C);
                    break;
                case 50:
                    this.SLL(this.D);
                    break;
                case 51:
                    this.SLL(this.E);
                    break;
                case 52:
                    this.SLL(this.H);
                    break;
                case 53:
                    this.SLL(this.L);
                    break;
                case 54:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.SLL(bytetemp);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 55:
                    this.SLL(this.A);
                    break;
                case 56:
                    this.SRL(this.B);
                    break;
                case 57:
                    this.SRL(this.C);
                    break;
                case 58:
                    this.SRL(this.D);
                    break;
                case 59:
                    this.SRL(this.E);
                    break;
                case 60:
                    this.SRL(this.H);
                    break;
                case 61:
                    this.SRL(this.L);
                    break;
                case 62:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    let bytetemp : Register = this.tempreg;
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.SRL(bytetemp);
                    this.machine.writebyte(this.HL.word, bytetemp.get());
                };
                    break;
                case 63:
                    this.SRL(this.A);
                    break;
                case 64:
                    this.BIT(0, this.B);
                    break;
                case 65:
                    this.BIT(0, this.C);
                    break;
                case 66:
                    this.BIT(0, this.D);
                    break;
                case 67:
                    this.BIT(0, this.E);
                    break;
                case 68:
                    this.BIT(0, this.H);
                    break;
                case 69:
                    this.BIT(0, this.L);
                    break;
                case 70:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    this.contend(this.HL, 4);
                    this.BIT(0, this.tempreg);
                };
                    break;
                case 71:
                    this.BIT(0, this.A);
                    break;
                case 72:
                    this.BIT(1, this.B);
                    break;
                case 73:
                    this.BIT(1, this.C);
                    break;
                case 74:
                    this.BIT(1, this.D);
                    break;
                case 75:
                    this.BIT(1, this.E);
                    break;
                case 76:
                    this.BIT(1, this.H);
                    break;
                case 77:
                    this.BIT(1, this.L);
                    break;
                case 78:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    this.contend(this.HL, 4);
                    this.BIT(1, this.tempreg);
                };
                    break;
                case 79:
                    this.BIT(1, this.A);
                    break;
                case 80:
                    this.BIT(2, this.B);
                    break;
                case 81:
                    this.BIT(2, this.C);
                    break;
                case 82:
                    this.BIT(2, this.D);
                    break;
                case 83:
                    this.BIT(2, this.E);
                    break;
                case 84:
                    this.BIT(2, this.H);
                    break;
                case 85:
                    this.BIT(2, this.L);
                    break;
                case 86:
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    this.contend(this.HL, 4);
                    this.BIT(2, this.tempreg);
                    break;
                case 87:
                    this.BIT(2, this.A);
                    break;
                case 88:
                    this.BIT(3, this.B);
                    break;
                case 89:
                    this.BIT(3, this.C);
                    break;
                case 90:
                    this.BIT(3, this.D);
                    break;
                case 91:
                    this.BIT(3, this.E);
                    break;
                case 92:
                    this.BIT(3, this.H);
                    break;
                case 93:
                    this.BIT(3, this.L);
                    break;
                case 94:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    this.contend(this.HL, 4);
                    this.BIT(3, this.tempreg);
                };
                    break;
                case 95:
                    this.BIT(3, this.A);
                    break;
                case 96:
                    this.BIT(4, this.B);
                    break;
                case 97:
                    this.BIT(4, this.C);
                    break;
                case 98:
                    this.BIT(4, this.D);
                    break;
                case 99:
                    this.BIT(4, this.E);
                    break;
                case 100:
                    this.BIT(4, this.H);
                    break;
                case 101:
                    this.BIT(4, this.L);
                    break;
                case 102:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    this.contend(this.HL, 4);
                    this.BIT(4, this.tempreg);
                };
                    break;
                case 103:
                    this.BIT(4, this.A);
                    break;
                case 104:
                    this.BIT(5, this.B);
                    break;
                case 105:
                    this.BIT(5, this.C);
                    break;
                case 106:
                    this.BIT(5, this.D);
                    break;
                case 107:
                    this.BIT(5, this.E);
                    break;
                case 108:
                    this.BIT(5, this.H);
                    break;
                case 109:
                    this.BIT(5, this.L);
                    break;
                case 110:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    this.contend(this.HL, 4);
                    this.BIT(5, this.tempreg);
                };
                    break;
                case 111:
                    this.BIT(5, this.A);
                    break;
                case 112:
                    this.BIT(6, this.B);
                    break;
                case 113:
                    this.BIT(6, this.C);
                    break;
                case 114:
                    this.BIT(6, this.D);
                    break;
                case 115:
                    this.BIT(6, this.E);
                    break;
                case 116:
                    this.BIT(6, this.H);
                    break;
                case 117:
                    this.BIT(6, this.L);
                    break;
                case 118:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    this.contend(this.HL, 4);
                    this.BIT(6, this.tempreg);
                };
                    break;
                case 119:
                    this.BIT(6, this.A);
                    break;
                case 120:
                    this.BIT7(this.B);
                    break;
                case 121:
                    this.BIT7(this.C);
                    break;
                case 122:
                    this.BIT7(this.D);
                    break;
                case 123:
                    this.BIT7(this.E);
                    break;
                case 124:
                    this.BIT7(this.H);
                    break;
                case 125:
                    this.BIT7(this.L);
                    break;
                case 126:
                {
                    this.tempreg.set(this.machine.readbyte(this.HL.word));
                    this.contend(this.HL, 4);
                    this.BIT7(this.tempreg);
                };
                    break;
                case 127:
                    this.BIT7(this.A);
                    break;
                case 128:
                    this.B.and(254);
                    break;
                case 129:
                    this.C.and(254);
                    break;
                case 130:
                    this.D.and(254);
                    break;
                case 131:
                    this.E.and(254);
                    break;
                case 132:
                    this.H.and(254);
                    break;
                case 133:
                    this.L.and(254);
                    break;
                case 134:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) & 254);
                    break;
                case 135:
                    this.A.and(254);
                    break;
                case 136:
                    this.B.and(253);
                    break;
                case 137:
                    this.C.and(253);
                    break;
                case 138:
                    this.D.and(253);
                    break;
                case 139:
                    this.E.and(253);
                    break;
                case 140:
                    this.H.and(253);
                    break;
                case 141:
                    this.L.and(253);
                    break;
                case 142:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) & 253);
                    break;
                case 143:
                    this.A.and(253);
                    break;
                case 144:
                    this.B.and(251);
                    break;
                case 145:
                    this.C.and(251);
                    break;
                case 146:
                    this.D.and(251);
                    break;
                case 147:
                    this.E.and(251);
                    break;
                case 148:
                    this.H.and(251);
                    break;
                case 149:
                    break;
                case 150:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) & 251);
                    break;
                case 151:
                    this.A.and(251);
                    break;
                case 152:
                    this.B.and(247);
                    break;
                case 153:
                    this.C.and(247);
                    break;
                case 154:
                    this.D.and(247);
                    break;
                case 155:
                    this.E.and(247);
                    break;
                case 156:
                    this.H.and(247);
                    break;
                case 157:
                    this.L.and(247);
                    break;
                case 158:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) & 247);
                    break;
                case 159:
                    this.A.and(247);
                    break;
                case 160:
                    this.B.and(239);
                    break;
                case 161:
                    this.C.and(239);
                    break;
                case 162:
                    this.D.and(239);
                    break;
                case 163:
                    this.E.and(239);
                    break;
                case 164:
                    this.H.and(239);
                    break;
                case 165:
                    this.L.and(239);
                    break;
                case 166:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) & 239);
                    break;
                case 167:
                    this.A.and(239);
                    break;
                case 168:
                    this.B.and(223);
                    break;
                case 169:
                    this.C.and(223);
                    break;
                case 170:
                    this.D.and(223);
                    break;
                case 171:
                    this.E.and(223);
                    break;
                case 172:
                    this.H.and(223);
                    break;
                case 173:
                    this.L.and(223);
                    break;
                case 174:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) & 223);
                    break;
                case 175:
                    this.A.and(223);
                    break;
                case 176:
                    this.B.and(191);
                    break;
                case 177:
                    this.C.and(191);
                    break;
                case 178:
                    this.D.and(191);
                    break;
                case 179:
                    this.E.and(191);
                    break;
                case 180:
                    this.H.and(191);
                    break;
                case 181:
                    this.L.and(191);
                    break;
                case 182:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) & 191);
                    break;
                case 183:
                    this.A.and(191);
                    break;
                case 184:
                    this.B.and(127);
                    break;
                case 185:
                    this.C.and(127);
                    break;
                case 186:
                    this.D.and(127);
                    break;
                case 187:
                    this.E.and(127);
                    break;
                case 188:
                    this.H.and(127);
                    break;
                case 189:
                    this.L.and(127);
                    break;
                case 190:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) & 127);
                    break;
                case 191:
                    this.A.and(127);
                    break;
                case 192:
                    this.B.or(1);
                    break;
                case 193:
                    this.C.or(1);
                    break;
                case 194:
                    this.D.or(1);
                    break;
                case 195:
                    this.E.or(1);
                    break;
                case 196:
                    this.H.or(1);
                    break;
                case 197:
                    this.L.or(1);
                    break;
                case 198:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) | 1);
                    break;
                case 199:
                    this.A.or(1);
                    break;
                case 200:
                    this.B.or(2);
                    break;
                case 201:
                    this.C.or(2);
                    break;
                case 202:
                    this.D.or(2);
                    break;
                case 203:
                    this.E.or(2);
                    break;
                case 204:
                    this.H.or(2);
                    break;
                case 205:
                    this.L.or(2);
                    break;
                case 206:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) | 2);
                    break;
                case 207:
                    this.A.or(2);
                    break;
                case 208:
                    this.B.or(4);
                    break;
                case 209:
                    this.C.or(4);
                    break;
                case 210:
                    this.D.or(4);
                    break;
                case 211:
                    this.E.or(4);
                    break;
                case 212:
                    this.H.or(4);
                    break;
                case 213:
                    this.L.or(4);
                    break;
                case 214:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) | 4);
                    break;
                case 215:
                    this.A.or(4);
                    break;
                case 216:
                    this.B.or(8);
                    break;
                case 217:
                    this.C.or(8);
                    break;
                case 218:
                    this.D.or(8);
                    break;
                case 219:
                    this.E.or(8);
                    break;
                case 220:
                    this.H.or(8);
                    break;
                case 221:
                    this.L.or(8);
                    break;
                case 222:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) | 8);
                    break;
                case 223:
                    this.A.or(8);
                    break;
                case 224:
                    this.B.or(16);
                    break;
                case 225:
                    this.C.or(16);
                    break;
                case 226:
                    this.D.or(16);
                    break;
                case 227:
                    this.E.or(16);
                    break;
                case 228:
                    this.H.or(16);
                    break;
                case 229:
                    this.L.or(16);
                    break;
                case 230:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) | 16);
                    break;
                case 231:
                    this.A.or(16);
                    break;
                case 232:
                    this.B.or(32);
                    break;
                case 233:
                    this.C.or(32);
                    break;
                case 234:
                    this.D.or(32);
                    break;
                case 235:
                    this.E.or(32);
                    break;
                case 236:
                    this.H.or(32);
                    break;
                case 237:
                    this.L.or(32);
                    break;
                case 238:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) | 32);
                    break;
                case 239:
                    this.A.or(32);
                    break;
                case 240:
                    this.B.or(64);
                    break;
                case 241:
                    this.C.or(64);
                    break;
                case 242:
                    this.D.or(64);
                    break;
                case 243:
                    this.E.or(64);
                    break;
                case 244:
                    this.H.or(64);
                    break;
                case 245:
                    this.L.or(64);
                    break;
                case 246:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) | 64);
                    break;
                case 247:
                    this.A.or(64);
                    break;
                case 248:
                    this.B.or(128);
                    break;
                case 249:
                    this.C.or(128);
                    break;
                case 250:
                    this.D.or(128);
                    break;
                case 251:
                    this.E.or(128);
                    break;
                case 252:
                    this.H.or(128);
                    break;
                case 253:
                    this.L.or(128);
                    break;
                case 254:
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, this.machine.readbyte(this.HL.word) | 128);
                    break;
                case 255:
                    this.A.or(128);
                    break;
            }
        }

        private do_opcode_ED(opcode2 : number) {
            switch((opcode2)) {
                case 64:
                    this.tstates += 1;
                    this.IN(this.B, this.BC);
                    break;
                case 65:
                    this.tstates += 1;
                    this.OUT(this.BC, this.B);
                    break;
                case 66:
                    this.tstates += 7;
                    this.SBC16(this.BC.word);
                    break;
                case 67:
                    this.LD16_NNRR(this.BC.word);
                    break;
                case 68:
                case 76:
                case 84:
                case 92:
                case 100:
                case 108:
                case 116:
                case 124:
                {
                    let bytetemp : number = this.A.value;
                    this.A.set(0);
                    this.SUB(bytetemp);
                };
                    break;
                case 69:
                case 77:
                case 85:
                case 93:
                case 101:
                case 109:
                case 117:
                case 125:
                    this.IFF1 = this.IFF2;
                    this.RET();
                    break;
                case 70:
                case 78:
                case 102:
                case 110:
                    this.IM = 0;
                    break;
                case 71:
                    this.tstates += 1;
                    this.I = this.A.value;
                    break;
                case 72:
                    this.tstates += 1;
                    this.IN(this.C, this.BC);
                    break;
                case 73:
                    this.tstates += 1;
                    this.OUT(this.BC, this.C);
                    break;
                case 74:
                    this.tstates += 7;
                    this.ADC16(this.BC.word);
                    break;
                case 75:
                    this.BC.set(this.LD16_RRNN());
                    break;
                case 79:
                    this.tstates += 1;
                    this.R = this.R7 = this.A.value;
                    break;
                case 80:
                    this.tstates += 1;
                    this.IN(this.D, this.BC);
                    break;
                case 81:
                    this.tstates += 1;
                    this.OUT(this.BC, this.D);
                    break;
                case 82:
                    this.tstates += 7;
                    this.SBC16(this.DE.word);
                    break;
                case 83:
                    this.LD16_NNRR(this.DE.word);
                    break;
                case 86:
                case 118:
                    this.IM = 1;
                    break;
                case 87:
                    this.tstates += 1;
                    this.A.set(this.I);
                    this.F.set((this.F.value & Z80.FLAG_C) | Z80.sz53_table_$LI$()[this.A.value] | (this.IFF2 !== 0?Z80.FLAG_V_$LI$():0));
                    break;
                case 88:
                    this.tstates += 1;
                    this.IN(this.E, this.BC);
                    break;
                case 89:
                    this.tstates += 1;
                    this.OUT(this.BC, this.E);
                    break;
                case 90:
                    this.tstates += 7;
                    this.ADC16(this.DE.word);
                    break;
                case 91:
                    this.DE.set(this.LD16_RRNN());
                    break;
                case 94:
                case 126:
                    this.IM = 2;
                    break;
                case 95:
                    this.tstates += 1;
                    this.A.set((this.R & 127) | (this.R7 & 128));
                    this.F.set((this.F.value & Z80.FLAG_C) | Z80.sz53_table_$LI$()[this.A.value] | (this.IFF2 !== 0?Z80.FLAG_V_$LI$():0));
                    break;
                case 96:
                    this.tstates += 1;
                    this.IN(this.H, this.BC);
                    break;
                case 97:
                    this.tstates += 1;
                    this.OUT(this.BC, this.H);
                    break;
                case 98:
                    this.tstates += 7;
                    this.SBC16(this.HL.word);
                    break;
                case 99:
                    this.LD16_NNRR(this.HL.word);
                    break;
                case 103:
                {
                    let bytetemp : number = this.machine.readbyte(this.HL.word);
                    this.contend(this.HL, 7);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, (this.A.value << 4) | (bytetemp >> 4));
                    this.A.set((this.A.value & 240) | (bytetemp & 15));
                    this.F.set((this.F.value & Z80.FLAG_C) | Z80.sz53p_table_$LI$()[this.A.value]);
                };
                    break;
                case 104:
                    this.tstates += 1;
                    this.IN(this.L, this.BC);
                    break;
                case 105:
                    this.tstates += 1;
                    this.OUT(this.BC, this.L);
                    break;
                case 106:
                    this.tstates += 7;
                    this.ADC16(this.HL.word);
                    break;
                case 107:
                    this.HL.set(this.LD16_RRNN());
                    break;
                case 111:
                {
                    let bytetemp : number = this.machine.readbyte(this.HL.word);
                    this.contend(this.HL, 7);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, (bytetemp << 4) | (this.A.value & 15));
                    this.A.set((this.A.value & 240) | (bytetemp >> 4));
                    this.F.set((this.F.value & Z80.FLAG_C) | Z80.sz53p_table_$LI$()[this.A.value]);
                };
                    break;
                case 112:
                    this.tstates += 1;
                {
                    let bytetemp : number = 0;
                    this.IN(bytetemp, this.BC);
                };
                    break;
                case 113:
                    this.tstates += 1;
                    this.OUT(this.BC, 0);
                    break;
                case 114:
                    this.tstates += 7;
                    this.SBC16(this.SP);
                    break;
                case 115:
                    this.LD16_NNRR(this.SP);
                    break;
                case 120:
                    this.tstates += 1;
                    this.IN(this.A, this.BC);
                    break;
                case 121:
                    this.tstates += 1;
                    this.OUT(this.BC, this.A);
                    break;
                case 122:
                    this.tstates += 7;
                    this.ADC16(this.SP);
                    break;
                case 123:
                    this.SP = this.LD16_RRNN();
                    break;
                case 160:
                {
                    let bytetemp : number = this.machine.readbyte(this.HL.word);
                    this.contend(this.HL, 3);
                    this.contend(this.DE, 3);
                    this.contend(this.DE, 1);
                    this.contend(this.DE, 1);
                    this.BC.dec();
                    this.machine.writebyte(this.DE.word, bytetemp);
                    this.DE.inc();
                    this.HL.inc();
                    bytetemp += this.A.value;
                    this.F.set((this.F.value & (Z80.FLAG_C | Z80.FLAG_Z | Z80.FLAG_S)) | (this.BC.word !== 0?Z80.FLAG_V_$LI$():0) | (bytetemp & Z80.FLAG_3) | ((bytetemp & 2) !== 0?Z80.FLAG_5:0));
                };
                    break;
                case 161:
                {
                    let value : number = this.machine.readbyte(this.HL.word);
                    let bytetemp : number = this.A.value - value;
                    let lookup : number = ((this.A.value & 8) >> 3) | (((value) & 8) >> 2) | ((bytetemp & 8) >> 1);
                    this.contend(this.HL, 3);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.HL.inc();
                    this.BC.dec();
                    this.F.set((this.F.value & Z80.FLAG_C) | (this.BC.word !== 0?(Z80.FLAG_V_$LI$() | Z80.FLAG_N):Z80.FLAG_N) | Z80.halfcarry_sub_table_$LI$()[lookup] | (bytetemp !== 0?0:Z80.FLAG_Z) | (bytetemp & Z80.FLAG_S));
                    if((this.F.value & Z80.FLAG_H) !== 0) bytetemp--;
                    this.F.or((bytetemp & Z80.FLAG_3) | ((bytetemp & 2) !== 0?Z80.FLAG_5:0));
                };
                    break;
                case 162:
                {
                    let initemp : number = this.machine.readport(this.BC.word);
                    this.tstates += 2;
                    this.contend_io(this.BC, 3);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, initemp);
                    this.B.dec();
                    this.HL.inc();
                    this.F.set(((initemp & 128) !== 0?Z80.FLAG_N:0) | Z80.sz53_table_$LI$()[this.B.get()]);
                };
                    break;
                case 163:
                {
                    let outitemp : number = this.machine.readbyte(this.HL.word);
                    this.B.dec();
                    this.tstates++;
                    this.contend(this.HL, 4);
                    this.contend_io(this.BC, 3);
                    this.HL.inc();
                    this.machine.writeport(this.BC.word, outitemp);
                    this.F.set(((outitemp & 128) !== 0?Z80.FLAG_N:0) | Z80.sz53_table_$LI$()[this.B.get()]);
                };
                    break;
                case 168:
                {
                    let bytetemp : number = this.machine.readbyte(this.HL.word);
                    this.contend(this.HL, 3);
                    this.contend(this.DE, 3);
                    this.contend(this.DE, 1);
                    this.contend(this.DE, 1);
                    this.BC.dec();
                    this.machine.writebyte(this.DE.word, bytetemp);
                    this.DE.dec();
                    this.HL.dec();
                    bytetemp += this.A.value;
                    this.F.set((this.F.value & (Z80.FLAG_C | Z80.FLAG_Z | Z80.FLAG_S)) | (this.BC.word !== 0?Z80.FLAG_V_$LI$():0) | (bytetemp & Z80.FLAG_3) | ((bytetemp & 2) !== 0?Z80.FLAG_5:0));
                };
                    break;
                case 169:
                {
                    let value : number = this.machine.readbyte(this.HL.word);
                    let bytetemp : number = this.A.value - value;
                    let lookup : number = ((this.A.value & 8) >> 3) | (((value) & 8) >> 2) | ((bytetemp & 8) >> 1);
                    this.contend(this.HL, 3);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.HL.dec();
                    this.BC.dec();
                    this.F.set((this.F.value & Z80.FLAG_C) | (this.BC.word !== 0?(Z80.FLAG_V_$LI$() | Z80.FLAG_N):Z80.FLAG_N) | Z80.halfcarry_sub_table_$LI$()[lookup] | (bytetemp !== 0?0:Z80.FLAG_Z) | (bytetemp & Z80.FLAG_S));
                    if((this.F.value & Z80.FLAG_H) !== 0) bytetemp--;
                    this.F.or((bytetemp & Z80.FLAG_3) | ((bytetemp & 2) !== 0?Z80.FLAG_5:0));
                };
                    break;
                case 170:
                {
                    let initemp : number = this.machine.readport(this.BC.word);
                    this.tstates += 2;
                    this.contend_io(this.BC, 3);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, initemp);
                    this.B.dec();
                    this.HL.dec();
                    this.F.set(((initemp & 128) !== 0?Z80.FLAG_N:0) | Z80.sz53_table_$LI$()[this.B.get()]);
                };
                    break;
                case 171:
                {
                    let outitemp : number = this.machine.readbyte(this.HL.word);
                    this.B.dec();
                    this.tstates++;
                    this.contend(this.HL, 4);
                    this.contend_io(this.BC, 3);
                    this.HL.dec();
                    this.machine.writeport(this.BC.word, outitemp);
                    this.F.set(((outitemp & 128) !== 0?Z80.FLAG_N:0) | Z80.sz53_table_$LI$()[this.B.get()]);
                };
                    break;
                case 176:
                {
                    let bytetemp : number = this.machine.readbyte(this.HL.word);
                    this.contend(this.HL, 3);
                    this.contend(this.DE, 3);
                    this.contend(this.DE, 1);
                    this.contend(this.DE, 1);
                    this.machine.writebyte(this.DE.word, bytetemp);
                    this.HL.inc();
                    this.DE.inc();
                    this.BC.dec();
                    bytetemp += this.A.value;
                    this.F.set((this.F.value & (Z80.FLAG_C | Z80.FLAG_Z | Z80.FLAG_S)) | (this.BC.word !== 0?Z80.FLAG_V_$LI$():0) | (bytetemp & Z80.FLAG_3) | ((bytetemp & 2) !== 0?Z80.FLAG_5:0));
                    if(this.BC.word !== 0) {
                        for(let i : number = 0; i < 5; ++i) this.contend(this.DE, 1)
                        this.PC -= 2;
                    }
                };
                    break;
                case 177:
                {
                    let value : number = this.machine.readbyte(this.HL.word);
                    let bytetemp : number = this.A.value - value;
                    let lookup : number = ((this.A.value & 8) >> 3) | (((value) & 8) >> 2) | ((bytetemp & 8) >> 1);
                    this.contend(this.HL, 3);
                    for(let i : number = 0; i < 5; ++i) this.contend(this.HL, 1)
                    this.HL.inc();
                    this.BC.dec();
                    this.F.set((this.F.value & Z80.FLAG_C) | (this.BC.word !== 0?(Z80.FLAG_V_$LI$() | Z80.FLAG_N):Z80.FLAG_N) | Z80.halfcarry_sub_table_$LI$()[lookup] | (bytetemp !== 0?0:Z80.FLAG_Z) | (bytetemp & Z80.FLAG_S));
                    if((this.F.value & Z80.FLAG_H) !== 0) bytetemp--;
                    this.F.or((bytetemp & Z80.FLAG_3) | ((bytetemp & 2) !== 0?Z80.FLAG_5:0));
                    if((this.F.value & (Z80.FLAG_V_$LI$() | Z80.FLAG_Z)) === Z80.FLAG_V_$LI$()) {
                        for(let i : number = 0; i < 5; ++i) this.contend(this.HL, 1)
                        this.PC -= 2;
                    }
                };
                    break;
                case 178:
                {
                    let initemp : number = this.machine.readport(this.BC.word);
                    this.tstates += 2;
                    this.contend_io(this.BC, 3);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, initemp);
                    this.B.dec();
                    this.HL.inc();
                    this.F.set(((initemp & 128) !== 0?Z80.FLAG_N:0) | Z80.sz53_table_$LI$()[this.B.get()]);
                    if(this.B.get() !== 0) {
                        for(let i : number = 0; i < 5; ++i) this.contend(this.HL, 1)
                        this.PC -= 2;
                    }
                };
                    break;
                case 179:
                {
                    let outitemp : number = this.machine.readbyte(this.HL.word);
                    this.tstates++;
                    this.contend(this.HL, 4);
                    this.B.dec();
                    this.HL.inc();
                    this.machine.writeport(this.BC.word, outitemp);
                    this.F.set(((outitemp & 128) !== 0?Z80.FLAG_N:0) | Z80.sz53_table_$LI$()[this.B.get()]);
                    if(this.B.get() !== 0) {
                        this.contend_io(this.BC, 1);
                        for(let i : number = 0; i < 6; ++i) this.contend(this.PC, 1)
                        this.contend(this.PC - 1, 1);
                        this.PC -= 2;
                    } else {
                        this.contend_io(this.BC, 3);
                    }
                };
                    break;
                case 184:
                {
                    let bytetemp : number = this.machine.readbyte(this.HL.word);
                    this.contend(this.HL, 3);
                    this.contend(this.DE, 3);
                    this.contend(this.DE, 1);
                    this.contend(this.DE, 1);
                    this.machine.writebyte(this.DE.word, bytetemp);
                    this.HL.dec();
                    this.DE.dec();
                    this.BC.dec();
                    bytetemp += this.A.value;
                    this.F.set((this.F.value & (Z80.FLAG_C | Z80.FLAG_Z | Z80.FLAG_S)) | (this.BC.word !== 0?Z80.FLAG_V_$LI$():0) | (bytetemp & Z80.FLAG_3) | ((bytetemp & 2) > 0?Z80.FLAG_5:0));
                    if(this.BC.word !== 0) {
                        for(let i : number = 0; i < 5; ++i) this.contend(this.DE, 1)
                        this.PC -= 2;
                    }
                };
                    break;
                case 185:
                {
                    let value : number = this.machine.readbyte(this.HL.word);
                    let bytetemp : number = this.A.value - value;
                    let lookup : number = ((this.A.value & 8) >> 3) | (((value) & 8) >> 2) | ((bytetemp & 8) >> 1);
                    this.contend(this.HL, 3);
                    for(let i : number = 0; i < 5; ++i) this.contend(this.HL, 1)
                    this.HL.dec();
                    this.BC.dec();
                    this.F.set((this.F.value & Z80.FLAG_C) | (this.BC.word !== 0?(Z80.FLAG_V_$LI$() | Z80.FLAG_N):Z80.FLAG_N) | Z80.halfcarry_sub_table_$LI$()[lookup] | (bytetemp !== 0?0:Z80.FLAG_Z) | (bytetemp & Z80.FLAG_S));
                    if((this.F.value & Z80.FLAG_H) !== 0) bytetemp--;
                    this.F.or((bytetemp & Z80.FLAG_3) | ((bytetemp & 2) !== 0?Z80.FLAG_5:0));
                    if((this.F.value & (Z80.FLAG_V_$LI$() | Z80.FLAG_Z)) === Z80.FLAG_V_$LI$()) {
                        for(let i : number = 0; i < 5; ++i) this.contend(this.HL, 1)
                        this.PC -= 2;
                    }
                };
                    break;
                case 186:
                {
                    let initemp : number = this.machine.readport(this.BC.word);
                    this.tstates += 2;
                    this.contend_io(this.BC, 3);
                    this.contend(this.HL, 3);
                    this.machine.writebyte(this.HL.word, initemp);
                    this.B.dec();
                    this.HL.dec();
                    this.F.set(((initemp & 128) !== 0?Z80.FLAG_N:0) | Z80.sz53_table_$LI$()[this.B.get()]);
                    if(this.B.get() !== 0) {
                        for(let i : number = 0; i < 5; ++i) this.contend(this.HL, 1)
                        this.PC -= 2;
                    }
                };
                    break;
                case 187:
                {
                    let outitemp : number = this.machine.readbyte(this.HL.word);
                    this.tstates++;
                    this.contend(this.HL, 4);
                    this.B.dec();
                    this.HL.dec();
                    this.machine.writeport(this.BC.word, outitemp);
                    this.F.set(((outitemp & 128) !== 0?Z80.FLAG_N:0) | Z80.sz53_table_$LI$()[this.B.get()]);
                    if(this.B.get() !== 0) {
                        this.contend_io(this.BC, 1);
                        for(let i : number = 0; i < 5; ++i) this.contend(this.PC, 1)
                        this.contend(this.PC - 1, 1);
                        this.PC -= 2;
                    } else {
                        this.contend_io(this.BC, 3);
                    }
                };
                    break;
                case 251:
                    break;
                default:
                    break;
            }
        }

        private do_opcode_DDFD(opcode2 : number, REGISTER : RegisterPair, REGISTERL : Register, REGISTERH : Register) {
            switch((opcode2)) {
                case 9:
                    this.ADD16(REGISTER, this.BC);
                    break;
                case 25:
                    this.ADD16(REGISTER, this.DE);
                    break;
                case 33:
                    this.contend(this.PC, 3);
                    REGISTERL.set(this.machine.readbyte(this.PC++));
                    this.contend(this.PC, 3);
                    REGISTERH.set(this.machine.readbyte(this.PC++));
                    break;
                case 34:
                    this.LD16_NNRR(REGISTER.get());
                    break;
                case 35:
                    this.tstates += 2;
                    REGISTER.inc();
                    break;
                case 36:
                    this.INC(REGISTERH);
                    break;
                case 37:
                    this.DEC(REGISTERH);
                    break;
                case 38:
                    this.contend(this.PC, 3);
                    REGISTERH.set(this.machine.readbyte(this.PC++));
                    break;
                case 41:
                    this.ADD16(REGISTER, REGISTER);
                    break;
                case 42:
                    REGISTER.set(this.LD16_RRNN());
                    break;
                case 43:
                    this.tstates += 2;
                    REGISTER.dec();
                    break;
                case 44:
                    this.INC(REGISTERL);
                    break;
                case 45:
                    this.DEC(REGISTERL);
                    break;
                case 46:
                    this.contend(this.PC, 3);
                    REGISTERL.set(this.machine.readbyte(this.PC++));
                    break;
                case 52:
                    this.tstates += 15;
                {
                    let dist : number = this.machine.readbyte(this.PC++);
                    dist = (dist < 128?dist:dist - 256);
                    let wordtemp : number = REGISTER.get() + dist;
                    this.tempreg.set(this.machine.readbyte(wordtemp));
                    let bytetemp : Register = this.tempreg;
                    this.INC(bytetemp);
                    this.machine.writebyte(wordtemp, bytetemp.get());
                };
                    break;
                case 53:
                    this.tstates += 15;
                {
                    let dist : number = this.machine.readbyte(this.PC++);
                    let wordtemp : number = REGISTER.get() + (dist < 128?dist:dist - 256);
                    this.tempreg.set(this.machine.readbyte(wordtemp));
                    let bytetemp : Register = this.tempreg;
                    this.DEC(bytetemp);
                    this.machine.writebyte(wordtemp, bytetemp.get());
                };
                    break;
                case 54:
                    this.tstates += 11;
                {
                    let dist : number = this.machine.readbyte(this.PC++);
                    let wordtemp : number = REGISTER.get() + (dist < 128?dist:dist - 256);
                    this.machine.writebyte(wordtemp, this.machine.readbyte(this.PC++));
                };
                    break;
                case 57:
                    this.ADD16(REGISTER, this.SP);
                    break;
                case 68:
                    this.B.set(REGISTERH);
                    break;
                case 69:
                    this.B.set(REGISTERL);
                    break;
                case 70:
                    this.tstates += 11;
                    let dist : number = this.machine.readbyte(this.PC++);
                    this.B.set(this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256)));
                    break;
                case 76:
                    this.C.set(REGISTERH);
                    break;
                case 77:
                    this.C.set(REGISTERL);
                    break;
                case 78:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.C.set(this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256)));
                    break;
                case 84:
                    this.D.set(REGISTERH);
                    break;
                case 85:
                    this.D.set(REGISTERL);
                    break;
                case 86:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.D.set(this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256)));
                    break;
                case 92:
                    this.E.set(REGISTERH);
                    break;
                case 93:
                    this.E.set(REGISTERL);
                    break;
                case 94:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.E.set(this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256)));
                    break;
                case 96:
                    REGISTERH.set(this.B);
                    break;
                case 97:
                    REGISTERH.set(this.C);
                    break;
                case 98:
                    REGISTERH.set(this.D);
                    break;
                case 99:
                    REGISTERH.set(this.E);
                    break;
                case 100:
                    break;
                case 101:
                    REGISTERH.set(REGISTERL);
                    break;
                case 102:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.H.set(this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256)));
                    break;
                case 103:
                    REGISTERH.set(this.A);
                    break;
                case 104:
                    REGISTERL.set(this.B);
                    break;
                case 105:
                    REGISTERL.set(this.C);
                    break;
                case 106:
                    REGISTERL.set(this.D);
                    break;
                case 107:
                    REGISTERL.set(this.E);
                    break;
                case 108:
                    REGISTERL.set(REGISTERH);
                    break;
                case 109:
                    break;
                case 110:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.L.set(this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256)));
                    break;
                case 111:
                    REGISTERL.set(this.A);
                    break;
                case 112:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.machine.writebyte(REGISTER.get() + (dist < 128?dist:dist - 256), this.B.get());
                    break;
                case 113:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.machine.writebyte(REGISTER.get() + (dist < 128?dist:dist - 256), this.C.get());
                    break;
                case 114:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.machine.writebyte(REGISTER.get() + (dist < 128?dist:dist - 256), this.D.get());
                    break;
                case 115:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.machine.writebyte(REGISTER.get() + (dist < 128?dist:dist - 256), this.E.get());
                    break;
                case 116:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.machine.writebyte(REGISTER.get() + (dist < 128?dist:dist - 256), this.H.get());
                    break;
                case 117:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.machine.writebyte(REGISTER.get() + (dist < 128?dist:dist - 256), this.L.get());
                    break;
                case 119:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.machine.writebyte(REGISTER.get() + (dist < 128?dist:dist - 256), this.A.value);
                    break;
                case 124:
                    this.A.set(REGISTERH);
                    break;
                case 125:
                    this.A.set(REGISTERL);
                    break;
                case 126:
                    this.tstates += 11;
                    dist = this.machine.readbyte(this.PC++);
                    this.A.set(this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256)));
                    break;
                case 132:
                    this.ADD(REGISTERH.get());
                    break;
                case 133:
                    this.ADD(REGISTERL.get());
                    break;
                case 134:
                    this.tstates += 11;
                {
                    dist = this.machine.readbyte(this.PC++);
                    let bytetemp : number = this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256));
                    this.ADD(bytetemp);
                };
                    break;
                case 140:
                    this.ADC(REGISTERH.get());
                    break;
                case 141:
                    this.ADC(REGISTERL.get());
                    break;
                case 142:
                    this.tstates += 11;
                {
                    dist = this.machine.readbyte(this.PC++);
                    let bytetemp : number = this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256));
                    this.ADC(bytetemp);
                };
                    break;
                case 148:
                    this.SUB(REGISTERH.get());
                    break;
                case 149:
                    this.SUB(REGISTERL.get());
                    break;
                case 150:
                    this.tstates += 11;
                {
                    dist = this.machine.readbyte(this.PC++);
                    let bytetemp : number = this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256));
                    this.SUB(bytetemp);
                };
                    break;
                case 156:
                    this.SBC(REGISTERH.get());
                    break;
                case 157:
                    this.SBC(REGISTERL.get());
                    break;
                case 158:
                    this.tstates += 11;
                {
                    dist = this.machine.readbyte(this.PC++);
                    let bytetemp : number = this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256));
                    this.SBC(bytetemp);
                };
                    break;
                case 164:
                    this.AND(REGISTERH.get());
                    break;
                case 165:
                    this.AND(REGISTERL.get());
                    break;
                case 166:
                    this.tstates += 11;
                {
                    dist = this.machine.readbyte(this.PC++);
                    let bytetemp : number = this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256));
                    this.AND(bytetemp);
                };
                    break;
                case 172:
                    this.XOR(REGISTERH);
                    break;
                case 173:
                    this.XOR(REGISTERL);
                    break;
                case 174:
                    this.tstates += 11;
                {
                    dist = this.machine.readbyte(this.PC++);
                    let bytetemp : number = this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256));
                    this.XOR(bytetemp);
                };
                    break;
                case 180:
                    this.OR(REGISTERH);
                    break;
                case 181:
                    this.OR(REGISTERL);
                    break;
                case 182:
                    this.tstates += 11;
                {
                    dist = this.machine.readbyte(this.PC++);
                    let bytetemp : number = this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256));
                    this.OR(bytetemp);
                };
                    break;
                case 188:
                    this.CP(REGISTERH.get());
                    break;
                case 189:
                    this.CP(REGISTERL.get());
                    break;
                case 190:
                    this.tstates += 11;
                {
                    dist = this.machine.readbyte(this.PC++);
                    let bytetemp : number = this.machine.readbyte(REGISTER.get() + (dist < 128?dist:dist - 256));
                    this.CP(bytetemp);
                };
                    break;
                case 203:
                {
                    this.contend(this.PC, 3);
                    dist = this.machine.readbyte(this.PC++);
                    let tempaddr : number = REGISTER.get() + (dist < 128?dist:dist - 256);
                    this.contend(this.PC, 4);
                    let opcode3 : number = this.machine.opcode_fetch(this.PC++);
                    this.do_opcode_DDFDCB(opcode3, tempaddr);
                };
                    break;
                case 225:
                    REGISTER.set(this.POP16());
                    break;
                case 227:
                {
                    let SPvalue : number = this.SP;
                    let bytetempl : number = this.machine.readbyte(SPvalue);
                    let bytetemph : number = this.machine.readbyte(SPvalue + 1);
                    this.contend(SPvalue, 3);
                    this.contend(SPvalue + 1, 4);
                    this.machine.writebyte(SPvalue, REGISTERL.get());
                    this.machine.writebyte(SPvalue + 1, REGISTERH.get());
                    this.contend(SPvalue, 3);
                    this.contend(SPvalue + 1, 5);
                    REGISTERL.set(bytetempl);
                    REGISTERH.set(bytetemph);
                };
                    break;
                case 229:
                    this.tstates++;
                    this.PUSH16(REGISTERL.get() + (REGISTERH.get() << 8));
                    break;
                case 233:
                    this.PC = REGISTER.get();
                    break;
                case 249:
                    this.tstates += 2;
                    this.SP = REGISTER.get();
                    break;
                default:
                    this.PC--;
                    this.R = (this.R - 1) & 255;
                    break;
            }
        }

        private do_opcode_DDFDCB(opcode3 : number, tempaddr : number) {
            switch((opcode3)) {
                case 0:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr));
                    this.RLC(this.B);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 1:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr));
                    this.RLC(this.C);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 2:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr));
                    this.RLC(this.D);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 3:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr));
                    this.RLC(this.E);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 4:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr));
                    this.RLC(this.H);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 5:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr));
                    this.RLC(this.L);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 6:
                    this.tstates += 8;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    let bytetemp : Register = this.tempreg;
                    this.RLC(bytetemp);
                    this.machine.writebyte(tempaddr, bytetemp.get());
                };
                    break;
                case 7:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr));
                    this.RLC(this.A);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 8:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr));
                    this.RRC(this.B);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 9:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr));
                    this.RRC(this.C);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 10:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr));
                    this.RRC(this.D);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 11:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr));
                    this.RRC(this.E);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 12:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr));
                    this.RRC(this.H);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 13:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr));
                    this.RRC(this.L);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 14:
                    this.tstates += 8;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    let bytetemp : Register = this.tempreg;
                    this.RRC(bytetemp);
                    this.machine.writebyte(tempaddr, bytetemp.get());
                };
                    break;
                case 15:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr));
                    this.RRC(this.A);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 16:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr));
                    this.RL(this.B);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 17:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr));
                    this.RL(this.C);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 18:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr));
                    this.RL(this.D);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 19:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr));
                    this.RL(this.E);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 20:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr));
                    this.RL(this.H);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 21:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr));
                    this.RL(this.L);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 22:
                    this.tstates += 8;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    let bytetemp : Register = this.tempreg;
                    this.RL(bytetemp);
                    this.machine.writebyte(tempaddr, bytetemp.get());
                };
                    break;
                case 23:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr));
                    this.RL(this.A);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 24:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr));
                    this.RR(this.B);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 25:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr));
                    this.RR(this.C);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 26:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr));
                    this.RR(this.D);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 27:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr));
                    this.RR(this.E);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 28:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr));
                    this.RR(this.H);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 29:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr));
                    this.RR(this.L);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 30:
                    this.tstates += 8;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    let bytetemp : Register = this.tempreg;
                    this.RR(bytetemp);
                    this.machine.writebyte(tempaddr, bytetemp.get());
                };
                    break;
                case 31:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr));
                    this.RR(this.A);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 32:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr));
                    this.SLA(this.B);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 33:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr));
                    this.SLA(this.C);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 34:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr));
                    this.SLA(this.D);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 35:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr));
                    this.SLA(this.E);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 36:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr));
                    this.SLA(this.H);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 37:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr));
                    this.SLA(this.L);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 38:
                    this.tstates += 8;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    let bytetemp : Register = this.tempreg;
                    this.SLA(bytetemp);
                    this.machine.writebyte(tempaddr, bytetemp.get());
                };
                    break;
                case 39:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr));
                    this.SLA(this.A);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 40:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr));
                    this.SRA(this.B);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 41:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr));
                    this.SRA(this.C);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 42:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr));
                    this.SRA(this.D);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 43:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr));
                    this.SRA(this.E);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 44:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr));
                    this.SRA(this.H);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 45:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr));
                    this.SRA(this.L);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 46:
                    this.tstates += 8;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    let bytetemp : Register = this.tempreg;
                    this.SRA(bytetemp);
                    this.machine.writebyte(tempaddr, bytetemp.get());
                };
                    break;
                case 47:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr));
                    this.SRA(this.A);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 48:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr));
                    this.SLL(this.B);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 49:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr));
                    this.SLL(this.C);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 50:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr));
                    this.SLL(this.D);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 51:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr));
                    this.SLL(this.E);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 52:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr));
                    this.SLL(this.H);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 53:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr));
                    this.SLL(this.L);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 54:
                    this.tstates += 8;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    let bytetemp : Register = this.tempreg;
                    this.SLL(bytetemp);
                    this.machine.writebyte(tempaddr, bytetemp.get());
                };
                    break;
                case 55:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr));
                    this.SLL(this.A);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 56:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr));
                    this.SRL(this.B);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 57:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr));
                    this.SRL(this.C);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 58:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr));
                    this.SRL(this.D);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 59:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr));
                    this.SRL(this.E);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 60:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr));
                    this.SRL(this.H);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 61:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr));
                    this.SRL(this.L);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 62:
                    this.tstates += 8;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    let bytetemp : Register = this.tempreg;
                    this.SRL(bytetemp);
                    this.machine.writebyte(tempaddr, bytetemp.get());
                };
                    break;
                case 63:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr));
                    this.SRL(this.A);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 64:
                case 65:
                case 66:
                case 67:
                case 68:
                case 69:
                case 70:
                case 71:
                    this.tstates += 5;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    this.BIT(0, this.tempreg);
                };
                    break;
                case 72:
                case 73:
                case 74:
                case 75:
                case 76:
                case 77:
                case 78:
                case 79:
                    this.tstates += 5;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    this.BIT(1, this.tempreg);
                };
                    break;
                case 80:
                case 81:
                case 82:
                case 83:
                case 84:
                case 85:
                case 86:
                case 87:
                    this.tstates += 5;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    this.BIT(2, this.tempreg);
                };
                    break;
                case 88:
                case 89:
                case 90:
                case 91:
                case 92:
                case 93:
                case 94:
                case 95:
                    this.tstates += 5;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    this.BIT(3, this.tempreg);
                };
                    break;
                case 96:
                case 97:
                case 98:
                case 99:
                case 100:
                case 101:
                case 102:
                case 103:
                    this.tstates += 5;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    this.BIT(4, this.tempreg);
                };
                    break;
                case 104:
                case 105:
                case 106:
                case 107:
                case 108:
                case 109:
                case 110:
                case 111:
                    this.tstates += 5;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    this.BIT(5, this.tempreg);
                };
                    break;
                case 112:
                case 113:
                case 114:
                case 115:
                case 116:
                case 117:
                case 118:
                case 119:
                    this.tstates += 5;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    this.BIT(6, this.tempreg);
                };
                    break;
                case 120:
                case 121:
                case 122:
                case 123:
                case 124:
                case 125:
                case 126:
                case 127:
                    this.tstates += 5;
                {
                    this.tempreg.set(this.machine.readbyte(tempaddr));
                    this.BIT7(this.tempreg);
                };
                    break;
                case 128:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) & 254);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 129:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) & 254);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 130:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) & 254);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 131:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) & 254);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 132:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) & 254);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 133:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) & 254);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 134:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) & 254);
                    break;
                case 135:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) & 254);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 136:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) & 253);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 137:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) & 253);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 138:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) & 253);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 139:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) & 253);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 140:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) & 253);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 141:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) & 253);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 142:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) & 253);
                    break;
                case 143:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) & 253);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 144:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) & 251);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 145:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) & 251);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 146:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) & 251);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 147:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) & 251);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 148:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) & 251);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 149:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) & 251);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 150:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) & 251);
                    break;
                case 151:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) & 251);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 152:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) & 247);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 153:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) & 247);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 154:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) & 247);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 155:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) & 247);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 156:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) & 247);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 157:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) & 247);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 158:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) & 247);
                    break;
                case 159:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) & 247);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 160:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) & 239);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 161:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) & 239);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 162:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) & 239);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 163:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) & 239);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 164:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) & 239);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 165:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) & 239);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 166:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) & 239);
                    break;
                case 167:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) & 239);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 168:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) & 223);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 169:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) & 223);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 170:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) & 223);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 171:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) & 223);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 172:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) & 223);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 173:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) & 223);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 174:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) & 223);
                    break;
                case 175:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) & 223);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 176:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) & 191);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 177:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) & 191);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 178:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) & 191);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 179:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) & 191);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 180:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) & 191);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 181:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) & 191);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 182:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) & 191);
                    break;
                case 183:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) & 191);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 184:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) & 127);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 185:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) & 127);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 186:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) & 127);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 187:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) & 127);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 188:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) & 127);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 189:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) & 127);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 190:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) & 127);
                    break;
                case 191:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) & 127);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 192:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) | 1);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 193:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) | 1);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 194:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) | 1);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 195:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) | 1);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 196:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) | 1);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 197:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) | 1);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 198:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) | 1);
                    break;
                case 199:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) | 1);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 200:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) | 2);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 201:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) | 2);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 202:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) | 2);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 203:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) | 2);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 204:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) | 2);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 205:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) | 2);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 206:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) | 2);
                    break;
                case 207:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) | 2);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 208:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) | 4);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 209:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) | 4);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 210:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) | 4);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 211:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) | 4);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 212:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) | 4);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 213:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) | 4);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 214:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) | 4);
                    break;
                case 215:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) | 4);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 216:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) | 8);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 217:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) | 8);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 218:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) | 8);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 219:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) | 8);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 220:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) | 8);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 221:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) | 8);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 222:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) | 8);
                    break;
                case 223:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) | 8);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 224:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) | 16);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 225:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) | 16);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 226:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) | 16);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 227:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) | 16);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 228:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) | 16);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 229:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) | 16);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 230:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) | 16);
                    break;
                case 231:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) | 16);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 232:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) | 32);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 233:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) | 32);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 234:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) | 32);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 235:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) | 32);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 236:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) | 32);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 237:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) | 32);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 238:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) | 32);
                    break;
                case 239:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) | 32);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 240:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) | 64);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 241:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) | 64);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 242:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) | 64);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 243:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) | 64);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 244:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) | 64);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 245:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) | 64);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 246:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) | 64);
                    break;
                case 247:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) | 64);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
                case 248:
                    this.tstates += 8;
                    this.B.set(this.machine.readbyte(tempaddr) | 128);
                    this.machine.writebyte(tempaddr, this.B.get());
                    break;
                case 249:
                    this.tstates += 8;
                    this.C.set(this.machine.readbyte(tempaddr) | 128);
                    this.machine.writebyte(tempaddr, this.C.get());
                    break;
                case 250:
                    this.tstates += 8;
                    this.D.set(this.machine.readbyte(tempaddr) | 128);
                    this.machine.writebyte(tempaddr, this.D.get());
                    break;
                case 251:
                    this.tstates += 8;
                    this.E.set(this.machine.readbyte(tempaddr) | 128);
                    this.machine.writebyte(tempaddr, this.E.get());
                    break;
                case 252:
                    this.tstates += 8;
                    this.H.set(this.machine.readbyte(tempaddr) | 128);
                    this.machine.writebyte(tempaddr, this.H.get());
                    break;
                case 253:
                    this.tstates += 8;
                    this.L.set(this.machine.readbyte(tempaddr) | 128);
                    this.machine.writebyte(tempaddr, this.L.get());
                    break;
                case 254:
                    this.tstates += 8;
                    this.machine.writebyte(tempaddr, this.machine.readbyte(tempaddr) | 128);
                    break;
                case 255:
                    this.tstates += 8;
                    this.A.set(this.machine.readbyte(tempaddr) | 128);
                    this.machine.writebyte(tempaddr, this.A.value);
                    break;
            }
        }
    }
    Z80["__class"] = "zx81emulator.z80.Z80";

}


zx81emulator.z80.Z80.sz53p_table_$LI$();

zx81emulator.z80.Z80.parity_table_$LI$();

zx81emulator.z80.Z80.sz53_table_$LI$();

zx81emulator.z80.Z80.overflow_sub_table_$LI$();

zx81emulator.z80.Z80.overflow_add_table_$LI$();

zx81emulator.z80.Z80.halfcarry_sub_table_$LI$();

zx81emulator.z80.Z80.halfcarry_add_table_$LI$();

zx81emulator.z80.Z80.FLAG_V_$LI$();

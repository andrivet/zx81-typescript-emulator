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

import Machine from "../machine/Machine";
import {MasterRegister, Register, Value8} from "./Register";
import {MasterRegisterPair, RegisterPair, SlaveRegisterPair} from "./RegisterPair";

const FLAG_C = 0x01;
const FLAG_N = 0x02;
const FLAG_P = 0x04;
const FLAG_V = FLAG_P;
const FLAG_3 = 0x08;
const FLAG_H = 0x10;
const FLAG_5 = 0x20;
const FLAG_Z = 0x40;
const FLAG_S = 0x80;

export default class Z80
{
    /* Whether a half carry occured or not can be determined by looking at
     the 3rd bit of the two arguments and the result; these are hashed
     into this table in the form r12, where r is the 3rd bit of the
     result, 1 is the 3rd bit of the 1st argument and 2 is the
     third bit of the 2nd argument; the tables differ for add and subtract
     operations */
    private static halfCarryAdd: number[] = [0, FLAG_H, FLAG_H, FLAG_H, 0, 0, 0, FLAG_H];
    private static halfCarrySub: number[] = [0, 0, FLAG_H, 0, FLAG_H, 0, FLAG_H, FLAG_H];

    /* Similarly, overflow can be determined by looking at the 7th bits; again
     the hash into this table is r12 */
    private static overflowAdd: number[] =  [0, 0, 0, FLAG_V, FLAG_V, 0, 0, 0];
    private static overflowSub: number[] = [0, FLAG_V, 0, 0, 0, 0, FLAG_V, 0];

    /* Some more tables; initialised in z80_init_tables() */
    private static sz53: number[] = new Array(0x100);     /* The S, Z, 5 and 3 bits of the lookup value */
    private static parity: number[] = new Array(0x100);   /* The parity of the lookup value */
    private static sz53p: number[] = new Array(0x100);    /* OR the above two tables together */

    private machine: Machine;

    public AF: RegisterPair = new SlaveRegisterPair();
    public BC: MasterRegisterPair = new MasterRegisterPair();
    public DE: MasterRegisterPair = new MasterRegisterPair();
    public HL: MasterRegisterPair = new MasterRegisterPair();
    public IX: RegisterPair = new MasterRegisterPair();
    public IY: RegisterPair = new MasterRegisterPair();
    public A: MasterRegister = <MasterRegister>this.AF.getRH();
    public F: MasterRegister = <MasterRegister>this.AF.getRL();
    public B: Register = this.BC.getRH();
    public C: Register = this.BC.getRL();
    public D: Register = this.DE.getRH();
    public E: Register = this.DE.getRL();
    public H: Register = this.HL.getRH();
    public L: Register= this.HL.getRL();
    public IXH: Register = this.IX.getRH();
    public IXL: Register = this.IX.getRL();
    public IYH: Register = this.IY.getRH();
    public IYL: Register = this.IY.getRL();
    public SP = 0;
    public PC = 0;
    public AF_ = 0;
    public BC_ = 0;
    public DE_ = 0;
    public HL_ = 0;
    public I = 0;
    public R = 0;
    public R7 = 0;
    public IFF1 = 0;
    public IFF2 = 0;
    public IM = 0;
    public halted = 0;
    private tstates = 0;

    /* Set up the z80 emulation */
    public constructor(machine: Machine)
    {
        this.machine = machine;
        Z80.init();
    }

    /* Initalise the tables used to set flags */
    private static init()
    {
        for (let i = 0; i < 0x100; i++)
        {
            Z80.sz53[i] = i & (FLAG_3 | FLAG_5 | FLAG_S);
            let j = i;
            let parity = 0;
            for (let k = 0; k < 8; k++)
            {
                parity ^= j & 1;
                j >>= 1;
            }
            Z80.parity[i] = (parity !== 0 ? 0 : FLAG_P);
            Z80.sz53p[i] = Z80.sz53[i] | Z80.parity[i];
        }
        Z80.sz53[0] |= FLAG_Z;
        Z80.sz53p[0] |= FLAG_Z;
    }

    /* Reset the z80 */
    public reset()
    {
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

    /* Process a z80 maskable interrupt */
    public interrupt(ts: number): number
    {
        if (this.IFF1 !== 0)
        {
            if (this.halted !== 0)
            {
                this.PC++;
                this.halted = 0;
            }

            this.IFF1 = this.IFF2 = 0;

            this.machine.writeByte(--this.SP, this.PC >> 8);
            this.machine.writeByte(--this.SP, this.PC & 0xFF);

            this.R = (this.R + 1) & 0xFF;

            switch (this.IM)
            {
                case 0: this.PC = 0x0038; return (13);
                case 1: this.PC = 0x0038; return (13);
                case 2:
                {
                    let inttemp = (this.I << 8) | 0xFF;
                    this.PC = this.machine.readByte(inttemp++) + (this.machine.readByte(inttemp) << 8);
                    return (19);
                }
                default:
                    return (12);
            }
        }
        return (0);
    }

    /* Process a z80 non-maskable interrupt */
    public nmi(ts: number): number
    {
        let waitstates = 0;

        this.IFF1 = 0;

        if (this.halted !== 0)
        {
            this.halted = 0;
            this.PC++;

            waitstates = (ts / 2) - this.machine.tPerScanLine;
            waitstates = 4 - waitstates;
            if (waitstates < 0)
                waitstates = 0;
        }

        this.machine.writeByte(--this.SP, this.PC >> 8);
        this.machine.writeByte(--this.SP, this.PC & 0xFF);

        this.R = (this.R + 1) & 0xFF;
        this.PC = 0x0066;
        return (4 + waitstates);
    }

    /* Get the appropriate contended memory delay */
    private contend(address: number | RegisterPair, time: number): void
    {
        this.tstates += this.machine.contendMem((address instanceof RegisterPair) ? address.get() : address, this.tstates, time);
    }

    private contendIO(port: number | RegisterPair, time: number): void
    {
        this.tstates += this.machine.contendIO((port instanceof RegisterPair) ? port.get() : port, this.tstates, time);
    }

    public AND(value: Register | number): void
    {
        this.A.set(this.A.get() & (value instanceof Register ? value.get() : value));
        this.F.set(FLAG_H | Z80.sz53p[this.A.get()]);
    }

    private ADC(value: number)
    {
        const adctemp = this.A.get() + value + (this.F.get() & FLAG_C);
        const lookup = ((this.A.get() & 0x88) >> 3) | (((value) & 0x88) >> 2) | ((adctemp & 0x88) >> 1);
        this.A.set(adctemp);
        this.F.set(((adctemp & 0x100) > 0 ? FLAG_C : 0) | Z80.halfCarryAdd[lookup & 7] | Z80.overflowAdd[lookup >> 4] | Z80.sz53[this.A.get()]);
    }

    private ADC16(value: number)
    {
        const add16temp = this.HL.get() + value + (this.F.get() & FLAG_C);
        const lookup = ((this.HL.get() & 0x8800) >> 11) | ((value & 0x8800) >> 10) | ((add16temp & 0x8800) >> 9);
        this.HL.set(add16temp);
        this.F.set(((add16temp & 0x10000) > 0 ? FLAG_C : 0)
            | Z80.overflowAdd[lookup >> 4]
            | (this.H.get() & (FLAG_3 | FLAG_5 | FLAG_S))
            | Z80.halfCarryAdd[lookup & 7]
            | (this.HL.get() === 0 ? 0 : FLAG_Z));
    }

    private ADD(value: number)
    {
        const addtemp = this.A.get() + value;
        const lookup = ((this.A.get() & 0x88) >> 3) | (((value) & 0x88) >> 2) | ((addtemp & 0x88) >> 1);
        this.A.set(addtemp);
        this.F.set(((addtemp & 0x100) > 0 ? FLAG_C : 0) | Z80.halfCarryAdd[lookup & 7] | Z80.overflowAdd[lookup >> 4] | Z80.sz53[this.A.get()]);
    }

    public ADD16(rp1: RegisterPair, rp2: RegisterPair | number): void
    {
        const value2 = (rp2 instanceof RegisterPair) ? rp2.get() : rp2;
        const add16temp = rp1.get() + value2;
        const lookup = ((rp1.get() & 0x0800) >> 11) | ((value2 & 0x0800) >> 10) | ((add16temp & 0x0800) >> 9);
        this.tstates += 7;
        rp1.set(add16temp);
        this.F.set((this.F.get() & (FLAG_V | FLAG_Z | FLAG_S)) | ((add16temp & 0x10000) > 0 ? FLAG_C : 0) | ((add16temp >> 8) & (FLAG_3 | FLAG_5)) | Z80.halfCarryAdd[lookup]);
    }

    private BIT(bit: number, value: Register)
    {
        this.F.set((this.F.get() & FLAG_C) | (value.get() & (FLAG_3 | FLAG_5)) | ((value.get() & (1 << bit)) > 0 ? FLAG_H : (FLAG_P | FLAG_H | FLAG_Z)));
    }

    private BIT7(value: Register)
    {
        this.F.set((this.F.get() & FLAG_C) | (value.get() & (FLAG_3 | FLAG_5)) | ((value.get() & 0x80) > 0 ? (FLAG_H | FLAG_S) : (FLAG_P | FLAG_H | FLAG_Z)));
    }

    private CALL()
    {
        const calltempl = this.machine.readByte(this.PC++);
        this.contend(this.PC, 1);
        const calltemph = this.machine.readByte(this.PC++);
        this.PUSH16(this.PC);
        this.PC = calltempl + (calltemph << 8);
    }

    private CP(value: number)
    {
        const cptemp = this.A.get() - value;
        const lookup = ((this.A.get() & 0x88) >> 3) | (((value) & 0x88) >> 2) | ((cptemp & 0x88) >> 1);
        this.F.set(((cptemp & 0x100) > 0 ? FLAG_C : (cptemp > 0 ? 0 : FLAG_Z))
            | FLAG_N
            | Z80.halfCarrySub[lookup & 7]
            | Z80.overflowSub[lookup >> 4]
            | (value & (FLAG_3 | FLAG_5))
            | (cptemp & FLAG_S));
    }

    private DEC(reg: Register)
    {
        this.F.set((this.F.get() & FLAG_C) | ((reg.get() & 0x0f) > 0 ? 0 : FLAG_H) | FLAG_N);
        reg.dec();
        this.F.or((reg.get() === 0x7f ? FLAG_V : 0) | Z80.sz53[reg.get()]);
    }

    public IN(reg: Register | number, rp: RegisterPair | number): void
    {
        const value = reg instanceof Register ? reg.get() : reg;
        const port = (rp instanceof RegisterPair) ? rp.get() : rp;

        this.contendIO(port, 3);
        const result = this.machine.readPort(port);
        if(reg instanceof Register) reg.set(result);
        this.F.set((this.F.get() & FLAG_C) | Z80.sz53p[value]);
    }

    private INC(reg: Register)
    {
        reg.inc();
        this.F.set((this.F.get() & FLAG_C) | (reg.get() === 0x80 ? FLAG_V : 0) | ((reg.get() & 0x0f) > 0 ? 0 : FLAG_H) | Z80.sz53[reg.get()]);
    }

    private LD16_NNRR(value: number)
    {
        this.contend(this.PC, 3);
        let ldtemp = this.machine.readByte(this.PC++);
        this.contend(this.PC, 3);
        ldtemp |= this.machine.readByte(this.PC++) << 8;
        this.contend(ldtemp, 3);
        this.machine.writeByte(ldtemp++, value & 0xFF);
        this.contend(ldtemp, 3);
        this.machine.writeByte(ldtemp, value >> 8);
    }

    private LD16_RRNN(): number
    {
        this.contend(this.PC, 3);
        let ldtemp = this.machine.readByte(this.PC++);
        this.contend(this.PC, 3);
        ldtemp |= this.machine.readByte(this.PC++) << 8;
        this.contend(ldtemp, 3);
        this.contend(ldtemp, 3);
        return this.machine.readByte(ldtemp++) + (this.machine.readByte(ldtemp) << 8);
    }

    private JP()
    {
        let jptemp = this.PC;
        this.PC = this.machine.readByte(jptemp++) + (this.machine.readByte(jptemp) << 8);
    }

    private JR()
    {
        this.contend(this.PC, 1);
        this.contend(this.PC, 1);
        this.contend(this.PC, 1);
        this.contend(this.PC, 1);
        this.contend(this.PC, 1);
        let dist = this.machine.readByte(this.PC);
        dist = (dist < 0x80 ? dist : dist - 0x100);
        this.PC += dist;
    }

    public OR(r: Register | number): void
    {
        this.A.set(this.A.get() | (r instanceof Register ? r.get() : r));
        this.F.set(Z80.sz53p[this.A.get()]);
    }

    public OUT(rp: RegisterPair | number, reg: Register | number): void
    {
        const port = (rp instanceof RegisterPair) ? rp.get() : rp;
        const value = reg instanceof Register ? reg.get() : reg;
        this.contendIO(port, 3);
        this.machine.writePort(port, value);
    }

    private POP16(): number
    {
        this.contend(this.SP, 3);
        this.contend(this.SP, 3);
        return this.machine.readByte(this.SP++) + (this.machine.readByte(this.SP++) << 8);
    }

    private PUSH16(value: number)
    {
        this.SP--;
        this.contend(this.SP, 3);
        this.machine.writeByte(this.SP, value >> 8);
        this.SP--;
        this.contend(this.SP, 3);
        this.machine.writeByte(this.SP, value & 0xFF);
    }

    private RET()
    {
        this.PC = this.POP16();
    }

    private RL(reg: Register)
    {
        const rltemp = reg.get();
        reg.set((rltemp << 1) | (this.F.get() & FLAG_C));
        this.F.set((rltemp >> 7) | Z80.sz53p[reg.get()]);
    }

    private RLC(reg: Register)
    {
        const before = reg.get();
        const newValue = (before << 1) | (before >> 7);
        reg.set(newValue);
        const after = reg.get();
        this.F.set((after & FLAG_C) | Z80.sz53p[after]);
    }

    private RR(reg: Register)
    {
        const rrtemp = reg.get();
        reg.set((reg.get() >> 1) | (this.F.get() << 7));
        this.F.set((rrtemp & FLAG_C) | Z80.sz53p[reg.get()]);
    }

    private RRC(reg: Register)
    {
        this.F.set(reg.get() & FLAG_C);
        reg.set((reg.get() >> 1) | (reg.get() << 7));
        this.F.or(Z80.sz53p[reg.get()]);
    }

    private RST(value: number)
    {
        this.PUSH16(this.PC);
        this.PC = value;
    }

    private SBC(value: number)
    {
        const sbctemp = this.A.get() - (value) - (this.F.get() & FLAG_C);
        const lookup = ((this.A.get() & 0x88) >> 3) | (((value) & 0x88) >> 2) | ((sbctemp & 0x88) >> 1);
        this.A.set(sbctemp);
        this.F.set(((sbctemp & 0x100) > 0 ? FLAG_C : 0)
            | FLAG_N
            | Z80.halfCarrySub[lookup & 7]
            | Z80.overflowSub[lookup >> 4]
            | Z80.sz53[this.A.get()]);
    }

    private SBC16(value: number)
    {
        const sub16temp = this.HL.get() - (value) - (this.F.get() & FLAG_C);
        const lookup = ((this.HL.get() & 34816) >> 11) | (((value) & 34816) >> 10) | ((sub16temp & 34816) >> 9);
        this.HL.set(sub16temp);
        this.F.set(((sub16temp & 0x10000) > 0 ? FLAG_C : 0)
            | FLAG_N
            | Z80.overflowSub[lookup >> 4]
            | (this.H.get() & (FLAG_3 | FLAG_5 | FLAG_S))
            | Z80.halfCarrySub[lookup & 7]
            | (this.HL.get() > 0 ? 0 : FLAG_Z));
    }

    private SLA(reg: Register)
    {
        this.F.set(reg.get() >> 7);
        reg.set(reg.get() << 1);
        this.F.or(Z80.sz53p[reg.get()]);
    }

    private SLL(reg: Register)
    {
        this.F.set(reg.get() >> 7);
        reg.set((reg.get() << 1) | 1);
        this.F.or(Z80.sz53p[reg.get()]);
    }

    private SRA(reg: Register)
    {
        this.F.set(reg.get() & FLAG_C);
        reg.set((reg.get() & 0x80) | (reg.get() >> 1));
        this.F.or(Z80.sz53p[reg.get()]);
    }

    private SRL(reg: Register)
    {
        this.F.set(reg.get() & FLAG_C);
        reg.set(reg.get() >> 1);
        this.F.or(Z80.sz53p[reg.get()]);
    }

    private SUB(value: number)
    {
        const subtemp = this.A.get() - (value);
        const lookup = ((this.A.get() & 0x88) >> 3) | (((value) & 0x88) >> 2) | ((subtemp & 0x88) >> 1);
        this.A.set(subtemp);
        this.F.set(((subtemp & 0x100) > 0 ? FLAG_C : 0) | FLAG_N | Z80.halfCarrySub[lookup & 7] | Z80.overflowSub[lookup >> 4] | Z80.sz53[this.A.get()]);
    }

    public XOR(r: Register | number): void
    {
        const value = r instanceof Register ? r.get() : r;
        this.A.set(this.A.get() ^ value);
        this.F.set(Z80.sz53p[this.A.get()]);
    }

    public doOpcode(): number
    {
        this.tstates = 0;
        this.contend(this.PC, 4);
        this.R = (this.R + 1) & 0xFF;
        const opcode = this.machine.opcodeFetch(this.PC++);

        switch(opcode)
        {
            case 0x0:   // NOP
                break;
            case 0x01:  // LD BC,nnnn
                this.contend(this.PC, 3);
                this.C.set(this.machine.readByte(this.PC++));
                this.contend(this.PC, 3);
                this.B.set(this.machine.readByte(this.PC++));
                break;
            case 0x02:  // LD (BC),A
                this.contend(this.BC, 3);
                this.machine.writeByte(this.BC.get(), this.A.get());
                break;
            case 0x03:  // INC BC
                this.tstates += 2;
                this.BC.inc();
                break;
            case 0x04:  // INC B
                this.INC(this.B);
                break;
            case 0x05:  // DEC B
                this.DEC(this.B);
                break;
            case 0x06:  // LD B,nn
                this.contend(this.PC, 3);
                this.B.set(this.machine.readByte(this.PC++));
                break;
            case 0x07:  // RLCA
                this.A.set((this.A.get() << 1) | (this.A.get() >> 7));
                this.F.set((this.F.get() & (FLAG_P | FLAG_Z | FLAG_S)) | (this.A.get() & (FLAG_C | FLAG_3 | FLAG_5)));
                break;
            case 0x08:  // EX AF,AF'
                {
                    const wordtemp = this.AF.get();
                    this.AF.set(this.AF_);
                    this.AF_ = wordtemp;
                }
                break;
            case 0x09:  // ADD HL,BC
                this.ADD16(this.HL, this.BC);
                break;
            case 0x0A:  // LD A,(BC)
                this.contend(this.BC, 3);
                this.A.set(this.machine.readByte(this.BC.get()));
                break;
            case 0x0B:  // DEC BC
                this.tstates += 2;
                this.BC.dec();
                break;
            case 0x0C:  // INC C
                this.INC(this.C);
                break;
            case 0x0D:  // DEC C
                this.DEC(this.C);
                break;
            case 0x0E:  // LD C,nn
                this.contend(this.PC, 3);
                this.C.set(this.machine.readByte(this.PC++));
                break;
            case 0x0F:  // RRCA
                this.F.set((this.F.get() & (FLAG_P | FLAG_Z | FLAG_S)) | (this.A.get() & FLAG_C));
                this.A.set((this.A.get() >> 1) | (this.A.get() << 7));
                this.F.or(this.A.get() & (FLAG_3 | FLAG_5));
                break;
            case 0x10:  // DJNZ offset
                this.tstates++;
                this.contend(this.PC, 3);
                this.B.dec();
                if (this.B.get() !== 0)
                    this.JR();
                this.PC++;
                break;
            case 0x11:  // LD DE,nnnn
                this.contend(this.PC, 3);
                this.E.set(this.machine.readByte(this.PC++));
                this.contend(this.PC, 3);
                this.D.set(this.machine.readByte(this.PC++));
                break;
            case 0x12:  // LD (DE),A
                this.contend(this.DE, 3);
                this.machine.writeByte(this.DE.get(), this.A.get());
                break;
            case 0x13:  // INC DE
                this.tstates += 2;
                this.DE.inc();
                break;
            case 0x14:  // INC D
                this.INC(this.D);
                break;
            case 0x15:  // DEC D
                this.DEC(this.D);
                break;
            case 0x16:  // LD D,nn
                this.contend(this.PC, 3);
                this.D.set(this.machine.readByte(this.PC++));
                break;
            case 0x17:  // RLA
                {
                    const bytetemp = this.A.get();
                    this.A.set((this.A.get() << 1) | (this.F.get() & FLAG_C));
                    this.F.set((this.F.get() & (FLAG_P | FLAG_Z | FLAG_S)) | (this.A.get() & (FLAG_3 | FLAG_5)) | (bytetemp >> 7));
                }
                break;
            case 0x18:  // JR offset
                this.contend(this.PC, 3);
                this.JR();
                this.PC++;
                break;
            case 0x19:  // ADD HL,DE
                this.ADD16(this.HL, this.DE);
                break;
            case 0x1A:  // LD A,(DE)
                this.contend(this.DE, 3);
                this.A.set(this.machine.readByte(this.DE.get()));
                break;
            case 0x1B:  // DEC DE
                this.tstates += 2;
                this.DE.dec();
                break;
            case 0x1C:  // INC E
                this.INC(this.E);
                break;
            case 0x1D:  // DEC E
                this.DEC(this.E);
                break;
            case 0x1E:  // LD E,nn
                this.contend(this.PC, 3);
                this.E.set(this.machine.readByte(this.PC++));
                break;
            case 0x1F:  // RRA
                {
                    const bytetemp = this.A.get();
                    this.A.set((this.A.get() >> 1) | (this.F.get() << 7));
                    this.F.set((this.F.get() & (FLAG_P | FLAG_Z | FLAG_S)) | (this.A.get() & (FLAG_3 | FLAG_5)) | (bytetemp & FLAG_C));
                }
                break;
            case 0x20:  // JR NZ,offset
                this.contend(this.PC, 3);
                if ((this.F.get() & FLAG_Z) === 0)
                    this.JR();
                this.PC++;
                break;
            case 0x21:  // LD HL,nnnn
                this.contend(this.PC, 3);
                this.L.set(this.machine.readByte(this.PC++));
                this.contend(this.PC, 3);
                this.H.set(this.machine.readByte(this.PC++));
                break;
            case 0x22:  // LD (nnnn),HL
                this.LD16_NNRR(this.HL.get());
                break;
            case 0x23:  // INC HL
                this.tstates += 2;
                this.HL.inc();
                break;
            case 0x24:  // INC H
                this.INC(this.H);
                break;
            case 0x25:  // DEC H
                this.DEC(this.H);
                break;
            case 0x26:  // LD H,nn
                this.contend(this.PC, 3);
                this.H.set(this.machine.readByte(this.PC++));
                break;
            case 0x27:  // DAA
                {
                    let add = 0;
                    let carry = (this.F.get() & FLAG_C);
                    if (((this.F.get() & FLAG_H) !== 0) || (this.A.get() & 0x0f) > 9)
                        add = 6;
                    if (carry !== 0 || (this.A.get() > 0x0f9))
                        add |= 0x60;
                    if (this.A.get() > 0x99)
                        carry = 1;
                    if ((this.F.get() & FLAG_N) !== 0)
                        this.SUB(add);
                    else
                    {
                        if ((this.A.get() > 0x90) && ((this.A.get() & 0x0f) > 9))
                            add |= 0x60;
                        this.ADD(add);
                    }
                    this.F.set((this.F.get() & ~(FLAG_C | FLAG_P)) | carry | Z80.parity[this.A.get()]);
                }
                break;
            case 0x28:  // JR Z,offset
                this.contend(this.PC, 3);
                if ((this.F.get() & FLAG_Z) !== 0)
                    this.JR();
                this.PC++;
                break;
            case 0x29:  // ADD HL,HL
                this.ADD16(this.HL, this.HL);
                break;
            case 0x2A:  // LD HL,(nnnn)
                this.HL.set(this.LD16_RRNN());
                break;
            case 0x2B:  // DEC HL
                this.tstates += 2;
                this.HL.dec();
                break;
            case 0x2C:  // INC L
                this.INC(this.L);
                break;
            case 0x2D:  // DEC L
                this.DEC(this.L);
                break;
            case 0x2E:  // LD L,nn
                this.contend(this.PC, 3);
                this.L.set(this.machine.readByte(this.PC++));
                break;
            case 0x2F:  // CPL
                this.A.set(this.A.get() ^ 0xFF);
                this.F.set((this.F.get() & (FLAG_C | FLAG_P | FLAG_Z | FLAG_S)) |
                           (this.A.get() & (FLAG_3 | FLAG_5)) | (FLAG_N | FLAG_H));
                break;
            case 0x30:  // JR NC,offset
                this.contend(this.PC, 3);
                if ((this.F.get() & FLAG_C) === 0)
                    this.JR();
                this.PC++;
                break;
            case 0x31:  // LD SP,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC, 3);
                this.SP = this.machine.readByte(this.PC++) + (this.machine.readByte(this.PC++) << 8);
                break;
            case 0x32:  // LD (nnnn),A
                this.contend(this.PC, 3);
                {
                    let wordtemp = this.machine.readByte(this.PC++);
                    this.contend(this.PC, 3);
                    wordtemp |= this.machine.readByte(this.PC++) << 8;
                    this.contend(wordtemp, 3);
                    this.machine.writeByte(wordtemp, this.A.get());
                }
                break;
            case 0x33:  // INC SP
                this.tstates += 2;
                this.SP++;
                break;
            case 0x34:  // INC (HL)
                this.contend(this.HL, 4);
                {
                    const bytetemp: Register = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.INC(bytetemp);
                    this.contend(this.HL, 3);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x35:  // DEC (HL)
                this.contend(this.HL, 4);
                {
                    const bytetemp: Register = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.DEC(bytetemp);
                    this.contend(this.HL, 3);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x36:  // LD (HL),nn
                this.contend(this.PC, 3);
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.PC++));
                break;
            case 0x37:  // SCF
                this.F.and(~(FLAG_N | FLAG_H));
                this.F.or((this.A.get() & (FLAG_3 | FLAG_5)) | FLAG_C);
                break;
            case 0x38:  // JR C,offset
                this.contend(this.PC, 3);
                if ((this.F.get() & FLAG_C) !== 0)
                    this.JR();
                this.PC++;
                break;
            case 0x39:  // ADD HL,SP
                this.ADD16(this.HL, this.SP);
                break;
            case 0x3A:  // LD A,(nnnn)
                {
                    this.contend(this.PC, 3);
                    let wordtemp = this.machine.readByte(this.PC++);
                    this.contend(this.PC, 3);
                    wordtemp |= (this.machine.readByte(this.PC++) << 8);
                    this.contend(wordtemp, 3);
                    this.A.set(this.machine.readByte(wordtemp));
                }
                break;
            case 0x3B:  // DEC SP
                this.tstates += 2;
                this.SP--;
                break;
            case 0x3C:  // INC A
                this.INC(this.A);
                break;
            case 0x3D:  // DEC A
                this.DEC(this.A);
                break;
            case 0x3E:  // LD A,nn
                this.contend(this.PC, 3);
                this.A.set(this.machine.readByte(this.PC++));
                break;
            case 0x3F:  // CCF
                this.F.set((this.F.get() & (FLAG_P | FLAG_Z | FLAG_S)) |
                          ((this.F.get() & FLAG_C) !== 0 ? FLAG_H : FLAG_C) |
                           (this.A.get() & (FLAG_3 | FLAG_5)));
                break;
            case 0x40:  // LD B,B
                break;
            case 0x41:  // LD B,C
                this.B.set(this.C);
                break;
            case 0x42:  // LD B,D
                this.B.set(this.D);
                break;
            case 0x43:  // LD B,E
                this.B.set(this.E);
                break;
            case 0x44:  // LD B,H
                this.B.set(this.H);
                break;
            case 0x45:  // LD B,L
                this.B.set(this.L);
                break;
            case 0x46:  // LD B,(HL)
                this.contend(this.HL, 3);
                this.B.set(this.machine.readByte(this.HL.get()));
                break;
            case 0x47:  // LD B,A
                this.B.set(this.A);
                break;
            case 0x48:  // LD C,B
                this.C.set(this.B);
                break;
            case 0x49:  // LD C,C
                break;
            case 0x4A:  // LD C,D
                this.C.set(this.D);
                break;
            case 0x4B:  // LD C,E
                this.C.set(this.E);
                break;
            case 0x4C:  // LD C,H
                this.C.set(this.H);
                break;
            case 0x4D:  // LD C,L
                this.C.set(this.L);
                break;
            case 0x4E:  // LD C,(HL)
                this.contend(this.HL, 3);
                this.C.set(this.machine.readByte(this.HL.get()));
                break;
            case 0x4F:  // LD C,A
                this.C.set(this.A);
                break;
            case 0x50:  // LD D,B
                this.D.set(this.B);
                break;
            case 0x51:  // LD D,C
                this.D.set(this.C);
                break;
            case 0x52:  // LD D,D
                break;
            case 0x53:  // LD D,E
                this.D.set(this.E);
                break;
            case 0x54:  // LD D,H
                this.D.set(this.H);
                break;
            case 0x55:  // LD D,L
                this.D.set(this.L);
                break;
            case 0x56:  // LD D,(HL)
                this.contend(this.HL, 3);
                this.D.set(this.machine.readByte(this.HL.get()));
                break;
            case 0x57:  // LD D,A
                this.D.set(this.A);
                break;
            case 0x58:  // LD E,B
                this.E.set(this.B);
                break;
            case 0x59:  // LD E,C
                this.E.set(this.C);
                break;
            case 0x5A: // LD E,D
                this.E.set(this.D);
                break;
            case 0x5B:  // LD E,E
                break;
            case 0x5C:  // LD E,H
                this.E.set(this.H);
                break;
            case 0x5D:  // LD E,L
                this.E.set(this.L);
                break;
            case 0x5E:  // LD E,(HL)
                this.contend(this.HL, 3);
                this.E.set(this.machine.readByte(this.HL.get()));
                break;
            case 0x5F:  // LD E,A
                this.E.set(this.A);
                break;
            case 0x60:  // LD H,B
                this.H.set(this.B);
                break;
            case 0x61:  // LD H,C
                this.H.set(this.C);
                break;
            case 0x62:  // LD H,D
                this.H.set(this.D);
                break;
            case 0x63:  // LD H,E
                this.H.set(this.E);
                break;
            case 0x64:  // LD H,H
                break;
            case 0x65:  // LD H,L
                this.H.set(this.L);
                break;
            case 0x66:  // LD H,(HL)
                this.contend(this.HL, 3);
                this.H.set(this.machine.readByte(this.HL.get()));
                break;
            case 0x67:  // LD H,A
                this.H.set(this.A);
                break;
            case 0x68:  // LD L,B
                this.L.set(this.B);
                break;
            case 0x69:  // LD L,C
                this.L.set(this.C);
                break;
            case 0x6A:  // LD L,D
                this.L.set(this.D);
                break;
            case 0x6B:  // LD L,E
                this.L.set(this.E);
                break;
            case 0x6C:  // LD L,H
                this.L.set(this.H);
                break;
            case 0x6D:  // LD L,L
                break;
            case 0x6E:  // LD L,(HL)
                this.contend(this.HL, 3);
                this.L.set(this.machine.readByte(this.HL.get()));
                break;
            case 0x6F:  // LD L,A
                this.L.set(this.A);
                break;
            case 0x70:  // LD (HL),B
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.B.get());
                break;
            case 0x71:  // LD (HL),C
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.C.get());
                break;
            case 0x72:  // LD (HL),D
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.D.get());
                break;
            case 0x73:  // LD (HL),E
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.E.get());
                break;
            case 0x74:  // LD (HL),H
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.H.get());
                break;
            case 0x75:  // LD (HL),L
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.L.get());
                break;
            case 0x76:  // HALT
                this.halted = 1;
                this.PC--;
                break;
            case 0x77:  // LD (HL),A
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.A.get());
                break;
            case 0x78:  // LD A,B
                this.A.set(this.B);
                break;
            case 0x79:  // LD A,C
                this.A.set(this.C);
                break;
            case 0x7A:  // LD A,D
                this.A.set(this.D);
                break;
            case 0x7B:  // LD A,E
                this.A.set(this.E);
                break;
            case 0x7C:  // LD A,H
                this.A.set(this.H);
                break;
            case 0x7D:  // LD A,L
                this.A.set(this.L);
                break;
            case 0x7E:  // LD A,(HL)
                this.contend(this.HL, 3);
                this.A.set(this.machine.readByte(this.HL.get()));
                break;
            case 0x7F:  // LD A,A
                break;
            case 0x80:  // ADD A,B
                this.ADD(this.B.get());
                break;
            case 0x81:   // ADD A,C
                this.ADD(this.C.get());
                break;
            case 0x82:  // ADD A,D
                this.ADD(this.D.get());
                break;
            case 0x83:  // ADD A,E
                this.ADD(this.E.get());
                break;
            case 0x84:  // ADD A,H
                this.ADD(this.H.get());
                break;
            case 0x85:  // ADD A,L
                this.ADD(this.L.get());
                break;
            case 0x86:  // ADD A,(HL)
                this.contend(this.HL, 3);
                this.ADD(this.machine.readByte(this.HL.get()));
                break;
            case 0x87:  // ADD A,A
                this.ADD(this.A.get());
                break;
            case 0x88:  // ADC A,B
                this.ADC(this.B.get());
                break;
            case 0x89:  // ADC A,C
                this.ADC(this.C.get());
                break;
            case 0x8A:  // ADC A,D
                this.ADC(this.D.get());
                break;
            case 0x8B:  // ADC A,E
                this.ADC(this.E.get());
                break;
            case 0x8C:  // ADC A,H
                this.ADC(this.H.get());
                break;
            case 0x8D:  // ADC A,L
                this.ADC(this.L.get());
                break;
            case 0x8E:  // ADC A,(HL)
                this.contend(this.HL, 3);
                this.ADC(this.machine.readByte(this.HL.get()));
                break;
            case 0x8F:  // ADC A,A
                this.ADC(this.A.get());
                break;
            case 0x90:  // SUB A,B
                this.SUB(this.B.get());
                break;
            case 0x91:  // SUB A,C
                this.SUB(this.C.get());
                break;
            case 0x92:  // SUB A,D
                this.SUB(this.D.get());
                break;
            case 0x93:  // SUB A,E
                this.SUB(this.E.get());
                break;
            case 0x94:  // SUB A,H
                this.SUB(this.H.get());
                break;
            case 0x95:  // SUB A,L
                this.SUB(this.L.get());
                break;
            case 0x96:  // SUB A,(HL)
                this.contend(this.HL, 3);
                this.SUB(this.machine.readByte(this.HL.get()));
                break;
            case 0x97:  // SUB A,A
                this.SUB(this.A.get());
                break;
            case 0x98:  // SBC A,B
                this.SBC(this.B.get());
                break;
            case 0x99:  // SBC A,C
                this.SBC(this.C.get());
                break;
            case 0x9A:  // SBC A,D
                this.SBC(this.D.get());
                break;
            case 0x9B:  // SBC A,E
                this.SBC(this.E.get());
                break;
            case 0x9C:  // SBC A,H
                this.SBC(this.H.get());
                break;
            case 0x9D:  // SBC A,L
                this.SBC(this.L.get());
                break;
            case 0x9E:  // SBC A,(HL)
                this.contend(this.HL, 3);
                this.SBC(this.machine.readByte(this.HL.get()));
                break;
            case 0x9F:  // SBC A,A
                this.SBC(this.A.get());
                break;
            case 0xA0:  // AND A,B
                this.AND(this.B);
                break;
            case 0xA1:  // AND A,C
                this.AND(this.C);
                break;
            case 0xA2:  // AND A,D
                this.AND(this.D);
                break;
            case 0xA3:  // AND A,E
                this.AND(this.E);
                break;
            case 0xA4:  // AND A,H
                this.AND(this.H);
                break;
            case 0xA5:  // AND A,L
                this.AND(this.L);
                break;
            case 0xA6:  // AND A,(HL)
                this.contend(this.HL, 3);
                this.AND(this.machine.readByte(this.HL.get()));
                break;
            case 0xA7:  // AND A,A
                this.AND(this.A.get());
                break;
            case 0xA8:  // XOR A,B
                this.XOR(this.B);
                break;
            case 0xA9:  // XOR A,C
                this.XOR(this.C);
                break;
            case 0xAA:  // XOR A,D
                this.XOR(this.D);
                break;
            case 0xAB:  // XOR A,E
                this.XOR(this.E);
                break;
            case 0xAC:  // XOR A,H
                this.XOR(this.H);
                break;
            case 0xAD:  // XOR A,L
                this.XOR(this.L);
                break;
            case 0xAE:  // XOR A,(HL)
                this.contend(this.HL, 3);
                this.XOR(this.machine.readByte(this.HL.get()));
                break;
            case 0xAF:  // XOR A,A
                this.XOR(this.A);
                break;
            case 0xB0:  // OR A,B
                this.OR(this.B);
                break;
            case 0xB1:  // OR A,C
                this.OR(this.C);
                break;
            case 0xB2:  // OR A,D
                this.OR(this.D);
                break;
            case 0xB3:  // OR A,E
                this.OR(this.E);
                break;
            case 0xB4:  // OR A,H
                this.OR(this.H);
                break;
            case 0xB5:  // OR A,L
                this.OR(this.L);
                break;
            case 0xB6:  // OR A,(HL)
                this.contend(this.HL, 3);
                this.OR(this.machine.readByte(this.HL.get()));
                break;
            case 0xB7:  // OR A,A
                this.OR(this.A);
                break;
            case 0xB8:  // CP B
                this.CP(this.B.get());
                break;
            case 0xB9:  // CP C
                this.CP(this.C.get());
                break;
            case 0xBA:  // CP D
                this.CP(this.D.get());
                break;
            case 0xBB:  // CP E
                this.CP(this.E.get());
                break;
            case 0xBC:  // CP H
                this.CP(this.H.get());
                break;
            case 0xBD:  // CP L
                this.CP(this.L.get());
                break;
            case 0xBE:  // CP (HL)
                this.contend(this.HL, 3);
                this.CP(this.machine.readByte(this.HL.get()));
                break;
            case 0xBF:  // CP A
                this.CP(this.A.get());
                break;
            case 0xC0:  // RET NZ
                this.tstates++;
                if ((this.F.get() & FLAG_Z) === 0)
                    this.RET();
                break;
            case 0xC1:  // POP BC
                this.BC.set(this.POP16());
                break;
            case 0xC2:  // JP NZ,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_Z) === 0)
                    this.JP();
                else
                    this.PC += 2;
                break;
            case 0xC3:  // JP nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                this.JP();
                break;
            case 0xC4:  // CALL NZ,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_Z) === 0)
                    this.CALL();
                else
                    this.PC += 2;
                break;
            case 0xC5:  // PUSH BC
                this.tstates++;
                this.PUSH16(this.BC.get());
                break;
            case 0xC6:  // ADD A,nn
                this.contend(this.PC, 3);
                this.ADD(this.machine.readByte(this.PC++));
                break;
            case 0xC7:  // RST 00
                this.tstates++;
                this.RST(0);
                break;
            case 0xC8:  // RET Z
                this.tstates++;
                if ((this.F.get() & FLAG_Z) !== 0)
                    this.RET();
                break;
            case 0xC9:  // RET
                this.RET();
                break;
            case 0xCA:  // JP Z,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_Z) > 0)
                    this.JP();
                else
                    this.PC += 2;
                break;
            case 0xCB:  // CBxx opcodes
                {
                    this.contend(this.PC, 4);
                    const opcode2 = this.machine.opcodeFetch(this.PC++);
                    this.R = (this.R + 1) & 0xFF;
                    this.doOpcodeCB(opcode2);
                }
                break;
            case 0xCC:  // CALL Z,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_Z) > 0)
                    this.CALL();
                else
                    this.PC += 2;
                break;
            case 0xCD:  // CALL nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                this.CALL();
                break;
            case 0xCE:  // ADC A,nn
                this.contend(this.PC, 3);
                this.ADC(this.machine.readByte(this.PC++));
                break;
            case 0xCF:  // RST 8
                this.tstates++;
                this.RST(8);
                break;
            case 0xD0:  // RET NC
                this.tstates++;
                if ((this.F.get() & FLAG_C) === 0)
                    this.RET();
                break;
            case 0xD1:  // POP DE
                this.DE.set(this.POP16());
                break;
            case 0xD2:  // JP NC,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_C) === 0)
                    this.JP();
                else
                    this.PC += 2;
                break;
            case 0xD3:  // OUT (nn),A
                this.contend(this.PC, 4);
                this.OUT(this.machine.readByte(this.PC++) + (this.A.get() << 8), this.A);
                break;
            case 0xD4:  // CALL NC,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_C) === 0)
                    this.CALL();
                else
                    this.PC += 2;
                break;
            case 0xD5:  // PUSH DE
                this.tstates++;
                this.PUSH16(this.DE.get());
                break;
            case 0xD6:  // SUB nn
                this.contend(this.PC, 3);
                this.SUB(this.machine.readByte(this.PC++));
                break;
            case 0xD7:  // RST 10
                this.tstates++;
                this.RST(0x10);
                break;
            case 0xD8: // RET C
                this.tstates++;
                if ((this.F.get() & FLAG_C) > 0)
                    this.RET();
                break;
            case 0xD9:  // EXX
                {
                    let wordtemp = this.BC.get(); this.BC.set(this.BC_); this.BC_ = wordtemp;
                    wordtemp = this.DE.get(); this.DE.set(this.DE_); this.DE_ = wordtemp;
                    wordtemp = this.HL.get(); this.HL.set(this.HL_); this.HL_ = wordtemp;
                }
                break;
            case 0xDA:  // JP C,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_C) > 0)
                    this.JP();
                else
                    this.PC += 2;
                break;
            case 0xDB:  // IN A,(nn)
                {
                    this.contend(this.PC, 4);
                    const intemp = this.machine.readByte(this.PC++) + (this.A.get() << 8);
                    this.contendIO(intemp, 3);
                    this.A.set(this.machine.readPort(intemp));
                }
                break;
            case 0xDC:  // CALL C,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_C) > 0)
                    this.CALL();
                else
                    this.PC += 2;
                break;
            case 0xDD:  // DDxx opcodes
                {
                    this.contend(this.PC, 4);
                    const opcode2 = this.machine.opcodeFetch(this.PC++);
                    this.R = (this.R + 1) & 0xFF;
                    this.doOpcodeDDFD(opcode2, this.IX, this.IXL, this.IXH);
                }
                break;
            case 0xDE:  // SBC A,nn
                this.contend(this.PC, 3);
                this.SBC(this.machine.readByte(this.PC++));
                break;
            case 0xDF:  // RST 18
                this.tstates++;
                this.RST(0x18);
                break;
            case 0xE0: // RET PO
                this.tstates++;
                if ((this.F.get() & FLAG_P) === 0)
                    this.RET();
                break;
            case 0xE1:  // POP HL
                this.HL.set(this.POP16());
                break;
            case 0xE2:  // JP PO,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_P) === 0)
                    this.JP();
                else
                    this.PC += 2;
                break;
            case 0xE3:  // EX (SP),HL
                {
                    const bytetempl = this.machine.readByte(this.SP);
                    const bytetemph = this.machine.readByte(this.SP + 1);
                    this.contend(this.SP, 3);
                    this.contend(this.SP + 1, 4);
                    this.contend(this.SP, 3);
                    this.contend(this.SP + 1, 5);
                    this.machine.writeByte(this.SP, this.L.get());
                    this.machine.writeByte(this.SP + 1, this.H.get());
                    this.L.set(bytetempl);
                    this.H.set(bytetemph);
                }
                break;
            case 0xE4:  // CALL PO,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_P) === 0)
                    this.CALL();
                else
                    this.PC += 2;
                break;
            case 0xE5:  // PUSH HL
                this.tstates++;
                this.PUSH16(this.HL.get());
                break;
            case 0xE6:  // AND nn
                this.contend(this.PC, 3);
                this.AND(this.machine.readByte(this.PC++));
                break;
            case 0xE7:  // RST 20
                this.tstates++;
                this.RST(0x20);
                break;
            case 0xE8:  // RET PE
                this.tstates++;
                if ((this.F.get() & FLAG_P) > 0)
                    this.RET();
                break;
            case 0xE9:  // JP HL
                this.PC = this.HL.get();
                break;
            case 0xEA:  // JP PE,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_P) > 0)
                    this.JP();
                else
                    this.PC += 2;
                break;
            case 0xEB:  // EX DE,HL
                {
                    const wordtemp = this.DE.get();
                    this.DE.set(this.HL.get());
                    this.HL.set(wordtemp);
                }
                break;
            case 0xEC:  // CALL PE,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_P) > 0)
                    this.CALL();
                else
                    this.PC += 2;
                break;
            case 0xED:  // EDxx opcodes
                {
                    this.contend(this.PC, 4);
                    const opcode2 = this.machine.opcodeFetch(this.PC++);
                    this.R = (this.R + 1) & 0xFF;
                    this.doOpcodeED(opcode2);
                }
                break;
            case 0xEE:  // XOR A,nn
                this.contend(this.PC, 3);
                {
                    const bytetemp = this.machine.readByte(this.PC++);
                    this.XOR(bytetemp);
                }
                break;
            case 0xEF:  // RST 28
                this.tstates++;
                this.RST(0x28);
                break;
            case 0xF0:  // RET P
                this.tstates++;
                if ((this.F.get() & FLAG_S) === 0)
                    this.RET();
                break;
            case 0xF1:  // POP AF
                this.AF.set(this.POP16());
                break;
            case 0xF2:  // JP P,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_S) === 0)
                    this.JP();
                else
                    this.PC += 2;
                break;
            case 0xF3:  // DI
                this.IFF1 = this.IFF2 = 0;
                break;
            case 0xF4:  // CALL P,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_S) === 0)
                    this.CALL();
                else
                    this.PC += 2;
                break;
            case 0xF5:  // PUSH AF
                this.tstates++;
                this.PUSH16(this.AF.get());
                break;
            case 0xF6:  // OR nn
                this.contend(this.PC, 3);
                this.OR(this.machine.readByte(this.PC++));
                break;
            case 0xF7:  // RST 30
                this.tstates++;
                this.RST(0x30);
                break;
            case 0xF8:  // RET M
                this.tstates++;
                if ((this.F.get() & FLAG_S) !== 0)
                    this.RET();
                break;
            case 0xF9:  // LD SP,HL
                this.tstates += 2;
                this.SP = this.HL.get();
                break;
            case 0xFA:  // JP M,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_S) > 0)
                    this.JP();
                else
                    this.PC += 2;
                break;
            case 0xFB:  // EI
                this.IFF1 = this.IFF2 = 1;
                break;
            case 0xFC:  // CALL M,nnnn
                this.contend(this.PC, 3);
                this.contend(this.PC + 1, 3);
                if ((this.F.get() & FLAG_S) > 0)
                    this.CALL();
                else
                    this.PC += 2;
                break;
            case 0xFD:  // FDxx opcodes
                {
                    this.contend(this.PC, 4);
                    const opcode2 = this.machine.opcodeFetch(this.PC++);
                    this.R = (this.R + 1) & 0xFF;
                    this.doOpcodeDDFD(opcode2, this.IY, this.IYL, this.IYH);
                }
                break;
            case 0xFE:  // CP nn
                this.contend(this.PC, 3);
                this.CP(this.machine.readByte(this.PC++));
                break;
            case 0xFF:  // RST 38
                this.tstates++;
                this.RST(0x38);
                break;
        }

        this.R = this.R & 0x7f;
        return (this.tstates);
    }

    private doOpcodeCB(opcode2: number)
    {
        switch (opcode2)
        {
            case 0x00:
                this.RLC(this.B);
                break;
            case 0x01:
                this.RLC(this.C);
                break;
            case 0x02:
                this.RLC(this.D);
                break;
            case 0x03:
                this.RLC(this.E);
                break;
            case 0x04:
                this.RLC(this.H);
                break;
            case 0x05:
                this.RLC(this.L);
                break;
            case 0x06:  // RLC (HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.RLC(bytetemp);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x07:
                this.RLC(this.A);
                break;
            case 0x08:
                this.RRC(this.B);
                break;
            case 0x09:
                this.RRC(this.C);
                break;
            case 0x0A:
                this.RRC(this.D);
                break;
            case 0x0B:
                this.RRC(this.E);
                break;
            case 0x0C:
                this.RRC(this.H);
                break;
            case 0x0D:
                this.RRC(this.L);
                break;
            case 0x0E:  // RRC (HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.RRC(bytetemp);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x0F:
                this.RRC(this.A);
                break;
            case 0x10:
                this.RL(this.B);
                break;
            case 0x11:
                this.RL(this.C);
                break;
            case 0x12:
                this.RL(this.D);
                break;
            case 0x13:
                this.RL(this.E);
                break;
            case 0x14:
                this.RL(this.H);
                break;
            case 0x15:
                this.RL(this.L);
                break;
            case 0x16:  // RL (HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.RL(bytetemp);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x17:
                this.RL(this.A);
                break;
            case 0x18:
                this.RR(this.B);
                break;
            case 0x19:
                this.RR(this.C);
                break;
            case 0x1A:
                this.RR(this.D);
                break;
            case 0x1B:
                this.RR(this.E);
                break;
            case 0x1C:
                this.RR(this.H);
                break;
            case 0x1D:
                this.RR(this.L);
                break;
            case 0x1E:  // RR (HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.RR(bytetemp);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x1F:
                this.RR(this.A);
                break;
            case 0x20:
                this.SLA(this.B);
                break;
            case 0x21:
                this.SLA(this.C);
                break;
            case 0x22:
                this.SLA(this.D);
                break;
            case 0x23:
                this.SLA(this.E);
                break;
            case 0x24:
                this.SLA(this.H);
                break;
            case 0x25:
                this.SLA(this.L);
                break;
            case 0x26:  // SLA (HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.SLA(bytetemp);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x27:
                this.SLA(this.A);
                break;
            case 0x28:
                this.SRA(this.B);
                break;
            case 0x29:
                this.SRA(this.C);
                break;
            case 0x2A:
                this.SRA(this.D);
                break;
            case 0x2B:
                this.SRA(this.E);
                break;
            case 0x2C:
                this.SRA(this.H);
                break;
            case 0x2D:
                this.SRA(this.L);
                break;
            case 0x2E:  // SRA (HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.SRA(bytetemp);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x2F:
                this.SRA(this.A);
                break;
            case 0x30:
                this.SLL(this.B);
                break;
            case 0x31:
                this.SLL(this.C);
                break;
            case 0x32:
                this.SLL(this.D);
                break;
            case 0x33:
                this.SLL(this.E);
                break;
            case 0x34:
                this.SLL(this.H);
                break;
            case 0x35:
                this.SLL(this.L);
                break;
            case 0x36:  // SLL (HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.SLL(bytetemp);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x37:
                this.SLL(this.A);
                break;
            case 0x38:
                this.SRL(this.B);
                break;
            case 0x39:
                this.SRL(this.C);
                break;
            case 0x3A:
                this.SRL(this.D);
                break;
            case 0x3B:
                this.SRL(this.E);
                break;
            case 0x3C:
                this.SRL(this.H);
                break;
            case 0x3D:
                this.SRL(this.L);
                break;
            case 0x3E:  // SRL (HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.contend(this.HL, 3);
                    this.SRL(bytetemp);
                    this.machine.writeByte(this.HL.get(), bytetemp.get());
                }
                break;
            case 0x3F:
                this.SRL(this.A);
                break;
            case 0x40:
                this.BIT(0, this.B);
                break;
            case 0x41:
                this.BIT(0, this.C);
                break;
            case 0x42:
                this.BIT(0, this.D);
                break;
            case 0x43:
                this.BIT(0, this.E);
                break;
            case 0x44:
                this.BIT(0, this.H);
                break;
            case 0x45:
                this.BIT(0, this.L);
                break;
            case 0x46:  // BIT 0,(HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.BIT(0, bytetemp);
                }
                break;
            case 0x47:
                this.BIT(0, this.A);
                break;
            case 0x48:
                this.BIT(1, this.B);
                break;
            case 0x49:
                this.BIT(1, this.C);
                break;
            case 0x4A:
                this.BIT(1, this.D);
                break;
            case 0x4B:
                this.BIT(1, this.E);
                break;
            case 0x4C:
                this.BIT(1, this.H);
                break;
            case 0x4D:
                this.BIT(1, this.L);
                break;
            case 0x4E:  // BIT 1,(HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.BIT(1, bytetemp);
                }
                break;
            case 0x4F:
                this.BIT(1, this.A);
                break;
            case 0x50:
                this.BIT(2, this.B);
                break;
            case 0x51:
                this.BIT(2, this.C);
                break;
            case 0x52:
                this.BIT(2, this.D);
                break;
            case 0x53:
                this.BIT(2, this.E);
                break;
            case 0x54:
                this.BIT(2, this.H);
                break;
            case 0x55:
                this.BIT(2, this.L);
                break;
            case 0x56:  // BIT 2,(HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.BIT(2, bytetemp);
                }
                break;

            case 0x57:
                this.BIT(2, this.A);
                break;
            case 0x58:
                this.BIT(3, this.B);
                break;
            case 0x59:
                this.BIT(3, this.C);
                break;
            case 0x5A:
                this.BIT(3, this.D);
                break;
            case 0x5B:
                this.BIT(3, this.E);
                break;
            case 0x5C:
                this.BIT(3, this.H);
                break;
            case 0x5D:
                this.BIT(3, this.L);
                break;
            case 0x5E:  // BIT 3,(HL)
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.BIT(3, bytetemp);
                }
                break;
            case 0x5F:
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
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.BIT(4, bytetemp);
                }
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
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.BIT(5, bytetemp);
                }
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
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.BIT(6, bytetemp);
                }
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
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(this.HL.get()));
                    this.contend(this.HL, 4);
                    this.BIT7(bytetemp);
                }
                break;
            case 0x7f:
                this.BIT7(this.A);
                break;
            case 0x80:
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) & 254);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) & 253);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) & 251);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) & 247);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) & 239);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) & 223);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) & 191);
                break;
            case 183:
                this.A.and(191);
                break;
            case 184:
                this.B.and(0x7f);
                break;
            case 185:
                this.C.and(0x7f);
                break;
            case 186:
                this.D.and(0x7f);
                break;
            case 187:
                this.E.and(0x7f);
                break;
            case 188:
                this.H.and(0x7f);
                break;
            case 189:
                this.L.and(0x7f);
                break;
            case 190:
                this.contend(this.HL, 4);
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) & 0x7f);
                break;
            case 191:
                this.A.and(0x7f);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) | 1);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) | 2);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) | 4);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) | 8);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) | 16);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) | 32);
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
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) | 64);
                break;
            case 247:
                this.A.or(64);
                break;
            case 248:
                this.B.or(0x80);
                break;
            case 249:
                this.C.or(0x80);
                break;
            case 250:
                this.D.or(0x80);
                break;
            case 251:
                this.E.or(0x80);
                break;
            case 252:
                this.H.or(0x80);
                break;
            case 253:
                this.L.or(0x80);
                break;
            case 254:
                this.contend(this.HL, 4);
                this.contend(this.HL, 3);
                this.machine.writeByte(this.HL.get(), this.machine.readByte(this.HL.get()) | 0x80);
                break;
            case 0xFF:
                this.A.or(0x80);
                break;
        }
    }

    private doOpcodeED(opcode2: number)
    {
        switch (opcode2)
        {
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
                this.SBC16(this.BC.get());
                break;
            case 67:
                this.LD16_NNRR(this.BC.get());
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
                    const bytetemp = this.A.get();
                    this.A.set(0);
                    this.SUB(bytetemp);
                }
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
                this.I = this.A.get();
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
                this.ADC16(this.BC.get());
                break;
            case 75:
                this.BC.set(this.LD16_RRNN());
                break;
            case 79:
                this.tstates += 1;
                this.R = this.R7 = this.A.get();
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
                this.SBC16(this.DE.get());
                break;
            case 83:
                this.LD16_NNRR(this.DE.get());
                break;
            case 86:
            case 118:
                this.IM = 1;
                break;
            case 87:
                this.tstates += 1;
                this.A.set(this.I);
                this.F.set((this.F.get() & FLAG_C) | Z80.sz53[this.A.get()] | (this.IFF2 !== 0 ? FLAG_V : 0));
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
                this.ADC16(this.DE.get());
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
                this.A.set((this.R & 0x7f) | (this.R7 & 0x80));
                this.F.set((this.F.get() & FLAG_C) | Z80.sz53[this.A.get()] | (this.IFF2 !== 0 ? FLAG_V : 0));
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
                this.SBC16(this.HL.get());
                break;
            case 99:
                this.LD16_NNRR(this.HL.get());
                break;
            case 103:
                {
                    const bytetemp = this.machine.readByte(this.HL.get());
                    this.contend(this.HL, 7);
                    this.contend(this.HL, 3);
                    this.machine.writeByte(this.HL.get(), (this.A.get() << 4) | (bytetemp >> 4));
                    this.A.set((this.A.get() & 240) | (bytetemp & 0x0f));
                    this.F.set((this.F.get() & FLAG_C) | Z80.sz53p[this.A.get()]);
                }
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
                this.ADC16(this.HL.get());
                break;
            case 107:
                this.HL.set(this.LD16_RRNN());
                break;
            case 111:
                {
                    const bytetemp = this.machine.readByte(this.HL.get());
                    this.contend(this.HL, 7);
                    this.contend(this.HL, 3);
                    this.machine.writeByte(this.HL.get(), (bytetemp << 4) | (this.A.get() & 0x0f));
                    this.A.set((this.A.get() & 240) | (bytetemp >> 4));
                    this.F.set((this.F.get() & FLAG_C) | Z80.sz53p[this.A.get()]);
                }
                break;
            case 112:
                this.tstates += 1;
                this.IN(0, this.BC.get());
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
                    let bytetemp = this.machine.readByte(this.HL.get());
                    this.contend(this.HL, 3);
                    this.contend(this.DE, 3);
                    this.contend(this.DE, 1);
                    this.contend(this.DE, 1);
                    this.BC.dec();
                    this.machine.writeByte(this.DE.get(), bytetemp);
                    this.DE.inc();
                    this.HL.inc();
                    bytetemp += this.A.get();
                    this.F.set((this.F.get() & (FLAG_C | FLAG_Z | FLAG_S)) | (this.BC.get() !== 0 ? FLAG_V : 0) | (bytetemp & FLAG_3) | ((bytetemp & 2) !== 0 ? FLAG_5 : 0));
                }
                break;
            case 161:
                {
                    const value = this.machine.readByte(this.HL.get());
                    let bytetemp = this.A.get() - value;
                    const lookup = ((this.A.get() & 8) >> 3) | (((value) & 8) >> 2) | ((bytetemp & 8) >> 1);
                    this.contend(this.HL, 3);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.HL.inc();
                    this.BC.dec();
                    this.F.set((this.F.get() & FLAG_C)
                        | (this.BC.get() !== 0 ? (FLAG_V | FLAG_N) : FLAG_N)
                        | Z80.halfCarrySub[lookup]
                        | (bytetemp !== 0 ? 0 : FLAG_Z)
                        | (bytetemp & FLAG_S));
                    if ((this.F.get() & FLAG_H) !== 0) bytetemp--;
                    this.F.or((bytetemp & FLAG_3) | ((bytetemp & 2) !== 0 ? FLAG_5 : 0));
                }
                break;
            case 162:
                {
                    const initemp = this.machine.readPort(this.BC.get());
                    this.tstates += 2;
                    this.contendIO(this.BC, 3);
                    this.contend(this.HL, 3);
                    this.machine.writeByte(this.HL.get(), initemp);
                    this.B.dec();
                    this.HL.inc();
                    this.F.set(((initemp & 0x80) !== 0 ? FLAG_N : 0) | Z80.sz53[this.B.get()]);
                }
                break;
            case 163:
                {
                    const outitemp = this.machine.readByte(this.HL.get());
                    this.B.dec();
                    this.tstates++;
                    this.contend(this.HL, 4);
                    this.contendIO(this.BC, 3);
                    this.HL.inc();
                    this.machine.writePort(this.BC.get(), outitemp);
                    this.F.set(((outitemp & 0x80) !== 0 ? FLAG_N : 0) | Z80.sz53[this.B.get()]);
                }
                break;
            case 168:
                {
                    let bytetemp = this.machine.readByte(this.HL.get());
                    this.contend(this.HL, 3);
                    this.contend(this.DE, 3);
                    this.contend(this.DE, 1);
                    this.contend(this.DE, 1);
                    this.BC.dec();
                    this.machine.writeByte(this.DE.get(), bytetemp);
                    this.DE.dec();
                    this.HL.dec();
                    bytetemp += this.A.get();
                    this.F.set((this.F.get() & (FLAG_C | FLAG_Z | FLAG_S)) | (this.BC.get() !== 0 ? FLAG_V : 0) | (bytetemp & FLAG_3) | ((bytetemp & 2) !== 0 ? FLAG_5 : 0));
                }
                break;
            case 169:
                {
                    const value = this.machine.readByte(this.HL.get());
                    let bytetemp = this.A.get() - value;
                    const lookup = ((this.A.get() & 8) >> 3) | (((value) & 8) >> 2) | ((bytetemp & 8) >> 1);
                    this.contend(this.HL, 3);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.HL.dec();
                    this.BC.dec();
                    this.F.set((this.F.get() & FLAG_C)
                        | (this.BC.get() !== 0 ? (FLAG_V | FLAG_N) : FLAG_N)
                        | Z80.halfCarrySub[lookup]
                        | (bytetemp !== 0 ? 0 : FLAG_Z)
                        | (bytetemp & FLAG_S));
                    if ((this.F.get() & FLAG_H) !== 0) bytetemp--;
                    this.F.or((bytetemp & FLAG_3) | ((bytetemp & 2) !== 0 ? FLAG_5 : 0));
                }
                break;
            case 170:
                {
                    const initemp = this.machine.readPort(this.BC.get());
                    this.tstates += 2;
                    this.contendIO(this.BC, 3);
                    this.contend(this.HL, 3);
                    this.machine.writeByte(this.HL.get(), initemp);
                    this.B.dec();
                    this.HL.dec();
                    this.F.set(((initemp & 0x80) !== 0 ? FLAG_N : 0) | Z80.sz53[this.B.get()]);
                }
                break;
            case 171:
                {
                    const outitemp = this.machine.readByte(this.HL.get());
                    this.B.dec();
                    this.tstates++;
                    this.contend(this.HL, 4);
                    this.contendIO(this.BC, 3);
                    this.HL.dec();
                    this.machine.writePort(this.BC.get(), outitemp);
                    this.F.set(((outitemp & 0x80) !== 0 ? FLAG_N : 0) | Z80.sz53[this.B.get()]);
                }
                break;
            case 176:
                {
                    let bytetemp = this.machine.readByte(this.HL.get());
                    this.contend(this.HL, 3);
                    this.contend(this.DE, 3);
                    this.contend(this.DE, 1);
                    this.contend(this.DE, 1);
                    this.machine.writeByte(this.DE.get(), bytetemp);
                    this.HL.inc();
                    this.DE.inc();
                    this.BC.dec();
                    bytetemp += this.A.get();
                    this.F.set((this.F.get() & (FLAG_C | FLAG_Z | FLAG_S)) | (this.BC.get() !== 0 ? FLAG_V : 0) | (bytetemp & FLAG_3) | ((bytetemp & 2) !== 0 ? FLAG_5 : 0));
                    if (this.BC.get() !== 0)
                    {
                        this.contend(this.DE, 1);
                        this.contend(this.DE, 1);
                        this.contend(this.DE, 1);
                        this.contend(this.DE, 1);
                        this.contend(this.DE, 1);
                        this.PC -= 2;
                    }
                }
                break;
            case 177:
                {
                    const value = this.machine.readByte(this.HL.get());
                    let bytetemp = this.A.get() - value;
                    const lookup = ((this.A.get() & 8) >> 3) | (((value) & 8) >> 2) | ((bytetemp & 8) >> 1);
                    this.contend(this.HL, 3);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.contend(this.HL, 1);
                    this.HL.inc();
                    this.BC.dec();
                    this.F.set((this.F.get() & FLAG_C)
                        | (this.BC.get() !== 0 ? (FLAG_V | FLAG_N) : FLAG_N)
                        | Z80.halfCarrySub[lookup]
                        | (bytetemp !== 0 ? 0 : FLAG_Z)
                        | (bytetemp & FLAG_S));
                    if ((this.F.get() & FLAG_H) !== 0) bytetemp--;
                    this.F.or((bytetemp & FLAG_3) | ((bytetemp & 2) !== 0 ? FLAG_5 : 0));
                    if ((this.F.get() & (FLAG_V | FLAG_Z)) === FLAG_V)
                    {
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.PC -= 2;
                    }
                }
                break;
            case 178:
                {
                    const initemp = this.machine.readPort(this.BC.get());
                    this.tstates += 2;
                    this.contendIO(this.BC, 3);
                    this.contend(this.HL, 3);
                    this.machine.writeByte(this.HL.get(), initemp);
                    this.B.dec();
                    this.HL.inc();
                    this.F.set(((initemp & 0x80) !== 0 ? FLAG_N : 0) | Z80.sz53[this.B.get()]);
                    if (this.B.get() !== 0)
                    {
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.PC -= 2;
                    }
                }
                break;
            case 179:
                {
                    const outitemp = this.machine.readByte(this.HL.get());
                    this.tstates++;
                    this.contend(this.HL, 4);
                    this.B.dec();
                    this.HL.inc();
                    this.machine.writePort(this.BC.get(), outitemp);
                    this.F.set(((outitemp & 0x80) !== 0 ? FLAG_N : 0) | Z80.sz53[this.B.get()]);
                    if(this.B.get() !== 0)
                    {
                        this.contendIO(this.BC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC - 1, 1);
                        this.PC -= 2;
                    }
                    else
                    {
                        this.contendIO(this.BC, 3);
                    }
                }
                break;
            case 184:
                {
                    let bytetemp = this.machine.readByte(this.HL.get());
                    this.contend(this.HL, 3);
                    this.contend(this.DE, 3);
                    this.contend(this.DE, 1);
                    this.contend(this.DE, 1);
                    this.machine.writeByte(this.DE.get(), bytetemp);
                    this.HL.dec();
                    this.DE.dec();
                    this.BC.dec();
                    bytetemp += this.A.get();
                    this.F.set((this.F.get() & (FLAG_C | FLAG_Z | FLAG_S)) | (this.BC.get() !== 0 ? FLAG_V : 0) | (bytetemp & FLAG_3) | ((bytetemp & 2) > 0 ? FLAG_5 : 0));
                    if (this.BC.get() !== 0)
                    {
                        this.contend(this.DE, 1);
                        this.contend(this.DE, 1);
                        this.contend(this.DE, 1);
                        this.contend(this.DE, 1);
                        this.contend(this.DE, 1);
                        this.PC -= 2;
                    }
                }
                break;
            case 185:
                {
                    const value = this.machine.readByte(this.HL.get());
                    let bytetemp = this.A.get() - value;
                    const lookup = ((this.A.get() & 8) >> 3) | (((value) & 8) >> 2) | ((bytetemp & 8) >> 1);
                    this.contend(this.HL, 3);
                    for (let i = 0; i < 5; ++i) this.contend(this.HL, 1);
                    this.HL.dec();
                    this.BC.dec();
                    this.F.set((this.F.get() & FLAG_C)
                        | (this.BC.get() !== 0 ? (FLAG_V | FLAG_N) : FLAG_N)
                        | Z80.halfCarrySub[lookup]
                        | (bytetemp !== 0 ? 0 : FLAG_Z)
                        | (bytetemp & FLAG_S));
                    if ((this.F.get() & FLAG_H) !== 0) bytetemp--;
                    this.F.or((bytetemp & FLAG_3) | ((bytetemp & 2) !== 0 ? FLAG_5 : 0));
                    if ((this.F.get() & (FLAG_V | FLAG_Z)) === FLAG_V)
                    {
                        this.contend(this.HL, 3);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.PC -= 2;
                    }
                }
                break;
            case 186:
                {
                    const initemp = this.machine.readPort(this.BC.get());
                    this.tstates += 2;
                    this.contendIO(this.BC, 3);
                    this.contend(this.HL, 3);
                    this.machine.writeByte(this.HL.get(), initemp);
                    this.B.dec();
                    this.HL.dec();
                    this.F.set(((initemp & 0x80) !== 0 ? FLAG_N : 0) | Z80.sz53[this.B.get()]);
                    if (this.B.get() !== 0)
                    {
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.contend(this.HL, 1);
                        this.PC -= 2;
                    }
                }
                break;
            case 187:
                {
                    const outitemp = this.machine.readByte(this.HL.get());
                    this.tstates++;
                    this.contend(this.HL, 4);
                    this.B.dec();
                    this.HL.dec();
                    this.machine.writePort(this.BC.get(), outitemp);
                    this.F.set(((outitemp & 0x80) !== 0 ? FLAG_N : 0) | Z80.sz53[this.B.get()]);
                    if (this.B.get() !== 0)
                    {
                        this.contendIO(this.BC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC, 1);
                        this.contend(this.PC - 1, 1);
                        this.PC -= 2;
                    }
                    else
                    {
                        this.contendIO(this.BC, 3);
                    }
                }
                break;
            case 251:
                break;
            default:
                break;
        }
    }

    private doOpcodeDDFD(opcode2: number, REGISTER: RegisterPair, REGISTERL: Register, REGISTERH: Register)
    {
        switch ((opcode2))
        {
            case 9:
                this.ADD16(REGISTER, this.BC);
                break;
            case 25:
                this.ADD16(REGISTER, this.DE);
                break;
            case 33:
                this.contend(this.PC, 3);
                REGISTERL.set(this.machine.readByte(this.PC++));
                this.contend(this.PC, 3);
                REGISTERH.set(this.machine.readByte(this.PC++));
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
                REGISTERH.set(this.machine.readByte(this.PC++));
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
                REGISTERL.set(this.machine.readByte(this.PC++));
                break;
            case 52:
                this.tstates += 0x0f;
                {
                    let d = this.machine.readByte(this.PC++);
                    d = (d < 0x80 ? d : d - 0x100);
                    const wordtemp = REGISTER.get() + d;
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(wordtemp));
                    this.INC(bytetemp);
                    this.machine.writeByte(wordtemp, bytetemp.get());
                }
                break;
            case 53:
                this.tstates += 0x0f;
                {
                    const d = this.machine.readByte(this.PC++);
                    const wordtemp = REGISTER.get() + (d < 0x80 ? d : d - 0x100);
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(wordtemp));
                    this.DEC(bytetemp);
                    this.machine.writeByte(wordtemp, bytetemp.get());
                }
                break;
            case 54:
                this.tstates += 11;
                {
                    const d = this.machine.readByte(this.PC++);
                    const wordtemp = REGISTER.get() + (d < 0x80 ? d : d - 0x100);
                    this.machine.writeByte(wordtemp, this.machine.readByte(this.PC++));
                }
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
                let dist = this.machine.readByte(this.PC++);
                this.B.set(this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100)));
                break;
            case 76:
                this.C.set(REGISTERH);
                break;
            case 77:
                this.C.set(REGISTERL);
                break;
            case 78:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.C.set(this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100)));
                break;
            case 84:
                this.D.set(REGISTERH);
                break;
            case 85:
                this.D.set(REGISTERL);
                break;
            case 86:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.D.set(this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100)));
                break;
            case 92:
                this.E.set(REGISTERH);
                break;
            case 93:
                this.E.set(REGISTERL);
                break;
            case 94:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.E.set(this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100)));
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
                dist = this.machine.readByte(this.PC++);
                this.H.set(this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100)));
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
                dist = this.machine.readByte(this.PC++);
                this.L.set(this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100)));
                break;
            case 111:
                REGISTERL.set(this.A);
                break;
            case 112:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.machine.writeByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100), this.B.get());
                break;
            case 113:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.machine.writeByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100), this.C.get());
                break;
            case 114:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.machine.writeByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100), this.D.get());
                break;
            case 115:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.machine.writeByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100), this.E.get());
                break;
            case 116:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.machine.writeByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100), this.H.get());
                break;
            case 117:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.machine.writeByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100), this.L.get());
                break;
            case 119:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.machine.writeByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100), this.A.get());
                break;
            case 124:
                this.A.set(REGISTERH);
                break;
            case 125:
                this.A.set(REGISTERL);
                break;
            case 126:
                this.tstates += 11;
                dist = this.machine.readByte(this.PC++);
                this.A.set(this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100)));
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
                    dist = this.machine.readByte(this.PC++);
                    const bytetemp = this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100));
                    this.ADD(bytetemp);
                }
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
                    dist = this.machine.readByte(this.PC++);
                    const bytetemp = this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100));
                    this.ADC(bytetemp);
                }
                break;
            case 148:
                this.SUB(REGISTERH.get());
                break;
            case 149:
                this.SUB(REGISTERL.get());
                break;
            case 0x0f0:
                this.tstates += 11;
                {
                    dist = this.machine.readByte(this.PC++);
                    const bytetemp = this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100));
                    this.SUB(bytetemp);
                }
                break;
            case 0x0f6:
                this.SBC(REGISTERH.get());
                break;
            case 0x0f7:
                this.SBC(REGISTERL.get());
                break;
            case 0x0f8:
                this.tstates += 11;
                {
                    dist = this.machine.readByte(this.PC++);
                    const bytetemp = this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100));
                    this.SBC(bytetemp);
                }
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
                    dist = this.machine.readByte(this.PC++);
                    const bytetemp = this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100));
                    this.AND(bytetemp);
                }
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
                    dist = this.machine.readByte(this.PC++);
                    const bytetemp = this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100));
                    this.XOR(bytetemp);
                }
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
                    dist = this.machine.readByte(this.PC++);
                    const bytetemp = this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100));
                    this.OR(bytetemp);
                }
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
                    dist = this.machine.readByte(this.PC++);
                    const bytetemp = this.machine.readByte(REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100));
                    this.CP(bytetemp);
                }
                break;
            case 203:
                {
                    this.contend(this.PC, 3);
                    dist = this.machine.readByte(this.PC++);
                    const tempaddr = REGISTER.get() + (dist < 0x80 ? dist : dist - 0x100);
                    this.contend(this.PC, 4);
                    const opcode3 = this.machine.opcodeFetch(this.PC++);
                    this.doOpcodeDDFDCB(opcode3, tempaddr);
                }
                break;
            case 225:
                REGISTER.set(this.POP16());
                break;
            case 227:
                {
                    const SPvalue = this.SP;
                    const bytetempl = this.machine.readByte(SPvalue);
                    const bytetemph = this.machine.readByte(SPvalue + 1);
                    this.contend(SPvalue, 3);
                    this.contend(SPvalue + 1, 4);
                    this.machine.writeByte(SPvalue, REGISTERL.get());
                    this.machine.writeByte(SPvalue + 1, REGISTERH.get());
                    this.contend(SPvalue, 3);
                    this.contend(SPvalue + 1, 5);
                    REGISTERL.set(bytetempl);
                    REGISTERH.set(bytetemph);
                }
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
                this.R = (this.R - 1) & 0xFF;
                break;
        }
    }

    private doOpcodeDDFDCB(opcode3: number, tempaddr: number)
    {
        switch (opcode3)
        {
            case 0:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr));
                this.RLC(this.B);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 1:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr));
                this.RLC(this.C);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 2:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr));
                this.RLC(this.D);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 3:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr));
                this.RLC(this.E);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 4:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr));
                this.RLC(this.H);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 5:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr));
                this.RLC(this.L);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 6:
                this.tstates += 8;
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(tempaddr));
                    this.RLC(bytetemp);
                    this.machine.writeByte(tempaddr, bytetemp.get());
                }
                break;
            case 7:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr));
                this.RLC(this.A);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 8:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr));
                this.RRC(this.B);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 9:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr));
                this.RRC(this.C);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 10:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr));
                this.RRC(this.D);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 11:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr));
                this.RRC(this.E);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 12:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr));
                this.RRC(this.H);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 13:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr));
                this.RRC(this.L);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 14:
                this.tstates += 8;
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(tempaddr));
                    this.RRC(bytetemp);
                    this.machine.writeByte(tempaddr, bytetemp.get());
                }
                break;
            case 15:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr));
                this.RRC(this.A);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 16:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr));
                this.RL(this.B);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 17:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr));
                this.RL(this.C);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 18:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr));
                this.RL(this.D);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 19:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr));
                this.RL(this.E);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 20:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr));
                this.RL(this.H);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 21:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr));
                this.RL(this.L);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 22:
                this.tstates += 8;
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(tempaddr));
                    this.RL(bytetemp);
                    this.machine.writeByte(tempaddr, bytetemp.get());
                }
                break;
            case 23:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr));
                this.RL(this.A);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 24:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr));
                this.RR(this.B);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 25:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr));
                this.RR(this.C);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 26:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr));
                this.RR(this.D);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 27:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr));
                this.RR(this.E);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 28:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr));
                this.RR(this.H);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 29:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr));
                this.RR(this.L);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 30:
                this.tstates += 8;
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(tempaddr));
                    this.RR(bytetemp);
                    this.machine.writeByte(tempaddr, bytetemp.get());
                }
                break;
            case 31:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr));
                this.RR(this.A);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 32:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr));
                this.SLA(this.B);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 33:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr));
                this.SLA(this.C);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 34:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr));
                this.SLA(this.D);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 35:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr));
                this.SLA(this.E);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 36:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr));
                this.SLA(this.H);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 37:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr));
                this.SLA(this.L);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 38:
                this.tstates += 8;
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(tempaddr));
                    this.SLA(bytetemp);
                    this.machine.writeByte(tempaddr, bytetemp.get());
                }
                break;
            case 39:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr));
                this.SLA(this.A);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 40:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr));
                this.SRA(this.B);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 41:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr));
                this.SRA(this.C);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 42:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr));
                this.SRA(this.D);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 43:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr));
                this.SRA(this.E);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 44:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr));
                this.SRA(this.H);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 45:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr));
                this.SRA(this.L);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 46:
                this.tstates += 8;
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(tempaddr));
                    this.SRA(bytetemp);
                    this.machine.writeByte(tempaddr, bytetemp.get());
                }
                break;
            case 47:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr));
                this.SRA(this.A);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 48:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr));
                this.SLL(this.B);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 49:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr));
                this.SLL(this.C);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 50:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr));
                this.SLL(this.D);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 51:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr));
                this.SLL(this.E);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 52:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr));
                this.SLL(this.H);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 53:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr));
                this.SLL(this.L);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 54:
                this.tstates += 8;
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(tempaddr));
                    this.SLL(bytetemp);
                    this.machine.writeByte(tempaddr, bytetemp.get());
                }
                break;
            case 55:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr));
                this.SLL(this.A);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 56:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr));
                this.SRL(this.B);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 57:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr));
                this.SRL(this.C);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 58:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr));
                this.SRL(this.D);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 59:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr));
                this.SRL(this.E);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 60:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr));
                this.SRL(this.H);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 61:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr));
                this.SRL(this.L);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 62:
                this.tstates += 8;
                {
                    const bytetemp = new Value8();
                    bytetemp.set(this.machine.readByte(tempaddr));
                    this.SRL(bytetemp);
                    this.machine.writeByte(tempaddr, bytetemp.get());
                }
                break;
            case 63:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr));
                this.SRL(this.A);
                this.machine.writeByte(tempaddr, this.A.get());
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
                    const tempreg = new Value8();
                    tempreg.set(this.machine.readByte(tempaddr));
                    this.BIT(0, tempreg);
                }
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
                    const tempreg = new Value8();
                    tempreg.set(this.machine.readByte(tempaddr));
                    this.BIT(1, tempreg);
                }
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
                    const tempreg = new Value8();
                    tempreg.set(this.machine.readByte(tempaddr));
                    this.BIT(2, tempreg);
                }
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
                    const tempreg = new Value8();
                    tempreg.set(this.machine.readByte(tempaddr));
                    this.BIT(3, tempreg);
                }
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
                    const tempreg = new Value8();
                    tempreg.set(this.machine.readByte(tempaddr));
                    this.BIT(4, tempreg);
                }
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
                    const tempreg = new Value8();
                    tempreg.set(this.machine.readByte(tempaddr));
                    this.BIT(5, tempreg);
                }
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
                    const tempreg = new Value8();
                    tempreg.set(this.machine.readByte(tempaddr));
                    this.BIT(6, tempreg);
                }
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
                    const tempreg = new Value8();
                    tempreg.set(this.machine.readByte(tempaddr));
                    this.BIT7(tempreg);
                }
                break;
            case 128:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) & 254);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 129:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) & 254);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 130:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) & 254);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 131:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) & 254);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 132:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) & 254);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 133:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) & 254);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 134:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) & 254);
                break;
            case 135:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) & 254);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 136:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) & 253);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 137:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) & 253);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 138:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) & 253);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 139:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) & 253);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 140:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) & 253);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 141:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) & 253);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 142:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) & 253);
                break;
            case 143:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) & 253);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 144:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) & 251);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 145:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) & 251);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 146:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) & 251);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 147:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) & 251);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 148:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) & 251);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 149:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) & 251);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 150:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) & 251);
                break;
            case 151:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) & 251);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 152:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) & 247);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 153:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) & 247);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 154:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) & 247);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 155:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) & 247);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 156:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) & 247);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 157:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) & 247);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 158:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) & 247);
                break;
            case 159:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) & 247);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 160:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) & 239);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 161:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) & 239);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 162:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) & 239);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 163:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) & 239);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 164:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) & 239);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 165:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) & 239);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 166:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) & 239);
                break;
            case 167:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) & 239);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 168:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) & 223);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 169:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) & 223);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 170:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) & 223);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 171:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) & 223);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 172:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) & 223);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 173:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) & 223);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 174:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) & 223);
                break;
            case 175:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) & 223);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 176:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) & 191);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 177:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) & 191);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 178:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) & 191);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 179:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) & 191);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 180:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) & 191);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 181:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) & 191);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 182:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) & 191);
                break;
            case 183:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) & 191);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 184:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) & 0x7f);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 185:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) & 0x7f);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 186:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) & 0x7f);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 187:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) & 0x7f);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 188:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) & 0x7f);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 189:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) & 0x7f);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 190:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) & 0x7f);
                break;
            case 191:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) & 0x7f);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 192:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) | 1);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 193:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) | 1);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 194:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) | 1);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 195:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) | 1);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 196:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) | 1);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 197:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) | 1);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 198:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) | 1);
                break;
            case 199:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) | 1);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 200:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) | 2);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 201:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) | 2);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 202:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) | 2);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 203:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) | 2);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 204:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) | 2);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 205:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) | 2);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 206:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) | 2);
                break;
            case 207:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) | 2);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 208:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) | 4);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 209:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) | 4);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 210:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) | 4);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 211:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) | 4);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 212:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) | 4);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 213:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) | 4);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 214:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) | 4);
                break;
            case 215:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) | 4);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 216:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) | 8);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 217:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) | 8);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 218:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) | 8);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 219:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) | 8);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 220:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) | 8);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 221:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) | 8);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 222:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) | 8);
                break;
            case 223:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) | 8);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 224:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) | 16);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 225:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) | 16);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 226:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) | 16);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 227:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) | 16);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 228:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) | 16);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 229:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) | 16);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 230:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) | 16);
                break;
            case 231:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) | 16);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 232:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) | 32);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 233:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) | 32);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 234:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) | 32);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 235:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) | 32);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 236:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) | 32);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 237:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) | 32);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 238:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) | 32);
                break;
            case 239:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) | 32);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 240:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) | 64);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 241:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) | 64);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 242:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) | 64);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 243:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) | 64);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 244:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) | 64);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 245:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) | 64);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 246:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) | 64);
                break;
            case 247:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) | 64);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
            case 248:
                this.tstates += 8;
                this.B.set(this.machine.readByte(tempaddr) | 0x80);
                this.machine.writeByte(tempaddr, this.B.get());
                break;
            case 249:
                this.tstates += 8;
                this.C.set(this.machine.readByte(tempaddr) | 0x80);
                this.machine.writeByte(tempaddr, this.C.get());
                break;
            case 250:
                this.tstates += 8;
                this.D.set(this.machine.readByte(tempaddr) | 0x80);
                this.machine.writeByte(tempaddr, this.D.get());
                break;
            case 251:
                this.tstates += 8;
                this.E.set(this.machine.readByte(tempaddr) | 0x80);
                this.machine.writeByte(tempaddr, this.E.get());
                break;
            case 252:
                this.tstates += 8;
                this.H.set(this.machine.readByte(tempaddr) | 0x80);
                this.machine.writeByte(tempaddr, this.H.get());
                break;
            case 253:
                this.tstates += 8;
                this.L.set(this.machine.readByte(tempaddr) | 0x80);
                this.machine.writeByte(tempaddr, this.L.get());
                break;
            case 254:
                this.tstates += 8;
                this.machine.writeByte(tempaddr, this.machine.readByte(tempaddr) | 0x80);
                break;
            case 0xFF:
                this.tstates += 8;
                this.A.set(this.machine.readByte(tempaddr) | 0x80);
                this.machine.writeByte(tempaddr, this.A.get());
                break;
        }
    }
}

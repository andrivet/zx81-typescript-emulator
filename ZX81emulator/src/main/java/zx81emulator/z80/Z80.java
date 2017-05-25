/* ZX81emulator  - A ZX81 emulator.
 * EightyOne Copyright (C) 2003-2006 Michael D Wynne
 * JtyOne Java translation (C) 2006 Simon Holdsworth and others.
 * ZX81emulator Javascript JSweet transcompilation (C) 2017 Sebastien Andrivet.
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package zx81emulator.z80;


import zx81emulator.config.Machine;

public final class Z80 {
    private static final int FLAG_C = 0x01;
    private static final int FLAG_N = 0x02;
    private static final int FLAG_P = 0x04;
    private static final int FLAG_V = FLAG_P;
    private static final int FLAG_3 = 0x08;
    private static final int FLAG_H = 0x10;
    private static final int FLAG_5 = 0x20;
    private static final int FLAG_Z = 0x40;
    private static final int FLAG_S = 0x80;

    /* Whether a half carry occured or not can be determined by looking at
   the 3rd bit of the two arguments and the result; these are hashed
   into this table in the form r12, where r is the 3rd bit of the
   result, 1 is the 3rd bit of the 1st argument and 2 is the
   third bit of the 2nd argument; the tables differ for add and subtract
   operations */
    private final static int[] halfcarry_add_table = {0, FLAG_H, FLAG_H, FLAG_H, 0, 0, 0, FLAG_H};
    private final static int[] halfcarry_sub_table = {0, 0, FLAG_H, 0, FLAG_H, 0, FLAG_H, FLAG_H};

    /* Similarly, overflow can be determined by looking at the 7th bits; again
   the hash into this table is r12 */
    private final static int[] overflow_add_table = {0, 0, 0, FLAG_V, FLAG_V, 0, 0, 0};
    private final static int[] overflow_sub_table = {0, FLAG_V, 0, 0, 0, 0, FLAG_V, 0};

    /* Some more tables; initialised in z80_init_tables() */
    private final static int[] sz53_table = new int[0x100]; /* The S, Z, 5 and 3 bits of the lookup value */
    private final static int[] parity_table = new int[0x100]; /* The parity of the lookup value */
    private final static int[] sz53p_table = new int[0x100]; /* OR the above two tables together */

    private Machine machine;

    /* This is what everything acts on! */
    //processor z80;
    public final MasterRegisterPair BC, DE, HL;
    public final RegisterPair AF;
    public final MasterRegister A, F;
    public final Register B, C;
    public final Register D, E;
    public final Register H, L;
    public final RegisterPair IX, IY;
    public final Register IXH, IXL;
    public final Register IYH, IYL;
    public int SP, PC;
    public int AF_, BC_, DE_, HL_;
    public int I;
    public int R;
    public int R7;
    public int IFF1, IFF2, IM;
    public int halted;

    private final value8 tempreg = new value8("value8");

    /* Set up the z80 emulation */
    public Z80(Machine machine) {
        AF = new SlaveRegisterPair("AF");    // AF is slave as A and F are much more often updated than AF.
        BC = new MasterRegisterPair("BC");
        DE = new MasterRegisterPair("DE");
        HL = new MasterRegisterPair("HL");
        IX = new MasterRegisterPair("IX");
        IY = new MasterRegisterPair("IY");
        A = (MasterRegister) AF.getRH("A");
        F = (MasterRegister) AF.getRL("F");
        B = BC.getRH("B");
        C = BC.getRL("C");
        D = DE.getRH("D");
        E = DE.getRL("E");
        H = HL.getRH("H");
        L = HL.getRL("L");
        IXH = IX.getRH("IXH");
        IXL = IX.getRL("IXL");
        IYH = IY.getRH("IYH");
        IYL = IY.getRL("IYL");

        this.machine = machine;
        init_tables();
    }

    /* Initalise the tables used to set flags */
    //void z80_init_tables(void)
    private void init_tables() {
        int i, j, k;
        int parity;

        for (i = 0; i < 0x100; i++) {
            sz53_table[i] = i & (FLAG_3 | FLAG_5 | FLAG_S);
            j = i;
            parity = 0;
            for (k = 0; k < 8; k++) {
                parity ^= j & 1;
                j >>= 1;
            }

            parity_table[i] = (parity != 0 ? 0 : FLAG_P);
            sz53p_table[i] = sz53_table[i] | parity_table[i];
        }

        sz53_table[0] |= FLAG_Z;
        sz53p_table[0] |= FLAG_Z;
    }

    /* Reset the z80 */
    public void reset() {
        AF.set(0);
        BC.set(0);
        DE.set(0);
        HL.set(0);
        IX.set(0);
        IY.set(0);
        SP = PC = 0;
        AF_ = BC_ = DE_ = HL_ = 0;
        I = R = R7 = 0;
        IFF1 = IFF2 = IM = 0;
        halted = 0;
    }

    /* Process a z80 maskable interrupt */
    public int interrupt(int ts) {
    /* Process if IFF1 set */
        if (IFF1 != 0) {
            if (halted != 0) {
                PC++;
                halted = 0;
            }

            IFF1 = IFF2 = 0;

            machine.writebyte(--SP, PC >> 8);
            machine.writebyte(--SP, PC & 0xff);

            //R++;
            R = (R + 1) & 0xff;

            switch (IM) {
                case 0:
                    PC = 0x0038;
                    return (13);
                case 1:
                    PC = 0x0038;
                    return (13);
                case 2: {
                    int inttemp = (I << 8) | 0xff;
                    PC = machine.readbyte(inttemp++) + (machine.readbyte(inttemp) << 8);

                    return (19);
                }
                default:
                    return (12);
            }
        }
        return (0);
    }

    /* Process a z80 non-maskable interrupt */
    public int nmi(int ts) {
        int waitstates = 0;

        IFF1 = 0;

        if (halted != 0) {
            halted = 0;
            PC++;

            waitstates = (ts / 2) - machine.tperscanline;
            waitstates = 4 - waitstates;
            if (waitstates < 0) waitstates = 0;
        }

        machine.writebyte(--SP, PC >> 8);
        machine.writebyte(--SP, PC & 0xff);
        R = (R + 1) & 0xff;
        PC = 0x0066;

        return (4 + waitstates);
    }

    //==========================================================================================
    // From Z80Macros.
    //==========================================================================================

    private int tstates;

    /* Get the appropriate contended memory delay. Use this macro later
  to avoid a function call if memory contention is disabled */
    private void contend(int address, int time) {
        tstates += machine.contendmem(address, tstates, time);
    }

    private void contend(RegisterPair rp, int time) {
        tstates += machine.contendmem(rp.get(), tstates, time);
    }

    private void contend_io(int port, int time) {
        tstates += machine.contendio(port, tstates, time);
    }

    private void contend_io(RegisterPair rp, int time) {
        tstates += machine.contendio(rp.get(), tstates, time);
    }

    /* Some commonly used instructions */
    private void AND(Register r) {
        A.value &= r.get();
        F.set(FLAG_H | sz53p_table[A.value]);
    }

    private void AND(int value) {
        A.value &= value;
        F.set(FLAG_H | sz53p_table[A.value]);
    }

    private void ADC(int value) {
        int adctemp = A.value + value + (F.value & FLAG_C);
        int lookup = ((A.value & 0x88) >> 3) | (((value) & 0x88) >> 2) |
                ((adctemp & 0x88) >> 1);
        A.set(adctemp);
        F.set(((adctemp & 0x100) > 0 ? FLAG_C : 0) |
                halfcarry_add_table[lookup & 0x07] | overflow_add_table[lookup >> 4] |
                sz53_table[A.value]);
    }

    private void ADC16(int value) {
        int add16temp = HL.word + value + (F.value & FLAG_C);
        int lookup = ((HL.word & 0x8800) >> 11) |
                ((value & 0x8800) >> 10) |
                ((add16temp & 0x8800) >> 9);
        HL.set(add16temp);
        F.set(((add16temp & 0x10000) > 0 ? FLAG_C : 0) |
                overflow_add_table[lookup >> 4] |
                (H.get() & (FLAG_3 | FLAG_5 | FLAG_S)) |
                halfcarry_add_table[lookup & 0x07] |
                (HL.word == 0 ? 0 : FLAG_Z));
    }

    private void ADD(int value) {
        int addtemp = A.value + value;
        int lookup = ((A.value & 0x88) >> 3) | (((value) & 0x88) >> 2) |
                ((addtemp & 0x88) >> 1);
        A.set(addtemp);
        F.set(((addtemp & 0x100) > 0 ? FLAG_C : 0) |
                halfcarry_add_table[lookup & 0x07] | overflow_add_table[lookup >> 4] |
                sz53_table[A.value]);
    }

    private void ADD16(RegisterPair rp1, RegisterPair rp2) {
        int add16temp = rp1.get() + rp2.get();
        int lookup = ((rp1.get() & 0x0800) >> 11) |
                ((rp2.get() & 0x0800) >> 10) |
                ((add16temp & 0x0800) >> 9);
        tstates += 7;
        rp1.set(add16temp);
        F.set((F.value & (FLAG_V | FLAG_Z | FLAG_S)) |
                ((add16temp & 0x10000) > 0 ? FLAG_C : 0) |
                ((add16temp >> 8) & (FLAG_3 | FLAG_5)) |
                halfcarry_add_table[lookup]);
    }

    private void ADD16(RegisterPair rp1, int value) {
        int add16temp = rp1.get() + value;
        int lookup = ((rp1.get() & 0x0800) >> 11) |
                ((value & 0x0800) >> 10) |
                ((add16temp & 0x0800) >> 9);
        tstates += 7;
        rp1.set(add16temp);
        F.set((F.value & (FLAG_V | FLAG_Z | FLAG_S)) |
                ((add16temp & 0x10000) > 0 ? FLAG_C : 0) |
                ((add16temp >> 8) & (FLAG_3 | FLAG_5)) |
                halfcarry_add_table[lookup]);
    }

    private void BIT(int bit, Register value) {
        F.set((F.value & FLAG_C) | (value.get() & (FLAG_3 | FLAG_5)) |
                ((value.get() & (0x01 << bit)) > 0 ? FLAG_H : (FLAG_P | FLAG_H | FLAG_Z)));
    }

    private void BIT7(Register value) {
        F.set((F.value & FLAG_C) | (value.get() & (FLAG_3 | FLAG_5)) |
                ((value.get() & 0x80) > 0 ? (FLAG_H | FLAG_S) :
                        (FLAG_P | FLAG_H | FLAG_Z)));
    }

    private void CALL() {
        int calltempl = machine.readbyte(PC++);
        contend(PC, 1);
        int calltemph = machine.readbyte(PC++);
        PUSH16(PC);
        PC = calltempl + (calltemph << 8);
    }

    private void CP(int value) {
        int cptemp = A.value - value;
        int lookup = ((A.value & 0x88) >> 3) | (((value) & 0x88) >> 2) |
                ((cptemp & 0x88) >> 1);
        F.set(((cptemp & 0x100) > 0 ? FLAG_C : (cptemp > 0 ? 0 : FLAG_Z)) | FLAG_N |
                halfcarry_sub_table[lookup & 0x07] |
                overflow_sub_table[lookup >> 4] |
                (value & (FLAG_3 | FLAG_5)) |
                (cptemp & FLAG_S));
    }

/* Macro for the {DD,FD} CB dd xx rotate/shift instructions */
//#define DDFDCB_ROTATESHIFT(time, target, instruction)\
//tstates+=(time);\
//{\
// (target) = machine.readbyte( tempaddr );\
// instruction( (target) );\
// writebyte( tempaddr, (target) );\
//}\
//break
//TODO: figure out how to implement this!

    private void DEC(Register reg) {
        F.set((F.value & FLAG_C) | ((reg.get() & 0x0f) > 0 ? 0 : FLAG_H) | FLAG_N);
        reg.dec();
        F.or((reg.get() == 0x7f ? FLAG_V : 0) | sz53_table[reg.get()]);
        //if( DEBUG && !reg.name.equals("value8")) System.out.print(" DEC "+reg.name+" ; "+reg);
    }

    private void IN(Register reg, RegisterPair rp) {
        int port = rp.get();
        contend_io(port, 3);
        reg.set(machine.readport(port));
        F.set((F.value & FLAG_C) | sz53p_table[reg.get()]);
    }

    private void IN(Register reg, int port) {
        contend_io(port, 3);
        reg.set(machine.readport(port));
        F.set((F.value & FLAG_C) | sz53p_table[reg.get()]);
    }

    private void IN(int value, RegisterPair rp) {
        contend_io(rp, 3);
        machine.readport(rp.get());
        F.set((F.value & FLAG_C) | sz53p_table[value]);
    }

    private void INC(Register reg) {
        reg.inc();
        F.set((F.value & FLAG_C) | (reg.get() == 0x80 ? FLAG_V : 0) |
                ((reg.get() & 0x0f) > 0 ? 0 : FLAG_H) | sz53_table[reg.get()]);
        //if( DEBUG ) System.out.print(" INC "+reg.name+" ; "+reg);
    }

    private void LD16_NNRR(int value) {
        contend(PC, 3);
        int ldtemp = machine.readbyte(PC++);
        contend(PC, 3);
        ldtemp |= machine.readbyte(PC++) << 8;
        //if( DEBUG ) System.out.print(" LD ("+toHex16(ldtemp)+"),"+regl.rp.name+" ; "+regl.rp);
        contend(ldtemp, 3);
        machine.writebyte(ldtemp++, value & 0xff);
        contend(ldtemp, 3);
        machine.writebyte(ldtemp, value >> 8);
    }

    private int LD16_RRNN() {
        contend(PC, 3);
        int ldtemp = machine.readbyte(PC++);
        contend(PC, 3);
        ldtemp |= machine.readbyte(PC++) << 8;
        //if( DEBUG ) System.out.print(" LD "+regl.rp.name+",("+toHex16(ldtemp)+")");
        contend(ldtemp, 3);
        contend(ldtemp, 3);
        return machine.readbyte(ldtemp++) + (machine.readbyte(ldtemp) << 8);
        //if( DEBUG ) System.out.print(" ; "+regl.rp);
    }

    private void JP() {
        int jptemp = PC;
        PC = machine.readbyte(jptemp++) + (machine.readbyte(jptemp) << 8);
    }

    private void JR() {
        contend(PC, 1);
        contend(PC, 1);
        contend(PC, 1);
        contend(PC, 1);
        contend(PC, 1);
        int dist = machine.readbyte(PC);
        dist = (dist < 128 ? dist : dist - 256);
        PC += dist;
    }

    private void OR(Register r) {
        A.value |= r.get();
        F.set(sz53p_table[A.value]);
    }

    private void OR(int value) {
        A.value |= value;
        F.set(sz53p_table[A.value]);
    }

    private void OUT(RegisterPair rp, Register reg) {
        contend_io(rp.get(), 3);
        machine.writeport(rp.get(), reg.get());
    }

    private void OUT(RegisterPair rp, int value) {
        contend_io(rp.get(), 3);
        machine.writeport(rp.get(), value);
    }

    private void OUT(int port, Register reg) {
        contend_io(port, 3);
        machine.writeport(port, reg.get());
    }

    private int POP16() {
        contend(SP, 3);
        contend(SP, 3);
        return machine.readbyte(SP++) + (machine.readbyte(SP++) << 8);
    }

    private void PUSH16(int value) {
        SP--;
        contend(SP, 3);
        machine.writebyte(SP, value >> 8);
        SP--;
        contend(SP, 3);
        machine.writebyte(SP, value & 0xff);
    }


    private void RET() {
        PC = POP16();
    }

    private void RL(Register reg) {
        int rltemp = reg.get();
        reg.set((rltemp << 1) | (F.value & FLAG_C));
        F.set((rltemp >> 7) | sz53p_table[reg.get()]);
        //if( DEBUG ) System.out.print(" RL "+value.name+" ; "+value.get());
    }

    private void RLC(Register reg) {
        int before = reg.get();
        int newValue = (before << 1) | (before >> 7);
        reg.set(newValue);
        int after = reg.get();
        F.set((after & FLAG_C) | sz53p_table[after]);
        //if( DEBUG ) System.out.print(" RLC "+value.name+" ; "+value.get());
    }

    private void RR(Register reg) {
        int rrtemp = reg.get();
        reg.set((reg.get() >> 1) | (F.value << 7));
        F.set((rrtemp & FLAG_C) | sz53p_table[reg.get()]);
        //if( DEBUG ) System.out.print(" RR "+value.name+" ; "+value.get());
    }

    private void RRC(Register reg) {
        F.set(reg.get() & FLAG_C);
        reg.set((reg.get() >> 1) | (reg.get() << 7));
        F.or(sz53p_table[reg.get()]);
        //if( DEBUG ) System.out.print(" RRC "+value.name+" ; "+value.get());
    }

    private void RST(int value) {
        PUSH16(PC);
        PC = value;
        //if( DEBUG ) System.out.print(" RST "+toHex8(value));
    }

    private void SBC(int value) {
        int sbctemp = A.value - (value) - (F.value & FLAG_C);
        int lookup = ((A.value & 0x88) >> 3) | (((value) & 0x88) >> 2) |
                ((sbctemp & 0x88) >> 1);
        A.set(sbctemp);
        F.set(((sbctemp & 0x100) > 0 ? FLAG_C : 0) | FLAG_N |
                halfcarry_sub_table[lookup & 0x07] | overflow_sub_table[lookup >> 4] |
                sz53_table[A.value]);
    }

    private void SBC16(int value) {
        int sub16temp = HL.word - (value) - (F.value & FLAG_C);
        int lookup = ((HL.word & 0x8800) >> 11) |
                (((value) & 0x8800) >> 10) |
                ((sub16temp & 0x8800) >> 9);
        HL.set(sub16temp);
        F.set(((sub16temp & 0x10000) > 0 ? FLAG_C : 0) |
                FLAG_N | overflow_sub_table[lookup >> 4] |
                (H.get() & (FLAG_3 | FLAG_5 | FLAG_S)) |
                halfcarry_sub_table[lookup & 0x07] |
                (HL.word > 0 ? 0 : FLAG_Z));
    }

    private void SLA(Register reg) {
        F.set(reg.get() >> 7);
        reg.set(reg.get() << 1);
        F.or(sz53p_table[reg.get()]);
        //if( DEBUG ) System.out.print(" SLA "+value.name+" ; "+value.get());
    }

    private void SLL(Register reg) {
        F.set(reg.get() >> 7);
        reg.set((reg.get() << 1) | 0x01);
        F.or(sz53p_table[reg.get()]);
        //if( DEBUG ) System.out.print(" SLL "+value.name+" ; "+value.get());
    }

    private void SRA(Register reg) {
        F.set(reg.get() & FLAG_C);
        reg.set((reg.get() & 0x80) | (reg.get() >> 1));
        F.or(sz53p_table[reg.get()]);
        //if( DEBUG ) System.out.print(" SRA "+value.name+" ; "+value.get());
    }

    private void SRL(Register reg) {
        F.set(reg.get() & FLAG_C);
        reg.set(reg.get() >> 1);
        F.or(sz53p_table[reg.get()]);
        //if( DEBUG ) System.out.print(" SRL "+value.name+" ; "+value.get());
    }

    private void SUB(int value) {
        int subtemp = A.value - (value);
        int lookup = ((A.value & 0x88) >> 3) | (((value) & 0x88) >> 2) |
                ((subtemp & 0x88) >> 1);
        A.set(subtemp);
        F.set(((subtemp & 0x100) > 0 ? FLAG_C : 0) | FLAG_N |
                halfcarry_sub_table[lookup & 0x07] | overflow_sub_table[lookup >> 4] |
                sz53_table[A.value]);
    }

    private void XOR(Register r) {
        A.set(A.value ^ r.get());
        F.set(sz53p_table[A.value]);
    }

    private void XOR(int value) {
        A.set(A.value ^ (value));
        F.set(sz53p_table[A.value]);
    }


    //==========================================================================================
    // From Z80_Ops.
    //==========================================================================================

    public int do_opcode() {
        tstates = 0;

    /* Do the instruction fetch; readbyte_internal used here to avoid
       triggering read breakpoints */

        contend(PC, 4);
        R = (R + 1) & 0xff;

        int opcode = machine.opcode_fetch(PC++);

        //if( DEBUG ) System.out.print(": "+opToHex8(opcode));

        switch (opcode) {
            case 0x00:    /* NOP */
                break;
            case 0x01:    /* LD BC,nnnn */
                contend(PC, 3);
                C.set(machine.readbyte(PC++));
                contend(PC, 3);
                B.set(machine.readbyte(PC++));
                //if( DEBUG ) System.out.print(" LD BC,"+BC);
                break;
            case 0x02:    /* LD (BC),A */
                contend(BC, 3);
                machine.writebyte(BC.word, A.value);
                break;
            case 0x03:    /* INC BC */
                tstates += 2;
                BC.inc();
                //if( DEBUG ) System.out.print(" INC BC ; "+BC);
                break;
            case 0x04:    /* INC B */
                INC(B);
                break;
            case 0x05:    /* DEC B */
                DEC(B);
                break;
            case 0x06:    /* LD B,nn */
                contend(PC, 3);
                B.set(machine.readbyte(PC++));
                //if( DEBUG ) System.out.print(" LD B,"+B);
                break;
            case 0x07:    /* RLCA */
                A.set((A.value << 1) | (A.value >> 7));
                F.set((F.value & (FLAG_P | FLAG_Z | FLAG_S)) |
                        (A.value & (FLAG_C | FLAG_3 | FLAG_5)));
                break;
            case 0x08:    /* EX AF,AF' */ {
                int wordtemp = AF.get();
                AF.set(AF_);
                AF_ = wordtemp;
                //if( DEBUG ) System.out.print(" EX AF,AF'");
            }

            break;
            case 0x09:    /* ADD HL,BC */
                ADD16(HL, BC);
                //if( DEBUG ) System.out.print(" ADD HL,BC ; "+HL);
                break;
            case 0x0a:    /* LD A,(BC) */
                contend(BC, 3);
                A.set(machine.readbyte(BC.word));
                break;
            case 0x0b:    /* DEC BC */
                tstates += 2;
                BC.dec();
                break;
            case 0x0c:    /* INC C */
                INC(C);
                break;
            case 0x0d:    /* DEC C */
                DEC(C);
                break;
            case 0x0e:    /* LD C,nn */
                contend(PC, 3);
                C.set(machine.readbyte(PC++));
                //if( DEBUG ) System.out.print(" LD C,"+C);
                break;
            case 0x0f:    /* RRCA */
                F.set((F.value & (FLAG_P | FLAG_Z | FLAG_S)) | (A.value & FLAG_C));
                A.set((A.value >> 1) | (A.value << 7));
                F.or(A.value & (FLAG_3 | FLAG_5));
                break;
            case 0x10:    /* DJNZ offset */
                tstates++;
                contend(PC, 3);
                B.dec();
                //if( DEBUG ) System.out.print(" DJNZ B now "+B);

                if (B.get() != 0) {
                    JR();
                }
                PC++;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0x11:    /* LD DE,nnnn */
                contend(PC, 3);
                E.set(machine.readbyte(PC++));
                contend(PC, 3);
                //D=machine.readbyte(PC++);
                D.set(machine.readbyte(PC++));
                break;
            case 0x12:    /* LD (DE),A */
                contend(DE, 3);
                machine.writebyte(DE.word, A.value);
                break;
            case 0x13:    /* INC DE */
                tstates += 2;
                DE.inc();
                break;
            case 0x14:    /* INC D */
                INC(D);
                break;
            case 0x15:    /* DEC D */
                DEC(D);
                break;
            case 0x16:    /* LD D,nn */
                contend(PC, 3);
                D.set(machine.readbyte(PC++));
                break;
            case 0x17:    /* RLA */ {
                int bytetemp = A.value;
                A.set((A.value << 1) | (F.value & FLAG_C));
                F.set((F.value & (FLAG_P | FLAG_Z | FLAG_S)) |
                        (A.value & (FLAG_3 | FLAG_5)) | (bytetemp >> 7));
                //if( DEBUG ) System.out.print(" RLA ; "+A);
            }
            break;
            case 0x18:    /* JR offset */
                contend(PC, 3);
                JR();
                PC++;
                //if( DEBUG ) System.out.print(" JR "+PC);
                break;
            case 0x19:    /* ADD HL,DE */
                ADD16(HL, DE);
                break;
            case 0x1a:    /* LD A,(DE) */
                contend(DE, 3);
                A.set(machine.readbyte(DE.word));
                break;
            case 0x1b:    /* DEC DE */
                tstates += 2;
                DE.dec();
                break;
            case 0x1c:    /* INC E */
                INC(E);
                break;
            case 0x1d:    /* DEC E */
                DEC(E);
                break;
            case 0x1e:    /* LD E,nn */
                contend(PC, 3);
                E.set(machine.readbyte(PC++));
                break;
            case 0x1f:    /* RRA */ {
                int bytetemp = A.value;
                A.set((A.value >> 1) | (F.value << 7));
                F.set((F.value & (FLAG_P | FLAG_Z | FLAG_S)) |
                        (A.value & (FLAG_3 | FLAG_5)) | (bytetemp & FLAG_C));
            }
            break;
            case 0x20:    /* JR NZ,offset */
                contend(PC, 3);
                //if( DEBUG ) System.out.print(" JR NZ will jump: "+((F.value & FLAG_Z)==0));
                if ((F.value & FLAG_Z) == 0) {
                    JR();
                }
                PC++;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0x21:    /* LD HL,nnnn */
                contend(PC, 3);
                L.set(machine.readbyte(PC++));
                contend(PC, 3);
                H.set(machine.readbyte(PC++));
                //if( DEBUG ) System.out.print(" LD HL,"+HL);
                break;
            case 0x22:    /* LD (nnnn),HL */
                LD16_NNRR(HL.word);
                break;
            case 0x23:    /* INC HL */
                tstates += 2;
                HL.inc();
                //if( DEBUG ) System.out.print(" INC HL ; "+HL);
                break;
            case 0x24:    /* INC H */
                INC(H);
                break;
            case 0x25:    /* DEC H */
                DEC(H);
                break;
            case 0x26:    /* LD H,nn */
                contend(PC, 3);
                //H=machine.readbyte(PC++);
                H.set(machine.readbyte(PC++));
                break;
            case 0x27:    /* DAA */ {
                int add = 0, carry = (F.value & FLAG_C);
                if (((F.value & FLAG_H) != 0) || ((A.value & 0x0f) > 9)) add = 6;
                if (carry != 0 || (A.value > 0x9f)) add |= 0x60;
                if (A.value > 0x99) carry = 1;
                if ((F.value & FLAG_N) != 0) {
                    SUB(add);
                } else {
                    if ((A.value > 0x90) && ((A.value & 0x0f) > 9)) add |= 0x60;
                    ADD(add);
                }
                F.set((F.value & ~(FLAG_C | FLAG_P)) | carry | parity_table[A.value]);
            }
            break;
            case 0x28:    /* JR Z,offset */
                contend(PC, 3);
                //if( DEBUG ) System.out.print(" JR Z will jump: "+((F.value & FLAG_Z)!=0));
                if ((F.value & FLAG_Z) != 0) {
                    JR();
                }
                PC++;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0x29:    /* ADD HL,HL */
                ADD16(HL, HL);
                break;
            case 0x2a:    /* LD HL,(nnnn) */
                HL.set(LD16_RRNN());
                break;
            case 0x2b:    /* DEC HL */
                tstates += 2;
                HL.dec();
                //if( DEBUG ) System.out.print(" DEC HL ; "+HL);
                break;
            case 0x2c:    /* INC L */
                INC(L);
                break;
            case 0x2d:    /* DEC L */
                DEC(L);
                break;
            case 0x2e:    /* LD L,nn */
                contend(PC, 3);
                L.set(machine.readbyte(PC++));
                break;
            case 0x2f:    /* CPL */
                A.set(A.value ^ 0xff);
                F.set((F.value & (FLAG_C | FLAG_P | FLAG_Z | FLAG_S)) |
                        (A.value & (FLAG_3 | FLAG_5)) | (FLAG_N | FLAG_H));
                break;
            case 0x30:    /* JR NC,offset */
                contend(PC, 3);
                //if( DEBUG ) System.out.print(" JR NC will jump: "+((F.value & FLAG_C)==0));
                if ((F.value & FLAG_C) == 0) {
                    JR();
                }
                PC++;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0x31:    /* LD SP,nnnn */
                contend(PC, 3);
                contend(PC, 3);
                SP = machine.readbyte(PC++) + (machine.readbyte(PC++) << 8);
                //if( DEBUG ) System.out.print(" LD SP,"+SP);
                break;
            case 0x32:    /* LD (nnnn),A */
                contend(PC, 3);
            {
                int wordtemp = machine.readbyte(PC++);
                contend(PC, 3);
                wordtemp |= machine.readbyte(PC++) << 8;
                contend(wordtemp, 3);
                machine.writebyte(wordtemp, A.value);
            }
            break;
            case 0x33:    /* INC SP */
                tstates += 2;
                SP++;
                break;
            case 0x34:    /* INC (HL) */
                contend(HL, 4);
            {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                INC(bytetemp);
                contend(HL, 3);
                machine.writebyte(HL.word, bytetemp.get());
            }
            break;
            case 0x35:    /* DEC (HL) */
                contend(HL, 4);
            {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                DEC(bytetemp);
                contend(HL, 3);
                machine.writebyte(HL.word, bytetemp.get());
                //if( DEBUG ) System.out.print(" DEC (HL) ; $"+toHex8(bytetemp));
            }
            break;
            case 0x36:    /* LD (HL),nn */
                contend(PC, 3);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(PC++));
                //if( DEBUG ) System.out.print(" LD (HL),"+toHex8((ZX81.memory[HL()])&0xff));
                break;
            case 0x37:    /* SCF */
                F.and(~(FLAG_N | FLAG_H));
                F.or((A.value & (FLAG_3 | FLAG_5)) | FLAG_C);
                //if( DEBUG ) System.out.print(" SCF ; "+F.toBinaryString());
                break;
            case 0x38:    /* JR C,offset */
                contend(PC, 3);
                //if( DEBUG ) System.out.print(" JR C will jump: "+((F.value & FLAG_C)!=0));
                if ((F.value & FLAG_C) != 0) {
                    JR();
                }
                PC++;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0x39:    /* ADD HL,SP */
                ADD16(HL, SP);
                break;
            case 0x3a:    /* LD A,(nnnn) */ {
                int wordtemp;
                contend(PC, 3);
                wordtemp = machine.readbyte(PC++);
                contend(PC, 3);
                wordtemp |= (machine.readbyte(PC++) << 8);
                contend(wordtemp, 3);
                A.set(machine.readbyte(wordtemp));
                //if( DEBUG ) System.out.print(" LD A,("+toHex16(wordtemp)+") ; "+A);
            }
            break;
            case 0x3b:    /* DEC SP */
                tstates += 2;
                SP--;
                break;
            case 0x3c:    /* INC A */
                INC(A);
                break;
            case 0x3d:    /* DEC A */
                DEC(A);
                break;
            case 0x3e:    /* LD A,nn */
                contend(PC, 3);
                A.set(machine.readbyte(PC++));
                //if( DEBUG ) System.out.print(" LD A,"+A);
                break;
            case 0x3f:    /* CCF */
                F.set((F.value & (FLAG_P | FLAG_Z | FLAG_S)) |
                        ((F.value & FLAG_C) != 0 ? FLAG_H : FLAG_C) | (A.value & (FLAG_3 | FLAG_5)));
                break;
            case 0x40:    /* LD B,B */
                break;
            case 0x41:    /* LD B,C */
                B.set(C);
                //if( DEBUG ) System.out.print(" LD B,C ; "+B);
                break;
            case 0x42:    /* LD B,D */
                B.set(D);
                //if( DEBUG ) System.out.print(" LD B,D ; "+B);
                break;
            case 0x43:    /* LD B,E */
                B.set(E);
                //if( DEBUG ) System.out.print(" LD B,E ; "+B);
                break;
            case 0x44:    /* LD B,H */
                B.set(H);
                //if( DEBUG ) System.out.print(" LD B,H ; "+B);
                break;
            case 0x45:    /* LD B,L */
                B.set(L);
                //if( DEBUG ) System.out.print(" LD B,L ; "+B);
                break;
            case 0x46:    /* LD B,(HL) */
                contend(HL, 3);
                B.set(machine.readbyte(HL.word));
                //if( DEBUG ) System.out.print(" LD B,(HL) ; "+B);
                break;
            case 0x47:    /* LD B,A */
                B.set(A);
                //if( DEBUG ) System.out.print(" LD B,A ; "+B);
                break;
            case 0x48:    /* LD C,B */
                C.set(B);
                //if( DEBUG ) System.out.print(" LD C,B ; "+C);
                break;
            case 0x49:    /* LD C,C */
                //if( DEBUG ) System.out.print(" LD C,C ; "+C);
                break;
            case 0x4a:    /* LD C,D */
                C.set(D);
                //if( DEBUG ) System.out.print(" LD C,D ; "+C);
                break;
            case 0x4b:    /* LD C,E */
                C.set(E);
                //if( DEBUG ) System.out.print(" LD C,E ; "+C);
                break;
            case 0x4c:    /* LD C,H */
                C.set(H);
                //if( DEBUG ) System.out.print(" LD C,H ; "+C);
                break;
            case 0x4d:    /* LD C,L */
                C.set(L);
                //if( DEBUG ) System.out.print(" LD C,L ; "+C);
                break;
            case 0x4e:    /* LD C,(HL) */
                contend(HL, 3);
                C.set(machine.readbyte(HL.word));
                //if( DEBUG ) System.out.print(" LD C,(HL) ; "+C);
                break;
            case 0x4f:    /* LD C,A */
                C.set(A);
                //if( DEBUG ) System.out.print(" LD C,A ; "+C);
                break;
            case 0x50:    /* LD D,B */
                D.set(B);
                //if( DEBUG ) System.out.print(" LD D,B ; "+D);
                break;
            case 0x51:    /* LD D,C */
                D.set(C);
                //if( DEBUG ) System.out.print(" LD D,C ; "+D);
                break;
            case 0x52:    /* LD D,D */
                //if( DEBUG ) System.out.print(" LD D,D ; "+D);
                break;
            case 0x53:    /* LD D,E */
                D.set(E);
                //if( DEBUG ) System.out.print(" LD D,E ; "+D);
                break;
            case 0x54:    /* LD D,H */
                D.set(H);
                //if( DEBUG ) System.out.print(" LD D,H ; "+D);
                break;
            case 0x55:    /* LD D,L */
                D.set(L);
                //if( DEBUG ) System.out.print(" LD D,L ; "+D);
                break;
            case 0x56:    /* LD D,(HL) */
                contend(HL, 3);
                D.set(machine.readbyte(HL.word));
                //if( DEBUG ) System.out.print(" LD D,(HL) ; "+D);
                break;
            case 0x57:    /* LD D,A */
                D.set(A);
                //if( DEBUG ) System.out.print(" LD D,A ; "+D);
                break;
            case 0x58:    /* LD E,B */
                E.set(B);
                //if( DEBUG ) System.out.print(" LD E,B ; "+E);
                break;
            case 0x59:    /* LD E,C */
                E.set(C);
                //if( DEBUG ) System.out.print(" LD E,C ; "+E);
                break;
            case 0x5a:    /* LD E,D */
                E.set(D);
                //if( DEBUG ) System.out.print(" LD E,D ; "+E);
                break;
            case 0x5b:    /* LD E,E */
                //if( DEBUG ) System.out.print(" LD E,E ; "+E);
                break;
            case 0x5c:    /* LD E,H */
                E.set(H);
                //if( DEBUG ) System.out.print(" LD E,H ; "+E);
                break;
            case 0x5d:    /* LD E,L */
                E.set(L);
                //if( DEBUG ) System.out.print(" LD E,L ; "+E);
                break;
            case 0x5e:    /* LD E,(HL) */
                contend(HL, 3);
                E.set(machine.readbyte(HL.word));
                //if( DEBUG ) System.out.print(" LD E,(HL) ; "+E);
                break;
            case 0x5f:    /* LD E,A */
                E.set(A);
                //if( DEBUG ) System.out.print(" LD E,A ; "+E);
                break;
            case 0x60:    /* LD H,B */
                H.set(B);
                //if( DEBUG ) System.out.print(" LD H,B ; "+H);
                break;
            case 0x61:    /* LD H,C */
                H.set(C);
                break;
            case 0x62:    /* LD H,D */
                H.set(D);
                break;
            case 0x63:    /* LD H,E */
                H.set(E);
                break;
            case 0x64:    /* LD H,H */
                break;
            case 0x65:    /* LD H,L */
                H.set(L);
                break;
            case 0x66:    /* LD H,(HL) */
                contend(HL, 3);
                H.set(machine.readbyte(HL.word));
                break;
            case 0x67:    /* LD H,A */
                H.set(A);
                break;
            case 0x68:    /* LD L,B */
                L.set(B);
                break;
            case 0x69:    /* LD L,C */
                L.set(C);
                //if( DEBUG ) System.out.print(" LD L,C ; "+L);
                break;
            case 0x6a:    /* LD L,D */
                L.set(D);
                //if( DEBUG ) System.out.print(" L,D ; "+L);
                break;
            case 0x6b:    /* LD L,E */
                L.set(E);
                //if( DEBUG ) System.out.print(" L,E ; "+L);
                break;
            case 0x6c:    /* LD L,H */
                L.set(H);
                //if( DEBUG ) System.out.print(" L,H ; "+L);
                break;
            case 0x6d:    /* LD L,L */
                //if( DEBUG ) System.out.print(" L,L ; "+L);
                break;
            case 0x6e:    /* LD L,(HL) */
                contend(HL, 3);
                L.set(machine.readbyte(HL.word));
                //if( DEBUG ) System.out.print(" L,(HL) ; "+L);
                break;
            case 0x6f:    /* LD L,A */
                L.set(A);
                //if( DEBUG ) System.out.print(" L,A ; "+L);
                break;
            case 0x70:    /* LD (HL),B */
                contend(HL, 3);
                machine.writebyte(HL.word, B.get());
                //if( DEBUG ) System.out.print(" LD (HL),B ; "+B);
                break;
            case 0x71:    /* LD (HL),C */
                contend(HL, 3);
                machine.writebyte(HL.word, C.get());
                //if( DEBUG ) System.out.print(" LD (HL),C ; "+C);
                break;
            case 0x72:    /* LD (HL),D */
                contend(HL, 3);
                machine.writebyte(HL.word, D.get());
                //if( DEBUG ) System.out.print(" LD (HL),D ; "+D);
                break;
            case 0x73:    /* LD (HL),E */
                contend(HL, 3);
                machine.writebyte(HL.word, E.get());
                //if( DEBUG ) System.out.print(" LD (HL),E ; "+E);
                break;
            case 0x74:    /* LD (HL),H */
                contend(HL, 3);
                machine.writebyte(HL.word, H.get());
                //if( DEBUG ) System.out.print(" LD (HL),H ; "+H);
                break;
            case 0x75:    /* LD (HL),L */
                contend(HL, 3);
                machine.writebyte(HL.word, L.get());
                //if( DEBUG ) System.out.print(" LD (HL),L ; "+L);
                break;
            case 0x76:    /* HALT */
                halted = 1;
                PC--;
                //if( DEBUG ) System.out.print(" HALT");
                break;
            case 0x77:    /* LD (HL),A */
                contend(HL, 3);
                machine.writebyte(HL.word, A.value);
                //if( DEBUG ) System.out.print(" LD (HL),A ; "+A);
                break;
            case 0x78:    /* LD A,B */
                A.set(B);
                //if( DEBUG ) System.out.print(" LD A,B ; "+A);
                break;
            case 0x79:    /* LD A,C */
                A.set(C);
                //if( DEBUG ) System.out.print(" LD A,C ; "+A);
                break;
            case 0x7a:    /* LD A,D */
                A.set(D);
                //if( DEBUG ) System.out.print(" LD A,D ; "+A);
                break;
            case 0x7b:    /* LD A,E */
                A.set(E);
                //if( DEBUG ) System.out.print(" LD A,E ; "+A);
                break;
            case 0x7c:    /* LD A,H */
                A.set(H);
                //if( DEBUG ) System.out.print(" LD A,H ; "+A);
                break;
            case 0x7d:    /* LD A,L */
                A.set(L);
                //if( DEBUG ) System.out.print(" LD A,L ; "+A);
                break;
            case 0x7e:    /* LD A,(HL) */
                contend(HL, 3);
                A.set(machine.readbyte(HL.word));
                //if( DEBUG ) System.out.print(" LD A,(HL) ; "+A);
                break;
            case 0x7f:    /* LD A,A */
                //if( DEBUG ) System.out.print(" LD A,A ; "+A);
                break;
            case 0x80:    /* ADD A,B */
                ADD(B.get());
                break;
            case 0x81:    /* ADD A,C */
                ADD(C.get());
                break;
            case 0x82:    /* ADD A,D */
                ADD(D.get());
                break;
            case 0x83:    /* ADD A,E */
                ADD(E.get());
                break;
            case 0x84:    /* ADD A,H */
                ADD(H.get());
                break;
            case 0x85:    /* ADD A,L */
                ADD(L.get());
                break;
            case 0x86:    /* ADD A,(HL) */
                contend(HL, 3);
            {
                int bytetemp = machine.readbyte(HL.word);
                ADD(bytetemp);
            }
            break;
            case 0x87:    /* ADD A,A */
                ADD(A.get());
                break;
            case 0x88:    /* ADC A,B */
                ADC(B.get());
                break;
            case 0x89:    /* ADC A,C */
                ADC(C.get());
                break;
            case 0x8a:    /* ADC A,D */
                ADC(D.get());
                break;
            case 0x8b:    /* ADC A,E */
                ADC(E.get());
                break;
            case 0x8c:    /* ADC A,H */
                ADC(H.get());
                break;
            case 0x8d:    /* ADC A,L */
                ADC(L.get());
                break;
            case 0x8e:    /* ADC A,(HL) */
                contend(HL, 3);
            {
                int bytetemp = machine.readbyte(HL.word);
                ADC(bytetemp);
            }
            break;
            case 0x8f:    /* ADC A,A */
                ADC(A.get());
                break;
            case 0x90:    /* SUB A,B */
                SUB(B.get());
                break;
            case 0x91:    /* SUB A,C */
                SUB(C.get());
                break;
            case 0x92:    /* SUB A,D */
                SUB(D.get());
                break;
            case 0x93:    /* SUB A,E */
                SUB(E.get());
                break;
            case 0x94:    /* SUB A,H */
                SUB(H.get());
                break;
            case 0x95:    /* SUB A,L */
                SUB(L.get());
                break;
            case 0x96:    /* SUB A,(HL) */
                contend(HL, 3);
            {
                int bytetemp = machine.readbyte(HL.word);
                SUB(bytetemp);
            }
            break;
            case 0x97:    /* SUB A,A */
                SUB(A.get());
                break;
            case 0x98:    /* SBC A,B */
                SBC(B.get());
                break;
            case 0x99:    /* SBC A,C */
                SBC(C.get());
                break;
            case 0x9a:    /* SBC A,D */
                SBC(D.get());
                break;
            case 0x9b:    /* SBC A,E */
                SBC(E.get());
                break;
            case 0x9c:    /* SBC A,H */
                SBC(H.get());
                break;
            case 0x9d:    /* SBC A,L */
                SBC(L.get());
                break;
            case 0x9e:    /* SBC A,(HL) */
                contend(HL, 3);
            {
                int bytetemp = machine.readbyte(HL.word);
                SBC(bytetemp);
            }
            break;
            case 0x9f:    /* SBC A,A */
                SBC(A.get());
                break;
            case 0xa0:    /* AND A,B */
                AND(B);
                break;
            case 0xa1:    /* AND A,C */
                AND(C);
                break;
            case 0xa2:    /* AND A,D */
                AND(D);
                break;
            case 0xa3:    /* AND A,E */
                AND(E);
                break;
            case 0xa4:    /* AND A,H */
                AND(H);
                break;
            case 0xa5:    /* AND A,L */
                AND(L);
                break;
            case 0xa6:    /* AND A,(HL) */
                contend(HL, 3);
            {
                int bytetemp = machine.readbyte(HL.word);
                AND(bytetemp);
            }
            break;
            case 0xa7:    /* AND A,A */
                AND(A.get());
                break;
            case 0xa8:    /* XOR A,B */
                XOR(B);
                break;
            case 0xa9:    /* XOR A,C */
                XOR(C);
                break;
            case 0xaa:    /* XOR A,D */
                XOR(D);
                break;
            case 0xab:    /* XOR A,E */
                XOR(E);
                break;
            case 0xac:    /* XOR A,H */
                XOR(H);
                break;
            case 0xad:    /* XOR A,L */
                XOR(L);
                break;
            case 0xae:    /* XOR A,(HL) */
                contend(HL, 3);
            {
                int bytetemp = machine.readbyte(HL.word);
                XOR(bytetemp);
            }
            //if( DEBUG ) System.out.print(" XOR A,(HL) ; "+A);
            break;
            case 0xaf:    /* XOR A,A */
                XOR(A);
                break;
            case 0xb0:    /* OR A,B */
                OR(B);
                break;
            case 0xb1:    /* OR A,C */
                OR(C);
                break;
            case 0xb2:    /* OR A,D */
                OR(D);
                break;
            case 0xb3:    /* OR A,E */
                OR(E);
                break;
            case 0xb4:    /* OR A,H */
                OR(H);
                break;
            case 0xb5:    /* OR A,L */
                OR(L);
                break;
            case 0xb6:    /* OR A,(HL) */
                contend(HL, 3);
            {
                int bytetemp = machine.readbyte(HL.word);
                OR(bytetemp);
            }
            break;
            case 0xb7:    /* OR A,A */
                OR(A);
                break;
            case 0xb8:    /* CP B */
                CP(B.get());
                break;
            case 0xb9:    /* CP C */
                CP(C.get());
                break;
            case 0xba:    /* CP D */
                CP(D.get());
                break;
            case 0xbb:    /* CP E */
                CP(E.get());
                break;
            case 0xbc:    /* CP H */
                CP(H.get());
                break;
            case 0xbd:    /* CP L */
                CP(L.get());
                break;
            case 0xbe:    /* CP (HL) */
                contend(HL, 3);
            {
                int bytetemp = machine.readbyte(HL.word);
                CP(bytetemp);
                //if( DEBUG ) System.out.print(" CP (HL) ; "+toHex8(bytetemp)+", "+F.toBinaryString());
            }
            break;
            case 0xbf:    /* CP A */
                CP(A.get());
                break;
            case 0xc0:    /* RET NZ */
                tstates++;
                //if( DEBUG ) System.out.print(" RET NZ returning "+((F.value & FLAG_Z)==0));
                if ((F.value & FLAG_Z) == 0) {
                    RET();
                }
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xc1:    /* POP BC */
                BC.set(POP16());
                //if( DEBUG ) System.out.print(" POP BC ; "+BC);
                break;
            case 0xc2:    /* JP NZ,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                //if( DEBUG ) System.out.print(" JP NZ jumping "+((F.value & FLAG_Z)==0));
                if ((F.value & FLAG_Z) == 0) {
                    JP();
                } else PC += 2;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xc3:    /* JP nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                JP();
                //if( DEBUG ) System.out.print(" JP to "+PC);
                break;
            case 0xc4:    /* CALL NZ,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                if ((F.value & FLAG_Z) == 0) {
                    CALL();
                } else PC += 2;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xc5:    /* PUSH BC */
                tstates++;
                PUSH16(BC.word);
                //if( DEBUG ) System.out.print(" PUSH BC ; "+BC);
                break;
            case 0xc6:    /* ADD A,nn */
                contend(PC, 3);
            {
                int bytetemp = machine.readbyte(PC++);
                ADD(bytetemp);
            }
            break;
            case 0xc7:    /* RST 00 */
                tstates++;
                RST(0x00);
                break;
            case 0xc8:    /* RET Z */
                tstates++;
                //if( DEBUG ) System.out.print(" RET Z returning "+((F.value & FLAG_Z)!=0));
                if ((F.value & FLAG_Z) != 0) {
                    RET();
                }
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xc9:    /* RET */
                RET();
                //if( DEBUG ) System.out.print(" RET to "+PC);
                break;
            case 0xca:    /* JP Z,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                //if( DEBUG ) System.out.print(" JP Z jumping "+((F.value & FLAG_Z)!=0));
                if ((F.value & FLAG_Z) > 0) {
                    JP();
                } else PC += 2;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xcb:    /* CBxx opcodes */ {
                contend(PC, 4);
                int opcode2 = machine.opcode_fetch(PC++);
                R = (R + 1) & 0xff;

                do_opcode_CB(opcode2);
            }
            break;
            case 0xcc:    /* CALL Z,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                if ((F.value & FLAG_Z) > 0) {
                    CALL();
                } else PC += 2;
                break;
            case 0xcd:    /* CALL nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                CALL();
                //if( DEBUG ) System.out.print(" CALL "+PC);
                break;
            case 0xce:    /* ADC A,nn */
                contend(PC, 3);
            {
                int bytetemp = machine.readbyte(PC++);
                ADC(bytetemp);
            }
            break;
            case 0xcf:    /* RST 8 */
                tstates++;
                RST(0x08);
                break;
            case 0xd0:    /* RET NC */
                tstates++;
                //if( DEBUG ) System.out.print(" RET NC returning "+((F.value & FLAG_C)==0));
                if ((F.value & FLAG_C) == 0) {
                    RET();
                }
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xd1:    /* POP DE */
                DE.set(POP16());
                //if( DEBUG ) System.out.print(" POP DE ; "+DE);
                break;
            case 0xd2:    /* JP NC,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                //if( DEBUG ) System.out.print(" JP NC jumping "+((F.value & FLAG_C)==0));
                if ((F.value & FLAG_C) == 0) {
                    JP();
                } else PC += 2;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xd3:    /* OUT (nn),A */ {
                contend(PC, 4);
                int outtemp = machine.readbyte(PC++) + (A.value << 8);
                OUT(outtemp, A);
            }
            break;
            case 0xd4:    /* CALL NC,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                if ((F.value & FLAG_C) == 0) {
                    CALL();
                } else PC += 2;
                break;
            case 0xd5:    /* PUSH DE */
                tstates++;
                PUSH16(DE.word);
                //if( DEBUG ) System.out.print(" PUSH DE ; "+DE);
                break;
            case 0xd6:    /* SUB nn */
                contend(PC, 3);
            {
                int bytetemp = machine.readbyte(PC++);
                SUB(bytetemp);
            }
            break;
            case 0xd7:    /* RST 10 */
                tstates++;
                RST(0x10);
                break;
            case 0xd8:    /* RET C */
                tstates++;
                //if( DEBUG ) System.out.print(" RET C returning "+((F.value & FLAG_C)>0));
                if ((F.value & FLAG_C) > 0) {
                    RET();
                }
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xd9:    /* EXX */ {
                int wordtemp = BC.word;
                BC.set(BC_);
                BC_ = wordtemp;
                wordtemp = DE.word;
                DE.set(DE_);
                DE_ = wordtemp;
                wordtemp = HL.word;
                HL.set(HL_);
                HL_ = wordtemp;
                //if( DEBUG ) System.out.print(" EXX");
            }
            break;
            case 0xda:    /* JP C,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                //if( DEBUG ) System.out.print(" JP M jumping "+((F.value & FLAG_C)>0));
                if ((F.value & FLAG_C) > 0) {
                    JP();
                } else PC += 2;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xdb:    /* IN A,(nn) */ {
                contend(PC, 4);
                int intemp = machine.readbyte(PC++) + (A.value << 8);
                contend_io(intemp, 3);
                A.set(machine.readport(intemp));
            }
            break;
            case 0xdc:    /* CALL C,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                if ((F.value & FLAG_C) > 0) {
                    CALL();
                } else PC += 2;
                break;
            case 0xdd:    /* DDxx opcodes */ {
                contend(PC, 4);
                int opcode2 = machine.opcode_fetch(PC++);
                R = (R + 1) & 0xff;
                do_opcode_DDFD(opcode2, IX, IXL, IXH);
            }
            break;
            case 0xde:    /* SBC A,nn */
                contend(PC, 3);
            {
                int bytetemp = machine.readbyte(PC++);
                SBC(bytetemp);
            }
            break;
            case 0xdf:    /* RST 18 */
                tstates++;
                RST(0x18);
                break;
            case 0xe0:    /* RET PO */
                tstates++;
                //if( DEBUG ) System.out.print(" RET PO returning "+((F.value & FLAG_P)==0));
                if ((F.value & FLAG_P) == 0) {
                    RET();
                }
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xe1:    /* POP HL */
                HL.set(POP16());
                //if( DEBUG ) System.out.print(" POP HL ; "+HL);
                break;
            case 0xe2:    /* JP PO,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                //if( DEBUG ) System.out.print(" JP PO jumping "+((F.value & FLAG_P)==0));
                if ((F.value & FLAG_P) == 0) {
                    JP();
                } else PC += 2;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xe3:    /* EX (SP),HL */ {
                int bytetempl = machine.readbyte(SP), bytetemph = machine.readbyte(SP + 1);
                contend(SP, 3);
                contend(SP + 1, 4);
                contend(SP, 3);
                contend(SP + 1, 5);
                machine.writebyte(SP, L.get());
                machine.writebyte(SP + 1, H.get());
                L.set(bytetempl);
                H.set(bytetemph);
                //if( DEBUG ) System.out.print(" EX (SP),HL ; "+HL);
            }
            break;
            case 0xe4:    /* CALL PO,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                if ((F.value & FLAG_P) == 0) {
                    CALL();
                } else PC += 2;
                break;
            case 0xe5:    /* PUSH HL */
                tstates++;
                PUSH16(HL.word);
                //if( DEBUG ) System.out.print(" PUSH HL ; "+HL);
                break;
            case 0xe6:    /* AND nn */
                contend(PC, 3);
            {
                int bytetemp = machine.readbyte(PC++);
                AND(bytetemp);
                //if( DEBUG ) System.out.print(" AND "+toHex8(bytetemp)+" ; "+A);
            }
            break;
            case 0xe7:    /* RST 20 */
                tstates++;
                RST(0x20);
                break;
            case 0xe8:    /* RET PE */
                tstates++;
                //if( DEBUG ) System.out.print(" RET PE returning "+((F.value & FLAG_P)>0));
                if ((F.value & FLAG_P) > 0) {
                    RET();
                }
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xe9:    /* JP HL */
                PC = HL.word;
                //if( DEBUG ) System.out.print("JP (HL) to "+PC);
                break;
            case 0xea:    /* JP PE,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                //if( DEBUG ) System.out.print(" JP PE jumping "+((F.value & FLAG_P)>0));
                if ((F.value & FLAG_P) > 0) {
                    JP();
                } else PC += 2;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xeb:    /* EX DE,HL */ {
                int wordtemp = DE.word;
                DE.set(HL);
                HL.set(wordtemp);
                //if( DEBUG ) System.out.print(" EX DE,HL");
            }
            break;
            case 0xec:    /* CALL PE,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                if ((F.value & FLAG_P) > 0) {
                    CALL();
                } else PC += 2;
                break;
            case 0xed:    /* EDxx opcodes */ {
                contend(PC, 4);
                int opcode2 = machine.opcode_fetch(PC++);
                R = (R + 1) & 0xff;

                do_opcode_ED(opcode2);
            }
            break;
            case 0xee:    /* XOR A,nn */
                contend(PC, 3);
            {
                int bytetemp = machine.readbyte(PC++);
                XOR(bytetemp);
            }
            break;
            case 0xef:    /* RST 28 */
                tstates++;
                RST(0x28);
                break;
            case 0xf0:    /* RET P */
                tstates++;
                //if( DEBUG ) System.out.print(" RET P returning "+((F.value & FLAG_S)==0));
                if ((F.value & FLAG_S) == 0) {
                    RET();
                }
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xf1:    /* POP AF */
                AF.set(POP16());
                //if( DEBUG ) System.out.print(" POP AF ; "+AF);
                break;
            case 0xf2:    /* JP P,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                //if( DEBUG ) System.out.print(" JP P jumping "+((F.value & FLAG_S)==0));
                if ((F.value & FLAG_S) == 0) {
                    JP();
                } else PC += 2;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xf3:    /* DI */
                IFF1 = IFF2 = 0;
                break;
            case 0xf4:    /* CALL P,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                if ((F.value & FLAG_S) == 0) {
                    CALL();
                } else PC += 2;
                break;
            case 0xf5:    /* PUSH AF */
                tstates++;
                PUSH16(AF.get());
                //if( DEBUG ) System.out.print(" PUSH AF ; "+AF);
                break;
            case 0xf6:    /* OR nn */
                contend(PC, 3);
            {
                int bytetemp = machine.readbyte(PC++);
                OR(bytetemp);
            }
            break;
            case 0xf7:    /* RST 30 */
                tstates++;
                RST(0x30);
                break;
            case 0xf8:    /* RET M */
                tstates++;
                //if( DEBUG ) System.out.print(" RET M returning "+((F.value & FLAG_S)!=0));
                if ((F.value & FLAG_S) != 0) {
                    RET();
                }
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xf9:    /* LD SP,HL */
                tstates += 2;
                SP = HL.word;
                //if( DEBUG ) System.out.print(" LD SP,HL ; "+HL);
                break;
            case 0xfa:    /* JP M,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                //if( DEBUG ) System.out.print(" JP M jumping "+((F.value & FLAG_S)>0));
                if ((F.value & FLAG_S) > 0) {
                    JP();
                } else PC += 2;
                //if( DEBUG ) System.out.print(" to "+PC);
                break;
            case 0xfb:    /* EI */
                IFF1 = IFF2 = 1;
                break;
            case 0xfc:    /* CALL M,nnnn */
                contend(PC, 3);
                contend(PC + 1, 3);
                if ((F.value & FLAG_S) > 0) {
                    CALL();
                } else PC += 2;
                break;
            case 0xfd:    /* FDxx opcodes */ {
                contend(PC, 4);
                int opcode2 = machine.opcode_fetch(PC++);
                R = (R + 1) & 0xff;
                do_opcode_DDFD(opcode2, IY, IYL, IYH);
            }
            break;
            case 0xfe:    /* CP nn */
                contend(PC, 3);
            {
                int bytetemp = machine.readbyte(PC++);
                CP(bytetemp);
                //if( DEBUG ) System.out.print(" CP "+toHex8(bytetemp)+" ; "+F.toBinaryString());
            }
            break;
            case 0xff:    /* RST 38 */
                tstates++;
                RST(0x38);
                break;
        }     /* Matches switch(opcode) { */

        R = R & 127;

        //if( DEBUG ) System.out.println(" T: "+tstates+" row: "+ZX81.rowcounter+" Memory[$4028]="+toHex8(ZX81.memory[0x4028]));
        //if( DEBUG ) System.out.println("      HL :"+HL+ " BC :"+BC+ " DE :"+DE+" A :"+A+" F :"+F.toBinaryString());
        //if( DEBUG ) System.out.println("      HL':"+HL_+" BC':"+BC_+" DE':"+DE_+" A':"+A_+" F':"+F_.toBinaryString());
        //if( DEBUG ) System.out.println("      IX :"+IX+ " IY :"+IY+ " IR :"+toHex16(IR())+" SP:"+SP+" PC:"+PC);

        return (tstates);
    }


    //==========================================================================================
    // From Z80_CB.
    //==========================================================================================

    private void do_opcode_CB(int opcode2) {

        //if( DEBUG ) System.out.print(opToHex8(opcode2));

        switch (opcode2) {
            case 0x00:  /* RLC B */
                RLC(B);
                break;

            case 0x01:  /* RLC C */
                RLC(C);
                break;

            case 0x02:  /* RLC D */
                RLC(D);
                break;

            case 0x03:  /* RLC E */
                RLC(E);
                break;

            case 0x04:  /* RLC H */
                RLC(H);
                break;

            case 0x05:  /* RLC L */
                RLC(L);
                break;

            case 0x06:  /* RLC (HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                contend(HL, 4);
                contend(HL, 3);
                RLC(bytetemp);
                machine.writebyte(HL.word, bytetemp.get());
            }
            break;

            case 0x07:  /* RLC A */
                RLC(A);
                break;

            case 0x08:  /* RRC B */
                RRC(B);
                break;

            case 0x09:  /* RRC C */
                RRC(C);
                break;

            case 0x0a:  /* RRC D */
                RRC(D);
                break;

            case 0x0b:  /* RRC E */
                RRC(E);
                break;

            case 0x0c:  /* RRC H */
                RRC(H);
                break;

            case 0x0d:  /* RRC L */
                RRC(L);
                break;

            case 0x0e:  /* RRC (HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                contend(HL, 4);
                contend(HL, 3);
                RRC(bytetemp);
                machine.writebyte(HL.word, bytetemp.get());
            }
            break;

            case 0x0f:  /* RRC A */
                RRC(A);
                break;

            case 0x10:  /* RL B */
                RL(B);
                break;

            case 0x11:  /* RL C */
                RL(C);
                break;

            case 0x12:  /* RL D */
                RL(D);
                break;

            case 0x13:  /* RL E */
                RL(E);
                break;

            case 0x14:  /* RL H */
                RL(H);
                break;

            case 0x15:  /* RL L */
                RL(L);
                break;

            case 0x16:  /* RL (HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                contend(HL, 4);
                contend(HL, 3);
                RL(bytetemp);
                machine.writebyte(HL.word, bytetemp.get());
            }
            break;

            case 0x17:  /* RL A */
                RL(A);
                break;

            case 0x18:  /* RR B */
                RR(B);
                break;

            case 0x19:  /* RR C */
                RR(C);
                break;

            case 0x1a:  /* RR D */
                RR(D);
                break;

            case 0x1b:  /* RR E */
                RR(E);
                break;

            case 0x1c:  /* RR H */
                RR(H);
                break;

            case 0x1d:  /* RR L */
                RR(L);
                break;

            case 0x1e:  /* RR (HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                contend(HL, 4);
                contend(HL, 3);
                RR(bytetemp);
                machine.writebyte(HL.word, bytetemp.get());
            }
            break;

            case 0x1f:  /* RR A */
                RR(A);
                break;

            case 0x20:  /* SLA B */
                SLA(B);
                break;

            case 0x21:  /* SLA C */
                SLA(C);
                break;

            case 0x22:  /* SLA D */
                SLA(D);
                break;

            case 0x23:  /* SLA E */
                SLA(E);
                break;

            case 0x24:  /* SLA H */
                SLA(H);
                break;

            case 0x25:  /* SLA L */
                SLA(L);
                break;

            case 0x26:  /* SLA (HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                contend(HL, 4);
                contend(HL, 3);
                SLA(bytetemp);
                machine.writebyte(HL.word, bytetemp.get());
            }
            break;

            case 0x27:  /* SLA A */
                SLA(A);
                break;

            case 0x28:  /* SRA B */
                SRA(B);
                break;

            case 0x29:  /* SRA C */
                SRA(C);
                break;

            case 0x2a:  /* SRA D */
                SRA(D);
                break;

            case 0x2b:  /* SRA E */
                SRA(E);
                break;

            case 0x2c:  /* SRA H */
                SRA(H);
                break;

            case 0x2d:  /* SRA L */
                SRA(L);
                break;

            case 0x2e:  /* SRA (HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                contend(HL, 4);
                contend(HL, 3);
                SRA(bytetemp);
                machine.writebyte(HL.word, bytetemp.get());
            }
            break;

            case 0x2f:  /* SRA A */
                SRA(A);
                break;

            case 0x30:  /* SLL B */
                SLL(B);
                break;

            case 0x31:  /* SLL C */
                SLL(C);
                break;

            case 0x32:  /* SLL D */
                SLL(D);
                break;

            case 0x33:  /* SLL E */
                SLL(E);
                break;

            case 0x34:  /* SLL H */
                SLL(H);
                break;

            case 0x35:  /* SLL L */
                SLL(L);
                break;

            case 0x36:  /* SLL (HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                contend(HL, 4);
                contend(HL, 3);
                SLL(bytetemp);
                machine.writebyte(HL.word, bytetemp.get());
            }
            break;

            case 0x37:  /* SLL A */
                SLL(A);
                break;

            case 0x38:  /* SRL B */
                SRL(B);
                break;

            case 0x39:  /* SRL C */
                SRL(C);
                break;

            case 0x3a:  /* SRL D */
                SRL(D);
                break;

            case 0x3b:  /* SRL E */
                SRL(E);
                break;

            case 0x3c:  /* SRL H */
                SRL(H);
                break;

            case 0x3d:  /* SRL L */
                SRL(L);
                break;

            case 0x3e:  /* SRL (HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                Register bytetemp = tempreg;
                contend(HL, 4);
                contend(HL, 3);
                SRL(bytetemp);
                machine.writebyte(HL.word, bytetemp.get());
            }
            break;

            case 0x3f:  /* SRL A */
                SRL(A);
                break;

            case 0x40:  /* BIT 0,B */
                BIT(0, B);
                break;

            case 0x41:  /* BIT 0,C */
                BIT(0, C);
                break;

            case 0x42:  /* BIT 0,D */
                BIT(0, D);
                break;

            case 0x43:  /* BIT 0,E */
                BIT(0, E);
                break;

            case 0x44:  /* BIT 0,H */
                BIT(0, H);
                break;

            case 0x45:  /* BIT 0,L */
                BIT(0, L);
                break;

            case 0x46:  /* BIT 0,(HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                contend(HL, 4);
                BIT(0, tempreg);
            }
            break;

            case 0x47:  /* BIT 0,A */
                BIT(0, A);
                break;

            case 0x48:  /* BIT 1,B */
                BIT(1, B);
                break;

            case 0x49:  /* BIT 1,C */
                BIT(1, C);
                break;

            case 0x4a:  /* BIT 1,D */
                BIT(1, D);
                break;

            case 0x4b:  /* BIT 1,E */
                BIT(1, E);
                break;

            case 0x4c:  /* BIT 1,H */
                BIT(1, H);
                break;

            case 0x4d:  /* BIT 1,L */
                BIT(1, L);
                break;

            case 0x4e:  /* BIT 1,(HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                contend(HL, 4);
                BIT(1, tempreg);
            }
            break;

            case 0x4f:  /* BIT 1,A */
                BIT(1, A);
                break;

            case 0x50:  /* BIT 2,B */
                BIT(2, B);
                break;

            case 0x51:  /* BIT 2,C */
                BIT(2, C);
                break;

            case 0x52:  /* BIT 2,D */
                BIT(2, D);
                break;

            case 0x53:  /* BIT 2,E */
                BIT(2, E);
                break;

            case 0x54:  /* BIT 2,H */
                BIT(2, H);
                break;

            case 0x55:  /* BIT 2,L */
                BIT(2, L);
                break;

            case 0x56:  /* BIT 2,(HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                contend(HL, 4);
                BIT(2, tempreg);
            }
            break;

            case 0x57:  /* BIT 2,A */
                BIT(2, A);
                break;

            case 0x58:  /* BIT 3,B */
                BIT(3, B);
                break;

            case 0x59:  /* BIT 3,C */
                BIT(3, C);
                break;

            case 0x5a:  /* BIT 3,D */
                BIT(3, D);
                break;

            case 0x5b:  /* BIT 3,E */
                BIT(3, E);
                break;

            case 0x5c:  /* BIT 3,H */
                BIT(3, H);
                break;

            case 0x5d:  /* BIT 3,L */
                BIT(3, L);
                break;

            case 0x5e:  /* BIT 3,(HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                contend(HL, 4);
                BIT(3, tempreg);
            }
            break;

            case 0x5f:  /* BIT 3,A */
                BIT(3, A);
                break;

            case 0x60:  /* BIT 4,B */
                BIT(4, B);
                break;

            case 0x61:  /* BIT 4,C */
                BIT(4, C);
                break;

            case 0x62:  /* BIT 4,D */
                BIT(4, D);
                break;

            case 0x63:  /* BIT 4,E */
                BIT(4, E);
                break;

            case 0x64:  /* BIT 4,H */
                BIT(4, H);
                break;

            case 0x65:  /* BIT 4,L */
                BIT(4, L);
                break;

            case 0x66:  /* BIT 4,(HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                contend(HL, 4);
                BIT(4, tempreg);
            }
            break;

            case 0x67:  /* BIT 4,A */
                BIT(4, A);
                break;

            case 0x68:  /* BIT 5,B */
                BIT(5, B);
                break;

            case 0x69:  /* BIT 5,C */
                BIT(5, C);
                break;

            case 0x6a:  /* BIT 5,D */
                BIT(5, D);
                break;

            case 0x6b:  /* BIT 5,E */
                BIT(5, E);
                break;

            case 0x6c:  /* BIT 5,H */
                BIT(5, H);
                break;

            case 0x6d:  /* BIT 5,L */
                BIT(5, L);
                break;

            case 0x6e:  /* BIT 5,(HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                contend(HL, 4);
                BIT(5, tempreg);
            }
            break;

            case 0x6f:  /* BIT 5,A */
                BIT(5, A);
                break;

            case 0x70:  /* BIT 6,B */
                BIT(6, B);
                break;

            case 0x71:  /* BIT 6,C */
                BIT(6, C);
                break;

            case 0x72:  /* BIT 6,D */
                BIT(6, D);
                break;

            case 0x73:  /* BIT 6,E */
                BIT(6, E);
                break;

            case 0x74:  /* BIT 6,H */
                BIT(6, H);
                break;

            case 0x75:  /* BIT 6,L */
                BIT(6, L);
                break;

            case 0x76:  /* BIT 6,(HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                contend(HL, 4);
                BIT(6, tempreg);
            }
            break;

            case 0x77:  /* BIT 6,A */
                BIT(6, A);
                break;

            case 0x78:  /* BIT 7,B */
                BIT7(B);
                break;

            case 0x79:  /* BIT 7,C */
                BIT7(C);
                break;

            case 0x7a:  /* BIT 7,D */
                BIT7(D);
                break;

            case 0x7b:  /* BIT 7,E */
                BIT7(E);
                break;

            case 0x7c:  /* BIT 7,H */
                BIT7(H);
                break;

            case 0x7d:  /* BIT 7,L */
                BIT7(L);
                break;

            case 0x7e:  /* BIT 7,(HL) */ {
                tempreg.set(machine.readbyte(HL.word));
                contend(HL, 4);
                BIT7(tempreg);
            }
            break;

            case 0x7f:  /* BIT 7,A */
                BIT7(A);
                break;

            case 0x80:  /* RES 0,B */
                B.and(0xfe);
                break;

            case 0x81:  /* RES 0,C */
                C.and(0xfe);
                break;

            case 0x82:  /* RES 0,D */
                D.and(0xfe);
                break;

            case 0x83:  /* RES 0,E */
                E.and(0xfe);
                break;

            case 0x84:  /* RES 0,H */
                H.and(0xfe);
                break;

            case 0x85:  /* RES 0,L */
                L.and(0xfe);
                break;

            case 0x86:  /* RES 0,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) & 0xfe);
                break;

            case 0x87:  /* RES 0,A */
                A.and(0xfe);
                break;

            case 0x88:  /* RES 1,B */
                B.and(0xfd);
                break;

            case 0x89:  /* RES 1,C */
                C.and(0xfd);
                break;

            case 0x8a:  /* RES 1,D */
                D.and(0xfd);
                break;

            case 0x8b:  /* RES 1,E */
                E.and(0xfd);
                break;

            case 0x8c:  /* RES 1,H */
                H.and(0xfd);
                break;

            case 0x8d:  /* RES 1,L */
                L.and(0xfd);
                break;

            case 0x8e:  /* RES 1,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) & 0xfd);
                break;

            case 0x8f:  /* RES 1,A */
                A.and(0xfd);
                break;

            case 0x90:  /* RES 2,B */
                B.and(0xfb);
                break;

            case 0x91:  /* RES 2,C */
                C.and(0xfb);
                break;

            case 0x92:  /* RES 2,D */
                D.and(0xfb);
                break;

            case 0x93:  /* RES 2,E */
                E.and(0xfb);
                break;

            case 0x94:  /* RES 2,H */
                //H &= 0xfb;
                H.and(0xfb);
                break;

            case 0x95:  /* RES 2,L */
                //L &= 0xfb;
                break;

            case 0x96:  /* RES 2,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) & 0xfb);
                break;

            case 0x97:  /* RES 2,A */
                A.and(0xfb);
                break;

            case 0x98:  /* RES 3,B */
                B.and(0xf7);
                break;

            case 0x99:  /* RES 3,C */
                C.and(0xf7);
                break;

            case 0x9a:  /* RES 3,D */
                D.and(0xf7);
                break;

            case 0x9b:  /* RES 3,E */
                E.and(0xf7);
                break;

            case 0x9c:  /* RES 3,H */
                H.and(0xf7);
                break;

            case 0x9d:  /* RES 3,L */
                L.and(0xf7);
                break;

            case 0x9e:  /* RES 3,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) & 0xf7);
                break;

            case 0x9f:  /* RES 3,A */
                A.and(0xf7);
                break;

            case 0xa0:  /* RES 4,B */
                B.and(0xef);
                break;

            case 0xa1:  /* RES 4,C */
                C.and(0xef);
                break;

            case 0xa2:  /* RES 4,D */
                D.and(0xef);
                break;

            case 0xa3:  /* RES 4,E */
                E.and(0xef);
                break;

            case 0xa4:  /* RES 4,H */
                H.and(0xef);
                break;

            case 0xa5:  /* RES 4,L */
                L.and(0xef);
                break;

            case 0xa6:  /* RES 4,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) & 0xef);
                break;

            case 0xa7:  /* RES 4,A */
                A.and(0xef);
                break;

            case 0xa8:  /* RES 5,B */
                B.and(0xdf);
                break;

            case 0xa9:  /* RES 5,C */
                C.and(0xdf);
                break;

            case 0xaa:  /* RES 5,D */
                D.and(0xdf);
                break;

            case 0xab:  /* RES 5,E */
                E.and(0xdf);
                break;

            case 0xac:  /* RES 5,H */
                H.and(0xdf);
                break;

            case 0xad:  /* RES 5,L */
                L.and(0xdf);
                break;

            case 0xae:  /* RES 5,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) & 0xdf);
                break;

            case 0xaf:  /* RES 5,A */
                A.and(0xdf);
                break;

            case 0xb0:  /* RES 6,B */
                B.and(0xbf);
                break;

            case 0xb1:  /* RES 6,C */
                C.and(0xbf);
                break;

            case 0xb2:  /* RES 6,D */
                D.and(0xbf);
                break;

            case 0xb3:  /* RES 6,E */
                E.and(0xbf);
                break;

            case 0xb4:  /* RES 6,H */
                H.and(0xbf);
                break;

            case 0xb5:  /* RES 6,L */
                L.and(0xbf);
                break;

            case 0xb6:  /* RES 6,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) & 0xbf);
                break;

            case 0xb7:  /* RES 6,A */
                A.and(0xbf);
                break;

            case 0xb8:  /* RES 7,B */
                B.and(0x7f);
                break;

            case 0xb9:  /* RES 7,C */
                C.and(0x7f);
                break;

            case 0xba:  /* RES 7,D */
                D.and(0x7f);
                break;

            case 0xbb:  /* RES 7,E */
                E.and(0x7f);
                break;

            case 0xbc:  /* RES 7,H */
                H.and(0x7f);
                break;

            case 0xbd:  /* RES 7,L */
                L.and(0x7f);
                break;

            case 0xbe:  /* RES 7,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) & 0x7f);
                break;

            case 0xbf:  /* RES 7,A */
                A.and(0x7f);
                break;

            case 0xc0:  /* SET 0,B */
                B.or(0x01);
                break;

            case 0xc1:  /* SET 0,C */
                C.or(0x01);
                break;

            case 0xc2:  /* SET 0,D */
                D.or(0x01);
                break;

            case 0xc3:  /* SET 0,E */
                E.or(0x01);
                break;

            case 0xc4:  /* SET 0,H */
                H.or(0x01);
                break;

            case 0xc5:  /* SET 0,L */
                L.or(0x01);
                break;

            case 0xc6:  /* SET 0,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) | 0x01);
                break;

            case 0xc7:  /* SET 0,A */
                A.or(0x01);
                break;

            case 0xc8:  /* SET 1,B */
                B.or(0x02);
                break;

            case 0xc9:  /* SET 1,C */
                C.or(0x02);
                break;

            case 0xca:  /* SET 1,D */
                D.or(0x02);
                break;

            case 0xcb:  /* SET 1,E */
                E.or(0x02);
                break;

            case 0xcc:  /* SET 1,H */
                H.or(0x02);
                break;

            case 0xcd:  /* SET 1,L */
                L.or(0x02);
                break;

            case 0xce:  /* SET 1,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) | 0x02);
                break;

            case 0xcf:  /* SET 1,A */
                A.or(0x02);
                break;

            case 0xd0:  /* SET 2,B */
                B.or(0x04);
                break;

            case 0xd1:  /* SET 2,C */
                C.or(0x04);
                break;

            case 0xd2:  /* SET 2,D */
                D.or(0x04);
                break;

            case 0xd3:  /* SET 2,E */
                E.or(0x04);
                break;

            case 0xd4:  /* SET 2,H */
                H.or(0x04);
                break;

            case 0xd5:  /* SET 2,L */
                L.or(0x04);
                break;

            case 0xd6:  /* SET 2,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) | 0x04);
                break;

            case 0xd7:  /* SET 2,A */
                A.or(0x04);
                break;

            case 0xd8:  /* SET 3,B */
                B.or(0x08);
                break;

            case 0xd9:  /* SET 3,C */
                C.or(0x08);
                break;

            case 0xda:  /* SET 3,D */
                D.or(0x08);
                break;

            case 0xdb:  /* SET 3,E */
                E.or(0x08);
                break;

            case 0xdc:  /* SET 3,H */
                H.or(0x08);
                break;

            case 0xdd:  /* SET 3,L */
                L.or(0x08);
                break;

            case 0xde:  /* SET 3,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) | 0x08);
                break;

            case 0xdf:  /* SET 3,A */
                A.or(0x08);
                break;

            case 0xe0:  /* SET 4,B */
                B.or(0x10);
                break;

            case 0xe1:  /* SET 4,C */
                C.or(0x10);
                break;

            case 0xe2:  /* SET 4,D */
                D.or(0x10);
                break;

            case 0xe3:  /* SET 4,E */
                E.or(0x10);
                break;

            case 0xe4:  /* SET 4,H */
                H.or(0x10);
                break;

            case 0xe5:  /* SET 4,L */
                L.or(0x10);
                break;

            case 0xe6:  /* SET 4,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) | 0x10);
                break;

            case 0xe7:  /* SET 4,A */
                A.or(0x10);
                break;

            case 0xe8:  /* SET 5,B */
                B.or(0x20);
                break;

            case 0xe9:  /* SET 5,C */
                C.or(0x20);
                break;

            case 0xea:  /* SET 5,D */
                D.or(0x20);
                break;

            case 0xeb:  /* SET 5,E */
                E.or(0x20);
                break;

            case 0xec:  /* SET 5,H */
                H.or(0x20);
                break;

            case 0xed:  /* SET 5,L */
                L.or(0x20);
                break;

            case 0xee:  /* SET 5,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) | 0x20);
                break;

            case 0xef:  /* SET 5,A */
                A.or(0x20);
                break;

            case 0xf0:  /* SET 6,B */
                B.or(0x40);
                break;

            case 0xf1:  /* SET 6,C */
                C.or(0x40);
                break;

            case 0xf2:  /* SET 6,D */
                D.or(0x40);
                break;

            case 0xf3:  /* SET 6,E */
                E.or(0x40);
                break;

            case 0xf4:  /* SET 6,H */
                H.or(0x40);
                break;

            case 0xf5:  /* SET 6,L */
                L.or(0x40);
                break;

            case 0xf6:  /* SET 6,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) | 0x40);
                break;

            case 0xf7:  /* SET 6,A */
                A.or(0x40);
                break;

            case 0xf8:  /* SET 7,B */
                B.or(0x80);
                break;

            case 0xf9:  /* SET 7,C */
                C.or(0x80);
                break;

            case 0xfa:  /* SET 7,D */
                D.or(0x80);
                break;

            case 0xfb:  /* SET 7,E */
                E.or(0x80);
                break;

            case 0xfc:  /* SET 7,H */
                H.or(0x80);
                break;

            case 0xfd:  /* SET 7,L */
                L.or(0x80);
                break;

            case 0xfe:  /* SET 7,(HL) */
                contend(HL, 4);
                contend(HL, 3);
                machine.writebyte(HL.word, machine.readbyte(HL.word) | 0x80);
                break;

            case 0xff:  /* SET 7,A */
                A.or(0x80);
                break;
        }
    }
    //==========================================================================================
    // From Z80_ED.
    //==========================================================================================

    private void do_opcode_ED(int opcode2) {

//if( DEBUG ) System.out.print(opToHex8(opcode2));

        switch (opcode2) {

            case 0x40:  /* IN B,(C) */
                tstates += 1;
                IN(B, BC);
                //if( DEBUG ) System.out.print(" IN B,(C)");
                break;

            case 0x41:  /* OUT (C),B */
                tstates += 1;
                OUT(BC, B);
                //if( DEBUG ) System.out.print(" OUT (C),B");
                break;

            case 0x42:  /* SBC HL,BC */
                tstates += 7;
                SBC16(BC.word);
                //if( DEBUG ) System.out.print(" SBC HL,BC ; "+HL);
                break;

            case 0x43:  /* LD (nnnn),BC */
                LD16_NNRR(BC.word);
                break;

            case 0x44:
            case 0x4c:
            case 0x54:
            case 0x5c:
            case 0x64:
            case 0x6c:
            case 0x74:
            case 0x7c:  /* NEG */ {
                int bytetemp = A.value;
                A.set(0);
                SUB(bytetemp);
            }
            break;

            case 0x45:
            case 0x4d:
            case 0x55:
            case 0x5d:
            case 0x65:
            case 0x6d:
            case 0x75:
            case 0x7d:      /* RETN */
                IFF1 = IFF2;
                RET();
                break;

            case 0x46:
            case 0x4e:
            case 0x66:
            case 0x6e:  /* IM 0 */
                IM = 0;
                break;

            case 0x47:  /* LD I,A */
                tstates += 1;
                I = A.value;
                //if( DEBUG ) System.out.print(" LD I,A ; "+A);
                break;

            case 0x48:  /* IN C,(C) */
                tstates += 1;
                IN(C, BC);
                break;

            case 0x49:  /* OUT (C),C */
                tstates += 1;
                OUT(BC, C);
                break;

            case 0x4a:  /* ADC HL,BC */
                tstates += 7;
                ADC16(BC.word);
                break;

            case 0x4b:  /* LD BC,(nnnn) */
                BC.set(LD16_RRNN());
                break;

            case 0x4f:  /* LD R,A */
                tstates += 1;
                R = R7 = A.value;
                break;

            case 0x50:  /* IN D,(C) */
                tstates += 1;
                IN(D, BC);
                break;

            case 0x51:  /* OUT (C),D */
                tstates += 1;
                OUT(BC, D);
                break;

            case 0x52:  /* SBC HL,DE */
                tstates += 7;
                SBC16(DE.word);
                break;

            case 0x53:  /* LD (nnnn),DE */
                LD16_NNRR(DE.word);
                break;

            case 0x56:
            case 0x76:  /* IM 1 */
                IM = 1;
                //if( DEBUG ) System.out.print(" IM 1");
                break;

            case 0x57:  /* LD A,I */
                tstates += 1;
                A.set(I);
                F.set((F.value & FLAG_C) | sz53_table[A.value] | (IFF2 != 0 ? FLAG_V : 0));

                break;

            case 0x58:  /* IN E,(C) */
                tstates += 1;
                IN(E, BC);
                break;

            case 0x59:  /* OUT (C),E */
                tstates += 1;
                OUT(BC, E);
                break;

            case 0x5a:  /* ADC HL,DE */
                tstates += 7;
                ADC16(DE.word);
                break;

            case 0x5b:  /* LD DE,(nnnn) */
                DE.set(LD16_RRNN());
                break;

            case 0x5e:
            case 0x7e:  /* IM 2 */
                IM = 2;
                break;

            case 0x5f:  /* LD A,R */
                tstates += 1;
                A.set((R & 0x7f) | (R7 & 0x80));
                F.set((F.value & FLAG_C) | sz53_table[A.value] | (IFF2 != 0 ? FLAG_V : 0));
                break;

            case 0x60:  /* IN H,(C) */
                tstates += 1;
                IN(H, BC);
                break;

            case 0x61:  /* OUT (C),H */
                tstates += 1;
                OUT(BC, H);
                break;

            case 0x62:  /* SBC HL,HL */
                tstates += 7;
                SBC16(HL.word);
                break;

            case 0x63:  /* LD (nnnn),HL */
                LD16_NNRR(HL.word);
                break;

            case 0x67:  /* RRD */ {
                int bytetemp = machine.readbyte(HL.word);
                contend(HL, 7);
                contend(HL, 3);
                machine.writebyte(HL.word, (A.value << 4) | (bytetemp >> 4));
                A.set((A.value & 0xf0) | (bytetemp & 0x0f));
                F.set((F.value & FLAG_C) | sz53p_table[A.value]);
            }
            break;

            case 0x68:  /* IN L,(C) */
                tstates += 1;
                IN(L, BC);
                break;

            case 0x69:  /* OUT (C),L */
                tstates += 1;
                OUT(BC, L);
                break;

            case 0x6a:  /* ADC HL,HL */
                tstates += 7;
                ADC16(HL.word);
                break;

            case 0x6b:  /* LD HL,(nnnn) */
                HL.set(LD16_RRNN());
                break;

            case 0x6f:  /* RLD */ {
                int bytetemp = machine.readbyte(HL.word);
                contend(HL, 7);
                contend(HL, 3);
                machine.writebyte(HL.word, (bytetemp << 4) | (A.value & 0x0f));
                A.set((A.value & 0xf0) | (bytetemp >> 4));
                F.set((F.value & FLAG_C) | sz53p_table[A.value]);
            }
            break;

            case 0x70:  /* IN F,(C) */
                tstates += 1;
            {
                int bytetemp = 0;
                IN(bytetemp, BC);
            }
            break;

            case 0x71:  /* OUT (C),0 */
                tstates += 1;
                OUT(BC, 0);
                break;

            case 0x72:  /* SBC HL,SP */
                tstates += 7;
                SBC16(SP);
                break;

            case 0x73:  /* LD (nnnn),SP */
                LD16_NNRR(SP);
                break;

            case 0x78:  /* IN A,(C) */
                tstates += 1;
                IN(A, BC);
                break;

            case 0x79:  /* OUT (C),A */
                tstates += 1;
                OUT(BC, A);
                break;

            case 0x7a:  /* ADC HL,SP */
                tstates += 7;
                ADC16(SP);
                break;

            case 0x7b:  /* LD SP,(nnnn) */
                SP = LD16_RRNN();
                break;

            case 0xa0:  /* LDI */ {
                int bytetemp = machine.readbyte(HL.word);
                contend(HL, 3);
                contend(DE, 3);
                contend(DE, 1);
                contend(DE, 1);
                BC.dec();
                machine.writebyte(DE.word, bytetemp);
                DE.inc();
                HL.inc();
                bytetemp += A.value;
                F.set((F.value & (FLAG_C | FLAG_Z | FLAG_S)) | (BC.word != 0 ? FLAG_V : 0) |
                        (bytetemp & FLAG_3) | ((bytetemp & 0x02) != 0 ? FLAG_5 : 0));
            }
            break;

            case 0xa1:  /* CPI */ {
                int value = machine.readbyte(HL.word), bytetemp = A.value - value,
                        lookup = ((A.value & 0x08) >> 3) | (((value) & 0x08) >> 2) |
                                ((bytetemp & 0x08) >> 1);
                contend(HL, 3);
                contend(HL, 1);
                contend(HL, 1);
                contend(HL, 1);
                contend(HL, 1);
                contend(HL, 1);
                HL.inc();
                BC.dec();
                F.set((F.value & FLAG_C) | (BC.word != 0 ? (FLAG_V | FLAG_N) : FLAG_N) |
                        halfcarry_sub_table[lookup] | (bytetemp != 0 ? 0 : FLAG_Z) |
                        (bytetemp & FLAG_S));
                if ((F.value & FLAG_H) != 0) bytetemp--;
                F.or((bytetemp & FLAG_3) | ((bytetemp & 0x02) != 0 ? FLAG_5 : 0));
            }
            break;

            case 0xa2:  /* INI */ {
                int initemp = machine.readport(BC.word);
                tstates += 2;
                contend_io(BC, 3);
                contend(HL, 3);
                machine.writebyte(HL.word, initemp);
                B.dec();
                HL.inc();
                F.set(((initemp & 0x80) != 0 ? FLAG_N : 0) | sz53_table[B.get()]);
      /* C,H and P/V flags not implemented */
            }
            break;

            case 0xa3:  /* OUTI */ {
                int outitemp = machine.readbyte(HL.word);
                B.dec();
                tstates++;
                contend(HL, 4);
                contend_io(BC, 3);
                HL.inc();
                machine.writeport(BC.word, outitemp);
                F.set(((outitemp & 0x80) != 0 ? FLAG_N : 0) | sz53_table[B.get()]);
      /* C,H and P/V flags not implemented */
            }
            break;

            case 0xa8:  /* LDD */ {
                int bytetemp = machine.readbyte(HL.word);
                contend(HL, 3);
                contend(DE, 3);
                contend(DE, 1);
                contend(DE, 1);
                BC.dec();
                machine.writebyte(DE.word, bytetemp);
                DE.dec();
                HL.dec();
                bytetemp += A.value;
                F.set((F.value & (FLAG_C | FLAG_Z | FLAG_S)) | (BC.word != 0 ? FLAG_V : 0) |
                        (bytetemp & FLAG_3) | ((bytetemp & 0x02) != 0 ? FLAG_5 : 0));
            }
            break;

            case 0xa9:  /* CPD */ {
                int value = machine.readbyte(HL.word), bytetemp = A.value - value,
                        lookup = ((A.value & 0x08) >> 3) | (((value) & 0x08) >> 2) |
                                ((bytetemp & 0x08) >> 1);
                contend(HL, 3);
                contend(HL, 1);
                contend(HL, 1);
                contend(HL, 1);
                contend(HL, 1);
                contend(HL, 1);
                HL.dec();
                BC.dec();
                F.set((F.value & FLAG_C) | (BC.word != 0 ? (FLAG_V | FLAG_N) : FLAG_N) |
                        halfcarry_sub_table[lookup] | (bytetemp != 0 ? 0 : FLAG_Z) |
                        (bytetemp & FLAG_S));
                if ((F.value & FLAG_H) != 0) bytetemp--;
                F.or((bytetemp & FLAG_3) | ((bytetemp & 0x02) != 0 ? FLAG_5 : 0));
            }
            break;

            case 0xaa:  /* IND */ {
                int initemp = machine.readport(BC.word);
                tstates += 2;
                contend_io(BC, 3);
                contend(HL, 3);
                machine.writebyte(HL.word, initemp);
                B.dec();
                HL.dec();
                F.set(((initemp & 0x80) != 0 ? FLAG_N : 0) | sz53_table[B.get()]);
      /* C,H and P/V flags not implemented */
            }
            break;

            case 0xab:  /* OUTD */ {
                int outitemp = machine.readbyte(HL.word);
                B.dec();    /* This does happen first, despite what the specs say */
                tstates++;
                contend(HL, 4);
                contend_io(BC, 3);
                HL.dec();
                machine.writeport(BC.word, outitemp);
                F.set(((outitemp & 0x80) != 0 ? FLAG_N : 0) | sz53_table[B.get()]);
      /* C,H and P/V flags not implemented */
            }
            break;

            case 0xb0:  /* LDIR */ {
                int bytetemp = machine.readbyte(HL.word);
                contend(HL, 3);
                contend(DE, 3);
                contend(DE, 1);
                contend(DE, 1);
                machine.writebyte(DE.word, bytetemp);
                HL.inc();
                DE.inc();
                BC.dec();
                bytetemp += A.value;
                F.set((F.value & (FLAG_C | FLAG_Z | FLAG_S)) | (BC.word != 0 ? FLAG_V : 0) |
                        (bytetemp & FLAG_3) | ((bytetemp & 0x02) != 0 ? FLAG_5 : 0));
                if (BC.word != 0) {
                    for (int i = 0; i < 5; ++i)
                        contend(DE, 1);
                    PC -= 2;
                    //if( DEBUG ) System.out.print(" LDIR repeating; BC = "+BC);
                } //else
                //if( DEBUG ) System.out.print(" LDIR completed");
            }
            break;

            case 0xb1:  /* CPIR */ {
                int value = machine.readbyte(HL.word), bytetemp = A.value - value,
                        lookup = ((A.value & 0x08) >> 3) | (((value) & 0x08) >> 2) |
                                ((bytetemp & 0x08) >> 1);
                contend(HL, 3);
                for (int i = 0; i < 5; ++i)
                    contend(HL, 1);
                HL.inc();
                BC.dec();
                F.set((F.value & FLAG_C) | (BC.word != 0 ? (FLAG_V | FLAG_N) : FLAG_N) |
                        halfcarry_sub_table[lookup] | (bytetemp != 0 ? 0 : FLAG_Z) |
                        (bytetemp & FLAG_S));
                if ((F.value & FLAG_H) != 0) bytetemp--;
                F.or((bytetemp & FLAG_3) | ((bytetemp & 0x02) != 0 ? FLAG_5 : 0));
                if ((F.value & (FLAG_V | FLAG_Z)) == FLAG_V) {
                    for (int i = 0; i < 5; ++i)
                        contend(HL, 1);
                    PC -= 2;
                    //if( DEBUG ) System.out.print(" CPIR repeating; BC = "+BC);
                }
                //if( DEBUG ) System.out.print(" CPIR finished");
            }
            break;

            case 0xb2:  /* INIR */ {
                int initemp = machine.readport(BC.word);
                tstates += 2;
                contend_io(BC, 3);
                contend(HL, 3);
                machine.writebyte(HL.word, initemp);
                //B--; HL++;
                //F = (initemp & 0x80 ? FLAG_N : 0 ) | sz53_table[B];
                B.dec();
                HL.inc();
                F.set(((initemp & 0x80) != 0 ? FLAG_N : 0) | sz53_table[B.get()]);
      /* C,H and P/V flags not implemented */
                //if(B) {
                if (B.get() != 0) {
                    for (int i = 0; i < 5; ++i)
                        contend(HL, 1);
                    PC -= 2;
                    //if( DEBUG ) System.out.print(" INIR repeating; BC = "+BC);
                }
                //if( DEBUG ) System.out.print(" INIR completed");
            }
            break;

            case 0xb3:  /* OTIR */ {
                int outitemp = machine.readbyte(HL.word);
                tstates++;
                contend(HL, 4);
                B.dec();
                HL.inc(); /* This does happen first, despite what the specs say */
                machine.writeport(BC.word, outitemp);
                F.set(((outitemp & 0x80) != 0 ? FLAG_N : 0) | sz53_table[B.get()]);
      /* C,H and P/V flags not implemented */
                //if(B) {
                if (B.get() != 0) {
                    contend_io(BC, 1);
                    for (int i = 0; i < 6; ++i)
                        contend(PC, 1);
                    contend(PC - 1, 1);
                    PC -= 2;
                    //if( DEBUG ) System.out.print(" OTIR repeating; BC = "+BC);
                } else {
                    contend_io(BC, 3);
                    //if( DEBUG ) System.out.print(" OTIR completed");
                }
            }
            break;

            case 0xb8:  /* LDDR */ {
                int bytetemp = machine.readbyte(HL.word);
                contend(HL, 3);
                contend(DE, 3);
                contend(DE, 1);
                contend(DE, 1);
                machine.writebyte(DE.word, bytetemp);
                HL.dec();
                DE.dec();
                BC.dec();
                bytetemp += A.value;
                F.set((F.value & (FLAG_C | FLAG_Z | FLAG_S)) | (BC.word != 0 ? FLAG_V : 0) |
                        (bytetemp & FLAG_3) | ((bytetemp & 0x02) > 0 ? FLAG_5 : 0));
                //if(BC) {
                if (BC.word != 0) {
                    for (int i = 0; i < 5; ++i)
                        contend(DE, 1);
                    PC -= 2;
                    //if( DEBUG ) System.out.print(" LDDR repeating; BC = "+BC);
                } //else
                //if( DEBUG ) System.out.print(" LDDR completed");
            }
            break;

            case 0xb9:  /* CPDR */ {
                int value = machine.readbyte(HL.word), bytetemp = A.value - value,
                        lookup = ((A.value & 0x08) >> 3) | (((value) & 0x08) >> 2) |
                                ((bytetemp & 0x08) >> 1);
                contend(HL, 3);
                for (int i = 0; i < 5; ++i)
                    contend(HL, 1);
                HL.dec();
                BC.dec();
                F.set((F.value & FLAG_C) | (BC.word != 0 ? (FLAG_V | FLAG_N) : FLAG_N) |
                        halfcarry_sub_table[lookup] | (bytetemp != 0 ? 0 : FLAG_Z) |
                        (bytetemp & FLAG_S));
                if ((F.value & FLAG_H) != 0) bytetemp--;
                F.or((bytetemp & FLAG_3) | ((bytetemp & 0x02) != 0 ? FLAG_5 : 0));
                if ((F.value & (FLAG_V | FLAG_Z)) == FLAG_V) {
                    for (int i = 0; i < 5; ++i)
                        contend(HL, 1);
                    PC -= 2;
                    //if( DEBUG ) System.out.print(" CPDR repeating; BC = "+BC);
                } //else
                //if( DEBUG ) System.out.print(" CPDR completed");
            }
            break;

            case 0xba:  /* INDR */ {
                int initemp = machine.readport(BC.word);
                tstates += 2;
                contend_io(BC, 3);
                contend(HL, 3);
                machine.writebyte(HL.word, initemp);
                B.dec();
                HL.dec();
                F.set(((initemp & 0x80) != 0 ? FLAG_N : 0) | sz53_table[B.get()]);
                if (B.get() != 0) {
                    for (int i = 0; i < 5; ++i)
                        contend(HL, 1);
                    PC -= 2;
                    //if( DEBUG ) System.out.print(" INDR repeating; BC = "+BC);
                } //else
                //if( DEBUG ) System.out.print(" INDR completed");
            }
            break;

            case 0xbb:  /* OTDR */ {
                int outitemp = machine.readbyte(HL.word);
                tstates++;
                contend(HL, 4);
                B.dec();
                HL.dec();  /* This does happen first, despite what the specs say */
                machine.writeport(BC.word, outitemp);
                F.set(((outitemp & 0x80) != 0 ? FLAG_N : 0) | sz53_table[B.get()]);
                if (B.get() != 0) {
                    contend_io(BC, 1);
                    for (int i = 0; i < 5; ++i)
                        contend(PC, 1);
                    contend(PC - 1, 1);
                    PC -= 2;
                    //if( DEBUG ) System.out.print(" OTDR repeating; BC = "+BC);
                } else {
                    contend_io(BC, 3);
                    //if( DEBUG ) System.out.print(" OTDR completed");
                }
            }
            break;

            case 0xfb:  /* Emulator trap to load .slt data */
                break;

            default:  /* All other opcodes are NOPD */
                break;
        }
    }

    //==========================================================================================
    // From Z80_DDFD
    //==========================================================================================

    private void do_opcode_DDFD(int opcode2,
                                RegisterPair REGISTER,
                                Register REGISTERL,
                                Register REGISTERH) {

        //if( DEBUG ) System.out.print(opToHex8(opcode2));

        switch (opcode2) {
            case 0x09:    /* ADD REGISTER,BC */
                ADD16(REGISTER, BC);
                break;

            case 0x19:    /* ADD REGISTER,DE */
                ADD16(REGISTER, DE);
                break;

            case 0x21:    /* LD REGISTER,nnnn */
                contend(PC, 3);
                REGISTERL.set(machine.readbyte(PC++));
                contend(PC, 3);
                REGISTERH.set(machine.readbyte(PC++));
                //if( DEBUG ) System.out.print(" LD "+REGISTER.name+","+REGISTER);
                break;

            case 0x22:    /* LD (nnnn),REGISTER */
                LD16_NNRR(REGISTER.get());
                break;

            case 0x23:    /* INC REGISTER */
                tstates += 2;
                REGISTER.inc();
                break;

            case 0x24:    /* INC REGISTERH */
                INC(REGISTERH);
                break;

            case 0x25:    /* DEC REGISTERH */
                DEC(REGISTERH);
                break;

            case 0x26:    /* LD REGISTERH,nn */
                contend(PC, 3);
                REGISTERH.set(machine.readbyte(PC++));
                break;

            case 0x29:    /* ADD REGISTER,REGISTER */
                ADD16(REGISTER, REGISTER);
                break;

            case 0x2a:    /* LD REGISTER,(nnnn) */
                REGISTER.set(LD16_RRNN());
                break;

            case 0x2b:    /* DEC REGISTER */
                tstates += 2;
                REGISTER.dec();
                break;

            case 0x2c:    /* INC REGISTERL */
                INC(REGISTERL);
                break;

            case 0x2d:    /* DEC REGISTERL */
                DEC(REGISTERL);
                break;

            case 0x2e:    /* LD REGISTERL,nn */
                contend(PC, 3);
                REGISTERL.set(machine.readbyte(PC++));
                break;

            case 0x34:    /* INC (REGISTER+dd) */
                tstates += 15;    /* FIXME: how is this contended? */
            {
                int dist = machine.readbyte(PC++);
                dist = (dist < 128 ? dist : dist - 256);
                int wordtemp = REGISTER.get() + dist;
                tempreg.set(machine.readbyte(wordtemp));
                Register bytetemp = tempreg;
                INC(bytetemp);
                machine.writebyte(wordtemp, bytetemp.get());
            }
            break;

            case 0x35:    /* DEC (REGISTER+dd) */
                tstates += 15;    /* FIXME: how is this contended? */
            {
                int dist = machine.readbyte(PC++);
                int wordtemp = REGISTER.get() + (dist < 128 ? dist : dist - 256);
                tempreg.set(machine.readbyte(wordtemp));
                Register bytetemp = tempreg;
                DEC(bytetemp);
                machine.writebyte(wordtemp, bytetemp.get());
            }
            break;

            case 0x36:    /* LD (REGISTER+dd),nn */
                tstates += 11;    /* FIXME: how is this contended? */
            {
                int dist = machine.readbyte(PC++);
                int wordtemp = REGISTER.get() + (dist < 128 ? dist : dist - 256);
                machine.writebyte(wordtemp, machine.readbyte(PC++));
                //if( DEBUG ) System.out.print(" LD ("+REGISTER.name+"+"+toHex8(dist)+"),"+toHex8(ZX81.memory[wordtemp]));
            }
            break;

            case 0x39:    /* ADD REGISTER,SP */
                ADD16(REGISTER, SP);
                break;

            case 0x44:    /* LD B,REGISTERH */
                B.set(REGISTERH);
                break;

            case 0x45:    /* LD B,REGISTERL */
                B.set(REGISTERL);
                break;

            case 0x46:    /* LD B,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
                int dist = machine.readbyte(PC++);
                B.set(machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256)));
                break;

            case 0x4c:    /* LD C,REGISTERH */
                C.set(REGISTERH);
                break;

            case 0x4d:    /* LD C,REGISTERL */
                C.set(REGISTERL);
                break;

            case 0x4e:    /* LD C,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                C.set(machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256)));

                break;

            case 0x54:    /* LD D,REGISTERH */
                D.set(REGISTERH);
                break;

            case 0x55:    /* LD D,REGISTERL */
                D.set(REGISTERL);
                break;

            case 0x56:    /* LD D,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                D.set(machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256)));
                break;

            case 0x5c:    /* LD E,REGISTERH */
                E.set(REGISTERH);
                break;

            case 0x5d:    /* LD E,REGISTERL */
                E.set(REGISTERL);
                break;

            case 0x5e:    /* LD E,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                E.set(machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256)));
                break;

            case 0x60:    /* LD REGISTERH,B */
                REGISTERH.set(B);
                break;

            case 0x61:    /* LD REGISTERH,C */
                REGISTERH.set(C);
                break;

            case 0x62:    /* LD REGISTERH,D */
                REGISTERH.set(D);
                break;

            case 0x63:    /* LD REGISTERH,E */
                REGISTERH.set(E);
                break;

            case 0x64:    /* LD REGISTERH,REGISTERH */
                break;

            case 0x65:    /* LD REGISTERH,REGISTERL */
                REGISTERH.set(REGISTERL);
                break;

            case 0x66:    /* LD H,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                H.set(machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256)));
                break;

            case 0x67:    /* LD REGISTERH,A */
                REGISTERH.set(A);
                break;

            case 0x68:    /* LD REGISTERL,B */
                REGISTERL.set(B);
                break;

            case 0x69:    /* LD REGISTERL,C */
                REGISTERL.set(C);
                break;

            case 0x6a:    /* LD REGISTERL,D */
                REGISTERL.set(D);
                break;

            case 0x6b:    /* LD REGISTERL,E */
                REGISTERL.set(E);
                break;

            case 0x6c:    /* LD REGISTERL,REGISTERH */
                REGISTERL.set(REGISTERH);
                break;

            case 0x6d:    /* LD REGISTERL,REGISTERL */
                break;

            case 0x6e:    /* LD L,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                L.set(machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256)));
                break;

            case 0x6f:    /* LD REGISTERL,A */
                REGISTERL.set(A);
                break;

            case 0x70:    /* LD (REGISTER+dd),B */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                machine.writebyte(REGISTER.get() + (dist < 128 ? dist : dist - 256), B.get());
                break;

            case 0x71:    /* LD (REGISTER+dd),C */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                machine.writebyte(REGISTER.get() + (dist < 128 ? dist : dist - 256), C.get());
                break;

            case 0x72:    /* LD (REGISTER+dd),D */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                machine.writebyte(REGISTER.get() + (dist < 128 ? dist : dist - 256), D.get());
                break;

            case 0x73:    /* LD (REGISTER+dd),E */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                machine.writebyte(REGISTER.get() + (dist < 128 ? dist : dist - 256), E.get());
                break;

            case 0x74:    /* LD (REGISTER+dd),H */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                machine.writebyte(REGISTER.get() + (dist < 128 ? dist : dist - 256), H.get());
                break;

            case 0x75:    /* LD (REGISTER+dd),L */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                machine.writebyte(REGISTER.get() + (dist < 128 ? dist : dist - 256), L.get());
                break;

            case 0x77:    /* LD (REGISTER+dd),A */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                machine.writebyte(REGISTER.get() + (dist < 128 ? dist : dist - 256), A.value);
                break;

            case 0x7c:    /* LD A,REGISTERH */
                A.set(REGISTERH);
                break;

            case 0x7d:    /* LD A,REGISTERL */
                A.set(REGISTERL);
                break;

            case 0x7e:    /* LD A,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
                dist = machine.readbyte(PC++);
                A.set(machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256)));
                break;

            case 0x84:    /* ADD A,REGISTERH */
                ADD(REGISTERH.get());
                break;

            case 0x85:    /* ADD A,REGISTERL */
                ADD(REGISTERL.get());
                break;

            case 0x86:    /* ADD A,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
            {
                dist = machine.readbyte(PC++);
                int bytetemp = machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256));
                ADD(bytetemp);
            }
            break;

            case 0x8c:    /* ADC A,REGISTERH */
                ADC(REGISTERH.get());
                break;

            case 0x8d:    /* ADC A,REGISTERL */
                ADC(REGISTERL.get());
                break;

            case 0x8e:    /* ADC A,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
            {
                dist = machine.readbyte(PC++);
                int bytetemp = machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256));
                ADC(bytetemp);
            }
            break;

            case 0x94:    /* SUB A,REGISTERH */
                SUB(REGISTERH.get());
                break;

            case 0x95:    /* SUB A,REGISTERL */
                SUB(REGISTERL.get());
                break;

            case 0x96:    /* SUB A,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
            {
                dist = machine.readbyte(PC++);
                int bytetemp = machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256));
                SUB(bytetemp);
            }
            break;

            case 0x9c:    /* SBC A,REGISTERH */
                SBC(REGISTERH.get());
                break;

            case 0x9d:    /* SBC A,REGISTERL */
                SBC(REGISTERL.get());
                break;

            case 0x9e:    /* SBC A,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
            {
                dist = machine.readbyte(PC++);
                int bytetemp = machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256));
                SBC(bytetemp);
            }
            break;

            case 0xa4:    /* AND A,REGISTERH */
                AND(REGISTERH.get());
                break;

            case 0xa5:    /* AND A,REGISTERL */
                AND(REGISTERL.get());
                break;

            case 0xa6:    /* AND A,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
            {
                dist = machine.readbyte(PC++);
                int bytetemp = machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256));
                AND(bytetemp);
            }
            break;

            case 0xac:    /* XOR A,REGISTERH */
                XOR(REGISTERH);
                break;

            case 0xad:    /* XOR A,REGISTERL */
                XOR(REGISTERL);
                break;

            case 0xae:    /* XOR A,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
            {
                dist = machine.readbyte(PC++);
                int bytetemp = machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256));
                XOR(bytetemp);
            }
            break;

            case 0xb4:    /* OR A,REGISTERH */
                OR(REGISTERH);
                break;

            case 0xb5:    /* OR A,REGISTERL */
                OR(REGISTERL);
                break;

            case 0xb6:    /* OR A,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
            {
                dist = machine.readbyte(PC++);
                int bytetemp = machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256));
                OR(bytetemp);
            }
            break;

            case 0xbc:    /* CP A,REGISTERH */
                CP(REGISTERH.get());
                break;

            case 0xbd:    /* CP A,REGISTERL */
                CP(REGISTERL.get());
                break;

            case 0xbe:    /* CP A,(REGISTER+dd) */
                tstates += 11;    /* FIXME: how is this contended? */
            {
                dist = machine.readbyte(PC++);
                int bytetemp = machine.readbyte(REGISTER.get() + (dist < 128 ? dist : dist - 256));
                CP(bytetemp);
            }
            break;

      /* FIXME: contention here is just a guess */
            case 0xcb:    /* {DD,FD}CBxx opcodes */ {
                contend(PC, 3);
                dist = machine.readbyte(PC++);
                int tempaddr = REGISTER.get() + (dist < 128 ? dist : dist - 256);

                contend(PC, 4);
                int opcode3 = machine.opcode_fetch(PC++);
                do_opcode_DDFDCB(opcode3, tempaddr);
            }
            break;

            case 0xe1:    /* POP REGISTER */
                REGISTER.set(POP16());
                //if( DEBUG ) System.out.print(" POP "+REGISTER.name+" ; "+REGISTER);
                break;

            case 0xe3:    /* EX (SP),REGISTER */ {
                int SPvalue = SP;
                int bytetempl = machine.readbyte(SPvalue), bytetemph = machine.readbyte(SPvalue + 1);
                contend(SPvalue, 3);
                contend(SPvalue + 1, 4);
                machine.writebyte(SPvalue, REGISTERL.get());
                machine.writebyte(SPvalue + 1, REGISTERH.get());
                contend(SPvalue, 3);
                contend(SPvalue + 1, 5);
                REGISTERL.set(bytetempl);
                REGISTERH.set(bytetemph);
            }
            break;

            case 0xe5:    /* PUSH REGISTER */
                tstates++;
                PUSH16(REGISTERL.get() + (REGISTERH.get() << 8));
                break;

            case 0xe9:    /* JP REGISTER */
                //PC=REGISTER;    /* NB: NOT INDIRECT! */
                PC = REGISTER.get();

                break;

      /* Note EB (EX DE,HL) does not get modified to use either IX or IY;
         this is because all EX DE,HL does is switch an internal flip-flop
         in the Z80 which says which way round DE and HL are, which can't
         be used with IX or IY. (This is also why EX DE,HL is very quick
         at only 4 T states).
      */

            case 0xf9:    /* LD SP,REGISTER */
                tstates += 2;
                SP = REGISTER.get();
                break;

            default:    /* Instruction did not involve H or L, so backtrack
               one instruction and parse again */
                PC--;
                R = (R - 1) & 0xff;
                break;
        }
    }

    //==========================================================================================
    // From Z80_DDFDCB
    //==========================================================================================

    private void do_opcode_DDFDCB(int opcode3,
                          //RegisterPair REGISTER,
                          //Register REGISTERL,
                          //Register REGISTERH,
                          int tempaddr
                          //int dist )
    ) {

        //if( DEBUG ) System.out.print(opToHex8(opcode3));

        switch (opcode3) {

/* FIXME: Contention details are unknown */

            case 0x00:  /* LD B,RLC (REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr));
                RLC(B);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x01:  /* LD C,RLC (REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr));
                RLC(C);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x02:  /* LD D,RLC (REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr));
                RLC(D);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x03:  /* LD E,RLC (REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr));
                RLC(E);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x04:  /* LD H,RLC (REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr));
                RLC(H);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x05:  /* LD L,RLC (REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr));
                RLC(L);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x06:  /* RLC (REGISTER+dd) */
                tstates += 8;
            {
                tempreg.set(machine.readbyte(tempaddr));
                Register bytetemp = tempreg;
                RLC(bytetemp);
                machine.writebyte(tempaddr, bytetemp.get());
            }
            break;

            case 0x07:  /* LD A,RLC (REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr));
                RLC(A);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x08:  /* LD B,RRC (REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr));
                RRC(B);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x09:  /* LD C,RRC (REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr));
                RRC(C);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x0a:  /* LD D,RRC (REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr));
                RRC(D);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x0b:  /* LD E,RRC (REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr));
                RRC(E);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x0c:  /* LD H,RRC (REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr));
                RRC(H);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x0d:  /* LD L,RRC (REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr));
                RRC(L);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x0e:  /* RRC (REGISTER+dd) */
                tstates += 8;
            {
                tempreg.set(machine.readbyte(tempaddr));
                Register bytetemp = tempreg;
                RRC(bytetemp);
                machine.writebyte(tempaddr, bytetemp.get());
            }
            break;

            case 0x0f:  /* LD A,RRC (REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr));
                RRC(A);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x10:  /* LD B,RL (REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr));
                RL(B);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x11:  /* LD C,RL (REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr));
                RL(C);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x12:  /* LD D,RL (REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr));
                RL(D);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x13:  /* LD E,RL (REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr));
                RL(E);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x14:  /* LD H,RL (REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr));
                RL(H);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x15:  /* LD L,RL (REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr));
                RL(L);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x16:  /* RL (REGISTER+dd) */
                tstates += 8;
            {
                tempreg.set(machine.readbyte(tempaddr));
                Register bytetemp = tempreg;
                RL(bytetemp);
                machine.writebyte(tempaddr, bytetemp.get());
            }
            break;

            case 0x17:  /* LD A,RL (REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr));
                RL(A);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x18:  /* LD B,RR (REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr));
                RR(B);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x19:  /* LD C,RR (REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr));
                RR(C);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x1a:  /* LD D,RR (REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr));
                RR(D);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x1b:  /* LD E,RR (REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr));
                RR(E);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x1c:  /* LD H,RR (REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr));
                RR(H);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x1d:  /* LD L,RR (REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr));
                RR(L);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x1e:  /* RR (REGISTER+dd) */
                tstates += 8;
            {
                tempreg.set(machine.readbyte(tempaddr));
                Register bytetemp = tempreg;
                RR(bytetemp);
                machine.writebyte(tempaddr, bytetemp.get());
            }
            break;

            case 0x1f:  /* LD A,RR (REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr));
                RR(A);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x20:  /* LD B,SLA (REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr));
                SLA(B);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x21:  /* LD C,SLA (REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr));
                SLA(C);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x22:  /* LD D,SLA (REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr));
                SLA(D);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x23:  /* LD E,SLA (REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr));
                SLA(E);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x24:  /* LD H,SLA (REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr));
                SLA(H);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x25:  /* LD L,SLA (REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr));
                SLA(L);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x26:  /* SLA (REGISTER+dd) */
                tstates += 8;
            {
                tempreg.set(machine.readbyte(tempaddr));
                Register bytetemp = tempreg;
                SLA(bytetemp);
                machine.writebyte(tempaddr, bytetemp.get());
            }
            break;

            case 0x27:  /* LD A,SLA (REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr));
                SLA(A);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x28:  /* LD B,SRA (REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr));
                SRA(B);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x29:  /* LD C,SRA (REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr));
                SRA(C);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x2a:  /* LD D,SRA (REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr));
                SRA(D);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x2b:  /* LD E,SRA (REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr));
                SRA(E);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x2c:  /* LD H,SRA (REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr));
                SRA(H);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x2d:  /* LD L,SRA (REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr));
                SRA(L);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x2e:  /* SRA (REGISTER+dd) */
                tstates += 8;
            {
                tempreg.set(machine.readbyte(tempaddr));
                Register bytetemp = tempreg;
                SRA(bytetemp);
                machine.writebyte(tempaddr, bytetemp.get());
            }
            break;

            case 0x2f:  /* LD A,SRA (REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr));
                SRA(A);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x30:  /* LD B,SLL (REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr));
                SLL(B);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x31:  /* LD C,SLL (REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr));
                SLL(C);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x32:  /* LD D,SLL (REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr));
                SLL(D);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x33:  /* LD E,SLL (REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr));
                SLL(E);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x34:  /* LD H,SLL (REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr));
                SLL(H);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x35:  /* LD L,SLL (REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr));
                SLL(L);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x36:  /* SLL (REGISTER+dd) */
                tstates += 8;
            {
                tempreg.set(machine.readbyte(tempaddr));
                Register bytetemp = tempreg;
                SLL(bytetemp);
                machine.writebyte(tempaddr, bytetemp.get());
            }
            break;

            case 0x37:  /* LD A,SLL (REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr));
                SLL(A);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x38:  /* LD B,SRL (REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr));
                SRL(B);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x39:  /* LD C,SRL (REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr));
                SRL(C);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x3a:  /* LD D,SRL (REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr));
                SRL(D);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x3b:  /* LD E,SRL (REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr));
                SRL(E);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x3c:  /* LD H,SRL (REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr));
                SRL(H);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x3d:  /* LD L,SRL (REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr));
                SRL(L);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x3e:  /* SRL (REGISTER+dd) */
                tstates += 8;
            {
                tempreg.set(machine.readbyte(tempaddr));
                Register bytetemp = tempreg;
                SRL(bytetemp);
                machine.writebyte(tempaddr, bytetemp.get());
            }
            break;

            case 0x3f:  /* LD A,SRL (REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr));
                SRL(A);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x40:  /* BIT 0,(REGISTER+dd) */
            case 0x41:
            case 0x42:
            case 0x43:
            case 0x44:
            case 0x45:
            case 0x46:
            case 0x47:
                tstates += 5;
            {
                tempreg.set(machine.readbyte(tempaddr));
                BIT(0, tempreg);
            }
            break;

            case 0x48:  /* BIT 1,(REGISTER+dd) */
            case 0x49:
            case 0x4a:
            case 0x4b:
            case 0x4c:
            case 0x4d:
            case 0x4e:
            case 0x4f:
                tstates += 5;
            {
                tempreg.set(machine.readbyte(tempaddr));
                BIT(1, tempreg);
            }
            break;

            case 0x50:  /* BIT 2,(REGISTER+dd) */
            case 0x51:
            case 0x52:
            case 0x53:
            case 0x54:
            case 0x55:
            case 0x56:
            case 0x57:
                tstates += 5;
            {
                tempreg.set(machine.readbyte(tempaddr));
                BIT(2, tempreg);
            }
            break;

            case 0x58:  /* BIT 3,(REGISTER+dd) */
            case 0x59:
            case 0x5a:
            case 0x5b:
            case 0x5c:
            case 0x5d:
            case 0x5e:
            case 0x5f:
                tstates += 5;
            {
                tempreg.set(machine.readbyte(tempaddr));
                BIT(3, tempreg);
            }
            break;

            case 0x60:  /* BIT 4,(REGISTER+dd) */
            case 0x61:
            case 0x62:
            case 0x63:
            case 0x64:
            case 0x65:
            case 0x66:
            case 0x67:
                tstates += 5;
            {
                tempreg.set(machine.readbyte(tempaddr));
                BIT(4, tempreg);
            }
            break;

            case 0x68:  /* BIT 5,(REGISTER+dd) */
            case 0x69:
            case 0x6a:
            case 0x6b:
            case 0x6c:
            case 0x6d:
            case 0x6e:
            case 0x6f:
                tstates += 5;
            {
                tempreg.set(machine.readbyte(tempaddr));
                BIT(5, tempreg);
            }
            break;

            case 0x70:  /* BIT 6,(REGISTER+dd) */
            case 0x71:
            case 0x72:
            case 0x73:
            case 0x74:
            case 0x75:
            case 0x76:
            case 0x77:
                tstates += 5;
            {
                tempreg.set(machine.readbyte(tempaddr));
                BIT(6, tempreg);
            }
            break;

            case 0x78:  /* BIT 7,(REGISTER+dd) */
            case 0x79:
            case 0x7a:
            case 0x7b:
            case 0x7c:
            case 0x7d:
            case 0x7e:
            case 0x7f:
                tstates += 5;
            {
                tempreg.set(machine.readbyte(tempaddr));
                BIT7(tempreg);
            }
            break;

            case 0x80:  /* LD B,RES 0,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) & 0xfe);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x81:  /* LD C,RES 0,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) & 0xfe);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x82:  /* LD D,RES 0,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) & 0xfe);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x83:  /* LD E,RES 0,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) & 0xfe);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x84:  /* LD H,RES 0,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) & 0xfe);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x85:  /* LD L,RES 0,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) & 0xfe);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x86:  /* RES 0,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) & 0xfe);
                break;

            case 0x87:  /* LD A,RES 0,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) & 0xfe);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x88:  /* LD B,RES 1,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) & 0xfd);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x89:  /* LD C,RES 1,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) & 0xfd);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x8a:  /* LD D,RES 1,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) & 0xfd);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x8b:  /* LD E,RES 1,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) & 0xfd);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x8c:  /* LD H,RES 1,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) & 0xfd);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x8d:  /* LD L,RES 1,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) & 0xfd);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x8e:  /* RES 1,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) & 0xfd);
                //if( DEBUG ) System.out.print(" RES 1,("+REGISTER.name+"+$"+toHex8(dist)+")");
                break;

            case 0x8f:  /* LD A,RES 1,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) & 0xfd);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x90:  /* LD B,RES 2,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) & 0xfb);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x91:  /* LD C,RES 2,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) & 0xfb);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x92:  /* LD D,RES 2,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) & 0xfb);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x93:  /* LD E,RES 2,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) & 0xfb);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x94:  /* LD H,RES 2,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) & 0xfb);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x95:  /* LD L,RES 2,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) & 0xfb);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x96:  /* RES 2,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) & 0xfb);
                break;

            case 0x97:  /* LD A,RES 2,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) & 0xfb);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0x98:  /* LD B,RES 3,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) & 0xf7);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0x99:  /* LD C,RES 3,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) & 0xf7);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0x9a:  /* LD D,RES 3,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) & 0xf7);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0x9b:  /* LD E,RES 3,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) & 0xf7);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0x9c:  /* LD H,RES 3,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) & 0xf7);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0x9d:  /* LD L,RES 3,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) & 0xf7);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0x9e:  /* RES 3,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) & 0xf7);
                break;

            case 0x9f:  /* LD A,RES 3,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) & 0xf7);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xa0:  /* LD B,RES 4,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) & 0xef);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xa1:  /* LD C,RES 4,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) & 0xef);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xa2:  /* LD D,RES 4,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) & 0xef);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xa3:  /* LD E,RES 4,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) & 0xef);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xa4:  /* LD H,RES 4,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) & 0xef);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xa5:  /* LD L,RES 4,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) & 0xef);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xa6:  /* RES 4,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) & 0xef);
                break;

            case 0xa7:  /* LD A,RES 4,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) & 0xef);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xa8:  /* LD B,RES 5,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) & 0xdf);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xa9:  /* LD C,RES 5,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) & 0xdf);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xaa:  /* LD D,RES 5,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) & 0xdf);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xab:  /* LD E,RES 5,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) & 0xdf);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xac:  /* LD H,RES 5,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) & 0xdf);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xad:  /* LD L,RES 5,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) & 0xdf);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xae:  /* RES 5,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) & 0xdf);
                break;

            case 0xaf:  /* LD A,RES 5,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) & 0xdf);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xb0:  /* LD B,RES 6,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) & 0xbf);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xb1:  /* LD C,RES 6,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) & 0xbf);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xb2:  /* LD D,RES 6,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) & 0xbf);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xb3:  /* LD E,RES 6,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) & 0xbf);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xb4:  /* LD H,RES 6,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) & 0xbf);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xb5:  /* LD L,RES 6,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) & 0xbf);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xb6:  /* RES 6,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) & 0xbf);
                break;

            case 0xb7:  /* LD A,RES 6,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) & 0xbf);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xb8:  /* LD B,RES 7,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) & 0x7f);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xb9:  /* LD C,RES 7,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) & 0x7f);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xba:  /* LD D,RES 7,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) & 0x7f);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xbb:  /* LD E,RES 7,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) & 0x7f);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xbc:  /* LD H,RES 7,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) & 0x7f);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xbd:  /* LD L,RES 7,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) & 0x7f);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xbe:  /* RES 7,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) & 0x7f);
                break;

            case 0xbf:  /* LD A,RES 7,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) & 0x7f);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xc0:  /* LD B,SET 0,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) | 0x01);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xc1:  /* LD C,SET 0,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) | 0x01);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xc2:  /* LD D,SET 0,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) | 0x01);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xc3:  /* LD E,SET 0,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) | 0x01);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xc4:  /* LD H,SET 0,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) | 0x01);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xc5:  /* LD L,SET 0,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) | 0x01);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xc6:  /* SET 0,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) | 0x01);
                break;

            case 0xc7:  /* LD A,SET 0,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) | 0x01);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xc8:  /* LD B,SET 1,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) | 0x02);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xc9:  /* LD C,SET 1,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) | 0x02);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xca:  /* LD D,SET 1,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) | 0x02);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xcb:  /* LD E,SET 1,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) | 0x02);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xcc:  /* LD H,SET 1,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) | 0x02);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xcd:  /* LD L,SET 1,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) | 0x02);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xce:  /* SET 1,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) | 0x02);
                break;

            case 0xcf:  /* LD A,SET 1,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) | 0x02);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xd0:  /* LD B,SET 2,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) | 0x04);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xd1:  /* LD C,SET 2,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) | 0x04);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xd2:  /* LD D,SET 2,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) | 0x04);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xd3:  /* LD E,SET 2,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) | 0x04);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xd4:  /* LD H,SET 2,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) | 0x04);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xd5:  /* LD L,SET 2,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) | 0x04);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xd6:  /* SET 2,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) | 0x04);
                break;

            case 0xd7:  /* LD A,SET 2,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) | 0x04);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xd8:  /* LD B,SET 3,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) | 0x08);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xd9:  /* LD C,SET 3,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) | 0x08);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xda:  /* LD D,SET 3,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) | 0x08);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xdb:  /* LD E,SET 3,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) | 0x08);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xdc:  /* LD H,SET 3,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) | 0x08);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xdd:  /* LD L,SET 3,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) | 0x08);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xde:  /* SET 3,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) | 0x08);
                break;

            case 0xdf:  /* LD A,SET 3,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) | 0x08);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xe0:  /* LD B,SET 4,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) | 0x10);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xe1:  /* LD C,SET 4,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) | 0x10);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xe2:  /* LD D,SET 4,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) | 0x10);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xe3:  /* LD E,SET 4,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) | 0x10);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xe4:  /* LD H,SET 4,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) | 0x10);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xe5:  /* LD L,SET 4,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) | 0x10);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xe6:  /* SET 4,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) | 0x10);
                break;

            case 0xe7:  /* LD A,SET 4,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) | 0x10);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xe8:  /* LD B,SET 5,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) | 0x20);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xe9:  /* LD C,SET 5,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) | 0x20);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xea:  /* LD D,SET 5,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) | 0x20);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xeb:  /* LD E,SET 5,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) | 0x20);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xec:  /* LD H,SET 5,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) | 0x20);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xed:  /* LD L,SET 5,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) | 0x20);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xee:  /* SET 5,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) | 0x20);
                break;

            case 0xef:  /* LD A,SET 5,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) | 0x20);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xf0:  /* LD B,SET 6,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) | 0x40);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xf1:  /* LD C,SET 6,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) | 0x40);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xf2:  /* LD D,SET 6,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) | 0x40);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xf3:  /* LD E,SET 6,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) | 0x40);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xf4:  /* LD H,SET 6,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) | 0x40);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xf5:  /* LD L,SET 6,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) | 0x40);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xf6:  /* SET 6,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) | 0x40);
                break;

            case 0xf7:  /* LD A,SET 6,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) | 0x40);
                machine.writebyte(tempaddr, A.value);
                break;

            case 0xf8:  /* LD B,SET 7,(REGISTER+dd) */
                tstates += 8;
                B.set(machine.readbyte(tempaddr) | 0x80);
                machine.writebyte(tempaddr, B.get());
                break;

            case 0xf9:  /* LD C,SET 7,(REGISTER+dd) */
                tstates += 8;
                C.set(machine.readbyte(tempaddr) | 0x80);
                machine.writebyte(tempaddr, C.get());
                break;

            case 0xfa:  /* LD D,SET 7,(REGISTER+dd) */
                tstates += 8;
                D.set(machine.readbyte(tempaddr) | 0x80);
                machine.writebyte(tempaddr, D.get());
                break;

            case 0xfb:  /* LD E,SET 7,(REGISTER+dd) */
                tstates += 8;
                E.set(machine.readbyte(tempaddr) | 0x80);
                machine.writebyte(tempaddr, E.get());
                break;

            case 0xfc:  /* LD H,SET 7,(REGISTER+dd) */
                tstates += 8;
                H.set(machine.readbyte(tempaddr) | 0x80);
                machine.writebyte(tempaddr, H.get());
                break;

            case 0xfd:  /* LD L,SET 7,(REGISTER+dd) */
                tstates += 8;
                L.set(machine.readbyte(tempaddr) | 0x80);
                machine.writebyte(tempaddr, L.get());
                break;

            case 0xfe:  /* SET 7,(REGISTER+dd) */
                tstates += 8;
                machine.writebyte(tempaddr, machine.readbyte(tempaddr) | 0x80);
                break;

            case 0xff:  /* LD A,SET 7,(REGISTER+dd) */
                tstates += 8;
                A.set(machine.readbyte(tempaddr) | 0x80);
                machine.writebyte(tempaddr, A.value);
                break;
        }
    }
}

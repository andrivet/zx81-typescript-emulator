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


import Machine from "../config/Machine";
import Z80 from "../z80/Z80";
import ZX81Options from "../config/ZX81Options";
import Tape from "../io/Tape";
import ZX81Config, {CHRGENCHR16, CHRGENQS, SYNCTYPEH, SYNCTYPEV} from "../config/ZX81Config";
import Snap from "../io/Snap";
import {KBStatus} from "../io/KBStatus";
import Scanline from "../display/Scanline";
import {ROMPatch} from "./ROMPatch";

export default class ZX81 extends Machine
{
    static VBLANKCOLOUR: number = 0;
    static LASTINSTNONE: number = 0;
    static LASTINSTINFE: number = 1;
    static LASTINSTOUTFE: number = 2;
    static LASTINSTOUTFD: number = 3;
    static LASTINSTOUTFF: number = 4;

    private border: number = 7;
    private ink: number = 0;
    private paper: number = 7;
    private hsync_counter: number = 207;
    private zx81_stop: boolean = false;
    private LastInstruction: number;
    private font: number[] = new Array(1024);
    private memhrg: number[] = new Array(1024);
    private shift_register: number = 0;
    private shift_reg_inv: number;
    private int_pending: boolean = false;
    private z80: Z80;
    private zx81opts: ZX81Options;
    private mTape: Tape;

    public NMI_generator: boolean = false;
    public HSYNC_generator: boolean = false;
    public rowcounter: number = 0;
    public borrow: number = 0;

    public initialise(config: ZX81Config)
    {
        this.zx81opts = config.zx81opts;
        this.CurRom = config.zx81opts.ROM81;
        this.z80 = new Z80(this);
        let snap: Snap = new Snap(config);
        this.mTape = new Tape();
        this.memory = new Array(64 * 1024);
        let i: number;

        for (i = 0; i < 65536; i++)
            this.memory[i] = 7

        snap.memory_load("ROM/" + this.CurRom, 0, 65536, () => {
            this.ink = 0;
            this.paper = this.border = 7;
            this.NMI_generator = false;
            this.HSYNC_generator = false;
            this.z80.reset();
        });
    }

    public writebyte(Address: number, Data: number)
    {
        if (this.zx81opts.chrgen === CHRGENQS && Address >= 33792 && Address <= 34815)
        {
            this.font[Address - 33792] = Data;
            this.zx81opts.enableqschrgen = true;
        }
        if (Address > this.zx81opts.RAMTOP) Address = (Address & (this.zx81opts.RAMTOP));
        if (Address <= this.zx81opts.ROMTOP && this.zx81opts.protectROM)
        {
            return;
        }
        this.memory[Address] = Data;
    }

    public readbyte(Address: number): number
    {
        let data: number;
        if (Address <= this.zx81opts.RAMTOP) data = this.memory[Address]; else data = this.memory[(Address & (this.zx81opts.RAMTOP - 16384)) + 16384];
        return (data);
    }

    public opcode_fetch(Address: number): number
    {
        let inv: boolean;
        let update: boolean = false;
        let opcode: number;
        let bit6: boolean;
        let data: number;
        if (Address < this.zx81opts.m1not)
        {
            data = this.readbyte(Address);
            return (data);
        }
        data = this.readbyte((Address >= 49152) ? Address & 32767 : Address);
        opcode = data;
        bit6 = (opcode & 64) !== 0;
        if (!bit6) opcode = 0;
        inv = (data & 128) !== 0;
        if (!bit6)
        {
            if ((this.zx81opts.chrgen === CHRGENCHR16 && (this.z80.I & 1) !== 0) || (this.zx81opts.chrgen === CHRGENQS && this.zx81opts.enableqschrgen))
                data = ((data & 128) >> 1) | (data & 63); else data = data & 63;
            if (this.z80.I < 64 || (this.z80.I >= 128 && this.z80.I < 192 && this.zx81opts.chrgen === CHRGENCHR16))
            {
                if ((this.zx81opts.chrgen === CHRGENQS && this.zx81opts.enableqschrgen))
                    data = this.font[(data << 3) | this.rowcounter];
                else
                    data = this.readbyte(((this.z80.I & 254) << 8) + (data << 3) | this.rowcounter);
            }
            else
                data = 255;
            update = true;
        }
        if (update)
        {
            this.shift_register |= data;
            this.shift_reg_inv |= inv ? 255 : 0;
            return (0);
        }
        else
        {
            return (opcode);
        }
    }

    public writeport(Address: number, Data: number)
    {
        switch ((Address & 255))
        {
            case 253:
                this.LastInstruction = ZX81.LASTINSTOUTFD;
                break;
            case 254:
                this.LastInstruction = ZX81.LASTINSTOUTFE;
                break;
            default:
                break;
        }
        if (this.LastInstruction === 0) this.LastInstruction = ZX81.LASTINSTOUTFF;
    }

    private beeper: number = 0;

    public readport(Address: number): number
    {
        if ((Address & 1) === 0)
        {
            let keyb: number;
            let data: number = 0;
            let i: number;
            data |= 128;
            this.LastInstruction = ZX81.LASTINSTINFE;
            keyb = (Address / 256 | 0);
            for (i = 0; i < 8; i++)
            {
                if ((keyb & (1 << i)) === 0) data |= KBStatus.ZXKeyboard[i];
            }
            return ((~data) & 255);
        } else switch ((Address & 255))
        {
            case 1:
                return (0);
            case 95:
                return (255);
            case 245:
                this.beeper = 1 - this.beeper;
                return (255);
            default:
                break;
        }
        return (255);
    }

    public contendmem(Address: number, states: number, time: number): number
    {
        return (time);
    }

    public contendio(Address: number, states: number, time: number): number
    {
        return (time);
    }

    public do_scanline(CurScanLine: Scanline): number
    {
        let tstotal: number = 0;
        CurScanLine.scanline_len = 0;
        let MaxScanLen: number = 420;
        if (CurScanLine.sync_valid !== 0)
        {
            CurScanLine.add_blank(this.borrow, this.HSYNC_generator ? (16 * this.paper) : ZX81.VBLANKCOLOUR);
            this.borrow = 0;
            CurScanLine.sync_valid = 0;
            CurScanLine.sync_len = 0;
        }
        do {
            this.LastInstruction = ZX81.LASTINSTNONE;
            this.z80.PC = ROMPatch.PatchTest(this, this.z80);
            let ts: number = this.z80.do_opcode();
            if (this.int_pending)
            {
                ts += this.z80.interrupt(ts);
                this.paper = this.border;
                this.int_pending = false;
            }
            let pixels: number = ts << 1;
            for (let i: number = 0; i < pixels; i++)
            {
                let colour: number;
                let bit: number;
                bit = ((this.shift_register ^ this.shift_reg_inv) & 32768);
                if (this.HSYNC_generator) colour = (bit !== 0 ? this.ink : this.paper) << 4; else colour = ZX81.VBLANKCOLOUR;
                CurScanLine.scanline[CurScanLine.scanline_len++] = colour;
                this.shift_register <<= 1;
                this.shift_reg_inv <<= 1;
            }
            switch ((this.LastInstruction))
            {
                case ZX81.LASTINSTOUTFD:
                    this.NMI_generator = false;
                    if (!this.HSYNC_generator) this.rowcounter = 0;
                    if (CurScanLine.sync_len !== 0) CurScanLine.sync_valid = SYNCTYPEV;
                    this.HSYNC_generator = true;
                    break;
                case ZX81.LASTINSTOUTFE:
                    this.NMI_generator = true;
                    if (!this.HSYNC_generator) this.rowcounter = 0;
                    if (CurScanLine.sync_len !== 0) CurScanLine.sync_valid = SYNCTYPEV;
                    this.HSYNC_generator = true;
                    break;
                case ZX81.LASTINSTINFE:
                    if (!this.NMI_generator)
                    {
                        this.HSYNC_generator = false;
                        if (CurScanLine.sync_len === 0) CurScanLine.sync_valid = 0;
                    }
                    break;
                case ZX81.LASTINSTOUTFF:
                    if (!this.HSYNC_generator) this.rowcounter = 0;
                    if (CurScanLine.sync_len !== 0) CurScanLine.sync_valid = SYNCTYPEV;
                    this.HSYNC_generator = true;
                    break;
                default:
                    break;
            }
            this.hsync_counter -= ts;
            if ((this.z80.R & 64) === 0) this.int_pending = true;
            if (!this.HSYNC_generator) CurScanLine.sync_len += ts;
            if (this.hsync_counter <= 0)
            {
                if (this.NMI_generator)
                {
                    let nmilen: number;
                    nmilen = this.z80.nmi(CurScanLine.scanline_len);
                    this.hsync_counter -= nmilen;
                    ts += nmilen;
                }
                this.borrow = -this.hsync_counter;
                if (this.HSYNC_generator && CurScanLine.sync_len === 0)
                {
                    CurScanLine.sync_len = 10;
                    CurScanLine.sync_valid = SYNCTYPEH;
                    if (CurScanLine.scanline_len >= (this.tperscanline * 2)) CurScanLine.scanline_len = this.tperscanline * 2;
                    this.rowcounter = (++this.rowcounter) & 7;
                }
                this.hsync_counter += this.tperscanline;
            }
            tstotal += ts;
        } while ((CurScanLine.scanline_len < MaxScanLen && CurScanLine.sync_valid === 0 && !this.zx81_stop));
        if (CurScanLine.sync_valid === SYNCTYPEV)
        {
            this.hsync_counter = this.tperscanline;
        }
        return (tstotal);
    }

    public stop(): boolean
    {
        return this.zx81_stop;
    }

    public getTape(): Tape
    {
        return this.mTape;
    }

    constructor()
    {
        super();
        this.LastInstruction = 0;
        this.shift_reg_inv = 0;
    }
}


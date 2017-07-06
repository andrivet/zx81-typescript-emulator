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

import Z80 from "../z80/Z80";
import Tape from "../io/Tape";
import Snap from "../io/Snap";
import {KBStatus} from "../io/KBStatus";
import Scanline from "../display/Scanline";
import {ROMPatch} from "./ROMPatch";

const RAMTOP: number = 32767;
const ROMTOP: number = 8191;
const ROM: string = "ROM/ZX81.data";

const SYNCTYPEH : number = 1;
const SYNCTYPEV : number = 2;

export default class ZX81
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
    private lastInstruction: number;
    private shift_register: number = 0;
    private shift_reg_inv: number;
    private int_pending: boolean = false;
    private z80: Z80;
    private tape: Tape;

    public tperscanline: number = 207;
    public tperframe: number = 312 * 207;
    public memory: number[];
    public NMI_generator: boolean = false;
    public HSYNC_generator: boolean = false;
    public rowcounter: number = 0;
    public borrow: number = 0;

    public constructor()
    {
        this.lastInstruction = 0;
        this.shift_reg_inv = 0;

        this.z80 = new Z80(this);
        this.tape = new Tape();
        this.memory = new Array(64 * 1024);
        let i: number;

        for (i = 0; i < 65536; i++)
            this.memory[i] = 7

        let snap: Snap = new Snap(this);
        snap.memory_load(ROM, 0, 65536, () => {
            this.ink = 0;
            this.paper = this.border = 7;
            this.NMI_generator = false;
            this.HSYNC_generator = false;
            this.z80.reset();
        });
    }

    public writebyte(address: number, data: number)
    {
        if (address > RAMTOP)
            address = address & RAMTOP;
        if (address <= ROMTOP)
            return;
        this.memory[address] = data;
    }

    public readbyte(address: number): number
    {
        let data: number;
        if (address <= RAMTOP)
            data = this.memory[address];
        else
            data = this.memory[(address & (RAMTOP - 16384)) + 16384];
        return (data);
    }

    public opcode_fetch(address: number): number
    {
        let inv: boolean;
        let update: boolean = false;
        let opcode: number;
        let bit6: boolean;
        let data: number;

        if(address <= RAMTOP)
            return this.readbyte(address);

        data = this.readbyte((address >= 49152) ? address & 32767 : address);
        opcode = data;
        bit6 = (opcode & 64) !== 0;
        if (!bit6)
            opcode = 0;
        inv = (data & 128) !== 0;
        if (!bit6)
        {
            data = data & 63;
            if (this.z80.I < 64)
                data = this.readbyte(((this.z80.I & 254) << 8) + (data << 3) | this.rowcounter);
            else
                data = 255;
            update = true;
        }

        if(update)
        {
            this.shift_register |= data;
            this.shift_reg_inv |= inv ? 255 : 0;
            return (0);
        }
        else
            return (opcode);
    }

    public writeport(address: number, Data: number)
    {
        switch (address & 255)
        {
            case 253:
                this.lastInstruction = ZX81.LASTINSTOUTFD;
                break;
            case 254:
                this.lastInstruction = ZX81.LASTINSTOUTFE;
                break;
            default:
                break;
        }
        if (this.lastInstruction === 0) this.lastInstruction = ZX81.LASTINSTOUTFF;
    }

    public readport(address: number): number
    {
        if ((address & 1) === 0)
        {
            let keyb: number;
            let data: number = 0;
            let i: number;
            data |= 128;
            this.lastInstruction = ZX81.LASTINSTINFE;
            keyb = (address / 256 | 0);
            for (i = 0; i < 8; i++)
            {
                if ((keyb & (1 << i)) === 0) data |= KBStatus.ZXKeyboard[i];
            }
            return (~data) & 255;
        }
        else switch (address & 255)
        {
            case 1:
                return 0;
            case 95:
                return 255;
            case 245:
                return 255;
            default:
                break;
        }
        return 255;
    }

    public contendmem(Address: number, states: number, time: number): number
    {
        return time;
    }

    public contendio(Address: number, states: number, time: number): number
    {
        return time;
    }

    public do_scanline(scanLine: Scanline): number
    {
        let tstotal: number = 0;
        scanLine.scanline_len = 0;
        let maxScanLen: number = 420;
        if (scanLine.sync_valid !== 0)
        {
            scanLine.add_blank(this.borrow, this.HSYNC_generator ? (16 * this.paper) : ZX81.VBLANKCOLOUR);
            this.borrow = 0;
            scanLine.sync_valid = 0;
            scanLine.sync_len = 0;
        }
        do
        {
            this.lastInstruction = ZX81.LASTINSTNONE;
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
                if (this.HSYNC_generator)
                    colour = (bit !== 0 ? this.ink : this.paper) << 4;
                else
                    colour = ZX81.VBLANKCOLOUR;
                scanLine.scanline[scanLine.scanline_len++] = colour;
                this.shift_register <<= 1;
                this.shift_reg_inv <<= 1;
            }
            switch ((this.lastInstruction))
            {
                case ZX81.LASTINSTOUTFD:
                    this.NMI_generator = false;
                    if (!this.HSYNC_generator) this.rowcounter = 0;
                    if (scanLine.sync_len !== 0) scanLine.sync_valid = SYNCTYPEV;
                    this.HSYNC_generator = true;
                    break;
                case ZX81.LASTINSTOUTFE:
                    this.NMI_generator = true;
                    if (!this.HSYNC_generator) this.rowcounter = 0;
                    if (scanLine.sync_len !== 0) scanLine.sync_valid = SYNCTYPEV;
                    this.HSYNC_generator = true;
                    break;
                case ZX81.LASTINSTINFE:
                    if (!this.NMI_generator)
                    {
                        this.HSYNC_generator = false;
                        if (scanLine.sync_len === 0) scanLine.sync_valid = 0;
                    }
                    break;
                case ZX81.LASTINSTOUTFF:
                    if (!this.HSYNC_generator) this.rowcounter = 0;
                    if (scanLine.sync_len !== 0) scanLine.sync_valid = SYNCTYPEV;
                    this.HSYNC_generator = true;
                    break;
                default:
                    break;
            }
            this.hsync_counter -= ts;
            if ((this.z80.R & 64) === 0) this.int_pending = true;
            if (!this.HSYNC_generator) scanLine.sync_len += ts;
            if (this.hsync_counter <= 0)
            {
                if (this.NMI_generator)
                {
                    let nmilen: number;
                    nmilen = this.z80.nmi(scanLine.scanline_len);
                    this.hsync_counter -= nmilen;
                    ts += nmilen;
                }
                this.borrow = -this.hsync_counter;
                if (this.HSYNC_generator && scanLine.sync_len === 0)
                {
                    scanLine.sync_len = 10;
                    scanLine.sync_valid = SYNCTYPEH;
                    if (scanLine.scanline_len >= (this.tperscanline * 2)) scanLine.scanline_len = this.tperscanline * 2;
                    this.rowcounter = (++this.rowcounter) & 7;
                }
                this.hsync_counter += this.tperscanline;
            }
            tstotal += ts;
        }
        while ((scanLine.scanline_len < maxScanLen && scanLine.sync_valid === 0 && !this.zx81_stop));

        if (scanLine.sync_valid === SYNCTYPEV)
            this.hsync_counter = this.tperscanline;

        return tstotal;
    }

    public stop(): boolean
    {
        return this.zx81_stop;
    }

    public getTape(): Tape
    {
        return this.tape;
    }
}


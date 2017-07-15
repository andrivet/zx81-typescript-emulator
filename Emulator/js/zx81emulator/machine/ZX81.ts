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
import Keyboard, {VK_J, VK_P, VK_ENTER, VK_SHIFT} from "../io/Keyboard";
import Resource from "../io/Resource";
import Machine from "../machine/Machine";
import Scanline from "../display/Scanline";

const RAMTOP: number = 32767;
const ROMTOP: number = 8191;
const ROM: string = "ROM/ZX81.data";

const enum SYNCTYPE { H = 1, V = 2 }
const enum LASTINST { NONE = 0, INFE, OUTFE, OUTFD, OUTFF }
const enum COLOR { BLACK = 0, WHITE = 1 }
const MEMORY_SIZE = 64 * 1024;

export default class ZX81 extends Machine
{
    private keyboard: Keyboard = new Keyboard();
    private hsyncCounter: number = 207;
    private lastInstruction: number = 0;
    private shiftRegister: number = 0;
    private shiftRegInv: number = 0;
    private intPending: boolean = false;
    private z80: Z80 = new Z80(this);
    private program: Uint8Array = null;
    private memory: Uint8Array = new Uint8Array(MEMORY_SIZE);
    private nmiGenerator: boolean = false;
    private hsyncGenerator: boolean = false;
    private rowCounter: number = 0;
    private borrow: number = 0;

    public constructor()
    {
        super();

        for (let i = 0; i < MEMORY_SIZE; i++)
            this.memory[i] = 7
    }

    public async loadROM(): Promise<void>
    {
        await this.memory_load(ROM, 0, MEMORY_SIZE);
        this.nmiGenerator = false;
        this.hsyncGenerator = false;
        this.z80.reset();
    }

    public writeByte(address: number, data: number)
    {
        if (address > RAMTOP)
            address = address & RAMTOP;
        if (address <= ROMTOP)
            return;
        this.memory[address] = data;
    }

    public readByte(address: number): number
    {
        return (address <= RAMTOP)
            ? this.memory[address]
            : this.memory[(address & (RAMTOP - 16384)) + 16384];
    }

    // Given an address, opcode fetch return the byte at that memory address,
    // modified depending on certain circumstances.
    // It also loads the video shift register.
    //
    // If Address is less than M1NOT, all code is executed,
    // the shift register is cleared.
    //
    // If Address >= M1NOT, and bit 6 of the fetched opcode is not set
    // a NOP is returned and we load the shift register accordingly,
    // depending on which video system is in use (WRX/Memotech/etc.)
    //
    // The ZX81 has effectively two busses.  The ROM is on the first bus
    // while (usually) RAM is on the second.  In video generation, the ROM
    // bus is used to get character bitmap data while the second bus
    // is used to get the display file.  This is important because depending
    // on which bus RAM is placed, it can either be used for extended
    // Fonts OR WRX style hi-res graphics, but never both.
    public opcodeFetch(address: number): number
    {
        let update: boolean = false;

        if(address <= RAMTOP)
            return this.readByte(address);

        let data: number = this.readByte((address >= 49152) ? address & 32767 : address);
        let opcode: number = data;
        let bit6: boolean = (opcode & 64) !== 0;
        if (!bit6)
            opcode = 0;
        let inv: boolean = (data & 128) !== 0;
        if (!bit6)
        {
            // standard ZX81 character sets are only 64 characters in size.
            data = data & 63;
            // If points to ROM, fetch the bitmap from there.
            // Otherwise, we can't get a bitmap from anywhere, so display 11111111 (what does a real ZX81 do?).
            if (this.z80.I < 64)
                data = this.readByte(((this.z80.I & 254) << 8) + (data << 3) | this.rowCounter);
            else
                data = 255;
            update = true;
        }

        if(update)
        {
            // load the bitmap we retrieved into the video shift register
            this.shiftRegister |= data;
            this.shiftRegInv |= inv ? 255 : 0;
            return (0);
        }
        else
            // This is the fallthrough for when we found an opcode with
            // bit 6 set in the display file.  We actually execute these opcodes
            return (opcode);
    }

    public writePort(address: number, data: number)
    {
        switch (address & 255)
        {
            case 253:
                this.lastInstruction = LASTINST.OUTFD;
                break;
            case 254:
                this.lastInstruction = LASTINST.OUTFE;
                break;
            default:
                break;
        }
        if (this.lastInstruction === 0)
            this.lastInstruction = LASTINST.OUTFF;
    }

    public readPort(address: number): number
    {
        if ((address & 1) === 0)
        {
            let data: number = 128;
            this.lastInstruction = LASTINST.INFE;
            let keyb: number = address / 256;
            for (let i = 0; i < 8; i++)
            {
                if ((keyb & (1 << i)) === 0)
                    data |= this.keyboard.get(i);
            }
            return (~data) & 255;
        }
        else if ((address & 255) == 1)
            return 0;

        return 255;
    }

    public contendMem(address: number, states: number, time: number): number
    {
        return time;
    }

    public contendIO(address: number, states: number, time: number): number
    {
        return time;
    }

    private  pop16(): number
    {
        let l = this.memory[this.z80.SP++];
        let h = this.memory[this.z80.SP++];
        return ((h << 8) | l);
    }

    private patch(): number
    {
        let b: number = this.memory[this.z80.PC];
        if (this.z80.PC === 0x0356 && b === 0x1f) // ZX81, start loading
        {
            if (this.program != null)
            {
                for(let i: number = 0; i < this.program.length; i++)
                    this.memory[0x4009 + i] = this.program[i];
                this.pop16();
                return 0x0207;  // ZX81, load complete.
            }
        }

        return this.z80.PC;
    }

    public doScanline(scanLine: Scanline): number
    {
        let tstotal: number = 0;
        scanLine.reset();

        let maxScanLen: number = 420;
        if (scanLine.getSyncValid() !== 0)
        {
            scanLine.addBlank(this.borrow, this.hsyncGenerator ? COLOR.WHITE : COLOR.BLACK);
            this.borrow = 0;
        }

        do
        {
            this.lastInstruction = LASTINST.NONE;
            this.z80.PC = this.patch();
            let ts: number = this.z80.doOpcode();

            if (this.intPending)
            {
                ts += this.z80.interrupt(ts);
                this.intPending = false;
            }

            let pixels: number = ts << 1;
            for (let i: number = 0; i < pixels; i++)
            {
                let bit: number = ((this.shiftRegister ^ this.shiftRegInv) & 32768);

                let colour: number = 0;
                if (this.hsyncGenerator)
                    colour = (bit !== 0 ? COLOR.BLACK : COLOR.WHITE);

                scanLine.addPixel(colour);

                this.shiftRegister <<= 1;
                this.shiftRegInv <<= 1;
            }

            switch (this.lastInstruction)
            {
                case LASTINST.OUTFD:
                    this.nmiGenerator = false;
                    if (!this.hsyncGenerator)
                        this.rowCounter = 0;
                    if (scanLine.getSyncLength() !== 0)
                        scanLine.setSynValid(SYNCTYPE.V);
                    this.hsyncGenerator = true;
                    break;
                case LASTINST.OUTFE:
                    this.nmiGenerator = true;
                    if (!this.hsyncGenerator)
                        this.rowCounter = 0;
                    if (scanLine.getSyncLength() !== 0)
                        scanLine.setSynValid(SYNCTYPE.V);
                    this.hsyncGenerator = true;
                    break;
                case LASTINST.INFE:
                    if (!this.nmiGenerator)
                    {
                        this.hsyncGenerator = false;
                        if (scanLine.getSyncLength() === 0)
                            scanLine.setSynValid(0);
                    }
                    break;
                case LASTINST.OUTFF:
                    if (!this.hsyncGenerator)
                        this.rowCounter = 0;
                    if (scanLine.getSyncLength() !== 0)
                        scanLine.setSynValid(SYNCTYPE.V);
                    this.hsyncGenerator = true;
                    break;
                default:
                    break;
            }

            this.hsyncCounter -= ts;

            if ((this.z80.R & 64) === 0)
                this.intPending = true;
            if (!this.hsyncGenerator)
                scanLine.addSyncLength(ts);

            if (this.hsyncCounter <= 0)
            {
                if (this.nmiGenerator)
                {
                    let nmilen: number = this.z80.nmi(scanLine.getLength());
                    this.hsyncCounter -= nmilen;
                    ts += nmilen;
                }

                this.borrow = -this.hsyncCounter;
                if (this.hsyncGenerator && scanLine.getSyncLength() === 0)
                {
                    scanLine.resetSync(10, SYNCTYPE.H);
                    if (scanLine.getLength() >= (this.tPerScanLine * 2))
                        scanLine.reset(this.tPerScanLine * 2);
                    this.rowCounter = (++this.rowCounter) & 7;
                }
                this.hsyncCounter += this.tPerScanLine;
            }
            tstotal += ts;
        }
        while (scanLine.getLength() < maxScanLen && scanLine.getSyncValid() == 0);

        if (scanLine.getSyncValid() === SYNCTYPE.V)
            this.hsyncCounter = this.tPerScanLine;

        return tstotal;
    }

    private async memory_load(filename: string, address: number, length: number): Promise<void>
    {
        let resource: Resource = new Resource();
        let data = await resource.get(filename);

        let maxLength = (data.length < length) ? data.length : length;
        for (let i = 0; i < maxLength; ++i)
            this.memory[address + i] = data[i];
    }

    public async load_program(filename: string): Promise<void>
    {
        let program = new Resource();
        let data = await program.get(filename);
        this.program = data;
    }

    public keyDown(code: number, shift: boolean = false)
    {
        this.keyboard.keyDown(code, shift);
    }

    public keyUp(code: number, shift: boolean = false)
    {
        this.keyboard.keyUp(code, shift);
    }

    private async key(code: number, shift: boolean = false)
    {
        if(shift)
        {
            this.keyDown(VK_SHIFT, true);
            await Machine.sleep(200);
        }
        this.keyDown(code, shift);
        await Machine.sleep(200);
        this.keyUp(code);
        await Machine.sleep(200);
        if(shift)
        {
            this.keyUp(VK_SHIFT);
            await Machine.sleep(200);
        }
    }

    public async autoLoad()
    {
        await Machine.sleep(4000);

        await this.key(VK_J);
        await this.key(VK_P, true);
        await this.key(VK_P, true);
        await this.key(VK_ENTER);
    }
}


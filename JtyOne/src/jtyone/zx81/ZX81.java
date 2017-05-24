/* 
 * Copyright (C) 2003-2006 Michael D Wynne
 * Java translation (C) 2006 Simon Holdsworth
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * zx81.c
 *
 */

/*
#include <stdlib.h>
#include <fcntl.h>
#include <io.h>

#include "zx81.h"
#include "z80\z80.h"
#include "snap.h"
#include "zx81config.h"
#include "WavCInterface.h"
#include "sound.h"
#include "dev8255.h"
#include "serialport.h"
#include "zxprinter_c.h"
#include "rompatch.h"
#include "tzxman.h"
*/

package jtyone.zx81;

import jtyone.config.Machine;
import jtyone.config.ZX81Config;
import jtyone.config.ZX81ConfigDefs;
import jtyone.config.ZX81Options;
import jtyone.display.Scanline;
import jtyone.io.KBStatus;
import jtyone.io.Snap;
import jtyone.io.Tape;
import jtyone.z80.Z80;

import java.io.IOException;

public final class ZX81
        extends Machine
        implements ZX81ConfigDefs // Allow use of constant names directly.
{

    private static final int VBLANKCOLOUR = 0;

    private static final int LASTINSTNONE = 0;
    private static final int LASTINSTINFE = 1;
    private static final int LASTINSTOUTFE = 2;
    private static final int LASTINSTOUTFD = 3;
    private static final int LASTINSTOUTFF = 4;

    private int border = 7, ink = 0, paper = 7;
    public boolean NMI_generator = false;
    public boolean HSYNC_generator = false;
    public int rowcounter = 0;
    private int hsync_counter = 207;
    public int borrow = 0;

    private boolean zx81_stop = false;
    private int LastInstruction;

    private int[] font = new int[1024];
    private int[] memhrg = new int[1024];

    private int shift_register = 0;
    private int shift_reg_inv;

    private boolean int_pending = false;

    // Added here to avoid needing to update original code
    private Z80 z80;
    private ZX81Options zx81opts;
    private Tape mTape;

    public void initialise(ZX81Config config) {
        zx81opts = config.zx81opts;
        z80 = new Z80(this);
        Snap snap = new Snap(config);
        mTape = new Tape();

        memory = new int[64 * 1024];

        try {
            int i, romlen;
            //z80_init();

            for (i = 0; i < 65536; i++) memory[i] = 7;

            romlen = snap.memory_load(CurRom, 0, 65536);
            zx81opts.romcrc = CRC32Block(memory, romlen);

            if (zx81opts.extfont) snap.font_load("lmbfnt.rom", font, 512);
            if (zx81opts.chrgen == CHRGENDK) romlen += snap.memory_load("dkchr.rom", 8192, 65536);

            if (zx81opts.shadowROM && romlen <= 8192) {
                for (i = 0; i < 8192; i++) memory[i + 8192] = memory[i];
                zx81opts.ROMTOP = 16383;
            } else zx81opts.ROMTOP = romlen - 1;

            if (zx81opts.truehires == HIRESMEMOTECH) snap.memory_load("memohrg.rom", 8192, 2048);
            if (zx81opts.truehires == HIRESG007) snap.memory_load("g007hrg.rom", 10240, 2048);

            else {
                ink = 0;
                paper = border = 7;
            }

            NMI_generator = false;
            HSYNC_generator = false;

            z80.reset();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    public void writebyte(int Address, int Data) {
        //noise = (noise<<8) | Data;
        // TODO: AccDraw.noise = (AccDraw.noise<<8) | Data;

        if (zx81opts.chrgen == CHRGENQS && Address >= 0x8400 && Address <= 0x87ff) {
            font[Address - 0x8400] = Data;
            zx81opts.enableqschrgen = true;
        }

        if (Address > zx81opts.RAMTOP) Address = (Address & (zx81opts.RAMTOP));

        if (Address <= zx81opts.ROMTOP && zx81opts.protectROM) {
            if ((zx81opts.truehires == HIRESMEMOTECH) && (Address < 1024))
                memhrg[Address] = Data;
            return;
        }

        if (Address > 8191 && Address < 16384 && zx81opts.shadowROM && zx81opts.protectROM) return;
        if (Address < 10240 && zx81opts.truehires == HIRESMEMOTECH) return;
        if (Address >= 10240 && Address < 12288 && zx81opts.truehires == HIRESG007) return;

        memory[Address] = Data;
    }

    public int readbyte(int Address) {
        int data;

        if (Address <= zx81opts.RAMTOP) data = memory[Address];
        else data = memory[(Address & (zx81opts.RAMTOP - 16384)) + 16384];

        // TODO: AccDraw.noise = (AccDraw.noise<<8) | data;
        return (data);
    }

// BYTE opcode_fetch(int Address)
//
// Given an address, opcode fetch return the byte at that memory address,
// modified depending on certain circumstances.
// It also loads the video shift register and generates video noise.
//
// If Address is less than M1NOT, all code is executed,
// the shift register is cleared and video noise is set to what is on
// the data bus.
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

    public int opcode_fetch(int Address) {

        boolean inv, update = false;
        int opcode;
        boolean bit6;
        int data;

        if (Address < zx81opts.m1not) {
            // This is not video related, so just return the opcode
            // and make some noise onscreen.
            //data = zx81_readbyte(Address);
            //noise |= data;
            data = readbyte(Address);
            // TODO: AccDraw.noise |= data;
            return (data);
        }

        // We can only execute code below M1NOT.  If an opcode fetch occurs
        // above M1NOT, we actually fetch (address&32767).  This is important
        // because it makes it impossible to place the display file in the
        // 48-64k region if a 64k RAM Pack is used.  How does the real
        // Hardware work?

        data = readbyte((Address >= 49152) ? Address & 32767 : Address);
        opcode = data;
        bit6 = (opcode & 64) != 0;

        // Since we got here, we're generating video (ouch!)
        // Bit six of the opcode is important.  If set, the opcode
        // gets executed and nothing appears onscreen.  If unset
        // the Z80 executes a NOP and the code is used to somehow
        // generate the TV picture (exactly how depends on which
        // display method is used)

        if (!bit6) opcode = 0;
        inv = (data & 128) != 0;

        // First check for WRX graphics.  This is easy, we just create a
        // 16 bit Address from the IR Register pair and fetch that byte
        // loading it into the video shift register.
        if (zx81opts.truehires == HIRESWRX && z80.I >= zx81opts.maxireg && !bit6) {
            data = readbyte((z80.I << 8) | (z80.R7 & 128) | ((z80.R - 1) & 127));
            update = true;
        } else if (!bit6) {
            // If we get here, we're generating normal Characters
            // (or pseudo Hi-Res), but we still need to figure out
            // where to get the bitmap for the character from

            // First try to figure out which character set we're going
            // to use if CHR$x16 is in use.  Else, standard ZX81
            // character sets are only 64 characters in size.

            if ((zx81opts.chrgen == CHRGENCHR16 && (z80.I & 1) != 0)
                    || (zx81opts.chrgen == CHRGENQS && zx81opts.enableqschrgen))
                data = ((data & 128) >> 1) | (data & 63);
            else data = data & 63;


            // If I points to ROM, OR I points to the 8-16k region for
            // CHR$x16, we'll fetch the bitmap from there.
            // Lambda and the QS Character board have external memory
            // where the character set is stored, so if one of those
            // is enabled we better fetch it from the dedicated
            // external memory.
            // Otherwise, we can't get a bitmap from anywhere, so
            // display 11111111 (??What does a real ZX81 do?).

            if (z80.I < 64 || (z80.I >= 128 && z80.I < 192 && zx81opts.chrgen == CHRGENCHR16)) {
                if (zx81opts.extfont || (zx81opts.chrgen == CHRGENQS && zx81opts.enableqschrgen))
                    data = font[(data << 3) | rowcounter];
                else data = readbyte(((z80.I & 254) << 8) + (data << 3) | rowcounter);
            } else data = 255;

            update = true;
        }

        if (update) {
            // Update gets set to true if we managed to fetch a bitmap from
            // somewhere.  The only time this doesn't happen is if we encountered
            // an opcode with bit 6 set above M1NOT.

            // Finally load the bitmap we retrieved into the video shift
            // register, remembering to make some video noise too.

            shift_register |= data;
            shift_reg_inv |= inv ? 255 : 0;
            //if (zx81.machine==MACHINELAMBDA) noise |= (Address>>8);
            //else noise |= z80.i;
            // TODO: if (zx81.machine==MACHINELAMBDA) AccDraw.noise |= (Address>>8);
            // TODO: else AccDraw.noise |= z80.i.get();
            return (0);
        } else {
            // This is the fallthrough for when we found an opcode with
            // bit 6 set in the display file.  We actually execute these
            // opcodes, and generate the noise.

            //noise |= data;
            // TODO: AccDraw.noise |= data;
            return (opcode);
        }
    }

    public void writeport(int Address, int Data) {
        switch (Address & 255) {
            case 0xfd:
                LastInstruction = LASTINSTOUTFD;
                break;
            case 0xfe:
                LastInstruction = LASTINSTOUTFE;
                break;
            default:
                break;
        }

        if (LastInstruction == 0) LastInstruction = LASTINSTOUTFF;
        if (zx81opts.vsyncsound)
            sound_beeper(1);
    }

    private int beeper = 0;

    public int readport(int Address) {
        if ((Address & 1) == 0) {
            int keyb, data = 0;
            int i;
            if (zx81opts.vsyncsound)
                sound_beeper(0);
            if (zx81opts.NTSC) data |= 64;
            if (!GetEarState()) data |= 128;

            LastInstruction = LASTINSTINFE;

            keyb = Address / 256;
            for (i = 0; i < 8; i++) {
                if ((keyb & (1 << i)) == 0) data |= KBStatus.ZXKeyboard[i];
            }
            return ((~data) & 0xff);

        } else
            switch (Address & 255) {
                case 0x01: {
                    // TODO: what's this about?!?!
                    return (0);
                }

                case 0x5f:
                    return (255);

                case 0x73:
                    if (zx81opts.ts2050) return (d8251readDATA());

                case 0x77:
                    if (zx81opts.ts2050) return (d8251readCTRL());

                case 0xf5:
                    beeper = 1 - beeper;
                    return (255);
                case 0xfb:
                    if (zx81opts.zxprinter) return (ZXPrinterReadPort());
                default:
                    break;
            }
        return (255);
    }

    public int contendmem(int Address, int states, int time) {
        return (time);
    }

    public int contendio(int Address, int states, int time) {
        return (time);
    }

    public int do_scanline(Scanline CurScanLine) {
        int tstotal = 0;
        CurScanLine.scanline_len = 0;

        int MaxScanLen = (zx81opts.single_step ? 1 : 420);

        if (CurScanLine.sync_valid != 0) {
            CurScanLine.add_blank(borrow, HSYNC_generator ? (16 * paper) : VBLANKCOLOUR);
            borrow = 0;
            CurScanLine.sync_valid = 0;
            CurScanLine.sync_len = 0;
        }
        do {
            LastInstruction = LASTINSTNONE;
            z80.PC = ROMPatch.PatchTest(this, z80);
            int ts = z80.do_opcode();

            if (int_pending) {
                ts += z80.interrupt(ts);
                paper = border;
                int_pending = false;
            }

            // TODO: AccDraw.frametstates += ts;
            WavClockTick(ts, !HSYNC_generator);
            if (zx81opts.zxprinter) ZXPrinterClockTick(ts);

            int pixels = ts << 1;

            for (int i = 0; i < pixels; i++) {
                int colour, bit;

                bit = ((shift_register ^ shift_reg_inv) & 32768);

                if (HSYNC_generator) colour = (bit != 0 ? ink : paper) << 4;
                else colour = VBLANKCOLOUR;

                CurScanLine.scanline[CurScanLine.scanline_len++] = colour;

                shift_register <<= 1;
                shift_reg_inv <<= 1;
            }

            switch (LastInstruction) {
                case LASTINSTOUTFD:
                    NMI_generator = false;
                    if (!HSYNC_generator) rowcounter = 0;
                    if (CurScanLine.sync_len != 0) CurScanLine.sync_valid = SYNCTYPEV;
                    HSYNC_generator = true;
                    break;
                case LASTINSTOUTFE:
                    NMI_generator = true;
                    if (!HSYNC_generator) rowcounter = 0;
                    if (CurScanLine.sync_len != 0) CurScanLine.sync_valid = SYNCTYPEV;
                    HSYNC_generator = true;
                    break;
                case LASTINSTINFE:
                    if (!NMI_generator) {
                        HSYNC_generator = false;
                        if (CurScanLine.sync_len == 0) CurScanLine.sync_valid = 0;
                    }
                    break;
                case LASTINSTOUTFF:
                    if (!HSYNC_generator) rowcounter = 0;
                    if (CurScanLine.sync_len != 0) CurScanLine.sync_valid = SYNCTYPEV;
                    HSYNC_generator = true;
                    break;
                default:
                    break;
            }

            hsync_counter -= ts;

            if ((z80.R & 64) == 0)
                int_pending = true;
            if (!HSYNC_generator) CurScanLine.sync_len += ts;
            if (hsync_counter <= 0) {
                if (NMI_generator) {
                    int nmilen;
                    nmilen = z80.nmi(CurScanLine.scanline_len);
                    hsync_counter -= nmilen;
                    ts += nmilen;
                }

                borrow = -hsync_counter;
                if (HSYNC_generator && CurScanLine.sync_len == 0) {
                    CurScanLine.sync_len = 10;
                    CurScanLine.sync_valid = SYNCTYPEH;
                    if (CurScanLine.scanline_len >= (tperscanline * 2))
                        CurScanLine.scanline_len = tperscanline * 2;
                    rowcounter = (++rowcounter) & 7;
                }
                hsync_counter += tperscanline;
            }

            tstotal += ts;

            // TODO: DebugUpdate();

        } while (CurScanLine.scanline_len < MaxScanLen && CurScanLine.sync_valid == 0 && !zx81_stop);

        if (CurScanLine.sync_valid == SYNCTYPEV) {
            hsync_counter = tperscanline;
            //borrow=0;
        }

        return (tstotal);
    }

    // From Machine
    public void reset() {
    }

    public void nmi() {
    }

    public void exit() {
    }

    public boolean stop() {
        return zx81_stop;
    }

    public Tape getTape() {
        return mTape;
    }

    private int CRC32Block(int[] memory, int romlen) {
        return 0;
    }

    private int ZXPrinterReadPort() {
        return 0;
    }

    private void sound_beeper(int a) {
    }

    private boolean GetEarState() {
        return false;
    }

    private int d8251readDATA() {
        return 0;
    }

    private int d8251readCTRL() {
        return 0;
    }

    private void WavClockTick(int a, boolean b) {
    }

    private void ZXPrinterClockTick(int a) {
    }
}
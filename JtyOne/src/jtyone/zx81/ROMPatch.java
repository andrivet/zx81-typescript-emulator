/* EightyOne  - A Windows ZX80/81/clone emulator.
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
 *
 * rompatch.c
 */

package jtyone.zx81;

import jtyone.config.Machine;
import jtyone.z80.Z80;


class ROMPatch {

    private static int pop16(Machine machine, Z80 z80) {
        int h, l;

        l = machine.memory[z80.SP++];
        h = machine.memory[z80.SP++];
        return ((h << 8) | l);
    }

    static int PatchTest(Machine machine, Z80 z80) {
        int b = machine.memory[z80.PC];

        if (z80.PC == 0x0356 && b == 0x1f)  // ZX81, start loading
        {
            byte[] currentProgram = machine.getTape().getNextEntry();
            if (currentProgram != null) {
                // Skip the ZX81 program name.
                // TODO: really ought to compare the ZX81 program name with that being
                // loaded (if any).
                int pos = 0;
                while ((currentProgram[pos++] & 0x80) == 0) /* empty*/;
                for (int i = pos; i < currentProgram.length; i++)
                    machine.memory[0x4009 + i - pos] = currentProgram[i] & 0xff;
                // Note: can't do arraycopy as memory is ints, currentProgram is bytes...

                pop16(machine, z80);
                return 0x0207;    // ZX81, load complete.
            }
        }

        if (z80.PC == 0x0222 && b == 0x3E)  // ZX80, start loading
        {
            byte[] currentProgram = machine.getTape().getNextEntry();
            if (currentProgram != null) {
                for (int i = 0; i < currentProgram.length; i++)
                    machine.memory[0x4000 + i] = currentProgram[i] & 0xff;
                // Note: can't do arraycopy as memory is ints, currentProgram is bytes...

                pop16(machine, z80);
                return 0x0203;    // ZX80, load complete.
            }
        }

        return (z80.PC);
    }
}
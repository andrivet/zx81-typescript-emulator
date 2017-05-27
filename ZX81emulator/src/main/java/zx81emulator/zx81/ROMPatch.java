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
 * along with ZX81emulator.  If not, see <http://www.gnu.org/licenses/>.
 */
package zx81emulator.zx81;

import zx81emulator.config.Machine;
import zx81emulator.z80.Z80;


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
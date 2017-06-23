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

namespace zx81emulator.zx81 {
    import Machine = zx81emulator.config.Machine;

    import Z80 = zx81emulator.z80.Z80;

    export class ROMPatch {
        private static pop16(machine : Machine, z80 : Z80) : number {
            let h : number;
            let l : number;
            l = machine.memory[z80.SP++];
            h = machine.memory[z80.SP++];
            return ((h << 8) | l);
        }

        static PatchTest(machine : Machine, z80 : Z80) : number {
            let b : number = machine.memory[z80.PC];
            if(z80.PC === 854 && b === 31) {
                let currentProgram : number[] = machine.getTape().getNextEntry();
                if(currentProgram != null) {
                    let pos : number = 0;
                    while(((currentProgram[pos++] & 128) === 0));
                    for(let i : number = pos; i < currentProgram.length; i++) machine.memory[16393 + i - pos] = currentProgram[i] & 255
                    ROMPatch.pop16(machine, z80);
                    return 519;
                }
            }
            if(z80.PC === 546 && b === 62) {
                let currentProgram : number[] = machine.getTape().getNextEntry();
                if(currentProgram != null) {
                    for(let i : number = 0; i < currentProgram.length; i++) machine.memory[16384 + i] = currentProgram[i] & 255
                    ROMPatch.pop16(machine, z80);
                    return 515;
                }
            }
            return (z80.PC);
        }
    }
    ROMPatch["__class"] = "zx81emulator.zx81.ROMPatch";

}


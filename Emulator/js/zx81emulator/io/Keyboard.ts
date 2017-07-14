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

export class kb
{
    Shift: number;
    Code: number;
    Addr1: number;
    Data1: number;
    Addr2: number;
    Data2: number;

    constructor(s: number, c: number, a1: number, d1: number, a2: number, d2: number)
    {
        this.Shift = s;
        this.Code = c;
        this.Addr1 = a1;
        this.Data1 = d1;
        this.Addr2 = a2;
        this.Data2 = d2;
    }
}

const kbD0: number = 1;
const kbD1: number = 2;
const kbD2: number = 4;
const kbD3: number = 8;
const kbD4: number = 16;
const kbA8: number = 0;
const kbA9: number = 1;
const kbA10: number = 2;
const kbA11: number = 3;
const kbA12: number = 4;
const kbA13: number = 5;
const kbA14: number = 6;
const kbA15: number = 7;

const VKSHIFT: number = 16;

export class KBStatus
{
    private PCShift: number = 1;

    static KeyMap: kb[] = [
        new kb(0,  16, kbA8, kbD0, 255, 255),           // Shift
        new kb(0,  13, kbA14, kbD0, 255, 255),          // Return
        new kb(0,   8, kbA12, kbD0, kbA8, kbD0),        // Backspace
        new kb(0,  37, kbA11, kbD4, kbA8, kbD0),        // Arrow Left
        new kb(0,  38, kbA12, kbD3, kbA8, kbD0),        // Arrow Up
        new kb(0,  39, kbA12, kbD2, kbA8, kbD0),        // Arrow Right
        new kb(0,  40, kbA12, kbD4, kbA8, kbD0),        // Arrow Down
        new kb(0,  17, kbA14, kbD0, kbA8 , kbD0),       // Control
        new kb(0,  32, kbA15, kbD0, 255, 255),          // space
        new kb(0,  65, kbA9, kbD0, 255, 255),           // A
        new kb(0,  66, kbA15, kbD4, 255, 255),          // B
        new kb(0,  67, kbA8, kbD3, 255, 255),           // C
        new kb(0,  68, kbA9, kbD2, 255, 255),           // D
        new kb(0,  69, kbA10, kbD2, 255, 255),          // E
        new kb(0,  70, kbA9, kbD3, 255, 255),           // F
        new kb(0,  71, kbA9, kbD4, 255, 255),           // G
        new kb(0,  72, kbA14, kbD4, 255, 255),          // H
        new kb(0,  73, kbA13, kbD2, 255, 255),          // I
        new kb(0,  74, kbA14, kbD3, 255, 255),          // J
        new kb(0,  75, kbA14, kbD2, 255, 255),          // K
        new kb(0,  76, kbA14, kbD1, 255, 255),          // L
        new kb(0,  77, kbA15, kbD2, 255, 255),          // M
        new kb(0,  78, kbA15, kbD3, 255, 255),          // N
        new kb(0,  79, kbA13, kbD1, 255, 255),          // O
        new kb(0,  80, kbA13, kbD0, 255, 255),          // P
        new kb(0,  81, kbA10, kbD0, 255, 255),          // Q
        new kb(0,  82, kbA10, kbD3, 255, 255),          // R
        new kb(0,  83, kbA9, kbD1, 255, 255),           // S
        new kb(0,  84, kbA10, kbD4, 255, 255),          // T
        new kb(0,  85, kbA13, kbD3, 255, 255),          // U
        new kb(0,  86, kbA8, kbD4, 255, 255),           // V
        new kb(0,  87, kbA10, kbD1, 255, 255),          // W
        new kb(0,  88, kbA8, kbD2, 255, 255),           // X
        new kb(0,  89, kbA13, kbD4, 255, 255),          // Y
        new kb(0,  90, kbA8, kbD1, 255, 255),           // Z
        new kb(0,  49, kbA11, kbD0, 255, 255),          // 1
        new kb(0,  50, kbA11, kbD1, 255, 255),          // 2
        new kb(0,  51, kbA11, kbD2, 255, 255),          // 3
        new kb(0,  52, kbA11, kbD3, 255, 255),          // 4
        new kb(0,  53, kbA11, kbD4, 255, 255),          // 5
        new kb(0,  54, kbA12, kbD4, 255, 255),          // 6
        new kb(0,  55, kbA12, kbD3, 255, 255),          // 7
        new kb(0,  56, kbA12, kbD2, 255, 255),          // 8
        new kb(0,  57, kbA12, kbD1, 255, 255),          // 9
        new kb(0,  48, kbA12, kbD0, 255, 255),          // 0
        new kb(0, 186, kbA15, kbD1, 255, 255),          // , Shift
        new kb(0, 188, kbA15, kbD1, 255, 255),          // ,
        new kb(0, 190, kbA15, kbD1, 255, 255),          // .
        new kb(0, 0, 0, 0, 0, 0)
    ];

    public static ZXKeyboard: number[] = new Array(8);

    public constructor()
    {
        for (let i: number = 0; i < 8; i++)
            KBStatus.ZXKeyboard[i] = 0
    }

    public PCKeyDown(code: number, shift: boolean = false, ctrl: boolean = false, alt: boolean = false)
    {
        if(shift)
            this.PCShift = 2;

        let i: number = 0;
        while (KBStatus.KeyMap[i].Code !== 0)
        {
            if (KBStatus.KeyMap[i].Code === code &&  ((KBStatus.KeyMap[i].Shift == this.PCShift) || (KBStatus.KeyMap[i].Shift == 0)))
            {
                KBStatus.ZXKeyboard[KBStatus.KeyMap[i].Addr1] |= KBStatus.KeyMap[i].Data1;
                if (KBStatus.KeyMap[i].Addr2 !== 255)
                    KBStatus.ZXKeyboard[KBStatus.KeyMap[i].Addr2] |= KBStatus.KeyMap[i].Data2;
                break;
            }
            i++;
        }
    }

    public PCKeyUp(code: number, shift: boolean = false, ctrl: boolean = false, al: boolean = false)
    {
        if(code === VKSHIFT)
            this.PCShift = 1;

        let i: number = 0;
        while (KBStatus.KeyMap[i].Code !== 0)
        {
            if (KBStatus.KeyMap[i].Code === code)
            {
                KBStatus.ZXKeyboard[KBStatus.KeyMap[i].Addr1] &= ~KBStatus.KeyMap[i].Data1;
                if (KBStatus.KeyMap[i].Addr2 !== 255)
                    KBStatus.ZXKeyboard[KBStatus.KeyMap[i].Addr2] &= ~KBStatus.KeyMap[i].Data2;
                break;
            }
            i++;
        }

        if(this.PCShift === 2)
            KBStatus.ZXKeyboard[kbA8] |= kbD0;
    }
}


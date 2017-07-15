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

/* Keyboard is arranged by rows (address A) and column (data D).
It is splitted into two parts as the following:

      D0 D1 D2 D3 D4 | D4 D3 D2 D1 D0
      --------------------------------
A11 |  1  2  3  4  5 |  6  7  8  9  0 | A12
A10 |  Q  W  E  R  T |  Y  U  I  O  P | A13
A9  |  A  S  D  F  G |  H  J  K  L NL | A14
A8  | SH  Z  X  C  V |  B  N  M  . SP | A15

NL: New Line, SH: Shift, SP: Space
*/

export class Key
{
    code: number;
    addr1: number;
    data1: number;
    addr2: number;
    data2: number;

    constructor(c: number, a1: number, d1: number, a2: number, d2: number)
    {
        this.code = c;
        this.addr1 = a1;
        this.data1 = d1;
        this.addr2 = a2;
        this.data2 = d2;
    }
}

const D0: number = 1;
const D1: number = 2;
const D2: number = 4;
const D3: number = 8;
const D4: number = 16;
const A8: number = 0;
const A9: number = 1;
const A10: number = 2;
const A11: number = 3;
const A12: number = 4;
const A13: number = 5;
const A14: number = 6;
const A15: number = 7;

export const VK_J:number = 74;
export const VK_P = 80;
export const VK_ENTER: number = 13;
export const VK_SHIFT: number = 16;

export default class Keyboard
{
    static keyMap: Key[] = [
        new Key( 16, A8, D0, 255, 255),     // Shift
        new Key( 13, A14, D0, 255, 255),    // Return
        new Key(  8, A12, D0, A8, D0),      // Backspace
        new Key( 37, A11, D4, A8, D0),      // Arrow Left
        new Key( 38, A12, D3, A8, D0),      // Arrow Up
        new Key( 39, A12, D2, A8, D0),      // Arrow Right
        new Key( 40, A12, D4, A8, D0),      // Arrow Down
        new Key( 17, A14, D0, A8 , D0),     // Control
        new Key( 32, A15, D0, 255, 255),    // Space
        new Key( 65, A9, D0, 255, 255),     // A
        new Key( 66, A15, D4, 255, 255),    // B
        new Key( 67, A8, D3, 255, 255),     // C
        new Key( 68, A9, D2, 255, 255),     // D
        new Key( 69, A10, D2, 255, 255),    // E
        new Key( 70, A9, D3, 255, 255),     // F
        new Key( 71, A9, D4, 255, 255),     // G
        new Key( 72, A14, D4, 255, 255),    // H
        new Key( 73, A13, D2, 255, 255),    // I
        new Key( 74, A14, D3, 255, 255),    // J
        new Key( 75, A14, D2, 255, 255),    // K
        new Key( 76, A14, D1, 255, 255),    // L
        new Key( 77, A15, D2, 255, 255),    // M
        new Key( 78, A15, D3, 255, 255),    // N
        new Key( 79, A13, D1, 255, 255),    // O
        new Key( 80, A13, D0, 255, 255),    // P
        new Key( 81, A10, D0, 255, 255),    // Q
        new Key( 82, A10, D3, 255, 255),    // R
        new Key( 83, A9, D1, 255, 255),     // S
        new Key( 84, A10, D4, 255, 255),    // T
        new Key( 85, A13, D3, 255, 255),    // U
        new Key( 86, A8, D4, 255, 255),     // V
        new Key( 87, A10, D1, 255, 255),    // W
        new Key( 88, A8, D2, 255, 255),     // X
        new Key( 89, A13, D4, 255, 255),    // Y
        new Key( 90, A8, D1, 255, 255),     // Z
        new Key( 49, A11, D0, 255, 255),    // 1
        new Key( 50, A11, D1, 255, 255),    // 2
        new Key( 51, A11, D2, 255, 255),    // 3
        new Key( 52, A11, D3, 255, 255),    // 4
        new Key( 53, A11, D4, 255, 255),    // 5
        new Key( 54, A12, D4, 255, 255),    // 6
        new Key( 55, A12, D3, 255, 255),    // 7
        new Key( 56, A12, D2, 255, 255),    // 8
        new Key( 57, A12, D1, 255, 255),    // 9
        new Key( 48, A12, D0, 255, 255),    // 0
        new Key(186, A15, D1, 255, 255),    // , Shift
        new Key(188, A15, D1, 255, 255),    // ,
        new Key(190, A15, D1, 255, 255),    // .
        new Key(0, 0, 0, 0, 0)
    ];

    private map: number[] = new Array(8);

    public constructor()
    {
        for (let i: number = 0; i < 8; i++)
            this.map[i] = 0
    }

    public keyDown(code: number, shift: boolean)
    {
        let i: number = 0;
        while (Keyboard.keyMap[i].code !== 0)
        {
            if (Keyboard.keyMap[i].code === code)
            {
                this.map[Keyboard.keyMap[i].addr1] |= Keyboard.keyMap[i].data1;

                if (Keyboard.keyMap[i].addr2 !== 255)
                    this.map[Keyboard.keyMap[i].addr2] |= Keyboard.keyMap[i].data2;
                break;
            }
            i++;
        }
    }

    public keyUp(code: number, shift: boolean)
    {
        let i: number = 0;
        while (Keyboard.keyMap[i].code !== 0)
        {
            if (Keyboard.keyMap[i].code === code)
            {
                this.map[Keyboard.keyMap[i].addr1] &= ~Keyboard.keyMap[i].data1;
                if (Keyboard.keyMap[i].addr2 !== 255)
                    this.map[Keyboard.keyMap[i].addr2] &= ~Keyboard.keyMap[i].data2;
                break;
            }
            i++;
        }

        if(shift)
            this.map[A8] |= D0;
    }

    public get(i: number): number
    {
        return this.map[i];
    }
}


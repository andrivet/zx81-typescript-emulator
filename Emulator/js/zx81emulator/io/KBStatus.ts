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

namespace zx81emulator.io {
    import ZX81ConfigDefs = zx81emulator.config.ZX81ConfigDefs;

    export class kb {
        WinKey : string;

        Addr1 : number;

        Data1 : number;

        Addr2 : number;

        Data2 : number;

        constructor(w : string, a1 : number, d1 : number, a2 : number, d2 : number) {
            this.Addr1 = 0;
            this.Data1 = 0;
            this.Addr2 = 0;
            this.Data2 = 0;
            this.WinKey = w;
            this.Addr1 = a1;
            this.Data1 = d1;
            this.Addr2 = a2;
            this.Data2 = d2;
        }
    }
    kb["__class"] = "zx81emulator.io.kb";


    export class KBStatus implements ZX81ConfigDefs {
        private PCShift : number = 1;

        static kbD0 : number = 1;

        static kbD1 : number = 2;

        static kbD2 : number = 4;

        static kbD3 : number = 8;

        static kbD4 : number = 16;

        static kbA8 : number = 0;

        static kbA9 : number = 1;

        static kbA10 : number = 2;

        static kbA11 : number = 3;

        static kbA12 : number = 4;

        static kbA13 : number = 5;

        static kbA14 : number = 6;

        static kbA15 : number = 7;

        static KeyMap : kb[]; public static KeyMap_$LI$() : kb[] { if(KBStatus.KeyMap == null) KBStatus.KeyMap = [new kb("Shift", KBStatus.kbA8, KBStatus.kbD0, 255, 255), new kb("Return", KBStatus.kbA14, KBStatus.kbD0, 255, 255), new kb(" ", KBStatus.kbA15, KBStatus.kbD0, 255, 255), new kb("Spacebar", KBStatus.kbA15, KBStatus.kbD0, 255, 255), new kb("A", KBStatus.kbA9, KBStatus.kbD0, 255, 255), new kb("B", KBStatus.kbA15, KBStatus.kbD4, 255, 255), new kb("C", KBStatus.kbA8, KBStatus.kbD3, 255, 255), new kb("D", KBStatus.kbA9, KBStatus.kbD2, 255, 255), new kb("E", KBStatus.kbA10, KBStatus.kbD2, 255, 255), new kb("F", KBStatus.kbA9, KBStatus.kbD3, 255, 255), new kb("G", KBStatus.kbA9, KBStatus.kbD4, 255, 255), new kb("H", KBStatus.kbA14, KBStatus.kbD4, 255, 255), new kb("I", KBStatus.kbA13, KBStatus.kbD2, 255, 255), new kb("J", KBStatus.kbA14, KBStatus.kbD3, 255, 255), new kb("K", KBStatus.kbA14, KBStatus.kbD2, 255, 255), new kb("L", KBStatus.kbA14, KBStatus.kbD1, 255, 255), new kb("M", KBStatus.kbA15, KBStatus.kbD2, 255, 255), new kb("N", KBStatus.kbA15, KBStatus.kbD3, 255, 255), new kb("O", KBStatus.kbA13, KBStatus.kbD1, 255, 255), new kb("P", KBStatus.kbA13, KBStatus.kbD0, 255, 255), new kb("Q", KBStatus.kbA10, KBStatus.kbD0, 255, 255), new kb("R", KBStatus.kbA10, KBStatus.kbD3, 255, 255), new kb("S", KBStatus.kbA9, KBStatus.kbD1, 255, 255), new kb("T", KBStatus.kbA10, KBStatus.kbD4, 255, 255), new kb("U", KBStatus.kbA13, KBStatus.kbD3, 255, 255), new kb("V", KBStatus.kbA8, KBStatus.kbD4, 255, 255), new kb("W", KBStatus.kbA10, KBStatus.kbD1, 255, 255), new kb("X", KBStatus.kbA8, KBStatus.kbD2, 255, 255), new kb("Y", KBStatus.kbA13, KBStatus.kbD4, 255, 255), new kb("Z", KBStatus.kbA8, KBStatus.kbD1, 255, 255), new kb("1", KBStatus.kbA11, KBStatus.kbD0, 255, 255), new kb("2", KBStatus.kbA11, KBStatus.kbD1, 255, 255), new kb("3", KBStatus.kbA11, KBStatus.kbD2, 255, 255), new kb("4", KBStatus.kbA11, KBStatus.kbD3, 255, 255), new kb("5", KBStatus.kbA11, KBStatus.kbD4, 255, 255), new kb("6", KBStatus.kbA12, KBStatus.kbD4, 255, 255), new kb("7", KBStatus.kbA12, KBStatus.kbD3, 255, 255), new kb("8", KBStatus.kbA12, KBStatus.kbD2, 255, 255), new kb("9", KBStatus.kbA12, KBStatus.kbD1, 255, 255), new kb("0", KBStatus.kbA12, KBStatus.kbD0, 255, 255), new kb("*", KBStatus.kbA15, KBStatus.kbD4, KBStatus.kbA8, KBStatus.kbD0), new kb("/", KBStatus.kbA8, KBStatus.kbD4, KBStatus.kbA8, KBStatus.kbD0), new kb(";", KBStatus.kbA8, KBStatus.kbD2, KBStatus.kbA8, KBStatus.kbD0), new kb(":", KBStatus.kbA8, KBStatus.kbD1, KBStatus.kbA8, KBStatus.kbD0), new kb("-", KBStatus.kbA14, KBStatus.kbD3, KBStatus.kbA8, KBStatus.kbD0), new kb("=", KBStatus.kbA14, KBStatus.kbD1, KBStatus.kbA8, KBStatus.kbD0), new kb("+", KBStatus.kbA14, KBStatus.kbD2, KBStatus.kbA8, KBStatus.kbD0), new kb(",", KBStatus.kbA15, KBStatus.kbD1, KBStatus.kbA8, KBStatus.kbD0), new kb("<", KBStatus.kbA15, KBStatus.kbD3, KBStatus.kbA8, KBStatus.kbD0), new kb(".", KBStatus.kbA15, KBStatus.kbD1, 255, 255), new kb(">", KBStatus.kbA15, KBStatus.kbD2, KBStatus.kbA8, KBStatus.kbD0), new kb("/", KBStatus.kbA8, KBStatus.kbD4, KBStatus.kbA8, KBStatus.kbD0), new kb("?", KBStatus.kbA8, KBStatus.kbD3, KBStatus.kbA8, KBStatus.kbD0), new kb("(", KBStatus.kbA13, KBStatus.kbD2, KBStatus.kbA8, KBStatus.kbD0), new kb(")", KBStatus.kbA13, KBStatus.kbD1, KBStatus.kbA8, KBStatus.kbD0), new kb("\u0000", 0, 0, 0, 0)]; return KBStatus.KeyMap; };

        public static ZXKeyboard : number[]; public static ZXKeyboard_$LI$() : number[] { if(KBStatus.ZXKeyboard == null) KBStatus.ZXKeyboard = new Array(8); return KBStatus.ZXKeyboard; };

        public constructor() {
            for(let i : number = 0; i < 8; i++) KBStatus.ZXKeyboard_$LI$()[i] = 0
        }

        public PCKeyDown(key : string, shift : boolean = false, ctrl : boolean = false, alt : boolean = false) {
            let i : number = 0;
            while((KBStatus.KeyMap_$LI$()[i].WinKey !== "\u0000")){
                if(KBStatus.KeyMap_$LI$()[i].WinKey === key) {
                    KBStatus.ZXKeyboard_$LI$()[KBStatus.KeyMap_$LI$()[i].Addr1] |= KBStatus.KeyMap_$LI$()[i].Data1;
                    if(KBStatus.KeyMap_$LI$()[i].Addr2 !== 255) KBStatus.ZXKeyboard_$LI$()[KBStatus.KeyMap_$LI$()[i].Addr2] |= KBStatus.KeyMap_$LI$()[i].Data2;
                    return;
                }
                i++;
            };
        }

        public PCKeyUp(key : string, shift : boolean = false, ctrl : boolean = false, al : boolean = false) {
            let i : number = 0;
            while((KBStatus.KeyMap_$LI$()[i].WinKey !== "\u0000")){
                if(KBStatus.KeyMap_$LI$()[i].WinKey === key) {
                    KBStatus.ZXKeyboard_$LI$()[KBStatus.KeyMap_$LI$()[i].Addr1] &= ~KBStatus.KeyMap_$LI$()[i].Data1;
                    if(KBStatus.KeyMap_$LI$()[i].Addr2 !== 255) KBStatus.ZXKeyboard_$LI$()[KBStatus.KeyMap_$LI$()[i].Addr2] &= ~KBStatus.KeyMap_$LI$()[i].Data2;
                }
                i++;
            };
        }
    }
    KBStatus["__class"] = "zx81emulator.io.KBStatus";
    KBStatus["__interfaces"] = ["zx81emulator.config.ZX81ConfigDefs"];


}


zx81emulator.io.KBStatus.ZXKeyboard_$LI$();

zx81emulator.io.KBStatus.KeyMap_$LI$();

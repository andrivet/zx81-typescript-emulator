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
package zx81emulator.io;

import zx81emulator.config.ZX81ConfigDefs;


class kb {
    String WinKey;
    int Addr1, Data1, Addr2, Data2;

    kb(String w, int a1, int d1, int a2, int d2) {
        WinKey = w;
        Addr1 = a1;
        Data1 = d1;
        Addr2 = a2;
        Data2 = d2;
    }
}

public class KBStatus
        implements ZX81ConfigDefs {
    private int PCShift = 1;

    private static final int kbD0 = 1;
    private static final int kbD1 = 2;
    private static final int kbD2 = 4;
    private static final int kbD3 = 8;
    private static final int kbD4 = 16;

    private static final int kbA8 = 0;
    private static final int kbA9 = 1;
    private static final int kbA10 = 2;
    private static final int kbA11 = 3;
    private static final int kbA12 = 4;
    private static final int kbA13 = 5;
    private static final int kbA14 = 6;
    private static final int kbA15 = 7;

    private static kb[] KeyMap = new kb[] {

        new kb("Shift", kbA8, kbD0, 255, 255),
        new kb("Return", kbA14, kbD0, 255, 255),
        new kb(" ", kbA15, kbD0, 255, 255),
        new kb("Spacebar", kbA15, kbD0, 255, 255),

        new kb("A", kbA9, kbD0, 255, 255),
        new kb("B", kbA15, kbD4, 255, 255),
        new kb("C", kbA8, kbD3, 255, 255),
        new kb("D", kbA9, kbD2, 255, 255),
        new kb("E", kbA10, kbD2, 255, 255),
        new kb("F", kbA9, kbD3, 255, 255),
        new kb("G", kbA9, kbD4, 255, 255),
        new kb("H", kbA14, kbD4, 255, 255),
        new kb("I", kbA13, kbD2, 255, 255),
        new kb("J", kbA14, kbD3, 255, 255),
        new kb("K", kbA14, kbD2, 255, 255),
        new kb("L", kbA14, kbD1, 255, 255),
        new kb("M", kbA15, kbD2, 255, 255),
        new kb("N", kbA15, kbD3, 255, 255),
        new kb("O", kbA13, kbD1, 255, 255),
        new kb("P", kbA13, kbD0, 255, 255),
        new kb("Q", kbA10, kbD0, 255, 255),
        new kb("R", kbA10, kbD3, 255, 255),
        new kb("S", kbA9, kbD1, 255, 255),
        new kb("T", kbA10, kbD4, 255, 255),
        new kb("U", kbA13, kbD3, 255, 255),
        new kb("V", kbA8, kbD4, 255, 255),
        new kb("W", kbA10, kbD1, 255, 255),
        new kb("X", kbA8, kbD2, 255, 255),
        new kb("Y", kbA13, kbD4, 255, 255),
        new kb("Z", kbA8, kbD1, 255, 255),

        new kb("1", kbA11, kbD0, 255, 255),
        new kb("2", kbA11, kbD1, 255, 255),
        new kb("3", kbA11, kbD2, 255, 255),
        new kb("4", kbA11, kbD3, 255, 255),
        new kb("5", kbA11, kbD4, 255, 255),
        new kb("6", kbA12, kbD4, 255, 255),
        new kb("7", kbA12, kbD3, 255, 255),
        new kb("8", kbA12, kbD2, 255, 255),
        new kb("9", kbA12, kbD1, 255, 255),
        new kb("0", kbA12, kbD0, 255, 255),

        new kb("*", kbA15, kbD4, kbA8, kbD0),
        new kb("/", kbA8, kbD4, kbA8, kbD0),
        new kb(";", kbA8, kbD2, kbA8, kbD0),
        new kb(":", kbA8, kbD1, kbA8, kbD0),
        new kb("-", kbA14, kbD3, kbA8, kbD0),
        new kb("=", kbA14, kbD1, kbA8, kbD0),
        new kb("+", kbA14, kbD2, kbA8, kbD0),
        new kb(",", kbA15, kbD1, kbA8, kbD0),
        new kb("<", kbA15, kbD3, kbA8, kbD0),
        new kb(".", kbA15, kbD1, 255, 255),
        new kb(">", kbA15, kbD2, kbA8, kbD0),
        new kb("/", kbA8, kbD4, kbA8, kbD0),
        new kb("?", kbA8, kbD3, kbA8, kbD0),
        new kb("(", kbA13, kbD2, kbA8, kbD0),
        new kb(")", kbA13, kbD1, kbA8, kbD0),

        new kb("\0", 0, 0, 0, 0)
    };

    // For convenience in the code below.
    public static int[] ZXKeyboard = new int[8];

    public KBStatus() {
        for (int i = 0; i < 8; i++)
            ZXKeyboard[i] = 0;
    }

    public void PCKeyDown(String key) {
        PCKeyDown(key, false, false, false);
    }

    public void PCKeyDown(String key, boolean shift, boolean ctrl, boolean alt) {
        int i = 0;
        while (KeyMap[i].WinKey != "\0") {
            if (KeyMap[i].WinKey == key) {
                ZXKeyboard[KeyMap[i].Addr1] |= KeyMap[i].Data1;
                if (KeyMap[i].Addr2 != 255)
                    ZXKeyboard[KeyMap[i].Addr2] |= KeyMap[i].Data2;
                return;
            }
            i++;
        }
    }

    public void PCKeyUp(String key) {
        PCKeyUp(key, false, false, false);
    }

    public void PCKeyUp(String key, boolean shift, boolean ctrl, boolean al) {
        int i = 0;
        while (KeyMap[i].WinKey != "\0") {
            if (KeyMap[i].WinKey == key) {
                ZXKeyboard[KeyMap[i].Addr1] &= ~KeyMap[i].Data1;
                if (KeyMap[i].Addr2 != 255)
                    ZXKeyboard[KeyMap[i].Addr2] &= ~KeyMap[i].Data2;
            }
            i++;
        }
    }
}

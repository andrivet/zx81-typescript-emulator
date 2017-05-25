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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package zx81emulator.io;

import zx81emulator.config.ZX81Config;
import zx81emulator.config.ZX81ConfigDefs;

import java.awt.event.KeyEvent;

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

    // For convenience in the following tables.
    private static final int VK_SHIFT = KeyEvent.VK_SHIFT;
    private static final int VK_RETURN = KeyEvent.VK_ENTER;
    private static final int VK_SPACE = KeyEvent.VK_SPACE;
    private static final int VK_NUMPAD0 = KeyEvent.VK_NUMPAD0;
    private static final int VK_NUMPAD1 = KeyEvent.VK_NUMPAD1;
    private static final int VK_NUMPAD2 = KeyEvent.VK_NUMPAD2;
    private static final int VK_NUMPAD3 = KeyEvent.VK_NUMPAD3;
    private static final int VK_NUMPAD4 = KeyEvent.VK_NUMPAD4;
    private static final int VK_NUMPAD5 = KeyEvent.VK_NUMPAD5;
    private static final int VK_NUMPAD6 = KeyEvent.VK_NUMPAD6;
    private static final int VK_NUMPAD7 = KeyEvent.VK_NUMPAD7;
    private static final int VK_NUMPAD8 = KeyEvent.VK_NUMPAD8;
    private static final int VK_NUMPAD9 = KeyEvent.VK_NUMPAD9;
    private static final int VK_MULTIPLY = KeyEvent.VK_MULTIPLY;
    private static final int VK_DIVIDE = KeyEvent.VK_DIVIDE;
    private static final int VK_SUBTRACT = KeyEvent.VK_SUBTRACT;
    private static final int VK_ADD = KeyEvent.VK_ADD;
    private static final int VK_DECIMAL = KeyEvent.VK_DECIMAL;
    private static final int VK_BACK = KeyEvent.VK_BACK_SPACE;
    private static final int VK_DOWN = KeyEvent.VK_DOWN;
    private static final int VK_UP = KeyEvent.VK_UP;
    private static final int VK_LEFT = KeyEvent.VK_LEFT;
    private static final int VK_RIGHT = KeyEvent.VK_RIGHT;
    private static final int VK_CONTROL = KeyEvent.VK_CONTROL;
    private static final int VK_QUOTE = KeyEvent.VK_QUOTE;

    private static kb[] KeyMap;

    private static int[][] KBZX81_ints =
            {
                    {0, VK_SHIFT, kbA8, kbD0, 255, 255},
                    {0, VK_RETURN, kbA14, kbD0, 255, 255},
                    {0, VK_SPACE, kbA15, kbD0, 255, 255},

                    {0, 'A', kbA9, kbD0, 255, 255},
                    {0, 'B', kbA15, kbD4, 255, 255},
                    {0, 'C', kbA8, kbD3, 255, 255},
                    {0, 'D', kbA9, kbD2, 255, 255},
                    {0, 'E', kbA10, kbD2, 255, 255},
                    {0, 'F', kbA9, kbD3, 255, 255},
                    {0, 'G', kbA9, kbD4, 255, 255},
                    {0, 'H', kbA14, kbD4, 255, 255},
                    {0, 'I', kbA13, kbD2, 255, 255},
                    {0, 'J', kbA14, kbD3, 255, 255},
                    {0, 'K', kbA14, kbD2, 255, 255},
                    {0, 'L', kbA14, kbD1, 255, 255},
                    {0, 'M', kbA15, kbD2, 255, 255},
                    {0, 'N', kbA15, kbD3, 255, 255},
                    {0, 'O', kbA13, kbD1, 255, 255},
                    {0, 'P', kbA13, kbD0, 255, 255},
                    {0, 'Q', kbA10, kbD0, 255, 255},
                    {0, 'R', kbA10, kbD3, 255, 255},
                    {0, 'S', kbA9, kbD1, 255, 255},
                    {0, 'T', kbA10, kbD4, 255, 255},
                    {0, 'U', kbA13, kbD3, 255, 255},
                    {0, 'V', kbA8, kbD4, 255, 255},
                    {0, 'W', kbA10, kbD1, 255, 255},
                    {0, 'X', kbA8, kbD2, 255, 255},
                    {0, 'Y', kbA13, kbD4, 255, 255},
                    {0, 'Z', kbA8, kbD1, 255, 255},

                    {0, '1', kbA11, kbD0, 255, 255},
                    {0, '2', kbA11, kbD1, 255, 255},
                    {0, '3', kbA11, kbD2, 255, 255},
                    {0, '4', kbA11, kbD3, 255, 255},
                    {0, '5', kbA11, kbD4, 255, 255},
                    {0, '6', kbA12, kbD4, 255, 255},
                    {0, '7', kbA12, kbD3, 255, 255},
                    {0, '8', kbA12, kbD2, 255, 255},
                    {0, '9', kbA12, kbD1, 255, 255},
                    {0, '0', kbA12, kbD0, 255, 255},

                    {0, VK_NUMPAD1, kbA11, kbD0, 255, 255},
                    {0, VK_NUMPAD2, kbA11, kbD1, 255, 255},
                    {0, VK_NUMPAD3, kbA11, kbD2, 255, 255},
                    {0, VK_NUMPAD4, kbA11, kbD3, 255, 255},
                    {0, VK_NUMPAD5, kbA11, kbD4, 255, 255},
                    {0, VK_NUMPAD6, kbA12, kbD4, 255, 255},
                    {0, VK_NUMPAD7, kbA12, kbD3, 255, 255},
                    {0, VK_NUMPAD8, kbA12, kbD2, 255, 255},
                    {0, VK_NUMPAD9, kbA12, kbD1, 255, 255},
                    {0, VK_NUMPAD0, kbA12, kbD0, 255, 255},

                    {0, VK_MULTIPLY, kbA15, kbD4, kbA8, kbD0},
                    {0, VK_DIVIDE, kbA8, kbD4, kbA8, kbD0},

                    {1, ';', kbA8, kbD2, kbA8, kbD0},           // ;
                    {2, ';', kbA8, kbD1, kbA8, kbD0},           // : (Shift ;)
                    {1, '-', kbA14, kbD3, kbA8, kbD0},          // -
                    {0, VK_SUBTRACT, kbA14, kbD3, kbA8, kbD0},

                    {1, '=', kbA14, kbD1, kbA8, kbD0},          // =
                    {2, '=', kbA14, kbD2, kbA8, kbD0},          // + (Shift =)
                    {0, VK_ADD, kbA14, kbD2, kbA8, kbD0},       // + (numpad+)

                    {1, ',', kbA15, kbD1, kbA8, kbD0},          // ,
                    {2, ',', kbA15, kbD3, kbA8, kbD0},          // < (Shift ,)

                    {1, '.', kbA15, kbD1, 255, 255},            // .
                    {0, VK_DECIMAL, kbA15, kbD1, 255, 255},     // . (numpad.)
                    {2, '.', kbA15, kbD2, kbA8, kbD0},         // > (Shift .)
                    {1, '/', kbA8, kbD4, kbA8, kbD0},          // /
                    {2, '/', kbA8, kbD3, kbA8, kbD0},          // ? (Shift /)
                    {0, '[', kbA13, kbD2, kbA8, kbD0},         // ( ([ or { these are not in ZX81 character set)
                    {0, ']', kbA13, kbD1, kbA8, kbD0},         // ) (] or } these are not in ZX81 character set)
                    {1, 520, kbA13, kbD0, kbA8, kbD0},         // " (# or ~ these are not in ZX81 character set)
                    {0, VK_QUOTE, kbA13, kbD0, kbA8, kbD0},    // " (' or @ these are not in ZX81 character set)

                    {0, VK_BACK, kbA12, kbD0, kbA8, kbD0},
                    {0, VK_LEFT, kbA11, kbD4, kbA8, kbD0},
                    {0, VK_DOWN, kbA12, kbD4, kbA8, kbD0},
                    {0, VK_UP, kbA12, kbD3, kbA8, kbD0},
                    {0, VK_RIGHT, kbA12, kbD2, kbA8, kbD0},

                    {0, VK_CONTROL, kbA14, kbD0, kbA8, kbD0},

                    {0, 0, 0, 0, 0, 0}
            };


    // For convenience in the code below.
    public static int[] ZXKeyboard = new int[8];

    private static kb[] KBZX81 = intTokb(KBZX81_ints);

    private static kb[] intTokb(int[][] ints) {
        kb[] kb = new kb[ints.length];
        for (int i = 0; i < ints.length; i++)
            kb[i] = new kb(ints[i][0], ints[i][1], ints[i][2], ints[i][3], ints[i][4], ints[i][5]);
        return kb;
    }

    public KBStatus(ZX81Config config) {

        for (int i = 0; i < 8; i++) ZXKeyboard[i] = 0;

        switch (config.zx81opts.machine) {
            default:
                KeyMap = KBZX81;
                break;
        }
    }

    public void PCKeyDown(int key) {
        int i = 0;
        if (key == VK_SHIFT) PCShift = 2;

        while (KeyMap[i].WinKey != 0) {
            if ((KeyMap[i].WinKey == key) &&
                    ((KeyMap[i].Shift == PCShift) || (KeyMap[i].Shift == 0))) {
                ZXKeyboard[KeyMap[i].Addr1] |= KeyMap[i].Data1;
                if (KeyMap[i].Addr2 != 255)
                    ZXKeyboard[KeyMap[i].Addr2] |= KeyMap[i].Data2;
                return;
            }
            i++;
        }
    }

    public void PCKeyUp(int key) {
        int i = 0;

        if (key == VK_SHIFT) PCShift = 1;

        while (KeyMap[i].WinKey != 0) {
            if (KeyMap[i].WinKey == key) {
                ZXKeyboard[KeyMap[i].Addr1] &= ~KeyMap[i].Data1;
                if (KeyMap[i].Addr2 != 255)
                    ZXKeyboard[KeyMap[i].Addr2] &= ~KeyMap[i].Data2;

            }
            i++;
        }
        if (PCShift == 2) ZXKeyboard[kbA8] |= kbD0;
    }
}

class kb {
    int Shift;
    int WinKey;
    int Addr1, Data1, Addr2, Data2;

    kb(int s, int w, int a1, int d1, int a2, int d2) {
        Shift = s;
        WinKey = w;
        Addr1 = a1;
        Data1 = d1;
        Addr2 = a2;
        Data2 = d2;
    }
}
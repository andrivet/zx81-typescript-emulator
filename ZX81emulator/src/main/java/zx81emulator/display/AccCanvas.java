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
package zx81emulator.display;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;

public class AccCanvas
        extends Canvas {
    private AccDraw mDisplayDrawer;

    AccCanvas(AccDraw dd) {
        mDisplayDrawer = dd;
    }

    void setRequiredSize(Dimension size) {
        setSize(size);
    }

    public void paint(Graphics g) {
        mDisplayDrawer.RedrawDisplay(g);
    }
}
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

import jsweet.dom.HTMLCanvasElement;
import jsweet.dom.ImageData;
import zx81emulator.config.Machine;
import zx81emulator.config.ZX81Config;
import zx81emulator.config.ZX81ConfigDefs;

import jsweet.dom.CanvasRenderingContext2D;

import static jsweet.util.Globals.any;
import static jsweet.util.Globals.function;
import static jsweet.dom.Globals.window;
import static jsweet.util.StringTypes._2d;


public class AccDraw
        implements Runnable, ZX81ConfigDefs {
    private Machine machine;

    private static final int HTOL = 405;
    private static final int VTOLMIN = 290;
    private static final int VTOLMAX = 340;
    private static final int HMIN = 10;
    private static final int VMIN = 350;

    private static int HSYNC_TOLLERANCE = HTOL;
    private static int VSYNC_TOLLERANCEMIN = VTOLMIN;
    private static int VSYNC_TOLLERANCEMAX = VTOLMAX;
    private static int HSYNC_MINLEN = HMIN;
    private static int VSYNC_MINLEN = VMIN;

    private static final int NoWinT = 32;
    private static final int NoWinB = (NoWinT + 240);
    private static final int NoWinL = 42;
    private static final int NoWinR = (NoWinL + 320);

    private int WinR = NoWinR;
    private int WinL = NoWinL;
    private int WinT = NoWinT;
    private int WinB = NoWinB;

    private int RasterX = 0, RasterY = 0;
    private static int TVW = 520;
    private static int TVH = 380;
    private int ScanLen;
    private int Scale = 1;

    private CanvasRenderingContext2D context;
    private ImageData imageData;

    private int[] Palette = new int[256], Colours = new int[256];

    private static int targetFrameTime = 1000 / 50; // Target frame time should result in 50Hz display.
    private boolean mKeepGoing = true;
    private boolean mPaused = false;

    private int dest = 0;
    private long lastDisplayUpdate = 0;

    public AccDraw(ZX81Config config, int scale, HTMLCanvasElement canvas) {
        machine = config.machine;

        canvas.width = (NoWinR - NoWinL);
        canvas.height = (NoWinB - NoWinT);

        /*if (mCanvas.getWidth() == 0 || mCanvas.getHeight() == 0)
            mCanvas.setRequiredSize(new Dimension((NoWinR - NoWinL) * canvasScale, (NoWinB - NoWinT) * canvasScale));*/

        ScanLen = 2 + machine.tperscanline * 2;

        context = canvas.getContext(_2d);
        imageData = context.getImageData(0, 0, canvas.width, canvas.height);

        InitializePalette();
    }

    private void AccurateUpdateDisplay() {
        long currentTime = System.currentTimeMillis();
        // Aim for 50Hz display
        if (currentTime - lastDisplayUpdate >= 1000 / 50) {
            context.putImageData(imageData, 0, 0);
            lastDisplayUpdate = currentTime;
        }
    }

    void RedrawDisplay() {
        context.putImageData(imageData, 0, 0);
    }

    private int FrameNo = 0;
    private int Shade = 0;

    private void setPixel(int i, int color) {
        i *= 4;
        int[] data = any(imageData.data);
        data[i + 0] = color;
        data[i + 1] = color;
        data[i + 2] = color;
        data[i + 3] = 0;
    }

    private void AccurateDraw(Scanline Line) {
        int bufferPos = dest + FrameNo * TVW;
        for (int i = 0; i < Line.scanline_len; i++) {
            int c = Line.scanline[i];

            setPixel(bufferPos + RasterX, Colours[c + Shade]);

            RasterX += 1;
            if (RasterX > ScanLen) {
                RasterX = 0;
                dest += TVW * Scale;
                bufferPos = dest + FrameNo * TVW;
                RasterY += Scale;
                Shade = 8 - Shade;

                if (RasterY >= TVH) {
                    i = Line.scanline_len + 1;
                    Line.sync_valid = 1;
                }
            }
        }

        if (Line.sync_len < HSYNC_MINLEN)
            Line.sync_valid = 0;
        if (Line.sync_valid != 0) {
            if (RasterX > HSYNC_TOLLERANCE) {
                RasterX = 0;
                RasterY += Scale;
                Shade = 8 - Shade;
                dest += TVW * Scale;
            }

            if (RasterY >= TVH || RasterY >= VSYNC_TOLLERANCEMAX
                    || (Line.sync_len > VSYNC_MINLEN
                    && RasterY > VSYNC_TOLLERANCEMIN)) {
                CompleteFrame();
                RasterX = RasterY = 0;
                dest = 0;

                FrameNo = 0;
                Shade = 0;

                AccurateUpdateDisplay();
            }
        }
    }

    private boolean dumpedscanlines = false;

    private void CompleteFrame() {
        if (!dumpedscanlines) {
            dumpedscanlines = true;
        }

        int x = RasterX, y = RasterY;
        int dest = y * TVW;

        while (y <= WinB) {
            while (x <= WinR) {
                setPixel(dest + x, 0);
                x += 1;
            }
            x = 0;
            y++;
            dest += TVW;
        }
    }

    private void RecalcPalette() {
        int rsz, gsz, bsz;  //bitsize of field
        int rsh, gsh, bsh;  //0's on left (the shift value)
        int i, r, g, b;

        rsz = 8;
        gsz = 8;
        bsz = 8;
        rsh = 16;
        gsh = 8;
        bsh = 0;

        for (i = 0; i < 256; i++) {
            r = Palette[i] & 0xff;
            g = (Palette[i] >> 8) & 0xff;
            b = (Palette[i] >> 16) & 0xff;

            r >>= (8 - rsz);  //keep only the MSB bits of component
            g >>= (8 - gsz);
            b >>= (8 - bsz);
            r <<= rsh;  //SHIFT THEM INTO PLACE
            g <<= gsh;
            b <<= bsh;

            Colours[i] =  r | g | b;
        }
    }

    private int DoPal(int r, int g, int b) {
        return ((((b > 255 ? 255 : (b < 0 ? 0 : b)) & 0xff) << 16)
                | (((g > 255 ? 255 : (g < 0 ? 0 : g)) & 0xff) << 8)
                | ((r > 255 ? 255 : (r < 0 ? 0 : r)) & 0xff));
    }

    private void InitializePalette() {
        int NoiseLevel, GhostLevel, GhostLevel2;
        int BrightnessLevel, ContrastLevel, ColourLevel;
        int HiBrightLevel;
        int r, g, b, colour, i, f;
        int basecolour, difference;
        int colr, colg, colb, bwr, bwg, bwb;

        VSYNC_TOLLERANCEMIN = 283;
        VSYNC_TOLLERANCEMAX = VSYNC_TOLLERANCEMIN + 40;

        NoiseLevel = -20;
        GhostLevel = -40;
        BrightnessLevel = 255 - 188;
        GhostLevel2 = GhostLevel / 3;
        ContrastLevel = 255 - 125;
        ColourLevel = 255;

        BrightnessLevel -= ContrastLevel;
        HiBrightLevel = BrightnessLevel + (ContrastLevel / 2) + 2 * ContrastLevel;
        ContrastLevel = BrightnessLevel + ContrastLevel + ContrastLevel;

        for (i = 0; i < 16; i++) {
            colour = i;

            difference = (1000 * (((colour > 7) ? HiBrightLevel : ContrastLevel) - BrightnessLevel)) / 16;
            basecolour = (difference * ((colour & 7) + 9)) / 1000;
            if (colour == 0 || colour == 8) basecolour = BrightnessLevel;

            colb = BrightnessLevel + ((colour & 1) != 0 ? basecolour : 0);
            colg = BrightnessLevel + ((colour & 4) != 0 ? basecolour : 0);
            colr = BrightnessLevel + ((colour & 2) != 0 ? basecolour : 0);

            bwb = BrightnessLevel + basecolour;
            bwg = BrightnessLevel + basecolour;
            bwr = BrightnessLevel + basecolour;

            r = (((colr - bwr) * ColourLevel) / 255) + bwr;
            g = (((colg - bwg) * ColourLevel) / 255) + bwg;
            b = (((colb - bwb) * ColourLevel) / 255) + bwb;

            Palette[i * 16] = DoPal(r, g, b);
            Palette[4 + i * 16] = DoPal(r + GhostLevel2, g + GhostLevel2, b + GhostLevel2);

            b += NoiseLevel;
            g += NoiseLevel;
            r += NoiseLevel;
            Palette[i * 16 + 1] = DoPal(r, g, b);
            Palette[4 + i * 16 + 1] = DoPal(r + GhostLevel2, g + GhostLevel2, b + GhostLevel2);

            b += GhostLevel;
            g += GhostLevel;
            r += GhostLevel;
            Palette[i * 16 + 3] = DoPal(r, g, b);
            Palette[4 + i * 16 + 3] = DoPal(r, g, b);

            b -= NoiseLevel;
            g -= NoiseLevel;
            r -= NoiseLevel;
            Palette[i * 16 + 2] = DoPal(r, g, b);
            Palette[4 + i * 16 + 2] = DoPal(r + GhostLevel2, g + GhostLevel2, b + GhostLevel2);
        }

        for (i = 0; i < 16; i++) {
            for (f = 0; f < 8; f++) {
                colour = Palette[i * 16 + f];

                b = ((colour & 0x00ff0000) >> 16);
                g = ((colour & 0x0000ff00) >> 8);
                r = (colour & 0xff);

                Palette[i * 16 + f + 8] = DoPal(r, g, b);
            }
        }

        RecalcPalette();
    }

    private Scanline BuildLine;
    private long framesStartTime = 0;
    private int fps;
    private int borrow;

    private void AnimTimer1Timer() {

        if (machine.stop()) {
            AccurateUpdateDisplay();
            return;
        }

        fps++;

        int j = machine.tperframe + borrow;

        while (j > 0 && !machine.stop()) {
            j -= machine.do_scanline(BuildLine);

            AccurateDraw(BuildLine);

        }

        if (!machine.stop()) borrow = j;
    }

    public void run() {
        Scanline[] Video = new Scanline[]{new Scanline(), new Scanline()};
        BuildLine = Video[0];

        if(!mKeepGoing)
            return;

        if(mPaused) {
            window.setTimeout(function(this::run), 1000);
            return;
        }

        if(framesStartTime == 0) {
            fps = 0;
            framesStartTime = System.currentTimeMillis();
        }

        AnimTimer1Timer();

        // Pace the emulator to run at 100%
        long currentTime = System.currentTimeMillis();
        long delay = (targetFrameTime * fps) - (currentTime - framesStartTime);
        if (delay > 0) {
            window.setTimeout(function(this::run), delay);
            return;
        }

        if (fps == 100) {
            framesStartTime = System.currentTimeMillis();
            fps = 0;
        }
    }

    public void start() {
        mKeepGoing = true;
        window.setTimeout(function(this::run), 0);
    }

    public void stop() {
        mKeepGoing = false;
    }

    public void setPaused(boolean paused) {
        mPaused = paused;
    }
}

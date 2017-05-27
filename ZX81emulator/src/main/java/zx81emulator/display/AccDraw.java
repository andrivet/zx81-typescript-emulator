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

import zx81emulator.config.Machine;
import zx81emulator.config.ZX81Config;
import zx81emulator.config.ZX81ConfigDefs;
import zx81emulator.zx81.ZX81;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class AccDraw
        implements Runnable, ZX81ConfigDefs {
    private Machine machine;
    private boolean fullSpeed = true;

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
    private int TVH;
    private int TVP;
    private int ScanLen;
    private int Scale;

    private int[] Palette = new int[256], Colours = new int[256];

    private AccCanvas mCanvas;
    private BufferedImage mScreenImage;
    private int[] mScreenImageBufferData = null;
    private boolean mKeepGoing = true;
    private boolean mPaused = false;

    private int dest = 0;

    private void AccurateInit(int canvasScale) {
        if (mCanvas.getWidth() == 0 || mCanvas.getHeight() == 0)
            mCanvas.setRequiredSize(new Dimension((NoWinR - NoWinL) * canvasScale, (NoWinB - NoWinT) * canvasScale));

        RasterX = 0;
        RasterY = 0;

        Scale = 1;

        ScanLen = 2 + machine.tperscanline * 2;

        WinL = NoWinL;
        WinR = NoWinR;
        WinT = NoWinT;
        WinB = NoWinB;

        int TVW;
        TVW = 520;
        TVH = 380;
        HSYNC_TOLLERANCE = HTOL;
        HSYNC_MINLEN = 10;

        mScreenImage = new BufferedImage(TVW, TVH, BufferedImage.TYPE_INT_RGB);
        mScreenImageBufferData = ((DataBufferInt) mScreenImage.getRaster().getDataBuffer()).getData();

        dest = 0;
        TVP = TVW;

        RecalcPalette();
    }

    private long lastDisplayUpdate = 0;

    private void AccurateUpdateDisplay() {
        long currentTime = System.currentTimeMillis();
        // Aim for 50Hz display
        if (currentTime - lastDisplayUpdate >= 1000 / 50) {
            int width = mCanvas.getWidth();
            int height = mCanvas.getHeight();
            mCanvas.getGraphics().drawImage(mScreenImage, 0, 0, width, height, WinL, WinT, WinR, WinB, null);

            lastDisplayUpdate = currentTime;
        }
    }

    void RedrawDisplay(Graphics g) {
        int width = mCanvas.getWidth();
        int height = mCanvas.getHeight();
        g.drawImage(mScreenImage, 0, 0, width, height, WinL, WinT, WinR, WinB, null);
    }

    private int FrameNo = 0;
    private int Shade = 0;

    private void AccurateDraw(Scanline Line) {
        int bufferPos = dest + FrameNo * TVP;
        for (int i = 0; i < Line.scanline_len; i++) {
            int c = Line.scanline[i];

            mScreenImageBufferData[bufferPos + RasterX] = Colours[c + Shade];

            RasterX += 1;
            if (RasterX > ScanLen) {
                RasterX = 0;
                dest += TVP * Scale;
                bufferPos = dest + FrameNo * TVP;
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
                dest += TVP * Scale;
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
        int dest = y * TVP;

        while (y <= WinB) {
            while (x <= WinR) {
                    mScreenImageBufferData[dest + x] = 0;

                x += 1;
            }
            x = 0;
            y++;
            dest += TVP;
        }
    }

    private void RecalcPalette() {
        int rsz, gsz, bsz;  //bitsize of field
        int rsh, gsh, bsh;  //0's on left (the shift value)
        int CompiledPixel;
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

            CompiledPixel = r | g | b;
            Colours[i] = CompiledPixel;
        }
    }

    public AccDraw(ZX81Config config, boolean fullSpeed, int scale) {
        machine = config.machine;
        this.fullSpeed = fullSpeed;

        InitializePalette();
        mCanvas = new AccCanvas(this);
        AccurateInit(scale);
    }

    public Canvas getCanvas() {
        return mCanvas;
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
    private int fps;
    private int borrow;
    private static int scanLineNumber = 0;

    private void AnimTimer1Timer() {

        if (machine.stop()) {
            AccurateUpdateDisplay();
            return;
        }

        fps++;

        int j = machine.tperframe + borrow;

        while (j > 0 && !machine.stop()) {
            j -= machine.do_scanline(BuildLine);

            scanLineNumber++;
            if (scanLineNumber < 0) //< 50000 )
            {
                System.out.println(scanLineNumber + ":" + RasterX + "," + RasterY + " len " + BuildLine.scanline_len +
                        " slen " + BuildLine.sync_len +
                        " sv " + BuildLine.sync_valid +
                        " borrow " + ((ZX81) machine).borrow);
            }

            AccurateDraw(BuildLine);

        }

        if (!machine.stop()) borrow = j;
    }

    /**
     * Main routine to draw frames.
     */
    public void run() {
        Scanline[] Video = new Scanline[]{new Scanline(), new Scanline()};
        BuildLine = Video[0];

        fps = 0;
        long framesStartTime = System.currentTimeMillis();

        // Target frame time should result in 50Hz display.
        int targetFrameTime = 1000 / 50;
        mKeepGoing = true;
        while (mKeepGoing) {
            if (mPaused) {
                try {
                    Thread.sleep(1000);
                } catch (Throwable exc) {
                }
                fps = 0;
                framesStartTime = System.currentTimeMillis();
            } else {
                // Process a scanline

                AnimTimer1Timer();

                // Pace the emulator to run at 100%
                long currentTime = System.currentTimeMillis();
                long delay = (targetFrameTime * fps) - (currentTime - framesStartTime);
                if (!fullSpeed && delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException exc) {
                    }
                }

                if (fps == 100) {
                    framesStartTime = System.currentTimeMillis();
                    fps = 0;
                }
            }
        }
    }

    /**
     * Stops the display drawing routine.
     */
    public void
    stop() {
        mKeepGoing = false;
    }

    public void setPaused(boolean paused) {
        mPaused = paused;
    }
}

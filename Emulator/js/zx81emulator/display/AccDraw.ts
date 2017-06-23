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

namespace zx81emulator.display
{
    import Machine = zx81emulator.config.Machine;
    import ZX81Config = zx81emulator.config.ZX81Config;
    import ZX81ConfigDefs = zx81emulator.config.ZX81ConfigDefs;

    const HTOL: number = 405;
    const VTOLMIN: number = 290;
    const VTOLMAX: number = 340;
    const HMIN: number = 10;
    const VMIN: number = 350;

    const HSYNC_TOLLERANCE: number = HTOL;
    const VSYNC_TOLLERANCEMIN: number = VTOLMIN;
    const VSYNC_TOLLERANCEMAX: number = VTOLMAX;
    const HSYNC_MINLEN: number = HMIN;
    const VSYNC_MINLEN: number = VMIN;

    const NoWinT: number = 32;
    const NoWinB: number = NoWinT + 240;
    const NoWinL: number = 42;
    const NoWinR: number = NoWinL + 320;

    const WinR: number = NoWinR;
    const WinL: number = NoWinL;
    const WinT: number = NoWinT;
    const WinB: number = NoWinB;

    const RasterX: number = 0;
    const RasterY: number = 0;
    const TVW: number = 520;
    const TVH: number = 380;

    const targetFrameTime: number = 1000 / 50; // Target frame time should result in 50Hz display

    export class AccDraw implements java.lang.Runnable, ZX81ConfigDefs
    {
        private machine: Machine;
        private ScanLen: number;
        private Scale: number = 1;
        private context: CanvasRenderingContext2D;
        private imageData: ImageData;
        private Palette: number[] = new Array(256);
        private Colours: number[] = new Array(256);
        private mKeepGoing: boolean = true;
        private mPaused: boolean = false;
        private dest: number = 0;
        private lastDisplayUpdate: number = 0;

        public constructor(config: ZX81Config, scale: number, canvas: HTMLCanvasElement)
        {
            this.ScanLen = 0;
            this.fps = 0;
            this.borrow = 0;
            this.machine = config.machine;
            canvas.width = (NoWinR - NoWinL);
            canvas.height = (NoWinB - NoWinT);
            this.ScanLen = 2 + this.machine.tperscanline * 2;
            this.context = canvas.getContext("2d");
            this.imageData = this.context.getImageData(0, 0, canvas.width, canvas.height);
            this.InitializePalette();
        }

        private AccurateUpdateDisplay()
        {
            let currentTime: number = +new Date();
            // Aim for 50Hz display
            if (currentTime - this.lastDisplayUpdate >= (1000 / 50))
            {
                this.context.putImageData(this.imageData, 0, 0);
                this.lastDisplayUpdate = currentTime;
            }
        }

        RedrawDisplay()
        {
            this.context.putImageData(this.imageData, 0, 0);
        }

        private FrameNo: number = 0;
        private Shade: number = 0;

        private setPixel(i: number, color: number)
        {
            i *= 4;
            let data: number[] = (<any>this.imageData.data);
            data[i + 0] = color;
            data[i + 1] = color;
            data[i + 2] = color;
            data[i + 3] = 0;
        }

        private AccurateDraw(Line: Scanline)
        {
            let bufferPos: number = this.dest + this.FrameNo * AccDraw.TVW;
            for (let i: number = 0; i < Line.scanline_len; i++)
            {
                let c: number = Line.scanline[i];
                this.setPixel(bufferPos + this.RasterX, this.Colours[c + this.Shade]);
                this.RasterX += 1;
                if (this.RasterX > this.ScanLen)
                {
                    this.RasterX = 0;
                    this.dest += AccDraw.TVW * this.Scale;
                    bufferPos = this.dest + this.FrameNo * AccDraw.TVW;
                    this.RasterY += this.Scale;
                    this.Shade = 8 - this.Shade;
                    if (this.RasterY >= AccDraw.TVH)
                    {
                        i = Line.scanline_len + 1;
                        Line.sync_valid = 1;
                    }
                }
            }
            if (Line.sync_len < AccDraw.HSYNC_MINLEN_$LI$()) Line.sync_valid = 0;
            if (Line.sync_valid !== 0)
            {
                if (this.RasterX > AccDraw.HSYNC_TOLLERANCE_$LI$())
                {
                    this.RasterX = 0;
                    this.RasterY += this.Scale;
                    this.Shade = 8 - this.Shade;
                    this.dest += AccDraw.TVW * this.Scale;
                }
                if (this.RasterY >= AccDraw.TVH || this.RasterY >= AccDraw.VSYNC_TOLLERANCEMAX_$LI$() || (Line.sync_len > AccDraw.VSYNC_MINLEN_$LI$() && this.RasterY > AccDraw.VSYNC_TOLLERANCEMIN_$LI$()))
                {
                    this.CompleteFrame();
                    this.RasterX = this.RasterY = 0;
                    this.dest = 0;
                    this.FrameNo = 0;
                    this.Shade = 0;
                    this.AccurateUpdateDisplay();
                }
            }
        }

        private dumpedscanlines: boolean = false;

        private CompleteFrame()
        {
            if (!this.dumpedscanlines)
            {
                this.dumpedscanlines = true;
            }
            let x: number = this.RasterX;
            let y: number = this.RasterY;
            let dest: number = y * AccDraw.TVW;
            while ((y <= this.WinB))
            {
                while ((x <= this.WinR))
                {
                    this.setPixel(dest + x, 0);
                    x += 1;
                }
                ;
                x = 0;
                y++;
                dest += AccDraw.TVW;
            }
            ;
        }

        private RecalcPalette()
        {
            let rsz: number;
            let gsz: number;
            let bsz: number;
            let rsh: number;
            let gsh: number;
            let bsh: number;
            let i: number;
            let r: number;
            let g: number;
            let b: number;
            rsz = 8;
            gsz = 8;
            bsz = 8;
            rsh = 16;
            gsh = 8;
            bsh = 0;
            for (i = 0; i < 256; i++)
            {
                r = this.Palette[i] & 255;
                g = (this.Palette[i] >> 8) & 255;
                b = (this.Palette[i] >> 16) & 255;
                r >>= (8 - rsz);
                g >>= (8 - gsz);
                b >>= (8 - bsz);
                r <<= rsh;
                g <<= gsh;
                b <<= bsh;
                this.Colours[i] = r | g | b;
            }
        }

        private DoPal(r: number, g: number, b: number): number
        {
            return ((((b > 255 ? 255 : (b < 0 ? 0 : b)) & 255) << 16) | (((g > 255 ? 255 : (g < 0 ? 0 : g)) & 255) << 8) | ((r > 255 ? 255 : (r < 0 ? 0 : r)) & 255));
        }

        private InitializePalette()
        {
            let NoiseLevel: number;
            let GhostLevel: number;
            let GhostLevel2: number;
            let BrightnessLevel: number;
            let ContrastLevel: number;
            let ColourLevel: number;
            let HiBrightLevel: number;
            let r: number;
            let g: number;
            let b: number;
            let colour: number;
            let i: number;
            let f: number;
            let basecolour: number;
            let difference: number;
            let colr: number;
            let colg: number;
            let colb: number;
            let bwr: number;
            let bwg: number;
            let bwb: number;
            AccDraw.VSYNC_TOLLERANCEMIN = 283;
            AccDraw.VSYNC_TOLLERANCEMAX = AccDraw.VSYNC_TOLLERANCEMIN + 40;
            NoiseLevel = -20;
            GhostLevel = -40;
            BrightnessLevel = 255 - 188;
            GhostLevel2 = (GhostLevel / 3 | 0);
            ContrastLevel = 255 - 125;
            ColourLevel = 255;
            BrightnessLevel -= ContrastLevel;
            HiBrightLevel = BrightnessLevel + ((ContrastLevel / 2 | 0)) + 2 * ContrastLevel;
            ContrastLevel = BrightnessLevel + ContrastLevel + ContrastLevel;
            for (i = 0; i < 16; i++)
            {
                colour = i;
                difference = ((1000 * (((colour > 7) ? HiBrightLevel : ContrastLevel) - BrightnessLevel)) / 16 | 0);
                basecolour = ((difference * ((colour & 7) + 9)) / 1000 | 0);
                if (colour === 0 || colour === 8) basecolour = BrightnessLevel;
                colb = BrightnessLevel + ((colour & 1) !== 0 ? basecolour : 0);
                colg = BrightnessLevel + ((colour & 4) !== 0 ? basecolour : 0);
                colr = BrightnessLevel + ((colour & 2) !== 0 ? basecolour : 0);
                bwb = BrightnessLevel + basecolour;
                bwg = BrightnessLevel + basecolour;
                bwr = BrightnessLevel + basecolour;
                r = ((((colr - bwr) * ColourLevel) / 255 | 0)) + bwr;
                g = ((((colg - bwg) * ColourLevel) / 255 | 0)) + bwg;
                b = ((((colb - bwb) * ColourLevel) / 255 | 0)) + bwb;
                this.Palette[i * 16] = this.DoPal(r, g, b);
                this.Palette[4 + i * 16] = this.DoPal(r + GhostLevel2, g + GhostLevel2, b + GhostLevel2);
                b += NoiseLevel;
                g += NoiseLevel;
                r += NoiseLevel;
                this.Palette[i * 16 + 1] = this.DoPal(r, g, b);
                this.Palette[4 + i * 16 + 1] = this.DoPal(r + GhostLevel2, g + GhostLevel2, b + GhostLevel2);
                b += GhostLevel;
                g += GhostLevel;
                r += GhostLevel;
                this.Palette[i * 16 + 3] = this.DoPal(r, g, b);
                this.Palette[4 + i * 16 + 3] = this.DoPal(r, g, b);
                b -= NoiseLevel;
                g -= NoiseLevel;
                r -= NoiseLevel;
                this.Palette[i * 16 + 2] = this.DoPal(r, g, b);
                this.Palette[4 + i * 16 + 2] = this.DoPal(r + GhostLevel2, g + GhostLevel2, b + GhostLevel2);
            }
            for (i = 0; i < 16; i++)
            {
                for (f = 0; f < 8; f++)
                {
                    colour = this.Palette[i * 16 + f];
                    b = ((colour & 16711680) >> 16);
                    g = ((colour & 65280) >> 8);
                    r = (colour & 255);
                    this.Palette[i * 16 + f + 8] = this.DoPal(r, g, b);
                }
            }
            this.RecalcPalette();
        }

        private BuildLine: Scanline;

        private framesStartTime: number = 0;

        private fps: number;

        private borrow: number;

        private AnimTimer1Timer()
        {
            if (this.machine.stop())
            {
                this.AccurateUpdateDisplay();
                return;
            }
            this.fps++;
            let j: number = this.machine.tperframe + this.borrow;
            while ((j > 0 && !this.machine.stop()))
            {
                j -= this.machine.do_scanline(this.BuildLine);
                this.AccurateDraw(this.BuildLine);
            }
            ;
            if (!this.machine.stop()) this.borrow = j;
        }

        public run()
        {
            let Video: Scanline[] = [new Scanline(), new Scanline()];
            this.BuildLine = Video[0];
            if (!this.mKeepGoing) return;
            if (this.mPaused)
            {
                window.setTimeout((() =>
                {
                    return this.run()
                }), 1000);
                return;
            }
            if (this.framesStartTime === 0)
            {
                this.fps = 0;
                this.framesStartTime = java.lang.System.currentTimeMillis();
            }
            this.AnimTimer1Timer();
            let currentTime: number = java.lang.System.currentTimeMillis();
            let delay: number = (AccDraw.targetFrameTime_$LI$() * this.fps) - (currentTime - this.framesStartTime);
            if (delay > 0)
            {
                window.setTimeout((() =>
                {
                    return this.run()
                }), delay);
                return;
            }
            if (this.fps === 100)
            {
                this.framesStartTime = java.lang.System.currentTimeMillis();
                this.fps = 0;
            }
        }

        public start()
        {
            this.mKeepGoing = true;
            window.setTimeout((() =>
            {
                return this.run()
            }), 0);
        }

        public stop()
        {
            this.mKeepGoing = false;
        }

        public setPaused(paused: boolean)
        {
            this.mPaused = paused;
        }
    }
}


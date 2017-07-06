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

import ZX81 from "../zx81/ZX81";
import Scanline from "./Scanline";

const HTOL: number = 405;
const VTOLMIN: number = 290;
const VTOLMAX: number = 340;
const HMIN: number = 10;
const VMIN: number = 350;

const HSYNC_TOLLERANCE: number = HTOL;
const VSYNC_TOLLERANCEMIN: number = 283;
const VSYNC_TOLLERANCEMAX: number = VSYNC_TOLLERANCEMIN + 40;
const HSYNC_MINLEN: number = HMIN;
const VSYNC_MINLEN: number = VMIN;

const WinW: number = 320;
const WinH: number = 240;

const NoWinT: number = 32;
const NoWinB: number = NoWinT + WinH;
const NoWinL: number = 42;
const NoWinR: number = NoWinL + WinW;

const WinR: number = NoWinR;
const WinL: number = NoWinL;
const WinT: number = NoWinT;
const WinB: number = NoWinB;

const TVW: number = 520;
const TVH: number = 380;

const targetFrameTime: number = 1000 / 50; // Target frame time should result in 50Hz display

export default class AccDraw
{
    private machine: ZX81;
    private scanLen: number = 0;
    private scale: number = 1;
    private canvas: HTMLCanvasElement;
    private context: CanvasRenderingContext2D;
    private imageData: ImageData;
    private argb: Uint32Array;
    private srcCanvas: HTMLCanvasElement;
    private srcContext: CanvasRenderingContext2D;
    private palette: number[] = new Array(256);
    private colours: number[] = new Array(256);
    private keepGoing: boolean = true;
    private paused: boolean = false;
    private dest: number = 0;
    private lastDisplayUpdate: number = 0;
    private rasterX: number = 0;
    private rasterY: number = 0;
    private frameNo: number = 0;
    private shade: number = 0;
    private dumpedscanlines: boolean = false;
    private framesStartTime: number = 0;
    private fps: number = 0;
    private borrow: number = 0;

    public constructor(machine: ZX81, scale: number, canvas: HTMLCanvasElement)
    {
        this.scale = scale;
        this.canvas = canvas;
        this.machine = machine;
        this.scanLen = 2 + this.machine.tperscanline * 2;

        this.canvas.width = TVW * this.scale;
        this.canvas.height = TVH * this.scale;
        this.canvas.hidden = false;
        this.context = this.canvas.getContext("2d");
        this.context.webkitImageSmoothingEnabled = false;

        this.srcCanvas = <HTMLCanvasElement>(document.createElement("CANVAS"));
        this.srcCanvas.width = TVW;
        this.srcCanvas.height = TVH;
        this.srcCanvas.hidden = true;
        this.srcContext = this.srcCanvas.getContext("2d");
        this.imageData = this.srcContext.getImageData(0, 0, TVW, TVH);
        this.argb = new Uint32Array(this.imageData.data.buffer);

        this.InitializePalette();
    }

    private static currentTimeMillis(): number
    {
        return +new Date();
    }

    private UpdateDisplay()
    {
        let currentTime: number = AccDraw.currentTimeMillis();
        // Aim for 50Hz display
        if (currentTime - this.lastDisplayUpdate >= (1000 / 50))
        {
            this.RedrawDisplay();
            this.lastDisplayUpdate = currentTime;
        }
    }

    public RedrawDisplay()
    {
        this.srcContext.putImageData(this.imageData, 0, 0, WinL, WinT, WinW, WinH);
        this.context.fillStyle = "white";
        this.context.fillRect(0, 0, this.canvas.width, this.canvas.height);
        this.context.drawImage(this.srcCanvas, 0, 0, WinW * this.scale, WinH * this.scale);
    }

    private Draw(Line: Scanline)
    {
        let bufferPos: number = this.dest + this.frameNo * TVW;
        for (let i: number = 0; i < Line.scanline_len; i++)
        {
            let c: number = Line.scanline[i];

            this.argb[bufferPos + this.rasterX] = 255 - this.colours[c + this.shade];
            this.rasterX += 1;

            if (this.rasterX > this.scanLen)
            {
                this.rasterX = 0;
                this.dest += TVW;
                bufferPos = this.dest + this.frameNo * TVW;
                this.rasterY += 1;
                this.shade = 8 - this.shade;
                if (this.rasterY >= TVH)
                {
                    i = Line.scanline_len + 1;
                    Line.sync_valid = 1;
                }
            }
        }
        if (Line.sync_len < HSYNC_MINLEN) Line.sync_valid = 0;
        if (Line.sync_valid !== 0)
        {
            if (this.rasterX > HSYNC_TOLLERANCE)
            {
                this.rasterX = 0;
                this.rasterY += 1;
                this.shade = 8 - this.shade;
                this.dest += TVW;
            }
            if (this.rasterY >= TVH || this.rasterY >= VSYNC_TOLLERANCEMAX || (Line.sync_len > VSYNC_MINLEN && this.rasterY > VSYNC_TOLLERANCEMIN))
            {
                this.CompleteFrame();
                this.rasterX = this.rasterY = 0;
                this.dest = 0;
                this.frameNo = 0;
                this.shade = 0;
                this.UpdateDisplay();
            }
        }
    }

    private CompleteFrame()
    {
        if (!this.dumpedscanlines)
        {
            this.dumpedscanlines = true;
        }
        let x: number = this.rasterX;
        let y: number = this.rasterY;
        let dest: number = y * TVW;
        while(y <= WinB)
        {
            while(x <= WinR)
            {
                this.argb[dest + x] = 0;
                x += 1;
            }
            x = 0;
            y++;
            dest += TVW;
        }
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
            r = this.palette[i] & 255;
            g = (this.palette[i] >> 8) & 255;
            b = (this.palette[i] >> 16) & 255;
            r >>= (8 - rsz);
            g >>= (8 - gsz);
            b >>= (8 - bsz);
            r <<= rsh;
            g <<= gsh;
            b <<= bsh;
            this.colours[i] = r | g | b;
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
            this.palette[i * 16] = this.DoPal(r, g, b);
            this.palette[4 + i * 16] = this.DoPal(r + GhostLevel2, g + GhostLevel2, b + GhostLevel2);
            b += NoiseLevel;
            g += NoiseLevel;
            r += NoiseLevel;
            this.palette[i * 16 + 1] = this.DoPal(r, g, b);
            this.palette[4 + i * 16 + 1] = this.DoPal(r + GhostLevel2, g + GhostLevel2, b + GhostLevel2);
            b += GhostLevel;
            g += GhostLevel;
            r += GhostLevel;
            this.palette[i * 16 + 3] = this.DoPal(r, g, b);
            this.palette[4 + i * 16 + 3] = this.DoPal(r, g, b);
            b -= NoiseLevel;
            g -= NoiseLevel;
            r -= NoiseLevel;
            this.palette[i * 16 + 2] = this.DoPal(r, g, b);
            this.palette[4 + i * 16 + 2] = this.DoPal(r + GhostLevel2, g + GhostLevel2, b + GhostLevel2);
        }
        for (i = 0; i < 16; i++)
        {
            for (f = 0; f < 8; f++)
            {
                colour = this.palette[i * 16 + f];
                b = ((colour & 16711680) >> 16);
                g = ((colour & 65280) >> 8);
                r = (colour & 255);
                this.palette[i * 16 + f + 8] = this.DoPal(r, g, b);
            }
        }
        this.RecalcPalette();
    }

    public run()
    {
        let buildLine = new Scanline();
        this.fps = 0;
        this.framesStartTime = AccDraw.currentTimeMillis();

        while(this.keepGoing)
        {
            if (this.paused)
            {
                window.setTimeout((() => { return this.run() }), 1000);
                return;
            }

            if (this.machine.stop())
            {
                this.UpdateDisplay();
                return;
            }

            this.fps++;
            let j: number = this.machine.tperframe + this.borrow;
            while ((j > 0 && !this.machine.stop()))
            {
                j -= this.machine.do_scanline(buildLine);
                this.Draw(buildLine);
            }
            if (!this.machine.stop())
                this.borrow = j;

            let currentTime: number = AccDraw.currentTimeMillis();
            let delay: number = (targetFrameTime * this.fps) - (currentTime - this.framesStartTime);
            if (delay > 0)
            {
                window.setTimeout((() => { return this.run() }), delay);
                return;
            }

            if (this.fps === 100)
            {
                this.framesStartTime = AccDraw.currentTimeMillis();
                this.fps = 0;
            }
        }
    }

    public start()
    {
        this.keepGoing = true;
        window.setTimeout((() => { return this.run(); }), 0);
    }

    public stop()
    {
        this.keepGoing = false;
    }

    public setPaused(paused: boolean)
    {
        this.paused = paused;
    }
}



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

export default class Drawer
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
        this.context.fillStyle = "red";
        this.context.fillRect(0, 0, this.canvas.width, this.canvas.height);

        this.srcCanvas = <HTMLCanvasElement>(document.createElement("CANVAS"));
        this.srcCanvas.width = TVW;
        this.srcCanvas.height = TVH;
        this.srcCanvas.hidden = true;
        this.srcContext = this.srcCanvas.getContext("2d");
        this.imageData = this.srcContext.getImageData(0, 0, TVW, TVH);
        this.argb = new Uint32Array(this.imageData.data.buffer);
    }

    private static currentTimeMillis(): number
    {
        return +new Date();
    }

    private UpdateDisplay()
    {
        let currentTime: number = Drawer.currentTimeMillis();
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
        this.context.drawImage(this.srcCanvas, 0, 0, WinW * this.scale, WinH * this.scale);
    }

    private Draw(line: Scanline)
    {
        let bufferPos: number = this.dest + this.frameNo * TVW;
        for (let i: number = 0; i < line.scanline_len; i++)
        {
            let c: number = line.scanline[i];

            this.argb[bufferPos + this.rasterX] = c ? 0xFFFFFFFF : 0xFF000000;
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
                    i = line.scanline_len + 1;
                    line.sync_valid = 1;
                }
            }
        }
        if (line.sync_len < HSYNC_MINLEN)
            line.sync_valid = 0;
        if (line.sync_valid !== 0)
        {
            if (this.rasterX > HSYNC_TOLLERANCE)
            {
                this.rasterX = 0;
                this.rasterY += 1;
                this.shade = 8 - this.shade;
                this.dest += TVW;
            }
            if (this.rasterY >= TVH || this.rasterY >= VSYNC_TOLLERANCEMAX || (line.sync_len > VSYNC_MINLEN && this.rasterY > VSYNC_TOLLERANCEMIN))
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

    public run()
    {
        let buildLine = new Scanline();
        this.fps = 0;
        this.framesStartTime = Drawer.currentTimeMillis();

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

            let currentTime: number = Drawer.currentTimeMillis();
            let delay: number = (targetFrameTime * this.fps) - (currentTime - this.framesStartTime);
            if (delay > 0)
            {
                window.setTimeout((() => { return this.run() }), delay);
                return;
            }

            if (this.fps === 100)
            {
                this.framesStartTime = Drawer.currentTimeMillis();
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



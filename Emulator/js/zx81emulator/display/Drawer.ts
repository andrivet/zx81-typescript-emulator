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

import Machine from "../machine/Machine";
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

const WinL: number = 42;
const WinT: number = 32;
const WinR: number = WinL + WinW;
const WinB: number = WinT + WinH;

const TVW: number = 520;
const TVH: number = 380;

const targetFrameTime: number = 1000 / 50; // Target frame time should result in 50Hz display

const enum COLOR
{
    BLACK = 0xFFFFFFFF,
    WHITE = 0xFF000000
}

function sleep(delay: number): Promise<void> { return new Promise((resolve) => { setTimeout(() => resolve(), delay); }); }

export default class Drawer
{
    private machine: Machine;
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
    private borrow: number = 0;

    public constructor(machine: Machine, scale: number, canvas: HTMLCanvasElement)
    {
        this.scale = scale;
        this.canvas = canvas;
        this.machine = machine;
        this.scanLen = 2 + this.machine.tperscanline * 2;

        this.canvas.width = WinW * this.scale;
        this.canvas.height = WinH * this.scale;
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
        this.srcContext.putImageData(this.imageData, 0, 0, 0, 0, TVW, TVH);
        this.context.drawImage(this.srcCanvas,
            WinL, WinT, WinW, WinH,
           0, 0, this.canvas.width, this.canvas.height);
    }

    private Draw(scanline: Scanline)
    {
        let bufferPos: number = this.dest + this.frameNo * TVW;
        for (let i: number = 0; i < scanline.get_length(); i++)
        {
            this.argb[bufferPos + this.rasterX] = scanline.get_pixel(i) ? COLOR.WHITE : COLOR.BLACK;
            this.rasterX += 1;

            if (this.rasterX > this.scanLen)
            {
                this.rasterX = 0;
                this.dest += TVW;
                bufferPos = this.dest + this.frameNo * TVW;
                this.rasterY += 1;
                if (this.rasterY >= TVH)
                    i = scanline.next_line();
            }
        }

        if (scanline.check_sync_length(HSYNC_MINLEN))
        {
            if (this.rasterX > HSYNC_TOLLERANCE)
            {
                this.rasterX = 0;
                this.rasterY += 1;
                this.dest += TVW;
            }
            if (this.rasterY >= TVH || this.rasterY >= VSYNC_TOLLERANCEMAX || (scanline.get_sync_length() > VSYNC_MINLEN && this.rasterY > VSYNC_TOLLERANCEMIN))
            {
                this.CompleteFrame();
                this.rasterX = this.rasterY = 0;
                this.dest = 0;
                this.frameNo = 0;
                this.UpdateDisplay();
            }
        }
    }

    private CompleteFrame()
    {
        let x: number = this.rasterX;
        let y: number = this.rasterY;
        let dest: number = y * TVW;
        while(y <= WinB)
        {
            while(x <= WinR)
            {
                this.argb[dest + x] = 0xFFAAAAAA; // Gray
                x += 1;
            }
            x = 0;
            y++;
            dest += TVW;
        }
    }

    public async run()
    {
        let buildLine = new Scanline();
        let framesStartTime: number = 0;
        let fps: number = 0;

        this.keepGoing = true;
        while(this.keepGoing)
        {
            try
            {
                if (this.paused)
                {
                    await sleep(1000);
                    fps = 0;
                    framesStartTime = Drawer.currentTimeMillis();
                    return;
                }

                fps++;

                let j: number = this.machine.tperframe + this.borrow;
                while (j > 0)
                {
                    j -= this.machine.do_scanline(buildLine);
                    this.Draw(buildLine);
                }
                this.borrow = j;

                let currentTime: number = Drawer.currentTimeMillis();
                let delay: number = (targetFrameTime * fps) - (currentTime - framesStartTime);
                if (delay > 0)
                {
                    await sleep(delay);
                }

                if (fps === 100)
                {
                    framesStartTime = Drawer.currentTimeMillis();
                    fps = 0;
                }
            }
            catch(err)
            {
                // TODO
            }
        }
    }

    public start()
    {
        this.run();
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



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

class Color {public r: number; public g: number; public b: number; public a: number;}
const Black = {r: 0x20, g: 0x20, b:0x20, a:0xFF};
const White = {r: 0xFF, g: 0xFF, b:0xFF, a:0xFF};
const Gray  = {r: 0xAA, g: 0xAA, b:0xAA, a:0xFF};

const targetFrameTime = 1000 / 50; // Target frame time should result in 50Hz display
const targetDisplayUpdate = 1000 / 50;

export default class Drawer
{
    private machine: Machine;
    private scanLen: number = 0;
    private scale: number = 1;
    private canvas: HTMLCanvasElement;
    private context: CanvasRenderingContext2D | null;
    private argb: ImageData;
    private srcCanvas: HTMLCanvasElement;
    private srcContext: CanvasRenderingContext2D | null;
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
        this.scanLen = 2 + this.machine.tPerScanLine * 2;

        this.canvas.width = WinW * this.scale;
        this.canvas.height = WinH * this.scale;
        this.canvas.hidden = false;
        this.context = this.canvas.getContext("2d");
        if(this.context != null)
            this.context.webkitImageSmoothingEnabled = false;

        this.srcCanvas = <HTMLCanvasElement>(document.createElement("CANVAS"));
        this.srcCanvas.width = TVW;
        this.srcCanvas.height = TVH;
        this.srcCanvas.hidden = true;
        this.srcContext = this.srcCanvas.getContext("2d");
        if(this.srcContext != null)
            this.argb = this.srcContext.getImageData(0, 0, TVW, TVH);
    }

    private static currentTimeMillis(): number
    {
        return +new Date();
    }

    private updateDisplay()
    {
        const currentTime: number = Drawer.currentTimeMillis();
        if (currentTime - this.lastDisplayUpdate >= targetDisplayUpdate)
        {
            this.redrawDisplay();
            this.lastDisplayUpdate = currentTime;
        }
    }

    public redrawDisplay()
    {
        if(this.srcContext == null || this.context == null)
            return;

        this.srcContext.putImageData(this.argb, 0, 0, 0, 0, TVW, TVH);
        this.context.drawImage(this.srcCanvas,
            WinL, WinT, WinW, WinH,
           0, 0, this.canvas.width, this.canvas.height);
    }

    private draw(scanline: Scanline)
    {
        let bufferPos: number = this.dest + this.frameNo * TVW;
        let p = (bufferPos + this.rasterX) * 4;
        for (let i: number = 0; i < scanline.getLength(); i++)
        {
            const color = scanline.getPixel(i) ? Black : White;
            this.argb.data[p    ] = color.r;
            this.argb.data[p + 1] = color.g;
            this.argb.data[p + 2] = color.b;
            this.argb.data[p + 3] = color.a;
            this.rasterX++;
            p += 4;

            if (this.rasterX > this.scanLen)
            {
                this.rasterX = 0;
                this.dest += TVW;
                bufferPos = this.dest + this.frameNo * TVW;
                p = (bufferPos + this.rasterX) * 4;
                this.rasterY++;
                if (this.rasterY >= TVH)
                    i = scanline.nextLine();
            }
        }

        if (scanline.checkSyncLength(HSYNC_MINLEN))
        {
            if (this.rasterX > HSYNC_TOLLERANCE)
            {
                this.rasterX = 0;
                this.rasterY++;
                this.dest += TVW;
            }
            if (this.rasterY >= TVH || this.rasterY >= VSYNC_TOLLERANCEMAX || (scanline.getSyncLength() > VSYNC_MINLEN && this.rasterY > VSYNC_TOLLERANCEMIN))
            {
                this.completeFrame();
                this.frameNo = 0;
                this.updateDisplay();
            }
        }
    }

    private completeFrame()
    {
        let dest: number = this.rasterY * TVW;
        let p = (dest + this.rasterX) * 4;
        while(this.rasterY <= WinB)
        {
            while(this.rasterX <= WinR)
            {
                this.argb.data[p    ] = Gray.r;
                this.argb.data[p + 1] = Gray.g;
                this.argb.data[p + 2] = Gray.b;
                this.argb.data[p + 3] = Gray.a;
                this.rasterX += 1;
                p += 4;
            }
            this.rasterX = 0;
            this.rasterY++;
            dest += TVW;
            p = (dest + this.rasterX) * 4;
        }

        this.rasterX = this.rasterY = 0;
        this.dest = 0;
    }

    public async run(): Promise<void>
    {
        const buildLine = new Scanline();
        let framesStartTime: number = 0;
        let fps: number = 0;

        this.keepGoing = true;
        while(this.keepGoing)
        {
            if (this.paused)
            {
                await Machine.sleep(1000);
                fps = 0;
                framesStartTime = Drawer.currentTimeMillis();
                return;
            }

            fps++;

            let j: number = this.machine.tPerFrame + this.borrow;
            while (j > 0)
            {
                j -= this.machine.doScanline(buildLine);
                this.draw(buildLine);
            }
            this.borrow = j;

            const currentTime: number = Drawer.currentTimeMillis();
            const delay: number = (targetFrameTime * fps) - (currentTime - framesStartTime);
            if (delay > 0)
                await Machine.sleep(delay);

            if (fps === 100)
            {
                framesStartTime = Drawer.currentTimeMillis();
                fps = 0;
            }
        }
    }

    public start()
    {
        window.setTimeout(() => this.run(), 0);
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

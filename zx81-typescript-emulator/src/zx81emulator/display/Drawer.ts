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

import Time from "../io/Time";
import Machine from "../machine/Machine";
import Scanline from "./Scanline";

const HTOL = 405;
const HMIN = 10;
const VMIN = 350;

const HSYNC_TOLLERANCE = HTOL;
const VSYNC_TOLLERANCEMIN = 283;
const VSYNC_TOLLERANCEMAX = VSYNC_TOLLERANCEMIN + 40;
const HSYNC_MINLEN = HMIN;
const VSYNC_MINLEN = VMIN;

const WinW = 320;
const WinH = 240;

const WinL = 42;
const WinT = 32;
const WinR = WinL + WinW;
const WinB = WinT + WinH;

const TVW = 520;
const TVH = 380;

//const Black = 0xFF202020;
//const White = 0xFFFFFFFF;
const Black = 0xFFFFFFFF;
const White = 0xFF202020;
const Gray  = 0xFFAAAAAA;

const targetFrameTime = 1000 / 50; // Target frame time should result in 50Hz display
const targetDisplayUpdate = 1000 / 50;

export default class Drawer
{
    private machine: Machine;
    private scanLen: number = 0;
    private scale: number = 1;
    private canvas: HTMLCanvasElement;
    private context: CanvasRenderingContext2D | null;
    private imageData: ImageData;
    private argb: Uint32Array;
    private hiddenCanvas: HTMLCanvasElement;
    private hiddenContext: CanvasRenderingContext2D | null;
    private keepGoing: boolean = true;
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
        if(!this.context)
            throw new Error("Error creating 2D context");

        this.context.fillStyle = "#808080";
        this.context.fillRect(0, 0,this.canvas.width, this.canvas.height);

        // Create an hidden Canvas (and associated context).
        // It will be used to generate the screen of the emulator (1:1 scale).
        // This screen will then be scaled (eventually) on the visible canvas.
        this.hiddenCanvas = <HTMLCanvasElement>(document.createElement("CANVAS"));
        this.hiddenCanvas.width = TVW;
        this.hiddenCanvas.height = TVH;
        this.hiddenCanvas.hidden = true;
        this.hiddenContext = this.hiddenCanvas.getContext("2d");
        if(!this.hiddenContext)
            throw new Error("Error creating hidden 2D context");

        this.imageData = this.hiddenContext.createImageData(TVW, TVH);
        this.argb = new Uint32Array(this.imageData.data.buffer);
    }

    private updateDisplay()
    {
        const currentTime = Time.currentTimeMillis();
        if (currentTime - this.lastDisplayUpdate >= targetDisplayUpdate)
        {
            this.redrawDisplay();
            this.lastDisplayUpdate = currentTime;
        }
    }

    public redrawDisplay()
    {
        if(!this.hiddenContext || !this.context)
            return;

        // Set the bytes of the hidden canvas and then draw it (eventually scalled) in the visible one
        this.hiddenContext.putImageData(this.imageData, 0, 0, 0, 0, TVW, TVH);
        this.context.drawImage(this.hiddenCanvas,
            WinL, WinT, WinW, WinH,
           0, 0, this.canvas.width, this.canvas.height);
    }

    private draw(scanline: Scanline)
    {
        let bufferPos = this.dest + this.frameNo * TVW + this.rasterX;

        for (let i = 0; i < scanline.getLength(); i++)
        {
            this.argb[bufferPos] = scanline.getPixel(i) ? Black : White;
            this.rasterX++;
            bufferPos++;

            if (this.rasterX > this.scanLen)
            {
                this.rasterX = 0;
                this.dest += TVW;
                bufferPos = this.dest + this.frameNo * TVW + this.rasterX;
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
        let bufferPos = this.dest + this.frameNo * TVW + this.rasterX;

        while(this.rasterY <= WinB)
        {
            while(this.rasterX <= WinR)
            {
                this.argb[bufferPos] = Gray;
                this.rasterX++;
                bufferPos++;
            }
            this.rasterX = 0;
            this.rasterY++;
            this.dest = this.rasterY * TVW;
            bufferPos = this.dest + this.frameNo * TVW + this.rasterX;
        }

        this.rasterX = this.rasterY = 0;
        this.dest = 0;
    }

    public async run(): Promise<void>
    {
        const buildLine = new Scanline();
        let framesStartTime = 0;
        let fps = 0;

        this.keepGoing = true;
        while(this.keepGoing)
        {
            fps++;

            let j = this.machine.tPerFrame + this.borrow;
            while (j > 0)
            {
                j -= this.machine.doScanline(buildLine);
                this.draw(buildLine);
            }
            this.borrow = j;

            const currentTime = Time.currentTimeMillis();
            const delay = (targetFrameTime * fps) - (currentTime - framesStartTime);
            if (delay > 0)
                await Time.sleep(delay);

            if (fps === 100)
            {
                framesStartTime = Time.currentTimeMillis();
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
}

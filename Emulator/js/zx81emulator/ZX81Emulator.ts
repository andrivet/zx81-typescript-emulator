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


import AccDraw from "./display/AccDraw";
import {KBStatus} from "./io/KBStatus";
import ZX81Config from "./config/ZX81Config";
import ZX81 from "./zx81/ZX81";

export class ZX81Emulator
{
    private mDisplayDrawer: AccDraw;
    private mKeyboard: KBStatus;
    private mConfig: ZX81Config;

    public constructor()
    {
        this.mConfig = new ZX81Config();
        this.mConfig.load_config();

    }

    public load(tzxFileName: string, scale: number, canvasID: string): void
    {
        let canvas: HTMLCanvasElement = <HTMLCanvasElement>document.getElementById(canvasID);
        if (canvas == null)
            throw new Error("No HTML element found with id \'canvas\'");

        this.installListeners(canvas);
        this.mConfig.machine.initialise(this.mConfig);
        this.mKeyboard = new KBStatus();
        this.mDisplayDrawer = new AccDraw(this.mConfig, scale, canvas);
        if (tzxFileName != null)
        {
            let tzxEntry: string;
            let entryNum: number = 0;
            let atPos: number = tzxFileName.indexOf('@');
            if (atPos !== -1)
            {
                tzxEntry = tzxFileName.substring(atPos + 1);
                tzxFileName = tzxFileName.substring(0, atPos);
                entryNum = +tzxEntry;
            }
            //this.mConfig.machine.getTape().loadTZX(this.mConfig, this.mKeyboard, tzxFileName, entryNum);
            this.start();
        }
    }

    public start()
    {
        this.mDisplayDrawer.start();
    }

    /**
     * Stops the applet.
     */
    public stop()
    {
        this.mDisplayDrawer.stop();
    }

    private installListeners(container: HTMLElement)
    {
        container.addEventListener("keydown", (event) =>
        {
            this.onKeyDown(event);
            return null;
        }, true);
        container.addEventListener("keyup", (event) =>
        {
            this.onKeyUp(event);
            return null;
        }, true);
    }

    private onKeyDown(e: KeyboardEvent)
    {
        e.preventDefault();
        this.mKeyboard.PCKeyDown(e.key, e.shiftKey, e.ctrlKey, e.altKey);
    }

    private onKeyUp(e: KeyboardEvent)
    {
        e.preventDefault();
        this.mKeyboard.PCKeyUp(e.key, e.shiftKey, e.ctrlKey, e.altKey);
    }

    private windowActive(active: boolean)
    {
        this.mDisplayDrawer.setPaused(!active);
    }
}

let emulator: ZX81Emulator = new ZX81Emulator;
window.onload = () =>
{
    emulator.load("USINE.tzx", 2, "canvas");
};

window.onunload = () =>
{
    emulator.stop();
};



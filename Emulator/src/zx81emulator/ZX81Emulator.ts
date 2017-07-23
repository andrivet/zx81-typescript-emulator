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

import "../css/main.css";
import "../css/normalize.css";
import Drawer from "./display/Drawer";
import ZX81 from "./machine/ZX81";

export class ZX81Emulator
{
    private machine: ZX81;
    private drawer: Drawer;

    public async load(fileNameID: string, scale: number, canvasID: string): Promise<void>
    {
        try
        {
            let canvas: HTMLCanvasElement = <HTMLCanvasElement>document.getElementById(canvasID);
            if (canvas == null)
                throw new Error("No HTML element found with id \'canvas\'");

            let filename: string | null = null;
            let filenameInput: HTMLInputElement = <HTMLInputElement>document.getElementById(fileNameID);
            if(filenameInput != null)
                filename = filenameInput.value;

            this.machine = new ZX81();
            this.drawer = new Drawer(this.machine, scale, canvas);

            this.installListeners();

            await this.start();

            if (filename != null && filename.length > 0)
            {
                await this.machine.load_program(filename);
                await this.machine.autoLoad();
            }
        }
        catch(err)
        {
            console.log("Error while initializing Emulator: " + err);
        }
    }

    public async start(): Promise<void>
    {
        this.drawer.start();
        await this.machine.loadROM();
    }

    public stop()
    {
        this.drawer.stop();
    }

    private installListeners()
    {
        window.addEventListener("keydown", (event) =>
        {
            event.preventDefault();
            this.machine.keyDown(event.which, event.shiftKey);
            return null;
        }, false);

        window.addEventListener("keyup", (event) =>
        {
            event.preventDefault();
            this.machine.keyUp(event.which, event.shiftKey);
            return null;
        }, false);
    }
}

let emulator: ZX81Emulator = new ZX81Emulator;

window.onload = () => { emulator.load("program", 3, "canvas"); };
window.onunload = () => { emulator.stop(); };

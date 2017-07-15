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


import Drawer from "./display/Drawer";
import ZX81 from "./machine/ZX81";

export class ZX81Emulator
{
    private machine: ZX81;
    private drawer: Drawer;

    public constructor()
    {
    }

    public load(fileName: string, scale: number, canvasID: string): void
    {
        let canvas: HTMLCanvasElement = <HTMLCanvasElement>document.getElementById(canvasID);
        if (canvas == null)
            throw new Error("No HTML element found with id \'canvas\'");

        this.installListeners(canvas);
        this.machine = new ZX81();
        this.drawer = new Drawer(this.machine, scale, canvas);

        if (fileName != null)
            this.machine.load_program(fileName);

        this.start();
    }

    public start()
    {
        this.drawer.start();
    }

    public stop()
    {
        this.drawer.stop();
    }

    private installListeners(container: HTMLElement)
    {
        window.addEventListener("keydown", (event) =>
        {
            event.preventDefault();
            this.machine.onKeyDown(event);
            return null;
        }, false);

        window.addEventListener("keyup", (event) =>
        {
            event.preventDefault();
            this.machine.onKeyUp(event);
            return null;
        }, false);
    }

    private windowActive(active: boolean)
    {
        this.drawer.setPaused(!active);
    }
}

let emulator: ZX81Emulator = new ZX81Emulator;
window.onload = () =>
{
    emulator.load("PROGS/FACTORY.P", 3, "canvas");
};

window.onunload = () =>
{
    emulator.stop();
};



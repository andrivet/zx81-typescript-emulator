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

const enum StatusKind { OK, Info, Warning, Error }
const mapStatus: string[] = ["alert-success", "alert-info", "alert-warning", "alert-danger"];

export default class ZX81Emulator
{
    private status: HTMLDivElement;
    private lastStatusKind: StatusKind = StatusKind.OK;
    private machine: ZX81;
    private drawer: Drawer;

    public constructor(status: HTMLDivElement)
    {
        this.status = status;
    }

    public async load(fileName: string, scale: number, canvas: HTMLCanvasElement): Promise<void>
    {
        try
        {
            this.setStatus(StatusKind.Info, "Initializing emulator...");

            this.machine = new ZX81();
            this.drawer = new Drawer(this.machine, scale, canvas);

            this.installListeners();

            await this.start();

            if (fileName.length > 0)
            {
                this.setStatus(StatusKind.Info, "Loading program " + fileName + "...");
                await this.machine.load_program(fileName);
                this.setStatus(StatusKind.Info, "Program " + fileName + " loaded, execute it...");
                await this.machine.autoLoad();
            }

            this.setStatus(StatusKind.OK, "Emulator ready and running");
        }
        catch(err)
        {
            this.setStatus(StatusKind.Error,"Error while initializing Emulator: " + err);
        }
    }

    private showStatus(show: boolean): void
    {
        if(null == this.status)
            return;

        this.status.style.visibility = show ? "visible": "hidden";
    }

    private setStatus(kind: StatusKind, message: string): void
    {
        if(null == this.status)
            return;

        if(kind !== this.lastStatusKind)
        {
            this.status.classList.remove(mapStatus[this.lastStatusKind]);
            this.status.classList.add(mapStatus[kind]);
        }

        this.status.textContent = message;
        this.lastStatusKind = kind;
    }

    public async start(): Promise<void>
    {
        this.drawer.start();
        this.setStatus(StatusKind.Info, "Loading ROM...");
        await this.machine.loadROM();
        this.setStatus(StatusKind.OK, "ROM loaded");
    }

    public stop(): void
    {
        this.drawer.stop();
    }

    private installListeners()
    {
        window.addEventListener("keydown", (event) => this.onKeyDown(event), false);
        window.addEventListener("keyup", (event) => this.onKeyUp(event), false);
    }

    public onKeyDown(event: KeyboardEvent): any
    {
        event.preventDefault();
        this.machine.keyDown(event.which, event.shiftKey);
        return null;
    }

    public onKeyUp(event: KeyboardEvent): any
    {
        event.preventDefault();
        this.machine.keyUp(event.which, event.shiftKey);
        return null;
    }
}

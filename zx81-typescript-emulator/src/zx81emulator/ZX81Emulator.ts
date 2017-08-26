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
import {Status, StatusKind} from "./display/Status";
import Time from "./io/Time";
import ZX81 from "./machine/ZX81";

const MinDayBetweenStatuses = 500; // 500 ms

export default class ZX81Emulator
{
    private canvas: HTMLCanvasElement;
    private status: Status;
    private lastStatusTime: number = 0;
    private lastStatusKind: StatusKind = StatusKind.OK;
    private machine: ZX81;
    private drawer: Drawer;

    public constructor(canvas: HTMLCanvasElement, status: Status)
    {
        this.canvas = canvas;
        this.status = status;
    }

    public async load(fileName: string, rom: string, scale: number): Promise<void>
    {
        this.setStatus(StatusKind.Info, "Initializing emulator...");

        this.machine = new ZX81();
        this.drawer = new Drawer(this.machine, scale, this.canvas);

        this.installListeners();
        await this.start(rom);

        if (fileName.length > 0)
        {
            this.setStatus(StatusKind.Info, "Loading program " + fileName + "...");
            await this.machine.load_program(fileName);
            this.setStatus(StatusKind.Info, "Program " + fileName + " loaded.");
            await Time.sleep(4000);
            this.setStatus(StatusKind.Info, "Execute the program...");
            await this.machine.autoLoad();
            this.setStatus(StatusKind.OK, "Emulator ready and program running");
        }
        else
            this.setStatus(StatusKind.OK, "Emulator ready");
    }

    public setStatus(kind: StatusKind, message: string): void
    {
        this.displayStatus(kind, message).catch();
    }

    private async displayStatus(kind: StatusKind, message: string): Promise<void>
    {
        // In the last message was an error, be sure it stays
        if(!this.status || StatusKind.Error === this.lastStatusKind)
            return;

        // Use while because several calls may be waiting. Be sure to have the same minimal delay between them
        while(Time.currentTimeMillis() - this.lastStatusTime < MinDayBetweenStatuses)
            await Time.sleep(MinDayBetweenStatuses);

        this.status.status(message, kind, this.lastStatusKind);
        this.lastStatusKind = kind;
        this.lastStatusTime = Time.currentTimeMillis();
    }

    public async start(rom: string): Promise<void>
    {
        if(!this.drawer || !this.machine)
            return;

        this.setStatus(StatusKind.Info, "Loading ROM...");
        await this.machine.loadROM(rom);
        this.setStatus(StatusKind.Info, "ROM loaded");
        this.drawer.start();
    }

    public stop(): void
    {
        if(!this.drawer)
            return;
        this.drawer.stop();
    }

    private installListeners()
    {
        window.addEventListener("keydown", (event) => this.onKeyDown(event), false);
        window.addEventListener("keyup", (event) => this.onKeyUp(event), false);
    }

    public onKeyDown(event: KeyboardEvent): any
    {
        if(!this.machine)
            return;
        event.preventDefault();
        this.machine.keyDown(event.which, event.shiftKey);
        return null;
    }

    public onKeyUp(event: KeyboardEvent): any
    {
        if(!this.machine)
            return;
        event.preventDefault();
        this.machine.keyUp(event.which, event.shiftKey);
        return null;
    }
}

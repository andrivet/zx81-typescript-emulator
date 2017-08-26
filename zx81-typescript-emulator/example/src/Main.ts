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

import ZX81Emulator from "zx81-emulator";
const pkg = require("../../package.json") as { version: string };

const versionID = "version";
const canvasID = "canvas";
const fileNameID = "program";
const romID = "rom";
const keyboardInputID = "keyboardInput";
const statusID = "status";
const scaleID = "scale";

class Main implements ZX81Emulator.Status
{
    private showKeyboard(keyboardInput: HTMLInputElement): void
    {
        if (keyboardInput)
        {
            keyboardInput.style.visibility = "visible";
            keyboardInput.focus();
            keyboardInput.style.visibility = "hidden";
        }
    }

    private getValue(elementID: string, defaultValue: string = ""): string
    {
        const input = <HTMLInputElement>document.getElementById(elementID);
        if (input)
            return input.value;
        return defaultValue;
    }

    private setVersion(): void
    {
        const span = <HTMLSpanElement>document.getElementById(versionID);
        if (!span)
            return;
        span.textContent = pkg.version;
    }

    public status(message: string, kind: ZX81Emulator.StatusKind, previousKind: ZX81Emulator.StatusKind): void
    {
        const status = <HTMLDivElement>document.getElementById(statusID);
        if(!status)
            return;

        status.textContent = message;
    }

    public main(): void
    {
        this.setVersion();

        const canvas = <HTMLCanvasElement>document.getElementById(canvasID);
        if (!canvas)
            throw new Error("No HTML element found with id \'canvas\'");

        const filename = this.getValue(fileNameID);
        const rom = this.getValue(romID, "./ROM/ZX81.rom");
        const scale = +this.getValue(scaleID, "3");
        const keyboardInput = <HTMLInputElement>document.getElementById(keyboardInputID);

        const emulator = new ZX81Emulator(canvas, this as ZX81Emulator.Status);

        window.addEventListener("load", () => {
            emulator.load(filename, rom, scale).then(() => {
                this.showKeyboard(keyboardInput);
                // When the iPad displays the keyboard, it scrolls the view so scroll it back to top
                window.scrollTo(0, 0);
            })
                .catch(
                    (err: Error) => {
                        emulator.setStatus(ZX81Emulator.StatusKind.Error, err.message);
                    }
                );
        });

        window.addEventListener("unload", () => {
            emulator.stop();
        });
    }

}

(new Main()).main();

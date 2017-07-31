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

import ZX81Emulator, {StatusKind} from "./zx81emulator/ZX81Emulator";

const canvasID = "canvas";
const fileNameID = "program";
const keyboardInputID = "keyboardInput";
const statusID = "status";
const scale = 3;

function ShowKeyboard(keyboardInput: HTMLInputElement): void
{
    if(null != keyboardInput)
    {
        keyboardInput.style.visibility = "visible";
        keyboardInput.focus();
        keyboardInput.style.visibility = "hidden";
    }
}

function Main(): void
{
    const canvas = <HTMLCanvasElement>document.getElementById(canvasID);
    if (canvas == null)
        throw new Error("No HTML element found with id \'canvas\'");

    const status = <HTMLDivElement>document.getElementById(statusID);

    let filename: string = "";
    const filenameInput: HTMLInputElement = <HTMLInputElement>document.getElementById(fileNameID);
    if(filenameInput != null)
        filename = filenameInput.value;

    const keyboardInput = <HTMLInputElement>document.getElementById(keyboardInputID);

    const emulator = new ZX81Emulator(status);

    window.addEventListener("load",  () =>
    {
        emulator.load(filename, scale, canvas).then(() =>
            {
                ShowKeyboard(keyboardInput);
                // When the iPad displays the keyboard, it scrolls the view so scroll it back to top
                window.scrollTo(0, 0);
            })
            .catch(
                (err) => { emulator.setStatus(StatusKind.Error, err); }
            );
    });

    window.addEventListener("unload",  () => { emulator.stop(); });
}

Main();

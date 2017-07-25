
import ZX81Emulator from "./zx81emulator/ZX81Emulator";

const canvasID = "canvas";
const fileNameID = "program";
const scale = 3;

function Main(): void
{
    let canvas: HTMLCanvasElement = <HTMLCanvasElement>document.getElementById(canvasID);
    if (canvas == null)
        throw new Error("No HTML element found with id \'canvas\'");

    let filename: string = "";
    let filenameInput: HTMLInputElement = <HTMLInputElement>document.getElementById(fileNameID);
    if(filenameInput != null)
        filename = filenameInput.value;

    let emulator: ZX81Emulator = new ZX81Emulator();

    window.onload = () => { emulator.load(filename, scale, canvas); };
    window.onunload = () => { emulator.stop(); };
}

Main();



import ZX81Emulator from "./zx81emulator/ZX81Emulator";

const canvasID = "canvas";
const fileNameID = "program";
const keyboardInputID = "keyboardInput";
const statusID = "status";
const scale = 2;

function ShowKeyboard(keyboardInput: HTMLInputElement): void
{
    if(null != keyboardInput)
    {
        keyboardInput.style.visibility = 'visible';
        keyboardInput.focus();
        keyboardInput.style.visibility = 'hidden';
    }
}

function Main(): void
{
    let canvas = <HTMLCanvasElement>document.getElementById(canvasID);
    if (canvas == null)
        throw new Error("No HTML element found with id \'canvas\'");

    let status = <HTMLDivElement>document.getElementById(statusID);

    let filename: string = "";
    let filenameInput: HTMLInputElement = <HTMLInputElement>document.getElementById(fileNameID);
    if(filenameInput != null)
        filename = filenameInput.value;

    let keyboardInput = <HTMLInputElement>document.getElementById(keyboardInputID);

    let emulator = new ZX81Emulator(status);

    window.addEventListener("load",  () =>
    {
        emulator.load(filename, scale, canvas);
        ShowKeyboard(keyboardInput);
        // When the iPad displays the keyboard, it scrolls the view so scroll it back to top
        window.scrollTo(0, 0);
    });

    window.addEventListener("unload",  () => { emulator.stop(); });
}

Main();


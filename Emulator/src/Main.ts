
import ZX81Emulator from "./zx81emulator/ZX81Emulator";

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
            .catch(/* nothing */);
    });

    window.addEventListener("unload",  () => { emulator.stop(); });
}

Main();

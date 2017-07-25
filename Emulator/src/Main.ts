
import ZX81Emulator from "./zx81emulator/ZX81Emulator";

let emulator: ZX81Emulator = new ZX81Emulator();

window.onload = () => { emulator.load("program", 3, "canvas"); };
window.onunload = () => { emulator.stop(); };

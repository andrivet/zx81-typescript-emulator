// Type definitions for zx81-typescript-emulator
// Project: https://github.com/andrivet/zx81-typescript-emulator
// Definitions by: Sebastien Andrivet <http://github.com/andrivet>

declare class ZX81Emulator
{
    constructor(canvas: HTMLCanvasElement, status: ZX81Emulator.Status);

    load(fileName: string, rom: string, scale: number): Promise<void>;
    setStatus(kind: ZX81Emulator.StatusKind, message: string): void;
    start(rom: string): Promise<void>;
    stop(): void;
    onKeyDown(event: KeyboardEvent): any;
    onKeyUp(event: KeyboardEvent): any;
}

declare namespace ZX81Emulator
{
    export const enum StatusKind { OK, Info, Warning, Error }

    export interface Status
    {
        status(message: string, kind: StatusKind, previousKind: StatusKind): void;
    }
}

export default ZX81Emulator;

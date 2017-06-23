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

namespace zx81emulator.main
{
    import ZX81Config = zx81emulator.config.ZX81Config;

    import ZX81ConfigDefs = zx81emulator.config.ZX81ConfigDefs;

    import AccDraw = zx81emulator.display.AccDraw;

    import KBStatus = zx81emulator.io.KBStatus;

    import ZX81 = zx81emulator.zx81.ZX81;

    import IOException = java.io.IOException;

    export class Emulator
    {
        private mDisplayDrawer: AccDraw;

        private mKeyboard: KBStatus;

        private mConfig: ZX81Config;

        public static main(args: string[])
        {
            try
            {
                let canvas: HTMLCanvasElement = <HTMLCanvasElement>document.getElementById("canvas");
                if (canvas == null) throw new Error("No HTML element found with id \'canvas\'");
                let emulator: Emulator = new Emulator();
                emulator.init(args, canvas);
                emulator.installListeners(canvas);
                emulator.start();
            } catch (exc)
            {
                console.error("Error: " + exc);
                console.error(exc.message, exc);
            }
            ;
        }

        constructor()
        {
            this.mConfig = new ZX81Config();
            this.mConfig.machine = new ZX81();
            this.mConfig.load_config();
        }

        private init$java_lang_String_A$jsweet_dom_HTMLCanvasElement(args: string[], canvas: HTMLCanvasElement)
        {
            let tzxFileName: string = (args.length > 0 && !/* startsWith */((str, searchString, position = 0) => str.substr(position, searchString.length) === searchString)(args[0], "-")) ? args[0] : null;
            let scale: string = null;
            let hires: string = null;
            for (let aPos: number = tzxFileName == null ? 0 : 1; aPos < args.length; aPos++)
            {
                if ((args[aPos] === "-scale") && aPos < args.length - 1)
                {
                    scale = args[++aPos];
                }
                if ((args[aPos] === "-hires") && aPos < args.length - 1)
                {
                    hires = args[++aPos];
                }
            }
            this.init(tzxFileName, hires, scale, canvas);
        }

        public init(tzxFileName?: any, hires?: any, scale?: any, canvas?: any): any
        {
            if (((typeof tzxFileName === 'string') || tzxFileName === null) && ((typeof hires === 'string') || hires === null) && ((typeof scale === 'string') || scale === null) && ((canvas != null && canvas instanceof HTMLCanvasElement) || canvas === null))
            {
                let __args = Array.prototype.slice.call(arguments);
                return <any>(() =>
                {
                    this.mConfig.machine.CurRom = this.mConfig.zx81opts.ROM81;
                    let scaleCanvas: number = 2;
                    if (scale != null && scale.length > 0) scaleCanvas = javaemul.internal.IntegerHelper.parseInt(scale);
                    if (("qs" === hires)) this.mConfig.zx81opts.chrgen = ZX81ConfigDefs.CHRGENQS; else if (("dk" === hires)) this.mConfig.zx81opts.chrgen = ZX81ConfigDefs.CHRGENDK;
                    this.mConfig.machine.initialise(this.mConfig);
                    this.mKeyboard = new KBStatus();
                    this.mDisplayDrawer = new AccDraw(this.mConfig, scaleCanvas, canvas);
                    if (tzxFileName != null)
                    {
                        let tzxEntry: string;
                        let entryNum: number = 0;
                        let atPos: number = tzxFileName.indexOf('@');
                        if (atPos !== -1)
                        {
                            tzxEntry = tzxFileName.substring(atPos + 1);
                            tzxFileName = tzxFileName.substring(0, atPos);
                            entryNum = javaemul.internal.IntegerHelper.parseInt(tzxEntry);
                        }
                        this.mConfig.machine.getTape().loadTZX(this.mConfig, this.mKeyboard, tzxFileName, entryNum);
                    }
                })();
            } else if (((tzxFileName != null && tzxFileName instanceof Array) || tzxFileName === null) && ((hires != null && hires instanceof HTMLCanvasElement) || hires === null) && scale === undefined && canvas === undefined)
            {
                return <any>this.init$java_lang_String_A$jsweet_dom_HTMLCanvasElement(tzxFileName, hires);
            } else throw new Error('invalid overload');
        }

        public start()
        {
            this.mDisplayDrawer.start();
        }

        /**
         * Stops the applet.
         */
        public stop()
        {
            this.mDisplayDrawer.stop();
        }

        private installListeners(container: HTMLElement)
        {
            container.addEventListener("keydown", (event) =>
            {
                this.onKeyDown(event);
                return null;
            }, true);
            container.addEventListener("keyup", (event) =>
            {
                this.onKeyUp(event);
                return null;
            }, true);
        }

        private onKeyDown(e: KeyboardEvent)
        {
            e.preventDefault();
            this.mKeyboard.PCKeyDown(e.key, e.shiftKey, e.ctrlKey, e.altKey);
        }

        private onKeyUp(e: KeyboardEvent)
        {
            e.preventDefault();
            this.mKeyboard.PCKeyUp(e.key, e.shiftKey, e.ctrlKey, e.altKey);
        }

        private windowActive(active: boolean)
        {
            this.mDisplayDrawer.setPaused(!active);
        }
    }
    Emulator["__class"] = "zx81emulator.main.Emulator";

}


zx81emulator.main.Emulator.main(null);

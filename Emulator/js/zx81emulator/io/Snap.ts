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

namespace zx81emulator.io
{
    import ZX81Config = zx81emulator.config.ZX81Config;
    import Z80 = zx81emulator.z80.Z80;
    import ZX81 = zx81emulator.zx81.ZX81;

    export class Snap
    {
        private mConfig: ZX81Config;

        public constructor(config: ZX81Config)
        {
            this.mConfig = config;
        }

        private get_token(f: InputStream): string
        {
            let buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
            let c: number = f.read();
            while ((c !== -1 && javaemul.internal.CharacterHelper.isWhitespace(c)))c = f.read();
            buffer.append(c);
            c = f.read();
            while ((c !== -1 && !javaemul.internal.CharacterHelper.isWhitespace(c)))
            {
                buffer.append(c);
                c = f.read();
            }
            ;
            return buffer.toString();
        }

        private hex2dec(str: string): number
        {
            let num: number;
            num = 0;
            let pos: number = 0;
            while ((pos < str.length))
            {
                num = num * 16;
                let ch: string = str.charAt(pos);
                if ((ch).charCodeAt(0) >= ('0').charCodeAt(0) && (ch).charCodeAt(0) <= ('9').charCodeAt(0)) num += (ch).charCodeAt(0) - ('0').charCodeAt(0); else if ((ch).charCodeAt(0) >= ('a').charCodeAt(0) && (ch).charCodeAt(0) <= ('f').charCodeAt(0)) num += (ch).charCodeAt(0) + 10 - ('a').charCodeAt(0); else if ((ch).charCodeAt(0) >= ('A').charCodeAt(0) && (ch).charCodeAt(0) <= ('F').charCodeAt(0)) num += (ch).charCodeAt(0) + 10 - ('A').charCodeAt(0); else return (num);
                pos++;
            }
            ;
            return (num);
        }

        private load_snap_cpu(f: InputStream, z80: Z80)
        {
            let tok: string;
            while ((f.available() > 0))
            {
                tok = this.get_token(f);
                if ((tok === "[MEMORY]"))
                {
                    this.load_snap_mem(f, z80);
                    return;
                }
                if ((tok === "[ZX81]"))
                {
                    this.load_snap_zx81(f, z80);
                    return;
                }
                if ((tok === "PC")) z80.PC = this.hex2dec(this.get_token(f));
                if ((tok === "SP")) z80.SP = this.hex2dec(this.get_token(f));
                if ((tok === "AF")) z80.AF.set(this.hex2dec(this.get_token(f)));
                if ((tok === "HL_")) z80.HL_ = this.hex2dec(this.get_token(f));
                if ((tok === "DE_")) z80.DE_ = this.hex2dec(this.get_token(f));
                if ((tok === "BC_")) z80.BC_ = this.hex2dec(this.get_token(f));
                if ((tok === "AF_")) z80.AF_ = this.hex2dec(this.get_token(f));
                if ((tok === "IX")) z80.IX.set(this.hex2dec(this.get_token(f)));
                if ((tok === "IY")) z80.IY.set(this.hex2dec(this.get_token(f)));
                if ((tok === "IM")) z80.IM = this.hex2dec(this.get_token(f));
                if ((tok === "IF1")) z80.IFF1 = this.hex2dec(this.get_token(f));
                if ((tok === "IF2")) z80.IFF2 = this.hex2dec(this.get_token(f));
                if ((tok === "HT")) z80.halted = this.hex2dec(this.get_token(f));
                if ((tok === "IR"))
                {
                    let a: number;
                    a = this.hex2dec(this.get_token(f));
                    z80.I = (a >> 8) & 255;
                    z80.R = a & 255;
                    z80.R7 = a & 128;
                }
            }
            ;
        }

        private load_snap_zx81(f: InputStream, z80: Z80)
        {
            let tok: string;
            while ((f.available() > 0))
            {
                tok = this.get_token(f);
                if ((tok === "[MEMORY]"))
                {
                    this.load_snap_mem(f, z80);
                    return;
                }
                if ((tok === "[CPU]"))
                {
                    this.load_snap_cpu(f, z80);
                    return;
                }
                if ((tok === "NMI")) (<ZX81>this.mConfig.machine).NMI_generator = this.hex2dec(this.get_token(f)) > 0;
                if ((tok === "HSYNC")) (<ZX81>this.mConfig.machine).HSYNC_generator = this.hex2dec(this.get_token(f)) > 0;
                if ((tok === "ROW")) (<ZX81>this.mConfig.machine).rowcounter = this.hex2dec(this.get_token(f));
            }
            ;
        }

        private load_snap_mem(f: InputStream, z80: Z80)
        {
            let Addr: number;
            let Count: number;
            let Chr: number;
            let tok: string;
            Addr = 16384;
            while ((f.available() > 0))
            {
                tok = this.get_token(f);
                if ((tok === "[CPU]"))
                {
                    this.load_snap_cpu(f, z80);
                    return;
                } else if ((tok === "[ZX81]"))
                {
                    this.load_snap_zx81(f, z80);
                    return;
                } else if ((tok === "MEMRANGE"))
                {
                    Addr = this.hex2dec(this.get_token(f));
                    this.get_token(f);
                } else if (tok.charAt(0) === '*')
                {
                    Count = this.hex2dec(tok + 1);
                    Chr = this.hex2dec(this.get_token(f));
                    while ((Count-- > 0))this.mConfig.machine.memory[Addr++] = Chr;
                } else this.mConfig.machine.memory[Addr++] = this.hex2dec(tok);
            }
            ;
        }

        public memory_load(filename: string, address: number, length: number, callback: Callback)
        {
            let resource: Resource = new Resource();
            resource.get(filename, (() =>
            {
                let f = function ()
                {
                    this.call = () =>
                    {
                    }
                };
                return new f();
            })());
        }
    }
}


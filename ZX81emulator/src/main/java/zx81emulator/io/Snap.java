/* ZX81emulator  - A ZX81 emulator.
 * EightyOne Copyright (C) 2003-2006 Michael D Wynne
 * JtyOne Java translation (C) 2006 Simon Holdsworth and others.
 * ZX81emulator Javascript JSweet transcompilation (C) 2017 Sebastien Andrivet.
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package zx81emulator.io;

import zx81emulator.config.ZX81Config;
import zx81emulator.z80.Z80;
import zx81emulator.zx81.ZX81;
import java.io.IOException;
import java.io.InputStream;


public class Snap {
    private ZX81Config mConfig;

    public Snap(ZX81Config config) {
        mConfig = config;
    }

    private String get_token(InputStream f)
            throws IOException {
        StringBuilder buffer = new StringBuilder();

        int c = f.read();
        while (c != -1 && Character.isWhitespace(c))
            c = f.read();

        buffer.append(c);

        c = f.read();
        while (c != -1 && !Character.isWhitespace(c)) {
            buffer.append(c);
            c = f.read();
        }

        return buffer.toString();
    }

    private int hex2dec(String str) {
        int num;

        num = 0;
        int pos = 0;
        while (pos < str.length()) {
            num = num * 16;
            char ch = str.charAt(pos);
            if (ch >= '0' && ch <= '9') num += ch - '0';
            else if (ch >= 'a' && ch <= 'f') num += ch + 10 - 'a';
            else if (ch >= 'A' && ch <= 'F') num += ch + 10 - 'A';
            else return (num);
            pos++;
        }
        return (num);
    }

    private void load_snap_cpu(InputStream f, Z80 z80)
            throws IOException {
        String tok;

        while (f.available() > 0) {
            tok = get_token(f);
            if (tok.equals("[MEMORY]")) {
                load_snap_mem(f, z80);
                return;
            }
            if (tok.equals("[ZX81]")) {
                load_snap_zx81(f, z80);
                return;
            }

            if (tok.equals("PC")) z80.PC = hex2dec(get_token(f));
            if (tok.equals("SP")) z80.SP = hex2dec(get_token(f));
            // TODO: if (tok.equals("HL")) z80.HL.set(hex2dec(get_token(f)));
            // TODO: if (tok.equals("DE")) z80.DE.set(hex2dec(get_token(f)));
            // TODO: if (tok.equals("BC")) z80.BC.set(hex2dec(get_token(f)));
            if (tok.equals("AF")) z80.AF.set(hex2dec(get_token(f)));
            if (tok.equals("HL_")) z80.HL_ = hex2dec(get_token(f));
            if (tok.equals("DE_")) z80.DE_ = hex2dec(get_token(f));
            if (tok.equals("BC_")) z80.BC_ = hex2dec(get_token(f));
            if (tok.equals("AF_")) z80.AF_ = hex2dec(get_token(f));
            if (tok.equals("IX")) z80.IX.set(hex2dec(get_token(f)));
            if (tok.equals("IY")) z80.IY.set(hex2dec(get_token(f)));
            if (tok.equals("IM")) z80.IM = hex2dec(get_token(f));
            if (tok.equals("IF1")) z80.IFF1 = hex2dec(get_token(f));
            if (tok.equals("IF2")) z80.IFF2 = hex2dec(get_token(f));
            if (tok.equals("HT")) z80.halted = hex2dec(get_token(f));

            if (tok.equals("IR")) {
                int a;

                a = hex2dec(get_token(f));

                z80.I = (a >> 8) & 0xff;
                z80.R = a & 0xff;
                z80.R7 = a & 0x80;
            }
        }
    }

    private void load_snap_zx81(InputStream f, Z80 z80)
            throws IOException {
        String tok;

        while (f.available() > 0) {
            tok = get_token(f);
            if (tok.equals("[MEMORY]")) {
                load_snap_mem(f, z80);
                return;
            }
            if (tok.equals("[CPU]")) {
                load_snap_cpu(f, z80);
                return;
            }

            if (tok.equals("NMI")) ((ZX81) mConfig.machine).NMI_generator = hex2dec(get_token(f)) > 0;
            if (tok.equals("HSYNC")) ((ZX81) mConfig.machine).HSYNC_generator = hex2dec(get_token(f)) > 0;
            if (tok.equals("ROW")) ((ZX81) mConfig.machine).rowcounter = hex2dec(get_token(f));
        }
    }

    private void load_snap_mem(InputStream f, Z80 z80)
            throws IOException {
        int Addr, Count, Chr;
        String tok;

        Addr = 16384;

        while (f.available() > 0) {
            tok = get_token(f);

            if (tok.equals("[CPU]")) {
                load_snap_cpu(f, z80);
                return;
            } else if (tok.equals("[ZX81]")) {
                load_snap_zx81(f, z80);
                return;
            } else if (tok.equals("MEMRANGE")) {
                Addr = hex2dec(get_token(f));
                get_token(f);
            } else if (tok.charAt(0) == '*') {
                Count = hex2dec(tok + 1);
                Chr = hex2dec(get_token(f));
                while (Count-- > 0) mConfig.machine.memory[Addr++] = Chr;
            } else mConfig.machine.memory[Addr++] = hex2dec(tok);
        }
    }

    public int memory_load(String filename, int address, int length)
            throws IOException {

        InputStream is = Snap.class.getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new IOException("could not get resource: " + filename);
        }

        int val = is.read();
        int len = 0;
        while (len < length && val != -1) {
            mConfig.machine.memory[address++] = val;
            len++;
            val = is.read();
        }

        is.close();

        return (len);
    }

    public int font_load(String filename, int[] font, int length)
            throws IOException {

        InputStream is = Snap.class.getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new IOException("could not get resource: " + filename);
        }

        int val = is.read();
        int len = 0;
        while (len < length && val != -1) {
            font[len++] = val;
            val = is.read();
        }

        is.close();

        return (len);
    }
}
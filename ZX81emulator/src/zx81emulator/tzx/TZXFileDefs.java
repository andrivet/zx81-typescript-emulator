/* EightyOne  - A Windows ZX80/81/clone emulator.
 * Copyright (C) 2003-2006 Michael D Wynne
 * Java translation (C) 2006 Simon Holdsworth
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * tzxfile.h
 *
 */

package zx81emulator.tzx;

public class TZXFileDefs {

    public static final int TZX_MAX_BLOCKS = 2000;

    //TODO: public static float TZXSCALE(int x,int clockSpeed) { return (((float)x)/(((float)3500000)/((float)clockSpeed))); }

    public static final int TZX_BLOCK_ROM = 0x10;
    public static final int TZX_BLOCK_TURBO = 0x11;
    public static final int TZX_BLOCK_TONE = 0x12;
    public static final int TZX_BLOCK_PULSE = 0x13;
    public static final int TZX_BLOCK_DATA = 0x14;
    public static final int TZX_BLOCK_DREC = 0x15;
    public static final int TZX_BLOCK_CSW = 0x18;
    public static final int TZX_BLOCK_GENERAL = 0x19;
    public static final int TZX_BLOCK_PAUSE = 0x20;
    public static final int TZX_BLOCK_GSTART = 0x21;
    public static final int TZX_BLOCK_GEND = 0x22;
    public static final int TZX_BLOCK_JUMP = 0x23;
    public static final int TZX_BLOCK_LSTART = 0x24;
    public static final int TZX_BLOCK_LEND = 0x25;
    public static final int TZX_BLOCK_SBLOCK = 0x28;
    public static final int TZX_BLOCK_STOP48K = 0x2a;
    public static final int TZX_BLOCK_SETLEVEL = 0x2b;
    public static final int TZX_BLOCK_TEXT = 0x30;
    public static final int TZX_BLOCK_MESSAGE = 0x31;
    public static final int TZX_BLOCK_ARCHIVE = 0x32;
    public static final int TZX_BLOCK_HWTYPE = 0x33;
    public static final int TZX_BLOCK_CUSTOM = 0x35;
    public static final int TZX_BLOCK_GLUE = 0x5a;

    public static final String T81_HEADER_ID = "EO81";
    public static final int TZX_BYTE_EMPTY = -1;
}

class TZXHeader {
    byte[] id = new byte[8];
    int major;
    int minor;
}

class TZXUnknown extends TZXBlockInfo {
    int type;
    int length;
}

class TZXROM extends TZXBlockInfo {
    int DataLen;
}

class TZXTurbo extends TZXBlockInfo {
    int PilotLen;
    int Sync1Len;
    int Sync2Len;
    int Bit0Len;
    int Bit1Len;
    int PilotPulses;
    int FinalBits;
    int DataLen;
}

class TZXTone extends TZXBlockInfo {
    int PulseLen;
    int NoPulses;
}

class TZXPulse extends TZXBlockInfo {
    int NoPulses;
}

class TZXData extends TZXBlockInfo {
    int Len0;
    int Len1;
    int FinalBits;
    int DataLen;
}

class TZXDRec extends TZXBlockInfo {
    int SampleLen;
    int FinalBits;
    int Samples;
}

class TZXCSW extends TZXBlockInfo {
    int BlockLen;
    int SampleRate;
    int Compression;
    int Flags;
    int NoPulses;
}

class TZXGeneral extends TZXBlockInfo {
    int TOTP, NPP, ASP, TOTD, NPD, ASD;
    int DataLen;

    //int Flags, DataLen;
    //int PilotLen;
    //int PilotPulses;
    //int SyncPulses;
    //int MaxPulsesBit;
    //int NoSymbols;
    //int FinalBits;
}

class TZXPause extends TZXBlockInfo {
}

class TZXGStart extends TZXBlockInfo {
    int NameLen;
}

class TZXGEnd extends TZXBlockInfo {
};

class TZXJump extends TZXBlockInfo {
    int JumpRel;
}

class TZXLStart extends TZXBlockInfo {
    int Repeats;
}

class TZXLEend extends TZXBlockInfo {
}

class TZXSelect extends TZXBlockInfo {
    int Offset;
    int TextLen;
}

class TZXSBlock extends TZXBlockInfo {
    int BlockLen;
    int NoSelections;
}

class TZXStop48K extends TZXBlockInfo {
    int BlockLen;
}

class TZXSetLevel extends TZXBlockInfo {
    int Level;
}

class TZXText extends TZXBlockInfo {
    int TextLen;
}

class TZXMessage extends TZXBlockInfo {
    int Time;
    int TextLen;
}

class TZXArchiveText extends TZXBlockInfo {
    int TextID;
    int TextLen;
}

class TZXArchive extends TZXBlockInfo {
    int BlockLen;
    int NoStrings;
}

class TZXHWInfo extends TZXBlockInfo {
    int Type;
    int ID;
    int Information;
}

class TZXHWType extends TZXBlockInfo {
    int NoTypes;
}

class TZXCustom extends TZXBlockInfo {
    String IDString;
    int Length;
}

class TZXGlue extends TZXBlockInfo {
}

class TZXBlockInfo {
}

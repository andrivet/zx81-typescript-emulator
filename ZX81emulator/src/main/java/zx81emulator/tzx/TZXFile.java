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
 * along with ZX81emulator.  If not, see <http://www.gnu.org/licenses/>.
 */
package zx81emulator.tzx;

import java.io.IOException;
import java.io.InputStream;

public class TZXFile
        extends TZXFileDefs {

    private static final String TZX_ID = "ZXTape!\032";

    public static TZXBlock[] Tape = new TZXBlock[TZX_MAX_BLOCKS];

    static {
        for (int i = 0; i < Tape.length; i++)
            Tape[i] = new TZXBlock();
    }

    public static int Blocks;
    private static int CurBlock;

    private static void EraseAll() {
        int i;
        for (i = 0; i < Blocks; i++) EraseBlock(i);
        Blocks = CurBlock = 0;
    }

    private static void EraseBlock(int BlockNo) {

        Tape[BlockNo].BlockID = 0;

        if (Tape[BlockNo].Data.Pulses != null) {
            Tape[BlockNo].Data.Pulses = null;
        }

        if (Tape[BlockNo].SymDefP != null) {
            Tape[BlockNo].SymDefP = null;
        }

        if (Tape[BlockNo].SymDefD != null) {
            Tape[BlockNo].SymDefD = null;
        }

        if (Tape[BlockNo].PRLE != null) {
            Tape[BlockNo].PRLE = null;
        }
    }

    private static void DeleteBlock(int Block) {
        int i;

        if (Block >= Blocks) return;
        EraseBlock(Block);

        for (i = Block; i < Blocks; i++)
            Tape[i] = Tape[i + 1];

        Blocks--;
    }

    private static void InsertBlock(int Position) {
        int i;
        i = Blocks;

        while (i >= Position) {
            Tape[i + 1] = Tape[i];
            i--;
        }

        Tape[Position] = new TZXBlock();
        if (Position <= CurBlock) CurBlock++;
        Blocks++;
    }

    private static int ReadByte(InputStream f)
            throws IOException {
        return f.read();
    }

    private static int ReadWord(InputStream f)
            throws IOException {
        return f.read() + (f.read() << 8);
    }

    private static int ReadDWord(InputStream f)
            throws IOException {
        return f.read() +
                (f.read() << 8) +
                (f.read() << 16) +
                (f.read() << 24);
    }

    private static int Read3Bytes(InputStream f)
            throws IOException {
        return f.read() +
                (f.read() << 8) +
                (f.read() << 16);
    }

    private static void ReadBytes(InputStream f, int len, byte[] buf)
            throws IOException {
        f.read(buf, 0, len);
    }

    private static void ReadWords(InputStream f, int len, int[] buf)
            throws IOException {
        for (int i = 0; i < len; i++)
            buf[i] = ReadWord(f);
    }

    private static boolean LoadOldGeneralBlock(InputStream f)
            throws IOException {
        int bl, flags, pl, pp, ns, np, as, pause;
        int datalen;
        int i;

        int[] SymDef =
                {3, 530, 520, 530, 520, 530, 520, 530, 4689,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        3, 530, 520, 530, 520, 530, 520, 530, 520, 530,
                        520, 530, 520, 530, 520, 530, 520, 530, 4689};

        int[] SymDefD = new int[2 * 19];
        System.arraycopy(SymDef, 0, SymDefD, 0, SymDefD.length);

        bl = ReadDWord(f);
        flags = ReadByte(f);
        pl = ReadWord(f);
        pp = ReadWord(f);
        ns = ReadByte(f);

        if ((flags != 0) && (flags != 1)) {
            return (true);
        }
        if (pl != 0) {
            return (true);
        }
        if (pp != 0) {
            return (true);
        }
        if (ns != 0) {
            return (true);
        }

        np = ReadByte(f);
        as = ReadByte(f);

        if (as != 2) {
            return (true);
        }

        for (i = 0; i < (np * as); i++)
            ReadWord(f);

        ReadByte(f); // usedbits
        pause = ReadWord(f);

        datalen = bl - (11 + 2 * (ns + np * as));
        byte[] data = new byte[datalen];
        ReadBytes(f, datalen, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_GENERAL;
        Tape[CurBlock].Pause = pause;
        Tape[CurBlock].Head = new TZXGeneral();
        ((TZXGeneral) Tape[CurBlock].Head).TOTP = 0;
        ((TZXGeneral) Tape[CurBlock].Head).NPP = 0;
        ((TZXGeneral) Tape[CurBlock].Head).ASP = 0;
        ((TZXGeneral) Tape[CurBlock].Head).TOTD = datalen * 8;
        ((TZXGeneral) Tape[CurBlock].Head).NPD = 19;
        ((TZXGeneral) Tape[CurBlock].Head).ASD = 2;
        ((TZXGeneral) Tape[CurBlock].Head).DataLen = datalen;

        Tape[CurBlock].SymDefD = SymDefD;
        Tape[CurBlock].Data.Data = data;
        Tape[CurBlock].SymDefP = null;
        Tape[CurBlock].PRLE = null;

        return (false);
    }

    private static boolean LoadGeneralBlock(InputStream f)
            throws IOException {
        int[] SymDefP, SymDefD = null, PRLE;
        byte[] Data;
        int Pause;
        int TOTP, NPP, ASP, TOTD, NPD, ASD;
        int bits, bytes = 0;
        int i, j, k;

        f.mark(65536);

        if (!LoadOldGeneralBlock(f)) return (false);

        f.reset();

        ReadDWord(f); // DataLen
        Pause = ReadWord(f);
        TOTP = ReadDWord(f);
        NPP = ReadByte(f) + 1;
        ASP = ReadByte(f);
        if (ASP == 0 && TOTP > 0) ASP = 256;

        if (TOTP == 0) {
            NPP = 0;
            ASP = 0;
        }

        TOTD = ReadDWord(f);
        NPD = ReadByte(f) + 1;
        ASD = ReadByte(f);
        if (ASD == 0 && TOTP > 0) ASD = 256;

        if (TOTD == 0) {
            NPD = 0;
            ASD = 0;
        }

        if (TOTP > 0) {
            SymDefP = new int[ASP * NPP];
            PRLE = new int[2 * TOTP];

            for (i = 0; i < (ASP); i++) {
                SymDefP[i * NPP] = ReadByte(f);
                for (j = 0; j < (NPP - 1); j++) {
                    k = ReadWord(f);
                    SymDefP[i * NPP + j + 1] = k;
                }
            }
            for (i = 0; i < TOTP; i++) {
                PRLE[i * 2] = ReadByte(f);
                PRLE[i * 2 + 1] = ReadWord(f);
            }
        } else {
            SymDefP = null;
            PRLE = null;
        }

        if (TOTD > 0) {

            i = 1;
            bits = 0;
            while (i < ASD) {
                i <<= 1;
                bits++;
            }

            bits = bits * TOTD;
            bytes = bits / 8;
            if ((bytes * 8) < bits) bytes++;

            SymDefD = new int[ASD * NPD];

            for (i = 0; i < (ASD); i++) {
                SymDefD[i * NPD] = ReadByte(f);
                for (j = 0; j < (NPD - 1); j++) {
                    k = ReadWord(f);
                    SymDefD[i * NPD + j + 1] = k;
                }
            }


            Data = new byte[bytes];
            ReadBytes(f, bytes, Data);
        } else Data = null;

        Tape[CurBlock].BlockID = TZX_BLOCK_GENERAL;
        Tape[CurBlock].Pause = Pause;
        Tape[CurBlock].Head = new TZXGeneral();
        ((TZXGeneral) Tape[CurBlock].Head).TOTP = TOTP;
        ((TZXGeneral) Tape[CurBlock].Head).NPP = NPP;
        ((TZXGeneral) Tape[CurBlock].Head).ASP = ASP;
        ((TZXGeneral) Tape[CurBlock].Head).TOTD = TOTD;
        ((TZXGeneral) Tape[CurBlock].Head).NPD = NPD;
        ((TZXGeneral) Tape[CurBlock].Head).ASD = ASD;
        ((TZXGeneral) Tape[CurBlock].Head).DataLen = bytes;

        Tape[CurBlock].Data.Data = Data;
        Tape[CurBlock].SymDefP = SymDefP;
        Tape[CurBlock].SymDefD = SymDefD;
        Tape[CurBlock].PRLE = PRLE;

        return (false);
    }


    private static boolean LoadROMBlock(InputStream f)
            throws IOException {
        int length;
        int pause;
        byte[] data;

        pause = ReadWord(f);
        length = ReadWord(f);
        data = new byte[length];
        ReadBytes(f, length, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_ROM;
        Tape[CurBlock].Data.Data = data;
        Tape[CurBlock].Head = new TZXROM();
        ((TZXROM) Tape[CurBlock].Head).DataLen = length;
        Tape[CurBlock].Pause = pause;

        return (false);
    }

    private static boolean LoadTurboBlock(InputStream f)
            throws IOException {
        int datalen, lp, ls1, ls2, l0, l1, lpt, usedbits, pause;
        byte[] data;

        lp = ReadWord(f);
        ls1 = ReadWord(f);
        ls2 = ReadWord(f);
        l0 = ReadWord(f);
        l1 = ReadWord(f);
        lpt = ReadWord(f);
        usedbits = ReadByte(f);
        pause = ReadWord(f);
        datalen = Read3Bytes(f);

        data = new byte[datalen];
        ReadBytes(f, datalen, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_TURBO;
        Tape[CurBlock].Data.Data = data;
        Tape[CurBlock].Head = new TZXTurbo();
        ((TZXTurbo) Tape[CurBlock].Head).PilotLen = lp;
        ((TZXTurbo) Tape[CurBlock].Head).Sync1Len = ls1;
        ((TZXTurbo) Tape[CurBlock].Head).Sync2Len = ls2;
        ((TZXTurbo) Tape[CurBlock].Head).Bit0Len = l0;
        ((TZXTurbo) Tape[CurBlock].Head).Bit1Len = l1;
        ((TZXTurbo) Tape[CurBlock].Head).PilotPulses = lpt;
        ((TZXTurbo) Tape[CurBlock].Head).FinalBits = usedbits;
        Tape[CurBlock].Pause = pause;
        ((TZXTurbo) Tape[CurBlock].Head).DataLen = datalen;

        return (false);
    }

    private static boolean LoadToneBlock(InputStream f)
            throws IOException {
        int pulselen, pulses;

        pulselen = ReadWord(f);
        pulses = ReadWord(f);

        Tape[CurBlock].BlockID = TZX_BLOCK_TONE;
        Tape[CurBlock].Head = new TZXTone();
        ((TZXTone) Tape[CurBlock].Head).PulseLen = pulselen;
        ((TZXTone) Tape[CurBlock].Head).NoPulses = pulses;

        return (false);
    }

    private static boolean LoadPulseBlock(InputStream f)
            throws IOException {
        int nopulses;
        int[] pulses;

        nopulses = ReadByte(f);
        pulses = new int[nopulses * 2];
        ReadWords(f, nopulses * 2, pulses);

        Tape[CurBlock].BlockID = TZX_BLOCK_PULSE;
        Tape[CurBlock].Head = new TZXPulse();
        Tape[CurBlock].Data.Pulses = pulses;
        ((TZXPulse) Tape[CurBlock].Head).NoPulses = nopulses;

        return (false);
    }

    private static boolean LoadDataBlock(InputStream f)
            throws IOException {
        int datalen, len0, len1, usedbits, pause;
        byte[] data;

        len0 = ReadWord(f);
        len1 = ReadWord(f);
        usedbits = ReadByte(f);
        pause = ReadWord(f);
        datalen = Read3Bytes(f);
        data = new byte[datalen];
        ReadBytes(f, datalen, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_DATA;
        Tape[CurBlock].Head = new TZXData();
        Tape[CurBlock].Data.Data = data;
        ((TZXData) Tape[CurBlock].Head).Len0 = len0;
        ((TZXData) Tape[CurBlock].Head).Len1 = len1;
        ((TZXData) Tape[CurBlock].Head).FinalBits = usedbits;
        Tape[CurBlock].Pause = pause;
        ((TZXData) Tape[CurBlock].Head).DataLen = datalen;

        return (false);
    }

    private static boolean LoadDRecBlock(InputStream f)
            throws IOException {
        int samplelen, pause, usedbits, datalen;
        byte[] data;

        samplelen = ReadWord(f);
        pause = ReadWord(f);
        usedbits = ReadByte(f);
        datalen = Read3Bytes(f);

        data = new byte[datalen];
        ReadBytes(f, datalen, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_DREC;
        Tape[CurBlock].Head = new TZXDRec();
        Tape[CurBlock].Data.Data = data;
        ((TZXDRec) Tape[CurBlock].Head).SampleLen = samplelen;
        Tape[CurBlock].Pause = pause;
        ((TZXDRec) Tape[CurBlock].Head).FinalBits = usedbits;
        ((TZXDRec) Tape[CurBlock].Head).Samples = datalen;

        return (false);
    }

    private static boolean LoadCSWBlock(InputStream f)
            throws IOException {
        int datalen, pause, samplerate, compression, flags, nopulses;
        byte[] data;

        datalen = ReadDWord(f) - 11;
        pause = ReadWord(f);
        samplerate = Read3Bytes(f);
        compression = ReadByte(f);
        flags = ReadByte(f);
        nopulses = ReadDWord(f);

        data = new byte[datalen];
        ReadBytes(f, datalen, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_CSW;
        Tape[CurBlock].Head = new TZXCSW();
        Tape[CurBlock].Data.Data = data;
        ((TZXCSW) Tape[CurBlock].Head).BlockLen = datalen;
        Tape[CurBlock].Pause = pause;
        ((TZXCSW) Tape[CurBlock].Head).SampleRate = samplerate;
        ((TZXCSW) Tape[CurBlock].Head).Compression = compression;
        ((TZXCSW) Tape[CurBlock].Head).Flags = flags;
        ((TZXCSW) Tape[CurBlock].Head).NoPulses = nopulses;

        return (false);
    }

    private static boolean LoadPauseBlock(InputStream f)
            throws IOException {
        int pause;

        pause = ReadWord(f);
        Tape[CurBlock].BlockID = TZX_BLOCK_PAUSE;
        Tape[CurBlock].Pause = pause;

        return (false);
    }

    private static boolean LoadGStartBlock(InputStream f)
            throws IOException {
        int length;
        byte[] data;

        length = ReadByte(f);
        data = new byte[length];
        ReadBytes(f, length, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_GSTART;
        Tape[CurBlock].Head = new TZXGStart();
        Tape[CurBlock].Data.Data = data;
        ((TZXGStart) Tape[CurBlock].Head).NameLen = length;

        return (false);
    }

    private static boolean LoadGEndBlock()
            throws IOException {
        Tape[CurBlock].BlockID = TZX_BLOCK_GEND;
        return (false);
    }

    private static boolean LoadJumpBlock(InputStream f)
            throws IOException {
        int jump;

        jump = ReadWord(f);
        Tape[CurBlock].BlockID = TZX_BLOCK_JUMP;
        Tape[CurBlock].Head = new TZXJump();
        ((TZXJump) Tape[CurBlock].Head).JumpRel = jump;

        return (false);
    }

    private static boolean LoadLStartBlock(InputStream f)
            throws IOException {
        int repeats;

        repeats = ReadWord(f);

        Tape[CurBlock].BlockID = TZX_BLOCK_LSTART;
        Tape[CurBlock].Head = new TZXLStart();
        ((TZXLStart) Tape[CurBlock].Head).Repeats = repeats;

        return (false);
    }

    private static boolean LoadLEndBlock()
            throws IOException {
        Tape[CurBlock].BlockID = TZX_BLOCK_LEND;
        return (false);
    }

    private static boolean LoadSBlock(InputStream f)
            throws IOException {
        int length, selections;
        byte[] data;

        length = ReadWord(f) - 1;
        selections = ReadByte(f);
        data = new byte[length];
        ReadBytes(f, length, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_SBLOCK;
        Tape[CurBlock].Head = new TZXSBlock();
        Tape[CurBlock].Data.Data = data;
        ((TZXSBlock) Tape[CurBlock].Head).BlockLen = length;
        ((TZXSBlock) Tape[CurBlock].Head).NoSelections = selections;

        return (false);
    }

    private static boolean LoadStop48KBlock(InputStream f)
            throws IOException {
        ReadDWord(f);
        Tape[CurBlock].BlockID = TZX_BLOCK_STOP48K;

        return (false);
    }

    private static boolean LoadSetLevelBlock(InputStream f)
            throws IOException {
        int level;

        ReadDWord(f);
        level = ReadByte(f);

        Tape[CurBlock].BlockID = TZX_BLOCK_SETLEVEL;
        Tape[CurBlock].Head = new TZXSetLevel();
        ((TZXSetLevel) Tape[CurBlock].Head).Level = level;

        return (false);
    }

    private static boolean LoadTextBlock(InputStream f)
            throws IOException {
        int length;
        byte[] data;

        length = ReadByte(f);
        data = new byte[length];
        ReadBytes(f, length, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_TEXT;
        Tape[CurBlock].Head = new TZXText();
        Tape[CurBlock].Data.Data = data;
        ((TZXText) Tape[CurBlock].Head).TextLen = length;

        return (false);
    }

    private static boolean LoadMessageBlock(InputStream f)
            throws IOException {
        int length, time;
        byte[] data;

        time = ReadByte(f);
        length = ReadByte(f);
        data = new byte[length];
        ReadBytes(f, length, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_MESSAGE;
        Tape[CurBlock].Head = new TZXMessage();
        Tape[CurBlock].Data.Data = data;
        ((TZXMessage) Tape[CurBlock].Head).TextLen = length;
        ((TZXMessage) Tape[CurBlock].Head).Time = time;

        return (false);
    }

    private static boolean LoadArchiveBlock(InputStream f)
            throws IOException {
        int length, strings;
        byte[] data;

        length = ReadWord(f) - 1;
        data = new byte[length];
        strings = ReadByte(f);
        ReadBytes(f, length, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_ARCHIVE;
        Tape[CurBlock].Head = new TZXArchive();
        Tape[CurBlock].Data.Data = data;
        ((TZXArchive) Tape[CurBlock].Head).BlockLen = length;
        ((TZXArchive) Tape[CurBlock].Head).NoStrings = strings;

        return (false);
    }

    private static boolean LoadHWTypeBlock(InputStream f)
            throws IOException {
        int blocks, i;
        TZXHWInfo[] data;
        int p;

        blocks = ReadByte(f);
        data = new TZXHWInfo[blocks];
        p = 0;

        for (i = 0; i < blocks; i++) {
            data[p].Type = ReadByte(f);
            data[p].ID = ReadByte(f);
            data[p].Information = ReadByte(f);
            p++;
        }

        Tape[CurBlock].BlockID = TZX_BLOCK_HWTYPE;
        Tape[CurBlock].Head = new TZXHWType();
        Tape[CurBlock].Data.HWTypes = data;
        ((TZXHWType) Tape[CurBlock].Head).NoTypes = blocks;

        return (false);
    }

    private static boolean LoadCustomBlock(InputStream f)
            throws IOException {
        byte[] data, id = new byte[17];
        int len;

        ReadBytes(f, 16, id);
        id[16] = '\0';

        len = ReadDWord(f);
        data = new byte[len];
        ReadBytes(f, len, data);

        Tape[CurBlock].BlockID = TZX_BLOCK_CUSTOM;
        Tape[CurBlock].Head = new TZXCustom();
        ((TZXCustom) Tape[CurBlock].Head).IDString = new String(id);
        Tape[CurBlock].Data.Data = data;
        ((TZXCustom) Tape[CurBlock].Head).Length = len;

        return (false);
    }

    private static boolean LoadGlueBlock(InputStream f)
            throws IOException {
        Tape[CurBlock].BlockID = TZX_BLOCK_GLUE;
        ReadDWord(f);
        ReadDWord(f);
        return (false);
    }

    private static boolean LoadUnknownBlock(InputStream f, int BlockID)
            throws IOException {
        int length;
        byte[] data;

        length = ReadDWord(f);
        data = new byte[length];
        ReadBytes(f, length, data);

        Tape[CurBlock].BlockID = 0;
        Tape[CurBlock].Head = new TZXUnknown();
        Tape[CurBlock].Data.Data = data;
        ((TZXUnknown) Tape[CurBlock].Head).type = BlockID;
        ((TZXUnknown) Tape[CurBlock].Head).length = length;

        return (false);
    }

    public static boolean LoadFile(InputStream f, boolean Insert)
            throws IOException {
        TZXHeader head = new TZXHeader();
        boolean error;
        int BlockID;

        ReadBytes(f, 8, head.id);
        head.major = ReadByte(f);
        head.minor = ReadByte(f);

        if (!new String(head.id).equals(TZX_ID)) {
            return (false);
        }

        if (!Insert) EraseAll();
        error = false;

        while (f.available() > 0 && !error) {
            BlockID = ReadByte(f);

            if (Insert) {
                InsertBlock(CurBlock);
                CurBlock--;
            }
            EraseBlock(CurBlock);

            switch (BlockID) {
                case TZX_BLOCK_ROM:
                    error = LoadROMBlock(f);
                    break;
                case TZX_BLOCK_TURBO:
                    error = LoadTurboBlock(f);
                    break;
                case TZX_BLOCK_TONE:
                    error = LoadToneBlock(f);
                    break;
                case TZX_BLOCK_PULSE:
                    error = LoadPulseBlock(f);
                    break;
                case TZX_BLOCK_DATA:
                    error = LoadDataBlock(f);
                    break;
                case TZX_BLOCK_DREC:
                    error = LoadDRecBlock(f);
                    break;
                case TZX_BLOCK_CSW:
                    error = LoadCSWBlock(f);
                    break;
                case TZX_BLOCK_GENERAL:
                    error = LoadGeneralBlock(f);
                    break;
                case TZX_BLOCK_PAUSE:
                    error = LoadPauseBlock(f);
                    break;
                case TZX_BLOCK_GSTART:
                    error = LoadGStartBlock(f);
                    break;
                case TZX_BLOCK_GEND:
                    error = LoadGEndBlock();
                    break;
                case TZX_BLOCK_JUMP:
                    error = LoadJumpBlock(f);
                    break;
                case TZX_BLOCK_LSTART:
                    error = LoadLStartBlock(f);
                    break;
                case TZX_BLOCK_LEND:
                    error = LoadLEndBlock();
                    break;
                case TZX_BLOCK_SBLOCK:
                    error = LoadSBlock(f);
                    break;
                case TZX_BLOCK_STOP48K:
                    error = LoadStop48KBlock(f);
                    break;
                case TZX_BLOCK_SETLEVEL:
                    error = LoadSetLevelBlock(f);
                    break;
                case TZX_BLOCK_TEXT:
                    error = LoadTextBlock(f);
                    break;
                case TZX_BLOCK_MESSAGE:
                    error = LoadMessageBlock(f);
                    break;
                case TZX_BLOCK_ARCHIVE:
                    error = LoadArchiveBlock(f);
                    break;
                case TZX_BLOCK_HWTYPE:
                    error = LoadHWTypeBlock(f);
                    break;
                case TZX_BLOCK_CUSTOM:
                    error = LoadCustomBlock(f);
                    break;
                case TZX_BLOCK_GLUE:
                    error = LoadGlueBlock(f);
                    break;
                case 0xf0:
                case 0:
                    error = true;
                    break;
                default:
                    error = LoadUnknownBlock(f, BlockID);
                    break;
            }

            if (error) {
                if (Insert) DeleteBlock(CurBlock);
            } else {
                CurBlock++;
                if (!Insert) Blocks++;
            }
        }

        GroupCount();
        return (true);
    }

    private static void GroupCount() {
        int i;

        int GroupCount = 0;

        for (i = 0; i < Blocks; i++) {
            if (Tape[i].BlockID == TZX_BLOCK_GEND
                    || Tape[i].BlockID == TZX_BLOCK_LEND)
                GroupCount--;

            Tape[i].Group = GroupCount;

            if (Tape[i].BlockID == TZX_BLOCK_GSTART
                    || Tape[i].BlockID == TZX_BLOCK_LSTART)
                GroupCount++;
        }
    }
}

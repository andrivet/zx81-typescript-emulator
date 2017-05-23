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
 *
 * tzxfile.cpp
 */

package jtyone.tzx;

import java.io.IOException;
import java.io.InputStream;

public class TZXFile
extends TZXFileDefs
  {
  public static int TZXByte;
  public static int TZXEventCounter;

  static final String TZX_ID = "ZXTape!\032";

  public static TZXBlock[] Tape = new TZXBlock[TZX_MAX_BLOCKS];
  static {
    for( int i = 0; i < Tape.length; i++ )
      Tape[i] = new TZXBlock();
  }
  
  static String FileName;
  public static int Blocks, CurBlock;
  static int CurBlockLen, CurBlockProgress;
  static int Pause;
  static boolean FlashLoad;
  static boolean AutoStart;
  static boolean Playing;
  static boolean Stopping;
  static int StartBlock;
  static int EarState;
  static int LoopBlockStart, LoopBlockCounter;
  static boolean BlockInProgress;

  static final String[] HWName=  
    {
    "ZX Spectrum 16k",
    "ZX Spectrum 48k, Plus",
    "ZX Spectrum 48k ISSUE 1",
    "ZX Spectrum 128k +(Sinclair)",
    "ZX Spectrum 128k +2 (grey case)",
    "ZX Spectrum 128k +2A, +3",
    "Timex Sinclair TC-2048",
    "Timex Sinclair TS-2068",
    "Pentagon 128",
    "Sam Coupe",
    "Didaktik M",
    "Didaktik Gama",
    "ZX-80",
    "ZX-81",
    "ZX Spectrum 128k, Spanish version",
    "ZX Spectrum, Arabic version",
    "TK 90-X",
    "TK 95",
    "Byte",
    "Elwro",
    "ZS Scorpion",
    "Amstrad CPC 464",
    "Amstrad CPC 664",
    "Amstrad CPC 6128",
    "Amstrad CPC 464+",
    "Amstrad CPC 6128+",
    "Jupiter ACE",
    "Enterprise",
    "Commodore 64",
    "Commodore 128",
    "Inves Spectrum+",
    "Profi",
    "GrandRomMax",
    "Kay 1024",
    "Ice Felix HC91",
    "Ice Felix HC 2000",
    "Amaterske RADIO Mistrum",
    "Quorum 128",
    "MicroART ATM",
    "MicroART ATM Turbo 2",
    "Chrome",
    "ZX Badaloc",
    "TS1500",
    "Lambda",
    "TK85",
    "ZX97"
    };


  static BLKNAMES[] BlockNames = 
    {  
        new BLKNAMES( 0,                      "Unknown" ),
        new BLKNAMES( TZX_BLOCK_ROM,          "Spectrum ROM" ),
        new BLKNAMES( TZX_BLOCK_TURBO,        "Turbo Loader" ),
        new BLKNAMES( TZX_BLOCK_TONE,         "Pure Tone" ),
        new BLKNAMES( TZX_BLOCK_PULSE,        "Pulse Sequence" ),
        new BLKNAMES( TZX_BLOCK_DATA,         "Pure Data" ),
        new BLKNAMES( TZX_BLOCK_DREC,         "Direct Recording" ),
        new BLKNAMES( TZX_BLOCK_CSW,          "CSW Recording" ),
        new BLKNAMES( TZX_BLOCK_GENERAL,      "General Data" ),
        new BLKNAMES( TZX_BLOCK_PAUSE,        "Pause" ),
        new BLKNAMES( TZX_BLOCK_GSTART,       "Group Start" ),
        new BLKNAMES( TZX_BLOCK_GEND,         "Group End" ),
        new BLKNAMES( TZX_BLOCK_JUMP,         "Jump to Block" ),
        new BLKNAMES( TZX_BLOCK_LSTART,       "Loop Start" ),
        new BLKNAMES( TZX_BLOCK_LEND,         "Loop End" ),
        new BLKNAMES( TZX_BLOCK_SBLOCK,       "Select Block" ),
        new BLKNAMES( TZX_BLOCK_STOP48K,      "Stop Tape" ),
        new BLKNAMES( TZX_BLOCK_SETLEVEL,     "Set Level" ),
        new BLKNAMES( TZX_BLOCK_TEXT,         "Text Description" ),
        new BLKNAMES( TZX_BLOCK_MESSAGE,      "Message" ),
        new BLKNAMES( TZX_BLOCK_ARCHIVE,      "Archive info" ),
        new BLKNAMES( TZX_BLOCK_HWTYPE,       "Hardware Type" ),
        new BLKNAMES( TZX_BLOCK_CUSTOM,       "Custom Info" ),
        new BLKNAMES( TZX_BLOCK_GLUE,         "Glue" ),
        new BLKNAMES( -1,                     "" )
} ;

static void EraseAll()
{
        int i;
        for(i=0;i<Blocks;i++) EraseBlock(i);
        Blocks=CurBlock=0;
}

static void NewTZX()
{
        EraseAll();
        AddTextBlock("Created with EightyOneTZX");
        //AddHWTypeBlock(0x00, 0x0c);
        CurBlock=0;
        AutoStart=true;
}

static void EraseBlock(int BlockNo)
{ 
  
        Tape[BlockNo].BlockID=0;

        if (Tape[BlockNo].Data.Pulses!=null)
        {
                Tape[BlockNo].Data.Pulses=null;
        }

        if (Tape[BlockNo].SymDefP!=null)
        {
                Tape[BlockNo].SymDefP=null;
        }

        if (Tape[BlockNo].SymDefD!=null)
        {
                Tape[BlockNo].SymDefD=null;
        }

        if (Tape[BlockNo].PRLE!=null)
        {
                Tape[BlockNo].PRLE=null;
        }
}

static int GetGroup(int Block)
{
        return(Tape[Block].Group);
}


static String GetBlockName(int BlockNo)
{
        int i, len, BlockID;
        String text, parameters;
        char c;
        byte[] data;

        BlockID=Tape[BlockNo].BlockID;
        parameters="";

        switch(BlockID)
        {
        case 0x10:
                data=Tape[BlockNo].Data.Data;
                if ((data[0]==0) && ((TZXROM)Tape[BlockNo].Head).DataLen==19
                                        || ((TZXROM)Tape[BlockNo].Head).DataLen==20)
                {
                        switch(data[1])
                        {
                        case 0:
                                text="Program: ";
                                i=(data[14]&0xff)+(data[15]&0xff)*256;
                                if (i<32768)
                                {
                                        parameters=" LINE ";
                                        parameters+=i;
                                }
                                break;
                        case 1: text="Num Array: "; break;
                        case 2: text="Chr Array: "; break;
                        case 3:
                                text="Code: ";
                                i=(data[14]&0xff)+(data[15]&0xff)*256;
                                parameters+=i;
                                parameters+=",";
                                i=(data[12]&0xff)+(data[13]&0xff)*256;
                                parameters+=i;
                                if (parameters=="16384,6912")
                                        parameters="SCREEN$";

                                break;
                        default: text="Unknown: "; break;
                        }

                        text += "\"";
                        for(i=2;i<12;i++)
                                if (data[i]>=32 && data[i]<127) text += data[i];
                                else text+="?";
                        text.trim();
                        text += "\" ";
                        text += parameters;
                }
                else if (data[0]==0 && ((TZXROM)Tape[BlockNo].Head).DataLen==27)
                {
                        switch(data[1])
                        {
                        case 0: text="Dict: \""; break;
                        case 32:
                                text="Bytes: \"";
                                i=(data[14]&0xff)+(data[15]&0xff)*256;
                                parameters+=i;
                                parameters+=",";
                                i=(data[12]&0xff)+(data[13]&0xff)*256;
                                parameters+=i;
                                break;
                        default: text="Unknown: \""; break;
                        }
                        for(i=2;i<12;i++)
                                if (data[i]>=32 && data[i]<127) text += data[i];
                                else text+="?";
                        text.trim();
                        text+="\" "+parameters;
                }
                else text="";
                return(text);

        case 0x30:
        case 0x21:
                len=((TZXText)Tape[BlockNo].Head).TextLen;

                text="";
                for(i=0;i<len;i++)
                {
                        c=(char)((Tape[BlockNo].Data.Data[i])&0xff);
                        text += c;
                }

                return(text);

        case 0x33:
                text="Hardware - ";
                text += HWName[Tape[BlockNo].Data.HWTypes[0].ID];
                return(text);

        case 0x19:
                if ((((TZXGeneral)Tape[BlockNo].Head).TOTP==0)
                        && (((TZXGeneral)Tape[BlockNo].Head).NPP==0)
                        && (((TZXGeneral)Tape[BlockNo].Head).ASP==0)
                        && (((TZXGeneral)Tape[BlockNo].Head).NPD==19)
                        && (((TZXGeneral)Tape[BlockNo].Head).ASD==2))
                {
                        text=GetFName(BlockNo);
                        if (text!="") text = "Program: \""+text+"\"";
                        else text="Code";
                }
                else    text="";
                return(text);

        case TZX_BLOCK_PAUSE:
                if (Tape[BlockNo].Pause==0) text="--- Stop The Tape ---";
                else text="Pause";
                return(text);

        default:
                i=0;
                do
                {
                        if (BlockNames[i].id==Tape[BlockNo].BlockID)
                                return(BlockNames[i].name);

                                i++;
                } while (BlockNames[i].id != -1);
        }

        return("");
}

static byte[] GetBlockData(int Block)
{
        return(Tape[Block].Data.Data);
}

static void DeleteBlock(int Block)
{
        int i;

        if (Block>=Blocks) return;
        EraseBlock(Block);

        for(i=Block; i<Blocks; i++)
                Tape[i]=Tape[i+1];

        Blocks--;
}

static void InsertBlock(int Position)
{
        int i;
        i=Blocks;

        while(i>=Position)
        {
                Tape[i+1]=Tape[i];
                i--;
        }

        Tape[Position] = new TZXBlock();
        if (Position<=CurBlock) CurBlock++;
        Blocks++;
}

static void MoveBlock(int from, int to)
{
        while(from!=to)
        {
                if (from==to) return;
                if (from>to)
                {
                        SwapBlocks(from, from-1);
                        from--;
                }
                else
                {
                        SwapBlocks(from, from+1);
                        from++;
                }
        }
}

static void SwapBlocks(int b1, int b2)
{
        TZXBlock b;

        b=Tape[b1];
        Tape[b1]=Tape[b2];
        Tape[b2]=b;
}

static void MergeBlocks()
{
        int i;
        if (Blocks==0) return;

        for(i=0;i<Blocks;i++)
        {
                while((Tape[i].BlockID==TZX_BLOCK_GENERAL
                        || Tape[i].BlockID==TZX_BLOCK_PAUSE)
                        && Tape[i+1].BlockID==TZX_BLOCK_PAUSE)
                {
                        Tape[i].Pause += Tape[i+1].Pause;
                        DeleteBlock(i+1);
                }
        }

        i=0;
        while(Tape[i].BlockID==TZX_BLOCK_TEXT
                || Tape[i].BlockID==TZX_BLOCK_MESSAGE
                || Tape[i].BlockID==TZX_BLOCK_ARCHIVE
                || Tape[i].BlockID==TZX_BLOCK_HWTYPE) i++;

        if (Tape[i].BlockID==TZX_BLOCK_PAUSE) DeleteBlock(i);
}

static boolean IsEditable(int BlockNo)
{
        switch(Tape[BlockNo].BlockID)
        {
        case TZX_BLOCK_TEXT:
        case TZX_BLOCK_HWTYPE:
        case TZX_BLOCK_GENERAL:
        case TZX_BLOCK_PAUSE:
        case TZX_BLOCK_ARCHIVE:
        case TZX_BLOCK_GSTART:
        case TZX_BLOCK_ROM:
        case TZX_BLOCK_TURBO:
        case TZX_BLOCK_DATA:
                return(true);

        default:
                return(false);
        }
}

String GetBlockType(int BlockNo)
{
        switch(Tape[BlockNo].BlockID)
        {
        case 0x30:
        case 0x32:
        case 0x33:
        case 0x35:
                return("Info");

        default:
                return(Integer.toHexString(Tape[BlockNo].BlockID));
        }
        //return(Tape[BlockNo].BlockID);
}

static String GetBlockLength(int BlockNo)
{
        String value;
        int len=-1;

        switch (Tape[BlockNo].BlockID)
        {
        case TZX_BLOCK_PAUSE:
                        len=-Tape[BlockNo].Pause;
                        break;
        case TZX_BLOCK_ROM:
                        len=((TZXROM)Tape[BlockNo].Head).DataLen;
                        break;
        case TZX_BLOCK_TURBO:
                        len=((TZXTurbo)Tape[BlockNo].Head).DataLen;
                        break;
        case TZX_BLOCK_DATA:
                        len=((TZXData)Tape[BlockNo].Head).DataLen;
                        break;
        case TZX_BLOCK_GENERAL:
                        len=((TZXGeneral)Tape[BlockNo].Head).DataLen;
                        break;
        case TZX_BLOCK_TONE:
        case TZX_BLOCK_PULSE:
        case TZX_BLOCK_DREC:
        case TZX_BLOCK_CSW:
        default:
                break;
        }

        if (len==-1) return("");

        if (len>=0) return(len+"ms");

        len=-len;
        value=len+"ms";

        return(value);
}

static boolean GetEarState()
{
        return(EarState!=0);
}

static String GetFName(int BlockNo)
{
        String Name="";
        byte[] p;
        int pos = 0;
        int i=32;
        boolean end=false;


        if (Tape[BlockNo].BlockID != TZX_BLOCK_GENERAL) return("");

        p=Tape[BlockNo].Data.Data;

        do
        {
                int c=p[pos++];

                if ((c&128)!=0)
                {
                        end=true;
                        c=c&127;
                }

                if (c==0) Name += " ";
                if (c>=28 && c<=37) Name += (char)((c-28)+('0'));
                if (c>=38 && c<=63) Name += (char)((c-38)+('A'));
                i--;
        } while(i!=0 && !end);

        return(Name);
    }

  static void EditBlock(int Block, int Mx, int My)
    {
    switch(Tape[Block].BlockID)
      {
      case TZX_BLOCK_PAUSE:
              //EditPauseForm->Go(Block, Mx, My);
              break;

      case TZX_BLOCK_ARCHIVE:
              //EditArchiveInfo->Go(Block, Mx, My);
              break;

      case TZX_BLOCK_TEXT:
      case TZX_BLOCK_GSTART:
              //EditTextForm->Go(Block, Mx, My);
              break;

      case TZX_BLOCK_HWTYPE:
              //EditHWInfoForm->Go(Block, Mx, My);
              break;
      case TZX_BLOCK_GENERAL:
              //EditGeneralForm->Go(Block, Mx, My);
              break;

      case TZX_BLOCK_ROM:
      case TZX_BLOCK_TURBO:
      case TZX_BLOCK_DATA:
              //EditDataForm->Go(Block, Mx, My);
              break;

      default:
                break;
      }
    }
  
  static int ReadByte(InputStream f)
  throws IOException
    {
    return f.read();
    }
    
  static int ReadWord(InputStream f)
  throws IOException
    {
    return f.read() + (f.read() << 8);
    }

  static int ReadDWord(InputStream f)
  throws IOException
    {
    return f.read() +
           (f.read() << 8) +
           (f.read() << 16) +
           (f.read() << 24);
    }           
    
  static int Read3Bytes(InputStream f)
  throws IOException
    {
    return f.read() +
           (f.read() << 8) +
           (f.read() << 16);
    }

  static void ReadBytes(InputStream f, int len, byte[] buf)
  throws IOException
    {
    f.read(buf,0,len);
    }

  static void ReadWords(InputStream f, int len, int[] buf)
  throws IOException
    {
    for( int i = 0; i < len; i++ )
      buf[i] = ReadWord(f);
    }

  static boolean LoadOldGeneralBlock(InputStream f)
  throws IOException
    {
    int bl, flags, pl, pp, ns, np, as, usedbits, pause;
    int datalen;
    int BlockType;
    long pos;
    int i;

    int[] SymDef=
    { 3, 530, 520, 530, 520, 530, 520, 530, 4689,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      3, 530, 520, 530, 520, 530, 520, 530, 520, 530,
      520, 530, 520, 530, 520, 530, 520, 530, 4689 };
   
    int[] SymDefD = new int[2*19];
    System.arraycopy(SymDef,0,SymDefD,0,SymDefD.length);

    bl=ReadDWord(f);
    flags=ReadByte(f);
    pl=ReadWord(f);
    pp=ReadWord(f);
    ns=ReadByte(f);

    if ((flags!=0) && (flags!=1)) { return(true); }
    if (pl!=0) { return(true); }
    if (pp!=0) { return(true); }
    if (ns!=0) { return(true); }

    int[] sp = null;
    if (ns!=0)
    {
            sp=new int[ns];
            for(i=0;i<pp;i++) sp[i]=ReadWord(f);
    } 

    np=ReadByte(f);
    as=ReadByte(f);

    if (as!=2) { return(true); }

    int[] at = new int[np*as];
    for(i=0; i< (np*as); i++) at[i]=ReadWord(f);

    usedbits=ReadByte(f);
    pause=ReadWord(f);

    datalen=bl-(11+2*(ns+np*as));
    byte[] data=new byte[datalen];
    ReadBytes(f, datalen, data);

    Tape[CurBlock].BlockID=TZX_BLOCK_GENERAL;
    Tape[CurBlock].Pause=pause;
    Tape[CurBlock].Head = new TZXGeneral();
    ((TZXGeneral)Tape[CurBlock].Head).TOTP=0;
    ((TZXGeneral)Tape[CurBlock].Head).NPP=0;
    ((TZXGeneral)Tape[CurBlock].Head).ASP=0;
    ((TZXGeneral)Tape[CurBlock].Head).TOTD=datalen*8;
    ((TZXGeneral)Tape[CurBlock].Head).NPD=19;
    ((TZXGeneral)Tape[CurBlock].Head).ASD=2;
    ((TZXGeneral)Tape[CurBlock].Head).DataLen=datalen;

    Tape[CurBlock].SymDefD=SymDefD;
    Tape[CurBlock].Data.Data=data;
    Tape[CurBlock].SymDefP=null;
    Tape[CurBlock].PRLE=null;

    return(false);
}

  static boolean LoadGeneralBlock(InputStream f)
  throws IOException
{
        int[] SymDefP, SymDefD = null, PRLE = null;
        byte[] Data;
        int DataLen, Pause;
        int TOTP, NPP, ASP, TOTD, NPD,ASD;
        int bits, bytes = 0;
        int i,j,k;

        long pos;

        f.mark(65536);

        if (!LoadOldGeneralBlock(f)) return(false);
        
        f.reset();

        DataLen=ReadDWord(f);
        Pause=ReadWord(f);
        TOTP=ReadDWord(f);
        NPP=ReadByte(f)+1;
        ASP=ReadByte(f); if (ASP==0 && TOTP>0) ASP=256;

        if (TOTP==0) { NPP=0; ASP=0; }

        TOTD=ReadDWord(f);
        NPD=ReadByte(f)+1;
        ASD=ReadByte(f); if (ASD==0 && TOTP>0) ASD=256;

        if (TOTD==0) { NPD=0; ASD=0; }

        if (TOTP>0)
        {
                SymDefP = new int[ASP*NPP];
                PRLE= new int[2*TOTP];

                for(i=0;i<(ASP);i++)
                {
                        SymDefP[i*NPP]=ReadByte(f);
                        for(j=0;j<(NPP-1);j++)
                        {
                                k=ReadWord(f);
                                SymDefP[i*NPP+j+1]=k;
                                //if (k==0) j=NPP;
                        }
                }
                for(i=0;i<TOTP;i++)
                {
                        PRLE[i*2]=ReadByte(f);
                        PRLE[i*2+1]=ReadWord(f);
                }
        }
        else
        {
                SymDefP=null;
                PRLE=null;
        }

        if (TOTD>0)
        {
                int SymSize;

                i=1;
                bits=0;
                while(i<ASD)
                {
                        i<<=1;
                        bits++;
                }

                bits = bits*TOTD;
                bytes = bits/8;
                if ((bytes*8)<bits) bytes++;

                SymDefD=new int[ASD*NPD];

                for(i=0;i<(ASD);i++)
                {
                        SymDefD[i*NPD]=ReadByte(f);
                        for(j=0;j<(NPD-1);j++)
                        {
                                k=ReadWord(f);
                                SymDefD[i*NPD+j+1]=k;
                                //if (k==0) j=NPP;
                        }
                }


                Data=new byte[bytes];
                ReadBytes(f,bytes, Data);
        }
        else    Data=null;

        Tape[CurBlock].BlockID=TZX_BLOCK_GENERAL;
        Tape[CurBlock].Pause=Pause;
        Tape[CurBlock].Head = new TZXGeneral();
        ((TZXGeneral)Tape[CurBlock].Head).TOTP=TOTP;
        ((TZXGeneral)Tape[CurBlock].Head).NPP=NPP;
        ((TZXGeneral)Tape[CurBlock].Head).ASP=ASP;
        ((TZXGeneral)Tape[CurBlock].Head).TOTD=TOTD;
        ((TZXGeneral)Tape[CurBlock].Head).NPD=NPD;
        ((TZXGeneral)Tape[CurBlock].Head).ASD=ASD;
        ((TZXGeneral)Tape[CurBlock].Head).DataLen=bytes;

        Tape[CurBlock].Data.Data=Data;
        Tape[CurBlock].SymDefP=SymDefP;
        Tape[CurBlock].SymDefD=SymDefD;
        Tape[CurBlock].PRLE=PRLE;

        return(false);
}


  static boolean LoadROMBlock(InputStream f)
  throws IOException
    {
        int length;
        int pause;
        byte[] data;

        pause=ReadWord(f);
        length=ReadWord(f);
        data=new byte[length];
        ReadBytes(f,length,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_ROM;
        Tape[CurBlock].Data.Data=data;
        Tape[CurBlock].Head = new TZXROM();
        ((TZXROM)Tape[CurBlock].Head).DataLen=length;
        Tape[CurBlock].Pause=pause;

        return(false);
}

  static boolean LoadTurboBlock(InputStream f)
  throws IOException
    {
        int datalen, lp,ls1,ls2,l0,l1,lpt, usedbits,pause;
        byte[] data;

        lp=ReadWord(f);
        ls1=ReadWord(f);
        ls2=ReadWord(f);
        l0=ReadWord(f);
        l1=ReadWord(f);
        lpt=ReadWord(f);
        usedbits=ReadByte(f);
        pause=ReadWord(f);
        datalen=Read3Bytes(f);

        data=new byte[datalen];
        ReadBytes(f,datalen,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_TURBO;
        Tape[CurBlock].Data.Data=data;
        Tape[CurBlock].Head = new TZXTurbo();
        ((TZXTurbo)Tape[CurBlock].Head).PilotLen=lp;
        ((TZXTurbo)Tape[CurBlock].Head).Sync1Len=ls1;
        ((TZXTurbo)Tape[CurBlock].Head).Sync2Len=ls2;
        ((TZXTurbo)Tape[CurBlock].Head).Bit0Len=l0;
        ((TZXTurbo)Tape[CurBlock].Head).Bit1Len=l1;
        ((TZXTurbo)Tape[CurBlock].Head).PilotPulses=lpt;
        ((TZXTurbo)Tape[CurBlock].Head).FinalBits=usedbits;
        Tape[CurBlock].Pause=pause;
        ((TZXTurbo)Tape[CurBlock].Head).DataLen=datalen;

        return(false);
}

  static boolean LoadToneBlock(InputStream f)
  throws IOException
    {
        int pulselen, pulses;

        pulselen=ReadWord(f);
        pulses=ReadWord(f);

        Tape[CurBlock].BlockID=TZX_BLOCK_TONE;
        Tape[CurBlock].Head = new TZXTone();
        ((TZXTone)Tape[CurBlock].Head).PulseLen=pulselen;
        ((TZXTone)Tape[CurBlock].Head).NoPulses=pulses;

        return(false);
}

  static boolean LoadPulseBlock(InputStream f)
  throws IOException
    {
        int nopulses;
        int[] pulses;

        nopulses=ReadByte(f);
        pulses=new int[nopulses*2];
        ReadWords(f,nopulses*2,pulses);

        Tape[CurBlock].BlockID=TZX_BLOCK_PULSE;
        Tape[CurBlock].Head = new TZXPulse();
        Tape[CurBlock].Data.Pulses=pulses;
        ((TZXPulse)Tape[CurBlock].Head).NoPulses=nopulses;

        return(false);
}

  static boolean LoadDataBlock(InputStream f)
  throws IOException
    {
        int datalen, len0, len1, usedbits, pause;
        byte[] data;

        len0=ReadWord(f);
        len1=ReadWord(f);
        usedbits=ReadByte(f);
        pause=ReadWord(f);
        datalen=Read3Bytes(f);
        data=new byte[datalen];
        ReadBytes(f,datalen,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_DATA;
        Tape[CurBlock].Head = new TZXData();
        Tape[CurBlock].Data.Data=data;
        ((TZXData)Tape[CurBlock].Head).Len0=len0;
        ((TZXData)Tape[CurBlock].Head).Len1=len1;
        ((TZXData)Tape[CurBlock].Head).FinalBits=usedbits;
        Tape[CurBlock].Pause=pause;
        ((TZXData)Tape[CurBlock].Head).DataLen=datalen;

        return(false);
}

  static boolean LoadDRecBlock(InputStream f)
  throws IOException
    {
        int samplelen, pause, usedbits, datalen;
        byte[] data;

        samplelen=ReadWord(f);
        pause=ReadWord(f);
        usedbits=ReadByte(f);
        datalen=Read3Bytes(f);

        data=new byte[datalen];
        ReadBytes(f,datalen,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_DREC;
        Tape[CurBlock].Head = new TZXDRec();        
        Tape[CurBlock].Data.Data=data;
        ((TZXDRec)Tape[CurBlock].Head).SampleLen=samplelen;
        Tape[CurBlock].Pause=pause;
        ((TZXDRec)Tape[CurBlock].Head).FinalBits=usedbits;
        ((TZXDRec)Tape[CurBlock].Head).Samples=datalen;

        return(false);
}

  static boolean LoadCSWBlock(InputStream f)
  throws IOException
    {
        int datalen, pause, samplerate, compression, flags, nopulses;
        byte[] data;

        datalen=ReadDWord(f)-11;
        pause=ReadWord(f);
        samplerate=Read3Bytes(f);
        compression=ReadByte(f);
        flags=ReadByte(f);
        nopulses=ReadDWord(f);

        data=new byte[datalen];
        ReadBytes(f,datalen,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_CSW;
        Tape[CurBlock].Head = new TZXCSW();        
        Tape[CurBlock].Data.Data=data;
        ((TZXCSW)Tape[CurBlock].Head).BlockLen=datalen;
        Tape[CurBlock].Pause=pause;
        ((TZXCSW)Tape[CurBlock].Head).SampleRate=samplerate;
        ((TZXCSW)Tape[CurBlock].Head).Compression=compression;
        ((TZXCSW)Tape[CurBlock].Head).Flags=flags;
        ((TZXCSW)Tape[CurBlock].Head).NoPulses=nopulses;

        return(false);
}

  static boolean LoadPauseBlock(InputStream f)
  throws IOException
{
        int pause;

        pause=ReadWord(f);
        Tape[CurBlock].BlockID=TZX_BLOCK_PAUSE;
        Tape[CurBlock].Pause=pause;

        return(false);
}
static boolean LoadGStartBlock(InputStream f)
throws IOException
{
        int length;
        byte[] data;

        length=ReadByte(f);
        data=new byte[length];
        ReadBytes(f,length,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_GSTART;
        Tape[CurBlock].Head = new TZXGStart();
        Tape[CurBlock].Data.Data=data;
        ((TZXGStart)Tape[CurBlock].Head).NameLen=length;

        return(false);
}
static boolean LoadGEndBlock(InputStream f)
throws IOException
{
        Tape[CurBlock].BlockID=TZX_BLOCK_GEND;
        return(false);
}
static boolean LoadJumpBlock(InputStream f)
throws IOException
{
        int jump;

        jump=ReadWord(f);
        Tape[CurBlock].BlockID=TZX_BLOCK_JUMP;
        Tape[CurBlock].Head = new TZXJump();
        ((TZXJump)Tape[CurBlock].Head).JumpRel=jump;

        return(false);
}
static boolean LoadLStartBlock(InputStream f)
throws IOException
{
        int repeats;

        repeats=ReadWord(f);

        Tape[CurBlock].BlockID=TZX_BLOCK_LSTART;
        Tape[CurBlock].Head = new TZXLStart();
        ((TZXLStart)Tape[CurBlock].Head).Repeats=repeats;

        return(false);
}
static boolean LoadLEndBlock(InputStream f)
throws IOException
{
        Tape[CurBlock].BlockID=TZX_BLOCK_LEND;
        return(false);
}
static boolean LoadSBlock(InputStream f)
throws IOException
{
        int length, selections;
        byte[] data;

        length=ReadWord(f)-1;
        selections=ReadByte(f);
        data=new byte[length];
        ReadBytes(f,length,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_SBLOCK;
        Tape[CurBlock].Head = new TZXSBlock();
        Tape[CurBlock].Data.Data=data;
        ((TZXSBlock)Tape[CurBlock].Head).BlockLen=length;
        ((TZXSBlock)Tape[CurBlock].Head).NoSelections=selections;

        return(false);
}
static boolean LoadStop48KBlock(InputStream f)
throws IOException
{
        ReadDWord(f);
        Tape[CurBlock].BlockID=TZX_BLOCK_STOP48K;

        return(false);
}
static boolean LoadSetLevelBlock(InputStream f)
throws IOException
{
        int level;

        ReadDWord(f);
        level=ReadByte(f);

        Tape[CurBlock].BlockID=TZX_BLOCK_SETLEVEL;
        Tape[CurBlock].Head = new TZXSetLevel();
        ((TZXSetLevel)Tape[CurBlock].Head).Level=level;

        return(false);
}
static boolean LoadTextBlock(InputStream f)
throws IOException
{
        int length;
        byte[] data;

        length=ReadByte(f);
        data=new byte[length];
        ReadBytes(f,length,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_TEXT;
        Tape[CurBlock].Head = new TZXText();
        Tape[CurBlock].Data.Data=data;
        ((TZXText)Tape[CurBlock].Head).TextLen=length;

        return(false);
}
static boolean LoadMessageBlock(InputStream f)
throws IOException
{
        int length, time;
        byte[] data;

        time=ReadByte(f);
        length=ReadByte(f);
        data=new byte[length];
        ReadBytes(f,length,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_MESSAGE;
        Tape[CurBlock].Head = new TZXMessage();
        Tape[CurBlock].Data.Data=data;
        ((TZXMessage)Tape[CurBlock].Head).TextLen=length;
        ((TZXMessage)Tape[CurBlock].Head).Time=time;

        return(false);
}
static boolean LoadArchiveBlock(InputStream f)
throws IOException
{
        int length,strings;
        byte[] data;

        length=ReadWord(f)-1;
        data=new byte[length];
        strings=ReadByte(f);
        ReadBytes(f,length,data);

        Tape[CurBlock].BlockID=TZX_BLOCK_ARCHIVE;
        Tape[CurBlock].Head = new TZXArchive();
        Tape[CurBlock].Data.Data=data;
        ((TZXArchive)Tape[CurBlock].Head).BlockLen=length;
        ((TZXArchive)Tape[CurBlock].Head).NoStrings=strings;

        return(false);
}
static boolean LoadHWTypeBlock(InputStream f)
throws IOException
{
        int blocks,i;
        TZXHWInfo[] data;
        int p;

        blocks=ReadByte(f);
        data=new TZXHWInfo[blocks];
        p=0;

        for(i=0;i<blocks;i++)
        {
                data[p].Type = ReadByte(f);
                data[p].ID = ReadByte(f);
                data[p].Information = ReadByte(f);
                p++;
        }

        Tape[CurBlock].BlockID=TZX_BLOCK_HWTYPE;
        Tape[CurBlock].Head = new TZXHWType();
        Tape[CurBlock].Data.HWTypes=data;
        ((TZXHWType)Tape[CurBlock].Head).NoTypes=blocks;

        return(false);
}
static boolean LoadCustomBlock(InputStream f)
throws IOException
{
        byte[] data, id = new byte[17];
        int len;

        ReadBytes(f, 16, id);
        id[16]='\0';

        len=ReadDWord(f);
        data=new byte[len];
        ReadBytes(f, len, data);

        Tape[CurBlock].BlockID=TZX_BLOCK_CUSTOM;
        Tape[CurBlock].Head = new TZXCustom();
        ((TZXCustom)Tape[CurBlock].Head).IDString = new String(id);
        Tape[CurBlock].Data.Data=data;
        ((TZXCustom)Tape[CurBlock].Head).Length=len;

        return(false);
}
static boolean LoadGlueBlock(InputStream f)
throws IOException
{
        Tape[CurBlock].BlockID=TZX_BLOCK_GLUE;
        ReadDWord(f);
        ReadDWord(f);
        return(false);
}
static boolean LoadUnknownBlock(InputStream f, int BlockID)
throws IOException
{
        int length;
        byte[] data;

        length=ReadDWord(f);
        data=new byte[length];
        ReadBytes(f,length,data);

        Tape[CurBlock].BlockID=0;
        Tape[CurBlock].Head = new TZXUnknown();
        Tape[CurBlock].Data.Data=data;
        ((TZXUnknown)Tape[CurBlock].Head).type=BlockID;
        ((TZXUnknown)Tape[CurBlock].Head).length=length;

        return(false);
}

static boolean LoadTAPFile(String FileName, boolean Insert)
throws IOException
{
        String p;
        int BlockID, i;
        int HeaderLen;
        int len;
        int AddSync, AddChecksum;
        boolean FirstBlock, error;
        byte[] data = new byte[65536];

        InputStream f = TZXFile.class.getClassLoader().getResourceAsStream(FileName);
        if (f==null) return(false);
        TZXFile.FileName=FileName;

        FirstBlock=true;
        AddSync=0; AddChecksum=0;

        if (!Insert) EraseAll();
        error=false;

        // TODO: available() probably not correct here...
        while(f.available() > 0 && !error)
        {
                len=ReadWord(f);

                if (FirstBlock)
                {
                        if (len==26) AddSync=1;
                        if (len==25) { AddSync=1; AddChecksum=1; }
                        HeaderLen=len;
                }
                FirstBlock=false;

                if (len<1 || len>65536) error=true;
                else
                {
                  /* TODO:
                        ReadBytes(f, len, data+AddSync);
                        if (AddSync)
                        {
                                if (len==HeaderLen) data[0]=0;
                                else data[0]=255;
                        }

                        if (AddChecksum)
                        {
                                unsigned char check=0;

                                for(i=0;i<(len);i++)
                                        check = check ^ data[i+AddSync];
                                data[len+AddSync]=check;
                        }

                        len+= AddSync+AddChecksum;

                        MoveBlock(AddROMBlock(data, len), CurBlock);
                        if (AddSync)
                        {
                                if (len==27) Tape[CurBlock].Pause=100;
                                else Tape[CurBlock].Pause=5000;
                        }
                        CurBlock++;
                        */
                }
        }

        f.close();
        GroupCount();
        return(true);
}

static boolean LoadPFile(String FileName, boolean Insert)
throws IOException
{
        int len, fnamelen;
        byte[] tempdata = new byte[65536+256];

        InputStream f = TZXFile.class.getClassLoader().getResourceAsStream(FileName);
        if (f==null) return(false);
        TZXFile.FileName=FileName;

        if (!Insert) NewTZX();

        /* TODO:
        if (FileName.toUpperCase().endsWith(".P") )
        {
                ConvertASCIIZX81(RemoveExt(RemovePath(FileName)), tempdata);
                fnamelen=ZX81Strlen(tempdata);
        }
        else    fnamelen=0;

        len=fread(tempdata+fnamelen, 1, 65536, f);

        MoveBlock(AddGeneralBlock(tempdata, len+fnamelen), CurBlock);
        */
        Tape[CurBlock].Pause=3000;

        f.close();
        GroupCount();
        return(true);
}

static boolean LoadT81File(String FileName, boolean Insert)
throws IOException
{
        byte[] header = new byte[5];
        byte[] fname = new byte[32], flen = new byte[16];
        byte[] buffer1 = new byte[65536+256], buffer2 = new byte[65535+256];

        InputStream fptr;
        int length, zxnamelen,i;

        InputStream f = TZXFile.class.getClassLoader().getResourceAsStream(FileName);
        if (f==null) return(false);
        TZXFile.FileName=FileName;

        ReadBytes(f,4,header);
        if( !new String(header).equals(T81_HEADER_ID) )
        {
                f.close();
                return(false);
        }

        if (!Insert) NewTZX();

        do
        {   
                ReadBytes(f,32,fname);
                ReadBytes(f,16,flen);

                length = Integer.parseInt(new String(flen));
                String sfname = new String(fname);

                if ( (sfname.length()>29) || (length < 20) || (length > 65535) )
                        break;

                /* TODO:
                if (sfname.equals("<Silence>")) MoveBlock(TZXFile.AddPauseBlock(length), CurBlock++);
                else
                {
                        fread(buffer1, length, 1, fptr);
                        if ( (*buffer1==0x00) || (*buffer1==255) || (*buffer1==1) ) // If buffer doesn't include the filename, add one
                        {
                                ConvertASCIIZX81(fname, buffer2);
                                zxnamelen = ZX81Strlen(buffer2);
                        }
                        else    zxnamelen = 0;

                        memcpy(buffer2+zxnamelen, buffer1, length);
                        length += zxnamelen;

                        while(length>0 && buffer2[length-1]!=0x80) length--;

                        MoveBlock(AddGeneralBlock(buffer2, length), CurBlock++);
                }
                */
        } while(f.available() > 0);

        f.close();
        MergeBlocks();

        for(i=1;i<Blocks;i++)
                if (Tape[i].BlockID==TZX_BLOCK_GENERAL && Tape[i].Pause==0) Tape[i].Pause=5000;

        GroupCount();
        return(true);
}


public static boolean LoadFile(String FileName, boolean Insert)
throws IOException
{
        int extPos = FileName.lastIndexOf(".");
        String Extension = extPos == -1 ? "" : FileName.substring(extPos+1).toUpperCase();
        
        if (Extension.equals(".TAP")) return(LoadTAPFile(FileName, Insert));
        if (Extension.equals(".P")
                || Extension.equals(".O")
                || Extension.equals(".A83") ) return(LoadPFile(FileName, Insert));
        if (Extension == ".T81") return(LoadT81File(FileName, Insert));

        InputStream f = TZXFile.class.getClassLoader().getResourceAsStream(FileName);
        if (f==null) return(false);
        TZXFile.FileName=FileName;
        
        return LoadFile(f,Insert);
}

public static boolean LoadFile(InputStream f, boolean Insert)
throws IOException
{        
        TZXHeader head = new TZXHeader();
        boolean error;
        int BlockID, i, OldCurBlock;
        
        ReadBytes(f,8,head.id); 
        head.major = ReadByte(f);
        head.minor = ReadByte(f);
        
        if (!new String(head.id).equals(TZX_ID) )
        {
                //f.close();
                return(false);
        }

        if (!Insert) EraseAll();
        error=false;

        while(f.available() > 0 && !error)
        {
                BlockID=ReadByte(f);

                if (Insert) { InsertBlock(CurBlock); CurBlock--; }
                EraseBlock(CurBlock);

                switch(BlockID)
                {
                case TZX_BLOCK_ROM:      error=LoadROMBlock(f); break;
                case TZX_BLOCK_TURBO:    error=LoadTurboBlock(f); break;
                case TZX_BLOCK_TONE:     error=LoadToneBlock(f); break;
                case TZX_BLOCK_PULSE:    error=LoadPulseBlock(f); break;
                case TZX_BLOCK_DATA:     error=LoadDataBlock(f); break;
                case TZX_BLOCK_DREC:     error=LoadDRecBlock(f); break;
                case TZX_BLOCK_CSW:      error=LoadCSWBlock(f); break;
                case TZX_BLOCK_GENERAL:  error=LoadGeneralBlock(f); break;
                case TZX_BLOCK_PAUSE:    error=LoadPauseBlock(f); break;
                case TZX_BLOCK_GSTART:   error=LoadGStartBlock(f); break;
                case TZX_BLOCK_GEND:     error=LoadGEndBlock(f); break;
                case TZX_BLOCK_JUMP:     error=LoadJumpBlock(f); break;
                case TZX_BLOCK_LSTART:   error=LoadLStartBlock(f); break;
                case TZX_BLOCK_LEND:     error=LoadLEndBlock(f); break;
                case TZX_BLOCK_SBLOCK:   error=LoadSBlock(f); break;
                case TZX_BLOCK_STOP48K:  error=LoadStop48KBlock(f); break;
                case TZX_BLOCK_SETLEVEL: error=LoadSetLevelBlock(f); break;
                case TZX_BLOCK_TEXT:     error=LoadTextBlock(f); break;
                case TZX_BLOCK_MESSAGE:  error=LoadMessageBlock(f); break;
                case TZX_BLOCK_ARCHIVE:  error=LoadArchiveBlock(f); break;
                case TZX_BLOCK_HWTYPE:   error=LoadHWTypeBlock(f); break;
                case TZX_BLOCK_CUSTOM:   error=LoadCustomBlock(f); break;
                case TZX_BLOCK_GLUE:     error=LoadGlueBlock(f); break;
                case 0xf0:
                case 0:                  error=true; break;
                default:                 error=LoadUnknownBlock(f,BlockID); break;
                }

                if (error)
                {
                        if (Insert) DeleteBlock(CurBlock);
                }
                else
                {
                        CurBlock++;
                        if (!Insert) Blocks++;
                }
        }

        //f.close();
        GroupCount();
        return(true);
}

  static void GroupCount()
    {
    int i;

    int GroupCount=0;

    for(i=0;i<Blocks;i++)
      {
      if (Tape[i].BlockID==TZX_BLOCK_GEND
              || Tape[i].BlockID==TZX_BLOCK_LEND)
                      GroupCount--;

      Tape[i].Group=GroupCount;

      if (Tape[i].BlockID==TZX_BLOCK_GSTART
              || Tape[i].BlockID==TZX_BLOCK_LSTART)
                      GroupCount++;
      } 
    }
  
  static int AddGroupStartBlock(String str)
  {
          byte[] data = str.getBytes();

          Tape[Blocks].BlockID=TZX_BLOCK_GSTART;
          Tape[Blocks].Head = new TZXGStart();
          Tape[Blocks].Data.Data=data;
          ((TZXGStart)Tape[Blocks].Head).NameLen=str.length();
          return(Blocks++);
  }

  static int AddGroupEndBlock()
  {
          Tape[Blocks].BlockID=TZX_BLOCK_GEND;
          return(Blocks++);
  }

  static int AddTextBlock(String str)
  {
          byte[] data = str.getBytes();

          Tape[Blocks].BlockID=TZX_BLOCK_TEXT;
          Tape[Blocks].Head = new TZXText();
          Tape[Blocks].Data.Data=data;
          ((TZXText)Tape[Blocks].Head).TextLen=str.length();
          return(Blocks++);
  }

  static int AddHWTypeBlock(int type, int id)
  {
          TZXHWInfo[] data = new TZXHWInfo[] { new TZXHWInfo() };

          data[0].Type = type;
          data[0].ID = id;
          data[0].Information = 0;

          Tape[Blocks].BlockID=TZX_BLOCK_HWTYPE;
          Tape[Blocks].Head = new TZXHWType();
          Tape[Blocks].Data.HWTypes=data;
          ((TZXHWType)Tape[Blocks].Head).NoTypes=1;
          return(Blocks++);
  }

  static int AddPauseBlock(int len)
  {
          if (len>65535)
          {
                  AddPauseBlock(65535);
                  return(AddPauseBlock(len-65535));
          }

          Tape[Blocks].BlockID=TZX_BLOCK_PAUSE;
          Tape[Blocks].Head = new TZXPause();
          Tape[Blocks].Pause=len;
          return(Blocks++);
  }
  static int AddROMBlock(byte[] data, int len)
  {
          Tape[Blocks].BlockID=TZX_BLOCK_ROM;
          Tape[Blocks].Head = new TZXROM();

          Tape[Blocks].Pause=3000;
          ((TZXROM)Tape[Blocks].Head).DataLen=len;
          Tape[Blocks].Data.Data=data;
          return(Blocks++);
  }

  static int AddGeneralBlock(byte[] data, int len)
  {
          int i;

          int[] SymDef =
          { 3, 530, 520, 530, 520, 530, 520, 530, 4689,
                  0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            3, 530, 520, 530, 520, 530, 520, 530, 520, 530,
            520, 530, 520, 530, 520, 530, 520, 530, 4689 };

          Tape[Blocks].BlockID=TZX_BLOCK_GENERAL;
          Tape[Blocks].Head = new TZXGeneral();
          Tape[Blocks].Pause=Pause;
          ((TZXGeneral)Tape[Blocks].Head).TOTP=0;
          ((TZXGeneral)Tape[Blocks].Head).NPP=0;
          ((TZXGeneral)Tape[Blocks].Head).ASP=0;
          ((TZXGeneral)Tape[Blocks].Head).TOTD=len*8;
          ((TZXGeneral)Tape[Blocks].Head).NPD=19;
          ((TZXGeneral)Tape[Blocks].Head).ASD=2;
          ((TZXGeneral)Tape[Blocks].Head).DataLen=len;

          Tape[Blocks].SymDefD=SymDef;
          Tape[Blocks].Data.Data=data;
          Tape[Blocks].SymDefP=null;
          Tape[Blocks].PRLE=null;

          return(Blocks++);
  }

  static int AddArchiveBlock(String str)
  {
          byte[] p = new byte[str.length()+2];
          str.getBytes(0,str.length(),p,2);

          p[0]=0;
          p[1]=(byte)str.length();

          Tape[Blocks].BlockID=TZX_BLOCK_ARCHIVE;
          Tape[Blocks].Head = new TZXArchive();
          Tape[Blocks].Data.Data=p;
          ((TZXArchive)Tape[Blocks].Head).NoStrings=1;
          return(Blocks++);

  }  
  }

class BLKNAMES
  {
  int id;
  String name;
  public BLKNAMES(int id,String name) {this.id = id; this.name = name; }
  }
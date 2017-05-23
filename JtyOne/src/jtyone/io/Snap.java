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
 * snap.c
 *
 */

package jtyone.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jtyone.config.ZX81Config;
import jtyone.z80.Z80;
import jtyone.zx81.ZX81;


/*
#include <stdlib.h>
#include <stdio.h>

#include <fcntl.h>
#include <io.h>
#include <mem.h>
#include <sys\stat.h>
#include <ctype.h>
#include <string.h>

#include "snap.h"
#include "zx81.h"
#include "zx81config.h"
#include "z80\z80.h"

#include "debug_c.h"
*/

public class Snap
//This allows Z80 to act directly on registers
//Not a very natural approach....
{
//extern int rowcounter;

//void load_snap_cpu(FILE *f);
//void load_snap_mem(FILE *f);
//void load_snap_zx81(FILE *f);

  private ZX81Config mConfig;
  
  public Snap( ZX81Config config )
    {
    mConfig = config;
    }
  
//char *get_token(FILE *f)
  String get_token(InputStream f)
  throws IOException  
    {
    //static char buffer[256];
    //int buflen;
    char c;
    StringBuffer buffer = new StringBuffer();

    //c=fgetc(f);
    c = (char)f.read();
    while(c != -1 && Character.isWhitespace(c)) c = (char)f.read();
    //while(isspace(c) && !feof(f)) c=fgetc(f);
    

    //if (feof(f)) return(NULL);

    //buflen=0;
    //buffer[buflen++]=c;
    buffer.append(c);

    //c=fgetc(f);
    c = (char)f.read();
    while(c != -1 && !Character.isWhitespace(c))
    //while(!isspace(c) && !feof(f) && buflen<255)
    {
            //buffer[buflen++]=c;
            //c=fgetc(f);
            buffer.append(c);
            c = (char)f.read();
    }

    //buffer[buflen]='\0';

    //if (!buflen) return(NULL);
    //return(buffer);
    return buffer.toString();
    }

//int hex2dec(char *str)
  public 
  int hex2dec(String str)
    {
    int num;

    num=0;
    int pos = 0;
    //while(*str)
    while(pos < str.length())
      {
      num=num*16;
      char ch = str.charAt(pos);
      //if (*str>='0' && *str<='9') num += *str - '0';
      //else if (*str>='a' && *str<='f') num += *str +10 - 'a';
      //else if (*str>='A' && *str<='F') num += *str +10 - 'A';
      if (ch>='0' && ch<='9') num += ch - '0';
      else if (ch>='a' && ch<='f') num += ch +10 - 'a';
      else if (ch>='A' && ch<='F') num += ch +10 - 'A';
      else return(num);
      //str++;
      pos++;
      }
    return(num);
    }

//void load_snap_cpu(FILE *f)
  public 
  void load_snap_cpu(InputStream f, Z80 z80)
  throws IOException
    {
    //char *tok;
    String tok;

    //while(!feof(f))
    while(f.available() > 0)
      {
      tok=get_token(f);
      //if (!strcmp(tok,"[MEMORY]"))
      if (tok.equals("[MEMORY]"))
      {
              load_snap_mem(f,z80);
              return;
      }
      //if (!strcmp(tok,"[ZX81]"))
      if (tok.equals("[ZX81]"))
      {
              load_snap_zx81(f,z80);
              return;
      }

      //if (!strcmp(tok,"PC")) z80.pc.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"SP")) z80.sp.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"HL")) z80.hl.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"DE")) z80.de.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"BC")) z80.bc.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"AF")) z80.af.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"HL_")) z80.hl_.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"DE_")) z80.de_.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"BC_")) z80.bc_.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"AF_")) z80.af_.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"IX")) z80.ix.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"IY")) z80.iy.w = hex2dec(get_token(f));
      //if (!strcmp(tok,"IM")) z80.im = hex2dec(get_token(f));
      //if (!strcmp(tok,"IF1")) z80.iff1 = hex2dec(get_token(f));
      //if (!strcmp(tok,"IF2")) z80.iff2 = hex2dec(get_token(f));
      //if (!strcmp(tok,"HT")) z80.halted = hex2dec(get_token(f));
      if (tok.equals("PC")) z80.PC=hex2dec(get_token(f));
      if (tok.equals("SP")) z80.SP=hex2dec(get_token(f));
      // TODO: if (tok.equals("HL")) z80.HL.set(hex2dec(get_token(f)));
      // TODO: if (tok.equals("DE")) z80.DE.set(hex2dec(get_token(f)));
      // TODO: if (tok.equals("BC")) z80.BC.set(hex2dec(get_token(f)));
      if (tok.equals("AF")) z80.AF.set(hex2dec(get_token(f)));
      if (tok.equals("HL_")) z80.HL_=hex2dec(get_token(f));
      if (tok.equals("DE_")) z80.DE_=hex2dec(get_token(f));
      if (tok.equals("BC_")) z80.BC_=hex2dec(get_token(f));
      if (tok.equals("AF_")) z80.AF_=hex2dec(get_token(f));
      if (tok.equals("IX")) z80.IX.set(hex2dec(get_token(f)));
      if (tok.equals("IY")) z80.IY.set(hex2dec(get_token(f)));
      if (tok.equals("IM")) z80.IM = hex2dec(get_token(f));
      if (tok.equals("IF1")) z80.IFF1 = hex2dec(get_token(f));
      if (tok.equals("IF2")) z80.IFF2 = hex2dec(get_token(f));
      if (tok.equals("HT")) z80.halted = hex2dec(get_token(f));

      //if (!strcmp(tok,"IR"))
      if (tok.equals("IR"))
        {
        int a;

        a=hex2dec(get_token(f));

        z80.I = (a>>8) & 0xff;
        z80.R = a & 0xff;
        z80.R7 = a & 0x80;
        }
      }
    }

//void load_snap_zx81(FILE *f)
  public 
  void load_snap_zx81(InputStream f, Z80 z80)
  throws IOException
    {
    //char *tok;
    String tok;

    //while(!feof(f))
    while(f.available() > 0)
      {
      tok=get_token(f);
      //if (!strcmp(tok,"[MEMORY]"))
      if (tok.equals("[MEMORY]"))
      {
              load_snap_mem(f,z80);
              return;
      }
      //if (!strcmp(tok,"[CPU]"))
      if (tok.equals("[CPU]"))
      {
              load_snap_cpu(f,z80);
              return;
      }

      //if (!strcmp(tok,"NMI")) NMI_generator = hex2dec(get_token(f));
      //if (!strcmp(tok,"HSYNC")) HSYNC_generator = hex2dec(get_token(f));
      //if (!strcmp(tok,"ROW")) rowcounter = hex2dec(get_token(f));
      if (tok.equals("NMI")) ((ZX81)mConfig.machine).NMI_generator = hex2dec(get_token(f)) > 0;
      if (tok.equals("HSYNC")) ((ZX81)mConfig.machine).HSYNC_generator = hex2dec(get_token(f)) > 0;
      if (tok.equals("ROW")) ((ZX81)mConfig.machine).rowcounter = hex2dec(get_token(f));
      }
    }

//void load_snap_mem(FILE *f)
  public  
  void load_snap_mem(InputStream f, Z80 z80)
  throws IOException
    {
    int Addr, Count, Chr;
    //char *tok;
    String tok;

    Addr=16384;

    //while(!feof(f))
    while(f.available() > 0)
      {
      tok=get_token(f);

      //if (!strcmp(tok,"[CPU]"))
      if (tok.equals("[CPU]"))
        {
        load_snap_cpu(f,z80);
        return;
        }
      //else if (!strcmp(tok,"[ZX81]"))
      else if (tok.equals("[ZX81]"))
        {
        load_snap_zx81(f,z80);
        return;
        }
      //else if (!strcmp(tok,"MEMRANGE"))
      else if (tok.equals("MEMRANGE"))
        {
        Addr=hex2dec(get_token(f));
        get_token(f);
        }
      //else if (*tok=='*')
      else if (tok.charAt(0)=='*')
        {
        Count=hex2dec(tok+1);
        Chr=hex2dec(get_token(f));
        //while(Count--) memory[Addr++]=Chr;
        while(Count-- > 0) mConfig.machine.memory[Addr++]=Chr;
        }
      //else memory[Addr++]=hex2dec(tok);
      else mConfig.machine.memory[Addr++]=hex2dec(tok);
      }
    }

//void load_snap_ace(FILE *f)
  public  
  void load_snap_ace(InputStream f)
  throws IOException  
    {
/* TODO: load_snap_ace
        int memptr=0x2000;
        unsigned char c;
        int len, eof;

        eof=0;

        while(!eof)
        {
                c=fgetc(f);

                if (c!=0xED) memory[memptr++]=c;
                else
                {
                        len=fgetc(f);

                        if (!len) eof=1;
                        else
                        {
                                c=fgetc(f);
                                while(len--) memory[memptr++]=c;
                        }
                }

                if (feof(f)) eof=1;
        }

        zx81.RAMTOP = (memory[0x2081]*256)-1;
        if (zx81.RAMTOP == -1) zx81.RAMTOP=65535;

        memptr=0x2100;

        z80.af.b.l = memory[memptr]; z80.af.b.h = memory[memptr+1]; memptr+=4;
        z80.bc.b.l = memory[memptr]; z80.bc.b.h = memory[memptr+1]; memptr+=4;
        z80.de.b.l = memory[memptr]; z80.de.b.h = memory[memptr+1]; memptr+=4;
        z80.hl.b.l = memory[memptr]; z80.hl.b.h = memory[memptr+1]; memptr+=4;
        z80.ix.b.l = memory[memptr]; z80.ix.b.h = memory[memptr+1]; memptr+=4;
        z80.iy.b.l = memory[memptr]; z80.iy.b.h = memory[memptr+1]; memptr+=4;
        z80.sp.b.l = memory[memptr]; z80.sp.b.h = memory[memptr+1]; memptr+=4;
        z80.pc.b.l = memory[memptr]; z80.pc.b.h = memory[memptr+1]; memptr+=4;
        z80.af_.b.l = memory[memptr]; z80.af_.b.h = memory[memptr+1]; memptr+=4;
        z80.bc_.b.l = memory[memptr]; z80.bc.b.h = memory[memptr+1]; memptr+=4;
        z80.de_.b.l = memory[memptr]; z80.de_.b.h = memory[memptr+1]; memptr+=4;
        z80.hl_.b.l = memory[memptr]; z80.hl_.b.h = memory[memptr+1]; memptr+=4;

        z80.im = memory[memptr]; memptr+=4;
        z80.iff1 = memory[memptr]; memptr+=4;
        z80.iff2 = memory[memptr]; memptr+=4;
        z80.i = memory[memptr]; memptr+=4;
        z80.r = memory[memptr];
*/        
    }


//int load_snap(char *filename)
  public  
  int load_snap(String filename, Z80 z80)
  throws IOException
    {
    //char *p;
    //FILE *f;
    String p;
    FileInputStream f;

    //p=filename+strlen(filename)-4;
    p=filename.substring(filename.length()-4);

    //if (strcmp(p,".Z81") && strcmp(p,".z81")
    //        && strcmp(p,".ace") && strcmp(p,".ACE") ) return(0);
    if (!p.equals(".Z81") && !p.equals(".z81")
        && !p.equals(".ace") && !p.equals(".ACE") ) return(0);


    //if (!strcmp(p,".ace") || !strcmp(p,".ACE"))
    if (p.equalsIgnoreCase(".ace"))
    {
            //f=fopen(filename,"rb");
            //if (!f) return(0);
            f=new FileInputStream(new File(filename));
            load_snap_ace(f);
    }
    else
    {
            //f=fopen(filename,"rt");
            //if (!f) return(0);
            f=new FileInputStream(new File(filename));

            //while(!feof(f))
            while(f.available() > 0)
            {
                    //if (!strcmp(get_token(f),"[CPU]")) load_snap_cpu(f);
                    //if (!strcmp(get_token(f),"[MEMORY]")) load_snap_mem(f);
                    //if (!strcmp(get_token(f),"[ZX81]")) load_snap_zx81(f);
                    if (get_token(f).equals("[CPU]")) load_snap_cpu(f,z80);
                    if (get_token(f).equals("[MEMORY]")) load_snap_mem(f,z80);
                    if (get_token(f).equals("[ZX81]")) load_snap_zx81(f,z80);
            }
    }

    //fclose(f);
    f.close();
    // TODO: DebugUpdate();
    return(1);
    }

//int save_snap(char *filename)
  public 
  int save_snap(String filename)
    {
/* TODO: save_snap
        FILE *f;
        char *p;
        int Addr, Count, Chr, memptr;

        p=filename+strlen(filename)-4;

        if (strcmp(p,".Z81") && strcmp(p,".z81")
                && strcmp(p,".ace") && strcmp(p,".ACE") ) return(0);


        if (!strcmp(p,".ace") || !strcmp(p,".ACE"))
        {
                f=fopen(filename,"wb");
                if (!f) return(0);

                memptr=0x2000;
                memory[memptr]=0x01; memory[memptr+1]=0x80;
                memory[memptr+2]=0x00; memory[memptr+3]=0x00;

                memptr=0x2080;
                memory[memptr]=0x00; memory[memptr+1]=(zx81.RAMTOP+1)/256;
                memory[memptr+2]=0x00; memory[memptr+3]=0x00;

                memptr=0x0284;
                memory[memptr]=0x00; memory[memptr+1]=0x00;
                memory[memptr+2]=0x00; memory[memptr+3]=0x00;

                memptr=0x0288;
                memory[memptr]=0x00; memory[memptr+1]=0x00;
                memory[memptr+2]=0x00; memory[memptr+3]=0x00;

                memptr=0x028c;
                memory[memptr]=0x03; memory[memptr+1]=0x00;
                memory[memptr+2]=0x00; memory[memptr+3]=0x00;

                memptr=0x0290;
                memory[memptr]=0x03; memory[memptr+1]=0x00;
                memory[memptr+2]=0x00; memory[memptr+3]=0x00;

                memptr=0x0294;
                memory[memptr]=0xfd; memory[memptr+1]=0xfd;
                memory[memptr+2]=0x00; memory[memptr+3]=0x00;

                memptr+=0x0298;
                memory[memptr]=0x01; memory[memptr+1]=0x00;
                memory[memptr+2]=0x00; memory[memptr+3]=0x00;

                memptr+=0x029c;
                memory[memptr]=0x00; memory[memptr+1]=0x00;
                memory[memptr+2]=0x00; memory[memptr+3]=0x00;

                memptr=0x2100;

                memory[memptr] = z80.af.b.l; memory[memptr+1] = z80.af.b.h; memptr+=4;
                memory[memptr] = z80.bc.b.l; memory[memptr+1] = z80.bc.b.h; memptr+=4;
                memory[memptr] = z80.de.b.l; memory[memptr+1] = z80.de.b.h; memptr+=4;
                memory[memptr] = z80.hl.b.l; memory[memptr+1] = z80.hl.b.h; memptr+=4;
                memory[memptr] = z80.ix.b.l; memory[memptr+1] = z80.ix.b.h; memptr+=4;
                memory[memptr] = z80.iy.b.l; memory[memptr+1] = z80.iy.b.h; memptr+=4;
                memory[memptr] = z80.sp.b.l; memory[memptr+1] = z80.sp.b.h; memptr+=4;
                memory[memptr] = z80.pc.b.l; memory[memptr+1] = z80.pc.b.h; memptr+=4;
                memory[memptr] = z80.af_.b.l; memory[memptr+1] = z80.af_.b.h; memptr+=4;
                memory[memptr] = z80.bc_.b.l; memory[memptr+1] = z80.bc.b.h ; memptr+=4;
                memory[memptr] = z80.de_.b.l; memory[memptr+1] = z80.de_.b.h; memptr+=4;
                memory[memptr] = z80.hl_.b.l; memory[memptr+1] = z80.hl_.b.h; memptr+=4;

                memory[memptr] = z80.im ; memptr+=4;
                memory[memptr] = z80.iff1; memptr+=4;
                memory[memptr] = z80.iff2; memptr+=4;
                memory[memptr] = z80.i; memptr+=4;
                memory[memptr] = z80.r;
                
                Addr=0x2000;

                while(Addr<32768)
                {
                        Chr=memory[Addr];
                        Count=1;

                        while((memory[Addr+Count]==Chr) && ((Addr+Count)<=32768))
                                Count++;

                        if (Count>240) Count=240;

                        if (Count>3 || Chr==0xed)
                        {
                                fputc(0xed,f);
                                fputc(Count,f);
                        }
                        else    Count=1;

                        fputc(Chr,f);
                        Addr+=Count;
                }

                fputc(0xed,f);
                fputc(0x00,f);
        }
        else
        {
                f=fopen(filename,"wt");
                if (!f) return(1);

                fprintf(f,"[CPU]\n");
                fprintf(f,"PC %04X    SP  %04X\n", z80.pc.w,z80.sp.w);
                fprintf(f,"HL %04X    HL_ %04X\n", z80.hl.w,z80.hl_.w);
                fprintf(f,"DE %04X    DE_ %04X\n", z80.de.w,z80.de_.w);
                fprintf(f,"BC %04X    BC_ %04X\n", z80.bc.w,z80.bc_.w);
                fprintf(f,"AF %04X    AF_ %04X\n", z80.af.w,z80.af_.w);
                fprintf(f,"IX %04X    IY  %04X\n", z80.ix.w,z80.iy.w);
                fprintf(f,"IR %04X\n", (z80.i<<8) | (z80.r7 & 128) | ((z80.r) & 127));

                fprintf(f,"IM %02X      IF1 %02X\n", z80.im, z80.iff1);
                fprintf(f,"HT %02X      IF2 %02X\n", z80.halted, z80.iff2);

                fprintf(f,"\n[ZX81]\n");
                fprintf(f,"NMI %02X     HSYNC %02X\n",
                                NMI_generator, HSYNC_generator);
                fprintf(f,"ROW %03X\n", rowcounter);

                fprintf(f,"\n[MEMORY]\n");

                fprintf(f,"MEMRANGE %04X %04X\n", zx81.ROMTOP+1, zx81.RAMTOP);

                Addr=zx81.ROMTOP+1;

                while(Addr<=zx81.RAMTOP)
                {
                        Chr=memory[Addr];
                        Count=1;

                        while((memory[Addr+Count]==Chr) && ((Addr+Count)<=zx81.RAMTOP))
                                Count++;

                        if (Count>1) fprintf(f,"*%04X %02X ",Count, Chr);
                        else fprintf(f,"%02X ",Chr);

                        Addr += Count;
                }
                fprintf(f,"\n\n[EOF]\n");
        }
        fclose(f);
*/        
        return(0);
    }


//int memory_load(char *filename, int address, int length)
  public  
  int memory_load(String filename, int address, int length)
  throws IOException
    {
    //int fptr;
    //char file[256];
    String file;
    //int len;


    //if (strchr(filename, '\\') || strchr(filename, '/'))
    //{
    //        strcpy(file, filename);
    //}
    if( filename.indexOf('\\') != -1 || filename.indexOf('/') != -1)
            file = filename;
    else
    {
            //strcpy(file, zx81.cwd);
            //if (file[strlen(file)-1]!='\\') strcat(file,"\\");
            //strcat(file,"ROM\\");
            //strcat(file,filename);
            file = mConfig.zx81opts.cwd;
            if (!file.endsWith("\\")) file+="\\";
            file += "ROM\\";
            file += filename;
    }

    //fptr=open(file, O_RDONLY | O_BINARY);
    //if (fptr<1) return(errno);
    
    //if ((len=read(fptr, memory+address, length))==-1)
    //{
    //  int err;
    //
    //  err=errno;
    //  close(fptr);
    //  return(err);
    //}
    InputStream is = Snap.class.getClassLoader().getResourceAsStream(filename);
    int val = is.read();
    int len = 0;
    while( len < length && val != -1 )
      {
      mConfig.machine.memory[address++] = val;
      len++;
      val = is.read();
      }
    
    //close(fptr);
    is.close();

    return(len);
    }

//int font_load(char *filename, char *address, int length)
  public  
  int font_load(String filename, int[] font, int length)
  throws IOException
    {
    //int fptr;
    //char file[256];
    //int len;

    //strcpy(file, zx81.cwd);
    //if (file[strlen(file)-1]!='\\') strcat(file,"\\");
    //strcat(file,"ROM\\");
    //strcat(file,filename);
    String file = mConfig.zx81opts.cwd;
    if (!file.endsWith("\\")) file+="\\";
    file += "ROM\\";
    file += filename;

    //fptr=open(file, O_RDONLY | O_BINARY);
    //if (fptr<1) return(errno);

    //if ((len=read(fptr, address, length))==-1)
    //{
    //        int err;
    //
    //        err=errno;
    //        close(fptr);
    //        return(err);
    //}
    //FileInputStream fis = new FileInputStream(new File(filename));
    InputStream is = Snap.class.getClassLoader().getResourceAsStream(filename);
    int val = is.read();
    int len = 0;
    while( len < length && val != -1 )
      {
      font[len++] = val;
      val = is.read();
      }

    //close(fptr);
    is.close();

    return(len);
    }
  }
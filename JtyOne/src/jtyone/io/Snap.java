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


public class Snap
{
  private ZX81Config mConfig;
  
  public Snap( ZX81Config config )
    {
    mConfig = config;
    }
  
  String get_token(InputStream f)
  throws IOException  
    {
    char c;
    StringBuffer buffer = new StringBuffer();

    c = (char)f.read();
    while(c != -1 && Character.isWhitespace(c)) c = (char)f.read();


    buffer.append(c);

    c = (char)f.read();
    while(c != -1 && !Character.isWhitespace(c))
    {
            buffer.append(c);
            c = (char)f.read();
    }

    return buffer.toString();
    }

  public
  int hex2dec(String str)
    {
    int num;

    num=0;
    int pos = 0;
    while(pos < str.length())
      {
      num=num*16;
      char ch = str.charAt(pos);
      if (ch>='0' && ch<='9') num += ch - '0';
      else if (ch>='a' && ch<='f') num += ch +10 - 'a';
      else if (ch>='A' && ch<='F') num += ch +10 - 'A';
      else return(num);
      pos++;
      }
    return(num);
    }

  public
  void load_snap_cpu(InputStream f, Z80 z80)
  throws IOException
    {
    String tok;

    while(f.available() > 0)
      {
      tok=get_token(f);
      if (tok.equals("[MEMORY]"))
      {
              load_snap_mem(f,z80);
              return;
      }
      if (tok.equals("[ZX81]"))
      {
              load_snap_zx81(f,z80);
              return;
      }

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

  public
  void load_snap_zx81(InputStream f, Z80 z80)
  throws IOException
    {
    String tok;

    while(f.available() > 0)
      {
      tok=get_token(f);
      if (tok.equals("[MEMORY]"))
      {
              load_snap_mem(f,z80);
              return;
      }
      if (tok.equals("[CPU]"))
      {
              load_snap_cpu(f,z80);
              return;
      }

      if (tok.equals("NMI")) ((ZX81)mConfig.machine).NMI_generator = hex2dec(get_token(f)) > 0;
      if (tok.equals("HSYNC")) ((ZX81)mConfig.machine).HSYNC_generator = hex2dec(get_token(f)) > 0;
      if (tok.equals("ROW")) ((ZX81)mConfig.machine).rowcounter = hex2dec(get_token(f));
      }
    }

  public
  void load_snap_mem(InputStream f, Z80 z80)
  throws IOException
    {
    int Addr, Count, Chr;
    String tok;

    Addr=16384;

    while(f.available() > 0)
      {
      tok=get_token(f);

      if (tok.equals("[CPU]"))
        {
        load_snap_cpu(f,z80);
        return;
        }
      else if (tok.equals("[ZX81]"))
        {
        load_snap_zx81(f,z80);
        return;
        }
      else if (tok.equals("MEMRANGE"))
        {
        Addr=hex2dec(get_token(f));
        get_token(f);
        }
      else if (tok.charAt(0)=='*')
        {
        Count=hex2dec(tok+1);
        Chr=hex2dec(get_token(f));
        while(Count-- > 0) mConfig.machine.memory[Addr++]=Chr;
        }
      else mConfig.machine.memory[Addr++]=hex2dec(tok);
      }
    }

  public
  int load_snap(String filename, Z80 z80)
  throws IOException
    {
    String p;
    FileInputStream f;

    p=filename.substring(filename.length()-4);

    if (!p.equals(".Z81") && !p.equals(".z81")
        && !p.equals(".ace") && !p.equals(".ACE") ) return(0);


      f=new FileInputStream(new File(filename));

      while(f.available() > 0)
      {
              if (get_token(f).equals("[CPU]")) load_snap_cpu(f,z80);
              if (get_token(f).equals("[MEMORY]")) load_snap_mem(f,z80);
              if (get_token(f).equals("[ZX81]")) load_snap_zx81(f,z80);
      }

    f.close();
    // TODO: DebugUpdate();
    return(1);
    }

  public
  int save_snap(String filename)
    {
        return(0);
    }


  public
  int memory_load(String filename, int address, int length)
  throws IOException
    {
    String file;
    if( filename.indexOf('\\') != -1 || filename.indexOf('/') != -1)
            file = filename;
    else
    {
            file = mConfig.zx81opts.cwd;
            if (!file.endsWith("\\")) file+="\\";
            file += "ROM\\";
    }

    InputStream is = Snap.class.getClassLoader().getResourceAsStream(filename);
    int val = is.read();
    int len = 0;
    while( len < length && val != -1 )
      {
      mConfig.machine.memory[address++] = val;
      len++;
      val = is.read();
      }
    
    is.close();

    return(len);
    }

  public
  int font_load(String filename, int[] font, int length)
  throws IOException
    {
    String file = mConfig.zx81opts.cwd;
    if (!file.endsWith("\\")) file+="\\";
    file += "ROM\\";
    file += filename;

    InputStream is = Snap.class.getClassLoader().getResourceAsStream(filename);
    int val = is.read();
    int len = 0;
    while( len < length && val != -1 )
      {
      font[len++] = val;
      val = is.read();
      }

    is.close();

    return(len);
    }
  }

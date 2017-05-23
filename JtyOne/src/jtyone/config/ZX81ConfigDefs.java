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
 * zx81config.c
 *
 */

package jtyone.config;


public interface ZX81ConfigDefs
  {
    int EMUID = 0x85;
    int MAJORVERSION = 0;
    int MINORVERSION = 5;

    int SYNCTYPEH = 1;
    int SYNCTYPEV = 2;

  int BORDERNONE   = 0;
  int BORDERSMALL  = 1;
  int BORDERNORMAL = 2;
  int BORDERLARGE  = 3;
  int BORDERFULL   = 4;

  //int MACHINEZX80   = 0;
  int MACHINEZX81   = 1;
  //int MACHINEACE    = 2;
  //int MACHINETS1500 = 3;
  //int MACHINELAMBDA = 4;
  //int MACHINEZX97LE = 5;
  //int MACHINESPEC48 = 6;
  //int MACHINEQL     = 7;

  int SPECCY16       = 0;
  int SPECCY48       = 1;
  int SPECCYTC2048   = 2;
  int SPECCYTS2068   = 3;
  int SPECCY128      = 4;
  int SPECCYPLUS2    = 5;
  int SPECCYPLUS2A   = 6;
  int SPECCYPLUS3    = 7;
  int SPECCYSE       = 8;

  int SPECKBISS2     = 0;
  int SPECKBISS3     = 1;

  int HIRESDISABLED  = 0;
  int HIRESWRX       = 1;
  int HIRESG007      = 2;
  int HIRESMEMOTECH  = 3;

  int CHRGENSINCLAIR = 0;
  int CHRGENDK       = 1;
  int CHRGENQS       = 2;
  int CHRGENCHR16    = 3;
  int CHRGENLAMBDA   = 4;

  int COLOURDISABLED = 0;
  int COLOURPRISM    = 1;
  int COLOURLAMBDA   = 2;
  int COLOURDDC      = 3;
  int COLOURHAVEN    = 4;
  int COLOURACE      = 5;

  int CRCACE    = 0x0a09;
  int CRCASZMIC = 0xcac9;
  int CRCH4TH   = 0xa5cd;
  int CRCSG81   = 0x72f4;
  int CRCSP81   = 0x877d;
  int CRCTK85   = 0x28a9;
  int CRCTREE4TH  = 0x9dc7;
  int CRCTS1500 = 0x63b7;
  int CRCZX80   = 0x3a68;
  int CRCZX97LE = 0x68bb;
  int CRCZX81   = 0x2914;
  int CRCLAMBDA = 0x4d3c;
  int CRC8300   = 0x0d4e;
  int CRCR470         = 0x5413;
  int CRCSP48         = 0xACE0;

  int FLOPPYNONE     = 0;
  int FLOPPYPLUS3    = 1;
  int FLOPPYPLUSD    = 2;
  int FLOPPYDISCIPLE = 3;
  int FLOPPYOPUSD    = 4;
  int FLOPPYBETA     = 5;
  int FLOPPYIF1      = 6;

  int DRIVENONE      = 0;
  int DRIVE3INCHSS   = 1;
  int DRIVE3INCHDS   = 2;
  int DRIVE35INCHDS  = 3;

  int HDNONE         = 0;
  int HDPLUS3E       = 1;
  int HDDIVIDE       = 2;
  int HDZXCF         = 3;
  int HDACECF        = 4;
  int HDPITERSCF     = 5;
  int HDPITERS8B     = 6;
  int HDPITERS16B    = 7;

  int MFNONE         = 0;
  int MF128          = 1;
  int MFPLUS3        = 2;
  }
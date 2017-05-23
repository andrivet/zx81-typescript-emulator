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

/*
#include <string.h>
#include <dir.h>

#include "zx81config.h"

ZX81 zx81;
ZX97 zx97;
SPECTRUM spectrum;
TV tv;
MACHINE machine;
MOUSE mouse;
*/

public interface ZX81ConfigDefs
  {

  // From zx81config.h...

  //#define EMUID           0x85
  //#define MAJORVERSION    0
  //#define MINORVERSION    5
//  #define TESTVERSION     'e'
  public static final int EMUID = 0x85;
  public static final int MAJORVERSION = 0;
  public static final int MINORVERSION = 5;

  //#define SYNCTYPEH       1
  //#define SYNCTYPEV       2
  public static final int SYNCTYPEH = 1;
  public static final int SYNCTYPEV = 2;

  //#define BORDERNONE      0
  //#define BORDERSMALL     1
  //#define BORDERNORMAL    2
  //#define BORDERLARGE     3
  //#define BORDERFULL      4
  public static final int BORDERNONE   = 0;
  public static final int BORDERSMALL  = 1;
  public static final int BORDERNORMAL = 2;
  public static final int BORDERLARGE  = 3;
  public static final int BORDERFULL   = 4;

  //#define MACHINEZX80     0
  //#define MACHINEZX81     1
  //#define MACHINEACE      2
  //#define MACHINETS1500   3
  //#define MACHINELAMBDA   4
  //#define MACHINEZX97LE   5
  //#define MACHINESPEC48   6
  //#define MACHINEQL       7
  public static final int MACHINEZX80   = 0;
  public static final int MACHINEZX81   = 1;
  public static final int MACHINEACE    = 2;
  public static final int MACHINETS1500 = 3;
  public static final int MACHINELAMBDA = 4;
  public static final int MACHINEZX97LE = 5;
  public static final int MACHINESPEC48 = 6;
  public static final int MACHINEQL     = 7;

  //#define SPECCY16        0
  //#define SPECCY48        1
  //#define SPECCYTC2048    2
  //#define SPECCYTS2068    3
  //#define SPECCY128       4
  //#define SPECCYPLUS2     5
  //#define SPECCYPLUS2A    6
  //#define SPECCYPLUS3     7
  //#define SPECCYSE        8
  public static final int SPECCY16       = 0;
  public static final int SPECCY48       = 1;
  public static final int SPECCYTC2048   = 2;
  public static final int SPECCYTS2068   = 3;
  public static final int SPECCY128      = 4;
  public static final int SPECCYPLUS2    = 5;
  public static final int SPECCYPLUS2A   = 6;
  public static final int SPECCYPLUS3    = 7;
  public static final int SPECCYSE       = 8;

  //#define SPECKBISS2      0
  //#define SPECKBISS3      1
  public static final int SPECKBISS2     = 0;
  public static final int SPECKBISS3     = 1;

  //#define HIRESDISABLED   0
  //#define HIRESWRX        1
  //#define HIRESG007       2
  //#define HIRESMEMOTECH   3
  public static final int HIRESDISABLED  = 0;
  public static final int HIRESWRX       = 1;
  public static final int HIRESG007      = 2;
  public static final int HIRESMEMOTECH  = 3;

  //#define CHRGENSINCLAIR  0
  //#define CHRGENDK        1
  //#define CHRGENQS        2
  //#define CHRGENCHR16     3
  //#define CHRGENLAMBDA    4
  public static final int CHRGENSINCLAIR = 0;
  public static final int CHRGENDK       = 1;
  public static final int CHRGENQS       = 2;
  public static final int CHRGENCHR16    = 3;
  public static final int CHRGENLAMBDA   = 4;

  //#define COLOURDISABLED  0
  //#define COLOURPRISM     1
  //#define COLOURLAMBDA    2
  //#define COLOURDDC       3
  //#define COLOURHAVEN     4
  //#define COLOURACE       5
  public static final int COLOURDISABLED = 0;
  public static final int COLOURPRISM    = 1;
  public static final int COLOURLAMBDA   = 2;
  public static final int COLOURDDC      = 3;
  public static final int COLOURHAVEN    = 4;
  public static final int COLOURACE      = 5;

  //#define CRCACE    0x0a09
  //#define CRCASZMIC 0xcac9
  //#define CRCH4TH   0xa5cd
  //#define CRCSG81   0x72f4
  //#define CRCSP81   0x877d
  //#define CRCTK85   0x28a9
  //#define CRCTREE4TH  0x9dc7
  //#define CRCTS1500 0x63b7
  //#define CRCZX80   0x3a68
  //#define CRCZX97LE 0x68bb
  //#define CRCZX81   0x2914
  //#define CRCLAMBDA 0x4d3c
  //#define CRC8300   0x0d4e
  //#define CRCR470         0x5413
  //#define CRCSP48         0xACE0
  public static final int CRCACE    = 0x0a09;
  public static final int CRCASZMIC = 0xcac9;
  public static final int CRCH4TH   = 0xa5cd;
  public static final int CRCSG81   = 0x72f4;
  public static final int CRCSP81   = 0x877d;
  public static final int CRCTK85   = 0x28a9;
  public static final int CRCTREE4TH  = 0x9dc7;
  public static final int CRCTS1500 = 0x63b7;
  public static final int CRCZX80   = 0x3a68;
  public static final int CRCZX97LE = 0x68bb;
  public static final int CRCZX81   = 0x2914;
  public static final int CRCLAMBDA = 0x4d3c;
  public static final int CRC8300   = 0x0d4e;
  public static final int CRCR470         = 0x5413;
  public static final int CRCSP48         = 0xACE0;

  //#define FLOPPYNONE      0
  //#define FLOPPYPLUS3     1
  //#define FLOPPYPLUSD     2
  //#define FLOPPYDISCIPLE  3
  //#define FLOPPYOPUSD     4
  //#define FLOPPYBETA      5
  //#define FLOPPYIF1       6
  public static final int FLOPPYNONE     = 0;
  public static final int FLOPPYPLUS3    = 1;
  public static final int FLOPPYPLUSD    = 2;
  public static final int FLOPPYDISCIPLE = 3;
  public static final int FLOPPYOPUSD    = 4;
  public static final int FLOPPYBETA     = 5;
  public static final int FLOPPYIF1      = 6;

  //#define DRIVENONE       0
  //#define DRIVE3INCHSS    1
  //#define DRIVE3INCHDS    2
  //#define DRIVE35INCHDS   3
  public static final int DRIVENONE      = 0;
  public static final int DRIVE3INCHSS   = 1;
  public static final int DRIVE3INCHDS   = 2;
  public static final int DRIVE35INCHDS  = 3;

  //#define HDNONE          0
  //#define HDPLUS3E        1
  //#define HDDIVIDE        2
  //#define HDZXCF          3
  //#define HDACECF         4
  //#define HDPITERSCF      5
  //#define HDPITERS8B      6
  //#define HDPITERS16B     7
  public static final int HDNONE         = 0;
  public static final int HDPLUS3E       = 1;
  public static final int HDDIVIDE       = 2;
  public static final int HDZXCF         = 3;
  public static final int HDACECF        = 4;
  public static final int HDPITERSCF     = 5;
  public static final int HDPITERS8B     = 6;
  public static final int HDPITERS16B    = 7;

  //#define MFNONE          0
  //#define MF128           1
  //#define MFPLUS3         2
  public static final int MFNONE         = 0;
  public static final int MF128          = 1;
  public static final int MFPLUS3        = 2;
  }
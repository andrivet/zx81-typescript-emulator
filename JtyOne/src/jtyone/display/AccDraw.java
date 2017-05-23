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
 * AccDRaw_.cpp
 */

//---------------------------------------------------------------------------
/*
#include <vcl.h>
#include <ddraw.h>
#include <stdio.h>
#pragma hdrstop

#include "AccDraw_.h"
#include "main_.h"
#include "zx81.h"
#include "zx81config.h"
#include "Fullscreen.h"
*/
//---------------------------------------------------------------------------

//#pragma package(smart_init)

package jtyone.display;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import jtyone.config.Machine;
import jtyone.config.TVOptions;
import jtyone.config.ZX81Config;
import jtyone.config.ZX81ConfigDefs;
import jtyone.config.ZX81Options;
import jtyone.zx81.ZX81;

public class AccDraw 
implements Runnable, ZX81ConfigDefs
  {
  // Here for convenience.
  ZX81Options zx81opts;
  TVOptions tv;
  Machine machine;
  boolean fullSpeed = true;

  //#define HTOL 405
//  #define VTOL 290
  //#define VTOLMIN 290
  //#define VTOLMAX 340
  //#define HMIN 10
  //#define VMIN 350
  private static final int HTOL = 405;
  //static final int VTOL = 290;
  private static final int VTOLMIN = 290;
  private static final int VTOLMAX = 340;
  private static final int HMIN = 10;
  private static final int VMIN = 350;

  //int HSYNC_TOLLERANCE=HTOL;
  //int VSYNC_TOLLERANCEMIN=VTOLMIN;
  //int VSYNC_TOLLERANCEMAX=VTOLMAX;
  //int HSYNC_MINLEN=HMIN;
  //int VSYNC_MINLEN=VMIN;  
  private static int HSYNC_TOLLERANCE=HTOL;
  private static int VSYNC_TOLLERANCEMIN=VTOLMIN;
  private static int VSYNC_TOLLERANCEMAX=VTOLMAX;
  private static int HSYNC_MINLEN=HMIN;
  private static int VSYNC_MINLEN=VMIN;  

  //#define BlWinT 56
  //#define BlWinB (BlWinT+192)
  //#define BlWinL 74
  //#define BlWinR (BlWinL+256)
  private static final int BlWinT = 56;
  private static final int BlWinB = (BlWinT+192);
  private static final int BlWinL = 74;
  private static final int BlWinR = (BlWinL+256);

  //#define SmWinT 52
  //#define SmWinB (SmWinT+200)
  //#define SmWinL 70
  //#define SmWinR (SmWinL+264)
  private static final int SmWinT = 52;
  private static final int SmWinB = (SmWinT+200);
  private static final int SmWinL = 70;
  private static final int SmWinR = (SmWinL+264);

  //#define NoWinT  32
  //#define NoWinB  (NoWinT+240)
  //#define NoWinL  42
  //#define NoWinR  (NoWinL+320)
  private static final int NoWinT = 32;
  private static final int NoWinB = (NoWinT+240);
  private static final int NoWinL = 42;
  private static final int NoWinR = (NoWinL+320);

  //#define LaWinT 0
  //#define LaWinB (LaWinT+300)
  //#define LaWinL 0
  //#define LaWinR (LaWinL+400)
  private static final int LaWinT = 0;
  private static final int LaWinB = (LaWinT+300);
  private static final int LaWinL = 0;
  private static final int LaWinR = (LaWinL+400);

  //#define FuWinT 0
  //#define FuWinB (FuWinT+312)
  //#define FuWinL 0
  //#define FuWinR (FuWinL+413)
  private static final int FuWinT = 0;
  private static final int FuWinB = (FuWinT+312);
  private static final int FuWinL = 0;
  private static final int FuWinR = (FuWinL+413);

  /*
  #define Plot(x,c) { if (Paletteised) dest[RasterX+(x)]= (c); \
                      else *(DWORD *)(dest+RasterX+(x)) = Colours[(c)]; }
  */

  //#define Plot(x,c) { *(DWORD *)(dest+RasterX+(x)) = Colours[(c)]; }
  void Plot(int x, int c) { mScreenImageBufferData[dest+RasterX+x] = Colours[c]; }
  
  //int WinR=NoWinR;
  //int WinL=NoWinL;
  //int WinT=NoWinT;
  //int WinB=NoWinB;
  int WinR=NoWinR;
  int WinL=NoWinL;
  int WinT=NoWinT;
  int WinB=NoWinB;

  //int RasterX=0, RasterY=0;
  //int TVW, TVH, TVP;
  //int BPP, ScanLen;
  //int Paletteised, Scale;
  int RasterX=0, RasterY=0;
  int TVW, TVH, TVP;
  int BPP, ScanLen;
  int Paletteised, Scale;

  //long noise;
  int noise;

  //DWORD Palette[256], Colours[256], LetterBoxColour;
  int[] Palette = new int[256], Colours = new int[256], LetterBoxColour;

  //LPDIRECTDRAW7 m_pDD=NULL;
  //LPDIRECTDRAWSURFACE7 m_pddsFrontBuffer=NULL;
  //LPDIRECTDRAWSURFACE7 m_pddsFrame=NULL;
  //LPDIRECTDRAWCLIPPER pcClipper=NULL;
  //HWND hWnd;
  //LPDIRECTDRAWSURFACE7 DDFrame;
  //DDSURFACEDESC2 DDFrameSurface;
  AccCanvas mCanvas;
  Label mStatusLabel;
  BufferedImage mScreenImage;
  private int[] mScreenImageBufferData = null;
  private boolean mKeepGoing = true;
  private boolean mPaused = false;

  //Graphics::TBitmap *GDIFrame;
  
  //BYTE *dest=NULL, *buffer=NULL;
  int dest=0;

  //TRect BorderTop, BorderBottom, BorderLeft, BorderRight;
  //TRect rcsource, rcdest;

  //void add_blank(SCANLINE *CurScanLine, int tstates, BYTE colour)
  // Moved to Scanline.java
  
//-----------------------------------------------------------------------------
//DirectDraw Functions - REMOVED
//-----------------------------------------------------------------------------

  //void DDAccurateInit(int resize)
  void AccurateInit(int canvasScale)
    {
    //DDPIXELFORMAT DDpf;
    //DDSURFACEDESC2 ddsd;

    //float OrigW, OrigH, ScaleW, ScaleH;

    //OrigW=Form1->ClientWidth;
    //OrigH=Form1->ClientHeight;
    //OrigW=mComponent.getWidth();
    //OrigH=mComponent.getHeight();
    
    //if (Form1->StatusBar1->Visible) OrigH -= Form1->StatusBar1->Height;

    //if (Form1->BaseWidth==0) Form1->BaseWidth= NoWinR-NoWinL;
    //if (Form1->BaseHeight==0) Form1->BaseHeight= NoWinT-NoWinB;
    if (mCanvas.getWidth()==0 || mCanvas.getHeight()==0) mCanvas.setRequiredSize(new Dimension((NoWinR-NoWinL)*canvasScale,(NoWinB-NoWinT)*canvasScale));

    //ScaleW = OrigW / Form1->BaseWidth;
    //ScaleH = OrigH / Form1->BaseHeight;

    RasterX=0;
    //RasterY=random(256);
    // TODO: this causes problems.... 
    //RasterY=new Random().nextInt()%256;
    RasterY=0;

    //fill the DDpf structure and get the BytesPerPixel
    //ZeroMemory (&DDpf, sizeof(DDpf));
    //DDpf.dwSize = sizeof(DDpf);
    //m_pddsFrontBuffer->GetPixelFormat(&DDpf);
    //BPP = DDpf.dwRGBBitCount/8;
    // Actually ints per pixel...
    BPP = 1;

    //Paletteised = (BPP==1) ? true:false;
    Paletteised = 0;
    Scale= tv.AdvancedEffects?2:1;

    //ScanLen=460*BPP;
    ScanLen=(2+machine.tperscanline*2)*BPP;

    switch(zx81opts.bordersize)
      {
      case BORDERNONE:
        WinL=BlWinL; WinR=BlWinR; WinT=BlWinT; WinB=BlWinB;
        if (zx81opts.NTSC) { WinT-=24; WinB-=24; }
        break;
      case BORDERSMALL:
        WinL=SmWinL; WinR=SmWinR; WinT=SmWinT; WinB=SmWinB;
        if (zx81opts.NTSC) { WinT-=24; WinB-=24; }
        break;
      case BORDERNORMAL:
        WinL=NoWinL; WinR=NoWinR; WinT=NoWinT; WinB=NoWinB;
        if (zx81opts.NTSC) { WinT-=24; WinB-=24; }
        break;
      case BORDERLARGE:
        WinL=LaWinL; WinR=LaWinR; WinT=LaWinT; WinB=LaWinB;
        if (zx81opts.NTSC) { WinB-=24; }
        break;
      case BORDERFULL:
        WinL=FuWinL; WinR=FuWinR; WinT=FuWinT; WinB=FuWinB;
        if (zx81opts.NTSC) WinB-=51;
        break;
      }

    //ZeroMemory( &ddsd, sizeof( ddsd ) );
    //ddsd.dwSize = sizeof( ddsd );
    // Create the backbuffer surface
    //ddsd.dwFlags = DDSD_CAPS | DDSD_WIDTH | DDSD_HEIGHT;
    //ddsd.ddsCaps.dwCaps = DDSCAPS_OFFSCREENPLAIN | DDSCAPS_3DDEVICE;

    if (tv.AdvancedEffects)
      {
      WinL*=2; WinR*=2; WinT*=2; WinB*=2; ScanLen*=2;
      //TVW=ddsd.dwWidth = 1024;
      //TVH=ddsd.dwHeight = 768;
      TVW=1024;
      TVH=768;
      HSYNC_TOLLERANCE=HTOL*2; HSYNC_MINLEN=10;
      //VSYNC_TOLLERANCEMIN=VTOLMIN*2; VSYNC_MINLEN=350;
      //VSYNC_TOLLERANCEMAX=VTOLMAX*2;
      }
    else
      {
      //TVW=ddsd.dwWidth = 520;
      //TVH=ddsd.dwHeight = 380;
      TVW=520;
      TVH=380;
      HSYNC_TOLLERANCE=HTOL; HSYNC_MINLEN=10;
      //VSYNC_TOLLERANCEMIN=VTOLMIN; VSYNC_MINLEN=350;
      //VSYNC_TOLLERANCEMAX=VTOLMAX;
      }

    //m_pddsFrame->Release(); m_pddsFrame = NULL;
    //m_pDD->CreateSurface(&ddsd, &m_pddsFrame, NULL);
    mScreenImage = new BufferedImage(TVW,TVH,BufferedImage.TYPE_INT_RGB);
    mScreenImageBufferData = ((DataBufferInt)mScreenImage.getRaster().getDataBuffer()).getData();

    //if (zx81.NTSC)
    //{
    //        VSYNC_TOLLERANCEMIN-=60;
    //        VSYNC_TOLLERANCEMAX-=60;
    //}

    /* TODO:
    if ((resize) && (!Form1->FullScreen))
    {
            Form1->BaseWidth=WinR-WinL;
            Form1->BaseHeight=WinB-WinT;

            OrigW = Form1->BaseWidth * ScaleW;
            OrigH = Form1->BaseHeight * ScaleH;
            if (Form1->StatusBar1->Visible) OrigH += Form1->StatusBar1->Height;

            Form1->ClientWidth = OrigW;
            Form1->ClientHeight = OrigH;
    }
    */

    //DDFrame=m_pddsFrame;

    //ZeroMemory(&DDFrameSurface, sizeof(DDFrameSurface));
    //DDFrameSurface.dwSize = sizeof(DDFrameSurface);
    //DDFrame->Lock(NULL, &DDFrameSurface, DDLOCK_WAIT |  DDLOCK_NOSYSLOCK, NULL);
    //dest=buffer= (BYTE*)DDFrameSurface.lpSurface;
    //TVP=DDFrameSurface.lPitch;
    dest = 0;
    TVP=TVW; 

    RecalcPalette();
    //RecalcWinSize();
    }

  long lastDisplayUpdate = 0;
  int frameSkip = 0;
  int lastFrameSkip = 0;
  
  //void DDAccurateUpdateDisplay(bool singlestep)
  void AccurateUpdateDisplay(boolean singlestep)
    {
    long currentTime = System.currentTimeMillis();
    // Aim for 50Hz display
    if( currentTime - lastDisplayUpdate >= 1000/tv.frequency )
      {
      int width = mCanvas.getWidth();
      int height = mCanvas.getHeight();
      mCanvas.getGraphics().drawImage(mScreenImage,0,0,width,height,WinL,WinT,WinR,WinB,null);
      
      //try { System.in.read(); } catch(IOException exc) {}
      lastDisplayUpdate = currentTime;
      lastFrameSkip = frameSkip;
      frameSkip = 0;
      }
    else
      frameSkip++;
    }

  void RedrawDisplay(Graphics g)
    {
    int width = mCanvas.getWidth();
    int height = mCanvas.getHeight();
    g.drawImage(mScreenImage,0,0,width,height,WinL,WinT,WinR,WinB,null);
    }

  // From AccDraw_.cpp:
  int FrameNo=0;
  int LastVSyncLen=0, Shade=0;  

  //int AccurateDraw(SCANLINE *Line)
  int AccurateDraw(Scanline Line)
  {
    
    
    //static int FrameNo=0;
    //static int LastVSyncLen=0, Shade=0;
    //register int i,c;

    //if (!dest) return(0);

    int bufferPos = dest+FrameNo*TVP;
    //for(i=0; i<Line->scanline_len; i++)
    for(int i=0; i<Line.scanline_len; i++)
    {
            //c=Line->scanline[i];
            int c = Line.scanline[i];         

            //Plot(FrameNo*TVP, c+Shade);
            mScreenImageBufferData[bufferPos+RasterX] = Colours[c+Shade];

            /* TODO:
            if (tv.AdvancedEffects)
            {
                    if (!tv.Interlaced) Plot(TVP, c+8-Shade);                    

                    if (zx81.machine!=MACHINESPEC48)
                    {
                            RasterX +=BPP;
                            Plot(FrameNo*TVP, c+Shade);
                            if (!tv.Interlaced) Plot(TVP, c+8-Shade);
                    }

            }
            */

            RasterX += BPP;
            if (RasterX > ScanLen)
            {
                    RasterX=0;
                    dest += TVP * Scale;
                    bufferPos = dest + FrameNo*TVP;
                    RasterY += Scale;
                    if (!tv.AdvancedEffects) Shade=8-Shade;

                    if (RasterY>=TVH)
                    {
                            //RasterX=0;
                            //RasterY=0;
                            //dest=buffer;
                            //i=Line->scanline_len+1;
                            //Line->sync_valid=true;
                            i=Line.scanline_len+1;
                            Line.sync_valid=1;
                    }
            }
    }
    
    //if (Line->sync_len<HSYNC_MINLEN) Line->sync_valid=0;
    if (Line.sync_len<HSYNC_MINLEN) Line.sync_valid=0;
    //if (Line->sync_valid)
    if (Line.sync_valid!=0)
    {
            if (RasterX>(HSYNC_TOLLERANCE*BPP))
            {
                    //scanlen[RasterY]=RasterX;
                    RasterX=0;
                    RasterY+= Scale;
                    if (!tv.AdvancedEffects) Shade=8-Shade;
                    dest += TVP* Scale;
            }
                        
            if (RasterY>=TVH ||  RasterY>=VSYNC_TOLLERANCEMAX
                                  //||  (Line->sync_len>VSYNC_MINLEN
                                    ||  (Line.sync_len>VSYNC_MINLEN
                                      && RasterY>VSYNC_TOLLERANCEMIN))
            {
                    CompleteFrame();
                    RasterX=RasterY=0;
                    //dest=buffer;
                    dest = 0;

                    if (tv.Interlaced)
                    {
                            FrameNo = 1-FrameNo;
                            Shade=FrameNo*8;

                            //if (Line->scanline_len >= ((LastVSyncLen*5)/4)) FrameNo=0;
                            //LastVSyncLen=Line->scanline_len;
                            if (Line.scanline_len >= ((LastVSyncLen*5)/4)) FrameNo=0;
                            LastVSyncLen=Line.scanline_len;
                    }
                    else
                    {
                            FrameNo=0;
                            Shade=0;
                    }
                    AccurateUpdateDisplay(false);
            }
    }

    if (zx81opts.single_step)
    {
            int i;

            //for(i=0;i<8;i++) *(DWORD *)(dest+RasterX+i*BPP) = Colours[15];
            for(i=0;i<8;i++) mScreenImageBufferData[dest+RasterX+i*BPP] = Colours[15];
            AccurateUpdateDisplay(true);
    }
    
    return(0);
  }
  
  private boolean dumpedscanlines = false;
  private boolean dumpscanlines = false;
  
  //void CompleteFrame(void)
  void CompleteFrame()
  { 
    dumpscanlines = false;
    if( !dumpedscanlines )
    {
      dumpedscanlines=true;
      dumpscanlines = true;
    }
    
    int x=RasterX, y=RasterY;
    //BYTE *dest=buffer + y*TVP;
    int dest = y * TVP; 

            while(y<=WinB)
            {
                    while(x<=(WinR*BPP))
                    {
                            //if (BPP==1) dest[x]= 0;
                            //else *(DWORD *)(dest+x) = Colours[0];
                            if (BPP==1) mScreenImageBufferData[dest+x] = 0;
                            else mScreenImageBufferData[dest+x] = Colours[0];

                            x += BPP;
                    }
                    x=0;
                    y++;
                    dest+=TVP;
            }
    }
  
  //void RecalcPalette(void)
  void RecalcPalette()
  {
          int rsz, gsz, bsz;  //bitsize of field
          int rsh, gsh, bsh;  //0’s on left (the shift value)
          //DWORD CompiledPixel;
          int CompiledPixel;
          int i,r,g,b;

          /*
          if (Form1->RenderMode==RENDERDDRAW)
          {
                  DDPIXELFORMAT DDpf;
                  ZeroMemory (&DDpf, sizeof(DDpf));
                  DDpf.dwSize = sizeof(DDpf);
                  m_pddsFrontBuffer->GetPixelFormat(&DDpf);

                  rsz = DDGetMaskInfo (DDpf.dwRBitMask, &rsh);
                  gsz = DDGetMaskInfo (DDpf.dwGBitMask, &gsh);
                  bsz = DDGetMaskInfo (DDpf.dwBBitMask, &bsh);
          }
          else
          {
                  rsz=5;
                  gsz=6;
                  bsz=5;
                  rsh=11;
                  gsh=5;
                  bsh=0;
          }
          */
          rsz = 8;
          gsz = 8;
          bsz = 8;
          rsh = 16;
          gsh = 8;
          bsh = 0;

          for(i=0;i<256;i++)
          {
                  r=Palette[i]&0xff;
                  g=(Palette[i]>>8)&0xff;
                  b=(Palette[i]>>16)&0xff;

                  r >>= (8-rsz);  //keep only the MSB bits of component
                  g >>= (8-gsz);
                  b >>= (8-bsz);
                  r <<= rsh;  //SHIFT THEM INTO PLACE
                  g <<= gsh;
                  b <<= bsh;

                  //CompiledPixel = (DWORD)(r | g | b);
                  CompiledPixel = r | g | b;
                  Colours[i]=CompiledPixel;
                  /* TODO:
                  if (i==0 && !FScreen.WhiteLetterbox) LetterBoxColour=CompiledPixel;
                  if (i==(7*16) && FScreen.WhiteLetterbox) LetterBoxColour=CompiledPixel;
                  */

          }
  }

  // Rest of this stuff doesn't actually belong here...
  public int frametstates = 0;  // Actually from Sound

  /**Default constructor.
   * 
   * @param comp
   */
  public AccDraw(ZX81Config config, Label statusLabel, boolean fullSpeed, int scale)
    {
    zx81opts = config.zx81opts;
    machine = config.machine;
    tv = config.tv;
    this.fullSpeed = fullSpeed;
    
    InitializePalette();
    mCanvas = new AccCanvas(this);
    mStatusLabel = statusLabel;
    AccurateInit(scale);
    //comp.setSize(WinR-WinL,WinB-WinT);
    }
  
  public Canvas getCanvas() 
    {
    return mCanvas;
    }
  
  // From Artifacts_.cpp

  //#define DoPal(r,g,b) ((((b>255?255:(b<0?0:b))&0xff)<<16) \
  //    | (((g>255?255:(g<0?0:g))&0xff)<<8) \
  //    | ((r>255?255:(r<0?0:r))&0xff))
  int DoPal(int r,int g,int b)
  {
    return ((((b>255?255:(b<0?0:b))&0xff)<<16) 
          | (((g>255?255:(g<0?0:g))&0xff)<<8) 
          | ((r>255?255:(r<0?0:r))&0xff));
  }
  
  void InitializePalette()
    {
    int NoiseLevel, GhostLevel, GhostLevel2, ScanLineLevel;
    int BrightnessLevel, ContrastLevel, ColourLevel;
    int HiBrightLevel;
    int r,g,b,colour,i,f;
    int basecolour, difference;
    int colr, colg, colb, bwr, bwg, bwb;

    // TODO: VSYNC_TOLLERANCEMIN= 283 + VBias->Position;
    // TODO: VSYNC_TOLLERANCEMAX = VSYNC_TOLLERANCEMIN + VGain->Position + 40;
    VSYNC_TOLLERANCEMIN= 283 + 0;
    VSYNC_TOLLERANCEMAX = VSYNC_TOLLERANCEMIN + 0 + 40;

    if (tv.AdvancedEffects)
    {
            VSYNC_TOLLERANCEMIN *= 2;
            VSYNC_TOLLERANCEMAX *= 2;
    }

    if (zx81opts.NTSC)
    {
            VSYNC_TOLLERANCEMIN-=60;
            VSYNC_TOLLERANCEMAX-=60;
    }

    /* TODO: 
    NoiseLevel = - NoiseTrack->Position;
    GhostLevel = - GhostTrack->Position;
    ScanLineLevel = - ScanLineTrack->Position;
    BrightnessLevel = BrightTrack->Max - BrightTrack->Position;
    GhostLevel2 = GhostLevel/3;
    ContrastLevel = ContrastTrack->Max - ContrastTrack->Position;
    ColourLevel = ColourTrack->Max - ColourTrack->Position;
    */
    NoiseLevel = -20;
    GhostLevel = -40;
    ScanLineLevel = -1;
    BrightnessLevel = 255-188;
    GhostLevel2 = GhostLevel/3;
    ContrastLevel = 255-125;
    ColourLevel = 255-0;
    
    BrightnessLevel -= ContrastLevel;
    HiBrightLevel = BrightnessLevel + (ContrastLevel/2) + 2*ContrastLevel ;
    ContrastLevel = BrightnessLevel + ContrastLevel + ContrastLevel;

    for(i=0;i<16;i++)
    {
            colour=i;
            //if (zx81.inverse) colour=(i&8) + (7-(colour&7));
            if (zx81opts.inverse!=0) colour=(i&8) + (7-(colour&7));

            difference=(1000*(((colour>7)?HiBrightLevel:ContrastLevel)-BrightnessLevel))/16;
            basecolour=(difference*((colour&7)+9))/1000;
            if (colour==0 || colour==8) basecolour=BrightnessLevel;

            //TODO: if (Vibrant->Checked)
            if( false )
            {
                    //colb = (colour&1) ? ((i>7) ? HiBrightLevel:ContrastLevel) :BrightnessLevel;
                    //colg = (colour&4) ? ((i>7) ? HiBrightLevel:ContrastLevel) :BrightnessLevel;
                    //colr = (colour&2) ? ((i>7) ? HiBrightLevel:ContrastLevel) :BrightnessLevel;
                    colb = (colour&1)!=0 ? ((i>7) ? HiBrightLevel:ContrastLevel) :BrightnessLevel;
                    colg = (colour&4)!=0 ? ((i>7) ? HiBrightLevel:ContrastLevel) :BrightnessLevel;
                    colr = (colour&2)!=0 ? ((i>7) ? HiBrightLevel:ContrastLevel) :BrightnessLevel;
            }
            else
            {
                    //colb = BrightnessLevel+ ((colour&1) ? basecolour:0);
                    //colg = BrightnessLevel+ ((colour&4) ? basecolour:0);
                    //colr = BrightnessLevel+ ((colour&2) ? basecolour:0);
                    colb = BrightnessLevel+ ((colour&1)!=0 ? basecolour:0);
                    colg = BrightnessLevel+ ((colour&4)!=0 ? basecolour:0);
                    colr = BrightnessLevel+ ((colour&2)!=0 ? basecolour:0);
            }

            bwb = BrightnessLevel+ basecolour;
            bwg = BrightnessLevel+ basecolour;
            bwr = BrightnessLevel+ basecolour;

            r=(((colr-bwr)*ColourLevel)/255)+bwr;
            g=(((colg-bwg)*ColourLevel)/255)+bwg;
            b=(((colb-bwb)*ColourLevel)/255)+bwb;

            Palette[i*16]=DoPal(r,g,b);
            Palette[4+i*16]=DoPal(r+GhostLevel2,g+GhostLevel2,b+GhostLevel2);

            b += NoiseLevel; g += NoiseLevel; r += NoiseLevel;
            Palette[i*16+1]=DoPal(r,g,b);
            Palette[4+i*16+1]=DoPal(r+GhostLevel2,g+GhostLevel2,b+GhostLevel2);

            b += GhostLevel; g += GhostLevel; r += GhostLevel;
            Palette[i*16+3]=DoPal(r,g,b);
            Palette[4+i*16+3]=DoPal(r,g,b);

            b -= NoiseLevel; g -= NoiseLevel; r -= NoiseLevel;
            Palette[i*16+2]=DoPal(r,g,b);
            Palette[4+i*16+2]=DoPal(r+GhostLevel2,g+GhostLevel2,b+GhostLevel2);
    }

    for(i=0;i<16;i++)
    {
            for(f=0;f<8;f++)
            {
                    colour = Palette[i*16+f];

                    b= ((colour&0x00ff0000)>>16);
                    g= ((colour&0x0000ff00)>>8);
                    r= (colour&0xff);

                    // TODO: if (ArtEnabled->Checked)
                    if( false )
                    {
                            b += ScanLineLevel; g += ScanLineLevel; r += ScanLineLevel;
                    }
                    Palette[i*16+f+8]=DoPal(r,g,b);
            }
    }

    RecalcPalette();
  }
  
  // From main_.cpp
  public Scanline BuildLine;
  int fps;
  
  int j, borrow, Drive;
  
  public static int scanLineNumber = 0;
  
  //void __fastcall TForm1::AnimTimer1Timer(TObject *Sender)
  void AnimTimer1Timer()
    {
    //static int j, borrow, Drive;
    //SCANLINE *templine;
      
    /* TODO:
    if (zx81.UseRShift>0)
    { 
            boolean L=((GetAsyncKeyState(VK_LSHIFT)&32768)!=0);
            boolean R=((GetAsyncKeyState(VK_RSHIFT)&32768)!=0);
            TShiftState z;

            if (R != RShift)
            {
                    RShift=R;
                    if (R) FormKeyDown(NULL, VK_RSHIFT, z);
                    else FormKeyUp(NULL, VK_RSHIFT,z);
            }

            if (L != LShift)
            {
                    LShift=L;
                    if (L) FormKeyDown(NULL, VK_LSHIFT,z);
                    else FormKeyUp(NULL, VK_LSHIFT,z);
            }  
    }
    */

    // TODO: if (!nosound) sound_frame();
    //if (zx81_stop)
    if (machine.stop())
    {
            AccurateUpdateDisplay(false);
            return;
    }
    // if (AutoLoadCount) DoAutoLoad();

    /* TODO: 
    if (spectrum.drivebusy != Drive)
    {
            StatusBar1->Repaint();
            Drive=spectrum.drivebusy;
    }

    mouse.x = Controls::Mouse->CursorPos.x;
    mouse.y = Screen->Height - Controls::Mouse->CursorPos.y;
    */

    fps++;
    frametstates=0;

    j=zx81opts.single_step?1:(machine.tperframe + borrow);

    if (zx81opts.machine != MACHINESPEC48 && j!=1)
    {
            j += (zx81opts.speedup * machine.tperframe) / machine.tperscanline;
    }
    
    //while (j>0 && !zx81_stop)
    while (j>0 && !machine.stop())
    {
            j-= machine.do_scanline(BuildLine);
            
            scanLineNumber++;
            if( scanLineNumber < 0 ) //< 50000 )
            {
              System.out.println(scanLineNumber+":"+RasterX+","+RasterY+" len "+BuildLine.scanline_len+
                                 " slen "+BuildLine.sync_len+
                                 " sv "+BuildLine.sync_valid+
                                 " borrow "+((ZX81)machine).borrow);
            }           
            
            AccurateDraw(BuildLine);
            
            //WaitForSingleObject(Mutex,INFINITE);
            //templine=BuildLine;
            //BuildLine=DisplayLine;
            //DisplayLine=templine;
            //BuildLine->sync_len=DisplayLine->sync_len;
            //BuildLine->sync_valid=DisplayLine->sync_valid;
            //ReleaseMutex(Mutex);
            //SwitchToThread();
            //Sleep(0);
            //AccurateDraw(DisplayLine);
    }

    //if (!zx81_stop) borrow=j;
    if (!machine.stop()) borrow=j;
    } 
  
//---------------------------------------------------------------------------
//---------------------------------------------------------------------------

  /**Main routine to draw frames.
   * 
   */
  public void run()
    {
    Scanline[] Video = new Scanline[] { new Scanline(), new Scanline() };
    BuildLine = Video[0];
    
    //static int j, borrow, Drive;
    //SCANLINE *templine;
    fps = 0;
    long framesStartTime = System.currentTimeMillis();
    
    // Target frame time should result in 50Hz display.
    int targetFrameTime = 1000/tv.frequency;
    mKeepGoing = true;
    while(mKeepGoing)
      {
      if( mPaused )
        {
        try { Thread.sleep(1000); } catch( Throwable exc ) {}
        fps = 0;
        framesStartTime = System.currentTimeMillis();
        }
      else
        {
        // Process a scanline
      
        AnimTimer1Timer();
        
        // Pace the emulator to run at 100%
        long currentTime = System.currentTimeMillis();
        long delay = (targetFrameTime * fps) - (currentTime - framesStartTime);
        if( !fullSpeed && delay > 0 )
          {
          //System.out.println("Sleeping for "+delay);
          try { Thread.sleep(delay); } catch(InterruptedException exc) {}
          }
              
        if( fps == 100 )
          {
          mStatusLabel.setText("FPS: "+(fps*1000/(currentTime-framesStartTime)));
          framesStartTime = System.currentTimeMillis();
          fps = 0;
          }
        }
      }
    mStatusLabel.setText("Stopped");
    }
  
  /**Stops the display drawing routine.
   */
  public void
  stop()
    {    
    mKeepGoing = false;
    }
  
  public void setPaused( boolean paused )
    {    
    if( paused )
      mStatusLabel.setText("Paused");
    else
      mStatusLabel.setText("Running");
    
    mPaused = paused;
    }
  }
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
 */
package jtyone.io;

import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jtyone.config.ZX81Config;
import jtyone.config.ZX81ConfigDefs;
import jtyone.tzx.TZXFile;
import jtyone.tzx.TZXFileDefs;

public class Tape 
  {
  private List mPrograms = new ArrayList();
  private int mCurrentProgram = 0;
  
  /**Load data from a .TZX file.  
   * <p>
   * All we are interested in is the ZX81 programs...
   * 
   * @param fileName The name of the TZX file.
   */
  public 
  void loadTZX( ZX81Config config, KBStatus keyboard, String fileName, int entryNum, boolean applet )  
    {
    mPrograms.clear();
    mCurrentProgram = 0;
    try
      {
      InputStream is = null;
      if( applet )
        is = Tape.class.getClassLoader().getResourceAsStream(fileName);
      else
        is = new FileInputStream(fileName);
      
      if( is == null )
        {
        System.err.println("Error - could not get resource: "+fileName);
        return;
        }
      
      // Process each TZX file in turn insize a .ZIP file.
      if( fileName.toLowerCase().endsWith(".zip") )
        {
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry ze = zis.getNextEntry();
        while( ze != null )
          {  
          // If its a .TZX file, add the entries from it.
          if( ze.getName().toLowerCase().endsWith(".tzx") )
            {
            // Read in the .ZIP entry, and create a byte array input stream
            // with the same bytes.  That's necessary to allow mark/reset
            // on the input stream which is not supported by the ZIPInputStream.
            byte[] data = new byte[65536];
            int len = zis.read(data);
            int total = 0;
            while( len > 0 )
              {
              total += len;
              len = zis.read(data,total,data.length-total);
              }
 
            byte[] newData = new byte[total];
            System.arraycopy(data,0,newData,0,total);
            ByteArrayInputStream bis = new ByteArrayInputStream(newData);
            
            // Not sure why we can't just do the following, but it doesn't
            // work...
            //BufferedInputStream bis = new BufferedInputStream(zis);
            addTZXEntries(bis);
            }          
          ze = zis.getNextEntry();
          }
        }
      
      // Load directly from the file.
      // Use a buffered input stream filter to allow mark/reset to work.
      else
        {
        addTZXEntries(new BufferedInputStream(is));
        }
      
      is.close();
      }
    catch( IOException exc )
      {
      exc.printStackTrace();
      }
    
    mCurrentProgram = entryNum;
    if( config.autoload )
      {
      //System.out.println("Autoloading");
      if( config.zx81opts.machine == ZX81ConfigDefs.MACHINEZX81 )
        autoload(keyboard,'J',true);
      else if( config.zx81opts.machine == ZX81ConfigDefs.MACHINEZX80 )
        autoload(keyboard,'W',false);
      }
    }
  
  /**Add all ZX81 file entries from the .TZX file to the current list.
   * 
   * @param is
   * @param length
   */
  private 
  void addTZXEntries(InputStream is)
    {
    try
      {
      // Parse through the .TZX file, looking for ZX81 file entries.
      TZXFile.LoadFile(is,false);
      //System.out.println("Number of blocks: "+TZXFile.Blocks);
      for( int i = 0; i < TZXFile.Blocks; i++ )
        {
        if( TZXFile.Tape[i].BlockID == TZXFileDefs.TZX_BLOCK_GENERAL )
          {
          mPrograms.add(TZXFile.Tape[i].Data.Data);
          //System.out.println("Adding General block; data length = "+TZXFile.Tape[i].Data.Data.length+" now got "+mPrograms.size()+" data="+TZXFile.Tape[i].Data.Data);
          }
        } 
      }
    catch( IOException exc )
      {
      exc.printStackTrace();
      }
    }
  
  /**Get the bytes for the next entry.
   * 
   * @return
   */
  public 
  byte[] getNextEntry()
    {
    if( mCurrentProgram < mPrograms.size() )
      return (byte[])mPrograms.get(mCurrentProgram++);   
    
    return null;
    }
  
  /**Get the bytes for the given entry.
   * 
   * @return
   */
  public 
  void setCurrentEntry( int entry )
    {
    mCurrentProgram = entry;
    }
  
  public
  void autoload(KBStatus keyboard, char loadKey, boolean useQuotes )
    {
    new Thread(new AutoLoader(keyboard,loadKey,useQuotes)).start();
    }
  }

/**Class to support auto-loading, i.e. typing LOAD "" after
 * a tape has been selected.
 * 
 */
class AutoLoader implements Runnable
  {
  private KBStatus mKeyboard;
  private char mLoadKey;
  private boolean mUseQuotes;
  
  public AutoLoader(KBStatus keyboard, char loadKey, boolean useQuotes )
    {
    mKeyboard = keyboard;
    mLoadKey = loadKey;
    mUseQuotes = useQuotes;
    }
  
  public void run() 
    {
    try
      {
      // TODO: need to trigger autoload by when the Z80 gets to a particular
      // address, in case it takes longer than 4 seconds to start up.
      Thread.sleep(5000);
      mKeyboard.PCKeyDown(mLoadKey);
      Thread.sleep(200);
      mKeyboard.PCKeyUp(mLoadKey);
      Thread.sleep(200);
      
      if( mUseQuotes )
        {
        mKeyboard.PCKeyDown(KeyEvent.VK_QUOTE);
        Thread.sleep(200);
        mKeyboard.PCKeyUp(KeyEvent.VK_QUOTE);
        Thread.sleep(200);
        mKeyboard.PCKeyDown(KeyEvent.VK_QUOTE);
        Thread.sleep(200);
        mKeyboard.PCKeyUp(KeyEvent.VK_QUOTE);
        Thread.sleep(200);
        }
      
      mKeyboard.PCKeyDown(KeyEvent.VK_ENTER);
      Thread.sleep(200);
      mKeyboard.PCKeyUp(KeyEvent.VK_ENTER);
      }
    catch( Throwable exc )
      {
      exc.printStackTrace();
      }
    }
  }
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
import zx81emulator.config.ZX81ConfigDefs;
import zx81emulator.tzx.TZXFile;
import zx81emulator.tzx.TZXFileDefs;

import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Tape {
    private List mPrograms = new ArrayList();
    private int mCurrentProgram = 0;

    /**
     * Load data from a .TZX file.
     */
    public void loadTZX(ZX81Config config, KBStatus keyboard, String fileName, int entryNum) throws IOException {
        mPrograms.clear();
        mCurrentProgram = 0;

        InputStream is = Snap.class.getClassLoader().getResourceAsStream("Tapes/" + fileName);
        if (is == null) {
            throw new IOException("Error - could not get resource: " + fileName);
        }

        // Process each TZX file in turn insize a .ZIP file.
        if (fileName.toLowerCase().endsWith(".zip")) {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                // If its a .TZX file, add the entries from it.
                if (ze.getName().toLowerCase().endsWith(".tzx")) {
                    // Read in the .ZIP entry, and create a byte array input stream
                    // with the same bytes.  That's necessary to allow mark/reset
                    // on the input stream which is not supported by the ZIPInputStream.
                    byte[] data = new byte[65536];
                    int len = zis.read(data);
                    int total = 0;
                    while (len > 0) {
                        total += len;
                        len = zis.read(data, total, data.length - total);
                    }

                    byte[] newData = new byte[total];
                    System.arraycopy(data, 0, newData, 0, total);
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
        else {
            addTZXEntries(new BufferedInputStream(is));
        }

        is.close();

        mCurrentProgram = entryNum;
        if (config.autoload) {
            //System.out.println("Autoloading");
            if (config.zx81opts.machine == ZX81ConfigDefs.MACHINEZX81)
                autoload(keyboard);
        }
    }

    /**
     * Add all ZX81 file entries from the .TZX file to the current list.
     */
    private void addTZXEntries(InputStream is) throws IOException {

        // Parse through the .TZX file, looking for ZX81 file entries.
        TZXFile.LoadFile(is, false);
        //System.out.println("Number of blocks: "+TZXFile.Blocks);
        for (int i = 0; i < TZXFile.Blocks; i++) {
            if (TZXFile.Tape[i].BlockID == TZXFileDefs.TZX_BLOCK_GENERAL) {
                mPrograms.add(TZXFile.Tape[i].Data.Data);
                //System.out.println("Adding General block; data length = "+TZXFile.Tape[i].Data.Data.length+" now got "+mPrograms.size()+" data="+TZXFile.Tape[i].Data.Data);
            }
        }
    }

    /**
     * Get the bytes for the next entry.
     */
    public byte[] getNextEntry() {
        if (mCurrentProgram < mPrograms.size())
            return (byte[]) mPrograms.get(mCurrentProgram++);

        return null;
    }

    private void autoload(KBStatus keyboard) {
        new Thread(new AutoLoader(keyboard)).start();
    }
}

/**
 * Class to support auto-loading, i.e. typing LOAD "" after a tape has been selected.
 */
class AutoLoader implements Runnable {
    private KBStatus mKeyboard;

    AutoLoader(KBStatus keyboard) {
        mKeyboard = keyboard;
    }

    public void run() {
        try {
            char key = 'J';

            // TODO: need to trigger autoload by when the Z80 gets to a particular
            // address, in case it takes longer than 4 seconds to start up.
            Thread.sleep(5000);
            mKeyboard.PCKeyDown(key);
            Thread.sleep(200);
            mKeyboard.PCKeyUp(key);
            Thread.sleep(200);

            mKeyboard.PCKeyDown(KeyEvent.VK_QUOTE);
            Thread.sleep(200);
            mKeyboard.PCKeyUp(KeyEvent.VK_QUOTE);
            Thread.sleep(200);
            mKeyboard.PCKeyDown(KeyEvent.VK_QUOTE);
            Thread.sleep(200);
            mKeyboard.PCKeyUp(KeyEvent.VK_QUOTE);
            Thread.sleep(200);

            mKeyboard.PCKeyDown(KeyEvent.VK_ENTER);
            Thread.sleep(200);
            mKeyboard.PCKeyUp(KeyEvent.VK_ENTER);
        } catch (Throwable exc) {
            System.err.println("Autoload failed");
            exc.printStackTrace();
        }
    }
}
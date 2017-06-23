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
package zx81emulator.io;

import zx81emulator.config.ZX81Config;
import zx81emulator.tzx.TZXFile;
import zx81emulator.tzx.TZXFileDefs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Tape {
    private List<byte[]> mPrograms = new ArrayList<>();
    private int mCurrentProgram = 0;

    /**
     * Load data from a .TZX file.
     */
    public void loadTZX(ZX81Config config, KBStatus keyboard, String fileName, int entryNum) throws IOException {
        // TODO
        /*
        mPrograms.clear();
        mCurrentProgram = 0;

        InputStream is = Snap.class.getClassLoader().getResourceAsStream("Tapes/" + fileName);
        if (is == null) {
            throw new IOException("Error - could not get resource: " + fileName);
        }

        // Load directly from the file.
        // Use a buffered input stream filter to allow mark/reset to work.
        addTZXEntries(new BufferedInputStream(is));

        is.close();

        mCurrentProgram = entryNum;
        if (config.autoload) {
            autoload(keyboard);
        }*/
    }

    /**
     * Add all ZX81 file entries from the .TZX file to the current list.
     */
    private void addTZXEntries(InputStream is) throws IOException {

        // Parse through the .TZX file, looking for ZX81 file entries.
        TZXFile.LoadFile(is, false);
        for (int i = 0; i < TZXFile.Blocks; i++) {
            if (TZXFile.Tape[i].BlockID == TZXFileDefs.TZX_BLOCK_GENERAL) {
                mPrograms.add(TZXFile.Tape[i].Data.Data);
            }
        }
    }

    /**
     * Get the bytes for the next entry.
     */
    public byte[] getNextEntry() {
        if (mCurrentProgram < mPrograms.size())
            return mPrograms.get(mCurrentProgram++);

        return null;
    }

    private void autoload(KBStatus keyboard) {
        // TODO
        /*new Thread(new AutoLoader(keyboard)).start() */;
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
        // TODO
        /*try {
            String key = "J";

            Thread.sleep(5000);
            mKeyboard.PCKeyDown(key);
            Thread.sleep(200);
            mKeyboard.PCKeyUp(key);
            Thread.sleep(200);

            mKeyboard.PCKeyDown("\"");
            Thread.sleep(200);
            mKeyboard.PCKeyUp("\"");
            Thread.sleep(200);
            mKeyboard.PCKeyDown("\"");
            Thread.sleep(200);
            mKeyboard.PCKeyUp("\"");
            Thread.sleep(200);

            mKeyboard.PCKeyDown("Enter");
            Thread.sleep(200);
            mKeyboard.PCKeyUp("Enter");
        } catch (Throwable exc) {
            System.err.println("Autoload failed");
            exc.printStackTrace();
        }
        */
    }
}
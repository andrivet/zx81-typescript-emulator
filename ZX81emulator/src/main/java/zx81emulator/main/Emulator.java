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
package zx81emulator.main;

import jsweet.dom.HTMLElement;
import jsweet.dom.KeyboardEvent;
import zx81emulator.config.ZX81Config;
import zx81emulator.config.ZX81ConfigDefs;
import zx81emulator.display.AccDraw;
import zx81emulator.io.KBStatus;
import zx81emulator.zx81.ZX81;

import jsweet.dom.Event;
import jsweet.dom.HTMLCanvasElement;

import static jsweet.dom.Globals.console;
import static jsweet.dom.Globals.document;
import static jsweet.util.StringTypes.keydown;
import static jsweet.util.StringTypes.keyup;

import java.io.IOException;


public class Emulator {
    private AccDraw mDisplayDrawer;
    private KBStatus mKeyboard;
    private ZX81Config mConfig;

    public static void main(String[] args) {

        try {
            HTMLCanvasElement canvas = (HTMLCanvasElement) document.getElementById("canvas");
            if(canvas == null)
                    throw new Exception("No HTML element found with id 'canvas'");

            Emulator emulator = new Emulator();
            emulator.init(args, canvas);
            emulator.installListeners(canvas); // TODO: or window?
            emulator.start();
        }
        catch (Exception exc) {
            console.error("Error: " + exc);
            exc.printStackTrace();
        }
    }

    private Emulator() {
        mConfig = new ZX81Config();
        mConfig.machine = new ZX81();
        mConfig.load_config();
    }

    private void init(String[] args, HTMLCanvasElement canvas) throws IOException {
        String tzxFileName = (args.length > 0 && !args[0].startsWith("-")) ? args[0] : null;
        String scale = null;
        String hires = null;

        for (int aPos = tzxFileName == null ? 0 : 1; aPos < args.length; aPos++) {
            if (args[aPos].equals("-scale") && aPos < args.length - 1) {
                scale = args[++aPos];
            }
            if (args[aPos].equals("-hires") && aPos < args.length - 1) {
                hires = args[++aPos];
            }
        }

        init(tzxFileName, hires, scale, canvas);
    }

    private void init(String tzxFileName, String hires, String scale, HTMLCanvasElement canvas)  throws IOException {
        mConfig.machine.CurRom = mConfig.zx81opts.ROM81;

        int scaleCanvas = 2;
        if (scale != null && scale.length() > 0)
            scaleCanvas = Integer.parseInt(scale);

        if ("qs".equals(hires))
            mConfig.zx81opts.chrgen = ZX81ConfigDefs.CHRGENQS;
        else if ("dk".equals(hires))
            mConfig.zx81opts.chrgen = ZX81ConfigDefs.CHRGENDK;

        mConfig.machine.initialise(mConfig);

        // Set up keyboard.
        mKeyboard = new KBStatus();

        // Set up the various components.
        //container.setLayout(new BorderLayout());
        //container.addKeyListener(this);
        //container.addFocusListener(this);

        // Set up the display.
        mDisplayDrawer = new AccDraw(mConfig, scaleCanvas, canvas);
        //mCanvas = mDisplayDrawer.getCanvas();
        //mCanvas.addFocusListener(this);
        //mCanvas.addKeyListener(this);
        //container.add(mCanvas, "Center");

        // Load the .TZX file.
        if (tzxFileName != null) {
            String tzxEntry;
            int entryNum = 0;
            int atPos = tzxFileName.indexOf('@');
            if (atPos != -1) {
                tzxEntry = tzxFileName.substring(atPos + 1);
                tzxFileName = tzxFileName.substring(0, atPos);
                entryNum = Integer.parseInt(tzxEntry);
            }

            mConfig.machine.getTape().loadTZX(mConfig, mKeyboard, tzxFileName, entryNum);
        }
    }

    public void start() {
        //mCanvas.requestFocus();
        //mDisplayThread = new Thread(mDisplayDrawer);
        //mDisplayThread.start();
        mDisplayDrawer.start();
    }

    /**
     * Stops the applet.
     */
    public void
    stop() {
        /*try {
            mDisplayDrawer.stop();
            mDisplayThread.join();
            mDisplayThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        mDisplayDrawer.stop();
    }

    private void installListeners(HTMLElement container) {
        container.addEventListener(keydown, event -> {
            this.onKeyDown(event);
            return null;
        }, true);
        container.addEventListener(keyup, event -> {
            this.onKeyUp(event);
            return null;
        }, true);
    }

    private void onKeyDown(KeyboardEvent e) { e.preventDefault(); mKeyboard.PCKeyDown(e.key, e.shiftKey, e.ctrlKey, e.altKey); }

    private void onKeyUp(KeyboardEvent e) {
        e.preventDefault(); mKeyboard.PCKeyUp(e.key, e.shiftKey, e.ctrlKey, e.altKey);
    }

    /*public void windowClosing(WindowEvent e) {
        stop();
        System.exit(0);
    }*/

    private void windowActive(boolean active) {
        mDisplayDrawer.setPaused(!active);
    }

    /*public void focusGained(FocusEvent e) {
        windowActive(true);
    }

    public void focusLost(FocusEvent e) {
        windowActive(false);
    }*/
}
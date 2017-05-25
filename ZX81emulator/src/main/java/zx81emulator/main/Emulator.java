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
package zx81emulator.main;

import zx81emulator.config.ZX81Config;
import zx81emulator.config.ZX81ConfigDefs;
import zx81emulator.display.AccDraw;
import zx81emulator.io.KBStatus;
import zx81emulator.zx81.ZX81;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * This class allows display of the emulator as an application or
 * applet.
 *
 * @author Simon Holdsworth
 */
public class Emulator
        implements KeyListener, WindowListener, ActionListener, FocusListener {
    private AccDraw mDisplayDrawer;
    private KBStatus mKeyboard;
    private ZX81Config mConfig;

    private Canvas mCanvas;
    private Thread mDisplayThread;
    private Button mPauseButton;
    private Button mResetButton;

    public static void main(String[] args) {

        try {
            Emulator emulator = new Emulator();
            Frame f = new Frame("ZX81 Emulator");
            emulator.init(args, f);
            f.addWindowListener(emulator);
            f.addKeyListener(emulator);
            f.pack();
            f.setVisible(true);
            emulator.start();
        }
        catch (Exception exc) {
            System.out.println("Error: " + exc);
            exc.printStackTrace();
        }
    }

    private Emulator() {
        // One-off initialisation.
        mConfig = new ZX81Config();
        mConfig.machine = new ZX81();
        mConfig.load_config();
        mConfig.zx81opts.cwd = ".";
        mConfig.zx81opts.m1not = 32768;
    }

    private void init(String[] args, Container container) throws IOException {
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

        init(tzxFileName, hires, scale, container);
    }

    private void init(String tzxFileName, String hires, String scale, Container container)  throws IOException {
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
        mKeyboard = new KBStatus(mConfig);

        // Set up the various components.
        container.setLayout(new BorderLayout());
        container.addKeyListener(this);
        container.addFocusListener(this);
        Panel bottomPanel = new Panel();
        bottomPanel.setLayout(new BorderLayout());
        container.add(bottomPanel, "South");
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomPanel.add(buttonPanel, "East");
        mPauseButton = new Button("Pause");
        mPauseButton.addActionListener(this);
        buttonPanel.add(mPauseButton);
        mResetButton = new Button("Reset");
        mResetButton.addActionListener(this);
        buttonPanel.add(mResetButton);
        Label mStatusLabel = new Label("status");
        bottomPanel.add(mStatusLabel, "Center");

        // Set up the display.
        mDisplayDrawer = new AccDraw(mConfig, mStatusLabel, false, scaleCanvas);
        mCanvas = mDisplayDrawer.getCanvas();
        mCanvas.addFocusListener(this);
        mCanvas.addKeyListener(this);
        container.add(mCanvas, "Center");

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
        mCanvas.requestFocus();
        mDisplayThread = new Thread(mDisplayDrawer);
        mDisplayThread.start();
    }

    /**
     * Stops the applet.
     */
    public void
    stop() {
        mDisplayDrawer.stop();
        mDisplayThread.stop();
        mDisplayThread = null;
    }

    public void keyPressed(KeyEvent e) {
        mKeyboard.PCKeyDown(e.getKeyCode());
    }

    public void keyReleased(KeyEvent e) {
        mKeyboard.PCKeyUp(e.getKeyCode());
    }

    public void keyTyped(KeyEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        stop();
        System.exit(0);
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Button) {
            Button button = (Button) e.getSource();
            if (button.getLabel().equals("Pause"))
                windowActive(false);
            else if (button.getLabel().equals("Start"))
                windowActive(true);
            else if (button.getLabel().equals("Reset")) {
                try {
                    mConfig.machine.initialise(mConfig);
                    mCanvas.requestFocus();
                    windowActive(true);
                } catch (IOException e1) {
                    System.err.println("Reset failed");
                    e1.printStackTrace();
                }
            }
        }
    }

    private void windowActive(boolean active) {
        mDisplayDrawer.setPaused(!active);
        mPauseButton.setLabel(active ? "Pause" : "Start");
    }

    public void focusGained(FocusEvent e) {
        windowActive(true);
    }

    public void focusLost(FocusEvent e) {
        if (e.getOppositeComponent() != mPauseButton &&
                e.getOppositeComponent() != mResetButton &&
                e.getOppositeComponent() != mCanvas)
            windowActive(false);
    }
}
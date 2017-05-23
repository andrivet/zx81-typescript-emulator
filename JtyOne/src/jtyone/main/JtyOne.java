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
package jtyone.main;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import jtyone.config.ZX81Config;
import jtyone.config.ZX81ConfigDefs;
import jtyone.display.AccDraw;
import jtyone.io.KBStatus;
import jtyone.zx81.ZX81;

/**
 * This class allows display of the Jtyone emulator as an application or
 * applet.
 * 
 * @author Simon Holdsworth
 */
public class JtyOne 
extends Applet
implements KeyListener, WindowListener, ActionListener, FocusListener
  {
  private AccDraw mDisplayDrawer;
  private KBStatus mKeyboard;
  private ZX81Config mConfig;
  
  private Canvas mCanvas;
  private Thread mDisplayThread;
  private Button mPauseButton;
  private Button mResetButton;
  private Label mStatusLabel;
  private Label mJtyOneLabel;
  
  /**
   * @param args
   */
  public static 
  void main(String[] args) 
    {
    JtyOne jtyone = new JtyOne();
    Frame f = new Frame("JtyOne Emulator Window");
    jtyone.init(args,f,false);
    // Focus listening is only done for the application. For the applet, that's
    // handled via javascript on the web page.
    f.addWindowListener(jtyone);
    f.addKeyListener(jtyone);
    f.pack();    
    f.setVisible(true);
    jtyone.start();
    }
  
  /** Constructor.
   */
  public
  JtyOne()
    {
    // One-off initialisation.
    mConfig = new ZX81Config();
    mConfig.machine = new ZX81();
    mConfig.load_config();
    mConfig.zx81opts.cwd = ".";
    mConfig.zx81opts.m1not = 32768;
    }
  
  /**
   *  
   */
  public
  void init()
    {
    init(getParameter("tzxFileName"),
         getParameter("hires"),
         getParameter("scale"),
         getParameter("machine"),
         this,true);
    }
  

  /**
   *
   */
  public
  void init(String[] args, Container container, boolean applet )
    {   
    String tzxFileName = (args.length > 0 && !args[0].startsWith("-")) ? args[0] : null;
    String scale = null;
    String hires = null;
    String machine = null;
    
    for( int aPos = tzxFileName == null ? 0 : 1; aPos < args.length; aPos++ )
      {
      if( args[aPos].equals("-scale") && aPos < args.length-1 )
        {
        scale = args[++aPos];    
        }
      if( args[aPos].equals("-hires") && aPos < args.length-1 )
        {
        hires = args[++aPos];    
        }
      if( args[aPos].equals("-machine") && aPos < args.length-1 )
        {
        machine = args[++aPos];    
        }
      }

    init(tzxFileName,hires,scale,machine,container,applet);
    }
  
  /**
   * 
   */
  private
  void init(String tzxFileName, String hires, String scale, String machine, Container container, boolean applet )
    {
    // Sort out any configured options for the emulator.
    if( "ZX80".equals(machine) )
      {
      mConfig.machine.CurRom = mConfig.zx81opts.ROM80;
      mConfig.zx81opts.machine = ZX81ConfigDefs.MACHINEZX80;
      }
    else 
      mConfig.machine.CurRom = mConfig.zx81opts.ROM81;
    
    int scaleCanvas = 2;
    if( scale != null && scale.length() > 0 )
      scaleCanvas = Integer.parseInt(scale);
    
    if( "qs".equals(hires) )
      mConfig.zx81opts.chrgen = ZX81ConfigDefs.CHRGENQS;
    else if( "dk".equals(hires) )
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
    container.add(bottomPanel,"South");
    Panel buttonPanel = new Panel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
    bottomPanel.add(buttonPanel,"East");
    mPauseButton = new Button("Pause");
    mPauseButton.addActionListener(this);
    buttonPanel.add(mPauseButton);
    mResetButton = new Button("Reset");
    mResetButton.addActionListener(this);
    buttonPanel.add(mResetButton);
    mStatusLabel = new Label("status");
    bottomPanel.add(mStatusLabel,"Center");
    mJtyOneLabel = new Label("JtyOne");
    bottomPanel.add(mJtyOneLabel,"West");

    // Set up the display.
    mDisplayDrawer = new AccDraw(mConfig,mStatusLabel,false,scaleCanvas);
    mCanvas = mDisplayDrawer.getCanvas();
    mCanvas.addFocusListener(this);
    mCanvas.addKeyListener(this);
    container.add(mCanvas,"Center");

    // Load the .TZX file.
    try 
      {
      if( tzxFileName != null )
        {
        String tzxEntry = null;
        int entryNum = 0;
        int atPos = tzxFileName.indexOf('@');
        if( atPos != -1 )
          {
          tzxEntry = tzxFileName.substring(atPos+1);
          tzxFileName = tzxFileName.substring(0,atPos);
          entryNum = Integer.parseInt(tzxEntry);
          }

        mConfig.machine.getTape().loadTZX(mConfig,mKeyboard,tzxFileName,entryNum,applet);
        }
      }
    catch( Exception exc )
      {
      System.out.println("Error: "+exc);
      exc.printStackTrace();
      }
    }

  public
  void start()
    {   
    mCanvas.requestFocus();
    mDisplayThread = new Thread(mDisplayDrawer);
    mDisplayThread.start();           
    }

  /**Stops the applet.
   */
  public void
  stop()
    {
    mDisplayDrawer.stop();
    mDisplayThread.stop();
    mDisplayThread = null;
    }
  
  public void keyPressed(KeyEvent e)
    {
    mKeyboard.PCKeyDown(e.getKeyCode());
    }
  
  public void keyReleased(KeyEvent e) 
    {
    mKeyboard.PCKeyUp(e.getKeyCode());
    }
  
  public void keyTyped(KeyEvent e) 
    {
    }
  
  public void windowOpened(WindowEvent e) { }

  public void windowClosing(WindowEvent e) 
    { 
    stop();
    System.exit(0);
    }

  public void windowClosed(WindowEvent e) 
    { 
    }

  public void windowIconified(WindowEvent e) { }

  public void windowDeiconified(WindowEvent e) { }

  public void windowActivated(WindowEvent e) { }

  public void windowDeactivated(WindowEvent e) { }
  
  public void actionPerformed(ActionEvent e) 
    {
    if( e.getSource() instanceof Button )
      {
      Button button = (Button)e.getSource();
      if( button.getLabel().equals("Pause") )
        windowActive(false);
      else if( button.getLabel().equals("Start") )
        windowActive(true);
      else if( button.getLabel().equals("Reset") )
        {
        mConfig.machine.initialise(mConfig);
        mCanvas.requestFocus();
        windowActive(true);
        }
      }
    }

  public void windowActive(boolean active)
    {
    mDisplayDrawer.setPaused(!active);
    mPauseButton.setLabel(active?"Pause":"Start");
    }

  public void focusGained(FocusEvent e) 
    {
    windowActive(true);
    }

  public void focusLost(FocusEvent e) 
    {
    if( e.getOppositeComponent() != mPauseButton &&
        e.getOppositeComponent() != mResetButton &&
        e.getOppositeComponent() != mCanvas )
      windowActive(false);
    }    
  }
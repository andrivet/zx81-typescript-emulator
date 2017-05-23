/* z80_macros.h: Some commonly used z80 things as macros
   Copyright (c) 1999-2001 Philip Kendall
   Java translation (C) 2006 Simon Holdsworth
  
   $Id: z80_macros.h,v 1.19 2003/02/10 15:04:12 pak21 Exp $

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

   Author contact information:

   E-mail: pak@ast.cam.ac.uk
   Postal address: 15 Crescent Road, Wokingham, Berks, RG40 2DB, England

*/

package jtyone.z80;

/* Macros used for accessing the registers */

public abstract class Register
  {
  public String name;
  public abstract int get();
  public abstract void set(int v);
  public abstract void set(Register r);
  public abstract int postInc();
  public abstract int postDec();
  public abstract void inc();
  public abstract void dec();
  public abstract void and(int a);
  public abstract void or(int o);
  public abstract void add(int a);
  public String toString() { return "$"+Integer.toHexString(get()+0x100).substring(1).toUpperCase(); }
  public String toBinaryString() { return "b"+Integer.toBinaryString(get()+0x100).substring(1); }
  }
  
final class RegisterHigh extends Register
  {
  private MasterRegisterPair rp;
  RegisterHigh(MasterRegisterPair rp,String name) {this.rp = rp; this.name=name;}
  public int get() { return rp.hi(); }
  public void set(int v) { rp.setHi(v); }
  public void set(Register r) { rp.setHi(r.get()); }
  public int postInc() { int val = rp.hi(); rp.setHi(val+1); return val; }
  public int postDec() { int val = rp.hi(); rp.setHi(val-1); return val; }
  public void inc() { rp.setHi(rp.hi()+1); }
  public void dec() { rp.setHi(rp.hi()-1); }
  
  // TODO:  BUG FIX!!!!!
  
  public void and(int a) { rp.word &= ((a<<8)|0xff); }
  public void or(int o) { rp.word |= (o<<8); }    
  public void add(int a) { rp.setHi(rp.hi() + a); }
  }
  
final class RegisterLow extends Register
  {
  private MasterRegisterPair rp;
  public RegisterLow(MasterRegisterPair rp,String name) {this.rp = rp; this.name=name;}
  public int get() { return rp.lo(); }
  public void set(int v) { rp.setLo(v); }
  public void set(Register r) { rp.setLo(r.get()); }
  public int postInc() { int val = rp.lo(); rp.setLo(val+1); return val; }
  public int postDec() { int val = rp.lo(); rp.setLo(val-1); return val; }
  public void inc() { rp.setLo(rp.lo()+1); }
  public void dec() { rp.setLo(rp.lo()-1); }
  
  // TODO:  BUG FIX!!!!!
  
  public void and(int a) { rp.word &= (a|0xff00); }
  public void or(int o) { rp.word |= o; }    
  public void add(int a) { rp.setLo(rp.lo() + a); }
  }

// This class is here to allow a non-register 8-bit value to be updated by
// macros.
final class value8 extends Register
  {
  private int value;
  value8(String name) {this.value = 0; this.name=name;}
  public int get() { return value; }
  public void set(int v) { value = v&0xff; }
  public void set(Register r) { value = r.get(); }
  public int postInc() { int val = value; value = (value+1)&0xff; return val; }
  public int postDec() { int val = value; value = (value-1)&0xff; return val; }
  public void inc() { value = (value+1)&0xff; }
  public void dec() { value = (value-1)&0xff; }
  public void and(int a) { value = value & a; }
  public void or(int o) { value = value | o; }
  public void add(int a) { value = (value+a)&0xff; }
  }

final class MasterRegister extends Register
  {
  int value;
  public MasterRegister(String name) {this.value = 0; this.name=name;}
  public int get() { return value; }
  public void set(int v) { value = v&0xff; }
  public void set(Register r) { value = r.get(); }
  public int postInc() { int val = value; value = (value+1)&0xff; return val; }
  public int postDec() { int val = value; value = (value-1)&0xff; return val; }
  public void inc() { value = (value+1)&0xff; }
  public void dec() { value = (value-1)&0xff; }
  public void and(int a) { value = value & a; }
  public void or(int o) { value = value | o; }    
  public void add(int a) { value = (value+a)&0xff; }
  }
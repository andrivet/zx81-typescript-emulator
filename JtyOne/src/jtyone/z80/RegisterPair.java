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

public interface RegisterPair {
    int get();

    void set(int w);

    void set(RegisterPair rp);

    int postInc();

    void inc();

    int postDec();

    int preDec();

    void dec();

    void add(int a);

    Register getRH(String name);

    Register getRL(String name);

    String toString();

    String getName();
}

final class MasterRegisterPair
        implements RegisterPair {
    int word;
    public String name;

    public MasterRegisterPair(String name) {
        this.name = name;
    }

    public int hi() {
        return word >> 8;
    }

    public int lo() {
        return word & 0xff;
    }

    public int get() {
        return word;
    }

    public void setHi(int h) {
        word = ((h & 0xff) << 8) + (word & 0xff);
    }

    public void setLo(int l) {
        word = (word & 0xff00) + (l & 0xff);
    }

    public void set(int w) {
        word = w & 0xffff;
    }

    public void set(RegisterPair rp) {
        word = rp.get();
    }

    public int postInc() {
        int oldVal = word;
        word = (word + 1) & 0xffff;
        return oldVal;
    }

    public void inc() {
        word = (word + 1) & 0xffff;
    }

    public int postDec() {
        int oldVal = word;
        word = (word - 1) & 0xffff;
        return oldVal;
    }

    public int preDec() {
        word = (word - 1) & 0xffff;
        return word;
    }

    public void dec() {
        word = (word - 1) & 0xffff;
    }

    public void add(int a) {
        word = (word + a) & 0xffff;
    }

    public Register getRH(String name) {
        return new RegisterHigh(this, name);
    }

    public Register getRL(String name) {
        return new RegisterLow(this, name);
    }

    public String toString() {
        return "$" + Integer.toHexString(get() + 0x10000).substring(1).toUpperCase();
    }

    public String getName() {
        return name;
    }
}

final class SlaveRegisterPair
        implements RegisterPair {
    MasterRegister hi;
    MasterRegister low;
    public String name;

    public SlaveRegisterPair(String name) {
        this.name = name;
    }

    public int get() {
        return (hi.value << 8) + low.value;
    }

    public void set(int w) {
        hi.value = (w >> 8) & 0xff;
        low.value = w & 0xff;
    }

    public void set(RegisterPair rp) {
        int word = rp.get();
        hi.value = word >> 8;
        low.value = word & 0xff;
    }

    public int postInc() {
        int oldVal = (hi.value << 8) + low.value;
        int word = (oldVal + 1) & 0xffff;
        hi.value = word >> 8;
        low.value = word & 0xff;
        return oldVal;
    }

    public void inc() {
        int word = ((hi.value << 8) + low.value + 1) & 0xffff;
        hi.value = word >> 8;
        low.value = word & 0xff;
    }

    public int postDec() {
        int oldVal = (hi.value << 8) + low.value;
        int word = (oldVal - 1) & 0xffff;
        hi.value = word >> 8;
        low.value = word & 0xff;
        return oldVal;
    }

    public int preDec() {
        int word = ((hi.value << 8) + low.value - 1) & 0xffff;
        hi.value = word >> 8;
        low.value = word & 0xff;
        return word;
    }

    public void dec() {
        int word = ((hi.value << 8) + low.value - 1) & 0xffff;
        hi.value = word >> 8;
        low.value = word & 0xff;
    }

    public void add(int a) {
        int word = ((hi.value << 8) + low.value + a) & 0xffff;
        hi.value = word >> 8;
        low.value = word & 0xff;
    }

    public Register getRH(String name) {
        hi = new MasterRegister(name);
        return hi;
    }

    public Register getRL(String name) {
        low = new MasterRegister(name);
        return low;
    }

    public String toString() {
        return "$" + Integer.toHexString(get() + 0x10000).substring(1).toUpperCase();
    }

    public String getName() {
        return name;
    }
}
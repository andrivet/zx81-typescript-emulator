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
package zx81emulator.z80;


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
    private String name;

    MasterRegisterPair(String name) {
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
    private MasterRegister hi;
    private MasterRegister low;
    private String name;

    SlaveRegisterPair(String name) {
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
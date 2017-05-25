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
package zx81emulator.config;


public interface ZX81ConfigDefs {
    int EMUID = 0x85;
    int MAJORVERSION = 0;
    int MINORVERSION = 5;

    int SYNCTYPEH = 1;
    int SYNCTYPEV = 2;

    int BORDERNONE = 0;
    int BORDERSMALL = 1;
    int BORDERNORMAL = 2;
    int BORDERLARGE = 3;
    int BORDERFULL = 4;

    //int MACHINEZX80   = 0;
    int MACHINEZX81 = 1;
    //int MACHINEACE    = 2;
    //int MACHINETS1500 = 3;
    //int MACHINELAMBDA = 4;
    //int MACHINEZX97LE = 5;
    //int MACHINESPEC48 = 6;
    //int MACHINEQL     = 7;

    int HIRESDISABLED = 0;
    int HIRESWRX = 1;
    int HIRESG007 = 2;
    int HIRESMEMOTECH = 3;

    int CHRGENSINCLAIR = 0;
    int CHRGENDK = 1;
    int CHRGENQS = 2;
    int CHRGENCHR16 = 3;
    int CHRGENLAMBDA = 4;
}
/* z81/xz81, Linux console and X ZX81/ZX80 emulators.
 * Copyright (C) 1994 Ian Collier. z81 changes (C) 1995-2001 Russell Marks.
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
 *
 * sound.h
 */

package jtyone.io;

/*
#ifndef SOUND_H
#define SOUND_H
*/

public interface SoundDefs
  {
//#define AY_TYPE_QUICKSILVA 1
//#define AY_TYPE_ZONX 2
//#define AY_TYPE_FULLER 3
//#define AY_TYPE_ACE 4
//#define AY_TYPE_SINCLAIR 5
//#define AY_TYPE_TIMEX 6
//#define AY_TYPE_BOLDFIELD 7
  public static final int AY_TYPE_QUICKSILVA = 1;
  public static final int AY_TYPE_ZONX = 2;
  public static final int AY_TYPE_FULLER = 3;
  public static final int AY_TYPE_ACE = 4;
  public static final int AY_TYPE_SINCLAIR = 5;
  public static final int AY_TYPE_TIMEX = 6;
  public static final int AY_TYPE_BOLDFIELD = 7;
  }
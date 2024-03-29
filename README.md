# ZX81 Emulator written in Typescript

## Sinclair ZX81

The [Sinclair ZX81](https://en.wikipedia.org/wiki/ZX81) was my first computer, in 1982. It is one of those home computers very popular in the '80s and affordable (£49.95 in kit). 
Other very popular computers were the [Commodore 64](https://en.wikipedia.org/wiki/Commodore_64) and the [Apple II](https://en.wikipedia.org/wiki/Apple_II) but were far more expensive.
The successor of the ZX81 was the [ZX Spectrum](https://en.wikipedia.org/wiki/ZX_Spectrum).

![ZX81](https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Sinclair-ZX81.png/320px-Sinclair-ZX81.png)

The ZX81 computer was based on a 8-bit processor ([`Z80A`](https://en.wikipedia.org/wiki/Zilog_Z80) from Zilog or `µPD780C` from NEC) at 3.5 MHz with 1 KiB of RAM and 8 KiB of ROM. It was possible the buy a RAM extension (I had a 16 KiB one).

The characters set was not based on `ASCII` but was custom and includes only capital letters. It includes also some graphic characters:

![CharactersSet](https://upload.wikimedia.org/wikipedia/commons/3/35/ZX81_characters_0x00-3F%2C_0x80-BF.png).

Using these characters, it was possible to make some graphics but it was very limited (64x48). Using some tricks (and sometimes some hardware modifications), it was possible to achieve (more or less) 256x192 (more or less because it was not possible to get all pixel combinations per line).

## Motivation

I wanted to learn [Typescript](https://www.typescriptlang.org) and thus, I was looking for a project not too small (not just an example), but also not too big.
I find also interresting to explore the question of how to transform a Java applet into a pure HTML5/Javascript program.

I find a HTML5/Javascript [ZX81 emulator made by Simon Holdsworth](http://www.zx81stuff.org.uk/zx81/jtyone.html), but I found only a minification version, so it was not usable.
However, on the same page, there is also a Java version. So I took this Java application as a starting point.

## Objectives

These are my objectives for this project:

* Find a way to transform automatically Java code into [Typescript](https://www.typescriptlang.org). The `Z80` emulation code is too big to be translated manually.
* Use the latest version of Typescript but at the same time, be able to run on all major Internet browsers (i.e. be compatible with Javascript ECMAScript 5).
* Use the latest version of [Webpack](https://webpack.js.org).
* Use [Promise](https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Promise), [fetch](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API), [async](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/async_function) and [await](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/await) syntax.

## Current status

Currently, all the objectives are fullfiled except those:

* There are still some Javascript code, in particular for Webpack configuration.
* The keyboard does not always display properly on iPad.

## Game

I found one of my first applications: a game I build in 1985. Texts were in French and I have translated them in English.
It was written with a mix of `BASIC` (to display the menu, the rules of the game, etc) and `Z80` assembly code (for the game itself).
I have recently (2023) rewritten it in pure Z80 assembler and with comments to explain the code.
The complete source code (with comments) of this game is [published on GitHub](https://github.com/andrivet/Factory-2023).

Want to try the game and the emulator? It is available at the following address: [www.andrivet.com/static/ZX81/]((http://www.andrivet.com/static/ZX81/)):

[![FACTORY](https://github.com/andrivet/zx81-typescript-emulator/raw/master/Programs/FACTORY.png)](http://www.andrivet.com/static/ZX81/)

## Files

| Folder | Description                                                                                                                                                                                                                                                  |
|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Documents | Contains some documents related to ZX81, such as the original manual [Sinclair ZX81 BASIC Programming](https://github.com/andrivet/zx81-typescript-emulator/raw/master/Documents/Sinclair%20ZX81%20BASIC%20Programming.pdf), the Z80 CPU User document, etc. |
| Emulator | The Typescript emulator                                                                                                                                                                                                                                      |
| Programs | The `factory2023` game in English                                                                                                                                                                                                                          |

## Credits and Copyrights

_**Note**: A full list of components used by the emulator can be found in [NOTICE.md](https://github.com/andrivet/zx81-typescript-emulator/blob/master/NOTICE.md)._

* **ZX81 Emulator written in Typescript** Copyright &copy; 2017-2023 Sebastien Andrivet.
* `FACTORY2023` game Copyright &copy; 1985-2023 Sebastien Andrivet.

This emulator is based on these software:

* C++ emulator [EightyOne](https://sourceforge.net/projects/eightyone-sinclair-emulator/) Copyright &copy; 2003-2006 Michael D Wynne and by others.
* Java emulator [JtyOne Java translation](http://www.zx81stuff.org.uk/zx81/JtyOne.1.3.src.zip) &copy; 2006 Simon Holdsworth and others.
* Z80 Emulation based on [Fuse - the Free Unix Spectrum Emulator](http://fuse-emulator.sourceforge.net) Copyright &copy; 2003 Philip Kendall.

## License

The emulator code is released under [GNU General Public License version 3 (GPLv3)](https://github.com/andrivet/zx81-typescript-emulator/blob/master/LICENSE). Components are released under their respective licenses.


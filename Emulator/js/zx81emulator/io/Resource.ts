/* ZX81emulator  - A ZX81 emulator.
 * EightyOne Copyright (C) 2003-2006 Michael D Wynne
 * JtyOne Java translation (C) 2006 Simon Holdsworth and others.
 * ZX81emulator Typescript/Javascript transcompilation (C) 2017 Sebastien Andrivet.
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

export interface Callback
{
    (data: ArrayBuffer): void;
}


export class Resource
{
    public data: ArrayBuffer;

    public get(name: string, callback: Callback)
    {
        let pathname: string = window.location.pathname;
        let dir: string = pathname.substring(0, pathname.lastIndexOf('/'));
        let request: XMLHttpRequest = new XMLHttpRequest();
        request.responseType = "arraybuffer";
        request.open("GET", dir + "/" + name, true);
        request.onreadystatechange = () =>
        {
            if (request.readyState === 4 && request.status === 200)
            {
                this.data = request.response;
                callback(this.data);
            }
            else if (request.status === 404)
            {
            }
        };
        request.send();
    }

    constructor()
    {
    }
}


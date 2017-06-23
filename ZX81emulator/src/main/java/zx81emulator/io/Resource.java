package zx81emulator.io;

import jsweet.dom.Blob;
import jsweet.dom.XMLHttpRequest;
import jsweet.lang.ArrayBuffer;

import static jsweet.dom.Globals.window;

public class Resource {

    public Blob blob;

    public void get(String name, Callback callback) {

        String pathname = window.location.pathname;
        String dir = pathname.substring(0, pathname.lastIndexOf('/'));
        XMLHttpRequest request = new XMLHttpRequest();
        request.responseType = "arraybuffer";
        request.open("GET", dir + "/" + name, true);

        request.onreadystatechange = (e) -> {
            if (request.readyState == 4 && request.status == 200) {
                this.blob = (Blob)request.response;
                callback.call();
            } else if (request.status == 404) {
                /*404 === this.status && (o.loadedLen = -1)*/
            }

            return 0;
        };
        request.send();
    }
}

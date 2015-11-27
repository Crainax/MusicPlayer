package com.ruffneck.player.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    public static String Stream2String(InputStream is, String charsetName) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len;
        while ((len = is.read(b)) != -1) {
            baos.write(b, 0, len);
        }
        return baos.toString(charsetName);
    }



}

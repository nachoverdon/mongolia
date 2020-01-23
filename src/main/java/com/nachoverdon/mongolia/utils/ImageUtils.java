package com.nachoverdon.mongolia.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ImageUtils {

    /**
     * Returns a base64 String of the image from the inputStream and the given MIME type.
     *
     * @param inputStream InputStream of the image
     * @param mimeType MIME type of the image https://www.sitepoint.com/mime-types-complete-list/
     * @return The image encoded to base64 or an empty string
     * @throws IOException
     */
    public static String imageToBase64(InputStream inputStream, String mimeType ) throws IOException {
        String base64Data = new String(
                Base64.encodeBase64(IOUtils.toByteArray(inputStream)),
                StandardCharsets.UTF_8.name()
        );

        return "data:" + mimeType + ";base64," + base64Data;
    }
}

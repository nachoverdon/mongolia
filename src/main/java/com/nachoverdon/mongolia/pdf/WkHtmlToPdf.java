package com.nachoverdon.mongolia.pdf;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class WkHtmlToPdf {

    private static final Logger log = LoggerFactory.getLogger(WkHtmlToPdf.class);

    /**
     * You can find documentation on wkhtmltopdf on https://wkhtmltopdf.org/docs.html
     *
     * Images:
     * Images cannot be directly linked, they need to be encoded to Base64 and added inline. To accomplish this, you can
     * use:
     *  {@link com.nachoverdon.mongolia.templating.MongoliaTemplatingFunctions#resourcesImageToBase64}
     *  {@link com.nachoverdon.mongolia.templating.MongoliaTemplatingFunctions#damImageToBase64}
     *
     * CSS:
     * Similarly, CSS must also be embedded using style tags instead of linked.
     * wkhtmltopdf uses Qt 4.8.* (Qt WebKit) rendering engine internally to render the page. Some CSS3 features, like
     * flexbox, are NOT supported by default. You might need to use older syntax. More info:
     * @see <a href="https://github.com/wkhtmltopdf/wkhtmltopdf/issues/1522#issuecomment-159767618">Github issue</a>
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/box-align">MDN Docs on box-align</a>
     *
     * Headers/Footers:
     * These should be .html files and must be accessible from the webapp module, so put them on your resources folder.
     * Ex: /yourmodule/src/main/resources/mgnl-resources/yourmodule/header.thml
     *
     * Fonts:
     * Fonts should be installed on the machine, or they wont load properly. Remember to install them on your production
     * environment as well.
     *
     * Note:
     * If you have the time, checkout Puppeteer, which might be a better alternative to wkhtmltopdf.
     * @see <a href="https://pptr.dev">Puppeteer</a>
     *
     * @param html The HTML you want to convert to PDF
     * @param parameters A list of wkhtmltopdf parameters. ex: Arrays.asList("--header-html", "my/path/header.html");
     * @return An InputStream with the PDF data.
     */
    public static InputStream generatePdfAsInputStream(String html, List<String> parameters) {
        List<String> command = Arrays.asList("wkhtmltopdf", "-q");

        // Return null if we have no parameters
        if (parameters != null && parameters.size() > 0)
            command.addAll(parameters);
        else
            return null;

        // This tells wkhtmltopdf to use stdin and stdout as input for the html and output for the pdf respectively
        command.addAll(Arrays.asList("-", "-"));

        Process process = null;

        try {
            // Create a process with the given command and parameters and start it
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            // HTML to PDF
            OutputStream outputStream = process.getOutputStream();
            IOUtils.write(html.getBytes(StandardCharsets.UTF_8.name()), outputStream);
            outputStream.close();

            InputStream inputStream = process.getInputStream();
            int status = process.waitFor();

            log.debug("Process exited with code: " + status);

            return inputStream;

        } catch (IOException e) {

            log.error(e.getMessage());

        } catch (InterruptedException e) {

            log.error(e.getMessage());

        } finally {
            if (process != null) process.destroy();
        }

        return null;
    }
}
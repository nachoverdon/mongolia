package com.nachoverdon.mongolia.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;

@Slf4j
public class WkHtmlToPdf {

  /**
   * In order to use wkhtmltopdf, you must install it and make it accessible on your machine's
   * path.
   * You can find documentation on wkhtmltopdf on https://wkhtmltopdf.org/docs.html
   * <p>
   *
   * Images:
   * Images cannot be directly linked, they need to be encoded to Base64 and added inline. To
   * accomplish this, you can
   * use:
   *  {@link com.nachoverdon.mongolia.templating.MongoliaTemplatingFunctions#resourcesImageToBase64}
   *  {@link com.nachoverdon.mongolia.templating.MongoliaTemplatingFunctions#damImageToBase64}
   *
   * CSS:
   * Similarly, CSS must also be embedded using style tags instead of linked.
   * wkhtmltopdf uses Qt 4.8.* (Qt WebKit) rendering engine internally to render the page. Some
   * CSS3 features, like flexbox, are NOT supported by default. You might need to use older
   * syntax. More info:
   * * <a href="https://github.com/wkhtmltopdf/wkhtmltopdf/issues/1522#issuecomment-159767618">Github issue</a>
   * * <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/box-align">MDN Docs on box-align</a>
   *
   * Headers/Footers:
   * These should be .html files and must be accessible from the webapp module, so put them on
   * your resources folder.
   * Ex: /yourmodule/src/main/resources/mgnl-resources/yourmodule/header.thml
   *
   * Fonts:
   * Fonts should be installed on the machine, or they wont load properly. Remember to install
   * them on your production environment as well.
   *
   * Note:
   * If you have the time, checkout Puppeteer, which might be a better alternative to wkhtmltopdf.
   * <a href="https://pptr.dev">Puppeteer</a>
   * </p>
   *
   * @param html The HTML you want to convert to PDF
   * @param parameters A list of wkhtmltopdf parameters.
   *                   ex: Arrays.asList("--header-html", "my/path/header.html");
   * @return An InputStream with the PDF data.
   */
  @Nullable
  public static InputStream generatePdfAsInputStream(String html, List<String> parameters) {
    List<String> command = Arrays.asList("wkhtmltopdf", "-q");

    // Return null if we have no parameters
    if (CollectionUtils.isEmpty(parameters)) {
      return null;
    }

    command.addAll(parameters);

    // This tells wkhtmltopdf to use stdin and stdout as input for the html and output for the pdf
    // respectively.
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
    } catch (IOException | InterruptedException e) {
      log.error(e.getMessage());
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return null;
  }
}

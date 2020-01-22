package com.nachoverdon.mongolia.pdf

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory

import java.io.*
import java.nio.charset.StandardCharsets
import java.util.Arrays

class WkHtmlToPdf {

    /**
     * You can find documentation on wkhtmltopdf on https://wkhtmltopdf.org/docs.html
     *
     * Images:
     * Images cannot be directly linked, they need to be encoded to Base64 and added inline. To accomplish this, you can
     * use resourcesImageToBase64() and damImageToBase64() from MongoTemplatingFunctions.
     *
     * CSS:
     * Similarly, CSS must also be embedded using <style> stags instead of linked.
     * wkhtmltopdf uses Qt 4.8.* (Qt WebKit) rendering engine internally to render the page. Some CSS3 features, like
     * flexbox, are NOT supported by default. You might need to use older syntax. More info:
     *      https://github.com/wkhtmltopdf/wkhtmltopdf/issues/1522#issuecomment-159767618
     *      https://developer.mozilla.org/en-US/docs/Web/CSS/box-align
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
     * If you have the time, checkout Puppeteer (https://pptr.dev), which might be a better alternative to wkhtmltopdf
     *
     * @param html The HTML you want to convert to PDF
     * @param parameters A list of wkhtmltopdf parameters. ex: Arrays.asList("--header-html", "my/path/header.html");
     * @return
     */
    fun generatePdfAsInputStream(html: String, parameters: List<String>?): InputStream? {

        val command = Arrays.asList("wkhtmltopdf", "-q")

        // Return null if we have no parameters
        if (parameters != null && parameters.size > 0)
            command.addAll(parameters)
        else
            return null

        // This tells wkhtmltopdf to use stdin and stdout as input for the html and output for the pdf respectively
        command.addAll(Arrays.asList("-", "-"))

        var process: Process? = null

        try {
            // Create a process with the given command and parameters and start it
            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            process = processBuilder.start()

            // HTML to PDF
            val outputStream = process!!.outputStream
            IOUtils.write(html.toByteArray(charset(StandardCharsets.UTF_8.name())), outputStream)
            outputStream.close()

            val inputStream = process.inputStream
            val status = process.waitFor()

            log.debug("Process exited with code: $status")

            return inputStream

        } catch (e: IOException) {

            log.error(e.message)

        } catch (e: InterruptedException) {

            log.error(e.message)

        } finally {

            process?.destroy()

        }

        return null
    }

    companion object {

        private val log = LoggerFactory.getLogger(WkHtmlToPdf::class.java)
    }
}

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

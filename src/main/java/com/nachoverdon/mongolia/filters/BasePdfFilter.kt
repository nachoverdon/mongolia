package com.nachoverdon.mongolia.filters

import com.nachoverdon.mongolia.pdf.WkHtmlToPdf
import com.nachoverdon.mongolia.utils.CharResponseWrapper
import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter
import org.apache.commons.io.IOUtils

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.io.InputStream
import java.util.Arrays

/**
 * This class uses WkHtmlToPdf to convert a page rendered with Magnolia into a PDF. To use it, extend from it and
 * override its methods. Then add the path to your extended filter on your Magnolia configuration on
 *  config:
 *    server:
 *      filters:
 *        cms:
 *          yourPdfFilter: (Make sure to put your filter BEFORE the "rendering" filter or it wont work)
 *            class: your.package.YourPdfFilter
 *            enabled: true
 *
 *
 * There are 4 methods that you might want to override.
 * checkCondition().
 *      Checks whether it should proceed with the PDF conversion. Override this class to check if the
 *      current page is should be converted, for exemple, by checking the node page's template.
 *
 * getParameters()
 *      A List of wkhtmltopdf parameters https://wkhtmltopdf.org/usage/wkhtmltopdf.txt. WkHtmlToPdf automatically
 *      includes ["-", "-"] as last parameters, so you don't need to include them.
 *
 * shouldDownload()
 *      Whether the file should be served or you want to do something else with it, like store it as a Resource Node.
 *
 * getFileName()
 *      The name of the served file.
 *
 *
 * Optionally, you can also override download() and action()
 * download()
 *      Will be triggered when shouldDownload() returns true. By default it serves the PDF file.
 *
 * action()
 *      Will be triggered when shouldDownload returns false. Does nothing unless overriden.
 *
 */
open class BasePdfFilter : OncePerRequestAbstractMgnlFilter() {
    var filterParameters: FilterParameters? = null;

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        filterParameters = FilterParameters(request, response, chain)

        if (checkCondition()) {
            val wk = WkHtmlToPdf()
            val newResponse = CharResponseWrapper(response)
            chain.doFilter(request, newResponse)

            val html: String? = newResponse.toString()

            if (html != null) {
                val parameters = getParameters()
                val inputStream = wk.generatePdfAsInputStream(html, parameters)

                if (shouldDownload())
                    download(inputStream)
                else
                    action()

                filterParameters = null
            }
        } else {
            chain.doFilter(request, response)
            filterParameters = null
            return
        }
    }

    open fun checkCondition(): Boolean {
        return true
    }

    open fun getParameters(): List<String> {
        return Arrays.asList("--print-media-type")
    }

    open fun shouldDownload(): Boolean {
        return true
    }

    open fun getFileName(): String {
        return "document"
    }

    @Throws(IOException::class)
    open fun download(inputStream: InputStream?) {
        IOUtils.copy(inputStream!!, filterParameters!!.response.outputStream)
        inputStream.close()

        val fileName = getFileName()
        filterParameters!!.response.setHeader("Content-Type", "application/pdf;charset=utf-8")
        filterParameters!!.response.setHeader("Content-Disposition", "attachment; filename=$fileName.pdf")
    }

    open fun action() {}

}

class FilterParameters(val request: HttpServletRequest, val response: HttpServletResponse, val chain: FilterChain)
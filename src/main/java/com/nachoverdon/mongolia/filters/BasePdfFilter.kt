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

open class BasePdfFilter : OncePerRequestAbstractMgnlFilter() {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val filterParameters = FilterParameters(request, response, chain)

        if (checkCondition(filterParameters)) {
            val wk = WkHtmlToPdf()
            val newResponse = CharResponseWrapper(response)
            chain.doFilter(request, newResponse)

            val html = newResponse.toString()

            if (html != null) {
                val parameters = getParameters(filterParameters)
                val inputStream = wk.generatePdfAsInputStream(html, parameters)

                if (shouldDownload(filterParameters))
                    download(filterParameters, inputStream)
                else
                    action(filterParameters)

            }
        } else {
            chain.doFilter(request, response)
            return
        }
    }

    open fun checkCondition(filterParameters: FilterParameters): Boolean {
        return true
    }

    open fun getParameters(filterParameters: FilterParameters): List<String> {
        return Arrays.asList("--print-media-type")
    }

    open fun shouldDownload(filterParameters: FilterParameters): Boolean {
        return true
    }

    open fun getFileName(filterParameters: FilterParameters): String {
        return "document"
    }

    @Throws(IOException::class)
    open fun download(filterParameters: FilterParameters, inputStream: InputStream?) {
        IOUtils.copy(inputStream!!, filterParameters.response.outputStream)
        inputStream.close()

        val fileName = getFileName(filterParameters)
        filterParameters.response.setHeader("Content-Type", "application/pdf;charset=utf-8")
        filterParameters.response.setHeader("Content-Disposition", "attachment; filename=$fileName.pdf")
    }

    open fun action(filterParameters: FilterParameters) {}

}

class FilterParameters(val request: HttpServletRequest, val response: HttpServletResponse, val chain: FilterChain)
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

class BasePdfFilter : OncePerRequestAbstractMgnlFilter() {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (checkCondition(request, response, chain)) {
            val wk = WkHtmlToPdf()
            val newResponse = CharResponseWrapper(response)
            chain.doFilter(request, newResponse)

            val html = newResponse.toString()

            if (html != null) {
                val parameters = getParameters(request, response, chain)
                val inputStream = wk.generatePdfAsInputStream(html, parameters)

                if (shouldDownload(request, response, chain))
                    download(request, response, chain, inputStream)
                else
                    action(request, response, chain)

            }
        } else {
            chain.doFilter(request, response)
            return
        }
    }

    fun checkCondition(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): Boolean {
        return true
    }

    fun getParameters(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): List<String> {
        return Arrays.asList("--print-media-type")
    }

    fun shouldDownload(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): Boolean {
        return true
    }

    fun getFileName(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): String {
        return "document"
    }

    @Throws(IOException::class)
    fun download(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain, inputStream: InputStream?) {
        IOUtils.copy(inputStream!!, response.outputStream)
        inputStream.close()

        val fileName = getFileName(request, response, chain)
        response.setHeader("Content-Type", "application/pdf;charset=utf-8")
        response.setHeader("Content-Disposition", "attachment; filename=$fileName.pdf")
    }

    fun action(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {}

}

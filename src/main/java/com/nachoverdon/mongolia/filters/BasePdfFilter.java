package com.nachoverdon.mongolia.filters;

import com.nachoverdon.mongolia.pdf.WkHtmlToPdf;
import org.apache.commons.io.IOUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * This class uses WkHtmlToPdf to convert a page rendered with Magnolia into a PDF. To use it, extend from it and
 * override its methods. Then add the path to your extended filter on your Magnolia configuration on
 * <pre>{@code
 *  config:
 *    server:
 *      filters:
 *        cms:
 *          yourPdfFilter: (Make sure to put your filter BEFORE the "rendering" filter or it wont work)
 *            class: your.package.YourPdfFilter
 *            enabled: true
 * }</pre>
 *
 * There is 1 method that you MUST override and and a few others that you might want to override.
 * {@link #checkCondition}
 *      Must be overridden. Checks whether it should proceed with the PDF conversion. Override this class to check if the
 *      current page is should be converted, for example, by checking the node page's template.
 *
 * {@link #getParameters}
 *      A List of wkhtmltopdf parameters. Automatically includes ["-", "-"] as last parameters, so you don't need to
 *      include them. <a href="https://wkhtmltopdf.org/usage/wkhtmltopdf.txt">wkthmltopdf docs</a>
 *
 * {@link #shouldDownload}
 *      Whether the file should be served or you want to do something else with it, like store it as a Resource Node.
 *
 * {@link #getFileName}
 *      The name of the served file.
 *
 * Optionally, you can also override {@link #download} and {@link #action}
 * {@link #download}
 *      Will be triggered when {@link #shouldDownload} returns true. By default it serves the PDF file.
 *
 * {@link #action}
 *      Will be triggered when shouldDownload returns false. Does nothing unless overridden.
 *
 */
abstract public class BasePdfFilter extends BaseFilter {
    protected FilterParameters filterParameters = null;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        filterParameters = new FilterParameters(request, response, chain);

        if (checkCondition()) {
            CharResponseWrapper newResponse = new CharResponseWrapper(response);
            chain.doFilter(request, newResponse);

            String html = newResponse.toString();

            if (html != null) {
                List<String> parameters = getParameters();
                InputStream inputStream = WkHtmlToPdf.generatePdfAsInputStream(html, parameters);

                if (shouldDownload())
                    download(inputStream);
                else
                    action();

                filterParameters = null;
            }
        } else {
            filterParameters = null;
            chain.doFilter(request, response);
        }
    }

    /**
     * Checks if the request should be processed.
     *
     * @return true if it should be processed.
     */
    abstract protected boolean checkCondition();

    /**
     * Action to be performed when the PDF is not set to be downloaded.
     *
     */
    protected void action() {}

    /**
     * Gets a list of wkhtmltopdf parameters.
     *
     * @return A list of parameters for wkhtmltopdf
     */
    protected List<String> getParameters() {
        return Collections.singletonList("--print-media-type");
    }

    /**
     * Checks if it should serve the generated PDF as download.
     *
     * @return true if it should serve it.
     */
    protected boolean shouldDownload() {
        return true;
    }

    /**
     * Gets the file name that the served PDF wil have without extension.
     *
     * @return The file name
     */
    protected String getFileName() {
        return "document";
    }

    /**
     * The action to be performed if the generated PDf is set to be downloaded.
     * Copies the content of the given InputStream into the response and serves the file.
     *
     * @param inputStream The generated PDF data.
     * @throws IOException Possible exception when handling the streams
     */
    protected void download(InputStream inputStream) throws IOException {
        IOUtils.copy(inputStream, filterParameters.getResponse().getOutputStream());
        inputStream.close();

        String fileName = getFileName();
        filterParameters.getResponse().setHeader("Content-Type", "application/pdf;charset=utf-8");
        filterParameters.getResponse().setHeader("Content-Disposition", "attachment; filename=" + fileName
                + ".pdf");
    }

}
package com.nachoverdon.mongolia.utils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CharResponseWrapper extends HttpServletResponseWrapper {

    private CharArrayWriter charWriter;
    private PrintWriter writer;
    private boolean getOutputStreamCalled;
    private boolean getWriterCalled;

    public CharResponseWrapper(HttpServletResponse response) {
        super(response);
        this.setCharacterEncoding("UTF-8");
        charWriter = new CharArrayWriter();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        if (getWriterCalled) {
            throw new IllegalStateException("getWriter already called");
        }

        getOutputStreamCalled = true;
        return super.getOutputStream();
    }

    public PrintWriter getWriter() {
        if (writer != null) {
            return writer;
        }
        if (getOutputStreamCalled) {
            throw new IllegalStateException("getOutputStream already called");
        }
        getWriterCalled = true;
        writer = new PrintWriter(charWriter);
        return writer;
    }

    public String toString() {
        String s = null;

        if (writer != null) {
            s = charWriter.toString();
        }
        return s;
    }
}
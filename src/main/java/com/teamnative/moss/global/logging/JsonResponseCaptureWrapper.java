package com.teamnative.moss.global.logging;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

final class JsonResponseCaptureWrapper extends HttpServletResponseWrapper {
    private final int maxCaptureBytes;
    private final ByteArrayOutputStream capture = new ByteArrayOutputStream();
    private TeeServletOutputStream teeOutputStream;
    private PrintWriter writer;
    private boolean outputStreamRequested;
    private boolean writerRequested;
    private boolean truncated;

    JsonResponseCaptureWrapper(HttpServletResponse response, int maxCaptureBytes) {
        super(response);
        this.maxCaptureBytes = maxCaptureBytes;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writerRequested) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }
        outputStreamRequested = true;
        return teeOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStreamRequested) {
            throw new IllegalStateException("getOutputStream() has already been called on this response.");
        }
        writerRequested = true;
        if (writer == null) {
            String encoding = getCharacterEncoding();
            Charset charset = encoding == null ? StandardCharsets.ISO_8859_1 : Charset.forName(encoding);
            writer = new PrintWriter(new OutputStreamWriter(teeOutputStream(), charset));
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        } else if (teeOutputStream != null) {
            teeOutputStream.flush();
        }
        super.flushBuffer();
    }

    byte[] capturedJsonBody() {
        if (writer != null) {
            writer.flush();
        }
        return isJsonContentType() ? capture.toByteArray() : new byte[0];
    }

    boolean isTruncated() {
        return truncated;
    }

    private TeeServletOutputStream teeOutputStream() throws IOException {
        if (teeOutputStream == null) {
            teeOutputStream = new TeeServletOutputStream(super.getOutputStream());
        }
        return teeOutputStream;
    }

    private boolean isJsonContentType() {
        String contentType = getContentType();
        if (contentType == null) {
            return false;
        }
        String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(java.util.Locale.ROOT);
        return mediaType.equals("application/json") || mediaType.endsWith("+json");
    }

    private void capture(byte[] bytes, int offset, int length) {
        int remaining = maxCaptureBytes - capture.size();
        int bytesToCapture = Math.min(remaining, length);
        if (bytesToCapture > 0) {
            capture.write(bytes, offset, bytesToCapture);
        }
        if (bytesToCapture < length) {
            truncated = true;
        }
    }

    private final class TeeServletOutputStream extends ServletOutputStream {
        private final ServletOutputStream delegate;

        private TeeServletOutputStream(ServletOutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int value) throws IOException {
            delegate.write(value);
            byte[] oneByte = {(byte) value};
            capture(oneByte, 0, 1);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            delegate.write(bytes, offset, length);
            capture(bytes, offset, length);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            delegate.setWriteListener(writeListener);
        }
    }
}

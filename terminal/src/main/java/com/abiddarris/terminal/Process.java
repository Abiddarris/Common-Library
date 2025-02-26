package com.abiddarris.terminal;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Process {

    private final Future<Integer> future;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final InputStream errorStream;

    Process(Future<Integer> future, InputStream inputStream, OutputStream outputStream, InputStream errorStream) {
        this.future = future;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.errorStream = errorStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public int getResultCode() throws ExecutionException, InterruptedException {
        return future.get();
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getErrorStream() {
        return errorStream;
    }
}

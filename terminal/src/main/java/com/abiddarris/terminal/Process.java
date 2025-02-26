package com.abiddarris.terminal;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Process {

    private final Future<Integer> future;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    Process(Future<Integer> future, InputStream inputStream, OutputStream outputStream) {
        this.future = future;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
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
}

package com.abiddarris.terminal;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Process {

    private final Future<Integer> future;
    private final InputStream inputStream;

    Process(Future<Integer> future, InputStream inputStream) {
        this.future = future;
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public int getResultCode() throws ExecutionException, InterruptedException {
        return future.get();
    }
}

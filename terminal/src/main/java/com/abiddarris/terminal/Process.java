package com.abiddarris.terminal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Process {

    private final Future<Integer> future;

    Process(Future<Integer> future) {
        this.future = future;
    }

    public int getResultCode() throws ExecutionException, InterruptedException {
        return future.get();
    }
}

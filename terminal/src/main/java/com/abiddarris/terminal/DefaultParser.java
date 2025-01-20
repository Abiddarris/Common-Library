package com.abiddarris.terminal;

public class DefaultParser implements Parser {

    @Override
    public String[] parse(String command) {
        return command.split(" ");
    }
}

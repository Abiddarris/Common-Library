package com.abiddarris.common.utils;

import java.util.Random;

public final class Randoms {

    private static final String[] CHARACTERS;
    private static final Random RANDOM = new Random();

    static {
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String upperCase = lowerCase.toUpperCase();
        String number = "1234567890";

        String character = lowerCase + upperCase + number;
        CHARACTERS = character.split("");
    }

    public static String newRandomString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = randomInt(0, CHARACTERS.length);
            if(index == CHARACTERS.length) {
                index--;
            }

            builder.append(CHARACTERS[index]);
        }

        return builder.toString();
    }

    public static int randomInt(int start, int end) {
        return start + randomInt(end - start);
    }

    public static int randomInt(int end) {
        return (int) (Math.random() * end);
    }

}

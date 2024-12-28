package com.abiddarris.common.utils;

public final class Randoms {

    private static final String[] CHARACTERS;

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
            int index = (int)(Math.random() * CHARACTERS.length);
            if(index == CHARACTERS.length) {
                index--;
            }

            builder.append(CHARACTERS[index]);
        }

        return builder.toString();
    }

}

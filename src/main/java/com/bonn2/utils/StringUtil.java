package com.bonn2.utils;

import org.jetbrains.annotations.NotNull;

public class StringUtil {

    public static @NotNull String capitalize(@NotNull String string) {
        String[] words = string.split("[\s_]");
        StringBuilder capitalizeWord = new StringBuilder();
        for(String w : words){
            String first = w.substring(0,1);
            String afterFirst = w.substring(1);
            capitalizeWord.append(first.toUpperCase()).append(afterFirst).append(" ");
        }
        return capitalizeWord.toString().trim();
    }
}

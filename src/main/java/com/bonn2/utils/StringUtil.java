package com.bonn2.utils;

public class StringUtil {

    public static String capitalize(String string) {
        String[] words = string.split("[\s_-]");
        StringBuilder capitalizeWord = new StringBuilder();
        for(String w : words){
            String first = w.substring(0,1);
            String afterFirst = w.substring(1);
            capitalizeWord.append(first.toUpperCase()).append(afterFirst).append(" ");
        }
        return capitalizeWord.toString().trim();
    }
}

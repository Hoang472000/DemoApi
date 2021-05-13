package com.example.demoapi;


import android.util.Log;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StandardizedString {


    public String standardized(String str) {
        str = str.trim();
        if (str.equals("") == false) {
            str = str.replaceAll("\\s+", " ");
            String temp[] = str.split(" ");
            str = "";
            for (int i = 0; i < temp.length; i++) {
                str += String.valueOf(temp[i].charAt(0)).toUpperCase() + temp[i].substring(1);
                if (i < temp.length - 1)
                    str += " ";
            }
        }
        return str;

    }


    public String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }


}
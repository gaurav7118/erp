/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.common.util;

import com.krawler.common.admin.KWLCurrency;
import java.text.DecimalFormat;

/**
 *
 * @author krawler
 */
public class CommonIndonesianNumberToWords {

    private final String[] tensNamesID = {
        "", " Sepuluh", " Dua Puluh", " Tiga Puluh", " Empat Puluh", " Lima Puluh", " Enam Puluh", " Tujuh Puluh", " Delapan Puluh", " Sembilan Puluh"
    };
    private final String[] numNamesID = {
        " Nol", " Satu", " Dua", " Tiga", " Empat", " Lima", " Enam", " Tujuh", " Delapan", " Sembilan", " Sepuluh", " Sebelas", " Dua Belas",
        " Tiga Belas", " Empat Belas", " Lima Belas", " Enam Belas", " Tujuh Belas", " Delapan Belas", " Sembilan Belas"
    };

    /**
     * Below function converts the units, tens and hundreds in words.
     *
     * @param number
     * @return
     */
    private String convertLessThanOneThousand(int number) {
        String soFar;
            if (number % 100 < 20) {
                soFar = numNamesID[number % 100];
                number /= 100;
            } else {
                if (number % 10 != 0) {
                    soFar = numNamesID[number % 10];
                } else {
                    soFar = "";
                }
                number /= 10;
                soFar = tensNamesID[number % 10] + soFar;
                number /= 10;
            }
            soFar = soFar.replace("Nol", "");
            if (number == 0) {
                return soFar;
            }
            return numNamesID[number] + " Ratus" + soFar;
        }

    /**
     * Below function converts less than one value i.e digits after decimal
     * point.
     *
     * @param number
     * @return
     */
    private String convertLessOne(int number, KWLCurrency currency) {
        String soFar;
        String val = "";
        if (number % 100 < 20) {
            soFar = numNamesID[number % 100];
            number /= 100;
        } else {
            soFar = numNamesID[number % 10];
            number /= 10;
            soFar = tensNamesID[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) {
            return soFar + " " + val;
        }
        return numNamesID[number] + " " + soFar + " " + val;
    }

    public String convert(Double number, KWLCurrency currency, int countryLanguageId) {
        String answer = "";
        if (number == 0) {
            return "";
        }
        answer = indonesiaConvert(number, currency);
        return answer;
    }

    /**
     * This function divides the number in Trillion, Billions, Millions, hundred
     * thousands, thousands and fractions then according to division converts
     * the number in words Ex : 123456789 = "seratus dua puluh tiga juta empat
     * ratus lima puluh enam ribu tujuh ratus delapan puluh sembilan rupiah"
     * which in english is . 123456789 = "one hundred twenty-three million four
     * hundred fifty-six thousand seven hundred eighty-nine".
     *
     * @param number
     * @param currency
     * @return
     */
    public String indonesiaConvert(Double number, KWLCurrency currency) {
        boolean isNegative = false;
        if (number < 0) {
            isNegative = true;
            number = -1 * number;
        }
        String snumber = Double.toString(number);
        String mask = "000000000000000.00";
        DecimalFormat df = new DecimalFormat(mask);
        snumber = df.format(number);
        int trillion = Integer.parseInt(snumber.substring(0, 3));
        int billions = Integer.parseInt(snumber.substring(3, 6));
        int millions = Integer.parseInt(snumber.substring(6, 9));
        int hundredThousands = Integer.parseInt(snumber.substring(9, 12));
        int thousands = Integer.parseInt(snumber.substring(12, 15));
//        int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
        String fraction_string = snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "";
        String tradTrillions;
        switch (trillion) {
            case 0:
                tradTrillions = "";
                break;
            case 1:
                tradTrillions = convertLessThanOneThousand(trillion) + " Triliun ";
                break;
            default:
                tradTrillions = convertLessThanOneThousand(trillion) + " Triliun ";
        }
        String result = tradTrillions;
        String tradBillions;
        switch (billions) {
            case 0:
                tradBillions = "";
                break;
            case 1:
                tradBillions = convertLessThanOneThousand(billions) + " Miliar ";
                break;
            default:
                tradBillions = convertLessThanOneThousand(billions) + " Miliar ";
        }
        result += tradBillions;

        String tradMillions;
        switch (millions) {
            case 0:
                tradMillions = "";
                break;
            case 1:
                tradMillions = convertLessThanOneThousand(millions) + " Juta ";
                break;
            default:
                tradMillions = convertLessThanOneThousand(millions) + " Juta ";
        }
        result = result + tradMillions;

        String tradHundredThousands;
        switch (hundredThousands) {
            case 0:
                tradHundredThousands = "";
                break;
            case 1:
                tradHundredThousands = "Seribu ";
                break;
            default:
                tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Ribu";
        }
        result = result + tradHundredThousands;
        String tradThousand;
        tradThousand = convertLessThanOneThousand(thousands);
        
        if (StringUtil.isNullOrEmptyWithTrim(tradThousand) && StringUtil.isNullOrEmptyWithTrim(result)) {
            result = " Nol";
        } else {
            result = result + tradThousand;
        }
        String afterDecimalConversion = "";
        if (fraction_string != null && fraction_string != "" && !fraction_string.equals("00")) {
            afterDecimalConversion += " Koma ";
            for (int i = 0; i < fraction_string.length(); i++) {
                if (!(i == fraction_string.length() - 1 && (fraction_string.charAt(i) + "").equals("0"))) {
                    afterDecimalConversion += convertLessOne(Integer.parseInt(fraction_string.charAt(i) + ""), currency);
                }
            }
        }
//        switch (fractions) {
//            case 0:
//                afterDecimalConversion = "";
//                break;
//            default:
//                afterDecimalConversion = convertLessOne(fractions);
//        }
        result = result + afterDecimalConversion + " Rupiah"; //to be done later
        result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
        if (isNegative) {
            result = "Minus " + result;
        }

        result = result.replace("Satu Puluh Satu", "Sebelas");
        result = result.replace("Satu Puluh", "Sepuluh");
        result = result.replace("Satu Ratus", "Seratus");
        result = result.replace("Satu Ribu", "Seribu");
        return result;
    }
}

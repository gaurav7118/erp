/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.Constants;
import java.text.DecimalFormat;

/**
 *
 * @author krawler
 */
public class CommonEnglishNumberToWords {
    
        private final String[] tensNames = {
            "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
        };
        private final String[] numNames = {
            "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten", " Eleven", " Twelve",
            " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"
        };

        private String convertLessThanOneThousand(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        }

        private String convertLessThanOneThousandWithHypen(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                if(!soFar.isEmpty()){
                soFar = tensNames[number % 10]+"-"+ soFar.replaceFirst(" ", "");
                } else {
                soFar = tensNames[number % 10] + soFar;
                }
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        }

        private String convertLessOne(int number, KWLCurrency currency) {
            String soFar;
            String val = currency.getAfterDecimalName();
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                if(Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId && !soFar.isEmpty()){
                    soFar = tensNames[number % 10] +"-"+ soFar.replaceFirst(" ", "");
                } else {
                soFar = tensNames[number % 10] + soFar;
                }
                number /= 10;
            }
            if (number == 0) {
                if (Integer.parseInt(currency.getCurrencyID()) == Constants.CountryIndiaCurrencyId || Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId) {
                    return " And " + soFar + " " + val;
                } else {
                    return " And " + val + " " + soFar;
                }
            }
            return " And " + numNames[number] + " " + val + soFar;
        }

    public String convert(Double number, KWLCurrency currency, int countryLanguageId) {
        
        String answer = "";
        if (number == 0) {
            return "Zero";
        }

        if (countryLanguageId == Constants.OtherCountryLanguageId) { // For universal conversion of amount in words. i.e. in Billion,trillion etc
            answer = universalConvert(number, currency);
        } else if (countryLanguageId == Constants.CountryIndiaLanguageId){ // For Indian word format.ie. in lakhs, crores
            answer = indianConvert(number, currency);
        }
        return answer;
    }
        
    public String universalConvert(Double number,KWLCurrency currency) {
        boolean isNegative = false;
        if (number < 0) {
            isNegative = true;
            number = -1 * number;
        }
        String snumber = Double.toString(number);
        String mask = "000000000000000.00";  //ERP-17681
        DecimalFormat df = new DecimalFormat(mask);
        snumber = df.format(number);
        int trillion = Integer.parseInt(snumber.substring(0, 3));//ERP-17681
        int billions = Integer.parseInt(snumber.substring(3, 6));
        int millions = Integer.parseInt(snumber.substring(6, 9));
        int hundredThousands = Integer.parseInt(snumber.substring(9, 12));
        int thousands = Integer.parseInt(snumber.substring(12, 15));
        int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
        String tradTrillions;
        switch (trillion) {
            case 0:
                tradTrillions = "";
                break;
            case 1:
                tradTrillions = convertLessThanOneThousand(trillion) + " Trillion ";
                break;
            default:
                tradTrillions = convertLessThanOneThousand(trillion) + " Trillion ";
        }
        String result = tradTrillions;
        String tradBillions;
        switch (billions) {
            case 0:
                tradBillions = "";
                break;
            case 1:
                tradBillions = convertLessThanOneThousand(billions) + " Billion ";
                break;
            default:
                tradBillions = convertLessThanOneThousand(billions) + " Billion ";
        }
        result += tradBillions;

        String tradMillions;
        switch (millions) {
            case 0:
                tradMillions = "";
                break;
            case 1:
                tradMillions = convertLessThanOneThousand(millions) + " Million ";
                break;
            default:
                tradMillions = convertLessThanOneThousand(millions) + " Million ";
        }
        result = result + tradMillions;

        String tradHundredThousands;
        switch (hundredThousands) {
            case 0:
                tradHundredThousands = "";
                break;
            case 1:
                tradHundredThousands = "One Thousand ";
                break;
            default:
                tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
        }
        result = result + tradHundredThousands;
        String tradThousand;
        if(Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId){
           tradThousand = convertLessThanOneThousandWithHypen(thousands); 
        } else {
           tradThousand = convertLessThanOneThousand(thousands);
        }
        result = result + tradThousand;
        String paises;
        switch (fractions) {
            case 0:
                paises = "";
                break;
            default:
                paises = convertLessOne(fractions, currency);
        }
        if(Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId){
          result = result +" Dollars "+ paises;    
        }else {
        result = result + paises; //to be done later
        }
        result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
        if (isNegative) {
            result = "Minus " + result;
        }
//            result = result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase(); // Make first letter of operand capital.
        return result;
    }
    
    public String indianConvert(Double number,KWLCurrency currency) {
        boolean isNegative = false;
        if (number < 0) {
            isNegative = true;
            number = -1 * number;
        }
        String snumber = Double.toString(number);
        String mask = "000000000000000.00";  
        DecimalFormat df = new DecimalFormat(mask);
        snumber = df.format(number);
        int Neel = Integer.parseInt(snumber.substring(0, 2));
        int Kharab = Integer.parseInt(snumber.substring(2, 4));
        int Arab = Integer.parseInt(snumber.substring(4, 6));
        int Crore = Integer.parseInt(snumber.substring(6, 8));
        int Lakh = Integer.parseInt(snumber.substring(8, 10));
        int Thousands = Integer.parseInt(snumber.substring(10, 12));
        int Hundred = Integer.parseInt(snumber.substring(12, 13));
        int Ten = Integer.parseInt(snumber.substring(13, 15));
        int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
        String tradNeel;
        switch (Neel) {
            case 0:
                tradNeel = "";
                break;
//            case 1:
//                tradNeel = convertLessThanOneThousand(Neel) + " Kharab ";
//                break;
            default:
                tradNeel = convertLessThanOneThousand(Neel) + " Kharab ";
        }
        String result = tradNeel;
        
        String tradKharab;
        switch (Kharab) {
            case 0:
                tradKharab = "";
                break;
//            case 1:
//                tradKharab = convertLessThanOneThousand(Kharab) + " Kharab ";
//                break;
            default:
                tradKharab = convertLessThanOneThousand(Kharab) + " Kharab ";
        }
        result += tradKharab;
        
        String tradArab;
        switch (Arab) {
            case 0:
                tradArab = "";
                break;
//            case 1:
//                tradArab = convertLessThanOneThousand(Arab) + " Arab ";
//                break;
            default:
                tradArab = convertLessThanOneThousand(Arab) + " Arab ";
        }
        result += tradArab;
        
        String tradCrore;
        switch (Crore) {
            case 0:
                tradCrore = "";
                break;
//            case 1:
//                tradCrore = convertLessThanOneThousand(Crore) + " Crore ";
//                break;
            default:
                tradCrore = convertLessThanOneThousand(Crore) + " Crore ";
        }
        result += tradCrore;

        String tradLakh;
        switch (Lakh) {
            case 0:
                tradLakh = "";
                break;
//            case 1:
//                tradLakh = convertLessThanOneThousand(Lakh) + " Lakh ";
//                break;
            default:
                tradLakh = convertLessThanOneThousand(Lakh) + " Lakh ";
        }
        result = result + tradLakh;

        String tradThousands;
        switch (Thousands) {
            case 0:
                tradThousands = "";
                break;
            case 1:
                tradThousands = "One Thousand ";
                break;
            default:
                tradThousands = convertLessThanOneThousand(Thousands) + " Thousand ";
        }
        result = result + tradThousands;
        
        String tradHundred;
        switch (Hundred) {
            case 0:
                tradHundred = "";
                break;
            case 1:
                tradHundred = "One Hundred ";
                break;
            default:
                tradHundred = convertLessThanOneThousand(Hundred) + " Hundred ";
        }
        result = result + tradHundred;
        
        String tradTen;
        switch (Ten) {
            case 0:
                tradTen = "";
                break;
//            case 1:
//                tradTen = convertLessThanOneThousand(Ten);
//                break;
            default:
                tradTen = convertLessThanOneThousand(Ten) ;
        }
        result = result + tradTen;
     
        String paises;
        switch (fractions) {
            case 0:
                paises = "";
                break;
            default:
                paises = convertLessOne(fractions, currency);
        }
        result = result + paises; //to be done later
        result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
        if (isNegative) {
            result = "Minus " + result;
        }
//            result = result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase(); // Make first letter of operand capital.
        return result;
    }
   /* public String indianConvert1(Double number,KWLCurrency currency) {
        boolean isNegative = false;
        if (number < 0) {
            isNegative = true;
            number = -1 * number;
        }
        String snumber = Double.toString(number);
        String mask = "000000000000000.00";  //ERP-17681
        DecimalFormat df = new DecimalFormat(mask);
        snumber = df.format(number);

        int n = Integer.parseInt(snumber.substring(0, 15));
        int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
        if (n == 0) {
            return "Zero";
        }
        String arr1[] = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String arr2[] = {"Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        String unit[] = {"Arab", "Crore", "Lakh", "Thousand", "Hundred", ""};
        int factor[] = {1000000000, 10000000, 100000, 1000, 100, 1};
        String answer = "", paises = "";
        if (n < 0) {
            answer = "Minus";
            n = -n;
        }
        int quotient, units, tens;
        for (int i = 0; i < factor.length; i++) {
            quotient = n / factor[i];
            if (quotient > 0) {
                if (quotient < 20) {
                    answer = answer + " " + arr1[quotient - 1];
                } else {
                    units = quotient % 10;
                    tens = quotient / 10;
                    if (units > 0) {
                        answer = answer + " " + arr2[tens - 2] + " " + arr1[units - 1];
                    } else {
                        answer = answer + " " + arr2[tens - 2] + " ";
                    }
                }
                answer = answer + " " + unit[i];
            }
            n = n % factor[i];
        }
        switch (fractions) {
            case 0:
                paises = "";
                break;
            default:
                paises = convertLessOne(fractions, currency);
        }
        answer = answer + paises; //to be done later
        if (isNegative) {
            answer = "Minus " + answer;
        }
        return answer.trim();
    }*/
}

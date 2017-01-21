package com.egangotri

/**
 * Created by user on 7/23/2015.
 */
class IndicToCE {
    public static final int yearToConvert = 1940

    public static final int shakaYearDiff = 78
    public static final int vkYearDiff = 56 //actually 56.7

    static main(args) {
        println "Shaka Yr $yearToConvert is ${yearToConvert + shakaYearDiff} CE"
        println "Vikram Yr $yearToConvert is ${yearToConvert - vkYearDiff} CE"
    }
}

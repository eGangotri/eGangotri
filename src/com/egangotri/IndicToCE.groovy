package com.egangotri

/**
 * Created by user on 7/23/2015.
 */
class IndicToCE {
    static final int yearToConvert = 1940

    static final int shakaYearDiff = 78
    static final int vkYearDiff = 56 //actually 56.7

    static main(args) {
        println "Shaka Yr $yearToConvert is ${yearToConvert + shakaYearDiff} CE"
        println "Vikram Yr $yearToConvert is ${yearToConvert - vkYearDiff} CE"
    }
}

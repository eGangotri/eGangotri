package com.egangotri

import groovy.util.logging.Slf4j

/**
 * Created by user on 7/23/2015.
 */
@Slf4j
class IndicToCE {
    static final List<Integer> yearsToConvert = [5035, 1880, 2018]

    static final int shakaYearDiff = 78
    static final int saptarshiYearDiff = -3076
    static final int vkYearDiff = -56 //actually 56.7
    //shubh samvat
    static final int shubhSamvatDiff = 1825

    static void main(String[] args) {
        List<List<Integer>> allConverstions = []
        yearsToConvert.forEach { yearToConvert ->
            List<Integer> convertedYear = []
            convertedYear << yearToConvert + shakaYearDiff
            convertedYear << yearToConvert + vkYearDiff
            convertedYear << yearToConvert + saptarshiYearDiff
            convertedYear << yearToConvert + shubhSamvatDiff
            log.info "Shaka Yr $yearToConvert is ${convertedYear[0]} CE"
            log.info "Vikram Yr $yearToConvert is ${convertedYear[1]} CE"
            log.info "Saptarshi Yr $yearToConvert is ${convertedYear[2]} CE"
            log.info "Shubh Yr $yearToConvert is ${convertedYear[3]} CE"
            allConverstions << convertedYear
        }
        allConverstions.each { List<Integer> _yrs ->


        }
    }
}

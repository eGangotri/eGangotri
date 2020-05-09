package com.egangotri

import groovy.util.logging.Slf4j

/**
 * Created by user on 7/23/2015.
 */
@Slf4j
class IndicToCE {
    static final int yearToConvert = 1940

    static final int shakaYearDiff = 78
    static final int vkYearDiff = 56 //actually 56.7

    static main(String[] args) {
        log.info "Shaka Yr $yearToConvert is ${yearToConvert + shakaYearDiff} CE"
        log.info "Vikram Yr $yearToConvert is ${yearToConvert - vkYearDiff} CE"
    }
}

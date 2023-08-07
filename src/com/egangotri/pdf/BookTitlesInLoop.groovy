package com.egangotri.pdf

import groovy.util.logging.Slf4j

@Slf4j
class BookTitlesInLoop {
    static List<String> FOLDER_NAMES = []
    static void main(String[] args) {
        if (args?.size() > 0) {
            String args0 = args[0]
            FOLDER_NAMES = args0.split(",")*.trim().findAll {it.length()>1}.toList()
            for(String folder: FOLDER_NAMES){
                BookTitles.resetCounters()
                BookTitles.incrementFolderCounter()
                BookTitles.execute(folder)
                log.info("Finished processing ${folder}")
                BookTitles.counterStats()
            }
        }
    }
}

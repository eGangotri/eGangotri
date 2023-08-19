package com.egangotri.pdf

import groovy.util.logging.Slf4j

@Slf4j
class BookTitlesInLoop {
    static List<String> FOLDER_NAMES = []
    static void main(String[] args) {
        if (args?.size() > 0) {
            String args0 = args[0]
            if(args0.contains("_ALL_")){
                args0 =  args0.replace("_ALL_","");
                FOLDER_NAMES = new File(args0).list().collect {"${args0}${it}"}
                log.info("args0 ${args0} FOLDER_NAMES ${FOLDER_NAMES}")
            }
            else{
            FOLDER_NAMES = args0.split(",")*.trim().findAll {it.length()>1}.toList()
            }
            if(args?.size()==2){
                String args1 = args[1];
                if(args1.startsWith("folder="))
                BookTitles.DUMP_DIRECTORY = args1.replace("dumpFolder=","")
            }
            for(String folder: FOLDER_NAMES){
                BookTitles.execute(folder)
                log.info("Finished processing ${folder}")
                BookTitles.counterStats()
                BookTitles.resetCounters()
                BookTitles.incrementFolderCounter()
            }
        }
    }
}

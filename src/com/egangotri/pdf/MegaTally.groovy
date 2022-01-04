package com.egangotri.pdf

import com.egangotri.util.GenericUtil
import groovy.util.logging.Slf4j

@Slf4j
class MegaTally {
    static void main(String[] args) {
        //MegaTally.execute(args[0])
        String args0 = /*args[0]?:*/"D:\\NMM\\Oct-2019"
        String args1 = /*args[1]?: */"E:\\Oct-2019"
        MegaTally.execute(args0,args1)
    }

    static void execute(String args0, String args1){
        List<File> tiffSrcs = GenericUtil.getDirectoriesSortedByName(args0)
        Map<String,String> tallyMap = [:]
        tiffSrcs.eachWithIndex(folder,counter) -> {
            def hobbyMap = [(folder.getAbsolutePath()): "${args1}\\ramtek-${counter+1}"]
            tallyMap.putAll(hobbyMap)
        }
        log.info("tiffSrcs ${tiffSrcs}")
        log.info("tallyMap ${tallyMap}")
        List<String> reports = []
        tallyMap.eachWithIndex { entry, index ->
            def indent = ((index == 0 || index % 2 == 0) ? "   " : "")
            println "MegaTally ${index+1} of ${tallyMap.size()} Src: $entry.key Dest: $entry.value"
            reports << Tally.tally(entry.key, entry.value)
        }
        log.info(reports.join("\n"))
        File megaTallyLogs = new File("MegaTallyLog-${new Date()}")
        megaTallyLogs << "${reports}\n"

    }
}

package com.egangotri.pdf

import com.egangotri.util.GenericUtil
import com.egangotri.util.PdfUtil
import groovy.util.logging.Slf4j

@Slf4j
class MegaTally {
    static void main(String[] args) {
        //MegaTally.execute(args[0])
        String args0 = /*args[0]?:*/"D:\\NMM\\Oct-2019"
        String args1 = /*args[1]?: */"E:\\Oct-2019"
        MegaTally.execute(args0,args1)
    }

    static void execute(String pdfSrc){
        List<File> pdfSrcs = GenericUtil.getDirectories(pdfSrc)
        Map<String,String> tallyMap = [:]
        pdfSrcs.eachWithIndex(pdf,counter) -> {
            String key = "${PdfUtil.extractTiffFolderName(pdf)}";
            LinkedHashMap<String,String> tmpMap =
                    [ key: "${pdf.absolutePath}"]
            tallyMap.putAll(tmpMap)
        }
        log.info("tallyMap ${tallyMap}")
        List<String> reports = []
        tallyMap.eachWithIndex { entry, index ->
            def indent = ((index == 0 || index % 2 == 0) ? "   " : "")
            println "MegaTally ${index+1} of ${tallyMap.size()} Src: $entry.key Dest: $entry.value"
            reports << Tally.tally(entry.key, entry.value)
        }
        log.info(reports.join("\n"))
    }
}

package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

import java.nio.file.Files

@Slf4j
class RunPdfTk {

    static main(args) {
        File directory = new File(EGangotriUtil.CODE_404_BAD_DATA_FOLDER+File.separator+"pdf")
        File _repaired = new File(directory,"_repaired")
        if(!_repaired.exists()){
            _repaired.mkdir()
        }
        File[] files = directory.listFiles()
        files.eachWithIndex { File file, int _counter ->
            if (!file.isDirectory() && file.name.endsWith(EGangotriUtil.PDF)) {
                repairPdf(file,_repaired,"${_counter+1}). ")
            }
        }
    }

    static void repairPdf(File src, File destDir, String _counter = ""){
        String repairedFileName = src.name.replace('.pdf','_rep.pdf')
        String dest = destDir.absolutePath + File.separator + repairedFileName
        String execInstruction = "cmd /c pdftk \"${src}\"  output \"${dest}\""
        log.info("$_counter"+ execInstruction)
        try{
            execInstruction.execute().text
            log.info("after execution of  ${src.name}")
        }
        catch(Exception ex){
            ex.printStackTrace()
        }
    }
}

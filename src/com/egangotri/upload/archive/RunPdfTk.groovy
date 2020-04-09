package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

import java.nio.file.Files

@Slf4j
class RunPdfTk {
    static File directory = new File(EGangotriUtil.CODE_404_BAD_DATA_FOLDER+File.separator+"pdf")

    static File _repaired = new File(directory,"_repaired")
    static File _altered = new File(directory, "_altered")
    static main(args) {
        if(!_repaired.exists()){
            _repaired.mkdir()
        }
        if(!_altered.exists()){
            _altered.mkdir()
        }
        File[] files = directory.listFiles()
        files.eachWithIndex { File file, int _counter ->
            if (!file.isDirectory() && file.name.endsWith(EGangotriUtil.PDF)) {
                repairPdf(file,_repaired,"${_counter+1}). ")
            }
        }
    }

    static void repairPdf(File src, File destDir, String _counter = ""){
        String repairedFile = destDir.absolutePath + File.separator + src.name.replace('.pdf','_rep.pdf')
        String execInstruction = "cmd /c pdftk \"${src}\"  output \"${repairedFile}\""
        log.info("$_counter"+ execInstruction)
        try{
            execInstruction.execute().text
            if(new File(repairedFile).exists()){
                log.info("after execution of  ${src.name}")
                Files.copy(src.toPath(), new File(_altered, src.name).toPath())
            }
        }
        catch(Exception ex){
            ex.printStackTrace()
        }
    }
}

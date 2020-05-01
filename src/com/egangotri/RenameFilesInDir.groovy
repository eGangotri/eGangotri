package com.egangotri

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import groovy.util.logging.Slf4j

@Slf4j
class RenameFilesInDir {

    static String FOLDER_NAME = "G:\\OctoberBooks\\prtiek\\pratiekMan - Copy\\17Oct"
    static String RENAMED_FOLDER_NAME = "renamed"
    int totalFilesSplittable = 0
    static main(args) {
        String args0 = ""//args[0]
        log.info "args0:$args0"
        File directory = new File(args0?:RenameFilesInDir.FOLDER_NAME)
        RenameFilesInDir renameFilesInDir = new RenameFilesInDir()
        renameFilesInDir.createRenameFolder()
        for(File file : directory.listFiles() ){
            if(!file.isDirectory() && file.name.endsWith(".pdf"))  {
                log.info "Renaming $file.name"
                String newFileName = ""
                def splits = file.name?.split("_")
                 splits.eachWithIndex{ str, i ->
                     newFileName += str + "${i==0?'_UPSS':''}" + "${str.endsWith('.pdf')?'':'_'}"
                }
                File  newFile = new File(RenameFilesInDir.FOLDER_NAME + "//" + RenameFilesInDir.RENAMED_FOLDER_NAME + "//"+ newFileName)
                try {
                    org.apache.commons.io.FileUtils.moveFile(file, newFile)
                    log.info "Moved $newFileName"

                } catch (IOException e) {
                    e.printStackTrace()
                }
            }
        }

        log.info "***Total Files Split: ${renameFilesInDir.totalFilesSplittable}"
    }

    def createRenameFolder(){
        File directory = new File(RenameFilesInDir.FOLDER_NAME)

        if(directory) {
            File renameFolder = new File(RenameFilesInDir.FOLDER_NAME + "//" + RenameFilesInDir.RENAMED_FOLDER_NAME)
            if(!renameFolder.exists())     {
                log.info("${RenameFilesInDir.RENAMED_FOLDER_NAME} missing. Creating")
                renameFolder.mkdir()
            }
        }
    }
}

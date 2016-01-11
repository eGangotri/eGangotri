package com.egangotri

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy

class RenameFilesInDir {

    static String FOLDER_NAME = "G:\\OctoberBooks\\prtiek\\pratiekMan - Copy\\17Oct";
    static String RENAMED_FOLDER_NAME = "renamed"
    int totalFilesSplittable = 0
    static main(args) {
        String args0 = ""//args[0]
        println "args0:$args0"
        File directory = new File(args0?:RenameFilesInDir.FOLDER_NAME)
        RenameFilesInDir renameFilesInDir = new RenameFilesInDir()
        renameFilesInDir.createRenameFolder()
        for(File file : directory.listFiles() ){
            if(!file.isDirectory() && file.name.endsWith(".pdf"))  {
                println "Renaming $file.name"
                String newFileName = ""
                def splits = file.name?.split("_")
                 splits.eachWithIndex{ str, i ->
                     newFileName += str + "${i==0?'_UPSS':''}" + "${str.endsWith('.pdf')?'':'_'}"
                }
                File  newFile = new File(RenameFilesInDir.FOLDER_NAME + "//" + RenameFilesInDir.RENAMED_FOLDER_NAME + "//"+ newFileName)
                try {
                    org.apache.commons.io.FileUtils.moveFile(file, newFile);
                    println "Moved $newFileName"

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        println "***Total Files Split: ${renameFilesInDir.totalFilesSplittable}"
    }

    def createRenameFolder(){
        File directory = new File(RenameFilesInDir.FOLDER_NAME)

        if(directory) {
            File renameFolder = new File(RenameFilesInDir.FOLDER_NAME + "//" + RenameFilesInDir.RENAMED_FOLDER_NAME)
            if(!renameFolder.exists())     {
                println("${RenameFilesInDir.RENAMED_FOLDER_NAME} missing. Creating")  ;
                renameFolder.mkdir()
            }
        }
    }
}

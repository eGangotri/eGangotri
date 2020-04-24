package com.egangotri.batch

import com.egangotri.mail.Mailer

import java.text.SimpleDateFormat

class SnapToHtml {

    static String srcFolder = "D:\\Treasures25"
    static String snap2HtmlPath = "D:\\Snap2HTML\\Snap2HTML.exe"
    static def dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa")
    static String execCmd = """
    $snap2HtmlPath -path:$srcFolder -outfile:"$srcFolder\\FILE_TITLE.html" -title:"FILE_TITLE"
    """

    static void main(String[] args) {
        try{
            execute(args)
        }
        catch(Exception e){
            e.printStackTrace()
        }
    }

    static void execute(String[] args){
        if (args && args.size() == 2) {
            srcFolder = args[0]
            snap2HtmlPath = args[1]
        }
        println "cmd /c echo Make sure snap2html is on the Path".execute().text
        String fileTitle = "snap2html @ " + dateFormat.format(new Date())
        execCmd = execCmd.replaceAll('FILE_TITLE', fileTitle)
        println "cmd /c ${execCmd}".execute().text
        Mailer.notify("$fileTitle Generated", fileTitle, "${srcFolder}${File.separator}${fileTitle}.html")
    }
}

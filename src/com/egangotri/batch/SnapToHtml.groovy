package com.egangotri.batch

import java.text.SimpleDateFormat

class SnapToHtml {

    static String srcFolder = "D:\\Treasures25"
    static String snap2HtmlPath = "D:\\Snap2HTML\\Snap2HTML.exe"
    static def dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa")
    static String execCmd = """
    $snap2HtmlPath -path:$srcFolder -outfile:"$srcFolder\\snap2html @ TIME_STAMP.html" -title:"snap2html @ TIME_STAMP"
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
        String formattedTime = dateFormat.format(new Date())
        execCmd = execCmd.replaceAll('TIME_STAMP', formattedTime)
        println "cmd /c ${execCmd}".execute().text
    }
}

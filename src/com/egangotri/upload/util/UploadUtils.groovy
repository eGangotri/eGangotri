package com.egangotri.upload.util

import com.egangotri.util.FileUtil

import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

/**
 * Created by user on 1/16/2016.
 */
class UploadUtils {
    static final String HOME = System.getProperty('user.home')
    static final String PDF = ".pdf"

    public static Hashtable loadProperties(String fileName) {
        Properties properties = new Properties()
        File propertiesFile = new File(fileName)
        propertiesFile.withInputStream {
            properties.load(it)
        }

        Hashtable<String, String> metaDataMap = [:]
        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String val = new String(properties.get(key).getBytes("ISO-8859-1"), "UTF-8")
            metaDataMap.put(key, val);
        }
        metaDataMap.each { k, v ->
            // println "$k $v"
        }
        return metaDataMap
    }

    static boolean hasAtleastOnePdf(File directory) {
        if (directory && directory.list().any { it.endsWith(PDF) }) {
            return true
        }

        //if directory has sub-directories
        List<File> subDirs = directory.listFiles()
        if (subDirs && subDirs.any { it.isDirectory() }) {
            for (int i = 0; i < subDirs.size(); i++) {
                if (subDirs[i].isDirectory()) {
                    if (hasAtleastOnePdf(subDirs[i])) {
                        return true
                    }
                }
            }
        }
        return false
    }

    static boolean hasAtleastOnePdf(String dirName) {
        return hasAtleastOnePdf(new File(dirName))
    }

    static boolean isFilePdf(File file) {
        return !file.isDirectory() && file.name.endsWith(PDF)
    }

    static boolean isFilePdf(String fileName) {
        return isFilePdf(new File(fileName))
    }

    static List<String> getFiles(List folderPaths) {
        List<String> uploadables = []
        folderPaths.each { String folder ->
            if (hasAtleastOnePdf(folder)) {
                getPdfFiles(folder).each { String file ->
                    uploadables << file
                }
            }
        }
        return uploadables
    }

    //Goes One Level Deep
    static List<String> getPdfFiles(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)
        println "getPdfFiles in Dir $directory"
        def files = directory.listFiles()
        List<String> uploadables = []
        files.each { File file ->
            if (isFilePdf(file)) {
                uploadables << file.absolutePath
            }

            if (file.isDirectory()) {
                println "getPdfFiles in Sub-Directory $file"

                if (!file.name.equals(FileUtil.PRE_57) && hasAtleastOnePdf(file)) {
                    List pdfs = file.listFiles().findAll { isFilePdf(it) }
                    pdfs.each { File _fl -> uploadables << _fl.absolutePath }
                }
            }
        }
        println "***Total Files uploadables: ${uploadables.size()}"
        return uploadables
    }


    public static void pasteFileNameAndCloseUploadPopup(String fileName) {
        // A short pause, just to be sure that OK is selected
        Thread.sleep(1000);
        setClipboardData(fileName);
        //native key strokes for CTRL, V and ENTER keys
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    public static void tabPasteFolderNameAndCloseUploadPopup(String fileName) {
        println "$fileName  being pasted"
        // A short pause, just to be sure that OK is selected
        Thread.sleep(1000);
        setClipboardData(fileName);
        //native key strokes for CTRL, V and ENTER keys
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_TAB);
        robot.keyRelease(KeyEvent.VK_TAB);

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    public static void setClipboardData(String string) {
        StringSelection stringSelection = new StringSelection(string);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }

}

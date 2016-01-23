package com.egangotri.upload.util

import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

/**
 * Created by user on 1/16/2016.
 */
class UploadUtils {

    public static Hashtable loadProperties(String fileName) {
        Properties properties = new Properties()
        File propertiesFile = new File(fileName)
        propertiesFile.withInputStream {
            properties.load(it)
        }

        Hashtable<String, String> metaDataMap = [:]
        for (Enumeration e = properties.keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            String val = new String(properties.get(key).getBytes("ISO-8859-1"), "UTF-8")
            metaDataMap.put(key, val);
        }
        metaDataMap.each { k, v ->
           // println "$k $v"
        }
        return metaDataMap
    }


    static List<String> getFiles(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)
        println "processAFolder $directory"
        def files = directory.listFiles()
        List<String> uploadables = []
        files.each { File file ->
            if (!file.isDirectory() && file.name.endsWith(PDF)) {
                uploadables << file.absolutePath
            }
        }
        println "***Total Files uploadables: ${uploadables.size()}"
        return uploadables
    }


    public static void pasteFileNameAndCloseUploadPopup(String fileName) {
        println "$fileName  being pasted"
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

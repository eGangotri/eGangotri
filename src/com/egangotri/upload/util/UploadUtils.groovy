package com.egangotri.upload.util

import com.egangotri.filter.PdfFileFilter
import com.egangotri.filter.NonPre57DirectoryFilter
import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.util.FileUtil
import groovy.io.FileType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

/**
 * Created by user on 1/16/2016.
 */
class UploadUtils {
    final static Logger Log = LoggerFactory.getLogger(this.simpleName)

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
            // Log.info "$k $v"
        }
        return metaDataMap
    }

    static boolean hasAtleastOneUploadablePdfForProfile(String archiveProfile) {
        List<File> folders = ArchiveHandler.pickFolderBasedOnArchiveProfile(archiveProfile).collect { new File(it) }
        boolean atlestOne = false
        if (archiveProfile == ArchiveHandler.PROFILE_ENUMS.ib.toString() && hasAtleastOnePdfInPre57Folders(folders)) {
            atlestOne = true
        } else if (hasAtleastOnePdfExcludePre57(folders)) {
            atlestOne = true
        }
        Log.info "atlestOne[$archiveProfile]: $atlestOne"
        return atlestOne
    }

    static List<String> getUploadablePdfsForProfile(String archiveProfile) {
        List<File> folders = ArchiveHandler.pickFolderBasedOnArchiveProfile(archiveProfile).collect { new File(it) }
        List<String> pdfs = []
        if (archiveProfile == ArchiveHandler.PROFILE_ENUMS.ib.toString()) {
            pdfs = getPdfsInPre57Folders(folders)
        } else {
            pdfs = getAllPdfsExceptPre57(folders)
        }
        return pdfs
    }


    static boolean hasAtleastOnePdf(File folder) {
        return hasAtleastOnePdf(folder, false)
    }

    static boolean hasAtleastOnePdf(File folder, boolean excludePre57) {
        return getAllPdfs(folder, excludePre57)?.size()
    }

    static boolean hasAtleastOnePdfExcludePre57(File folder) {
        return hasAtleastOnePdf(folder, true)
    }

    static boolean hasAtleastOnePdfExcludePre57(List<File> folders) {
        boolean atlestOne = false
        folders.each { folder ->
            if (hasAtleastOnePdfExcludePre57(folder)) {
                atlestOne = true
            }
        }
        return atlestOne
    }

    static boolean hasAtleastOnePdfInPre57Folders(List<File> folders) {
        boolean atlestOne = false
        if(getPdfsInPre57Folders(folders)) {
            atlestOne = true
        }
        return atlestOne
    }

    static List<String> getAllPdfs(File folder) {
        getAllPdfs(folder, false)
    }

    static List<String> getAllPdfsExceptPre57(File folder) {
        getAllPdfs(folder, false)
    }

    static List<String> getAllPdfsExceptPre57(List<File> folders) {
        List<String> pdfs = []
        folders.each { folder ->
            pdfs.addAll(getAllPdfsExceptPre57(folder))
        }
        return pdfs
    }

    static List<String> getAllPdfs(File folder, boolean excludePre57) {
        List<String> pdfs = []
        Map optionsMap = [type      : FileType.ANY,
                          nameFilter: ~(FileUtil.PDF_REGEX)
        ]
        if (excludePre57) {
            optionsMap.put("excludeFilter", { it.absolutePath.contains(FileUtil.PRE_57) })
        }
        folder.traverse(optionsMap) {
            Log.info ">>" + it
            pdfs << it.absolutePath
        }
        return pdfs
    }

    static List<String> getPdfsInPre57Folder(File folder) {
        List<String> pdfs = []
        Map optionsMap = [type         : FileType.ANY,
                          nameFilter   : ~(FileUtil.PDF_REGEX),
                          excludeFilter: { !it.absolutePath.contains(FileUtil.PRE_57) }
        ]
        folder.traverse(optionsMap) {
            Log.info ">>" + it
            pdfs << it.absolutePath
        }
        return pdfs
    }

    static List<String> getPdfsInPre57Folders(List<File> folders) {
        List<String> pdfs = []
        folders.each { folder ->
            pdfs.addAll(getPdfsInPre57Folder(folder))
        }
        return pdfs
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
        Log.info "$fileName  being pasted"
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

package com.egangotri.upload.util

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

@Slf4j
class UploadUtils {
    public static Hashtable<String, String> loadProperties(String fileName) {
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
            //log.info "$k $v"
        }
        return metaDataMap
    }

    static boolean hasAtleastOneUploadablePdfForProfile(String archiveProfile) {
        List<File> folders = ArchiveHandler.pickFolderBasedOnArchiveProfile(archiveProfile).collect { new File(it) }
        boolean atlestOne = false
        println "folders: $folders"
        if (EGangotriUtil.isAPreCutOffProfile(archiveProfile) && hasAtleastOnePdfInPreCutOffFolders(folders)) {
            atlestOne = true
        } else if (hasAtleastOnePdfExcludePreCutOff(folders)) {
            atlestOne = true
        }
       log.info "atlestOne[$archiveProfile]: $atlestOne"
        return atlestOne
    }

    static List<String> getUploadablePdfsForProfile(String archiveProfile) {
        List<File> folders = ArchiveHandler.pickFolderBasedOnArchiveProfile(archiveProfile).collect { new File(it) }
        List<String> pdfs = []
        println "getUploadablePdfsForProfile: $archiveProfile"
        if (EGangotriUtil.isAPreCutOffProfile(archiveProfile)) {
            pdfs = getPdfsInPreCutOffFolders(folders)
        } else {
            pdfs = getAllPdfsExceptPreCutOff(folders)
        }
        return pdfs
    }

    static int getCountOfUploadablePdfsForProfile(String archiveProfile) {
        return getUploadablePdfsForProfile(archiveProfile)?.size()
    }

    static boolean hasAtleastOnePdf(File folder) {
        return hasAtleastOnePdf(folder, false)
    }

    static boolean hasAtleastOnePdf(File folder, boolean excludePreCutOff) {
        return getAllPdfs(folder, excludePreCutOff)?.size()
    }

    static boolean hasAtleastOnePdfExcludePreCutOff(File folder) {
        return hasAtleastOnePdf(folder, true)
    }

    static boolean hasAtleastOnePdfExcludePreCutOff(List<File> folders) {
        boolean atlestOne = false
        folders.each { folder ->
            if (hasAtleastOnePdfExcludePreCutOff(folder)) {
                atlestOne = true
            }
        }
        return atlestOne
    }

    static boolean hasAtleastOnePdfInPreCutOffFolders(List<File> folders) {
        boolean atlestOne = false
        if (getPdfsInPreCutOffFolders(folders)) {
            atlestOne = true
        }
        return atlestOne
    }

    static List<String> getAllPdfsExceptPreCutOff(File folder) {
        getAllPdfs(folder, true)
    }

    static List<String> getAllPdfsExceptPreCutOff(List<File> folders) {
        List<String> pdfs = []
        folders.each { folder ->
            pdfs.addAll(getAllPdfsExceptPreCutOff(folder))
        }
        return pdfs
    }

    static List<String> getAllPdfs(File folder, Boolean excludePreCutOff) {
        List<String> pdfs = []
        Map optionsMap = [type      : FileType.ANY,
                          nameFilter: ~(FileUtil.PDF_REGEX)
        ]
        if (excludePreCutOff) {
            optionsMap.put("excludeFilter", { it.absolutePath.contains(FileUtil.PRE_CUTOFF) })
        }
        folder.traverse(optionsMap) {
           //log.info "getAllPdfs>>" + it
            pdfs << it.absolutePath
        }
        return pdfs
    }

    static List<String> getAllPdfs(File folder){
        return getAllPdfs(folder, false)
    }

    static List<String> getPdfsInPreCutOffFolder(File folder) {
        List<String> pdfs = []
        Map optionsMap = [type  : FileType.ANY,
                          filter: {
                              it.absolutePath.contains(FileUtil.PRE_CUTOFF) && it.name.endsWith(EGangotriUtil.PDF)
                          }
        ]
        folder.traverse(optionsMap) {
           log.info ">>>" + it
           log.info "${it.absolutePath.contains(FileUtil.PRE_CUTOFF)}"
            pdfs << it.absolutePath
        }
        return pdfs
    }

    static List<String> getPdfsInPreCutOffFolders(List<File> folders) {
        List<String> pdfs = []
        folders.each { folder ->
            pdfs.addAll(getPdfsInPreCutOffFolder(folder))
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
       log.info "$fileName  being pasted"
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

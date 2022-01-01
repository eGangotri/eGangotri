package com.egangotri.upload.util

import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.io.FileType
import groovy.util.logging.Slf4j

@Slf4j
class FileRetrieverUtil {

    static String pickFolderBasedOnArchiveProfile(String archiveProfile) {
        return FileUtil.ALL_FOLDERS."${archiveProfile.toUpperCase()}"
    }

    static List<String> getUploadablesForProfile(String archiveProfile) {
        String _folder = pickFolderBasedOnArchiveProfile(archiveProfile)
        File folder = new File(_folder)
        List<String> files = getAllFiles(folder, archiveProfile)
        return files
    }

    static int getCountOfUploadableItemsForProfile(String archiveProfile) {
        return getUploadablesForProfile(archiveProfile)?.size()
    }

    static List<String> getAllPdfFiles(File folder, String archiveProfile = "") {
        return getAllFiles(folder, archiveProfile, true)
    }

    static List<String> getAllPdfFilesIncludingInIgnoredExtensions(File folder, String archiveProfile = "") {
        return getAllFiles(folder, archiveProfile, true, false)
    }

    static List<String> getAllFiles(File folder, String archiveProfile = "", boolean pdfOnly=false,boolean useIgnoreFileAndFoldersSetting=true) {
        String allowedExtensionsRegex = FileUtil.ALLOWED_EXTENSIONS_REGEX
        List<String> ignoreableExtensions = SettingsUtil.IGNORE_EXTENSIONS
        if ((archiveProfile && EGangotriUtil.IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS.contains(archiveProfile)) || pdfOnly ) {
            ignoreableExtensions = []
            allowedExtensionsRegex = FileUtil.PDF_ONLY_REGEX
        }

        List ignoreableKeywords = useIgnoreFileAndFoldersSetting ? SettingsUtil.IGNORE_FILES_AND_FOLDERS_WITH_KEYWORDS: ["jxuxnxk"]

        List<String> files = []
        Map optionsMap = [type      : FileType.FILES,
                          nameFilter: ~(allowedExtensionsRegex)
        ]
        optionsMap.put("excludeFilter", { File file ->
            ignoreableKeywords*.toLowerCase().stream().anyMatch {
                String ignorableKeyWords -> file.absolutePath.toLowerCase().contains(ignorableKeyWords)
            } ||
                    file.name.startsWith(".") ||
                    !file.name.contains(".") ||
                    ignoreableExtensions.contains(UploadUtils.getFileEnding(file.name).toLowerCase())
        })

        if (!folder.exists()) {
            log.error("$folder doesnt exist. returning")
            return []
        }
        folder.traverse(optionsMap) {
            files << it.absolutePath
        }
        return files.sort()
    }

}

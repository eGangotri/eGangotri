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
        File folder = new File(pickFolderBasedOnArchiveProfile(archiveProfile))
        return getAllFiles(folder, archiveProfile)
    }

    static int getCountOfUploadableItemsForProfile(String archiveProfile) {
        return getUploadablesForProfile(archiveProfile)?.size()
    }

    static List<String> getAllFiles(File folder, String archiveProfile ="") {
        String allowedExtensionsRegex = FileUtil.ALLOWED_EXTENSIONS_REGEX
        List<String> ignoreableExtensions = SettingsUtil.IGNORE_EXTENSIONS
        if(archiveProfile && EGangotriUtil.IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS.contains(archiveProfile)){
            ignoreableExtensions = []
            allowedExtensionsRegex =FileUtil.PDF_ONLY_REGEX
        }

        List<String> files = []
        Map optionsMap = [type      : FileType.FILES,
                          nameFilter: ~(allowedExtensionsRegex)
        ]
        optionsMap.put("excludeFilter", { File file ->
                    SettingsUtil.IGNORE_FILES_AND_FOLDERS_WITH_KEYWORDS*.toLowerCase().stream().anyMatch {
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

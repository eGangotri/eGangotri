package com.egangotri.util

import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Slf4j
class FileUtil {

    static Map getFoldersCorrespondingToProfile() {
        Properties properties = new Properties()
        File propertiesFile = new File(EGangotriUtil.LOCAL_FOLDERS_PROPERTIES_FILE)
        propertiesFile.withInputStream {
            properties.load(it)
        }

        Map profileAndFolder = [:]

        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String val = new String(properties.get(key).getBytes("ISO-8859-1"), "UTF-8")
            if (key.contains(".src")) {
                profileAndFolder.put((key - (".src")), val)
            }
        }
        return profileAndFolder
    }

    static final Map<String, String> ALL_FOLDERS = getFoldersCorrespondingToProfile()

    static final String PRE_CUTOFF = "pre57"
    static String PDF_REGEX = /.*.pdf/

    public static moveDir(String srcDir, String destDir) {
        // create an ant-builder
        def ant = new AntBuilder()
        log.info("Src $srcDir " + "dst: $destDir")

        ant.move(todir: destDir, verbose: 'true', overwrite: 'false', preservelastmodified: 'true') {
            fileset(dir: srcDir) {
                include(name: "**/*.*")
            }
        }
    }
}



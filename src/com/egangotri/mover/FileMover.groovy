package com.egangotri.mover

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import org.slf4j.*

class FileMover {
    static String DEST_ROOT_DIR = "C:\\Treasures6"
    static Map<String, List<String>> srcDestMap
    final static Logger Log = LoggerFactory.getLogger(this.simpleName)

    static List profiles = ["DT", "SR", "JG", "NK"] //"SR", "DT", "JG"

    static main(args) {
        if (args) {
            Log.info "args $args"
            profiles = args.toList()
        }
        new FileMover().move()
    }

    void move() {
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.LOCAL_FOLDERS_PROPERTIES_FILE)
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]

        int index = 1
        profiles.each { profile ->
            String srcDir = metaDataMap["${profile}.src"]
            String destDir = metaDataMap["${profile}.dest"]
            FilenameFilter fileNameFilter = new PDFFileNameFilter()

            int srcFlesCount = new File(srcDir).list(new PDFFileNameFilter()).size()
            int dirFlesCountBeforeMove = new File(destDir).list(new PDFFileNameFilter()).size()
            FileUtil.moveDir(srcDir, destDir);
            int dirFlesCountAfterMove = new File(destDir).list(new PDFFileNameFilter()).size()

            int diff = dirFlesCountAfterMove - dirFlesCountBeforeMove
            String rep = "$srcDir, \t $srcFlesCount,\t $destDir,\t ${diff} \t" + (srcFlesCount == diff ? 'Success' : 'Failure!!!!')
            uploadSuccessCheckingMatrix.put((index++), rep)
        }

        uploadSuccessCheckingMatrix.each { k, v ->
            Log.info "$k) $v"
        }
    }

    class PDFFileNameFilter implements FilenameFilter {

        @Override
        boolean accept(File dir, String name) {
            if (name.endsWith(EGangotriUtil.PDF)) {
                return true
            }
        }
    }
}

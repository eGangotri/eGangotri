package com.egangotri.upload.archive.uploaders


import groovy.util.logging.Slf4j

/**
 * works.
 *
 * expects path a excel file with no header and just the Abs Path:
 * C:\tmp\_data\tmp\Veda Mata Gayatri.pdf
 C:\tmp\_data\tmp\testFolder\Veda Mata Gayatri.pdf
 C:\tmp\_data\tmp\Veda Mata Gayatri - Copy.pdf

 Other fields will be created by using the Profile Metadata
 */

@Slf4j
class UploadToArchiveViaExcelV3WithOneColMulti {
    static void main(String[] args) {
        log.info("UploadToArchiveViaExcelV3WithOneCol.main(${args})")
        if (args.length >= 2) {
            String archiveProfile = args[0]
            String excelFileName = args[1]
            String uploadCycleId = args.length >= 3 ? args[2] : "";
            String range = args.length == 4 ? args[3] : "";
            if (archiveProfile.contains(",")) {
                String[] profiles = archiveProfile.split(",");
                String[] excelFiles = excelFileName.split(",");
                for (int i = 0; i < profiles.size(); i++) {
                    UploadToArchiveViaExcelV3WithOneCol.main(new String[]{profiles[i], excelFiles[i], uploadCycleId, range})
                }
            } else {
                UploadToArchiveViaExcelV3WithOneCol.main(new String[]{archiveProfile, excelFileName, uploadCycleId, range})
                log.info("Profiles and Excel Files are not equal. Exiting")
            }
        } else {
            log.info "Must have 3-4 arg.s Profile name and fileName(s) of pdf as PERCENT Sign Separated. Optional Range"
        }
    }
}


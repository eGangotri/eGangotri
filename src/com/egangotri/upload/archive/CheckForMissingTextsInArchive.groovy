package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the $HOME_DRECTORY/eGangotri Folder, or
 * use VM Argument -Dwebdriver.chrome.driver=c:/chromedriver.exe

 */
@Slf4j
class CheckForMissingTextsInArchive {

    static main(args) {
        List archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            log.info "args $args"
            archiveProfiles = args.toList()
        }

        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        execute(archiveProfiles, metaDataMap)
    }

    static boolean execute(List profiles, Map metaDataMap) {
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        log.info "Start uploading to Archive"

        List list = ['1059_Netra', '1089_Ishvara', '1094.1_Ishvar', '1094.3,4_Lalla', '1103_Spanda', '1139_Shiva', '1142_Ishvar', '1146_Ishvara', '1156.1_Shiva', '1161_Ishvara', '1176_Shiva', '1189_Vigyan', '1209.2_Utpal', '1260.2-3_Paramartha', '1290.2_Goraksha', '1329.1_Shiva', '1335_Lalla', '1342.4_Stavaraja', '1352_Tantraloka', '1362.1_Shiva', '1362.2_Jyotish', '1372.3_Ramanama', '1375.6_Siddhanta', '1377.12-16_Pratyangira', '1377.17-21_Stuti', '1377.1_Maharajni', '1377.2-4_Devi', '1377.5-6_Devi', '1377.5_Devi', '1377.8-11_Rajni', '1384_Sharada', '1409.13_Shyama', '1409.14-16_Bala', '1409_Yakshini', '1411_Shiva', '1423_Spanda', '1459.4_Shatpadi', '1477_Ishvar', '1483.4_Spanda', '1483_Utpal', '1518.1_Vigyan', '1519_Malinin', '1521_Neta', '1540.2-4_Dehastha', '1540.8_Tantra', '1560.5-6_Paramarchan', '1560.7-8_Para', '1563_Svatmopalabdhi', '1586.19-22_Hara', '1586.3_Pratyag', '1586.5-7_Bhavopahara', '1586.8-14_Chitta', '1609_Spanda', '1616_Shiva', '1653.1_Tantrasara', '1657_Krama', '1661.2_Vigyan', '1661.8_Spanda', '1662_Ishvara', '1672_Ishvara', '1674_Spanda', '1681_Vigyan', '1696.2_Ishvara', '1696.4_Spanda', '1696.5_Samba', '1740.7_Utpal', '1740.9_Mahartha', '1742.9_Spanda', '1742_Deva', '1792_Tantraloka', '1804', '1804.1-8_Ramanama', '1876_Shiva', '1892_Shiva', '1939_Shiva', '1977.2_Sharika', '2048_Panchastavi', '2051_Mukunda', '2078_Utpal', '2081_Tantraloka', '2145_Vijnana', '2155.1_Stotravali', '2201_Tantraloka', '2232_Spanda', '2250_Ishvara', '2256_Mahanaya', '2260_Tantra', '2263_Mahanaya', '2299_Utpal', '2302.5-6_Utpal', '2303_Utpal', '2307.1_Utpal', '2310_Malinin', '2312_Para', '2314_Utpal', '2321_Vigyan', '2331_Ishvara', '2333_Para', '2334_Netra', '2346_Krama', '2347_Chitta', '2348.1-3_Anuttara', '2352_Pratyabhijna', '2354_Mahanaya', '2360_Para', '2368_Bodha', '2369_Vigyan', '2371.1_Anandeshwar', '2389_Bodha', '2403_Ishvar', '2416_Kali', '2426.5_Jnanarnava', '2464_Kali', '2473.3_Paramartha', '2498_Tantra', '2539_Vairagya', '2550_Tantraloka', '2562_Pratyabhjna']
        ArchiveHandler.checkForMissingUploadsInArchive("https://archive.org/details/@mcintoshskipperroth",  list)
        log.info "Upload Report:\n"

        uploadSuccessCheckingMatrix.each { k, v ->
            log.info "$k) $v"
        }

        log.info "***Browser for Archive Upload Launches Done"
        return true
    }
}



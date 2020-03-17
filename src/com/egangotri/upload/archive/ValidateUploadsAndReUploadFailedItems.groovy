package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.util.ValidateLinksUtil
import com.egangotri.upload.vo.ItemsVO
import com.egangotri.upload.vo.LinksVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

import static com.egangotri.upload.util.ArchiveUtil.storeQueuedItemsInFile

@Slf4j
class ValidateUploadsAndReUploadFailedItems {
    static Set archiveProfiles = []
    static File identifierFile = null
    static File queuedFile = null
    static List<LinksVO> identifierLinksForTesting = []
    static List<ItemsVO> queuedItemsForTesting = []
    static List<LinksVO> failedLinks = []
    static List<ItemsVO> missedOutQueuedItems = []
    static List<? extends UploadVO> allFailedItems =  []


    static main(args) {
        EGangotriUtil.recordProgramStart("ValidateUploadsAndReUploadFailedItems")
        setCSVsForValidation(args)
        ArchiveUtil.ValidateUploadsAndReUploadFailedItems = true
        SettingsUtil.applySettings()
        execute()
    }

    static void execute(){
        processUsheredCSV()
        processQueuedCSV()
        findQueueItemsNotInUsheredCSV()
      //filterFailedUsheredItems()
        //for use in special cases only
        generateFailedLinksFromStaticList()
        combineAllFailedItems()
        startReuploadOfFailedItems()
        System.exit(0)
    }
    static void runForQuickTestOfMissedQueueItemsOnlyWithoutUploding(){
        EGangotriUtil.recordProgramStart("ValidateUploadsAndReUploadFailedItems")
        setCSVsForValidation(null)
        ArchiveUtil.ValidateUploadsAndReUploadFailedItems = true
        SettingsUtil.applySettings()
        SettingsUtil.IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=false
        SettingsUtil.IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=true
        SettingsUtil.ONLY_GENERATE_STATS_IN_REUPLOAD_FAILED_ITEMS=true
        execute()

    }
    static void setCSVsForValidation(def args) {
        identifierFile = new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER).listFiles()?.sort { -it.lastModified() }?.head()
        queuedFile = new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER).listFiles()?.sort { -it.lastModified() }?.head()

        if (!identifierFile) {
            log.error("No Files in ${EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }

        if (!queuedFile) {
            log.error("No Files in ${EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }
        if (args) {
            println "args $args"
            if (args?.size() > 2) {
                log.error("Only 2 File Name(s) can be accepted.Cannot proceed. Quitting")
                System.exit(0)
            }
            String _file_1 = args.first().endsWith(".csv") ? args.first() : args.first() + ".csv"
            String _file_2 = args.last().endsWith(".csv") ? args.last() : args.last() + ".csv"
            identifierFile = new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER + File.separator + _file_1)
            queuedFile = new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER + File.separator + _file_2)
            if (!identifierFile) {
                log.error("No such File ${identifierFile} in ${EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
            if (!queuedFile) {
                log.error("No such File ${queuedFile} in ${EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
        }
        println("Identifier File for processing: ${identifierFile.name}")
        println("Queue File for processing: ${queuedFile.name}")
    }

    static void processUsheredCSV() {
        identifierLinksForTesting = ValidateLinksUtil.csvToUsheredItemsVO(identifierFile)
        archiveProfiles = identifierLinksForTesting*.archiveProfile as Set
        log.info("Converted " + identifierLinksForTesting.size() + " links of upload-ushered Item(s) from CSV in " + "Profiles ${archiveProfiles.toString()}")
    }

    static void processQueuedCSV() {
        queuedItemsForTesting = ValidateLinksUtil.csvToItemsVO(queuedFile)
        Set queuedProfiles = queuedItemsForTesting*.archiveProfile as Set
        log.info("Converted " + queuedItemsForTesting.size() + " Queued Item(s) from CSV in " + "Profiles ${queuedProfiles.toString()}")
    }

    // Thsi function produces QueuedItem - IdentifierGeneratedItem
    //Queued Item is a superset of IdentifierGeneratedItem
    static void findQueueItemsNotInUsheredCSV() {
        if(SettingsUtil.IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS){
            log.info("Queued Items will be ignored for upload")
            return
        }
        List allFilePaths = identifierLinksForTesting*.path
        log.info("Searching from ${queuedItemsForTesting?.size()} Queued Item(s) that were never upload-ushered in ${allFilePaths.size()} identifiers")

        queuedItemsForTesting.each { queuedItem ->
            if (!allFilePaths.contains(queuedItem.path)) {
                missedOutQueuedItems << queuedItem
                log.info("\tFound missing Item [${queuedItem.archiveProfile}] ${queuedItem.title} ")
            }
        }
        log.info("${missedOutQueuedItems.size()}/${queuedItemsForTesting.size()} Items found in Queued List that missed upload. Affected Profies "  +  (missedOutQueuedItems*.archiveProfile as Set).toString())
    }

    static void filterFailedUsheredItems() {
        if(SettingsUtil.IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS){
            log.info("Ushered Items will be ignored for upload")
            return
        }
        int testableLinksCount = identifierLinksForTesting.size()
        log.info("Testing ${testableLinksCount} Links in archive for upload-success-confirmation")

        identifierLinksForTesting.eachWithIndex { LinksVO entry, int i ->

            try {
                entry.archiveLink.toURL().text
                print("${i},")
            }
            catch (FileNotFoundException e) {
                failedLinks << entry
                println("Failed Link(${failedLinks.size()} of $testableLinksCount) !!! @ ${i}..")
            }
            catch (Exception e) {
                log.error("This is an Unsual Error. ${entry.archiveLink} Check Manually" + e.message)
                e.printStackTrace()
                failedLinks << "'${entry}'"
            }
            if(i%75 == 0){
                Thread.sleep(5000)
                println("")
            }
        }
        log.info("\n${failedLinks.size()} failedLink" + " Item(s) found in Ushered List that were missing." +
                " Affected Profie(s)" +  (failedLinks*.archiveProfile as Set).toString())
        log.info("Failed Links: " + failedLinks*.archiveLink.toString())
    }


    //This static variable can only be used with generateFailedLinksFromStaticList()
    static  List<String> _staticListOfBadLinks = ["https://archive.org/details/paribhashaindushekaranageshbhattachandrikabalbodhinied.sripadasatynaraynamurthir_374_T","https://archive.org/details/paribhashaindushekaranageshbhattavakyarthachandrikaharisastribhagavated.kallurkamashastri_741_C","https://archive.org/details/phitsutraofshantanavaacharyafranzkeilhorn1866_493_Z","https://archive.org/details/prakriyakaumudiramachandraprakasaed.srikrishnamuralidharmisravol1sampoornanaduniversity1977_749_S","https://archive.org/details/prakriyakaumudiramachandraprakasaed.srikrishnamuralidharmisravol1sampoornanaduniversity2000_685_p","https://archive.org/details/prakriyakaumudiramachandraprakasaed.srikrishnamuralidharmisravol2sampoornanaduniversity1977_359_f","https://archive.org/details/prakriyakaumudiramachandraprakasaed.srikrishnamuralidharmisravol2sampoornanaduniversity2000_876_X","https://archive.org/details/prakriyakaumudiramachandraprakasaed.srikrishnamuralidharmisravol3sampoornanaduniversity1955_662_O","https://archive.org/details/prakriyakaumudiramachandraprakasaed.srikrishnamuralidharmisravol3sampoornanaduniversity1980_965_w","https://archive.org/details/prakriyakaumudiramachandraprakasaed.srikrishnamuralidharmisravol3sampoornanaduniversity2000_450_d","https://archive.org/details/prakriyakaumudiramachandraprasadavitthalakamalashankarpranshankartrivedipart1bss_833_F","https://archive.org/details/prakriyakaumudiramachandraprasadavitthalakamalashankarpranshankartrivedipart2bss_915_M","https://archive.org/details/prakriyakaumudiramachandraprasadaofvitthalakamalashankarpranshankartrivedipart1bssalt_970_m","https://archive.org/details/prakriyakaumudiramachandraprasadaofvitthalakamalashankarpranshankartrivedipart2bssalt_57_z","https://archive.org/details/prakriyasarvasvanarayanabhattaed.kunhanrajac.universityofmadrasms_756_U","https://archive.org/details/prakriyasarvasvanaryanbhattaed.madhavanuniim.vol5universityoftravancore258_599_h","https://archive.org/details/praudamanoramabrihadbruhadsabdaratnaharidiksitalaghusabdaratnanagesabhattasitara_456_i","https://archive.org/details/praudamanoramakuchamadhinijagannathapanditprabhavibharatnadipaikasadashivasharmashastrichowk_525_w","https://archive.org/details/praudamanoramalaghusabdaratnaharidiksitabharavibhavaprakasasaralaed.bhairavamish_875_u","https://archive.org/details/praudamanoramalaghusabdaratnaharidiksitaed.ratnagopalabhattachowkambha_470_K","https://archive.org/details/praudamanoramasabdaratnaharidiksitabharavisabdaratnapradipikatikamadhavsastribha_150_V","https://archive.org/details/praudamanoramatathamanoramatikapothioroblong_177_q","https://archive.org/details/purvapakshavalied.unkownchowkambha_977_o","https://archive.org/details/readeronthesanskritgrammariansfritsstaalarticles_782_j","https://archive.org/details/sabdaapasabdavivekacharudevashastrinagpublications_852_c","https://archive.org/details/sabdaartharatnataranathatarkavachaspatibhattacharya1902_541_T","https://archive.org/details/sabdabodhamimamsaramanujatatacharyan.s.vol1frenchinstitute_444_g","https://archive.org/details/sabdabodhamimamsaramanujatatacharyan.s.vol2frenchinstitute_229_","https://archive.org/details/sabdabodhamimamsaramanujatatacharyan.s.vol3frenchinstitute_228_B","https://archive.org/details/sabdachandrikalakshimidharaed.kamalashankarpranshankartrivedibssprakrit_357_P","https://archive.org/details/sabdaindushekarsadashivabhattitikapothioroblong_389_","https://archive.org/details/sabdaindushekarchandrakaloodaraonsabdaindushekara_723_j","https://archive.org/details/sabdaindushekaravishamibhashyaraghavendracharyapothioroblonog_379_e","https://archive.org/details/sabdakaustubabhattojidikshitpothioroblong_633_J","https://archive.org/details/sabdakaustubabhattojidikshited.ganapatisastrimokatevindyesvariprasadadvivedivol2_129_n","https://archive.org/details/sabdakaustubabhattojidikshited.gopalshastrinenevol1chowkambha1adhyaya1pada_528_v","https://archive.org/details/sabdakaustubabhattojidikshited.gopalshastrinenevol2chowkambha2padaof1adhaya2pada3adhyaya_728_o","https://archive.org/details/sabdakaustubabhattojidikshited.ramakrishnashastrivol1chowkambha_466_M","https://archive.org/details/sabdakaustubabhattojidikshited.ramakrishnashastrivol2chowkambha_675_t","https://archive.org/details/sabdakaustubabhattojidikshited.ramakrishnashastrivol3chowkambha_92_F","https://archive.org/details/sabdakaustubabhattojidikshitvisamapadavyakhyanagesabhattajamesbenson_72_r","https://archive.org/details/sabdaratnataranathatarkavachaspatibhattacharyavyakarna_191_y","https://archive.org/details/sabdataranginisubrahmanyasastriv.sanskriteducationsocietymadras_332_d","https://archive.org/details/sadashivabhattivyakarna......................................................................._612_o","https://archive.org/details/sakatayanadhatupathacoverpagesmissing_624_","https://archive.org/details/sakatayanavyakaranachintamanivrittipanditedition_176_","https://archive.org/details/samskrtasanskritbhasavigyanaramadhinchaturvedichowkambha_495_","https://archive.org/details/sandhichandrikaramchandrajhachowkambha1952hindi_599_r","https://archive.org/details/sandhichandrikaramchandrajhachowkambha1977hindi_897_v","https://archive.org/details/sandhitheoreticalphoneticandhistoricalbasisofwordjuctioninsanskritsidneyallenw..","https://archive.org/details/sanskritgrammarmanuscriptsoffatherheinrichroths.j.16101668arnufcampsjeanclaudmullerbrill_940_O","https://archive.org/details/sanskritlanguageitsgrammarmadhavdeshpandem._952_K","https://archive.org/details/sanskritsyntaxgrammarofcasebrahmacharisurendrakumarksduniversitydarbangh_354_F","https://archive.org/details/sanskritvyakarandarsanramsureshtripathihindi_131_N","https://archive.org/details/sanskritvyakaranmenganapathkiparamparaauracharyapaninikapildevsastri1961_46_t","https://archive.org/details/sanskritvyakarnashastraitihasyudhistiramimamsakapart1_654_T","https://archive.org/details/sanskritvyakarnashastraitihasyudhistiramimamsakapart2_619_","https://archive.org/details/sanskritvyakarnashastraitihasyudhistiramimamsakapart3_151_E","https://archive.org/details/saramanjarijayakrishnatarkalankarawithcommentaryjivanandvidyasagara1935_51_J","https://archive.org/details/saraswatikanthibharanabhojachitraprakarnatikaratnerswarapandita_494_r","https://archive.org/details/saraswatikanthibharanabhojaanundoramborooah1884_728_","https://archive.org/details/saraswatikanthibharanabhojabhashyaramasimhajagaddharakedarnathsharmawasudevlaxma_934__","https://archive.org/details/saraswatikanthibharanabhojachintamanit.r.universityofmadrasms_449_f","https://archive.org/details/saraswatikanthibharanabhojaed.dravidaviresvarshastri4and5parichheda_613_F","https://archive.org/details/saraswatikanthibharanabhojaed.ramaswamisastriv.a.part4universityoftravancore154_673_K","https://archive.org/details/saraswatikanthibharanabhojaed.samabasivasastrik.part1universityoftravancore117_144_r","https://archive.org/details/saraswatikanthibharanabhojaed.samabasivasastrik.part2universityoftravancore127_100_","https://archive.org/details/saraswatikanthibharanabhojaed.samabasivasastrik.part3universityoftravancore140_31_v","https://archive.org/details/saraswatikanthibharanabhojaratnadarpanaratnesvaraed.kameshwarnathamishrapart1chowkambha_267_h","https://archive.org/details/saraswatikanthibharanabhojaratnadarpanaratnesvaraed.kameshwarnathamishrapart2chowkambha_313_C","https://archive.org/details/saraswatikanthibharanabhojaratnadarpanavyakhyaratnesvari_781_E","https://archive.org/details/saraswatikanthibharanabhojaratnesvarjagadharatikaed.visvanathbhattacharyabenarashinduuniversity_587_","https://archive.org/details/saraswatikanthibharanabhojaratnesvariratnesvaraandjivanandvidyasagar1894alt1_677_q","https://archive.org/details/saraswatikanthibharanabhojaratnesvariratnesvaraandjivanandvidyasagar1894alt_121_N","https://archive.org/details/saraswatikanthibharanabhojaratnesvariratnesvaraandjivanandvidyasagar1894_536_J","https://archive.org/details/saraswatikanthibharanabhojaonpoeticsed.sundarisiddharthavol1ignca_353_Z","https://archive.org/details/saraswatikanthibharanabhojaonpoeticsed.sundarisiddharthavol2ignca_704_K","https://archive.org/details/saraswatikanthibharanabhojaonpoeticsed.sundarisiddharthavol3ignca_242_T","https://archive.org/details/laghubhasyaonsaraswatavyakaranaofraghunathanagared.vamshidharavenkateswarasteampress1957_818_U","https://archive.org/details/sarasvatavyakarnaedshivadattashastri_177_n","https://archive.org/details/sarasvatavyakarnaguruprasadshastripart1_182_b","https://archive.org/details/sarasvatavyakarnatikaofkashirampathakvenkateswarasteampress_462_M","https://archive.org/details/sarasvatavyakarnawithvivarnaofmadhavatippaniofnanakramvenkateswarasteampress_293_G","https://archive.org/details/saraswatavyakaranabalabodhiindumatinaraharishastripendaseramachandrajhachowkambha_974_b","https://archive.org/details/saraswatavyakaranalaghubhashyavyakhyaragunathnagarvenkateswarasteampresssaraswati_323_b","https://archive.org/details/saraswatavyakaranasubodhikachandrakirtiprasadavausdevabhattinavakishorakarasharmavol1chowkambha_811_m","https://archive.org/details/saraswatavyakaranasubodhikachandrakirtiprasadavausdevabhattinavakishorakarasharmavol2chowkambha_828_","https://archive.org/details/saraswatavyakaranabyanaubhtisvarupacharyajivanandvidyasagarjivanandvidyasagar_832_","https://archive.org/details/saraswatavyakaranabyanaubhtisvarupacharyasubodhikaofchandrakirtinirnayasagarpress_829_U","https://archive.org/details/shabdaratnakarasadhusudharnaganied.haragovinddasbechardas_182_","https://archive.org/details/shridharishridharashastrilaghusabdaindusekaraed.dravidesvarashastri_83_Q","https://archive.org/details/siddhantachintamanivyakarnaprinciplesrulesindarsanaramprasadtripartisampoornanad","https://archive.org/details/4990010095964sikshadivedapandagan.a.446preligion.theologysanskrit0_14_O","https://archive.org/details/fouranusvarasoftaittiriyashakhashriramanasharma_291_q","https://archive.org/details/foursvarasoftaittiriyashakhashriramanasharma_680_Y","https://archive.org/details/katis39u-lipis39u-sam39skr39tam39-likhyate_612_x","https://archive.org/details/l39kaaratattvam39-nihitam39-guhaayaam_739_M","https://archive.org/details/raast39riya-shabda-saadhutvam_956_","https://archive.org/details/sam39skr39te-ardha-ekaarah39-okaarah39_500_t","https://archive.org/details/shiksaa-prathama-klptih_202003_436_o","https://archive.org/details/bharadvajasiksaramachandradikshitarv.r.sundaramayyarp.s.bori_960_V","https://archive.org/details/naradiyasiksasamavediyasatyavratasamasiromani1890_29_l","https://archive.org/details/naradiyasiksatikaofbhattashobhakaraed.datiyaswamipitambratrust_23_P","https://archive.org/details/naradiyasiksawithhinditika_567_o","https://archive.org/details/naradiyasiksawithcommentaryofbhattasobhakarapitambaraswami_746_R","https://archive.org/details/paninisiksamanmohangosh_980_","https://archive.org/details/paniniyasiksabacchulalavastibalakrishnasharmasantoshpandey......_137_","https://archive.org/details/paniniyasikshawithcommentarybechanramtripathi1877_234_s"]

    /** This method is used in unique cases.
     *  Where u have a list of failed Archive Urls and you want to use them to reupload them only
     * So u take the links copy paste to _staticListOfBadLinks ,
     * have following settings:
     * IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=true
     * IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=false
     * ONLY_GENERATE_STATS_IN_REUPLOAD_FAILED_ITEMS=false
     *
     * comment out call to filterFailedUsheredItems()
     * uncomment call to generateFailedLinksFromStaticList() and execute tehe program
     *
     * .
     * Then upload the VOS
     */
    static void generateFailedLinksFromStaticList(){
        log.info("generating vos from static list of Links with size: " + _staticListOfBadLinks.size())

        identifierLinksForTesting.eachWithIndex{ LinksVO entry, int i ->
            if(_staticListOfBadLinks*.trim().contains(entry.archiveLink)){
                println("entry.uploadLink: " + entry.uploadLink.replace("=eng", "=san"))
                failedLinks << entry
            }
        }
    }
    static void combineAllFailedItems(){
        if (missedOutQueuedItems || failedLinks) {
            allFailedItems = missedOutQueuedItems

            failedLinks.each { failedLink ->
                allFailedItems.add(failedLink)
            }
            log.info("Combined figure for re-uploading(${missedOutQueuedItems.size()} + ${failedLinks.size()}) :" + allFailedItems.size() + " in Profiles: ${allFailedItems*.archiveProfile as Set}" )
        }
    }

    static void startReuploadOfFailedItems() {
        if(SettingsUtil.ONLY_GENERATE_STATS_IN_REUPLOAD_FAILED_ITEMS){
            log.info("Only stats generated. No Uploading due to Setting")
            return
        }
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        Set<String> profilesWithFailedLinks = allFailedItems*.archiveProfile as Set
        Set<String> purgedProfilesWithFailedLinks = ArchiveUtil.purgeBrokenProfiles(profilesWithFailedLinks, metaDataMap)

        ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = allFailedItems.size()
        if(ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION > EGangotriUtil.MAX_UPLODABLES){
            log.info("Uploadable Count ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} exceeds ${EGangotriUtil.MAX_UPLODABLES}. Canoot proceed. Quitting")
            System.exit(1)
        }
        log.info("Total Uploadable Count for Current Execution ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}")

        int attemptedItemsTotal = 0

        purgedProfilesWithFailedLinks*.toString().eachWithIndex { archiveProfile, index ->
            List<UploadVO> failedItemsForProfile = allFailedItems.findAll { it.archiveProfile == archiveProfile }
            int countOfUploadableItems = failedItemsForProfile.size()
            log.info "${index + 1}). Starting upload in archive.org for Profile $archiveProfile. Total Uplodables: ${countOfUploadableItems}"
            if (countOfUploadableItems) {
                log.info "getUploadablesForProfile: $archiveProfile: ${countOfUploadableItems}"
                storeQueuedItemsInFile(failedItemsForProfile)
                List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(metaDataMap, failedItemsForProfile)
                String report = UploadUtils.generateStats([uploadStats], archiveProfile, countOfUploadableItems)
                uploadSuccessCheckingMatrix.put((index + 1), report)
                attemptedItemsTotal += countOfUploadableItems
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }

        EGangotriUtil.recordProgramEnd()
        ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, attemptedItemsTotal)
    }
}

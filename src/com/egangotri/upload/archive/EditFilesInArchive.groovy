package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver

import static com.egangotri.upload.util.ArchiveUtil.getResultsCount

@Slf4j
class EditFilesInArchive {
    static int MAX_ITEMS_RETRIEVABLE = 10
    static void main(String[] args) {
        List<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            log.info "args $args"
            archiveProfiles = args.toList()
        }
        EGangotriUtil.recordProgramStart("eGangotri Archive Logger")
        def metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        if(archiveProfiles.last().isInteger()){
            MAX_ITEMS_RETRIEVABLE = archiveProfiles.last().toInteger()
            archiveProfiles = archiveProfiles.take(archiveProfiles.size()-1);
        }
        log.info "archiveProfiles $archiveProfiles ${MAX_ITEMS_RETRIEVABLE}"

        archiveProfiles.each { String archiveProfile ->
            log.info "Logging for Profile $archiveProfile"
            ChromeDriver driver = new ChromeDriver()
            if(ArchiveUtil.navigateLoginLogic(driver, metaDataMap, archiveProfile)){
                getResultsCount(driver, true)
                UploadUtils.maximizeBrowser(driver)
                def attributeList =
                        driver.findElements(By.xpath("//div[@class='item-ia hov']"))
                                *.getAttribute("data-id")
                if(MAX_ITEMS_RETRIEVABLE== -1) {
                    MAX_ITEMS_RETRIEVABLE = attributeList.size()
                }
                else{
                    MAX_ITEMS_RETRIEVABLE = attributeList.size()>MAX_ITEMS_RETRIEVABLE
                            ?MAX_ITEMS_RETRIEVABLE:attributeList.size()
                };

                log.info "MAX_ITEMS_RETRIEVABLE ${MAX_ITEMS_RETRIEVABLE}"
                attributeList?.take(MAX_ITEMS_RETRIEVABLE)?.forEach( dataId -> {
                    try {
                        String editableUrl = "https://archive.org/editxml/${dataId}"
                        log.info("editableUrl ${editableUrl}")
                        UploadUtils.openNewTab(driver)
                        UploadUtils.switchToLastOpenTab(driver)
                        driver.navigate().to(editableUrl)
                        driver.get(editableUrl)
                        doFollowingEditTasks(driver)
                        }
                    catch(Exception e){
                        log.info("exception handling dataId ${dataId}",e)
                    }
                })
            }
        }
        EGangotriUtil.recordProgramEnd()
        System.exit(0)
    }

    /**
     * provide customized tasks for specific work
     * @param driver
     * @return
     */
    static def doFollowingEditTasks(ChromeDriver driver){
        task1(driver)
    }
    static def task1(ChromeDriver driver){
        def element = driver.findElementByName('field_default_subject')
        element.clear()
        //element.sendKeys("Colllection")
        //driver.findElementByClassName("btn-archive").click()
    }

}


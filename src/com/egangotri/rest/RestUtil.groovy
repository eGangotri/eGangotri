package com.egangotri.rest

import com.egangotri.upload.util.SettingsUtil
import groovy.util.logging.Slf4j
import org.apache.groovy.json.internal.LazyMap

import static groovyx.net.http.HttpBuilder.configure
import static groovy.json.JsonOutput.toJson

@Slf4j
class RestUtil {
    static String ITEMS_QUEUED_PATH = "itemsQueued"
    static String ITEMS_USHERED_PATH = "itemsUshered"

    static makePostCall(String path,
                        Map body,
                        String backendServer = SettingsUtil.EGANGOTRI_BACKEND_SERVER) {
        body.put("superadmin_user", SettingsUtil.EGANGOTRI_BACKEND_SUPERADMIN_USER)
        body.put("superadmin_password", SettingsUtil.EGANGOTRI_BACKEND_SUPERADMIN_PASSWORD)
        def posts
        try {
            posts = configure {
                request.uri = backendServer
                request.uri.path = path
                request.contentType = 'application/json'
                request.body = toJson(body)
            }.post()
            log.info("posts ${posts}")
        }
        catch (Exception e) {
            log.info("""makePostCall Error while calling ${backendServer}${path}
                        ${toJson(body)}""", e)
        }
        return posts
    }

    static def makeGetCall(String path, Map queryMap = [name: 'Bob'], String backendServer = SettingsUtil.EGANGOTRI_BACKEND_SERVER) {
        try {
            def doGet = configure {
                request.uri = backendServer
            }.get {
                request.uri.path = path
                request.uri.query = queryMap ?: [:]
            }
            log.info("doGet ${doGet}")
            return doGet
        }
        catch (Exception e) {
            log.info("MakeGetCall Error while calling ${backendServer}/${path}", e.message)
            return null
        }
    }

    static boolean startDBServerIfOff() {
        String mongoServerExecScript = "./bat_files/startMongoApiServer.bat"
        if(SettingsUtil.ENV_TYPE.equalsIgnoreCase(SettingsUtil.ENV_DEV)){
            if (!checkIfDBServerIsOn()) {
                log.info("Starting DB Server")
                Runtime.getRuntime().exec(mongoServerExecScript)
                Thread.sleep(10000)
            }
        }
    }

    static boolean startDashboardServerIfOff() {
        String mongoServerExecScript = "./bat_files/startDashboardServer.bat"
        if (!checkIfDashboardServerIsOn()) {
            log.info("Starting DB Server")
            Runtime.getRuntime().exec(mongoServerExecScript)
            Thread.sleep(30000)
        }
    }

    static boolean checkIfDBServerIsOn() {
        try {
            LazyMap result = makeGetCall("/") ?: [:] as Map
            log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
            return (result && result.containsKey("response")) ? result.response == "eGangotri-node-backend" : false
        }
        catch (ConnectException e) {
            log.error(e.message)
            return false;
        }
    }

    static boolean checkIfDashboardServerIsOn() {
        try {
            LazyMap result = makeGetCall("/") ?: [:] as Map
            log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
            return (result && result.containsKey("response")) ? result.response == "eGangotri-node-backend" : false
        }
        catch (ConnectException e) {
            log.error(e.message)
            return false;
        }
    }

    static def listQueues() {
        LazyMap result = makeGetCall("/${ITEMS_QUEUED_PATH}/list") ?: [:]
        log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
        return (result && result.containsKey("response")) ? result.response : []
    }

    static void main(String[] args) {
        log.info("checkIfDBServerIsOn ${checkIfDBServerIsOn()}")
        log.info("listQueues ${listQueues()}")
        log.info("End of RestUtil")
    }
}

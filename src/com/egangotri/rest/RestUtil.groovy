package com.egangotri.rest

import com.egangotri.upload.util.SettingsUtil
import groovy.util.logging.Slf4j
import org.apache.groovy.json.internal.LazyMap

import static groovyx.net.http.HttpBuilder.configure
import static groovy.json.JsonOutput.toJson
import groovyx.net.http.RESTClient

@Slf4j
class RestUtil {
    static String ITEMS_QUEUED_PATH = "itemsQueued"
    static String ITEMS_USHERED_PATH = "itemsUshered"
    static String UPLOAD_CYCLE_ROUTE = "uploadCycleRoute"
    static String backendServer = SettingsUtil.EGANGOTRI_BACKEND_SERVER
    static RESTClient restClient = new RESTClient(backendServer)

    static makePostCall(String path,
                        Map requestData) {

        requestData.put("superadmin_user", SettingsUtil.EGANGOTRI_BACKEND_SUPERADMIN_USER)
        requestData.put("superadmin_password", SettingsUtil.EGANGOTRI_BACKEND_SUPERADMIN_PASSWORD)

        def jsonRequestBody = new groovy.json.JsonBuilder(requestData).toPrettyString()

        // Set the 'Content-Type' header to specify JSON
        def headers = ['Content-Type': 'application/json']
        def response;
        try {
            // Make a POST request with JSON data
            response = restClient.post(path: path,
                    body: jsonRequestBody,
                    requestContentType: groovyx.net.http.ContentType.JSON,
                    headers: headers)

            println "Response Code: ${response.status}"
            println "Response Data: ${response.data}"
        }
        catch (Exception e) {
            log.info("""makePostCall Error while calling ${backendServer}${path}
                        ${toJson(requestData)}""", e)
        }
        return response.data
    }

    static def makeGetCall(String path, Map queryMap = [name: 'Bob'], String backendServer = SettingsUtil.EGANGOTRI_BACKEND_SERVER) {
        log.info("backendServer ${backendServer} path ${path}")
        try {
            def response = restClient.get(path: path)
            println "Response Code: ${response.status}"
            println "Response Data: ${response.data}"
            def dataMap = ['response': response.data['response']]
            return dataMap
        }
        catch (Exception e) {
            log.info("MakeGetCall Error while calling ${backendServer}${path} ${e.message}", e)
            return null
        }
    }

    static boolean startDBServerIfOff() {
        try {
            log.info("startDBServerIfOff for env:${SettingsUtil.ENV_TYPE}")
            String mongoServerExecScript = ".\\bat_files\\startMongoApiServer.bat"

            if (SettingsUtil.ENV_TYPE.equalsIgnoreCase(SettingsUtil.ENV_DEV)) {
                if (!checkIfDBServerIsOn()) {
                    log.info("startDBServerIfOff:Starting DB Server")
                }
            }
        }
        catch (Exception ex) {
            log.error(ex, "Couldnt start Mongo Server");
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
        boolean _result = false
        String dbServerSuccessMsg = "eGangotri-node-backend (egangotri_upload_db)"
        try {
            LazyMap result = makeGetCall("/") ?: [:] as Map
            _result = (result && result.containsKey("response")) ? result.response == dbServerSuccessMsg : false
            log.info("toJson.response ${result && result.containsKey("response") ? result.response : '--'}")
        }
        catch (ConnectException e) {
            log.error(e.message, "checkIfDBServerIsOn exception thrown")
            _result = false;
        }
        log.info("checkIfDBServerIsOn ${_result}");
        return _result;
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
